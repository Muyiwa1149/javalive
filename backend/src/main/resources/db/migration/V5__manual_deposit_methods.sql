-- Seeds the manual (admin-approved) deposit methods requested for Phase 4: Wire Transfer and ACH
-- (bank-style, admin fills in real account details later), Check Mail (admin fills in a mailing
-- address). Cryptocurrency deposits reuse the existing `wdmethods` crypto rows (Bitcoin, USDT,
-- etc.) already migrated from the source data -- no new rows needed for those.
--
-- `wdmethods.wallet_address` is reused here to hold Check Mail's mailing address text (it's a
-- generic TEXT column despite the crypto-flavored name; no dedicated address field exists on this
-- table and adding one for a single method wasn't worth a schema change).

INSERT INTO wdmethods (name, method_type, type, minimum_amount, maximum_amount, charges_amount, charges_type,
                        duration_note, bank_name, account_name, account_number, swift_code, wallet_address,
                        is_default, status, created_at, updated_at)
VALUES
    ('Wire Transfer', 'bank', 'deposit', 100.00000000, 1000000.00000000, 0, 'percentage',
     '1-3 business days', NULL, NULL, NULL, NULL, NULL, 0, 'enabled', NOW(), NOW()),
    ('ACH', 'bank', 'deposit', 50.00000000, 500000.00000000, 0, 'percentage',
     '3-5 business days', NULL, NULL, NULL, NULL, NULL, 0, 'enabled', NOW(), NOW()),
    ('Check Mail', 'mail', 'deposit', 50.00000000, 500000.00000000, 0, 'percentage',
     '7-14 business days', NULL, NULL, NULL, NULL, NULL, 0, 'enabled', NOW(), NOW());
