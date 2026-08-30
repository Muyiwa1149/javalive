<script setup>
import { ref, onMounted } from 'vue'
import {
  Wallet, TrendingUp, Gift, ArrowDownCircle, ArrowUpCircle, Server, ShieldCheck, ShieldAlert, Clock,
  PlusCircle, MinusCircle, Target, Users2,
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
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Welcome back, {{ authUser.user?.name?.split(' ')[0] }}</h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">Here's what's happening with your account today.</p>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading dashboard…</div>
    <div v-else-if="error" class="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl text-red-700 dark:text-red-300">{{ error }}</div>

    <template v-else-if="summary">
      <!-- KYC status pill -->
      <div v-if="summary.kycRequired" class="inline-flex items-center gap-2 px-4 py-2 rounded-full border text-sm font-medium" :class="kycStatusMeta(summary.accountVerifyStatus).class">
        <component :is="kycStatusMeta(summary.accountVerifyStatus).icon" class="w-4 h-4" />
        <span>{{ kycStatusMeta(summary.accountVerifyStatus).label }}</span>
        <RouterLink v-if="summary.accountVerifyStatus !== 'Verified' && summary.accountVerifyStatus !== 'Under review'" :to="{ name: 'user.kyc' }" class="underline ml-1">Verify now</RouterLink>
      </div>

      <!-- Stat cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Account Balance</span>
            <Wallet class="w-5 h-5 text-blue-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.accountBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">ROI Balance</span>
            <TrendingUp class="w-5 h-5 text-emerald-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.roiBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Bonus Balance</span>
            <Gift class="w-5 h-5 text-purple-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.bonusBalance, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">MT4 Accounts</span>
            <Server class="w-5 h-5 text-indigo-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ summary.tradingAccountsCount }}</div>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Total Deposited</span>
            <ArrowDownCircle class="w-5 h-5 text-green-500" />
          </div>
          <div class="text-xl font-bold text-gray-900 dark:text-white mt-2">{{ money(summary.totalDeposited, summary.currencySymbol) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Total Withdrawal</span>
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
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ p.status }}</div>
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
