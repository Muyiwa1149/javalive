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
@Table(name = "bot_trading_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotTradingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bot_investment_id", nullable = false)
    private UserBotInvestment userBotInvestment;

    @Column(name = "trade_type", nullable = false)
    private String tradeType;

    @Column(name = "trading_pair", nullable = false)
    private String tradingPair;

    @Column(name = "entry_price", nullable = false)
    private BigDecimal entryPrice;

    @Column(name = "exit_price")
    private BigDecimal exitPrice;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "profit_loss", nullable = false)
    private BigDecimal profitLoss;

    @Column(name = "profit_percentage", nullable = false)
    private BigDecimal profitPercentage;

    /** 'pending'/'profit'/'loss'. */
    @Column(name = "result", nullable = false, length = 10)
    private String result;

    @Column(name = "strategy_used")
    private String strategyUsed;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotTradingHistory that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
