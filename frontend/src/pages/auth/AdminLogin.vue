<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShieldCheck, Mail, Lock, Eye, EyeOff, AlertCircle, Info } from 'lucide-vue-next'
import { useAuthAdminStore } from '@/stores/authAdmin'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'

const router = useRouter()
const route = useRoute()
const authAdmin = useAuthAdminStore()
const settingsStore = usePublicSettingsStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const submitting = ref(false)
const error = ref('')

onMounted(() => {
  settingsStore.ensureLoaded()
  if (authAdmin.isAuthenticated) {
    router.replace({ name: 'admin.dashboard' })
  }
})

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    const data = await authAdmin.login({ email: email.value, password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    if (data.twoFactorRequired) {
      router.push({ name: 'admin.two-factor', query: redirect ? { redirect } : {} })
    } else {
      router.push(redirect || { name: 'admin.dashboard' })
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid email or password.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-gray-900">
    <div class="max-w-md w-full space-y-8">
      <div class="bg-gray-900 border border-gray-700 rounded-2xl p-8 shadow-2xl">
        <div class="text-center mb-8">
          <RouterLink to="/" class="inline-block group">
            <div class="relative">
              <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-16 w-auto mx-auto transition-transform duration-300 group-hover:scale-105" :alt="settingsStore.settings?.siteName">
              <div class="absolute inset-0 bg-gradient-to-r from-blue-500/20 to-indigo-500/20 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
            </div>
          </RouterLink>

          <div class="mt-4 inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-red-500/10 text-red-400 border border-red-500/20">
            <ShieldCheck class="w-3 h-3 mr-2" />
            Administrator Access
          </div>
        </div>

        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-white mb-2">Admin Sign In</h1>
          <p class="text-gray-400 text-sm">Secure access to the trading platform management</p>
        </div>

        <div v-if="error" class="mb-6 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400">
          <div class="flex items-center">
            <AlertCircle class="w-5 h-5 mr-3 flex-shrink-0" />
            <span class="text-sm font-medium">{{ error }}</span>
          </div>
        </div>

        <form class="space-y-6" @submit.prevent="submit">
          <div>
            <label for="email" class="block text-sm font-bold text-gray-200 mb-3">Email Address</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Mail class="h-5 w-5 text-gray-400" />
              </div>
              <input id="email" v-model="email" type="email" autocomplete="email" required autofocus
                class="block w-full pl-12 pr-4 py-4 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-base font-medium"
                placeholder="admin@example.com">
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-3">
              <label for="password" class="block text-sm font-bold text-gray-200">Password</label>
            </div>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Lock class="h-5 w-5 text-gray-400" />
              </div>
              <input id="password" v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" required
                class="block w-full pl-12 pr-12 py-4 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 text-base font-medium"
                placeholder="Enter your password">
              <button type="button" @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-300 transition-colors duration-200">
                <Eye v-if="!showPassword" class="h-5 w-5" />
                <EyeOff v-else class="h-5 w-5" />
              </button>
            </div>
          </div>

          <div class="pt-4">
            <button type="submit" :disabled="submitting"
              class="group relative w-full flex justify-center py-4 px-4 border border-transparent text-base font-bold rounded-xl text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none">
              <span class="absolute left-0 inset-y-0 flex items-center pl-4">
                <ShieldCheck class="h-5 w-5 text-blue-300 group-hover:text-blue-200 transition-colors duration-200" />
              </span>
              {{ submitting ? 'Signing in…' : 'Sign In to Admin Panel' }}
            </button>
          </div>

          <div class="mt-6 p-4 bg-yellow-500/10 border border-yellow-500/20 rounded-lg">
            <div class="flex items-start">
              <Info class="w-5 h-5 text-yellow-400 mr-3 mt-0.5 flex-shrink-0" />
              <div class="text-sm text-yellow-300">
                <p class="font-medium mb-1">Security Notice</p>
                <p class="text-yellow-400">This is a restricted area. All access attempts are logged and monitored.</p>
              </div>
            </div>
          </div>
        </form>
      </div>

      <div class="text-center">
        <p class="text-gray-500 text-sm">
          Need help? Contact <a href="mailto:support@company.com" class="text-blue-400 hover:text-blue-300 font-medium">technical support</a>
        </p>
      </div>
    </div>
  </div>
</template>
