<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { FileText, Plus, Pencil, Trash2 } from 'lucide-vue-next'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

const tab = ref('faq')
const loading = ref(true)

const faqs = ref([])
const testimonials = ref([])
const images = ref([])
const pages = ref([])
const terms = ref({ description: '', useTerms: false })

const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const imageFile = ref(null)

const faqForm = ref({ question: '', answer: '' })
const testimonyForm = ref({ name: '', position: '', whatIsSaid: '', picture: '' })
const imageForm = ref({ title: '', description: '' })
const pageForm = ref({ title: '', description: '' })

async function loadAll() {
  loading.value = true
  try {
    const [f, t, i, p, tp] = await Promise.all([
      api.get('/admin/content/faqs'),
      api.get('/admin/content/testimonials'),
      api.get('/admin/content/images'),
      api.get('/admin/content/pages'),
      api.get('/admin/content/privacy-policy'),
    ])
    faqs.value = f.data
    testimonials.value = t.data
    images.value = i.data
    pages.value = p.data
    terms.value = tp.data
  } finally {
    loading.value = false
  }
}
onMounted(loadAll)

function openCreate() {
  editingId.value = null
  imageFile.value = null
  if (tab.value === 'faq') faqForm.value = { question: '', answer: '' }
  else if (tab.value === 'testimonials') testimonyForm.value = { name: '', position: '', whatIsSaid: '', picture: '' }
  else if (tab.value === 'images') imageForm.value = { title: '', description: '' }
  else if (tab.value === 'pages') pageForm.value = { title: '', description: '' }
  showForm.value = true
}

function openEdit(item) {
  editingId.value = item.id
  imageFile.value = null
  if (tab.value === 'faq') faqForm.value = { ...item }
  else if (tab.value === 'testimonials') testimonyForm.value = { ...item }
  else if (tab.value === 'images') imageForm.value = { ...item }
  else if (tab.value === 'pages') pageForm.value = { ...item }
  showForm.value = true
}

