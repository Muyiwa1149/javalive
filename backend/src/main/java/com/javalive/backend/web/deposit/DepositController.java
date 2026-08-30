package com.javalive.backend.web.deposit;

import com.javalive.backend.dto.deposit.DepositMethodSummary;
import com.javalive.backend.dto.deposit.DepositSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.deposit.DepositService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @GetMapping("/methods")
    public List<DepositMethodSummary> methods() {
        return depositService.listMethods();
    }

    @GetMapping
    public List<DepositSummary> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return depositService.myDeposits(principal.getId());
    }

    @PostMapping(consumes = "multipart/form-data")
    public DepositSummary submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long methodId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String txnId,
            @RequestParam MultipartFile proof) {
        return depositService.submit(principal.getId(), methodId, amount, txnId, proof);
    }
}
