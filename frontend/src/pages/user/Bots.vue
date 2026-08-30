<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Bot, TrendingUp, Users, ChevronDown, XCircle } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const tab = ref('browse')
const loading = ref(true)
const bots = ref([])
const dashboard = ref(null)
const busyId = ref(null)
const expandedId = ref(null)
const history = ref([])
const historyLoading = ref(false)

async function loadBots() {
  const { data } = await api.get('/bots')
  bots.value = data
}
async function loadDashboard() {
  const { data } = await api.get('/bots/dashboard')
  dashboard.value = data
}

onMounted(async () => {
  try {
    await Promise.all([loadBots(), loadDashboard()])
  } finally {
    loading.value = false
  }
})

async function invest(bot) {
  const { value: amountStr, isConfirmed } = await Swal.fire({
    icon: 'question', title: `Invest in ${bot.name}`,
    html: `Range: <strong>${authUser.user?.currencySymbol}${Number(bot.minInvestment).toLocaleString()}</strong> – <strong>${authUser.user?.currencySymbol}${Number(bot.maxInvestment).toLocaleString()}</strong><br>Daily profit: ${bot.dailyProfitMin}% – ${bot.dailyProfitMax}% · ${bot.durationDays} days`,
    input: 'number', inputValue: bot.minInvestment,
    inputAttributes: { min: bot.minInvestment, max: bot.maxInvestment, step: '0.01' },
    showCancelButton: true, confirmButtonText: 'Invest Now',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!isConfirmed || !amountStr) return
  busyId.value = bot.id
  try {
    await api.post(`/bots/${bot.id}/invest`, { amount: Number(amountStr), autoReinvest: false })
    await authUser.fetchProfile()
    await Promise.all([loadBots(), loadDashboard()])
    tab.value = 'my'
    Swal.fire({ icon: 'success', title: `Invested in ${bot.name}!`, background: '#1F2937', color: '#E5E7EB', timer: 1800, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function cancelInvestment(inv) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Cancel investment in ${inv.botName}?`,
    text: 'Your current balance for this bot will be credited back to your account.',
    showCancelButton: true, confirmButtonText: 'Cancel Investment', confirmButtonColor: '#DC2626',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!confirm.isConfirmed) return
  busyId.value = inv.id
  try {
    await api.post(`/bots/investments/${inv.id}/cancel`)
    await authUser.fetchProfile()
    await Promise.all([loadBots(), loadDashboard()])
    Swal.fire({ icon: 'success', title: 'Cancelled', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function toggleExpand(inv) {
  if (expandedId.value === inv.id) {
    expandedId.value = null
    return
  }
  expandedId.value = inv.id
  historyLoading.value = true
  try {
    const { data } = await api.get(`/bots/investments/${inv.id}/history`)
    history.value = data
  } finally {
    historyLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between flex-wrap gap-4">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Bot class="w-6 h-6 text-blue-500" /> AI Trading Bots
      </h1>
      <div class="flex rounded-lg border border-gray-200 dark:border-gray-800 overflow-hidden">
        <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'browse' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'browse'">Browse Bots</button>
        <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'my' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'my'">My Investments</button>
      </div>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else-if="tab === 'browse'">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="b in bots" :key="b.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6 flex flex-col">
          <div class="flex items-center justify-between mb-2">
            <h3 class="font-bold text-gray-900 dark:text-white">{{ b.name }}</h3>
            <span class="px-2 py-1 text-xs font-semibold rounded-full bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-300 capitalize">{{ b.botType }}</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 mb-4 line-clamp-3">{{ b.description }}</p>
          <div class="grid grid-cols-2 gap-3 text-sm mb-4">
            <div><div class="text-gray-500 dark:text-gray-400">Daily Profit</div><div class="font-semibold text-emerald-600 dark:text-emerald-400 flex items-center gap-1"><TrendingUp class="w-3 h-3" />{{ b.dailyProfitMin }}%–{{ b.dailyProfitMax }}%</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Success Rate</div><div class="font-semibold text-gray-900 dark:text-white">{{ b.successRate }}%</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Duration</div><div class="font-semibold text-gray-900 dark:text-white">{{ b.durationDays }} days</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Investors</div><div class="font-semibold text-gray-900 dark:text-white flex items-center gap-1"><Users class="w-3 h-3" />{{ b.totalUsers }}</div></div>
          </div>
          <div class="text-xs text-gray-500 dark:text-gray-400 mb-4">
            Range: {{ authUser.user?.currencySymbol }}{{ Number(b.minInvestment).toLocaleString() }} – {{ authUser.user?.currencySymbol }}{{ Number(b.maxInvestment).toLocaleString() }}
          </div>
          <button type="button" :disabled="b.alreadyInvested || busyId === b.id"
            class="mt-auto w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50 disabled:bg-gray-400"
            @click="invest(b)">
            {{ b.alreadyInvested ? 'Already Invested' : (busyId === b.id ? 'Processing…' : 'Invest Now') }}
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <div v-if="dashboard" class="grid grid-cols-2 sm:grid-cols-5 gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Active Bots</div>
          <div class="text-xl font-bold text-gray-900 dark:text-white">{{ dashboard.activeBots }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Total Invested</div>
          <div class="text-xl font-bold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(dashboard.totalInvested).toLocaleString() }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Current Balance</div>
          <div class="text-xl font-bold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(dashboard.currentBalance).toLocaleString() }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Total Profit</div>
          <div class="text-xl font-bold text-emerald-600 dark:text-emerald-400">{{ authUser.user?.currencySymbol }}{{ Number(dashboard.totalProfit).toLocaleString() }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Total Loss</div>
          <div class="text-xl font-bold text-red-600 dark:text-red-400">{{ authUser.user?.currencySymbol }}{{ Number(dashboard.totalLoss).toLocaleString() }}</div>
        </div>
      </div>

      <div v-if="!dashboard?.investments?.length" class="text-gray-500 dark:text-gray-400">You haven't invested in any bots yet.</div>
      <div v-else class="space-y-3">
        <div v-for="inv in dashboard.investments" :key="inv.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
          <button type="button" class="w-full flex items-center justify-between p-4" @click="toggleExpand(inv)">
            <div class="text-left">
              <div class="font-medium text-gray-900 dark:text-white">{{ inv.botName }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">Invested {{ authUser.user?.currencySymbol }}{{ Number(inv.investmentAmount).toLocaleString() }} · Balance {{ authUser.user?.currencySymbol }}{{ Number(inv.currentBalance).toLocaleString() }}</div>
            </div>
            <div class="flex items-center gap-3">
              <span class="px-2 py-1 rounded-full text-xs font-medium capitalize" :class="inv.status === 'active' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300' : 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'">{{ inv.status }}</span>
              <ChevronDown class="w-4 h-4 text-gray-400 transition-transform" :class="expandedId === inv.id ? 'rotate-180' : ''" />
            </div>
          </button>
          <div v-if="expandedId === inv.id" class="border-t border-gray-100 dark:border-gray-800 p-4 space-y-4">
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
              <div><div class="text-gray-500 dark:text-gray-400">Winning Trades</div><div class="font-semibold text-gray-900 dark:text-white">{{ inv.successfulTrades }}</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Losing Trades</div><div class="font-semibold text-gray-900 dark:text-white">{{ inv.failedTrades }}</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Started</div><div class="font-semibold text-gray-900 dark:text-white">{{ new Date(inv.startedAt).toLocaleDateString() }}</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Expires</div><div class="font-semibold text-gray-900 dark:text-white">{{ inv.expiresAt ? new Date(inv.expiresAt).toLocaleDateString() : '—' }}</div></div>
            </div>
            <div v-if="historyLoading" class="text-sm text-gray-500 dark:text-gray-400">Loading history…</div>
            <div v-else-if="history.length === 0" class="text-sm text-gray-500 dark:text-gray-400">No trades recorded yet.</div>
            <ul v-else class="divide-y divide-gray-100 dark:divide-gray-800">
              <li v-for="t in history" :key="t.id" class="py-2 flex items-center justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-300">{{ t.tradingPair }} · {{ t.tradeType }}</span>
                <span class="font-medium" :class="Number(t.profitLoss) >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">{{ Number(t.profitLoss) >= 0 ? '+' : '' }}{{ authUser.user?.currencySymbol }}{{ t.profitLoss }}</span>
              </li>
            </ul>
            <button v-if="inv.status === 'active'" type="button" :disabled="busyId === inv.id"
              class="flex items-center gap-2 px-4 py-2 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm font-medium rounded-lg disabled:opacity-50"
              @click="cancelInvestment(inv)">
              <XCircle class="w-4 h-4" /> Cancel Investment
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
