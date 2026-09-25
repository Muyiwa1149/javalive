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
    <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ClipboardList class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Manage Tasks</h1>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="tasks.length === 0" class="text-slate-500 dark:text-slate-400">No tasks created yet.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="t in tasks" :key="t.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ t.title }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ t.note }}</div>
          </div>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium" :class="priorityClass(t.priority)">{{ t.priority }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Delegated to</div><div class="text-slate-700 dark:text-slate-300">{{ t.assignedAdminName }}</div></div>
          <div><div class="text-[11px] text-slate-500">Status</div><span class="px-2 py-0.5 rounded-full text-[11px] font-medium" :class="t.status === 'Completed' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400'">{{ t.status }}</span></div>
        </div>
        <div class="text-xs text-slate-500 dark:text-slate-400">{{ t.startDate }} → {{ t.endDate }}</div>
        <div class="flex items-center gap-1 pt-2 border-t border-slate-100 dark:border-white/5">
          <button v-if="t.status !== 'Completed'" class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-500/10 rounded-lg" @click="markDone(t)"><Check class="w-3.5 h-3.5" /> Mark done</button>
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="remove(t)"><Trash2 class="w-3.5 h-3.5" /> Delete</button>
        </div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && tasks.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
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
