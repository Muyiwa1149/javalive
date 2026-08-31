<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { ClipboardList, Check, Trash2 } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const tasks = ref([])

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/crm/tasks')
    tasks.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

const priorityClass = (p) => ({
  High: 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400',
  Medium: 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400',
  Low: 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300',
}[p] || 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300')

async function markDone(task) {
  await api.post(`/admin/crm/tasks/${task.id}/done`)
  await load()
}

async function remove(task) {
  const confirm = await Swal.fire({ icon: 'warning', title: `Delete "${task.title}"?`, showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/crm/tasks/${task.id}`)
  await load()
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ClipboardList class="w-6 h-6 text-indigo-500" /> Manage Tasks</h1>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="tasks.length === 0" class="text-slate-500 dark:text-slate-400">No tasks created yet.</div>

    <div v-else class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">Task</th>
            <th class="py-3 px-4 font-medium">Delegated To</th>
            <th class="py-3 px-4 font-medium">Priority</th>
            <th class="py-3 px-4 font-medium">Dates</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in tasks" :key="t.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ t.title }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400 truncate max-w-xs">{{ t.note }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ t.assignedAdminName }}</td>
            <td class="py-3 px-4"><span class="px-2 py-1 rounded-full text-xs font-medium" :class="priorityClass(t.priority)">{{ t.priority }}</span></td>
            <td class="py-3 px-4 text-xs text-slate-500 dark:text-slate-400">{{ t.startDate }} → {{ t.endDate }}</td>
            <td class="py-3 px-4">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="t.status === 'Completed' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400'">{{ t.status }}</span>
            </td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-1">
                <button v-if="t.status !== 'Completed'" class="p-2 text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg" title="Mark done" @click="markDone(t)"><Check class="w-4 h-4" /></button>
                <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" title="Delete" @click="remove(t)"><Trash2 class="w-4 h-4" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
