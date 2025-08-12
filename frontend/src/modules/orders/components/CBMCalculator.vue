<template>
  <div class="cbm-calculator">
    <div class="calculator-header">
      <h3 class="calculator-title">{{ $t('cbm.calculator.title') }}</h3>
      <p class="calculator-subtitle">{{ $t('cbm.calculator.subtitle') }}</p>
    </div>

    <div class="boxes-container">
      <!-- Box List -->
      <div v-for="(box, index) in localBoxes" :key="box.id" class="box-item">
        <div class="box-header">
          <h4 class="box-title">
            {{ $t('cbm.calculator.box') }} {{ index + 1 }}
          </h4>
          <button
            v-if="localBoxes.length > 1"
            type="button"
            class="remove-box-btn"
            @click="removeBox(index)"
            :aria-label="$t('cbm.calculator.remove_box')"
          >
            <TrashIcon class="remove-icon" />
          </button>
        </div>

        <div class="dimensions-grid">
          <div class="dimension-input">
            <label class="dimension-label">
              {{ $t('cbm.calculator.width') }} (cm)
            </label>
            <input
              v-model.number="box.width"
              type="number"
              min="0.1"
              step="0.1"
              class="dimension-field"
              :class="{ 'error': box.errors?.width }"
              @input="validateAndCalculate"
              @blur="formatDimension(box, 'width')"
            >
            <span v-if="box.errors?.width" class="error-message">
              {{ box.errors.width }}
            </span>
          </div>

          <div class="dimension-input">
            <label class="dimension-label">
              {{ $t('cbm.calculator.height') }} (cm)
            </label>
            <input
              v-model.number="box.height"
              type="number"
              min="0.1"
              step="0.1"
              class="dimension-field"
              :class="{ 'error': box.errors?.height }"
              @input="validateAndCalculate"
              @blur="formatDimension(box, 'height')"
            >
            <span v-if="box.errors?.height" class="error-message">
              {{ box.errors.height }}
            </span>
          </div>

          <div class="dimension-input">
            <label class="dimension-label">
              {{ $t('cbm.calculator.depth') }} (cm)
            </label>
            <input
              v-model.number="box.depth"
              type="number"
              min="0.1"
              step="0.1"
              class="dimension-field"
              :class="{ 'error': box.errors?.depth }"
              @input="validateAndCalculate"
              @blur="formatDimension(box, 'depth')"
            >
            <span v-if="box.errors?.depth" class="error-message">
              {{ box.errors.depth }}
            </span>
          </div>
        </div>

        <!-- Box CBM Result -->
        <div class="box-result">
          <div class="cbm-display">
            <span class="cbm-label">CBM:</span>
            <span class="cbm-value" :class="{ 'high-cbm': box.cbm > 5 }">
              {{ formatCBM(box.cbm) }} m³
            </span>
          </div>
          
          <!-- Volume breakdown -->
          <div class="volume-breakdown" v-if="showBreakdown">
            <span class="breakdown-text">
              {{ box.width }} × {{ box.height }} × {{ box.depth }} = 
              {{ formatNumber(box.width * box.height * box.depth) }} cm³
            </span>
          </div>
        </div>
      </div>

      <!-- Add Box Button -->
      <button
        type="button"
        class="add-box-btn"
        @click="addBox"
        :disabled="localBoxes.length >= maxBoxes"
      >
        <PlusIcon class="add-icon" />
        {{ $t('cbm.calculator.add_box') }}
        <span class="box-count">({{ localBoxes.length }}/{{ maxBoxes }})</span>
      </button>
    </div>

    <!-- Total Results -->
    <div class="total-results">
      <div class="total-summary">
        <div class="summary-item">
          <span class="summary-label">{{ $t('cbm.calculator.total_boxes') }}:</span>
          <span class="summary-value">{{ localBoxes.length }}</span>
        </div>
        
        <div class="summary-item main-result">
          <span class="summary-label">{{ $t('cbm.calculator.total_cbm') }}:</span>
          <span class="summary-value total-cbm" :class="getCBMClass(totalCBM)">
            {{ formatCBM(totalCBM) }} m³
          </span>
        </div>

        <div class="summary-item">
          <span class="summary-label">{{ $t('cbm.calculator.threshold') }}:</span>
          <span class="summary-value">{{ threshold }} m³</span>
        </div>
      </div>

      <!-- Threshold Status -->
      <div class="threshold-status" :class="getThresholdStatusClass()">
        <div class="status-indicator">
          <component :is="getThresholdIcon()" class="status-icon" />
          <span class="status-text">{{ getThresholdStatusText() }}</span>
        </div>
        
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :class="getProgressClass()"
            :style="{ width: getProgressWidth() }"
          ></div>
        </div>
        
        <div class="threshold-details">
          <span class="remaining-capacity" v-if="totalCBM < threshold">
            {{ $t('cbm.calculator.remaining') }}: 
            {{ formatCBM(threshold - totalCBM) }} m³
          </span>
          <span class="exceeded-amount" v-else-if="totalCBM > threshold">
            {{ $t('cbm.calculator.exceeded') }}: 
            {{ formatCBM(totalCBM - threshold) }} m³
          </span>
        </div>
      </div>
    </div>

    <!-- Conversion Notice -->
    <div v-if="showConversionNotice" class="conversion-notice">
      <ExclamationTriangleIcon class="notice-icon" />
      <div class="notice-content">
        <h4 class="notice-title">{{ $t('cbm.calculator.auto_conversion') }}</h4>
        <p class="notice-description">
          {{ $t('cbm.calculator.conversion_description') }}
        </p>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions" v-if="showActions">
      <button
        type="button"
        class="action-btn secondary"
        @click="resetCalculator"
      >
        <ArrowPathIcon class="action-icon" />
        {{ $t('cbm.calculator.reset') }}
      </button>
      
      <button
        type="button"
        class="action-btn secondary"
        @click="$emit('save-template')"
        v-if="totalCBM > 0"
      >
        <BookmarkIcon class="action-icon" />
        {{ $t('cbm.calculator.save_template') }}
      </button>
      
      <button
        type="button"
        class="action-btn primary"
        @click="applyCalculation"
        :disabled="!isValid || totalCBM === 0"
      >
        <CheckIcon class="action-icon" />
        {{ $t('cbm.calculator.apply') }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import {
  TrashIcon,
  PlusIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ExclamationCircleIcon,
  ArrowPathIcon,
  BookmarkIcon,
  CheckIcon
} from '@heroicons/vue/24/outline'

interface Box {
  id: string
  width: number
  height: number
  depth: number
  cbm: number
  errors?: {
    width?: string
    height?: string
    depth?: string
  }
}

interface Props {
  modelValue?: Box[]
  threshold?: number
  maxBoxes?: number
  showBreakdown?: boolean
  showActions?: boolean
  autoCalculate?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  threshold: 29,
  maxBoxes: 10,
  showBreakdown: true,
  showActions: true,
  autoCalculate: true
})

