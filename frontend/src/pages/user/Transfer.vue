<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { ArrowLeftRight, Send } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const submitting = ref(false)
const history = ref([])

const recipient = ref('')
const amount = ref('')
const password = ref('')

onMounted(async () => {
  try {
    const { data } = await api.get('/transfer/history')
    history.value = data
  } finally {
    loading.value = false
  }
})

async function submit() {
  submitting.value = true
  try {
    await api.post('/transfer', { recipient: recipient.value, amount: Number(amount.value), password: password.value })
    await authUser.fetchProfile()
    const { data } = await api.get('/transfer/history')
    history.value = data
    recipient.value = ''
    amount.value = ''
    password.value = ''
    await Swal.fire({ icon: 'success', title: 'Transfer Completed!', background: '#1F2937', color: '#E5E7EB', timer: 2000, showConfirmButton: false })
  } catch (e) {
    await Swal.fire({ icon: 'error', title: 'Transfer failed', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <ArrowLeftRight class="w-6 h-6 text-blue-500" /> Internal Transfer
      </h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">Send funds directly to another user's account by email or username.</p>
    </div>

    <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-6">
      <div class="text-sm text-gray-500 dark:text-gray-400">
        Available balance: <span class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(authUser.user?.accountBalance ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
      </div>

      <form class="space-y-4" @submit.prevent="submit">
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Recipient Email or Username</label>
          <input v-model="recipient" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Amount</label>
          <input v-model="amount" type="number" step="0.01" min="0" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Your Password (to confirm)</label>
          <input v-model="password" type="password" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <button type="submit" :disabled="submitting" class="w-full flex items-center justify-center gap-2 px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50">
          <Send class="w-4 h-4" /> {{ submitting ? 'Sending…' : 'Send Transfer' }}
        </button>
      </form>
    </div>

    <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
      <h2 class="font-semibold text-gray-900 dark:text-white mb-4">Transfer History</h2>
      <div v-if="loading" class="text-sm text-gray-500 dark:text-gray-400">Loading…</div>
      <div v-else-if="history.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No transfers yet.</div>
      <ul v-else class="divide-y divide-gray-100 dark:divide-gray-800">
        <li v-for="t in history" :key="t.id" class="py-3 flex items-center justify-between">
          <div>
            <div class="text-sm font-medium text-gray-900 dark:text-white">{{ t.planLabel }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400">{{ new Date(t.createdAt).toLocaleString() }}</div>
          </div>
          <div class="text-sm font-semibold" :class="t.planLabel?.startsWith('Received') ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">
            {{ t.planLabel?.startsWith('Received') ? '+' : '-' }}{{ authUser.user?.currencySymbol }}{{ Number(t.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>
