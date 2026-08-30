package com.javalive.backend.web.copytrading;

import com.javalive.backend.dto.copytrading.CopyTradeAnalytics;
import com.javalive.backend.dto.copytrading.CopyTradeSummary;
import com.javalive.backend.dto.copytrading.CopyTradingDashboard;
import com.javalive.backend.dto.copytrading.ExpertSummary;
import com.javalive.backend.dto.copytrading.SimulatedTrade;
import com.javalive.backend.dto.copytrading.StartCopyTradingRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.copytrading.CopyTradingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/copy-trading")
public class CopyTradingController {

    private final CopyTradingService copyTradingService;

    public CopyTradingController(CopyTradingService copyTradingService) {
        this.copyTradingService = copyTradingService;
    }

    @GetMapping("/dashboard")
    public CopyTradingDashboard dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return copyTradingService.dashboard(principal.getId());
    }

    @GetMapping("/experts")
    public List<ExpertSummary> experts(@AuthenticationPrincipal UserPrincipal principal) {
        return copyTradingService.experts(principal.getId());
    }

    @PostMapping("/start")
    public CopyTradeSummary start(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody StartCopyTradingRequest request) {
        return copyTradingService.start(principal.getId(), request);
    }

    @PostMapping("/{id}/stop")
    public void stop(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        copyTradingService.stop(principal.getId(), id);
    }

    @GetMapping("/{id}/activity")
    public List<SimulatedTrade> recentActivity(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return copyTradingService.recentActivity(principal.getId(), id);
    }

    @GetMapping("/{id}/analytics")
    public CopyTradeAnalytics analytics(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        CopyTradeSummary summary = copyTradingService.analytics(principal.getId(), id);
        return new CopyTradeAnalytics(summary, copyTradingService.daysActive(summary));
    }
}