const emit = defineEmits<{
  'update:modelValue': [boxes: Box[]]
  'cbm-change': [totalCBM: number, exceedsThreshold: boolean]
  'save-template': []
  'apply': [boxes: Box[], totalCBM: number]
}>()

// Local state
const localBoxes = ref<Box[]>([createEmptyBox()])

// Computed properties
const totalCBM = computed(() => {
  return localBoxes.value.reduce((sum, box) => sum + (box.cbm || 0), 0)
})

const showConversionNotice = computed(() => {
  return totalCBM.value > props.threshold
})

const isValid = computed(() => {
  return localBoxes.value.every(box => 
    box.width > 0 && box.height > 0 && box.depth > 0 &&
    !box.errors?.width && !box.errors?.height && !box.errors?.depth
  )
})

// Watch for external changes
watch(() => props.modelValue, (newValue) => {
  if (newValue && newValue.length > 0) {
    localBoxes.value = [...newValue]
  }
}, { immediate: true })

// Watch for internal changes
watch(localBoxes, (newBoxes) => {
  if (props.autoCalculate) {
    emit('update:modelValue', [...newBoxes])
    emit('cbm-change', totalCBM.value, totalCBM.value > props.threshold)
  }
}, { deep: true })

// Methods
function createEmptyBox(): Box {
  return {
    id: generateBoxId(),
    width: 0,
    height: 0,
    depth: 0,
    cbm: 0
  }
}

