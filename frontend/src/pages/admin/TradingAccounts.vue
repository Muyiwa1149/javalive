<script setup>
import { ref, onMounted } from 'vue'
import { Landmark, AlertCircle } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const data = ref(null)

onMounted(async () => {
  try {
    const { data: res } = await api.get('/admin/trading-accounts')
    data.value = res
  } catch (e) {
    if (e.response?.status === 503) unavailable.value = true
    else errorMessage.value = e.response?.data?.message || 'Failed to load trading accounts.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Landmark class="w-6 h-6 text-indigo-500" /> Trading Accounts</h1>
    <p class="text-sm text-slate-500 dark:text-slate-400">Proxies the external provisioned-accounts API — see Settings → App Settings for the merchant key.</p>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-slate-900 dark:text-white mb-1">Trading accounts service not configured</h2>
      <p class="text-sm text-slate-500 dark:text-slate-400">This feature connects to an external MT4/MT5 provisioning API that hasn't been configured with a merchant key yet.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-800 rounded-2xl p-6 text-rose-700 dark:text-rose-300">{{ errorMessage }}</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
      <pre class="text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap overflow-x-auto">{{ JSON.stringify(data, null, 2) }}</pre>
    </div>
  </div>
</template>
