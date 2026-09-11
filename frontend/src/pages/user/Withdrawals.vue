<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Bitcoin, Landmark, Send, Mail as MailIcon } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const submitting = ref(false)
const requestingOtp = ref(false)
const methods = ref([])
const history = ref([])
const selectedMethodId = ref(null)

const amount = ref('')
const otpCode = ref('')
const otpSent = ref(false)
const details = ref('')
const bankName = ref('')
const accountName = ref('')
const accountNumber = ref('')
const swiftCode = ref('')

const selectedMethod = computed(() => methods.value.find((m) => m.id === selectedMethodId.value) || null)
const isBankMethod = computed(() => selectedMethod.value?.methodType === 'bank')
const methodIcon = (methodType) => ({ bank: Landmark, crypto: Bitcoin, mail: MailIcon }[methodType] || Landmark)

const estimatedCharges = computed(() => {
  if (!selectedMethod.value || !amount.value) return 0
  const amt = Number(amount.value)
  return selectedMethod.value.chargesType === 'percentage'
    ? (amt * Number(selectedMethod.value.chargesAmount)) / 100
    : Number(selectedMethod.value.chargesAmount)
})
const totalToDeduct = computed(() => (Number(amount.value) || 0) + estimatedCharges.value)

onMounted(async () => {
  try {
    const [methodsRes, historyRes] = await Promise.all([
      api.get('/withdrawals/methods'),
      api.get('/withdrawals'),
    ])
    methods.value = methodsRes.data
    history.value = historyRes.data
    if (methods.value.length > 0) selectedMethodId.value = methods.value[0].id
  } finally {
    loading.value = false
  }
})

