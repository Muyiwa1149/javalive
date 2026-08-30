<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { ShieldCheck, ShieldAlert, Clock, Upload } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loadingStatus = ref(true)
const status = ref(null)
const submitting = ref(false)

const form = ref({
  firstName: '', lastName: '', email: '', phoneNumber: '', dob: '', socialMedia: '',
  address: '', city: '', state: '', country: '', documentType: 'Passport',
})
const frontImg = ref(null)
const backImg = ref(null)

onMounted(async () => {
  await authUser.fetchProfile()
  form.value.email = authUser.user?.email || ''
  try {
    const { data } = await api.get('/kyc/status')
    status.value = data
  } finally {
    loadingStatus.value = false
  }
})

function onFrontChange(e) { frontImg.value = e.target.files[0] || null }
function onBackChange(e) { backImg.value = e.target.files[0] || null }

async function submit() {
  if (!frontImg.value || !backImg.value) {
    Swal.fire({ icon: 'warning', title: 'Missing documents', text: 'Please upload both the front and back of your document.', background: '#1F2937', color: '#E5E7EB' })
    return
  }
  submitting.value = true
  try {
    const payload = new FormData()
    Object.entries(form.value).forEach(([key, value]) => payload.append(key, value ?? ''))
    payload.append('frontImg', frontImg.value)
    payload.append('backImg', backImg.value)
    const { data } = await api.post('/kyc', payload, { headers: { 'Content-Type': 'multipart/form-data' } })
    status.value = data
    await Swal.fire({
      icon: 'success', title: 'Submitted!',
      text: 'Please wait while we verify your application. You will receive an email regarding the status.',
      background: '#1F2937', color: '#E5E7EB',
    })
  } catch (e) {
    await Swal.fire({ icon: 'error', title: 'Submission failed', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Identity Verification</h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">Complete your KYC to unlock full trading and withdrawal features.</p>
    </div>

    <div v-if="loadingStatus" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <div v-else-if="status?.submitted" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
      <div v-if="status.status === 'Verified'" class="flex items-center gap-3 text-green-700 dark:text-green-300">
        <ShieldCheck class="w-8 h-8" />
        <div>
          <div class="font-semibold">Your account is verified</div>
          <div class="text-sm text-gray-500 dark:text-gray-400">Submitted {{ new Date(status.submittedAt).toLocaleDateString() }} — {{ status.documentType }}</div>
        </div>
      </div>
      <div v-else-if="status.status === 'Under review'" class="flex items-center gap-3 text-yellow-700 dark:text-yellow-300">
        <Clock class="w-8 h-8" />
        <div>
          <div class="font-semibold">Your verification is under review</div>
          <div class="text-sm text-gray-500 dark:text-gray-400">We'll email you once it's been reviewed.</div>
        </div>
      </div>
      <div v-else class="flex items-center gap-3 text-red-700 dark:text-red-300">
        <ShieldAlert class="w-8 h-8" />
        <div>
          <div class="font-semibold">Verification {{ status.status }}</div>
          <div class="text-sm text-gray-500 dark:text-gray-400">Please contact support or submit a new application below.</div>
        </div>
      </div>
    </div>

    <form v-if="!loadingStatus && (!status?.submitted || status.status !== 'Verified')" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4" @submit.prevent="submit">
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">First Name</label>
          <input v-model="form.firstName" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Last Name</label>
          <input v-model="form.lastName" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Email</label>
          <input v-model="form.email" type="email" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Phone Number</label>
          <input v-model="form.phoneNumber" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Date of Birth</label>
          <input v-model="form.dob" type="date" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Social Media (optional)</label>
          <input v-model="form.socialMedia" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div class="sm:col-span-2">
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Address</label>
          <input v-model="form.address" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">City</label>
          <input v-model="form.city" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">State / Province</label>
          <input v-model="form.state" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Country</label>
          <input v-model="form.country" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Document Type</label>
          <select v-model="form.documentType" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-gray-900 dark:text-white">
            <option>Passport</option>
            <option>Driver's License</option>
            <option>National ID Card</option>
          </select>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Document — Front</label>
          <label class="flex flex-col items-center justify-center gap-2 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg p-6 cursor-pointer hover:border-blue-400">
            <Upload class="w-6 h-6 text-gray-400" />
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ frontImg?.name || 'Click to upload (JPG/PNG)' }}</span>
            <input type="file" accept="image/jpeg,image/png,image/jpg" class="hidden" @change="onFrontChange">
          </label>
        </div>
        <div>
          <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">Document — Back</label>
          <label class="flex flex-col items-center justify-center gap-2 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg p-6 cursor-pointer hover:border-blue-400">
            <Upload class="w-6 h-6 text-gray-400" />
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ backImg?.name || 'Click to upload (JPG/PNG)' }}</span>
            <input type="file" accept="image/jpeg,image/png,image/jpg" class="hidden" @change="onBackChange">
          </label>
        </div>
      </div>

      <button type="submit" :disabled="submitting" class="w-full px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50">
        {{ submitting ? 'Submitting…' : 'Submit for Verification' }}
      </button>
    </form>
  </div>
</template>
