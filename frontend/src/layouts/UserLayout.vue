<script setup>
// Full sidebar/topbar/balance/notifications markup ported from layouts/dasht.blade.php in Phase 4.
</script>

<template>
  <div class="min-h-screen flex" data-theme="dark">
    <aside class="w-64 border-r">
      <!-- TODO(Phase 4): port layouts/dasht.blade.php sidebar -->
    </aside>
    <div class="flex-1 flex flex-col">
      <header class="border-b">
        <!-- TODO(Phase 4): port layouts/dasht.blade.php topbar -->
      </header>
      <main class="flex-1">
        <RouterView />
      </main>
    </div>
  </div>
</template>
