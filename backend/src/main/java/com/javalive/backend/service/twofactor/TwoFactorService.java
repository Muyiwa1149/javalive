package com.javalive.backend.service.twofactor;

import com.javalive.backend.dto.twofactor.TwoFactorRecoveryCodesResponse;
import com.javalive.backend.dto.twofactor.TwoFactorSetupResponse;
import com.javalive.backend.dto.twofactor.TwoFactorStatusResponse;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.service.wallet.AesEncryptionService;
import com.javalive.backend.web.exception.ApiException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Java port of source's Laravel Fortify {@code Features::twoFactorAuthentication()} — genuinely
 * live in source (confirmed: enabled in {@code config/fortify.php}, {@code users.two_factor_secret}/
 * {@code two_factor_recovery_codes} columns present and reachable via the standard Jetstream
 * account-security UI), not dead scaffolding. No real migrated user currently has it turned on, but
 * any user could enable it today in source, so this had to be built for real parity.
 *
 * <p>TOTP (RFC 6238) via {@code googleauth}, matching Fortify's own use of the same algorithm
 * (Google Authenticator / Authy / any standard authenticator app all work identically either way).
 * QR generated server-side with ZXing so no new frontend dependency is needed. Secret and recovery
 * codes are AES-256-GCM encrypted at rest via {@link AesEncryptionService} — an improvement over
 * source's plaintext {@code two_factor_secret}/{@code two_factor_recovery_codes} columns, same
 * decision already made for wallet-connect and MT4 passwords.
 */
@Service
public class TwoFactorService {

    private static final int RECOVERY_CODE_COUNT = 8;
    /** Codes never contain this character (see {@link #randomRecoveryCode}), so a plain join/split is safe. */
    private static final String CODE_DELIMITER = "|";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final GoogleAuthenticator GOOGLE_AUTHENTICATOR = new GoogleAuthenticator();

    private final UserRepository userRepository;
    private final AesEncryptionService encryptionService;
    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;

    public TwoFactorService(UserRepository userRepository, AesEncryptionService encryptionService,
                             SettingsService settingsService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.settingsService = settingsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public TwoFactorStatusResponse status(Long userId) {
        User user = findUser(userId);
        return new TwoFactorStatusResponse(user.getTwoFactorConfirmedAt() != null, user.getTwoFactorConfirmedAt());
    }

    /** Generates a new secret and stores it as PENDING — login is not gated until {@link #confirm} succeeds,
     *  so an interrupted setup can never lock a user out. Calling this again before confirming replaces
     *  the pending secret (matches Fortify's own "re-enable overwrites the unconfirmed one" behavior). */
    @Transactional
    public TwoFactorSetupResponse setup(Long userId) {
        User user = findUser(userId);

        GoogleAuthenticatorKey credentials = GOOGLE_AUTHENTICATOR.createCredentials();
        String secret = credentials.getKey();

        user.setTwoFactorSecret(encryptionService.encrypt(secret));
        user.setTwoFactorRecoveryCodes(null);
        user.setTwoFactorConfirmedAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String issuer = settingsService.get().getSiteName();
        if (issuer == null || issuer.isBlank()) {
            issuer = "Velnora Partners";
        }
        String otpauthUri = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, user.getEmail(), credentials);

        return new TwoFactorSetupResponse(secret, otpauthUri, buildQrDataUri(otpauthUri));
    }

    /** Verifies the first code from the authenticator app, activates 2FA, and returns one-time-visible
     *  recovery codes — the standard "confirm before it's live" flow. */
    @Transactional
    public TwoFactorRecoveryCodesResponse confirm(Long userId, String code) {
        User user = findUser(userId);
        if (user.getTwoFactorSecret() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start two-factor setup before confirming a code.");
        }

        String secret = encryptionService.decrypt(user.getTwoFactorSecret());
        if (!GOOGLE_AUTHENTICATOR.authorize(secret, parseCode(code))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification code, please try again.");
        }

        List<String> plainCodes = generateRecoveryCodes();
        user.setTwoFactorRecoveryCodes(encryptRecoveryCodes(plainCodes));
        user.setTwoFactorConfirmedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new TwoFactorRecoveryCodesResponse(plainCodes);
    }

    @Transactional
    public void disable(Long userId, String password) {
        User user = findUser(userId);
        requireCorrectPassword(user, password);

        user.setTwoFactorSecret(null);
        user.setTwoFactorRecoveryCodes(null);
        user.setTwoFactorConfirmedAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public TwoFactorRecoveryCodesResponse regenerateRecoveryCodes(Long userId, String password) {
        User user = findUser(userId);
        requireCorrectPassword(user, password);
        if (user.getTwoFactorConfirmedAt() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Two-factor authentication is not enabled.");
        }

        List<String> plainCodes = generateRecoveryCodes();
        user.setTwoFactorRecoveryCodes(encryptRecoveryCodes(plainCodes));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new TwoFactorRecoveryCodesResponse(plainCodes);
    }

    /** Used by the login-challenge step — accepts either a live TOTP code or a single-use recovery code,
     *  matching Jetstream's real challenge behavior exactly. Consumed recovery codes are removed. */
    @Transactional
    public boolean verifyLoginChallenge(User user, String code) {
        String secret = encryptionService.decrypt(user.getTwoFactorSecret());
        Integer totp = parseCode(code);
        if (totp != null && GOOGLE_AUTHENTICATOR.authorize(secret, totp)) {
            return true;
        }

        List<String> codes = decryptRecoveryCodes(user.getTwoFactorRecoveryCodes());
        if (codes.remove(code.trim())) {
            user.setTwoFactorRecoveryCodes(encryptRecoveryCodes(codes));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    private void requireCorrectPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect password.");
        }
    }

    private List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            codes.add(randomRecoveryCode());
        }
        return codes;
    }

    private String randomRecoveryCode() {
        String chars = "abcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i == 5) sb.append('-');
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String encryptRecoveryCodes(List<String> plainCodes) {
        return encryptionService.encrypt(String.join(CODE_DELIMITER, plainCodes));
    }

    private List<String> decryptRecoveryCodes(String encrypted) {
        if (encrypted == null) {
            return new ArrayList<>();
        }
        String joined = encryptionService.decrypt(encrypted);
        if (joined.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(joined.split("\\" + CODE_DELIMITER)));
    }

    private Integer parseCode(String code) {
        try {
            return Integer.parseInt(code.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildQrDataUri(String otpauthUri) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(otpauthUri, BarcodeFormat.QR_CODE, 250, 250);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
