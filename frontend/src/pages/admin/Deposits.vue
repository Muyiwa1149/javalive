<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Check, Trash2, ExternalLink } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const deposits = ref([])
const filter = ref('Pending')
const processingId = ref(null)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/deposits', { params: filter.value ? { status: filter.value } : {} })
    deposits.value = data
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function approve(deposit) {
  const confirm = await Swal.fire({
    icon: 'question', title: 'Approve this deposit?',
    text: `Credit ${deposit.userName} with ${deposit.amount} and process referral commissions?`,
    showCancelButton: true, confirmButtonText: 'Approve', background: '#1F2937', color: '#E5E7EB',
  })
  if (!confirm.isConfirmed) return
  processingId.value = deposit.id
  try {
    await api.post(`/admin/deposits/${deposit.id}/approve`)
    await load()
    Swal.fire({ icon: 'success', title: 'Approved', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    processingId.value = null
  }
}

async function remove(deposit) {
  const confirm = await Swal.fire({
    icon: 'warning', title: 'Delete this deposit request?', text: 'This cannot be undone.',
    showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#DC2626',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!confirm.isConfirmed) return
  processingId.value = deposit.id
  try {
    await api.delete(`/admin/deposits/${deposit.id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    processingId.value = null
  }
}

const statusClass = (status) => ({
  Processed: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Manage Deposits</h1>
      <select v-model="filter" class="rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" @change="load">
        <option value="Pending">Pending</option>
        <option value="Processed">Processed</option>
        <option value="">All</option>
      </select>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="deposits.length === 0" class="text-gray-500 dark:text-gray-400">No deposits found.</div>

    <div v-else class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-800">
            <th class="py-3 px-4">User</th>
            <th class="py-3 px-4">Method</th>
            <th class="py-3 px-4">Amount</th>
            <th class="py-3 px-4">Reference</th>
            <th class="py-3 px-4">Proof</th>
            <th class="py-3 px-4">Status</th>
            <th class="py-3 px-4">Date</th>
            <th class="py-3 px-4">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in deposits" :key="d.id" class="border-b border-gray-100 dark:border-gray-800 last:border-0">
            <td class="py-3 px-4">
              <div class="text-gray-900 dark:text-white font-medium">{{ d.userName }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ d.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-gray-700 dark:text-gray-300">{{ d.paymentMode }}</td>
            <td class="py-3 px-4 text-gray-900 dark:text-white font-medium">{{ Number(d.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</td>
            <td class="py-3 px-4 text-gray-500 dark:text-gray-400">{{ d.txnId || '—' }}</td>
            <td class="py-3 px-4">
              <a v-if="d.proofImage" :href="`/storage/${d.proofImage}`" target="_blank" class="text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1">
                View <ExternalLink class="w-3 h-3" />
              </a>
            </td>
            <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(d.status)">{{ d.status }}</span></td>
            <td class="py-3 px-4 text-gray-500 dark:text-gray-400">{{ new Date(d.createdAt).toLocaleString() }}</td>
            <td class="py-3 px-4">
              <div v-if="d.status === 'Pending'" class="flex items-center gap-2">
                <button :disabled="processingId === d.id" class="p-2 text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-lg disabled:opacity-50" title="Approve" @click="approve(d)">
                  <Check class="w-4 h-4" />
                </button>
                <button :disabled="processingId === d.id" class="p-2 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg disabled:opacity-50" title="Delete" @click="remove(d)">
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
