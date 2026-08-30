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
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "active")
    private String active;

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

    @Column(name = "profit_withdrawn", nullable = false, precision = 20, scale = 8)
    private BigDecimal profitWithdrawn;

    @Column(name = "withdrawal_disabled", nullable = false)
    private Boolean withdrawalDisabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Investment other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
