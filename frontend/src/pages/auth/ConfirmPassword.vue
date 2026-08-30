<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KeyRound, Lock, AlertCircle, ShieldCheck } from 'lucide-vue-next'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'
import api from '@/lib/api'

const route = useRoute()
const router = useRouter()
const settingsStore = usePublicSettingsStore()

const password = ref('')
const submitting = ref(false)
const error = ref('')
const year = new Date().getFullYear()

onMounted(() => settingsStore.ensureLoaded())

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    await api.post('/auth/confirm-password', { password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    router.push(redirect || { name: 'user.dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'The password you entered is incorrect.'
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
            <div class="p-2 bg-blue-500/20 rounded-lg"><KeyRound class="w-6 h-6 text-blue-400" /></div>
            <h1 class="text-2xl sm:text-3xl font-bold text-white">Confirm Password</h1>
          </div>
          <p class="text-gray-400 text-sm sm:text-base max-w-sm mx-auto leading-relaxed">
            This is a secure area. Please confirm your password before continuing.
          </p>
        </div>
      </div>

      <div class="bg-gray-900 backdrop-blur-sm border border-gray-700 rounded-2xl p-6 sm:p-8 space-y-6">
        <div v-if="error" class="p-4 bg-red-500/10 border border-red-500/20 rounded-xl">
          <div class="flex items-center gap-3">
            <AlertCircle class="w-5 h-5 text-red-400" />
            <p class="text-red-300 text-sm font-bold">{{ error }}</p>
          </div>
        </div>

        <form class="space-y-6" @submit.prevent="submit">
          <div class="space-y-2">
            <label for="password" class="block text-sm font-bold text-gray-200">Enter Password <span class="text-red-400">*</span></label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Lock class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
              <input id="password" v-model="password" type="password" required autofocus autocomplete="current-password"
                class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                placeholder="Enter your current password">
            </div>
          </div>

          <button type="submit" :disabled="submitting"
            class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 focus:ring-2 focus:ring-blue-400/20 disabled:opacity-50">
            <ShieldCheck class="w-5 h-5" />
            <span>{{ submitting ? 'Confirming…' : 'Confirm' }}</span>
          </button>
        </form>
      </div>

      <div class="text-center">
        <p class="text-xs text-gray-500">© {{ year }} {{ settingsStore.settings?.siteName }}. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
