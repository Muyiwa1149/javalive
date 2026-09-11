<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { User, CreditCard, Lock, Mail, ShieldCheck } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const savingProfile = ref(false)
const savingPayout = ref(false)
const savingPassword = ref(false)
const savingEmail = ref(false)

const profileForm = ref({ name: '', dob: '', phone: '', address: '' })
const payoutForm = ref({ bankName: '', bankAccountName: '', bankAccountNumber: '', bankSwiftCode: '', btcAddress: '', ethAddress: '', ltcAddress: '', usdtAddress: '' })
const passwordForm = ref({ currentPassword: '', password: '', passwordConfirmation: '' })
const emailForm = ref({ sendOtpEmail: false, sendRoiEmail: true, sendInvPlanEmail: true })

const readOnly = ref({ username: '', email: '', country: '' })

function applyProfile(data) {
  profileForm.value = { name: data.name || '', dob: data.dob || '', phone: data.phone || '', address: data.address || '' }
  payoutForm.value = {
    bankName: data.bankName || '', bankAccountName: data.bankAccountName || '',
    bankAccountNumber: data.bankAccountNumber || '', bankSwiftCode: data.bankSwiftCode || '',
    btcAddress: data.btcAddress || '', ethAddress: data.ethAddress || '',
    ltcAddress: data.ltcAddress || '', usdtAddress: data.usdtAddress || '',
  }
  emailForm.value = { sendOtpEmail: data.sendOtpEmail, sendRoiEmail: data.sendRoiEmail, sendInvPlanEmail: data.sendInvPlanEmail }
  readOnly.value = { username: data.username, email: data.email, country: data.country }
}

// Two-Factor Authentication (TOTP) — mirrors source's genuinely-enabled Laravel Fortify feature.
const twoFactor = ref({ enabled: false, confirmedAt: null })
const twoFactorStep = ref('idle') // idle | setup | confirm | recovery-codes
const twoFactorSetup = ref(null) // { secret, otpauthUri, qrCodeDataUri }
const twoFactorConfirmCode = ref('')
const twoFactorRecoveryCodes = ref([])
const twoFactorPassword = ref('')
const twoFactorBusy = ref(false)

async function loadTwoFactorStatus() {
  const { data } = await api.get('/2fa/status')
  twoFactor.value = data
}

async function startTwoFactorSetup() {
  twoFactorBusy.value = true
  try {
    const { data } = await api.post('/2fa/setup')
    twoFactorSetup.value = data
    twoFactorStep.value = 'confirm'
  } catch (e) {
    notifyError(e)
  } finally {
    twoFactorBusy.value = false
  }
}

async function confirmTwoFactor() {
  twoFactorBusy.value = true
  try {
    const { data } = await api.post('/2fa/confirm', { code: twoFactorConfirmCode.value })
    twoFactorRecoveryCodes.value = data.recoveryCodes
    twoFactorStep.value = 'recovery-codes'
    twoFactorConfirmCode.value = ''
    await loadTwoFactorStatus()
  } catch (e) {
    notifyError(e)
  } finally {
    twoFactorBusy.value = false
  }
}

function finishTwoFactorSetup() {
  twoFactorStep.value = 'idle'
  twoFactorSetup.value = null
  twoFactorRecoveryCodes.value = []
  notifySuccess('Two-factor authentication is now enabled.')
}

