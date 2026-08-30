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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    // Single full-name field — the live DB has no last-name column despite the model fillable list mentioning one.
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "two_factor_recovery_codes")
    private String twoFactorRecoveryCodes;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "notify")
    private String notify;

    // legacy cstatus: Lead/Customer
    @Column(name = "customer_status")
    private String customerStatus;

    @Column(name = "trade_type", nullable = false)
    private String tradeType;

    @Column(name = "number_of_trades", nullable = false)
    private Integer numberOfTrades;

    // Legacy `assign_to` — free-text in the real data (not a real FK id), kept as-is.
    @Column(name = "assigned_agent")
    private String assignedAgent;

    @Column(name = "address")
    private String address;

    @Column(name = "country")
    private String country;

    @Column(name = "phone")
    private String phone;

    @Column(name = "dashboard_style", nullable = false)
    private String dashboardStyle;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_swift_code")
    private String bankSwiftCode;

    @Column(name = "btc_address")
    private String btcAddress;

    @Column(name = "eth_address")
    private String ethAddress;

    @Column(name = "ltc_address")
    private String ltcAddress;

    @Column(name = "usdt_address")
    private String usdtAddress;

    @Column(name = "account_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal accountBalance;

    @Column(name = "roi_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal roiBalance;

    @Column(name = "bonus_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal bonusBalance;

    @Column(name = "referral_bonus_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal referralBonusBalance;

    @Column(name = "signup_bonus", nullable = false, precision = 20, scale = 8)
    private BigDecimal signupBonus;

    @Column(name = "bonus_released", nullable = false)
    private Boolean bonusReleased;

    // legacy ref_by
    @Column(name = "referred_by_code")
    private String referredByCode;

    // legacy ref_link
    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "tax_type", nullable = false)
    private String taxType;

    // Verified/Pending/Rejected
    @Column(name = "account_verify_status")
    private String accountVerifyStatus;

    // active/blocked
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "trade_mode", nullable = false)
    private String tradeMode;

    @Column(name = "remember_token")
    private String rememberToken;

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    @Column(name = "withdraw_otp")
    private String withdrawOtp;

    @Column(name = "send_otp_email", nullable = false)
    private Boolean sendOtpEmail;

    @Column(name = "send_roi_email", nullable = false)
    private Boolean sendRoiEmail;

    @Column(name = "send_promo_email", nullable = false)
    private Boolean sendPromoEmail;

    @Column(name = "send_inv_plan_email", nullable = false)
    private Boolean sendInvPlanEmail;

    @Column(name = "signal_status", nullable = false)
    private String signalStatus;

    @Column(name = "plan_status", nullable = false)
    private String planStatus;

    @Column(name = "withdrawal_code_status", nullable = false)
    private String withdrawalCodeStatus;

    @Column(name = "user_withdrawal_code")
    private String userWithdrawalCode;

    // legacy `progress` cosmetic field
    @Column(name = "copy_trading_progress", nullable = false)
    private Integer copyTradingProgress;

    @Column(name = "wallet_connected", nullable = false)
    private Boolean walletConnected;

    // legacy `signals` truthy flag — gates the "My Signals" nav link in dasht.blade.php
    @Column(name = "has_signal_subscription", nullable = false)
    private Boolean hasSignalSubscription;

    // Legacy scratch fields preserved as-is (see V1 migration comment) — resolve real usage in Phase 4.
    @Column(name = "legacy_plan_field")
    private String legacyPlanField;

    @Column(name = "legacy_trades_field")
    private String legacyTradesField;

    @Column(name = "legacy_user_plan_field")
    private String legacyUserPlanField;

    @Column(name = "legacy_user_signal_field")
    private String legacyUserSignalField;

    @Column(name = "legacy_user_plan_upgrade_field")
    private String legacyUserPlanUpgradeField;

    @Column(name = "legacy_trade_field", nullable = false)
    private Integer legacyTradeField;

    @Column(name = "legacy_copy_field")
    private String legacyCopyField;

    @Column(name = "legacy_copy_plan_field")
    private String legacyCopyPlanField;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
