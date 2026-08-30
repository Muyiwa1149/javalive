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
