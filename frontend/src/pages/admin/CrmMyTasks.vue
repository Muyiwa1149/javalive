<script setup>
import { ref, onMounted } from 'vue'
import { ListChecks, Check } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const tasks = ref([])

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/crm/tasks/mine')
    tasks.value = data
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function markDone(task) {
  await api.post(`/admin/crm/tasks/${task.id}/done`)
  await load()
}

const priorityClass = (p) => ({
  High: 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400',
  Medium: 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400',
  Low: 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300',
}[p] || 'bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300')
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ListChecks class="w-6 h-6 text-indigo-500" /> My Tasks</h1>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="tasks.length === 0" class="text-slate-500 dark:text-slate-400">No tasks delegated to you.</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="t in tasks" :key="t.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 space-y-2">
        <div class="flex items-start justify-between">
          <p class="font-semibold text-slate-900 dark:text-white">{{ t.title }}</p>
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="priorityClass(t.priority)">{{ t.priority }}</span>
        </div>
        <p class="text-sm text-slate-500 dark:text-slate-400">{{ t.note }}</p>
        <p class="text-xs text-slate-400 dark:text-slate-500">{{ t.startDate }} → {{ t.endDate }}</p>
        <div class="flex items-center justify-between pt-1">
          <span class="px-2 py-1 rounded-full text-xs font-medium" :class="t.status === 'Completed' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-amber-100 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400'">{{ t.status }}</span>
          <button v-if="t.status !== 'Completed'" class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-medium" @click="markDone(t)">
            <Check class="w-3.5 h-3.5" /> Mark Done
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
