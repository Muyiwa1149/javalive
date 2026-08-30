package com.javalive.backend.service.signal;

import com.javalive.backend.dto.signal.SignalPlanSummary;
import com.javalive.backend.dto.signal.UserSignalSummary;
import com.javalive.backend.repository.SignalPlanRepository;
import com.javalive.backend.repository.UserSignalPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mirrors the source app's legacy signal-plan browsing (ViewsController@signal/mysingals) — a
 * "signal plan" a user can subscribe to, structurally like an investment plan. <b>Finding</b>:
 * confirmed by exhaustive search that no code path anywhere in the source app ever creates a
 * {@code User_signal} row — there is no purchase/subscribe endpoint, only browsing. This is
 * read-only here too, matching that reality rather than inventing a purchase flow the source
 * never had (same precedent as the dead email-verification flow).
 */
@Service
public class SignalPlanService {

    private final SignalPlanRepository signalPlanRepository;
    private final UserSignalPlanRepository userSignalPlanRepository;

    public SignalPlanService(SignalPlanRepository signalPlanRepository, UserSignalPlanRepository userSignalPlanRepository) {
        this.signalPlanRepository = signalPlanRepository;
        this.userSignalPlanRepository = userSignalPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<SignalPlanSummary> availablePlans() {
        return signalPlanRepository.findByType("main").stream().map(SignalPlanSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserSignalSummary> mySignals(Long userId, String status) {
        List<com.javalive.backend.entity.UserSignalPlan> plans = (status == null || status.isBlank() || "All".equalsIgnoreCase(status))
                ? userSignalPlanRepository.findByUserIdOrderByIdDesc(userId)
                : userSignalPlanRepository.findByUserIdAndStatusOrderByIdDesc(userId, status);
        return plans.stream().map(UserSignalSummary::from).toList();
    }
}
