<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { ShieldCheck, Check, X, ExternalLink } from 'lucide-vue-next'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

const loading = ref(true)
const applications = ref([])
const filter = ref('Under review')
const processingId = ref(null)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/kyc')
    applications.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

const filtered = computed(() => filter.value
  ? applications.value.filter((a) => a.status === filter.value)
  : applications.value)

const statusClass = (status) => ({
  Verified: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400',
  'Under review': 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400',
}[status] || 'bg-slate-100 text-slate-700 dark:bg-white/10 dark:text-slate-300')

async function decide(app, action) {
  const accepting = action === 'Accept'
  const confirm = await Swal.fire({
    icon: accepting ? 'question' : 'warning',
    title: accepting ? `Verify ${app.userName}?` : `Reject ${app.userName}'s KYC?`,
    text: accepting ? 'This marks the account as identity-verified.' : 'This deletes the submitted documents so the user can resubmit.',
    showCancelButton: true, confirmButtonText: accepting ? 'Verify' : 'Reject',
    confirmButtonColor: accepting ? '#6366f1' : '#e11d48',
    input: 'text', inputLabel: 'Optional email subject to notify the user',
    inputPlaceholder: accepting ? 'Identity verified' : 'Identity verification rejected',
  })
  if (!confirm.isConfirmed) return
  processingId.value = app.id
  try {
    await api.post(`/admin/kyc/${app.id}/decide`, {
      action,
      subject: confirm.value || (accepting ? 'Identity verified' : 'Identity verification rejected'),
      message: accepting
        ? `Hi ${app.userName}, your identity verification has been approved. You now have full access to all trading features.`
        : `Hi ${app.userName}, your identity verification could not be approved. Please resubmit clear, valid documents.`,
    })
    await load()
    Swal.fire({ icon: 'success', title: 'Done', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    processingId.value = null
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ShieldCheck class="w-6 h-6 text-indigo-500" /> KYC Applications</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ applications.length }} total submissions</p>
      </div>
      <select v-model="filter" class="rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] px-3 py-2.5 text-sm text-slate-900 dark:text-white">
        <option value="Under review">Under review</option>
        <option value="Verified">Verified</option>
        <option value="">All</option>
      </select>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="filtered.length === 0" class="text-slate-500 dark:text-slate-400">No KYC applications found.</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="app in filtered" :key="app.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 space-y-3">
        <div class="flex items-start justify-between">
          <div>
            <p class="font-semibold text-slate-900 dark:text-white">{{ app.firstName }} {{ app.lastName }}</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">{{ app.userEmail }}</p>
          </div>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(app.status)">{{ app.status }}</span>
        </div>

        <div class="text-xs text-slate-500 dark:text-slate-400 space-y-1">
          <p><span class="text-slate-400 dark:text-slate-500">Document:</span> {{ app.documentType || '—' }}</p>
          <p><span class="text-slate-400 dark:text-slate-500">Location:</span> {{ [app.city, app.state, app.country].filter(Boolean).join(', ') || '—' }}</p>
          <p><span class="text-slate-400 dark:text-slate-500">Phone:</span> {{ app.phoneNumber || '—' }}</p>
          <p><span class="text-slate-400 dark:text-slate-500">Submitted:</span> {{ new Date(app.createdAt).toLocaleString() }}</p>
        </div>

        <div class="flex gap-2">
          <a v-if="app.frontImage" :href="storageUrl(app.frontImage)" target="_blank" class="flex-1 text-center text-xs px-2 py-2 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-white/10 flex items-center justify-center gap-1">
            Front <ExternalLink class="w-3 h-3" />
          </a>
          <a v-if="app.backImage" :href="storageUrl(app.backImage)" target="_blank" class="flex-1 text-center text-xs px-2 py-2 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-white/10 flex items-center justify-center gap-1">
            Back <ExternalLink class="w-3 h-3" />
          </a>
        </div>

        <div v-if="app.status === 'Under review'" class="flex gap-2 pt-1">
          <button :disabled="processingId === app.id" class="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-medium disabled:opacity-50" @click="decide(app, 'Accept')">
            <Check class="w-4 h-4" /> Accept
          </button>
          <button :disabled="processingId === app.id" class="flex-1 inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl bg-rose-500 hover:bg-rose-600 text-white text-sm font-medium disabled:opacity-50" @click="decide(app, 'Reject')">
            <X class="w-4 h-4" /> Reject
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
