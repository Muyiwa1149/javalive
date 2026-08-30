-- The legacy `tp__transactions` table reused a single `user_plan_id` column loosely for both
-- `user_plans` (leveraged trade placements) and `investments` (named plan purchases) rows,
-- depending on transaction type -- a polymorphic-by-convention pattern with no real FK integrity.
-- Per docs/CANONICAL-MODULES.md's guidance to keep clean, properly-typed tracking rather than
-- replicate that looseness, this adds a dedicated `investment_id` FK so ROI/profit-withdrawal
-- ledger entries can be correctly and safely scoped to one specific investment.

ALTER TABLE ledger_transactions
    ADD COLUMN investment_id BIGINT UNSIGNED NULL AFTER user_plan_id,
    ADD CONSTRAINT fk_ledger_transactions_investment
        FOREIGN KEY (investment_id) REFERENCES investments (id) ON DELETE SET NULL;
