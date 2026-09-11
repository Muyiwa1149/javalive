package com.javalive.backend.service.transfer;

import com.javalive.backend.dto.dashboard.DashboardActivitySummary;
import com.javalive.backend.dto.transfer.TransferRequest;
import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.util.MoneyFormat;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's TransferController@transfertouser — direct user-to-user balance transfer. */
@Service
public class TransferService {

    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SettingsService settingsService;

    public TransferService(UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository,
                            PasswordEncoder passwordEncoder, MailService mailService, SettingsService settingsService) {
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.settingsService = settingsService;
    }

    @Transactional
    public void transfer(Long senderId, TransferRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        if (!passwordEncoder.matches(request.password(), sender.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect password.");
        }

        User receiver = userRepository.findByEmailOrUsername(request.recipient(), request.recipient())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No user with this email or username exists."));

        if (sender.getId().equals(receiver.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot send funds to yourself.");
        }

        AppSetting settings = settingsService.get();
        BigDecimal charges = request.amount().multiply(safeOrZero(settings.getTransferCharges()))
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal toDeduct = request.amount().add(charges);

        if (settings.getMinTransferAmount() != null && request.amount().compareTo(settings.getMinTransferAmount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "The minimum amount you can transfer is " + sender.getCurrencySymbol() + MoneyFormat.of(settings.getMinTransferAmount()) + ".");
        }
        if (sender.getAccountBalance().compareTo(toDeduct) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
        }

        sender.setAccountBalance(sender.getAccountBalance().subtract(toDeduct));
        sender.setUpdatedAt(LocalDateTime.now());
        userRepository.save(sender);

        receiver.setAccountBalance(receiver.getAccountBalance().add(request.amount()));
        receiver.setUpdatedAt(LocalDateTime.now());
        userRepository.save(receiver);

        LocalDateTime now = LocalDateTime.now();
        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(sender).planLabel("Transferred to " + receiver.getName()).amount(request.amount())
                .type("Fund Transfer").createdAt(now).updatedAt(now).build());
        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(receiver).planLabel("Received from " + sender.getName()).amount(request.amount())
                .type("Fund Transfer").createdAt(now).updatedAt(now).build());

        mailService.send(receiver.getEmail(), "Credit Alert",
                "You just received " + receiver.getCurrencySymbol() + MoneyFormat.of(request.amount()) + " from " + sender.getName()
                        + " and your account balance is now " + receiver.getCurrencySymbol() + MoneyFormat.of(receiver.getAccountBalance()) + ".");
    }

    @Transactional(readOnly = true)
    public List<DashboardActivitySummary> history(Long userId) {
        return ledgerTransactionRepository.findByUserIdAndType(userId, "Fund Transfer").stream()
                .map(DashboardActivitySummary::from).toList();
    }

    private BigDecimal safeOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
