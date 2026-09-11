<script setup>
import { ref, onMounted } from 'vue'
import { Zap, TrendingUp } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const plans = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/signals/plans')
    plans.value = data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Zap class="w-5 h-5 sm:w-6 sm:h-6 text-amber-500" /> Premium Signal Plans
    </h1>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="plans.length === 0" class="text-gray-500 dark:text-gray-400">No signal plans currently available.</div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="p in plans" :key="p.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6">
        <h3 class="font-bold text-gray-900 dark:text-white mb-2">{{ p.name }}</h3>
        <div class="text-2xl font-extrabold text-gray-900 dark:text-white mb-4">{{ authUser.user?.currencySymbol }}{{ Number(p.price).toLocaleString() }}</div>
        <div v-if="p.incrementAmount" class="flex items-center gap-2 text-sm text-emerald-600 dark:text-emerald-400">
          <TrendingUp class="w-4 h-4" /> {{ p.incrementAmount }}% expected growth
        </div>
      </div>
    </div>
  </div>
</template>
