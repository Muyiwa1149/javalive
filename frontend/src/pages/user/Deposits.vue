<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { RouterLink } from 'vue-router'
import { Landmark, Bitcoin, Mail, Upload, Copy, Check, History } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const submitting = ref(false)
const methods = ref([])
const selectedMethodId = ref(null)
const amount = ref('')
const txnId = ref('')
const proofFile = ref(null)
const copied = ref(false)

const selectedMethod = computed(() => methods.value.find((m) => m.id === selectedMethodId.value) || null)

const methodIcon = (methodType) => ({ bank: Landmark, crypto: Bitcoin, mail: Mail }[methodType] || Landmark)

onMounted(async () => {
  try {
    const { data } = await api.get('/deposits/methods')
    methods.value = data
    if (methods.value.length > 0) selectedMethodId.value = methods.value[0].id
  } finally {
    loading.value = false
  }
})

function onFileChange(e) { proofFile.value = e.target.files[0] || null }

async function copyAddress() {
  if (!selectedMethod.value?.walletAddress) return
  await navigator.clipboard.writeText(selectedMethod.value.walletAddress)
  copied.value = true
  setTimeout(() => { copied.value = false }, 1500)
}

async function submit() {
  if (!selectedMethodId.value || !amount.value || !proofFile.value) {
    Swal.fire({ icon: 'warning', title: 'Missing information', text: 'Please select a method, enter an amount, and upload proof of payment.', background: '#1F2937', color: '#E5E7EB' })
    return
  }
  submitting.value = true
  try {
    const payload = new FormData()
    payload.append('methodId', selectedMethodId.value)
    payload.append('amount', amount.value)
    if (txnId.value) payload.append('txnId', txnId.value)
    payload.append('proof', proofFile.value)
    await api.post('/deposits', payload, { headers: { 'Content-Type': 'multipart/form-data' } })
    amount.value = ''
    txnId.value = ''
    proofFile.value = null
    await Swal.fire({
      icon: 'success', title: 'Deposit Request Submitted!',
      text: 'Please wait while we validate this transaction. You will be notified once it is approved.',
      background: '#1F2937', color: '#E5E7EB',
    })
  } catch (e) {
    await Swal.fire({ icon: 'error', title: 'Submission failed', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}

</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Deposit Funds</h1>
        <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400 mt-1">Fund your account balance using one of the methods below. All deposits are reviewed before your balance is updated.</p>
      </div>
      <RouterLink :to="{ name: 'user.deposits-history' }"
        class="flex-shrink-0 inline-flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 text-sm font-medium rounded-lg">
        <History class="w-4 h-4" /> History
      </RouterLink>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else>
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-6">
        <!-- Method selection -->
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-2">Select Payment Method</label>
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

        <!-- Method details -->
        <div v-if="selectedMethod" class="bg-gray-50 dark:bg-gray-800 rounded-xl p-4 space-y-2 text-sm">
          <div class="flex justify-between text-gray-500 dark:text-gray-400">
            <span>Min / Max amount</span>
            <span class="text-gray-900 dark:text-white font-medium">{{ authUser.user?.currencySymbol }}{{ Number(selectedMethod.minimumAmount).toLocaleString() }} – {{ authUser.user?.currencySymbol }}{{ Number(selectedMethod.maximumAmount).toLocaleString() }}</span>
          </div>
          <div class="flex justify-between text-gray-500 dark:text-gray-400">
            <span>Processing time</span>
            <span class="text-gray-900 dark:text-white font-medium">{{ selectedMethod.durationNote }}</span>
          </div>

          <template v-if="selectedMethod.methodType === 'crypto'">
            <div class="pt-2 border-t border-gray-200 dark:border-gray-700">
              <div class="text-gray-500 dark:text-gray-400 mb-1">Send {{ selectedMethod.name }} ({{ selectedMethod.network }}) to:</div>
              <div class="flex items-center gap-2">
                <code class="flex-1 break-all text-gray-900 dark:text-white bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2">{{ selectedMethod.walletAddress }}</code>
                <button type="button" class="p-2 text-gray-500 hover:text-blue-600" @click="copyAddress">
                  <Check v-if="copied" class="w-4 h-4 text-green-500" /> <Copy v-else class="w-4 h-4" />
                </button>
              </div>
            </div>
          </template>
          <template v-else-if="selectedMethod.methodType === 'bank'">
            <div v-if="selectedMethod.bankName || selectedMethod.accountName || selectedMethod.accountNumber || selectedMethod.swiftCode"
              class="pt-2 border-t border-gray-200 dark:border-gray-700 space-y-1">
              <div v-if="selectedMethod.bankName" class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">Bank Name</span><span class="text-gray-900 dark:text-white font-medium">{{ selectedMethod.bankName }}</span></div>
              <div v-if="selectedMethod.accountName" class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">Account Name</span><span class="text-gray-900 dark:text-white font-medium">{{ selectedMethod.accountName }}</span></div>
              <div v-if="selectedMethod.accountNumber" class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">Account Number</span><span class="text-gray-900 dark:text-white font-medium">{{ selectedMethod.accountNumber }}</span></div>
              <div v-if="selectedMethod.swiftCode" class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">Swift Code</span><span class="text-gray-900 dark:text-white font-medium">{{ selectedMethod.swiftCode }}</span></div>
            </div>
            <div v-else class="pt-2 border-t border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400">
              Bank account details for {{ selectedMethod.name }} haven't been set up yet — please check with support.
            </div>
          </template>
          <template v-else-if="selectedMethod.methodType === 'mail'">
            <div v-if="selectedMethod.walletAddress" class="pt-2 border-t border-gray-200 dark:border-gray-700">
              <div class="text-gray-500 dark:text-gray-400 mb-1">Mail your check to:</div>
              <div class="text-gray-900 dark:text-white font-medium whitespace-pre-line">{{ selectedMethod.walletAddress }}</div>
            </div>
            <div v-else class="pt-2 border-t border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400">
              A mailing address for {{ selectedMethod.name }} hasn't been set up yet — please check with support.
            </div>
          </template>
        </div>

        <!-- Amount + proof -->
        <form class="space-y-4" @submit.prevent="submit">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Amount</label>
              <input v-model="amount" type="number" step="0.01" min="0" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
            </div>
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Transaction Reference (optional)</label>
              <input v-model="txnId" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Proof of Payment</label>
            <label class="flex flex-col items-center justify-center gap-2 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg p-6 cursor-pointer hover:border-blue-400">
              <Upload class="w-6 h-6 text-gray-400" />
              <span class="text-sm text-gray-500 dark:text-gray-400">{{ proofFile?.name || 'Click to upload receipt/screenshot (JPG, PNG, PDF, DOC)' }}</span>
              <input type="file" accept="image/jpeg,image/png,image/jpg,.pdf,.doc,.docx" class="hidden" @change="onFileChange">
            </label>
          </div>
          <button type="submit" :disabled="submitting" class="w-full px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50">
            {{ submitting ? 'Submitting…' : 'Submit Deposit Request' }}
          </button>
        </form>
      </div>
    </template>
  </div>
</template>
