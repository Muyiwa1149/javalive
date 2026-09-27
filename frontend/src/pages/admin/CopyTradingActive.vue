<script setup>
import { ref, onMounted } from 'vue'
import { Activity } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const trades = ref([])
const filter = ref('yes')

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/copy-trading/history', { params: filter.value ? { active: filter.value } : {} })
    trades.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}

const statusClass = (active) => (active === 'yes'
  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'
  : 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300')
const statusLabel = (active) => (active === 'yes' ? 'Active' : 'Stopped')
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Activity class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Copy Trading History</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ trades.length }} copy trades</p>
      </div>
      <select v-model="filter" class="rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] px-3 py-2.5 text-sm text-slate-900 dark:text-white" @change="load">
        <option value="">All</option>
        <option value="yes">Active</option>
        <option value="no">Stopped</option>
      </select>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="trades.length === 0" class="text-slate-500 dark:text-slate-400">No copy trades found.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="t in trades" :key="t.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ t.userName }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ t.userEmail }}</div>
          </div>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium" :class="statusClass(t.active)">{{ statusLabel(t.active) }}</span>
        </div>
        <div class="text-xs text-indigo-500">Copying {{ t.expertName }}</div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Invested</div><div class="text-slate-900 dark:text-white">{{ money(t.price) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Balance</div><div class="text-slate-900 dark:text-white">{{ money(t.currentBalance) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Profit</div><div class="text-emerald-600 dark:text-emerald-400">{{ money(t.totalProfit) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Trades (W)</div><div class="text-slate-500 dark:text-slate-400">{{ t.totalTrades }} ({{ t.winningTrades }}W)</div></div>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-white/5">Started {{ t.startedAt ? new Date(t.startedAt).toLocaleDateString() : '—' }}</div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && trades.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Expert</th>
            <th class="py-3 px-4 font-medium text-right">Invested</th>
            <th class="py-3 px-4 font-medium text-right">Balance</th>
            <th class="py-3 px-4 font-medium text-right">Profit</th>
            <th class="py-3 px-4 font-medium text-right">Trades (W/L)</th>
            <th class="py-3 px-4 font-medium">Status</th>
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
            <td class="py-3 px-4"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(t.active)">{{ statusLabel(t.active) }}</span></td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ t.startedAt ? new Date(t.startedAt).toLocaleDateString() : '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
