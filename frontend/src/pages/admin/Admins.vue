<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { ShieldAlert, UserPlus, Ban, CheckCircle2, Pencil, Trash2, Send } from 'lucide-vue-next'
import api from '@/lib/api'

const TYPES = ['Super Admin', 'Admin', 'Rentention Agent', 'Conversion Agent']

const loading = ref(true)
const admins = ref([])
const saving = ref(false)

const showAdd = ref(false)
const addForm = ref({ firstName: '', lastName: '', email: '', phone: '', type: 'Admin', password: '' })

const showEdit = ref(false)
const editForm = ref({ id: null, firstName: '', lastName: '', email: '', phone: '', type: 'Admin' })

const showMail = ref(false)
const mailTarget = ref(null)
const mailForm = ref({ subject: '', message: '' })

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/admins')
    admins.value = data
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Access denied', text: e.response?.data?.message || 'Only Super Admins can view this page.' })
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function createAdmin() {
  saving.value = true
  try {
    await api.post('/admin/admins', addForm.value)
    showAdd.value = false
    addForm.value = { firstName: '', lastName: '', email: '', phone: '', type: 'Admin', password: '' }
    await load()
    Swal.fire({ icon: 'success', title: 'Admin created', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

function openEdit(admin) {
  editForm.value = { id: admin.id, firstName: admin.firstName, lastName: admin.lastName, email: admin.email, phone: admin.phone || '', type: admin.type }
  showEdit.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await api.put(`/admin/admins/${editForm.value.id}`, editForm.value)
    showEdit.value = false
    await load()
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function toggleBlock(admin) {
  const blocking = admin.status !== 'blocked'
  const confirm = await Swal.fire({
    icon: 'question', title: blocking ? `Block ${admin.firstName}?` : `Unblock ${admin.firstName}?`,
    showCancelButton: true, confirmButtonText: blocking ? 'Block' : 'Unblock',
    confirmButtonColor: blocking ? '#e11d48' : '#6366f1',
  })
  if (!confirm.isConfirmed) return
  await api.post(`/admin/admins/${admin.id}/${blocking ? 'block' : 'unblock'}`)
  await load()
}

async function remove(admin) {
  const confirm = await Swal.fire({
    icon: 'warning', title: `Delete ${admin.firstName} ${admin.lastName}?`, text: 'This cannot be undone.',
    showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48',
  })
  if (!confirm.isConfirmed) return
  try {
    await api.delete(`/admin/admins/${admin.id}`)
    await load()
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  }
}

function openMail(admin) {
  mailTarget.value = admin
  mailForm.value = { subject: '', message: '' }
  showMail.value = true
}

async function sendMail() {
  saving.value = true
  try {
    await api.post(`/admin/admins/${mailTarget.value.id}/mail`, mailForm.value)
    showMail.value = false
    Swal.fire({ icon: 'success', title: 'Email sent', timer: 1200, showConfirmButton: false })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><ShieldAlert class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> Manage Admins</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400">Super Admin only — server-enforced, not just hidden nav</p>
      </div>
      <button class="inline-flex items-center justify-center gap-2 px-3.5 py-2 sm:px-4 sm:py-2.5 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 text-white text-sm font-medium shadow-lg shadow-indigo-500/20" @click="showAdd = true">
        <UserPlus class="w-4 h-4" /> Add Manager
      </button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <!-- Mobile: stacked cards -->
    <div v-else class="sm:hidden space-y-3">
      <div v-for="a in admins" :key="a.id" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-4 space-y-2">
        <div class="flex items-start justify-between gap-2">
          <div class="min-w-0">
            <div class="text-slate-900 dark:text-white font-medium truncate">{{ a.firstName }} {{ a.lastName }}</div>
            <div class="text-xs text-slate-500 dark:text-slate-400 truncate">{{ a.email }}</div>
          </div>
          <span class="shrink-0 px-2 py-0.5 rounded-full text-[11px] font-medium" :class="a.status === 'blocked' ? 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400' : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'">{{ a.status }}</span>
        </div>
        <div class="grid grid-cols-2 gap-2 text-sm pt-1">
          <div><div class="text-[11px] text-slate-500">Role</div><div class="text-slate-700 dark:text-slate-300">{{ a.type }}</div></div>
          <div><div class="text-[11px] text-slate-500">2FA</div><div class="text-slate-500 dark:text-slate-400">{{ a.enable2fa ? 'On' : 'Off' }}</div></div>
        </div>
        <div class="flex items-center gap-1 pt-2 border-t border-slate-100 dark:border-white/5">
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5 rounded-lg" @click="openEdit(a)"><Pencil class="w-3.5 h-3.5" /> Edit</button>
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5 rounded-lg" @click="openMail(a)"><Send class="w-3.5 h-3.5" /> Email</button>
          <button class="flex-1 inline-flex items-center justify-center gap-1.5 py-2 text-xs font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-white/5" :class="a.status === 'blocked' ? 'text-emerald-600' : 'text-amber-600'" @click="toggleBlock(a)">
            <CheckCircle2 v-if="a.status === 'blocked'" class="w-3.5 h-3.5" />
            <Ban v-else class="w-3.5 h-3.5" />
            {{ a.status === 'blocked' ? 'Unblock' : 'Block' }}
          </button>
          <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" title="Delete" @click="remove(a)"><Trash2 class="w-3.5 h-3.5" /></button>
        </div>
      </div>
    </div>

    <!-- Desktop: table -->
    <div v-if="!loading" class="hidden sm:block bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-left text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-white/5">
            <th class="py-3 px-4 font-medium">Name</th>
            <th class="py-3 px-4 font-medium">Role</th>
            <th class="py-3 px-4 font-medium">Status</th>
            <th class="py-3 px-4 font-medium">2FA</th>
            <th class="py-3 px-4 font-medium text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in admins" :key="a.id" class="border-b border-slate-100 dark:border-white/5 last:border-0">
            <td class="py-3 px-4">
              <div class="text-slate-900 dark:text-white font-medium">{{ a.firstName }} {{ a.lastName }}</div>
              <div class="text-xs text-slate-500 dark:text-slate-400">{{ a.email }}</div>
            </td>
            <td class="py-3 px-4 text-slate-700 dark:text-slate-300">{{ a.type }}</td>
            <td class="py-3 px-4">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="a.status === 'blocked' ? 'bg-rose-100 text-rose-700 dark:bg-rose-500/10 dark:text-rose-400' : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'">{{ a.status }}</span>
            </td>
            <td class="py-3 px-4 text-slate-500 dark:text-slate-400">{{ a.enable2fa ? 'On' : 'Off' }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center justify-end gap-1">
                <button class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" title="Edit" @click="openEdit(a)"><Pencil class="w-4 h-4" /></button>
                <button class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" title="Send Email" @click="openMail(a)"><Send class="w-4 h-4" /></button>
                <button class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5" :class="a.status === 'blocked' ? 'text-emerald-500' : 'text-amber-500'" :title="a.status === 'blocked' ? 'Unblock' : 'Block'" @click="toggleBlock(a)">
                  <CheckCircle2 v-if="a.status === 'blocked'" class="w-4 h-4" />
                  <Ban v-else class="w-4 h-4" />
                </button>
                <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" title="Delete" @click="remove(a)"><Trash2 class="w-4 h-4" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add modal -->
    <div v-if="showAdd" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showAdd = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Add Manager</h2>
        <form class="space-y-3" @submit.prevent="createAdmin">
          <div class="grid grid-cols-2 gap-3">
            <input v-model="addForm.firstName" required placeholder="First name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="addForm.lastName" required placeholder="Last name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <input v-model="addForm.email" type="email" required placeholder="Email" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="addForm.phone" placeholder="Phone" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <select v-model="addForm.type" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
            <option v-for="t in TYPES" :key="t" :value="t">{{ t }}</option>
          </select>
          <input v-model="addForm.password" type="password" required minlength="8" placeholder="Password (min 8 chars)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showAdd = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Create</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit modal -->
    <div v-if="showEdit" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showEdit = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Edit Admin</h2>
        <form class="space-y-3" @submit.prevent="saveEdit">
          <div class="grid grid-cols-2 gap-3">
            <input v-model="editForm.firstName" required placeholder="First name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="editForm.lastName" required placeholder="Last name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <input v-model="editForm.email" type="email" required placeholder="Email" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="editForm.phone" placeholder="Phone" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <select v-model="editForm.type" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
            <option v-for="t in TYPES" :key="t" :value="t">{{ t }}</option>
          </select>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showEdit = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Mail modal -->
    <div v-if="showMail" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" @click.self="showMail = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-md p-6">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">Send Email to {{ mailTarget?.firstName }}</h2>
        <form class="space-y-3" @submit.prevent="sendMail">
          <input v-model="mailForm.subject" required placeholder="Subject" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <textarea v-model="mailForm.message" required rows="5" placeholder="Message" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showMail = false">Cancel</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Send</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
