<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container" :class="modalClass">
      <div class="modal-header">
        <div class="modal-icon-container" :class="iconContainerClass">
          <component :is="warningIcon" class="modal-icon" />
        </div>
        <div class="modal-title-container">
          <h2 class="modal-title">{{ title }}</h2>
          <p v-if="subtitle" class="modal-subtitle">{{ subtitle }}</p>
        </div>
        <button
          type="button"
          class="modal-close-btn"
          @click="$emit('close')"
        >
          <XMarkIcon class="close-icon" />
        </button>
      </div>

      <div class="modal-body">
        <slot name="content">
          <p class="modal-message">{{ message }}</p>
        </slot>

        <!-- Warning Details -->
        <div v-if="warnings.length > 0" class="warning-details">
          <h3 class="warning-details-title">{{ $t('warnings.details') }}</h3>
          <ul class="warning-list">
            <li
              v-for="warning in warnings"
              :key="warning.type"
              class="warning-item"
            >
              <div class="warning-item-header">
                <component :is="getWarningIcon(warning.type)" class="warning-item-icon" />
                <span class="warning-item-title">{{ getWarningTitle(warning.type) }}</span>
              </div>
              <p class="warning-item-message">{{ warning.message }}</p>
              <div v-if="warning.details" class="warning-item-details">
                <pre>{{ formatDetails(warning.details) }}</pre>
              </div>
            </li>
          </ul>
        </div>

        <!-- Actions -->
        <slot name="actions">
          <div class="modal-actions">
            <button
              v-if="showCancel"
              type="button"
              class="btn-cancel"
              @click="$emit('cancel')"
            >
              {{ cancelText || $t('common.cancel') }}
            </button>
            <button
              type="button"
              class="btn-confirm"
              :class="confirmButtonClass"
              @click="$emit('confirm')"
            >
              {{ confirmText || $t('common.confirm') }}
            </button>
          </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ExclamationTriangleIcon,
  ExclamationCircleIcon,
  InformationCircleIcon,
  XMarkIcon,
  TruckIcon,
  CurrencyDollarIcon,
  UserIcon,
  ClockIcon
} from '@heroicons/vue/24/outline'

interface Warning {
  type: string
  message: string
  details?: any
}

interface Props {
  show: boolean
  type?: 'warning' | 'error' | 'info' | 'success'
  title: string
  subtitle?: string
  message?: string
  warnings?: Warning[]
  showCancel?: boolean
  confirmText?: string
  cancelText?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'warning',
  warnings: () => [],
  showCancel: true
})

defineEmits<{
  close: []
  confirm: []
  cancel: []
}>()

const modalClass = computed(() => `modal-${props.type}`)

const iconContainerClass = computed(() => `icon-container-${props.type}`)

const confirmButtonClass = computed(() => `btn-${props.type}`)

const warningIcon = computed(() => {
  switch (props.type) {
    case 'error':
      return ExclamationCircleIcon
    case 'info':
      return InformationCircleIcon
    case 'success':
      return InformationCircleIcon
    default:
      return ExclamationTriangleIcon
  }
})

const getWarningIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    CBM_EXCEEDED: TruckIcon,
    AMOUNT_EXCEEDED_THB_1500: CurrencyDollarIcon,
    MEMBER_CODE_REQUIRED: UserIcon,
    APPROVAL_REQUIRED: ClockIcon,
    EMS_VALIDATION_FAILED: ExclamationTriangleIcon,
    HS_VALIDATION_FAILED: ExclamationTriangleIcon,
    default: ExclamationCircleIcon
  }
  return iconMap[type] || iconMap.default
}

const getWarningTitle = (type: string) => {
  const titleMap: Record<string, string> = {
    CBM_EXCEEDED: 'CBM 초과',
    AMOUNT_EXCEEDED_THB_1500: '금액 제한 초과',
    MEMBER_CODE_REQUIRED: '회원 코드 필요',
    APPROVAL_REQUIRED: '승인 대기',
    EMS_VALIDATION_FAILED: 'EMS 코드 오류',
    HS_VALIDATION_FAILED: 'HS 코드 오류'
  }
  return titleMap[type] || '경고'
}

const formatDetails = (details: any) => {
  if (typeof details === 'object') {
    return JSON.stringify(details, null, 2)
  }
  return String(details)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  padding: 1rem;
}

.modal-container {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 32rem;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  padding: 1.5rem 1.5rem 0 1.5rem;
  gap: 1rem;
}

.modal-icon-container {
  flex-shrink: 0;
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-container-warning {
  background: #fef3c7;
}

.icon-container-error {
  background: #fecaca;
}

.icon-container-info {
  background: #dbeafe;
}

.icon-container-success {
  background: #d1fae5;
}

.modal-icon {
  width: 1.5rem;
  height: 1.5rem;
}

.icon-container-warning .modal-icon {
  color: #f59e0b;
}

.icon-container-error .modal-icon {
  color: #ef4444;
}

.icon-container-info .modal-icon {
  color: #3b82f6;
}

.icon-container-success .modal-icon {
  color: #10b981;
}

.modal-title-container {
  flex: 1;
}

.modal-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.modal-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.modal-close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  color: #9ca3af;
  transition: color 0.2s;
}

.modal-close-btn:hover {
  color: #6b7280;
}

.close-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.modal-body {
  padding: 1rem 1.5rem 1.5rem 1.5rem;
}

.modal-message {
  color: #4b5563;
  line-height: 1.6;
  margin: 0 0 1rem 0;
}

.warning-details {
  margin: 1rem 0;
}

.warning-details-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.75rem 0;
}

.warning-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.warning-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
}

.warning-item-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.warning-item-icon {
  width: 1rem;
  height: 1rem;
  color: #f59e0b;
  flex-shrink: 0;
}

.warning-item-title {
  font-weight: 600;
  font-size: 0.875rem;
  color: #374151;
}

.warning-item-message {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.5rem 1.5rem;
}

.warning-item-details {
  margin-left: 1.5rem;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.25rem;
  padding: 0.5rem;
}

.warning-item-details pre {
  font-size: 0.75rem;
  color: #4b5563;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 640px) {
  .modal-actions {
    flex-direction: column-reverse;
  }
}

.btn-cancel,
.btn-confirm {
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-warning {
  background: #f59e0b;
  color: white;
}

.btn-warning:hover {
  background: #d97706;
}

.btn-error {
  background: #ef4444;
  color: white;
}

.btn-error:hover {
  background: #dc2626;
}

.btn-info {
  background: #3b82f6;
  color: white;
}

.btn-info:hover {
  background: #2563eb;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
}
</style>