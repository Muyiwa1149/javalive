<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Target, Plus, Pencil, Trash2, Power } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const plans = ref([])
const saving = ref(false)
const showForm = ref(false)
const editingId = ref(null)

const emptyForm = () => ({
  name: '', tag: '', price: 0, minPrice: 0, maxPrice: 0, minReturnPct: 0, maxReturnPct: 0,
  gift: 0, expectedReturn: '', incrementType: 'percentage', incrementInterval: 'daily',
  incrementAmount: 0, expirationDays: 30,
})
const form = ref(emptyForm())

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/plans')
    plans.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  showForm.value = true
}

function openEdit(plan) {
  editingId.value = plan.id
  form.value = { ...plan }
  showForm.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/admin/plans/${editingId.value}`, form.value)
    } else {
      await api.post('/admin/plans', form.value)
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

async function toggle(plan) {
  await api.post(`/admin/plans/${plan.id}/toggle`)
  await load()
}

async function remove(plan) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Delete "${plan.name}"?`,
    text: 'This also removes every investment made under this plan. This cannot be undone.',
    showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/plans/${plan.id}`)
  await load()
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Target class="w-6 h-6 text-indigo-500" /> Investment Plans</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ plans.length }} plans configured</p>
      </div>
      <button class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium shadow-lg shadow-indigo-500/20" @click="openCreate">
        <Plus class="w-4 h-4" /> New Plan
      </button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="plans.length === 0" class="text-slate-500 dark:text-slate-400">No plans configured.</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="p in plans" :key="p.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 space-y-3">
        <div class="flex items-start justify-between">
          <div>
            <p class="font-semibold text-slate-900 dark:text-white">{{ p.name }}</p>
            <p v-if="p.tag" class="text-xs text-indigo-500">{{ p.tag }}</p>
          </div>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="p.active ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'">{{ p.active ? 'Active' : 'Disabled' }}</span>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400 space-y-1">
          <p><span class="text-slate-400 dark:text-slate-500">Range:</span> {{ p.minPrice }} – {{ p.maxPrice }}</p>
          <p><span class="text-slate-400 dark:text-slate-500">Return:</span> {{ p.minReturnPct }}% – {{ p.maxReturnPct }}% ({{ p.incrementType }}, {{ p.incrementInterval }})</p>
          <p><span class="text-slate-400 dark:text-slate-500">Duration:</span> {{ p.expirationDays }} days</p>
          <p v-if="Number(p.gift) > 0"><span class="text-slate-400 dark:text-slate-500">Sign-up gift:</span> {{ p.gift }}</p>
        </div>
        <div class="flex gap-2 pt-1">
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-sm font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="openEdit(p)">
            <Pencil class="w-4 h-4" /> Edit
          </button>
          <button class="p-2 rounded-xl bg-slate-100 dark:bg-white/5 hover:bg-slate-200 dark:hover:bg-white/10" :class="p.active ? 'text-amber-500' : 'text-emerald-500'" title="Toggle active" @click="toggle(p)">
            <Power class="w-4 h-4" />
          </button>
          <button class="p-2 rounded-xl bg-rose-50 dark:bg-rose-500/10 text-rose-500 hover:bg-rose-100 dark:hover:bg-rose-500/20" title="Delete" @click="remove(p)">
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Form modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 my-8">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ editingId ? 'Edit Plan' : 'New Plan' }}</h2>
        <form class="space-y-3" @submit.prevent="save">
          <input v-model="form.name" required placeholder="Plan name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.tag" placeholder="Tag (e.g. Popular)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-3 gap-3">
            <div><label class="text-xs text-slate-500">Price</label><input v-model.number="form.price" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Min</label><input v-model.number="form.minPrice" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Max</label><input v-model.number="form.maxPrice" type="number" step="0.01" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Min return %</label><input v-model.number="form.minReturnPct" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Max return %</label><input v-model.number="form.maxReturnPct" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <select v-model="form.incrementType" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="percentage">Percentage</option>
              <option value="fixed">Fixed</option>
            </select>
            <select v-model="form.incrementInterval" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="daily">Daily</option>
              <option value="weekly">Weekly</option>
              <option value="monthly">Monthly</option>
            </select>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Increment amount</label><input v-model.number="form.incrementAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Duration (days)</label><input v-model.number="form.expirationDays" type="number" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div><label class="text-xs text-slate-500">Sign-up gift</label><input v-model.number="form.gift" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <input v-model="form.expectedReturn" placeholder="Expected return label (shown to users)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
