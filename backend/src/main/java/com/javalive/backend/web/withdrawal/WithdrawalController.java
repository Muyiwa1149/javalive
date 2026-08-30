package com.javalive.backend.web.withdrawal;

import com.javalive.backend.dto.withdrawal.SubmitWithdrawalRequest;
import com.javalive.backend.dto.withdrawal.WithdrawalMethodSummary;
import com.javalive.backend.dto.withdrawal.WithdrawalSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.withdrawal.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping("/methods")
    public List<WithdrawalMethodSummary> methods() {
        return withdrawalService.listMethods();
    }

    @GetMapping
    public List<WithdrawalSummary> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return withdrawalService.myWithdrawals(principal.getId());
    }

    @PostMapping("/otp")
    public Map<String, String> requestOtp(@AuthenticationPrincipal UserPrincipal principal) {
        return withdrawalService.requestOtp(principal.getId());
    }

    @PostMapping
    public WithdrawalSummary submit(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SubmitWithdrawalRequest request) {
        return withdrawalService.submit(principal.getId(), request);
    }
}