async function requestOtp() {
  requestingOtp.value = true
  try {
    await api.post('/withdrawals/otp')
    otpSent.value = true
    Swal.fire({ icon: 'success', title: 'OTP sent', text: 'Check your email for the verification code.', background: '#1F2937', color: '#E5E7EB', timer: 2000, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed to send OTP', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    requestingOtp.value = false
  }
}

async function submit() {
  if (!selectedMethodId.value || !amount.value) {
    Swal.fire({ icon: 'warning', title: 'Missing information', text: 'Please select a method and enter an amount.', background: '#1F2937', color: '#E5E7EB' })
    return
  }
  submitting.value = true
  try {
    const { data } = await api.post('/withdrawals', {
      methodId: selectedMethodId.value,
      amount: Number(amount.value),
      otpCode: otpCode.value || null,
      details: isBankMethod.value ? null : details.value,
      bankName: isBankMethod.value ? bankName.value : null,
      accountName: isBankMethod.value ? accountName.value : null,
      accountNumber: isBankMethod.value ? accountNumber.value : null,
      swiftCode: isBankMethod.value ? swiftCode.value : null,
    })
    history.value.unshift(data)
    amount.value = ''
    otpCode.value = ''
    details.value = ''
    otpSent.value = false
    await Swal.fire({
      icon: 'success', title: 'Withdrawal Request Submitted!',
      text: 'Please wait while we process your request.',
      background: '#1F2937', color: '#E5E7EB',
    })
  } catch (e) {
    await Swal.fire({ icon: 'error', title: 'Submission failed', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}

const statusClass = (status) => ({
  Processed: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300',
  Rejected: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Withdraw Funds</h1>
      <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400 mt-1">Request a withdrawal from your account balance. All requests are reviewed by our team.</p>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else>
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-6">
        <div class="text-sm text-gray-500 dark:text-gray-400">
          Available balance: <span class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(authUser.user?.accountBalance ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
        </div>

        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-2">Select Withdrawal Method</label>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <button v-for="m in methods" :key="m.id" type="button"
              class="flex flex-col items-center gap-2 p-4 rounded-xl border transition-colors"
              :class="selectedMethodId === m.id ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' : 'border-gray-200 dark:border-gray-700 hover:border-blue-300'"
              @click="selectedMethodId = m.id">
              <img v-if="m.imageUrl" :src="m.imageUrl" class="w-8 h-8 object-contain" :alt="m.name">
              <component v-else :is="methodIcon(m.methodType)" class="w-8 h-8 text-blue-500" />
              <span class="text-sm font-medium text-gray-700 dark:text-gray-200">{{ m.name }}</span>
            </button>
          </div>
        </div>

        <form class="space-y-4" @submit.prevent="submit">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Amount</label>
              <input v-model="amount" type="number" step="0.01" min="0" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
              <p v-if="selectedMethod" class="text-xs text-gray-400 mt-1">
                Min {{ authUser.user?.currencySymbol }}{{ Number(selectedMethod.minimumAmount).toLocaleString() }} · Charges: {{ estimatedCharges.toFixed(2) }} · Total deducted: {{ totalToDeduct.toFixed(2) }}
              </p>
            </div>
            <div v-if="authUser.user?.sendOtpEmail !== false">
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">OTP Code</label>
              <div class="flex gap-2">
                <input v-model="otpCode" class="flex-1 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
                <button type="button" :disabled="requestingOtp" class="px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-lg text-sm whitespace-nowrap disabled:opacity-50" @click="requestOtp">
                  {{ requestingOtp ? 'Sending…' : (otpSent ? 'Resend OTP' : 'Send OTP') }}
                </button>
              </div>
            </div>
          </div>

          <template v-if="isBankMethod">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Bank Name</label>
                <input v-model="bankName" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
              </div>
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Account Name</label>
                <input v-model="accountName" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
              </div>
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Account Number</label>
                <input v-model="accountNumber" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
              </div>
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Swift / Routing Number</label>
                <input v-model="swiftCode" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
              </div>
            </div>
          </template>
          <template v-else>
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">
                {{ selectedMethod?.methodType === 'crypto' ? 'Wallet Address' : 'Payment Details' }}
              </label>
              <input v-model="details" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white font-mono text-sm">
            </div>
          </template>

          <button type="submit" :disabled="submitting" class="w-full flex items-center justify-center gap-2 px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50">
            <Send class="w-4 h-4" /> {{ submitting ? 'Submitting…' : 'Submit Withdrawal Request' }}
          </button>
        </form>
      </div>

      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <h2 class="font-semibold text-gray-900 dark:text-white mb-4">Withdrawal History</h2>
        <div v-if="history.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No withdrawals yet.</div>
        <template v-else>
          <!-- Mobile: card list -->
          <div class="sm:hidden space-y-3">
            <div v-for="w in history" :key="w.id" class="border border-gray-100 dark:border-gray-800 rounded-lg p-3">
              <div class="flex items-center justify-between mb-2">
                <span class="font-medium text-gray-900 dark:text-white">{{ w.paymentMode }}</span>
                <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span>
              </div>
              <div class="flex items-center justify-between text-sm mb-1">
                <span class="text-gray-500 dark:text-gray-400">Amount</span>
                <span class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(w.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
              </div>
              <div class="flex items-center justify-between text-sm mb-1">
                <span class="text-gray-500 dark:text-gray-400">Deducted</span>
                <span class="text-gray-700 dark:text-gray-300">{{ authUser.user?.currencySymbol }}{{ Number(w.toDeduct).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
              </div>
              <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ new Date(w.createdAt).toLocaleDateString() }}</div>
            </div>
          </div>
          <!-- Desktop: table -->
          <div class="hidden sm:block overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="text-left text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-800">
                  <th class="py-2 pr-4">Method</th>
                  <th class="py-2 pr-4">Amount</th>
                  <th class="py-2 pr-4">Deducted</th>
                  <th class="py-2 pr-4">Status</th>
                  <th class="py-2 pr-4">Date</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="w in history" :key="w.id" class="border-b border-gray-100 dark:border-gray-800 last:border-0">
                  <td class="py-3 pr-4 text-gray-900 dark:text-white">{{ w.paymentMode }}</td>
                  <td class="py-3 pr-4 text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(w.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
                  <td class="py-3 pr-4 text-gray-500 dark:text-gray-400">{{ authUser.user?.currencySymbol }}{{ Number(w.toDeduct).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
                  <td class="py-3 pr-4"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span></td>
                  <td class="py-3 pr-4 text-gray-500 dark:text-gray-400">{{ new Date(w.createdAt).toLocaleDateString() }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>
