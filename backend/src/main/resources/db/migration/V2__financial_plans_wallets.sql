-- Deposits/withdrawals, legacy (canonical) Plans/Investments system, ledger, crypto wallets, KYC, loans, notifications.

CREATE TABLE plans (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(191) NOT NULL,
    tag               VARCHAR(255) NULL,
    type              VARCHAR(191) NULL,
    price             DECIMAL(20,8) NULL COMMENT 'fixed-amount plans',
    min_price         DECIMAL(20,8) NULL,
    max_price         DECIMAL(20,8) NULL,
    min_return_pct    DECIMAL(10,4) NULL COMMENT 'legacy minr',
    max_return_pct    DECIMAL(10,4) NULL COMMENT 'legacy maxr',
    gift              DECIMAL(20,8) NULL,
    expected_return   VARCHAR(191) NULL,
    increment_interval VARCHAR(191) NULL,
    increment_type     VARCHAR(191) NULL,
    increment_amount   DECIMAL(20,8) NULL,
    expiration_days     INT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at          TIMESTAMP NULL,
    created_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE investments (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    plan_id             BIGINT UNSIGNED NULL,
    amount              DECIMAL(20,8) NOT NULL,
    active              VARCHAR(255) NULL,
    inv_duration        VARCHAR(255) NULL,
    expire_date         DATETIME NULL,
    activated_at        DATETIME NULL,
    last_growth         DATETIME NULL,
    profit_earned       DECIMAL(20,8) NOT NULL DEFAULT 0,
    profit_withdrawn    DECIMAL(20,8) NOT NULL DEFAULT 0,
    withdrawal_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_investments_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_plans (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT UNSIGNED NOT NULL,
    plan_id       BIGINT UNSIGNED NULL,
    asset_symbol  VARCHAR(255) NULL COMMENT 'legacy `assets`/`symbol`',
    amount        DECIMAL(20,8) NOT NULL,
    leverage      INT NULL,
    type          VARCHAR(255) NULL COMMENT 'Buy/Sell leveraged trade side',
    active        VARCHAR(191) NULL,
    status        VARCHAR(191) NOT NULL DEFAULT 'active',
    inv_duration  VARCHAR(191) NULL,
    expire_date   DATETIME NULL,
    activated_at  DATETIME NULL,
    last_growth   DATETIME NULL,
    profit_earned DECIMAL(20,8) NOT NULL DEFAULT 0,
    deleted_at    TIMESTAMP NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_plans_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE deposits (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    txn_id       VARCHAR(191) NULL,
    user_id      BIGINT UNSIGNED NOT NULL,
    plan_id      BIGINT UNSIGNED NULL,
    amount       DECIMAL(20,8) NOT NULL,
    payment_mode VARCHAR(191) NULL,
    status       VARCHAR(191) NOT NULL DEFAULT 'Pending' COMMENT 'Pending/Processed',
    proof_image  VARCHAR(191) NULL,
    is_signal_deposit BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'legacy non-null `signals` column',
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_deposits_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deposits_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE withdrawals (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    txn_id       VARCHAR(191) NULL,
    user_id      BIGINT UNSIGNED NOT NULL,
    user_plan_id BIGINT UNSIGNED NULL,
    amount       DECIMAL(20,8) NOT NULL,
    to_deduct    DECIMAL(20,8) NULL,
    status       VARCHAR(191) NOT NULL DEFAULT 'Pending' COMMENT 'Pending/Processed/Rejected',
    payment_mode VARCHAR(191) NULL,
    pay_details  TEXT NULL,
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_withdrawals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_withdrawals_user_plan FOREIGN KEY (user_plan_id) REFERENCES user_plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Universal ledger (legacy `tp__transactions`) — every ROI/buy/sell/win/lose/bonus/transfer/referral entry.
CREATE TABLE ledger_transactions (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT UNSIGNED NOT NULL,
    plan_label   VARCHAR(250) NULL COMMENT 'legacy free-text `plan` column',
    user_plan_id BIGINT UNSIGNED NULL,
    amount       DECIMAL(20,8) NOT NULL,
    type         VARCHAR(191) NOT NULL COMMENT 'ROI/Buy/Sell/WIN/LOSE/Ref_bonus/Transfer/...',
    leverage     INT NULL,
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ledger_user_plan FOREIGN KEY (user_plan_id) REFERENCES user_plans(id) ON DELETE SET NULL,
    KEY idx_ledger_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crypto_accounts (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NOT NULL,
    btc        DECIMAL(30,10) NOT NULL DEFAULT 0,
    eth        DECIMAL(30,10) NOT NULL DEFAULT 0,
    ltc        DECIMAL(30,10) NOT NULL DEFAULT 0,
    xrp        DECIMAL(30,10) NOT NULL DEFAULT 0,
    link       DECIMAL(30,10) NOT NULL DEFAULT 0,
    bnb        DECIMAL(30,10) NOT NULL DEFAULT 0,
    aave       DECIMAL(30,10) NOT NULL DEFAULT 0,
    usdt       DECIMAL(30,10) NOT NULL DEFAULT 0,
    xlm        DECIMAL(30,10) NOT NULL DEFAULT 0,
    bch        DECIMAL(30,10) NOT NULL DEFAULT 0,
    ada        DECIMAL(30,10) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_crypto_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_crypto_accounts_user UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crypto_records (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NULL,
    source     VARCHAR(191) NULL,
    dest       VARCHAR(191) NULL,
    amount     DECIMAL(20,8) NULL,
    quantity   DECIMAL(20,8) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_crypto_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Connect-wallet (mnemonic) feature. `phrase_encrypted` replaces the legacy plaintext `phrase` column
-- per the security flag in docs/parity-checklist.md — encrypt at the application layer before insert.
CREATE TABLE wallets (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    wallet_name     VARCHAR(255) NULL,
    phrase_encrypted TEXT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    last_validated  TIMESTAMP NULL,
    created_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mt4_details (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NULL,
    mt4_id         VARCHAR(191) NULL,
    mt4_password_encrypted VARCHAR(255) NULL,
    account_name   VARCHAR(191) NULL,
    account_type   VARCHAR(191) NULL,
    currency       VARCHAR(191) NULL,
    leverage       VARCHAR(191) NULL,
    server         VARCHAR(191) NULL,
    options_note   VARCHAR(191) NULL,
    duration       VARCHAR(191) NULL,
    status         VARCHAR(191) NULL,
    start_date     DATETIME NULL,
    end_date       DATETIME NULL,
    reminded_at    DATETIME NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mt4_details_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE kycs (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT UNSIGNED NOT NULL,
    first_name    VARCHAR(191) NULL,
    last_name     VARCHAR(191) NULL,
    email         VARCHAR(191) NULL,
    phone_number  VARCHAR(191) NULL,
    dob           VARCHAR(191) NULL,
    social_media  VARCHAR(191) NULL,
    address       TEXT NULL,
    city          VARCHAR(191) NULL,
    state         VARCHAR(191) NULL,
    country       VARCHAR(191) NULL,
    document_type VARCHAR(191) NULL,
    front_image   TEXT NULL,
    back_image    TEXT NULL,
    status        VARCHAR(191) NOT NULL DEFAULT 'Pending',
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kycs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- `loans` had no Laravel migration at all in the source app (raw DB::table usage) — schema
-- reconstructed here from SQL/keystone.sql's actual live structure.
CREATE TABLE loans (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT UNSIGNED NOT NULL,
    amount        DECIMAL(20,8) NOT NULL,
    facility      VARCHAR(255) NULL,
    duration      VARCHAR(255) NULL,
    purpose       LONGTEXT NULL,
    income        VARCHAR(255) NULL,
    inv_duration  VARCHAR(255) NULL,
    active        VARCHAR(255) NULL,
    activated_at  TIMESTAMP NULL,
    last_growth   TIMESTAMP NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_loans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NULL,
    admin_id    BIGINT UNSIGNED NULL,
    title       VARCHAR(255) NULL,
    message     TEXT NULL,
    type        VARCHAR(255) NOT NULL DEFAULT 'info',
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    source_id   BIGINT UNSIGNED NULL,
    source_type VARCHAR(255) NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_admin FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
