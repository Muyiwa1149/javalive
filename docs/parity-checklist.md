# Parity checklist

Master "nothing missing" tracker for the JAVALIVE rewrite. Every row gets three checkmarks as work lands: **B**ackend endpoint built, **F**rontend page/component built, **V**erified against the running PHP app (Phase 7). Canonical-module decisions from `CANONICAL-MODULES.md` are already applied here — retired duplicates are not listed as separate rows.

Legend: `[ ]` not started · `[~]` in progress · `[x]` done

---

## Phase 3 — Public site

| Module | B | F | V | Notes |
|---|---|---|---|---|
| Layout shell (header/nav/footer/ticker/chat widgets) | [x] | [x] | [ ] | `PublicLayout.vue`, ported from `layouts/base.blade.php`; site is dark-mode-only in source (toggle UI was dead code), so no light-mode toggle built |
| Home `/` | — | [x] | [ ] | `Home.vue`; 15 sections ported (source has ~400 lines of genuinely dead code inside broken HTML comments, verified via comment-nesting trace, correctly excluded); plan cards + BTC hero stats stubbed pending a public plans API (BTC price itself already live via client-side CoinGecko fetch) |
| Terms `/terms`, trading-conditions alias | — | [x] | [ ] | `Terms.vue`; fully static/hardcoded in source (does NOT use `$terms->description` — that's Privacy only) |
| Privacy `/privacy` | [x] | [x] | [ ] | `Privacy.vue`, wired to new `GET /api/public/privacy-policy`. Finding: the checklist's earlier "`$policy` var" note was actually `$terms` (`TermsPrivacy::find(1)`) — not a bug, it's populated via a global `View::share` in `AppServiceProvider`, not the controller's `->with()` |
| About `/about` | — | [x] | [ ] | `About.vue` |
| Contact `/contact` + `/contacts` alias + contact form submit | [x] | [x] | [ ] | `Contact.vue` submits to new `POST /api/public/contact`. Finding: source form actually only builds a `mailto:` link client-side (Alpine `@click.prevent`) — `UsersController@sendcontact` exists but isn't wired to this UI. Deliberately used the real backend endpoint instead of replicating the mailto shortcut |
| FAQ `/faq` | [x] | [x] | [ ] | `Faq.vue`, wired to new `GET /api/public/faqs` (confirmed live data, not hardcoded) |
| Why Us `/why-us` | — | [x] | [ ] | `WhyUs.vue` |
| Regulation `/regulation` | — | [x] | [ ] | `Regulation.vue` |
| ETFs `/etfs` | — | [x] | [ ] | `Etfs.vue`; listings table has no data rows in source either (DataTables-populated, empty fallback) |
| Forex `/forex` | — | [x] | [ ] | `Forex.vue` |
| For Traders `/for-traders` | — | [x] | [ ] | `ForTraders.vue` (source file is `fortrader.blade.php`) |
| Cryptocurrencies `/cryptocurrencies` | — | [x] | [ ] | `Cryptocurrencies.vue`; markets table empty in source too |
| Indices `/indices` | — | [x] | [ ] | `Indices.vue` |
| Shares `/shares` | — | [x] | [ ] | `Shares.vue`; table empty in source too |
| Trade `/trade` (marketing page, not the dashboard trade module) | — | [x] | [ ] | `Trade.vue`; pricing-card `$plans` loop omitted (no fallback data in source, no public plans API yet) |
| Automate `/automate` | — | [x] | [ ] | `Automate.vue` |
| Copy (marketing) `/copy` | — | [x] | [ ] | `Copy.vue` |
| ~~NFTs `/nfts`~~ | — | — | — | excluded: view doesn't exist today, route 500s |
| ~~investment/license/security/assetss~~ | — | — | — | excluded: orphaned, no route |
| Auth: login/register | [x] | [x] | [ ] | `Login.vue`, `Register.vue` (3-step wizard, matches source). **Finding+fix**: `username` was missing entirely from Phase 2's register flow — source requires it (unique) and referral resolution depends on it; added to `RegisterRequest`/`AuthService`, tested. **Finding+fix**: referral resolution — `/ref/{username}` must resolve to the sponsor's numeric id (all downline/commission logic keys on that), not store the raw referral string; fixed and tested end-to-end (sponsor→referred chain correct in DB) |
| Auth: forgot-password/reset-password (both guards) | [x] | [x] | [ ] | `ForgotPassword.vue`, `ResetPassword.vue`. `PasswordResetService`, single-use 60min tokens, shared `password_reset_tokens` table (guard-tagged) — tested end-to-end incl. reuse rejection |
| Auth: confirm-password | [x] | [x] | [ ] | `ConfirmPassword.vue` — standalone re-auth screen, `redirect` query param support |
| Auth: 2FA (admin) | [x] | [x] | [ ] | `AdminLogin.vue`/`AdminTwoFactor.vue`. **Found and fixed a real bug** while wiring the frontend: the pending-2FA token was captured on login but never sent with the `/2fa` verify call (the shared `api.js` interceptor was clobbering any explicit `Authorization` header with the stale `admin_token`). Fixed `api.js` (explicit header now wins) and `authAdmin.js` (stores/sends the pending token). Tested end-to-end twice — once with a throwaway 2FA-enabled admin (login → OTP → verify → real session), once with a throwaway admin through forgot/reset-password → login with new password — all correct. AdminTwoFactor.vue correctly built against the real 5-digit-OTP format (not source's UI copy claiming "6-digit"), no recovery-code UI (backend has none) |
| Auth: admin forgot/reset-password | [x] | [x] | [ ] | `AdminForgotPassword.vue`/`AdminResetPassword.vue`. **Finding**: source's reset page expects a hand-typed 10-digit token, but the actual issued token is a 32-char UUID delivered via emailed link — built against the real format (query-param link, same as the user flow), not the source UI's stale expectation |
| Auth: 2FA (user)/verify-email | [ ] | [x] | [ ] | `VerifyEmail.vue` ported for visual parity only. **Finding**: source's email-verification is dead code — `UserObserver` checks `$settings->enable_verification` (wrong column name; real column is `enable_email_verification`), so the condition is always false and `email_verified_at` is never auto-set; Laravel's `verified` route middleware is also never applied anywhere. Despite `enable_email_verification=1` in live data, nothing is actually gated. Decision: ported the page but did not build a resend API (none exists, none should — would invent behavior the source never had); "Resend" shows the same success message the source shows, "Sign Out" is genuinely wired. User-side login 2FA challenge (separate from admin's) was never built in Phase 2 — `enable_2fa=false` in live data so it's currently invisible; tracked as a real gap for later, not silently dropped |
| Social login redirect/callback (Google, Facebook live; Twitter/LinkedIn/GitHub/Bitbucket routed-but-inert) | [ ] | [ ] | [ ] | |
| Referral capture `/ref/{id}` | — | [ ] | [ ] | backend already accepts `refBy` on register; just need a frontend route that captures the code and forwards to `/register` |
| Language switch (session-only, no real i18n today) | [ ] | [ ] | [ ] | low priority — currently has no visible effect |
| CoinGecko price ticker/hero stats | [x] | [x] | [ ] | client-side fetch, ported live in `Home.vue` |
| FAQ/testimonial/content CMS read endpoints | [x] | [~] | [ ] | `GET /api/public/faqs`, `/testimonials`, `/privacy-policy` all built and tested; testimonials endpoint has no frontend consumer yet (0 rows in current data anyway) |

---

## Phase 4 — User dashboard

| Module | B | F | V | Notes |
|---|---|---|---|---|
| Layout shell (sidebar/topbar/balance/notif bell/dark-mode/CoinGecko marquee) | — | [x] | [ ] | `UserLayout.vue`, ported from `layouts/dasht.blade.php`. **All previously-commented-out nav sections un-hidden** per decision: Trading & Markets, Trading Signals, Credit & Financing, Currency Exchange were all commented out in source — now live. **Added new nav items** not in source at all: Membership/Courses, MT4 Subscription, Connect Wallet, external Signal Providers — per "make hidden features live" decision. Dark/light toggle also made functional (source had full `dark:` CSS classes throughout but the toggle button itself was commented out — low-risk to enable since the styling was already there) |
| Dashboard home | [x] | [x] | [ ] | `Dashboard.vue` + `GET /api/dashboard/summary`. Ported the source's lazy signup-bonus-crediting side effect (credited on first dashboard load) — tested end-to-end including double-credit protection. Stat cards, KYC pill, recent plans, recent activity all wired to real data. Referral link now computed client-side from username (`/ref/{username}`) rather than stored server-side — source stored a full absolute URL in `ref_link` that could go stale if the domain changed; this is a low-risk simplification |
| Profile / account settings | [x] | [x] | [ ] | `Profile.vue` + `ProfileController`/`ProfileService` — profile info, payout/withdrawal details (bank + crypto addresses), password change, email preferences, matching the source's 4 separate update endpoints. Tested end-to-end including wrong-current-password rejection |
| Account security (2FA-ish settings page) | [ ] | [ ] | [ ] | `profile.show` composite — not yet built, low priority (overlaps with Profile page) |
| Connect wallet (mnemonic) | [ ] | [ ] | [ ] | **flag for security review before porting as-is** — currently stores phrase in plaintext; decide encryption-at-rest during Phase 2/4 build, don't silently replicate a plaintext-secret store |
| KYC submission + status | [x] | [x] | [ ] | `KycController`/`KycService`, id front/back upload via new `FileStorageService`. Tested end-to-end incl. bad-extension rejection |
| Deposits: manual methods (Wire Transfer, ACH, Cryptocurrency, Check Mail), admin approval | [x] | [x] | [ ] | **Decision (superseded from original plan)**: no live payment-gateway integration (Stripe/Paystack/Flutterwave/CoinPayments/Binance) — user explicitly chose manual-only, admin-approved deposits across these 4 method categories instead. Cryptocurrency reuses the existing migrated `wdmethods` crypto rows (Bitcoin, USDT, etc.); Wire Transfer/ACH/Check Mail seeded via `V5__manual_deposit_methods.sql`. Built the full admin-approval crediting path: balance credit, deposit-bonus %, and the 6-level referral commission cascade (`ReferralCommissionService`, reimplements source's `getAncestors` recursion as a direct chain walk — same result, no full-table scan). Built a minimal functional `AdminLayout.vue` + admin Deposits page to host the approval flow (full Phase 5 admin rebuild still pending). **Bug found+fixed during testing**: `adminList()` threw 500 on lazy `deposit.getUser()` access outside a transaction (`open-in-view: false`) — added `@Transactional(readOnly = true)`. Tested end-to-end: submit → admin list → approve → balance + referral commission credited correctly (verified exact % math) → double-approve rejected → delete/reject path also verified |
| Deposit history | [x] | [x] | [ ] | part of Deposits.vue |
| Withdrawals: method select, OTP flow, submit | [x] | [x] | [ ] | `WithdrawalController`/`WithdrawalService`, admin `pwithdrawal` mirrored via `AdminWithdrawalController`. No live payout-gateway (CoinPayments auto-withdraw) — same manual/admin-approved decision as deposits. **Preserved exactly**: the dual `deduction_option` timing (`userRequest` debits at request time + refunds on reject; `AdminApprove` only touches balance on admin action) — tested end-to-end in both modes, including OTP validation, KYC gate (reused `KycGuardService` from Phase 2), min-amount/insufficient-balance checks, charges calc (%/fixed), and crypto/bank destination address saved to the user's profile. **Finding**: source's separate `userwithdrawal()` withdrawal-code-PIN endpoint is dead code — never called by the actual withdrawal flow (`completewithdrawal` doesn't check it) — not built, matching the verify-email precedent |
| Withdrawal history | [x] | [x] | [ ] | part of Withdrawals.vue |
| Internal transfer | [x] | [x] | [ ] | `TransferController`/`TransferService`, mirrors `TransferController@transfertouser` exactly (password re-auth, self-transfer block, email-or-username recipient lookup, % charges, ledger entries both sides, receiver email). **Bug found+fixed during testing**: `TransferRequest.amount` had `@NotBlank` on a `BigDecimal` field (copy-paste mistake) — Bean Validation doesn't support `@NotBlank` on non-`CharSequence` types, threw an unhandled 500. Also found and fixed a real gap while debugging this: `GlobalExceptionHandler`'s catch-all handler wasn't logging unexpected exceptions at all, making this kind of bug invisible server-side — added `log.error(...)`. Tested end-to-end: wrong password, self-transfer, insufficient funds, transfer by username, transfer by email, history — all correct |
| Plans: browse/buy, my plans, plan details, cancel, withdraw profit | [x] | [x] | [ ] | `PlanController`/`PlanService`, mirrors `UserInvPlanController@joininvestmentplan/cancelPlan` + `PlanProfitController@withdrawProfit` exactly (min/max price validation, gift bonus, expiry from `Plan.expirationDays`, capital-refund-on-cancel, available-profit-exceeded check). **Schema improvement**: added a proper `investment_id` FK to `ledger_transactions` (V6 migration) instead of replicating the source's loosely-typed polymorphic `user_plan_id` reuse — per `CANONICAL-MODULES.md`'s explicit guidance to keep clean tracking. Tested end-to-end: purchase (balance debit, expiry date math verified exactly against `expirationDays`), withdraw-profit (exceeds-available rejected, correct credit), cancel (capital refund, double-cancel rejected), investment-detail transaction history correctly scoped per-investment. Plans.vue (browse/purchase) + MyPlans.vue (expandable portfolio list with cancel/withdraw-profit inline, no separate detail route needed) |
| Copy Trading: dashboard, experts list, start/stop, analytics | [x] | [x] | [ ] | Canonical "new system" (`CopyTradingController`/`CopyTradingAdminController`) per `CANONICAL-MODULES.md`. Mirrors dashboard/experts/startCopyTrading/stopCopyTrading/analytics exactly — min-investment check, balance check, no-duplicate-active-copy guard, follower count inc/dec, capital-return-on-stop. Recent-activity feed replicates the source's own randomized simulated-trades display (that's the designed behavior, not a shortcut — the actual profit/loss accrual scheduler is separate, deliberately deferred to Phase 6 per the canonical-modules note). Tested end-to-end incl. duplicate-copy and below-minimum rejection. CopyTrading.vue: tabbed My-Copies/Browse-Experts UI (single page, since both map to one nav entry) |
| Bots: browse, invest, my investments, history, analytics | [x] | [x] | [ ] | `BotController`/`BotService`, mirrors `UserBotController` exactly (min/max investment range, no-duplicate-active-investment guard, capital-refund-on-cancel, per-bot trade history). Trade-history/profit-accrual scheduler intentionally not built — same Phase 6 deferral as copy trading. Tested end-to-end against real bot data: invest (range validation, duplicate rejection), dashboard aggregates, cancel (balance credit). Bots.vue: tabbed Browse/My-Investments UI matching the CopyTrading.vue pattern |
| Trade/Markets: instrument list, single, monitor, search | [ ] | [ ] | [ ] | `TradeController` — now live in nav |
| Exchange: asset balances, swap, swap history | [ ] | [ ] | [ ] | `ExchangeController` — now live in nav |
| Loans: apply, history | [ ] | [ ] | [ ] | schema reconstructed from SQL dump — now live in nav |
| Signals (legacy signal-plan subscription) | [ ] | [ ] | [ ] | `ViewsController@signal/mysingals/tradeSignals` — now live in nav |
| Signals (external SaaS broadcast + Telegram) | [ ] | [ ] | [ ] | user-facing subscriber view of `SignalProvderController` |
| MT4 subscription (save/delete/renew) | [ ] | [ ] | [ ] | `UserSubscriptionController`, proxies external API |
| Membership/Courses | [ ] | [ ] | [ ] | proxies external API — was fully commented out; **build it live** per decision |
| Notifications: list/show/mark-read/delete/count | [x] | [x] | [ ] | backend done since Phase 2; `Notifications.vue` full page + topbar bell dropdown (real unread count/list) both built now |
| Referral page + downline view | [ ] | [ ] | [ ] | |
| Support page | [ ] | [ ] | [ ] | |
| Trading/account history pages | [ ] | [ ] | [ ] | |

---

## Phase 5 — Admin dashboard

| Module | B | F | V | Notes |
|---|---|---|---|---|
| Layout shell (Tailwind rebuild of Atlantis look) | [ ] | [ ] | [ ] | sidebar nav gets **every** item un-hidden per decision, not just the currently-visible subset |
| Admin auth: login/logout, forgot/reset password, email-OTP 2FA | [ ] | [ ] | [ ] | |
| Dashboard home: 8 stat cards + bar chart | [ ] | [ ] | [ ] | Chart.js in Vue |
| Manage Users: list/detail/edit/block/unblock/impersonate/reset-pwd/history/segmented email | [ ] | [ ] | [ ] | |
| Manage Deposits: list, approve (+ referral cascade), delete, view proof | [ ] | [ ] | [ ] | note: no explicit "reject" today, only delete/approve — preserve as-is |
| Manage Withdrawals: review, approve/reject (dual deduction-timing modes) | [ ] | [ ] | [ ] | preserve `AdminApprove` vs `userRequest` branching exactly |
| KYC: list, accept/reject | [ ] | [ ] | [ ] | |
| Plans (legacy, canonical): CRUD, active investments | [ ] | [ ] | [ ] | |
| Copy Trading (new system, canonical): expert CRUD, statistics, active trades | [ ] | [ ] | [ ] | now needs a real nav entry (currently had none) |
| Bots: full CRUD, per-bot analytics, bots-wide dashboard | [ ] | [ ] | [ ] | |
| CRM: task CRUD, lead conversion, lead assignment | [ ] | [ ] | [ ] | |
| Import: Excel bulk lead/user import + template download | [ ] | [ ] | [ ] | Apache POI |
| IP blacklist: list/add/delete | [ ] | [ ] | [ ] | fix HTML-in-JSON response into a real JSON contract |
| Manage Admins: CRUD, block/unblock, 2FA toggle, roles | [ ] | [ ] | [ ] | **add server-side role enforcement** (security hardening, see plan) |
| Settings: App settings | [ ] | [ ] | [ ] | |
| Settings: Payment gateways (Stripe/Paystack/Flutterwave/CoinPayments/Binance/manual methods) | [ ] | [ ] | [ ] | |
| Settings: Referral/bonus | [ ] | [ ] | [ ] | |
| Settings: Subscription fees | [ ] | [ ] | [ ] | |
| Settings: Crypto/asset toggles + exchange fee | [ ] | [ ] | [ ] | |
| Frontend/content management: FAQ, testimonials, images, page content, privacy policy | [ ] | [ ] | [ ] | |
| Notifications (canonical: `NotificationController`) | [ ] | [ ] | [ ] | |
| Membership/Courses/Categories admin | [ ] | [ ] | [ ] | proxies external API |
| Subscription (copy-trade master accounts) admin | [ ] | [ ] | [ ] | proxies external API |
| Trading Accounts admin (incl. fixed `tra.pay` route) | [ ] | [ ] | [ ] | proxies external API |
| Signal Provider admin (post/publish/update-result + Telegram push, fee settings) | [ ] | [ ] | [ ] | proxies external API |
| Wallet-connect admin (client phrase keys, phrase settings) | [ ] | [ ] | [ ] | tie to the plaintext-storage flag above |
| Clear cache utility | [ ] | [ ] | [ ] | |

---

## Phase 2 progress — Backend core (auth done, shared services in progress)

- [x] JWT infrastructure (`JwtService`, `JwtAuthenticationFilter`), `UserPrincipal`/`AdminPrincipal`, `SecurityConfig` with guard-isolated routing (`/api/**` = ROLE_USER, `/api/admin/**` = ROLE_ADMIN)
- [x] User register/login/me — tested end-to-end against the running app (not just compiled)
- [x] Admin login + email-OTP 2FA (pending-token exchange flow) + me — tested end-to-end including guard isolation (a user token correctly gets 403 on admin routes)
- [x] **Bug found via live testing, fixed**: new-user AUTO_INCREMENT could collide with orphaned historical `user_id` references (see `data-migration/MIGRATION-NOTES.md`) — `@Lob` mismapping on 3 entities also found and fixed the same way (schema-validation failure caught at boot, not by compiling alone)
- [x] Settings service (`SettingsService`, cached single-row `app_settings` wrapper) + `PublicSettingsSummary`/`PublicSettingsController` (`GET /api/public/settings`) — tested live, returns real migrated branding data
- [x] `NotificationService` (shared user/admin) + `UserNotificationController` (`/api/notifications*`) + `AdminNotificationController` (`/api/admin/notifications*`) — list/count/mark-read/mark-all-read/delete, tested live
- [x] IP blacklist: `IpBlacklistService` (cached) + `IpBlacklistFilter` (global, wired into `SecurityConfig` before JWT filter) — tested live (blocked IP gets 403, others pass)
- [x] `KycGuardService.requireVerified(user)` — reusable guard for Phase 4 controllers to call on money-moving actions, gated by `enableKyc` setting
- [ ] Consolidated scheduled jobs, external SaaS proxy client, Telegram bot service — deferred: build alongside the Phase 4/5/6 features that actually consume them, not as standalone infra

## Phase 6 — Cross-cutting integrations

| Item | B | F | V | Notes |
|---|---|---|---|---|
| Email templates (all Mailables) | [ ] | — | [ ] | Deposit/Withdrawal status, KYC decision, 2FA OTP, admin OTP/success, new registration, task assignment, etc. |
| Telegram bot: greeting handler + signal push notifications | [ ] | — | [ ] | `SignalConversation` was empty — nothing to port there |
| File storage: KYC docs, deposit proofs, bot/plan/copy-trading photos, avatars, barcodes | [ ] | — | [ ] | local disk + optional S3 |
| Excel import/export | [ ] | — | [ ] | Apache POI |
| Scheduled jobs (consolidated): ROI, copy-trading profits, bot profits, price fetch, notification cleanup | [ ] | — | [ ] | canonical choice per `CANONICAL-MODULES.md` |
| External SaaS proxy client (`OnlineTraderApiClient`) | [ ] | — | [ ] | shared by Membership/Subscription/TradingAccounts/SignalProvider |

---

## Phase 1 — Data migration (tracked separately, see `data-migration/`) — DONE

- [x] Schema reconciled into Flyway migrations (legacy Plans schema retained; modern Plan/UserPlan schema dropped; `loans` reconstructed; redundant migrations collapsed) — V1-V4 in `backend/src/main/resources/db/migration/`, applied cleanly, Hibernate validation passes
- [x] `keystone.sql` loaded into staging (`javalive_staging`, isolated from live `keystone` DB) and transformed into `javalive_db`
- [x] Row-count spot checks per table — exact 1:1 match on every table, see `data-migration/MIGRATION-NOTES.md`
- [~] Balance-sum integrity check — deferred to Phase 2 (needs business logic for which ledger transaction types affect balance); real dataset only has 1 current user so this check has limited value right now anyway
- [ ] Referral-chain spot check — deferred, not meaningful with only 1 real user row (see MIGRATION-NOTES.md finding on orphaned historical data)
- [!] **Finding for the business**: source `users` table has only 1 real row, but financial/activity tables reference dozens of other historical user ids that no longer exist there — flagged in `data-migration/MIGRATION-NOTES.md`, not yet resolved with the user

---

## Phase 8 — Deployment readiness

- [ ] `mvn package` produces a clean runnable jar
- [ ] `vite build` produces a clean production bundle
- [ ] `application-prod.yml` populated from real `.env` / DB-stored settings
- [ ] docker-compose or native Windows run docs written and tested
- [ ] Fresh-machine smoke test: `mvn install` + `npm install` + boot, zero errors
