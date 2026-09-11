<script setup>
import { ref, onMounted } from 'vue'
import { FileText } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const loans = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/loans')
    loans.value = data
  } finally {
    loading.value = false
  }
})

const statusClass = (active) => ({
  Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
  Approved: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Rejected: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
}[active] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1 sm:gap-0">
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <FileText class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Credit History
      </h1>
      <RouterLink :to="{ name: 'user.loans' }" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">Apply for new credit</RouterLink>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="loans.length === 0" class="text-gray-500 dark:text-gray-400">You haven't applied for any loans yet.</div>

    <div v-else class="space-y-3">
      <div v-for="l in loans" :key="l.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
        <div class="flex items-center justify-between mb-2">
          <div class="font-medium text-gray-900 dark:text-white">{{ l.facility }}</div>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(l.active)">{{ l.active }}</span>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
          <div><div class="text-gray-500 dark:text-gray-400">Amount</div><div class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(l.amount).toLocaleString() }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Duration</div><div class="font-semibold text-gray-900 dark:text-white">{{ l.duration }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Income</div><div class="font-semibold text-gray-900 dark:text-white">{{ l.income }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Applied</div><div class="font-semibold text-gray-900 dark:text-white">{{ new Date(l.createdAt).toLocaleDateString() }}</div></div>
        </div>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-3">{{ l.purpose }}</p>
      </div>
    </div>
  </div>
</template>
