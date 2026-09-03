<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Signal, AlertCircle, Check, Trash2 } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const signals = ref(null)

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

async function markResult(id, result) {
  try {
    await api.put('/admin/signal-provider/signals/result', { signalId: id, result })
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  }
}

async function remove(id) {
  const confirm = await Swal.fire({ icon: 'warning', title: 'Delete this signal?', showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  try {
    await api.delete(`/admin/signal-provider/signals/${id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Signal class="w-6 h-6 text-indigo-500" /> Active Signals</h1>
    <p class="text-sm text-slate-500 dark:text-slate-400">Currently open signals from the same external trading-signals feed. Use the Signals page to post a new one.</p>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-slate-900 dark:text-white mb-1">Signal service not configured</h2>
      <p class="text-sm text-slate-500 dark:text-slate-400">This feature connects to an external forex signal broadcast API that hasn't been configured with a merchant key yet.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-800 rounded-2xl p-6 text-rose-700 dark:text-rose-300">{{ errorMessage }}</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-4">
      <pre class="text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap overflow-x-auto">{{ JSON.stringify(signals, null, 2) }}</pre>
      <p class="text-xs text-slate-400 dark:text-slate-500">Use a signal's id with the actions below once the real response shape is confirmed against a configured upstream.</p>
      <div class="flex gap-2">
        <button class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-medium" @click="() => { const id = prompt('Signal id'); if (id) markResult(id, 'WIN') }"><Check class="w-3.5 h-3.5" /> Mark WIN</button>
        <button class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-rose-500 hover:bg-rose-600 text-white text-xs font-medium" @click="() => { const id = prompt('Signal id'); if (id) markResult(id, 'LOSS') }"><Check class="w-3.5 h-3.5" /> Mark LOSS</button>
        <button class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-xs font-medium" @click="() => { const id = prompt('Signal id to delete'); if (id) remove(id) }"><Trash2 class="w-3.5 h-3.5" /> Delete</button>
      </div>
    </div>
  </div>
</template>
