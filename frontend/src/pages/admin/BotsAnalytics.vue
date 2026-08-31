<script setup>
import { ref, computed, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Filler } from 'chart.js'
import { BarChart3, Bot, Wallet, TrendingUp, Activity } from 'lucide-vue-next'
import api from '@/lib/api'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Filler)

const loading = ref(true)
const dashboard = ref(null)

onMounted(async () => {
  try {
    const { data } = await api.get('/admin/bots/dashboard')
    dashboard.value = data
  } finally {
    loading.value = false
  }
})

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}

const chartData = computed(() => ({
  labels: dashboard.value?.dailyProfits.map((d) => d.date.slice(5)) ?? [],
  datasets: [{
    label: 'Daily Profit',
    data: dashboard.value?.dailyProfits.map((d) => d.profit) ?? [],
    borderColor: '#6366f1',
    backgroundColor: 'rgba(99,102,241,0.15)',
    fill: true,
    tension: 0.3,
    pointRadius: 0,
  }],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { grid: { color: 'rgba(148,163,184,0.15)' }, ticks: { color: '#94a3b8' } },
    x: { grid: { display: false }, ticks: { color: '#94a3b8', maxTicksLimit: 10 } },
  },
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><BarChart3 class="w-6 h-6 text-indigo-500" /> Bot Trading Analytics</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">System-wide bot performance</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <template v-else-if="dashboard">
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Bot class="w-3.5 h-3.5" /> Active Bots</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ dashboard.activeBots }} / {{ dashboard.totalBots }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Wallet class="w-3.5 h-3.5" /> Total Invested</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ money(dashboard.totalInvestments) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><TrendingUp class="w-3.5 h-3.5" /> Total Profits</div>
          <p class="mt-1.5 text-lg font-bold text-emerald-500">{{ money(dashboard.totalProfits) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Activity class="w-3.5 h-3.5" /> Investments</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ dashboard.activeInvestments }} active · {{ dashboard.completedInvestments }} done</p>
        </div>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6">
        <h2 class="text-base font-semibold text-slate-900 dark:text-white mb-1">Daily Profit — Last 30 Days</h2>
        <div class="h-64"><Line :data="chartData" :options="chartOptions" /></div>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
              <th class="py-3 px-4 font-medium">Top Bots</th>
              <th class="py-3 px-4 font-medium text-right">Investments</th>
              <th class="py-3 px-4 font-medium text-right">Active</th>
              <th class="py-3 px-4 font-medium text-right">Total Profits</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in dashboard.topBots" :key="b.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
              <td class="py-3 px-4 text-slate-900 dark:text-white font-medium">{{ b.name }}</td>
              <td class="py-3 px-4 text-right text-slate-700 dark:text-slate-300">{{ b.investmentsCount }}</td>
              <td class="py-3 px-4 text-right text-slate-700 dark:text-slate-300">{{ b.activeInvestmentsCount }}</td>
              <td class="py-3 px-4 text-right text-emerald-500">{{ money(b.totalProfits) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
