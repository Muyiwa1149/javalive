import { createRouter, createWebHistory } from 'vue-router'
import { useAuthUserStore } from '@/stores/authUser'
import { useAuthAdminStore } from '@/stores/authAdmin'

const PublicLayout = () => import('@/layouts/PublicLayout.vue')
const AuthLayout = () => import('@/layouts/AuthLayout.vue')
const UserLayout = () => import('@/layouts/UserLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

const routes = [
  {
    path: '/',
    component: PublicLayout,
    children: [
      { path: '', name: 'home', component: () => import('@/pages/public/Home.vue') },
      { path: 'about', name: 'about', component: () => import('@/pages/public/About.vue') },
      { path: 'why-us', name: 'why-us', component: () => import('@/pages/public/WhyUs.vue') },
      { path: 'trade', name: 'trade', component: () => import('@/pages/public/Trade.vue') },
      { path: 'regulation', name: 'regulation', component: () => import('@/pages/public/Regulation.vue') },
      { path: 'for-traders', name: 'for-traders', component: () => import('@/pages/public/ForTraders.vue') },
      { path: 'automate', name: 'automate', component: () => import('@/pages/public/Automate.vue') },
      { path: 'copy', name: 'copy', component: () => import('@/pages/public/Copy.vue') },
      { path: 'faq', name: 'faq', component: () => import('@/pages/public/Faq.vue') },
      { path: 'forex', name: 'forex', component: () => import('@/pages/public/Forex.vue') },
      { path: 'etfs', name: 'etfs', component: () => import('@/pages/public/Etfs.vue') },
      { path: 'shares', name: 'shares', component: () => import('@/pages/public/Shares.vue') },
      { path: 'indices', name: 'indices', component: () => import('@/pages/public/Indices.vue') },
      { path: 'cryptocurrencies', name: 'cryptocurrencies', component: () => import('@/pages/public/Cryptocurrencies.vue') },
      { path: 'terms', name: 'terms', component: () => import('@/pages/public/Terms.vue') },
      { path: 'trading-conditions', redirect: { name: 'terms' } },
      { path: 'privacy', name: 'privacy', component: () => import('@/pages/public/Privacy.vue') },
      { path: 'contact', name: 'contact', component: () => import('@/pages/public/Contact.vue') },
      { path: 'contacts', redirect: { name: 'contact' } },
    ],
  },
  {
    path: '/',
    component: AuthLayout,
    children: [
      { path: 'login', name: 'login', component: () => import('@/pages/auth/Login.vue') },
      { path: 'two-factor', name: 'two-factor', component: () => import('@/pages/auth/TwoFactorChallenge.vue') },
      { path: 'register', name: 'register', component: () => import('@/pages/auth/Register.vue') },
      { path: 'forgot-password', name: 'forgot-password', component: () => import('@/pages/auth/ForgotPassword.vue') },
      { path: 'reset-password', name: 'reset-password', component: () => import('@/pages/auth/ResetPassword.vue') },
      { path: 'confirm-password', name: 'confirm-password', meta: { requiresUserAuth: true }, component: () => import('@/pages/auth/ConfirmPassword.vue') },
      { path: 'verify-email', name: 'verify-email', meta: { requiresUserAuth: true }, component: () => import('@/pages/auth/VerifyEmail.vue') },
      { path: 'ref/:username', name: 'ref-capture', component: () => import('@/pages/auth/RefCapture.vue') },
      { path: 'impersonate', name: 'impersonate', component: () => import('@/pages/auth/Impersonate.vue') },
      { path: 'admin/login', name: 'admin.login', component: () => import('@/pages/auth/AdminLogin.vue') },
      { path: 'admin/two-factor', name: 'admin.two-factor', component: () => import('@/pages/auth/AdminTwoFactor.vue') },
      { path: 'admin/forgot-password', name: 'admin.forgot-password', component: () => import('@/pages/auth/AdminForgotPassword.vue') },
      { path: 'admin/reset-password', name: 'admin.reset-password', component: () => import('@/pages/auth/AdminResetPassword.vue') },
    ],
  },
  {
    path: '/dashboard',
    component: UserLayout,
    meta: { requiresUserAuth: true },
    children: [
      { path: '', name: 'user.dashboard', component: () => import('@/pages/user/Dashboard.vue') },
      { path: 'history', name: 'user.history', component: () => import('@/pages/user/History.vue') },
      { path: 'plans', name: 'user.plans', component: () => import('@/pages/user/Plans.vue') },
      { path: 'my-plans', name: 'user.my-plans', component: () => import('@/pages/user/MyPlans.vue') },
      { path: 'trade', name: 'user.trade', component: () => import('@/pages/user/Trade.vue') },
      { path: 'copy-trading', name: 'user.copy-trading', component: () => import('@/pages/user/CopyTrading.vue') },
      { path: 'bots', name: 'user.bots', component: () => import('@/pages/user/Bots.vue') },
      { path: 'signals', name: 'user.signals', component: () => import('@/pages/user/Signals.vue') },
      { path: 'signals/premium', name: 'user.signals-premium', component: () => import('@/pages/user/SignalsPremium.vue') },
      { path: 'signals/external', name: 'user.signals-external', component: () => import('@/pages/user/SignalsExternal.vue') },
      { path: 'deposits', name: 'user.deposits', component: () => import('@/pages/user/Deposits.vue') },
      { path: 'deposits/history', name: 'user.deposits-history', component: () => import('@/pages/user/DepositHistory.vue') },
      { path: 'withdrawals', name: 'user.withdrawals', component: () => import('@/pages/user/Withdrawals.vue') },
      { path: 'transfer', name: 'user.transfer', component: () => import('@/pages/user/Transfer.vue') },
      { path: 'exchange', name: 'user.exchange', component: () => import('@/pages/user/Exchange.vue') },
      { path: 'loans', name: 'user.loans', component: () => import('@/pages/user/Loans.vue') },
      { path: 'loans/history', name: 'user.loans-history', component: () => import('@/pages/user/LoansHistory.vue') },
      { path: 'profile', name: 'user.profile', component: () => import('@/pages/user/Profile.vue') },
      { path: 'kyc', name: 'user.kyc', component: () => import('@/pages/user/Kyc.vue') },
      { path: 'wallet-connect', name: 'user.wallet-connect', component: () => import('@/pages/user/WalletConnect.vue') },
      { path: 'referrals', name: 'user.referrals', component: () => import('@/pages/user/Referrals.vue') },
      { path: 'membership', name: 'user.membership', component: () => import('@/pages/user/Membership.vue') },
      { path: 'mt4', name: 'user.mt4', component: () => import('@/pages/user/Mt4.vue') },
      { path: 'support', name: 'user.support', component: () => import('@/pages/user/Support.vue') },
      { path: 'notifications', name: 'user.notifications', component: () => import('@/pages/user/Notifications.vue') },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdminAuth: true },
    children: [
      { path: 'dashboard', name: 'admin.dashboard', component: () => import('@/pages/admin/Dashboard.vue') },
      { path: 'users', name: 'admin.users', component: () => import('@/pages/admin/Users.vue') },
      { path: 'users/:id', name: 'admin.user-detail', component: () => import('@/pages/admin/UserDetail.vue') },
      { path: 'kyc', name: 'admin.kyc', component: () => import('@/pages/admin/Kyc.vue') },
      { path: 'deposits', name: 'admin.deposits', component: () => import('@/pages/admin/Deposits.vue') },
      { path: 'withdrawals', name: 'admin.withdrawals', component: () => import('@/pages/admin/Withdrawals.vue') },
      { path: 'plans', name: 'admin.plans', component: () => import('@/pages/admin/Plans.vue') },
      { path: 'investments', name: 'admin.investments', component: () => import('@/pages/admin/Investments.vue') },
      { path: 'copy-trading', name: 'admin.copy-trading', component: () => import('@/pages/admin/CopyTrading.vue') },
      { path: 'copy-trading/active', name: 'admin.copy-trading-active', component: () => import('@/pages/admin/CopyTradingActive.vue') },
      { path: 'bots', name: 'admin.bots', component: () => import('@/pages/admin/Bots.vue') },
      { path: 'bots/analytics', name: 'admin.bots-analytics', component: () => import('@/pages/admin/BotsAnalytics.vue') },
      { path: 'loans', name: 'admin.loans', component: () => import('@/pages/admin/Loans.vue') },
      { path: 'signals', name: 'admin.signals', component: () => import('@/pages/admin/Signals.vue') },
      { path: 'signals/active', name: 'admin.signals-active', component: () => import('@/pages/admin/SignalsActive.vue') },
      { path: 'signals/subscribers', name: 'admin.signals-subscribers', component: () => import('@/pages/admin/SignalsSubscribers.vue') },
      { path: 'signals/settings', name: 'admin.signals-settings', component: () => import('@/pages/admin/SignalsSettings.vue') },
      { path: 'trading-accounts', name: 'admin.trading-accounts', component: () => import('@/pages/admin/TradingAccounts.vue') },
      { path: 'trading-accounts/fees', name: 'admin.trading-accounts-fees', component: () => import('@/pages/admin/TradingAccountsFees.vue') },
      { path: 'membership', name: 'admin.membership', component: () => import('@/pages/admin/Membership.vue') },
      { path: 'wallet-connect', name: 'admin.wallet-connect', component: () => import('@/pages/admin/WalletConnect.vue') },
      { path: 'wallet-connect/settings', name: 'admin.wallet-connect-settings', component: () => import('@/pages/admin/WalletConnectSettings.vue') },
      { path: 'crm/new-task', name: 'admin.crm-new-task', component: () => import('@/pages/admin/CrmNewTask.vue') },
      { path: 'crm/tasks', name: 'admin.crm-tasks', component: () => import('@/pages/admin/CrmTasks.vue') },
      { path: 'crm/my-tasks', name: 'admin.crm-my-tasks', component: () => import('@/pages/admin/CrmMyTasks.vue') },
      { path: 'crm/leads', name: 'admin.crm-leads', component: () => import('@/pages/admin/CrmLeads.vue') },
      { path: 'crm/import', name: 'admin.crm-import', component: () => import('@/pages/admin/CrmImport.vue') },
      { path: 'email-services', name: 'admin.email-services', component: () => import('@/pages/admin/EmailServices.vue') },
      { path: 'notifications', name: 'admin.notifications', component: () => import('@/pages/admin/Notifications.vue') },
      { path: 'content', name: 'admin.content', component: () => import('@/pages/admin/Content.vue') },
      { path: 'admins', name: 'admin.admins', component: () => import('@/pages/admin/Admins.vue') },
      { path: 'settings/app', name: 'admin.settings-app', component: () => import('@/pages/admin/SettingsApp.vue') },
      { path: 'settings/referral', name: 'admin.settings-referral', component: () => import('@/pages/admin/SettingsReferral.vue') },
      { path: 'settings/subscription', name: 'admin.settings-subscription', component: () => import('@/pages/admin/SettingsSubscription.vue') },
      { path: 'settings/payment', name: 'admin.settings-payment', component: () => import('@/pages/admin/SettingsPayment.vue') },
      { path: 'settings/crypto', name: 'admin.settings-crypto', component: () => import('@/pages/admin/SettingsCrypto.vue') },
      { path: 'ip-blacklist', name: 'admin.ip-blacklist', component: () => import('@/pages/admin/IpBlacklist.vue') },
      { path: 'profile', name: 'admin.profile', component: () => import('@/pages/admin/Profile.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/pages/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresUserAuth) {
    const authUser = useAuthUserStore()
    if (!authUser.isAuthenticated) {
      return { name: 'home' }
    }
  }
  if (to.meta.requiresAdminAuth) {
    const authAdmin = useAuthAdminStore()
    if (!authAdmin.isAuthenticated) {
      return { name: 'home' }
    }
  }
  return true
})

export default router
