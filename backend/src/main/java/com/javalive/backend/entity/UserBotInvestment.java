package com.javalive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_bot_investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBotInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.javalive.backend.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private TradingBot bot;

    @Column(name = "investment_amount", nullable = false)
    private BigDecimal investmentAmount;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "total_profit", nullable = false)
    private BigDecimal totalProfit;

    @Column(name = "total_loss", nullable = false)
    private BigDecimal totalLoss;

    @Column(name = "successful_trades", nullable = false)
    private Integer successfulTrades;

    @Column(name = "failed_trades", nullable = false)
    private Integer failedTrades;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_profit_at")
    private LocalDateTime lastProfitAt;

    /** 'active'/'completed'/'cancelled'/'expired'. */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "auto_reinvest", nullable = false)
    private Boolean autoReinvest;

    @Column(name = "reinvest_percentage", nullable = false)
    private BigDecimal reinvestPercentage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBotInvestment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
