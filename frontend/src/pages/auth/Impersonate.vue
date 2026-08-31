<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthUserStore } from '@/stores/authUser'

const route = useRoute()
const router = useRouter()
const authUser = useAuthUserStore()

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    router.replace({ name: 'login' })
    return
  }
  authUser.token = token
  localStorage.setItem('user_token', token)
  try {
    await authUser.fetchProfile()
  } catch {
    // fall through to dashboard regardless; the guard will bounce to login if the token is bad
  }
  router.replace({ name: 'user.dashboard' })
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center text-gray-500 dark:text-gray-400">
    Signing you in…
  </div>
</template>
