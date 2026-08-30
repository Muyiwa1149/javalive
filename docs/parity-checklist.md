# Parity checklist

Master "nothing missing" tracker for the JAVALIVE rewrite. Every row gets three checkmarks as work lands: **B**ackend endpoint built, **F**rontend page/component built, **V**erified against the running PHP app (Phase 7). Canonical-module decisions from `CANONICAL-MODULES.md` are already applied here — retired duplicates are not listed as separate rows.

Legend: `[ ]` not started · `[~]` in progress · `[x]` done

---

## Phase 3 — Public site

| Module | B | F | V | Notes |
|---|---|---|---|---|
| Layout shell (header/nav/footer/ticker/chat widgets) | [ ] | [ ] | [ ] | from `layouts/base.blade.php` |
| Home `/` | [ ] | [ ] | [ ] | `home/index.blade.php` |
| Terms `/terms`, trading-conditions alias | [ ] | [ ] | [ ] | |
| Privacy `/privacy` | [ ] | [ ] | [ ] | fix unused `$policy` var while porting |
| About `/about` | [ ] | [ ] | [ ] | |
| Contact `/contact` + `/contacts` alias + contact form submit | [ ] | [ ] | [ ] | POST handled by `UsersController@sendcontact` today |
| FAQ `/faq` | [ ] | [ ] | [ ] | hardcoded tabs; wire to real `Faq` model data instead of hardcoding (behavior-invisible improvement) |
| Why Us `/why-us` | [ ] | [ ] | [ ] | |
| Regulation `/regulation` | [ ] | [ ] | [ ] | |
| ETFs `/etfs` | [ ] | [ ] | [ ] | |
| Forex `/forex` | [ ] | [ ] | [ ] | |
| For Traders `/for-traders` | [ ] | [ ] | [ ] | |
| Cryptocurrencies `/cryptocurrencies` | [ ] | [ ] | [ ] | |
| Indices `/indices` | [ ] | [ ] | [ ] | |
| Shares `/shares` | [ ] | [ ] | [ ] | |
| Trade `/trade` (marketing page, not the dashboard trade module) | [ ] | [ ] | [ ] | passes plans/mplans/pplans |
| Automate `/automate` | [ ] | [ ] | [ ] | |
| Copy (marketing) `/copy` | [ ] | [ ] | [ ] | |
| ~~NFTs `/nfts`~~ | — | — | — | excluded: view doesn't exist today, route 500s |
| ~~investment/license/security/assetss~~ | — | — | — | excluded: orphaned, no route |
| Auth: login/register/forgot-password/reset-password/confirm-password/2FA/verify-email | [ ] | [ ] | [ ] | from `guest1` layout + `auth/*.blade.php` |
| Social login redirect/callback (Google, Facebook live; Twitter/LinkedIn/GitHub/Bitbucket routed-but-inert) | [ ] | [ ] | [ ] | |
| Referral capture `/ref/{id}` | [ ] | [ ] | [ ] | |
| Language switch (session-only, no real i18n today) | [ ] | [ ] | [ ] | low priority — currently has no visible effect |
| CoinGecko price ticker/hero stats | [ ] | [ ] | [ ] | server + client-side today |
| FAQ/testimonial/content CMS read endpoints | [ ] | [ ] | [ ] | admin-manageable via `FrontendController` |

---

## Phase 4 — User dashboard

| Module | B | F | V | Notes |
|---|---|---|---|---|
| Layout shell (sidebar/topbar/balance/notif bell/dark-mode/CoinGecko marquee) | [ ] | [ ] | [ ] | from `layouts/dasht.blade.php`; **all sections un-hidden** per user decision |
| Dashboard home | [ ] | [ ] | [ ] | stat cards, KYC pill, recent plans/transactions |
| Profile / account settings | [ ] | [ ] | [ ] | `ProfileController` |
| Account security (2FA-ish settings page) | [ ] | [ ] | [ ] | `profile.show` composite |
| Connect wallet (mnemonic) | [ ] | [ ] | [ ] | **flag for security review before porting as-is** — currently stores phrase in plaintext; decide encryption-at-rest during Phase 2/4 build, don't silently replicate a plaintext-secret store |
| KYC submission + status | [ ] | [ ] | [ ] | `VerifyController`, id front/back upload |
| Deposits: method select, Stripe, Paystack, manual methods | [ ] | [ ] | [ ] | Flutterwave currently unwired in PHP — **include it live** per "make hidden features live" (it's fully implemented, just unrouted) |
| Deposits: CoinPayments, Binance | [ ] | [ ] | [ ] | |
| Deposit history | [ ] | [ ] | [ ] | |
| Withdrawals: method select, OTP flow, submit | [ ] | [ ] | [ ] | |
| Withdrawal history | [ ] | [ ] | [ ] | |
| Internal transfer | [ ] | [ ] | [ ] | |
| Plans: browse/buy, my plans, plan details, cancel, withdraw profit | [ ] | [ ] | [ ] | legacy `Plans`/`Investment`/`User_plans` (canonical) |
| Copy Trading: dashboard, experts list, start/stop, analytics | [ ] | [ ] | [ ] | `CopyTradingController` (canonical) — now live in nav |
| Bots: browse, invest, my investments, history, analytics | [ ] | [ ] | [ ] | `UserBotController` — now live in nav |
| Trade/Markets: instrument list, single, monitor, search | [ ] | [ ] | [ ] | `TradeController` — now live in nav |
| Exchange: asset balances, swap, swap history | [ ] | [ ] | [ ] | `ExchangeController` — now live in nav |
| Loans: apply, history | [ ] | [ ] | [ ] | schema reconstructed from SQL dump — now live in nav |
| Signals (legacy signal-plan subscription) | [ ] | [ ] | [ ] | `ViewsController@signal/mysingals/tradeSignals` — now live in nav |
| Signals (external SaaS broadcast + Telegram) | [ ] | [ ] | [ ] | user-facing subscriber view of `SignalProvderController` |
| MT4 subscription (save/delete/renew) | [ ] | [ ] | [ ] | `UserSubscriptionController`, proxies external API |
| Membership/Courses | [ ] | [ ] | [ ] | proxies external API — was fully commented out; **build it live** per decision |
| Notifications: list/show/mark-read/delete/count | [ ] | [ ] | [ ] | |
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
