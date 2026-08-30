<script setup>
import { ref, onMounted, watch } from 'vue'
import { onClickOutside } from '@vueuse/core'
import { RouterLink, RouterView } from 'vue-router'
import { useAuthUserStore } from '@/stores/authUser'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { storageUrl } from '@/lib/storage'

const authUser = useAuthUserStore()
const settingsStore = usePublicSettingsStore()

const mobileMenuOpen = ref(false)
const companyMenuOpen = ref(false)
const companyMenuRef = ref(null)
onClickOutside(companyMenuRef, () => { companyMenuOpen.value = false })

const tickerContainer = ref(null)
const year = new Date().getFullYear()

onMounted(async () => {
  await settingsStore.ensureLoaded()
  if (settingsStore.settings?.siteTitle) {
    document.title = `${settingsStore.settings.siteTitle}`
  }
  loadTickerWidget()
})

watch(() => settingsStore.settings, (s) => {
  if (s?.siteTitle) document.title = s.siteTitle
})

function loadTickerWidget() {
  if (!tickerContainer.value) return
  const script = document.createElement('script')
  script.type = 'text/javascript'
  script.async = true
  script.src = 'https://s3.tradingview.com/external-embedding/embed-widget-ticker-tape.js'
  script.innerHTML = JSON.stringify({
    symbols: [
      { proName: 'FX_IDC:EURUSD', title: 'EUR/USD' },
      { proName: 'BITSTAMP:BTCUSD', title: 'BTC/USD' },
      { proName: 'BITSTAMP:ETHUSD', title: 'ETH/USD' },
    ],
    colorTheme: 'dark',
    isTransparent: false,
    displayMode: 'adaptive',
    locale: 'en',
  })
  tickerContainer.value.appendChild(script)
}
</script>

