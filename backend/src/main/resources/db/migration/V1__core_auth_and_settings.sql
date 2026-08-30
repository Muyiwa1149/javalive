-- Core auth (users, admins), platform settings, and password-reset tokens.
-- Ported from the legacy `users`/`admins`/`settings`/`settings_conts`/`paystacks`/`cp_transactions` tables.
-- Column names normalized (e.g. `user` -> `user_id`) and monetary/quantity columns moved from
-- float/varchar to DECIMAL for correctness in a financial app (see docs/CANONICAL-MODULES.md).

CREATE TABLE users (
    id                     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(191) NOT NULL COMMENT 'single full-name field — the live DB has no last-name column despite the model fillable list mentioning one',
    email                  VARCHAR(191) NOT NULL,
    username               VARCHAR(191) NULL,
    email_verified_at      TIMESTAMP NULL,
    password               VARCHAR(255) NOT NULL,
    two_factor_secret      TEXT NULL,
    two_factor_recovery_codes TEXT NULL,
    dob                    DATE NULL,
    currency_symbol        VARCHAR(10) NOT NULL DEFAULT '$',
    currency_code          VARCHAR(10) NOT NULL DEFAULT 'USD',
    notify                 TEXT NULL,
    customer_status        VARCHAR(191) NULL COMMENT 'legacy cstatus: Lead/Customer',
    trade_type             VARCHAR(255) NOT NULL DEFAULT 'Profit',
    number_of_trades       INT NOT NULL DEFAULT 2,
    assigned_agent         VARCHAR(191) NULL COMMENT 'legacy `assign_to` — free-text in the real data (not a real FK id), kept as-is',
    address                TEXT NULL,
    country                VARCHAR(191) NULL,
    phone                  VARCHAR(191) NULL,
    dashboard_style        VARCHAR(191) NOT NULL DEFAULT 'light',
    bank_name              VARCHAR(191) NULL,
    bank_account_name      VARCHAR(191) NULL,
    bank_account_number    VARCHAR(191) NULL,
    bank_swift_code        VARCHAR(191) NULL,
    btc_address            VARCHAR(191) NULL,
    eth_address            VARCHAR(191) NULL,
    ltc_address            VARCHAR(191) NULL,
    usdt_address            VARCHAR(191) NULL,
    account_balance        DECIMAL(20,8) NOT NULL DEFAULT 0,
    roi_balance            DECIMAL(20,8) NOT NULL DEFAULT 0,
    bonus_balance          DECIMAL(20,8) NOT NULL DEFAULT 0,
    referral_bonus_balance DECIMAL(20,8) NOT NULL DEFAULT 0,
    signup_bonus           DECIMAL(20,8) NOT NULL DEFAULT 0,
    bonus_released         BOOLEAN NOT NULL DEFAULT FALSE,
    referred_by_code       VARCHAR(191) NULL COMMENT 'legacy ref_by',
    referral_code          VARCHAR(191) NULL COMMENT 'legacy ref_link',
    tax_amount             DECIMAL(10,2) NULL,
    tax_type               VARCHAR(255) NOT NULL DEFAULT 'off',
    account_verify_status  VARCHAR(191) NULL COMMENT 'Verified/Pending/Rejected',
    status                 VARCHAR(25) NOT NULL DEFAULT 'active' COMMENT 'active/blocked',
    trade_mode             VARCHAR(191) NOT NULL DEFAULT 'on',
    remember_token         VARCHAR(100) NULL,
    profile_photo_path     TEXT NULL,
    withdraw_otp           VARCHAR(191) NULL,
    send_otp_email         BOOLEAN NOT NULL DEFAULT FALSE,
    send_roi_email         BOOLEAN NOT NULL DEFAULT TRUE,
    send_promo_email       BOOLEAN NOT NULL DEFAULT TRUE,
    send_inv_plan_email    BOOLEAN NOT NULL DEFAULT TRUE,
    signal_status          VARCHAR(255) NOT NULL DEFAULT 'off',
    plan_status            VARCHAR(11) NOT NULL DEFAULT 'off',
    withdrawal_code_status VARCHAR(22) NOT NULL DEFAULT 'on',
    user_withdrawal_code   VARCHAR(255) NULL,
    copy_trading_progress  INT NOT NULL DEFAULT 0 COMMENT 'legacy `progress` cosmetic field',
    wallet_connected       BOOLEAN NOT NULL DEFAULT FALSE,
    has_signal_subscription BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'legacy `signals` truthy flag — gates the "My Signals" nav link in dasht.blade.php',
    -- The following legacy columns (`plan`, `trades`, `user_plan`, `user_signal`, `user_plan_upgade`, `trade`,
    -- `copy`, `copy_plan` on the old `users` table) are preserved as free-text scratch fields rather than
    -- dropped, since their exact business meaning wasn't conclusively identified during research and dropping
    -- data is riskier than keeping an unused column — resolve their real usage during Phase 4 (user dashboard)
    -- implementation and either wire them up or formally retire them then, not now.
    legacy_plan_field       VARCHAR(191) NULL,
    legacy_trades_field     VARCHAR(255) NULL,
    legacy_user_plan_field  VARCHAR(191) NULL,
    legacy_user_signal_field VARCHAR(255) NULL,
    legacy_user_plan_upgrade_field VARCHAR(225) NULL,
    legacy_trade_field      INT NOT NULL DEFAULT 0,
    legacy_copy_field       VARCHAR(255) NULL,
    legacy_copy_plan_field  VARCHAR(255) NULL,
    created_at             TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admins (
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name       VARCHAR(191) NOT NULL,
    last_name        VARCHAR(191) NOT NULL,
    email            VARCHAR(191) NOT NULL,
    email_verified_at TIMESTAMP NULL,
    password         VARCHAR(255) NOT NULL,
    phone            VARCHAR(191) NULL,
    dashboard_style  VARCHAR(191) NOT NULL DEFAULT 'dark',
    remember_token   VARCHAR(100) NULL,
    status           VARCHAR(191) NOT NULL DEFAULT 'active',
    type             VARCHAR(191) NOT NULL COMMENT 'Super Admin / Admin / Rentention Agent / Conversion Agent',
    enable_2fa       BOOLEAN NOT NULL DEFAULT FALSE,
    token_2fa        VARCHAR(191) NULL,
    token_2fa_expiry TIMESTAMP NULL,
    pass_2fa         BOOLEAN NOT NULL DEFAULT FALSE,
    password_reset_token VARCHAR(100) NULL,
    created_at       TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_admins_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE agents (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    admin_id        BIGINT UNSIGNED NULL,
    total_referred  INT NOT NULL DEFAULT 0,
    total_activated INT NOT NULL DEFAULT 0,
    earnings        DECIMAL(20,8) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_agents_admin FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(191) NOT NULL,
    guard       VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT 'user or admin',
    token       VARCHAR(191) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_password_reset_email (email, guard)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ipaddresses (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(191) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_ipaddresses_ip UNIQUE (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activities (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NULL,
    ip_address VARCHAR(191) NULL,
    device     VARCHAR(191) NULL,
    browser    VARCHAR(191) NULL,
    os         VARCHAR(191) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Single-row global settings (replaces `settings` + `settings_conts` + `paystacks` + `cp_transactions`,
-- which were all separate single-row tables for the same conceptual "app configuration" in the PHP app).
CREATE TABLE app_settings (
    id                       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    site_name                VARCHAR(191) NULL,
    site_title               VARCHAR(191) NULL,
    site_address             VARCHAR(191) NULL,
    description              TEXT NULL,
    keywords                 VARCHAR(191) NULL,
    logo                     VARCHAR(191) NULL,
    favicon                  VARCHAR(191) NULL,
    default_currency_symbol  VARCHAR(10) NULL,
    contact_email            VARCHAR(191) NULL,
    timezone                 VARCHAR(191) NULL,
    welcome_message          TEXT NULL,
    google_translate_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    weekend_trade_enabled    BOOLEAN NOT NULL DEFAULT FALSE,
    trade_mode               VARCHAR(191) NULL,
    trading_winrate          INT NOT NULL DEFAULT 62,
    -- Mail
    mail_server              VARCHAR(20) NULL,
    mail_from_address        VARCHAR(191) NULL,
    mail_from_name           VARCHAR(191) NULL,
    smtp_host                VARCHAR(191) NULL,
    smtp_port                VARCHAR(191) NULL,
    smtp_encryption          VARCHAR(191) NULL,
    smtp_username            VARCHAR(191) NULL,
    smtp_password            VARCHAR(191) NULL,
    -- Captcha / social login
    captcha_provider         VARCHAR(191) NULL,
    captcha_secret           VARCHAR(191) NULL,
    captcha_site_key         VARCHAR(191) NULL,
    social_login_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    google_client_id         VARCHAR(191) NULL,
    google_client_secret     VARCHAR(191) NULL,
    google_redirect_uri      VARCHAR(191) NULL,
    -- KYC / 2FA / verification toggles
    enable_2fa               BOOLEAN NOT NULL DEFAULT FALSE,
    enable_kyc               BOOLEAN NOT NULL DEFAULT FALSE,
    enable_kyc_registration  BOOLEAN NOT NULL DEFAULT FALSE,
    enable_email_verification BOOLEAN NOT NULL DEFAULT TRUE,
    -- Referral / bonus
    referral_commission_pct  DECIMAL(6,3) NULL,
    referral_commission_l1   DECIMAL(6,3) NULL,
    referral_commission_l2   DECIMAL(6,3) NULL,
    referral_commission_l3   DECIMAL(6,3) NULL,
    referral_commission_l4   DECIMAL(6,3) NULL,
    referral_commission_l5   DECIMAL(6,3) NULL,
    signup_bonus             DECIMAL(20,8) NULL,
    deposit_bonus_pct        DECIMAL(6,3) NULL,
    -- Payments
    withdrawal_option        VARCHAR(191) NOT NULL DEFAULT 'auto' COMMENT 'auto/manual',
    deposit_option            VARCHAR(191) NULL,
    deduction_option          VARCHAR(191) NOT NULL DEFAULT 'userRequest' COMMENT 'userRequest/AdminApprove',
    auto_merchant_option      VARCHAR(191) NOT NULL DEFAULT 'Coinpayment',
    credit_card_provider      VARCHAR(191) NOT NULL DEFAULT 'Paystack',
    return_capital            BOOLEAN NOT NULL DEFAULT TRUE,
    should_cancel_plan        BOOLEAN NOT NULL DEFAULT TRUE,
    commission_type           VARCHAR(191) NULL,
    commission_fee            DECIMAL(10,2) NULL,
    min_return                DECIMAL(20,8) NOT NULL DEFAULT 3000,
    min_balance               DECIMAL(20,8) NOT NULL DEFAULT 30000,
    -- Subscription fees
    subscription_monthly_fee  DECIMAL(10,2) NULL,
    subscription_quarterly_fee DECIMAL(10,2) NULL,
    subscription_yearly_fee   DECIMAL(10,2) NULL,
    -- Stripe
    stripe_secret_key         VARCHAR(255) NULL,
    stripe_public_key         VARCHAR(255) NULL,
    -- PayPal
    paypal_client_id          VARCHAR(255) NULL,
    paypal_client_secret      VARCHAR(255) NULL,
    -- Paystack
    paystack_public_key       TEXT NULL,
    paystack_secret_key       TEXT NULL,
    paystack_url              VARCHAR(191) NULL,
    paystack_email            VARCHAR(191) NULL,
    -- Flutterwave
    flutterwave_public_key    VARCHAR(191) NULL,
    flutterwave_secret_key    VARCHAR(191) NULL,
    flutterwave_secret_hash   VARCHAR(191) NULL,
    -- Binance Pay
    binance_api_key           VARCHAR(191) NULL,
    binance_secret_key        VARCHAR(191) NULL,
    -- CoinPayments
    coinpayments_public_key   VARCHAR(191) NULL,
    coinpayments_private_key  VARCHAR(191) NULL,
    coinpayments_merchant_id  VARCHAR(191) NULL,
    coinpayments_ipn_secret   VARCHAR(191) NULL,
    coinpayments_debug_email  VARCHAR(191) NULL,
    -- Crypto / exchange
    use_crypto_feature        BOOLEAN NOT NULL DEFAULT TRUE,
    exchange_fee_pct          DECIMAL(6,3) NULL DEFAULT 0,
    currency_rate             DECIMAL(20,8) NULL,
    min_topup_amount          DECIMAL(20,8) NULL,
    use_internal_transfer     BOOLEAN NOT NULL DEFAULT TRUE,
    min_transfer_amount       DECIMAL(20,8) NULL DEFAULT 0,
    transfer_charges          DECIMAL(20,8) NULL DEFAULT 0,
    local_currency             VARCHAR(20) NULL,
    base_currency               VARCHAR(20) NULL,
    -- External SaaS + Telegram
    merchant_key               VARCHAR(192) NULL COMMENT 'app.getonlinetrader.pro API token',
    telegram_bot_api_token     VARCHAR(192) NULL,
    -- Live chat widgets
    tawk_to_embed              LONGTEXT NULL,
    whatsapp_number            VARCHAR(255) NULL,
    created_at                 TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crypto_asset_toggles (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    symbol      VARCHAR(10) NOT NULL COMMENT 'btc/eth/ltc/link/bnb/aave/usdt/bch/xlm/xrp/ada',
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_crypto_asset_toggles_symbol UNIQUE (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wdmethods (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(191) NOT NULL,
    method_type     VARCHAR(191) NULL COMMENT 'bank/crypto',
    type            VARCHAR(191) NOT NULL COMMENT 'deposit/withdrawal',
    minimum_amount  DECIMAL(20,8) NULL,
    maximum_amount  DECIMAL(20,8) NULL,
    charges_amount  DECIMAL(20,8) NULL,
    charges_type    VARCHAR(191) NULL COMMENT 'fixed/percentage',
    duration_note   VARCHAR(191) NULL,
    image_url       TEXT NULL,
    bank_name       VARCHAR(191) NULL,
    account_name    VARCHAR(191) NULL,
    account_number  VARCHAR(191) NULL,
    swift_code      VARCHAR(191) NULL,
    wallet_address  TEXT NULL,
    barcode_image   TEXT NULL,
    network         VARCHAR(191) NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    status          VARCHAR(191) NOT NULL DEFAULT 'enabled',
    created_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
