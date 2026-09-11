<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Swal from 'sweetalert2'
import { CandlestickChart, Search, TrendingUp, TrendingDown, ArrowLeft, X } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const instruments = ref([])
const query = ref('')
const typeFilter = ref('all')
const selected = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const placing = ref(false)
const monitorTrade = ref(null)
const monitorLoading = ref(false)
let searchDebounce = null

const form = ref({ amount: '', orderType: 'Buy', leverage: 10, expire: '1 hours' })

const TYPES = ['all', 'crypto', 'forex', 'stock', 'commodity', 'bond']
const EXPIRIES = ['30 minutes', '1 hours', '4 hours', '1 days', '3 days', '7 days', '1 months']

async function loadInstruments() {
  loading.value = true
  try {
    const params = {}
    if (typeFilter.value !== 'all') params.type = typeFilter.value
    const url = query.value.trim() ? '/trade/search' : '/trade/instruments'
    if (query.value.trim()) params.q = query.value.trim()
    const { data } = await api.get(url, { params })
    instruments.value = data
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(loadInstruments, 300)
}

onMounted(loadInstruments)
onBeforeUnmount(() => clearTimeout(searchDebounce))

async function selectInstrument(instrument) {
  selected.value = instrument
  monitorTrade.value = null
  detailLoading.value = true
  try {
    const { data } = await api.get(`/trade/instruments/${instrument.id}`)
    detail.value = data
  } finally {
    detailLoading.value = false
  }
}

function backToMarkets() {
  selected.value = null
  detail.value = null
  monitorTrade.value = null
}

async function placeTrade() {
  if (!form.value.amount || Number(form.value.amount) <= 0) {
    Swal.fire({ icon: 'warning', title: 'Enter an amount', background: '#1F2937', color: '#E5E7EB' })
    return
  }
  placing.value = true
  try {
    await api.post('/trade', {
      assetSymbol: selected.value.symbol,
      amount: Number(form.value.amount),
      orderType: form.value.orderType,
      leverage: Number(form.value.leverage),
      expire: form.value.expire,
    })
    await authUser.fetchProfile()
    const { data } = await api.get(`/trade/instruments/${selected.value.id}`)
    detail.value = data
    form.value.amount = ''
    Swal.fire({ icon: 'success', title: 'Trade placed!', background: '#1F2937', color: '#E5E7EB', timer: 1800, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Trade failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    placing.value = false
  }
}

async function viewMonitor(trade) {
  monitorLoading.value = true
  monitorTrade.value = { id: trade.id }
  try {
    const { data } = await api.get(`/trade/${trade.id}/monitor`)
    monitorTrade.value = data
  } finally {
    monitorLoading.value = false
  }
}

const sym = computed(() => authUser.user?.currencySymbol || '$')
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <CandlestickChart class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Live Markets
    </h1>

    <!-- Markets list view -->
    <template v-if="!selected">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <Search class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input v-model="query" type="text" placeholder="Search instruments…" class="w-full pl-9 pr-3 py-2 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm" @input="onSearchInput">
        </div>
        <select v-model="typeFilter" class="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-900 px-3 py-2 text-sm text-gray-900 dark:text-white capitalize" @change="loadInstruments">
          <option v-for="t in TYPES" :key="t" :value="t">{{ t === 'all' ? 'All Types' : t }}</option>
        </select>
      </div>

      <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
      <div v-else-if="instruments.length === 0" class="text-gray-500 dark:text-gray-400">No instruments found.</div>
      <div v-else class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
            <tr><th class="px-4 py-3">Symbol</th><th class="px-4 py-3">Type</th><th class="px-4 py-3 text-right">Price</th><th class="px-4 py-3 text-right">24h Change</th></tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            <tr v-for="i in instruments" :key="i.id" class="hover:bg-gray-50 dark:hover:bg-gray-800/50 cursor-pointer" @click="selectInstrument(i)">
              <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">{{ i.symbol }} <span class="text-gray-400 font-normal">{{ i.name }}</span></td>
              <td class="px-4 py-3 text-gray-500 dark:text-gray-400 capitalize">{{ i.type }}</td>
              <td class="px-4 py-3 text-right text-gray-900 dark:text-white">{{ Number(i.price).toLocaleString(undefined, { maximumFractionDigits: 6 }) }}</td>
              <td class="px-4 py-3 text-right font-medium" :class="Number(i.percentChange24h) >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">
                {{ Number(i.percentChange24h) >= 0 ? '+' : '' }}{{ i.percentChange24h }}%
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- Instrument detail / trade view -->
    <template v-else>
      <button type="button" class="flex items-center gap-2 text-sm text-blue-600 dark:text-blue-400" @click="backToMarkets">
        <ArrowLeft class="w-4 h-4" /> Back to Markets
      </button>

      <div v-if="detailLoading" class="text-gray-500 dark:text-gray-400">Loading…</div>
      <template v-else-if="detail">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 space-y-6">
            <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
              <div class="flex items-center justify-between">
                <div>
                  <h2 class="text-xl font-bold text-gray-900 dark:text-white">{{ selected.symbol }}</h2>
                  <p class="text-gray-500 dark:text-gray-400">{{ selected.name }}</p>
                </div>
                <div class="text-right">
                  <div class="text-2xl font-bold text-gray-900 dark:text-white">{{ Number(selected.price).toLocaleString(undefined, { maximumFractionDigits: 6 }) }}</div>
                  <div class="font-medium" :class="Number(selected.percentChange24h) >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">
                    {{ Number(selected.percentChange24h) >= 0 ? '+' : '' }}{{ selected.percentChange24h }}%
                  </div>
                </div>
              </div>
            </div>

            <div>
              <h3 class="font-semibold text-gray-900 dark:text-white mb-3">Open Trades</h3>
              <div v-if="detail.openTrades.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No open trades on this instrument.</div>
              <ul v-else class="space-y-2">
                <li v-for="t in detail.openTrades" :key="t.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg p-3 flex items-center justify-between cursor-pointer hover:border-blue-400" @click="viewMonitor(t)">
                  <div class="flex items-center gap-2">
                    <component :is="t.type === 'Buy' ? TrendingUp : TrendingDown" class="w-4 h-4" :class="t.type === 'Buy' ? 'text-emerald-500' : 'text-red-500'" />
                    <span class="text-sm text-gray-900 dark:text-white">{{ t.type }} · {{ t.leverage }}x · {{ sym }}{{ Number(t.amount).toLocaleString() }}</span>
                  </div>
                  <span class="text-xs text-gray-500 dark:text-gray-400">{{ t.invDuration }}</span>
                </li>
              </ul>
            </div>

            <div v-if="detail.closedTrades.length">
              <h3 class="font-semibold text-gray-900 dark:text-white mb-3">Closed Trades</h3>
              <ul class="space-y-2">
                <li v-for="t in detail.closedTrades" :key="t.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-lg p-3 flex items-center justify-between cursor-pointer hover:border-blue-400" @click="viewMonitor(t)">
                  <span class="text-sm text-gray-900 dark:text-white">{{ t.type }} · {{ t.leverage }}x · {{ sym }}{{ Number(t.amount).toLocaleString() }}</span>
                  <span class="text-xs text-gray-500 dark:text-gray-400">Closed</span>
                </li>
              </ul>
            </div>

            <!-- Trade monitor panel -->
            <div v-if="monitorTrade" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 relative">
              <button type="button" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600" @click="monitorTrade = null"><X class="w-4 h-4" /></button>
              <h3 class="font-semibold text-gray-900 dark:text-white mb-4">Trade Monitor</h3>
              <div v-if="monitorLoading" class="text-sm text-gray-500 dark:text-gray-400">Loading…</div>
              <template v-else-if="monitorTrade.pnl">
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm mb-4">
                  <div><div class="text-gray-500 dark:text-gray-400">Current Value</div><div class="font-semibold text-gray-900 dark:text-white">{{ sym }}{{ Number(monitorTrade.pnl.currentValue).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div></div>
                  <div><div class="text-gray-500 dark:text-gray-400">P&amp;L</div><div class="font-semibold" :class="monitorTrade.pnl.isProfit ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">{{ monitorTrade.pnl.isProfit ? '+' : '' }}{{ sym }}{{ Number(monitorTrade.pnl.profitLoss).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div></div>
                  <div><div class="text-gray-500 dark:text-gray-400">Return</div><div class="font-semibold" :class="monitorTrade.pnl.isProfit ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">{{ monitorTrade.pnl.returnPercentage }}%</div></div>
                  <div><div class="text-gray-500 dark:text-gray-400">Time Left</div><div class="font-semibold text-gray-900 dark:text-white">{{ monitorTrade.timeLeft }}</div></div>
                </div>
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs text-gray-500 dark:text-gray-400 border-t border-gray-100 dark:border-gray-800 pt-4">
                  <div>Total trades on {{ selected.symbol }}: <span class="font-medium text-gray-900 dark:text-white">{{ monitorTrade.stats.totalTrades }}</span></div>
                  <div>Active: <span class="font-medium text-gray-900 dark:text-white">{{ monitorTrade.stats.activeTrades }}</span></div>
                  <div>Completed: <span class="font-medium text-gray-900 dark:text-white">{{ monitorTrade.stats.completedTrades }}</span></div>
                  <div>Avg size: <span class="font-medium text-gray-900 dark:text-white">{{ sym }}{{ Number(monitorTrade.stats.avgTradeSize).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span></div>
                </div>
              </template>
            </div>
          </div>

          <!-- Place trade form -->
          <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 h-fit space-y-4">
            <h3 class="font-semibold text-gray-900 dark:text-white">Place Trade</h3>
            <div class="grid grid-cols-2 gap-2">
              <button type="button" class="py-2 rounded-lg text-sm font-medium border" :class="form.orderType === 'Buy' ? 'bg-emerald-600 text-white border-emerald-600' : 'border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300'" @click="form.orderType = 'Buy'">Buy</button>
              <button type="button" class="py-2 rounded-lg text-sm font-medium border" :class="form.orderType === 'Sell' ? 'bg-red-600 text-white border-red-600' : 'border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300'" @click="form.orderType = 'Sell'">Sell</button>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Amount</label>
              <input v-model="form.amount" type="number" min="0" step="0.01" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="100.00">
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Leverage: {{ form.leverage }}x</label>
              <input v-model.number="form.leverage" type="range" min="1" max="100" class="w-full">
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Duration</label>
              <select v-model="form.expire" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
                <option v-for="e in EXPIRIES" :key="e" :value="e">{{ e }}</option>
              </select>
            </div>
            <button type="button" :disabled="placing"
              class="w-full py-3 rounded-lg font-medium text-white disabled:opacity-50"
              :class="form.orderType === 'Buy' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-red-600 hover:bg-red-700'"
              @click="placeTrade">
              {{ placing ? 'Placing…' : `${form.orderType} ${selected.symbol}` }}
            </button>
            <p class="text-xs text-gray-400 dark:text-gray-500">Balance: {{ sym }}{{ Number(authUser.user?.accountBalance ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</p>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>
