<template>
  <div class="order-create-view">
    <!-- 헤더 -->
    <div class="header">
      <h1>{{ $t('orders.create.title') }}</h1>
      <p class="subtitle">{{ $t('orders.create.subtitle') }}</p>
    </div>

    <!-- 진행 단계 표시 -->
    <div class="progress-steps">
      <div 
        v-for="(step, index) in steps" 
        :key="step.key"
        class="step"
        :class="{ 
          active: currentStep === index, 
          completed: currentStep > index 
        }"
      >
        <div class="step-number">{{ index + 1 }}</div>
        <div class="step-label">{{ $t(step.label) }}</div>
      </div>
    </div>

    <!-- 메인 폼 -->
    <form @submit.prevent="handleSubmit" class="order-form">
      <!-- 1단계: 수취인 정보 -->
      <div v-if="currentStep === 0" class="form-step">
        <h2>{{ $t('orders.create.recipient_info') }}</h2>
        
        <div class="form-group">
          <label for="recipientName" class="required">{{ $t('orders.recipient.name') }}</label>
          <input
            id="recipientName"
            v-model="formData.recipient.name"
            type="text"
            :placeholder="$t('orders.recipient.name_placeholder')"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="recipientPhone">{{ $t('orders.recipient.phone') }}</label>
          <input
            id="recipientPhone"
            v-model="formData.recipient.phone"
            type="tel"
            :placeholder="$t('orders.recipient.phone_placeholder')"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="recipientAddress" class="required">{{ $t('orders.recipient.address') }}</label>
          <textarea
            id="recipientAddress"
            v-model="formData.recipient.address"
            :placeholder="$t('orders.recipient.address_placeholder')"
            required
            rows="3"
            class="form-textarea"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="recipientCountry" class="required">{{ $t('orders.recipient.country') }}</label>
            <select
              id="recipientCountry"
              v-model="formData.recipient.country"
              required
              class="form-select"
            >
              <option value="">{{ $t('common.select_country') }}</option>
              <option value="TH">{{ $t('countries.TH') }}</option>
              <option value="VN">{{ $t('countries.VN') }}</option>
              <option value="CN">{{ $t('countries.CN') }}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="recipientZipCode">{{ $t('orders.recipient.zip_code') }}</label>
            <input
              id="recipientZipCode"
              v-model="formData.recipient.zipCode"
              type="text"
              :placeholder="$t('orders.recipient.zip_placeholder')"
              class="form-input"
            />
          </div>
        </div>
      </div>

      <!-- 2단계: 상품 정보 -->
      <div v-if="currentStep === 1" class="form-step">
        <div class="step-header">
          <h2>{{ $t('orders.create.items_info') }}</h2>
          <button type="button" @click="addItem" class="btn-add">
            {{ $t('orders.items.add_item') }}
          </button>
        </div>

        <div 
          v-for="(item, index) in formData.items" 
          :key="`item-${index}`"
          class="item-card"
        >
          <div class="item-header">
            <h3>{{ $t('orders.items.item_number', { number: index + 1 }) }}</h3>
            <button 
              v-if="formData.items.length > 1"
              type="button" 
              @click="removeItem(index)"
              class="btn-remove"
            >
              {{ $t('common.remove') }}
            </button>
          </div>

          <div class="form-row">
            <div class="form-group flex-2">
              <label :for="`itemName-${index}`" class="required">{{ $t('orders.items.name') }}</label>
              <input
                :id="`itemName-${index}`"
                v-model="item.name"
                type="text"
                :placeholder="$t('orders.items.name_placeholder')"
                required
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label :for="`itemQuantity-${index}`" class="required">{{ $t('orders.items.quantity') }}</label>
              <input
                :id="`itemQuantity-${index}`"
                v-model.number="item.quantity"
                type="number"
                min="1"
                required
                class="form-input"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label :for="`itemWeight-${index}`">{{ $t('orders.items.weight') }}</label>
              <input
                :id="`itemWeight-${index}`"
                v-model.number="item.weight"
                type="number"
                step="0.1"
                min="0"
                :placeholder="$t('orders.items.weight_placeholder')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label :for="`itemAmount-${index}`" class="required">{{ $t('orders.items.amount') }}</label>
              <input
                :id="`itemAmount-${index}`"
                v-model.number="item.amount"
                type="number"
                step="0.01"
                min="0"
                required
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label :for="`itemCurrency-${index}`" class="required">{{ $t('orders.items.currency') }}</label>
              <select
                :id="`itemCurrency-${index}`"
                v-model="item.currency"
                required
                class="form-select"
              >
                <option value="THB">THB</option>
                <option value="KRW">KRW</option>
                <option value="USD">USD</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label :for="`itemHsCode-${index}`">{{ $t('orders.items.hs_code') }}</label>
            <input
              :id="`itemHsCode-${index}`"
              v-model="item.hsCode"
              type="text"
              :placeholder="$t('orders.items.hs_code_placeholder')"
              class="form-input"
              @blur="validateHSCode(item, index)"
            />
            <div v-if="hsCodeValidation[index]?.message" class="validation-message">
              {{ hsCodeValidation[index].message }}
            </div>
          </div>
        </div>

        <!-- 총액 표시 -->
        <div class="total-amount">
          <div class="total-label">{{ $t('orders.create.total_amount') }}</div>
          <div class="total-value">
            {{ formatCurrency(totalAmount) }} {{ formData.items[0]?.currency || 'THB' }}
          </div>
        </div>
      </div>

      <!-- 3단계: 박스 정보 -->
      <div v-if="currentStep === 2" class="form-step">
        <div class="step-header">
          <h2>{{ $t('orders.create.boxes_info') }}</h2>
          <button type="button" @click="addBox" class="btn-add">
            {{ $t('orders.boxes.add_box') }}
          </button>
        </div>

        <div 
          v-for="(box, index) in formData.boxes" 
          :key="`box-${index}`"
          class="box-card"
        >
          <div class="box-header">
            <h3>{{ $t('orders.boxes.box_number', { number: index + 1 }) }}</h3>
            <button 
              v-if="formData.boxes.length > 1"
              type="button" 
              @click="removeBox(index)"
              class="btn-remove"
            >
              {{ $t('common.remove') }}
            </button>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label :for="`boxWidth-${index}`" class="required">
                {{ $t('orders.boxes.width') }} (cm)
              </label>
              <input
                :id="`boxWidth-${index}`"
                v-model.number="box.width"
                type="number"
                step="0.1"
                min="0.1"
                required
                class="form-input"
                @input="calculateCBM"
              />
            </div>

            <div class="form-group">
              <label :for="`boxHeight-${index}`" class="required">
                {{ $t('orders.boxes.height') }} (cm)
              </label>
              <input
                :id="`boxHeight-${index}`"
                v-model.number="box.height"
                type="number"
                step="0.1"
                min="0.1"
                required
                class="form-input"
                @input="calculateCBM"
              />
            </div>

            <div class="form-group">
              <label :for="`boxDepth-${index}`" class="required">
                {{ $t('orders.boxes.depth') }} (cm)
              </label>
              <input
                :id="`boxDepth-${index}`"
                v-model.number="box.depth"
                type="number"
                step="0.1"
                min="0.1"
                required
                class="form-input"
                @input="calculateCBM"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label :for="`boxWeight-${index}`">{{ $t('orders.boxes.weight') }} (kg)</label>
              <input
                :id="`boxWeight-${index}`"
                v-model.number="box.weight"
                type="number"
                step="0.1"
                min="0"
                :placeholder="$t('orders.boxes.weight_placeholder')"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <div class="cbm-display">
                <label>{{ $t('orders.boxes.cbm') }}</label>
                <div class="cbm-value">
                  {{ formatCBM(calculateBoxCBM(box)) }} m³
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- CBM 경고 -->
        <CBMWarning 
          :total-cbm="totalCBM" 
          :threshold="29" 
          @shipping-method-changed="handleShippingMethodChange"
        />
      </div>

      <!-- 4단계: 배송 정보 -->
      <div v-if="currentStep === 3" class="form-step">
        <h2>{{ $t('orders.create.shipping_info') }}</h2>

        <div class="form-group">
          <label>{{ $t('orders.shipping.preferred_type') }}</label>
          <div class="radio-group">
            <label class="radio-option">
              <input
                v-model="formData.shipping.preferredType"
                type="radio"
                value="sea"
                :disabled="totalCBM > 29"
              />
              <span>{{ $t('orders.shipping.sea') }}</span>
              <small v-if="totalCBM > 29" class="disabled-note">
                ({{ $t('orders.shipping.sea_disabled_cbm') }})
              </small>
            </label>
            <label class="radio-option">
              <input
                v-model="formData.shipping.preferredType"
                type="radio"
                value="air"
              />
              <span>{{ $t('orders.shipping.air') }}</span>
              <small v-if="totalCBM > 29" class="recommended-note">
                ({{ $t('orders.shipping.air_recommended') }})
              </small>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label>{{ $t('orders.shipping.urgency') }}</label>
          <div class="radio-group">
            <label class="radio-option">
              <input
                v-model="formData.shipping.urgency"
                type="radio"
                value="normal"
              />
              <span>{{ $t('orders.shipping.normal') }}</span>
            </label>
            <label class="radio-option">
              <input
                v-model="formData.shipping.urgency"
                type="radio"
                value="urgent"
              />
              <span>{{ $t('orders.shipping.urgent') }}</span>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label class="checkbox-option">
            <input
              v-model="formData.shipping.needsRepacking"
              type="checkbox"
            />
            <span>{{ $t('orders.shipping.needs_repacking') }}</span>
          </label>
        </div>

        <div class="form-group">
          <label for="specialInstructions">{{ $t('orders.shipping.special_instructions') }}</label>
          <textarea
            id="specialInstructions"
            v-model="formData.shipping.specialInstructions"
            :placeholder="$t('orders.shipping.instructions_placeholder')"
            rows="3"
            class="form-textarea"
          ></textarea>
        </div>
      </div>

      <!-- 하단 버튼들 -->
      <div class="form-actions">
        <button 
          v-if="currentStep > 0"
          type="button" 
          @click="prevStep"
          class="btn btn-secondary"
        >
          {{ $t('common.previous') }}
        </button>

        <button 
          v-if="currentStep < steps.length - 1"
          type="button" 
          @click="nextStep"
          class="btn btn-primary"
          :disabled="!canProceedToNext"
        >
          {{ $t('common.next') }}
        </button>

        <button 
          v-if="currentStep === steps.length - 1"
          type="submit"
          class="btn btn-primary"
          :disabled="loading || !canSubmit"
        >
          <LoadingSpinner v-if="loading" size="small" />
          {{ $t('orders.create.submit') }}
        </button>
      </div>
    </form>

    <!-- 경고 모달들 -->
    <WarningModal
      :show="showWarnings.length > 0"
      :warnings="showWarnings"
      @continue="handleWarningsContinue"
      @cancel="handleWarningsCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useOrdersStore } from '@/stores/orders'
