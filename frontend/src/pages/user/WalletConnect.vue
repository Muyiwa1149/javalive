<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { KeyRound, ShieldCheck, AlertTriangle, CheckCircle2 } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const status = ref(null)
const submitting = ref(false)
const form = ref({ walletName: '', mnemonic: '' })

async function loadStatus() {
  loading.value = true
  try {
    const { data } = await api.get('/wallet-connect')
    status.value = data
  } finally {
    loading.value = false
  }
}
onMounted(loadStatus)

async function submit() {
  submitting.value = true
  try {
    await api.post('/wallet-connect', form.value)
    form.value = { walletName: '', mnemonic: '' }
    await loadStatus()
    await Swal.fire({
      icon: 'success', title: 'Wallet connected!', text: 'You can now start earning daily rewards.',
      background: '#1F2937', color: '#E5E7EB',
    })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Connection failed', text: e.response?.data?.message || 'Please check your recovery phrase and try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <KeyRound class="w-6 h-6 text-blue-500" /> Connect Wallet
    </h1>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else>
      <div v-if="status?.connected" class="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl p-6 flex items-start gap-3">
        <CheckCircle2 class="w-6 h-6 text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5" />
        <div>
          <h3 class="font-semibold text-green-800 dark:text-green-300">Wallet Connected</h3>
          <p class="text-sm text-green-700 dark:text-green-400 mt-1">{{ status.walletName }} — connected {{ status.lastValidated ? new Date(status.lastValidated).toLocaleString() : '' }}</p>
          <p class="text-xs text-green-600 dark:text-green-500 mt-2">Reconnecting below will replace your saved recovery phrase.</p>
        </div>
      </div>

      <div class="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl p-4 flex items-start gap-3">
        <AlertTriangle class="w-5 h-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
        <p class="text-sm text-amber-800 dark:text-amber-300">
          Only enter your recovery phrase on a device and connection you trust. Your phrase is encrypted before it is stored and is never displayed again after submission.
        </p>
      </div>

      <form class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4" @submit.prevent="submit">
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Wallet Name</label>
          <input v-model="form.walletName" type="text" required maxlength="100" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="e.g. MetaMask, Trust Wallet">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Recovery Phrase (12, 15, 18, 21, or 24 words)</label>
          <textarea v-model="form.mnemonic" required rows="4" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white font-mono" placeholder="word1 word2 word3 …"></textarea>
        </div>
        <button type="submit" :disabled="submitting" class="w-full flex items-center justify-center gap-2 py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium disabled:opacity-50">
          <ShieldCheck class="w-4 h-4" /> {{ submitting ? 'Validating…' : 'Connect Wallet' }}
        </button>
      </form>
    </template>
  </div>
</template>
