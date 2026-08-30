<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  MailCheck, CheckCircle, Mail, RefreshCw, LogOut, HelpCircle,
  ShieldCheck, Bell, Key,
} from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'

// NOTE: Email verification is not functional on the backend yet — the source app's own
// UserObserver checks a misspelled/always-false settings column and its `verified` middleware is
// never applied to any route, so nothing actually gates on this. This page is a faithful port of
// the source's static content/design only. The "Resend" button below does not call any API — there
// is no resend-verification endpoint on our backend and none should be invented here — it simply
// shows the same inline success message the source page shows after a resend, which is harmless
// since the source flow is equally non-functional.
const router = useRouter()
const authUser = useAuthUserStore()

const sent = ref(false)
const sending = ref(false)

onMounted(() => {})

function resend() {
  sending.value = true
  setTimeout(() => {
    sending.value = false
    sent.value = true
  }, 500)
}

function signOut() {
  authUser.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
    <div class="max-w-md w-full space-y-8 relative z-10">
      <div class="bg-gray-900 rounded-2xl p-8 shadow-2xl border border-gray-700">
        <div class="text-center mb-8">
          <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-500/10 mb-4">
            <MailCheck class="h-8 w-8 text-green-400" />
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">Verify Your Email</h1>
          <p class="text-gray-400 text-sm md:text-base">Secure your trading account by verifying your email address</p>
        </div>

        <div v-if="sent" class="mb-6 p-4 bg-green-500/10 border border-green-500/20 rounded-xl">
          <div class="flex items-start gap-3">
            <CheckCircle class="w-5 h-5 text-green-400 mt-0.5 flex-shrink-0" />
            <div class="text-sm">
              <p class="text-green-300 font-bold mb-1">Verification Email Sent</p>
              <p class="text-gray-300">A verification link has been sent to your email address. Please check your inbox and click the link to verify your account.</p>
            </div>
          </div>
        </div>

        <div class="mb-8 p-6 bg-blue-500/10 border border-blue-500/20 rounded-xl">
          <div class="flex items-start gap-4">
            <Mail class="w-6 h-6 text-blue-400 mt-1 flex-shrink-0" />
            <div>
              <h3 class="text-blue-300 font-bold text-lg mb-3">Check Your Email</h3>
              <p class="text-gray-300 text-sm mb-4 leading-relaxed">
                We've sent a secure verification link to your registered email address.
                Click the link in the email to activate your trading account and access all features.
              </p>
              <div class="space-y-3">
                <div class="flex items-center gap-3 text-sm text-gray-300">
                  <span class="flex-shrink-0 w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center text-blue-400 font-bold text-xs">1</span>
                  <span>Check your inbox for our verification email</span>
                </div>
                <div class="flex items-center gap-3 text-sm text-gray-300">
                  <span class="flex-shrink-0 w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center text-blue-400 font-bold text-xs">2</span>
                  <span>Click the "Verify Email" button in the email</span>
                </div>
                <div class="flex items-center gap-3 text-sm text-gray-300">
                  <span class="flex-shrink-0 w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center text-blue-400 font-bold text-xs">3</span>
                  <span>Return to complete your account setup</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-4">
          <button type="button" :disabled="sending" @click="resend"
            class="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-bold py-3 px-6 rounded-xl transition-all duration-200 hover:shadow-lg hover:shadow-blue-500/25 focus:outline-none focus:ring-2 focus:ring-blue-500/50 disabled:opacity-50">
            <span class="flex items-center justify-center gap-2">
              <RefreshCw class="w-5 h-5" :class="{ 'animate-spin': sending }" />
              {{ sending ? 'Sending…' : 'Resend Verification Email' }}
            </span>
          </button>

          <button type="button" @click="signOut"
            class="w-full bg-gray-800 hover:bg-gray-700 text-gray-300 hover:text-white font-bold py-3 px-6 rounded-xl transition-all duration-200 border border-gray-600 hover:border-gray-500 focus:outline-none focus:ring-2 focus:ring-gray-500/50">
            <span class="flex items-center justify-center gap-2">
              <LogOut class="w-5 h-5" />
              Sign Out
            </span>
          </button>
        </div>

        <div class="mt-8 bg-gray-800/50 rounded-lg p-4 border border-gray-700">
          <h4 class="text-white font-bold text-sm mb-3 flex items-center gap-2">
            <HelpCircle class="w-4 h-4 text-yellow-400" />
            Can't Find the Email?
          </h4>
          <ul class="text-gray-300 text-xs space-y-2">
            <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Check your spam or junk folder</li>
            <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Ensure the email address is correct</li>
            <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Wait a few minutes for delivery</li>
            <li class="flex items-start gap-2"><span class="text-yellow-400 mt-1">•</span>Click "Resend" if needed after 2 minutes</li>
          </ul>
        </div>

        <div class="mt-6 pt-6 border-t border-gray-700">
          <div class="text-center">
            <p class="text-xs text-gray-500 mb-3">Why verify your email?</p>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
              <div class="flex flex-col items-center gap-1 text-gray-400">
                <ShieldCheck class="w-4 h-4 text-green-400" />
                <span>Account Security</span>
              </div>
              <div class="flex flex-col items-center gap-1 text-gray-400">
                <Bell class="w-4 h-4 text-blue-400" />
                <span>Trade Alerts</span>
              </div>
              <div class="flex flex-col items-center gap-1 text-gray-400">
                <Key class="w-4 h-4 text-purple-400" />
                <span>Password Recovery</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="absolute inset-0 -z-10 overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-br from-green-900/5 via-gray-900 to-blue-900/5"></div>
        <div class="absolute top-0 left-1/2 -translate-x-1/2 w-96 h-96 bg-green-500/5 rounded-full blur-3xl"></div>
        <div class="absolute bottom-0 right-1/2 translate-x-1/2 w-96 h-96 bg-blue-500/5 rounded-full blur-3xl"></div>
      </div>

      <div class="text-center mt-6">
        <p class="text-gray-500 text-sm">
          Need help?
          <a href="mailto:support@bluetrade.com" class="text-blue-400 hover:text-blue-300 font-medium ml-1 transition-colors duration-200">
            Contact Support
          </a>
        </p>
      </div>
    </div>
  </div>
</template>
