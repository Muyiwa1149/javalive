<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { UserPlus, UserCheck } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const leads = ref([])
const admins = ref([])

async function load() {
  loading.value = true
  try {
    const [leadsRes, adminsRes] = await Promise.all([
      api.get('/admin/crm/leads'),
      api.get('/admin/crm/admins'),
    ])
    leads.value = leadsRes.data
    admins.value = adminsRes.data
  } finally {
    loading.value = false
  }
}
onMounted(load)

function adminName(id) {
  return admins.value.find((a) => String(a.id) === String(id))?.name
}

async function convert(lead) {
  const confirm = await Swal.fire({ icon: 'question', title: `Convert ${lead.name} to Customer?`, showCancelButton: true, confirmButtonText: 'Convert' })
  if (!confirm.isConfirmed) return
  await api.post(`/admin/crm/leads/${lead.id}/convert`)
  await load()
}

async function assign(lead) {
  const { value: adminId } = await Swal.fire({
    title: `Assign ${lead.name} to…`,
    input: 'select',
    inputOptions: Object.fromEntries(admins.value.map((a) => [a.id, a.name])),
    showCancelButton: true, confirmButtonText: 'Assign',
  })
  if (!adminId) return
  await api.post(`/admin/crm/leads/${lead.id}/assign`, { adminId: Number(adminId) })
  await load()
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><UserPlus class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Leads</h1>
      <p class="text-sm text-slate-500 dark:text-slate-400">{{ leads.length }} not-yet-converted registrations</p>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>
    <div v-else-if="leads.length === 0" class="text-slate-500 dark:text-slate-400">No leads at the moment.</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="l in leads" :key="l.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="min-w-0">
          <div class="text-slate-900 dark:text-white font-medium truncate">{{ l.name }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ l.email }}</div>
          <div class="text-xs text-slate-500 dark:text-slate-400">{{ l.phone }}</div>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Agent</div><div class="text-slate-700 dark:text-slate-300">{{ adminName(l.assignedAgent) || '—' }}</div></div>
          <div><div class="text-[11px] text-slate-500">Registered</div><div class="text-slate-500 dark:text-slate-400">{{ l.createdAt ? new Date(l.createdAt).toLocaleDateString() : '—' }}</div></div>
        </div>
        <div class="flex items-center gap-2 pt-2 border-t border-slate-100 dark:border-white/5">
          <button class="flex-1 py-2 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="assign(l)">Assign</button>
          <button class="flex-1 inline-flex items-center justify-center gap-1 py-2 rounded-lg bg-indigo-500 hover:bg-indigo-600 text-white text-xs font-medium" @click="convert(l)">
            <UserCheck class="w-3.5 h-3.5" /> Convert
          </button>
        </div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading && leads.length" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">Name</th>
            <th class="py-3 px-4 font-medium">Contact</th>
            <th class="py-3 px-4 font-medium">Assigned Agent</th>
            <th class="py-3 px-4 font-medium">Registered</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in leads" :key="l.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4 text-slate-900 dark:text-white font-medium">{{ l.name }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ l.email }}<br /><span class="text-xs">{{ l.phone }}</span></td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ adminName(l.assignedAgent) || '—' }}</td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ l.createdAt ? new Date(l.createdAt).toLocaleDateString() : '—' }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-2">
                <button class="px-3 py-1.5 rounded-lg bg-slate-100 dark:bg-white/5 text-slate-700 dark:text-slate-200 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10" @click="assign(l)">Assign</button>
                <button class="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-indigo-500 hover:bg-indigo-600 text-white text-xs font-medium" @click="convert(l)">
                  <UserCheck class="w-3.5 h-3.5" /> Convert
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
