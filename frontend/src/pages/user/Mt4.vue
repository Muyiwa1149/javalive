<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Server, RefreshCw, Trash2, PlusCircle } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const subscriptions = ref([])
const showForm = ref(false)
const submitting = ref(false)
const busyId = ref(null)

const form = ref({ userid: '', pswrd: '', acntype: 'Standard', name: '', currency: 'USD', leverage: '1:500', server: '', duration: 'Monthly', amount: 0 })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/mt4')
    subscriptions.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function submit() {
  submitting.value = true
  try {
    await api.post('/mt4', form.value)
    await authUser.fetchProfile()
    await load()
    showForm.value = false
    form.value = { userid: '', pswrd: '', acntype: 'Standard', name: '', currency: 'USD', leverage: '1:500', server: '', duration: 'Monthly', amount: 0 }
    Swal.fire({ icon: 'success', title: 'Subscription submitted', text: 'Please wait for the system to validate your credentials.', background: '#1F2937', color: '#E5E7EB' })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}

async function renew(sub) {
  busyId.value = sub.id
  try {
    await api.post(`/mt4/${sub.id}/renew`)
    await authUser.fetchProfile()
    await load()
    Swal.fire({ icon: 'success', title: 'Renewed', background: '#1F2937', color: '#E5E7EB', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Renewal failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

async function remove(sub) {
  const confirm = await Swal.fire({ icon: 'warning', title: 'Delete this subscription?', showCancelButton: true, confirmButtonColor: '#DC2626', background: '#1F2937', color: '#E5E7EB' })
  if (!confirm.isConfirmed) return
  busyId.value = sub.id
  try {
    await api.delete(`/mt4/${sub.id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    busyId.value = null
  }
}

const statusClass = (status) => ({
  Active: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',
  Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300',
}[status] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center justify-between flex-wrap gap-4">
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Server class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> MT4 Subscription
      </h1>
      <button type="button" class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg" @click="showForm = !showForm">
        <PlusCircle class="w-4 h-4" /> New Subscription
      </button>
    </div>

    <form v-if="showForm" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4" @submit.prevent="submit">
      <div class="grid sm:grid-cols-2 gap-4">
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">MT4 Account ID</label>
          <input v-model="form.userid" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">MT4 Password</label>
          <input v-model="form.pswrd" type="password" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Account Name</label>
          <input v-model="form.name" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Account Type</label>
          <select v-model="form.acntype" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
            <option>Standard</option><option>ECN</option><option>Pro</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Currency</label>
          <input v-model="form.currency" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Leverage</label>
          <input v-model="form.leverage" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Server</label>
          <input v-model="form.server" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Duration</label>
          <select v-model="form.duration" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
            <option>Monthly</option><option>Quaterly</option><option>Yearly</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Amount ({{ authUser.user?.currencySymbol }})</label>
          <input v-model.number="form.amount" type="number" min="0" step="0.01" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
        </div>
      </div>
      <button type="submit" :disabled="submitting" class="w-full py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium disabled:opacity-50">
        {{ submitting ? 'Submitting…' : 'Submit Subscription' }}
      </button>
    </form>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="subscriptions.length === 0" class="text-gray-500 dark:text-gray-400">No MT4 subscriptions yet.</div>

    <div v-else class="space-y-3">
      <div v-for="s in subscriptions" :key="s.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-4">
        <div class="flex items-center justify-between mb-2">
          <div class="font-medium text-gray-900 dark:text-white">{{ s.accountName }} — {{ s.mt4Id }}</div>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(s.status)">{{ s.status }}</span>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm mb-3">
          <div><div class="text-gray-500 dark:text-gray-400">Server</div><div class="text-gray-900 dark:text-white">{{ s.server }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Leverage</div><div class="text-gray-900 dark:text-white">{{ s.leverage }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Duration</div><div class="text-gray-900 dark:text-white">{{ s.duration }}</div></div>
          <div><div class="text-gray-500 dark:text-gray-400">Expires</div><div class="text-gray-900 dark:text-white">{{ s.endDate ? new Date(s.endDate).toLocaleDateString() : '—' }}</div></div>
        </div>
        <div class="flex gap-3">
          <button type="button" :disabled="busyId === s.id" class="flex items-center gap-2 px-3 py-2 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 text-sm font-medium rounded-lg disabled:opacity-50" @click="renew(s)">
            <RefreshCw class="w-4 h-4" /> Renew
          </button>
          <button type="button" :disabled="busyId === s.id" class="flex items-center gap-2 px-3 py-2 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm font-medium rounded-lg disabled:opacity-50" @click="remove(s)">
            <Trash2 class="w-4 h-4" /> Delete
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
