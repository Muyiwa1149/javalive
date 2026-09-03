<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Send, Check, X } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const withdrawals = ref([])
const filter = ref('Pending')
const processingId = ref(null)

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
    await api.post(`/admin/withdrawals/${w.id}/reject`, { reason, subject: 'Withdrawal Rejected', sendEmail: true })
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    processingId.value = null
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
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Send class="w-6 h-6 text-indigo-500" /> Manage Withdrawals</h1>
      <select v-model="filter" class="rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] px-3 py-2.5 text-sm text-slate-900 dark:text-white" @change="load">
        <option value="Pending">Pending</option>
        <option value="Processed">Processed</option>
        <option value="Rejected">Rejected</option>
        <option value="">All</option>
      </select>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="withdrawals.length === 0" class="text-slate-500 dark:text-slate-400">No withdrawals found.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
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
              <div v-if="w.status === 'Pending'" class="flex items-center justify-end gap-1">
                <button :disabled="processingId === w.id" class="p-2 text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg disabled:opacity-50" title="Approve" @click="approve(w)">
                  <Check class="w-4 h-4" />
                </button>
                <button :disabled="processingId === w.id" class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg disabled:opacity-50" title="Reject" @click="reject(w)">
                  <X class="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
