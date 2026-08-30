package com.javalive.backend.dto.trade;

import com.javalive.backend.entity.UserPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserTradeSummary(
        Long id, String assetSymbol, BigDecimal amount, Integer leverage, String type, String active,
        String invDuration, LocalDateTime expireDate, LocalDateTime activatedAt, BigDecimal profitEarned
) {
    public static UserTradeSummary from(UserPlan p) {
        return new UserTradeSummary(p.getId(), p.getAssetSymbol(), p.getAmount(), p.getLeverage(), p.getType(),
                p.getActive(), p.getInvDuration(), p.getExpireDate(), p.getActivatedAt(), p.getProfitEarned());
    }
}
