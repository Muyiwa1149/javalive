<script setup>
import { ref, onMounted } from 'vue'
import { Receipt } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const history = ref(null)
const tab = ref('transactions')

onMounted(async () => {
  try {
    const { data } = await api.get('/account-history')
    history.value = data
  } finally {
    loading.value = false
  }
})

function money(value) {
  return `${authUser.user?.currencySymbol || '$'}${Number(value ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}`
}

const statusClass = (status) => ({
  Processed: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  Rejected: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Receipt class="w-6 h-6 text-blue-500" /> Account Statement
    </h1>

    <div class="flex rounded-lg border border-gray-200 dark:border-gray-800 overflow-hidden w-fit">
      <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'transactions' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'transactions'">Transactions</button>
      <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'deposits' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'deposits'">Deposits</button>
      <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'withdrawals' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'withdrawals'">Withdrawals</button>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else-if="history">
      <div v-if="tab === 'transactions'" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
        <div v-if="history.transactions.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">No transactions yet.</div>
        <table v-else class="w-full text-sm">
          <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
            <tr><th class="px-4 py-3">Description</th><th class="px-4 py-3">Type</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Date</th></tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            <tr v-for="t in history.transactions" :key="t.id">
              <td class="px-4 py-3 text-gray-900 dark:text-white">{{ t.planLabel || '—' }}</td>
              <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ t.type }}</td>
              <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(t.amount) }}</td>
              <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ new Date(t.createdAt).toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else-if="tab === 'deposits'" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
        <div v-if="history.deposits.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">No deposits yet.</div>
        <table v-else class="w-full text-sm">
          <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
            <tr><th class="px-4 py-3">Method</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Status</th><th class="px-4 py-3">Date</th></tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            <tr v-for="d in history.deposits" :key="d.id">
              <td class="px-4 py-3 text-gray-900 dark:text-white">{{ d.paymentMode }}</td>
              <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(d.amount) }}</td>
              <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span></td>
              <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ new Date(d.createdAt).toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
        <div v-if="history.withdrawals.length === 0" class="p-8 text-center text-gray-500 dark:text-gray-400">No withdrawals yet.</div>
        <table v-else class="w-full text-sm">
          <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
            <tr><th class="px-4 py-3">Method</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Status</th><th class="px-4 py-3">Date</th></tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            <tr v-for="w in history.withdrawals" :key="w.id">
              <td class="px-4 py-3 text-gray-900 dark:text-white">{{ w.paymentMode }}</td>
              <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(w.amount) }}</td>
              <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span></td>
              <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ new Date(w.createdAt).toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
