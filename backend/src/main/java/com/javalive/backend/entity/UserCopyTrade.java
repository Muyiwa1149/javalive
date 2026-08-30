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
@Table(name = "user_copy_trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCopyTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.javalive.backend.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private CopyTradingExpert expert;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "active")
    private String active;

    @Column(name = "type")
    private String type;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_profit_at")
    private LocalDateTime lastProfitAt;

    @Column(name = "total_profit", nullable = false)
    private BigDecimal totalProfit;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "total_trades", nullable = false)
    private Integer totalTrades;

    @Column(name = "winning_trades", nullable = false)
    private Integer winningTrades;

    @Column(name = "profit_percentage", nullable = false)
    private BigDecimal profitPercentage;

    @Column(name = "equity")
    private BigDecimal equity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCopyTrade that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
