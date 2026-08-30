-- Transforms data from `javalive_staging` (raw, untouched import of SQL/keystone.sql, the real
-- production dump) into `javalive_db` (the new Flyway-managed schema V1-V4). Read-only against
-- javalive_staging; javalive_db is assumed freshly migrated and empty when this runs.
-- Run: mysql -h localhost -u root javalive_db < transform-import.sql
-- (cross-database references use `javalive_staging.<table>` explicitly; connecting to javalive_db
-- as the default schema is what makes the unqualified INSERT targets resolve correctly)

SET FOREIGN_KEY_CHECKS = 0;

-- IMPORTANT: the source data itself has pre-existing referential-integrity gaps — e.g. only 1 row
-- currently exists in `users`, while `tp__transactions` (611 rows), `crypto_accounts` (147 rows),
-- `deposits` (7), `loans` (7), `user_copytradings` (65), etc. reference many other user ids that no
-- longer exist there. This is a real characteristic of the source database, not something this script
-- introduces. Per "nothing must be missing," every row is imported as-is (FK checks stay disabled
-- throughout, on purpose) rather than silently dropping rows whose parent no longer exists — do NOT
-- add `WHERE x IN (SELECT id FROM parent)` filters below, they would silently discard real financial
-- history. Flag this upstream data gap to the business before going live (see docs/parity-checklist.md).

-- ============ users ============
INSERT INTO users (id, name, email, username, email_verified_at, password, two_factor_secret,
  two_factor_recovery_codes, dob, currency_symbol, currency_code, notify, customer_status, trade_type,
  number_of_trades, assigned_agent, address, country, phone, dashboard_style, bank_name, bank_account_name,
  bank_account_number, bank_swift_code, btc_address, eth_address, ltc_address, usdt_address, account_balance,
  roi_balance, bonus_balance, referral_bonus_balance, signup_bonus, bonus_released, referred_by_code,
  referral_code, tax_amount, tax_type, account_verify_status, status, trade_mode, remember_token,
  profile_photo_path, withdraw_otp, send_otp_email, send_roi_email, send_promo_email, send_inv_plan_email,
  signal_status, plan_status, withdrawal_code_status, user_withdrawal_code, copy_trading_progress,
  wallet_connected, has_signal_subscription, legacy_plan_field, legacy_trades_field, legacy_user_plan_field,
  legacy_user_signal_field, legacy_user_plan_upgrade_field, legacy_trade_field, legacy_copy_field,
  legacy_copy_plan_field, created_at, updated_at)
SELECT id, name, email, username, email_verified_at, password, two_factor_secret, two_factor_recovery_codes,
  dob, COALESCE(NULLIF(currency,''), '$'), COALESCE(NULLIF(s_currency,''), 'USD'), notify, cstatus,
  COALESCE(NULLIF(tradetype,''), 'Profit'), COALESCE(numberoftrades, 2), assign_to, address, country, phone,
  COALESCE(NULLIF(dashboard_style,''), 'light'), bank_name, account_name, account_number, swift_code,
  btc_address, eth_address, ltc_address, usdt_address,
  COALESCE(account_bal, 0), COALESCE(roi, 0), COALESCE(bonus, 0), COALESCE(ref_bonus, 0),
  CAST(NULLIF(signup_bonus, '') AS DECIMAL(20,8)),
  (bonus_released = 1), ref_by, ref_link, taxamount, COALESCE(NULLIF(taxtype,''), 'off'), account_verify,
  COALESCE(NULLIF(status,''), 'active'), COALESCE(NULLIF(trade_mode,''), 'on'), remember_token,
  profile_photo_path, withdrawotp,
  (sendotpemail IN ('Yes','yes','1','true')), (sendroiemail IN ('Yes','yes','1','true')),
  (sendpromoemail IN ('Yes','yes','1','true')), (sendinvplanemail IN ('Yes','yes','1','true')),
  COALESCE(NULLIF(signal_status,''), 'off'), COALESCE(NULLIF(plan_status,''), 'off'),
  COALESCE(NULLIF(withdrawal_code,''), 'on'), user_withdrawalcode, COALESCE(progress, 0),
  (wallet_connected = 1), (signals IS NOT NULL AND signals NOT IN ('', '0')),
  plan, trades, user_plan, user_signal, user_plan_upgade, COALESCE(trade, 0), copy, copy_plan,
  created_at, updated_at
