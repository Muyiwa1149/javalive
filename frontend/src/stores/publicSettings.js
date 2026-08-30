import { defineStore } from 'pinia'
import api from '@/lib/api'

/** Site branding + feature toggles from GET /api/public/settings — fetched once, shared by the public layout and every public page. */
export const usePublicSettingsStore = defineStore('publicSettings', {
  state: () => ({
    settings: null,
    loaded: false,
  }),
  actions: {
    async ensureLoaded() {
      if (this.loaded) return this.settings
      const { data } = await api.get('/public/settings')
      this.settings = data
      this.loaded = true
      return data
    },
  },
})
