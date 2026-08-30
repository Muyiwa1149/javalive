package com.javalive.backend.web.trade;

import com.javalive.backend.dto.trade.InstrumentDetail;
import com.javalive.backend.dto.trade.InstrumentSummary;
import com.javalive.backend.dto.trade.PlaceTradeRequest;
import com.javalive.backend.dto.trade.TradeMonitorDetail;
import com.javalive.backend.dto.trade.UserTradeSummary;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.trade.TradeService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/instruments")
    public List<InstrumentSummary> instruments(@RequestParam(required = false) String type) {
        return tradeService.listInstruments(type);
    }

    @GetMapping("/search")
    public List<InstrumentSummary> search(@RequestParam(required = false) String q, @RequestParam(required = false) String type) {
        return tradeService.search(q, type);
    }

    @GetMapping("/instruments/{id}")
    public InstrumentDetail instrumentDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return tradeService.instrumentDetail(principal.getId(), id);
    }

    @PostMapping
    public UserTradeSummary placeTrade(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PlaceTradeRequest request) {
        return tradeService.placeTrade(principal.getId(), request);
    }

    @GetMapping("/{tradeId}/monitor")
    public TradeMonitorDetail monitor(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tradeId) {
        return tradeService.monitor(principal.getId(), tradeId);
    }
}
