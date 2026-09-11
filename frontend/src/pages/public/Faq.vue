<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

// Corrected in Phase 7 (found via a live screenshot diff against the running source app): this
// entire FAQ page is hardcoded Blade content, not driven by the `faqs` table at all (that table's
// one real row, "How can i withdraw", never actually appears in home/faq.blade.php) — same
// "fully static" category as Terms/Privacy's hardcoded text. Ported verbatim, category tabs included.
const categories = [
  { key: 'about', label: 'About Us', icon: 'building' },
  { key: 'crypto', label: 'Cryptocurrencies', icon: 'coins' },
  { key: 'account', label: 'My Account', icon: 'user' },
  { key: 'investment', label: 'Investment', icon: 'wallet' },
  { key: 'other', label: 'NFP', icon: 'question' },
]

const faqsByCategory = {
  about: [
    { q: 'What is Keystone Bit-Fx?', a: 'Keystone Bit-Fx is a licensed multi-investment company that obtained a license to trade the financial markets for investors worldwide. We primarily trade forex, cryptocurrencies, and commodities unless instructed otherwise.' },
    { q: 'In what countries is Keystone Bit-Fx available?', a: 'Keystone Bit-Fx is a worldwide platform, hence available to investors from all around the world.' },
  ],
  crypto: [
    { q: 'What are cryptocurrencies and is it Bitcoin?', a: 'A cryptocurrency is a digital or virtual currency secured by cryptography, making it nearly impossible to counterfeit or double-spend. Many cryptocurrencies are decentralized networks based on blockchain technology—a distributed ledger enforced by a disparate network of computers. Bitcoin is a cryptocurrency, but not all cryptocurrencies are Bitcoins.' },
    { q: 'Why Bitcoin?', a: 'Bitcoin is fast, has lower fees, is global, and is tax-free in many jurisdictions.' },
    { q: 'How to create a wallet in my country?', a: 'It is easy to create a blockchain wallet. Visit blockchain.com or contact us for the most convenient crypto exchange in your country.', link: { text: 'blockchain.com', href: 'https://blockchain.com' } },
  ],
  account: [
    { q: "What do I do if I can't log into my account because I forgot my password?", a: 'Click the "Forgot Password" link, type your username or email, and you\'ll receive a link to change your password.' },
    { q: 'How do I register on this website?', a: 'Go to Keystone Bit-Fx and click Sign Up. Fill in the sign-up form. After everything is correctly filled and our T&Cs agreed to, your account will be automatically created.' },
    { q: 'How safe is it to use Keystone Bit-Fx?', a: 'We use industry-standard processes and technical safeguards to preserve the integrity and security of your personal information. We regularly back up your data to prevent data loss and the company has put all the necessary anti-malware software, advanced protection technology, and employ SSL encryption to ensure that information passed between our site and your browser is secure. SSL-encryption is in place to guard all the transferred data between your browser and our website. Also, the website is adequately protected by the latest technology from DDoS attacks.' },
  ],
  investment: [
    { q: 'How can I invest with Keystone Bit-Fx?', a: 'To start an investment, you must first become a registered member of Keystone Bit-Fx by signing up. After registration, investors can make their first deposit. All deposits must be made through the investor’s personal account at sign-in. To log in, use the member username and password you signed up with.' },
    { q: 'Can I open multiple investments?', a: 'Yes, you can open as many investments as possible on a single account. Multiple accounts are also accepted.' },
    { q: 'What are your investment plans?', a: 'To learn more about our investment offers, please go to the Investment Plans section of our website.', link: { text: 'Investment Plans', href: '/trade' } },
  ],
  other: [
    { q: 'What is NFP?', a: 'Nonfarm Payroll (NFP) employment is a compiled name for goods, construction, and manufacturing companies in the US. It does not include farm workers, private household employees, or non-profit organization employees.' },
  ],
}

const activeCategory = ref('about')
const active = ref(null)

function selectCategory(key) {
  activeCategory.value = key
  active.value = null
}

function toggle(index) {
  active.value = active.value !== index ? index : null
}

