<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Smartphone, ShieldAlert, AlertCircle, KeyRound } from 'lucide-vue-next'
import { useAuthAdminStore } from '@/stores/authAdmin'

// The source page (two_factor.blade.php) has an Alpine.js toggle for switching between an
// authenticator-app code and an emergency "recovery code". Our backend's AdminAuthService only
// implements a single flow: a 5-digit numeric OTP emailed to the admin, valid for 10 minutes, with
// no separate recovery-code mechanism — so that toggle is dropped here rather than built against a
// backend feature that doesn't exist. The code-entry step itself is a faithful port of the design.
const router = useRouter()
const authAdmin = useAuthAdminStore()

const code = ref('')
const submitting = ref(false)
const error = ref('')

onMounted(() => {
  if (!authAdmin.twoFactorPending) {
    router.replace({ name: 'admin.login' })
  }
})

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    await authAdmin.verifyTwoFactor(code.value)
    router.push({ name: 'admin.dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid or expired verification code.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="bg-gray-900 rounded-2xl p-8 shadow-2xl border border-gray-700">
        <div class="text-center mb-8">
          <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-blue-500/10 mb-4">
            <Smartphone class="h-8 w-8 text-blue-400" />
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">Two-Step Verification</h1>
          <p class="text-gray-400 text-sm md:text-base">Enter the verification code sent to your admin email to secure your account</p>
        </div>

        <div class="mb-6 p-4 rounded-xl border bg-blue-500/10 border-blue-500/20">
          <div class="flex items-start gap-3">
            <ShieldAlert class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
            <div class="text-sm">
              <p class="font-bold mb-1 text-blue-300">Verification Required</p>
              <p class="text-gray-300">Check your admin email for a 5-digit verification code. The code expires in 10 minutes.</p>
            </div>
          </div>
        </div>

        <div v-if="error" class="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-xl">
          <div class="flex items-center gap-3">
            <AlertCircle class="w-5 h-5 text-red-400 flex-shrink-0" />
            <span class="text-red-300 text-sm font-medium">{{ error }}</span>
          </div>
        </div>

        <form @submit.prevent="submit">
          <div class="mb-5">
            <label for="code" class="block text-sm font-bold text-gray-200 mb-3">Verification Code</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 flex items-center pl-4"><KeyRound class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
              <input id="code" v-model="code" type="text" inputmode="numeric" maxlength="5" autocomplete="one-time-code" autofocus required
                class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-lg font-bold text-center tracking-[0.4em]"
                placeholder="00000">
            </div>
          </div>

          <button type="submit" :disabled="submitting"
            class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 focus:ring-2 focus:ring-blue-400/20 disabled:opacity-50">
            <span>{{ submitting ? 'Verifying…' : 'Verify & Sign In' }}</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
