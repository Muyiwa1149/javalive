<script setup>
// Full header/nav/footer/ticker/chat-widget markup ported from layouts/base.blade.php in Phase 3.
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <header class="border-b">
      <!-- TODO(Phase 3): port layouts/base.blade.php header/nav -->
    </header>
    <main class="flex-1">
      <RouterView />
    </main>
    <footer class="border-t">
      <!-- TODO(Phase 3): port layouts/base.blade.php footer -->
    </footer>
  </div>
</template>
