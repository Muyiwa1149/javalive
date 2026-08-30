package com.javalive.backend.dto.plan;

import com.javalive.backend.dto.dashboard.DashboardActivitySummary;

import java.util.List;

public record InvestmentDetail(InvestmentSummary investment, List<DashboardActivitySummary> transactions) {
}
