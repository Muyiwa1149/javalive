<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Mail, Lock, Eye, EyeOff, LogIn, ArrowRight, AlertCircle, ShieldCheck, Award } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'

const router = useRouter()
const route = useRoute()
const authUser = useAuthUserStore()
const settingsStore = usePublicSettingsStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const remember = ref(true)
const submitting = ref(false)
const error = ref('')
const year = new Date().getFullYear()

onMounted(() => {
  settingsStore.ensureLoaded()
  if (authUser.isAuthenticated) {
    router.replace({ name: 'user.dashboard' })
  }
})

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    await authUser.login({ email: email.value, password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    router.push(redirect || { name: 'user.dashboard' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid email or password.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 relative overflow-hidden">
    <div class="relative z-10 flex min-h-screen items-center justify-center px-4 py-8 sm:px-6 lg:px-8">
      <div class="w-full max-w-lg">
        <div class="relative bg-gray-900 border border-gray-700 rounded-3xl p-8 sm:p-10 shadow-2xl">
          <div class="relative">
            <div v-if="error" class="mb-6 p-4 bg-red-500/20 border border-red-400/30 rounded-2xl">
              <div class="flex items-center gap-3">
                <AlertCircle class="h-5 w-5 text-red-400 flex-shrink-0" />
                <div class="text-sm text-red-100">{{ error }}</div>
              </div>
            </div>

            <div class="text-center mb-8">
              <div class="flex items-center justify-center mb-6">
                <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-16 w-auto" :alt="settingsStore.settings?.siteName">
              </div>
              <h1 class="text-2xl sm:text-3xl font-bold text-white mb-2">Welcome Back</h1>
              <h2 class="text-lg sm:text-xl font-semibold mb-3">
                <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-400">{{ settingsStore.settings?.siteName }}</span>
              </h2>
              <p class="text-gray-300 text-sm sm:text-base">Access your trading dashboard</p>

              <div class="flex items-center justify-center gap-6 mt-6 text-xs sm:text-sm">
                <div class="text-center">
                  <div class="flex items-center justify-center gap-1 text-green-400 mb-1">
                    <div class="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                    <span class="font-medium">Live</span>
                  </div>
                  <div class="text-gray-400">24/7 Markets</div>
                </div>
                <div class="w-px h-10 bg-white/20"></div>
                <div class="text-center">
                  <div class="flex items-center justify-center gap-1 text-blue-400 mb-1">
                    <span class="font-medium">Fast</span>
                  </div>
                  <div class="text-gray-400">Execution</div>
                </div>
                <div class="w-px h-10 bg-white/20"></div>
                <div class="text-center">
                  <div class="flex items-center justify-center gap-1 text-cyan-400 mb-1">
                    <span class="font-medium">Secure</span>
                  </div>
                  <div class="text-gray-400">Platform</div>
                </div>
              </div>
            </div>

            <form class="space-y-6" @submit.prevent="submit">
              <div class="space-y-2">
                <label for="email" class="block text-sm font-semibold text-gray-200">Email Address</label>
                <div class="relative group">
                  <div class="absolute inset-y-0 left-0 flex items-center pl-4">
                    <Mail class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors duration-200" />
                  </div>
                  <input id="email" v-model="email" type="email" required autofocus
                    class="block w-full rounded-2xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-medium"
                    placeholder="your.email@example.com">
                </div>
              </div>

              <div class="space-y-2">
                <label for="password" class="block text-sm font-semibold text-gray-200">Password</label>
                <div class="relative group">
                  <div class="absolute inset-y-0 left-0 flex items-center pl-4">
                    <Lock class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors duration-200" />
                  </div>
                  <input id="password" v-model="password" :type="showPassword ? 'text' : 'password'" required
                    class="block w-full rounded-2xl border border-gray-600 bg-gray-900 pl-12 pr-12 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-medium"
                    placeholder="Enter your password">
                  <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-4 text-gray-400 hover:text-blue-400 transition-colors" @click="showPassword = !showPassword">
                    <Eye v-if="!showPassword" class="h-5 w-5" />
                    <EyeOff v-else class="h-5 w-5" />
                  </button>
                </div>
              </div>

              <div class="flex items-center justify-between text-sm">
                <div class="flex items-center">
                  <input id="remember" v-model="remember" type="checkbox"
                    class="h-4 w-4 rounded border-gray-600 bg-gray-900 text-blue-500 focus:ring-2 focus:ring-blue-400/20 transition-colors">
                  <label for="remember" class="ml-3 text-gray-300 font-medium">Remember me</label>
                </div>
                <RouterLink to="/forgot-password" class="text-blue-400 hover:text-blue-300 transition-colors underline underline-offset-2">
                  Forgot password?
                </RouterLink>
              </div>

              <div class="mt-8">
                <button type="submit" :disabled="submitting"
                  class="group relative flex w-full justify-center items-center gap-3 rounded-2xl bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 px-6 py-4 text-base font-bold text-white transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 focus:outline-none focus:ring-2 focus:ring-blue-400/50 disabled:opacity-50 disabled:cursor-not-allowed">
                  <LogIn class="h-5 w-5" />
                  <span>{{ submitting ? 'Signing in…' : 'Access Dashboard' }}</span>
                  <ArrowRight class="h-4 w-4 group-hover:translate-x-1 transition-transform" />
                </button>
              </div>
            </form>

            <div class="mt-10 text-center space-y-6">
              <div class="text-sm">
                <span class="text-gray-300">New to trading? </span>
                <RouterLink to="/register" class="font-semibold text-blue-400 hover:text-blue-300 transition-colors underline underline-offset-2">
                  Create your account
                </RouterLink>
              </div>

              <div class="flex items-center justify-center gap-6 py-4 text-xs text-gray-400">
                <div class="flex items-center gap-1">
                  <ShieldCheck class="w-3 h-3 text-green-400" />
                  <span>SSL Secured</span>
                </div>
                <div class="flex items-center gap-1">
                  <Lock class="w-3 h-3 text-blue-400" />
                  <span>256-bit Encryption</span>
                </div>
                <div class="flex items-center gap-1">
                  <Award class="w-3 h-3 text-cyan-400" />
                  <span>Regulated</span>
                </div>
              </div>

              <p class="text-xs text-gray-500">© {{ year }} {{ settingsStore.settings?.siteName }}. All rights reserved.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
