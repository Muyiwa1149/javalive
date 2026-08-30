import { ref } from 'vue'

/** Lightweight client-side CoinGecko price poll, mirrors the source dashboard's Alpine `cryptoPrices()`. */
export function useCryptoPrices(coins = ['bitcoin', 'ethereum'], intervalMs = 30000) {
  const prices = ref({})
  let timer = null

  async function fetchPrices() {
    try {
      const ids = coins.join(',')
      const res = await fetch(`https://api.coingecko.com/api/v3/simple/price?ids=${ids}&vs_currencies=usd&include_24hr_change=true`)
      if (!res.ok) return
      prices.value = await res.json()
    } catch {
      // Silently ignore — price ticker degrades gracefully to "..." on failure.
    }
  }

  function start() {
    fetchPrices()
    timer = setInterval(fetchPrices, intervalMs)
  }
  function stop() {
    if (timer) clearInterval(timer)
  }

  return { prices, start, stop, fetchPrices }
}
