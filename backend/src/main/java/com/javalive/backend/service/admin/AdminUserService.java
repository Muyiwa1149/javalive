package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminActivitySummary;
import com.javalive.backend.dto.admin.AdminUserDetail;
import com.javalive.backend.dto.admin.AdminUserEditRequest;
import com.javalive.backend.dto.admin.AdminUserSummary;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.ActivityRepository;
import com.javalive.backend.repository.BotTradingHistoryRepository;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.InvestmentRepository;
import com.javalive.backend.repository.KycRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.LoanRepository;
import com.javalive.backend.repository.Mt4DetailRepository;
import com.javalive.backend.repository.Mt4SubscriptionRepository;
import com.javalive.backend.repository.UserBotInvestmentRepository;
import com.javalive.backend.repository.UserCopyTradeRepository;
import com.javalive.backend.repository.UserPlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.UserSignalPlanRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import com.javalive.backend.security.JwtService;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's {@code Admin\ManageUsersController} + the Livewire {@code ManageUsers} list. */
@Service
public class AdminUserService {

    private static final String DEFAULT_RESET_PASSWORD = "user01236";

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final DepositRepository depositRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final UserPlanRepository userPlanRepository;
    private final LoanRepository loanRepository;
    private final UserSignalPlanRepository userSignalPlanRepository;
    private final UserCopyTradeRepository userCopyTradeRepository;
    private final UserBotInvestmentRepository userBotInvestmentRepository;
    private final BotTradingHistoryRepository botTradingHistoryRepository;
    private final InvestmentRepository investmentRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final Mt4DetailRepository mt4DetailRepository;
    private final Mt4SubscriptionRepository mt4SubscriptionRepository;
    private final KycRepository kycRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final NotificationService notificationService;

