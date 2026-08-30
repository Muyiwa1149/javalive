package com.javalive.backend.service.auth;

import com.javalive.backend.entity.Admin;
import com.javalive.backend.entity.PasswordResetToken;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.repository.PasswordResetTokenRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shared forgot/reset-password flow for both guards — the source app's Fortify-backed password
 * broker, reimplemented against the single `password_reset_tokens` table (already guard-tagged
 * from the Phase 1 migration). Always returns a generic success message on request, regardless of
 * whether the email exists, to avoid leaking account existence.
 */
@Service
public class PasswordResetService {

    private static final int TOKEN_VALID_MINUTES = 60;
    private static final String GUARD_USER = "user";
    private static final String GUARD_ADMIN = "admin";

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${javalive.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository, AdminRepository adminRepository,
                                 PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder,
                                 MailService mailService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void requestUserReset(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            return;
        }
        issueToken(email, GUARD_USER, "/reset-password");
    }

    public void requestAdminReset(String email) {
        if (adminRepository.findByEmail(email).isEmpty()) {
            return;
        }
        issueToken(email, GUARD_ADMIN, "/admin/reset-password");
    }

    public void resetUserPassword(String email, String token, String newPassword) {
        PasswordResetToken resetToken = consumeToken(email, GUARD_USER, token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "This reset link is no longer valid."));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }

    public void resetAdminPassword(String email, String token, String newPassword) {
        PasswordResetToken resetToken = consumeToken(email, GUARD_ADMIN, token);
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "This reset link is no longer valid."));
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
        tokenRepository.delete(resetToken);
    }

    private void issueToken(String email, String guard, String resetPath) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenRepository.save(PasswordResetToken.builder()
                .email(email).guard(guard).token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES))
                .createdAt(LocalDateTime.now())
                .build());

        String link = frontendUrl + resetPath + "?token=" + token + "&email=" + email;
        mailService.send(email, "Reset your password",
                "Click the link below to reset your password. This link expires in " + TOKEN_VALID_MINUTES + " minutes.\n\n" + link);
    }

    private PasswordResetToken consumeToken(String email, String guard, String token) {
        PasswordResetToken resetToken = tokenRepository.findByEmailAndGuardAndToken(email, guard, token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has already been used."));
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new ApiException(HttpStatus.BAD_REQUEST, "This reset link has expired. Please request a new one.");
        }
        return resetToken;
    }
}
