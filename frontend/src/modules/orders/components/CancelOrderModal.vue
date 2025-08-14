<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
      <!-- 헤더 -->
      <div class="flex items-center mb-4">
        <div class="flex-shrink-0">
          <ExclamationTriangleIcon class="h-6 w-6 text-red-500" />
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-gray-900">
            {{ $t('orders.cancel.title') }}
          </h3>
        </div>
      </div>

      <!-- 내용 -->
      <div class="mb-6">
        <p class="text-sm text-gray-600 mb-4">
          {{ $t('orders.cancel.confirmation_message') }}
        </p>
        
        <div v-if="order" class="bg-gray-50 rounded-lg p-4">
          <div class="text-sm space-y-1">
            <div>
              <span class="font-medium">{{ $t('orders.order_number') }}:</span>
              {{ order.order_number }}
            </div>
            <div>
              <span class="font-medium">{{ $t('orders.status') }}:</span>
              <span :class="getStatusColor(order.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                {{ $t(`orders.status.${order.status}`) }}
              </span>
            </div>
            <div v-if="order.total_amount">
              <span class="font-medium">{{ $t('orders.total_amount') }}:</span>
              {{ formatCurrency(order.total_amount) }}
            </div>
          </div>
        </div>

        <!-- 취소 사유 -->
        <div class="mt-4">
          <label for="cancel-reason" class="block text-sm font-medium text-gray-700 mb-2">
            {{ $t('orders.cancel.reason') }}
          </label>
          <select
            id="cancel-reason"
            v-model="cancelReason"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            :class="{ 'border-red-300': errors.reason }"
          >
            <option value="">{{ $t('orders.cancel.select_reason') }}</option>
            <option value="customer_request">{{ $t('orders.cancel.reasons.customer_request') }}</option>
            <option value="shipping_address_change">{{ $t('orders.cancel.reasons.shipping_address_change') }}</option>
            <option value="product_unavailable">{{ $t('orders.cancel.reasons.product_unavailable') }}</option>
            <option value="payment_issue">{{ $t('orders.cancel.reasons.payment_issue') }}</option>
            <option value="other">{{ $t('orders.cancel.reasons.other') }}</option>
          </select>
          <p v-if="errors.reason" class="mt-1 text-sm text-red-600">
            {{ errors.reason }}
          </p>
        </div>

        <!-- 기타 사유 입력 -->
        <div v-if="cancelReason === 'other'" class="mt-4">
          <label for="cancel-details" class="block text-sm font-medium text-gray-700 mb-2">
            {{ $t('orders.cancel.details') }}
          </label>
          <textarea
            id="cancel-details"
            v-model="cancelDetails"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            :class="{ 'border-red-300': errors.details }"
            :placeholder="$t('orders.cancel.details_placeholder')"
          />
          <p v-if="errors.details" class="mt-1 text-sm text-red-600">
            {{ errors.details }}
          </p>
        </div>

        <!-- 주의사항 -->
        <div class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
          <div class="flex">
            <ExclamationTriangleIcon class="h-5 w-5 text-yellow-400 flex-shrink-0" />
            <div class="ml-3">
              <h4 class="text-sm font-medium text-yellow-800">
                {{ $t('orders.cancel.notice_title') }}
              </h4>
              <ul class="mt-1 text-sm text-yellow-700 space-y-1">
                <li>• {{ $t('orders.cancel.notice_1') }}</li>
                <li>• {{ $t('orders.cancel.notice_2') }}</li>
                <li>• {{ $t('orders.cancel.notice_3') }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 버튼 -->
      <div class="flex space-x-3">
        <button
          type="button"
          @click="closeModal"
          class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          :disabled="loading"
        >
          {{ $t('common.cancel') }}
        </button>
        <button
          type="button"
          @click="handleCancel"
          class="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="loading || !cancelReason"
        >
          <span v-if="loading">{{ $t('orders.cancel.processing') }}</span>
          <span v-else>{{ $t('orders.cancel.confirm') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import { useI18n } from 'vue-i18n'

export interface Order {
  id: string
  order_number: string
  status: string
  total_amount?: number
}

interface Props {
  isOpen: boolean
  order: Order | null
}

interface Emits {
  (e: 'close'): void
  (e: 'confirm', data: { reason: string; details?: string }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { t } = useI18n()

const loading = ref(false)
const cancelReason = ref('')
const cancelDetails = ref('')

const errors = reactive({
  reason: '',
  details: ''
})

// 상태별 색상
const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    pending: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-blue-100 text-blue-800',
    shipped: 'bg-green-100 text-green-800',
    delivered: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800'
  }
  return colors[status] || 'bg-gray-100 text-gray-800'
}

// 통화 포맷
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

// 모달 닫기
const closeModal = () => {
  if (!loading.value) {
    resetForm()
    emit('close')
  }
}

// 폼 초기화
const resetForm = () => {
  cancelReason.value = ''
  cancelDetails.value = ''
  errors.reason = ''
  errors.details = ''
  loading.value = false
}

// 유효성 검사
const validateForm = () => {
  errors.reason = ''
  errors.details = ''
  
  if (!cancelReason.value) {
    errors.reason = t('orders.cancel.errors.reason_required')
    return false
  }
  
  if (cancelReason.value === 'other' && !cancelDetails.value?.trim()) {
    errors.details = t('orders.cancel.errors.details_required')
    return false
  }
  
  return true
}

// 취소 처리
const handleCancel = async () => {
  if (!validateForm()) return
  
  loading.value = true
  
  try {
    const cancelData = {
      reason: cancelReason.value,
      details: cancelReason.value === 'other' ? cancelDetails.value : undefined
    }
    
    emit('confirm', cancelData)
  } catch (error) {
    console.error('주문 취소 처리 중 오류:', error)
  } finally {
    loading.value = false
  }
}

// 모달이 열릴 때마다 폼 초기화
watch(() => props.isOpen, (newValue) => {
  if (newValue) {
    resetForm()
  }
})
</script>