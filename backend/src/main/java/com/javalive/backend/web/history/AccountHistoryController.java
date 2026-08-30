package com.javalive.backend.web.history;

import com.javalive.backend.dto.history.AccountHistory;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.history.AccountHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account-history")
public class AccountHistoryController {

    private final AccountHistoryService accountHistoryService;

    public AccountHistoryController(AccountHistoryService accountHistoryService) {
        this.accountHistoryService = accountHistoryService;
    }

    @GetMapping
    public AccountHistory history(@AuthenticationPrincipal UserPrincipal principal) {
        return accountHistoryService.history(principal.getId());
    }
}
