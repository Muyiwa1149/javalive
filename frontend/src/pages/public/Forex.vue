<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { usePublicSettingsStore } from '@/stores/publicSettings'

const settingsStore = usePublicSettingsStore()

const heroTextVisible = ref(false)
const heroCardVisible = ref(false)

onMounted(async () => {
  await settingsStore.ensureLoaded()
  setTimeout(() => { heroTextVisible.value = true }, 200)
  setTimeout(() => { heroCardVisible.value = true }, 500)
})

const categoryFilter = ref('All')
const searchQuery = ref('')

function setCategoryFilter(category) {
  categoryFilter.value = category
}

// Currency pairs as hardcoded in the source blade's investment-conditions table.
const pairs = [
  { symbol: 'AUD/CAD', category: 'Australasian', value: '0.9532', returns: '2.5%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/CHF', category: 'Australasian', value: '0.6721', returns: '2.3%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/JPY', category: 'Minor', value: '81.45', returns: '2.8%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/NZD', category: 'Australasian', value: '1.0732', returns: '2.1%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/SGD', category: 'Australasian', value: '0.9845', returns: '2.4%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/USD', category: 'Major', value: '0.7321', returns: '2.6%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
  { symbol: 'AUD/ZAR', category: '', value: '11.2345', returns: '3.0%', lotSize: '100000 AUD', fee: '$2.5 per $100K' },
]

const filteredPairs = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  return pairs.filter((p) => {
    const matchesCategory = categoryFilter.value === 'All' || p.category === categoryFilter.value
    const matchesSearch = !q || p.symbol.toLowerCase().includes(q) || p.category.toLowerCase().includes(q)
    return matchesCategory && matchesSearch
  })
})
</script>

