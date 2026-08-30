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
 * `loans` had no Laravel migration at all in the source app (raw DB::table usage) — schema
 * reconstructed from SQL/keystone.sql's actual live structure.
 */
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "facility")
    private String facility;

    @Column(name = "duration")
    private String duration;

    @Column(name = "purpose", columnDefinition = "LONGTEXT")
    private String purpose;

    @Column(name = "income")
    private String income;

    @Column(name = "inv_duration")
    private String invDuration;

    @Column(name = "active")
    private String active;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "last_growth")
    private LocalDateTime lastGrowth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
