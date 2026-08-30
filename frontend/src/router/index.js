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
      { path: 'register', name: 'register', component: () => import('@/pages/auth/Register.vue') },
      { path: 'forgot-password', name: 'forgot-password', component: () => import('@/pages/auth/ForgotPassword.vue') },
      { path: 'reset-password', name: 'reset-password', component: () => import('@/pages/auth/ResetPassword.vue') },
      { path: 'confirm-password', name: 'confirm-password', meta: { requiresUserAuth: true }, component: () => import('@/pages/auth/ConfirmPassword.vue') },
      { path: 'verify-email', name: 'verify-email', meta: { requiresUserAuth: true }, component: () => import('@/pages/auth/VerifyEmail.vue') },
      { path: 'ref/:username', name: 'ref-capture', component: () => import('@/pages/auth/RefCapture.vue') },
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
      { path: 'deposits', name: 'admin.deposits', component: () => import('@/pages/admin/Deposits.vue') },
      { path: 'withdrawals', name: 'admin.withdrawals', component: () => import('@/pages/admin/Withdrawals.vue') },
      // Remaining admin pages added in Phase 5.
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