import { useToast } from '@/composables/useToast'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import CBMWarning from '../components/CBMWarning.vue'
import WarningModal from '../components/WarningModal.vue'
import type { CreateOrderRequest, BusinessWarning } from '@/types/orders'

const router = useRouter()
const { t } = useI18n()
const ordersStore = useOrdersStore()
const { showToast } = useToast()

// 폼 단계 정의
const steps = [
  { key: 'recipient', label: 'orders.create.step_recipient' },
  { key: 'items', label: 'orders.create.step_items' },
  { key: 'boxes', label: 'orders.create.step_boxes' },
  { key: 'shipping', label: 'orders.create.step_shipping' }
]

const currentStep = ref(0)
const loading = ref(false)
const hsCodeValidation = ref<Record<number, { valid: boolean; message: string }>>({})
const showWarnings = ref<BusinessWarning[]>([])

// 폼 데이터
const formData = ref<CreateOrderRequest>({
  recipient: {
    name: '',
    phone: '',
    address: '',
    zipCode: '',
    country: ''
  },
  items: [{
    name: '',
    quantity: 1,
    weight: 0,
    amount: 0,
    currency: 'THB',
    category: '',
    hsCode: '',
    description: ''
  }],
  boxes: [{
    width: 0,
    height: 0,
    depth: 0,
    weight: 0,
    items: [0]
  }],
  shipping: {
    preferredType: 'sea',
    urgency: 'normal',
    needsRepacking: false,
    specialInstructions: ''
  },
  payment: {
    method: 'prepaid'
  }
})

