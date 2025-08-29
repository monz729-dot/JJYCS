<template>
  <transition name="fade-slide">
    <div
      v-if="show"
      :class="[
        'inline-message',
        `message-${type}`,
        { 'message-dismissible': dismissible }
      ]"
      role="alert"
    >
      <div class="message-content">
        <!-- Icon -->
        <div class="message-icon">
          <svg
            v-if="type === 'success'"
            class="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
          
          <svg
            v-else-if="type === 'error'"
            class="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          
          <svg
            v-else-if="type === 'warning'"
            class="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.982 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
          </svg>
          
          <svg
            v-else
            class="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>

        <!-- Message content -->
        <div class="message-text">
          <div v-if="title" class="message-title">{{ title }}</div>
          <div v-if="message" class="message-body">{{ message }}</div>
          <ul v-if="Array.isArray(errors) && errors.length > 0" class="message-list">
            <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
          </ul>
        </div>

        <!-- Dismiss button -->
        <button
          v-if="dismissible"
          @click="dismiss"
          class="message-dismiss"
          aria-label="닫기"
        >
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

      <!-- Actions -->
      <div v-if="actions && actions.length > 0" class="message-actions">
        <button
          v-for="action in actions"
          :key="action.label"
          @click="action.handler"
          :class="[
            'message-action-btn',
            action.style === 'primary' ? 'action-primary' : 'action-secondary'
          ]"
        >
          {{ action.label }}
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface MessageAction {
  label: string
  handler: () => void
  style?: 'primary' | 'secondary'
}

interface Props {
  type?: 'success' | 'error' | 'warning' | 'info'
  title?: string
  message?: string
  errors?: string[]
  show?: boolean
  dismissible?: boolean
  autoHide?: boolean
  duration?: number
  actions?: MessageAction[]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  show: true,
  dismissible: true,
  autoHide: false,
  duration: 5000
})

const emit = defineEmits<{
  dismiss: []
}>()

const show = ref(props.show)

const dismiss = () => {
  show.value = false
  emit('dismiss')
}

onMounted(() => {
  if (props.autoHide && props.duration > 0) {
    setTimeout(() => {
      dismiss()
    }, props.duration)
  }
})
</script>

<style scoped>
.inline-message {
  border-radius: 0.75rem;
  padding: 0.875rem;
  margin: 0.5rem 0;
  border-width: 1px;
  border-style: solid;
  transition: all 0.3s ease;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.message-icon {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.message-text {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.message-body {
  font-size: 0.875rem;
  line-height: 1.4;
}

.message-list {
  font-size: 0.875rem;
  line-height: 1.4;
  margin: 0;
  padding-left: 1rem;
}

.message-list li {
  margin-bottom: 0.125rem;
}

.message-dismiss {
  flex-shrink: 0;
  padding: 0.25rem;
  border-radius: 0.25rem;
  transition: all 0.2s ease;
  opacity: 0.7;
}

.message-dismiss:hover {
  opacity: 1;
  background-color: rgba(0, 0, 0, 0.1);
}

.message-actions {
  margin-top: 0.75rem;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.message-action-btn {
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.2s ease;
  border-width: 1px;
  border-style: solid;
}

.action-primary {
  background-color: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  color: #1e40af;
}

.action-primary:hover {
  background-color: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.5);
}

.action-secondary {
  background-color: rgba(107, 114, 128, 0.1);
  border-color: rgba(107, 114, 128, 0.3);
  color: #374151;
}

.action-secondary:hover {
  background-color: rgba(107, 114, 128, 0.2);
  border-color: rgba(107, 114, 128, 0.5);
}

/* Success styling */
.message-success {
  background-color: #f0fdf4;
  border-color: #bbf7d0;
  color: #166534;
}

.message-success .message-icon {
  color: #059669;
}

.message-success .message-title {
  color: #166534;
}

.message-success .message-dismiss:hover {
  background-color: rgba(34, 197, 94, 0.1);
}

/* Error styling */
.message-error {
  background-color: #fef2f2;
  border-color: #fecaca;
  color: #991b1b;
}

.message-error .message-icon {
  color: #dc2626;
}

.message-error .message-title {
  color: #991b1b;
}

.message-error .message-dismiss:hover {
  background-color: rgba(239, 68, 68, 0.1);
}

/* Warning styling */
.message-warning {
  background-color: #fffbeb;
  border-color: #fed7aa;
  color: #92400e;
}

.message-warning .message-icon {
  color: #f59e0b;
}

.message-warning .message-title {
  color: #92400e;
}

.message-warning .message-dismiss:hover {
  background-color: rgba(245, 158, 11, 0.1);
}

/* Info styling */
.message-info {
  background-color: #eff6ff;
  border-color: #bfdbfe;
  color: #1e40af;
}

.message-info .message-icon {
  color: #3b82f6;
}

.message-info .message-title {
  color: #1e40af;
}

.message-info .message-dismiss:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  margin: 0;
  padding: 0;
  max-height: 0;
}

/* Mobile responsiveness */
@media (max-width: 640px) {
  .inline-message {
    padding: 0.75rem;
  }
  
  .message-content {
    gap: 0.5rem;
  }
  
  .message-title,
  .message-body,
  .message-list {
    font-size: 0.8125rem;
  }
  
  .message-actions {
    margin-top: 0.5rem;
  }
  
  .message-action-btn {
    padding: 0.25rem 0.5rem;
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  .inline-message {
    border-width: 2px;
  }
  
  .message-title {
    font-weight: 700;
  }
}

/* Reduced motion support */
@media (prefers-reduced-motion: reduce) {
  .inline-message,
  .fade-slide-enter-active,
  .fade-slide-leave-active,
  .message-dismiss,
  .message-action-btn {
    transition: none;
  }
}
</style>