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
@Table(name = "copy_trading_experts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyTradingExpert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tag")
    private String tag;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "photo")
    private String photo;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "followers", nullable = false)
    private Integer followers;

    @Column(name = "equity", nullable = false)
    private BigDecimal equity;

    @Column(name = "total_profit", nullable = false)
    private BigDecimal totalProfit;

    /** 'active'/'inactive'. */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "win_rate", nullable = false)
    private Integer winRate;

    @Column(name = "total_trades", nullable = false)
    private Integer totalTrades;

    /** Copy-in fee, if any. */
    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CopyTradingExpert that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