// 계산된 값들
const totalAmount = computed(() => {
  return formData.value.items.reduce((sum, item) => sum + (item.amount || 0), 0)
})

const totalCBM = computed(() => {
  return formData.value.boxes.reduce((sum, box) => {
    return sum + calculateBoxCBM(box)
  }, 0)
})

const canProceedToNext = computed(() => {
  switch (currentStep.value) {
    case 0: // 수취인 정보
      return formData.value.recipient.name && 
             formData.value.recipient.address && 
             formData.value.recipient.country
    case 1: // 상품 정보
      return formData.value.items.every(item => 
        item.name && item.quantity > 0 && item.amount > 0 && item.currency)
    case 2: // 박스 정보
      return formData.value.boxes.every(box => 
        box.width > 0 && box.height > 0 && box.depth > 0)
    case 3: // 배송 정보
      return true
    default:
      return false
  }
})

const canSubmit = computed(() => {
  return canProceedToNext.value && !loading.value
})

// CBM 계산
const calculateBoxCBM = (box: any): number => {
  if (!box.width || !box.height || !box.depth) return 0
  return (box.width * box.height * box.depth) / 1000000
}

const calculateCBM = () => {
  // CBM 변경시 배송 방법 자동 조정
  if (totalCBM.value > 29 && formData.value.shipping.preferredType === 'sea') {
    formData.value.shipping.preferredType = 'air'
    showToast(t('orders.warnings.cbm_exceeded_auto_air'), 'warning')
  }
}

