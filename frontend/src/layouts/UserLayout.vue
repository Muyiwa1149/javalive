<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { onClickOutside } from '@vueuse/core'
import {
  LayoutDashboard, Receipt, Briefcase, Target, PieChart, TrendingUp, CandlestickChart, Users2, Bot,
  Radio, Signal, Zap, Wallet, PlusCircle, MinusCircle, ArrowLeftRight, Repeat, CreditCard, FilePlus,
  FileText, UserCircle, User, ShieldAlert, ShieldCheck, Clock, KeyRound, GraduationCap, Server,
  TrendingUp as TrendingUpIcon, Headphones, HelpCircle, Bell, ChevronDown, Menu, X, LogOut, Sun, Moon,
  BellOff, AlertTriangle, CheckCircle, AlertOctagon, Info, Home, Banknote,
} from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { useNotificationsStore } from '@/stores/notifications'
import { storageUrl } from '@/lib/storage'
import { useCryptoPrices } from '@/lib/cryptoPrices'

const route = useRoute()
const router = useRouter()
const authUser = useAuthUserStore()
const settingsStore = usePublicSettingsStore()
const notifications = useNotificationsStore()

const sidebarOpen = ref(false)
// Close the mobile sidebar automatically whenever the route changes, regardless of which link
// (or the logo, or a nested link like "Verify Now") was clicked.
watch(() => route.fullPath, () => { sidebarOpen.value = false })

const notifOpen = ref(false)
const notifRef = ref(null)
onClickOutside(notifRef, () => { notifOpen.value = false })
const userMenuOpen = ref(false)
const userMenuRef = ref(null)
onClickOutside(userMenuRef, () => { userMenuOpen.value = false })
const kycOpen = ref(false)

const isDark = ref(localStorage.getItem('theme') !== 'light')
function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

const { prices, start: startPrices, stop: stopPrices } = useCryptoPrices(['bitcoin', 'ethereum'])

const NAV_SECTIONS = [
  {
    label: 'Overview', icon: LayoutDashboard,
    items: [
      { name: 'user.dashboard', label: 'Dashboard', icon: LayoutDashboard },
      { name: 'user.history', label: 'Account Statement', icon: Receipt },
    ],
  },
  {
    label: 'Portfolio & Investments', icon: Briefcase,
    items: [
      { name: 'user.plans', label: 'Investment Plans', icon: Target },
      { name: 'user.my-plans', label: 'My Portfolio', icon: PieChart },
    ],
  },
  {
    label: 'Trading & Markets', icon: TrendingUp,
    items: [
      { name: 'user.trade', label: 'Live Markets', icon: CandlestickChart },
      { name: 'user.copy-trading', label: 'Copy Trading', icon: Users2 },
      { name: 'user.bots', label: 'AI Trading Bots', icon: Bot },
    ],
  },
  {
    label: 'Market Intelligence', icon: Radio,
    items: [
      { name: 'user.signals', label: 'My Signals', icon: Signal },
      { name: 'user.signals-premium', label: 'Premium Signals', icon: Zap },
      { name: 'user.signals-external', label: 'Signal Providers', icon: Radio },
    ],
  },
  {
    label: 'Wallet & Funds', icon: Wallet,
    items: [
      { name: 'user.deposits', label: 'Deposit Funds', icon: PlusCircle },
      { name: 'user.withdrawals', label: 'Withdraw Funds', icon: MinusCircle },
      { name: 'user.transfer', label: 'Internal Transfer', icon: ArrowLeftRight },
      { name: 'user.exchange', label: 'Currency Exchange', icon: Repeat },
    ],
  },
  {
    label: 'Credit & Financing', icon: CreditCard,
    items: [
      { name: 'user.loans', label: 'Apply for Credit', icon: FilePlus },
      { name: 'user.loans-history', label: 'Credit History', icon: FileText },
    ],
  },
  {
    label: 'Learning & Tools', icon: GraduationCap,
    items: [
      { name: 'user.membership', label: 'Membership & Courses', icon: GraduationCap },
      { name: 'user.mt4', label: 'MT4 Subscription', icon: Server },
    ],
  },
  {
    label: 'Growth & Rewards', icon: TrendingUpIcon,
    items: [
      { name: 'user.referrals', label: 'Referral Program', icon: Users2 },
    ],
  },
  {
    label: 'Support & Help', icon: Headphones,
    items: [
      { name: 'user.support', label: 'Support Center', icon: Headphones },
    ],
  },
]

const typeIcon = (type) => ({
  warning: AlertTriangle, success: CheckCircle, danger: AlertOctagon,
}[type] || Info)
const typeClass = (type) => ({
  warning: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-500',
  success: 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-500',
  danger: 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-500',
}[type] || 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-500')

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const seconds = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000)
  if (seconds < 60) return 'just now'
  const mins = Math.floor(seconds / 60)
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

