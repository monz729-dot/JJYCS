<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(async () => {
  // Initialize auth state listener first
  authStore.initializeAuth()
  
  // Check authentication status on app mount
  try {
    await authStore.fetchUserProfile()
  } catch (error) {
    // No user session found
  }
})
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>