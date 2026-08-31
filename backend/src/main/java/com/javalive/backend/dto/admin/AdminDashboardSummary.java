package com.javalive.backend.dto.admin;

import java.math.BigDecimal;

/** Mirrors the source app's {@code HomeController@index} — 8 stat cards + the 5-bar system chart. */
public record AdminDashboardSummary(
        BigDecimal totalDeposited,
        BigDecimal pendingDeposited,
        BigDecimal totalWithdrawn,
        BigDecimal pendingWithdrawn,
        long userCount,
        long activeUsers,
        long blockedUsers,
        long planCount,
        BigDecimal chartDeposits,
        BigDecimal chartPendingDeposits,
        BigDecimal chartWithdrawals,
        BigDecimal chartPendingWithdrawals,
        BigDecimal chartTransactions
) {
}
