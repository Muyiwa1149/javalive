package com.javalive.backend.service.auth;

import com.javalive.backend.dto.auth.AdminLoginResponse;
import com.javalive.backend.dto.auth.AdminSummary;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.entity.Admin;
import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.security.JwtService;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Mirrors the source app's admin login flow: email+password against the `admins` table (status
 * must be 'active'), then — if `enable2fa` is on — a random 5-digit OTP is emailed and a
 * short-lived pending-2FA token is returned instead of a real session, exactly like the PHP
 * `Auth\LoginController` / `TwoFactorController` pair.
 */
@Service
public class AdminAuthService {

    private static final int PENDING_2FA_EXPIRY_MINUTES = 10;
    private static final int OTP_VALID_MINUTES = 10;

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder,
                             JwtService jwtService, MailService mailService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
    }

    public AdminLoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        if (!"active".equalsIgnoreCase(admin.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This admin account is not active.");
        }

        if (Boolean.TRUE.equals(admin.getEnable2fa())) {
            String otp = String.format("%05d", random.nextInt(100_000));
            admin.setToken2fa(otp);
            admin.setToken2faExpiry(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
            admin.setPass2fa(false);
            adminRepository.save(admin);

            mailService.send(admin.getEmail(), "Your login verification code",
                    "Your verification code is " + otp + ". It expires in " + OTP_VALID_MINUTES + " minutes.");

            String pendingToken = jwtService.generateToken(admin.getId(), "ADMIN_2FA_PENDING", admin.getEmail(),
                    PENDING_2FA_EXPIRY_MINUTES);
            return new AdminLoginResponse(true, pendingToken, null);
        }

        String token = jwtService.generateToken(admin.getId(), "ADMIN", admin.getEmail());
        return new AdminLoginResponse(false, token, AdminSummary.from(admin));
    }

    public AdminLoginResponse verifyTwoFactor(String pendingToken, String code) {
        Long adminId;
        try {
            if (!jwtService.isValid(pendingToken) || !"ADMIN_2FA_PENDING".equals(jwtService.extractRole(pendingToken))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Your verification session has expired. Please log in again.");
            }
            adminId = jwtService.extractSubjectId(pendingToken);
        } catch (JwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Your verification session has expired. Please log in again.");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Admin account not found."));

        if (admin.getToken2faExpiry() == null || admin.getToken2faExpiry().isBefore(LocalDateTime.now())
                || admin.getToken2fa() == null || !admin.getToken2fa().equals(code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired verification code.");
        }

        admin.setPass2fa(true);
        admin.setToken2fa(null);
        adminRepository.save(admin);

        mailService.send(admin.getEmail(), "Successful login",
                "Your account was just verified and logged in successfully.");

        String token = jwtService.generateToken(admin.getId(), "ADMIN", admin.getEmail());
        return new AdminLoginResponse(false, token, AdminSummary.from(admin));
    }

    public AdminSummary me(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admin not found."));
        return AdminSummary.from(admin);
    }
}
