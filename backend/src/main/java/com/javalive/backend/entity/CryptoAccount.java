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
 * Per-user internal crypto balances (one row per user, enforced by a unique constraint on user_id).
 */
@Entity
@Table(name = "crypto_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "btc", nullable = false, precision = 30, scale = 10)
    private BigDecimal btc;

    @Column(name = "eth", nullable = false, precision = 30, scale = 10)
    private BigDecimal eth;

    @Column(name = "ltc", nullable = false, precision = 30, scale = 10)
    private BigDecimal ltc;

    @Column(name = "xrp", nullable = false, precision = 30, scale = 10)
    private BigDecimal xrp;

    @Column(name = "link", nullable = false, precision = 30, scale = 10)
    private BigDecimal link;

    @Column(name = "bnb", nullable = false, precision = 30, scale = 10)
    private BigDecimal bnb;

    @Column(name = "aave", nullable = false, precision = 30, scale = 10)
    private BigDecimal aave;

    @Column(name = "usdt", nullable = false, precision = 30, scale = 10)
    private BigDecimal usdt;

    @Column(name = "xlm", nullable = false, precision = 30, scale = 10)
    private BigDecimal xlm;

    @Column(name = "bch", nullable = false, precision = 30, scale = 10)
    private BigDecimal bch;

    @Column(name = "ada", nullable = false, precision = 30, scale = 10)
    private BigDecimal ada;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CryptoAccount other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
