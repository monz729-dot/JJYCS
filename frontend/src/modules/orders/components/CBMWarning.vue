<template>
  <div class="cbm-warning" :class="warningClass">
    <div class="warning-header">
      <ExclamationTriangleIcon class="warning-icon" />
      <h3 class="warning-title">{{ $t('cbm.warning.title') }}</h3>
    </div>
    
    <div class="warning-content">
      <div class="cbm-info">
        <p class="current-cbm">
          {{ $t('cbm.current') }}: <strong>{{ formatCBM(currentCBM) }} m³</strong>
        </p>
        <p class="threshold-info">
          {{ $t('cbm.threshold') }}: <strong>{{ formatCBM(threshold) }} m³</strong>
        </p>
      </div>
      
      <div v-if="exceedsThreshold" class="conversion-warning">
        <div class="conversion-badge">
          <ArrowRightIcon class="conversion-icon" />
          <span class="from-type">{{ $t('shipping.sea') }}</span>
          <ArrowRightIcon class="conversion-icon" />
          <span class="to-type">{{ $t('shipping.air') }}</span>
        </div>
        <p class="conversion-message">
          {{ $t('cbm.warning.auto_conversion') }}
        </p>
      </div>
      
      <div class="cost-impact" v-if="costImpact">
        <p class="cost-info">
          <span class="cost-label">{{ $t('cbm.warning.cost_impact') }}:</span>
          <span class="cost-amount" :class="{ 'cost-increase': costImpact > 0 }">
            {{ costImpact > 0 ? '+' : '' }}{{ formatCurrency(costImpact) }}
          </span>
        </p>
      </div>
    </div>
    
    <div class="warning-actions" v-if="showActions">
      <button
        type="button"
        class="btn-secondary"
        @click="$emit('modify')"
      >
        {{ $t('cbm.warning.modify_boxes') }}
      </button>
      <button
        type="button"
        class="btn-primary"
        @click="$emit('continue')"
      >
        {{ $t('cbm.warning.continue_anyway') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ExclamationTriangleIcon, ArrowRightIcon } from '@heroicons/vue/24/outline'

interface Props {
  currentCBM: number
  threshold?: number
  costImpact?: number
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  threshold: 29.0,
  costImpact: 0,
  showActions: true
})

defineEmits<{
  modify: []
  continue: []
}>()

const exceedsThreshold = computed(() => props.currentCBM > props.threshold)

const warningClass = computed(() => ({
  'warning-exceeded': exceedsThreshold.value,
  'warning-approaching': props.currentCBM > props.threshold * 0.9 && !exceedsThreshold.value
}))

const formatCBM = (value: number) => {
  return value.toFixed(3)
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: 'THB',
    minimumFractionDigits: 0
  }).format(amount)
}
</script>

<style scoped>
.cbm-warning {
  border-radius: 0.75rem;
  padding: 1.5rem;
  border: 2px solid;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.warning-approaching {
  border-color: #f59e0b;
  background: #fffbeb;
}

.warning-exceeded {
  border-color: #ef4444;
  background: #fef2f2;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.warning-icon {
  width: 1.5rem;
  height: 1.5rem;
  flex-shrink: 0;
}

.warning-approaching .warning-icon {
  color: #f59e0b;
}

.warning-exceeded .warning-icon {
  color: #ef4444;
}

.warning-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.warning-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cbm-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

@media (max-width: 640px) {
  .cbm-info {
    grid-template-columns: 1fr;
  }
}

.current-cbm,
.threshold-info {
  font-size: 0.875rem;
  margin: 0;
  color: #4b5563;
}

.current-cbm strong {
  color: #1f2937;
  font-weight: 600;
}

.threshold-info strong {
  color: #6b7280;
}

.conversion-warning {
  padding: 1rem;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 0.5rem;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.conversion-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.conversion-icon {
  width: 1rem;
  height: 1rem;
  color: #ef4444;
}

.from-type {
  color: #6b7280;
  text-decoration: line-through;
}

.to-type {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.conversion-message {
  text-align: center;
  font-size: 0.875rem;
  color: #7f1d1d;
  margin: 0;
}

.cost-impact {
  background: rgba(245, 158, 11, 0.1);
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.cost-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0;
  font-size: 0.875rem;
}

.cost-label {
  color: #92400e;
  font-weight: 500;
}

.cost-amount {
  font-weight: 600;
}

.cost-increase {
  color: #dc2626;
}

.warning-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 640px) {
  .warning-actions {
    flex-direction: column;
  }
}

.btn-primary,
.btn-secondary {
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.btn-secondary {
  background-color: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background-color: #f9fafb;
  border-color: #9ca3af;
}
</style>