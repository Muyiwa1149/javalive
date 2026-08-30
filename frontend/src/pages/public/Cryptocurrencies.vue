<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { usePublicSettingsStore } from '@/stores/publicSettings'

const settingsStore = usePublicSettingsStore()

const heroTextVisible = ref(false)
const heroCardVisible = ref(false)
const imageVisible = ref(false)
const contentVisible = ref(false)

onMounted(async () => {
  await settingsStore.ensureLoaded()
  setTimeout(() => { heroTextVisible.value = true }, 200)
  setTimeout(() => { heroCardVisible.value = true }, 500)
  setTimeout(() => { imageVisible.value = true }, 300)
  setTimeout(() => { contentVisible.value = true }, 500)
})

const categoryFilter = ref('All')
const searchQuery = ref('')

// The source blade's investment markets table is populated client-side
// with no hardcoded fallback rows (only the DataTable shell and headers
// exist in the blade). No live pricing feed is wired up yet, so the table
// renders empty, matching the source's "No matching records found" state.
const assets = []

const filteredAssets = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  return assets.filter((a) => {
    const matchesCategory = categoryFilter.value === 'All' || a.category === categoryFilter.value
    const matchesSearch = !q || a.symbol.toLowerCase().includes(q) || a.name.toLowerCase().includes(q)
    return matchesCategory && matchesSearch
  })
})
</script>

