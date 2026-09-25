<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { TrendingUp, Pencil, X } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const investments = ref([])
const plans = ref([])

const editing = ref(null)
const saving = ref(false)
const form = ref({})

async function load() {
  loading.value = true
  try {
    const [invRes, planRes] = await Promise.all([
      api.get('/admin/investments'),
      api.get('/admin/plans'),
    ])
    investments.value = invRes.data
    plans.value = planRes.data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function money(v) {
  return Number(v ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })
}

function toLocalInput(value) {
  if (!value) return ''
  const d = new Date(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function fromLocalInput(value) {
  return value ? value : null
}

function openEdit(investment) {
  editing.value = investment
  form.value = {
    planId: investment.planId,
    amount: investment.amount,
    active: investment.active,
    invDuration: investment.invDuration ?? '',
    activatedAt: toLocalInput(investment.activatedAt),
    expireDate: toLocalInput(investment.expireDate),
    lastGrowth: toLocalInput(investment.lastGrowth),
    profitEarned: investment.profitEarned,
    profitWithdrawn: investment.profitWithdrawn,
    withdrawalDisabled: !!investment.withdrawalDisabled,
  }
}

function closeEdit() {
  editing.value = null
}

const selectedPlan = computed(() => plans.value.find((p) => p.id === form.value.planId))

async function save() {
  saving.value = true
  try {
    const payload = {
      ...form.value,
      activatedAt: fromLocalInput(form.value.activatedAt),
      expireDate: fromLocalInput(form.value.expireDate),
      lastGrowth: fromLocalInput(form.value.lastGrowth),
    }
    const { data } = await api.put(`/admin/investments/${editing.value.id}`, payload)
    const idx = investments.value.findIndex((i) => i.id === data.id)
    if (idx !== -1) investments.value[idx] = data
    Swal.fire({ icon: 'success', title: 'Investment updated', timer: 1200, showConfirmButton: false })
    closeEdit()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

const statusStyle = {
  yes: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400',
  expired: 'bg-slate-500/10 text-slate-600 dark:text-slate-400',
  cancelled: 'bg-rose-500/10 text-rose-600 dark:text-rose-400',
}
function statusLabel(active) {
  return active === 'yes' ? 'Active' : active ? active[0].toUpperCase() + active.slice(1) : '—'
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><TrendingUp class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Active Investments</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">{{ investments.length }} active investments across all users — edit any field to correct amounts, dates, or ROI plan</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="investments.length === 0" class="text-slate-500 dark:text-slate-400">No investments.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="i in investments" :key="i.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ i.userName }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ i.userEmail }}</div>
          </div>
          <span class="shrink-0 text-[11px] font-medium px-2 py-0.5 rounded-full" :class="statusStyle[i.active] || statusStyle.expired">{{ statusLabel(i.active) }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Plan</div><div class="text-slate-700 dark:text-slate-300">{{ i.planName || '—' }}</div></div>
          <div><div class="text-[11px] text-slate-500">Amount</div><div class="font-medium text-slate-900 dark:text-white">{{ money(i.amount) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Profit Earned</div><div class="text-emerald-600 dark:text-emerald-400">{{ money(i.profitEarned) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Expires</div><div class="text-slate-500 dark:text-slate-400">{{ i.expireDate ? new Date(i.expireDate).toLocaleDateString() : '—' }}</div></div>
        </div>
        <button class="w-full mt-2 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-sm font-medium" @click="openEdit(i)">
          <Pencil class="w-3.5 h-3.5" /> Edit
        </button>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && investments.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Plan</th>
            <th class="py-3 px-4 font-medium text-right">Amount</th>
            <th class="py-3 px-4 font-medium text-right">Profit Earned</th>
            <th class="py-3 px-4 font-medium">Activated</th>
            <th class="py-3 px-4 font-medium">Expires</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="i in investments" :key="i.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ i.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ i.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ i.planName || '—' }}</td>
            <td class="py-3 px-4 text-right font-medium text-slate-900 dark:text-white">{{ money(i.amount) }}</td>
            <td class="py-3 px-4 text-right text-emerald-600 dark:text-emerald-400">{{ money(i.profitEarned) }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.activatedAt ? new Date(i.activatedAt).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ i.expireDate ? new Date(i.expireDate).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4"><span class="text-xs font-medium px-2 py-0.5 rounded-full" :class="statusStyle[i.active] || statusStyle.expired">{{ statusLabel(i.active) }}</span></td>
            <td class="py-3 px-4 text-right">
              <button class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="openEdit(i)">
                <Pencil class="w-3.5 h-3.5" /> Edit
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit modal -->
    <div v-if="editing" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="closeEdit">
      <div class="w-full max-w-lg max-h-[90vh] overflow-y-auto bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/10 rounded-2xl p-5 sm:p-6 space-y-4">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900 dark:text-white">Edit Investment</h2>
            <p class="text-xs text-slate-500 dark:text-slate-400">{{ editing.userName }} · {{ editing.userEmail }}</p>
          </div>
          <button class="p-1.5 rounded-lg text-slate-400 hover:bg-slate-100 dark:hover:bg-white/5" @click="closeEdit"><X class="w-5 h-5" /></button>
        </div>

        <form class="space-y-4" @submit.prevent="save">
          <div class="grid grid-cols-2 gap-3">
            <div class="col-span-2">
              <label class="text-xs text-slate-500">Plan (sets ROI rate &amp; interval)</label>
              <select v-model.number="form.planId" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
                <option :value="null">— none —</option>
                <option v-for="p in plans" :key="p.id" :value="p.id">{{ p.name }}</option>
              </select>
              <p v-if="selectedPlan" class="text-[11px] text-slate-500 mt-1">
                ROI: {{ selectedPlan.incrementAmount }}{{ selectedPlan.incrementType === 'Percentage' ? '%' : '' }} every {{ selectedPlan.incrementInterval }}
              </p>
            </div>

            <div>
              <label class="text-xs text-slate-500">Amount (principal)</label>
              <input v-model.number="form.amount" type="number" step="0.00000001" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <div>
              <label class="text-xs text-slate-500">Status</label>
              <select v-model="form.active" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
                <option value="yes">Active</option>
                <option value="expired">Expired</option>
                <option value="cancelled">Cancelled</option>
              </select>
            </div>

            <div class="col-span-2">
              <label class="text-xs text-slate-500">Duration label (e.g. "30 Days")</label>
              <input v-model="form.invDuration" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>

            <div>
              <label class="text-xs text-slate-500">Activated at</label>
              <input v-model="form.activatedAt" type="datetime-local" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <div>
              <label class="text-xs text-slate-500">Expires at</label>
              <input v-model="form.expireDate" type="datetime-local" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <div class="col-span-2">
              <label class="text-xs text-slate-500">Last ROI payout (next payout is calculated from this)</label>
              <input v-model="form.lastGrowth" type="datetime-local" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>

            <div>
              <label class="text-xs text-slate-500">Profit earned</label>
              <input v-model.number="form.profitEarned" type="number" step="0.00000001" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <div>
              <label class="text-xs text-slate-500">Profit withdrawn</label>
              <input v-model.number="form.profitWithdrawn" type="number" step="0.00000001" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>

            <div class="col-span-2">
              <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
                <input v-model="form.withdrawalDisabled" type="checkbox" class="rounded" /> Withdrawal disabled for this investment
              </label>
            </div>
          </div>

          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="closeEdit">Cancel</button>
            <button type="submit" :disabled="saving" class="px-5 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save Changes' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
