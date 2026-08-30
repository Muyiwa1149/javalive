<script setup>
// Mirrors the source app's Controller::ref($id) — captures a sponsor's username from /ref/:username
// and stashes it for the registration form to pick up, then redirects to /register.
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

onMounted(() => {
  const username = route.params.username
  if (username) {
    localStorage.setItem('ref_by', username)
  }
  router.replace({ name: 'register' })
})
</script>

<template>
  <div class="min-h-screen bg-gray-900"></div>
</template>
