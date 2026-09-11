import { defineStore } from 'pinia'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

// index.html ships a generic placeholder favicon.svg (leftover scaffolding, not the real site
// branding) — this swaps in the admin-configured favicon from settings as soon as it's known, on
// every layout (public, auth, user dashboard, admin), not just the public site.
function applyFavicon(faviconPath) {
  if (!faviconPath) return
  const href = storageUrl(faviconPath)
  const ext = faviconPath.split('.').pop()?.toLowerCase()
  const mime = { png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg', svg: 'image/svg+xml', ico: 'image/x-icon' }[ext] || 'image/png'
  let link = document.querySelector('link[rel="icon"]')
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    document.head.appendChild(link)
  }
  link.type = mime
  link.href = href
}

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
      applyFavicon(data.favicon)
      return data
    },
  },
})
