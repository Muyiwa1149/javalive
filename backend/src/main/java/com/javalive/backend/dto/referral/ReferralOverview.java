package com.javalive.backend.dto.referral;

import java.math.BigDecimal;
import java.util.List;

public record ReferralOverview(
        String referralLink, String username, long directReferralCount, BigDecimal referralEarnings,
        BigDecimal commissionPct, List<DownlineMember> downline
) {
}
