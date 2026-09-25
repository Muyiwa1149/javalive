<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Mail, Search, Send, Users2 } from 'lucide-vue-next'
import api from '@/lib/api'

const CATEGORIES = [
  { value: 'All', label: 'All Users', hint: 'Every registered account' },
  { value: 'No active plans', label: 'No active plans', hint: 'Users without a currently active investment' },
  { value: 'No deposit', label: 'No deposit', hint: 'Users who have never made a deposit' },
  { value: 'Select Users', label: 'Select Users', hint: 'Choose specific recipients below' },
]

const loadingUsers = ref(true)
const users = ref([])
const userSearch = ref('')
const selectedUserIds = ref(new Set())

const form = ref({
  category: 'All',
  greet: 'Hello',
  title: 'Investor',
  subject: '',
  message: '',
})
const sending = ref(false)

onMounted(async () => {
  try {
    const { data } = await api.get('/admin/users')
    users.value = data
  } finally {
    loadingUsers.value = false
  }
})

const filteredUsers = computed(() => {
  if (!userSearch.value.trim()) return users.value
  const q = userSearch.value.toLowerCase()
  return users.value.filter((u) => [u.name, u.email].some((f) => f && f.toLowerCase().includes(q)))
})

function toggleUser(id) {
  const next = new Set(selectedUserIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedUserIds.value = next
}

function selectAllFiltered() {
  const next = new Set(selectedUserIds.value)
  filteredUsers.value.forEach((u) => next.add(u.id))
  selectedUserIds.value = next
}

function clearSelection() {
  selectedUserIds.value = new Set()
}

const recipientCountLabel = computed(() => {
  if (form.value.category === 'Select Users') return `${selectedUserIds.value.size} selected`
  if (form.value.category === 'All') return `${users.value.length} users`
  return null
})

async function send() {
  if (form.value.category === 'Select Users' && selectedUserIds.value.size === 0) {
    Swal.fire({ icon: 'warning', title: 'Pick at least one user', text: 'Select Users is chosen but no recipients are checked.' })
    return
  }

  const confirm = await Swal.fire({
    icon: 'question',
    title: 'Send this email?',
    text: `This will send to: ${CATEGORIES.find((c) => c.value === form.value.category)?.label}${recipientCountLabel.value ? ' (' + recipientCountLabel.value + ')' : ''}.`,
    showCancelButton: true, confirmButtonText: 'Send', confirmButtonColor: '#6366f1',
  })
  if (!confirm.isConfirmed) return

  sending.value = true
  try {
    const { data } = await api.post('/admin/users/email-segment', {
      category: form.value.category,
      userIds: Array.from(selectedUserIds.value),
      subject: form.value.subject,
      message: form.value.message,
      greet: form.value.greet,
      title: form.value.title,
    })
    Swal.fire({ icon: 'success', title: 'Sent', text: `Email sent to ${data.sent} recipient${data.sent === 1 ? '' : 's'}.` })
    form.value.subject = ''
    form.value.message = ''
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed to send', text: e.response?.data?.message })
  } finally {
    sending.value = false
  }
}

const inputCls = 'w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/40 focus:border-indigo-500'
const labelCls = 'text-xs font-medium text-slate-500 dark:text-slate-400 mb-1 block'
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-5 sm:space-y-6 max-w-3xl">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Mail class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Email Services</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">Send a bulk email to all users or a targeted segment</p>
    </div>

    <form class="space-y-5" @submit.prevent="send">
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-3">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><Users2 class="w-4 h-4 text-indigo-500" /> Recipients</h2>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <label v-for="c in CATEGORIES" :key="c.value"
            class="flex items-start gap-2.5 p-3 rounded-xl border cursor-pointer transition-colors"
            :class="form.category === c.value ? 'border-indigo-500 bg-indigo-500/5' : 'border-slate-200 dark:border-white/10 hover:bg-slate-50 dark:hover:bg-white/5'">
            <input v-model="form.category" type="radio" :value="c.value" class="mt-0.5" />
            <span>
              <span class="block text-sm font-medium text-slate-800 dark:text-slate-200">{{ c.label }}</span>
              <span class="block text-xs text-slate-500 dark:text-slate-400">{{ c.hint }}</span>
            </span>
          </label>
        </div>

        <div v-if="form.category === 'Select Users'" class="pt-2 border-t border-slate-100 dark:border-white/5 space-y-2">
          <div class="flex items-center justify-between gap-2">
            <div class="relative flex-1">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
              <input v-model="userSearch" type="text" placeholder="Search name or email…" class="w-full pl-8 pr-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            </div>
            <span class="text-xs text-slate-500 dark:text-slate-400 whitespace-nowrap">{{ selectedUserIds.size }} selected</span>
          </div>
          <div class="flex gap-2 text-xs">
            <button type="button" class="text-indigo-500 hover:underline" @click="selectAllFiltered">Select all shown</button>
            <span class="text-slate-300 dark:text-slate-600">·</span>
            <button type="button" class="text-slate-500 hover:underline" @click="clearSelection">Clear</button>
          </div>
          <div v-if="loadingUsers" class="text-sm text-slate-500 dark:text-slate-400 py-3">Loading users…</div>
          <div v-else class="max-h-64 overflow-y-auto rounded-xl border border-slate-200 dark:border-white/10 divide-y divide-slate-100 dark:divide-white/5">
            <label v-for="u in filteredUsers" :key="u.id" class="flex items-center gap-2.5 px-3 py-2 hover:bg-slate-50 dark:hover:bg-white/5 cursor-pointer">
              <input type="checkbox" :checked="selectedUserIds.has(u.id)" class="rounded" @change="toggleUser(u.id)" />
              <span class="min-w-0 flex-1">
                <span class="block text-sm text-slate-800 dark:text-slate-200 truncate">{{ u.name }}</span>
                <span class="block text-xs text-slate-500 dark:text-slate-400 truncate">{{ u.email }}</span>
              </span>
            </label>
            <div v-if="filteredUsers.length === 0" class="px-3 py-4 text-sm text-slate-500 dark:text-slate-400 text-center">No users match.</div>
          </div>
        </div>

        <p v-else-if="recipientCountLabel" class="text-xs text-slate-500 dark:text-slate-400 pt-1">Will send to {{ recipientCountLabel }}.</p>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-3">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><Mail class="w-4 h-4 text-indigo-500" /> Message</h2>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label :class="labelCls">Greeting</label>
            <input v-model="form.greet" :class="inputCls" placeholder="Hello" />
          </div>
          <div>
            <label :class="labelCls">Salutation</label>
            <input v-model="form.title" :class="inputCls" placeholder="Investor" />
          </div>
        </div>
        <p class="text-xs text-slate-500 dark:text-slate-400 -mt-1">Renders as "{{ form.greet || 'Hello' }} {{ form.title || 'Investor' }}," at the top of the email.</p>

        <div>
          <label :class="labelCls">Subject</label>
          <input v-model="form.subject" required :class="inputCls" placeholder="e.g. Platform maintenance notice" />
        </div>
        <div>
          <label :class="labelCls">Message</label>
          <textarea v-model="form.message" required rows="8" :class="inputCls" placeholder="Write your message…"></textarea>
        </div>
      </div>

      <div class="flex justify-end">
        <button type="submit" :disabled="sending" class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50 shadow-lg shadow-indigo-500/20">
          <Send class="w-4 h-4" /> {{ sending ? 'Sending…' : 'Send Email' }}
        </button>
      </div>
    </form>
  </div>
</template>
