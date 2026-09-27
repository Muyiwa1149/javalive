package com.javalive.backend.web.deposit;

import com.javalive.backend.dto.deposit.AdminDepositSummary;
import com.javalive.backend.dto.deposit.AdminDepositUpdateRequest;
import com.javalive.backend.dto.deposit.RejectDepositRequest;
import com.javalive.backend.service.deposit.DepositService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/deposits")
public class AdminDepositController {

    private final DepositService depositService;

    public AdminDepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @GetMapping
    public List<AdminDepositSummary> list(@RequestParam(required = false) String status) {
        return depositService.adminList(status);
    }

    @PostMapping("/{id}/approve")
    public AdminDepositSummary approve(@PathVariable Long id) {
        return depositService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public AdminDepositSummary reject(@PathVariable Long id, @RequestBody RejectDepositRequest request) {
        return depositService.reject(id, request);
    }

    @PutMapping("/{id}")
    public AdminDepositSummary update(@PathVariable Long id, @Valid @RequestBody AdminDepositUpdateRequest request) {
        return depositService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        depositService.delete(id);
    }
}
