<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  Wallet, TrendingUp, Gift, ArrowDownCircle, ArrowUpCircle, Server, ShieldCheck, ShieldAlert, Clock,
  PlusCircle, MinusCircle, Target, Users2, Link as LinkIcon, CheckCircle, SignalLow, SignalMedium, SignalHigh,
} from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const summary = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await api.get('/dashboard/summary')
    summary.value = data
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load dashboard.'
  } finally {
    loading.value = false
  }
})

function money(value, symbol = '$') {
  return `${symbol}${Number(value ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const kycStatusMeta = (status) => {
  if (status === 'Verified') return { label: 'Verified', icon: ShieldCheck, class: 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-300 border-green-200 dark:border-green-800' }
  if (status === 'Under review') return { label: 'Under Review', icon: Clock, class: 'bg-yellow-50 dark:bg-yellow-900/20 text-yellow-700 dark:text-yellow-300 border-yellow-200 dark:border-yellow-800' }
  return { label: 'Not Verified', icon: ShieldAlert, class: 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-300 border-red-200 dark:border-red-800' }
}

// Trading Signal Strength — mirrors source's dashboard.blade.php: shown once progress > 2,
// same three-tier thresholds/colors/copy (weak <25, moderate <50, strong >=50).
const signal = computed(() => {
  const strength = summary.value?.signalStrength ?? 0
  if (strength < 25) {
    return { strength, gradient: 'from-red-500 to-red-600', label: 'Weak Signal', icon: SignalLow,
      badgeClass: 'bg-red-100 text-red-700 dark:bg-red-900/20 dark:text-red-400',
      note: '⚠️ Signal strength is low. Consider waiting for better market conditions.' }
  }
  if (strength < 50) {
    return { strength, gradient: 'from-yellow-500 to-orange-500', label: 'Moderate Signal', icon: SignalMedium,
      badgeClass: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400',
      note: '⚡ Moderate signal detected. Proceed with caution and proper risk management.' }
  }
  return { strength, gradient: 'from-green-500 to-emerald-600', label: 'Strong Signal', icon: SignalHigh,
    badgeClass: 'bg-green-100 text-green-700 dark:bg-green-900/20 dark:text-green-400',
    note: '🚀 Strong signal strength! Optimal conditions for trading opportunities.' }
})
const showSignalStrength = computed(() => (summary.value?.signalStrength ?? 0) > 2)
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Welcome back, {{ authUser.user?.name?.split(' ')[0] }}</h1>
        <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400 mt-1">Here's what's happening with your account today.</p>
      </div>
      <div v-if="summary" class="hidden sm:flex gap-3">
        <RouterLink v-if="summary.walletFeatureEnabled && !summary.walletConnected" :to="{ name: 'user.wallet-connect' }"
          class="inline-flex items-center justify-center gap-2 px-4 py-2 sm:py-3 bg-gradient-to-r from-indigo-600 to-blue-500 text-white rounded-lg shadow hover:from-indigo-700 transition animate-pulse text-sm sm:text-base">
          <LinkIcon class="w-4 h-4 sm:w-5 sm:h-5" /> Connect Wallet
        </RouterLink>
        <div v-else-if="summary.walletFeatureEnabled" class="inline-flex items-center justify-center gap-2 px-4 py-2 sm:py-3 bg-green-100 dark:bg-green-900/20 text-green-700 dark:text-green-300 rounded-lg text-sm sm:text-base">
          <CheckCircle class="w-4 h-4 sm:w-5 sm:h-5" /> Wallet Connected
        </div>
        <RouterLink :to="{ name: 'user.plans' }" class="inline-flex items-center justify-center gap-2 px-4 py-2 sm:py-3 bg-green-600 text-white rounded-lg shadow hover:bg-green-700 transition text-sm sm:text-base">
          <TrendingUp class="w-4 h-4 sm:w-5 sm:h-5" /> Invest Now
        </RouterLink>
      </div>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading dashboard…</div>
    <div v-else-if="error" class="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-300">{{ error }}</div>

    <template v-else-if="summary">
      <!-- Trading Signal Strength -->
      <div v-if="showSignalStrength" class="bg-white dark:bg-gray-900 rounded-xl p-4 sm:p-6 shadow-sm ring-1 ring-gray-200 dark:ring-gray-800">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 sm:gap-0 mb-3">
          <div class="flex items-center gap-2">
            <component :is="signal.icon" class="w-5 h-5 text-gray-600 dark:text-gray-300 flex-shrink-0" />
            <h2 class="text-sm sm:text-base font-semibold text-gray-800 dark:text-gray-100">Trading Signal Strength</h2>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">{{ signal.strength }}%</span>
            <span class="px-2 py-1 text-xs font-medium rounded-full whitespace-nowrap" :class="signal.badgeClass">{{ signal.label }}</span>
          </div>
        </div>
        <div class="w-full h-3 sm:h-4 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden relative">
          <div class="bg-gradient-to-r h-full rounded-full transition-all duration-700 ease-out relative" :class="signal.gradient" :style="{ width: signal.strength + '%' }">
            <div class="absolute inset-0 bg-white/20 animate-pulse rounded-full"></div>
          </div>
        </div>
        <div class="flex justify-between items-center mt-2 text-xs text-gray-500 dark:text-gray-400">
          <span>0% Weak</span>
          <span>25% Moderate</span>
          <span>50%+ Strong</span>
        </div>
        <p class="text-xs text-gray-600 dark:text-gray-400 mt-3 text-center">{{ signal.note }}</p>
      </div>

      <!-- Wallet connection prompt -->
      <div v-if="summary.walletFeatureEnabled && !summary.walletConnected" class="bg-gradient-to-r from-indigo-50 to-blue-50 dark:from-indigo-900/20 dark:to-blue-900/20 rounded-2xl p-4 sm:p-6 border border-indigo-200 dark:border-indigo-700">
        <div class="flex flex-col sm:flex-row items-start gap-4">
          <div class="p-3 bg-indigo-100 dark:bg-indigo-900/30 rounded-xl mx-auto sm:mx-0">
            <Wallet class="w-6 h-6 sm:w-8 sm:h-8 text-indigo-600 dark:text-indigo-400" />
          </div>
          <div class="flex-1 text-center sm:text-left">
            <h3 class="text-base sm:text-lg font-semibold text-indigo-900 dark:text-indigo-100 mb-2">Connect Your Wallet to Start Earning</h3>
            <p class="text-indigo-700 dark:text-indigo-300 text-sm mb-4">
              Connect your cryptocurrency wallet to unlock daily earning opportunities of up to
              <span class="font-semibold">{{ money(summary.minReturn, summary.currencySymbol) }}</span> per day.
            </p>
            <RouterLink :to="{ name: 'user.wallet-connect' }"
              class="inline-flex items-center gap-2 px-4 py-2 sm:py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-all duration-200 transform hover:scale-[1.02] text-sm sm:text-base">
              <PlusCircle class="w-4 h-4" /> Connect Wallet Now
            </RouterLink>
          </div>
        </div>
      </div>
      <!-- KYC status pill -->
      <div v-if="summary.kycRequired" class="inline-flex items-center gap-2 px-4 py-2 rounded-full border text-sm font-medium" :class="kycStatusMeta(summary.accountVerifyStatus).class">
        <component :is="kycStatusMeta(summary.accountVerifyStatus).icon" class="w-4 h-4" />
        <span>{{ kycStatusMeta(summary.accountVerifyStatus).label }}</span>
        <RouterLink v-if="summary.accountVerifyStatus !== 'Verified' && summary.accountVerifyStatus !== 'Under review'" :to="{ name: 'user.kyc' }" class="underline ml-1">Verify now</RouterLink>
      </div>

      <!-- Stat cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">Account Balance</span>
            <Wallet class="w-5 h-5 text-blue-500" />
          </div>
          <div class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-2 truncate">{{ money(summary.accountBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">ROI Balance</span>
            <TrendingUp class="w-5 h-5 text-emerald-500" />
          </div>
          <div class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-2 truncate">{{ money(summary.roiBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">Bonus Balance</span>
            <Gift class="w-5 h-5 text-purple-500" />
          </div>
          <div class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-2 truncate">{{ money(summary.bonusBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">MT4 Accounts</span>
            <Server class="w-5 h-5 text-indigo-500" />
          </div>
          <div class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mt-2 truncate">{{ summary.tradingAccountsCount }}</div>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3 sm:gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">Total Deposited</span>
            <ArrowDownCircle class="w-5 h-5 text-green-500" />
          </div>
          <div class="text-xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.totalDeposited, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4 sm:p-5">
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase truncate">Total Withdrawal</span>
            <ArrowUpCircle class="w-5 h-5 text-red-500" />
          </div>
          <div class="text-xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.totalWithdrawn, summary.currencySymbol) }}</div>
        </div>
      </div>

      <!-- Quick actions -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <RouterLink :to="{ name: 'user.deposits' }" class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl hover:border-blue-400 transition-colors">
          <PlusCircle class="w-6 h-6 text-green-500" /> <span class="text-sm font-medium text-gray-700 dark:text-gray-200">Deposit</span>
        </RouterLink>
        <RouterLink :to="{ name: 'user.withdrawals' }" class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl hover:border-blue-400 transition-colors">
          <MinusCircle class="w-6 h-6 text-red-500" /> <span class="text-sm font-medium text-gray-700 dark:text-gray-200">Withdraw</span>
        </RouterLink>
        <RouterLink :to="{ name: 'user.plans' }" class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl hover:border-blue-400 transition-colors">
          <Target class="w-6 h-6 text-blue-500" /> <span class="text-sm font-medium text-gray-700 dark:text-gray-200">Invest</span>
        </RouterLink>
        <RouterLink :to="{ name: 'user.referrals' }" class="flex flex-col items-center gap-2 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl hover:border-blue-400 transition-colors">
          <Users2 class="w-6 h-6 text-purple-500" /> <span class="text-sm font-medium text-gray-700 dark:text-gray-200">Refer</span>
        </RouterLink>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Recent plans -->
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-gray-900 dark:text-white">My Portfolio</h2>
            <RouterLink :to="{ name: 'user.my-plans' }" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">View all</RouterLink>
          </div>
          <div v-if="summary.recentPlans.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No active plans yet.</div>
          <ul v-else class="divide-y divide-gray-100 dark:divide-gray-800">
            <li v-for="p in summary.recentPlans" :key="p.id" class="py-3 flex items-center justify-between">
              <div>
                <div class="text-sm font-medium text-gray-900 dark:text-white">{{ p.planName }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ p.status === 'yes' ? 'Active' : p.status === 'expired' ? 'Expired' : p.status === 'cancelled' ? 'Cancelled' : p.status }}</div>
              </div>
              <div class="text-right">
                <div class="text-sm font-semibold text-gray-900 dark:text-white">{{ money(p.amount, summary.currencySymbol) }}</div>
                <div class="text-xs text-emerald-600 dark:text-emerald-400">+{{ money(p.profitEarned, summary.currencySymbol) }}</div>
              </div>
            </li>
          </ul>
        </div>

        <!-- Recent activity -->
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-gray-900 dark:text-white">Recent Activity</h2>
            <RouterLink :to="{ name: 'user.history' }" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">View all</RouterLink>
          </div>
          <div v-if="summary.recentActivity.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No recent activity.</div>
          <ul v-else class="divide-y divide-gray-100 dark:divide-gray-800">
            <li v-for="a in summary.recentActivity" :key="a.id" class="py-3 flex items-center justify-between">
              <div>
                <div class="text-sm font-medium text-gray-900 dark:text-white">{{ a.planLabel || a.type }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ new Date(a.createdAt).toLocaleString() }}</div>
              </div>
              <div class="text-sm font-semibold" :class="['WIN', 'Buy'].includes(a.type) ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">
                {{ money(a.amount, summary.currencySymbol) }}
              </div>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </div>
</template>
