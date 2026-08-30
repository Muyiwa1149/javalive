package com.javalive.backend.dto.copytrading;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Illustrative recent-activity feed for a copy position — the source app generates this the same way (randomized, not real broker fills). */
public record SimulatedTrade(int id, String pair, BigDecimal profitLoss, LocalDateTime createdAt) {
}
