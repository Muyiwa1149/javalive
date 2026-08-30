package com.javalive.backend.web.auth;

import com.javalive.backend.dto.auth.AdminLoginResponse;
import com.javalive.backend.dto.auth.AdminSummary;
import com.javalive.backend.dto.auth.AdminTwoFactorRequest;
import com.javalive.backend.dto.auth.ForgotPasswordRequest;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.dto.auth.ResetPasswordRequest;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.auth.AdminAuthService;
import com.javalive.backend.service.auth.PasswordResetService;
import com.javalive.backend.web.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final PasswordResetService passwordResetService;

    public AdminAuthController(AdminAuthService adminAuthService, PasswordResetService passwordResetService) {
        this.adminAuthService = adminAuthService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody LoginRequest request) {
        return adminAuthService.login(request);
    }

    /** Body carries the OTP; the pending-2FA token travels in the Authorization header like a normal Bearer token. */
    @PostMapping("/2fa")
    public AdminLoginResponse verifyTwoFactor(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                               @Valid @RequestBody AdminTwoFactorRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing verification session.");
        }
        String pendingToken = authHeader.substring(7);
        return adminAuthService.verifyTwoFactor(pendingToken, request.code());
    }

    @GetMapping("/me")
    public AdminSummary me(@AuthenticationPrincipal AdminPrincipal principal) {
        return adminAuthService.me(principal.getId());
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestAdminReset(request.email());
        return Map.of("message", "If an admin account exists for that email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetAdminPassword(request.email(), request.token(), request.password());
        return Map.of("message", "Your password has been reset. You can now log in.");
    }
}
