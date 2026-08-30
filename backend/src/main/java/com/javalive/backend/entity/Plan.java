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
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tag")
    private String tag;

    @Column(name = "type")
    private String type;

    @Column(name = "price", precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "min_price", precision = 20, scale = 8)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 20, scale = 8)
    private BigDecimal maxPrice;

    /** legacy `minr` */
    @Column(name = "min_return_pct", precision = 10, scale = 4)
    private BigDecimal minReturnPct;

    /** legacy `maxr` */
    @Column(name = "max_return_pct", precision = 10, scale = 4)
    private BigDecimal maxReturnPct;

    @Column(name = "gift", precision = 20, scale = 8)
    private BigDecimal gift;

    @Column(name = "expected_return")
    private String expectedReturn;

    @Column(name = "increment_interval")
    private String incrementInterval;

    @Column(name = "increment_type")
    private String incrementType;

    @Column(name = "increment_amount", precision = 20, scale = 8)
    private BigDecimal incrementAmount;

    @Column(name = "expiration_days")
    private Integer expirationDays;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
