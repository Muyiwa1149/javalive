<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import {
  UserCircle, User, UserCheck, Mail, Phone, Globe2, ChevronDown, Info,
  ShieldCheck, Lock, Key, Check, ArrowLeft, ArrowRight, UserPlus, Sparkles, AlertCircle,
} from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'
import { COUNTRIES } from '@/lib/countries'

const router = useRouter()
const authUser = useAuthUserStore()
const settingsStore = usePublicSettingsStore()

const steps = [
  { title: 'Personal Info', description: 'Basic details' },
  { title: 'Location', description: 'Regional settings' },
  { title: 'Security', description: 'Account protection' },
]
const currentStep = ref(0)
const year = new Date().getFullYear()

const form = ref({
  username: '', name: '', email: '', phone: '',
  country: '', password: '', passwordConfirmation: '',
  captcha: '', agree: false,
})
const captchaCode = ref('')
const fieldErrors = ref({})
const submitting = ref(false)
const submitError = ref('')

function generateCaptcha() {
  captchaCode.value = Math.random().toString(36).slice(2, 8).toUpperCase()
}

onMounted(() => {
  settingsStore.ensureLoaded()
  generateCaptcha()
  const refBy = localStorage.getItem('ref_by')
  if (refBy) form.value.refBy = refBy
  if (authUser.isAuthenticated) {
    router.replace({ name: 'user.dashboard' })
  }
})

function validateStep(step) {
  fieldErrors.value = {}
  const missing = []

  if (step === 0) {
    if (!form.value.username.trim()) missing.push('Username')
    if (!form.value.name.trim()) missing.push('Full Name')
    if (!form.value.email.trim() || !form.value.email.includes('@')) missing.push('Valid Email')
    if (!form.value.phone.trim()) missing.push('Phone Number')
  } else if (step === 1) {
    if (!form.value.country) missing.push('Country')
  } else if (step === 2) {
    if (!form.value.password) missing.push('Password')
    else if (form.value.password.length < 8) missing.push('Password (minimum 8 characters)')
    if (!form.value.passwordConfirmation) missing.push('Password Confirmation')
    else if (form.value.password !== form.value.passwordConfirmation) missing.push('Matching Passwords')
    if (!form.value.captcha.trim()) missing.push('Security Verification Code')
    else if (form.value.captcha.trim().toUpperCase() !== captchaCode.value) missing.push('Correct Security Code')
    if (!form.value.agree) missing.push('Terms Agreement')
  }

  if (missing.length) {
    Swal.fire({
      title: 'Incomplete Information',
      text: `Please provide: ${missing.join(', ')}`,
      icon: 'warning',
      confirmButtonText: 'Got it',
      confirmButtonColor: '#3B82F6',
      background: '#1F2937',
      color: '#E5E7EB',
    })
    return false
  }
  return true
}

