package com.javalive.backend.service.deposit;

import com.javalive.backend.dto.deposit.AdminDepositSummary;
import com.javalive.backend.dto.deposit.DepositMethodSummary;
import com.javalive.backend.dto.deposit.DepositSummary;
import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.Deposit;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.WithdrawalMethod;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WithdrawalMethodRepository;
import com.javalive.backend.service.finance.ReferralCommissionService;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manual (admin-approved) deposit flow — mirrors the source app's {@code DepositController@savedeposit}
 * plus the admin approval side (which credits the balance, deposit bonus, and referral chain, same
 * as the source's auto-processed gateway path did). No live payment-gateway integration is built
 * here per explicit decision — every deposit method is manual/proof-based and admin-reviewed.
 */
@Service
public class DepositService {

    private static final List<String> ALLOWED_PROOF_EXTENSIONS = List.of("pdf", "doc", "jpeg", "jpg", "png");

    private final DepositRepository depositRepository;
    private final WithdrawalMethodRepository withdrawalMethodRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final FileStorageService fileStorageService;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final ReferralCommissionService referralCommissionService;

    public DepositService(DepositRepository depositRepository, WithdrawalMethodRepository withdrawalMethodRepository,
                           UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository,
                           FileStorageService fileStorageService, MailService mailService,
                           NotificationService notificationService, SettingsService settingsService,
                           ReferralCommissionService referralCommissionService) {
        this.depositRepository = depositRepository;
        this.withdrawalMethodRepository = withdrawalMethodRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.fileStorageService = fileStorageService;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.referralCommissionService = referralCommissionService;
    }

    public List<DepositMethodSummary> listMethods() {
        return withdrawalMethodRepository.findByTypeInAndStatus(List.of("deposit", "both"), "enabled")
                .stream().map(DepositMethodSummary::from).toList();
    }

    public List<DepositSummary> myDeposits(Long userId) {
        return depositRepository.findByUserIdOrderByIdDesc(userId).stream().map(DepositSummary::from).toList();
    }

    @Transactional
    public DepositSummary submit(Long userId, Long methodId, BigDecimal amount, String txnId, MultipartFile proof) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        WithdrawalMethod method = withdrawalMethodRepository.findById(methodId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid payment method."));

        if (amount == null || amount.signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a valid deposit amount.");
        }
        if (method.getMinimumAmount() != null && amount.compareTo(method.getMinimumAmount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount is below the minimum for " + method.getName() + ".");
        }
        if (method.getMaximumAmount() != null && amount.compareTo(method.getMaximumAmount()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount exceeds the maximum for " + method.getName() + ".");
        }

        String proofPath = fileStorageService.storeImage(proof, "uploads", ALLOWED_PROOF_EXTENSIONS);

        Deposit deposit = Deposit.builder()
                .user(user).amount(amount).paymentMode(method.getName()).status("Pending")
                .proofImage(proofPath).txnId(txnId).isSignalDeposit(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        deposit = depositRepository.save(deposit);

        AppSetting settings = settingsService.get();
        if (settings.getContactEmail() != null) {
            mailService.send(settings.getContactEmail(), "New deposit request from " + user.getName(),
                    user.getName() + " submitted a " + method.getName() + " deposit of " + amount + ". Please review and approve.");
        }
        mailService.send(user.getEmail(), "Deposit request received",
                "We've received your " + method.getName() + " deposit request for " + amount + ". Please wait while we validate this transaction.");

        return DepositSummary.from(deposit);
    }

    @Transactional(readOnly = true)
    public List<AdminDepositSummary> adminList(String status) {
        List<Deposit> deposits = status != null && !status.isBlank()
                ? depositRepository.findByStatusWithUserOrderByIdDesc(status)
                : depositRepository.findAllWithUserOrderByIdDesc();
        return deposits.stream().map(AdminDepositSummary::from).toList();
    }

    @Transactional
    public AdminDepositSummary approve(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Deposit not found."));
        if (!"Pending".equals(deposit.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This deposit has already been processed.");
        }

        User user = deposit.getUser();
        AppSetting settings = settingsService.get();
        BigDecimal amount = deposit.getAmount();

        BigDecimal bonus = BigDecimal.ZERO;
        if (settings.getDepositBonusPct() != null && settings.getDepositBonusPct().signum() > 0) {
            bonus = amount.multiply(settings.getDepositBonusPct()).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        }

        user.setAccountBalance(user.getAccountBalance().add(amount).add(bonus));
        if (bonus.signum() > 0) {
            user.setBonusBalance(user.getBonusBalance().add(bonus));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if (bonus.signum() > 0) {
            ledgerTransactionRepository.save(LedgerTransaction.builder()
                    .user(user).planLabel("Deposit Bonus for " + user.getCurrencySymbol() + amount + " deposited")
                    .amount(bonus).type("Bonus")
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build());
        }

        deposit.setStatus("Processed");
        deposit.setUpdatedAt(LocalDateTime.now());
        depositRepository.save(deposit);

        referralCommissionService.creditChain(user, amount, settings);

        notificationService.notifyUser(user, "Deposit approved",
                "Your deposit of " + user.getCurrencySymbol() + amount + " has been approved and credited to your account.",
                "success", deposit.getId(), "deposit");
        mailService.send(user.getEmail(), "Deposit approved",
                "Your deposit of " + amount + " has been approved and credited to your account.");

        return AdminDepositSummary.from(deposit);
    }

    @Transactional
    public void delete(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Deposit not found."));
        if (!"Pending".equals(deposit.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pending deposits can be deleted.");
        }
        depositRepository.delete(deposit);
    }
}
