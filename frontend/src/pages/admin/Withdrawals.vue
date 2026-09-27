<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Send, Check, X, Pencil } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const withdrawals = ref([])
const filter = ref('')
const processingId = ref(null)

const showEdit = ref(false)
const editId = ref(null)
const editOriginalToDeduct = ref(0)
const editForm = ref({ amount: 0, toDeduct: 0, paymentMode: '', status: 'Pending', payDetails: '', createdAt: '', adjustBalance: false })
const saving = ref(false)
const balanceDelta = computed(() => editOriginalToDeduct.value - Number(editForm.value.toDeduct || 0))

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/withdrawals', { params: filter.value ? { status: filter.value } : {} })
    withdrawals.value = data
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function approve(w) {
  const confirm = await Swal.fire({
    icon: 'question', title: 'Approve this withdrawal?',
    text: `Mark ${w.userName}'s withdrawal of ${w.amount} as paid?`,
    showCancelButton: true, confirmButtonText: 'Approve', confirmButtonColor: '#6366f1',
  })
  if (!confirm.isConfirmed) return
  processingId.value = w.id
  try {
    await api.post(`/admin/withdrawals/${w.id}/approve`)
    await load()
    Swal.fire({ icon: 'success', title: 'Approved', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    processingId.value = null
  }
}

async function reject(w) {
  const { value: reason, isConfirmed } = await Swal.fire({
    icon: 'warning', title: 'Reject this withdrawal?',
    input: 'textarea',
    inputLabel: 'Reason (sent to the user)',
    inputPlaceholder: 'e.g. Withdrawal details could not be verified',
    showCancelButton: true, confirmButtonText: 'Reject', confirmButtonColor: '#e11d48',
  })
  if (!isConfirmed) return
  processingId.value = w.id
  try {
    await api.post(`/admin/withdrawals/${w.id}/reject`, { reason, subject: 'Withdrawal Rejected' })
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    processingId.value = null
  }
}

function openEdit(w) {
  editId.value = w.id
  editOriginalToDeduct.value = Number(w.toDeduct)
  editForm.value = {
    amount: w.amount,
    toDeduct: w.toDeduct,
    paymentMode: w.paymentMode || '',
    status: w.status,
    payDetails: w.payDetails || '',
    createdAt: w.createdAt ? w.createdAt.slice(0, 16) : '',
    adjustBalance: false,
  }
  showEdit.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await api.put(`/admin/withdrawals/${editId.value}`, editForm.value)
    showEdit.value = false
    await load()
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

const statusClass = (status) => ({
  Processed: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400',
  Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400',
  Rejected: 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400',
}[status] || 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300')
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Send class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Manage Withdrawals</h1>
      <select v-model="filter" class="rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] px-3 py-2.5 text-sm text-slate-900 dark:text-white" @change="load">
        <option value="">All</option>
        <option value="Pending">Pending</option>
        <option value="Processed">Processed</option>
        <option value="Rejected">Rejected</option>
      </select>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="withdrawals.length === 0" class="text-slate-500 dark:text-slate-400">No withdrawals found.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="w in withdrawals" :key="w.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ w.userName }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ w.userEmail }}</div>
          </div>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium" :class="statusClass(w.status)">{{ w.status }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Method</div><div class="text-slate-700 dark:text-slate-300">{{ w.paymentMode }}</div></div>
          <div><div class="text-[11px] text-slate-500">Amount</div><div class="font-medium text-slate-900 dark:text-white">{{ Number(w.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Deducted</div><div class="text-slate-500 dark:text-slate-400">{{ Number(w.toDeduct).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div></div>
          <div class="col-span-2"><div class="text-[11px] text-slate-500">Details</div><div class="text-slate-500 dark:text-slate-400 truncate" :title="w.payDetails">{{ w.payDetails }}</div></div>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400">{{ new Date(w.createdAt).toLocaleString() }}</div>
        <div class="flex items-center gap-1 pt-2 border-t border-slate-100 dark:border-white/5">
          <button :disabled="processingId === w.id" class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-indigo-500 hover:bg-indigo-50 dark:hover:bg-indigo-500/10 rounded-lg disabled:opacity-50" @click="openEdit(w)"><Pencil class="w-3.5 h-3.5" /> Edit</button>
          <button v-if="w.status === 'Pending'" :disabled="processingId === w.id" class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg disabled:opacity-50" @click="approve(w)"><Check class="w-3.5 h-3.5" /> Approve</button>
          <button v-if="w.status === 'Pending'" :disabled="processingId === w.id" class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg disabled:opacity-50" @click="reject(w)"><X class="w-3.5 h-3.5" /> Reject</button>
        </div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && withdrawals.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Method</th>
            <th class="py-3 px-4 font-medium text-right">Amount</th>
            <th class="py-3 px-4 font-medium text-right">Deducted</th>
            <th class="py-3 px-4 font-medium">Details</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium">Date</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in withdrawals" :key="w.id" class="border-b border-slate-100 dark:border-white/5 last:border-0 hover:bg-slate-50 dark:hover:bg-white/5">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ w.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ w.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ w.paymentMode }}</td>
            <td class="py-3 px-4 text-right text-slate-900 dark:text-white font-medium">{{ Number(w.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
            <td class="py-3 px-4 text-right text-slate-500 dark:text-slate-400">{{ Number(w.toDeduct).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400 max-w-xs truncate" :title="w.payDetails">{{ w.payDetails }}</td>
            <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(w.status)">{{ w.status }}</span></td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ new Date(w.createdAt).toLocaleString() }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-1">
                <button class="p-2 text-indigo-500 hover:bg-indigo-50 dark:hover:bg-indigo-500/10 rounded-lg" title="Edit" @click="openEdit(w)">
                  <Pencil class="w-4 h-4" />
                </button>
                <template v-if="w.status === 'Pending'">
                  <button :disabled="processingId === w.id" class="p-2 text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg disabled:opacity-50" title="Approve" @click="approve(w)">
                    <Check class="w-4 h-4" />
                  </button>
                  <button :disabled="processingId === w.id" class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg disabled:opacity-50" title="Reject" @click="reject(w)">
                    <X class="w-4 h-4" />
                  </button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showEdit = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Edit Withdrawal</h2>
        <form class="space-y-3" @submit.prevent="saveEdit">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Amount</label>
              <input v-model.number="editForm.amount" type="number" step="0.00000001" required class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <div>
              <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Deducted</label>
              <input v-model.number="editForm.toDeduct" type="number" step="0.00000001" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
          </div>
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Payment method</label>
            <input v-model="editForm.paymentMode" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Payout details</label>
            <textarea v-model="editForm.payDetails" rows="2" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          </div>
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Status</label>
            <select v-model="editForm.status" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="Pending">Pending</option>
              <option value="Processed">Processed</option>
              <option value="Rejected">Rejected</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500 dark:text-slate-400 block mb-1">Date &amp; time</label>
            <input v-model="editForm.createdAt" type="datetime-local" required class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <div class="p-3 rounded-xl bg-slate-50 dark:bg-white/5 space-y-2">
            <label class="flex items-start gap-2.5 text-sm text-slate-700 dark:text-slate-300 cursor-pointer">
              <input v-model="editForm.adjustBalance" type="checkbox" class="mt-0.5 rounded" />
              <span>Also adjust the user's account balance by the difference in deducted amount</span>
            </label>
            <p v-if="editForm.adjustBalance && balanceDelta !== 0" class="text-xs" :class="balanceDelta > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-500'">
              Balance will {{ balanceDelta > 0 ? 'increase' : 'decrease' }} by {{ Math.abs(balanceDelta).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
            </p>
            <p v-else-if="editForm.adjustBalance" class="text-xs text-slate-400">No change in deducted amount — balance won't be affected.</p>
          </div>
          <p class="text-xs text-amber-600 dark:text-amber-400">Leaving the box unchecked edits the record only — the balance stays untouched.</p>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showEdit = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
