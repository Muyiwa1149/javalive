<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { ClipboardPlus } from 'lucide-vue-next'
import api from '@/lib/api'

const router = useRouter()
const admins = ref([])
const saving = ref(false)
const form = ref({ title: '', note: '', assignedAdminId: '', startDate: '', endDate: '', priority: 'Medium' })

onMounted(async () => {
  const { data } = await api.get('/admin/crm/admins')
  admins.value = data
})

async function submit() {
  saving.value = true
  try {
    await api.post('/admin/crm/tasks', form.value)
    Swal.fire({ icon: 'success', title: 'Task created and assigned', timer: 1500, showConfirmButton: false })
    router.push({ name: 'admin.crm-tasks' })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-xl">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ClipboardPlus class="w-6 h-6 text-indigo-500" /> Create Task</h1>

    <form class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3" @submit.prevent="submit">
      <input v-model="form.title" required placeholder="Task title" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
      <textarea v-model="form.note" rows="4" placeholder="Notes" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
      <select v-model="form.assignedAdminId" required class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
        <option value="" disabled>Delegate to…</option>
        <option v-for="a in admins" :key="a.id" :value="a.id">{{ a.name }}</option>
      </select>
      <div class="grid grid-cols-2 gap-3">
        <div><label class="text-xs text-slate-500">Start date</label><input v-model="form.startDate" type="date" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">End date</label><input v-model="form.endDate" type="date" required class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
      </div>
      <select v-model="form.priority" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
        <option value="Low">Low</option>
        <option value="Medium">Medium</option>
        <option value="High">High</option>
      </select>
      <div class="flex justify-end pt-2">
        <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Create Task</button>
      </div>
    </form>
  </div>
</template>
