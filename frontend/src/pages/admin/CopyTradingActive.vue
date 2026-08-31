<script setup>
import { ref, onMounted } from 'vue'
import { Activity } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const trades = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/admin/copy-trading/active-trades')
    trades.value = data
  } finally {
    loading.value = false
  }
})

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Activity class="w-6 h-6 text-indigo-500" /> Active Copy Trades</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">{{ trades.length }} users currently copying an expert</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="trades.length === 0" class="text-slate-500 dark:text-slate-400">No active copy trades.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Expert</th>
            <th class="py-3 px-4 font-medium text-right">Invested</th>
            <th class="py-3 px-4 font-medium text-right">Balance</th>
            <th class="py-3 px-4 font-medium text-right">Profit</th>
            <th class="py-3 px-4 font-medium text-right">Trades (W/L)</th>
            <th class="py-3 px-4 font-medium">Started</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in trades" :key="t.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ t.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ t.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ t.expertName }}</td>
            <td class="py-3 px-4 text-right text-slate-900 dark:text-white">{{ money(t.price) }}</td>
            <td class="py-3 px-4 text-right text-slate-900 dark:text-white">{{ money(t.currentBalance) }}</td>
            <td class="py-3 px-4 text-right text-emerald-600 dark:text-emerald-400">{{ money(t.totalProfit) }}</td>
            <td class="py-3 px-4 text-right text-slate-500 dark:text-slate-400">{{ t.totalTrades }} ({{ t.winningTrades }}W)</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ t.startedAt ? new Date(t.startedAt).toLocaleDateString() : '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
