<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { RouterLink } from 'vue-router'
import { Wallet, Briefcase, TrendingUp, TrendingDown, Clock, PieChart, ArrowRight, ChevronDown, XCircle, PlusCircle } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

// Rebuilt in Phase 7 to match source's myplans.blade.php card design exactly (stats row, filter,
// date-range + progress bar per active plan) — the earlier accordion-list version, while
// functionally complete, visually diverged too far from the original. Inline expand/withdraw-profit/
// cancel actions are kept (a real UX improvement over source's separate plandetails.blade.php route)
// since they don't change how the page looks, only what happens when "View Details" is clicked.
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

function money(value) {
  return `${authUser.user?.currencySymbol ?? '$'}${Number(value ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
function formatDate(value, opts) {
  return value ? new Date(value).toLocaleDateString(undefined, opts) : '—'
}

const stats = computed(() => {
  const list = investments.value
  return {
    total: list.length,
    active: list.filter((p) => p.active === 'yes').length,
    expired: list.filter((p) => p.active === 'expired').length,
    totalInvested: list.reduce((sum, p) => sum + Number(p.amount || 0), 0),
    totalProfit: list.reduce((sum, p) => sum + Number(p.profitEarned || 0), 0),
  }
})

const statusMeta = (active) => ({
  yes: { label: 'Active', dot: 'bg-green-400', class: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300' },
  expired: { label: 'Expired', dot: 'bg-red-400', class: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300' },
  cancelled: { label: 'Cancelled', dot: 'bg-gray-400', class: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300' },
}[active] || { label: active, dot: 'bg-yellow-400', class: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300' })

function progressOf(inv) {
  if (!inv.activatedAt || !inv.expireDate) return { pct: 0, totalDays: 0, remainingDays: 0 }
  const start = new Date(inv.activatedAt).getTime()
  const end = new Date(inv.expireDate).getTime()
  const now = Date.now()
  const totalDays = Math.max(0, Math.round((end - start) / 86400000))
  const elapsedDays = Math.max(0, Math.round((now - start) / 86400000))
  const pct = totalDays > 0 ? Math.min((elapsedDays / totalDays) * 100, 100) : 0
  const remainingDays = now < end ? Math.round((end - now) / 86400000) : 0
  return { pct, totalDays, remainingDays }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
    <div class="container mx-auto px-4 sm:px-6">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 mb-6 sm:mb-8">
        <div>
          <h1 class="text-xl sm:text-3xl font-bold text-gray-900 dark:text-white">My Investment Plans</h1>
          <p class="text-xs sm:text-base text-gray-600 dark:text-gray-400 mt-1 sm:mt-2">Track and manage your active investment portfolios</p>
        </div>
        <RouterLink :to="{ name: 'user.plans' }"
          class="self-start inline-flex items-center gap-1.5 sm:gap-2 px-3.5 py-2 sm:px-6 sm:py-3 text-sm sm:text-base bg-gradient-to-r from-blue-600 to-blue-600 hover:from-blue-700 hover:to-blue-700 text-white rounded-lg sm:rounded-xl font-medium transition-all duration-200 shadow-lg hover:shadow-xl">
          <PlusCircle class="w-4 h-4 sm:w-5 sm:h-5" /> New Investment
        </RouterLink>
      </div>

      <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

      <template v-else>
        <!-- Statistics + Filter -->
        <div v-if="investments.length > 0 || filter !== 'All'" class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 mb-8">
          <!-- Mobile: compact stat strip, no icons -->
          <div class="sm:hidden p-4">
            <div class="grid grid-cols-3 divide-x divide-gray-100 dark:divide-gray-700 text-center mb-3">
              <div>
                <div class="text-lg font-bold text-gray-900 dark:text-white">{{ stats.total }}</div>
                <div class="text-[11px] text-gray-500 dark:text-gray-400">Total</div>
              </div>
              <div>
                <div class="text-lg font-bold text-green-600">{{ stats.active }}</div>
                <div class="text-[11px] text-gray-500 dark:text-gray-400">Active</div>
              </div>
              <div>
                <div class="text-lg font-bold text-red-600">{{ stats.expired }}</div>
                <div class="text-[11px] text-gray-500 dark:text-gray-400">Expired</div>
              </div>
            </div>
            <div class="grid grid-cols-2 divide-x divide-gray-100 dark:divide-gray-700 text-center border-t border-gray-100 dark:border-gray-700 pt-3">
              <div>
                <div class="text-base font-bold text-gray-900 dark:text-white truncate">{{ money(stats.totalInvested) }}</div>
                <div class="text-[11px] text-gray-500 dark:text-gray-400">Total Invested</div>
              </div>
              <div>
                <div class="text-base font-bold text-gray-900 dark:text-white truncate">{{ money(stats.totalProfit) }}</div>
                <div class="text-[11px] text-gray-500 dark:text-gray-400">Total Profit</div>
              </div>
            </div>
          </div>

          <!-- Desktop: spacious icon cards -->
          <div class="hidden sm:block p-6">
            <div class="grid grid-cols-5 gap-6 mb-6">
              <div class="text-center">
                <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <Briefcase class="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div class="text-2xl font-bold text-gray-900 dark:text-white">{{ stats.total }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Total Plans</div>
              </div>
              <div class="text-center">
                <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <TrendingUp class="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <div class="text-2xl font-bold text-green-600">{{ stats.active }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Active</div>
              </div>
              <div class="text-center">
                <div class="w-12 h-12 bg-red-100 dark:bg-red-900/30 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <TrendingDown class="w-6 h-6 text-red-600 dark:text-red-400" />
                </div>
                <div class="text-2xl font-bold text-red-600">{{ stats.expired }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Expired</div>
              </div>
              <div class="text-center">
                <div class="w-12 h-12 bg-yellow-100 dark:bg-yellow-900/30 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <Clock class="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
                </div>
                <div class="text-2xl font-bold text-gray-900 dark:text-white">{{ money(stats.totalInvested) }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Total Invested</div>
              </div>
              <div class="text-center">
                <div class="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <PieChart class="w-6 h-6 text-purple-600 dark:text-purple-400" />
                </div>
                <div class="text-2xl font-bold text-gray-900 dark:text-white">{{ money(stats.totalProfit) }}</div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Total Profit</div>
              </div>
            </div>
          </div>

          <div class="px-4 sm:px-6 pb-4 sm:pb-6 border-t border-gray-200 dark:border-gray-700 pt-4 sm:pt-6">
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div class="flex items-center gap-3">
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-300">Filter by status:</span>
                  <select v-model="filter" @change="load"
                    class="appearance-none bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg px-4 py-2 pr-8 text-sm font-medium text-gray-700 dark:text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    <option value="All">All Plans</option>
                    <option value="yes">Active Plans</option>
                    <option value="expired">Expired Plans</option>
                    <option value="cancelled">Cancelled Plans</option>
                  </select>
                </div>
                <div class="text-sm text-gray-600 dark:text-gray-400">Showing {{ investments.length }} plan{{ investments.length === 1 ? '' : 's' }}</div>
              </div>
          </div>
        </div>

        <!-- Plans list -->
        <div class="space-y-6">
          <div v-for="inv in investments" :key="inv.id" class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300">
            <div class="p-6">
              <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
                <!-- Plan Info -->
                <div class="flex items-start gap-4">
                  <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center flex-shrink-0">
                    <Briefcase class="w-8 h-8 text-white" />
                  </div>
                  <div class="flex-1">
                    <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">{{ inv.planName }}</h3>
                    <div class="flex flex-wrap items-center gap-4 text-sm">
                      <div class="flex items-center gap-2">
                        <span class="text-gray-600 dark:text-gray-400">Investment Amount:</span>
                        <span class="font-semibold text-gray-900 dark:text-white">{{ money(inv.amount) }}</span>
                      </div>
                      <div v-if="inv.planIncrementAmount != null" class="flex items-center gap-2">
                        <span class="text-gray-600 dark:text-gray-400">Expected ROI:</span>
                        <span class="font-semibold text-green-600">{{ inv.planIncrementAmount }}%</span>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-gray-600 dark:text-gray-400">Expiration:</span>
                        <span class="font-semibold text-gray-900 dark:text-white">{{ inv.invDuration }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Start / End Date -->
                <div class="grid grid-cols-3 gap-2 sm:gap-6 lg:max-w-md">
                  <div class="text-center">
                    <div class="text-sm sm:text-lg font-bold text-gray-900 dark:text-white">{{ formatDate(inv.activatedAt, { month: 'short', day: '2-digit' }) }}</div>
                    <div class="text-xs sm:text-sm text-gray-600 dark:text-gray-400">{{ formatDate(inv.activatedAt, { year: 'numeric' }) }}</div>
                    <div class="text-[10px] sm:text-xs text-gray-500 dark:text-gray-500 mt-1">Start Date</div>
                  </div>
                  <div class="flex items-center justify-center">
                    <ArrowRight class="w-4 h-4 sm:w-6 sm:h-6 text-gray-400" />
                  </div>
                  <div class="text-center">
                    <div class="text-sm sm:text-lg font-bold text-gray-900 dark:text-white">{{ formatDate(inv.expireDate, { month: 'short', day: '2-digit' }) }}</div>
                    <div class="text-xs sm:text-sm text-gray-600 dark:text-gray-400">{{ formatDate(inv.expireDate, { year: 'numeric' }) }}</div>
                    <div class="text-[10px] sm:text-xs text-gray-500 dark:text-gray-500 mt-1">End Date</div>
                  </div>
                </div>

                <!-- Status + View Details -->
                <div class="flex items-center gap-4">
                  <div class="text-center">
                    <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium" :class="statusMeta(inv.active).class">
                      <span class="w-2 h-2 rounded-full mr-2" :class="statusMeta(inv.active).dot"></span>
                      {{ statusMeta(inv.active).label }}
                    </span>
                    <div class="text-xs text-gray-500 dark:text-gray-500 mt-1">Status</div>
                  </div>
                  <button type="button" @click="toggleExpand(inv)"
                    class="inline-flex items-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-all duration-200">
                    View Details
                    <ChevronDown class="w-4 h-4 transition-transform" :class="expandedId === inv.id ? 'rotate-180' : ''" />
                  </button>
                </div>
              </div>

              <!-- Progress Bar (active plans only) -->
              <div v-if="inv.active === 'yes'" class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-300">Investment Progress</span>
                  <span class="text-sm text-gray-600 dark:text-gray-400">{{ progressOf(inv).remainingDays }} days remaining</span>
                </div>
                <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                  <div class="bg-gradient-to-r from-blue-500 to-blue-600 h-2 rounded-full transition-all duration-300" :style="{ width: progressOf(inv).pct + '%' }"></div>
                </div>
                <div class="flex justify-between text-xs text-gray-500 dark:text-gray-500 mt-1">
                  <span>{{ progressOf(inv).pct.toFixed(1) }}% complete</span>
                  <span>{{ progressOf(inv).totalDays }} total days</span>
                </div>
              </div>

              <!-- Expanded details -->
              <div v-if="expandedId === inv.id" class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700 space-y-4">
                <div v-if="detailLoading" class="text-sm text-gray-500 dark:text-gray-400">Loading…</div>
                <template v-else>
                  <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                    <div>
                      <div class="text-gray-500 dark:text-gray-400">Profit Earned</div>
                      <div class="font-semibold text-gray-900 dark:text-white">{{ money(inv.profitEarned) }}</div>
                    </div>
                    <div>
                      <div class="text-gray-500 dark:text-gray-400">Withdrawn</div>
                      <div class="font-semibold text-gray-900 dark:text-white">{{ money(inv.profitWithdrawn) }}</div>
                    </div>
                    <div>
                      <div class="text-gray-500 dark:text-gray-400">Available</div>
                      <div class="font-semibold text-emerald-600 dark:text-emerald-400">{{ money(inv.availableProfit) }}</div>
                    </div>
                    <div>
                      <div class="text-gray-500 dark:text-gray-400">Expires</div>
                      <div class="font-semibold text-gray-900 dark:text-white">{{ formatDate(inv.expireDate) }}</div>
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
                    <h4 class="text-sm font-semibold text-gray-900 dark:text-white mb-2">Transaction History</h4>
                    <ul class="divide-y divide-gray-100 dark:divide-gray-800">
                      <li v-for="t in detail.transactions" :key="t.id" class="py-2 flex items-center justify-between text-sm">
                        <span class="text-gray-600 dark:text-gray-300">{{ t.planLabel || t.type }} · {{ formatDate(t.createdAt) }}</span>
                        <span class="font-medium text-gray-900 dark:text-white">{{ money(t.amount) }}</span>
                      </li>
                    </ul>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-if="investments.length === 0" class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700">
            <div class="text-center py-16">
              <div class="w-20 h-20 bg-gray-100 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-6">
                <Briefcase class="w-10 h-10 text-gray-400" />
              </div>
              <h3 class="text-xl font-bold text-gray-900 dark:text-white mb-2">No Investment Plans Found</h3>
              <p class="text-gray-600 dark:text-gray-400 max-w-md mx-auto mb-8">
                You don't have any investment plans at the moment or no plans match your current filter criteria.
              </p>
              <RouterLink :to="{ name: 'user.plans' }"
                class="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-600 hover:from-blue-700 hover:to-blue-700 text-white rounded-xl font-medium transition-all duration-200 shadow-lg hover:shadow-xl">
                <PlusCircle class="w-5 h-5" /> Start Your First Investment
              </RouterLink>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
