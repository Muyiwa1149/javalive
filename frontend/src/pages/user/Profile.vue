<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { User, CreditCard, Lock, Mail } from 'lucide-vue-next'
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

onMounted(async () => {
  try {
    const { data } = await api.get('/profile')
    applyProfile(data)
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
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Profile Settings</h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">Manage your personal information, payout details, security, and email preferences.</p>
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
