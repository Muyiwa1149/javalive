import { defineStore } from 'pinia'
import api from '@/lib/api'

export const useAuthUserStore = defineStore('authUser', {
  state: () => ({
    user: null,
    token: localStorage.getItem('user_token') || null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(credentials) {
      const { data } = await api.post('/auth/login', credentials)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('user_token', data.token)
      return data
    },
    async register(payload) {
      const { data } = await api.post('/auth/register', payload)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('user_token', data.token)
      return data
    },
    async fetchProfile() {
      const { data } = await api.get('/auth/me')
      this.user = data
      return data
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('user_token')
    },
  },
})
