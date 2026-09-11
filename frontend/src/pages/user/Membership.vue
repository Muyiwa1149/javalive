<script setup>
import { ref, onMounted } from 'vue'
import { GraduationCap, AlertCircle, BookOpen } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const courses = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/membership/courses')
    courses.value = data?.data?.courses || data?.data || []
  } catch (e) {
    if (e.response?.status === 503) {
      unavailable.value = true
    } else {
      errorMessage.value = e.response?.data?.message || 'Failed to load courses.'
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1 sm:gap-0">
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <GraduationCap class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Membership &amp; Courses
      </h1>
      <RouterLink to="#" class="text-sm text-blue-600 dark:text-blue-400 hover:underline">My Courses</RouterLink>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-gray-900 dark:text-white mb-1">Membership service not configured</h2>
      <p class="text-sm text-gray-500 dark:text-gray-400">This feature connects to an external learning platform that hasn't been configured with an API key yet. Ask an administrator to set it up in Settings.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-6 text-red-700 dark:text-red-300">
      {{ errorMessage }}
    </div>

    <div v-else-if="courses.length === 0" class="text-gray-500 dark:text-gray-400">No courses available yet.</div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="c in courses" :key="c.id" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6 flex flex-col">
        <BookOpen class="w-8 h-8 text-blue-500 mb-3" />
        <h3 class="font-bold text-gray-900 dark:text-white mb-2">{{ c.title || c.name }}</h3>
        <p v-if="c.description" class="text-sm text-gray-500 dark:text-gray-400 mb-4 line-clamp-3">{{ c.description }}</p>
        <div v-if="c.amount" class="text-lg font-extrabold text-gray-900 dark:text-white mt-auto">${{ c.amount }}</div>
      </div>
    </div>
  </div>
</template>
