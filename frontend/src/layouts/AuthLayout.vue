<script setup>
// Minimal shell for standalone auth screens (login/register/forgot-password/etc.) — mirrors the
// source app's layouts/guest1.blade.php, which itself is just a full-bleed dark background with
// no shared header/nav/footer; each auth page supplies its own centered card.
</script>

<template>
  <div class="min-h-screen bg-gray-900 font-sans antialiased">
    <RouterView />
  </div>
</template>