const recentNotifications = computed(() => notifications.items.slice(0, 5))

function openNotifications() {
  notifOpen.value = !notifOpen.value
  if (notifOpen.value && !notifications.loaded) {
    notifications.fetchAll()
  }
}

async function logout() {
  authUser.logout()
  router.push({ name: 'login' })
}

// Source's dashboard layout embeds a GTranslate widget (a real third-party page-translation
// service, not the app's own dead LanguageController/session-based routes, which nothing in the
// UI actually calls) — mirrored here so the same real language-switching affordance exists.
const gtranslateContainer = ref(null)
function loadLanguageWidget() {
  if (!gtranslateContainer.value || window.gtranslateSettings) return
  window.gtranslateSettings = {
    default_language: 'en',
    alt_flags: { en: 'usa' },
    wrapper_selector: '.gtranslate_wrapper',
    flag_style: '3d',
  }
  const script = document.createElement('script')
  script.src = 'https://cdn.gtranslate.net/widgets/latest/float.js'
  script.defer = true
  document.body.appendChild(script)
}

onMounted(() => {
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
  settingsStore.ensureLoaded()
  authUser.fetchProfile().catch(() => {})
  notifications.fetchCount().catch(() => {})
  startPrices()
  loadLanguageWidget()
})
onBeforeUnmount(() => stopPrices())

