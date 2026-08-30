-- Trading instruments/trades, bots, copy trading (canonical "new system"), legacy signal-plan subscriptions.

CREATE TABLE instruments (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    symbol              VARCHAR(255) NOT NULL,
    coingecko_id        VARCHAR(255) NULL,
    name                VARCHAR(255) NOT NULL,
    type                VARCHAR(255) NOT NULL,
    open_price          DECIMAL(20,8) NULL,
    high_price          DECIMAL(20,8) NULL,
    low_price           DECIMAL(20,8) NULL,
    close_price         DECIMAL(20,8) NULL,
    price               DECIMAL(20,8) NULL,
    percent_change_24h  DECIMAL(10,4) NULL,
    change_amount       DECIMAL(20,8) NULL,
    market_cap          DECIMAL(30,2) NULL,
    volume              DECIMAL(30,2) NULL,
    logo                VARCHAR(255) NULL,
    created_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_instruments_symbol UNIQUE (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE market_prices (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    instrument_id BIGINT UNSIGNED NOT NULL,
    open_price    DECIMAL(20,8) NOT NULL,
    high_price    DECIMAL(20,8) NOT NULL,
    low_price     DECIMAL(20,8) NOT NULL,
    close_price   DECIMAL(20,8) NOT NULL,
    volume        DECIMAL(30,8) NULL,
    price_interval VARCHAR(255) NOT NULL DEFAULT '1m',
    recorded_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source        VARCHAR(255) NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_market_prices_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE,
    KEY idx_market_prices_lookup (instrument_id, recorded_at, price_interval)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE trades (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT UNSIGNED NOT NULL,
    instrument_id BIGINT UNSIGNED NOT NULL,
    side          VARCHAR(4) NOT NULL COMMENT 'buy/sell',
    amount        DECIMAL(20,2) NOT NULL,
    leverage      DECIMAL(6,2) NOT NULL DEFAULT 1.00,
    entry_price   DECIMAL(20,8) NOT NULL,
    exit_price    DECIMAL(20,8) NULL,
    pnl           DECIMAL(20,8) NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'open' COMMENT 'open/closed',
    stop_loss     DECIMAL(20,8) NULL,
    take_profit   DECIMAL(20,8) NULL,
    opened_at     TIMESTAMP NULL,
    closed_at     TIMESTAMP NULL,
    created_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trades_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_trades_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE trading_bots (
    id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    bot_type         VARCHAR(255) NOT NULL DEFAULT 'forex' COMMENT 'forex/crypto/stocks/commodities/indices',
    description      TEXT NOT NULL,
    image            VARCHAR(255) NULL,
    min_investment   DECIMAL(20,8) NOT NULL DEFAULT 100,
    max_investment   DECIMAL(20,8) NOT NULL DEFAULT 10000,
    daily_profit_min DECIMAL(6,2) NOT NULL DEFAULT 0.50,
    daily_profit_max DECIMAL(6,2) NOT NULL DEFAULT 3.00,
    success_rate     INT NOT NULL DEFAULT 85,
    duration_days    INT NOT NULL DEFAULT 30,
    total_earned     DECIMAL(20,8) NOT NULL DEFAULT 0,
    total_users      INT NOT NULL DEFAULT 0,
    status           VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active/inactive/maintenance',
    trading_pairs    JSON NULL,
    risk_settings    JSON NULL,
    strategy_details JSON NULL,
    last_trade_at    TIMESTAMP NULL,
    created_at       TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_bot_investments (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT UNSIGNED NOT NULL,
    bot_id              BIGINT UNSIGNED NOT NULL,
    investment_amount   DECIMAL(20,8) NOT NULL,
    current_balance     DECIMAL(20,8) NOT NULL,
    total_profit        DECIMAL(20,8) NOT NULL DEFAULT 0,
    total_loss          DECIMAL(20,8) NOT NULL DEFAULT 0,
    successful_trades   INT NOT NULL DEFAULT 0,
    failed_trades       INT NOT NULL DEFAULT 0,
    started_at          TIMESTAMP NULL,
    expires_at          TIMESTAMP NULL,
    last_profit_at      TIMESTAMP NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active/completed/cancelled/expired',
    auto_reinvest       BOOLEAN NOT NULL DEFAULT FALSE,
    reinvest_percentage DECIMAL(6,2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_bot_investments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_bot_investments_bot FOREIGN KEY (bot_id) REFERENCES trading_bots(id) ON DELETE CASCADE,
    KEY idx_user_bot_investments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bot_trading_history (
    id                     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_bot_investment_id BIGINT UNSIGNED NOT NULL,
    trade_type             VARCHAR(255) NOT NULL,
    trading_pair           VARCHAR(255) NOT NULL,
    entry_price             DECIMAL(20,8) NOT NULL,
    exit_price               DECIMAL(20,8) NULL,
    amount                   DECIMAL(20,2) NOT NULL,
    profit_loss              DECIMAL(20,2) NOT NULL DEFAULT 0,
    profit_percentage        DECIMAL(10,4) NOT NULL DEFAULT 0,
    result                   VARCHAR(10) NOT NULL DEFAULT 'pending' COMMENT 'pending/profit/loss',
    strategy_used            TEXT NULL,
    opened_at                TIMESTAMP NULL,
    closed_at                TIMESTAMP NULL,
    created_at               TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bot_trading_history_investment FOREIGN KEY (user_bot_investment_id) REFERENCES user_bot_investments(id) ON DELETE CASCADE,
    KEY idx_bot_trading_history_lookup (user_bot_investment_id, result),
    KEY idx_bot_trading_history_opened (opened_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Copy Trading — canonical "new system" (CopyTradingController / CopyTradingAdminController).
CREATE TABLE copy_trading_experts (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tag            VARCHAR(255) NULL,
    name           VARCHAR(255) NOT NULL,
    photo          VARCHAR(255) NULL,
    rating         INT NOT NULL DEFAULT 5,
    followers      INT NOT NULL DEFAULT 0,
    equity         DECIMAL(20,2) NOT NULL DEFAULT 0,
    total_profit   DECIMAL(20,2) NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active/inactive',
    description    TEXT NULL,
    win_rate       INT NOT NULL DEFAULT 80,
    total_trades   INT NOT NULL DEFAULT 0,
    price          DECIMAL(20,8) NULL COMMENT 'copy-in fee, if any',
    type           VARCHAR(20) NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_copy_trades (
    id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT UNSIGNED NOT NULL,
    expert_id          BIGINT UNSIGNED NOT NULL,
    price              DECIMAL(20,8) NULL,
    active             VARCHAR(255) NULL,
    type               VARCHAR(255) NULL,
    started_at         TIMESTAMP NULL,
    last_profit_at     TIMESTAMP NULL,
    total_profit       DECIMAL(20,2) NOT NULL DEFAULT 0,
    current_balance    DECIMAL(20,2) NOT NULL DEFAULT 0,
    total_trades       INT NOT NULL DEFAULT 0,
    winning_trades     INT NOT NULL DEFAULT 0,
    profit_percentage  DECIMAL(10,2) NOT NULL DEFAULT 0,
    equity             DECIMAL(20,2) NULL,
    created_at         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_copy_trades_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_copy_trades_expert FOREIGN KEY (expert_id) REFERENCES copy_trading_experts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy signal-plan subscription (a user-purchasable "signal plan," distinct from the
-- external-SaaS-backed SignalProvider broadcast feature — both are in scope, see CANONICAL-MODULES.md).
CREATE TABLE signal_plans (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255) NULL,
    price             DECIMAL(20,8) NULL,
    type              VARCHAR(255) NULL,
    increment_amount  DECIMAL(20,8) NULL,
    created_at        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_signal_plans (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT UNSIGNED NOT NULL,
    signal_plan_id BIGINT UNSIGNED NULL,
    asset        VARCHAR(255) NULL,
    order_type   VARCHAR(255) NULL,
    amount       DECIMAL(20,8) NULL,
    leverage     VARCHAR(255) NULL,
    status       VARCHAR(255) NULL,
    expiration   VARCHAR(255) NULL,
    created_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_signal_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_signal_plans_plan FOREIGN KEY (signal_plan_id) REFERENCES signal_plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- MT4/MT5 copy-trade "master account" subscription, external-SaaS-backed.
CREATE TABLE mt4_subscriptions (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NULL,
    mt4_details_id BIGINT UNSIGNED NULL,
    status         VARCHAR(191) NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mt4_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mt4_subscriptions_details FOREIGN KEY (mt4_details_id) REFERENCES mt4_details(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
