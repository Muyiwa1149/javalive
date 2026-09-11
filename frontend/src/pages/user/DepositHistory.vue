<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const history = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/deposits')
    history.value = data
  } finally {
    loading.value = false
  }
})

const statusClass = (status) => ({
  Processed: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Deposit History</h1>
        <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400 mt-1">All your past deposit requests and their status.</p>
      </div>
      <RouterLink :to="{ name: 'user.deposits' }"
        class="self-start sm:self-auto flex-shrink-0 inline-flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 text-sm font-medium rounded-lg">
        <ArrowLeft class="w-4 h-4" /> Back to Deposit
      </RouterLink>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <div v-else-if="history.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 text-sm text-gray-500 dark:text-gray-400">No deposits yet.</div>

    <template v-else>
      <!-- Mobile: card list -->
      <div class="sm:hidden space-y-3">
        <div v-for="d in history" :key="d.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="font-medium text-gray-900 dark:text-white">{{ d.paymentMode }}</span>
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500 dark:text-gray-400">{{ new Date(d.createdAt).toLocaleDateString() }}</span>
            <span class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(d.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
          </div>
        </div>
      </div>

      <!-- Desktop: table -->
      <div class="hidden sm:block bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-left text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-800">
                <th class="py-2 pr-4">Method</th>
                <th class="py-2 pr-4">Amount</th>
                <th class="py-2 pr-4">Status</th>
                <th class="py-2 pr-4">Date</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in history" :key="d.id" class="border-b border-gray-100 dark:border-gray-800 last:border-0">
                <td class="py-3 pr-4 text-gray-900 dark:text-white">{{ d.paymentMode }}</td>
                <td class="py-3 pr-4 text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(d.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
                <td class="py-3 pr-4"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span></td>
                <td class="py-3 pr-4 text-gray-500 dark:text-gray-400">{{ new Date(d.createdAt).toLocaleDateString() }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
