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
function shortDate(value) {
  return new Date(value).toLocaleDateString()
}
function fullDate(value) {
  return new Date(value).toLocaleString()
}

const statusClass = (status) => ({
  Processed: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  Rejected: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')

const tradeTypeClass = (type) => ['Buy', 'WIN'].includes(type)
  ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300'
  : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300'
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Receipt class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Account Statement
    </h1>

    <div class="grid grid-cols-2 sm:flex sm:flex-wrap gap-2 sm:gap-0 sm:rounded-lg sm:border sm:border-gray-200 sm:dark:border-gray-800 sm:overflow-hidden">
      <button type="button" class="px-3 py-2 text-sm font-medium rounded-lg sm:rounded-none text-center border border-gray-200 dark:border-gray-800 sm:border-0" :class="tab === 'transactions' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'transactions'">Transactions</button>
      <button type="button" class="px-3 py-2 text-sm font-medium rounded-lg sm:rounded-none text-center border border-gray-200 dark:border-gray-800 sm:border-0" :class="tab === 'deposits' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'deposits'">Deposits</button>
      <button type="button" class="px-3 py-2 text-sm font-medium rounded-lg sm:rounded-none text-center border border-gray-200 dark:border-gray-800 sm:border-0" :class="tab === 'withdrawals' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'withdrawals'">Withdrawals</button>
      <button type="button" class="px-3 py-2 text-sm font-medium rounded-lg sm:rounded-none text-center border border-gray-200 dark:border-gray-800 sm:border-0" :class="tab === 'trades' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'trades'">Trading History</button>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else-if="history">
      <!-- Transactions -->
      <template v-if="tab === 'transactions'">
        <div v-if="history.transactions.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">No transactions yet.</div>
        <template v-else>
          <div class="sm:hidden space-y-3">
            <div v-for="t in history.transactions" :key="t.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
              <div class="flex items-center justify-between mb-1">
                <span class="font-medium text-gray-900 dark:text-white">{{ t.planLabel || '—' }}</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ money(t.amount) }}</span>
              </div>
              <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                <span>{{ t.type }}</span>
                <span>{{ shortDate(t.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div class="hidden sm:block bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
                <tr><th class="px-4 py-3">Description</th><th class="px-4 py-3">Type</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Date</th></tr>
              </thead>
              <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                <tr v-for="t in history.transactions" :key="t.id">
                  <td class="px-4 py-3 text-gray-900 dark:text-white">{{ t.planLabel || '—' }}</td>
                  <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ t.type }}</td>
                  <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(t.amount) }}</td>
                  <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ fullDate(t.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </template>

      <!-- Deposits -->
      <template v-else-if="tab === 'deposits'">
        <div v-if="history.deposits.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">No deposits yet.</div>
        <template v-else>
          <div class="sm:hidden space-y-3">
            <div v-for="d in history.deposits" :key="d.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-900 dark:text-white">{{ d.paymentMode }}</span>
                <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">{{ shortDate(d.createdAt) }}</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ money(d.amount) }}</span>
              </div>
            </div>
          </div>
          <div class="hidden sm:block bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
                <tr><th class="px-4 py-3">Method</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Status</th><th class="px-4 py-3">Date</th></tr>
              </thead>
              <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                <tr v-for="d in history.deposits" :key="d.id">
                  <td class="px-4 py-3 text-gray-900 dark:text-white">{{ d.paymentMode }}</td>
                  <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(d.amount) }}</td>
                  <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span></td>
                  <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ fullDate(d.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </template>

      <!-- Withdrawals -->
      <template v-else-if="tab === 'withdrawals'">
        <div v-if="history.withdrawals.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">No withdrawals yet.</div>
        <template v-else>
          <div class="sm:hidden space-y-3">
            <div v-for="w in history.withdrawals" :key="w.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-900 dark:text-white">{{ w.paymentMode }}</span>
                <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">{{ shortDate(w.createdAt) }}</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ money(w.amount) }}</span>
              </div>
            </div>
          </div>
          <div class="hidden sm:block bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
                <tr><th class="px-4 py-3">Method</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Status</th><th class="px-4 py-3">Date</th></tr>
              </thead>
              <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                <tr v-for="w in history.withdrawals" :key="w.id">
                  <td class="px-4 py-3 text-gray-900 dark:text-white">{{ w.paymentMode }}</td>
                  <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(w.amount) }}</td>
                  <td class="px-4 py-3"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span></td>
                  <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ fullDate(w.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </template>

      <!-- Trading History -->
      <template v-else>
        <div v-if="history.trades.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">No trades yet.</div>
        <template v-else>
          <div class="sm:hidden space-y-3">
            <div v-for="t in history.trades" :key="t.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-900 dark:text-white">{{ t.planLabel }}</span>
                <span class="px-2 py-1 rounded-full text-xs font-medium" :class="tradeTypeClass(t.type)">{{ t.type }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500 dark:text-gray-400">{{ shortDate(t.createdAt) }}</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ money(t.amount) }}</span>
              </div>
            </div>
          </div>
          <div class="hidden sm:block bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
                <tr><th class="px-4 py-3">Asset</th><th class="px-4 py-3">Type</th><th class="px-4 py-3 text-right">Amount</th><th class="px-4 py-3">Date</th></tr>
              </thead>
              <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                <tr v-for="t in history.trades" :key="t.id">
                  <td class="px-4 py-3 text-gray-900 dark:text-white">{{ t.planLabel }}</td>
                  <td class="px-4 py-3">
                    <span class="px-2 py-1 rounded-full text-xs font-medium" :class="tradeTypeClass(t.type)">{{ t.type }}</span>
                  </td>
                  <td class="px-4 py-3 text-right font-medium text-gray-900 dark:text-white">{{ money(t.amount) }}</td>
                  <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ fullDate(t.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </template>
    </template>
  </div>
</template>
