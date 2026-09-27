-- Bots and copy-trading experts already have an admin-configurable win-rate/profit-magnitude
-- (success_rate, daily_profit_min/max, win_rate) but the LOSS magnitude used when a trade/position
-- loses was hardcoded in BotProfitScheduler/CopyTradingProfitScheduler with no admin control at all.
-- Defaults below exactly match those previously-hardcoded ranges, so existing behavior is unchanged
-- until an admin actually edits a bot/expert.
ALTER TABLE trading_bots ADD COLUMN loss_min DECIMAL(6,2) NOT NULL DEFAULT 0.50;
ALTER TABLE trading_bots ADD COLUMN loss_max DECIMAL(6,2) NOT NULL DEFAULT 2.00;

-- Copy trading has the identical gap on both sides (profit magnitude was also hardcoded, not just loss).
ALTER TABLE copy_trading_experts ADD COLUMN profit_min DECIMAL(6,2) NOT NULL DEFAULT 0.50;
ALTER TABLE copy_trading_experts ADD COLUMN profit_max DECIMAL(6,2) NOT NULL DEFAULT 4.00;
ALTER TABLE copy_trading_experts ADD COLUMN loss_min DECIMAL(6,2) NOT NULL DEFAULT 0.20;
ALTER TABLE copy_trading_experts ADD COLUMN loss_max DECIMAL(6,2) NOT NULL DEFAULT 2.00;
