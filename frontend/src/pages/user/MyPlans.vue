<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { PieChart, ChevronDown, XCircle, Wallet } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const filter = ref('All')
const investments = ref([])
const expandedId = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const busyId = ref(null)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/investments', { params: filter.value !== 'All' ? { active: filter.value } : {} })
    investments.value = data
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function toggleExpand(inv) {
  if (expandedId.value === inv.id) {
    expandedId.value = null
    detail.value = null
    return
  }
  expandedId.value = inv.id
  detailLoading.value = true
  try {
    const { data } = await api.get(`/investments/${inv.id}`)
    detail.value = data
  } finally {
    detailLoading.value = false
  }
}

async function cancelPlan(inv) {
  const confirm = await Swal.fire({
    icon: 'warning', title: 'Cancel this plan?',
    text: 'Your invested capital will be credited back to your account balance.',
    showCancelButton: true, confirmButtonText: 'Cancel Plan', confirmButtonColor: '#DC2626',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!confirm.isConfirmed) return
  busyId.value = inv.id
  try {
    await api.post(`/investments/${inv.id}/cancel`)
    await authUser.fetchProfile()
    await load()
    Swal.fire({ icon: 'success', title: 'Plan cancelled', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function withdrawProfit(inv) {
  const { value: amountStr, isConfirmed } = await Swal.fire({
    icon: 'question', title: 'Withdraw Profit to Wallet',
    html: `Available profit: <strong>${authUser.user?.currencySymbol}${Number(inv.availableProfit).toLocaleString(undefined, { minimumFractionDigits: 2 })}</strong>`,
    input: 'number',
    inputAttributes: { min: 0.01, max: inv.availableProfit, step: '0.01' },
    showCancelButton: true, confirmButtonText: 'Withdraw',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!isConfirmed || !amountStr) return
  busyId.value = inv.id
  try {
    await api.post(`/investments/${inv.id}/withdraw-profit`, { amount: Number(amountStr) })
    await authUser.fetchProfile()
    await load()
    if (expandedId.value === inv.id) {
      const { data } = await api.get(`/investments/${inv.id}`)
      detail.value = data
    }
    Swal.fire({ icon: 'success', title: 'Profit moved to wallet', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

const statusClass = (active) => ({
  yes: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  cancelled: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
}[active] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <PieChart class="w-6 h-6 text-blue-500" /> My Portfolio
      </h1>
      <select v-model="filter" class="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" @change="load">
        <option value="All">All</option>
        <option value="yes">Active</option>
        <option value="cancelled">Cancelled</option>
      </select>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="investments.length === 0" class="text-gray-500 dark:text-gray-400">You haven't invested in any plans yet.</div>

    <div v-else class="space-y-3">
      <div v-for="inv in investments" :key="inv.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden">
        <button type="button" class="w-full flex items-center justify-between p-4" @click="toggleExpand(inv)">
          <div class="text-left">
            <div class="font-medium text-gray-900 dark:text-white">{{ inv.planName }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400">Invested {{ authUser.user?.currencySymbol }}{{ Number(inv.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }} · {{ new Date(inv.activatedAt).toLocaleDateString() }}</div>
          </div>
          <div class="flex items-center gap-3">
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(inv.active)">{{ inv.active === 'yes' ? 'Active' : (inv.active === 'cancelled' ? 'Cancelled' : inv.active) }}</span>
            <ChevronDown class="w-4 h-4 text-gray-400 transition-transform" :class="expandedId === inv.id ? 'rotate-180' : ''" />
          </div>
        </button>

        <div v-if="expandedId === inv.id" class="border-t border-gray-100 dark:border-gray-800 p-4 space-y-4">
          <div v-if="detailLoading" class="text-sm text-gray-500 dark:text-gray-400">Loading…</div>
          <template v-else>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
              <div>
                <div class="text-gray-500 dark:text-gray-400">Profit Earned</div>
                <div class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(inv.profitEarned).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
              </div>
              <div>
                <div class="text-gray-500 dark:text-gray-400">Withdrawn</div>
                <div class="font-semibold text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(inv.profitWithdrawn).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
              </div>
              <div>
                <div class="text-gray-500 dark:text-gray-400">Available</div>
                <div class="font-semibold text-emerald-600 dark:text-emerald-400">{{ authUser.user?.currencySymbol }}{{ Number(inv.availableProfit).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
              </div>
              <div>
                <div class="text-gray-500 dark:text-gray-400">Expires</div>
                <div class="font-semibold text-gray-900 dark:text-white">{{ inv.expireDate ? new Date(inv.expireDate).toLocaleDateString() : '—' }}</div>
              </div>
            </div>

            <div v-if="inv.active === 'yes'" class="flex flex-wrap gap-3">
              <button type="button" :disabled="busyId === inv.id || inv.withdrawalDisabled || Number(inv.availableProfit) <= 0"
                class="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg disabled:opacity-50"
                @click="withdrawProfit(inv)">
                <Wallet class="w-4 h-4" /> Withdraw Profit
              </button>
              <button type="button" :disabled="busyId === inv.id"
                class="flex items-center gap-2 px-4 py-2 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm font-medium rounded-lg disabled:opacity-50"
                @click="cancelPlan(inv)">
                <XCircle class="w-4 h-4" /> Cancel Plan
              </button>
            </div>
            <p v-if="inv.withdrawalDisabled" class="text-xs text-amber-600 dark:text-amber-400">Profit withdrawal has been disabled for this investment. Please contact support.</p>

            <div v-if="detail?.transactions?.length">
              <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-2">Transaction History</h3>
              <ul class="divide-y divide-gray-100 dark:divide-gray-800">
                <li v-for="t in detail.transactions" :key="t.id" class="py-2 flex items-center justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-300">{{ t.planLabel || t.type }} · {{ new Date(t.createdAt).toLocaleDateString() }}</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ authUser.user?.currencySymbol }}{{ Number(t.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
                </li>
              </ul>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
