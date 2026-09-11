<script setup>
import { onMounted } from 'vue'
import { Bell, BellOff, Trash2, AlertTriangle, CheckCircle, AlertOctagon, Info } from 'lucide-vue-next'
import { useNotificationsStore } from '@/stores/notifications'

const notifications = useNotificationsStore()

onMounted(() => {
  notifications.fetchAll()
})

const typeIcon = (type) => ({
  warning: AlertTriangle, success: CheckCircle, danger: AlertOctagon,
}[type] || Info)
const typeClass = (type) => ({
  warning: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-500',
  success: 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-500',
  danger: 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-500',
}[type] || 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-500')
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-2">
        <Bell class="w-6 h-6 text-blue-500" />
        <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Notifications</h1>
      </div>
      <button v-if="notifications.unreadCount > 0" class="text-sm text-blue-600 dark:text-blue-400 hover:underline" @click="notifications.markAllRead()">
        Mark all read
      </button>
    </div>

    <div v-if="!notifications.loaded" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <div v-else-if="notifications.items.length === 0" class="text-center py-16">
      <BellOff class="h-12 w-12 mx-auto text-gray-400" />
      <p class="text-gray-600 dark:text-gray-400 mt-4">No notifications yet</p>
    </div>

    <ul v-else class="space-y-2">
      <li v-for="n in notifications.items" :key="n.id"
        class="flex items-start gap-3 p-4 rounded-xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900"
        :class="!n.isRead ? 'ring-1 ring-blue-200 dark:ring-blue-800' : ''">
        <span class="flex h-9 w-9 rounded-full items-center justify-center flex-shrink-0" :class="typeClass(n.type)">
          <component :is="typeIcon(n.type)" class="w-4 h-4" />
        </span>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-900 dark:text-white" :class="!n.isRead ? 'font-semibold' : ''">{{ n.title }}</p>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ n.message }}</p>
          <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">{{ new Date(n.createdAt).toLocaleString() }}</p>
        </div>
        <div class="flex items-center gap-2 flex-shrink-0">
          <button v-if="!n.isRead" class="text-xs text-blue-600 dark:text-blue-400 hover:underline" @click="notifications.markRead(n.id)">Mark read</button>
          <button class="text-gray-400 hover:text-red-500" @click="notifications.remove(n.id)"><Trash2 class="w-4 h-4" /></button>
        </div>
      </li>
    </ul>
  </div>
</template>
