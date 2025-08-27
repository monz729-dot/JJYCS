<template>
  <div 
    class="fixed top-4 right-4 z-50 space-y-2"
    style="max-width: 400px;"
  >
    <TransitionGroup name="toast" tag="div">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="getToastClasses(toast.type)"
        class="relative bg-white rounded-lg shadow-lg border-l-4 p-4 mb-2 max-w-sm"
        role="alert"
      >
        <!-- Close button -->
        <button
          @click="removeToast(toast.id)"
          class="absolute top-2 right-2 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>

        <!-- Icon -->
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <svg
              v-if="toast.type === 'success'"
              class="w-5 h-5 text-green-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
            
            <svg
              v-else-if="toast.type === 'error'"
              class="w-5 h-5 text-red-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            
            <svg
              v-else-if="toast.type === 'warning'"
              class="w-5 h-5 text-yellow-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.982 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
            </svg>
            
            <svg
              v-else
              class="w-5 h-5 text-blue-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>

          <div class="ml-3 flex-1 pr-4">
            <!-- Title -->
            <h4 class="text-sm font-medium text-gray-900">
              {{ toast.title }}
            </h4>

            <!-- Message -->
            <p v-if="toast.message" class="mt-1 text-sm text-gray-600">
              {{ toast.message }}
            </p>

            <!-- Actions -->
            <div v-if="toast.actions && toast.actions.length > 0" class="mt-3 flex space-x-2">
              <button
                v-for="action in toast.actions"
                :key="action.label"
                @click="action.action"
                :class="[
                  'text-xs px-2 py-1 rounded border transition-colors',
                  action.style === 'primary' 
                    ? 'bg-primary-600 text-white border-primary-600 hover:bg-primary-700' 
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                ]"
              >
                {{ action.label }}
              </button>
            </div>
          </div>
        </div>

        <!-- Progress bar for timed toasts -->
        <div
          v-if="toast.duration && toast.duration > 0"
          class="absolute bottom-0 left-0 h-1 bg-gray-200 w-full rounded-b-lg overflow-hidden"
        >
          <div
            class="h-full transition-all ease-linear"
            :class="getProgressBarClass(toast.type)"
            :style="`animation: toast-progress ${toast.duration}ms linear;`"
          ></div>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, removeToast } = useToast()

const getToastClasses = (type: string) => {
  const classes = {
    'success': 'border-l-green-500 bg-green-50',
    'error': 'border-l-red-500 bg-red-50',
    'warning': 'border-l-yellow-500 bg-yellow-50',
    'info': 'border-l-blue-500 bg-blue-50'
  }
  return classes[type as keyof typeof classes] || classes.info
}

const getProgressBarClass = (type: string) => {
  const classes = {
    'success': 'bg-green-500',
    'error': 'bg-red-500',
    'warning': 'bg-yellow-500',
    'info': 'bg-blue-500'
  }
  return classes[type as keyof typeof classes] || classes.info
}
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.toast-move {
  transition: transform 0.3s ease;
}

@keyframes toast-progress {
  0% {
    width: 100%;
  }
  100% {
    width: 0%;
  }
}
</style>