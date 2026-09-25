<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { usePublicSettingsStore } from '@/stores/publicSettings'

// Investment (marketing) page — ported from resources/views/home/trade.blade.php.
// This is the public "Investment Plans" marketing page, not the logged-in
// dashboard trading module.
//
// Skipped/adapted vs. the source:
// - The @foreach($plans as $plan) pricing-card loop has no hardcoded fallback
//   data in the blade file (plans are fetched server-side), and there is no
//   public plans API/store in the SPA yet, so the plan cards themselves are
//   omitted per the porting rules. The surrounding "Investment Opportunities"
//   header and "Need a Custom Plan?" CTA box are kept.
// - The JSON-LD BreadcrumbList <script type="application/ld+json"> block is
//   SEO metadata (not visual content) and depended on $settings->site_address,
//   which has no equivalent in the SPA settings store yet, so it is skipped.
// - "Explore Investment Resources" / "Join Free Webinars" links pointed at
//   relative routes ("investment-resources", "webinars") that don't exist
//   anywhere in the Laravel app or the new router, so they're kept as inert
//   anchors (href="#") rather than fabricating routes.
const settingsStore = usePublicSettingsStore()
const chartCanvas = ref(null)
let chartInstance = null

onMounted(async () => {
  await settingsStore.ensureLoaded()
  loadChart()
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }
})

function loadChart() {
  if (window.Chart) {
    renderChart()
    return
  }
  const script = document.createElement('script')
  script.src = 'https://cdn.jsdelivr.net/npm/chart.js'
  script.async = true
  script.onload = renderChart
  document.head.appendChild(script)
}