FROM javalive_staging.users;

-- ============ admins ============
INSERT INTO admins (id, first_name, last_name, email, email_verified_at, password, phone, dashboard_style,
  remember_token, status, type, enable_2fa, token_2fa, token_2fa_expiry, pass_2fa, password_reset_token,
  created_at, updated_at)
SELECT id, firstName, lastName, email, email_verified_at, password, phone,
  COALESCE(NULLIF(dashboard_style,''), 'dark'), remember_token, COALESCE(NULLIF(status,''), 'active'), type,
  (enable_2fa = 'enabled'), token_2fa, token_2fa_expiry, (pass_2fa = 'true'), password_token,
  created_at, updated_at
FROM javalive_staging.admins;

-- ============ agents ============
INSERT INTO agents (id, admin_id, total_referred, total_activated, earnings, created_at, updated_at)
SELECT id, NULL, CAST(NULLIF(total_refered,'') AS UNSIGNED), CAST(NULLIF(total_activated,'') AS UNSIGNED),
  CAST(NULLIF(earnings,'') AS DECIMAL(20,8)), created_at, updated_at
FROM javalive_staging.agents;

-- ============ app_settings (merges settings + settings_conts + paystacks, all single-row tables) ============
INSERT INTO app_settings (id, site_name, site_title, site_address, description, keywords, logo, favicon,
  default_currency_symbol, contact_email, timezone, welcome_message, google_translate_enabled,
  weekend_trade_enabled, trade_mode, trading_winrate, mail_server, mail_from_address, mail_from_name,
  smtp_host, smtp_port, smtp_encryption, smtp_username, smtp_password, captcha_secret, captcha_site_key,
  social_login_enabled, google_client_id, google_client_secret, google_redirect_uri, enable_2fa, enable_kyc,
  enable_kyc_registration, enable_email_verification, referral_commission_pct, referral_commission_l1,
  referral_commission_l2, referral_commission_l3, referral_commission_l4, referral_commission_l5,
  signup_bonus, deposit_bonus_pct, withdrawal_option, deposit_option, deduction_option, auto_merchant_option,
  credit_card_provider, return_capital, should_cancel_plan, commission_type, commission_fee, min_return,
  min_balance, subscription_monthly_fee, subscription_quarterly_fee, subscription_yearly_fee,
  stripe_secret_key, stripe_public_key, paypal_client_id, paypal_client_secret, paystack_public_key,
  paystack_secret_key, paystack_url, paystack_email, flutterwave_public_key, flutterwave_secret_key,
  flutterwave_secret_hash, binance_api_key, binance_secret_key, coinpayments_public_key,
  coinpayments_private_key, coinpayments_merchant_id, coinpayments_ipn_secret, coinpayments_debug_email,
  use_crypto_feature, exchange_fee_pct, currency_rate, min_topup_amount, use_internal_transfer,
  min_transfer_amount, transfer_charges, local_currency, base_currency, merchant_key,
  telegram_bot_api_token, tawk_to_embed, whatsapp_number, created_at, updated_at)
