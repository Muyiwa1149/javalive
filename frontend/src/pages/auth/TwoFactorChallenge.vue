<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Smartphone, ShieldAlert, AlertCircle, KeyRound } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'

// User-side login 2FA challenge — accepts either a live 6-digit authenticator-app code or a
// single-use recovery code, matching the backend's TwoFactorService.verifyLoginChallenge (mirrors
// Laravel Fortify's own real login-challenge behavior, which the source app has genuinely enabled).
const router = useRouter()
const route = useRoute()
const authUser = useAuthUserStore()

const code = ref('')
const submitting = ref(false)
const error = ref('')

onMounted(() => {
  if (!authUser.twoFactorPending) {
    router.replace({ name: 'login' })
  }
})

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    await authUser.verifyTwoFactor(code.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    router.push(redirect || { name: 'user.dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid verification code.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="bg-gray-900 rounded-3xl p-8 shadow-2xl border border-gray-700">
        <div class="text-center mb-8">
          <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-blue-500/10 mb-4">
            <Smartphone class="h-8 w-8 text-blue-400" />
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">Two-Factor Verification</h1>
          <p class="text-gray-300 text-sm md:text-base">Enter the code from your authenticator app to continue</p>
        </div>

        <div class="mb-6 p-4 rounded-2xl border bg-blue-500/10 border-blue-500/20">
          <div class="flex items-start gap-3">
            <ShieldAlert class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
            <div class="text-sm">
              <p class="font-bold mb-1 text-blue-300">Verification Required</p>
              <p class="text-gray-300">You can also use one of your recovery codes if you don't have access to your authenticator app.</p>
            </div>
          </div>
        </div>

        <div v-if="error" class="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-2xl">
          <div class="flex items-center gap-3">
            <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
            <span class="text-red-300 text-sm font-medium">{{ error }}</span>
          </div>
        </div>

        <form @submit.prevent="submit">
          <div class="mb-5">
            <label for="code" class="block text-sm font-semibold text-gray-200 mb-3">Verification or recovery code</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 flex items-center pl-4">
                <KeyRound class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" />
              </div>
              <input id="code" v-model="code" type="text" inputmode="text" autocomplete="one-time-code" autofocus required
                class="block w-full rounded-2xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-lg font-bold text-center tracking-widest"
                placeholder="123456">
            </div>
          </div>

          <button type="submit" :disabled="submitting"
            class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 text-white font-bold rounded-2xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 disabled:opacity-50">
            <span>{{ submitting ? 'Verifying…' : 'Verify & Sign In' }}</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
