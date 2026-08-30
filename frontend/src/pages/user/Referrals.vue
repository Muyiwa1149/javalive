<script setup>
import { ref, computed, onMounted } from 'vue'
import { Users2, Copy, Check, Gift, Users } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const authUser = useAuthUserStore()
const loading = ref(true)
const overview = ref(null)
const copied = ref(false)

onMounted(async () => {
  try {
    const { data } = await api.get('/referrals')
    overview.value = data
  } finally {
    loading.value = false
  }
})

const fullLink = computed(() => overview.value?.referralLink ? `${window.location.origin}${overview.value.referralLink}` : '')

async function copyLink() {
  try {
    await navigator.clipboard.writeText(fullLink.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // clipboard API unavailable — silently ignore, the link is still visible to copy manually
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
      <Users2 class="w-6 h-6 text-blue-500" /> Referral Program
    </h1>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>

    <template v-else-if="overview">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Direct Referrals</span>
            <Users class="w-5 h-5 text-blue-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ overview.directReferralCount }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Referral Earnings</span>
            <Gift class="w-5 h-5 text-purple-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ authUser.user?.currencySymbol }}{{ Number(overview.referralEarnings).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">Commission Rate</span>
            <Gift class="w-5 h-5 text-emerald-500" />
          </div>
          <div class="text-2xl font-bold text-gray-900 dark:text-white mt-2">{{ overview.commissionPct }}%</div>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-6">
        <h2 class="font-semibold text-gray-900 dark:text-white mb-3">Your Referral Link</h2>
        <div class="flex flex-col sm:flex-row gap-2">
          <input :value="fullLink" readonly class="flex-1 rounded-lg border border-gray-300 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 px-3 py-2 text-sm text-gray-900 dark:text-white font-mono">
          <button type="button" class="flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg" @click="copyLink">
            <component :is="copied ? Check : Copy" class="w-4 h-4" /> {{ copied ? 'Copied!' : 'Copy' }}
          </button>
        </div>
      </div>

      <div>
        <h2 class="font-semibold text-gray-900 dark:text-white mb-3">Your Downline</h2>
        <div v-if="overview.downline.length === 0" class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 text-center text-gray-500 dark:text-gray-400">
          No referrals yet. Share your link above to start earning commissions.
        </div>
        <div v-else class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
              <tr><th class="px-4 py-3">Name</th><th class="px-4 py-3">Level</th><th class="px-4 py-3">Referred By</th><th class="px-4 py-3">Status</th><th class="px-4 py-3">Joined</th></tr>
            </thead>
            <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
              <tr v-for="m in overview.downline" :key="m.id">
                <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">{{ m.name }}</td>
                <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ m.levelLabel }}</td>
                <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ m.parentName }}</td>
                <td class="px-4 py-3">
                  <span class="px-2 py-1 rounded-full text-xs font-medium capitalize" :class="m.status === 'active' ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300' : 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'">{{ m.status }}</span>
                </td>
                <td class="px-4 py-3 text-gray-500 dark:text-gray-400">{{ new Date(m.registeredAt).toLocaleDateString() }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
