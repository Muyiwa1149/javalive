package com.javalive.backend.web.transfer;

import com.javalive.backend.dto.dashboard.DashboardActivitySummary;
import com.javalive.backend.dto.transfer.TransferRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.transfer.TransferService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public Map<String, String> transfer(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TransferRequest request) {
        transferService.transfer(principal.getId(), request);
        return Map.of("message", "Transfer Completed!");
    }

    @GetMapping("/history")
    public List<DashboardActivitySummary> history(@AuthenticationPrincipal UserPrincipal principal) {
        return transferService.history(principal.getId());
    }
}
