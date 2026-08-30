package com.javalive.backend.web.deposit;

import com.javalive.backend.dto.deposit.AdminDepositSummary;
import com.javalive.backend.service.deposit.DepositService;
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        depositService.delete(id);
    }
}
