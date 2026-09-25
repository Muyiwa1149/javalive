<script setup>
import { ref } from 'vue'

const detailsOpen = ref(false)
</script>

<template>
  <section class="relative overflow-hidden bg-gradient-to-br from-gray-900 to-gray-800">
    <div class="absolute inset-0 z-20 md:z-0 pointer-events-none">
      <div class="absolute top-0 left-0 w-full h-full opacity-60 md:opacity-20">
        <svg class="absolute top-0 left-0 w-full h-full" viewBox="0 0 800 800" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="terms-a" x1="50%" x2="50%" y1="0%" y2="100%">
              <stop stop-color="#3B82F6" stop-opacity=".25" offset="0%"/>
              <stop stop-color="#10B981" stop-opacity=".2" offset="100%"/>
            </linearGradient>
          </defs>
          <path fill="url(#terms-a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(0 -50)" />
          <path fill="url(#terms-a)" d="M400,115 C515.46,115 615,214.54 615,330 C615,445.46 515.46,545 400,545 C284.54,545 185,445.46 185,330 C185,214.54 284.54,115 400,115 Z" transform="translate(350 150)" />
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

    <div class="relative z-10 px-4 py-16 mx-auto max-w-7xl sm:px-6 lg:px-8">
      <div class="text-center mb-12">
        <div class="inline-block px-3 py-1 mb-4 text-xs font-semibold tracking-wider text-blue-400 uppercase bg-blue-900 bg-opacity-30 rounded-full">
          Legal Documentation
        </div>
        <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl md:text-5xl">
          <span class="block">Privacy Policy &amp; Terms</span>
          <span class="block mt-1 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-teal-400">Velnora Partners</span>
        </h1>
        <p class="max-w-2xl mt-5 mx-auto text-xl text-gray-300">
          Learn about our commitment to protecting your data and the terms governing our services
        </p>
      </div>
    </div>
  </section>

  <section class="py-12 bg-gray-900">
    <div class="container max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden">
        <div class="p-6 md:p-10">
          <div class="prose prose-lg prose-invert max-w-none">
            <div class="space-y-8">
              <div>
                <h2 class="text-2xl font-bold text-blue-400 mb-4">Privacy Policy</h2>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Introduction</h3>
                <p class="text-gray-300 mb-4">
                  The purpose of this Privacy Policy is to provide detailed information on the use and treatment of personal data consented to by you, as well as the transfer of such data to third parties, and the rights that protect you as a consumer and user, in accordance with current legislation on the protection of personal data. This Privacy Policy applies to the processing of personal data you provide when you access our web portal, request information, use the services offered, acquire investment services, use functionalities or services available on the web portal, or submit a professional request, and while you maintain a relationship with Velnora Partners, whether commercial or professional.
                </p>
                <p class="text-gray-300 mb-4">
                  Out of respect for your right to privacy, Velnora Partners complies with current legislation, including Organic Law 15/1999 of 13 December on the Protection of Personal Data, its implementing regulations, and Regulation (EU) 2016/679 of the European Parliament and Council of 27 April 2016 ("GDPR"), effective from 25 May 2018, as well as any other rules that may develop or complement it in the future. By using our service, you agree to the collection and use of information in accordance with this policy.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Responsibility Over Your Information</h3>
                <p class="text-gray-300 mb-4">
                  Velnora Partners, with its registered CySEC office at Markou Botsari 15, Nicosia, Cyprus, is responsible for processing user data and guarantees their security and confidential treatment, in accordance with GDPR, CySEC, and other applicable regulations. You may contact our Data Protection Officer at <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a> to resolve any doubts related to the processing of your personal data.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Treatment and Protection of Personal Data</h3>
                <p class="text-gray-300 mb-4">
                  In accordance with current legislation on personal data protection, data collected through our website forms will be included in specific automated files of Velnora Partners users. The purpose of this collection and processing is to maintain a commercial relationship with you and to carry out information, training, advisory, and other activities of Velnora Partners. Data will only be transferred to entities necessary for these purposes or when required by competent authorities.
                </p>
                <p class="text-gray-300 mb-4">
                  Velnora Partners adopts necessary measures to guarantee the confidentiality, security, and integrity of your data, as provided in GDPR. Processing is carried out with your prior authorization, and you may provide specific, informed, and unequivocal consent by checking a box provided for this purpose. You agree to provide accurate information and keep it updated, communicating changes to Velnora Partners via <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a>. Failure to provide accurate data may prevent us from delivering requested services.
                </p>
                <p class="text-gray-300 mb-4">
                  You may exercise your rights to access, oppose, rectify, and cancel your personal data by sending an email to <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a>.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Purpose of Processing Personal Data</h3>
                <p class="text-gray-300 mb-4">Personal data collected through our web portal will be processed for:</p>
                <ul class="list-disc pl-6 text-gray-300 space-y-2 mb-4">
                  <li>Contracting investment services, maintaining contractual relationships, and managing, administering, informing, providing, and improving services.</li>
                  <li>Sending requested information through forms provided by Velnora Partners.</li>
                  <li>Sending newsletters, bulletins, commercial communications, promotions, and advertising from Velnora Partners and its companies.</li>
                  <li>Processing navigation data and cookies to improve accessibility, personalize navigation, and display targeted ads, in accordance with our Cookie Policy.</li>
                </ul>
                <p class="text-gray-300 mb-4">
                  Types of data collected include email address, names, phone number, address, cookies, and data usage. Data may be disclosed to protect legal liabilities, defend Velnora Partners's rights, ensure investor safety, meet legal requirements, or investigate wrongdoing. You may opt out of commercial communications by emailing <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a>.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Retention Period of Personal Data</h3>
                <p class="text-gray-300 mb-4">
                  Your personal data will be retained as long as the commercial and investment relationship is maintained, or until you request its deletion, and during the period for which legal responsibilities may arise from the services provided.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Data Collected by Users of the Services</h3>
                <p class="text-gray-300 mb-4">
                  Velnora Partners is not responsible for users' failure to comply with GDPR regarding personal data files they provide.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Security</h3>
                <p class="text-gray-300 mb-4">
                  When accessing our digital platforms, you can verify a secure environment by checking the URL, which begins with "https". We use commercially acceptable means to protect your personal data, though we cannot guarantee 100% security due to the nature of electronic storage.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Intellectual Property Rights</h3>
                <p class="text-gray-300 mb-4">
                  Velnora Partners owns the copyright, intellectual, and industrial property rights, know-how, and related rights for the content and services on this web portal. Users must respect third-party programs (e.g., Google Analytics) made available by Velnora Partners. Any use contrary to intellectual property legislation is prohibited, and users are liable for incidents resulting from negligent or malicious actions affecting our servers or security systems.
                </p>
              </div>

              <div>
                <h2 class="text-2xl font-bold text-blue-400 mb-4">Terms and Conditions</h2>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Principal</h3>
                <p class="text-gray-300 mb-4">
                  By accessing this website, you agree to be bound by the terms and conditions below. If you do not agree, please do not access this website. No act of downloading or copying will transfer title to any software or material. Anything transmitted to this site becomes the property of Velnora Partners and may be used for lawful purposes or disclosed as deemed appropriate, including to legal or regulatory authorities. We reserve all rights with respect to copyright and trademark ownership and will enforce such rights to the full extent of the law.
                </p>
                <p class="text-gray-300 mb-4">
                  Individuals aged 18 or older who complete registration on our official website become Clients and parties to cooperation regulated by these Terms and Conditions. Velnora Partners reserves the right to change these terms at any time, considering the company's interest and/or acceptance by the majority.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Rights and Obligations</h3>
                <p class="text-gray-300 mb-4">The Client has the right to:</p>
                <ul class="list-disc pl-6 text-gray-300 space-y-2 mb-4">
                  <li>Access information on trading services and investment reports via their account.</li>
                  <li>Withdraw funds when due, provided it does not fall below the minimum or contradict the lockdown rule.</li>
                  <li>Deposit funds into their account at any time.</li>
                  <li>Use all website functions, make investments, receive profits, earn referral bonuses, and use referral program features.</li>
                </ul>
                <p class="text-gray-300 mb-4">The Client is obligated to:</p>
                <ul class="list-disc pl-6 text-gray-300 space-y-2 mb-4">
                  <li>Make informed decisions and be loyal to the company, resolving disputes through available communication channels.</li>
                  <li>Pay fees and costs within the specified timeframe.</li>
                  <li>Accept profit/loss from executed trades per the agreement.</li>
                  <li>Keep authentication details confidential and not disclose them to third parties.</li>
                  <li>Not use malware, spying programs, or SPAM technology.</li>
                  <li>Provide authentic details, including a government-issued ID for verification.</li>
                </ul>
                <p class="text-gray-300 mb-4">The Company has the right to:</p>
                <ul class="list-disc pl-6 text-gray-300 space-y-2 mb-4">
                  <li>Manage client funds/assets for investment purposes according to proven strategies.</li>
                  <li>Execute swing trades or stop trading in cases of high market volatility, market closure, force majeure circumstances, or insufficient funds.</li>
                  <li>Withdraw charges from profits for commissions and fees.</li>
                </ul>
                <p class="text-gray-300 mb-4">The Company is obligated to:</p>
                <ul class="list-disc pl-6 text-gray-300 space-y-2 mb-4">
                  <li>Provide uninterrupted access to trading services and investment reports.</li>
                  <li>Guarantee stable accumulation of interest for chosen offers.</li>
                  <li>Store client information confidentially and not disclose it to third parties.</li>
                  <li>Guarantee 80% insurance of invested funds per the Cash Back Policy (CBP) in case of losses.</li>
                </ul>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Funds Deposit, Withdrawal, Fees, and Commissions</h3>
                <p class="text-gray-300 mb-4">
                  Clients can deposit funds via available methods and withdraw using the same or other convenient methods. A one-year lockdown period applies to initial capital deposits, preventing withdrawal during this period. Trading commences immediately after deposits, activating these terms. Multiple accounts may not be associated with one email, and Velnora Partners is not liable for losses due to such actions. Payout processing may take 10 minutes to 3 days. Clients pay interest rates, fees, and commissions per the investment plan. Referral and execution commissions are charged according to each plan. Payments are made through mutually suitable methods.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Force Majeure Events</h3>
                <p class="text-gray-300 mb-4">
                  If either party is hindered from fulfilling obligations due to circumstances beyond their control (e.g., technical disruptions, system failures, natural disasters, legislative changes, terrorist attacks, or other unpredictable events), such circumstances may be accepted as valid reasons for delaying contractual obligations, provided they align with principles of justice and faithfulness.
                </p>
              </div>

              <div>
                <h3 class="text-xl font-semibold text-white mt-6 mb-3">Conclusion</h3>
                <p class="text-gray-300 mb-4">
                  Velnora Partners processes do not guarantee identical results at different times. The company may suspend the website during force majeure events. All changes to these Terms and Conditions are effective immediately upon publication. All website pages must be read in conjunction with these Terms and Conditions.
                </p>
              </div>
            </div>
          </div>

          <div class="mt-12 text-center">
            <button
              @click="detailsOpen = !detailsOpen"
              class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150">
              <span>{{ detailsOpen ? 'Hide Data Protection Details' : 'Learn More About Data Protection' }}</span>
              <svg class="ml-2 -mr-1 h-5 w-5 transition-transform" :class="{ 'rotate-180': detailsOpen }" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>

            <div v-if="detailsOpen" class="animate-fadeIn mt-8 bg-gray-900 bg-opacity-50 p-6 rounded-xl backdrop-blur-sm border border-gray-700">
              <p class="text-gray-300">
                At Velnora Partners, we are committed to protecting your personal data and privacy rights. For any questions about our privacy practices or to exercise your data protection rights, please contact our Data Protection Officer at <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a>.
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-12 bg-gray-800 bg-opacity-70 backdrop-blur-sm rounded-2xl border border-gray-700 shadow-xl overflow-hidden">
        <div class="p-6 md:p-10">
          <h2 class="text-2xl font-bold text-blue-400 mb-4">Contact Us</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 class="text-xl font-semibold text-white mb-3">Cyprus Office</h3>
              <p class="text-gray-300">Markou Botsari 15, Nicosia, Cyprus</p>
              <p class="text-gray-300">Email: <a href="mailto:support@velnorapartners.com" class="text-blue-400 hover:text-blue-300">support@velnorapartners.com</a></p>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-white mb-3">US Offices</h3>
              <p class="text-gray-300">235 West Galena Street, Milwaukee, WI 53212</p>
              <p class="text-gray-300">60 East South Temple, Suite 2100, Salt Lake City, UT 84111</p>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
        <RouterLink to="/about" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">About Us</h3>
          <p class="text-gray-400">Learn more about our company and mission</p>
        </RouterLink>
        <RouterLink to="/contact" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">Contact Us</h3>
          <p class="text-gray-400">Questions about our services? Get in touch</p>
        </RouterLink>
        <RouterLink to="/register" class="bg-gray-800 bg-opacity-50 backdrop-blur-sm p-6 rounded-xl border border-gray-700 hover:border-blue-500 transition-colors duration-300 group">
          <h3 class="text-lg font-semibold text-white mb-2 group-hover:text-blue-400">Start Investing</h3>
          <p class="text-gray-400">Create an account to begin investing</p>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
