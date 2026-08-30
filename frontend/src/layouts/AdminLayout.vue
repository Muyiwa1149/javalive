<script setup>
// Tailwind rebuild of the Atlantis admin theme (sidebar/topbar/stat cards) done in Phase 5.
</script>

<template>
  <div class="min-h-screen flex">
    <aside class="w-64 border-r">
      <!-- TODO(Phase 5): rebuild admin/sidebar.blade.php in Tailwind -->
    </aside>
    <div class="flex-1 flex flex-col">
      <header class="border-b">
        <!-- TODO(Phase 5): rebuild admin/topmenu.blade.php in Tailwind -->
      </header>
      <main class="flex-1">
        <RouterView />
      </main>
    </div>
  </div>
</template>
