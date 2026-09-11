<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { useRouter } from 'vue-router'
import { Target, TrendingUp, Gift, Clock, Sparkles } from 'lucide-vue-next'
import { useAuthUserStore } from '@/stores/authUser'
import api from '@/lib/api'

const router = useRouter()
const authUser = useAuthUserStore()
const loading = ref(true)
const plans = ref([])
const purchasingId = ref(null)

onMounted(async () => {
  try {
    const { data } = await api.get('/plans', { params: { type: 'Main' } })
    plans.value = data
  } finally {
    loading.value = false
  }
})

async function purchase(plan) {
  const { value: amountStr, isConfirmed } = await Swal.fire({
    icon: 'question',
    title: `Invest in ${plan.name}`,
    html: `Enter an amount between <strong>${authUser.user?.currencySymbol}${Number(plan.minPrice).toLocaleString()}</strong> and <strong>${authUser.user?.currencySymbol}${Number(plan.maxPrice).toLocaleString()}</strong>`,
    input: 'number',
    inputValue: plan.price,
    inputAttributes: { min: plan.minPrice, max: plan.maxPrice, step: '0.01' },
    showCancelButton: true,
    confirmButtonText: 'Invest Now',
    background: '#1F2937', color: '#E5E7EB',
  })
  if (!isConfirmed || !amountStr) return

  purchasingId.value = plan.id
  try {
    await api.post('/investments', {
      planId: plan.id,
      amount: Number(amountStr),
      duration: `${plan.expirationDays} days`,
    })
    await authUser.fetchProfile()
    await Swal.fire({
      icon: 'success', title: 'Plan purchased!', text: `You have successfully purchased ${plan.name}.`,
      background: '#1F2937', color: '#E5E7EB',
    })
    router.push({ name: 'user.my-plans' })
  } catch (e) {
    await Swal.fire({ icon: 'error', title: 'Purchase failed', text: e.response?.data?.message || 'Please try again.', background: '#1F2937', color: '#E5E7EB' })
  } finally {
    purchasingId.value = null
  }
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
    <div>
      <h1 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Target class="w-5 h-5 sm:w-6 sm:h-6 text-blue-500" /> Investment Plans
      </h1>
      <p class="text-gray-500 dark:text-gray-400 mt-1">Choose a plan and start growing your portfolio.</p>
    </div>

    <div v-if="loading" class="text-gray-500 dark:text-gray-400">Loading…</div>
    <div v-else-if="plans.length === 0" class="text-gray-500 dark:text-gray-400">No plans currently available.</div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="plan in plans" :key="plan.id" class="relative bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-6 flex flex-col">
        <span v-if="plan.tag" class="absolute top-4 right-4 px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">{{ plan.tag }}</span>
        <h2 class="text-lg font-bold text-gray-900 dark:text-white">{{ plan.name }}</h2>
        <div class="text-3xl font-extrabold text-gray-900 dark:text-white mt-2">
          {{ authUser.user?.currencySymbol }}{{ Number(plan.price).toLocaleString() }}
        </div>
        <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
          Range: {{ authUser.user?.currencySymbol }}{{ Number(plan.minPrice).toLocaleString() }} – {{ authUser.user?.currencySymbol }}{{ Number(plan.maxPrice).toLocaleString() }}
        </div>

        <ul class="mt-4 space-y-2 text-sm text-gray-600 dark:text-gray-300 flex-1">
          <li class="flex items-center gap-2">
            <TrendingUp class="w-4 h-4 text-emerald-500" />
            {{ plan.minReturnPct }}% – {{ plan.maxReturnPct }}% return
          </li>
          <li v-if="plan.incrementAmount" class="flex items-center gap-2">
            <Clock class="w-4 h-4 text-blue-500" />
            {{ plan.incrementAmount }}{{ plan.incrementType === 'Percentage' ? '%' : '' }} growth, {{ plan.incrementInterval }}
          </li>
          <li v-if="plan.gift && Number(plan.gift) > 0" class="flex items-center gap-2">
            <Gift class="w-4 h-4 text-purple-500" />
            {{ authUser.user?.currencySymbol }}{{ Number(plan.gift).toLocaleString() }} signup gift
          </li>
          <li class="flex items-center gap-2">
            <Sparkles class="w-4 h-4 text-amber-500" />
            {{ plan.expirationDays }} day duration
          </li>
        </ul>

        <button type="button" :disabled="purchasingId === plan.id"
          class="mt-6 w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg disabled:opacity-50"
          @click="purchase(plan)">
          {{ purchasingId === plan.id ? 'Processing…' : 'Invest Now' }}
        </button>
      </div>
    </div>
  </div>
</template>
