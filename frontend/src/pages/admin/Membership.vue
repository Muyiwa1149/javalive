<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { GraduationCap, AlertCircle, Plus } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const unavailable = ref(false)
const errorMessage = ref('')
const courses = ref(null)
const categories = ref(null)

const showCourseForm = ref(false)
const courseForm = ref({ title: '', amount: '', image_url: '', category: '', desc: '' })
const showCategoryForm = ref(false)
const categoryForm = ref({ category: '' })
const saving = ref(false)

async function load() {
  loading.value = true
  unavailable.value = false
  errorMessage.value = ''
  try {
    const [c, cat] = await Promise.all([
      api.get('/admin/membership/courses'),
      api.get('/admin/membership/categories'),
    ])
    courses.value = c.data
    categories.value = cat.data
  } catch (e) {
    if (e.response?.status === 503) unavailable.value = true
    else errorMessage.value = e.response?.data?.message || 'Failed to load membership data.'
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function addCourse() {
  saving.value = true
  try {
    await api.post('/admin/membership/courses', courseForm.value)
    showCourseForm.value = false
    courseForm.value = { title: '', amount: '', image_url: '', category: '', desc: '' }
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function addCategory() {
  saving.value = true
  try {
    await api.post('/admin/membership/categories', categoryForm.value)
    showCategoryForm.value = false
    categoryForm.value = { category: '' }
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><GraduationCap class="w-6 h-6 text-indigo-500" /> Courses & Categories</h1>
    <p class="text-sm text-slate-500 dark:text-slate-400">Proxies the external membership/courses API — see Settings → App Settings for the merchant key.</p>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <div v-else-if="unavailable" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-8 text-center">
      <AlertCircle class="w-10 h-10 text-amber-500 mx-auto mb-3" />
      <h2 class="font-semibold text-slate-900 dark:text-white mb-1">Membership service not configured</h2>
      <p class="text-sm text-slate-500 dark:text-slate-400">This feature connects to an external courses/lessons API that hasn't been configured with a merchant key yet. Set one in Settings → App Settings.</p>
    </div>

    <div v-else-if="errorMessage" class="bg-rose-50 dark:bg-rose-500/10 border border-rose-200 dark:border-rose-800 rounded-2xl p-6 text-rose-700 dark:text-rose-300">{{ errorMessage }}</div>

    <template v-else>
      <div class="flex gap-2">
        <button class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-xs font-medium" @click="showCourseForm = true"><Plus class="w-3.5 h-3.5" /> Add Course</button>
        <button class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-xs font-medium" @click="showCategoryForm = true"><Plus class="w-3.5 h-3.5" /> Add Category</button>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
        <h2 class="font-semibold text-slate-900 dark:text-white mb-3">Courses</h2>
        <pre class="text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap overflow-x-auto">{{ JSON.stringify(courses, null, 2) }}</pre>
      </div>
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6">
        <h2 class="font-semibold text-slate-900 dark:text-white mb-3">Categories</h2>
        <pre class="text-xs text-slate-600 dark:text-slate-300 whitespace-pre-wrap overflow-x-auto">{{ JSON.stringify(categories, null, 2) }}</pre>
      </div>
    </template>

    <div v-if="showCourseForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showCourseForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Add Course</h2>
        <form class="space-y-3" @submit.prevent="addCourse">
          <input v-model="courseForm.title" required placeholder="Title" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="courseForm.amount" placeholder="Amount (blank = free)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="courseForm.image_url" placeholder="Image URL" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="courseForm.category" placeholder="Category" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="courseForm.desc" rows="3" placeholder="Description" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300" @click="showCourseForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showCategoryForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showCategoryForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-sm p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Add Category</h2>
        <form class="space-y-3" @submit.prevent="addCategory">
          <input v-model="categoryForm.category" required placeholder="Category name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300" @click="showCategoryForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
