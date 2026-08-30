package com.javalive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Maps the `wdmethods` table (legacy name kept as-is by the migration).
@Entity
@Table(name = "wdmethods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WithdrawalMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    // bank/crypto
    @Column(name = "method_type")
    private String methodType;

    // deposit/withdrawal
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "minimum_amount", precision = 20, scale = 8)
    private BigDecimal minimumAmount;

    @Column(name = "maximum_amount", precision = 20, scale = 8)
    private BigDecimal maximumAmount;

    @Column(name = "charges_amount", precision = 20, scale = 8)
    private BigDecimal chargesAmount;

    // fixed/percentage
    @Column(name = "charges_type")
    private String chargesType;

    @Column(name = "duration_note")
    private String durationNote;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "barcode_image")
    private String barcodeImage;

    @Column(name = "network")
    private String network;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
