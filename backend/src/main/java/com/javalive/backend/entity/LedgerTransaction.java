package com.javalive.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Universal ledger (legacy {@code tp__transactions}) — every ROI/buy/sell/win/lose/bonus/transfer/referral entry.
 */
@Entity
@Table(name = "ledger_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** legacy free-text `plan` column */
    @Column(name = "plan_label", length = 250)
    private String planLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plan_id")
    private UserPlan userPlan;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    /** ROI/Buy/Sell/WIN/LOSE/Ref_bonus/Transfer/... */
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "leverage")
    private Integer leverage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LedgerTransaction other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
