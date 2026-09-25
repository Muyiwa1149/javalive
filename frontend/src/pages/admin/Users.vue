<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import Swal from 'sweetalert2'
import { Search, UserPlus, Ban, CheckCircle2, Eye, ShieldCheck, ShieldOff } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const users = ref([])
const search = ref('')
const showAddModal = ref(false)
const saving = ref(false)
const form = ref({ name: '', username: '', email: '', password: '' })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/users')
    users.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

const filtered = computed(() => {
  if (!search.value.trim()) return users.value
  const q = search.value.toLowerCase()
  return users.value.filter((u) =>
    [u.name, u.username, u.email, u.phone].some((f) => f && f.toLowerCase().includes(q)))
})

function money(u) {
  return `${u.currencySymbol || '$'}${Number(u.accountBalance ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}`
}

const statusClass = (status) => status === 'blocked'
  ? 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400'
  : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'

const verifyClass = (status) => status === 'Verified'
  ? 'text-emerald-500' : 'text-slate-400'

async function toggleBlock(user) {
  const blocking = user.status !== 'blocked'
  const confirm = await Swal.fire({
    icon: 'question', title: blocking ? `Block ${user.name}?` : `Unblock ${user.name}?`,
    showCancelButton: true, confirmButtonText: blocking ? 'Block' : 'Unblock',
    confirmButtonColor: blocking ? '#e11d48' : '#6366f1',
  })
  if (!confirm.isConfirmed) return
  await api.post(`/admin/users/${user.id}/${blocking ? 'block' : 'unblock'}`)
  await load()
}

async function createUser() {
  saving.value = true
  try {
    await api.post('/admin/users', form.value)
    showAddModal.value = false
    form.value = { name: '', username: '', email: '', password: '' }
    await load()
    Swal.fire({ icon: 'success', title: 'User created', timer: 1500, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message || 'Could not create user.' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white">Manage Users</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ users.length }} registered accounts</p>
      </div>
      <button class="inline-flex items-center justify-center gap-2 px-3.5 py-2 sm:px-4 sm:py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium shadow-lg shadow-indigo-500/20 hover:opacity-90" @click="showAddModal = true">
        <UserPlus class="w-4 h-4" /> Add User
      </button>
    </div>

    <div class="relative max-w-md">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
      <input v-model="search" type="text" placeholder="Search name, username, email, phone…"
        class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] text-sm text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/50" />
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="filtered.length === 0" class="text-slate-500 dark:text-slate-400">No users found.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="u in filtered" :key="u.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <RouterLink :to="{ name: 'admin.user-detail', params: { id: u.id } }" class="flex items-center gap-3 min-w-0">
            <div class="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white text-xs font-semibold shrink-0">
              {{ (u.name || '?').charAt(0).toUpperCase() }}
            </div>
            <div class="min-w-0">
              <div class="text-slate-900 dark:text-white font-medium truncate">{{ u.name }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ u.email }}</div>
            </div>
          </RouterLink>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium capitalize" :class="statusClass(u.status)">{{ u.status || 'active' }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Balance</div><div class="font-medium text-slate-900 dark:text-white">{{ money(u) }}</div></div>
          <div><div class="text-[11px] text-slate-500">Joined</div><div class="text-slate-500 dark:text-slate-400">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—' }}</div></div>
          <div class="flex items-center gap-1.5">
            <ShieldCheck v-if="u.accountVerifyStatus === 'Verified'" class="w-4 h-4" :class="verifyClass(u.accountVerifyStatus)" />
            <ShieldOff v-else class="w-4 h-4 text-slate-300 dark:text-slate-600" />
            <span class="text-[11px] text-slate-500">KYC {{ u.accountVerifyStatus === 'Verified' ? 'verified' : 'unverified' }}</span>
          </div>
        </div>
        <div class="flex items-center gap-1 pt-2 border-t border-slate-100 dark:border-white/5">
          <RouterLink :to="{ name: 'admin.user-detail', params: { id: u.id } }" class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5 rounded-lg">
            <Eye class="w-3.5 h-3.5" /> View
          </RouterLink>
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-white/5"
            :class="u.status === 'blocked' ? 'text-emerald-600' : 'text-rose-500'" @click="toggleBlock(u)">
            <CheckCircle2 v-if="u.status === 'blocked'" class="w-3.5 h-3.5" />
            <Ban v-else class="w-3.5 h-3.5" />
            {{ u.status === 'blocked' ? 'Unblock' : 'Block' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && filtered.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">User</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium">KYC</th>
            <th class="py-3 px-4 font-medium text-right">Balance</th>
            <th class="py-3 px-4 font-medium">Joined</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in filtered" :key="u.id" class="border-b border-slate-100 dark:border-white/5 last:border-0 hover:bg-slate-50 dark:hover:bg-white/5">
            <td class="py-3 px-4">
              <RouterLink :to="{ name: 'admin.user-detail', params: { id: u.id } }" class="flex items-center gap-3 group">
                <div class="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white text-xs font-semibold shrink-0">
                  {{ (u.name || '?').charAt(0).toUpperCase() }}
                </div>
                <div class="min-w-0">
                  <div class="text-slate-900 dark:text-white font-medium group-hover:text-indigo-500 truncate">{{ u.name }}</div>
                  <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ u.email }}</div>
                </div>
              </RouterLink>
            </td>
            <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-medium capitalize" :class="statusClass(u.status)">{{ u.status || 'active' }}</span></td>
            <td class="py-3 px-4">
              <ShieldCheck v-if="u.accountVerifyStatus === 'Verified'" class="w-4 h-4" :class="verifyClass(u.accountVerifyStatus)" />
              <ShieldOff v-else class="w-4 h-4 text-slate-300 dark:text-slate-600" />
            </td>
            <td class="py-3 px-4 text-right font-medium text-slate-900 dark:text-white">{{ money(u) }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-1">
                <RouterLink :to="{ name: 'admin.user-detail', params: { id: u.id } }" class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" title="View">
                  <Eye class="w-4 h-4" />
                </RouterLink>
                <button class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5"
                  :class="u.status === 'blocked' ? 'text-emerald-500' : 'text-rose-500'"
                  :title="u.status === 'blocked' ? 'Unblock' : 'Block'" @click="toggleBlock(u)">
                  <CheckCircle2 v-if="u.status === 'blocked'" class="w-4 h-4" />
                  <Ban v-else class="w-4 h-4" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add user modal -->
    <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showAddModal = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Add User</h2>
        <form class="space-y-3" @submit.prevent="createUser">
          <input v-model="form.name" required placeholder="Full name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.username" required placeholder="Username" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.email" type="email" required placeholder="Email address" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="form.password" type="password" required minlength="8" placeholder="Password (min 8 chars)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showAddModal = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Creating…' : 'Create User' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
