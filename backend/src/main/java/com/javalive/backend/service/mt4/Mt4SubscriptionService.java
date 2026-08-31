package com.javalive.backend.service.mt4;

import com.javalive.backend.dto.mt4.Mt4DetailSummary;
import com.javalive.backend.dto.mt4.Mt4SubscriptionRequest;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.Mt4Detail;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.Mt4DetailRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.service.wallet.AesEncryptionService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the source app's UserSubscriptionController — this is a fully local feature, not an
 * external-SaaS proxy (unlike Membership/Signals, it never calls {@code fetctApi}). Reuses
 * {@link AesEncryptionService} for the MT4 password, same as WalletConnect, instead of the
 * source's plaintext {@code mt4_password} column.
 */
@Service
public class Mt4SubscriptionService {

    private final Mt4DetailRepository mt4DetailRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final MailService mailService;
    private final SettingsService settingsService;
    private final AesEncryptionService encryptionService;

    public Mt4SubscriptionService(Mt4DetailRepository mt4DetailRepository, UserRepository userRepository,
                                   LedgerTransactionRepository ledgerTransactionRepository, MailService mailService,
                                   SettingsService settingsService, AesEncryptionService encryptionService) {
        this.mt4DetailRepository = mt4DetailRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.mailService = mailService;
        this.settingsService = settingsService;
        this.encryptionService = encryptionService;
    }

    @Transactional(readOnly = true)
    public List<Mt4DetailSummary> mySubscriptions(Long userId) {
        return mt4DetailRepository.findByUserIdOrderByIdDesc(userId).stream().map(Mt4DetailSummary::from).toList();
    }

    @Transactional
    public Mt4DetailSummary save(Long userId, Mt4SubscriptionRequest request) {
        User user = findUser(userId);

        if (request.amount().signum() > 0 && user.getAccountBalance().compareTo(request.amount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sorry, your account balance is insufficient for this request.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.amount().signum() > 0) {
            user.setAccountBalance(user.getAccountBalance().subtract(request.amount()));
            user.setUpdatedAt(now);
            userRepository.save(user);
        }

        Mt4Detail mt4 = Mt4Detail.builder()
                .user(user).mt4Id(request.userid()).mt4PasswordEncrypted(encryptionService.encrypt(request.pswrd()))
                .accountType(request.acntype()).accountName(request.name()).currency(request.currency())
                .leverage(request.leverage()).server(request.server()).duration(request.duration())
                .status("Pending").createdAt(now).updatedAt(now)
                .build();
        mt4 = mt4DetailRepository.save(mt4);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Subscribed MT4 Trading").amount(request.amount()).type("MT4 Trading")
                .createdAt(now).updatedAt(now).build());

        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, "MT4 Details submitted",
                    "This is to notify you that " + user.getName() + " submitted MT4 details for trading, please login to take necessary action.");
        }

        return Mt4DetailSummary.from(mt4);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Mt4Detail mt4 = mt4DetailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MT4 subscription not found."));
        mt4DetailRepository.delete(mt4);
    }

    @Transactional
    public Mt4DetailSummary renew(Long userId, Long id) {
        Mt4Detail mt4 = mt4DetailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MT4 subscription not found."));
        User user = mt4.getUser();

        var settings = settingsService.get();
        BigDecimal amount;
        LocalDateTime endAt = mt4.getEndDate() != null ? mt4.getEndDate() : LocalDateTime.now();
        switch (mt4.getDuration() == null ? "" : mt4.getDuration()) {
            case "Monthly" -> { amount = settings.getSubscriptionMonthlyFee(); endAt = endAt.plusMonths(1); }
            case "Quaterly", "Quarterly" -> { amount = settings.getSubscriptionQuarterlyFee(); endAt = endAt.plusMonths(4); }
            case "Yearly" -> { amount = settings.getSubscriptionYearlyFee(); endAt = endAt.plusYears(1); }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown subscription duration.");
        }
        if (amount == null) amount = BigDecimal.ZERO;

        if (amount.compareTo(user.getAccountBalance()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your account balance is insufficient to renew your subscription, please make a deposit.");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setAccountBalance(user.getAccountBalance().subtract(amount));
        user.setUpdatedAt(now);
        userRepository.save(user);

        mt4.setStartDate(now);
        mt4.setEndDate(endAt);
        mt4.setRemindedAt(endAt.minusDays(10));
        mt4.setStatus("Active");
        mt4.setUpdatedAt(now);
        mt4 = mt4DetailRepository.save(mt4);

        mailService.send(user.getEmail(), "Your subscription have been renewed",
                "Your subscription with MT4-ID: " + mt4.getMt4Id() + " is renewed successfully.");
        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, "Subscription have been renewed",
                    "Subscription with MT4-ID: " + mt4.getMt4Id() + " has been renewed successfully.");
        }

        return Mt4DetailSummary.from(mt4);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
