package com.javalive.backend.web.mt4;

import com.javalive.backend.dto.mt4.Mt4DetailSummary;
import com.javalive.backend.dto.mt4.Mt4SubscriptionRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.mt4.Mt4SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mt4")
public class Mt4SubscriptionController {

    private final Mt4SubscriptionService mt4SubscriptionService;

    public Mt4SubscriptionController(Mt4SubscriptionService mt4SubscriptionService) {
        this.mt4SubscriptionService = mt4SubscriptionService;
    }

    @GetMapping
    public List<Mt4DetailSummary> mySubscriptions(@AuthenticationPrincipal UserPrincipal principal) {
        return mt4SubscriptionService.mySubscriptions(principal.getId());
    }

    @PostMapping
    public Mt4DetailSummary save(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody Mt4SubscriptionRequest request) {
        return mt4SubscriptionService.save(principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        mt4SubscriptionService.delete(principal.getId(), id);
    }

    @PostMapping("/{id}/renew")
    public Mt4DetailSummary renew(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return mt4SubscriptionService.renew(principal.getId(), id);
    }
}
