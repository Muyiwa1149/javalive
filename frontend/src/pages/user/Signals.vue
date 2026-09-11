<script setup>
import { ref, onMounted } from 'vue'
import { Signal } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const signals = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/signals/mine')
    signals.value = data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Signal class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> My Signals
      </h1>
      <RouterLink :to="{ name: 'user.signals-premium' }" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">Browse Signal Plans</RouterLink>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="signals.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">
      You don't have any signal subscriptions yet. Browse available plans to get started.
    </div>

    <div v-else class="space-y-3">
      <div v-for="s in signals" :key="s.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
        <div class="flex items-center justify-between">
          <div class="font-medium text-gray-900 dark:text-white">{{ s.planName || s.asset }}</div>
          <span class="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">{{ s.status }}</span>
        </div>
        <div class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ s.orderType }} · {{ authUser.user?.currencySymbol }}{{ Number(s.amount).toLocaleString() }} · {{ s.leverage }}x</div>
      </div>
    </div>
  </div>
</template>
