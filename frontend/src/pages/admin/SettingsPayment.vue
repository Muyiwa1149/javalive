<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { CreditCard, Plus, Pencil, Trash2, Power } from 'lucide-vue-next'
import api from '@/lib/api'

const loading = ref(true)
const saving = ref(false)
const form = ref({})

const methods = ref([])
const showMethodForm = ref(false)
const editingId = ref(null)
const methodSaving = ref(false)
const barcodeFile = ref(null)

const emptyMethodForm = () => ({
  name: '', methodType: '', type: 'both', minimumAmount: 0, maximumAmount: 0, chargesAmount: 0,
  chargesType: 'percentage', durationNote: '', imageUrl: '', bankName: '', accountName: '', accountNumber: '',
  swiftCode: '', walletAddress: '', network: '', status: 'enabled',
})
const methodForm = ref(emptyMethodForm())

async function load() {
  loading.value = true
  try {
    const [settingsRes, methodsRes] = await Promise.all([
      api.get('/admin/settings'),
      api.get('/admin/settings/payment-methods'),
    ])
    form.value = { ...settingsRes.data }
    methods.value = methodsRes.data
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function save() {
  saving.value = true
  try {
    const { data } = await api.put('/admin/settings/payment', form.value)
    form.value = { ...data }
    Swal.fire({ icon: 'success', title: 'Gateway settings saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    saving.value = false
  }
}

function openCreateMethod() {
  editingId.value = null
  methodForm.value = emptyMethodForm()
  barcodeFile.value = null
  showMethodForm.value = true
}

function openEditMethod(m) {
  editingId.value = m.id
  methodForm.value = { ...m }
  barcodeFile.value = null
  showMethodForm.value = true
}

async function saveMethod() {
  methodSaving.value = true
  try {
    const fd = new FormData()
    Object.entries(methodForm.value).forEach(([k, v]) => { if (v !== null && v !== undefined) fd.append(k, v) })
    if (barcodeFile.value) fd.append('barcode', barcodeFile.value)

    if (editingId.value) {
      await api.put(`/admin/settings/payment-methods/${editingId.value}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    } else {
      await api.post('/admin/settings/payment-methods', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    }
    showMethodForm.value = false
    const { data } = await api.get('/admin/settings/payment-methods')
    methods.value = data
    Swal.fire({ icon: 'success', title: 'Saved', timer: 1200, showConfirmButton: false })
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Failed', text: e.response?.data?.message })
  } finally {
    methodSaving.value = false
  }
}

async function toggleMethod(m) {
  await api.post(`/admin/settings/payment-methods/${m.id}/toggle`)
  const { data } = await api.get('/admin/settings/payment-methods')
  methods.value = data
}

async function removeMethod(m) {
  const confirm = await Swal.fire({ icon: 'warning', title: `Delete "${m.name}"?`, showCancelButton: true, confirmButtonText: 'Delete', confirmButtonColor: '#e11d48' })
  if (!confirm.isConfirmed) return
  await api.delete(`/admin/settings/payment-methods/${m.id}`)
  const { data } = await api.get('/admin/settings/payment-methods')
  methods.value = data
}
</script>

<template>
  <div class="p-4 sm:p-6 lg:p-8 space-y-6 max-w-3xl">
    <h1 class="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2"><CreditCard class="w-6 h-6 text-indigo-500" /> Payment Gateways</h1>

    <div v-if="loading" class="text-slate-500 dark:text-slate-400">Loading…</div>

    <template v-else>
      <form class="space-y-6" @submit.prevent="save">
        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Preferences</h2>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Withdrawal option</label><input v-model="form.withdrawalOption" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Deposit option</label><input v-model="form.depositOption" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Deduction option</label><input v-model="form.deductionOption" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Credit card provider</label><input v-model="form.creditCardProvider" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Min top-up amount</label><input v-model.number="form.minTopupAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Auto merchant option</label><input v-model="form.autoMerchantOption" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <h3 class="text-xs font-semibold text-slate-500 uppercase pt-2">Internal Transfer</h3>
          <div class="grid grid-cols-3 gap-3 items-end">
            <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300 pb-2"><input v-model="form.useInternalTransfer" type="checkbox" class="rounded" /> Enabled</label>
            <div><label class="text-xs text-slate-500">Min transfer</label><input v-model.number="form.minTransferAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Transfer charges</label><input v-model.number="form.transferCharges" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
        </div>

        <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Gateway Keys</h2>
          <p class="text-xs text-slate-500 dark:text-slate-400">Stored for reference — no live gateway is wired up; deposits/withdrawals are manual and admin-approved.</p>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="text-xs text-slate-500">Stripe secret key</label><input v-model="form.stripeSecretKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Stripe public key</label><input v-model="form.stripePublicKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">PayPal client ID</label><input v-model="form.paypalClientId" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">PayPal client secret</label><input v-model="form.paypalClientSecret" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Paystack public key</label><input v-model="form.paystackPublicKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Paystack secret key</label><input v-model="form.paystackSecretKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Paystack URL</label><input v-model="form.paystackUrl" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Paystack email</label><input v-model="form.paystackEmail" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Flutterwave public key</label><input v-model="form.flutterwavePublicKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Flutterwave secret key</label><input v-model="form.flutterwaveSecretKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Flutterwave secret hash</label><input v-model="form.flutterwaveSecretHash" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div></div>
            <div><label class="text-xs text-slate-500">Binance API key</label><input v-model="form.binanceApiKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Binance secret key</label><input v-model="form.binanceSecretKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">CoinPayments public key</label><input v-model="form.coinpaymentsPublicKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">CoinPayments private key</label><input v-model="form.coinpaymentsPrivateKey" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">CoinPayments merchant ID</label><input v-model="form.coinpaymentsMerchantId" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">CoinPayments IPN secret</label><input v-model="form.coinpaymentsIpnSecret" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">CoinPayments debug email</label><input v-model="form.coinpaymentsDebugEmail" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
        </div>

        <div class="flex justify-end">
          <button type="submit" :disabled="saving" class="px-5 py-2.5 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">{{ saving ? 'Saving…' : 'Save Gateway Settings' }}</button>
        </div>
      </form>

      <div class="bg-white dark:bg-[#0F1524] border border-slate-200 dark:border-white/5 rounded-2xl p-6 space-y-3">
        <div class="flex items-center justify-between">
          <h2 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">Manual / Crypto Methods</h2>
          <button class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-indigo-500 hover:bg-indigo-600 text-white text-xs font-medium" @click="openCreateMethod">
            <Plus class="w-3.5 h-3.5" /> Add Method
          </button>
        </div>
        <div v-if="methods.length === 0" class="text-sm text-slate-500 dark:text-slate-400">No payment methods configured.</div>
        <div v-else class="divide-y divide-slate-100 dark:divide-white/5">
          <div v-for="m in methods" :key="m.id" class="py-3 flex items-center justify-between gap-3">
            <div class="min-w-0">
              <p class="text-sm font-medium text-slate-900 dark:text-white">{{ m.name }} <span class="text-xs text-slate-400">({{ m.type }})</span></p>
              <p class="text-xs text-slate-500 dark:text-slate-400">Min {{ m.minimumAmount }} – Max {{ m.maximumAmount }}, {{ m.chargesAmount }}{{ m.chargesType === 'percentage' ? '%' : '' }} charge</p>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="m.status === 'enabled' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'">{{ m.status }}</span>
              <button class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 dark:hover:bg-white/5 rounded-lg" @click="openEditMethod(m)"><Pencil class="w-4 h-4" /></button>
              <button class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-white/5" :class="m.status === 'enabled' ? 'text-amber-500' : 'text-emerald-500'" @click="toggleMethod(m)"><Power class="w-4 h-4" /></button>
              <button class="p-2 text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10 rounded-lg" @click="removeMethod(m)"><Trash2 class="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Method form modal -->
    <div v-if="showMethodForm" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 overflow-y-auto" @click.self="showMethodForm = false">
      <div class="bg-white dark:bg-[#0F1524] rounded-2xl border border-slate-200 dark:border-white/10 w-full max-w-lg p-6 my-8">
        <h2 class="text-lg font-semibold text-slate-900 dark:text-white mb-4">{{ editingId ? 'Edit Method' : 'New Method' }}</h2>
        <form class="space-y-3" @submit.prevent="saveMethod">
          <div class="grid grid-cols-2 gap-3">
            <input v-model="methodForm.name" required placeholder="Name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <select v-model="methodForm.type" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="deposit">Deposit</option>
              <option value="withdrawal">Withdrawal</option>
              <option value="both">Both</option>
            </select>
          </div>
          <input v-model="methodForm.methodType" placeholder="Method type (e.g. crypto, bank)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-3 gap-3">
            <div><label class="text-xs text-slate-500">Min</label><input v-model.number="methodForm.minimumAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Max</label><input v-model.number="methodForm.maximumAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
            <div><label class="text-xs text-slate-500">Charges</label><input v-model.number="methodForm.chargesAmount" type="number" step="0.01" class="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <select v-model="methodForm.chargesType" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="percentage">Percentage</option>
              <option value="fixed">Fixed</option>
            </select>
            <select v-model="methodForm.status" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white">
              <option value="enabled">Enabled</option>
              <option value="disabled">Disabled</option>
            </select>
          </div>
          <input v-model="methodForm.durationNote" placeholder="Duration note (e.g. 1-3 business days)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div class="grid grid-cols-2 gap-3">
            <input v-model="methodForm.bankName" placeholder="Bank name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="methodForm.accountName" placeholder="Account name" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="methodForm.accountNumber" placeholder="Account number" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
            <input v-model="methodForm.swiftCode" placeholder="Swift code" class="px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          </div>
          <input v-model="methodForm.walletAddress" placeholder="Wallet address (crypto)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <input v-model="methodForm.network" placeholder="Network (e.g. ERC20, TRC20)" class="w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-white/10 bg-white dark:bg-[#0B0F1A] text-sm text-slate-900 dark:text-white" />
          <div>
            <label class="text-xs text-slate-500 block mb-1">Barcode / QR image</label>
            <input type="file" accept="image/*" class="w-full text-sm text-slate-500" @change="(e) => barcodeFile = e.target.files[0]" />
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-white/5" @click="showMethodForm = false">Cancel</button>
            <button type="submit" :disabled="methodSaving" class="px-4 py-2 rounded-xl text-sm font-medium bg-gradient-to-r from-indigo-500 to-blue-600 text-white disabled:opacity-50">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