<template>
  <!-- Hero Section -->
  <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
    <div class="absolute inset-0 z-0 pointer-events-none opacity-20">
      <div class="absolute top-0 right-0 w-full h-full">
        <svg class="absolute top-0 right-0 w-full h-full" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="cryptoGrad1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop stop-color="#F7931A" stop-opacity=".25" offset="0%"/>
              <stop stop-color="#627EEA" stop-opacity=".2" offset="50%"/>
              <stop stop-color="#11A97D" stop-opacity=".15" offset="100%"/>
            </linearGradient>
          </defs>
          <path fill="url(#cryptoGrad1)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" />
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
              Investment Assets
            </div>
            <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
              <span class="block">Investment Opportunities</span>
              <span class="block mt-2 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-purple-400 to-orange-400">Portfolios with Bitcoin, Ethereum, Equities, and more</span>
            </h1>
            <p class="mt-3 text-lg text-gray-300 max-w-2xl">
              Invest in a diversified portfolio, including digital currencies like Bitcoin and Ethereum, alongside traditional assets like equities and bonds. Our platform leverages advanced financial strategies to help you build wealth securely and efficiently.
            </p>
            <div class="flex flex-wrap gap-4 mt-6">
              <a href="#investment-table" class="px-6 py-3 text-base font-medium text-white bg-blue-600 rounded-lg shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-gray-900 transition-all duration-200">
                View Opportunities
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
            <div class="grid grid-cols-2 gap-6">
              <!-- Bitcoin -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center space-x-3 mb-2">
                  <img src="/dash/bitcoin-btc-logo.png" alt="Bitcoin" class="w-10 h-10">
                  <div>
                    <h3 class="text-lg font-bold text-white">BTC/USD</h3>
                  </div>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm text-gray-400">Value</span>
                  <span class="text-green-400 font-medium">$48,795.20</span>
                </div>
              </div>
              <!-- Ethereum -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center space-x-3 mb-2">
                  <img src="/dash/ethereum-eth-logo.png" alt="Ethereum" class="w-10 h-10">
                  <div>
                    <h3 class="text-lg font-bold text-white">ETH/USD</h3>
                  </div>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm text-gray-400">Value</span>
                  <span class="text-green-400 font-medium">$2,873.50</span>
                </div>
              </div>
              <!-- Tether -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center space-x-3 mb-2">
                  <img src="/dash/tether-usdt-logo.png" alt="Tether" class="w-10 h-10">
                  <div>
                    <h3 class="text-lg font-bold text-white">USDT/USD</h3>
                  </div>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm text-gray-400">Value</span>
                  <span class="text-gray-300 font-medium">$1.00</span>
                </div>
              </div>
              <!-- XRP -->
              <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 transform hover:translate-y-[-5px] transition-all duration-300">
                <div class="flex items-center space-x-3 mb-2">
                  <div class="w-10 h-10 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center text-blue-400">
                    XRP
                  </div>
                  <div>
                    <h3 class="text-lg font-bold text-white">XRP/USD</h3>
                  </div>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm text-gray-400">Value</span>
                  <span class="text-red-400 font-medium">$0.72</span>
                </div>
              </div>
            </div>

            <div class="absolute -bottom-6 -right-6 w-32 h-32 bg-blue-500 bg-opacity-20 rounded-full filter blur-xl"></div>
            <div class="absolute -top-6 -left-6 w-24 h-24 bg-purple-500 bg-opacity-20 rounded-full filter blur-xl"></div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Markets Table Section -->
  <section id="investment-table" class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Live Markets
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">Investment Markets</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Invest in a diversified portfolio of assets, including cryptocurrencies, equities, and bonds, with competitive returns and advanced tools
        </p>
      </div>

      <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm rounded-xl border border-gray-700 overflow-hidden shadow-lg">
        <!-- Search and Filter Controls -->
        <div class="p-4 border-b border-gray-700 flex flex-col md:flex-row justify-between items-center gap-4">
          <div class="relative w-full md:w-64">
            <input v-model="searchQuery" type="text" class="w-full bg-gray-900 border border-gray-700 rounded-lg py-2 px-4 pl-10 text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" placeholder="Search investment assets...">
            <svg class="absolute left-3 top-2.5 h-5 w-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
          </div>
          <div class="flex flex-wrap gap-2">
            <select v-model="categoryFilter" class="bg-gray-900 border border-gray-700 rounded-lg py-2 px-4 text-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option value="All">All Assets</option>
              <option value="Cryptocurrencies">Cryptocurrencies</option>
              <option value="Equities">Equities</option>
              <option value="Bonds">Bonds</option>
            </select>
            <button class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all duration-200">
              Filter
            </button>
          </div>
        </div>

        <!-- Data Table -->
        <div class="relative overflow-x-auto" style="min-height: 200px;">
          <div class="w-full">
            <table class="w-full text-sm text-left text-gray-300">
              <thead class="text-xs uppercase bg-gray-800 text-gray-400">
                <tr>
                  <th class="px-6 py-3">Symbol</th>
                  <th class="px-6 py-3">Name</th>
                  <th class="px-6 py-3">Value</th>
                  <th class="px-6 py-3">24h Change</th>
                  <th class="px-6 py-3">Market Cap</th>
                  <th class="px-6 py-3">Fee</th>
                  <th class="px-6 py-3">Action</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="asset in filteredAssets" :key="asset.symbol">
                  <td class="px-6 py-4">{{ asset.symbol }}</td>
                  <td class="px-6 py-4">{{ asset.name }}</td>
                  <td class="px-6 py-4">{{ asset.value }}</td>
                  <td class="px-6 py-4">{{ asset.change }}</td>
                  <td class="px-6 py-4">{{ asset.marketCap }}</td>
                  <td class="px-6 py-4">{{ asset.fee }}</td>
                  <td class="px-6 py-4"></td>
                </tr>
                <tr v-if="filteredAssets.length === 0">
                  <td colspan="7" class="px-6 py-8 text-center text-gray-500">No matching records found</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Features Section -->
  <section class="py-20 bg-gradient-to-b from-gray-900 to-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="text-center mb-16">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Investment Flexibility
        </div>
        <h2 class="text-3xl font-extrabold text-white sm:text-4xl">
          <span class="block">Invest what you want,</span>
          <span class="block text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-teal-400">When you want</span>
        </h2>
        <p class="mt-4 text-xl text-gray-300 max-w-3xl mx-auto">
          Our platform provides you with the tools and access you need for successful investing
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
        <!-- Image Column -->
        <div class="relative order-2 lg:order-1" data-aos="fade-right" data-aos-duration="1200" data-aos-anchor=".tr-cost">
          <div
            class="relative z-10 transition-all duration-700 ease-out"
            :class="imageVisible ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-8'"
          >
            <div class="relative rounded-2xl overflow-hidden shadow-2xl">
              <img src="/temp/custom/img/commission-scheme-crypt1t1.png" alt="Investment Platform" class="w-full">
              <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-teal-400 opacity-20"></div>
            </div>

            <div class="absolute -bottom-8 -right-8 w-40 h-40 bg-blue-500 bg-opacity-20 rounded-full filter blur-xl"></div>
            <div class="absolute -top-8 -left-8 w-32 h-32 bg-teal-500 bg-opacity-20 rounded-full filter blur-xl"></div>
          </div>
        </div>

        <!-- Content Column -->
        <div class="space-y-8 order-1 lg:order-2">
          <div
            class="transition-all duration-700 ease-out delay-300"
            :class="contentVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'"
          >
            <h3 class="text-2xl font-bold text-white mb-6">
              One of the primary goals of {{ settingsStore.settings?.siteName }} is to provide the best investment platform in the market. Our relationships with leading financial institutions ensure diversified portfolios and competitive returns for investors.
            </h3>

            <ul class="space-y-4">
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <p class="ml-3 text-lg text-gray-300">
                  Invest in Equities, Bonds, ETFs & Cryptocurrencies
                </p>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <p class="ml-3 text-lg text-gray-300">
                  Access global markets 24 hours / 7 days
                </p>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <p class="ml-3 text-lg text-gray-300">
                  Multilingual customer support
                </p>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="h-6 w-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <p class="ml-3 text-lg text-gray-300">
                  Manage investments on our mobile apps
                </p>
              </li>
            </ul>

            <div class="mt-8">
              <RouterLink to="/about" :title="`Learn ${settingsStore.settings?.siteName || ''}`" class="inline-flex items-center px-6 py-3 text-base font-medium text-white bg-blue-600 border border-transparent rounded-lg shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-900 focus:ring-blue-500 transition-all duration-200">
                Learn More
                <svg class="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Call to Action Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-gradient-to-r from-blue-900 to-blue-800 rounded-2xl overflow-hidden shadow-xl">
        <div class="relative px-8 py-12 md:p-12 lg:px-16 lg:py-16">
          <div class="absolute inset-0 overflow-hidden opacity-10">
            <svg class="absolute right-0 top-0 h-full" viewBox="0 0 800 800">
              <path fill="none" stroke="white" stroke-width="2" d="M769 229L1037 260.9M927 880L731 737 520 660 309 538 40 599 295 764">
              </path>
              <path fill="none" stroke="white" stroke-width="2" d="M-4 44L190 190 731 737 520 660 309 538 40 599 295 764">
              </path>
              <path fill="none" stroke="white" stroke-width="2" d="M-4 44L190 190 731 737M490 85L309 538 40 599 295 764">
              </path>
            </svg>
          </div>

          <div class="relative z-10 text-center max-w-2xl mx-auto">
            <h2 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
              Ready to Start Investing in Your Future?
            </h2>
            <p class="mt-4 text-xl text-blue-100">
              Join thousands of investors using our platform to build wealth with cryptocurrencies and other assets.
            </p>
            <div class="mt-8 flex flex-wrap justify-center gap-4">
              <RouterLink to="/register" class="px-8 py-4 text-base font-medium text-blue-900 bg-white border border-transparent rounded-lg shadow-lg hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-blue-800 transition-all duration-200">
                Create Free Account
              </RouterLink>
              <RouterLink to="/login" class="px-8 py-4 text-base font-medium text-white bg-transparent border border-white rounded-lg shadow-lg hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-blue-800 transition-all duration-200">
                Login
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