// 단계 이동
const nextStep = () => {
  if (canProceedToNext.value && currentStep.value < steps.length - 1) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 아이템/박스 추가/제거
const addItem = () => {
  formData.value.items.push({
    name: '',
    quantity: 1,
    weight: 0,
    amount: 0,
    currency: 'THB',
    category: '',
    hsCode: '',
    description: ''
  })
}

const removeItem = (index: number) => {
  if (formData.value.items.length > 1) {
    formData.value.items.splice(index, 1)
  }
}

const addBox = () => {
  formData.value.boxes.push({
    width: 0,
    height: 0,
    depth: 0,
    weight: 0,
    items: [formData.value.items.length - 1]
  })
}

const removeBox = (index: number) => {
  if (formData.value.boxes.length > 1) {
    formData.value.boxes.splice(index, 1)
  }
}

// HS 코드 검증
const validateHSCode = async (item: any, index: number) => {
  if (!item.hsCode) {
    delete hsCodeValidation.value[index]
    return
  }

  try {
    const result = await ordersStore.validateBusinessRules({ items: [item] })
    hsCodeValidation.value[index] = {
      valid: result.valid,
      message: result.valid ? 
        t('orders.validation.hs_code_valid') : 
        result.message || t('orders.validation.hs_code_invalid')
    }
  } catch (error) {
    hsCodeValidation.value[index] = {
      valid: true, // 폴백으로 통과 처리
      message: t('orders.validation.hs_code_unavailable')
    }
  }
}

// 배송 방법 변경 처리
const handleShippingMethodChange = (method: string) => {
  formData.value.shipping.preferredType = method
}

// 폼 제출
const handleSubmit = async () => {
  if (!canSubmit.value) return

  loading.value = true

  try {
    // 비즈니스 룰 사전 검증
    const validationResult = await ordersStore.validateBusinessRules({
      items: formData.value.items,
      boxes: formData.value.boxes,
      currency: formData.value.items[0]?.currency || 'THB',
      totalAmount: totalAmount.value
    })

    if (validationResult.warnings && validationResult.warnings.length > 0) {
      showWarnings.value = validationResult.warnings
      loading.value = false
      return
    }

    // 주문 생성
    const order = await ordersStore.createOrder(formData.value)
    
    showToast(t('orders.create.success'), 'success')
    
    // 주문 상세 페이지로 이동
    router.push({ name: 'OrderDetail', params: { id: order.orderId } })
    
  } catch (error: any) {
    console.error('Order creation error:', error)
    showToast(
      error.message || t('orders.create.error'), 
      'error'
    )
  } finally {
    loading.value = false
  }
}

// 경고 처리
const handleWarningsContinue = async () => {
  showWarnings.value = []
  
  // 경고를 무시하고 주문 생성 진행
  try {
    const order = await ordersStore.createOrder(formData.value)
    showToast(t('orders.create.success_with_warnings'), 'success')
    router.push({ name: 'OrderDetail', params: { id: order.orderId } })
  } catch (error: any) {
    showToast(error.message || t('orders.create.error'), 'error')
  }
}

const handleWarningsCancel = () => {
  showWarnings.value = []
}

// 포맷팅 함수들
const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('ko-KR').format(amount)
}

