package com.javalive.backend.dto.mt4;

import com.javalive.backend.entity.Mt4Detail;

import java.time.LocalDateTime;

/** Never includes the password — it stays encrypted server-side. */
public record Mt4DetailSummary(
        Long id, String mt4Id, String accountName, String accountType, String currency, String leverage,
        String server, String duration, String status, LocalDateTime startDate, LocalDateTime endDate
) {
    public static Mt4DetailSummary from(Mt4Detail m) {
        return new Mt4DetailSummary(m.getId(), m.getMt4Id(), m.getAccountName(), m.getAccountType(),
                m.getCurrency(), m.getLeverage(), m.getServer(), m.getDuration(), m.getStatus(),
                m.getStartDate(), m.getEndDate());
    }
}