async function disableTwoFactor() {
  const { value: password } = await Swal.fire({
    title: 'Disable two-factor authentication?', input: 'password',
    inputLabel: 'Confirm your password to continue', inputPlaceholder: 'Password',
    showCancelButton: true, confirmButtonText: 'Disable', confirmButtonColor: '#dc2626',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!password) return
  twoFactorBusy.value = true
  try {
    await api.post('/2fa/disable', { password })
    await loadTwoFactorStatus()
    notifySuccess('Two-factor authentication has been disabled.')
  } catch (e) {
    notifyError(e)
  } finally {
    twoFactorBusy.value = false
  }
}

async function regenerateRecoveryCodes() {
  const { value: password } = await Swal.fire({
    title: 'Regenerate recovery codes?', input: 'password',
    inputLabel: 'Confirm your password to continue', inputPlaceholder: 'Password',
    text: 'Your old recovery codes will stop working.',
    showCancelButton: true, confirmButtonText: 'Regenerate',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!password) return
  twoFactorBusy.value = true
  try {
    const { data } = await api.post('/2fa/recovery-codes/regenerate', { password })
    twoFactorRecoveryCodes.value = data.recoveryCodes
    twoFactorStep.value = 'recovery-codes'
  } catch (e) {
    notifyError(e)
  } finally {
    twoFactorBusy.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/profile')
    applyProfile(data)
    await loadTwoFactorStatus()
  } finally {
    loading.value = false
  }
})

function notifySuccess(message) {
  Swal.fire({ icon: 'success', title: 'Saved', text: message, background: '#1F2937', color: '#E5E7EB', timer: 2000, showConfirmButton: false })
}
function notifyError(e) {
  Swal.fire({ icon: 'error', title: 'Something went wrong', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
}

async function saveProfile() {
  savingProfile.value = true
  try {
    const { data } = await api.put('/profile', profileForm.value)
    applyProfile(data)
    notifySuccess('Profile Information Updated Successfully!')
  } catch (e) {
    notifyError(e)
  } finally {
    savingProfile.value = false
  }
}

async function savePayout() {
  savingPayout.value = true
  try {
    const { data } = await api.put('/profile/payout-info', payoutForm.value)
    applyProfile(data)
    notifySuccess('Withdrawal Info updated Successfully!')
  } catch (e) {
    notifyError(e)
  } finally {
    savingPayout.value = false
  }
}

async function savePassword() {
  if (passwordForm.value.password !== passwordForm.value.passwordConfirmation) {
    Swal.fire({ icon: 'warning', title: 'Passwords do not match', background: '#1F2937', color: '#E5E7EB' })
    return
  }
  savingPassword.value = true
  try {
    await api.put('/profile/password', {
      currentPassword: passwordForm.value.currentPassword,
      password: passwordForm.value.password,
    })
    passwordForm.value = { currentPassword: '', password: '', passwordConfirmation: '' }
    notifySuccess('Password updated successfully')
  } catch (e) {
    notifyError(e)
  } finally {
    savingPassword.value = false
  }
}

async function saveEmailPreferences() {
  savingEmail.value = true
  try {
    const { data } = await api.put('/profile/email-preferences', emailForm.value)
    applyProfile(data)
    notifySuccess('Email Preference updated')
  } catch (e) {
    notifyError(e)
  } finally {
    savingEmail.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white">Profile Settings</h1>
      <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400 mt-1">Manage your personal information, payout details, security, and email preferences.</p>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else>
      <!-- Personal Info -->
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <User class="w-5 h-5 text-blue-500" />
          <h2 class="font-semibold text-gray-900 dark:text-white">Personal Information</h2>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Username</label>
            <input :value="readOnly.username" disabled class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-gray-100 dark:bg-gray-800 px-3 py-2 text-gray-500">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Email</label>
            <input :value="readOnly.email" disabled class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-gray-100 dark:bg-gray-800 px-3 py-2 text-gray-500">
          </div>
        </div>
        <form class="grid grid-cols-1 sm:grid-cols-2 gap-4" @submit.prevent="saveProfile">
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Full Name</label>
            <input v-model="profileForm.name" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Date of Birth</label>
            <input v-model="profileForm.dob" type="date" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Phone</label>
            <input v-model="profileForm.phone" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Address</label>
            <input v-model="profileForm.address" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div class="sm:col-span-2">
            <button type="submit" :disabled="savingProfile" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50">
              {{ savingProfile ? 'Saving…' : 'Save Profile' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Payout Info -->
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <CreditCard class="w-5 h-5 text-emerald-500" />
          <h2 class="font-semibold text-gray-900 dark:text-white">Withdrawal / Payout Details</h2>
        </div>
        <form class="grid grid-cols-1 sm:grid-cols-2 gap-4" @submit.prevent="savePayout">
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Bank Name</label>
            <input v-model="payoutForm.bankName" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Account Name</label>
            <input v-model="payoutForm.bankAccountName" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Account Number</label>
            <input v-model="payoutForm.bankAccountNumber" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Swift Code</label>
            <input v-model="payoutForm.bankSwiftCode" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">BTC Address</label>
            <input v-model="payoutForm.btcAddress" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white font-mono text-sm">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">ETH Address</label>
            <input v-model="payoutForm.ethAddress" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white font-mono text-sm">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">LTC Address</label>
            <input v-model="payoutForm.ltcAddress" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white font-mono text-sm">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">USDT Address</label>
            <input v-model="payoutForm.usdtAddress" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white font-mono text-sm">
          </div>
          <div class="sm:col-span-2">
            <button type="submit" :disabled="savingPayout" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50">
              {{ savingPayout ? 'Saving…' : 'Save Payout Details' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Password -->
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <Lock class="w-5 h-5 text-red-500" />
          <h2 class="font-semibold text-gray-900 dark:text-white">Change Password</h2>
        </div>
        <form class="grid grid-cols-1 sm:grid-cols-3 gap-4" @submit.prevent="savePassword">
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Current Password</label>
            <input v-model="passwordForm.currentPassword" type="password" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">New Password</label>
            <input v-model="passwordForm.password" type="password" required minlength="8" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div>
            <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Confirm New Password</label>
            <input v-model="passwordForm.passwordConfirmation" type="password" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
          </div>
          <div class="sm:col-span-3">
            <button type="submit" :disabled="savingPassword" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50">
              {{ savingPassword ? 'Updating…' : 'Update Password' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Two-Factor Authentication -->
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <ShieldCheck class="w-5 h-5 text-green-500" />
          <h2 class="font-semibold text-gray-900 dark:text-white">Two-Factor Authentication</h2>
        </div>

        <template v-if="twoFactorStep === 'idle'">
          <p v-if="twoFactor.enabled" class="text-sm text-gray-600 dark:text-gray-400 mb-4">
            Two-factor authentication is <span class="font-semibold text-green-600 dark:text-green-400">enabled</span> on your account.
          </p>
          <p v-else class="text-sm text-gray-600 dark:text-gray-400 mb-4">
            Add an extra layer of security by requiring a code from an authenticator app (Google Authenticator, Authy, etc.) when you log in.
          </p>
          <div class="flex flex-wrap gap-3">
            <button v-if="!twoFactor.enabled" :disabled="twoFactorBusy" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50" @click="startTwoFactorSetup">
              Enable Two-Factor Authentication
            </button>
            <template v-else>
              <button :disabled="twoFactorBusy" class="px-5 py-2 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 text-sm font-medium rounded-lg disabled:opacity-50" @click="regenerateRecoveryCodes">
                Regenerate Recovery Codes
              </button>
              <button :disabled="twoFactorBusy" class="px-5 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg disabled:opacity-50" @click="disableTwoFactor">
                Disable Two-Factor Authentication
              </button>
            </template>
          </div>
        </template>

        <template v-else-if="twoFactorStep === 'confirm'">
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">Scan this QR code with your authenticator app, then enter the 6-digit code it shows.</p>
          <div class="flex flex-col sm:flex-row gap-6 items-start">
            <img :src="twoFactorSetup?.qrCodeDataUri" alt="Two-factor QR code" class="rounded-lg border border-gray-200 dark:border-gray-700 w-48 h-48">
            <div class="flex-1 space-y-3">
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Can't scan? Enter this code manually</label>
                <code class="block text-xs bg-gray-100 dark:bg-gray-800 px-3 py-2 rounded-lg break-all text-gray-800 dark:text-gray-200">{{ twoFactorSetup?.secret }}</code>
              </div>
              <form class="flex gap-3" @submit.prevent="confirmTwoFactor">
                <input v-model="twoFactorConfirmCode" type="text" inputmode="numeric" maxlength="6" required placeholder="123456"
                  class="w-40 rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white tracking-widest text-center font-bold">
                <button type="submit" :disabled="twoFactorBusy" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50">
                  {{ twoFactorBusy ? 'Verifying…' : 'Confirm' }}
                </button>
              </form>
              <button type="button" class="text-sm text-gray-500 hover:text-gray-700 dark:hover:text-gray-300" @click="twoFactorStep = 'idle'">Cancel</button>
            </div>
          </div>
        </template>

        <template v-else-if="twoFactorStep === 'recovery-codes'">
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
            Save these recovery codes somewhere safe. Each one can be used once to log in if you lose access to your authenticator app — they won't be shown again.
          </p>
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
            <code v-for="rc in twoFactorRecoveryCodes" :key="rc" class="text-xs bg-gray-100 dark:bg-gray-800 px-3 py-2 rounded-lg text-center text-gray-800 dark:text-gray-200">{{ rc }}</code>
          </div>
          <button class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg" @click="finishTwoFactorSetup">
            I've saved these codes
          </button>
        </template>
      </div>

      <!-- Email Preferences -->
      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <Mail class="w-5 h-5 text-purple-500" />
          <h2 class="font-semibold text-gray-900 dark:text-white">Email Preferences</h2>
        </div>
        <form class="space-y-3" @submit.prevent="saveEmailPreferences">
          <label class="flex items-center gap-3">
            <input v-model="emailForm.sendOtpEmail" type="checkbox" class="h-4 w-4 rounded border-gray-300 text-blue-600">
            <span class="text-sm text-gray-700 dark:text-gray-300">Send OTP emails for withdrawals</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="emailForm.sendRoiEmail" type="checkbox" class="h-4 w-4 rounded border-gray-300 text-blue-600">
            <span class="text-sm text-gray-700 dark:text-gray-300">Send ROI/profit notification emails</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="emailForm.sendInvPlanEmail" type="checkbox" class="h-4 w-4 rounded border-gray-300 text-blue-600">
            <span class="text-sm text-gray-700 dark:text-gray-300">Send investment plan update emails</span>
          </label>
          <button type="submit" :disabled="savingEmail" class="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg disabled:opacity-50">
            {{ savingEmail ? 'Saving…' : 'Save Preferences' }}
          </button>
        </form>
      </div>
    </template>
  </div>
</template>
