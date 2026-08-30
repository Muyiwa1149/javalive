package com.javalive.backend.web.auth;

import com.javalive.backend.dto.auth.AuthResponse;
import com.javalive.backend.dto.auth.ConfirmPasswordRequest;
import com.javalive.backend.dto.auth.ForgotPasswordRequest;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.dto.auth.RegisterRequest;
import com.javalive.backend.dto.auth.ResetPasswordRequest;
import com.javalive.backend.dto.auth.UserSummary;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.auth.AuthService;
import com.javalive.backend.service.auth.PasswordResetService;
import com.javalive.backend.web.exception.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, PasswordResetService passwordResetService,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserSummary me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.me(principal.getId());
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestUserReset(request.email());
        return Map.of("message", "If an account exists for that email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetUserPassword(request.email(), request.token(), request.password());
        return Map.of("message", "Your password has been reset. You can now log in.");
    }

    @PostMapping("/confirm-password")
    public Map<String, String> confirmPassword(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody ConfirmPasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect password.");
        }
        return Map.of("message", "Password confirmed.");
    }
}
