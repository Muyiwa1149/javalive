package com.javalive.backend.web.auth;

import com.javalive.backend.dto.auth.AuthResponse;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.dto.auth.RegisterRequest;
import com.javalive.backend.dto.auth.UserSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
