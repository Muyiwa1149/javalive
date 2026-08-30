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
@Table(name = "withdrawals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txn_id")
    private String txnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_plan_id")
    private UserPlan userPlan;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "to_deduct", precision = 20, scale = 8)
    private BigDecimal toDeduct;

    /** Pending/Processed/Rejected */
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "pay_details", columnDefinition = "TEXT")
    private String payDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Withdrawal other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
