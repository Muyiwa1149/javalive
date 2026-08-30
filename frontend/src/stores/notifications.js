import { defineStore } from 'pinia'
import api from '@/lib/api'

/** Backs the topbar notification bell and the full Notifications page — GET /api/notifications*. */
export const useNotificationsStore = defineStore('notifications', {
  state: () => ({
    items: [],
    unreadCount: 0,
    loaded: false,
  }),
  actions: {
    async fetchAll() {
      const { data } = await api.get('/notifications')
      this.items = data
      this.loaded = true
      return data
    },
    async fetchCount() {
      const { data } = await api.get('/notifications/count')
      this.unreadCount = data.unread
      return data.unread
    },
    async markRead(id) {
      await api.post(`/notifications/${id}/mark-read`)
      const item = this.items.find((n) => n.id === id)
      if (item) item.isRead = true
      await this.fetchCount()
    },
    async markAllRead() {
      await api.post('/notifications/mark-all-read')
      this.items.forEach((n) => { n.isRead = true })
      this.unreadCount = 0
    },
    async remove(id) {
      await api.delete(`/notifications/${id}`)
      this.items = this.items.filter((n) => n.id !== id)
      await this.fetchCount()
    },
  },
})