// Back-to-top button (mirrors the source's DOMContentLoaded/scroll script).
const showBackToTop = ref(false)
function onScroll() {
  showBackToTop.value = window.pageYOffset > 300
}
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
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
        <div class="text-center mb-12">
          <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
            Knowledge Base
          </div>
          <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
            <span class="block">Frequently Asked Questions</span>
            <span class="block mt-1 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-teal-400">All You Need to Know</span>
          </h1>
          <p class="max-w-2xl mt-5 mx-auto text-xl text-gray-300">
            Get answers to common questions about Keystone Bit-Fx and our services
          </p>
        </div>
      </div>
    </section>

    <!-- FAQ Content -->
    <section class="py-12 bg-gray-900">
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="space-y-10">
          <!-- FAQ Category Tabs -->
          <div class="flex flex-wrap justify-center gap-2 md:gap-4">
            <button
              v-for="cat in categories" :key="cat.key"
              @click="selectCategory(cat.key)"
              class="px-4 py-2 rounded-lg transition-all duration-200 text-sm md:text-base font-medium focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 focus:ring-offset-gray-900"
              :class="activeCategory === cat.key ? 'bg-blue-600 text-white' : 'bg-gray-800 text-gray-300 hover:bg-gray-700'"
            >
              {{ cat.label }}
            </button>
          </div>

          <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden">
            <div class="p-6 md:p-8">
              <div class="divide-y divide-gray-700">
                <div v-for="(faq, index) in faqsByCategory[activeCategory]" :key="index" class="py-4">
                  <button @click="toggle(index)" class="flex justify-between items-center w-full focus:outline-none">
                    <h4 class="text-lg font-medium text-white text-left">{{ faq.q }}</h4>
                    <svg :class="{ 'rotate-180': active === index }" class="w-5 h-5 text-blue-400 transform transition-transform duration-300 flex-shrink-0 ml-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                  </button>
                  <div v-show="active === index" class="mt-3 text-gray-300">
                    <p v-if="faq.link">
                      {{ faq.a.split(faq.link.text)[0] }}<a :href="faq.link.href" class="text-blue-400 hover:underline">{{ faq.link.text }}</a>{{ faq.a.split(faq.link.text)[1] }}
                    </p>
                    <p v-else>{{ faq.a }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-12 bg-gradient-to-br from-gray-900 to-gray-800">
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid lg:grid-cols-2 gap-8">
          <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-8">
            <div class="flex items-center space-x-4 mb-6">
              <div class="flex-shrink-0 w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <h2 class="text-2xl md:text-3xl font-bold text-white">Still have questions?</h2>
            </div>
            <p class="text-gray-300 mb-6">Our support team is available 24/7 to help you with any questions or concerns you may have about our platform.</p>
            <div class="flex space-x-4 pt-2">
              <RouterLink to="/contact" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150">
                Contact Support
                <svg class="ml-2 -mr-1 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
                </svg>
              </RouterLink>
            </div>
          </div>

          <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden p-8 relative">
            <!-- Decorative crypto icons in background -->
            <div class="absolute inset-0 z-0 opacity-10">
              <svg class="absolute top-4 right-4 w-16 h-16 text-blue-400" viewBox="0 0 24 24" fill="currentColor">
                <path d="M11.944 17.97L4.58 13.62 11.943 24l7.37-10.38-7.372 4.35h.003zM12.056 0L4.69 12.223l7.365 4.354 7.365-4.35L12.056 0z"/>
              </svg>
              <svg class="absolute bottom-4 left-4 w-12 h-12 text-blue-400" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0C5.372 0 0 5.373 0 12s5.372 12 12 12 12-5.373 12-12S18.628 0 12 0zm2.595 17.35a4.356 4.356 0 01-.778.55 5.858 5.858 0 01-.977.397c-.34.101-.68.15-1.05.182v1.4H10.2v-1.39c-.786-.091-1.518-.302-2.214-.605a5.567 5.567 0 01-1.698-1.246l1.245-1.507c.122.121.285.26.488.417.204.156.433.304.686.444.252.14.516.255.822.346.254.09.572.136.903.136.474 0 .89-.078 1.246-.233.357-.156.535-.45.535-.853a.766.766 0 00-.194-.535 1.676 1.676 0 00-.465-.356 3.869 3.869 0 00-.627-.266c-.234-.07-.44-.15-.627-.224a22.14 22.14 0 01-.905-.346 10.596 10.596 0 01-.938-.45 2.965 2.965 0 01-.778-.627c-.22-.252-.336-.573-.356-.938-.04-.697.204-1.296.673-1.813.47-.516 1.126-.876 1.977-1.05V7.44h1.592v1.245c.657.06 1.244.206 1.77.44.524.236.986.517 1.387.845l-1.167 1.506a6.52 6.52 0 00-1.02-.6 2.802 2.802 0 00-1.205-.249c-.497 0-.889.093-1.166.288-.279.196-.417.444-.417.744 0 .141.036.267.117.378.08.112.186.213.356.324.139.11.346.214.534.324.209.11.417.192.627.267.497.172.959.345 1.387.515.427.172.804.392 1.127.673.323.283.583.614.778 1.005.194.39.313.89.313 1.483a2.282 2.282 0 01-.268.8zm2.382-5.447h4.927v1.507h-4.927v-1.507z"/>
              </svg>
            </div>

            <div class="relative z-10">
              <div class="flex items-center space-x-4 mb-6">
                <div class="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-blue-400 to-teal-400 rounded-full flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h2 class="text-2xl md:text-3xl font-bold text-white">Ready to start trading?</h2>
              </div>
              <p class="text-gray-300 mb-6">Create an account now and start earning with our innovative trading platform. It only takes a few minutes to get started!</p>
              <div class="flex space-x-4 pt-2">
                <RouterLink to="/register" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-gray-900 bg-gradient-to-r from-blue-400 to-teal-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150">
                  Create Account
                  <svg class="ml-2 -mr-1 h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
                  </svg>
                </RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Back to top button -->
    <button
      @click="scrollToTop"
      class="fixed bottom-8 right-8 z-50 bg-blue-600 hover:bg-blue-700 text-white rounded-full p-3 shadow-lg transition-all duration-300"
      :class="showBackToTop ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'"
      aria-label="Back to top"
    >
      <svg class="w-6 h-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18" />
      </svg>
    </button>
  </div>
</template>
