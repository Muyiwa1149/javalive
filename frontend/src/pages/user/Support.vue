<script setup>
import { ref, onMounted, computed } from 'vue'
import Swal from 'sweetalert2'
import { Headphones, Mail, Send, ExternalLink } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const settingsStore = usePublicSettingsStore()
const message = ref('')
const submitting = ref(false)

onMounted(() => settingsStore.ensureLoaded())

const messageLength = computed(() => message.value.length)
const canSubmit = computed(() => message.value.trim().length >= 10 && !submitting.value)

async function submit() {
  submitting.value = true
  try {
    await api.post('/public/contact', {
      name: authUser.user?.name,
      email: authUser.user?.email,
      subject: 'Support Request',
      message: message.value,
    })
    message.value = ''
    await Swal.fire({
      icon: 'success', title: 'Message sent', text: 'We typically respond within 24 hours during business days.',
      background: '#1F2937', color: '#E5E7EB',
    })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed to send', text: e.response?.data?.message, background: '#1F2937', color: '#E5E7EB' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex items-center gap-4">
      <div class="w-16 h-16 rounded-3xl bg-gradient-to-br from-blue-500/10 to-indigo-500/10 flex items-center justify-center">
        <Headphones class="w-8 h-8 text-blue-600 dark:text-blue-400" />
      </div>
      <div>
        <h1 class="text-2xl sm:text-3xl font-light text-gray-800 dark:text-white">Support Center</h1>
        <p class="text-sm sm:text-base text-gray-500 dark:text-gray-400">We're here to help you with any questions or concerns</p>
      </div>
    </div>

    <div class="bg-white dark:bg-gray-900 rounded-2xl p-6 border border-gray-200 dark:border-gray-800">
      <div class="flex items-center gap-4 mb-4">
        <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500/10 to-blue-600/10 flex items-center justify-center">
          <Mail class="w-6 h-6 text-blue-600 dark:text-blue-400" />
        </div>
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white">Email Support</h3>
          <p class="text-sm text-gray-500 dark:text-gray-400">Get help via email</p>
        </div>
      </div>
      <a v-if="settingsStore.settings?.contactEmail" :href="`mailto:${settingsStore.settings.contactEmail}`" class="inline-flex items-center gap-2 text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium">
        <span>{{ settingsStore.settings.contactEmail }}</span>
        <ExternalLink class="w-4 h-4" />
      </a>
    </div>

    <div class="bg-white dark:bg-gray-900 rounded-3xl p-8 border border-gray-200 dark:border-gray-800">
      <div class="text-center mb-8">
        <h2 class="text-2xl font-light text-gray-800 dark:text-white mb-2">Send us a Message</h2>
        <p class="text-gray-500 dark:text-gray-400">Fill out the form below and our support team will get back to you as soon as possible.</p>
      </div>

      <form class="space-y-6" @submit.prevent="submit">
        <div class="grid sm:grid-cols-2 gap-4">
          <div class="bg-gray-50 dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2">Your Name</label>
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-full bg-blue-500 flex items-center justify-center text-white text-sm font-semibold">{{ (authUser.user?.name || '?').charAt(0) }}</div>
              <span class="text-gray-800 dark:text-white font-medium">{{ authUser.user?.name }}</span>
            </div>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700">
            <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-2">Your Email</label>
            <div class="flex items-center gap-3">
              <Mail class="w-5 h-5 text-gray-400" />
              <span class="text-gray-800 dark:text-white">{{ authUser.user?.email }}</span>
            </div>
          </div>
        </div>

        <div>
          <label for="message" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Message <span class="text-red-500">*</span></label>
          <textarea id="message" v-model="message" rows="6" maxlength="1000" required
            class="w-full px-4 py-4 bg-gray-50 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-xl text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            placeholder="Please describe your issue or question in detail…"></textarea>
          <div class="flex justify-between items-center mt-2">
            <p class="text-sm text-gray-500 dark:text-gray-400">Please provide as much detail as possible to help us assist you better.</p>
            <span class="text-sm text-gray-400">{{ messageLength }}/1000</span>
          </div>
        </div>

        <div class="text-center">
          <button type="submit" :disabled="!canSubmit"
            class="inline-flex items-center gap-3 px-8 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-xl shadow-lg disabled:opacity-50 disabled:cursor-not-allowed">
            <span>{{ submitting ? 'Sending…' : 'Send Message' }}</span>
            <Send v-if="!submitting" class="w-5 h-5" />
          </button>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-4">We typically respond within 24 hours during business days.</p>
        </div>
      </form>
    </div>
  </div>
</template>
