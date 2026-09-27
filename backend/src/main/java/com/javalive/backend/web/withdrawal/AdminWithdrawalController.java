package com.javalive.backend.web.withdrawal;

import com.javalive.backend.dto.withdrawal.AdminWithdrawalSummary;
import com.javalive.backend.dto.withdrawal.AdminWithdrawalUpdateRequest;
import com.javalive.backend.dto.withdrawal.RejectWithdrawalRequest;
import com.javalive.backend.service.withdrawal.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdrawals")
public class AdminWithdrawalController {

    private final WithdrawalService withdrawalService;

    public AdminWithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping
    public List<AdminWithdrawalSummary> list(@RequestParam(required = false) String status) {
        return withdrawalService.adminList(status);
    }

    @PostMapping("/{id}/approve")
    public AdminWithdrawalSummary approve(@PathVariable Long id) {
        return withdrawalService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public AdminWithdrawalSummary reject(@PathVariable Long id, @RequestBody RejectWithdrawalRequest request) {
        return withdrawalService.reject(id, request);
    }

    @PutMapping("/{id}")
    public AdminWithdrawalSummary update(@PathVariable Long id, @Valid @RequestBody AdminWithdrawalUpdateRequest request) {
        return withdrawalService.update(id, request);
    }
}
