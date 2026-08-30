import axios from 'axios'

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
    if (error.response?.status === 401) {
      const isAdminRequest = error.config?.url?.startsWith('/admin')
      if (isAdminRequest) {
        localStorage.removeItem('admin_token')
      } else {
        localStorage.removeItem('user_token')
      }
    }
    return Promise.reject(error)
  },
)

export default api
