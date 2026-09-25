import axios from 'axios'
import router from '@/router'
import { useAuthUserStore } from '@/stores/authUser'
import { useAuthAdminStore } from '@/stores/authAdmin'

// Login/2FA-verify calls legitimately return 401 for wrong credentials or an expired pending-2FA
// token — that's a form validation error the page itself already displays, not a live session
// going bad, so those endpoints are excluded from the auto-logout-and-redirect handling below.
const AUTH_ENDPOINTS = ['/auth/login', '/auth/register', '/auth/2fa/verify', '/admin/auth/login', '/admin/auth/2fa']
function isAuthEndpoint(url) {
  return AUTH_ENDPOINTS.some((path) => url?.startsWith(path))
}

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  // A caller-supplied Authorization header (e.g. the admin 2FA pending-token exchange, which must
  // use a short-lived token that isn't the stored admin_token) always wins over the stored token.
  if (config.headers.Authorization) {
    return config
  }
  const isAdminRequest = config.url?.startsWith('/admin')
  const token = isAdminRequest
    ? localStorage.getItem('admin_token')
    : localStorage.getItem('user_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isAuthEndpoint(error.config?.url)) {
      const isAdminRequest = error.config?.url?.startsWith('/admin')
      const loginRouteName = isAdminRequest ? 'admin.login' : 'login'

      if (isAdminRequest) {
        useAuthAdminStore().logout()
      } else {
        useAuthUserStore().logout()
      }

      if (router.currentRoute.value.name !== loginRouteName) {
        router.push({ name: loginRouteName, query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    return Promise.reject(error)
  },
)

export default api
