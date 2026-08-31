package com.javalive.backend.web.wallet;

import com.javalive.backend.dto.wallet.ConnectWalletRequest;
import com.javalive.backend.dto.wallet.WalletStatus;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.wallet.WalletConnectService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet-connect")
public class WalletConnectController {

    private final WalletConnectService walletConnectService;

    public WalletConnectController(WalletConnectService walletConnectService) {
        this.walletConnectService = walletConnectService;
    }

    @GetMapping
    public WalletStatus status(@AuthenticationPrincipal UserPrincipal principal) {
        return walletConnectService.status(principal.getId());
    }

    @PostMapping
    public WalletStatus connect(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ConnectWalletRequest request) {
        return walletConnectService.connect(principal.getId(), request);
    }
}
