<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Ban, Plus, Trash2 } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const addresses = ref([])
const newIp = ref('')
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/ip-blacklist')
    addresses.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function add() {
  if (!newIp.value.trim()) return
  saving.value = true
  try {
    await api.post('/admin/ip-blacklist', { ipAddress: newIp.value.trim() })
    newIp.value = ''
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function remove(entry) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Remove ${entry.ipAddress} from the blacklist?`,
    showCancelButton: true, confirmButtonText: 'Remove', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/ip-blacklist/${entry.id}`)
  await load()
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-3xl">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Ban class="w-6 h-6 text-indigo-500" /> IP Blacklist</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">Blocked addresses are rejected before any request reaches the app.</p>
    </div>

    <form class="flex gap-2" @submit.prevent="add">
      <input v-model="newIp" placeholder="e.g. 203.0.113.42" class="flex-1 px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] text-sm text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/50" />
      <button type="submit" :disabled="saving" class="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium disabled:opacity-50">
        <Plus class="w-4 h-4" /> Block
      </button>
    </form>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="addresses.length === 0" class="text-slate-500 dark:text-slate-400">No addresses blacklisted.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl divide-y divide-slate-100 dark:divide-white/5">
      <div v-for="a in addresses" :key="a.id" class="px-5 py-3 flex items-center justify-between">
        <div>
          <p class="font-mono text-sm text-slate-900 dark:text-white">{{ a.ipAddress }}</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">{{ new Date(a.createdAt).toLocaleString() }}</p>
        </div>
        <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="remove(a)">
          <Trash2 class="w-4 h-4" />
        </button>
      </div>
    </div>
  </div>
</template>
