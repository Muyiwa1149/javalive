<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Users2, Star, TrendingUp, TrendingDown, ChevronDown, XCircle } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const tab = ref('dashboard')
const loading = ref(true)
const dashboard = ref(null)
const experts = ref([])
const busyId = ref(null)
const expandedId = ref(null)
const activity = ref([])
const activityLoading = ref(false)

async function loadDashboard() {
  const { data } = await api.get('/copy-trading/dashboard')
  dashboard.value = data
}
async function loadExperts() {
  const { data } = await api.get('/copy-trading/experts')
  experts.value = data
}

onMounted(async () => {
  try {
    await Promise.all([loadDashboard(), loadExperts()])
  } finally {
    loading.value = false
  }
})

async function startCopying(expert) {
  const { value: amountStr, isConfirmed } = await Swal.fire({
    icon: 'question', title: `Copy ${expert.name}`,
    html: `Minimum investment: <strong>${authUser.user?.currencySymbol}${Number(expert.price).toLocaleString()}</strong>`,
    input: 'number', inputValue: expert.price,
    inputAttributes: { min: expert.price, step: '0.01' },
    showCancelButton: true, confirmButtonText: 'Start Copying',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!isConfirmed || !amountStr) return
  busyId.value = expert.id
  try {
    await api.post('/copy-trading/start', { expertId: expert.id, amount: Number(amountStr) })
    await authUser.fetchProfile()
    await Promise.all([loadDashboard(), loadExperts()])
    tab.value = 'dashboard'
    Swal.fire({ icon: 'success', title: `Now copying ${expert.name}!`, background: '#1F2937', color: '#E5E7EB', timer: 1800, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function stopCopying(copyTrade) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Stop copying ${copyTrade.expertName}?`,
    text: 'Your current balance for this copy position will be credited back to your account.',
    showCancelButton: true, confirmButtonText: 'Stop', confirmButtonColor: '#DC2626',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!confirm.isConfirmed) return
  busyId.value = copyTrade.id
  try {
    await api.post(`/copy-trading/${copyTrade.id}/stop`)
    await authUser.fetchProfile()
    await Promise.all([loadDashboard(), loadExperts()])
    Swal.fire({ icon: 'success', title: 'Stopped', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function toggleExpand(copyTrade) {
  if (expandedId.value === copyTrade.id) {
    expandedId.value = null
    return
  }
  expandedId.value = copyTrade.id
  activityLoading.value = true
  try {
    const { data } = await api.get(`/copy-trading/${copyTrade.id}/activity`)
    activity.value = data
  } finally {
    activityLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between flex-wrap gap-4">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Users2 class="w-6 h-6 text-blue-500" /> Copy Trading
      </h1>
      <div class="flex rounded-lg border border-gray-200 dark:border-gray-800 overflow-hidden">
        <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'dashboard' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'dashboard'">My Copies</button>
        <button type="button" class="px-4 py-2 text-sm font-medium" :class="tab === 'experts' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-900 text-gray-600 dark:text-gray-300'" @click="tab = 'experts'">Browse Experts</button>
      </div>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else-if="tab === 'dashboard'">
      <div v-if="dashboard" class="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
          <div class="text-xs text-gray-500 dark:text-gray-400">Active Copies</div>
          <div class="text-xl font-bold text-gray-900 dark:text-white">{{ dashboard.activeCopies }}</div>
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
          <div class="text-xl font-bold" :class="Number(dashboard.totalProfit) >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">{{ authUser.user?.currencySymbol }}{{ Number(dashboard.totalProfit).toLocaleString() }}</div>
        </div>
      </div>

      <div v-if="!dashboard?.copyTrades?.length" class="text-gray-500 dark:text-gray-400">You're not copying any experts yet.</div>
      <div v-else class="space-y-3">
        <div v-for="c in dashboard.copyTrades" :key="c.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
          <button type="button" class="w-full flex items-center justify-between p-4" @click="toggleExpand(c)">
            <div class="text-left">
              <div class="font-medium text-gray-900 dark:text-white">{{ c.expertName }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">Invested {{ authUser.user?.currencySymbol }}{{ Number(c.price).toLocaleString() }} · Balance {{ authUser.user?.currencySymbol }}{{ Number(c.currentBalance).toLocaleString() }}</div>
            </div>
            <div class="flex items-center gap-3">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="c.active === 'yes' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300' : 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'">{{ c.active === 'yes' ? 'Active' : 'Stopped' }}</span>
              <ChevronDown class="w-4 h-4 text-gray-400 transition-transform" :class="expandedId === c.id ? 'rotate-180' : ''" />
            </div>
          </button>
          <div v-if="expandedId === c.id" class="border-t border-gray-100 dark:border-gray-800 p-4 space-y-4">
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
              <div><div class="text-gray-500 dark:text-gray-400">Total Trades</div><div class="font-semibold text-gray-900 dark:text-white">{{ c.totalTrades }}</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Winning Trades</div><div class="font-semibold text-gray-900 dark:text-white">{{ c.winningTrades }}</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Profit %</div><div class="font-semibold text-gray-900 dark:text-white">{{ c.profitPercentage }}%</div></div>
              <div><div class="text-gray-500 dark:text-gray-400">Started</div><div class="font-semibold text-gray-900 dark:text-white">{{ new Date(c.startedAt).toLocaleDateString() }}</div></div>
            </div>
            <div v-if="activityLoading" class="text-sm text-gray-500 dark:text-gray-400">Loading activity…</div>
            <ul v-else class="divide-y divide-gray-100 dark:divide-gray-800">
              <li v-for="t in activity" :key="t.id" class="py-2 flex items-center justify-between text-sm">
                <span class="flex items-center gap-2 text-gray-600 dark:text-gray-300">
                  <component :is="Number(t.profitLoss) >= 0 ? TrendingUp : TrendingDown" class="w-4 h-4" :class="Number(t.profitLoss) >= 0 ? 'text-emerald-500' : 'text-red-500'" />
                  {{ t.pair }} · {{ new Date(t.createdAt).toLocaleDateString() }}
                </span>
                <span class="font-medium" :class="Number(t.profitLoss) >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'">{{ Number(t.profitLoss) >= 0 ? '+' : '' }}{{ authUser.user?.currencySymbol }}{{ t.profitLoss }}</span>
              </li>
            </ul>
            <button v-if="c.active === 'yes'" type="button" :disabled="busyId === c.id"
              class="flex items-center gap-2 px-4 py-2 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm font-medium rounded-lg disabled:opacity-50"
              @click="stopCopying(c)">
              <XCircle class="w-4 h-4" /> Stop Copying
            </button>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="e in experts" :key="e.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6 flex flex-col">
          <div class="flex items-center justify-between mb-2">
            <h3 class="font-bold text-gray-900 dark:text-white">{{ e.name }}</h3>
            <span v-if="e.tag" class="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">{{ e.tag }}</span>
          </div>
          <div class="flex items-center gap-1 text-amber-500 mb-3">
            <Star v-for="n in e.rating" :key="n" class="w-4 h-4 fill-current" />
          </div>
          <p v-if="e.description" class="text-sm text-gray-500 dark:text-gray-400 mb-4 line-clamp-3">{{ e.description }}</p>
          <div class="grid grid-cols-2 gap-3 text-sm mb-4">
            <div><div class="text-gray-500 dark:text-gray-400">Win Rate</div><div class="font-semibold text-emerald-600 dark:text-emerald-400">{{ e.winRate }}%</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Followers</div><div class="font-semibold text-gray-900 dark:text-white">{{ e.followers?.toLocaleString() }}</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Total Trades</div><div class="font-semibold text-gray-900 dark:text-white">{{ e.totalTrades }}</div></div>
            <div><div class="text-gray-500 dark:text-gray-400">Min. Copy</div><div class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(e.price).toLocaleString() }}</div></div>
          </div>
          <button type="button" :disabled="e.alreadyCopying || busyId === e.id"
            class="mt-auto w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50 disabled:bg-gray-400"
            @click="startCopying(e)">
            {{ e.alreadyCopying ? 'Already Copying' : (busyId === e.id ? 'Processing…' : 'Copy This Trader') }}
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
