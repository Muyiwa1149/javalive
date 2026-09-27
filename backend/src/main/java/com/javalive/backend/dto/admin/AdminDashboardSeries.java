package com.javalive.backend.dto.admin;

import java.math.BigDecimal;
import java.util.List;

/** 30-day daily-totals trend for the admin dashboard's analytics chart. */
public record AdminDashboardSeries(List<AdminDailyPoint> deposits, List<AdminDailyPoint> withdrawals) {

    public record AdminDailyPoint(String date, BigDecimal total) {
    }
}
