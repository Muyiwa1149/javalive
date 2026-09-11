package com.javalive.backend.service.withdrawal;

import com.javalive.backend.dto.withdrawal.AdminWithdrawalSummary;
import com.javalive.backend.dto.withdrawal.RejectWithdrawalRequest;
import com.javalive.backend.dto.withdrawal.SubmitWithdrawalRequest;
import com.javalive.backend.dto.withdrawal.WithdrawalMethodSummary;
import com.javalive.backend.dto.withdrawal.WithdrawalSummary;
import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.Withdrawal;
import com.javalive.backend.entity.WithdrawalMethod;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WithdrawalMethodRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.settings.KycGuardService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.util.MoneyFormat;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Manual (admin-approved) withdrawal flow — mirrors the source app's {@code WithdrawalController}
 * plus {@code Admin\ManageWithdrawalController}. No live payout-gateway integration (CoinPayments
 * auto-withdraw) is built, matching the deposit-side decision: every withdrawal is admin-reviewed.
 * Preserves the source's dual deduction-timing modes exactly (see {@link #submit} / {@link #approve}
 * / {@link #reject}): "userRequest" debits at request time and refunds on rejection; "AdminApprove"
 * only touches the balance when an admin approves.
 */
@Service
public class WithdrawalService {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WithdrawalRepository withdrawalRepository;
    private final WithdrawalMethodRepository withdrawalMethodRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final KycGuardService kycGuardService;

    public WithdrawalService(WithdrawalRepository withdrawalRepository, WithdrawalMethodRepository withdrawalMethodRepository,
                              UserRepository userRepository, MailService mailService, NotificationService notificationService,
                              SettingsService settingsService, KycGuardService kycGuardService) {
        this.withdrawalRepository = withdrawalRepository;
        this.withdrawalMethodRepository = withdrawalMethodRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.kycGuardService = kycGuardService;
    }

    public List<WithdrawalMethodSummary> listMethods() {
        return withdrawalMethodRepository.findByTypeInAndStatus(List.of("withdrawal", "both"), "enabled")
                .stream().map(WithdrawalMethodSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<WithdrawalSummary> myWithdrawals(Long userId) {
        return withdrawalRepository.findByUserIdOrderByIdDesc(userId).stream().map(WithdrawalSummary::from).toList();
    }

    @Transactional
    public Map<String, String> requestOtp(Long userId) {
        User user = findUser(userId);
        String code = randomCode(5);
        user.setWithdrawOtp(code);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        mailService.send(user.getEmail(), "OTP Request",
                "You have initiated a withdrawal request, use the OTP: " + code + " to complete your request.");
        return Map.of("message", "OTP has been sent to your email.");
    }

    @Transactional
    public WithdrawalSummary submit(Long userId, SubmitWithdrawalRequest request) {
        User user = findUser(userId);
        AppSetting settings = settingsService.get();

        if (Boolean.TRUE.equals(user.getSendOtpEmail())) {
            if (request.otpCode() == null || !request.otpCode().equals(user.getWithdrawOtp())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "OTP is incorrect, please recheck the code.");
            }
        }
        kycGuardService.requireVerified(user);

        WithdrawalMethod method = withdrawalMethodRepository.findById(request.methodId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid withdrawal method."));

        BigDecimal charges = "percentage".equalsIgnoreCase(method.getChargesType())
                ? request.amount().multiply(safeOrZero(method.getChargesAmount())).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                : safeOrZero(method.getChargesAmount());
        BigDecimal toDeduct = request.amount().add(charges);

        if (user.getAccountBalance().compareTo(toDeduct) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sorry, your account balance is insufficient for this request.");
        }
        if (method.getMinimumAmount() != null && request.amount().compareTo(method.getMinimumAmount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Sorry, the minimum amount you can withdraw is " + user.getCurrencySymbol() + MoneyFormat.of(method.getMinimumAmount())
                            + ", please try another payment method.");
        }

        saveWithdrawalDestination(user, method.getName(), request);

        if ("userRequest".equals(settings.getDeductionOption())) {
            user.setAccountBalance(user.getAccountBalance().subtract(toDeduct));
        }
        user.setWithdrawOtp(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        Withdrawal withdrawal = Withdrawal.builder()
                .user(user).amount(request.amount()).toDeduct(toDeduct).paymentMode(method.getName())
                .status("Pending").payDetails(buildPayDetails(method.getName(), request))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        withdrawal = withdrawalRepository.save(withdrawal);

        if (settings.getContactEmail() != null) {
            mailService.send(settings.getContactEmail(), "Withdrawal request from " + user.getName(),
                    user.getName() + " requested a " + method.getName() + " withdrawal of " + user.getCurrencySymbol() + MoneyFormat.of(request.amount()) + ". Please review.");
        }
        mailService.send(user.getEmail(), "Withdrawal request received",
                "Your withdrawal request of " + user.getCurrencySymbol() + MoneyFormat.of(request.amount()) + " has been received. Please wait while we process it.");

        return WithdrawalSummary.from(withdrawal);
    }

    @Transactional(readOnly = true)
    public List<AdminWithdrawalSummary> adminList(String status) {
        List<Withdrawal> withdrawals = status != null && !status.isBlank()
                ? withdrawalRepository.findByStatusWithUserOrderByIdDesc(status)
                : withdrawalRepository.findAllWithUserOrderByIdDesc();
        return withdrawals.stream().map(AdminWithdrawalSummary::from).toList();
    }

    @Transactional
    public AdminWithdrawalSummary approve(Long withdrawalId) {
        Withdrawal withdrawal = findWithdrawal(withdrawalId);
        requirePending(withdrawal);

        User user = withdrawal.getUser();
        AppSetting settings = settingsService.get();

        if ("AdminApprove".equals(settings.getDeductionOption())) {
            user.setAccountBalance(user.getAccountBalance().subtract(withdrawal.getToDeduct()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        withdrawal.setStatus("Processed");
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        notificationService.notifyUser(user, "Withdrawal Approved",
                "Your withdrawal request of " + user.getCurrencySymbol() + MoneyFormat.of(withdrawal.getAmount())
                        + " has been approved and processed. Funds have been sent to your selected account.",
                "success", withdrawal.getId(), "withdrawal");
        mailService.send(user.getEmail(), "Successful Withdrawal",
                "This is to inform you that your withdrawal request of " + user.getCurrencySymbol() + MoneyFormat.of(withdrawal.getAmount())
                        + " has been approved and funds have been sent to your selected account.");

        return AdminWithdrawalSummary.from(withdrawal);
    }

    @Transactional
    public AdminWithdrawalSummary reject(Long withdrawalId, RejectWithdrawalRequest request) {
        Withdrawal withdrawal = findWithdrawal(withdrawalId);
        requirePending(withdrawal);

        User user = withdrawal.getUser();
        AppSetting settings = settingsService.get();

        if ("userRequest".equals(settings.getDeductionOption())) {
            user.setAccountBalance(user.getAccountBalance().add(withdrawal.getToDeduct()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        withdrawal.setStatus("Rejected");
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        String reason = (request.reason() == null || request.reason().isBlank())
                ? "Your withdrawal request of " + user.getCurrencySymbol() + MoneyFormat.of(withdrawal.getAmount()) + " has been rejected."
                : request.reason();
        notificationService.notifyUser(user, "Withdrawal Rejected", reason, "danger", withdrawal.getId(), "withdrawal");

        if (request.sendEmail()) {
            String subject = request.subject() == null || request.subject().isBlank() ? "Withdrawal Rejected" : request.subject();
            mailService.send(user.getEmail(), subject, reason);
        }

        return AdminWithdrawalSummary.from(withdrawal);
    }

    private void saveWithdrawalDestination(User user, String methodName, SubmitWithdrawalRequest request) {
        switch (methodName) {
            case "Bitcoin" -> user.setBtcAddress(request.details());
            case "Ethereum" -> user.setEthAddress(request.details());
            case "Litecoin" -> user.setLtcAddress(request.details());
            case "USDT" -> user.setUsdtAddress(request.details());
            case "Bank Transfer", "Wire Transfer", "ACH" -> {
                user.setBankName(request.bankName());
                user.setBankAccountName(request.accountName());
                user.setBankAccountNumber(request.accountNumber());
                user.setBankSwiftCode(request.swiftCode());
            }
            default -> { /* other crypto/manual methods: destination is captured in payDetails only */ }
        }
    }

    private String buildPayDetails(String methodName, SubmitWithdrawalRequest request) {
        return switch (methodName) {
            case "Bank Transfer", "Wire Transfer", "ACH" -> "Bank: " + request.bankName()
                    + ", Account Name: " + request.accountName()
                    + ", Account Number: " + request.accountNumber()
                    + ", Swift/Routing: " + request.swiftCode();
            default -> request.details();
        };
    }

    private BigDecimal safeOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private void requirePending(Withdrawal withdrawal) {
        if (!"Pending".equals(withdrawal.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This withdrawal has already been processed.");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private Withdrawal findWithdrawal(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Withdrawal not found."));
    }
}
