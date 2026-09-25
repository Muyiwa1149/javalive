package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminInvestmentSummary;
import com.javalive.backend.dto.admin.AdminInvestmentUpdateRequest;
import com.javalive.backend.dto.admin.AdminPlanRequest;
import com.javalive.backend.dto.admin.AdminPlanSummary;
import com.javalive.backend.entity.Investment;
import com.javalive.backend.entity.Plan;
import com.javalive.backend.repository.InvestmentRepository;
import com.javalive.backend.repository.PlanRepository;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's legacy Plans admin surface — {@code HomeController@plans} (list) + {@code InvPlanController} (CRUD), per CANONICAL-MODULES.md. */
@Service
public class AdminPlanService {

    private final PlanRepository planRepository;
    private final InvestmentRepository investmentRepository;

    public AdminPlanService(PlanRepository planRepository, InvestmentRepository investmentRepository) {
        this.planRepository = planRepository;
        this.investmentRepository = investmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminPlanSummary> list() {
        return planRepository.findAllByOrderByIdDesc().stream().map(AdminPlanSummary::from).toList();
    }

    @Transactional
    public AdminPlanSummary create(AdminPlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.name()).tag(request.tag()).type("Main")
                .price(request.price()).minPrice(request.minPrice()).maxPrice(request.maxPrice())
                .minReturnPct(request.minReturnPct()).maxReturnPct(request.maxReturnPct()).gift(request.gift())
                .expectedReturn(request.expectedReturn()).incrementType(request.incrementType())
                .incrementInterval(request.incrementInterval()).incrementAmount(request.incrementAmount())
                .expirationDays(request.expirationDays()).active(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        return AdminPlanSummary.from(planRepository.save(plan));
    }

    @Transactional
    public AdminPlanSummary update(Long id, AdminPlanRequest request) {
        Plan plan = getPlan(id);
        plan.setName(request.name());
        plan.setTag(request.tag());
        plan.setPrice(request.price());
        plan.setMinPrice(request.minPrice());
        plan.setMaxPrice(request.maxPrice());
        plan.setMinReturnPct(request.minReturnPct());
        plan.setMaxReturnPct(request.maxReturnPct());
        plan.setGift(request.gift());
        plan.setExpectedReturn(request.expectedReturn());
        plan.setIncrementType(request.incrementType());
        plan.setIncrementInterval(request.incrementInterval());
        plan.setIncrementAmount(request.incrementAmount());
        plan.setExpirationDays(request.expirationDays());
        plan.setUpdatedAt(LocalDateTime.now());
        return AdminPlanSummary.from(planRepository.save(plan));
    }

    @Transactional
    public AdminPlanSummary toggleActive(Long id) {
        Plan plan = getPlan(id);
        plan.setActive(!Boolean.TRUE.equals(plan.getActive()));
        plan.setUpdatedAt(LocalDateTime.now());
        return AdminPlanSummary.from(planRepository.save(plan));
    }

    /** Mirrors source's {@code trashplan} — also removes every investment referencing this plan. */
    @Transactional
    public void delete(Long id) {
        Plan plan = getPlan(id);
        investmentRepository.deleteAll(investmentRepository.findByPlanId(id));
        planRepository.delete(plan);
    }

    @Transactional(readOnly = true)
    public List<AdminInvestmentSummary> activeInvestments() {
        return investmentRepository.findByActiveWithUserAndPlanOrderByIdDesc("yes")
                .stream().map(AdminInvestmentSummary::from).toList();
    }

    /** Full admin correction of an investment — amount, plan (which carries the ROI rate/interval), every date, and the accumulated profit figures. */
    @Transactional
    public AdminInvestmentSummary updateInvestment(Long id, AdminInvestmentUpdateRequest request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Investment not found."));

        if (request.planId() != null) {
            investment.setPlan(getPlan(request.planId()));
        }
        investment.setAmount(request.amount());
        investment.setActive(request.active());
        investment.setInvDuration(request.invDuration());
        investment.setActivatedAt(request.activatedAt());
        investment.setExpireDate(request.expireDate());
        investment.setLastGrowth(request.lastGrowth());
        investment.setProfitEarned(request.profitEarned());
        investment.setProfitWithdrawn(request.profitWithdrawn());
        investment.setWithdrawalDisabled(request.withdrawalDisabled());
        investment.setUpdatedAt(LocalDateTime.now());

        return AdminInvestmentSummary.from(investmentRepository.save(investment));
    }

    private Plan getPlan(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan not found."));
    }
}
