-- Source's `mwalletconnectsave` admin action includes a `wallet_status` toggle (enable/disable the
-- wallet-connect feature globally) alongside `min_balance`/`min_return`, which the initial migration
-- carried over but this one was missed. Adding it now so the admin Phrase Settings page can gate the
-- feature the same way source intended -- default 'enabled' matches the feature being live today.

ALTER TABLE app_settings
    ADD COLUMN wallet_status VARCHAR(20) NOT NULL DEFAULT 'enabled' AFTER min_balance;
