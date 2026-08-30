<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import { usePublicSettingsStore } from '@/stores/publicSettings'

const settingsStore = usePublicSettingsStore()

/* ---------------------------------------------------------------------- *
 * Hero fade-in (was Alpine x-data="{ isVisible: false }" + x-transition)
 * ---------------------------------------------------------------------- */
const heroVisible = ref(false)

/* Live clock shown in the hero chart panel's "LIVE" status bar. */
const clockTime = ref('')
let clockInterval = null

/* ---------------------------------------------------------------------- *
 * BTC live ticker (hero chart header). The source fetched this straight
 * from the public CoinGecko API client-side via Alpine x-init, so it is
 * ported the same way here -- no backend endpoint is required for it.
 * The controller-supplied $btcStats server-side fallback values (shown
 * momentarily before Alpine hydrated) are simply represented by the 0 /
 * '--' initial ref values below.
 * ---------------------------------------------------------------------- */
const btcPrice = ref(0)
const btcPrevPrice = ref(0)
const btcChange = ref(0)
const btcVolume = ref(0)
const btcUpdatedAtLabel = ref('')
let btcTickerInterval = null

async function updateBtcTicker() {
  try {
    const res = await fetch('https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true&include_24hr_vol=true')
    const data = await res.json()
    btcPrevPrice.value = btcPrice.value
    btcPrice.value = data.bitcoin.usd
    btcChange.value = data.bitcoin.usd_24h_change
    btcVolume.value = data.bitcoin.usd_24h_vol
  } catch (err) {
    console.error(err)
  }
}

/* ---------------------------------------------------------------------- *
 * Investment Plans section. HomePageController@index passes a live
 * $plans collection from the database and there is no public plans API
 * yet, so this is a stub placeholder that reproduces the exact card
 * structure/styling from the source until a real endpoint exists.
 * TODO(backend): replace with GET /api/public/plans once available.
 * ---------------------------------------------------------------------- */
const plans = ref([
  { name: 'Starter', minr: 5, maxr: 8, incrementInterval: 'Weekly', minPrice: 100, maxPrice: 999, incrementAmount: 5 },
  { name: 'Standard', minr: 8, maxr: 12, incrementInterval: 'Weekly', minPrice: 1000, maxPrice: 4999, incrementAmount: 10 },
  { name: 'Advanced', minr: 12, maxr: 18, incrementInterval: 'Weekly', minPrice: 5000, maxPrice: 19999, incrementAmount: 22 },
  { name: 'Premium', minr: 18, maxr: 25, incrementInterval: 'Weekly', minPrice: 20000, maxPrice: 100000, incrementAmount: 30 },
])

/* ---------------------------------------------------------------------- *
 * Script loading helper, shared by the TradingView + Chart.js widgets
 * below (same pattern as PublicLayout.vue's loadTickerWidget()).
 * ---------------------------------------------------------------------- */
