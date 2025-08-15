<template>
  <div>
    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <span class="ml-2 text-gray-600">결제 정보를 불러오는 중...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="rounded-md bg-red-50 p-4">
      <div class="flex">
        <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
        <div class="ml-3">
          <h3 class="text-sm font-medium text-red-800">오류 발생</h3>
          <p class="mt-1 text-sm text-red-700">{{ error }}</p>
          <button
            @click="loadPayment"
            class="mt-2 text-sm font-medium text-red-600 hover:text-red-500"
          >
            다시 시도
          </button>
        </div>
      </div>
    </div>

    <!-- Payment Details -->
    <div v-else-if="payment" class="space-y-6">
      <!-- Header -->
      <div class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="sm:flex sm:items-center sm:justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900">
                결제 상세 정보
              </h3>
              <p class="mt-1 max-w-2xl text-sm text-gray-500">
                결제번호: {{ payment.paymentNumber }}
              </p>
            </div>
            <div class="mt-5 sm:mt-0 sm:ml-6 sm:flex-shrink-0 sm:flex sm:items-center space-x-3">
              <span
                :class="getStatusClass(payment.status)"
                class="inline-flex items-center px-3 py-0.5 rounded-full text-sm font-medium"
              >
                <component :is="getStatusIcon(payment.status)" class="h-4 w-4 mr-1.5" />
                {{ getStatusText(payment.status) }}
              </span>
              
              <div class="flex space-x-2">
                <button
                  v-if="canRefund(payment)"
                  @click="initiateRefund"
                  :disabled="processing"
                  class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50"
                >
                  <ArrowUturnLeftIcon class="h-4 w-4 mr-2" />
                  환불
                </button>
                
                <button
                  v-if="canRetry(payment)"
                  @click="retryPayment"
                  :disabled="processing"
                  class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50"
                >
                  <ArrowPathIcon class="h-4 w-4 mr-2" />
                  재시도
                </button>
                
                <button
                  @click="downloadReceipt"
                  class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <DocumentArrowDownIcon class="h-4 w-4 mr-2" />
                  영수증 다운로드
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Information Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Basic Information -->
        <div class="bg-white shadow sm:rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">기본 정보</h3>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-6 sm:grid-cols-2">
              <div>
                <dt class="text-sm font-medium text-gray-500">결제 금액</dt>
                <dd class="mt-1 text-lg font-semibold text-gray-900">
                  {{ formatCurrency(payment.amount, payment.currency) }}
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-gray-500">결제 수단</dt>
                <dd class="mt-1 flex items-center text-sm text-gray-900">
                  <component :is="getMethodIcon(payment.method)" class="h-4 w-4 mr-2" />
                  {{ getMethodText(payment.method) }}
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-gray-500">주문 번호</dt>
                <dd class="mt-1 text-sm text-gray-900">
                  <router-link
                    :to="`/orders/${payment.orderNumber}`"
                    class="text-blue-600 hover:text-blue-500"
                  >
                    {{ payment.orderNumber }}
                  </router-link>
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-gray-500">회원명</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ payment.memberName }}</dd>
              </div>
              <div v-if="payment.installments > 1">
                <dt class="text-sm font-medium text-gray-500">할부</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ payment.installments }}개월</dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-gray-500">결제 일시</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ formatDate(payment.createdAt) }}</dd>
              </div>
            </dl>
          </div>
        </div>

        <!-- Payment Details -->
        <div class="bg-white shadow sm:rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">결제 상세</h3>
            <dl class="grid grid-cols-1 gap-x-4 gap-y-6">
              <div>
                <dt class="text-sm font-medium text-gray-500">결제 설명</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ payment.description }}</dd>
              </div>
              <div v-if="payment.gatewayTransactionId">
                <dt class="text-sm font-medium text-gray-500">게이트웨이 거래번호</dt>
                <dd class="mt-1 text-sm text-gray-900 font-mono">{{ payment.gatewayTransactionId }}</dd>
              </div>
              <div v-if="payment.gatewayResponse">
                <dt class="text-sm font-medium text-gray-500">게이트웨이 응답</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ payment.gatewayResponse }}</dd>
              </div>
              <div v-if="payment.failureReason">
                <dt class="text-sm font-medium text-gray-500">실패 사유</dt>
                <dd class="mt-1 text-sm text-red-600">{{ payment.failureReason }}</dd>
              </div>
              <div v-if="payment.refundReason">
                <dt class="text-sm font-medium text-gray-500">환불 사유</dt>
                <dd class="mt-1 text-sm text-gray-900">{{ payment.refundReason }}</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>

      <!-- Security Information -->
      <div v-if="payment.securityInfo" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">보안 정보</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="flex items-center">
              <ShieldCheckIcon class="h-5 w-5 text-green-500 mr-2" />
              <span class="text-sm text-gray-900">SSL 암호화</span>
            </div>
            <div class="flex items-center">
              <LockClosedIcon class="h-5 w-5 text-green-500 mr-2" />
              <span class="text-sm text-gray-900">PCI DSS 준수</span>
            </div>
            <div class="flex items-center">
              <CheckCircleIcon class="h-5 w-5 text-green-500 mr-2" />
              <span class="text-sm text-gray-900">3D Secure 인증</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Transaction History -->
      <div class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">거래 이력</h3>
          
          <div class="flow-root">
            <ul role="list" class="-mb-8">
              <li v-for="(transaction, index) in transactionHistory" :key="transaction.id">
                <div class="relative pb-8">
                  <span
                    v-if="index !== transactionHistory.length - 1"
                    class="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200"
                    aria-hidden="true"
                  ></span>
                  <div class="relative flex space-x-3">
                    <div>
                      <span
                        :class="getTransactionIconClass(transaction.type)"
                        class="h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white"
                      >
                        <component :is="getTransactionIcon(transaction.type)" class="h-5 w-5 text-white" />
                      </span>
                    </div>
                    <div class="min-w-0 flex-1 pt-1.5 flex justify-between space-x-4">
                      <div>
                        <p class="text-sm text-gray-500">
                          {{ transaction.description }}
                          <span v-if="transaction.amount" class="font-medium text-gray-900">
                            {{ formatCurrency(transaction.amount, payment.currency) }}
                          </span>
                        </p>
                        <p v-if="transaction.details" class="mt-1 text-xs text-gray-400">
                          {{ transaction.details }}
                        </p>
                      </div>
                      <div class="text-right text-sm whitespace-nowrap text-gray-500">
                        <time :datetime="transaction.createdAt">
                          {{ formatDate(transaction.createdAt) }}
                        </time>
                      </div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Receipt Preview -->
      <div v-if="showReceiptPreview" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg leading-6 font-medium text-gray-900">영수증 미리보기</h3>
            <button
              @click="showReceiptPreview = false"
              class="text-gray-400 hover:text-gray-500"
            >
              <XMarkIcon class="h-6 w-6" />
            </button>
          </div>
          
          <div class="border rounded-lg p-6 bg-gray-50">
            <!-- Receipt Content -->
            <div class="text-center mb-6">
              <h2 class="text-xl font-bold text-gray-900">YCS 물류관리 시스템</h2>
              <p class="text-sm text-gray-600">결제 영수증</p>
            </div>
            
            <div class="border-t border-b border-gray-200 py-4 mb-4">
              <dl class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <dt class="font-medium text-gray-900">결제번호:</dt>
                  <dd class="text-gray-600">{{ payment.paymentNumber }}</dd>
                </div>
                <div>
                  <dt class="font-medium text-gray-900">결제일시:</dt>
                  <dd class="text-gray-600">{{ formatDate(payment.createdAt) }}</dd>
                </div>
                <div>
                  <dt class="font-medium text-gray-900">결제수단:</dt>
                  <dd class="text-gray-600">{{ getMethodText(payment.method) }}</dd>
                </div>
                <div>
                  <dt class="font-medium text-gray-900">결제금액:</dt>
                  <dd class="font-bold text-gray-900">{{ formatCurrency(payment.amount, payment.currency) }}</dd>
                </div>
              </dl>
            </div>
            
            <div class="text-center text-xs text-gray-500">
              <p>본 영수증은 {{ formatDate(new Date().toISOString()) }}에 발급되었습니다.</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Refund Confirmation Modal -->
    <div
      v-if="showRefundModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
      @click="closeRefundModal"
    >
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
            <ExclamationTriangleIcon class="h-6 w-6 text-red-600" />
          </div>
          <div class="mt-3 text-center sm:mt-5">
            <h3 class="text-lg leading-6 font-medium text-gray-900">환불 확인</h3>
            <div class="mt-2">
              <p class="text-sm text-gray-500">
                {{ formatCurrency(payment?.amount || 0, payment?.currency || 'KRW') }}를 환불하시겠습니까?
              </p>
            </div>
            <div class="mt-4">
              <label for="refund-reason" class="block text-sm font-medium text-gray-700 text-left">
                환불 사유
              </label>
              <textarea
                id="refund-reason"
                v-model="refundReason"
                rows="3"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 text-sm"
                placeholder="환불 사유를 입력해주세요"
              ></textarea>
            </div>
          </div>
        </div>
        <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
          <button
            @click="confirmRefund"
            :disabled="processing || !refundReason.trim()"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:col-start-2 sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="processing" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></span>
            환불 실행
          </button>
          <button
            @click="closeRefundModal"
            :disabled="processing"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:col-start-1 sm:text-sm disabled:opacity-50"
          >
            취소
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SpringBootPaymentService, { type PaymentResponse, type PaymentTransaction, type RefundRequest } from '@/services/paymentApiService'
import {
  ExclamationTriangleIcon,
  ArrowUturnLeftIcon,
  ArrowPathIcon,
  DocumentArrowDownIcon,
  CheckCircleIcon,
  ClockIcon,
  XCircleIcon,
  XMarkIcon,
  CreditCardIcon,
  BanknotesIcon,
  DevicePhoneMobileIcon,
  CurrencyDollarIcon,
  ShieldCheckIcon,
  LockClosedIcon,
  PlayIcon,
  PauseIcon,
  StopIcon
} from '@heroicons/vue/24/outline'

