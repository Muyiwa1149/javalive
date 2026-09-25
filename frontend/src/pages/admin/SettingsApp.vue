<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import {
  Settings as SettingsIcon, RefreshCw, Globe, SlidersHorizontal, Mail, ShieldCheck,
  Eye, EyeOff, ImageUp,
} from 'lucide-vue-next'
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
const logoPreview = ref(null)
const faviconPreview = ref(null)

const tabs = [
  { id: 'website', label: 'Website', icon: Globe },
  { id: 'preferences', label: 'Preferences', icon: SlidersHorizontal },
  { id: 'email', label: 'Email / SMTP', icon: Mail },
  { id: 'social', label: 'Social Login / Captcha', icon: ShieldCheck },
]
const activeTab = ref('website')

const revealed = ref({})
function toggleReveal(key) {
  revealed.value[key] = !revealed.value[key]
}

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

function onLogoChange(e) {
  logoFile.value = e.target.files[0]
  logoPreview.value = logoFile.value ? URL.createObjectURL(logoFile.value) : null
}
function onFaviconChange(e) {
  faviconFile.value = e.target.files[0]
  faviconPreview.value = faviconFile.value ? URL.createObjectURL(faviconFile.value) : null
}

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
    logoPreview.value = null
    faviconPreview.value = null
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

const inputCls = 'w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/40 focus:border-indigo-500'
const labelCls = 'text-xs font-medium text-slate-500 dark:text-slate-400 mb-1 block'

