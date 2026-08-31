<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { User, KeyRound } from 'lucide-vue-next'
import api from '@/lib/api'
import { useAuthAdminStore } from '@/stores/authAdmin'

const authAdmin = useAuthAdminStore()
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = ref({ firstName: '', lastName: '', phone: '', enable2fa: false })
const passwordForm = ref({ oldPassword: '', password: '', passwordConfirmation: '' })

onMounted(async () => {
  if (!authAdmin.admin) await authAdmin.fetchProfile().catch(() => {})
  if (authAdmin.admin) {
    profileForm.value = {
      firstName: authAdmin.admin.firstName, lastName: authAdmin.admin.lastName,
      phone: authAdmin.admin.phone || '', enable2fa: false,
    }
  }
})

async function saveProfile() {
  savingProfile.value = true
  try {
    await api.put('/admin/profile', profileForm.value)
    await authAdmin.fetchProfile()
    Swal.fire({ icon: 'success', title: 'Profile updated', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  savingPassword.value = true
  try {
    await api.put('/admin/profile/password', passwordForm.value)
    passwordForm.value = { oldPassword: '', password: '', passwordConfirmation: '' }
    Swal.fire({ icon: 'success', title: 'Password changed', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-2xl">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white">My Profile</h1>

    <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
      <h2 class="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2 mb-4"><User class="w-5 h-5 text-indigo-500" /> Profile Details</h2>
      <form class="space-y-3" @submit.prevent="saveProfile">
        <div class="grid grid-cols-2 gap-3">
          <input v-model="profileForm.firstName" required placeholder="First name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="profileForm.lastName" required placeholder="Last name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
        </div>
        <input v-model="profileForm.phone" placeholder="Phone" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
        <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
          <input v-model="profileForm.enable2fa" type="checkbox" class="rounded" /> Enable email OTP 2FA on login
        </label>
        <div class="flex justify-end pt-2">
          <button type="submit" :disabled="savingProfile" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save Changes</button>
        </div>
      </form>
    </div>

    <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
      <h2 class="text-base font-semibold text-slate-900 dark:text-white flex items-center gap-2 mb-4"><KeyRound class="w-5 h-5 text-indigo-500" /> Change Password</h2>
      <form class="space-y-3" @submit.prevent="savePassword">
        <input v-model="passwordForm.oldPassword" type="password" required placeholder="Current password" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
        <input v-model="passwordForm.password" type="password" required minlength="8" placeholder="New password (min 8 chars)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
        <input v-model="passwordForm.passwordConfirmation" type="password" required placeholder="Confirm new password" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
        <div class="flex justify-end pt-2">
          <button type="submit" :disabled="savingPassword" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Change Password</button>
        </div>
      </form>
    </div>
  </div>
</template>
