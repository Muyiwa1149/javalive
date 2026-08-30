package com.javalive.backend.service.dashboard;

import com.javalive.backend.dto.dashboard.DashboardActivitySummary;
import com.javalive.backend.dto.dashboard.DashboardPlanSummary;
import com.javalive.backend.dto.dashboard.DashboardSummary;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.Mt4DetailRepository;
import com.javalive.backend.repository.UserPlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Backs the dashboard home. Mirrors {@code ViewsController@dashboard} in the source app,
 * including its lazy signup-bonus-crediting side effect (credited on first dashboard load rather
 * than at registration — preserved as-is since it's easy to reason about and matches source).
 */
@Service
public class DashboardService {

    private static final List<String> ACTIVITY_TYPES = List.of("Sell", "Buy", "WIN", "LOSE");

    private final UserRepository userRepository;
    private final DepositRepository depositRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final UserPlanRepository userPlanRepository;
    private final Mt4DetailRepository mt4DetailRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final SettingsService settingsService;

    public DashboardService(UserRepository userRepository, DepositRepository depositRepository,
                             WithdrawalRepository withdrawalRepository, UserPlanRepository userPlanRepository,
                             Mt4DetailRepository mt4DetailRepository, LedgerTransactionRepository ledgerTransactionRepository,
                             SettingsService settingsService) {
        this.userRepository = userRepository;
        this.depositRepository = depositRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.userPlanRepository = userPlanRepository;
        this.mt4DetailRepository = mt4DetailRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.settingsService = settingsService;
    }

    @Transactional
    public DashboardSummary getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        creditSignupBonusIfDue(user);

        BigDecimal totalDeposited = depositRepository.findByUserId(userId).stream()
                .filter(d -> "Processed".equals(d.getStatus()))
                .map(d -> d.getAmount() == null ? BigDecimal.ZERO : d.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawn = withdrawalRepository.findByUserId(userId).stream()
                .filter(w -> "Processed".equals(w.getStatus()))
                .map(w -> w.getAmount() == null ? BigDecimal.ZERO : w.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardPlanSummary> recentPlans = userPlanRepository
                .findTop2ByUserIdAndActiveOrderByIdDesc(userId, "yes").stream()
                .map(DashboardPlanSummary::from).toList();

        List<DashboardActivitySummary> recentActivity = ledgerTransactionRepository
                .findTop5ByUserIdAndTypeInOrderByIdDesc(userId, ACTIVITY_TYPES).stream()
                .map(DashboardActivitySummary::from).toList();

        long tradingAccounts = mt4DetailRepository.countByUserId(userId);

        boolean kycRequired = Boolean.TRUE.equals(settingsService.get().getEnableKyc());
        String referralLink = user.getUsername() != null ? "/ref/" + user.getUsername() : null;

        return new DashboardSummary(
                user.getAccountBalance(), user.getRoiBalance(), user.getBonusBalance(), user.getCurrencySymbol(),
                user.getAccountVerifyStatus(), kycRequired, referralLink,
                totalDeposited, totalWithdrawn, tradingAccounts, recentPlans, recentActivity
        );
    }

    private void creditSignupBonusIfDue(User user) {
        if (Boolean.TRUE.equals(user.getBonusReleased())) {
            return;
        }
        BigDecimal signupBonus = settingsService.get().getSignupBonus();
        if (signupBonus == null || signupBonus.signum() <= 0) {
            return;
        }
        user.setAccountBalance(user.getAccountBalance().add(signupBonus));
        user.setBonusBalance(user.getBonusBalance().add(signupBonus));
        user.setSignupBonus(signupBonus);
        user.setBonusReleased(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("SignUp Bonus").amount(signupBonus).type("Bonus")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }
}