function loadScript(src) {
  return new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[src="${src}"]`)
    if (existing) {
      if (existing.dataset.loaded === 'true') { resolve(); return }
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', reject)
      return
    }
    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.onload = () => { script.dataset.loaded = 'true'; resolve() }
    script.onerror = reject
    document.body.appendChild(script)
  })
}

/* Investment Distribution pie chart (Chart.js) -- static demo data, matches source. */
const investmentChartCanvas = ref(null)
let investmentChartInstance = null

async function initInvestmentChart() {
  await loadScript('https://cdn.jsdelivr.net/npm/chart.js')
  if (!investmentChartCanvas.value || !window.Chart) return
  investmentChartInstance = new window.Chart(investmentChartCanvas.value.getContext('2d'), {
    type: 'pie',
    data: {
      labels: ['Foreign Exchange', 'Cryptocurrency', 'Commodities', 'Precious Metals', 'Stocks'],
      datasets: [{
        data: [45, 30, 15, 5.5, 4.5],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',
          'rgba(16, 185, 129, 0.8)',
          'rgba(234, 179, 8, 0.8)',
          'rgba(168, 85, 247, 0.8)',
          'rgba(239, 68, 68, 0.8)',
        ],
        borderColor: [
          'rgba(59, 130, 246, 1)',
          'rgba(16, 185, 129, 1)',
          'rgba(234, 179, 8, 1)',
          'rgba(168, 85, 247, 1)',
          'rgba(239, 68, 68, 1)',
        ],
        borderWidth: 1,
      }],
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: 'bottom', labels: { color: '#ffffff', font: { size: 14 } } },
        tooltip: { callbacks: { label: (context) => `${context.label}: ${context.raw}%` } },
      },
    },
  })
}

/* Hero TradingView candlestick chart widget ("TradingView Widget BEGIN/END" in source). */
async function initHeroChart() {
  await loadScript('https://s3.tradingview.com/tv.js')
  if (!window.TradingView) return
  // eslint-disable-next-line no-new
  new window.TradingView.widget({
    autosize: true,
    symbol: 'BITSTAMP:BTCUSD',
    interval: 'D',
    timezone: 'Etc/UTC',
    theme: 'dark',
    style: '1',
    locale: 'en',
    toolbar_bg: '#1f2937',
    enable_publishing: false,
    hide_top_toolbar: false,
    withdateranges: true,
    hide_legend: false,
    allow_symbol_change: true,
    save_image: true,
    container_id: 'tradingview_chart',
    height: 900,
    studies: ['MASimple@tv-basicstudies', 'RSI@tv-basicstudies', 'MACD@tv-basicstudies'],
    overrides: {
      'mainSeriesProperties.candleStyle.upColor': '#0ecb81',
      'mainSeriesProperties.candleStyle.downColor': '#f6465d',
      'mainSeriesProperties.candleStyle.wickUpColor': '#0ecb81',
      'mainSeriesProperties.candleStyle.wickDownColor': '#f6465d',
    },
  })

  // Forex heat map widget (Trusted Brand section) reuses the same tv.js instance,
  // mirroring the source's x-init="setTimeout(() => new TradingView.widget({...}), 100)".
  setTimeout(() => {
    if (!document.getElementById('forex_heat_map')) return
    // eslint-disable-next-line no-new
    new window.TradingView.widget({
      container_id: 'forex_heat_map',
      width: '100%',
      height: 400,
      currencies: ['EUR', 'USD', 'JPY', 'GBP', 'CHF', 'AUD', 'CAD', 'NZD', 'CNY'],
      isTransparent: true,
      colorTheme: 'dark',
      locale: 'en',
    })
  }, 100)
}

/* Market ticker tape widget (below hero) -- same pattern as PublicLayout's sticky ticker,
 * but with the page-specific symbol list used in the source's "Market Ticker" block. */
const marketTickerContainer = ref(null)
function loadMarketTicker() {
  if (!marketTickerContainer.value) return
  const script = document.createElement('script')
  script.type = 'text/javascript'
  script.async = true
  script.src = 'https://s3.tradingview.com/external-embedding/embed-widget-ticker-tape.js'
  script.innerHTML = JSON.stringify({
    symbols: [
      { proName: 'FOREXCOM:SPXUSD', title: 'S&P 500' },
      { proName: 'FOREXCOM:NSXUSD', title: 'Nasdaq 100' },
      { proName: 'FX_IDC:EURUSD', title: 'EUR/USD' },
      { proName: 'BITSTAMP:BTCUSD', title: 'BTC/USD' },
      { proName: 'BITSTAMP:ETHUSD', title: 'ETH/USD' },
    ],
    showSymbolLogo: true,
    colorTheme: 'dark',
    isTransparent: false,
    displayMode: 'adaptive',
    locale: 'en',
  })
  marketTickerContainer.value.appendChild(script)
}

/* Client testimonials "view more" toggle (was Alpine x-data="{ showMore: false }"). */
const showMoreTestimonials = ref(false)

onMounted(async () => {
  await settingsStore.ensureLoaded()

  setTimeout(() => { heroVisible.value = true }, 200)

  clockTime.value = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  clockInterval = setInterval(() => {
    clockTime.value = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  }, 1000)

  btcUpdatedAtLabel.value = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
  updateBtcTicker()
  btcTickerInterval = setInterval(updateBtcTicker, 30000)

  initHeroChart()
  loadMarketTicker()
  initInvestmentChart()
})

onBeforeUnmount(() => {
  if (clockInterval) clearInterval(clockInterval)
  if (btcTickerInterval) clearInterval(btcTickerInterval)
  if (investmentChartInstance) investmentChartInstance.destroy()
})
</script>

<template>
  <!-- Hero Section -->
  <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
    <!-- Abstract Background Elements -->
    <div class="absolute inset-0 z-0">
      <div class="absolute top-0 left-0 w-full h-full opacity-20">
        <svg class="absolute top-0 left-0 w-full h-full" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="a" x1="50%" x2="50%" y1="0%" y2="100%">
              <stop stop-color="#3B82F6" stop-opacity=".25" offset="0%"/>
              <stop stop-color="#10B981" stop-opacity=".2" offset="100%"/>
            </linearGradient>
          </defs>
          <path fill="url(#a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(0 -50)" />
          <path fill="url(#a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(350 150)" />
        </svg>
      </div>
      <div class="absolute bottom-0 right-0 w-full h-full opacity-10">
        <svg width="100%" height="100%" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" stroke="#6366F1" stroke-width="2">
            <path d="M769 229L1037 260.9M927 880L731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737 520 660 309 538 40 599 295 764"/>
            <path d="M-4 44L190 190 731 737M490 85L309 538 40 599 295 764"/>
            <path d="M733 738L520 660M603 493L731 737M520 660L309 538"/>
          </g>
        </svg>
      </div>
    </div>

    <!-- Hero Content -->
    <div class="relative z-10 px-4 py-16 mx-auto max-w-7xl sm:px-6 lg:px-8">
      <div class="flex flex-col items-center gap-8 md:flex-row">
        <!-- Left Column - Text Content -->
        <div class="w-full md:w-1/2 order-1 md:order-1">
          <div
            class="space-y-6 transition ease-out duration-500 transform"
            :class="heroVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'"
          >
            <div class="inline-block px-3 py-1 mb-2 text-xs font-semibold tracking-wider text-primary uppercase bg-blue-900 bg-opacity-30 rounded-full">
              Innovative Trading Platform
            </div>
            <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
              <span class="block">Keystone Bit-Forex</span>
              <span class="block mt-1 text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">Trade Global Markets With Confidence</span>
            </h1>
            <p class="max-w-lg mt-5 text-xl text-gray-300">
              Access advanced trading tools for Forex, Cryptocurrencies, Commodities, Indices, and more with competitive spreads and lightning-fast execution.
            </p>
            <div class="flex flex-wrap gap-4 mt-8">
              <RouterLink to="/register" class="px-8 py-3 text-lg font-medium text-white transition-all duration-200 bg-blue-600 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 focus:ring-offset-gray-900">
                Create Account
              </RouterLink>
              <RouterLink to="/login" class="px-8 py-3 text-lg font-medium text-gray-200 transition-all duration-200 bg-dark-200 border border-gray-700 rounded-lg hover:bg-dark-100 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 focus:ring-offset-gray-900">
                Login
              </RouterLink>
            </div>
          </div>
        </div>

        <!-- Right Column - Advanced Chart Visualization -->
        <div class="w-full md:w-1/2 order-2 md:order-2">
          <div class="relative overflow-hidden backdrop-blur-sm bg-opacity-60 bg-gray-800 rounded-2xl border border-gray-700 shadow-[0_0_25px_rgba(59,130,246,0.15)]">
            <div class="absolute top-0 right-0 w-32 h-32 bg-blue-600 rounded-full filter blur-3xl opacity-10 -mr-10 -mt-10"></div>
            <div class="absolute bottom-0 left-0 w-32 h-32 bg-blue-400 rounded-full filter blur-3xl opacity-10 -ml-10 -mb-10"></div>

            <!-- Live Market Status -->
            <div class="bg-gray-800 bg-opacity-90 border-b border-gray-700">
              <div class="flex items-center justify-between px-4 py-2">
                <div class="flex items-center space-x-3">
                  <div class="flex items-center space-x-1">
                    <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                    <span class="text-xs text-green-400">LIVE</span>
                  </div>
                  <div class="text-xs text-gray-400">
                    <span>{{ clockTime }}</span>
                  </div>
                </div>
                <div class="flex items-center space-x-3">
                  <div class="flex items-center space-x-2">
                    <span class="text-xs text-green-400">Open</span>
                  </div>
                  <select class="bg-gray-700 text-gray-300 text-xs rounded border-none focus:ring-blue-500 py-1">
                    <option>1D</option>
                    <option>1W</option>
                    <option>1M</option>
                    <option>1Y</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Chart Header with Live Data -->
            <div class="flex items-center justify-between px-4 py-3 border-b border-gray-700">
              <div class="flex items-center space-x-2">
                <div class="flex items-center justify-center w-8 h-8 bg-blue-600 bg-opacity-20 rounded-md">
                  <i class="fas fa-chart-line text-blue-400"></i>
                </div>
                <div>
                  <div class="flex items-center space-x-2">
                    <span class="font-semibold text-white">BTC/USD</span>
                    <span
                      class="text-lg font-bold"
                      :class="btcPrice > btcPrevPrice ? 'text-green-400' : (btcPrice < btcPrevPrice ? 'text-red-400' : 'text-white')"
                    >${{ btcPrice.toLocaleString() }}</span>
                    <span
                      class="px-1.5 py-0.5 text-xs rounded"
                      :class="btcChange >= 0 ? 'bg-green-500 bg-opacity-20 text-green-400' : 'bg-red-500 bg-opacity-20 text-red-400'"
                    >{{ (btcChange >= 0 ? '+' : '') + btcChange.toFixed(2) }}%</span>
                  </div>
                  <div class="text-xs text-gray-400 flex items-center">
                    <span>Real-time data</span>
                    <span class="mx-1">&bull;</span>
                    <span>Vol: ${{ btcVolume ? (btcVolume / 1000000000).toFixed(1) + 'B' : '24.8B' }}</span>
                    <span class="mx-1">&bull;</span>
                    <span>Updated <span>{{ btcUpdatedAtLabel }}</span></span>
                  </div>
                </div>
              </div>
              <div class="flex space-x-2">
                <button class="p-1.5 bg-gray-700 bg-opacity-50 hover:bg-opacity-80 rounded-md transition-all">
                  <svg class="w-4 h-4 text-gray-300" fill="currentColor" viewBox="0 0 20 20"><path d="M5 4a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1H5zm1 2h8v8H6V6z"/></svg>
                </button>
                <button class="p-1.5 bg-gray-700 bg-opacity-50 hover:bg-opacity-80 rounded-md transition-all">
                  <svg class="w-4 h-4 text-gray-300" fill="currentColor" viewBox="0 0 20 20"><path d="M10 12a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"/><path fill-rule="evenodd" d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16zm0-2a6 6 0 1 0 0-12 6 6 0 0 0 0 12z"/></svg>
                </button>
                <button class="p-1.5 bg-blue-600 bg-opacity-50 hover:bg-opacity-80 rounded-md transition-all">
                  <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20"><path d="M15 8a.5.5 0 0 1-.5.5h-2.5V11a.5.5 0 0 1-1 0V8.5H8.5a.5.5 0 0 1 0-1H11V5a.5.5 0 0 1 1 0v2.5h2.5a.5.5 0 0 1 .5.5z"/></svg>
                </button>
              </div>
            </div>

            <!-- TradingView Widget BEGIN -->
            <div class="tradingview-widget-container">
              <div id="tradingview_chart" class="h-[400px]"></div>
            </div>
            <!-- TradingView Widget END -->

            <!-- Chart Footer Analytics (source authored this grid with no cell content -- kept empty for visual parity) -->
            <div class="grid grid-cols-2 sm:grid-cols-4 divide-x divide-gray-700 border-t border-gray-700"></div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Market Ticker -->
  <div class="py-2 bg-gray-800 border-t border-b border-gray-700">
    <div class="tradingview-widget-container" ref="marketTickerContainer">
      <div class="tradingview-widget-container__widget"></div>
    </div>
  </div>

  <!-- Features Section -->
  <section class="py-12 bg-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <h2 class="text-3xl font-bold text-white">Why Invest With Us</h2>
        <p class="mt-2 text-gray-400">Everything you need for successful investing</p>
      </div>

      <!-- Features Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        <div class="flex flex-col items-center">
          <div class="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gradient-to-br from-blue-600 to-blue-800 shadow-lg">
            <i class="fas fa-chart-line text-white text-2xl"></i>
          </div>
          <h3 class="mb-2 text-xl font-bold text-white text-center">Investment Tools</h3>
          <p class="text-gray-400 text-center">Plan your investments effectively with our wide range of free professional tools</p>
        </div>

        <div class="flex flex-col items-center">
          <div class="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gradient-to-br from-green-500 to-green-700 shadow-lg">
            <i class="fas fa-layer-group text-white text-2xl"></i>
          </div>
          <h3 class="mb-2 text-xl font-bold text-white text-center">Investment Products</h3>
          <p class="text-gray-400 text-center">Diverse opportunities to optimize your investment portfolio across multiple markets</p>
        </div>

        <div class="flex flex-col items-center">
          <div class="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 shadow-lg">
            <i class="fas fa-desktop text-white text-2xl"></i>
          </div>
          <h3 class="mb-2 text-xl font-bold text-white text-center">Investment Platforms</h3>
          <p class="text-gray-400 text-center">Powerful platforms to suit all investment styles and needs on any device</p>
        </div>

        <div class="flex flex-col items-center">
          <div class="flex items-center justify-center w-16 h-16 mb-4 rounded-full bg-gradient-to-br from-yellow-500 to-yellow-700 shadow-lg">
            <i class="fas fa-wallet text-white text-2xl"></i>
          </div>
          <h3 class="mb-2 text-xl font-bold text-white text-center">Funding Methods</h3>
          <p class="text-gray-400 text-center">Multiple quick, easy, and secure methods to fund your investment account</p>
        </div>
      </div>
    </div>
  </section>

  <!-- Pricing Plans Section -->
  <section class="py-16 bg-gray-900" id="Investment">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Investment Plans
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">Investment Opportunities</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Choose the perfect plan that suits your investment strategy and financial goals</p>
        <!-- Investment Plan Description -->
        <div class="mt-6 text-gray-300 max-w-3xl mx-auto text-center">
          <h3 class="text-xl font-semibold text-white mb-2">Our Investment Approach</h3>
          <p>Unlike non-standard investment strategies, the general management of investors' capital globalizes the process of making profit and creating benefits immediately for all program participants. Our company, in this case, serves as one of the major players in the market. The total capital allows us to earn large interests on the personal deposits of our clients. Part of this profit is the company's income, hence our traders work to increase their income on deposits because their profit directly depends on the profitability of investors.</p>
        </div>
      </div>

      <div class="overflow-hidden">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <!-- Plan Cards (placeholder pending a real plans API -- see TODO in <script>) -->
          <div v-for="plan in plans" :key="plan.name" class="relative group">
            <!-- Gradient background effect -->
            <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/20 rounded-xl transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>

            <!-- Card Content -->
            <div class="relative bg-gray-800 bg-opacity-90 rounded-xl overflow-hidden border border-gray-700 group-hover:border-blue-600 transform transition-all duration-500 hover:shadow-lg hover:shadow-blue-900/20 hover:-translate-y-1">
              <!-- Card Header -->
              <div class="h-24 bg-blue-600 flex items-center justify-center relative">
                <div class="absolute inset-0 bg-pattern opacity-10"></div>
                <h3 class="text-xl font-bold text-white uppercase tracking-wider">{{ plan.name }}</h3>
              </div>

              <!-- Pricing -->
              <div class="p-6 text-center">
                <div class="mb-6">
                  <span class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">{{ plan.minr }}% - {{ plan.maxr }}%</span>
                  <span class="text-gray-300 ml-1"> {{ plan.incrementInterval }} </span>
                </div>

                <!-- Features List -->
                <ul class="space-y-3 mb-8">
                  <li class="text-gray-300 flex items-center justify-center">
                    <svg class="w-5 h-5 text-blue-500 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                    </svg>
                    Instant Withdrawal
                  </li>
                  <li class="text-gray-300 flex items-center justify-center">
                    <svg class="w-5 h-5 text-blue-500 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                    </svg>
                    24/7 Support
                  </li>
                  <li class="text-white font-semibold flex items-center justify-center border-t border-gray-700 pt-3 mt-3">
                    <svg class="w-5 h-5 text-blue-400 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                      <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"></path>
                    </svg>
                    Min: <span class="text-blue-300 ml-1">${{ plan.minPrice }}</span>
                  </li>
                  <li class="text-white font-semibold flex items-center justify-center">
                    <svg class="w-5 h-5 text-blue-400 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                      <path fill-rule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"></path>
                    </svg>
                    Max: <span class="text-blue-300 ml-1">${{ plan.maxPrice }}</span>
                  </li>
                </ul>

                <!-- CTA Button -->
                <RouterLink to="/login" class="block w-full px-4 py-3 font-medium text-center text-white bg-blue-600 hover:bg-blue-700 rounded-lg shadow-md transition duration-300 transform hover:-translate-y-1">
                  Select Plan
                </RouterLink>
              </div>

              <!-- Visual indicator for premium plans -->
              <div v-if="plan.incrementAmount > 20" class="absolute top-0 right-0">
                <div class="bg-gradient-to-r from-amber-500 to-blue-500 text-white text-xs px-3 py-1 font-semibold rounded-bl-lg rounded-tr-lg">
                  Premium
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Additional Info -->
      <div class="mt-12 p-6 bg-gray-800 bg-opacity-70 rounded-xl border border-gray-700 shadow-lg">
        <div class="flex flex-col md:flex-row items-center justify-between">
          <div class="md:w-3/5 mb-6 md:mb-0">
            <h3 class="text-xl font-bold text-white mb-3">Need a custom plan?</h3>
            <p class="text-gray-300">Our team can create tailored investment solutions for institutional clients and high-net-worth individuals.</p>
          </div>
          <div>
            <RouterLink to="/contact" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 shadow-lg transition duration-300 transform hover:-translate-y-1">
              Contact Our Team
              <svg class="ml-2 -mr-1 w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
              </svg>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>
  <!-- End Pricing Section -->

  <section class="py-16 bg-dark-400">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- First Row: Chart and Text -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <!-- Chart Column -->
        <div class="rounded-xl overflow-hidden shadow-lg bg-dark-300 p-6">
          <h3 class="text-xl font-semibold text-white mb-4">Investment Distribution</h3>
          <canvas ref="investmentChartCanvas" class="w-full h-[300px]"></canvas>
        </div>
        <!-- Text Column -->
        <div class="space-y-6">
          <div>
            <span class="text-primary text-sm font-semibold uppercase tracking-wider">Investment Education</span>
            <h2 class="mt-2 text-3xl font-bold text-white">Why Invest With Keystone Bit-FX</h2>
          </div>
          <p class="text-gray-300 text-lg">
            Discover the power of diversified investing with our AI-driven strategies and expert-managed portfolios, designed to maximize returns across global markets.
          </p>
          <div class="bg-dark-300 rounded-lg p-6 border border-gray-800">
            <h3 class="text-xl font-semibold text-white mb-3">Why Choose Us</h3>
            <p class="text-gray-400">
              At Keystone Bit-FX, we leverage cutting-edge AI technology and expert analysis to offer secure, transparent, and profitable investment opportunities. Our diversified portfolios span forex, cryptocurrencies, commodities, precious metals, and stocks, ensuring stability and growth. With a focus on user-friendly platforms, competitive conditions, and robust security, we empower investors to achieve their financial goals with confidence.
            </p>
          </div>
          <div class="flex flex-wrap gap-4 mt-4">
            <RouterLink to="/investment-resources" class="inline-flex items-center px-5 py-2 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition duration-150">
              Explore Investment Resources
              <i class="fas fa-graduation-cap ml-2"></i>
            </RouterLink>
            <RouterLink to="/webinars" class="inline-flex items-center px-5 py-2 border border-gray-700 text-base font-medium rounded-md text-gray-300 hover:text-white hover:border-gray-500 transition duration-150">
              Join Free Webinars
              <i class="fas fa-video ml-2"></i>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Products Section -->
  <section class="py-16 bg-dark-300">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <h2 class="text-3xl font-bold text-white">Diverse Investment Products</h2>
        <p class="mt-2 text-gray-400">Access global markets with AI-driven strategies and expert management</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8">
        <!-- Forex Card -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-blue-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-blue-500 transition-all duration-300">
            <div class="w-12 h-12 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <i class="fas fa-globe text-blue-400 text-xl"></i>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">Forex (FIAT Currency)</h3>
            <p class="text-gray-400 mb-4 text-sm">The world's largest market with $5 trillion daily volume. Our Expert Advisors and automated robots provide stable, profitable trading without extensive market knowledge.</p>
          </div>
        </div>

        <!-- Crypto-currencies Card -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-r from-orange-600 to-orange-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-orange-500 transition-all duration-300">
            <div class="w-12 h-12 bg-orange-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <i class="fab fa-bitcoin text-orange-400 text-xl"></i>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">Crypto-currencies</h3>
            <p class="text-gray-400 mb-4 text-sm">$300B+ market cap with Bitcoin leading. Our Arbitrage Bot automatically trades across 16 exchanges, buying low and selling high for optimal profits.</p>
          </div>
        </div>

        <!-- Commodities Card -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-r from-yellow-600 to-yellow-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-yellow-500 transition-all duration-300">
            <div class="w-12 h-12 bg-yellow-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <i class="fas fa-seedling text-yellow-400 text-xl"></i>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">Commodities</h3>
            <p class="text-gray-400 mb-4 text-sm">Volatile markets like London Metal Exchange and Chicago Trade Board. Our self-learning bots capitalize on opportunities with a diversified, rationed approach.</p>
          </div>
        </div>

        <!-- Precious Metals Card -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-r from-gold-600 to-gold-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-gold-500 transition-all duration-300">
            <div class="w-12 h-12 bg-gold-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <i class="fas fa-gem text-gold-400 text-xl"></i>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">Precious Metals</h3>
            <p class="text-gray-400 mb-4 text-sm">Gold, silver, platinum as hedges against uncertainty. Our experts strategically trade to avoid physical storage risks and maximize returns.</p>
          </div>
        </div>

        <!-- Stocks Card -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-r from-green-600 to-green-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-green-500 transition-all duration-300">
            <div class="w-12 h-12 bg-green-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
              <i class="fas fa-chart-line text-green-400 text-xl"></i>
            </div>
            <h3 class="text-xl font-bold text-white mb-2">Stocks</h3>
            <p class="text-gray-400 mb-4 text-sm">Ownership in corporations for long-term growth. Trade shares on major exchanges with regulated, secure transactions for portfolio diversification.</p>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Cryptocurrency Trading Section -->
  <section class="py-16 bg-gradient-to-b from-dark-400 to-dark-300">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <div class="inline-block px-3 py-1 mb-2 text-xs font-semibold tracking-wider text-secondary uppercase bg-green-900 bg-opacity-30 rounded-full">
          Popular Asset Class
        </div>
        <h2 class="text-3xl font-bold text-white">Cryptocurrency Investment</h2>
        <p class="mt-2 text-gray-400 max-w-2xl mx-auto">Invest the world's most popular digital assets with competitive spreads and advanced tools</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Bitcoin Card -->
        <div class="bg-dark-400 rounded-xl overflow-hidden group hover:shadow-xl transition duration-300 transform hover:-translate-y-1 border border-gray-800">
          <div class="h-24 bg-gradient-to-r from-orange-500 to-yellow-500 flex items-center justify-center">
            <img src="/dash/bitcoin-btc-logo.png" alt="Bitcoin" class="h-16 w-16">
          </div>
          <div class="p-6">
            <h3 class="text-xl font-bold text-white mb-2">Bitcoin</h3>
            <p class="text-gray-400 text-sm h-24 overflow-hidden">
              Bitcoin is a decentralized digital currency without a central bank or administrator, allowing peer-to-peer transactions without intermediaries on the blockchain network.
            </p>
            <div class="mt-4 flex justify-between items-center">
              <RouterLink to="/trade" class="text-primary hover:underline text-sm font-medium flex items-center">
                Invest now
                <i class="fas fa-arrow-right ml-1"></i>
              </RouterLink>
              <span class="text-xs py-1 px-2 bg-gray-800 rounded-full text-gray-300">BTC/USD</span>
            </div>
          </div>
        </div>

        <!-- Ethereum Card -->
        <div class="bg-dark-400 rounded-xl overflow-hidden group hover:shadow-xl transition duration-300 transform hover:-translate-y-1 border border-gray-800">
          <div class="h-24 bg-gradient-to-r from-blue-500 to-indigo-600 flex items-center justify-center">
            <img src="/dash/ethereum-eth-logo.png" alt="Ethereum" class="h-16 w-16">
          </div>
          <div class="p-6">
            <h3 class="text-xl font-bold text-white mb-2">Ethereum</h3>
            <p class="text-gray-400 text-sm h-24 overflow-hidden">
              Ethereum is a decentralized, open-source blockchain with smart contract functionality. After Bitcoin, it is the largest cryptocurrency by market capitalization and most actively used blockchain.
            </p>
            <div class="mt-4 flex justify-between items-center">
              <RouterLink to="/trade" class="text-primary hover:underline text-sm font-medium flex items-center">
                Invest now
                <i class="fas fa-arrow-right ml-1"></i>
              </RouterLink>
              <span class="text-xs py-1 px-2 bg-gray-800 rounded-full text-gray-300">ETH/USD</span>
            </div>
          </div>
        </div>

        <!-- Ripple Card -->
        <div class="bg-dark-400 rounded-xl overflow-hidden group hover:shadow-xl transition duration-300 transform hover:-translate-y-1 border border-gray-800">
          <div class="h-24 bg-gradient-to-r from-blue-500 to-blue-700 flex items-center justify-center">
            <div class="h-16 w-16 bg-gray-900 rounded-full flex items-center justify-center border border-blue-400 shadow-lg">
              <span class="text-blue-400 text-3xl font-bold">XRP</span>
            </div>
          </div>
          <div class="p-6">
            <h3 class="text-xl font-bold text-white mb-2">Ripple</h3>
            <p class="text-gray-400 text-sm h-24 overflow-hidden">
              Ripple is a real-time gross settlement system, currency exchange and remittance network created by Ripple Labs. It uses XRP as its native cryptocurrency to facilitate fast, low-cost international transactions.
            </p>
            <div class="mt-4 flex justify-between items-center">
              <RouterLink to="/trade" class="text-primary hover:underline text-sm font-medium flex items-center">
                Invest now
                <i class="fas fa-arrow-right ml-1"></i>
              </RouterLink>
              <span class="text-xs py-1 px-2 bg-gray-800 rounded-full text-gray-300">XRP/USD</span>
            </div>
          </div>
        </div>

        <!-- Cardano Card -->
        <div class="bg-dark-400 rounded-xl overflow-hidden group hover:shadow-xl transition duration-300 transform hover:-translate-y-1 border border-gray-800">
          <div class="h-24 bg-gradient-to-r from-blue-800 to-indigo-800 flex items-center justify-center">
            <div class="h-16 w-16 bg-gray-900 rounded-full flex items-center justify-center border border-indigo-400 shadow-lg">
              <span class="text-indigo-400 text-3xl font-bold">ADA</span>
            </div>
          </div>
          <div class="p-6">
            <h3 class="text-xl font-bold text-white mb-2">Cardano</h3>
            <p class="text-gray-400 text-sm h-24 overflow-hidden">
              Cardano is a proof-of-stake blockchain platform created by Ethereum co-founder Charles Hoskinson. It aims to be more scalable, sustainable, and interoperable than other blockchain platforms.
            </p>
            <div class="mt-4 flex justify-between items-center">
              <RouterLink to="/trade" class="text-primary hover:underline text-sm font-medium flex items-center">
                Invest now
                <i class="fas fa-arrow-right ml-1"></i>
              </RouterLink>
              <span class="text-xs py-1 px-2 bg-gray-800 rounded-full text-gray-300">ADA/USD</span>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-10 text-center">
        <RouterLink to="/cryptocurrencies" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition duration-150">
          View all cryptocurrencies
          <i class="fas fa-arrow-right ml-2"></i>
        </RouterLink>
      </div>
    </div>
  </section>

  <!--
    NOTE: the source blade file also contains three large marketing sections here --
    "Trading Advantage" (tighter spreads), "Copy Trading", and "Market Analysis & Insights" --
    but they are wrapped in an HTML comment that is never closed correctly, so none of that
    markup has ever actually rendered on the live site. They are intentionally omitted here
    to match current live behavior; see the porting report for details if they should be
    resurrected later.
  -->

  <!-- About Us Section -->
  <section class="py-16 bg-gradient-to-b from-dark-400 to-dark-500 relative">
    <!-- Background overlay with image -->
    <div class="absolute inset-0 opacity-20" style="background-image: url(/temp/custom/img/abt.png); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>

    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10 text-center">
        <span class="inline-block px-3 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Our Story
        </span>
        <h2 class="mt-2 text-3xl font-bold text-white">About Us</h2>
        <div class="h-1 w-20 bg-primary mx-auto my-4"></div>
      </div>

      <div class="max-w-4xl mx-auto bg-dark-400 bg-opacity-80 p-8 rounded-xl border border-gray-800 shadow-xl">
        <p class="text-gray-300 leading-relaxed">
          {{ settingsStore.settings?.siteName }} has become one of the most reputable brokers in the industry, offering traders CFDs across Forex, Equities, Commodities and Futures. Trading on the Forex market is a legitimate and straightforward way of generating income.
        </p>

        <p class="text-gray-300 leading-relaxed mt-4">
          The good news is that you don't have to be a professional trader in order to make money. All you need is the right personality and the right skill set and you can make money trading on foreign exchanges. {{ settingsStore.settings?.siteName }} lets you trade in the way that best suits you.
        </p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <div class="bg-dark-300 bg-opacity-60 p-4 rounded-lg">
            <h3 class="text-white font-semibold flex items-center">
              <i class="fas fa-check-circle text-primary mr-2"></i>
              Personalized Trading
            </h3>
            <p class="text-gray-400 mt-2">
              Do you want to risk a little or a lot? Do you want gains in the short term or are you playing a longer game? Are you a day trader, a swing trader or a scalper?
            </p>
          </div>

          <div class="bg-dark-300 bg-opacity-60 p-4 rounded-lg">
            <h3 class="text-white font-semibold flex items-center">
              <i class="fas fa-check-circle text-primary mr-2"></i>
              Complete Control
            </h3>
            <p class="text-gray-400 mt-2">
              With the right tools, information and access to all the world's currencies, {{ settingsStore.settings?.siteName }} puts you in control of the trades you make.
            </p>
          </div>
        </div>

        <div class="mt-8 text-center">
          <RouterLink to="/about" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary hover:bg-blue-700 transition duration-150">
            Learn more about us
            <i class="fas fa-arrow-right ml-2"></i>
          </RouterLink>
        </div>
      </div>
    </div>
  </section>

  <!-- Expert Support Section -->
  <section class="py-16 bg-dark-400">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">
        <!-- Chart Widget -->
        <div class="bg-dark-300 rounded-xl overflow-hidden border border-gray-800 shadow-lg">
          <div class="w-full h-[400px]">
            <iframe
              src="https://widget.coinlib.io/widget?type=chart&theme=dark&coin_id=859&pref_coin_id=1505"
              scrolling="auto"
              marginwidth="0"
              marginheight="0"
              frameborder="0"
              class="w-full h-full border-0">
            </iframe>
          </div>
          <div class="p-2 text-right text-xs text-green-500">
            <a href="https://coinlib.io" target="_blank" rel="noopener" class="hover:underline">Cryptocurrency Prices by Coinlib</a>
          </div>
        </div>

        <!-- Expert Support Content -->
        <div class="space-y-8">
          <div>
            <h2 class="text-2xl font-bold text-white mb-2 flex items-center">
              <i class="fas fa-headset text-primary mr-3"></i>
              STAY SUPPORTED WITH OUR INVESTMENT EXPERTS
            </h2>
            <div class="h-1 w-20 bg-primary my-4"></div>
            <p class="text-gray-300 leading-relaxed">
              Our local and international teams are here to support you on a 24/5 basis in more than 20 languages, while our wide range of payment methods gives you greater flexibility when it comes to deposits and withdrawals.
            </p>
          </div>

          <div class="bg-dark-300 rounded-lg p-6 border border-gray-800">
            <h2 class="text-xl font-bold text-white mb-3 flex items-center">
              <i class="fas fa-star text-yellow-400 mr-3"></i>
              Experience More Than Investing
            </h2>
            <p class="text-gray-300 leading-relaxed">
              Our success is centered around a number of core values. They include providing competitive investment management fees, ensuring secure and efficient transactions, access to diversified investment portfolios with a wide range of assets, and exceptional customer service.
            </p>

            <div class="mt-6">
              <RouterLink to="/login" class="inline-flex items-center px-5 py-2 border border-gray-700 text-base font-medium rounded-md text-gray-300 hover:text-white hover:border-primary transition duration-150">
                Learn about our fees
                <i class="fas fa-arrow-right ml-2"></i>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Features Section -->
  <section class="py-16 bg-gradient-to-b from-dark-300 to-dark-400">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Platform Features
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">Advanced Investment Tools</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Our platform provides everything you need for successful investing in one powerful interface</p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
        <div class="bg-gray-800 bg-opacity-80 p-6 rounded-xl border border-gray-700 hover:border-blue-500 shadow-lg transition duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-full flex items-center justify-center mb-4 shadow-lg">
            <i class="fas fa-bolt text-white text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Reliable <span class="text-blue-400">Performance</span></h3>
          <p class="text-gray-300">
            Featuring secure and efficient processing, {{ settingsStore.settings?.siteName }} ensures your investments are managed seamlessly without delays or interruptions.
          </p>
        </div>

        <div class="bg-gray-800 bg-opacity-80 p-6 rounded-xl border border-gray-700 hover:border-blue-500 shadow-lg transition duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <div class="w-16 h-16 bg-gradient-to-br from-indigo-600 to-blue-700 rounded-full flex items-center justify-center mb-4 shadow-lg">
            <i class="fas fa-chart-line text-white text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Intelligent <span class="text-indigo-400">Insights</span></h3>
          <p class="text-gray-300">
            Make informed decisions with advanced portfolio analysis tools, real-time market data, and in-platform investment insights from leading financial experts.
          </p>
        </div>

        <div class="bg-gray-800 bg-opacity-80 p-6 rounded-xl border border-gray-700 hover:border-blue-500 shadow-lg transition duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-cyan-600 rounded-full flex items-center justify-center mb-4 shadow-lg">
            <i class="fas fa-search-dollar text-white text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Transparent <span class="text-cyan-400">Reporting</span></h3>
          <p class="text-gray-300">
            Access detailed portfolio performance reports, asset allocation charts, and comprehensive investment history for a clear understanding of your returns.
          </p>
        </div>

        <div class="bg-gray-800 bg-opacity-80 p-6 rounded-xl border border-gray-700 hover:border-blue-500 shadow-lg transition duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <div class="w-16 h-16 bg-gradient-to-br from-teal-600 to-green-700 rounded-full flex items-center justify-center mb-4 shadow-lg">
            <i class="fas fa-desktop text-white text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-3">Intuitive <span class="text-teal-400">Interface</span></h3>
          <p class="text-gray-300">
            Easy to use and navigate, {{ settingsStore.settings?.siteName }} was built with real investors' needs in mind. Manage your investments with a platform designed for simplicity and efficiency.
          </p>
        </div>
      </div>
    </div>
  </section>

  <!-- Trusted Brand Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Global Trust
        </span>
        <h2 class="mt-3 text-3xl font-bold text-white">Why <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600">{{ settingsStore.settings?.siteName }}</span> Is One of the World's Most Trusted Investment Platforms</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Experience the reliability and security that our global clients have come to trust</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
        <!-- Investment Insights Widget Container -->
        <div class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700 shadow-xl transform transition duration-500 hover:shadow-blue-900/20">
          <div class="p-4 bg-gray-800 border-b border-gray-700">
            <h3 class="text-xl font-semibold text-white">Real-Time Investment Insights</h3>
          </div>
          <!-- TradingView Widget BEGIN -->
          <div id="forex_heat_map" class="w-full h-0 bg-gray-800"></div>
          <div class="py-2 px-4 text-right text-xs text-blue-400 bg-gray-800">
            <a href="https://www.tradingview.com/markets/currencies/forex-heat-map/" rel="noopener" target="_blank" class="hover:underline">Market Insights by TradingView</a>
          </div>
          <!-- TradingView Widget END -->
        </div>

        <!-- Trust Features -->
        <div class="bg-gray-800 rounded-xl overflow-hidden border border-gray-700 shadow-xl transition duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <div class="p-6">
            <h3 class="text-xl font-bold text-white mb-6 border-b border-gray-700 pb-3">Our Trusted Reputation</h3>
            <ul class="space-y-5">
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <span class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 bg-opacity-20">
                    <i class="fas fa-check text-blue-400"></i>
                  </span>
                </div>
                <div class="ml-4">
                  <h4 class="text-white font-medium">Globally Regulated</h4>
                  <p class="text-gray-400 text-sm mt-1">Operating under strict financial regulations to ensure maximum security for your investments</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <span class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 bg-opacity-20">
                    <i class="fas fa-trophy text-blue-400"></i>
                  </span>
                </div>
                <div class="ml-4">
                  <h4 class="text-white font-medium">40+ International Awards</h4>
                  <p class="text-gray-400 text-sm mt-1">Recognition for excellence in investment services, platform technology, and customer support</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <span class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 bg-opacity-20">
                    <i class="fas fa-headset text-blue-400"></i>
                  </span>
                </div>
                <div class="ml-4">
                  <h4 class="text-white font-medium">24/7 Multilingual Support</h4>
                  <p class="text-gray-400 text-sm mt-1">Expert assistance available around the clock in multiple languages</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <span class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 bg-opacity-20">
                    <i class="fas fa-shield-alt text-blue-400"></i>
                  </span>
                </div>
                <div class="ml-4">
                  <h4 class="text-white font-medium">Segregated Client Funds</h4>
                  <p class="text-gray-400 text-sm mt-1">Your investments are kept in separate accounts for maximum security</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <span class="flex items-center justify-center w-8 h-8 rounded-full bg-blue-600 bg-opacity-20">
                    <i class="fas fa-user-tie text-blue-400"></i>
                  </span>
                </div>
                <div class="ml-4">
                  <h4 class="text-white font-medium">Personal Investment Advisors</h4>
                  <p class="text-gray-400 text-sm mt-1">Dedicated professionals to guide your investment journey</p>
                </div>
              </li>
            </ul>
            <div class="mt-8 text-center">
              <RouterLink to="/login" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
                Learn More
                <svg class="ml-2 -mr-1 w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- How It Works Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Simple Process
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600">How It Works</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Get started with investing in three simple steps</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- Step 1 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/20 rounded-xl transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-gray-800 bg-opacity-90 rounded-xl p-8 shadow-lg border border-gray-700 group-hover:border-blue-600 transform transition-all duration-500 hover:shadow-lg hover:shadow-blue-900/20 hover:-translate-y-2 h-full">
            <div class="flex justify-center mb-6">
              <div class="w-20 h-20 bg-blue-600 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg">1</div>
            </div>
            <h3 class="text-xl font-bold text-center text-white mb-3">Deposit</h3>
            <div class="flex justify-center mb-4">
              <div class="h-1 w-20 bg-blue-500 mx-auto"></div>
            </div>
            <p class="text-gray-300 text-center mb-8">
              Open a real account and add funds. We work with more than 20 payment systems for your convenience.
            </p>
            <div class="mt-auto text-center">
              <RouterLink to="/register" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
                Get Started
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>

        <!-- Step 2 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/20 rounded-xl transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-gray-800 bg-opacity-90 rounded-xl p-8 shadow-lg border border-gray-700 group-hover:border-blue-600 transform transition-all duration-500 hover:shadow-lg hover:shadow-blue-900/20 hover:-translate-y-2 h-full">
            <div class="flex justify-center mb-6">
              <div class="w-20 h-20 bg-blue-600 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg">2</div>
            </div>
            <h3 class="text-xl font-bold text-center text-white mb-3">Invest</h3>
            <div class="flex justify-center mb-4">
              <div class="h-1 w-20 bg-blue-500 mx-auto"></div>
            </div>
            <p class="text-gray-300 text-center mb-8">
              Invest in a diversified portfolio of assets. Use our analysis tools and expert insights to optimize your investment strategy.
            </p>
            <div class="mt-auto text-center">
              <RouterLink to="/login" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
                Explore Opportunities
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>

        <!-- Step 3 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/20 rounded-xl transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
          <div class="relative bg-gray-800 bg-opacity-90 rounded-xl p-8 shadow-lg border border-gray-700 group-hover:border-blue-600 transform transition-all duration-500 hover:shadow-lg hover:shadow-blue-900/20 hover:-translate-y-2 h-full">
            <div class="flex justify-center mb-6">
              <div class="w-20 h-20 bg-blue-600 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg">3</div>
            </div>
            <h3 class="text-xl font-bold text-center text-white mb-3">Withdraw</h3>
            <div class="flex justify-center mb-4">
              <div class="h-1 w-20 bg-blue-500 mx-auto"></div>
            </div>
            <p class="text-gray-300 text-center mb-8">
              Access your returns easily with our fast and secure withdrawal process to your bank card or e-wallet.
            </p>
            <div class="mt-auto text-center">
              <RouterLink to="/login" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
                Learn More
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Premium Investment Experience Section -->
  <section class="py-16 bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Freedom to Invest
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600">Invest What You Want, When You Want</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Experience unlimited investment possibilities with our comprehensive platform</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
        <!-- Left: Image Column -->
        <div class="relative group overflow-hidden rounded-xl shadow-2xl">
          <div class="absolute inset-0 bg-gradient-to-r from-blue-600/20 to-blue-900/20 rounded-xl transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-70"></div>
          <img :src="'/temp/custom/img/commission-scheme-crypt1t1.png'" alt="Investment Fee Structure" class="relative z-10 rounded-xl w-full transform transition-all duration-500 group-hover:scale-105">
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-gray-900 p-4">
            <p class="text-sm text-blue-300 text-center">Competitive fee structure for all investors</p>
          </div>
        </div>

        <!-- Right: Content Column -->
        <div class="bg-gray-800 bg-opacity-80 rounded-xl border border-gray-700 hover:border-blue-600 p-8 shadow-xl transition-all duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20">
          <h3 class="text-xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600 mb-4">Premium Investment Experience</h3>
          <p class="text-gray-300 mb-6 leading-relaxed">
            One of the primary goals of {{ settingsStore.settings?.siteName }} is to provide the best investment platform in the market. Our relationships with leading financial institutions ensure diversified portfolios and competitive returns for investors.
          </p>
          <div class="space-y-4 mb-6">
            <div class="flex items-start">
              <div class="flex-shrink-0 mt-1">
                <span class="flex items-center justify-center w-6 h-6 rounded-full bg-blue-600 bg-opacity-20">
                  <i class="fas fa-check text-blue-400 text-sm"></i>
                </span>
              </div>
              <p class="ml-3 text-gray-300">Invest in Equities, Bonds, ETFs &amp; Commodities</p>
            </div>
            <div class="flex items-start">
              <div class="flex-shrink-0 mt-1">
                <span class="flex items-center justify-center w-6 h-6 rounded-full bg-blue-600 bg-opacity-20">
                  <i class="fas fa-check text-blue-400 text-sm"></i>
                </span>
              </div>
              <p class="ml-3 text-gray-300">Access global markets 24 hours / 7 days</p>
            </div>
            <div class="flex items-start">
              <div class="flex-shrink-0 mt-1">
                <span class="flex items-center justify-center w-6 h-6 rounded-full bg-blue-600 bg-opacity-20">
                  <i class="fas fa-check text-blue-400 text-sm"></i>
                </span>
              </div>
              <p class="ml-3 text-gray-300">Multilingual customer support</p>
            </div>
            <div class="flex items-start">
              <div class="flex-shrink-0 mt-1">
                <span class="flex items-center justify-center w-6 h-6 rounded-full bg-blue-600 bg-opacity-20">
                  <i class="fas fa-check text-blue-400 text-sm"></i>
                </span>
              </div>
              <p class="ml-3 text-gray-300">Manage investments on our mobile apps</p>
            </div>
          </div>
          <div class="mt-6">
            <RouterLink to="/login" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
              Learn About Our Fees
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Investment Services Section -->
  <section class="py-16 bg-gray-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Comprehensive Services
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600">Start Investing with {{ settingsStore.settings?.siteName }}</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Everything you need for successful investing in one platform</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- Market Analysis Card -->
        <div class="bg-gray-800 bg-opacity-80 rounded-xl border border-gray-700 hover:border-blue-600 p-6 shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20 h-full">
          <div class="w-14 h-14 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
            <i class="fas fa-chart-line text-blue-400 text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-4">Investment Research</h3>
          <ul class="space-y-3 text-gray-300">
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Daily Market Insights</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Weekly Investment Webinars</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Live Q &amp; A Sessions</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Investment Strategy Support</span>
            </li>
          </ul>
        </div>

        <!-- Award-Winning Platform Card -->
        <div class="bg-gray-800 bg-opacity-80 rounded-xl border border-gray-700 hover:border-blue-600 p-6 shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20 h-full">
          <div class="w-14 h-14 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
            <i class="fas fa-trophy text-blue-400 text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-4">Award-Winning Platform</h3>
          <ul class="space-y-3 text-gray-300">
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>42+ Industry Awards</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Top 100 Financial Platforms</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Best Client Funds Security Global</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Best Investment Research Provider</span>
            </li>
          </ul>
        </div>

        <!-- Investment Options Card -->
        <div class="bg-gray-800 bg-opacity-80 rounded-xl border border-gray-700 hover:border-blue-600 p-6 shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-blue-900/20 h-full">
          <div class="w-14 h-14 bg-blue-600 bg-opacity-20 rounded-full flex items-center justify-center mb-4">
            <i class="fas fa-hand-holding-usd text-blue-400 text-xl"></i>
          </div>
          <h3 class="text-xl font-bold text-white mb-4">Investment Options</h3>
          <ul class="space-y-3 text-gray-300">
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Portfolio Copy - How it Works</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Become a Follower</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>PAMM Ranking</span>
            </li>
            <li class="flex items-start">
              <span class="flex-shrink-0 text-blue-400 mr-2">&bull;</span>
              <span>Become an Investor</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="mt-12 text-center">
        <RouterLink to="/register" class="inline-flex items-center px-8 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 shadow-lg transition duration-300 transform hover:-translate-y-1">
          Start Investing Now
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </RouterLink>
      </div>
    </div>
  </section>

  <!-- Client Testimonials Section -->
  <section class="py-16 bg-gray-900 relative overflow-hidden">
    <!-- Background Effects -->
    <div class="absolute inset-0">
      <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5"></div>
      <!-- Animated Grid Pattern -->
      <div class="absolute inset-0 opacity-10">
        <svg class="h-full w-full" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="testimonial-grid" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M0 40V0h40" fill="none" stroke="currentColor" stroke-width="0.5"/>
              <circle cx="20" cy="20" r="1" fill="currentColor"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#testimonial-grid)"/>
        </svg>
      </div>
    </div>

    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Section Header -->
      <div class="mb-12 text-center">
        <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
          Success Stories
        </span>
        <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-400">Client Testimonials</h2>
        <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Hear from our satisfied clients who have achieved impressive results with our platform</p>
      </div>

      <!-- Testimonials Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Testimonial 1 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-br from-blue-500/20 to-emerald-400/20 rounded-2xl filter blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-300"></div>
          <div class="relative h-full bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg v-for="n in 5" :key="n" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <svg class="w-8 h-8 text-blue-400 ml-auto opacity-20" fill="currentColor" viewBox="0 0 32 32">
                  <path d="M10 8c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4zm12 0c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4z"/>
                </svg>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">Since I started using {{ settingsStore.settings?.siteName }}, I have been earning like never before. You guys have the best signals.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/imge2.jpg'" alt="Malcom47" class="w-10 h-10 rounded-full object-cover border-2 border-blue-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Malcom47</h4>
                <p class="text-blue-400 text-xs">Verified Trader</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Testimonial 2 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/20 to-blue-400/20 rounded-2xl filter blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-300"></div>
          <div class="relative h-full bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg v-for="n in 5" :key="n" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <svg class="w-8 h-8 text-emerald-400 ml-auto opacity-20" fill="currentColor" viewBox="0 0 32 32">
                  <path d="M10 8c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4zm12 0c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4z"/>
                </svg>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I already got more than $200,000 within a month investing with {{ settingsStore.settings?.siteName }}. Will again invest soon.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/imge1.jpg'" alt="Christy" class="w-10 h-10 rounded-full object-cover border-2 border-emerald-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Christy</h4>
                <p class="text-emerald-400 text-xs">Elite Investor</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Testimonial 3 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-br from-blue-500/20 to-emerald-400/20 rounded-2xl filter blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-300"></div>
          <div class="relative h-full bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg v-for="n in 5" :key="n" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <svg class="w-8 h-8 text-blue-400 ml-auto opacity-20" fill="currentColor" viewBox="0 0 32 32">
                  <path d="M10 8c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4zm12 0c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4z"/>
                </svg>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I was able to earn additional $30,000 to my profit. It's amazing, you guys are the best, keep it up.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/imge3.jpg'" alt="Linday8" class="w-10 h-10 rounded-full object-cover border-2 border-blue-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Linday8</h4>
                <p class="text-blue-400 text-xs">Professional Trader</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Testimonial 4 -->
        <div class="relative group">
          <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/20 to-blue-400/20 rounded-2xl filter blur-xl opacity-0 group-hover:opacity-75 transition-opacity duration-300"></div>
          <div class="relative h-full bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg v-for="n in 5" :key="n" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <svg class="w-8 h-8 text-emerald-400 ml-auto opacity-20" fill="currentColor" viewBox="0 0 32 32">
                  <path d="M10 8c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4zm12 0c-3.3 0-6 2.7-6 6v10h6V14h-4c0-2.2 1.8-4 4-4z"/>
                </svg>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">This was a very easy process and I received my funds quickly as I needed them! Highly recommend {{ settingsStore.settings?.siteName }}.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/imge4.jpg'" alt="Crian" class="w-10 h-10 rounded-full object-cover border-2 border-emerald-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Crian</h4>
                <p class="text-emerald-400 text-xs">Active Trader</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Additional testimonials collapsed section -->
      <div class="mt-8">
        <div v-if="showMoreTestimonials" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
          <!-- Testimonial 5 -->
          <div class="relative bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <!-- Source only authored one star icon for testimonials 5-8 ("More stars..." placeholder comment); ported verbatim. -->
                </div>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I rate {{ settingsStore.settings?.siteName }} five stars because of the service, you register online, upload ID and you deposit and withdraw after trades. This is so lovely.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/imge5.jpg'" alt="Claudia" class="w-10 h-10 rounded-full object-cover border-2 border-blue-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Claudia</h4>
                <p class="text-blue-400 text-xs">Satisfied Investor</p>
              </div>
            </div>
          </div>

          <!-- Testimonial 6 -->
          <div class="relative bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I am very pleased with the customer service. Also online service is great and easy thank you {{ settingsStore.settings?.siteName }} team.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/jenny.jpg'" alt="Jenny" class="w-10 h-10 rounded-full object-cover border-2 border-emerald-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Jenny</h4>
                <p class="text-emerald-400 text-xs">Premium Member</p>
              </div>
            </div>
          </div>

          <!-- Testimonial 7 -->
          <div class="relative bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I'm happy, that in difficult times there are people that will support you and help you make more money, thank you {{ settingsStore.settings?.siteName }} for giving me a chance.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/mike.jpg'" alt="Mike" class="w-10 h-10 rounded-full object-cover border-2 border-blue-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Mike</h4>
                <p class="text-blue-400 text-xs">Regular Investor</p>
              </div>
            </div>
          </div>

          <!-- Testimonial 8 -->
          <div class="relative bg-gray-800 bg-opacity-80 rounded-2xl p-6 border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
            <div class="mb-6">
              <div class="flex items-center mb-4">
                <div class="flex text-yellow-400">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
              </div>
              <p class="text-gray-300 leading-relaxed text-sm">I've invested with {{ settingsStore.settings?.siteName }} several times, always paid back on time. The entire trade process is complete in just a few days. Very impressed and satisfied.</p>
            </div>
            <div class="flex items-center">
              <img :src="'/temp/custom/kathy.jpg'" alt="Kathy" class="w-10 h-10 rounded-full object-cover border-2 border-emerald-500">
              <div class="ml-3">
                <h4 class="text-white font-semibold text-sm">Kathy</h4>
                <p class="text-emerald-400 text-xs">Long-term Client</p>
              </div>
            </div>
          </div>
        </div>

        <!-- View More Button -->
        <div class="text-center mt-12">
          <button
            type="button"
            @click="showMoreTestimonials = !showMoreTestimonials"
            class="inline-flex items-center px-6 py-3 rounded-xl bg-gradient-to-r from-blue-500 to-emerald-400 text-white font-medium transition-transform duration-300 transform hover:scale-105 hover:shadow-lg hover:shadow-blue-500/25"
          >
            <span>{{ showMoreTestimonials ? 'Show Less' : 'View More Success Stories' }}</span>
            <svg
              class="w-5 h-5 ml-2 transition-transform duration-300"
              :class="{ 'rotate-180': showMoreTestimonials }"
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </section>

  <!-- Cryptocurrency Logos Section - Dark Mode -->
  <section class="bg-gray-900 py-10">
    <div class="container mx-auto px-4">
      <div class="max-w-7xl mx-auto">
        <div class="flex flex-wrap justify-center items-center gap-8 md:gap-12">
          <!-- Bitcoin -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/btc.png'" alt="Bitcoin" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Ethereum -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/eth.png'" alt="Ethereum" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Dogecoin -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/doge.png'" alt="Dogecoin" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Bitcoin Cash -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/bch.png'" alt="Bitcoin Cash" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Tether -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/usdt.png'" alt="Tether" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Binance Coin -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/bnb.png'" alt="Binance Coin" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>

          <!-- Litecoin -->
          <div class="w-1/4 sm:w-1/6 md:w-1/7 transform hover:scale-110 transition-transform duration-300">
            <div class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-4 rounded-xl border border-gray-700 hover:border-blue-500">
              <img :src="'/temp/custom/img/ltc.png'" alt="Litecoin" class="w-full h-auto filter drop-shadow-lg">
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
