package com.javalive.backend.web.referral;

import com.javalive.backend.dto.referral.ReferralOverview;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.referral.ReferralService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @GetMapping
    public ReferralOverview overview(@AuthenticationPrincipal UserPrincipal principal) {
        return referralService.overview(principal.getId());
    }
}
