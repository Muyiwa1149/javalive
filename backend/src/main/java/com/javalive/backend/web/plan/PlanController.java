package com.javalive.backend.web.plan;

import com.javalive.backend.dto.plan.InvestmentDetail;
import com.javalive.backend.dto.plan.InvestmentSummary;
import com.javalive.backend.dto.plan.PlanSummary;
import com.javalive.backend.dto.plan.PurchasePlanRequest;
import com.javalive.backend.dto.plan.WithdrawProfitRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.plan.PlanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/api/plans")
    public List<PlanSummary> listPlans(@RequestParam(required = false) String type) {
        return planService.listPlans(type);
    }

    @GetMapping("/api/investments")
    public List<InvestmentSummary> myInvestments(@AuthenticationPrincipal UserPrincipal principal,
                                                  @RequestParam(required = false) String active) {
        return planService.myInvestments(principal.getId(), active);
    }

    @GetMapping("/api/investments/{id}")
    public InvestmentDetail investmentDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return planService.investmentDetail(principal.getId(), id);
    }

    @PostMapping("/api/investments")
    public InvestmentSummary purchase(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PurchasePlanRequest request) {
        return planService.purchase(principal.getId(), request);
    }

    @PostMapping("/api/investments/{id}/cancel")
    public InvestmentSummary cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return planService.cancel(principal.getId(), id);
    }

    @PostMapping("/api/investments/{id}/withdraw-profit")
    public InvestmentSummary withdrawProfit(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                             @Valid @RequestBody WithdrawProfitRequest request) {
        return planService.withdrawProfit(principal.getId(), id, request);
    }
}
