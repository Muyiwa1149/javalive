<script setup>
import { ref, onMounted } from 'vue'
import { Radio, AlertCircle, MessageCircle } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const subscription = ref(null)
const settings = ref(null)

onMounted(async () => {
  try {
    const [subRes, setRes] = await Promise.all([
      api.get('/signals/external/subscription'),
      api.get('/signals/external/settings'),
    ])
    subscription.value = subRes.data?.data
    settings.value = setRes.data?.data?.settings
  } catch (e) {
    if (e.response?.status === 503) {
      unavailable.value = true
    } else {
      errorMessage.value = e.response?.data?.message || 'Failed to load signal provider details.'
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Radio class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Signal Providers
    </h1>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-gray-900 dark:text-white mb-1">Signal service not configured</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400">This feature connects to an external forex signal broadcast that hasn't been configured with an API key yet. Ask an administrator to set it up in Settings.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-6 text-red-700 dark:text-red-300">
      {{ errorMessage }}
    </div>

    <template v-else>
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <h2 class="font-semibold text-gray-900 dark:text-white mb-3">Your Subscription</h2>
        <pre v-if="subscription" class="text-xs text-gray-600 dark:text-gray-300 whitespace-pre-wrap">{{ JSON.stringify(subscription, null, 2) }}</pre>
        <p v-else class="text-sm text-gray-500 dark:text-gray-400">You are not currently subscribed to signal broadcasts.</p>
      </div>

      <div v-if="settings" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <h2 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2"><MessageCircle class="w-4 h-4" /> Telegram Broadcast Settings</h2>
        <pre class="text-xs text-gray-600 dark:text-gray-300 whitespace-pre-wrap">{{ JSON.stringify(settings, null, 2) }}</pre>
      </div>
    </template>
  </div>
</template>