    public AdminUserService(UserRepository userRepository, ActivityRepository activityRepository,
                             DepositRepository depositRepository, WithdrawalRepository withdrawalRepository,
                             UserPlanRepository userPlanRepository, LoanRepository loanRepository,
                             UserSignalPlanRepository userSignalPlanRepository,
                             UserCopyTradeRepository userCopyTradeRepository,
                             UserBotInvestmentRepository userBotInvestmentRepository,
                             BotTradingHistoryRepository botTradingHistoryRepository,
                             InvestmentRepository investmentRepository,
                             LedgerTransactionRepository ledgerTransactionRepository,
                             Mt4DetailRepository mt4DetailRepository, Mt4SubscriptionRepository mt4SubscriptionRepository,
                             KycRepository kycRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                             MailService mailService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.depositRepository = depositRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.userPlanRepository = userPlanRepository;
        this.loanRepository = loanRepository;
        this.userSignalPlanRepository = userSignalPlanRepository;
        this.userCopyTradeRepository = userCopyTradeRepository;
        this.userBotInvestmentRepository = userBotInvestmentRepository;
        this.botTradingHistoryRepository = botTradingHistoryRepository;
        this.investmentRepository = investmentRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.mt4DetailRepository = mt4DetailRepository;
        this.mt4SubscriptionRepository = mt4SubscriptionRepository;
        this.kycRepository = kycRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> list() {
        return userRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
                .stream().map(AdminUserSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserDetail detail(Long id) {
        return AdminUserDetail.from(getUser(id));
    }

    @Transactional
    public AdminUserDetail edit(Long id, AdminUserEditRequest request) {
        User user = getUser(id);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setCountry(request.country());
        user.setUsername(request.username());
        user.setPhone(request.phone());
        if (request.currencySymbol() != null && !request.currencySymbol().isBlank()) {
            user.setCurrencySymbol(request.currencySymbol());
        }
        if (request.currencyCode() != null && !request.currencyCode().isBlank()) {
            user.setCurrencyCode(request.currencyCode());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return AdminUserDetail.from(userRepository.save(user));
    }

    @Transactional
    public void create(String name, String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A user with this email already exists.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This username is already taken.");
        }
        userRepository.save(newUserDefaults(name, username, email, passwordEncoder.encode(password), null, null));
    }

    /**
     * Shared default field set for an admin-created account, used by both the single-user "Add
     * User" form and the bulk Excel import — see {@link com.javalive.backend.service.admin.AdminImportService}.
     */
    User newUserDefaults(String name, String username, String email, String encodedPassword, String country, String phone) {
        return User.builder()
                .name(name).username(username).email(email).password(encodedPassword).country(country).phone(phone)
                .currencySymbol("$").currencyCode("USD").tradeType("Profit").tradeMode("live")
                .numberOfTrades(0).accountBalance(java.math.BigDecimal.ZERO).roiBalance(java.math.BigDecimal.ZERO)
                .bonusBalance(java.math.BigDecimal.ZERO).referralBonusBalance(java.math.BigDecimal.ZERO)
                .signupBonus(java.math.BigDecimal.ZERO).bonusReleased(false).taxType("none")
                .status("active").signalStatus("off").planStatus("off").withdrawalCodeStatus("off")
                .copyTradingProgress(0).walletConnected(false).hasSignalSubscription(false)
                .legacyTradeField(0).sendOtpEmail(true).sendRoiEmail(true).sendPromoEmail(true).sendInvPlanEmail(true)
                .dashboardStyle("dark")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void block(Long id) {
        User user = getUser(id);
        user.setStatus("blocked");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void unblock(Long id) {
        User user = getUser(id);
        user.setStatus("active");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(Long id) {
        User user = getUser(id);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id) {
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String impersonate(Long id) {
        User user = getUser(id);
        return jwtService.generateToken(user.getId(), "USER", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<AdminActivitySummary> activity(Long id) {
        return activityRepository.findByUser_IdOrderByIdDesc(id).stream().map(AdminActivitySummary::from).toList();
    }

    @Transactional
    public void clearActivity(Long id) {
        activityRepository.deleteAll(activityRepository.findByUser_Id(id));
    }

    @Transactional
    public void sendMail(Long id, String subject, String message) {
        User user = getUser(id);
        mailService.send(user.getEmail(), subject, message);
        notificationService.notifyUser(user, subject, message, "message");
    }

    @Transactional
    public void notifyDashboard(Long id, String message) {
        User user = getUser(id);
        notificationService.notifyUser(user, "Notification", message, "info");
    }

    /** Mirrors source's {@code delsystemuser} — cascades across every table with a user FK. */
    @Transactional
    public void delete(Long id) {
        User user = getUser(id);

        depositRepository.deleteAll(depositRepository.findByUserId(id));
        withdrawalRepository.deleteAll(withdrawalRepository.findByUserId(id));
        userPlanRepository.deleteAll(userPlanRepository.findByUserId(id));
        loanRepository.deleteAll(loanRepository.findByUserId(id));
        userSignalPlanRepository.deleteAll(userSignalPlanRepository.findByUserIdOrderByIdDesc(id));
        userCopyTradeRepository.deleteAll(userCopyTradeRepository.findByUserIdOrderByCreatedAtDesc(id));
        botTradingHistoryRepository.deleteAll(botTradingHistoryRepository.findByUserBotInvestmentUserIdOrderByOpenedAtDesc(id));
        userBotInvestmentRepository.deleteAll(userBotInvestmentRepository.findByUserIdOrderByCreatedAtDesc(id));
        investmentRepository.deleteAll(investmentRepository.findByUserId(id));
        ledgerTransactionRepository.deleteAll(ledgerTransactionRepository.findByUserId(id));
        mt4SubscriptionRepository.deleteAll(mt4SubscriptionRepository.findByUserId(id));
        mt4DetailRepository.deleteAll(mt4DetailRepository.findByUserId(id));
        kycRepository.deleteAll(kycRepository.findByUserId(id));
        activityRepository.deleteAll(activityRepository.findByUser_Id(id));

        userRepository.delete(user);
    }

    /** Mirrors source's {@code sendmailtoall} — mail merged categories (All/No active plans/No deposit/Select Users). */
    @Transactional(readOnly = true)
    public int emailSegment(String category, List<Long> userIds, String subject, String message) {
        List<User> recipients = switch (category) {
            case "No active plans" -> {
                var activeIds = userPlanRepository.findDistinctUserIdsByActive("yes");
                yield userRepository.findAll().stream().filter(u -> !activeIds.contains(u.getId())).toList();
            }
            case "No deposit" -> {
                var depositedIds = depositRepository.findDistinctUserIds();
                yield userRepository.findAll().stream().filter(u -> !depositedIds.contains(u.getId())).toList();
            }
            case "Select Users" -> userRepository.findAllById(userIds);
            default -> userRepository.findAll();
        };
        for (User user : recipients) {
            mailService.send(user.getEmail(), subject, message);
        }
        return recipients.size();
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