async function submitForm() {
  saving.value = true
  try {
    if (tab.value === 'faq') {
      if (editingId.value) await api.put(`/admin/content/faqs/${editingId.value}`, faqForm.value)
      else await api.post('/admin/content/faqs', faqForm.value)
    } else if (tab.value === 'testimonials') {
      if (editingId.value) await api.put(`/admin/content/testimonials/${editingId.value}`, testimonyForm.value)
      else await api.post('/admin/content/testimonials', testimonyForm.value)
    } else if (tab.value === 'images') {
      const fd = new FormData()
      fd.append('title', imageForm.value.title)
      fd.append('description', imageForm.value.description)
      if (imageFile.value) fd.append('image', imageFile.value)
      if (editingId.value) await api.put(`/admin/content/images/${editingId.value}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      else await api.post('/admin/content/images', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    } else if (tab.value === 'pages') {
      if (editingId.value) await api.put(`/admin/content/pages/${editingId.value}`, pageForm.value)
      else await api.post('/admin/content/pages', pageForm.value)
    }
    showForm.value = false
    await loadAll()
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function removeFaq(item) {
  const confirm = await Swal.fire({ icon: 'warning', title: 'Delete this FAQ?', showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/content/faqs/${item.id}`)
  await loadAll()
}

async function removeTestimonial(item) {
  const confirm = await Swal.fire({ icon: 'warning', title: 'Delete this testimonial?', showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/content/testimonials/${item.id}`)
  await loadAll()
}

async function saveTerms() {
  saving.value = true
  try {
    const { data } = await api.put('/admin/content/privacy-policy', terms.value)
    terms.value = data
    Swal.fire({ icon: 'success', title: 'Privacy policy saved', timer: 1200, showConfirmButton: false })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><FileText class="w-6 h-6 text-indigo-500" /> Content Management</h1>

    <div class="flex flex-wrap gap-2 border-b border-slate-200 dark:border-white/5">
      <button v-for="t in [['faq','FAQ'],['testimonials','Testimonials'],['images','Images'],['pages','Page Content'],['privacy','Privacy Policy']]" :key="t[0]"
        class="px-4 py-2 text-sm font-medium border-b-2 -mb-px"
        :class="tab === t[0] ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400' : 'border-transparent text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'"
        @click="tab = t[0]">{{ t[1] }}</button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <template v-else>
      <div v-if="tab !== 'privacy'" class="flex justify-end">
        <button class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium" @click="openCreate">
          <Plus class="w-4 h-4" /> Add {{ tab === 'faq' ? 'FAQ' : tab === 'testimonials' ? 'Testimonial' : tab === 'images' ? 'Image' : 'Content' }}
        </button>
      </div>

      <!-- FAQ -->
      <div v-if="tab === 'faq'" class="space-y-3">
        <div v-if="faqs.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No FAQs yet.</div>
        <div v-for="f in faqs" :key="f.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex justify-between gap-3">
          <div class="min-w-0">
            <p class="font-medium text-slate-900 dark:text-white">{{ f.question }}</p>
            <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">{{ f.answer }}</p>
          </div>
          <div class="flex items-start gap-1 shrink-0">
            <button class="p-2 text-slate-400 hover:text-indigo-500 rounded-lg" @click="openEdit(f)"><Pencil class="w-4 h-4" /></button>
            <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="removeFaq(f)"><Trash2 class="w-4 h-4" /></button>
          </div>
        </div>
      </div>

      <!-- Testimonials -->
      <div v-else-if="tab === 'testimonials'" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-if="testimonials.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No testimonials yet.</div>
        <div v-for="t in testimonials" :key="t.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
          <div class="flex items-start justify-between">
            <div>
              <p class="font-medium text-slate-900 dark:text-white">{{ t.name }}</p>
              <p class="text-xs text-indigo-500">{{ t.position }}</p>
            </div>
            <div class="flex items-start gap-1">
              <button class="p-2 text-slate-400 hover:text-indigo-500 rounded-lg" @click="openEdit(t)"><Pencil class="w-4 h-4" /></button>
              <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="removeTestimonial(t)"><Trash2 class="w-4 h-4" /></button>
            </div>
          </div>
          <p class="text-sm text-slate-500 dark:text-slate-400">"{{ t.whatIsSaid }}"</p>
        </div>
      </div>

      <!-- Images -->
      <div v-else-if="tab === 'images'" class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div v-if="images.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No images yet.</div>
        <div v-for="img in images" :key="img.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
          <img :src="storageUrl(img.imagePath)" class="w-full h-32 object-cover rounded-xl" />
          <p class="font-medium text-slate-900 dark:text-white text-sm">{{ img.title }}</p>
          <p class="text-xs text-slate-500 dark:text-slate-400">{{ img.description }}</p>
          <button class="inline-flex items-center gap-1 text-xs text-indigo-500 hover:underline" @click="openEdit(img)"><Pencil class="w-3.5 h-3.5" /> Edit</button>
        </div>
      </div>

      <!-- Page content -->
      <div v-else-if="tab === 'pages'" class="space-y-3">
        <div v-if="pages.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No page content yet.</div>
        <div v-for="p in pages" :key="p.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex justify-between gap-3">
          <div class="min-w-0">
            <p class="font-medium text-slate-900 dark:text-white">{{ p.title }}</p>
            <p class="text-sm text-slate-500 dark:text-slate-400 mt-1 line-clamp-2">{{ p.description }}</p>
          </div>
          <button class="p-2 text-slate-400 hover:text-indigo-500 rounded-lg shrink-0" @click="openEdit(p)"><Pencil class="w-4 h-4" /></button>
        </div>
      </div>

      <!-- Privacy policy -->
      <div v-else-if="tab === 'privacy'" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-4 max-w-3xl">
        <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
          <input v-model="terms.useTerms" type="checkbox" class="rounded" /> Require acceptance at registration
        </label>
        <textarea v-model="terms.description" rows="14" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
        <div class="flex justify-end">
          <button :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50" @click="saveTerms">{{ saving ? 'Saving…' : 'Save Privacy Policy' }}</button>
        </div>
      </div>
    </template>

    <!-- Form modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 my-8">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ editingId ? 'Edit' : 'Add' }}</h2>

        <form v-if="tab === 'faq'" class="space-y-3" @submit.prevent="submitForm">
          <input v-model="faqForm.question" required placeholder="Question" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="faqForm.answer" required rows="4" placeholder="Answer" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>

        <form v-else-if="tab === 'testimonials'" class="space-y-3" @submit.prevent="submitForm">
          <input v-model="testimonyForm.name" required placeholder="Name" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="testimonyForm.position" placeholder="Position" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="testimonyForm.whatIsSaid" required rows="3" placeholder="Testimonial text" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <input v-model="testimonyForm.picture" placeholder="Picture URL" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>

        <form v-else-if="tab === 'images'" class="space-y-3" @submit.prevent="submitForm">
          <input v-model="imageForm.title" required placeholder="Title" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="imageForm.description" required rows="3" placeholder="Description" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <input type="file" accept="image/*" :required="!editingId" class="w-full text-sm text-slate-500" @change="(e) => imageFile = e.target.files[0]" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>

        <form v-else-if="tab === 'pages'" class="space-y-3" @submit.prevent="submitForm">
          <input v-model="pageForm.title" required placeholder="Title" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="pageForm.description" required rows="6" placeholder="Content" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showForm = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
