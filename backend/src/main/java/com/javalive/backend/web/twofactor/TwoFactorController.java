package com.javalive.backend.web.twofactor;

import com.javalive.backend.dto.twofactor.PasswordConfirmRequest;
import com.javalive.backend.dto.twofactor.TwoFactorConfirmRequest;
import com.javalive.backend.dto.twofactor.TwoFactorRecoveryCodesResponse;
import com.javalive.backend.dto.twofactor.TwoFactorSetupResponse;
import com.javalive.backend.dto.twofactor.TwoFactorStatusResponse;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.twofactor.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @GetMapping("/status")
    public TwoFactorStatusResponse status(@AuthenticationPrincipal UserPrincipal principal) {
        return twoFactorService.status(principal.getId());
    }

    @PostMapping("/setup")
    public TwoFactorSetupResponse setup(@AuthenticationPrincipal UserPrincipal principal) {
        return twoFactorService.setup(principal.getId());
    }

    @PostMapping("/confirm")
    public TwoFactorRecoveryCodesResponse confirm(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody TwoFactorConfirmRequest request) {
        return twoFactorService.confirm(principal.getId(), request.code());
    }

    @PostMapping("/disable")
    public void disable(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PasswordConfirmRequest request) {
        twoFactorService.disable(principal.getId(), request.password());
    }

    @PostMapping("/recovery-codes/regenerate")
    public TwoFactorRecoveryCodesResponse regenerateRecoveryCodes(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody PasswordConfirmRequest request) {
        return twoFactorService.regenerateRecoveryCodes(principal.getId(), request.password());
    }
}
