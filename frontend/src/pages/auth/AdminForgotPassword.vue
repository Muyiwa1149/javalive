<script setup>
import { ref, onMounted } from 'vue'
import {
  ShieldQuestion, Key, Info, Mail, AlertCircle, ShieldAlert, Send, ArrowLeft,
  ShieldCheck, Clock, Eye,
} from 'lucide-vue-next'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import api from '@/lib/api'

const settingsStore = usePublicSettingsStore()

const email = ref('')
const submitting = ref(false)
const error = ref('')
const sent = ref(false)

onMounted(() => settingsStore.ensureLoaded())

async function submit() {
  error.value = ''
  submitting.value = true
  try {
    await api.post('/admin/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch (e) {
    error.value = e.response?.data?.message || 'Something went wrong. Please try again.'
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
          <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-orange-500/10 mb-4">
            <ShieldQuestion class="h-8 w-8 text-orange-400" />
          </div>

          <div class="inline-flex items-center gap-2 bg-orange-500/10 border border-orange-500/20 rounded-full px-4 py-2 mb-4">
            <Key class="w-4 h-4 text-orange-400" />
            <span class="text-orange-300 text-sm font-bold">Admin Recovery</span>
          </div>

          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">Forgot Password?</h1>
          <p class="text-gray-400 text-sm md:text-base">Secure password recovery for administrative access</p>
        </div>

        <div v-if="sent" class="mb-6 p-4 bg-green-500/10 border border-green-500/20 rounded-xl">
          <div class="flex items-start gap-3">
            <ShieldCheck class="w-5 h-5 text-green-400 mt-0.5 flex-shrink-0" />
            <div class="text-sm">
              <p class="text-green-300 font-bold mb-1">Recovery Email Sent</p>
              <p class="text-gray-300">If an admin account exists for that email, a reset link has been sent.</p>
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

          <div class="mb-6 p-4 bg-blue-500/10 border border-blue-500/20 rounded-xl">
            <div class="flex items-start gap-3">
              <Info class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
              <div class="text-sm">
                <p class="text-blue-300 font-bold mb-1">Password Recovery Process</p>
                <p class="text-gray-300">Enter your admin email address below. We'll send you secure instructions with a recovery token to reset your password.</p>
              </div>
            </div>
          </div>

          <form class="space-y-6" @submit.prevent="submit">
            <div class="space-y-2">
              <label for="email" class="block text-sm font-bold text-gray-200">Admin Email Address</label>
              <div class="relative">
                <input id="email" v-model="email" type="email" autocomplete="email" required autofocus
                  class="w-full px-4 py-3 bg-gray-900 border border-gray-600 rounded-xl text-white placeholder-gray-400 focus:border-orange-500 focus:ring-2 focus:ring-orange-500/20 focus:outline-none transition-all duration-200"
                  placeholder="Enter your admin email address">
                <div class="absolute inset-y-0 right-0 pr-3 flex items-center">
                  <Mail class="h-5 w-5 text-gray-400" />
                </div>
              </div>
              <p class="text-xs text-gray-500">Enter the email address associated with your admin account</p>
            </div>

            <div class="bg-yellow-500/10 border border-yellow-500/20 rounded-xl p-4">
              <div class="flex items-start gap-3">
                <ShieldAlert class="w-5 h-5 text-yellow-400 mt-0.5 flex-shrink-0" />
                <div class="text-sm">
                  <p class="text-yellow-300 font-bold mb-1">Security Notice</p>
                  <ul class="text-gray-300 space-y-1">
                    <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Recovery token will expire in 15 minutes</li>
                    <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Only admin emails can request recovery</li>
                    <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>All recovery attempts are logged</li>
                  </ul>
                </div>
              </div>
            </div>

            <button type="submit" :disabled="submitting"
              class="w-full bg-gradient-to-r from-orange-600 to-orange-700 hover:from-orange-700 hover:to-orange-800 text-white font-bold py-3 px-6 rounded-xl transition-all duration-200 hover:shadow-lg hover:shadow-orange-500/25 focus:outline-none focus:ring-2 focus:ring-orange-500/50 disabled:opacity-50">
              <span class="flex items-center justify-center gap-2">
                <Send class="w-5 h-5" />
                {{ submitting ? 'Sending…' : 'Send Recovery Instructions' }}
              </span>
            </button>

            <div class="text-center">
              <RouterLink :to="{ name: 'admin.login' }" class="inline-flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors duration-200">
                <ArrowLeft class="w-4 h-4" />
                Back to Admin Sign In
              </RouterLink>
            </div>
          </form>
        </template>

        <div class="mt-8 bg-gray-800/50 rounded-lg p-4 border border-gray-700">
          <h4 class="text-white font-bold text-sm mb-3 flex items-center gap-2">
            <Key class="w-4 h-4 text-blue-400" />
            Recovery Process
          </h4>
          <div class="space-y-3">
            <div class="flex items-center gap-3 text-sm text-gray-300">
              <span class="flex-shrink-0 w-6 h-6 bg-orange-500/20 rounded-full flex items-center justify-center text-orange-400 font-bold text-xs">1</span>
              <span>Enter your admin email address</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-gray-300">
              <span class="flex-shrink-0 w-6 h-6 bg-orange-500/20 rounded-full flex items-center justify-center text-orange-400 font-bold text-xs">2</span>
              <span>Check your email for recovery instructions</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-gray-300">
              <span class="flex-shrink-0 w-6 h-6 bg-orange-500/20 rounded-full flex items-center justify-center text-orange-400 font-bold text-xs">3</span>
              <span>Use the token to reset your password</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-gray-300">
              <span class="flex-shrink-0 w-6 h-6 bg-orange-500/20 rounded-full flex items-center justify-center text-orange-400 font-bold text-xs">4</span>
              <span>Access your admin account with new password</span>
            </div>
          </div>
        </div>

        <div class="mt-6 pt-6 border-t border-gray-700">
          <div class="text-center">
            <p class="text-xs text-gray-500 mb-3">Enterprise admin security</p>
            <div class="flex items-center justify-center gap-4 text-gray-600">
              <span class="flex items-center gap-1"><ShieldCheck class="w-3 h-3" /><span class="text-xs">Encrypted</span></span>
              <span class="flex items-center gap-1"><Clock class="w-3 h-3" /><span class="text-xs">Time-Limited</span></span>
              <span class="flex items-center gap-1"><Eye class="w-3 h-3" /><span class="text-xs">Audit Logged</span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="absolute inset-0 -z-10 overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-br from-orange-900/5 via-gray-900 to-yellow-900/5"></div>
        <div class="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 bg-orange-500/5 rounded-full blur-3xl"></div>
        <div class="absolute bottom-0 right-1/2 translate-x-1/2 w-96 h-96 bg-yellow-500/5 rounded-full blur-3xl"></div>
      </div>

      <div class="text-center mt-6">
        <p class="text-gray-500 text-sm">
          Need immediate assistance?
          <a href="mailto:admin-support@bluetrade.com" class="text-orange-400 hover:text-orange-300 font-medium ml-1 transition-colors duration-200">
            Contact Admin Support
          </a>
        </p>
      </div>
    </div>
  </div>
</template>