const formatCBM = (cbm: number): string => {
  return cbm.toFixed(6)
}

// CBM 변경 감지
watch(totalCBM, (newCBM) => {
  calculateCBM()
})
</script>

<style scoped>
.order-create-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.subtitle {
  color: #6b7280;
  font-size: 16px;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 15px;
  left: calc(50% + 20px);
  right: calc(-50% + 20px);
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.step.completed::after {
  background: #10b981;
}

.step-number {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  color: #6b7280;
  position: relative;
  z-index: 1;
}

.step.active .step-number {
  background: #3b82f6;
  color: white;
}

.step.completed .step-number {
  background: #10b981;
  color: white;
}

.step-label {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}

.step.active .step-label {
  color: #3b82f6;
  font-weight: 500;
}

.order-form {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-step h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-add {
  background: #10b981;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-add:hover {
  background: #059669;
}

.item-card, .box-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.item-header, .box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.item-header h3, .box-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.btn-remove {
  color: #ef4444;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-row .form-group.flex-2 {
  flex: 2;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}

.form-group label.required::after {
  content: ' *';
  color: #ef4444;
}

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.15s ease;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}

.radio-option, .checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

.radio-option input[type="radio"], 
.checkbox-option input[type="checkbox"] {
  margin: 0;
}

.disabled-note {
  color: #6b7280;
  font-style: italic;
}

.recommended-note {
  color: #10b981;
  font-style: italic;
}

.total-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f3f4f6;
  border-radius: 8px;
  margin-top: 20px;
}

.total-label {
  font-weight: 500;
  color: #374151;
}

.total-value {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.cbm-display {
  display: flex;
  flex-direction: column;
}

.cbm-value {
  font-size: 16px;
  font-weight: 600;
  color: #3b82f6;
  margin-top: 4px;
}

.validation-message {
  font-size: 12px;
  margin-top: 4px;
  padding: 4px 8px;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  border-color: #2563eb;
}

.btn-secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #f9fafb;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .order-create-view {
    padding: 16px;
  }
  
  .progress-steps {
    padding: 0 10px;
  }
  
  .step-label {
    font-size: 10px;
  }
  
  .order-form {
    padding: 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .radio-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>