<template>
  <!-- Hero Section -->
  <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
    <!-- Abstract Background Elements -->
    <div class="absolute inset-0 z-0 pointer-events-none opacity-20">
      <!-- Abstract Investment Chart Pattern -->
      <div class="absolute top-0 right-0 w-full h-full">
        <svg class="absolute top-0 right-0 w-full h-full" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="investmentGrad1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop stop-color="#3B82F6" stop-opacity=".25" offset="0%"/>
              <stop stop-color="#10B981" stop-opacity=".2" offset="50%"/>
              <stop stop-color="#6366F1" stop-opacity=".15" offset="100%"/>
            </linearGradient>
          </defs>
          <path d="M120,300 L150,280 L200,290 L250,260 L300,240 L350,250 L400,220 L450,230 L500,200 L550,180 L600,210 L650,190 L700,170 L750,180 L800,150" fill="none" stroke="url(#investmentGrad1)" stroke-width="2"/>
          <path d="M120,300 L150,280 L200,290 L250,260 L300,240 L350,250 L400,220 L450,230 L500,200 L550,180 L600,210 L650,190 L700,170 L750,180 L800,150 L800,400 L120,400 Z" fill="url(#investmentGrad1)" opacity="0.2"/>
        </svg>
      </div>
      <div class="absolute inset-0">
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" stroke="#6366F1" stroke-width="2" opacity="0.15">
            <path d="M769 229L1037 260.9M927 880L731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737M490 85L309 538 40 599 295 764"/>
          </g>
        </svg>
      </div>
    </div>

    <!-- Hero Content -->
    <div class="relative z-10 px-4 py-20 mx-auto max-w-7xl sm:px-6 lg:px-8">
      <div class="flex flex-col lg:flex-row lg:items-center">
        <div class="w-full lg:w-2/3 mb-8 lg:mb-0">
          <div
            class="space-y-6 transition-all duration-500 ease-out"
            :class="heroTextVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'"
          >
            <div class="inline-block px-3 py-1 mb-2 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
              Global Investment
            </div>
            <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
              <span class="block">Forex Investment</span>
              <span class="block mt-2 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-indigo-400 to-green-400">Foreign Exchange Opportunities</span>
            </h1>
            <p class="mt-3 text-lg text-gray-300 max-w-2xl">
              Invest in the global foreign exchange market to diversify your portfolio. Our platform offers access to major, minor, and exotic currency pairs, enabling you to build wealth through strategic currency investments.
            </p>
            <div class="flex flex-wrap gap-4 mt-6">
              <a href="#investment-markets" class="px-6 py-3 text-base font-medium text-white bg-blue-600 rounded-lg shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition-all duration-200">
                Explore Opportunities
              </a>
              <RouterLink to="/register" class="px-6 py-3 text-base font-medium text-gray-300 bg-gray-800 border border-gray-700 rounded-lg shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition-all duration-200">
                Start Investing
              </RouterLink>
            </div>
          </div>
        </div>
        <div class="w-full lg:w-1/3">
          <div
            class="relative transition-all duration-500 ease-out delay-300"
            :class="heroCardVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-12'"
          >
            <!-- Currency Pair Cards -->
            <div class="grid gap-4">
              <!-- EUR/USD -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center text-blue-400 font-bold mr-3">
                      €/$
                    </div>
                    <div>
                      <h3 class="text-lg font-bold text-white">EUR/USD</h3>
                      <p class="text-sm text-gray-400">Euro / US Dollar</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-green-400 font-medium">1.1842</div>
                    <div class="text-xs text-green-400">+0.05%</div>
                  </div>
                </div>
                <div class="mt-3 h-10">
                  <svg class="w-full h-full" viewBox="0 0 100 30" xmlns="http://www.w3.org/2000/svg">
                    <path d="M0,15 L10,13 L20,16 L30,14 L40,17 L50,12 L60,15 L70,10 L80,13 L90,11 L100,8" fill="none" stroke="#10B981" stroke-width="2"/>
                  </svg>
                </div>
              </div>

              <!-- GBP/USD -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-indigo-600 bg-opacity-20 rounded-full flex items-center justify-center text-indigo-400 font-bold mr-3">
                      £/$
                    </div>
                    <div>
                      <h3 class="text-lg font-bold text-white">GBP/USD</h3>
                      <p class="text-sm text-gray-400">British Pound / US Dollar</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-red-400 font-medium">1.3721</div>
                    <div class="text-xs text-red-400">-0.12%</div>
                  </div>
                </div>
                <div class="mt-3 h-10">
                  <svg class="w-full h-full" viewBox="0 0 100 30" xmlns="http://www.w3.org/2000/svg">
                    <path d="M0,10 L10,12 L20,8 L30,14 L40,16 L50,15 L60,18 L70,20 L80,17 L90,19 L100,22" fill="none" stroke="#EF4444" stroke-width="2"/>
                  </svg>
                </div>
              </div>
            </div>

            <div class="absolute -bottom-6 -right-6 w-32 h-32 bg-blue-500 bg-opacity-20 rounded-full filter blur-xl"></div>
            <div class="absolute -top-6 -left-6 w-24 h-24 bg-green-500 bg-opacity-20 rounded-full filter blur-xl"></div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Market Overview Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Currency Investment Options
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">Investment Opportunities</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Explore a range of currency pairs to diversify your investment portfolio with competitive returns
        </p>
      </div>

      <!-- Currency Pair Categories -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-10">
        <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-xl p-4 border border-gray-700 hover:border-blue-400 transition-all duration-300 cursor-pointer" @click="setCategoryFilter('Major')">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mr-3">
              <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
              </svg>
            </div>
            <h3 class="text-base font-bold text-white">Major</h3>
          </div>
          <p class="text-gray-400 text-xs">EUR/USD, GBP/USD, USD/JPY, USD/CHF</p>
        </div>

        <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-xl p-4 border border-gray-700 hover:border-blue-400 transition-all duration-300 cursor-pointer" @click="setCategoryFilter('Minor')">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mr-3">
              <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
            </div>
            <h3 class="text-base font-bold text-white">Minor</h3>
          </div>
          <p class="text-gray-400 text-xs">EUR/GBP, EUR/CHF, GBP/JPY, EUR/JPY</p>
        </div>

        <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-xl p-4 border border-gray-700 hover:border-blue-400 transition-all duration-300 cursor-pointer" @click="setCategoryFilter('Exotic')">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mr-3">
              <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"></path>
              </svg>
            </div>
            <h3 class="text-base font-bold text-white">Exotic</h3>
          </div>
          <p class="text-gray-400 text-xs">USD/TRY, USD/ZAR, EUR/HUF, USD/MXN</p>
        </div>

        <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-xl p-4 border border-gray-700 hover:border-blue-400 transition-all duration-300 cursor-pointer" @click="setCategoryFilter('Australasian')">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mr-3">
              <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <h3 class="text-base font-bold text-white">Australasian</h3>
          </div>
          <p class="text-gray-400 text-xs">AUD/USD, NZD/USD, AUD/NZD, AUD/JPY</p>
        </div>

        <div class="bg-gradient-to-br from-gray-800 to-gray-900 rounded-xl p-4 border border-gray-700 hover:border-blue-400 transition-all duration-300 cursor-pointer" @click="setCategoryFilter('Scandinavian')">
          <div class="flex items-center mb-2">
            <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mr-3">
              <svg class="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
            </div>
            <h3 class="text-base font-bold text-white">Scandinavian</h3>
          </div>
          <p class="text-gray-400 text-xs">EUR/NOK, EUR/SEK, USD/NOK, USD/SEK</p>
        </div>
      </div>

      <!-- Investment Markets Table -->
      <div id="investment-markets" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm rounded-xl border border-gray-700 overflow-hidden shadow-lg">
        <div class="p-6 border-b border-gray-700">
          <h3 class="text-xl font-bold text-white">Forex - Investment Conditions</h3>
        </div>

        <!-- Search and Filter Controls -->
        <div class="p-4 border-b border-gray-700 flex flex-col md:flex-row justify-between items-center gap-4">
          <div class="w-full md:w-64">
            <select v-model="categoryFilter" class="bg-gray-900 border border-gray-700 rounded-lg py-2 px-4 w-full text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option value="All">All</option>
              <option value="Australasian">Australasian</option>
              <option value="Minor">Minor</option>
              <option value="Major">Major</option>
              <option value="Exotic">Exotic</option>
              <option value="Scandinavian">Scandinavian</option>
            </select>
          </div>
          <div class="relative w-full md:w-64">
            <input v-model="searchQuery" type="text" class="w-full bg-gray-900 border border-gray-700 rounded-lg py-2 px-4 pl-10 text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="Search investment assets">
            <svg class="absolute left-3 top-2.5 h-5 w-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </div>
        </div>

        <!-- Data Table -->
        <div class="relative overflow-x-auto" style="min-height: 200px;">
          <table class="w-full text-sm text-left text-gray-300">
            <thead class="text-xs uppercase bg-gray-800 text-gray-400">
              <tr>
                <th class="px-6 py-3">Symbol</th>
                <th class="px-6 py-3">Category</th>
                <th class="px-6 py-3">Value</th>
                <th class="px-6 py-3">Annualized Returns</th>
                <th class="px-6 py-3">Investment Size</th>
                <th class="px-6 py-3">Fee</th>
              </tr>
            </thead>
            <tbody class="bg-gray-900 divide-y divide-gray-800">
              <tr v-for="pair in filteredPairs" :key="pair.symbol" class="hover:bg-gray-800 transition-colors duration-200">
                <td class="px-6 py-4">
                  <div class="flex items-center">
                    <div class="w-8 h-8 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mr-3 text-xs text-blue-400 font-medium">{{ pair.symbol.split('/').map(c => c[0]).join('/') }}</div>
                    <a href="#" class="text-blue-400 hover:text-blue-300">{{ pair.symbol }}</a>
                  </div>
                </td>
                <td class="px-6 py-4">{{ pair.category }}</td>
                <td class="px-6 py-4">{{ pair.value }}</td>
                <td class="px-6 py-4">{{ pair.returns }}</td>
                <td class="px-6 py-4">{{ pair.lotSize }}</td>
                <td class="px-6 py-4">{{ pair.fee }}</td>
              </tr>
              <tr v-if="filteredPairs.length === 0">
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">No matching records found</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Features Section -->
  <section class="py-16 bg-gradient-to-b from-gray-900 to-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Why Invest in Forex
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">Advanced Forex Investment Features</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Discover the advantages of forex investing on our platform
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm border border-gray-700 rounded-xl p-6 transform transition-all duration-300 hover:translate-y-[-5px] hover:shadow-lg">
          <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Competitive Returns</h3>
          <p class="text-gray-300">Invest in major, minor, and exotic currency pairs with competitive annualized returns, offering opportunities for portfolio growth.</p>
        </div>

        <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm border border-gray-700 rounded-xl p-6 transform transition-all duration-300 hover:translate-y-[-5px] hover:shadow-lg">
          <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Portfolio Diversification</h3>
          <p class="text-gray-300">Diversify your investments across global currency markets to balance risk and enhance long-term wealth-building potential.</p>
        </div>

        <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm border border-gray-700 rounded-xl p-6 transform transition-all duration-300 hover:translate-y-[-5px] hover:shadow-lg">
          <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Risk Management Tools</h3>
          <p class="text-gray-300">Utilize advanced tools to monitor and manage your investment risks effectively in volatile currency markets.</p>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Tools Section -->
  <section class="py-16 bg-gray-900 relative overflow-hidden">
    <div class="absolute inset-0 z-0 opacity-10 pointer-events-none">
      <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAwIiBoZWlnaHQ9IjYwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBkPSJNMzAgMzBoNjB2NjBoLTYweiIgZmlsbD0iIzMzMyIgZmlsbC1vcGFjaXR5PSIuMSIvPjwvc3ZnPg==')] bg-[length:30px_30px]"></div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Advanced Tools
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">Professional Forex Investment Tools</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Take advantage of our cutting-edge tools designed for forex investors
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
        <div>
          <div class="space-y-8">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <div class="flex items-center justify-center h-12 w-12 rounded-md bg-gradient-to-r from-blue-500 to-indigo-600 text-white">
                  <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-white">Real-time Market Analysis</h3>
                <p class="mt-2 text-base text-gray-300">
                  Access in-depth market analysis with real-time data feeds, economic calendars, and market news to make informed investment decisions.
                </p>
              </div>
            </div>

            <div class="flex items-start">
              <div class="flex-shrink-0">
                <div class="flex items-center justify-center h-12 w-12 rounded-md bg-gradient-to-r from-blue-500 to-indigo-600 text-white">
                  <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-white">Economic Calendar</h3>
                <p class="mt-2 text-base text-gray-300">
                  Stay updated with upcoming economic events and announcements that impact currency markets to optimize your investment strategy.
                </p>
              </div>
            </div>

            <div class="flex items-start">
              <div class="flex-shrink-0">
                <div class="flex items-center justify-center h-12 w-12 rounded-md bg-gradient-to-r from-blue-500 to-indigo-600 text-white">
                  <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
                  </svg>
                </div>
              </div>
              <div class="ml-4">
                <h3 class="text-lg font-medium text-white">Advanced Charts</h3>
                <p class="mt-2 text-base text-gray-300">
                  Utilize our sophisticated charting tools with over 50 technical indicators and drawing tools to analyze market trends and inform investments.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="relative">
          <div class="bg-gray-800 bg-opacity-50 backdrop-filter backdrop-blur-sm border border-gray-700 rounded-2xl p-8 shadow-xl">
            <div class="relative overflow-hidden rounded-xl">
              <div class="aspect-w-16 aspect-h-9 bg-gray-800 rounded-lg overflow-hidden">
                <div class="absolute inset-0 flex items-center justify-center">
                  <div class="w-full h-full bg-gradient-to-br from-blue-500/20 to-indigo-600/20">
                    <svg class="w-full h-full" viewBox="0 0 800 450" xmlns="http://www.w3.org/2000/svg">
                      <rect width="800" height="450" fill="none" />
                      <g stroke="#2D3748" stroke-width="0.5" opacity="0.5">
                        <line x1="0" y1="50" x2="800" y2="50" />
                        <line x1="0" y1="100" x2="800" y2="100" />
                        <line x1="0" y1="150" x2="800" y2="150" />
                        <line x1="0" y1="200" x2="800" y2="200" />
                        <line x1="0" y1="250" x2="800" y2="250" />
                        <line x1="0" y1="300" x2="800" y2="300" />
                        <line x1="0" y1="350" x2="800" y2="350" />
                        <line x1="0" y1="400" x2="800" y2="400" />
                        <line x1="100" y1="0" x2="100" y2="450" />
                        <line x1="200" y1="0" x2="200" y2="450" />
                        <line x1="300" y1="0" x2="300" y2="450" />
                        <line x1="400" y1="0" x2="400" y2="450" />
                        <line x1="500" y1="0" x2="500" y2="450" />
                        <line x1="600" y1="0" x2="600" y2="450" />
                        <line x1="700" y1="0" x2="700" y2="450" />
                      </g>
                      <path d="M0,300 L50,280 L100,290 L150,260 L200,240 L250,250 L300,220 L350,230 L400,200 L450,180 L500,210 L550,190 L600,170 L650,180 L700,150 L750,160 L800,130"
                            fill="none" stroke="#3B82F6" stroke-width="2" />
                      <path d="M0,300 L50,280 L100,290 L150,260 L200,240 L250,250 L300,220 L350,230 L400,200 L450,180 L500,210 L550,190 L600,170 L650,180 L700,150 L750,160 L800,130 L800,450 L0,450 Z"
                            fill="url(#investmentChartGrad)" opacity="0.2" />
                      <defs>
                        <linearGradient id="investmentChartGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                          <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.8" />
                          <stop offset="100%" stop-color="#3B82F6" stop-opacity="0" />
                        </linearGradient>
                      </defs>
                    </svg>
                  </div>
                </div>
              </div>
              <div class="absolute inset-0 pointer-events-none border border-gray-700 rounded-lg"></div>
            </div>

            <div class="mt-6 grid grid-cols-2 gap-4">
              <div class="bg-gray-800 bg-opacity-70 p-4 rounded-lg">
                <p class="text-xs font-medium text-gray-400 uppercase">Currency Pair</p>
                <p class="text-lg font-semibold text-white">EUR/USD</p>
              </div>
              <div class="bg-gray-800 bg-opacity-70 p-4 rounded-lg">
                <p class="text-xs font-medium text-gray-400 uppercase">Current Value</p>
                <p class="text-lg font-semibold text-green-400">1.2145</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Getting Started Section -->
  <section class="py-16 bg-gradient-to-b from-gray-800 to-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Start Investing
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">How to Start Investing in Forex</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Begin your forex investment journey in just a few simple steps
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="relative">
          <div class="absolute -inset-0.5 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl blur opacity-30 group-hover:opacity-50 transition duration-1000 group-hover:duration-200"></div>
          <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm p-6 rounded-xl flex flex-col items-center text-center">
            <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <span class="text-2xl font-bold text-white">1</span>
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Create an Account</h3>
            <p class="text-gray-300">Sign up for an investment account in minutes with a simple verification process.</p>
            <div class="mt-6">
              <RouterLink to="/register" class="px-5 py-2 text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg hover:from-blue-500 hover:to-indigo-500 transition duration-300 ease-in-out transform hover:scale-105">
                Register Now
              </RouterLink>
            </div>
          </div>
        </div>

        <div class="relative">
          <div class="absolute -inset-0.5 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl blur opacity-30 group-hover:opacity-50 transition duration-1000 group-hover:duration-200"></div>
          <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm p-6 rounded-xl flex flex-col items-center text-center">
            <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <span class="text-2xl font-bold text-white">2</span>
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Fund Your Account</h3>
            <p class="text-gray-300">Deposit funds using various payment methods including credit/debit cards, bank transfers, or e-wallets.</p>
            <div class="mt-6">
              <RouterLink to="/register" class="px-5 py-2 text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg hover:from-blue-500 hover:to-indigo-500 transition duration-300 ease-in-out transform hover:scale-105">
                Deposit Options
              </RouterLink>
            </div>
          </div>
        </div>

        <div class="relative">
          <div class="absolute -inset-0.5 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl blur opacity-30 group-hover:opacity-50 transition duration-1000 group-hover:duration-200"></div>
          <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm p-6 rounded-xl flex flex-col items-center text-center">
            <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <span class="text-2xl font-bold text-white">3</span>
            </div>
            <h3 class="text-xl font-bold text-white mb-3">Start Investing</h3>
            <p class="text-gray-300">Begin investing in our user-friendly platform with access to a wide range of forex currency pairs.</p>
            <div class="mt-6">
              <RouterLink to="/register" class="px-5 py-2 text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg hover:from-blue-500 hover:to-indigo-500 transition duration-300 ease-in-out transform hover:scale-105">
                Start Now
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Features Section -->
  <section class="py-16 bg-gray-900 relative overflow-hidden">
    <div class="absolute inset-0">
      <div class="absolute inset-0 bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
        <div class="absolute inset-0 opacity-10">
          <svg class="h-full w-full" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <pattern id="investment-grid" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M0 40V0h40" fill="none" stroke="currentColor" stroke-width="0.5"/>
                <circle cx="20" cy="20" r="1" fill="currentColor"/>
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#investment-grid)"/>
          </svg>
        </div>
      </div>
      <div class="absolute top-1/4 right-1/4 w-96 h-96 bg-blue-500/20 rounded-full filter blur-3xl"></div>
      <div class="absolute bottom-1/4 left-1/4 w-96 h-96 bg-emerald-400/20 rounded-full filter blur-3xl"></div>
    </div>

    <div class="container mx-auto px-4 relative z-10">
      <div class="text-center mb-12">
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
          Invest What You Want, <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">When You Want</span>
        </h2>
        <div class="w-24 h-1 bg-gradient-to-r from-blue-500 to-emerald-400 mx-auto"></div>
      </div>

      <div class="grid lg:grid-cols-2 gap-12 items-center">
        <div class="relative" data-aos="fade-right" data-aos-duration="1200">
          <div class="relative">
            <div class="absolute -inset-4">
              <div class="w-full h-full mx-auto opacity-30 blur-lg filter">
                <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-emerald-500 rounded-3xl"></div>
              </div>
            </div>
            <img src="/temp/custom/img/commission-scheme-crypt1t1.png" alt="investment-platform" class="relative rounded-xl shadow-2xl w-full">
          </div>
        </div>

        <div class="space-y-8">
          <div class="bg-gray-800 bg-opacity-50 backdrop-filter backdrop-blur-sm rounded-xl p-8 border border-gray-700">
            <p class="text-xl text-gray-200 leading-relaxed mb-8">
              One of the primary goals of {{ settingsStore.settings?.siteName }} is to provide the best investment platform in the market. Our relationships with leading financial institutions ensure diversified currency portfolios and competitive returns for investors.
            </p>

            <ul class="space-y-4">
              <li class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <svg class="h-6 w-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                </div>
                <span class="text-gray-300 text-lg">Invest in Forex, Indices, Equities & Commodities</span>
              </li>
              <li class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <svg class="h-6 w-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                </div>
                <span class="text-gray-300 text-lg">Access global markets 24 hours / 7 days</span>
              </li>
              <li class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <svg class="h-6 w-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                </div>
                <span class="text-gray-300 text-lg">Multilingual customer support</span>
              </li>
              <li class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <svg class="h-6 w-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                </div>
                <span class="text-gray-300 text-lg">Manage investments on our mobile apps</span>
              </li>
            </ul>

            <div class="mt-8">
              <RouterLink to="/about" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-lg text-white bg-gradient-to-r from-blue-600 to-emerald-500 hover:from-blue-700 hover:to-emerald-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-300 shadow-lg hover:shadow-xl" :title="`Learn About ${settingsStore.settings?.siteName || ''}`">
                Learn More About Our Services
                <svg class="ml-2 -mr-1 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/>
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- CTA Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="relative overflow-hidden rounded-3xl bg-gradient-to-r from-blue-600 to-indigo-600">
        <div class="absolute inset-0 overflow-hidden opacity-20">
          <svg class="absolute bottom-0 left-0 transform translate-y-1/2 -translate-x-1/2 lg:bottom-0 lg:translate-y-0 lg:translate-x-0" width="800" height="800" fill="none" viewBox="0 0 400 400">
            <defs>
              <pattern id="e9033f3e-f665-41a6-84ef-756f6778e6fe" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <rect x="0" y="0" width="4" height="4" fill="currentColor" />
              </pattern>
            </defs>
            <rect width="800" height="800" fill="url(#e9033f3e-f665-41a6-84ef-756f6778e6fe)" />
          </svg>
          <svg class="absolute top-0 right-0 transform -translate-y-1/2 translate-x-1/2 lg:top-0 lg:translate-y-0 lg:translate-x-0" width="800" height="800" fill="none" viewBox="0 0 400 400">
            <defs>
              <pattern id="e9033f3e-f665-41a6-84ef-756f6778e6ff" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                <rect x="0" y="0" width="4" height="4" fill="currentColor" />
              </pattern>
            </defs>
            <rect width="800" height="800" fill="url(#e9033f3e-f665-41a6-84ef-756f6778e6ff)" />
          </svg>
        </div>
        <div class="relative px-6 py-12 sm:py-16 sm:px-12 lg:px-16">
          <div class="md:ml-auto md:w-1/2 md:pl-10">
            <div class="text-base max-w-prose mx-auto lg:max-w-none">
              <span class="text-sm font-semibold text-white uppercase tracking-wider">Ready to invest?</span>
              <h2 class="mt-2 text-3xl leading-8 font-extrabold tracking-tight text-white sm:text-4xl">Start Your Forex Investment Journey</h2>
            </div>
            <div class="mt-6 prose prose-indigo prose-lg text-white">
              <p>Join thousands of investors worldwide who trust our platform for their forex investment needs. Open a real investment account today.</p>
            </div>
            <div class="mt-8 flex space-x-4">
              <RouterLink to="/register" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-indigo-600 bg-white hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-white focus:ring-offset-indigo-600">
                Create Account
              </RouterLink>
              <RouterLink to="/why-us" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-indigo-800 bg-opacity-60 hover:bg-opacity-70 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                Learn More
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
