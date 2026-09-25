<script setup>
// About Us — ported from resources/views/home/about.blade.php
// All copy in the source is hardcoded (no $settings-> interpolation), so no
// store wiring is needed here. Alpine's `x-data="{ hover: false }"` on the
// value cards was unused (the hover visuals are driven purely by Tailwind's
// `group`/`group-hover:` classes), so it is dropped rather than ported.
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
      <!-- Abstract Background Elements -->
      <div class="absolute inset-0 z-20 md:z-0 pointer-events-none">
        <div class="absolute top-0 left-0 w-full h-full opacity-60 md:opacity-20">
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
        <div class="absolute bottom-0 right-0 w-full h-full opacity-50 md:opacity-10">
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
        <div class="text-center">
          <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
            Our Company
          </div>
          <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
            <span class="block">About Velnora Partners</span>
            <span class="block mt-1 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-teal-400">Empowering Wealth Creation</span>
          </h1>
          <p class="max-w-2xl mt-5 mx-auto text-xl text-gray-300">
            Discover our mission to revolutionize trading with innovative AI-driven solutions, global reach, and unparalleled security
          </p>
        </div>
      </div>
    </section>

    <!-- Our Values Section -->
    <section class="py-16 bg-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold text-white">Our Values</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-4">
          <!-- Value Card 1 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/5 rounded-xl transform group-hover:scale-105 transition-all duration-300 opacity-0 group-hover:opacity-100"></div>
            <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-xl overflow-hidden border border-gray-700 p-6 transition-all duration-300 group-hover:border-blue-500 group-hover:shadow-lg group-hover:shadow-blue-900/20 h-full flex flex-col items-center">
              <div class="w-20 h-20 mb-6 bg-blue-900 bg-opacity-30 rounded-full flex items-center justify-center">
                <img src="/temp/custom/img/users.png" alt="Client Focus" class="w-10 h-10">
              </div>
              <h3 class="text-xl font-bold text-white mb-4">Client Focus</h3>
              <p class="text-gray-300 text-center">Our priority is delivering exceptional support, with a dedicated team ensuring your financial goals are met with personalized service.</p>
            </div>
          </div>

          <!-- Value Card 2 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/5 rounded-xl transform group-hover:scale-105 transition-all duration-300 opacity-0 group-hover:opacity-100"></div>
            <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-xl overflow-hidden border border-gray-700 p-6 transition-all duration-300 group-hover:border-blue-500 group-hover:shadow-lg group-hover:shadow-blue-900/20 h-full flex flex-col items-center">
              <div class="w-20 h-20 mb-6 bg-blue-900 bg-opacity-30 rounded-full flex items-center justify-center">
                <img src="/temp/custom/img/website.png" alt="Innovation" class="w-10 h-10">
              </div>
              <h3 class="text-xl font-bold text-white mb-4">Innovation</h3>
              <p class="text-gray-300 text-center">We leverage cutting-edge AI and deep learning to create adaptive trading strategies for maximum profitability.</p>
            </div>
          </div>

          <!-- Value Card 3 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/5 rounded-xl transform group-hover:scale-105 transition-all duration-300 opacity-0 group-hover:opacity-100"></div>
            <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-xl overflow-hidden border border-gray-700 p-6 transition-all duration-300 group-hover:border-blue-500 group-hover:shadow-lg group-hover:shadow-blue-900/20 h-full flex flex-col items-center">
              <div class="w-20 h-20 mb-6 bg-blue-900 bg-opacity-30 rounded-full flex items-center justify-center">
                <img src="/temp/custom/img/speed.png" alt="Security" class="w-10 h-10">
              </div>
              <h3 class="text-xl font-bold text-white mb-4">Security</h3>
              <p class="text-gray-300 text-center">Our robust security systems, including SSL encryption and DDoS protection, ensure the safety of your investments.</p>
            </div>
          </div>

          <!-- Value Card 4 -->
          <div class="relative group">
            <div class="absolute inset-0 bg-gradient-to-b from-blue-600/20 to-blue-800/5 rounded-xl transform group-hover:scale-105 transition-all duration-300 opacity-0 group-hover:opacity-100"></div>
            <div class="relative bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-xl overflow-hidden border border-gray-700 p-6 transition-all duration-300 group-hover:border-blue-500 group-hover:shadow-lg group-hover:shadow-blue-900/20 h-full flex flex-col items-center">
              <div class="w-20 h-20 mb-6 bg-blue-900 bg-opacity-30 rounded-full flex items-center justify-center">
                <img src="/temp/custom/img/1497835.png" alt="Reliability" class="w-10 h-10">
              </div>
              <h3 class="text-xl font-bold text-white mb-4">Reliability</h3>
              <p class="text-gray-300 text-center">As an industry leader, we provide unmatched stability and trust, backed by global regulations and certifications.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Why Choose Us Section -->
    <section class="py-16 bg-gray-800 relative overflow-hidden">
      <!-- Background pattern -->
      <div class="absolute inset-0 z-0 opacity-5">
        <svg width="100%" height="100%" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="grid" width="50" height="50" patternUnits="userSpaceOnUse">
              <path d="M 50 0 L 0 0 0 50" fill="none" stroke="white" stroke-width="0.5"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
        </svg>
      </div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-white">Why Choose Velnora Partners?</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="flex flex-col lg:flex-row gap-8 items-center">
          <div class="w-full lg:w-1/2">
            <div class="bg-gray-900 bg-opacity-50 backdrop-blur-sm border border-gray-700 rounded-xl overflow-hidden shadow-lg">
              <img src="/temp/custom/img/commission-scheme-crypt1d.png" alt="Trading Platform" class="w-full h-auto">
            </div>
          </div>

          <div class="w-full lg:w-1/2">
            <ul class="space-y-4">
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">Globally Regulated</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">AI-Powered Trading</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">24/7 Multilingual Support</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">80% Cash Back Policy</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">Diversified Investment Options</p>
                </div>
              </li>
              <li class="flex items-start">
                <div class="flex-shrink-0 mt-1">
                  <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
                  </svg>
                </div>
                <div class="ml-3">
                  <p class="text-lg font-medium text-white">Global Accessibility</p>
                </div>
              </li>
            </ul>

            <div class="mt-8">
              <RouterLink to="/why-us" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200">
                Learn About Our Services
                <svg class="ml-2 -mr-1 w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- History Section -->
    <section class="py-16 bg-gray-900 relative overflow-hidden">
      <!-- Background image with overlay -->
      <div class="absolute inset-0 z-0 opacity-20" style="background-image: url(/temp/custom/img/abt.png); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-white">Our History</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-6 md:p-10">
          <div class="prose prose-lg prose-invert max-w-none">
            <p>Since its founding in 2009, Velnora Partners has been deeply involved with the trading industry. The same year, the company launched research on a trading bot called Trader's Partner (at the time) and has grown it to more than 20 million users over the course of a decade, rebranding it as Bit-Forex.</p>
            <p>In 2017, following a successful Nonfarm Payroll (NFP) event that raised $18.9 million, Velnora Partners established itself as a leading NFP trading company, building a suite of user-first trading services that enable anyone to easily create, manage, store, compound, and withdraw investments.</p>
            <p>Built as a broker and trading service on a robust online infrastructure, Velnora Partners comprises the Velnora Partners wallet, Bit-Forex bot, and 24/7 customer support from around the world. We empower individuals, investors, and traders with true passive trading of currencies, commodities, and digital assets to increase revenue, gain a competitive edge, and innovate in previously impossible ways.</p>
            <p>Forged in trading, Velnora Partners tools and services can be used worldwide by individuals or groups of all sizes seeking to create long-term annuities or dabble in currency, commodities, and crypto trading as part of their acquisition, retention, engagement, and monetization strategies.</p>
            <p>The Velnora Partners ecosystem is fueled by revenues from trading for itself and commissions, a strategy to directly back the company's expenditures on services. As a startup that has evolved into a leader in the modern financial and investment industry, Velnora Partners aims to change not only the financial market but also the lives of its partners and investors.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- About Us Section -->
    <section class="py-16 bg-gray-800 relative overflow-hidden">
      <!-- Background image with overlay -->
      <div class="absolute inset-0 z-0 opacity-20" style="background-image: url(/temp/custom/img/abt.png); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-white">About Velnora Partners</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-6 md:p-10">
          <div class="prose prose-lg prose-invert max-w-none">
            <p>Velnora Partners is an Artificial Intelligence Asset Management Company that uses advanced AI software to trade primarily in the Forex and cryptocurrency markets, generating income for our investors. Our software employs deep learning techniques to train large neural networks to recognize patterns in data, leveraging unstructured data from social media posts and news articles to develop trading strategies with profitable returns.</p>
            <p>By analyzing large amounts of historical and real-time trading data, our AI provides better market insights, allowing us to shape strategies based on current market volatility. Our investment managers, supported by AI, analyze forex, cryptocurrency, and other markets to create profitable, adaptive trading portfolios and make safe decisions.</p>
            <p>Our business is risk-free from an investing perspective, thanks to advanced trading technologies, a powerful expert team, diversified investment areas, and an 80% cash-back policy in case of trade losses. Financial flow separation ensures stability, and our high-quality service, coupled with constant technical and legal support, guarantees the security of our partners' financial assets.</p>
            <p>Velnora Partners is an officially registered company with all necessary licenses to conduct trade and financial activities in international markets, compliant with international legal requirements. Our head office is located in Nicosia, Cyprus, where our professional team of traders, political experts, sociologists, financial analysts, and economic experts conducts daily market analysis, monitoring changes in stock and cryptocurrency markets, tracking promising companies, and studying exchange rates.</p>
            <p>By partnering with Velnora Partners, you gain a stable and secure source of income and join a team dedicated to providing high-quality support. We analyze your requests and provide professional advice promptly, ensuring stability, development, and reliability in all investment activities.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Team Section -->
    <section class="py-16 bg-gray-900 relative overflow-hidden">
      <!-- Background image with overlay -->
      <div class="absolute inset-0 z-0 opacity-20" style="background-image: url(/temp/custom/img/abt.png); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-white">Our Team</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-6 md:p-10">
          <div class="prose prose-lg prose-invert max-w-none">
            <p>Our team comprises traders, political experts, sociologists, financial analysts, and economic experts who conduct detailed daily analysis and market monitoring. We track changes in stock and cryptocurrency markets, monitor promising companies and their transactions, and study exchange rates of all available cryptocurrencies and fiat funds.</p>
            <p>The success of our team stems from analyzing not only financial markets but also global political, social, and economic processes. This comprehensive approach keeps us one step ahead, enabling us to predict market trends and make informed investment decisions to maximize profits.</p>
            <p>Our management is meticulous in selecting personnel to ensure top-tier service quality. All our employees—analysts, traders, marketing and security specialists, lawyers, and managers—are accredited experts, forming a unified, well-functioning mechanism dedicated to your success.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Investment Plan Section -->
    <section class="py-16 bg-gray-800 relative overflow-hidden">
      <!-- Background image with overlay -->
      <div class="absolute inset-0 z-0 opacity-20" style="background-image: url(/temp/custom/img/abt.png); background-position: center; background-repeat: no-repeat; background-size: cover;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-white">Our Investment Plans</h2>
          <div class="w-24 h-1 mx-auto mt-4 rounded-full bg-gradient-to-r from-blue-500 to-teal-400"></div>
        </div>

        <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-6 md:p-10">
          <div class="prose prose-lg prose-invert max-w-none">
            <p>Unlike non-standard investment strategies, our approach globalizes the process of generating profit and creating benefits for all program participants. As a major player in the market, Velnora Partners manages total capital to earn significant returns on our clients' personal deposits.</p>
            <p>Our traders are incentivized to increase their income on deposits, as their profit directly correlates with the profitability of our investors. This alignment ensures that our team works diligently to maximize returns while maintaining a secure and diversified investment strategy.</p>
            <p>Learn more about our investment plans and how you can start building wealth with Velnora Partners by visiting our <RouterLink to="/trade" class="text-blue-400 hover:underline">Investment Plans</RouterLink> section.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Expert Support Section -->
    <section class="py-16 bg-gray-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col-reverse lg:flex-row gap-8 items-center">
          <!-- Left side: Image -->
          <div class="w-full lg:w-1/2">
            <div class="relative">
              <!-- Decorative elements -->
              <div class="absolute -top-4 -left-4 w-24 h-24 bg-blue-500 opacity-20 rounded-full blur-xl"></div>
              <div class="absolute -bottom-4 -right-4 w-32 h-32 bg-teal-400 opacity-20 rounded-full blur-xl"></div>

              <!-- Image -->
              <div class="relative bg-gray-900 bg-opacity-50 backdrop-blur-sm border border-gray-700 rounded-xl overflow-hidden shadow-lg">
                <img src="/temp/custom/img/commission-scheme-crypt1t.png" alt="Expert Support" class="w-full h-auto">
              </div>
            </div>
          </div>

          <!-- Right side: Content -->
          <div class="w-full lg:w-1/2 space-y-8">
            <div>
              <h2 class="text-2xl font-bold text-white mb-4">Stay Ahead with Our Experts</h2>
              <p class="text-gray-300">
                Our global team provides 24/7 support in over 20 languages, offering unparalleled assistance at every stage of your investment journey. From account setup to withdrawals, we ensure a seamless experience with our robust online infrastructure.
              </p>
            </div>

            <div>
              <h2 class="text-2xl font-bold text-white mb-4">Experience More Than Trading</h2>
              <p class="text-gray-300">
                Our success is driven by core values: leveraging AI-driven trading, ensuring robust security, offering diversified investment options, and delivering exceptional customer service. Join Velnora Partners to access advanced trading platforms and tailored financial products.
              </p>
            </div>

            <div class="pt-6">
              <RouterLink to="/register" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150">
                Start Trading Now
                <svg class="ml-2 -mr-1 w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
