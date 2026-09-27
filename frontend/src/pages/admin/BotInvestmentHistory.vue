<script setup>
import { ref, onMounted } from 'vue'
import { History } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const investments = ref([])
const filter = ref('')

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/bots/investments', { params: filter.value ? { status: filter.value } : {} })
    investments.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}

const statusStyle = {
  active: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400',
  completed: 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300',
  cancelled: 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400',
}
function statusLabel(status) {
  return status ? status[0].toUpperCase() + status.slice(1) : '—'
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><History class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Bot Investment History</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ investments.length }} bot investments across all users</p>
      </div>
      <select v-model="filter" class="rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] px-3 py-2.5 text-sm text-slate-900 dark:text-white" @change="load">
        <option value="">All</option>
        <option value="active">Active</option>
        <option value="completed">Completed</option>
        <option value="cancelled">Cancelled</option>
      </select>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="investments.length === 0" class="text-slate-500 dark:text-slate-400">No bot investments found.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="i in investments" :key="i.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ i.userName }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ i.userEmail }}</div>
          </div>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium" :class="statusStyle[i.status] || statusStyle.completed">{{ statusLabel(i.status) }}</span>
        </div>
        <div class="text-xs text-indigo-500">{{ i.botName }}</div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Invested</div><div class="text-slate-900 dark:text-white">{{ money(i.investmentAmount) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Balance</div><div class="text-slate-900 dark:text-white">{{ money(i.currentBalance) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Profit</div><div class="text-emerald-600 dark:text-emerald-400">{{ money(i.totalProfit) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Loss</div><div class="text-rose-500">{{ money(i.totalLoss) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Trades (W/L)</div><div class="text-slate-500 dark:text-slate-400">{{ i.successfulTrades }}/{{ i.failedTrades }}</div></div>
          <div><div class="text-[11px] text-slate-500">Expires</div><div class="text-slate-500 dark:text-slate-400">{{ i.expiresAt ? new Date(i.expiresAt).toLocaleDateString() : '—' }}</div></div>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-white/5">Started {{ i.startedAt ? new Date(i.startedAt).toLocaleDateString() : '—' }}</div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && investments.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Bot</th>
            <th class="py-3 px-4 font-medium text-right">Invested</th>
            <th class="py-3 px-4 font-medium text-right">Balance</th>
            <th class="py-3 px-4 font-medium text-right">Profit</th>
            <th class="py-3 px-4 font-medium text-right">Loss</th>
            <th class="py-3 px-4 font-medium text-right">Trades (W/L)</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium">Started</th>
            <th class="py-3 px-4 font-medium">Expires</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="i in investments" :key="i.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ i.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ i.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ i.botName }}</td>
            <td class="py-3 px-4 text-right text-slate-900 dark:text-white">{{ money(i.investmentAmount) }}</td>
            <td class="py-3 px-4 text-right text-slate-900 dark:text-white">{{ money(i.currentBalance) }}</td>
            <td class="py-3 px-4 text-right text-emerald-600 dark:text-emerald-400">{{ money(i.totalProfit) }}</td>
            <td class="py-3 px-4 text-right text-rose-500">{{ money(i.totalLoss) }}</td>
            <td class="py-3 px-4 text-right text-slate-500 dark:text-slate-400">{{ i.successfulTrades }}/{{ i.failedTrades }}</td>
            <td class="py-3 px-4"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusStyle[i.status] || statusStyle.completed">{{ statusLabel(i.status) }}</span></td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.startedAt ? new Date(i.startedAt).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.expiresAt ? new Date(i.expiresAt).toLocaleDateString() : '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
