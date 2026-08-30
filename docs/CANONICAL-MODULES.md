# Canonical module decisions

This app has accumulated duplicate/parallel implementations of several features. Per the approved plan, only one implementation per module gets ported. This document records which one, and why, so every later phase builds against the same answer instead of re-deciding it ad hoc.

Two rounds of user decisions apply here:
1. **Duplicate systems** → port only the version that's live/reachable today.
2. **Nav-hidden features** (discovered after the first pass) → Copy Trading, AI Bots, Live Trading/Markets, Signals, Loans, and Currency Exchange are fully built but their nav links are commented out in the current Blade UI. The user chose to make these **live** in the new app. This means "commented out of nav" is no longer, by itself, a reason to exclude a module — it only matters for picking *between* competing implementations of the *same* feature.

## Plans / Investments

**Canonical: legacy system** — `Plans`, `Investment`, `User_plans` models; `UserInvPlanController` (`joinplan`, `joininvestmentplan`, `cancelPlan`) + `ViewsController` (`mplans`, `myplans`, `planDetails`) on the user side; `HomeController`/`InvPlanController` on the admin side (`dashboard/plans`, `dashboard/new-plan`, `dashboard/active-investments`).

**Retired:** the modern `Plan`/`UserPlan`/`PlanPayout`/`PlanCategory`/`PlanFeature` system (`UserPlanController`, `routes/user/plan-routes.php`'s `user.plans.*` group, admin `PlanController`/`routes/admin/plan-routes.php`'s `admin.plans.*` group).

**Why:** confirmed directly in both `resources/views/admin/sidebar.blade.php` (line 96: `/admin/dashboard/plans` labeled "Legacy Plans" is the only live link; the `admin.plans.index` link at line 91 is commented out) and `resources/views/layouts/dasht.blade.php` (lines 547/554: `route('mplans')` / `route('myplans', 'All')` are live under "Portfolio & Investments"; no `user.plans.*` link exists anywhere, live or commented). The legacy system is unambiguously what's wired into the real UI today, on both sides.

**Java rewrite implication:** `PlanPayout`'s clean payout-ledger tracking is worth keeping as a *pattern* even though the legacy `Investment`/`User_plans` schema itself is what gets migrated — the JPA entities should still record payouts as proper rows rather than replicating the legacy schema's looser tracking, per "don't preserve technical debt that isn't visible behavior."

## Copy Trading

**Canonical: the "new system"** — `Copytrading` model (experts) + `User_copytradings` (positions); user side `CopyTradingController` (`prefix('copy')`, `copy.dashboard`/`copy.experts`/`copy.start`/`copy.stop`/`copy.analytics`); admin side `CopyTradingAdminController` (`admin/copy/*`, full CRUD, blocks delete on active copiers, `statistics()`/`activeTrades()`).

**Retired:** `UserCopyTradingController` (both its legacy `mcopytradings`/`copy-trading-dashboard` routes and its "modern" `user.copy-trading.*` group — three route groups on one controller, none canonical), `Copytradingcontroller` (admin legacy CRUD), `UserCopyTradingConroller.php` (0 bytes, dead file — delete, don't port).

**Why:** with nav-visibility off the table as a tiebreaker (both admin implementations are equally nav-invisible today, and the feature is being made live regardless), the decision falls to code quality: `CopyTradingController`/`CopyTradingAdminController` is the cleaner, more complete, more recently-built pair (proper validation, blocks unsafe deletes, `statistics()`/`analytics()` endpoints) and the two sides already agree on the same route-naming convention (`copy.*` user / `admin.copy.*` admin) and the same underlying tables. The legacy pair is duplicate functionality with looser validation and no real advantage.

**Note:** `AutoTaskController::automaticCopyTradingProfits()` and `App\Console\Commands\GenerateCopyTradingProfits` both implement copy-trading profit simulation independently — in Phase 2, consolidate to **one** Spring `@Scheduled` job, modeled on whichever of the two has the more correct/complete win-loss math (verify at implementation time, not guessed here).

## Notifications

**Canonical: the `NotificationController` (`routes/admin/notification.php`) implementation** — its unprefixed AJAX endpoints (`/admin/notifications/count`, `/mark-all-read`, `/mark-as-read/{id}`) are what `topmenu.blade.php`'s topbar bell actually calls via jQuery, confirmed in the admin research. `AdminNotificationController`'s parallel prefixed routes are unused by any view.

**Retired:** `AdminNotificationController`.

## ROI / profit-processing jobs

**Canonical: to be confirmed at Phase 2 implementation time**, not guessed here — there are five overlapping code paths (`AutoTaskController::autotopup()`, `AutoRoiController::processAutomaticRoi()`, `ProcessInvestmentRoi` command (scheduled every 5 min in `Kernel.php`), `ProcessInvestmentPlanRoi` command (unscheduled), plus the copy-trading profit duplication noted above). Since Plans is staying on the **legacy** `Investment`/`User_plans` schema, the canonical ROI job must be the one that actually operates on those tables with correct math — confirm which of `AutoTaskController::automaticRoi()` vs the `ProcessInvestmentRoi`/`ProcessInvestmentPlanRoi` commands targets the legacy schema (vs. the retired modern `Plan`/`UserPlan` schema) before porting, by reading each implementation's model references at that time.

## Signals — NOT a duplicate, both are in scope

`ViewsController@signal`/`@mysingals`/`@tradeSignals` (legacy `Signal` model — a user-purchasable "signal plan," structurally like an investment plan) and `SignalProvderController` (external-API-backed forex signal broadcast + Telegram push, per the "keep proxying external SaaS" decision) are **two different features that happen to share the word "signal."** Both get built: the legacy signal-plan subscription as a local feature (now live per the hidden-features decision), and `SignalProvderController` as part of the external SaaS integration already committed to.

## External SaaS-proxied modules (unaffected by canonical-module decisions)

Membership/Courses, Subscription (copy-trade master accounts), Trading Accounts, Signal Provider — no local duplicates to resolve; these get built as-is per the "keep proxying `app.getonlinetrader.pro`" decision.

## Dead files — confirmed, do not port

`PlanController_fixed.php`, `Settings/currencies copy.php`, `Auth/ResetPasswordController.php` (empty stub), `TradingPaymentController.php` (orphaned — but see below), `UserCopyTradingConroller.php` (0 bytes), `SomeController.php` (0 bytes), `SignalConversation.php` (0 bytes), `DebugController.php` (unrouted dev artifact), `admin/dashboard-redesign.blade.php` / `sidebar-redesign.blade.php` / `topmenu-redesign.blade.php` (abandoned redesign, unreferenced).

**One fix, not just a drop:** the `tra.pay` route today points at `TradingAccountController@payment`, a method that doesn't exist (broken in production). The intended handler is clearly `TradingPaymentController@payment` (its view, `admin.subscription.payment`, exists and matches). In the Java rewrite, wire this correctly rather than reproducing the 500 error — this is fixing an accidental break, not changing designed behavior.

## Known schema gaps to resolve in Phase 1

- `loans` table has no Laravel migration at all (raw `DB::table` usage) — reconstruct from `SQL/keystone.sql`.
- Three migrations touch `plan_plan_category` redundantly, two touch `notifications` redundantly, and `create_messages_table` is an empty migration — collapse to one clean Flyway migration each. (Since Plans stays on the legacy schema, the `plan_plan_category`/`plan_categories`/`plan_features` tables belong to the *retired* modern system and do not need porting at all — simpler than originally scoped.)
