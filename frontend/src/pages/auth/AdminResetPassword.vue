<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ShieldAlert, AlertCircle, Lock, Key, Eye, EyeOff, ShieldCheck, ArrowLeft, Shield, Eye as EyeIcon,
} from 'lucide-vue-next'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'
import api from '@/lib/api'

// The source blade page has admins manually type a 10-digit numeric token mailed to them. Our
// backend issues a UUID reset token embedded in the emailed link (`{frontendUrl}/admin/reset-password
// ?token=...&email=...`), the same scheme the user-facing ResetPassword.vue already follows — so
// this page reads email/token from the query string instead of asking the admin to hand-type a token.
const route = useRoute()
const router = useRouter()
const settingsStore = usePublicSettingsStore()

const email = ref('')
const token = ref('')
const password = ref('')
const passwordConfirmation = ref('')
const showPassword = ref(false)
const showPasswordConfirmation = ref(false)
const submitting = ref(false)
const error = ref('')

onMounted(() => {
  settingsStore.ensureLoaded()
  email.value = typeof route.query.email === 'string' ? route.query.email : ''
  token.value = typeof route.query.token === 'string' ? route.query.token : ''
})

async function submit() {
  error.value = ''
  if (password.value !== passwordConfirmation.value) {
    error.value = 'Passwords do not match.'
    return
  }
  if (password.value.length < 8) {
    error.value = 'Password must be at least 8 characters.'
    return
  }
  submitting.value = true
  try {
    await api.post('/admin/auth/reset-password', { email: email.value, token: token.value, password: password.value })
    router.push({ name: 'admin.login' })
  } catch (e) {
    error.value = e.response?.data?.message || 'This reset link is invalid or has expired.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
    <div class="max-w-md w-full space-y-8 relative z-10">
      <div class="bg-gray-900 rounded-2xl p-8 shadow-2xl border border-gray-700">
        <div class="text-center mb-8">
          <div class="mb-6">
            <RouterLink to="/" class="inline-block">
              <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-12 md:h-16 mx-auto" :alt="settingsStore.settings?.siteName">
            </RouterLink>
          </div>
          <div class="inline-flex items-center gap-2 bg-red-500/10 border border-red-500/20 rounded-full px-4 py-2 mb-4">
            <ShieldAlert class="w-4 h-4 text-red-400" />
            <span class="text-red-300 text-sm font-bold">Admin Recovery</span>
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">Reset Admin Password</h1>
          <p class="text-gray-400 text-sm md:text-base">Secure password recovery for administrative access</p>
        </div>

        <div v-if="!token || !email" class="mb-6 p-4 bg-yellow-500/10 border border-yellow-500/20 rounded-xl">
          <div class="flex items-start gap-3">
            <ShieldAlert class="w-5 h-5 text-yellow-400 mt-0.5 flex-shrink-0" />
            <div class="text-sm">
              <p class="text-yellow-300 font-bold mb-1">Security Notice</p>
              <p class="text-gray-300">This reset link is missing required information. Please request a new one from the admin forgot-password page.</p>
            </div>
          </div>
        </div>

        <template v-else>
          <div v-if="error" class="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-xl">
            <div class="flex items-center gap-3">
              <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
              <p class="text-red-300 text-sm font-bold">{{ error }}</p>
            </div>
          </div>

          <form class="space-y-6" @submit.prevent="submit">
            <div class="space-y-2">
              <label for="password" class="block text-sm font-bold text-gray-200">New Password</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Lock class="h-5 w-5 text-gray-400" /></div>
                <input id="password" v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" required
                  class="w-full pl-12 pr-12 py-3 bg-gray-900 border border-gray-600 rounded-xl text-white placeholder-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none transition-all duration-200"
                  placeholder="Create a strong password">
                <button type="button" class="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-300" @click="showPassword = !showPassword">
                  <Eye v-if="!showPassword" class="h-5 w-5" />
                  <EyeOff v-else class="h-5 w-5" />
                </button>
              </div>
            </div>

            <div class="space-y-2">
              <label for="password_confirmation" class="block text-sm font-bold text-gray-200">Confirm New Password</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Key class="h-5 w-5 text-gray-400" /></div>
                <input id="password_confirmation" v-model="passwordConfirmation" :type="showPasswordConfirmation ? 'text' : 'password'" autocomplete="new-password" required
                  class="w-full pl-12 pr-12 py-3 bg-gray-900 border border-gray-600 rounded-xl text-white placeholder-gray-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none transition-all duration-200"
                  placeholder="Confirm your password">
                <button type="button" class="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-300" @click="showPasswordConfirmation = !showPasswordConfirmation">
                  <EyeIcon v-if="!showPasswordConfirmation" class="h-5 w-5" />
                  <EyeOff v-else class="h-5 w-5" />
                </button>
              </div>
            </div>

            <div class="bg-gray-800/50 rounded-lg p-4 border border-gray-700">
              <h4 class="text-white font-bold text-sm mb-2 flex items-center gap-2">
                <ShieldCheck class="w-4 h-4 text-green-400" />
                Password Requirements
              </h4>
              <ul class="text-gray-300 text-xs space-y-1">
                <li class="flex items-start gap-2"><span class="text-green-400 mt-1">•</span>Minimum 8 characters long</li>
                <li class="flex items-start gap-2"><span class="text-green-400 mt-1">•</span>Include uppercase and lowercase letters</li>
                <li class="flex items-start gap-2"><span class="text-green-400 mt-1">•</span>Include at least one number</li>
                <li class="flex items-start gap-2"><span class="text-green-400 mt-1">•</span>Include at least one special character</li>
              </ul>
            </div>

            <button type="submit" :disabled="submitting"
              class="w-full bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 text-white font-bold py-3 px-6 rounded-xl transition-all duration-200 hover:shadow-lg hover:shadow-red-500/25 focus:outline-none focus:ring-2 focus:ring-red-500/50 disabled:opacity-50">
              <span class="flex items-center justify-center gap-2">
                <ShieldCheck class="w-5 h-5" />
                {{ submitting ? 'Resetting…' : 'Reset Admin Password' }}
              </span>
            </button>
          </form>
        </template>

        <div class="mt-6 text-center">
          <RouterLink :to="{ name: 'admin.login' }" class="inline-flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors duration-200">
            <ArrowLeft class="w-4 h-4" />
            Back to Admin Login
          </RouterLink>
        </div>

        <div class="mt-8 pt-6 border-t border-gray-700">
          <div class="text-center">
            <p class="text-xs text-gray-500 mb-2">Enterprise-grade admin security</p>
            <div class="flex items-center justify-center gap-4 text-gray-600">
              <span class="flex items-center gap-1"><Shield class="w-3 h-3" /><span class="text-xs">Admin Only</span></span>
              <span class="flex items-center gap-1"><Lock class="w-3 h-3" /><span class="text-xs">Token Verified</span></span>
              <span class="flex items-center gap-1"><EyeOff class="w-3 h-3" /><span class="text-xs">Secure Reset</span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="absolute inset-0 -z-10 overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-br from-red-900/5 via-gray-900 to-purple-900/5"></div>
        <div class="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 bg-red-500/5 rounded-full blur-3xl"></div>
        <div class="absolute bottom-0 right-1/2 translate-x-1/2 w-96 h-96 bg-purple-500/5 rounded-full blur-3xl"></div>
      </div>
    </div>
  </div>
</template>
