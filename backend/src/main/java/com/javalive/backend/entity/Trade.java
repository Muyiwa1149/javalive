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
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.javalive.backend.entity.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    /** 'buy' or 'sell'. */
    @Column(name = "side", nullable = false, length = 4)
    private String side;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "leverage", nullable = false)
    private BigDecimal leverage;

    @Column(name = "entry_price", nullable = false)
    private BigDecimal entryPrice;

    @Column(name = "exit_price")
    private BigDecimal exitPrice;

    @Column(name = "pnl")
    private BigDecimal pnl;

    /** 'open' or 'closed'. */
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "stop_loss")
    private BigDecimal stopLoss;

    @Column(name = "take_profit")
    private BigDecimal takeProfit;

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
        if (!(o instanceof Trade that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
