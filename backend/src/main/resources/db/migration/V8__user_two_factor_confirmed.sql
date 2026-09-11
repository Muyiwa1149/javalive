-- Source's `users` table (Jetstream/Fortify) has `two_factor_secret`/`two_factor_recovery_codes`
-- but never migrated a `two_factor_confirmed_at` column, even though Fortify's standard TOTP setup
-- flow requires distinguishing "secret generated, awaiting confirmation" from "actually active" —
-- without it, a half-finished setup (secret saved, confirmation code never entered) would
-- incorrectly gate login. Added here, matching V7's precedent for filling a real migration gap.
ALTER TABLE users ADD COLUMN two_factor_confirmed_at TIMESTAMP NULL DEFAULT NULL;
