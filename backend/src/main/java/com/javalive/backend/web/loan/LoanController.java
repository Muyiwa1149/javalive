package com.javalive.backend.web.loan;

import com.javalive.backend.dto.loan.LoanApplicationRequest;
import com.javalive.backend.dto.loan.LoanSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.loan.LoanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanSummary> myLoans(@AuthenticationPrincipal UserPrincipal principal) {
        return loanService.myLoans(principal.getId());
    }

    @PostMapping
    public LoanSummary apply(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody LoanApplicationRequest request) {
        return loanService.apply(principal.getId(), request);
    }
}
