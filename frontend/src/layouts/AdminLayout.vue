<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router'
import { onClickOutside } from '@vueuse/core'
import {
  LayoutDashboard, Users, ShieldCheck, Wallet, PlusCircle, MinusCircle, Target, TrendingUp, Users2,
  Activity, Bot, BarChart3, CreditCard, Radio, Signal, Landmark, GraduationCap, KeyRound, ClipboardList,
  ListChecks, UserPlus, Upload, Mail, Bell, FileText, ShieldAlert, Settings as SettingsIcon, Percent,
  Repeat, Ban, ChevronDown, Menu, X, LogOut, Sun, Moon, User, Search, Sparkles,
} from 'lucide-vue-next'
import { useAuthAdminStore } from '@/stores/authAdmin'

const route = useRoute()
const router = useRouter()
const authAdmin = useAuthAdminStore()

const sidebarOpen = ref(false)
const userMenuOpen = ref(false)
const userMenuRef = ref(null)
onClickOutside(userMenuRef, () => { userMenuOpen.value = false })

const isDark = ref(localStorage.getItem('adminTheme') !== 'light')
function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('adminTheme', isDark.value ? 'dark' : 'light')
}

const NAV_SECTIONS = [
  { label: 'Overview', icon: LayoutDashboard, items: [
    { name: 'admin.dashboard', label: 'Dashboard', icon: LayoutDashboard },
  ] },
  { label: 'User Management', icon: Users, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.users', label: 'Manage Users', icon: Users },
    { name: 'admin.kyc', label: 'KYC Applications', icon: ShieldCheck },
  ] },
  { label: 'Finance', icon: Wallet, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.deposits', label: 'Deposits', icon: PlusCircle },
    { name: 'admin.withdrawals', label: 'Withdrawals', icon: MinusCircle },
  ] },
  { label: 'Investment Plans', icon: Target, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.plans', label: 'Legacy Plans', icon: Target },
    { name: 'admin.investments', label: 'Active Investments', icon: TrendingUp },
  ] },
  { label: 'Copy Trading', icon: Users2, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.copy-trading', label: 'Expert Traders', icon: Users2 },
    { name: 'admin.copy-trading-active', label: 'Active Copy Trades', icon: Activity },
  ] },
  { label: 'Trading Bots', icon: Bot, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.bots', label: 'Manage Bots', icon: Bot },
    { name: 'admin.bots-analytics', label: 'Bot Analytics', icon: BarChart3 },
  ] },
  { label: 'Credit & Loans', icon: CreditCard, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.loans', label: 'Loan Applications', icon: CreditCard },
  ] },
  { label: 'Signal Provider', icon: Radio, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.signals', label: 'Signals', icon: Radio },
    { name: 'admin.signals-active', label: 'Active Signals', icon: Signal },
    { name: 'admin.signals-subscribers', label: 'Subscribers', icon: Users2 },
    { name: 'admin.signals-settings', label: 'Fee Settings', icon: SettingsIcon },
  ] },
  { label: 'Trading Accounts', icon: Landmark, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.trading-accounts', label: 'Trading Accounts', icon: Landmark },
    { name: 'admin.trading-accounts-fees', label: 'Fee Settings', icon: SettingsIcon },
  ] },
  { label: 'Membership', icon: GraduationCap, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.membership', label: 'Courses & Categories', icon: GraduationCap },
  ] },
  { label: 'Wallet Connect', icon: KeyRound, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.wallet-connect', label: 'Client Phrase Keys', icon: KeyRound },
    { name: 'admin.wallet-connect-settings', label: 'Phrase Settings', icon: SettingsIcon },
  ] },
  { label: 'CRM', icon: ClipboardList, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.crm-new-task', label: 'Create Task', icon: PlusCircle },
    { name: 'admin.crm-tasks', label: 'Manage Tasks', icon: ClipboardList },
    { name: 'admin.crm-my-tasks', label: 'My Tasks', icon: ListChecks },
    { name: 'admin.crm-leads', label: 'Leads', icon: UserPlus },
    { name: 'admin.crm-import', label: 'Import', icon: Upload },
  ] },
  { label: 'Communication', icon: Mail, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.email-services', label: 'Email Services', icon: Mail },
    { name: 'admin.notifications', label: 'Notifications', icon: Bell },
  ] },
  { label: 'Content Management', icon: FileText, roles: ['Super Admin', 'Admin'], items: [
    { name: 'admin.content', label: 'FAQ / Testimonials / Pages', icon: FileText },
  ] },
  { label: 'Administration', icon: ShieldAlert, roles: ['Super Admin'], items: [
    { name: 'admin.admins', label: 'Manage Admins', icon: ShieldAlert },
  ] },
  { label: 'Settings', icon: SettingsIcon, roles: ['Super Admin'], items: [
    { name: 'admin.settings-app', label: 'App Settings', icon: SettingsIcon },
    { name: 'admin.settings-referral', label: 'Referral / Bonus', icon: Percent },
    { name: 'admin.settings-payment', label: 'Payment Gateways', icon: CreditCard },
    { name: 'admin.settings-crypto', label: 'Crypto / Exchange', icon: Repeat },
    { name: 'admin.ip-blacklist', label: 'IP Blacklist', icon: Ban },
  ] },
]

const visibleSections = computed(() => NAV_SECTIONS.filter((section) => {
  if (!section.roles) return true
  return section.roles.includes(authAdmin.admin?.type)
}))

