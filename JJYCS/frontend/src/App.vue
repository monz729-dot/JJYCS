<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/components/ui/AppLayout.vue'

const authStore = useAuthStore()

onMounted(() => {
  // Initialize any app-level setup
  console.log('YSC LMS App initialized')
})
</script>

<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <AppLayout />
  </div>
</template>

<style>
/* Global styles are imported in main.ts */
#app {
  text-align: left; /* Override default center alignment */
}

/* PWA viewport fixes */
@media screen and (max-width: 768px) {
  body {
    -webkit-text-size-adjust: 100%;
  }
}

/* iOS Safari fixes */
@supports (-webkit-touch-callout: none) {
  #app {
    min-height: -webkit-fill-available;
  }
}

/* Loading states */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Toast notifications position */
.toast-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
}

@media (max-width: 640px) {
  .toast-container {
    left: 1rem;
    right: 1rem;
    top: 1rem;
  }
}
</style>