function renderChart() {
  if (!chartCanvas.value || !window.Chart) return
  const ctx = chartCanvas.value.getContext('2d')
  chartInstance = new window.Chart(ctx, {
    type: 'pie',
    data: {
      labels: ['Foreign Exchange', 'Cryptocurrency', 'Commodities', 'Precious Metals', 'Stocks'],
      datasets: [{
        data: [45, 30, 15, 5.5, 4.5],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)', // Blue for Forex
          'rgba(16, 185, 129, 0.8)', // Green for Crypto
          'rgba(234, 179, 8, 0.8)', // Yellow for Commodities
          'rgba(168, 85, 247, 0.8)', // Purple for Precious Metals
          'rgba(239, 68, 68, 0.8)', // Red for Stocks
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
        legend: {
          position: 'bottom',
          labels: {
            color: '#ffffff',
            font: { size: 14 },
          },
        },
        tooltip: {
          callbacks: {
            label: (context) => `${context.label}: ${context.raw}%`,
          },
        },
      },
    },
  })
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative bg-gray-900 overflow-hidden">
      <!-- Dynamic Background -->
      <div class="absolute inset-0">
        <div class="absolute inset-0 bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
          <!-- Grid Pattern -->
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
        <!-- Accent Elements -->
        <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-500/20 rounded-full filter blur-3xl"></div>
        <div class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-emerald-400/20 rounded-full filter blur-3xl"></div>
      </div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-12 relative z-10">
        <div class="max-w-4xl">
          <!-- Header Content -->
          <div class="space-y-6">
            <div class="inline-block px-4 py-1 rounded-full bg-blue-500/10 border border-blue-500/20">
              <p class="text-sm font-medium text-blue-400">House Investment Plans</p>
            </div>
            <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-white leading-tight">
              {{ settingsStore.settings?.siteName }} <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">INVEST</span>
            </h1>

            <!-- Breadcrumb -->
            <nav class="flex" aria-label="Breadcrumb">
              <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li class="inline-flex items-center">
                  <RouterLink to="/" class="text-gray-400 hover:text-white transition-colors flex items-center">
                    <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/>
                    </svg>
                    {{ settingsStore.settings?.siteName }}
                  </RouterLink>
                </li>
                <li>
                  <div class="flex items-center">
                    <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="text-gray-400 ml-1 md:ml-2">Invest</span>
                  </div>
                </li>
                <li>
                  <div class="flex items-center">
                    <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="text-gray-300 ml-1 md:ml-2">Plans</span>
                  </div>
                </li>
              </ol>
            </nav>
          </div>
        </div>
      </div>
    </section>

    <!-- Investment Plan Description Section -->
    <section class="py-16 bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-4xl mx-auto text-center">
          <h2 class="text-3xl font-bold text-white mb-6">Why Invest with {{ settingsStore.settings?.siteName }}?</h2>
          <p class="text-gray-300 leading-relaxed">
            Unlike non-standard investment strategies, the general management of investors' capital globalizes the process of making profit and creating benefits immediately for all program participants. Our company, in this case, serves as one of the major players in the market. The total capital allows us to earn large interests on the personal deposits of our clients. Part of this profit is the company's income hence our traders work to increase their income on deposits because their profit directly depends on profitability of investors.
          </p>
        </div>
      </div>
    </section>

    <!-- Pricing Plans Section -->
    <section class="py-16 bg-gray-900" id="pricing">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="mb-12 text-center">
          <span class="inline-block px-4 py-1 text-sm font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-70 rounded-full shadow-lg">
            Investment Plans
          </span>
          <h2 class="mt-3 text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">Investment Opportunities</h2>
          <p class="mt-3 text-gray-300 max-w-2xl mx-auto">Choose the perfect plan that suits your investment strategy and financial goals.</p>
        </div>

        <!-- NOTE: the live plan cards (@foreach($plans as $plan) in the source) require a
             public investment-plans API that doesn't exist in the SPA backend yet, and the
             blade file has no hardcoded fallback data, so the cards are intentionally omitted
             here pending that endpoint. -->

        <!-- Additional Info -->
        <div class="mt-12 p-6 bg-gray-800 bg-opacity-70 rounded-xl border border-gray-700 shadow-lg">
          <div class="flex flex-col md:flex-row items-center justify-between">
            <div class="md:w-3/5 mb-6 md:mb-0">
              <h3 class="text-xl font-bold text-white mb-3">Need a Custom Plan?</h3>
              <p class="text-gray-300">Our team can create tailored investment solutions for institutional clients and high-net-worth individuals.</p>
            </div>
            <div>
              <RouterLink to="/contact" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-gradient-to-r from-blue-600 to-emerald-500 hover:from-blue-500 hover:to-emerald-600 shadow-lg transition duration-300 transform hover:-translate-y-1">
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

    <section class="py-16 bg-dark-400">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <!-- First Row: Chart and Text -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          <!-- Chart Column -->
          <div class="rounded-xl overflow-hidden shadow-lg bg-dark-300 p-6">
            <h3 class="text-xl font-semibold text-white mb-4">Investment Distribution</h3>
            <canvas ref="chartCanvas" class="w-full h-[300px]"></canvas>
          </div>
          <!-- Text Column -->
          <div class="space-y-6">
            <div>
              <span class="text-primary text-sm font-semibold uppercase tracking-wider">Investment Education</span>
              <h2 class="mt-2 text-3xl font-bold text-white">Why Invest With Velnora Partners</h2>
            </div>
            <p class="text-gray-300 text-lg">
              Discover the power of diversified investing with our AI-driven strategies and expert-managed portfolios, designed to maximize returns across global markets.
            </p>
            <div class="bg-dark-300 rounded-lg p-6 border border-gray-800">
              <h3 class="text-xl font-semibold text-white mb-3">Why Choose Us</h3>
              <p class="text-gray-400">
                At Velnora Partners, we leverage cutting-edge AI technology and expert analysis to offer secure, transparent, and profitable investment opportunities. Our diversified portfolios span forex, cryptocurrencies, commodities, precious metals, and stocks, ensuring stability and growth. With a focus on user-friendly platforms, competitive conditions, and robust security, we empower investors to achieve their financial goals with confidence.
              </p>
            </div>
            <div class="flex flex-wrap gap-4 mt-4">
              <a href="#" class="inline-flex items-center px-5 py-2 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition duration-150">
                Explore Investment Resources
                <i class="fas fa-graduation-cap ml-2"></i>
              </a>
              <a href="#" class="inline-flex items-center px-5 py-2 border border-gray-700 text-base font-medium rounded-md text-gray-300 hover:text-white hover:border-gray-500 transition duration-150">
                Join Free Webinars
                <i class="fas fa-video ml-2"></i>
              </a>
            </div>
          </div>
        </div>
        <!-- Second Row: Videos -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <!-- Video Card 1 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-blue-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
            <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-blue-500 transition-all duration-300">
              <h3 class="text-lg font-semibold text-white mb-3">What is Bitcoin</h3>
              <div class="rounded-xl overflow-hidden shadow-lg">
                <iframe
                  src="https://www.youtube.com/embed/Gc2en3nHxA4"
                  allow="autoplay; encrypted-media"
                  allowfullscreen
                  class="w-full h-[150px] object-cover"
                  title="What is Bitcoin">
                </iframe>
              </div>
            </div>
          </div>
          <!-- Video Card 2 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-r from-green-600 to-green-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
            <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-green-500 transition-all duration-300">
              <h3 class="text-lg font-semibold text-white mb-3">Importance of the USD</h3>
              <div class="rounded-xl overflow-hidden shadow-lg">
                <iframe
                  src="https://www.youtube.com/embed/NDs9rymuhRs"
                  allow="autoplay; encrypted-media"
                  allowfullscreen
                  class="w-full h-[150px] object-cover"
                  title="Importance of USD at Velnora Partners">
                </iframe>
              </div>
            </div>
          </div>
          <!-- Video Card 3 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-r from-purple-600 to-purple-400 rounded-lg transform rotate-1 group-hover:rotate-0 transition-all duration-300 opacity-50"></div>
            <div class="relative bg-dark-400 p-6 rounded-lg border border-gray-700 group-hover:border-purple-500 transition-all duration-300">
              <h3 class="text-lg font-semibold text-white mb-3">Why Choose Us</h3>
              <div class="rounded-xl overflow-hidden shadow-lg">
                <iframe
                  src="https://www.youtube.com/embed/RijeGo6apU4?si=0vI_DvfQU2QxwjTF"
                  allow="autoplay; encrypted-media"
                  allowfullscreen
                  class="w-full h-[150px] object-cover"
                  title="Why You Should Choose Us">
                </iframe>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Expert Support Section -->
    <section class="py-16 bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid lg:grid-cols-2 gap-12 items-center">
          <!-- Left Column - Image -->
          <div class="relative">
            <div class="relative">
              <!-- Decorative Elements -->
              <div class="absolute -inset-4">
                <div class="w-full h-full mx-auto opacity-30 blur-lg filter">
                  <div class="absolute inset-0 bg-gradient-to-r from-blue-600 to-emerald-500 rounded-3xl"></div>
                </div>
              </div>
              <img src="/temp/custom/img/commission-scheme-crypt1d.png" alt="Investment Plan" class="relative rounded-xl shadow-2xl w-full">
            </div>
          </div>

          <!-- Right Column - Content -->
          <div class="space-y-10">
            <!-- Expert Support -->
            <div class="bg-gray-800 rounded-xl p-8 border border-gray-700 hover:border-blue-500/50 transition-all duration-300">
              <h2 class="text-2xl font-bold text-white mb-4 flex items-center">
                <svg class="w-6 h-6 text-blue-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                </svg>
                Expert Guidance
              </h2>
              <p class="text-gray-300 leading-relaxed">
                Our dedicated team of investment professionals is available 24/7 to guide you through your investment journey, offering personalized support in multiple languages.
              </p>
            </div>

            <!-- Investment Experience -->
            <div class="bg-gray-800 rounded-xl p-8 border border-gray-700 hover:border-emerald-500/50 transition-all duration-300">
              <h2 class="text-2xl font-bold text-white mb-4 flex items-center">
                <svg class="w-6 h-6 text-emerald-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
                Maximize Your Wealth
              </h2>
              <p class="text-gray-300 leading-relaxed">
                We focus on delivering high returns through diversified investment strategies, ensuring competitive returns, secure transactions, and exceptional customer service.
              </p>
            </div>

            <!-- CTA Button -->
            <div class="pt-4">
              <RouterLink to="/about" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-lg text-white bg-gradient-to-r from-blue-600 to-emerald-500 hover:from-blue-700 hover:to-emerald-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-300 shadow-lg hover:shadow-xl" :title="`Learn About ${settingsStore.settings?.siteName} Investment Plans`">
                Learn More About Our Services
                <svg class="ml-2 -mr-1 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/>
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
