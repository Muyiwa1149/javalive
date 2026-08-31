<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import Swal from 'sweetalert2'
import {
  ArrowLeft, ChevronDown, Ban, CheckCircle2, MailCheck, KeyRound, Pencil, LogIn, Trash2, Send, Bell,
  History, Wallet, TrendingUp, Gift, Users2, ShieldCheck, ShieldOff, Activity as ActivityIcon,
} from 'lucide-vue-next'
import api from '@/lib/api'

const route = useRoute()
const router = useRouter()
const userId = route.params.id

const loading = ref(true)
const user = ref(null)
const activity = ref([])
const activityLoaded = ref(false)
const menuOpen = ref(false)

const showEdit = ref(false)
const showMail = ref(false)
const showNotify = ref(false)
const showActivity = ref(false)
const editForm = ref({ name: '', email: '', username: '', phone: '', country: '', currencySymbol: '', currencyCode: '' })
const mailForm = ref({ subject: '', message: '' })
const notifyForm = ref({ message: '' })
const busy = ref(false)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get(`/admin/users/${userId}`)
    user.value = data
    editForm.value = { name: data.name, email: data.email, username: data.username, phone: data.phone || '', country: data.country || '', currencySymbol: data.currencySymbol, currencyCode: data.currencyCode }
  } finally {
    loading.value = false
  }
}
onMounted(load)

function money(value) {
  return `${user.value?.currencySymbol || '$'}${Number(value ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}`
}

const tradeModeLabel = computed(() => (user.value?.tradeType === 'Loss' || !user.value?.tradeMode) ? 'Loss' : 'Profit')

async function toggleBlock() {
  const blocking = user.value.status !== 'blocked'
  const confirm = await Swal.fire({
    icon: 'question', title: blocking ? 'Block this user?' : 'Unblock this user?',
    showCancelButton: true, confirmButtonText: blocking ? 'Block' : 'Unblock',
    confirmButtonColor: blocking ? '#e11d48' : '#6366f1',
  })
  if (!confirm.isConfirmed) return
  await api.post(`/admin/users/${userId}/${blocking ? 'block' : 'unblock'}`)
  await load()
  menuOpen.value = false
}

async function verifyEmail() {
  await api.post(`/admin/users/${userId}/verify-email`)
  await load()
  menuOpen.value = false
  Swal.fire({ icon: 'success', title: 'Email verified', timer: 1200, showConfirmButton: false })
}

async function resetPassword() {
  const confirm = await Swal.fire({
    icon: 'warning', title: 'Reset password to default?', text: 'The user\'s password will be reset to "user01236".',
    showCancelButton: true, confirmButtonText: 'Reset',
  })
  if (!confirm.isConfirmed) return
  await api.post(`/admin/users/${userId}/reset-password`)
  menuOpen.value = false
  Swal.fire({ icon: 'success', title: 'Password reset', timer: 1500, showConfirmButton: false })
}

async function impersonate() {
  const { data } = await api.post(`/admin/users/${userId}/impersonate`)
  window.open(`${window.location.origin}/impersonate?token=${encodeURIComponent(data.token)}`, '_blank')
  menuOpen.value = false
}

async function saveEdit() {
  busy.value = true
  try {
    await api.put(`/admin/users/${userId}`, editForm.value)
    showEdit.value = false
    await load()
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    busy.value = false
  }
}

async function sendMail() {
  busy.value = true
  try {
    await api.post(`/admin/users/${userId}/mail`, mailForm.value)
    showMail.value = false
    mailForm.value = { subject: '', message: '' }
    Swal.fire({ icon: 'success', title: 'Email sent', timer: 1200, showConfirmButton: false })
  } finally {
    busy.value = false
  }
}

async function sendNotify() {
  busy.value = true
  try {
    await api.post(`/admin/users/${userId}/notify`, notifyForm.value)
    showNotify.value = false
    notifyForm.value = { message: '' }
    Swal.fire({ icon: 'success', title: 'Notification sent', timer: 1200, showConfirmButton: false })
  } finally {
    busy.value = false
  }
}

async function loadActivity() {
  showActivity.value = true
  menuOpen.value = false
  if (activityLoaded.value) return
  const { data } = await api.get(`/admin/users/${userId}/activity`)
  activity.value = data
  activityLoaded.value = true
}

