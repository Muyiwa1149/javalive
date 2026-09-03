<script setup>
import { ref, onMounted } from 'vue'
import { Bell, BellOff, Trash2, AlertTriangle, CheckCircle, AlertOctagon, Info } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const items = ref([])
const unreadCount = ref(0)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/notifications')
    items.value = data
    unreadCount.value = data.filter((n) => !n.isRead).length
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function markRead(n) {
  await api.post(`/admin/notifications/${n.id}/mark-read`)
  n.isRead = true
  unreadCount.value = items.value.filter((i) => !i.isRead).length
}

async function markAllRead() {
  await api.post('/admin/notifications/mark-all-read')
  items.value.forEach((n) => { n.isRead = true })
  unreadCount.value = 0
}

async function remove(n) {
  await api.delete(`/admin/notifications/${n.id}`)
  items.value = items.value.filter((i) => i.id !== n.id)
  unreadCount.value = items.value.filter((i) => !i.isRead).length
}

const typeIcon = (type) => ({ warning: AlertTriangle, success: CheckCircle, danger: AlertOctagon }[type] || Info)
const typeClass = (type) => ({
  warning: 'bg-amber-100 text-amber-600 dark:bg-amber-500/10 dark:text-amber-400',
  success: 'bg-emerald-100 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400',
  danger: 'bg-rose-100 text-rose-600 dark:bg-rose-500/10 dark:text-rose-400',
}[type] || 'bg-indigo-100 text-indigo-600 dark:bg-indigo-500/10 dark:text-indigo-400')
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-3xl">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><Bell class="w-6 h-6 text-indigo-500" /> Notifications</h1>
      <button v-if="unreadCount > 0" class="text-sm text-indigo-500 hover:underline" @click="markAllRead">Mark all read</button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="items.length === 0" class="text-center py-16">
      <BellOff class="h-12 w-12 mx-auto text-slate-300 dark:text-slate-600" />
      <p class="text-slate-500 dark:text-slate-400 mt-4">No notifications yet</p>
    </div>

    <ul v-else class="space-y-2">
      <li v-for="n in items" :key="n.id"
        class="flex items-start gap-3 p-4 rounded-2xl border border-slate-200 dark:border-white/5 bg-white dark:bg-[#0F1524]"
        :class="!n.isRead ? 'ring-1 ring-indigo-200 dark:ring-indigo-800' : ''">
        <span class="flex h-9 w-9 rounded-full items-center justify-center flex-shrink-0" :class="typeClass(n.type)">
          <component :is="typeIcon(n.type)" class="w-4 h-4" />
        </span>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-slate-900 dark:text-white" :class="!n.isRead ? 'font-semibold' : ''">{{ n.title }}</p>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">{{ n.message }}</p>
          <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">{{ new Date(n.createdAt).toLocaleString() }}</p>
        </div>
        <div class="flex items-center gap-2 flex-shrink-0">
          <button v-if="!n.isRead" class="text-xs text-indigo-500 hover:underline" @click="markRead(n)">Mark read</button>
          <button class="text-slate-400 hover:text-rose-500" @click="remove(n)"><Trash2 class="w-4 h-4" /></button>
        </div>
      </li>
    </ul>
  </div>
</template>
