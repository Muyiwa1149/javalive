import { defineStore } from 'pinia'
import api from '@/lib/api'

export const useAuthAdminStore = defineStore('authAdmin', {
  state: () => ({
    admin: null,
    token: localStorage.getItem('admin_token') || null,
    twoFactorPending: false,
    pendingToken: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(credentials) {
      const { data } = await api.post('/admin/auth/login', credentials)
      if (data.twoFactorRequired) {
        this.twoFactorPending = true
        this.pendingToken = data.token
        return data
      }
      this.token = data.token
      this.admin = data.admin
      localStorage.setItem('admin_token', data.token)
      return data
    },
    async verifyTwoFactor(code) {
      const { data } = await api.post('/admin/auth/2fa', { code }, {
        headers: { Authorization: `Bearer ${this.pendingToken}` },
      })
      this.twoFactorPending = false
      this.pendingToken = null
      this.token = data.token
      this.admin = data.admin
      localStorage.setItem('admin_token', data.token)
      return data
    },
    async fetchProfile() {
      const { data } = await api.get('/admin/auth/me')
      this.admin = data
      return data
    },
    logout() {
      this.token = null
      this.admin = null
      this.twoFactorPending = false
      this.pendingToken = null
      localStorage.removeItem('admin_token')
    },
  },
})
