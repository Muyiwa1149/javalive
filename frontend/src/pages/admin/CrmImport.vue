<script setup>
import { ref } from 'vue'
import Swal from 'sweetalert2'
import { Upload, Download, FileSpreadsheet } from 'lucide-vue-next'
import api from '@/lib/api'

const file = ref(null)
const uploading = ref(false)
const result = ref(null)
const downloading = ref(false)

function onFileChange(e) {
  file.value = e.target.files[0] || null
  result.value = null
}

async function downloadTemplate() {
  downloading.value = true
  try {
    const response = await api.get('/admin/import/template', { responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = 'leads.xlsx'
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
  } finally {
    downloading.value = false
  }
}

async function upload() {
  if (!file.value) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file.value)
    const { data } = await api.post('/admin/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    result.value = data
    if (data.imported > 0) {
      Swal.fire({ icon: 'success', title: `${data.imported} leads imported`, timer: 1800, showConfirmButton: false })
    }
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Import failed', text: e.response?.data?.message })
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-xl">
    <div>
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><FileSpreadsheet class="w-6 h-6 text-indigo-500" /> Excel Import</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">Bulk-import leads from an Excel file. New accounts default to password "password".</p>
    </div>

    <button :disabled="downloading" class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-sm font-medium hover:bg-slate-200 dark:hover:bg-white/10 disabled:opacity-50" @click="downloadTemplate">
      <Download class="w-4 h-4" /> Download Template
    </button>

    <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-4">
      <input type="file" accept=".xlsx,.xls" class="w-full text-sm text-slate-500 dark:text-slate-400" @change="onFileChange" />
      <p class="text-xs text-slate-500 dark:text-slate-400">Expected columns: name, email, username, country, phone_number</p>
      <button :disabled="!file || uploading" class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium disabled:opacity-50" @click="upload">
        <Upload class="w-4 h-4" /> {{ uploading ? 'Importing…' : 'Import' }}
      </button>
    </div>

    <div v-if="result" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-2">
      <p class="text-sm text-slate-900 dark:text-white"><span class="font-semibold text-emerald-500">{{ result.imported }}</span> imported, <span class="font-semibold text-amber-500">{{ result.skipped }}</span> skipped</p>
      <ul v-if="result.errors.length" class="text-xs text-slate-500 dark:text-slate-400 list-disc pl-4 space-y-0.5 max-h-48 overflow-y-auto">
        <li v-for="(e, i) in result.errors" :key="i">{{ e }}</li>
      </ul>
    </div>
  </div>
</template>