SELECT s.id, s.site_name, s.site_title, s.site_address, s.description, s.keywords, s.logo, s.favicon,
  s.currency, s.contact_email, s.timezone, s.welcome_message, (s.google_translate = 'on'),
  (s.weekend_trade = 'on'), s.trade_mode, COALESCE(s.trading_winrate, 62), s.mail_server, s.emailfrom,
  s.emailfromname, s.smtp_host, s.smtp_port, s.smtp_encrypt, s.smtp_user, s.smtp_password, s.capt_secret,
  s.capt_sitekey, (s.enable_social_login = 'on'), s.google_id, s.google_secret, s.google_redirect,
  (s.enable_2fa = 'yes'), (s.enable_kyc = 'yes'), (s.enable_kyc_registration = 'yes'),
  (s.enable_verification = 'true'),
  CAST(NULLIF(s.referral_commission,'') AS DECIMAL(6,3)), CAST(NULLIF(s.referral_commission1,'') AS DECIMAL(6,3)),
  CAST(NULLIF(s.referral_commission2,'') AS DECIMAL(6,3)), CAST(NULLIF(s.referral_commission3,'') AS DECIMAL(6,3)),
  CAST(NULLIF(s.referral_commission4,'') AS DECIMAL(6,3)), CAST(NULLIF(s.referral_commission5,'') AS DECIMAL(6,3)),
  CAST(NULLIF(s.signup_bonus,'') AS DECIMAL(20,8)), s.deposit_bonus,
  COALESCE(NULLIF(s.withdrawal_option,''), 'auto'), s.deposit_option,
  COALESCE(NULLIF(s.deduction_option,''), 'userRequest'), COALESCE(NULLIF(s.auto_merchant_option,''), 'Coinpayment'),
  COALESCE(NULLIF(s.credit_card_provider,''), 'Paystack'), COALESCE(s.return_capital, 1) = 1,
  COALESCE(s.should_cancel_plan, 1) = 1, s.commission_type, CAST(NULLIF(s.commission_fee,'') AS DECIMAL(10,2)),
  CAST(COALESCE(NULLIF(s.min_return,''), '3000') AS DECIMAL(20,8)),
  CAST(COALESCE(NULLIF(s.min_balance,''), '30000') AS DECIMAL(20,8)),
  CAST(NULLIF(s.monthlyfee,'') AS DECIMAL(10,2)), CAST(NULLIF(s.quarterlyfee,'') AS DECIMAL(10,2)),
  CAST(NULLIF(s.yearlyfee,'') AS DECIMAL(10,2)), s.s_s_k, s.s_p_k, s.pp_ci, s.pp_cs,
  p.paystack_public_key, p.paystack_secret_key, p.paystack_url, p.paystack_email,
  sc.flw_public_key, sc.flw_secret_key, sc.flw_secret_hash, sc.bnc_api_key, sc.bnc_secret_key,
  NULL, NULL, NULL, NULL, NULL,
  COALESCE(sc.use_crypto_feature = 'true', TRUE), sc.fee, sc.currency_rate, sc.minamt,
  COALESCE(sc.use_transfer, 1) = 1, sc.min_transfer, sc.transfer_charges, sc.local_currency, sc.base_currency,
  s.merchant_key, sc.telegram_bot_api, s.tawk_to, s.whatsapp,
  s.created_at, s.updated_at
FROM javalive_staging.settings s
LEFT JOIN javalive_staging.settings_conts sc ON sc.id = 1
LEFT JOIN javalive_staging.paystacks p ON p.id = 1
WHERE s.id = 1;

