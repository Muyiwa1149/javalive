<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Radio, AlertCircle, Plus } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const signals = ref(null)
const showForm = ref(false)
const saving = ref(false)
const form = ref({ direction: 'BUY', pair: '', price: '', tp1: '', tp2: '', sl1: '' })

async function load() {
  loading.value = true
  unavailable.value = false
  errorMessage.value = ''
  try {
    const { data } = await api.get('/admin/signal-provider/signals')
    signals.value = data
  } catch (e) {
    if (e.response?.status === 503) unavailable.value = true
    else errorMessage.value = e.response?.data?.message || 'Failed to load signals.'
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function submit() {
  saving.value = true
  try {
    await api.post('/admin/signal-provider/signals', form.value)
    showForm.value = false
    await load()
    Swal.fire({ icon: 'success', title: 'Signal posted', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Radio class="w-6 h-6 text-indigo-500" /> Signals</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">Proxies the external trading-signals API</p>
      </div>
      <button class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium" @click="showForm = true"><Plus class="w-4 h-4" /> Post Signal</button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-slate-900 dark:text-white mb-1">Signal service not configured</h2>
      <p class="text-sm text-slate-500 dark:text-slate-400">This feature connects to an external forex signal broadcast API that hasn't been configured with a merchant key yet.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-800 rounded-2xl p-6 text-rose-700 dark:text-rose-300">{{ errorMessage }}</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
      <pre class="text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap overflow-x-auto">{{ JSON.stringify(signals, null, 2) }}</pre>
    </div>

    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Post Signal</h2>
        <form class="space-y-3" @submit.prevent="submit">
          <select v-model="form.direction" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
          <input v-model="form.pair" required placeholder="Pair (e.g. EUR/USD)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.price" required placeholder="Entry price" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-3 gap-3">
            <input v-model="form.tp1" placeholder="TP1" class="px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="form.tp2" placeholder="TP2" class="px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="form.sl1" placeholder="SL" class="px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Post</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
