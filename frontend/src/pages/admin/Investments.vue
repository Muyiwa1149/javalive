<script setup>
import { ref, onMounted } from 'vue'
import { TrendingUp } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const investments = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/admin/investments')
    investments.value = data
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
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><TrendingUp class="w-6 h-6 text-indigo-500" /> Active Investments</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">{{ investments.length }} active investments across all users</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="investments.length === 0" class="text-slate-500 dark:text-slate-400">No active investments.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Plan</th>
            <th class="py-3 px-4 font-medium text-right">Amount</th>
            <th class="py-3 px-4 font-medium text-right">Profit Earned</th>
            <th class="py-3 px-4 font-medium">Activated</th>
            <th class="py-3 px-4 font-medium">Expires</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="i in investments" :key="i.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ i.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ i.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ i.planName }}</td>
            <td class="py-3 px-4 text-right font-medium text-slate-900 dark:text-white">{{ money(i.amount) }}</td>
            <td class="py-3 px-4 text-right text-emerald-600 dark:text-emerald-400">{{ money(i.profitEarned) }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.activatedAt ? new Date(i.activatedAt).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.expireDate ? new Date(i.expireDate).toLocaleDateString() : '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