<template>
  <div class="min-h-screen flex flex-col antialiased text-gray-200 bg-dark-300 font-sans">
    <a href="#main-content" class="sr-only focus:not-sr-only focus:bg-blue-700 focus:text-white focus:fixed focus:px-4 focus:py-2 focus:top-2 focus:left-2 focus:z-50">
      Skip to main content
    </a>

    <!-- Header -->
    <header class="bg-dark-300 border-b border-gray-800 relative z-50">
      <div class="max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <RouterLink to="/" class="flex-shrink-0 flex items-center">
            <img v-if="settingsStore.settings?.logo" class="h-8 w-auto" :src="storageUrl(settingsStore.settings.logo)" :alt="settingsStore.settings?.siteName">
            <span v-else class="text-white font-bold text-lg">{{ settingsStore.settings?.siteName }}</span>
          </RouterLink>

          <nav class="hidden md:flex space-x-8">
            <RouterLink to="/" class="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-200 hover:text-white">Home</RouterLink>

            <div class="relative" ref="companyMenuRef">
              <button @click="companyMenuOpen = !companyMenuOpen" class="group inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-200 hover:text-white focus:outline-none">
                <span>Company</span>
                <svg class="ml-2 h-4 w-4 text-gray-400 group-hover:text-gray-300" :class="{ 'rotate-180': companyMenuOpen }" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </button>
              <div v-if="companyMenuOpen" class="animate-fadeIn absolute left-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-dark-300 ring-1 ring-black ring-opacity-5 z-50 border border-gray-800">
                <RouterLink to="/about" class="block px-4 py-2 text-sm text-gray-200 hover:bg-dark-200" @click="companyMenuOpen = false">About Us</RouterLink>
                <RouterLink to="/faq" class="block px-4 py-2 text-sm text-gray-200 hover:bg-dark-200" @click="companyMenuOpen = false">FAQ</RouterLink>
                <RouterLink to="/terms" class="block px-4 py-2 text-sm text-gray-200 hover:bg-dark-200" @click="companyMenuOpen = false">Terms &amp; Conditions</RouterLink>
              </div>
            </div>

            <RouterLink to="/why-us" class="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-200 hover:text-white">Our Services</RouterLink>
            <RouterLink to="/trade" class="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-200 hover:text-white">Investment</RouterLink>
            <RouterLink to="/contact" class="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-200 hover:text-white">Contact</RouterLink>
          </nav>

          <div class="hidden md:flex items-center space-x-4">
            <template v-if="authUser.isAuthenticated">
              <RouterLink to="/dashboard" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
                Dashboard
              </RouterLink>
            </template>
            <template v-else>
              <RouterLink to="/login" class="text-gray-200 hover:text-white flex items-center">
                <i class="fas fa-lock mr-1"></i>
                <span>Log in</span>
              </RouterLink>
              <RouterLink to="/register" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors">
                Sign up
              </RouterLink>
            </template>
          </div>

          <div class="flex md:hidden items-center">
            <button @click="mobileMenuOpen = !mobileMenuOpen" type="button" class="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white" aria-controls="mobile-menu" :aria-expanded="mobileMenuOpen">
              <span class="sr-only">Open main menu</span>
              <svg v-if="!mobileMenuOpen" class="block h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else class="block h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Mobile menu -->
        <div v-if="mobileMenuOpen" id="mobile-menu" class="md:hidden">
          <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            <RouterLink to="/" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">Home</RouterLink>
            <RouterLink to="/about" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">About Us</RouterLink>
            <RouterLink to="/faq" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">FAQ</RouterLink>
            <RouterLink to="/terms" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">Terms &amp; Conditions</RouterLink>
            <RouterLink to="/why-us" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">Our Services</RouterLink>
            <RouterLink to="/trade" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">Investment</RouterLink>
            <RouterLink to="/contact" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 rounded" @click="mobileMenuOpen = false">Contact</RouterLink>

            <div class="pt-4 pb-3 border-t border-gray-700">
              <div class="flex items-center space-x-3 px-4">
                <template v-if="authUser.isAuthenticated">
                  <RouterLink to="/dashboard" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium" @click="mobileMenuOpen = false">Dashboard</RouterLink>
                </template>
                <template v-else>
                  <RouterLink to="/login" class="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium" @click="mobileMenuOpen = false">
                    <i class="fas fa-lock mr-1"></i> Log in
                  </RouterLink>
                  <RouterLink to="/register" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium" @click="mobileMenuOpen = false">
                    Sign up
                  </RouterLink>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Market ticker widget -->
    <div class="bg-dark-300 border-b border-gray-800">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-1">
          <iframe
            src="https://widget.coinlib.io/widget?type=horizontal_v2&theme=dark&pref_coin_id=1505&invert_hover=no"
            width="100%" height="36px" scrolling="auto" marginwidth="0" marginheight="0" frameborder="0" class="w-full">
          </iframe>
        </div>
      </div>
    </div>

    <main id="main-content" class="flex-grow">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer class="bg-dark-400 text-gray-300">
      <div class="border-t border-gray-800">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="py-12 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <div>
              <div class="flex items-center mb-6">
                <RouterLink to="/" class="flex items-center">
                  <img v-if="settingsStore.settings?.logo" class="h-8 w-auto" :src="storageUrl(settingsStore.settings.logo)" :alt="settingsStore.settings?.siteName">
                </RouterLink>
              </div>
              <p class="text-sm text-gray-400 mb-6">
                {{ settingsStore.settings?.siteName }} offers CFD trading on stocks, forex, indices, commodities, and cryptocurrencies with competitive spreads and advanced trading tools.
              </p>
              <div class="flex space-x-4">
                <a href="https://www.facebook.com/keystonebitforex/" class="text-gray-400 hover:text-white" aria-label="Facebook"><i class="fab fa-facebook"></i></a>
                <a href="#" class="text-gray-400 hover:text-white" aria-label="LinkedIn"><i class="fab fa-linkedin"></i></a>
                <a v-if="settingsStore.settings?.contactEmail" :href="`mailto:${settingsStore.settings.contactEmail}`" class="text-gray-400 hover:text-white" aria-label="Email"><i class="fas fa-envelope"></i></a>
                <a v-if="settingsStore.settings?.whatsappNumber" :href="`https://wa.me/${settingsStore.settings.whatsappNumber.replace(/[^\d+]/g, '')}`" class="text-gray-400 hover:text-white" aria-label="WhatsApp"><i class="fab fa-whatsapp"></i></a>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-white uppercase tracking-wider mb-4">Quick Links</h3>
              <ul class="space-y-3">
                <li><RouterLink to="/about" class="text-sm text-gray-400 hover:text-white transition">About Us</RouterLink></li>
                <li><RouterLink to="/why-us" class="text-sm text-gray-400 hover:text-white transition">Why Choose Us</RouterLink></li>
                <li><RouterLink to="/for-traders" class="text-sm text-gray-400 hover:text-white transition">Education</RouterLink></li>
                <li><RouterLink to="/contact" class="text-sm text-gray-400 hover:text-white transition">Contact</RouterLink></li>
              </ul>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-white uppercase tracking-wider mb-4">Your Account</h3>
              <ul class="space-y-3">
                <li><RouterLink to="/login" class="text-sm text-gray-400 hover:text-white transition">Log In</RouterLink></li>
                <li><RouterLink to="/register" class="text-sm text-gray-400 hover:text-white transition">Create Account</RouterLink></li>
                <li><RouterLink to="/contact" class="text-sm text-gray-400 hover:text-white transition">Help Center</RouterLink></li>
              </ul>
            </div>
            <div></div>
          </div>
        </div>
      </div>

      <div class="bg-dark-500 border-t border-gray-800">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div class="text-xs text-gray-400">
            <p class="mb-4 leading-relaxed">
              <span class="font-semibold text-gray-300">RISK WARNING:</span> The Financial Products offered by the company include Contracts for Difference ('CFDs') and other complex financial products. Trading CFDs carries a high level of risk since leverage can work both to your advantage and disadvantage. As a result, CFDs may not be suitable for all investors because it is possible to lose all of your invested capital. You should never invest money that you cannot afford to lose. Before trading in the complex financial products offered, please ensure you understand the risks involved.
            </p>
            <div class="flex flex-wrap gap-4 mb-4">
              <RouterLink to="/terms" class="text-blue-400 hover:text-blue-300 transition">Terms &amp; Conditions</RouterLink>
              <RouterLink to="/privacy" class="text-blue-400 hover:text-blue-300 transition">Privacy Policy</RouterLink>
              <RouterLink to="/regulation" class="text-blue-400 hover:text-blue-300 transition">Legal Documents</RouterLink>
            </div>
            <p>© {{ year }} {{ settingsStore.settings?.siteName }}. All Rights Reserved.</p>
          </div>
        </div>
      </div>
    </footer>

    <!-- Sticky bottom ticker -->
    <div class="sticky bottom-0 z-30 bg-dark-400 border-t border-gray-800" ref="tickerContainer">
      <div class="tradingview-widget-container__widget"></div>
    </div>
  </div>
</template>
