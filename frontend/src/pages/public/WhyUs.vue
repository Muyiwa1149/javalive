<script setup>
// Our Services — ported from resources/views/home/whyus.blade.php
// The source only used $settings-> for the breadcrumb Home link (now a
// RouterLink) and a JSON-LD BreadcrumbList block; the JSON-LD SEO metadata
// is skipped (no visual content, and no $settings->site_address equivalent
// in the SPA settings store). Alpine's `x-data="{ hover: false }"` on the
// cards was unused (hover visuals are pure Tailwind `group`/`group-hover:`
// CSS), so it is dropped rather than ported.
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative bg-gray-900 overflow-hidden">
      <!-- Dynamic Background -->
      <div class="absolute inset-0">
        <div class="absolute inset-0 bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
          <!-- Animated Grid Pattern -->
          <div class="absolute inset-0 opacity-10">
            <svg class="h-full w-full" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <pattern id="services-grid" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                  <path d="M0 40V0h40" fill="none" stroke="currentColor" stroke-width="0.5"/>
                  <circle cx="20" cy="20" r="1" fill="currentColor"/>
                </pattern>
              </defs>
              <rect width="100%" height="100%" fill="url(#services-grid)"/>
            </svg>
          </div>
        </div>
        <!-- Glowing Accents -->
        <div class="absolute top-1/4 right-1/4 w-96 h-96 bg-blue-500/20 rounded-full filter blur-3xl"></div>
        <div class="absolute bottom-1/4 left-1/4 w-96 h-96 bg-emerald-400/20 rounded-full filter blur-3xl"></div>
      </div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16 relative z-10">
        <div class="max-w-4xl">
          <!-- Header Content -->
          <div class="space-y-6">
            <div class="inline-block px-4 py-1 rounded-full bg-blue-500/10 border border-blue-500/20">
              <p class="text-sm font-medium text-blue-400">Our Offerings</p>
            </div>
            <h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-white leading-tight">
              Discover <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">Our Services</span>
            </h1>

            <!-- Breadcrumb -->
            <nav class="flex" aria-label="Breadcrumb">
              <ol class="inline-flex items-center space-x-1 md:space-x-3">
                <li class="inline-flex items-center">
                  <RouterLink to="/" class="text-gray-400 hover:text-white transition-colors flex items-center">
                    <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"/>
                    </svg>
                    Home
                  </RouterLink>
                </li>
                <li>
                  <div class="flex items-center">
                    <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="text-gray-400 ml-1 md:ml-2">Company</span>
                  </div>
                </li>
                <li>
                  <div class="flex items-center">
                    <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                    </svg>
                    <span class="text-gray-300 ml-1 md:ml-2">Our Services</span>
                  </div>
                </li>
              </ol>
            </nav>
          </div>
        </div>
      </div>
    </section>

    <!-- Services Grid Section -->
    <section class="py-16 bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <!-- Commodities -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-blue-500/50">

                <!-- Icon -->
                <div class="w-16 h-16 mx-auto mb-6 bg-blue-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"/>
                  </svg>
                </div>

                <h3 class="text-xl font-bold text-white mb-4">Commodities</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    The <span class="font-semibold text-blue-400">Commodities Market</span> dates back to the 17th century in Chicago.
                    Key markets include the <span class="font-semibold">London Metal Exchange</span>, the
                    <span class="font-semibold">Chicago Trade Board</span>, and the
                    <span class="font-semibold">New York Mercantile Exchange</span>.
                  </p>
                  <p>
                    Unlike Forex, commodities involve <span class="font-semibold">tangible assets</span> you can actually buy.
                    The market is <span class="font-semibold text-blue-400">highly volatile</span>, offering strong profit potential with significant trading volumes.
                  </p>
                  <p>
                    <span class="font-semibold text-blue-400">Velnora Partners</span> expands its portfolio into this market with
                    self-learning and diversified bots, designed to capitalize on volatility while following a strategic, rationed approach.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Cryptocurrencies -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-yellow-500/5 to-pink-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-yellow-500/50">

                <div class="w-16 h-16 mx-auto mb-6 bg-yellow-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 8c-1.657 0-3 .672-3 1.5s1.343 1.5 3 1.5 3 .672 3 1.5-1.343 1.5-3 1.5m0-7c1.657 0 3 .672 3 1.5S13.657 9 12 9m0 7v2m0-18C6.477 0 2 4.477 2 10c0 4.73 3.327 8.64 7.688 9.707A2.996 2.996 0 0012 22a2.996 2.996 0 002.312-2.293C18.673 18.64 22 14.73 22 10c0-5.523-4.477-10-10-10z"/>
                  </svg>
                </div>

                <h3 class="text-xl font-bold text-white mb-4">Cryptocurrencies</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    <span class="font-semibold text-yellow-400">Cryptocurrency</span> is a digital or virtual form of currency that uses
                    cryptography for security and operates on decentralized blockchain networks.
                  </p>
                  <p>
                    The most popular is <span class="font-semibold">Bitcoin</span>, introduced in 2009. Others include
                    <span class="font-semibold">Ethereum</span>, <span class="font-semibold">Litecoin</span>, and <span class="font-semibold">Ripple</span>.
                  </p>
                  <p>
                    <span class="font-semibold text-yellow-400">Velnora Partners</span> bots are trained to capture both
                    short-term volatility and long-term momentum in crypto markets, balancing high-risk/high-reward strategies.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Forex -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-green-500/5 to-indigo-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-green-500/50">

                <div class="w-16 h-16 mx-auto mb-6 bg-green-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 8c-2.21 0-4 .895-4 2s1.79 2 4 2 4 .895 4 2-1.79 2-4 2m0-7c2.21 0 4 .895 4 2s-1.79 2-4 2m0 7v2m0-18C6.477 0 2 4.477 2 10c0 4.73 3.327 8.64 7.688 9.707A2.996 2.996 0 0012 22a2.996 2.996 0 002.312-2.293C18.673 18.64 22 14.73 22 10c0-5.523-4.477-10-10-10z"/>
                  </svg>
                </div>

                <h3 class="text-xl font-bold text-white mb-4">Forex</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    The <span class="font-semibold text-green-400">Foreign Exchange (Forex)</span> market is the world's largest
                    financial market, with daily trading volume exceeding <span class="font-semibold">$6 trillion</span>.
                  </p>
                  <p>
                    Forex trading involves buying one currency while simultaneously selling another, e.g., <span class="font-semibold">EUR/USD</span>.
                  </p>
                  <p>
                    <span class="font-semibold text-green-400">Velnora Partners</span> applies quantitative models and adaptive bots
                    to capture profits from volatility while minimizing downside risks.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Precious Metals -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-gray-400/5 to-yellow-300/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-gray-400/50">

                <div class="w-16 h-16 mx-auto mb-6 bg-gray-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M12 3v2m6.364 1.636l-1.414 1.414M21 12h-2M17.95 17.95l-1.414-1.414M12 21v-2M6.05 17.95l1.414-1.414M3 12h2M6.636 6.636l1.414 1.414"/>
                  </svg>
                </div>

                <h3 class="text-xl font-bold text-white mb-4">Precious Metals</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    <span class="font-semibold text-gray-300">Gold, silver, platinum,</span> and other metals have long been
                    considered safe-haven assets.
                  </p>
                  <p>
                    Precious metals retain value during economic downturns and inflationary periods, making them essential for
                    <span class="font-semibold">portfolio diversification</span>.
                  </p>
                  <p>
                    <span class="font-semibold text-gray-300">Velnora Partners</span> integrates metals trading into its strategies,
                    hedging risks while capitalizing on cyclical market trends.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Stocks -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-red-500/5 to-indigo-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-red-500/50">

                <div class="w-16 h-16 mx-auto mb-6 bg-red-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M3 17l6-6 4 4 8-8"/>
                  </svg>
                </div>

                <h3 class="text-xl font-bold text-white mb-4">Stocks</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    <span class="font-semibold text-red-400">Stocks</span> represent ownership in a company and a claim on part of its assets and earnings.
                  </p>
                  <p>
                    The stock market is a key driver of wealth creation, offering both
                    <span class="font-semibold">long-term growth</span> and <span class="font-semibold">short-term opportunities</span>.
                  </p>
                  <p>
                    <span class="font-semibold text-red-400">Velnora Partners</span> bots scan global equity markets, balancing
                    growth and defensive sectors for optimized performance.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- NFP -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
                <div class="w-16 h-16 mx-auto mb-6 bg-blue-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                  </svg>
                </div>
                <h3 class="text-xl font-bold text-white mb-4">NFP</h3>

                <div class="text-gray-300 text-sm md:text-base leading-relaxed space-y-3">
                  <p>
                    <span class="font-semibold text-blue-400">Nonfarm Payroll (NFP)</span> is a key U.S. economic indicator that tracks employment in goods, construction, and manufacturing industries. It excludes farm workers, private household employees, and non-profit organization staff.
                  </p>
                  <p>
                    Released monthly by the <span class="font-semibold">U.S. Department of Labor</span>, the report provides a comprehensive view of the labor market. The Bureau of Labor Statistics typically publishes it on the <span class="font-semibold">first Friday of the month</span> at 8:30 a.m. Eastern Time.
                  </p>
                  <p>
                    NFP data strongly influences the <span class="text-blue-400">U.S. dollar</span>, <span class="text-blue-400">foreign exchange markets</span>, the <span class="text-blue-400">bond market</span>, and the <span class="text-blue-400">stock market</span>.
                  </p>
                  <p>
                    The report reflects the <span class="font-semibold">change in nonfarm payrolls</span> compared to the previous month—typically ranging between <span class="font-semibold">+10,000 and +250,000 jobs</span> during stable economic periods.
                  </p>
                  <p>
                    For investors seeking <span class="font-semibold">short-term opportunities with large profit potential</span>, the NFP release is one of the most important market events to watch.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Service Benefits Section -->
    <section class="py-16 bg-gray-900 relative overflow-hidden">
      <!-- Background Effects -->
      <div class="absolute inset-0">
        <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5"></div>
        <div class="absolute inset-0 opacity-10">
          <svg class="h-full w-full" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <pattern id="benefits-grid" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M0 40V0h40" fill="none" stroke="currentColor" stroke-width="0.5"/>
                <circle cx="20" cy="20" r="1" fill="currentColor"/>
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#benefits-grid)"/>
          </svg>
        </div>
      </div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <!-- Section Header -->
        <div class="text-center mb-12">
          <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">Why Our Services Stand Out</span>
          </h2>
          <p class="text-lg text-gray-300">Our services are designed to empower investors with cutting-edge technology and expert management.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <!-- AI-Driven Trading -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
                <div class="w-16 h-16 mx-auto mb-6 bg-blue-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                  </svg>
                </div>
                <h3 class="text-xl font-bold text-white mb-4">AI-Driven Trading</h3>
                <p class="text-gray-300">Our advanced AI and self-learning bots analyze real-time market data to optimize trading strategies across commodities, crypto, and forex markets.</p>
              </div>
            </div>
          </div>

          <!-- Expert Management -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/5 to-blue-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
                <div class="w-16 h-16 mx-auto mb-6 bg-emerald-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0 0h3m-3 0H9m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                  </svg>
                </div>
                <h3 class="text-xl font-bold text-white mb-4">Expert Management</h3>
                <p class="text-gray-300">Our team of expert traders and analysts strategically manage your investments to maximize returns and minimize risks.</p>
              </div>
            </div>
          </div>

          <!-- Diversified Portfolios -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-emerald-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-blue-500/50">
                <div class="w-16 h-16 mx-auto mb-6 bg-blue-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"/>
                  </svg>
                </div>
                <h3 class="text-xl font-bold text-white mb-4">Diversified Portfolios</h3>
                <p class="text-gray-300">Invest across commodities, cryptocurrencies, forex, precious metals, and stocks to reduce risk and enhance returns.</p>
              </div>
            </div>
          </div>

          <!-- Secure Transactions -->
          <div class="group">
            <div class="relative h-full">
              <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/5 to-blue-400/5 rounded-xl transition-all duration-300 group-hover:scale-105"></div>
              <div class="relative h-full p-6 bg-gray-800 rounded-xl border border-gray-700 transition-all duration-300 hover:border-emerald-500/50">
                <div class="w-16 h-16 mx-auto mb-6 bg-emerald-500/10 rounded-xl flex items-center justify-center">
                  <svg class="w-8 h-8 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0 0h3m-3 0H9m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                  </svg>
                </div>
                <h3 class="text-xl font-bold text-white mb-4">Secure Transactions</h3>
                <p class="text-gray-300">All transactions are protected with advanced encryption protocols, ensuring the safety of your data and funds.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
