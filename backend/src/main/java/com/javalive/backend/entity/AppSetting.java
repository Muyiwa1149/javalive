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

// Single-row global settings table. Replaces the legacy `settings` + `settings_conts` +
// `paystacks` + `cp_transactions` tables, which were all separate single-row config tables.
@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_title")
    private String siteTitle;

    @Column(name = "site_address")
    private String siteAddress;

    @Column(name = "description")
    private String description;

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "logo")
    private String logo;

    @Column(name = "favicon")
    private String favicon;

    @Column(name = "default_currency_symbol")
    private String defaultCurrencySymbol;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "welcome_message")
    private String welcomeMessage;

    @Column(name = "google_translate_enabled", nullable = false)
    private Boolean googleTranslateEnabled;

    @Column(name = "weekend_trade_enabled", nullable = false)
    private Boolean weekendTradeEnabled;

    @Column(name = "trade_mode")
    private String tradeMode;

    @Column(name = "trading_winrate", nullable = false)
    private Integer tradingWinrate;

    // --- Mail ---
    @Column(name = "mail_server")
    private String mailServer;

    @Column(name = "mail_from_address")
    private String mailFromAddress;

    @Column(name = "mail_from_name")
    private String mailFromName;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private String smtpPort;

    @Column(name = "smtp_encryption")
    private String smtpEncryption;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password")
    private String smtpPassword;

    // --- Captcha / social login ---
    @Column(name = "captcha_provider")
    private String captchaProvider;

    @Column(name = "captcha_secret")
    private String captchaSecret;

    @Column(name = "captcha_site_key")
    private String captchaSiteKey;

    @Column(name = "social_login_enabled", nullable = false)
    private Boolean socialLoginEnabled;

    @Column(name = "google_client_id")
    private String googleClientId;

    @Column(name = "google_client_secret")
    private String googleClientSecret;

    @Column(name = "google_redirect_uri")
    private String googleRedirectUri;

    // --- KYC / 2FA / verification toggles ---
    @Column(name = "enable_2fa", nullable = false)
    private Boolean enable2fa;

    @Column(name = "enable_kyc", nullable = false)
    private Boolean enableKyc;

    @Column(name = "enable_kyc_registration", nullable = false)
    private Boolean enableKycRegistration;

    @Column(name = "enable_email_verification", nullable = false)
    private Boolean enableEmailVerification;

    // --- Referral / bonus ---
    @Column(name = "referral_commission_pct", precision = 6, scale = 3)
    private BigDecimal referralCommissionPct;

    @Column(name = "referral_commission_l1", precision = 6, scale = 3)
    private BigDecimal referralCommissionL1;

    @Column(name = "referral_commission_l2", precision = 6, scale = 3)
    private BigDecimal referralCommissionL2;

    @Column(name = "referral_commission_l3", precision = 6, scale = 3)
    private BigDecimal referralCommissionL3;

    @Column(name = "referral_commission_l4", precision = 6, scale = 3)
    private BigDecimal referralCommissionL4;

    @Column(name = "referral_commission_l5", precision = 6, scale = 3)
    private BigDecimal referralCommissionL5;

    @Column(name = "signup_bonus", precision = 20, scale = 8)
    private BigDecimal signupBonus;

    @Column(name = "deposit_bonus_pct", precision = 6, scale = 3)
    private BigDecimal depositBonusPct;

    // --- Payments ---
    // auto/manual
    @Column(name = "withdrawal_option", nullable = false)
    private String withdrawalOption;

    @Column(name = "deposit_option")
    private String depositOption;

    // userRequest/AdminApprove
    @Column(name = "deduction_option", nullable = false)
    private String deductionOption;

    @Column(name = "auto_merchant_option", nullable = false)
    private String autoMerchantOption;

    @Column(name = "credit_card_provider", nullable = false)
    private String creditCardProvider;

    @Column(name = "return_capital", nullable = false)
    private Boolean returnCapital;

    @Column(name = "should_cancel_plan", nullable = false)
    private Boolean shouldCancelPlan;

    @Column(name = "commission_type")
    private String commissionType;

    @Column(name = "commission_fee", precision = 10, scale = 2)
    private BigDecimal commissionFee;

    @Column(name = "min_return", nullable = false, precision = 20, scale = 8)
    private BigDecimal minReturn;

    @Column(name = "min_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal minBalance;

    /** 'enabled'/'disabled' — gates the wallet-connect feature. */
    @Column(name = "wallet_status", nullable = false, length = 20)
    private String walletStatus;

    // --- Subscription fees ---
    @Column(name = "subscription_monthly_fee", precision = 10, scale = 2)
    private BigDecimal subscriptionMonthlyFee;

    @Column(name = "subscription_quarterly_fee", precision = 10, scale = 2)
    private BigDecimal subscriptionQuarterlyFee;

    @Column(name = "subscription_yearly_fee", precision = 10, scale = 2)
    private BigDecimal subscriptionYearlyFee;

    // --- Stripe ---
    @Column(name = "stripe_secret_key")
    private String stripeSecretKey;

    @Column(name = "stripe_public_key")
    private String stripePublicKey;

    // --- PayPal ---
    @Column(name = "paypal_client_id")
    private String paypalClientId;

    @Column(name = "paypal_client_secret")
    private String paypalClientSecret;

    // --- Paystack ---
    @Column(name = "paystack_public_key")
    private String paystackPublicKey;

    @Column(name = "paystack_secret_key")
    private String paystackSecretKey;

    @Column(name = "paystack_url")
    private String paystackUrl;

    @Column(name = "paystack_email")
    private String paystackEmail;

    // --- Flutterwave ---
    @Column(name = "flutterwave_public_key")
    private String flutterwavePublicKey;

    @Column(name = "flutterwave_secret_key")
    private String flutterwaveSecretKey;

    @Column(name = "flutterwave_secret_hash")
    private String flutterwaveSecretHash;

    // --- Binance Pay ---
    @Column(name = "binance_api_key")
    private String binanceApiKey;

    @Column(name = "binance_secret_key")
    private String binanceSecretKey;

    // --- CoinPayments ---
    @Column(name = "coinpayments_public_key")
    private String coinpaymentsPublicKey;

    @Column(name = "coinpayments_private_key")
    private String coinpaymentsPrivateKey;

    @Column(name = "coinpayments_merchant_id")
    private String coinpaymentsMerchantId;

    @Column(name = "coinpayments_ipn_secret")
    private String coinpaymentsIpnSecret;

    @Column(name = "coinpayments_debug_email")
    private String coinpaymentsDebugEmail;

    // --- Crypto / exchange ---
    @Column(name = "use_crypto_feature", nullable = false)
    private Boolean useCryptoFeature;

    @Column(name = "exchange_fee_pct", precision = 6, scale = 3)
    private BigDecimal exchangeFeePct;

    @Column(name = "currency_rate", precision = 20, scale = 8)
    private BigDecimal currencyRate;

    @Column(name = "min_topup_amount", precision = 20, scale = 8)
    private BigDecimal minTopupAmount;

    @Column(name = "use_internal_transfer", nullable = false)
    private Boolean useInternalTransfer;

    @Column(name = "min_transfer_amount", precision = 20, scale = 8)
    private BigDecimal minTransferAmount;

    @Column(name = "transfer_charges", precision = 20, scale = 8)
    private BigDecimal transferCharges;

    @Column(name = "local_currency")
    private String localCurrency;

    @Column(name = "base_currency")
    private String baseCurrency;

    // --- External SaaS + Telegram ---
    // app.getonlinetrader.pro API token
    @Column(name = "merchant_key")
    private String merchantKey;

    @Column(name = "telegram_bot_api_token")
    private String telegramBotApiToken;

    // --- Live chat widgets ---
    @Column(name = "tawk_to_embed")
    private String tawkToEmbed;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