const btcPrice = computed(() => prices.value?.bitcoin?.usd)
const ethPrice = computed(() => prices.value?.ethereum?.usd)
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 font-sans" :data-theme="isDark ? 'dark' : 'light'">
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="fixed inset-y-0 left-0 z-40 w-72 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 transform transition-transform duration-200 flex flex-col lg:translate-x-0"
        :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'">
        <div class="flex-shrink-0 p-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
          <RouterLink :to="{ name: 'user.dashboard' }" class="flex items-center gap-2">
            <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-8 w-auto" :alt="settingsStore.settings?.siteName">
          </RouterLink>
          <button class="lg:hidden text-gray-500" @click="sidebarOpen = false"><X class="w-5 h-5" /></button>
        </div>

        <nav class="flex-1 overflow-y-auto no-scrollbar p-4 space-y-6 text-sm pb-20">
          <div v-for="section in NAV_SECTIONS" :key="section.label" class="space-y-2">
            <div class="flex items-center gap-2 px-2 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">
              <component :is="section.icon" class="w-4 h-4" />
              <span>{{ section.label }}</span>
            </div>
            <ul class="space-y-1">
              <li v-for="item in section.items" :key="item.name">
                <RouterLink :to="{ name: item.name }"
                  class="flex items-center px-3 py-2 rounded-lg text-gray-700 dark:text-gray-200 hover:bg-blue-50 dark:hover:bg-blue-900/40 transition-colors"
                  :class="route.name === item.name ? 'bg-blue-50 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-medium' : ''">
                  <component :is="item.icon" class="w-5 h-5 mr-3" />
                  {{ item.label }}
                </RouterLink>
              </li>
            </ul>
          </div>

          <!-- Account Management -->
          <div class="space-y-2">
            <div class="flex items-center gap-2 px-2 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">
              <UserCircle class="w-4 h-4" />
              <span>Account Management</span>
            </div>
            <ul class="space-y-1">
              <li>
                <RouterLink :to="{ name: 'user.profile' }"
                  class="flex items-center px-3 py-2 rounded-lg text-gray-700 dark:text-gray-200 hover:bg-blue-50 dark:hover:bg-blue-900/40 transition-colors"
                  :class="route.name === 'user.profile' ? 'bg-blue-50 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-medium' : ''">
                  <User class="w-5 h-5 mr-3" /> Profile Settings
                </RouterLink>
              </li>
              <li v-if="authUser.user">
                <div v-if="authUser.user.accountVerifyStatus === 'Verified'" class="flex items-center px-3 py-2 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-100 dark:border-green-800">
                  <ShieldCheck class="w-5 h-5 mr-3 text-green-600 dark:text-green-400" />
                  <span class="font-medium text-green-700 dark:text-green-300">Account Verified</span>
                </div>
                <div v-else>
                  <button class="flex items-center w-full px-3 py-2 rounded-lg text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800" @click="kycOpen = !kycOpen">
                    <ShieldAlert class="w-5 h-5 mr-3" />
                    <span class="flex-1 text-left">Identity Verification</span>
                    <ChevronDown class="w-4 h-4 transition-transform" :class="kycOpen ? 'rotate-180' : ''" />
                  </button>
                  <div v-if="kycOpen" class="mt-2 ml-8 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-100 dark:border-gray-700">
                    <p v-if="authUser.user.accountVerifyStatus === 'Under review'" class="text-sm text-gray-600 dark:text-gray-400 flex items-center gap-1">
                      <Clock class="w-3 h-3" /> Your verification is under review
                    </p>
                    <template v-else>
                      <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">Complete your identity verification to unlock full trading features</p>
                      <RouterLink :to="{ name: 'user.kyc' }" class="inline-flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg">
                        <ShieldCheck class="w-4 h-4" /> Verify Now
                      </RouterLink>
                    </template>
                  </div>
                </div>
              </li>
              <li>
                <RouterLink :to="{ name: 'user.wallet-connect' }"
                  class="flex items-center px-3 py-2 rounded-lg text-gray-700 dark:text-gray-200 hover:bg-blue-50 dark:hover:bg-blue-900/40 transition-colors"
                  :class="route.name === 'user.wallet-connect' ? 'bg-blue-50 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-medium' : ''">
                  <KeyRound class="w-5 h-5 mr-3" /> Connect Wallet
                </RouterLink>
              </li>
            </ul>
          </div>
        </nav>
      </aside>

      <div v-if="sidebarOpen" class="fixed inset-0 bg-black/50 z-30 lg:hidden" @click="sidebarOpen = false"></div>

      <!-- Main -->
      <div class="flex-1 min-w-0 lg:pl-72 min-h-screen flex flex-col">
        <header class="sticky top-0 z-20 bg-white/95 dark:bg-gray-900/95 backdrop-blur-xl border-b border-gray-200 dark:border-gray-800">
          <div class="px-4 sm:px-6 h-20 flex items-center justify-between gap-4">
            <div class="flex items-center gap-3">
              <button class="lg:hidden text-gray-500" @click="sidebarOpen = true"><Menu class="w-6 h-6" /></button>
              <RouterLink :to="{ name: 'user.dashboard' }" class="lg:hidden flex items-center">
                <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-6 w-auto" :alt="settingsStore.settings?.siteName">
              </RouterLink>
              <div class="hidden lg:flex items-center gap-4 text-sm">
                <div class="flex items-center gap-2">
                  <div class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                  <span class="text-xs font-medium text-gray-500 dark:text-gray-400">LIVE</span>
                </div>
                <div><span class="text-gray-500 dark:text-gray-400">BTC:</span> <span class="font-mono ml-1">${{ btcPrice ? btcPrice.toLocaleString() : '…' }}</span></div>
                <div><span class="text-gray-500 dark:text-gray-400">ETH:</span> <span class="font-mono ml-1">${{ ethPrice ? ethPrice.toLocaleString() : '…' }}</span></div>
                <div ref="gtranslateContainer" class="gtranslate_wrapper"></div>
              </div>
            </div>

            <div class="hidden md:block bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 px-4 py-2 rounded-lg border border-blue-100 dark:border-blue-800">
              <div class="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide">Account Balance</div>
              <div class="text-lg font-bold text-gray-900 dark:text-white">
                {{ authUser.user?.currencySymbol }}{{ Number(authUser.user?.accountBalance ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
              </div>
            </div>

            <div class="flex items-center gap-2">
              <button class="p-2 text-gray-500 hover:text-blue-600 dark:text-gray-300 dark:hover:text-blue-400 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg" @click="toggleTheme">
                <Sun v-if="!isDark" class="w-5 h-5" />
                <Moon v-else class="w-5 h-5" />
              </button>

              <div class="relative" ref="notifRef">
                <button class="relative p-2 text-gray-500 hover:text-blue-600 dark:text-gray-300 dark:hover:text-blue-400 hover:bg-gray-50 dark:hover:bg-gray-800 rounded-lg" @click="openNotifications">
                  <Bell class="w-5 h-5" />
                  <span v-if="notifications.unreadCount > 0" class="absolute -top-1 -right-1 flex items-center justify-center min-w-[18px] h-[18px] text-xs font-medium text-white bg-red-500 rounded-full px-1 border-2 border-white dark:border-gray-900">
                    {{ notifications.unreadCount > 99 ? '99+' : notifications.unreadCount }}
                  </span>
                </button>
                <div v-if="notifOpen" class="animate-fadeIn absolute right-0 mt-2 w-80 max-w-[90vw] bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-20">
                  <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
                    <h3 class="text-sm font-semibold text-gray-900 dark:text-white">Notifications</h3>
                    <button v-if="notifications.unreadCount > 0" class="text-xs text-blue-600 dark:text-blue-400 hover:underline" @click="notifications.markAllRead()">Mark all read</button>
                  </div>
                  <div class="max-h-[60vh] overflow-y-auto">
                    <button v-for="n in recentNotifications" :key="n.id" class="w-full text-left block border-b border-gray-100 dark:border-gray-700 last:border-0 hover:bg-gray-50 dark:hover:bg-gray-700/50" @click="notifications.markRead(n.id)">
                      <div class="px-4 py-3" :class="!n.isRead ? 'bg-blue-50 dark:bg-blue-900/10' : ''">
                        <div class="flex items-start">
                          <span class="flex h-8 w-8 rounded-full items-center justify-center flex-shrink-0" :class="typeClass(n.type)">
                            <component :is="typeIcon(n.type)" class="w-4 h-4" />
                          </span>
                          <div class="ml-3 flex-1 min-w-0">
                            <p class="text-sm font-medium text-gray-900 dark:text-white truncate" :class="!n.isRead ? 'font-semibold' : ''">{{ n.title }}</p>
                            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400 line-clamp-2">{{ n.message }}</p>
                            <p class="mt-1 text-xs text-gray-400 dark:text-gray-500">{{ timeAgo(n.createdAt) }}</p>
                          </div>
                        </div>
                      </div>
                    </button>
                    <div v-if="notifications.loaded && recentNotifications.length === 0" class="py-8 text-center">
                      <BellOff class="h-6 w-6 mx-auto text-gray-400" />
                      <p class="text-sm text-gray-600 dark:text-gray-400 mt-2">No notifications</p>
                    </div>
                  </div>
                  <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-700 text-center">
                    <RouterLink :to="{ name: 'user.notifications' }" class="text-sm text-blue-600 dark:text-blue-400 hover:underline" @click="notifOpen = false">View all notifications</RouterLink>
                  </div>
                </div>
              </div>

              <div class="relative" ref="userMenuRef">
                <button class="flex items-center gap-2 px-2 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800" @click="userMenuOpen = !userMenuOpen">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center text-white text-sm font-medium">
                    {{ (authUser.user?.name || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="hidden sm:block text-left">
                    <div class="text-sm font-medium text-gray-900 dark:text-white truncate max-w-[120px]">{{ authUser.user?.name }}</div>
                    <div class="text-xs text-gray-500 dark:text-gray-400">Investment Account</div>
                  </div>
                  <ChevronDown class="w-4 h-4 text-gray-400" :class="userMenuOpen ? 'rotate-180' : ''" />
                </button>
                <div v-if="userMenuOpen" class="animate-fadeIn absolute right-0 mt-2 w-64 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-20">
                  <div class="p-2">
                    <RouterLink :to="{ name: 'user.profile' }" class="flex items-center px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-md" @click="userMenuOpen = false">
                      <User class="w-4 h-4 mr-3" /> Profile Settings
                    </RouterLink>
                    <RouterLink :to="{ name: 'user.history' }" class="flex items-center px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-md" @click="userMenuOpen = false">
                      <Receipt class="w-4 h-4 mr-3" /> Account History
                    </RouterLink>
                    <RouterLink :to="{ name: 'user.support' }" class="flex items-center px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-md" @click="userMenuOpen = false">
                      <HelpCircle class="w-4 h-4 mr-3" /> Support Center
                    </RouterLink>
                    <div class="border-t border-gray-200 dark:border-gray-600 my-2"></div>
                    <button class="w-full flex items-center px-3 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md" @click="logout">
                      <LogOut class="w-4 h-4 mr-3" /> Sign Out
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </header>

        <main class="flex-1 min-w-0 pb-20 lg:pb-6">
          <RouterView />
        </main>

        <!-- Mobile bottom nav -->
        <nav class="fixed bottom-0 left-0 right-0 lg:hidden bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 flex items-center justify-around py-2 z-30">
          <RouterLink :to="{ name: 'user.dashboard' }" class="flex flex-col items-center text-xs" :class="route.name === 'user.dashboard' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-500'">
            <Home class="w-6 h-6" /> Home
          </RouterLink>
          <RouterLink :to="{ name: 'user.deposits' }" class="flex flex-col items-center text-xs" :class="route.name === 'user.deposits' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-500'">
            <Banknote class="w-6 h-6" /> Deposit
          </RouterLink>
          <RouterLink :to="{ name: 'user.support' }" class="flex flex-col items-center text-xs" :class="route.name === 'user.support' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-500'">
            <Headphones class="w-6 h-6" /> Support
          </RouterLink>
          <RouterLink :to="{ name: 'user.profile' }" class="flex flex-col items-center text-xs" :class="route.name === 'user.profile' ? 'text-blue-600 dark:text-blue-400' : 'text-gray-500'">
            <User class="w-6 h-6" /> Profile
          </RouterLink>
        </nav>
      </div>
    </div>
  </div>
</template>
