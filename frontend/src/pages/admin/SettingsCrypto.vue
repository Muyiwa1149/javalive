<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Repeat } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const saving = ref(false)
const form = ref({})

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/settings')
    form.value = { ...data }
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function save() {
  saving.value = true
  try {
    const { data } = await api.put('/admin/settings/crypto', form.value)
    form.value = { ...data }
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
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Repeat class="w-6 h-6 text-indigo-500" /> Crypto / Exchange Settings</h1>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <form v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-4" @submit.prevent="save">
      <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
        <input v-model="form.useCryptoFeature" type="checkbox" class="rounded" /> Enable crypto exchange feature
      </label>
      <div class="grid grid-cols-2 gap-3">
        <div><label class="text-xs text-slate-500">Exchange fee %</label><input v-model.number="form.exchangeFeePct" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Currency rate</label><input v-model.number="form.currencyRate" type="number" step="0.00000001" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Local currency</label><input v-model="form.localCurrency" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Base currency</label><input v-model="form.baseCurrency" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
      </div>
      <p class="text-xs text-slate-500 dark:text-slate-400">Individual crypto/deposit methods are managed under Payment Gateways → Manual / Crypto Methods.</p>
      <div class="flex justify-end pt-2">
        <button type="submit" :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save Settings' }}</button>
      </div>
    </form>
  </div>
</template>
