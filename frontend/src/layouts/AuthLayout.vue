<script setup>
// Minimal shell for standalone auth screens (login/register/forgot-password/etc.) — mirrors the
// source app's layouts/guest1.blade.php, which itself is just a full-bleed dark background with
// no shared header/nav/footer; each auth page supplies its own centered card.
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { usePublicSettingsStore } from '@/stores/publicSettings'
import { injectChatWidget } from '@/lib/chatWidget'

const route = useRoute()
const settingsStore = usePublicSettingsStore()

onMounted(async () => {
  await settingsStore.ensureLoaded()
  // Admin sign-in screens share this layout but shouldn't show the customer chat widget.
  if (!route.name?.toString().startsWith('admin.')) {
    injectChatWidget(settingsStore.settings?.tawkToEmbed)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 font-sans antialiased">
    <RouterView />
  </div>
</template>
