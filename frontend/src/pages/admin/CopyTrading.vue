<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Users2, Plus, Pencil, Trash2, Star, TrendingUp, Wallet, Percent } from 'lucide-vue-next'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

const loading = ref(true)
const experts = ref([])
const stats = ref(null)
const saving = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const photoFile = ref(null)

const emptyForm = () => ({
  name: '', tag: '', rating: 5, equity: 0, totalProfit: 0, winRate: 70, totalTrades: 0, price: 0,
  description: '', status: 'active',
})
const form = ref(emptyForm())

async function load() {
  loading.value = true
  try {
    const [expertsRes, statsRes] = await Promise.all([
      api.get('/admin/copy-trading/experts'),
      api.get('/admin/copy-trading/statistics'),
    ])
    experts.value = expertsRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  photoFile.value = null
  showForm.value = true
}

function openEdit(expert) {
  editingId.value = expert.id
  form.value = { ...expert }
  photoFile.value = null
  showForm.value = true
}

function onFileChange(e) {
  photoFile.value = e.target.files[0] || null
}

async function save() {
  saving.value = true
  try {
    const fd = new FormData()
    Object.entries(form.value).forEach(([k, v]) => { if (v !== null && v !== undefined) fd.append(k, v) })
    if (photoFile.value) fd.append('photo', photoFile.value)

    if (editingId.value) {
      await api.put(`/admin/copy-trading/experts/${editingId.value}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    } else {
      await api.post('/admin/copy-trading/experts', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
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

async function remove(expert) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Delete ${expert.name}?`, text: 'Experts with active copiers cannot be deleted.',
    showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  try {
    await api.delete(`/admin/copy-trading/experts/${expert.id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Users2 class="w-6 h-6 text-indigo-500" /> Expert Traders</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">Copy trading experts and statistics</p>
      </div>
      <button class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium shadow-lg shadow-indigo-500/20" @click="openCreate">
        <Plus class="w-4 h-4" /> New Expert
      </button>
    </div>

    <div v-if="stats" class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
        <div class="text-xs text-slate-500 dark:text-slate-400">Active Experts</div>
        <p class="mt-1 text-lg font-bold text-slate-900 dark:text-white">{{ stats.activeExperts }} / {{ stats.totalExperts }}</p>
      </div>
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
        <div class="text-xs text-slate-500 dark:text-slate-400">Active Copy Trades</div>
        <p class="mt-1 text-lg font-bold text-slate-900 dark:text-white">{{ stats.activeCopyTrades }} / {{ stats.totalCopyTrades }}</p>
      </div>
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
        <div class="text-xs text-slate-500 dark:text-slate-400">Total Invested</div>
        <p class="mt-1 text-lg font-bold text-slate-900 dark:text-white">{{ Number(stats.totalInvested).toLocaleString() }}</p>
      </div>
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
        <div class="text-xs text-slate-500 dark:text-slate-400">Users Copying</div>
        <p class="mt-1 text-lg font-bold text-slate-900 dark:text-white">{{ stats.totalUsersCopying }}</p>
      </div>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="experts.length === 0" class="text-slate-500 dark:text-slate-400">No expert traders configured.</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="e in experts" :key="e.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 space-y-3">
        <div class="flex items-start gap-3">
          <img v-if="e.photo" :src="storageUrl(e.photo)" class="w-12 h-12 rounded-full object-cover" />
          <div v-else class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white font-semibold">{{ e.name.charAt(0) }}</div>
          <div class="flex-1 min-w-0">
            <p class="font-semibold text-slate-900 dark:text-white truncate">{{ e.name }}</p>
            <p v-if="e.tag" class="text-xs text-indigo-500">{{ e.tag }}</p>
          </div>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="e.status === 'active' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'">{{ e.status }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-xs text-slate-500 dark:text-slate-400">
          <p class="flex items-center gap-1"><Star class="w-3.5 h-3.5" /> {{ e.rating }}/5</p>
          <p class="flex items-center gap-1"><Percent class="w-3.5 h-3.5" /> {{ e.winRate }}% win</p>
          <p class="flex items-center gap-1"><TrendingUp class="w-3.5 h-3.5" /> {{ e.totalTrades }} trades</p>
          <p class="flex items-center gap-1"><Wallet class="w-3.5 h-3.5" /> {{ Number(e.equity).toLocaleString() }} equity</p>
        </div>
        <p class="text-xs text-slate-500 dark:text-slate-400">{{ e.copiersCount }} total copiers · {{ e.activeCopiersCount }} active</p>
        <div class="flex gap-2 pt-1">
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-sm font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="openEdit(e)">
            <Pencil class="w-4 h-4" /> Edit
          </button>
          <button class="p-2 rounded-xl bg-rose-50 dark:bg-rose-500/10 text-rose-500 hover:bg-rose-100 dark:hover:bg-rose-500/20" title="Delete" @click="remove(e)">
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Form modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 my-8">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ editingId ? 'Edit Expert' : 'New Expert' }}</h2>
        <form class="space-y-3" @submit.prevent="save">
          <input v-model="form.name" required placeholder="Expert name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.tag" placeholder="Tag (e.g. Top Rated)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input type="file" accept="image/*" class="w-full text-sm text-slate-500 dark:text-slate-400" @change="onFileChange" />
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Rating (1-5)</label><input v-model.number="form.rating" type="number" min="1" max="5" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Win rate %</label><input v-model.number="form.winRate" type="number" min="0" max="100" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Equity</label><input v-model.number="form.equity" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Total profit</label><input v-model.number="form.totalProfit" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Total trades</label><input v-model.number="form.totalTrades" type="number" min="0" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Copy-in price</label><input v-model.number="form.price" type="number" step="0.01" min="1" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <select v-model="form.status" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
          </select>
          <textarea v-model="form.description" rows="3" placeholder="Description" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
