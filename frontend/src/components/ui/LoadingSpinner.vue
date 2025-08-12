<template>
  <div class="loading-spinner" :class="{ 'loading-overlay': overlay }">
    <div class="spinner" :class="sizeClass">
      <div class="spinner-circle"></div>
    </div>
    <p v-if="message" class="loading-message">{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  size?: 'small' | 'medium' | 'large'
  overlay?: boolean
  message?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  overlay: false
})

const sizeClass = computed(() => `spinner-${props.size}`)
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.spinner {
  position: relative;
}

.spinner-circle {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-small .spinner-circle {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

.spinner-medium .spinner-circle {
  width: 40px;
  height: 40px;
  border-width: 4px;
}

.spinner-large .spinner-circle {
  width: 60px;
  height: 60px;
  border-width: 6px;
}

.loading-message {
  color: #6b7280;
  font-size: 0.875rem;
  text-align: center;
  margin: 0;
}

.loading-overlay .loading-message {
  color: #ffffff;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>