async function clearActivity() {
  const confirm = await Swal.fire({ icon: 'warning', title: 'Clear all login activity?', showCancelButton: true, confirmButtonText: 'Clear' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/users/${userId}/activity`)
  activity.value = []
}

async function deleteUser() {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Delete ${user.value.name}?`,
    text: 'This permanently deletes the user and all associated deposits, withdrawals, plans, investments, and history. This cannot be undone.',
    showCancelButton: true, confirmButtonText: 'Delete permanently', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/users/${userId}`)
  router.push({ name: 'admin.users' })
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <RouterLink :to="{ name: 'admin.users' }" class="inline-flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400 hover:text-indigo-500">
      <ArrowLeft class="w-4 h-4" /> Back to users
    </RouterLink>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <template v-else-if="user">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white text-xl font-bold shrink-0">
            {{ (user.name || '?').charAt(0).toUpperCase() }}
          </div>
          <div>
            <h1 class="text-xl font-bold text-slate-900 dark:text-white">{{ user.name }}</h1>
            <p class="text-sm text-slate-500 dark:text-slate-400">@{{ user.username }} · {{ user.email }}</p>
          </div>
        </div>

        <div class="relative">
          <button class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-white text-sm font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="menuOpen = !menuOpen">
            Actions <ChevronDown class="w-4 h-4" :class="menuOpen ? 'rotate-180' : ''" />
          </button>
          <div v-if="menuOpen" class="absolute right-0 mt-2 w-64 bg-white dark:bg-[#141B2D] rounded-xl shadow-xl border border-slate-200 dark:border-white/10 z-20 p-1.5">
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="loadActivity">
              <History class="w-4 h-4" /> Login Activity
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="toggleBlock">
              <component :is="user.status === 'blocked' ? CheckCircle2 : Ban" class="w-4 h-4" /> {{ user.status === 'blocked' ? 'Unblock' : 'Block' }}
            </button>
            <button v-if="!user.emailVerified" class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="verifyEmail">
              <MailCheck class="w-4 h-4" /> Verify Email
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="resetPassword">
              <KeyRound class="w-4 h-4" /> Reset Password
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="showEdit = true; menuOpen = false">
              <Pencil class="w-4 h-4" /> Edit Details
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="showNotify = true; menuOpen = false">
              <Bell class="w-4 h-4" /> Notify Dashboard
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 dark:text-slate-200 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="showMail = true; menuOpen = false">
              <Send class="w-4 h-4" /> Send Email
            </button>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg" @click="impersonate">
              <LogIn class="w-4 h-4" /> Login as {{ user.name }}
            </button>
            <div class="border-t border-slate-200 dark:border-white/10 my-1.5"></div>
            <button class="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="deleteUser">
              <Trash2 class="w-4 h-4" /> Delete User
            </button>
          </div>
        </div>
      </div>

      <!-- Stat cards -->
      <div class="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Wallet class="w-3.5 h-3.5" /> Balance</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ money(user.accountBalance) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><TrendingUp class="w-3.5 h-3.5" /> Profit</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ money(user.roiBalance) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Gift class="w-3.5 h-3.5" /> Bonus</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ money(user.bonusBalance) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400"><Users2 class="w-3.5 h-3.5" /> Referral Bonus</div>
          <p class="mt-1.5 text-lg font-bold text-slate-900 dark:text-white">{{ money(user.referralBonusBalance) }}</p>
        </div>
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4">
          <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
            <component :is="user.accountVerifyStatus === 'Verified' ? ShieldCheck : ShieldOff" class="w-3.5 h-3.5" /> KYC
          </div>
          <p class="mt-1.5 text-sm font-semibold" :class="user.accountVerifyStatus === 'Verified' ? 'text-emerald-500' : 'text-slate-400'">{{ user.accountVerifyStatus || 'Not Verified' }}</p>
        </div>
      </div>

      <!-- User info -->
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl divide-y divide-slate-100 dark:divide-white/5">
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Full name</span><span class="font-medium text-slate-900 dark:text-white">{{ user.name }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Email</span><span class="font-medium text-slate-900 dark:text-white">{{ user.email }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Phone</span><span class="font-medium text-slate-900 dark:text-white">{{ user.phone || '—' }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Date of birth</span><span class="font-medium text-slate-900 dark:text-white">{{ user.dob || '—' }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Nationality</span><span class="font-medium text-slate-900 dark:text-white">{{ user.country || '—' }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Trade mode</span><span class="font-medium" :class="tradeModeLabel === 'Loss' ? 'text-rose-500' : 'text-emerald-500'">{{ tradeModeLabel }}</span></div>
        <div class="px-5 py-3 flex items-center justify-between text-sm"><span class="text-slate-500 dark:text-slate-400">Registered</span><span class="font-medium text-slate-900 dark:text-white">{{ user.createdAt ? new Date(user.createdAt).toLocaleString() : '—' }}</span></div>
      </div>
    </template>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showEdit = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Edit User</h2>
        <form class="space-y-3" @submit.prevent="saveEdit">
          <input v-model="editForm.name" required placeholder="Full name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="editForm.username" required placeholder="Username" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="editForm.email" type="email" required placeholder="Email" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="editForm.phone" placeholder="Phone" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="editForm.country" placeholder="Country" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-2 gap-3">
            <input v-model="editForm.currencySymbol" placeholder="Currency symbol ($)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="editForm.currencyCode" placeholder="Currency code (USD)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showEdit = false">Cancel</button>
            <button type="submit" :disabled="busy" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Mail modal -->
    <div v-if="showMail" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showMail = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Send Email</h2>
        <form class="space-y-3" @submit.prevent="sendMail">
          <input v-model="mailForm.subject" required placeholder="Subject" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="mailForm.message" required rows="5" placeholder="Message" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showMail = false">Cancel</button>
            <button type="submit" :disabled="busy" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Send</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Notify modal -->
    <div v-if="showNotify" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showNotify = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Notify Dashboard</h2>
        <form class="space-y-3" @submit.prevent="sendNotify">
          <textarea v-model="notifyForm.message" required rows="4" placeholder="Message shown in the user's notification bell" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showNotify = false">Cancel</button>
            <button type="submit" :disabled="busy" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Send</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Activity drawer -->
    <div v-if="showActivity" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showActivity = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 max-h-[80vh] flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-white flex items-center gap-2"><ActivityIcon class="w-5 h-5" /> Login Activity</h2>
          <button v-if="activity.length" class="text-xs text-rose-500 hover:underline" @click="clearActivity">Clear all</button>
        </div>
        <div class="overflow-y-auto space-y-2">
          <p v-if="activity.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No login activity recorded.</p>
          <div v-for="a in activity" :key="a.id" class="p-3 rounded-xl bg-slate-50 dark:bg-white/5 text-sm">
            <div class="flex justify-between text-slate-900 dark:text-white font-medium"><span>{{ a.ipAddress || 'Unknown IP' }}</span><span class="text-xs text-slate-400">{{ new Date(a.createdAt).toLocaleString() }}</span></div>
            <div class="text-xs text-slate-500 dark:text-slate-400 mt-1">{{ a.browser }} · {{ a.os }} · {{ a.device }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
