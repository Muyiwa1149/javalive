<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend,
} from 'chart.js'
import {
  Wallet, Hourglass, CreditCard, PauseCircle, Users, UserCheck, UserX, LineChart, ArrowUp, Clock, Ban,
} from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { useAuthAdminStore } from '@/stores/authAdmin'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import api from '@/lib/api'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const authAdmin = useAuthAdminStore()
const settingsStore = usePublicSettingsStore()
const loading = ref(true)
const summary = ref(null)

onMounted(async () => {
  settingsStore.ensureLoaded()
  try {
    const { data } = await api.get('/admin/dashboard/summary')
    summary.value = data
  } finally {
    loading.value = false
  }
})

function money(value) {
  const symbol = settingsStore.settings?.defaultCurrencySymbol || '$'
  return `${symbol}${Number(value ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const statCards = computed(() => summary.value ? [
  { label: 'Total Deposits', value: money(summary.value.totalDeposited), icon: Wallet, tone: 'emerald', hint: 'All time', hintIcon: ArrowUp, link: 'admin.deposits' },
  { label: 'Pending Deposits', value: money(summary.value.pendingDeposited), icon: Hourglass, tone: 'amber', hint: 'Awaiting approval', hintIcon: Clock, link: 'admin.deposits' },
  { label: 'Total Withdrawals', value: money(summary.value.totalWithdrawn), icon: CreditCard, tone: 'rose', hint: 'All time', hintIcon: ArrowUp, link: 'admin.withdrawals' },
  { label: 'Pending Withdrawals', value: money(summary.value.pendingWithdrawn), icon: PauseCircle, tone: 'sky', hint: 'Processing', hintIcon: Clock, link: 'admin.withdrawals' },
  { label: 'Total Users', value: Number(summary.value.userCount).toLocaleString(), icon: Users, tone: 'indigo', hint: 'Registered', hintIcon: UserCheck, link: 'admin.users' },
  { label: 'Active Users', value: Number(summary.value.activeUsers).toLocaleString(), icon: UserCheck, tone: 'emerald', hint: 'Online', hintIcon: UserCheck, link: 'admin.users' },
  { label: 'Blocked Users', value: Number(summary.value.blockedUsers).toLocaleString(), icon: UserX, tone: 'rose', hint: 'Suspended', hintIcon: Ban, link: 'admin.users' },
  { label: 'Investment Plans', value: Number(summary.value.planCount).toLocaleString(), icon: LineChart, tone: 'amber', hint: 'Available', hintIcon: LineChart, link: 'admin.plans' },
] : [])

const toneClasses = {
  emerald: 'bg-emerald-500/10 text-emerald-500',
  amber: 'bg-amber-500/10 text-amber-500',
  rose: 'bg-rose-500/10 text-rose-500',
  sky: 'bg-sky-500/10 text-sky-500',
  indigo: 'bg-indigo-500/10 text-indigo-500',
}

const chartData = computed(() => ({
  labels: ['Deposits', 'Pending Deposits', 'Withdrawals', 'Pending Withdrawals', 'Total Transactions'],
  datasets: [{
    label: `Amount in ${settingsStore.settings?.defaultCurrencySymbol || '$'}`,
    data: summary.value ? [
      summary.value.chartDeposits, summary.value.chartPendingDeposits, summary.value.chartWithdrawals,
      summary.value.chartPendingWithdrawals, summary.value.chartTransactions,
    ] : [],
    backgroundColor: ['#10b981', '#f59e0b', '#f43f5e', '#0ea5e9', '#6366f1'],
    borderRadius: 8,
    borderSkipped: false,
  }],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { beginAtZero: true, grid: { color: 'rgba(148,163,184,0.15)' }, ticks: { color: '#94a3b8' } },
    x: { grid: { display: false }, ticks: { color: '#94a3b8' } },
  },
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <!-- Header -->
    <div class="rounded-2xl bg-gradient-to-br from-indigo-600 via-indigo-500 to-blue-600 p-6 sm:p-8 text-white shadow-xl shadow-indigo-500/20">
      <h1 class="text-2xl sm:text-3xl font-bold">Dashboard</h1>
      <p class="mt-1.5 text-indigo-100">Welcome back, {{ authAdmin.admin?.firstName }} {{ authAdmin.admin?.lastName }}</p>
      <div class="mt-5 flex flex-wrap gap-2">
        <RouterLink :to="{ name: 'admin.deposits' }" class="px-4 py-2 rounded-lg bg-white/15 hover:bg-white/25 backdrop-blur text-sm font-medium transition-colors">Deposits</RouterLink>
        <RouterLink :to="{ name: 'admin.withdrawals' }" class="px-4 py-2 rounded-lg bg-white/15 hover:bg-white/25 backdrop-blur text-sm font-medium transition-colors">Withdrawals</RouterLink>
        <RouterLink :to="{ name: 'admin.users' }" class="px-4 py-2 rounded-lg bg-white/15 hover:bg-white/25 backdrop-blur text-sm font-medium transition-colors">Users</RouterLink>
      </div>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <template v-else>
      <!-- Stat cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <RouterLink v-for="card in statCards" :key="card.label" :to="{ name: card.link }"
          class="group bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 hover:shadow-lg hover:-translate-y-0.5 transition-all">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center shrink-0" :class="toneClasses[card.tone]">
              <component :is="card.icon" class="w-6 h-6" />
            </div>
            <div class="min-w-0">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 truncate">{{ card.label }}</p>
              <p class="text-xl font-bold text-slate-900 dark:text-white truncate">{{ card.value }}</p>
            </div>
          </div>
          <div class="mt-3 flex items-center gap-1 text-xs" :class="toneClasses[card.tone].split(' ')[1]">
            <component :is="card.hintIcon" class="w-3.5 h-3.5" /> {{ card.hint }}
          </div>
        </RouterLink>
      </div>

      <!-- Chart -->
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6">
        <h2 class="text-base font-semibold text-slate-900 dark:text-white">System Statistics</h2>
        <p class="text-sm text-slate-500 dark:text-slate-400">Financial overview and transaction analytics</p>
        <div class="mt-4 h-80">
          <Bar :data="chartData" :options="chartOptions" />
        </div>
      </div>
    </template>
  </div>
</template>
