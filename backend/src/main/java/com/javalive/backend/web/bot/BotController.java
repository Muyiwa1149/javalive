package com.javalive.backend.web.bot;

import com.javalive.backend.dto.bot.BotDashboard;
import com.javalive.backend.dto.bot.BotDetail;
import com.javalive.backend.dto.bot.BotInvestmentSummary;
import com.javalive.backend.dto.bot.BotSummary;
import com.javalive.backend.dto.bot.BotTradeSummary;
import com.javalive.backend.dto.bot.InvestBotRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.bot.BotService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bots")
public class BotController {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @GetMapping
    public List<BotSummary> listBots(@AuthenticationPrincipal UserPrincipal principal) {
        return botService.listBots(principal.getId());
    }

    @GetMapping("/dashboard")
    public BotDashboard dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return botService.dashboard(principal.getId());
    }

    @GetMapping("/{id}")
    public BotDetail botDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return botService.botDetail(principal.getId(), id);
    }

    @PostMapping("/{id}/invest")
    public BotInvestmentSummary invest(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
                                        @Valid @RequestBody InvestBotRequest request) {
        return botService.invest(principal.getId(), id, request);
    }

    @PostMapping("/investments/{investmentId}/cancel")
    public void cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long investmentId) {
        botService.cancel(principal.getId(), investmentId);
    }

    @GetMapping("/investments/{investmentId}/history")
    public List<BotTradeSummary> history(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long investmentId) {
        return botService.history(principal.getId(), investmentId);
    }
}
