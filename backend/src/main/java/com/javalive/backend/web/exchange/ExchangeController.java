package com.javalive.backend.web.exchange;

import com.javalive.backend.dto.exchange.AssetBalance;
import com.javalive.backend.dto.exchange.ExchangeQuoteResponse;
import com.javalive.backend.dto.exchange.ExchangeRecordSummary;
import com.javalive.backend.dto.exchange.ExchangeRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.exchange.ExchangeService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/balances")
    public List<AssetBalance> balances(@AuthenticationPrincipal UserPrincipal principal) {
        return exchangeService.assetBalances(principal.getId());
    }

    @GetMapping("/quote")
    public ExchangeQuoteResponse quote(@RequestParam String source, @RequestParam String destination, @RequestParam BigDecimal amount) {
        return exchangeService.quote(source, destination, amount);
    }

    @PostMapping
    public ExchangeRecordSummary exchange(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ExchangeRequest request) {
        return exchangeService.exchange(principal.getId(), request);
    }

    @GetMapping("/history")
    public List<ExchangeRecordSummary> history(@AuthenticationPrincipal UserPrincipal principal) {
        return exchangeService.history(principal.getId());
    }
}
