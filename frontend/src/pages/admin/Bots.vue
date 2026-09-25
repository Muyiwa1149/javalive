<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Bot, Plus, Pencil, Trash2, Power, Eye, Users2, TrendingUp } from 'lucide-vue-next'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

const loading = ref(true)
const bots = ref([])
const saving = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const imageFile = ref(null)

const showDetail = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

const BOT_TYPES = ['forex', 'crypto', 'stocks', 'commodities', 'indices']

const emptyForm = () => ({
  name: '', botType: 'crypto', description: '', minInvestment: 100, maxInvestment: 5000,
  dailyProfitMin: 1, dailyProfitMax: 3, successRate: 80, durationDays: 30,
  tradingPairsText: 'BTC/USD, ETH/USD', status: 'active',
})
const form = ref(emptyForm())

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/bots')
    bots.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  imageFile.value = null
  showForm.value = true
}

function openEdit(bot) {
  editingId.value = bot.id
  let pairs = []
  try { pairs = JSON.parse(bot.tradingPairs || '[]') } catch { pairs = [] }
  form.value = { ...bot, tradingPairsText: pairs.join(', ') }
  imageFile.value = null
  showForm.value = true
}

function onFileChange(e) {
  imageFile.value = e.target.files[0] || null
}

async function save() {
  saving.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.value.name)
    fd.append('botType', form.value.botType)
    fd.append('description', form.value.description)
    fd.append('minInvestment', form.value.minInvestment)
    fd.append('maxInvestment', form.value.maxInvestment)
    fd.append('dailyProfitMin', form.value.dailyProfitMin)
    fd.append('dailyProfitMax', form.value.dailyProfitMax)
    fd.append('successRate', form.value.successRate)
    fd.append('durationDays', form.value.durationDays)
    fd.append('status', form.value.status)
    form.value.tradingPairsText.split(',').map((s) => s.trim()).filter(Boolean).forEach((p) => fd.append('tradingPairs', p))
    if (imageFile.value) fd.append('image', imageFile.value)

    if (editingId.value) {
      await api.put(`/admin/bots/${editingId.value}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    } else {
      await api.post('/admin/bots', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    }
    showForm.value = false
    await load()
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function toggle(bot) {
  await api.post(`/admin/bots/${bot.id}/toggle`)
  await load()
}

async function remove(bot) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Delete ${bot.name}?`, text: 'Bots with active investments cannot be deleted.',
    showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  try {
    await api.delete(`/admin/bots/${bot.id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  }
}

async function viewDetail(bot) {
  showDetail.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const { data } = await api.get(`/admin/bots/${bot.id}`)
    detail.value = data
  } finally {
    detailLoading.value = false
  }
}

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Bot class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Trading Bots</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ bots.length }} bots configured</p>
      </div>
      <button class="inline-flex items-center justify-center gap-2 px-3.5 py-2 sm:px-4 sm:py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium shadow-lg shadow-indigo-500/20" @click="openCreate">
        <Plus class="w-4 h-4" /> New Bot
      </button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="bots.length === 0" class="text-slate-500 dark:text-slate-400">No trading bots configured.</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="b in bots" :key="b.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 space-y-3">
        <div class="flex items-start gap-3">
          <img v-if="b.image" :src="storageUrl(b.image)" class="w-12 h-12 rounded-xl object-cover" />
          <div v-else class="w-12 h-12 rounded-xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white"><Bot class="w-6 h-6" /></div>
          <div class="flex-1 min-w-0">
            <p class="font-semibold text-slate-900 dark:text-white truncate">{{ b.name }}</p>
            <p class="text-xs text-indigo-500 capitalize">{{ b.botType }}</p>
          </div>
          <span class="px-2 py-1 rounded-full text-xs font-medium capitalize" :class="b.status === 'active' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'">{{ b.status }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-xs text-slate-500 dark:text-slate-400">
          <p>Range: {{ money(b.minInvestment) }} – {{ money(b.maxInvestment) }}</p>
          <p>{{ b.dailyProfitMin }}% – {{ b.dailyProfitMax }}% daily</p>
          <p>{{ b.successRate }}% success rate</p>
          <p>{{ b.durationDays }} days</p>
        </div>
        <p class="text-xs text-slate-500 dark:text-slate-400">{{ b.investmentsCount }} total investments · {{ b.activeInvestmentsCount }} active</p>
        <div class="flex gap-2 pt-1">
          <button class="p-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 hover:bg-slate-200 dark:hover:bg-white/10" title="View" @click="viewDetail(b)"><Eye class="w-4 h-4" /></button>
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-sm font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="openEdit(b)">
            <Pencil class="w-4 h-4" /> Edit
          </button>
          <button class="p-2 rounded-xl bg-slate-100 dark:bg-white/5 hover:bg-slate-200 dark:hover:bg-white/10" :class="b.status === 'active' ? 'text-amber-500' : 'text-emerald-500'" title="Toggle status" @click="toggle(b)">
            <Power class="w-4 h-4" />
          </button>
          <button class="p-2 rounded-xl bg-rose-50 dark:bg-rose-500/10 text-rose-500 hover:bg-rose-100 dark:hover:bg-rose-500/20" title="Delete" @click="remove(b)">
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Form modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 my-8">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ editingId ? 'Edit Bot' : 'New Bot' }}</h2>
        <form class="space-y-3" @submit.prevent="save">
          <input v-model="form.name" required placeholder="Bot name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <select v-model="form.botType" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white capitalize">
            <option v-for="t in BOT_TYPES" :key="t" :value="t">{{ t }}</option>
          </select>
          <textarea v-model="form.description" required minlength="50" rows="3" placeholder="Description (min 50 characters)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <input type="file" accept="image/*" class="w-full text-sm text-slate-500 dark:text-slate-400" @change="onFileChange" />
          <input v-model="form.tradingPairsText" required placeholder="Trading pairs, comma-separated" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Min investment</label><input v-model.number="form.minInvestment" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Max investment</label><input v-model.number="form.maxInvestment" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Daily profit min %</label><input v-model.number="form.dailyProfitMin" type="number" step="0.1" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Daily profit max %</label><input v-model.number="form.dailyProfitMax" type="number" step="0.1" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Success rate %</label><input v-model.number="form.successRate" type="number" min="50" max="99" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Duration (days)</label><input v-model.number="form.durationDays" type="number" min="1" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <select v-model="form.status" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
            <option value="maintenance">Maintenance</option>
          </select>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Detail modal -->
    <div v-if="showDetail" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showDetail = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-2xl p-6 my-8">
        <div v-if="detailLoading" class="text-slate-500 dark:text-slate-400">Loading…</div>
        <template v-else-if="detail">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ detail.bot.name }}</h2>
          <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-4">
            <div class="bg-slate-50 dark:bg-white/5 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400"><Users2 class="w-3.5 h-3.5" /> Users</div>
              <p class="mt-1 font-bold text-slate-900 dark:text-white">{{ detail.totalUsers }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-white/5 rounded-xl p-3">
              <div class="text-xs text-slate-500 dark:text-slate-400">Active investments</div>
              <p class="mt-1 font-bold text-slate-900 dark:text-white">{{ detail.activeInvestments }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-white/5 rounded-xl p-3">
              <div class="text-xs text-slate-500 dark:text-slate-400">Total invested</div>
              <p class="mt-1 font-bold text-slate-900 dark:text-white">{{ money(detail.totalInvested) }}</p>
            </div>
            <div class="bg-slate-50 dark:bg-white/5 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400"><TrendingUp class="w-3.5 h-3.5" /> Avg success</div>
              <p class="mt-1 font-bold text-slate-900 dark:text-white">{{ detail.avgSuccessRate.toFixed(1) }}%</p>
            </div>
          </div>
          <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-2">Recent Trades</h3>
          <div v-if="detail.recentTrades.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No trades recorded yet.</div>
          <!-- Mobile: stacked cards -->
          <div v-else class="sm:hidden space-y-2">
            <div v-for="t in detail.recentTrades" :key="t.id" class="bg-slate-50 dark:bg-white/5 rounded-xl p-3 space-y-1">
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-slate-800 dark:text-slate-200">{{ t.userName || '—' }}</span>
                <span class="text-sm font-medium" :class="Number(t.profitLoss) >= 0 ? 'text-emerald-500' : 'text-rose-500'">{{ money(t.profitLoss) }}</span>
              </div>
              <div class="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400">
                <span>{{ t.tradingPair }} · {{ t.tradeType }}</span>
                <span class="capitalize">{{ t.result }}</span>
              </div>
            </div>
          </div>
          <!-- Desktop: table -->
          <div v-if="detail.recentTrades.length" class="hidden sm:block overflow-x-auto">
            <table class="w-full text-xs">
              <thead>
                <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
                  <th class="py-2 pr-3">User</th><th class="py-2 pr-3">Pair</th><th class="py-2 pr-3">Type</th><th class="py-2 pr-3 text-right">P/L</th><th class="py-2">Result</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in detail.recentTrades" :key="t.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
                  <td class="py-2 pr-3 text-slate-700 dark:text-slate-300">{{ t.userName || '—' }}</td>
                  <td class="py-2 pr-3 text-slate-700 dark:text-slate-300">{{ t.tradingPair }}</td>
                  <td class="py-2 pr-3 text-slate-500 dark:text-slate-400">{{ t.tradeType }}</td>
                  <td class="py-2 pr-3 text-right" :class="Number(t.profitLoss) >= 0 ? 'text-emerald-500' : 'text-rose-500'">{{ money(t.profitLoss) }}</td>
                  <td class="py-2 capitalize text-slate-500 dark:text-slate-400">{{ t.result }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="flex justify-end pt-4">
            <button class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showDetail = false">Close</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
