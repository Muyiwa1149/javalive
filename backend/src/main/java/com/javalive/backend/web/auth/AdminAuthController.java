package com.javalive.backend.web.auth;

import com.javalive.backend.dto.auth.AdminLoginResponse;
import com.javalive.backend.dto.auth.AdminSummary;
import com.javalive.backend.dto.auth.AdminTwoFactorRequest;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.security.AdminPrincipal;
import com.javalive.backend.service.auth.AdminAuthService;
import com.javalive.backend.web.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
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
}
