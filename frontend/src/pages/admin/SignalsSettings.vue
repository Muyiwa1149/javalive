<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Settings as SettingsIcon, AlertCircle } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const saving = ref(false)
const unavailable = ref(false)
const errorMessage = ref('')
const form = ref({ monthly: '', quaterly: '', yearly: '', telegram_link: '', telegram_bot_api: '' })

onMounted(async () => {
  try {
    const { data } = await api.get('/admin/signal-provider/settings')
    if (data) form.value = { ...form.value, ...data }
  } catch (e) {
    if (e.response?.status === 503) unavailable.value = true
    else errorMessage.value = e.response?.data?.message || 'Failed to load settings.'
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    await api.put('/admin/signal-provider/settings', form.value)
    Swal.fire({ icon: 'success', title: 'Settings saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-xl">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><SettingsIcon class="w-6 h-6 text-indigo-500" /> Signal Fee Settings</h1>
    <p class="text-sm text-slate-500 dark:text-slate-400">Proxies the external signal-settings API + saves the Telegram bot token locally.</p>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-slate-900 dark:text-white mb-1">Signal service not configured</h2>
      <p class="text-sm text-slate-500 dark:text-slate-400">This feature connects to an external API that hasn't been configured with a merchant key yet. You can still save the Telegram fields below once configured.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-800 rounded-2xl p-6 text-rose-700 dark:text-rose-300">{{ errorMessage }}</div>

    <form v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-4" @submit.prevent="save">
      <div class="grid grid-cols-3 gap-3">
        <div><label class="text-xs text-slate-500">Monthly fee</label><input v-model="form.monthly" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Quarterly fee</label><input v-model="form.quaterly" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Yearly fee</label><input v-model="form.yearly" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
      </div>
      <div><label class="text-xs text-slate-500">Telegram link</label><input v-model="form.telegram_link" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
      <div><label class="text-xs text-slate-500">Telegram bot API token</label><input v-model="form.telegram_bot_api" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
      <div class="flex justify-end pt-2">
        <button type="submit" :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save Settings' }}</button>
      </div>
    </form>
  </div>
</template>
