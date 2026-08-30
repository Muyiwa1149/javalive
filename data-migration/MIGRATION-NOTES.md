# Data migration notes

## What ran, and in what order

1. `SQL/keystone.sql` (the real production dump) imported as-is into a database called
   `javalive_staging` — this is a raw, untouched copy of the source data, kept only as the
   read-only source for the transform step below. It is never written to.
2. The new schema (`backend/src/main/resources/db/migration/V1__*.sql` .. `V4__*.sql`) applied via
   Flyway into a separate database, `javalive_db` — this is what the Spring Boot app actually
   connects to (see `application.yml`).
3. `data-migration/transform-import.sql` reads from `javalive_staging` and writes into `javalive_db`,
   renaming columns to match the new schema and fixing types (money fields moved from
   `float`/`varchar` to `DECIMAL`, boolean-ish `varchar` flags moved to real `BOOLEAN`).

The live `keystone` database (the one the running PHP app actually uses) was never touched by any
of this — everything above operates on copies.

## Verified: zero data loss

Every table's row count in `javalive_db` after the transform matches `javalive_staging` exactly
(611/611 ledger transactions, 147/147 crypto accounts, 1851/1851 activity logs, 65/65 copy-trades,
7/7 deposits, 7/7 loans, all others 1:1). An earlier draft of the transform script had defensive
`WHERE user_id IN (SELECT id FROM users)`-style filters that silently discarded rows referencing a
now-missing user — those were found and removed (see finding below for *why* they were discarding
so much) since `FOREIGN_KEY_CHECKS` is already disabled for this one-time historical import and the
filters were redundant except for their (undesirable) side effect of deleting real data.

## Finding to flag to the business: orphaned historical data

The `users` table in the source dump has exactly **one** row (id 278, "Madisyn Lowe DDS", balance
$1,960). But `ledger_transactions`, `crypto_accounts`, `deposits`, `withdrawals`, `loans`,
`user_copy_trades`, `activities`, `kycs`, and `investments` all contain rows referencing dozens of
*other* user ids (245, 198, 193, 93, 294, 208, 151, 295, and more) that don't exist in `users` at
all. For example, user id 93 has 68 ledger entries totaling over $5M in transaction volume, and user
id 245 has 137 entries totaling ~$928K — neither of those users exists in the current `users` table.

This means at some point the production `users` table was reset, truncated, or had rows deleted
without the associated financial/activity tables being cleaned up alongside it — this is a
pre-existing characteristic of the source database, not something introduced by this migration.
All of that historical data has been preserved in `javalive_db` as-is (nothing was dropped), but it
currently has no resolvable owner. Before going live, the business should decide: is this expected
(e.g. a known demo/test data reset), or does it indicate a prior data-loss incident with the
`users` table that's worth investigating separately? Either way, the new app's UI/reports should
account for the possibility of orphaned historical rows (e.g. an admin "all transactions" report
that joins to `users` needs to handle a missing user gracefully, not crash or silently omit rows).

## Bug found and fixed while testing: new-user ID collision with orphaned data

Discovered via a real end-to-end registration test (not just row-count checks): because `users` only
has 1 real row (id 278) while other tables reference orphaned historical user ids up to 295, MySQL's
AUTO_INCREMENT for `users` would hand out id 279 to the next real registration — which collided
immediately with a pre-existing `crypto_accounts` row for user_id 279 left over from the orphaned
historical data (unique constraint violation on save). Worse than the crash itself: had that
constraint not existed, a new user could have silently inherited a stranger's old ledger
transactions/notifications/etc. by ID coincidence. Fixed by bumping `users` AUTO_INCREMENT to 1000
(comfortably past the highest referenced id of 295) as the last step of `transform-import.sql`.

## Not yet done (deferred, not urgent given the small real dataset)

- Referral-chain trace: with only 1 real user row, there's no multi-level referral chain to trace
  right now — this check becomes meaningful once real user data exists post-launch, not before.
- Precise balance-vs-ledger reconciliation for user 278: the ledger mixes many transaction *types*
  (Buy/Sell/ROI/Bonus/WIN/LOSE/Ref_bonus/...) that don't all sum directly into `account_balance` —
  a real reconciliation needs the business logic for which types affect balance, which belongs in
  Phase 2 (backend business logic), not the data-migration step.