function generateBoxId(): string {
  return 'box_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

function addBox() {
  if (localBoxes.value.length < props.maxBoxes) {
    localBoxes.value.push(createEmptyBox())
  }
}

function removeBox(index: number) {
  if (localBoxes.value.length > 1) {
    localBoxes.value.splice(index, 1)
  }
}

function validateAndCalculate() {
  nextTick(() => {
    localBoxes.value.forEach(box => {
      validateBox(box)
      calculateBoxCBM(box)
    })
  })
}

function validateBox(box: Box) {
  box.errors = {}
  
  if (!box.width || box.width <= 0) {
    box.errors.width = '폭을 입력하세요 (0보다 큰 값)'
  } else if (box.width > 1000) {
    box.errors.width = '폭이 너무 큽니다 (최대 1000cm)'
  }
  
  if (!box.height || box.height <= 0) {
    box.errors.height = '높이를 입력하세요 (0보다 큰 값)'
  } else if (box.height > 1000) {
    box.errors.height = '높이가 너무 큽니다 (최대 1000cm)'
  }
  
  if (!box.depth || box.depth <= 0) {
    box.errors.depth = '깊이를 입력하세요 (0보다 큰 값)'
  } else if (box.depth > 1000) {
    box.errors.depth = '깊이가 너무 큽니다 (최대 1000cm)'
  }
}

function calculateBoxCBM(box: Box) {
  if (box.width > 0 && box.height > 0 && box.depth > 0) {
    box.cbm = (box.width * box.height * box.depth) / 1_000_000
  } else {
    box.cbm = 0
  }
}

function formatDimension(box: Box, dimension: keyof Pick<Box, 'width' | 'height' | 'depth'>) {
  if (box[dimension] && box[dimension] > 0) {
    box[dimension] = Math.round(box[dimension] * 10) / 10 // 소수점 1자리로 반올림
  }
}

function formatCBM(cbm: number): string {
  return cbm.toFixed(3)
}

function formatNumber(num: number): string {
  return new Intl.NumberFormat().format(Math.round(num))
}

function getCBMClass(cbm: number) {
  if (cbm === 0) return 'cbm-zero'
  if (cbm > props.threshold) return 'cbm-exceeded'
  if (cbm > props.threshold * 0.8) return 'cbm-warning'
  return 'cbm-normal'
}

function getThresholdStatusClass() {
  const percentage = (totalCBM.value / props.threshold) * 100
  
  if (percentage > 100) return 'status-exceeded'
  if (percentage > 90) return 'status-warning'
  return 'status-normal'
}

function getThresholdIcon() {
  if (totalCBM.value > props.threshold) return ExclamationCircleIcon
  if (totalCBM.value > props.threshold * 0.9) return ExclamationTriangleIcon
  return CheckCircleIcon
}

function getThresholdStatusText() {
  if (totalCBM.value > props.threshold) {
    return '임계값 초과 - 항공배송 전환'
  }
  if (totalCBM.value > props.threshold * 0.9) {
    return '임계값 근접 - 주의 필요'
  }
  return '정상 범위'
}

function getProgressClass() {
  const percentage = (totalCBM.value / props.threshold) * 100
  
  if (percentage > 100) return 'progress-exceeded'
  if (percentage > 90) return 'progress-warning'
  return 'progress-normal'
}

function getProgressWidth() {
  const percentage = Math.min((totalCBM.value / props.threshold) * 100, 100)
  return `${percentage}%`
}

function resetCalculator() {
  localBoxes.value = [createEmptyBox()]
}

function applyCalculation() {
  if (isValid.value) {
    emit('apply', [...localBoxes.value], totalCBM.value)
  }
}

// Initialize
if (!props.modelValue || props.modelValue.length === 0) {
  validateAndCalculate()
}
</script>

<style scoped>
.cbm-calculator {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.calculator-header {
  margin-bottom: 1.5rem;
}

.calculator-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.calculator-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.boxes-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.box-item {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1rem;
  background: #fafafa;
}

.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.box-title {
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.remove-box-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  color: #ef4444;
  transition: all 0.2s;
}

.remove-box-btn:hover {
  background: rgba(239, 68, 68, 0.1);
}

.remove-icon {
  width: 1rem;
  height: 1rem;
}

.dimensions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

@media (max-width: 640px) {
  .dimensions-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}

.dimension-input {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.dimension-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.dimension-field {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.dimension-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.dimension-field.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.error-message {
  font-size: 0.75rem;
  color: #ef4444;
}

.box-result {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.cbm-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cbm-label {
  font-weight: 500;
  color: #374151;
}

.cbm-value {
  font-weight: 600;
  font-size: 1.125rem;
  color: #1f2937;
}

.cbm-value.high-cbm {
  color: #f59e0b;
}

.volume-breakdown {
  font-size: 0.75rem;
  color: #6b7280;
  font-family: monospace;
}

.add-box-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  border: 2px dashed #d1d5db;
  border-radius: 0.75rem;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.add-box-btn:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
}

.add-box-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.box-count {
  font-size: 0.875rem;
  opacity: 0.7;
}

.total-results {
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.total-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

@media (max-width: 640px) {
  .total-summary {
    grid-template-columns: 1fr;
    text-align: center;
  }
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: white;
  border-radius: 0.5rem;
}

.summary-item.main-result {
  border: 2px solid #e5e7eb;
}

.summary-label {
  font-size: 0.875rem;
  color: #6b7280;
}

.summary-value {
  font-weight: 600;
  color: #1f2937;
}

.total-cbm {
  font-size: 1.25rem;
}

.total-cbm.cbm-exceeded {
  color: #ef4444;
}

.total-cbm.cbm-warning {
  color: #f59e0b;
}

.total-cbm.cbm-normal {
  color: #10b981;
}

.threshold-status {
  background: white;
  border-radius: 0.5rem;
  padding: 1rem;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.status-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.status-normal .status-icon {
  color: #10b981;
}

.status-warning .status-icon {
  color: #f59e0b;
}

.status-exceeded .status-icon {
  color: #ef4444;
}

.status-text {
  font-weight: 500;
  color: #374151;
}

.progress-bar {
  width: 100%;
  height: 0.5rem;
  background: #e5e7eb;
  border-radius: 0.25rem;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.progress-normal {
  background: linear-gradient(90deg, #10b981, #34d399);
}

.progress-warning {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

.progress-exceeded {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.threshold-details {
  text-align: center;
  font-size: 0.875rem;
}

.remaining-capacity {
  color: #059669;
  font-weight: 500;
}

.exceeded-amount {
  color: #dc2626;
  font-weight: 500;
}

.conversion-notice {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 0.5rem;
  margin-bottom: 1.5rem;
}

.notice-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #ef4444;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #b91c1c;
  margin: 0 0 0.25rem 0;
}

.notice-description {
  font-size: 0.875rem;
  color: #7f1d1d;
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .quick-actions {
    flex-direction: column;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.action-btn.primary {
  background: #3b82f6;
  color: white;
}

.action-btn.primary:hover:not(:disabled) {
  background: #2563eb;
}

.action-btn.secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.action-btn.secondary:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-icon {
  width: 1rem;
  height: 1rem;
}
</style>