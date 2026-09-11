package com.javalive.backend.service.plan;

import com.javalive.backend.dto.dashboard.DashboardActivitySummary;
import com.javalive.backend.dto.plan.InvestmentDetail;
import com.javalive.backend.dto.plan.InvestmentSummary;
import com.javalive.backend.dto.plan.PlanSummary;
import com.javalive.backend.dto.plan.PurchasePlanRequest;
import com.javalive.backend.dto.plan.WithdrawProfitRequest;
import com.javalive.backend.entity.Investment;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.Plan;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.InvestmentRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.PlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.util.MoneyFormat;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Canonical "legacy" Plans/Investment system per docs/CANONICAL-MODULES.md — mirrors
 * UserInvPlanController@joininvestmentplan/cancelPlan and PlanProfitController@withdrawProfit.
 * The modern Plan/UserPlan/PlanPayout system was retired during the Phase 1 migration.
 */
@Service
public class PlanService {

    private static final List<String> INVESTMENT_ACTIVITY_TYPES = List.of("ROI", "Profit Withdrawal");

    private final PlanRepository planRepository;
    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final SettingsService settingsService;

    public PlanService(PlanRepository planRepository, InvestmentRepository investmentRepository,
                        UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository,
                        MailService mailService, NotificationService notificationService, SettingsService settingsService) {
        this.planRepository = planRepository;
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public List<PlanSummary> listPlans(String type) {
        return planRepository.findByTypeIgnoreCaseAndActiveTrue(type == null || type.isBlank() ? "Main" : type)
                .stream().map(PlanSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InvestmentSummary> myInvestments(Long userId, String active) {
        List<Investment> investments = (active == null || active.isBlank() || "All".equalsIgnoreCase(active))
                ? investmentRepository.findByUserIdOrderByIdDesc(userId)
                : investmentRepository.findByUserIdAndActiveOrderByIdDesc(userId, active);
        return investments.stream().map(InvestmentSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public InvestmentDetail investmentDetail(Long userId, Long investmentId) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        List<DashboardActivitySummary> transactions = ledgerTransactionRepository
                .findByInvestmentIdAndTypeInOrderByIdDesc(investmentId, INVESTMENT_ACTIVITY_TYPES)
                .stream().map(DashboardActivitySummary::from).toList();
        return new InvestmentDetail(InvestmentSummary.from(investment), transactions);
    }

    @Transactional
    public InvestmentSummary purchase(Long userId, PurchasePlanRequest request) {
        User user = findUser(userId);
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Investment plan not found."));
        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This plan is no longer available.");
        }

        BigDecimal price = request.amount() != null ? request.amount() : plan.getPrice();
        if (plan.getMinPrice() != null && price.compareTo(plan.getMinPrice()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "The minimum investment for " + plan.getName() + " is " + user.getCurrencySymbol() + MoneyFormat.of(plan.getMinPrice()) + ".");
        }
        if (plan.getMaxPrice() != null && price.compareTo(plan.getMaxPrice()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "The maximum investment for " + plan.getName() + " is " + user.getCurrencySymbol() + MoneyFormat.of(plan.getMaxPrice()) + ".");
        }
        if (user.getAccountBalance().compareTo(price) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your account is insufficient to purchase this plan. Please make a deposit.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireDate = plan.getExpirationDays() != null ? now.plusDays(plan.getExpirationDays()) : null;

        if (plan.getGift() != null && plan.getGift().signum() > 0) {
            user.setBonusBalance(user.getBonusBalance().add(plan.getGift()));
            user.setAccountBalance(user.getAccountBalance().add(plan.getGift()));
        }
        user.setAccountBalance(user.getAccountBalance().subtract(price));
        user.setPlanStatus("on");
        user.setUpdatedAt(now);
        userRepository.save(user);

        Investment investment = Investment.builder()
                .user(user).plan(plan).amount(price).active("yes").invDuration(request.duration())
                .expireDate(expireDate).activatedAt(now).lastGrowth(now)
                .profitEarned(BigDecimal.ZERO).profitWithdrawn(BigDecimal.ZERO).withdrawalDisabled(false)
                .createdAt(now).updatedAt(now)
                .build();
        investment = investmentRepository.save(investment);

        if (plan.getGift() != null && plan.getGift().signum() > 0) {
            ledgerTransactionRepository.save(LedgerTransaction.builder()
                    .user(user).planLabel(plan.getName()).amount(plan.getGift()).type("Gift Bonus")
                    .createdAt(now).updatedAt(now).build());
        }
        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel(plan.getName()).amount(price).type("Plan purchase")
                .investment(investment).createdAt(now).updatedAt(now).build());

        notificationService.notifyUser(user, "Plan purchased",
                "You have successfully purchased the " + plan.getName() + " investment plan for "
                        + user.getCurrencySymbol() + MoneyFormat.of(price) + ".", "success", investment.getId(), "investment");

        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, user.getName() + " purchased " + plan.getName() + " Plan",
                    "This is to inform you that " + user.getName() + " just purchased the " + plan.getName()
                            + " investment plan for " + user.getCurrencySymbol() + MoneyFormat.of(price) + ".");
        }

        return InvestmentSummary.from(investment);
    }

    @Transactional
    public InvestmentSummary cancel(Long userId, Long investmentId) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        if ("cancelled".equals(investment.getActive())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plan is already cancelled.");
        }

        User user = investment.getUser();
        investment.setActive("cancelled");
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        user.setAccountBalance(user.getAccountBalance().add(investment.getAmount()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user)
                .planLabel(investment.getPlan() != null ? investment.getPlan().getName() : "Investment Plan")
                .amount(investment.getAmount()).type("Investment capital for cancelled plan")
                .investment(investment).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

        mailService.send(user.getEmail(), "Investment Plan Cancelled",
                "You have successfully cancelled your " + (investment.getPlan() != null ? investment.getPlan().getName() : "investment")
                        + " plan and your investment capital has been credited to your account. If this is a mistake, please contact us immediately to reactivate it for you.");

        return InvestmentSummary.from(investment);
    }

    @Transactional
    public InvestmentSummary withdrawProfit(Long userId, Long investmentId, WithdrawProfitRequest request) {
        Investment investment = findOwnedInvestment(userId, investmentId);
        if (Boolean.TRUE.equals(investment.getWithdrawalDisabled())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profit withdrawal has been disabled for this investment. Please contact support.");
        }

        BigDecimal earned = investment.getProfitEarned() == null ? BigDecimal.ZERO : investment.getProfitEarned();
        BigDecimal withdrawn = investment.getProfitWithdrawn() == null ? BigDecimal.ZERO : investment.getProfitWithdrawn();
        BigDecimal available = earned.subtract(withdrawn);

        if (request.amount().compareTo(available) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The amount requested exceeds your available profit for this plan.");
        }

        User user = investment.getUser();
        investment.setProfitWithdrawn(withdrawn.add(request.amount()));
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        user.setAccountBalance(user.getAccountBalance().add(request.amount()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Profit Withdrawal (Wallet)").amount(request.amount()).type("Profit Withdrawal")
                .investment(investment).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

        return InvestmentSummary.from(investment);
    }

    private Investment findOwnedInvestment(Long userId, Long investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Investment not found."));
        if (!investment.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You don't have permission to access this investment.");
        }
        return investment;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
