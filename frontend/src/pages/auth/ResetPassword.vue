<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, Key, AlertCircle, ShieldCheck } from 'lucide-vue-next'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'
import api from '@/lib/api'

const route = useRoute()
const router = useRouter()
const settingsStore = usePublicSettingsStore()

const email = ref('')
const token = ref('')
const password = ref('')
const passwordConfirmation = ref('')
const submitting = ref(false)
const error = ref('')
const year = new Date().getFullYear()

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
    await api.post('/auth/reset-password', { email: email.value, token: token.value, password: password.value })
    router.push({ name: 'login' })
  } catch (e) {
    error.value = e.response?.data?.message || 'This reset link is invalid or has expired.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center p-4">
    <div class="w-full max-w-md space-y-8">
      <div class="text-center space-y-6">
        <div class="flex justify-center">
          <RouterLink to="/" class="inline-block">
            <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-12 sm:h-16 w-auto" :alt="settingsStore.settings?.siteName">
          </RouterLink>
        </div>
        <div class="space-y-3">
          <div class="flex items-center justify-center gap-2 mb-4">
            <div class="p-2 bg-blue-500/20 rounded-lg"><ShieldCheck class="w-6 h-6 text-blue-400" /></div>
            <h1 class="text-2xl sm:text-3xl font-bold text-white">Reset Password</h1>
          </div>
          <p class="text-gray-400 text-sm sm:text-base max-w-sm mx-auto leading-relaxed">
            Choose a new password for your trading account.
          </p>
        </div>
      </div>

      <div class="bg-gray-900 backdrop-blur-sm border border-gray-700 rounded-2xl p-6 sm:p-8 space-y-6">
        <div v-if="!token || !email" class="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl">
          <p class="text-amber-300 text-sm font-bold">This reset link is missing required information. Please request a new one from the forgot-password page.</p>
        </div>

        <template v-else>
          <div v-if="error" class="p-4 bg-red-500/10 border border-red-500/20 rounded-xl">
            <div class="flex items-center gap-3">
              <AlertCircle class="w-5 h-5 text-red-400" />
              <p class="text-red-300 text-sm font-bold">{{ error }}</p>
            </div>
          </div>

          <form class="space-y-6" @submit.prevent="submit">
            <div class="space-y-2">
              <label for="password" class="block text-sm font-bold text-gray-200">New Password <span class="text-red-400">*</span></label>
              <div class="relative group">
                <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Lock class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                <input id="password" v-model="password" type="password" required
                  class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                  placeholder="At least 8 characters">
              </div>
            </div>
            <div class="space-y-2">
              <label for="password_confirmation" class="block text-sm font-bold text-gray-200">Confirm New Password <span class="text-red-400">*</span></label>
              <div class="relative group">
                <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Key class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                <input id="password_confirmation" v-model="passwordConfirmation" type="password" required
                  class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                  placeholder="Confirm your new password">
              </div>
            </div>

            <button type="submit" :disabled="submitting"
              class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 focus:ring-2 focus:ring-blue-400/20 disabled:opacity-50">
              <span>{{ submitting ? 'Resetting…' : 'Reset Password' }}</span>
            </button>
          </form>
        </template>

        <div class="text-center pt-4 border-t border-gray-700">
          <p class="text-gray-400 text-sm">
            Remember your password?
            <RouterLink to="/login" class="font-bold text-blue-400 hover:text-blue-300 transition-colors underline underline-offset-2">Back to Login</RouterLink>
          </p>
        </div>
      </div>

      <div class="text-center">
        <p class="text-xs text-gray-500">© {{ year }} {{ settingsStore.settings?.siteName }}. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
