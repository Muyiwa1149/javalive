<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { LayoutDashboard, Banknote, LogOut, Menu, X } from 'lucide-vue-next'
import { useAuthAdminStore } from '@/stores/authAdmin'

/**
 * Minimal functional admin shell — enough to host real admin features as they're built (Deposits
 * first). The full Atlantis-theme rebuild (stat cards, complete nav) is Phase 5 proper; this isn't
 * meant to be replaced later so much as grown as more admin sections land.
 */
const route = useRoute()
const router = useRouter()
const authAdmin = useAuthAdminStore()
const sidebarOpen = ref(false)

const NAV = [
  { name: 'admin.dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { name: 'admin.deposits', label: 'Deposits', icon: Banknote },
]

onMounted(() => {
  authAdmin.fetchProfile().catch(() => {})
})

async function logout() {
  authAdmin.logout()
  router.push({ name: 'admin.login' })
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-950 flex" data-theme="dark">
    <aside class="fixed inset-y-0 left-0 z-40 w-64 bg-gray-900 border-r border-gray-800 transform transition-transform lg:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'">
      <div class="p-4 border-b border-gray-800 flex items-center justify-between">
        <span class="text-white font-bold">Admin Panel</span>
        <button class="lg:hidden text-gray-400" @click="sidebarOpen = false"><X class="w-5 h-5" /></button>
      </div>
      <nav class="p-4 space-y-1">
        <RouterLink v-for="item in NAV" :key="item.name" :to="{ name: item.name }"
          class="flex items-center px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 transition-colors"
          :class="route.name === item.name ? 'bg-gray-800 text-white font-medium' : ''">
          <component :is="item.icon" class="w-5 h-5 mr-3" />
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div v-if="sidebarOpen" class="fixed inset-0 bg-black/50 z-30 lg:hidden" @click="sidebarOpen = false"></div>

    <div class="flex-1 lg:pl-64 min-h-screen flex flex-col">
      <header class="sticky top-0 z-20 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 h-16 flex items-center justify-between px-4 sm:px-6">
        <button class="lg:hidden text-gray-500" @click="sidebarOpen = true"><Menu class="w-6 h-6" /></button>
        <div class="flex-1"></div>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-700 dark:text-gray-200">{{ authAdmin.admin?.firstName }} {{ authAdmin.admin?.lastName }}</span>
          <button class="flex items-center gap-1 text-sm text-red-600 dark:text-red-400 hover:underline" @click="logout">
            <LogOut class="w-4 h-4" /> Sign Out
          </button>
        </div>
      </header>
      <main class="flex-1">
        <RouterView />
      </main>
    </div>
  </div>
</template>