const toggles = [
  { key: 'googleTranslateEnabled', label: 'Google Translate', hint: 'Show the language switcher widget' },
  { key: 'weekendTradeEnabled', label: 'Weekend trading', hint: 'Allow ROI processing on Sat/Sun' },
  { key: 'enableEmailVerification', label: 'Email verification', hint: 'Require verified email to log in' },
  { key: 'enableKyc', label: 'KYC required', hint: 'Require KYC before withdrawals' },
  { key: 'enableKycRegistration', label: 'KYC at registration', hint: 'Collect KYC during sign-up' },
  { key: 'socialLoginEnabled', label: 'Social login', hint: 'Allow Google/Facebook sign-in' },
  { key: 'returnCapital', label: 'Return capital on plan end', hint: 'Credit principal back at maturity' },
  { key: 'shouldCancelPlan', label: 'Auto-cancel plans', hint: 'Cancel instead of renewing at maturity' },
]
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-5 sm:space-y-6 max-w-6xl">
    <div class="flex items-center justify-between gap-3">
      <div>
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><SettingsIcon class="w-5 h-5 sm:w-6 sm:h-6 text-indigo-500" /> App Settings</h1>
        <p class="text-sm text-slate-500 dark:text-slate-400 hidden sm:block">Site branding, platform behavior, mail delivery, and login providers</p>
      </div>
      <button :disabled="clearingCache" class="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10 disabled:opacity-50 shrink-0" @click="clearCache">
        <RefreshCw class="w-3.5 h-3.5" :class="clearingCache ? 'animate-spin' : ''" /> Clear Cache
      </button>
    </div>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <form v-else class="lg:grid lg:grid-cols-[220px_1fr] lg:gap-6 lg:items-start" @submit.prevent="save">
      <!-- Mobile: dropdown tab switcher -->
      <select v-model="activeTab" class="lg:hidden mb-4 w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0F1524] text-sm font-medium text-slate-900 dark:text-white">
        <option v-for="t in tabs" :key="t.id" :value="t.id">{{ t.label }}</option>
      </select>

      <!-- Desktop: vertical tab nav, sticky -->
      <nav class="hidden lg:block sticky top-6 space-y-1">
        <button
          v-for="t in tabs" :key="t.id" type="button"
          class="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium text-left transition-colors"
          :class="activeTab === t.id
            ? 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400'
            : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5'"
          @click="activeTab = t.id"
        >
          <component :is="t.icon" class="w-4 h-4 shrink-0" /> {{ t.label }}
        </button>
      </nav>

      <div class="space-y-5">
        <!-- Website -->
        <div v-show="activeTab === 'website'" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-4">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><Globe class="w-4 h-4 text-indigo-500" /> Website Information</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label :class="labelCls">Site name</label><input v-model="form.siteName" :class="inputCls" /></div>
            <div><label :class="labelCls">Site title</label><input v-model="form.siteTitle" :class="inputCls" /></div>
            <div class="md:col-span-2"><label :class="labelCls">Site address (URL)</label><input v-model="form.siteAddress" :class="inputCls" /></div>
            <div><label :class="labelCls">Timezone</label><input v-model="form.timezone" :class="inputCls" /></div>
            <div><label :class="labelCls">Merchant key</label><input v-model="form.merchantKey" :class="inputCls" /></div>
            <div class="md:col-span-2"><label :class="labelCls">Description</label><textarea v-model="form.description" rows="2" :class="inputCls"></textarea></div>
            <div class="md:col-span-2"><label :class="labelCls">Keywords</label><input v-model="form.keywords" :class="inputCls" /></div>
            <div class="md:col-span-2"><label :class="labelCls">Welcome message</label><textarea v-model="form.welcomeMessage" rows="2" :class="inputCls"></textarea></div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-100 dark:border-white/5">
            <div class="pt-4">
              <label :class="labelCls">Logo</label>
              <div class="flex items-center gap-3">
                <div class="w-16 h-16 rounded-xl border border-dashed border-slate-300 dark:border-white/15 flex items-center justify-center overflow-hidden bg-slate-50 dark:bg-white/5 shrink-0">
                  <img v-if="logoPreview || currentLogo" :src="logoPreview || storageUrl(currentLogo)" class="w-full h-full object-contain p-1" />
                  <ImageUp v-else class="w-5 h-5 text-slate-400" />
                </div>
                <label class="cursor-pointer px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10">
                  Choose file
                  <input type="file" accept="image/*" class="hidden" @change="onLogoChange" />
                </label>
              </div>
            </div>
            <div class="pt-4">
              <label :class="labelCls">Favicon</label>
              <div class="flex items-center gap-3">
                <div class="w-16 h-16 rounded-xl border border-dashed border-slate-300 dark:border-white/15 flex items-center justify-center overflow-hidden bg-slate-50 dark:bg-white/5 shrink-0">
                  <img v-if="faviconPreview || currentFavicon" :src="faviconPreview || storageUrl(currentFavicon)" class="w-full h-full object-contain p-1" />
                  <ImageUp v-else class="w-5 h-5 text-slate-400" />
                </div>
                <label class="cursor-pointer px-3 py-2 rounded-xl bg-slate-100 dark:bg-white/5 text-slate-600 dark:text-slate-300 text-xs font-medium hover:bg-slate-200 dark:hover:bg-white/10">
                  Choose file
                  <input type="file" accept="image/*" class="hidden" @change="onFaviconChange" />
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Preferences -->
        <div v-show="activeTab === 'preferences'" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-4">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><SlidersHorizontal class="w-4 h-4 text-indigo-500" /> Platform Preferences</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div><label :class="labelCls">Contact email</label><input v-model="form.contactEmail" :class="inputCls" /></div>
            <div><label :class="labelCls">Currency symbol</label><input v-model="form.defaultCurrencySymbol" :class="inputCls" /></div>
            <div><label :class="labelCls">Trade mode</label><input v-model="form.tradeMode" :class="inputCls" /></div>
            <div><label :class="labelCls">Trading win rate %</label><input v-model.number="form.tradingWinrate" type="number" :class="inputCls" /></div>
            <div class="lg:col-span-2"><label :class="labelCls">Captcha provider</label><input v-model="form.captchaProvider" :class="inputCls" /></div>
          </div>

          <div class="pt-3 border-t border-slate-100 dark:border-white/5">
            <label :class="labelCls">Feature toggles</label>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              <label v-for="t in toggles" :key="t.key" class="flex items-start gap-2.5 p-3 rounded-xl border border-slate-200 dark:border-white/10 hover:bg-slate-50 dark:hover:bg-white/5 cursor-pointer">
                <input v-model="form[t.key]" type="checkbox" class="mt-0.5 rounded" />
                <span>
                  <span class="block text-sm font-medium text-slate-800 dark:text-slate-200">{{ t.label }}</span>
                  <span class="block text-xs text-slate-500 dark:text-slate-400">{{ t.hint }}</span>
                </span>
              </label>
            </div>
          </div>
        </div>

        <!-- Email / SMTP -->
        <div v-show="activeTab === 'email'" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-4">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><Mail class="w-4 h-4 text-indigo-500" /> Email / SMTP</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div><label :class="labelCls">Mail server</label><input v-model="form.mailServer" :class="inputCls" /></div>
            <div><label :class="labelCls">From address</label><input v-model="form.mailFromAddress" :class="inputCls" /></div>
            <div><label :class="labelCls">From name</label><input v-model="form.mailFromName" :class="inputCls" /></div>
            <div><label :class="labelCls">SMTP host</label><input v-model="form.smtpHost" :class="inputCls" /></div>
            <div><label :class="labelCls">SMTP port</label><input v-model="form.smtpPort" :class="inputCls" /></div>
            <div><label :class="labelCls">SMTP encryption</label><input v-model="form.smtpEncryption" :class="inputCls" /></div>
            <div><label :class="labelCls">SMTP username</label><input v-model="form.smtpUsername" :class="inputCls" /></div>
            <div>
              <label :class="labelCls">SMTP password</label>
              <div class="relative">
                <input v-model="form.smtpPassword" :type="revealed.smtp ? 'text' : 'password'" placeholder="Leave blank to keep current" :class="inputCls" class="pr-10" />
                <button type="button" class="absolute inset-y-0 right-0 px-3 flex items-center text-slate-400 hover:text-slate-600 dark:hover:text-slate-200" @click="toggleReveal('smtp')">
                  <component :is="revealed.smtp ? EyeOff : Eye" class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Social Login / Captcha -->
        <div v-show="activeTab === 'social'" class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-5 sm:p-6 space-y-4">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white flex items-center gap-2"><ShieldCheck class="w-4 h-4 text-indigo-500" /> Social Login / Captcha</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div><label :class="labelCls">Google client ID</label><input v-model="form.googleClientId" :class="inputCls" /></div>
            <div>
              <label :class="labelCls">Google client secret</label>
              <div class="relative">
                <input v-model="form.googleClientSecret" :type="revealed.google ? 'text' : 'password'" :class="inputCls" class="pr-10" />
                <button type="button" class="absolute inset-y-0 right-0 px-3 flex items-center text-slate-400 hover:text-slate-600 dark:hover:text-slate-200" @click="toggleReveal('google')">
                  <component :is="revealed.google ? EyeOff : Eye" class="w-4 h-4" />
                </button>
              </div>
            </div>
            <div><label :class="labelCls">Google redirect URI</label><input v-model="form.googleRedirectUri" :class="inputCls" /></div>
            <div>
              <label :class="labelCls">Captcha secret</label>
              <div class="relative">
                <input v-model="form.captchaSecret" :type="revealed.captcha ? 'text' : 'password'" :class="inputCls" class="pr-10" />
                <button type="button" class="absolute inset-y-0 right-0 px-3 flex items-center text-slate-400 hover:text-slate-600 dark:hover:text-slate-200" @click="toggleReveal('captcha')">
                  <component :is="revealed.captcha ? EyeOff : Eye" class="w-4 h-4" />
                </button>
              </div>
            </div>
            <div><label :class="labelCls">Captcha site key</label><input v-model="form.captchaSiteKey" :class="inputCls" /></div>
          </div>
        </div>

        <!-- Sticky save bar -->
        <div class="sticky bottom-0 -mx-4 sm:-mx-6 lg:mx-0 px-4 sm:px-6 lg:px-0 py-3 bg-gradient-to-t from-slate-50 dark:from-[#0B0F1A] via-slate-50/95 dark:via-[#0B0F1A]/95 to-transparent lg:bg-none flex justify-end">
          <button type="submit" :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50 shadow-lg shadow-indigo-500/20">{{ saving ? 'Saving…' : 'Save Settings' }}</button>
        </div>
      </div>
    </form>
  </div>
</template>
