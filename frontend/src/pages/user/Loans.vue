<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { FilePlus } from 'lucide-vue-next'
import api from '@/lib/api'

const router = useRouter()
const submitting = ref(false)
const form = ref({ amount: '', income: '', purpose: '', duration: '', facility: '' })

const FACILITIES = ['Personal Loan', 'Business Loan', 'Investment Loan', 'Emergency Loan']
const DURATIONS = ['3 months', '6 months', '12 months', '24 months', '36 months']

async function submit() {
  submitting.value = true
  try {
    await api.post('/loans', {
      amount: Number(form.value.amount),
      income: form.value.income,
      purpose: form.value.purpose,
      duration: form.value.duration,
      facility: form.value.facility,
    })
    await Swal.fire({
      icon: 'success', title: 'Application submitted',
      text: 'Your loan is currently pending review. You will be contacted soon.',
      background: '#1F2937', color: '#E5E7EB',
    })
    router.push({ name: 'user.loans-history' })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <FilePlus class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Apply for Credit
    </h1>

    <form class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6 space-y-4" @submit.prevent="submit">
      <div>
        <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Loan Facility</label>
        <select v-model="form.facility" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
          <option value="" disabled>Select a facility</option>
          <option v-for="f in FACILITIES" :key="f" :value="f">{{ f }}</option>
        </select>
      </div>
      <div>
        <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Amount Requested</label>
        <input v-model="form.amount" type="number" min="0" step="0.01" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="5000.00">
      </div>
      <div>
        <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Monthly Income</label>
        <input v-model="form.income" type="text" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="$3,000/month">
      </div>
      <div>
        <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Repayment Duration</label>
        <select v-model="form.duration" required class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white">
          <option value="" disabled>Select duration</option>
          <option v-for="d in DURATIONS" :key="d" :value="d">{{ d }}</option>
        </select>
      </div>
      <div>
        <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Purpose</label>
        <textarea v-model="form.purpose" required rows="4" class="w-full rounded-lg border border-gray-300 dark:border-gray-700 bg-white dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white" placeholder="Describe what this loan will be used for…"></textarea>
      </div>
      <button type="submit" :disabled="submitting" class="w-full py-3 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-medium disabled:opacity-50">
        {{ submitting ? 'Submitting…' : 'Submit Application' }}
      </button>
    </form>
  </div>
</template>
