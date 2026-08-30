package com.javalive.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    /** legacy `assets`/`symbol` */
    @Column(name = "asset_symbol")
    private String assetSymbol;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "leverage")
    private Integer leverage;

    /** Buy/Sell leveraged trade side */
    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private String active;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "inv_duration")
    private String invDuration;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "last_growth")
    private LocalDateTime lastGrowth;

    @Column(name = "profit_earned", nullable = false, precision = 20, scale = 8)
    private BigDecimal profitEarned;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPlan other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
