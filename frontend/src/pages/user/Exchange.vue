<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Swal from 'sweetalert2'
import { Repeat, ArrowDownUp, Clock } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const unavailable = ref(false)
const balances = ref([])
const history = ref([])

const source = ref('usd')
const destination = ref('btc')
const amount = ref('')
const quote = ref(null)
const quoting = ref(false)
const submitting = ref(false)
let quoteDebounce = null

async function load() {
  loading.value = true
  try {
    const [b, h] = await Promise.all([api.get('/exchange/balances'), api.get('/exchange/history')])
    balances.value = b.data
    history.value = h.data
  } catch (e) {
    if (e.response?.status === 404) unavailable.value = true
  } finally {
    loading.value = false
  }
}
onMounted(load)

function balanceFor(currency) {
  return balances.value.find((b) => b.currency === currency)?.balance ?? 0
}

async function fetchQuote() {
  if (!amount.value || Number(amount.value) <= 0 || source.value === destination.value) {
    quote.value = null
    return
  }
  quoting.value = true
  try {
    const { data } = await api.get('/exchange/quote', { params: { source: source.value, destination: destination.value, amount: amount.value } })
    quote.value = data
  } catch {
    quote.value = null
  } finally {
    quoting.value = false
  }
}

watch([source, destination, amount], () => {
  clearTimeout(quoteDebounce)
  quoteDebounce = setTimeout(fetchQuote, 400)
})

function swapDirection() {
  const tmp = source.value
  source.value = destination.value
  destination.value = tmp
}

async function submitExchange() {
  if (!quote.value) return
  submitting.value = true
  try {
    await api.post('/exchange', { source: source.value, destination: destination.value, amount: Number(amount.value) })
    await authUser.fetchProfile()
    await load()
    amount.value = ''
    quote.value = null
    Swal.fire({ icon: 'success', title: 'Exchange completed!', background: '#1F2937', color: '#E5E7EB', timer: 1800, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Exchange failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}

const CURRENCIES = ['usd', 'btc', 'eth', 'usdt', 'ltc', 'xrp', 'link', 'bnb', 'aave', 'xlm', 'bch', 'ada']
const sym = computed(() => authUser.user?.currencySymbol || '$')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Repeat class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Currency Exchange
    </h1>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="unavailable" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">
      The currency exchange feature is not currently available.
    </div>

    <template v-else>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div v-for="b in balances" :key="b.currency" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-3">
          <div class="text-xs text-gray-500 dark:text-gray-400 uppercase">{{ b.currency }}</div>
          <div class="text-sm font-semibold text-gray-900 dark:text-white">{{ Number(b.balance).toLocaleString(undefined, { maximumFractionDigits: 6 }) }}</div>
          <div class="text-xs text-gray-400 dark:text-gray-500">{{ sym }}{{ Number(b.usdValue).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-[1fr_auto_1fr] gap-3 items-end">
          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">From</label>
            <select v-model="source" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white uppercase">
              <option v-for="c in CURRENCIES" :key="c" :value="c">{{ c.toUpperCase() }} — {{ Number(balanceFor(c)).toLocaleString(undefined, { maximumFractionDigits: 4 }) }}</option>
            </select>
          </div>
          <button type="button" class="p-2 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:text-blue-600 mx-auto" @click="swapDirection">
            <ArrowDownUp class="w-4 h-4" />
          </button>
          <div>
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">To</label>
            <select v-model="destination" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white uppercase">
              <option v-for="c in CURRENCIES" :key="c" :value="c">{{ c.toUpperCase() }}</option>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Amount ({{ source.toUpperCase() }})</label>
          <input v-model="amount" type="number" min="0" step="0.00000001" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="0.00">
        </div>

        <div v-if="quoting" class="text-sm text-gray-500 dark:text-gray-400">Getting quote…</div>
        <div v-else-if="quote" class="bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded-lg p-4 text-sm space-y-1">
          <div class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">You'll receive</span><span class="font-semibold text-gray-900 dark:text-white">{{ Number(quote.quantity).toLocaleString(undefined, { maximumFractionDigits: 8 }) }} {{ destination.toUpperCase() }}</span></div>
          <div class="flex justify-between"><span class="text-gray-500 dark:text-gray-400">Fee ({{ quote.feePercentage }}%)</span><span class="text-gray-700 dark:text-gray-300">{{ quote.fee }} {{ source.toUpperCase() }}</span></div>
        </div>

        <button type="button" :disabled="!quote || submitting" class="w-full py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium disabled:opacity-50" @click="submitExchange">
          {{ submitting ? 'Exchanging…' : 'Exchange' }}
        </button>
      </div>

      <div v-if="history.length">
        <h2 class="font-semibold text-gray-900 dark:text-white mb-3 flex items-center gap-2"><Clock class="w-4 h-4" /> Swap History</h2>
        <ul class="divide-y divide-gray-100 dark:divide-gray-800 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
          <li v-for="h in history" :key="h.id" class="p-3 flex items-center justify-between text-sm">
            <span class="text-gray-700 dark:text-gray-300">{{ h.source }} → {{ h.dest }}</span>
            <span class="text-gray-900 dark:text-white font-medium">{{ h.amount }} → {{ h.quantity }}</span>
            <span class="text-xs text-gray-400 dark:text-gray-500">{{ new Date(h.createdAt).toLocaleDateString() }}</span>
          </li>
        </ul>
      </div>
    </template>
  </div>
</template>