// Use interfaces from the API service
type Payment = PaymentResponse
type Transaction = PaymentTransaction

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const processing = ref(false)
const error = ref<string | null>(null)
const payment = ref<Payment | null>(null)
const transactionHistory = ref<Transaction[]>([])
const showRefundModal = ref(false)
const showReceiptPreview = ref(false)
const refundReason = ref('')

const paymentId = computed(() => parseInt(route.params.id as string))

// Helper functions
const formatCurrency = (amount: number, currency: string): string => {
  const currencySymbols = {
    KRW: '₩',
    THB: '฿',
    USD: '$',
    BTC: '₿'
  }
  
  return `${currencySymbols[currency as keyof typeof currencySymbols] || currency} ${new Intl.NumberFormat('ko-KR').format(amount)}`
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getStatusClass = (status: string): string => {
  const classes = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    PROCESSING: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800',
    REFUNDED: 'bg-gray-100 text-gray-800',
    CANCELLED: 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string): string => {
  const texts = {
    PENDING: '결제 대기',
    PROCESSING: '결제 처리중',
    COMPLETED: '결제 완료',
    FAILED: '결제 실패',
    REFUNDED: '환불 완료',
    CANCELLED: '결제 취소'
  }
  return texts[status as keyof typeof texts] || status
}

const getStatusIcon = (status: string) => {
  const icons = {
    PENDING: ClockIcon,
    PROCESSING: ArrowPathIcon,
    COMPLETED: CheckCircleIcon,
    FAILED: XCircleIcon,
    REFUNDED: ArrowUturnLeftIcon,
    CANCELLED: XCircleIcon
  }
  return icons[status as keyof typeof icons] || CheckCircleIcon
}

const getMethodIcon = (method: string) => {
  const icons = {
    BANK_TRANSFER: BanknotesIcon,
    CREDIT_CARD: CreditCardIcon,
    MOBILE_PAYMENT: DevicePhoneMobileIcon,
    CRYPTO: CurrencyDollarIcon
  }
  return icons[method as keyof typeof icons] || CreditCardIcon
}

const getMethodText = (method: string): string => {
  const texts = {
    BANK_TRANSFER: '계좌이체',
    CREDIT_CARD: '신용카드',
    MOBILE_PAYMENT: '모바일결제',
    CRYPTO: '암호화폐'
  }
  return texts[method as keyof typeof texts] || method
}

const getTransactionIcon = (type: string) => {
  const icons = {
    CREATED: PlayIcon,
    PROCESSING: ArrowPathIcon,
    COMPLETED: CheckCircleIcon,
    FAILED: XCircleIcon,
    REFUNDED: ArrowUturnLeftIcon,
    CANCELLED: XCircleIcon
  }
  return icons[type as keyof typeof icons] || CheckCircleIcon
}

const getTransactionIconClass = (type: string): string => {
  const classes = {
    CREATED: 'bg-blue-500',
    PROCESSING: 'bg-yellow-500',
    COMPLETED: 'bg-green-500',
    FAILED: 'bg-red-500',
    REFUNDED: 'bg-gray-500',
    CANCELLED: 'bg-red-500'
  }
  return classes[type as keyof typeof classes] || 'bg-gray-500'
}

const canRefund = (payment: Payment): boolean => {
  return payment.status === 'COMPLETED' && payment.refundable
}

const canRetry = (payment: Payment): boolean => {
  return payment.status === 'FAILED'
}

// Actions
const loadPayment = async () => {
  loading.value = true
  error.value = null
  
  try {
    // Load payment details
    const paymentResult = await SpringBootPaymentService.getPayment(paymentId.value)
    if (paymentResult.success && paymentResult.data) {
      payment.value = paymentResult.data
    } else {
      error.value = paymentResult.error || '결제 정보를 불러올 수 없습니다.'
      return
    }

    // Load transaction history
    const transactionResult = await SpringBootPaymentService.getPaymentTransactions(paymentId.value)
    if (transactionResult.success && transactionResult.data) {
      transactionHistory.value = transactionResult.data
    } else {
      console.warn('Transaction history loading failed:', transactionResult.error)
      transactionHistory.value = []
    }
  } catch (err) {
    console.error('Payment loading error:', err)
    error.value = '결제 정보를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

const initiateRefund = () => {
  refundReason.value = ''
  showRefundModal.value = true
}

const closeRefundModal = () => {
  if (!processing.value) {
    showRefundModal.value = false
    refundReason.value = ''
  }
}

const confirmRefund = async () => {
  if (!payment.value || !refundReason.value.trim()) return
  
  processing.value = true
  
  try {
    const refundRequest: RefundRequest = {
      paymentId: payment.value.id,
      reason: refundReason.value.trim()
    }

    const result = await SpringBootPaymentService.refundPayment(refundRequest)
    
    if (result.success) {
      // Reload payment data to get updated status
      await loadPayment()
      
      // Emit success event
      window.dispatchEvent(new CustomEvent('payment-success', {
        detail: { 
          paymentId: payment.value.id, 
          type: 'refund',
          message: '환불이 성공적으로 처리되었습니다.'
        }
      }))
      
      closeRefundModal()
    } else {
      window.dispatchEvent(new CustomEvent('payment-error', {
        detail: { message: result.error || '환불 처리 중 오류가 발생했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Refund error:', error)
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '환불 처리 중 오류가 발생했습니다.' }
    }))
  } finally {
    processing.value = false
  }
}

const retryPayment = async () => {
  if (!payment.value) return
  
  processing.value = true
  
  try {
    const result = await SpringBootPaymentService.retryPayment(payment.value.id)
    
    if (result.success) {
      // Reload payment data to get updated status
      await loadPayment()
      
      window.dispatchEvent(new CustomEvent('payment-success', {
        detail: { 
          paymentId: payment.value.id, 
          type: 'retry',
          message: '결제 재시도가 시작되었습니다.'
        }
      }))
    } else {
      window.dispatchEvent(new CustomEvent('payment-error', {
        detail: { message: result.error || '결제 재시도 중 오류가 발생했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Payment retry error:', error)
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '결제 재시도 중 오류가 발생했습니다.' }
    }))
  } finally {
    processing.value = false
  }
}

const downloadReceipt = async () => {
  if (!payment.value) return
  
  try {
    const result = await SpringBootPaymentService.downloadReceipt(payment.value.id, 'pdf')
    
    if (result.success && result.data) {
      // Create download link
      const url = window.URL.createObjectURL(result.data)
      const link = document.createElement('a')
      link.href = url
      link.download = `payment-receipt-${payment.value.paymentNumber}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      // Show preview as well
      showReceiptPreview.value = true
      
      window.dispatchEvent(new CustomEvent('payment-success', {
        detail: { 
          paymentId: payment.value.id, 
          type: 'receipt_download',
          message: '영수증 다운로드가 완료되었습니다.'
        }
      }))
    } else {
      window.dispatchEvent(new CustomEvent('payment-error', {
        detail: { message: '영수증 다운로드 중 오류가 발생했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Receipt download error:', error)
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '영수증 다운로드 중 오류가 발생했습니다.' }
    }))
  }
}

onMounted(() => {
  loadPayment()
})
</script>