function nextStep() {
  if (validateStep(currentStep.value) && currentStep.value < steps.length - 1) {
    currentStep.value += 1
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}
function previousStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

async function submit() {
  if (!validateStep(2)) return
  submitError.value = ''
  submitting.value = true
  try {
    await authUser.register({
      username: form.value.username,
      name: form.value.name,
      email: form.value.email,
      phone: form.value.phone,
      country: form.value.country,
      password: form.value.password,
      refBy: form.value.refBy || null,
    })
    localStorage.removeItem('ref_by')
    router.push({ name: 'user.dashboard' })
  } catch (e) {
    submitError.value = e.response?.data?.message || 'Something went wrong. Please try again.'
    generateCaptcha()
    form.value.captcha = ''
    await Swal.fire({
      title: 'Registration Failed',
      text: submitError.value,
      icon: 'error',
      confirmButtonColor: '#3B82F6',
      background: '#1F2937',
      color: '#E5E7EB',
    })
  } finally {
    submitting.value = false
  }
}

const stepClass = (index) => computed(() => {
  if (currentStep.value > index) return 'bg-green-500 text-white'
  if (currentStep.value === index) return 'bg-blue-500 text-white'
  return 'bg-gray-700 text-gray-400'
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 relative overflow-hidden py-8 sm:py-12">
    <div class="relative z-10 flex items-center justify-center px-4 sm:px-6 lg:px-8">
      <div class="w-full max-w-2xl">
        <div class="bg-gray-900 border border-gray-700 rounded-2xl sm:rounded-3xl p-6 sm:p-8 lg:p-10 shadow-2xl">
          <div class="text-center mb-8">
            <div class="flex items-center justify-center mb-6">
              <img v-if="settingsStore.settings?.logo" :src="storageUrl(settingsStore.settings.logo)" class="h-12 sm:h-16 w-auto" :alt="settingsStore.settings?.siteName">
            </div>
            <h1 class="text-2xl sm:text-3xl lg:text-4xl font-bold text-white mb-2">
              Join <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-400">{{ settingsStore.settings?.siteName }}</span>
            </h1>
            <p class="text-gray-300 text-sm sm:text-base lg:text-lg mb-6">Start your professional trading journey</p>
          </div>

          <div class="mb-8">
            <div class="flex items-center justify-between sm:justify-center sm:space-x-8">
              <div v-for="(step, index) in steps" :key="index" class="flex flex-col items-center">
                <div class="relative mb-2">
                  <div class="flex items-center justify-center w-8 h-8 sm:w-10 sm:h-10 rounded-full text-xs sm:text-sm font-bold transition-all duration-300" :class="stepClass(index).value">
                    <span v-if="currentStep <= index">{{ index + 1 }}</span>
                    <Check v-else class="w-4 h-4 sm:w-5 sm:h-5" />
                  </div>
                </div>
                <div class="text-center">
                  <div class="text-xs sm:text-sm font-medium transition-colors duration-300" :class="currentStep >= index ? 'text-white' : 'text-gray-500'">{{ step.title }}</div>
                  <div class="text-xs text-gray-500 hidden sm:block">{{ step.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="submitError" class="mb-6 p-4 bg-red-500/20 border border-red-400/30 rounded-2xl">
            <div class="flex items-center gap-3">
              <AlertCircle class="h-5 w-5 text-red-400 flex-shrink-0" />
              <div class="text-sm text-red-100">{{ submitError }}</div>
            </div>
          </div>

          <form class="space-y-6" @submit.prevent="submit">
            <!-- Step 1: Personal Information -->
            <div v-show="currentStep === 0">
              <div class="mb-6 p-4 bg-blue-500/10 rounded-xl border border-blue-500/20">
                <div class="flex items-center gap-3">
                  <div class="p-2 bg-blue-500/20 rounded-lg"><UserCircle class="w-5 h-5 text-blue-400" /></div>
                  <div>
                    <h3 class="text-lg sm:text-xl font-bold text-white">Personal Information</h3>
                    <p class="text-gray-400 text-sm">Create your trading profile</p>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                <div class="space-y-2">
                  <label for="username" class="block text-sm font-bold text-gray-200">Trading Username <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><User class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="username" v-model="form.username" type="text" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="Choose username">
                  </div>
                </div>

                <div class="space-y-2">
                  <label for="name" class="block text-sm font-bold text-gray-200">Full Name <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><UserCheck class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="name" v-model="form.name" type="text" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="Enter full name">
                  </div>
                </div>

                <div class="space-y-2">
                  <label for="email" class="block text-sm font-bold text-gray-200">Email Address <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Mail class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="email" v-model="form.email" type="email" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="your.email@example.com">
                  </div>
                </div>

                <div class="space-y-2">
                  <label for="phone" class="block text-sm font-bold text-gray-200">Phone Number <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Phone class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="phone" v-model="form.phone" type="tel" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="+1 (555) 123-4567">
                  </div>
                </div>
              </div>
            </div>

            <!-- Step 2: Location -->
            <div v-show="currentStep === 1">
              <div class="mb-6 p-4 bg-purple-500/10 rounded-xl border border-purple-500/20">
                <div class="flex items-center gap-3">
                  <div class="p-2 bg-purple-500/20 rounded-lg"><Globe2 class="w-5 h-5 text-purple-400" /></div>
                  <div>
                    <h3 class="text-lg sm:text-xl font-bold text-white">Location</h3>
                    <p class="text-gray-400 text-sm">Set your regional trading preferences</p>
                  </div>
                </div>
              </div>

              <div class="space-y-2">
                <label for="country" class="block text-sm font-bold text-gray-200">Country <span class="text-red-400">*</span></label>
                <div class="relative group">
                  <div class="absolute inset-y-0 left-0 flex items-center pl-4 z-10"><Globe2 class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                  <select id="country" v-model="form.country" required
                    class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-8 py-4 text-white focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold appearance-none">
                    <option value="" disabled>Select your country</option>
                    <option v-for="c in COUNTRIES" :key="c" :value="c">{{ c }}</option>
                  </select>
                  <div class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none"><ChevronDown class="h-4 w-4 text-gray-400" /></div>
                </div>
              </div>

              <div class="mt-6 p-4 bg-blue-500/10 rounded-xl border border-blue-500/20">
                <div class="flex items-start gap-3">
                  <Info class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
                  <div class="text-sm">
                    <p class="text-blue-300 font-bold mb-1">Regional Trading Information</p>
                    <p class="text-gray-300">Your location helps us provide region-specific features, compliance, and optimal server connections for faster trading execution.</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Step 3: Security -->
            <div v-show="currentStep === 2">
              <div class="mb-6 p-4 bg-green-500/10 rounded-xl border border-green-500/20">
                <div class="flex items-center gap-3">
                  <div class="p-2 bg-green-500/20 rounded-lg"><ShieldCheck class="w-5 h-5 text-green-400" /></div>
                  <div>
                    <h3 class="text-lg sm:text-xl font-bold text-white">Account Security</h3>
                    <p class="text-gray-400 text-sm">Secure your trading account</p>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                <div class="space-y-2">
                  <label for="password" class="block text-sm font-bold text-gray-200">Password <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Lock class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="password" v-model="form.password" type="password" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="Create strong password">
                  </div>
                </div>

                <div class="space-y-2">
                  <label for="password_confirmation" class="block text-sm font-bold text-gray-200">Confirm Password <span class="text-red-400">*</span></label>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><Key class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="password_confirmation" v-model="form.passwordConfirmation" type="password" required
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold"
                      placeholder="Confirm your password">
                  </div>
                </div>
              </div>

              <div class="space-y-4 mt-6">
                <div class="space-y-2">
                  <label for="captcha" class="block text-sm font-bold text-gray-200">Security Verification <span class="text-red-400">*</span></label>
                  <div class="bg-gray-800 border border-gray-600 rounded-xl p-4 mb-3">
                    <div class="flex items-center justify-center">
                      <div class="bg-gradient-to-r from-blue-900 to-purple-900 rounded-lg p-4 border border-gray-600">
                        <div class="text-center">
                          <p class="text-xs text-gray-300 mb-2 font-medium">Enter the code below:</p>
                          <div class="bg-gray-900 rounded-lg px-6 py-3 border border-gray-700">
                            <span class="text-2xl font-bold text-yellow-400 tracking-[0.3em] select-none" style="font-family: 'Courier New', monospace;">{{ captchaCode }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="relative group">
                    <div class="absolute inset-y-0 left-0 flex items-center pl-4"><ShieldCheck class="h-5 w-5 text-gray-400 group-focus-within:text-blue-400 transition-colors" /></div>
                    <input id="captcha" v-model="form.captcha" type="text" required maxlength="6" autocomplete="off"
                      class="block w-full rounded-xl border border-gray-600 bg-gray-900 pl-12 pr-4 py-4 text-white placeholder-gray-400 focus:border-blue-400 focus:ring-2 focus:ring-blue-400/20 focus:bg-gray-800 transition-all duration-200 text-sm font-bold text-center tracking-widest uppercase"
                      placeholder="Enter the code above">
                  </div>
                  <p class="text-xs text-gray-400 flex items-center gap-1">
                    <Info class="w-3 h-3" /> This helps us verify that you're a real person and protects against automated registrations.
                  </p>
                </div>
              </div>

              <div class="mt-6 p-4 bg-gray-800/50 rounded-xl border border-gray-700">
                <p class="text-sm font-bold text-gray-200 mb-2">Password Requirements:</p>
                <ul class="text-xs text-gray-300 space-y-1">
                  <li class="flex items-center gap-2"><Check class="w-3 h-3 text-green-400" /> At least 8 characters long</li>
                  <li class="flex items-center gap-2"><Check class="w-3 h-3 text-green-400" /> Contains uppercase and lowercase letters</li>
                  <li class="flex items-center gap-2"><Check class="w-3 h-3 text-green-400" /> Includes at least one number or special character</li>
                </ul>
              </div>

              <div class="mt-6 p-6 bg-blue-500/10 rounded-xl border border-blue-500/20">
                <div class="flex items-start gap-4">
                  <div class="flex items-center h-5 mt-1">
                    <input id="agree" v-model="form.agree" type="checkbox" required
                      class="h-4 w-4 rounded border-gray-600 bg-gray-900 text-blue-500 focus:ring-2 focus:ring-blue-400/20 transition-colors">
                  </div>
                  <div class="flex-1">
                    <label for="agree" class="text-sm font-bold text-gray-200 leading-relaxed">
                      I agree to {{ settingsStore.settings?.siteName }}'s
                      <RouterLink to="/terms" target="_blank" class="text-blue-400 hover:text-blue-300 font-bold underline underline-offset-2">Terms and Conditions</RouterLink>
                      and acknowledge that I have read and understood the
                      <RouterLink to="/privacy" target="_blank" class="text-blue-400 hover:text-blue-300 font-bold underline underline-offset-2">Privacy Policy</RouterLink>
                    </label>
                    <p class="text-xs text-gray-400 mt-2">
                      By creating an account, you confirm that you are at least 18 years old and agree to receive trading updates and market insights.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex flex-col sm:flex-row justify-between items-center mt-10 pt-8 border-t border-gray-700 gap-4">
              <button v-if="currentStep > 0" type="button" @click="previousStep"
                class="inline-flex items-center gap-2 px-6 py-3 text-gray-400 hover:text-white transition-all duration-200 rounded-xl hover:bg-gray-800/50 group">
                <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                <span class="font-bold">Previous Step</span>
              </button>
              <div v-else></div>

              <div class="flex items-center gap-2 text-sm text-gray-400">
                <span class="font-bold">Step {{ currentStep + 1 }} of {{ steps.length }}</span>
              </div>

              <button v-if="currentStep < steps.length - 1" type="button" @click="nextStep"
                class="inline-flex items-center gap-2 px-8 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold rounded-xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 group">
                <span>Continue</span>
                <ArrowRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </button>
              <button v-else type="submit" :disabled="submitting"
                class="inline-flex items-center gap-2 px-8 py-3 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white font-bold rounded-xl transition-all duration-200 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 group disabled:opacity-50">
                <UserPlus class="w-5 h-5" />
                <span>{{ submitting ? 'Creating Account…' : 'Create Trading Account' }}</span>
                <Sparkles class="w-4 h-4 group-hover:rotate-12 transition-transform" />
              </button>
            </div>

            <div class="mt-8 text-center space-y-4">
              <p class="text-gray-400 text-sm">
                Already have an account?
                <RouterLink to="/login" class="font-bold text-blue-400 hover:text-blue-300 transition-colors underline underline-offset-2">Sign in here</RouterLink>
              </p>
              <p class="text-xs text-gray-500">
                © {{ year }} {{ settingsStore.settings?.siteName }}. All rights reserved. | Licensed and regulated trading platform.
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