-- ============ crypto_asset_toggles (unpivoted from settings_conts) ============
INSERT INTO crypto_asset_toggles (symbol, enabled, created_at, updated_at)
SELECT 'btc', (btc = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'eth', (eth = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'ltc', (ltc = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'link', (link = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'bnb', (bnb = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'aave', (aave = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'usdt', (usdt = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'bch', (bch = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'xlm', (xlm = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'xrp', (xrp = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1
UNION ALL SELECT 'ada', (ada = 'enabled'), NOW(), NOW() FROM javalive_staging.settings_conts WHERE id = 1;

-- ============ wdmethods ============
INSERT INTO wdmethods (id, name, method_type, type, minimum_amount, maximum_amount, charges_amount,
  charges_type, duration_note, image_url, bank_name, account_name, account_number, swift_code,
  wallet_address, barcode_image, network, is_default, status, created_at, updated_at)
SELECT id, name, methodtype, type, CAST(NULLIF(minimum,'') AS DECIMAL(20,8)),
  CAST(NULLIF(maximum,'') AS DECIMAL(20,8)), CAST(NULLIF(charges_amount,'') AS DECIMAL(20,8)),
  charges_type, duration, img_url, bankname, account_name, account_number, swift_code, wallet_address,
  barcode, network, (defaultpay = 'yes'), COALESCE(NULLIF(status,''), 'enabled'), created_at, updated_at
FROM javalive_staging.wdmethods;

-- ============ plans ============
INSERT INTO plans (id, name, tag, type, price, min_price, max_price, min_return_pct, max_return_pct, gift,
  expected_return, increment_interval, increment_type, increment_amount, expiration_days, active,
  deleted_at, created_at, updated_at)
SELECT id, name, tag, type, CAST(NULLIF(price,'') AS DECIMAL(20,8)), CAST(NULLIF(min_price,'') AS DECIMAL(20,8)),
  CAST(NULLIF(max_price,'') AS DECIMAL(20,8)), CAST(NULLIF(minr,'') AS DECIMAL(10,4)),
  CAST(NULLIF(maxr,'') AS DECIMAL(10,4)), CAST(NULLIF(gift,'') AS DECIMAL(20,8)), expected_return,
  increment_interval, increment_type, CAST(NULLIF(increment_amount,'') AS DECIMAL(20,8)),
  CAST(NULLIF(expiration,'') AS UNSIGNED), (active = 1), deleted_at, created_at, updated_at
FROM javalive_staging.plans;

-- ============ investments ============
INSERT INTO investments (id, user_id, plan_id, amount, active, inv_duration, expire_date, activated_at,
  last_growth, profit_earned, profit_withdrawn, withdrawal_disabled, created_at, updated_at)
SELECT id, user, plan, CAST(NULLIF(amount,'') AS DECIMAL(20,8)), active, inv_duration, expire_date,
  activated_at, last_growth, COALESCE(profit_earned, 0), COALESCE(profit_withdrawn, 0),
  (withdrawal_disabled = 1), created_at, updated_at
FROM javalive_staging.investments;

-- ============ user_plans ============
INSERT INTO user_plans (id, user_id, plan_id, asset_symbol, amount, leverage, type, active, status,
  inv_duration, expire_date, activated_at, last_growth, profit_earned, deleted_at, created_at, updated_at)
SELECT id, user, plan, COALESCE(assets, symbol), CAST(NULLIF(amount,'') AS DECIMAL(20,8)), leverage, type,
  active, COALESCE(NULLIF(status,''), 'active'), inv_duration, expire_date, activated_at, last_growth,
  COALESCE(profit_earned, 0), deleted_at, created_at, updated_at
FROM javalive_staging.user_plans;

-- ============ deposits ============
INSERT INTO deposits (id, txn_id, user_id, plan_id, amount, payment_mode, status, proof_image,
  is_signal_deposit, created_at, updated_at)
SELECT id, txn_id, user, plan, CAST(NULLIF(amount,'') AS DECIMAL(20,8)), payment_mode,
  COALESCE(NULLIF(status,''), 'Pending'), proof, (signals IS NOT NULL AND signals != ''), created_at, updated_at
FROM javalive_staging.deposits;

-- ============ withdrawals ============
INSERT INTO withdrawals (id, txn_id, user_id, user_plan_id, amount, to_deduct, status, payment_mode,
  pay_details, created_at, updated_at)
SELECT id, txn_id, user, user_plan_id, CAST(NULLIF(amount,'') AS DECIMAL(20,8)),
  CAST(NULLIF(to_deduct,'') AS DECIMAL(20,8)), COALESCE(NULLIF(status,''), 'Pending'), payment_mode,
  paydetails, created_at, updated_at
FROM javalive_staging.withdrawals;

-- ============ ledger_transactions (from tp__transactions) ============
INSERT INTO ledger_transactions (id, user_id, plan_label, user_plan_id, amount, type, leverage, created_at, updated_at)
SELECT id, user, plan, user_plan_id, CAST(NULLIF(amount,'') AS DECIMAL(20,8)), type, leverage, created_at, updated_at
FROM javalive_staging.tp__transactions;

-- ============ crypto_accounts ============
INSERT INTO crypto_accounts (id, user_id, btc, eth, ltc, xrp, link, bnb, aave, usdt, xlm, bch, ada,
  created_at, updated_at)
SELECT id, user_id, COALESCE(btc,0), COALESCE(eth,0), COALESCE(ltc,0), COALESCE(xrp,0), COALESCE(link,0),
  COALESCE(bnb,0), COALESCE(aave,0), COALESCE(usdt,0), COALESCE(xlm,0), COALESCE(bch,0), COALESCE(ada,0),
  created_at, updated_at
FROM javalive_staging.crypto_accounts;

-- ============ crypto_records ============
INSERT INTO crypto_records (id, user_id, source, dest, amount, quantity, created_at, updated_at)
SELECT id, NULL, source, dest, amount, quantity, created_at, updated_at
FROM javalive_staging.crypto_records;

-- ============ kycs ============
INSERT INTO kycs (id, user_id, first_name, last_name, email, phone_number, dob, social_media, address,
  city, state, country, document_type, front_image, back_image, status, created_at, updated_at)
SELECT id, user_id, first_name, last_name, email, phone_number, dob, social_media, address, city, state,
  country, document_type, frontimg, backimg, COALESCE(NULLIF(status,''), 'Pending'), created_at, updated_at
FROM javalive_staging.kycs;

-- ============ loans ============
INSERT INTO loans (id, user_id, amount, facility, duration, purpose, income, inv_duration, active,
  activated_at, last_growth, created_at, updated_at)
SELECT id, user, COALESCE(amount, 0), facility, duration, purpose, income, inv_duration, active,
  activated_at, last_growth, created_at, updated_at
FROM javalive_staging.loans;

-- ============ notifications ============
INSERT INTO notifications (id, user_id, admin_id, title, message, type, is_read, source_id, source_type,
  created_at, updated_at)
SELECT id, user_id,
  (SELECT a.id FROM javalive_staging.admins a WHERE a.id = n.admin_id LIMIT 1),
  title, message, COALESCE(NULLIF(type,''), 'info'), (is_read = 1), source_id, source_type, created_at, updated_at
FROM javalive_staging.notifications n;

-- ============ instruments ============
INSERT INTO instruments (id, symbol, coingecko_id, name, type, open_price, high_price, low_price,
  close_price, price, percent_change_24h, change_amount, market_cap, volume, logo, created_at, updated_at)
SELECT id, symbol, coingecko_id, name, type, `open`, high, low, `close`, price, percent_change_24h,
  `change`, market_cap, volume, logo, created_at, updated_at
FROM javalive_staging.instruments;

-- ============ market_prices ============
INSERT INTO market_prices (id, instrument_id, open_price, high_price, low_price, close_price, volume,
  price_interval, recorded_at, source, created_at, updated_at)
SELECT id, instrument_id, `open`, high, low, `close`, volume, `interval`, `timestamp`, source, created_at, updated_at
FROM javalive_staging.market_prices;

-- ============ trading_bots ============
INSERT INTO trading_bots (id, name, bot_type, description, image, min_investment, max_investment,
  daily_profit_min, daily_profit_max, success_rate, duration_days, total_earned, total_users, status,
  trading_pairs, risk_settings, strategy_details, last_trade_at, created_at, updated_at)
SELECT id, name, bot_type, description, image, min_investment, max_investment, daily_profit_min,
  daily_profit_max, success_rate, duration_days, total_earned, total_users, status,
  NULLIF(trading_pairs, ''), NULLIF(risk_settings, ''), NULLIF(strategy_details, ''), last_trade,
  created_at, updated_at
FROM javalive_staging.trading_bots;

-- ============ copy_trading_experts (from copytradings) ============
INSERT INTO copy_trading_experts (id, tag, name, photo, rating, followers, equity, total_profit, status,
  description, win_rate, total_trades, price, type, created_at, updated_at)
SELECT id, tag, name, photo, rating, followers, equity, total_profit, status, description, win_rate,
  total_trades, CAST(NULLIF(price,'') AS DECIMAL(20,8)), type, created_at, updated_at
FROM javalive_staging.copytradings;

-- ============ user_copy_trades (from user_copytradings) ============
INSERT INTO user_copy_trades (id, user_id, expert_id, price, active, type, started_at, last_profit_at,
  total_profit, current_balance, total_trades, winning_trades, profit_percentage, equity, created_at, updated_at)
SELECT id, user, cptrading, CAST(NULLIF(price,'') AS DECIMAL(20,8)), active, type, started_at, last_profit,
  total_profit, current_balance, total_trades, winning_trades, profit_percentage,
  CAST(NULLIF(equity,'') AS DECIMAL(20,8)), created_at, updated_at
FROM javalive_staging.user_copytradings;

-- ============ signal_plans (legacy Signal model) ============
INSERT INTO signal_plans (id, name, price, type, increment_amount, created_at, updated_at)
SELECT id, name, CAST(NULLIF(price,'') AS DECIMAL(20,8)), type,
  CAST(NULLIF(increment_amount,'') AS DECIMAL(20,8)), created_at, updated_at
FROM javalive_staging.signals;

-- ============ contents ============
INSERT INTO contents (id, ref_key, title, description, created_at, updated_at)
SELECT id, ref_key, title, description, created_at, updated_at FROM javalive_staging.contents;

-- ============ cms_images (from images) ============
INSERT INTO cms_images (id, ref_key, title, description, image_path, created_at, updated_at)
SELECT id, ref_key, title, description, img_path, created_at, updated_at FROM javalive_staging.images;

-- ============ faqs ============
INSERT INTO faqs (id, ref_key, question, answer, created_at, updated_at)
SELECT id, ref_key, question, answer, created_at, updated_at FROM javalive_staging.faqs;

-- ============ terms_privacies ============
INSERT INTO terms_privacies (id, description, use_terms, created_at, updated_at)
SELECT id, description, (useterms = 'yes'), created_at, updated_at FROM javalive_staging.terms_privacies;

-- ============ binance_pay_transactions (from bnc_transactions) ============
INSERT INTO binance_pay_transactions (id, user_id, prepay_id, deposit_id, type, status, created_at, updated_at)
SELECT id, user_id, prepay_id, deposit_id, type, status, created_at, updated_at
FROM javalive_staging.bnc_transactions;

-- ============ activities ============
INSERT INTO activities (id, user_id, ip_address, device, browser, os, created_at, updated_at)
SELECT id, user, ip_address, device, browser, os, created_at, updated_at
FROM javalive_staging.activities;

-- Bump `users`' AUTO_INCREMENT well past the highest user id referenced ANYWHERE in the imported
-- historical data (not just MAX(users.id), which is only 278) — otherwise a newly-registered real
-- user could be assigned an id that collides with an orphaned historical user_id in
-- ledger_transactions/crypto_accounts/deposits/etc. (see MIGRATION-NOTES.md), silently attaching a
-- brand-new account to a stranger's old financial history. Found via a one-time query for the max
-- across every user_id-bearing table; 1000 leaves comfortable headroom above the observed max of 295.
ALTER TABLE users AUTO_INCREMENT = 1000;

SET FOREIGN_KEY_CHECKS = 1;
