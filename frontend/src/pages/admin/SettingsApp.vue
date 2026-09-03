<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { Settings as SettingsIcon, RefreshCw } from 'lucide-vue-next'
import api from '@/lib/api'
import { storageUrl } from '@/lib/storage'

const loading = ref(true)
const saving = ref(false)
const clearingCache = ref(false)
const form = ref({})
const logoFile = ref(null)
const faviconFile = ref(null)
const currentLogo = ref(null)
const currentFavicon = ref(null)

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/settings')
    form.value = { ...data }
    currentLogo.value = data.logo
    currentFavicon.value = data.favicon
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function save() {
  saving.value = true
  try {
    const fd = new FormData()
    Object.entries(form.value).forEach(([k, v]) => {
      if (v !== null && v !== undefined) fd.append(k, v)
    })
    if (logoFile.value) fd.append('logo', logoFile.value)
    if (faviconFile.value) fd.append('favicon', faviconFile.value)
    const { data } = await api.post('/admin/settings/app', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.value = { ...data }
    currentLogo.value = data.logo
    currentFavicon.value = data.favicon
    logoFile.value = null
    faviconFile.value = null
    Swal.fire({ icon: 'success', title: 'Settings saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

async function clearCache() {
  clearingCache.value = true
  try {
    await api.post('/admin/cache/clear')
    Swal.fire({ icon: 'success', title: 'Cache cleared', timer: 1200, showConfirmButton: false })
  } finally {
    clearingCache.value = false
  }
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-3xl">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><SettingsIcon class="w-6 h-6 text-indigo-500" /> App Settings</h1>
      <button :disabled="clearingCache" class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10 disabled:opacity-50" @click="clearCache">
        <RefreshCw class="w-3.5 h-3.5" :class="clearingCache ? 'animate-spin' : ''" /> Clear Cache
      </button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <form v-else class="space-y-6" @submit.prevent="save">
      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Website Information</h2>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Site name</label><input v-model="form.siteName" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Site title</label><input v-model="form.siteTitle" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
        <div><label class="text-xs text-slate-500">Site address (URL)</label><input v-model="form.siteAddress" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Description</label><textarea v-model="form.description" rows="2" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea></div>
        <div><label class="text-xs text-slate-500">Keywords</label><input v-model="form.keywords" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div><label class="text-xs text-slate-500">Welcome message</label><textarea v-model="form.welcomeMessage" rows="2" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white"></textarea></div>
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="text-xs text-slate-500 block mb-1">Logo</label>
            <img v-if="currentLogo" :src="storageUrl(currentLogo)" class="h-8 mb-2" />
            <input type="file" accept="image/*" class="w-full text-sm text-slate-500" @change="(e) => logoFile = e.target.files[0]" />
          </div>
          <div>
            <label class="text-xs text-slate-500 block mb-1">Favicon</label>
            <img v-if="currentFavicon" :src="storageUrl(currentFavicon)" class="h-8 mb-2" />
            <input type="file" accept="image/*" class="w-full text-sm text-slate-500" @change="(e) => faviconFile = e.target.files[0]" />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Timezone</label><input v-model="form.timezone" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Merchant key</label><input v-model="form.merchantKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Preferences</h2>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Contact email</label><input v-model="form.contactEmail" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Currency symbol</label><input v-model="form.defaultCurrencySymbol" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Trade mode</label><input v-model="form.tradeMode" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Trading win rate %</label><input v-model.number="form.tradingWinrate" type="number" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
        <div><label class="text-xs text-slate-500">Captcha provider</label><input v-model="form.captchaProvider" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        <div class="grid grid-cols-2 gap-2 pt-2">
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.googleTranslateEnabled" type="checkbox" class="rounded" /> Google Translate</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.weekendTradeEnabled" type="checkbox" class="rounded" /> Weekend trading</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.enableEmailVerification" type="checkbox" class="rounded" /> Email verification</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.enableKyc" type="checkbox" class="rounded" /> KYC required</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.enableKycRegistration" type="checkbox" class="rounded" /> KYC at registration</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.socialLoginEnabled" type="checkbox" class="rounded" /> Social login</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.returnCapital" type="checkbox" class="rounded" /> Return capital on plan end</label>
          <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300"><input v-model="form.shouldCancelPlan" type="checkbox" class="rounded" /> Auto-cancel plans</label>
        </div>
      </div>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
        <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Email / SMTP</h2>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Mail server</label><input v-model="form.mailServer" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">From address</label><input v-model="form.mailFromAddress" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">From name</label><input v-model="form.mailFromName" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">SMTP host</label><input v-model="form.smtpHost" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">SMTP port</label><input v-model="form.smtpPort" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">SMTP encryption</label><input v-model="form.smtpEncryption" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">SMTP username</label><input v-model="form.smtpUsername" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">SMTP password</label><input v-model="form.smtpPassword" type="password" placeholder="Leave blank to keep current" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
        <h3 class="text-xs font-semibold text-slate-500 uppercase pt-2">Social Login / Captcha</h3>
        <div class="grid grid-cols-2 gap-3">
          <div><label class="text-xs text-slate-500">Google client ID</label><input v-model="form.googleClientId" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Google client secret</label><input v-model="form.googleClientSecret" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Google redirect URI</label><input v-model="form.googleRedirectUri" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div></div>
          <div><label class="text-xs text-slate-500">Captcha secret</label><input v-model="form.captchaSecret" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          <div><label class="text-xs text-slate-500">Captcha site key</label><input v-model="form.captchaSiteKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
        </div>
      </div>

      <div class="flex justify-end">
        <button type="submit" :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save Settings' }}</button>
      </div>
    </form>
  </div>
</template>