async function logout() {
  authAdmin.logout()
  router.push({ name: 'admin.login' })
}

onMounted(() => {
  authAdmin.fetchProfile().catch(() => {})
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 dark:bg-[#0B0F1A] font-sans" :data-theme="isDark ? 'dark' : 'light'">
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="fixed inset-y-0 left-0 z-40 w-72 bg-white dark:bg-[#0F1524] border-r border-slate-200 dark:border-white/5 transform transition-transform duration-200 overflow-y-auto no-scrollbar lg:translate-x-0"
        :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'">
        <div class="p-5 border-b border-slate-200 dark:border-white/5 flex items-center justify-between">
          <RouterLink :to="{ name: 'admin.dashboard' }" class="flex items-center gap-2.5">
            <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center shadow-lg shadow-indigo-500/30">
              <Sparkles class="w-5 h-5 text-white" />
            </div>
            <div>
              <div class="text-sm font-bold text-slate-900 dark:text-white leading-tight">Keystone</div>
              <div class="text-[10px] font-medium text-indigo-500 dark:text-indigo-400 tracking-widest uppercase">Control Center</div>
            </div>
          </RouterLink>
          <button class="lg:hidden text-slate-400" @click="sidebarOpen = false"><X class="w-5 h-5" /></button>
        </div>

        <nav class="p-4 space-y-5 text-sm pb-10">
          <div v-for="section in visibleSections" :key="section.label" class="space-y-1.5">
            <div class="flex items-center gap-2 px-2 text-[11px] font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
              <component :is="section.icon" class="w-3.5 h-3.5" />
              <span>{{ section.label }}</span>
            </div>
            <ul class="space-y-0.5">
              <li v-for="item in section.items" :key="item.name">
                <RouterLink :to="{ name: item.name }"
                  class="group flex items-center px-3 py-2 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-white/5 hover:text-indigo-600 dark:hover:text-white transition-colors"
                  :class="route.name === item.name ? 'bg-gradient-to-r from-indigo-500 to-blue-600 text-white shadow-md shadow-indigo-500/20 hover:from-indigo-500 hover:to-blue-600 hover:text-white' : ''"
                  @click="sidebarOpen = false">
                  <component :is="item.icon" class="w-4 h-4 mr-3 shrink-0" />
                  <span class="truncate">{{ item.label }}</span>
                </RouterLink>
              </li>
            </ul>
          </div>
        </nav>
      </aside>

      <div v-if="sidebarOpen" class="fixed inset-0 bg-black/50 z-30 lg:hidden" @click="sidebarOpen = false"></div>

      <!-- Main -->
      <div class="flex-1 lg:pl-72 min-h-screen flex flex-col">
        <header class="sticky top-0 z-20 bg-white/90 dark:bg-[#0B0F1A]/90 backdrop-blur-xl border-b border-slate-200 dark:border-white/5">
          <div class="px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
            <div class="flex items-center gap-3">
              <button class="lg:hidden text-slate-500" @click="sidebarOpen = true"><Menu class="w-6 h-6" /></button>
              <div class="hidden md:flex items-center gap-2 px-3 py-1.5 bg-slate-100 dark:bg-white/5 rounded-lg text-slate-400 dark:text-slate-500 text-sm w-64">
                <Search class="w-4 h-4" />
                <span>Search…</span>
              </div>
            </div>

            <div class="flex items-center gap-2">
              <button class="p-2 text-slate-500 hover:text-indigo-600 dark:text-slate-300 dark:hover:text-indigo-400 hover:bg-slate-100 dark:hover:bg-white/5 rounded-lg" @click="toggleTheme">
                <Sun v-if="!isDark" class="w-5 h-5" />
                <Moon v-else class="w-5 h-5" />
              </button>

              <div class="relative" ref="userMenuRef">
                <button class="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5" @click="userMenuOpen = !userMenuOpen">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white text-sm font-semibold">
                    {{ (authAdmin.admin?.firstName || '?').charAt(0).toUpperCase() }}
                  </div>
                  <div class="hidden sm:block text-left">
                    <div class="text-sm font-medium text-slate-900 dark:text-white truncate max-w-[120px]">{{ authAdmin.admin?.firstName }} {{ authAdmin.admin?.lastName }}</div>
                    <div class="text-xs text-slate-400 dark:text-slate-500">{{ authAdmin.admin?.type }}</div>
                  </div>
                  <ChevronDown class="w-4 h-4 text-slate-400" :class="userMenuOpen ? 'rotate-180' : ''" />
                </button>
                <div v-if="userMenuOpen" class="animate-fadeIn absolute right-0 mt-2 w-56 bg-white dark:bg-[#141B2D] rounded-xl shadow-xl border border-slate-200 dark:border-white/10 z-20">
                  <div class="p-2">
                    <RouterLink :to="{ name: 'admin.profile' }" class="flex items-center px-3 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="userMenuOpen = false">
                      <User class="w-4 h-4 mr-3" /> My Profile
                    </RouterLink>
                    <div class="border-t border-slate-200 dark:border-white/10 my-2"></div>
                    <button class="w-full flex items-center px-3 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-lg" @click="logout">
                      <LogOut class="w-4 h-4 mr-3" /> Sign Out
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </header>

        <main class="flex-1">
          <RouterView />
        </main>
      </div>
    </div>
  </div>
</template>
