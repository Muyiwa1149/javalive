package com.javalive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** 'forex'/'crypto'/'stocks'/'commodities'/'indices'. */
    @Column(name = "bot_type", nullable = false)
    private String botType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "min_investment", nullable = false)
    private BigDecimal minInvestment;

    @Column(name = "max_investment", nullable = false)
    private BigDecimal maxInvestment;

    @Column(name = "daily_profit_min", nullable = false)
    private BigDecimal dailyProfitMin;

    @Column(name = "daily_profit_max", nullable = false)
    private BigDecimal dailyProfitMax;

    @Column(name = "success_rate", nullable = false)
    private Integer successRate;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "total_earned", nullable = false)
    private BigDecimal totalEarned;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers;

    /** 'active'/'inactive'/'maintenance'. */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Raw JSON text. */
    @Column(name = "trading_pairs")
    private String tradingPairs;

    /** Raw JSON text. */
    @Column(name = "risk_settings")
    private String riskSettings;

    /** Raw JSON text. */
    @Column(name = "strategy_details")
    private String strategyDetails;

    @Column(name = "last_trade_at")
    private LocalDateTime lastTradeAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradingBot that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
