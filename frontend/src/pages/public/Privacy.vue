<script setup>
import { ref, onMounted } from 'vue'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import api from '@/lib/api'

const settingsStore = usePublicSettingsStore()
const detailsOpen = ref(false)
const description = ref('')

onMounted(async () => {
  await settingsStore.ensureLoaded()
  const { data } = await api.get('/public/privacy-policy')
  description.value = data.description
})
</script>

<template>
  <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
    <div class="absolute inset-0 z-20 md:z-0 pointer-events-none">
      <div class="absolute top-0 left-0 w-full h-full opacity-60 md:opacity-20">
        <svg class="absolute top-0 left-0 w-full h-full" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="privacy-a" x1="50%" x2="50%" y1="0%" y2="100%">
              <stop stop-color="#3B82F6" stop-opacity=".25" offset="0%"/>
              <stop stop-color="#10B981" stop-opacity=".2" offset="100%"/>
            </linearGradient>
          </defs>
          <path fill="url(#privacy-a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(0 -50)" />
          <path fill="url(#privacy-a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(350 150)" />
        </svg>
      </div>
      <div class="absolute bottom-0 right-0 w-full h-full opacity-50 md:opacity-10">
        <svg width="100%" height="100%" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" stroke="#6366F1" stroke-width="2">
            <path d="M769 229L1037 260.9M927 880L731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737M490 85L309 538 40 599 295 764"/>
            <path d="M733 738L520 660M603 493L731 737M520 660L309 538"/>
          </g>
        </svg>
      </div>
    </div>

    <div class="relative z-10 px-4 py-16 mx-auto max-w-7xl sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Legal Documentation
        </div>
        <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
          <span class="block">Privacy Policy</span>
          <span class="block mt-1 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-teal-400">Your Data Protection</span>
        </h1>
        <p class="max-w-2xl mt-5 mx-auto text-xl text-gray-300">
          Understanding how we protect your information and respect your privacy
        </p>
      </div>
    </div>
  </section>

  <section class="py-12 bg-gray-900">
    <div class="container max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden">
        <div class="p-6 md:p-10">
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div class="prose prose-lg prose-invert max-w-none" v-html="description"></div>

          <div class="mt-12 text-center">
            <button
              @click="detailsOpen = !detailsOpen"
              class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150">
              <span>{{ detailsOpen ? 'Hide Data Protection Details' : 'Learn More About Data Protection' }}</span>
              <svg class="ml-2 -mr-1 h-5 w-5 transition-transform" :class="{ 'rotate-180': detailsOpen }" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>

            <div v-if="detailsOpen" class="animate-fadeIn mt-8 bg-gray-900 bg-opacity-50 p-6 rounded-xl backdrop-blur-sm border border-gray-700">
              <p class="text-gray-300">
                At {{ settingsStore.settings?.siteName }}, we take data protection very seriously. All user data is encrypted and stored securely. We never share your personal information with third parties without your explicit consent, and we adhere strictly to international data protection regulations.
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
        <RouterLink to="/about" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">About Us</h3>
          <p class="text-gray-400">Learn more about our company and mission</p>
        </RouterLink>
        <RouterLink to="/contact" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">Contact Us</h3>
          <p class="text-gray-400">Questions about privacy? Get in touch</p>
        </RouterLink>
        <RouterLink to="/register" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">Start Trading</h3>
          <p class="text-gray-400">Create an account to begin trading</p>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
