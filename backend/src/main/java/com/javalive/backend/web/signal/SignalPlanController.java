package com.javalive.backend.web.signal;

import com.javalive.backend.dto.signal.SignalPlanSummary;
import com.javalive.backend.dto.signal.UserSignalSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.signal.SignalPlanService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/signals")
public class SignalPlanController {

    private final SignalPlanService signalPlanService;

    public SignalPlanController(SignalPlanService signalPlanService) {
        this.signalPlanService = signalPlanService;
    }

    @GetMapping("/plans")
    public List<SignalPlanSummary> plans() {
        return signalPlanService.availablePlans();
    }

    @GetMapping("/mine")
    public List<UserSignalSummary> mine(@AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) String status) {
        return signalPlanService.mySignals(principal.getId(), status);
    }
}
