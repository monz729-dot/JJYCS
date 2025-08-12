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
  CurrencyBitcoinIcon,
  ShieldCheckIcon,
  LockClosedIcon,
  PlayIcon,
  PauseIcon,
  StopIcon
} from '@heroicons/vue/24/outline'

interface Payment {
  id: string
  paymentNumber: string
  orderNumber: string
  memberId: string
  memberName: string
  amount: number
  currency: string
  method: string
  status: string
  installments: number
  description: string
  gatewayTransactionId?: string
  gatewayResponse?: string
  failureReason?: string
  refundReason?: string
  refundable: boolean
  securityInfo?: {
    sslEncrypted: boolean
    pciCompliant: boolean
    threeDSecure: boolean
  }
  createdAt: string
  updatedAt: string
}

interface Transaction {
  id: string
  type: 'created' | 'processed' | 'completed' | 'failed' | 'refunded'
  description: string
  details?: string
  amount?: number
  createdAt: string
}

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

const paymentId = computed(() => route.params.id as string)

// Mock data
const mockPayment: Payment = {
  id: 'pay_001',
  paymentNumber: 'PAY-2024-001',
  orderNumber: 'ORD-2024-001',
  memberId: 'user_001',
  memberName: '김철수',
  amount: 150000,
  currency: 'KRW',
  method: 'credit_card',
  status: 'completed',
  installments: 1,
  description: '태국 배송료',
  gatewayTransactionId: 'TXN-GW-2024-001',
  gatewayResponse: '승인완료',
  refundable: true,
  securityInfo: {
    sslEncrypted: true,
    pciCompliant: true,
    threeDSecure: true
  },
  createdAt: '2024-01-15T10:30:00Z',
  updatedAt: '2024-01-15T10:35:00Z'
}

const mockTransactionHistory: Transaction[] = [
  {
    id: 'txn_001',
    type: 'created',
    description: '결제 요청이 생성되었습니다',
    details: '사용자가 결제를 시작했습니다',
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: 'txn_002',
    type: 'processed',
    description: '결제 처리가 시작되었습니다',
    details: '결제 게이트웨이로 전송됨',
    createdAt: '2024-01-15T10:32:00Z'
  },
  {
    id: 'txn_003',
    type: 'completed',
    description: '결제가 성공적으로 완료되었습니다',
    details: '승인번호: 12345678',
    amount: 150000,
    createdAt: '2024-01-15T10:35:00Z'
  }
]

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
    pending: 'bg-yellow-100 text-yellow-800',
    processing: 'bg-blue-100 text-blue-800',
    completed: 'bg-green-100 text-green-800',
    failed: 'bg-red-100 text-red-800',
    refunded: 'bg-gray-100 text-gray-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string): string => {
  const texts = {
    pending: '결제 대기',
    processing: '결제 처리중',
    completed: '결제 완료',
    failed: '결제 실패',
    refunded: '환불 완료'
  }
  return texts[status as keyof typeof texts] || status
}

const getStatusIcon = (status: string) => {
  const icons = {
    pending: ClockIcon,
    processing: ArrowPathIcon,
    completed: CheckCircleIcon,
    failed: XCircleIcon,
    refunded: ArrowUturnLeftIcon
  }
  return icons[status as keyof typeof icons] || CheckCircleIcon
}

const getMethodIcon = (method: string) => {
  const icons = {
    bank_transfer: BanknotesIcon,
    credit_card: CreditCardIcon,
    mobile_payment: DevicePhoneMobileIcon,
    crypto: CurrencyBitcoinIcon
  }
  return icons[method as keyof typeof icons] || CreditCardIcon
}

const getMethodText = (method: string): string => {
  const texts = {
    bank_transfer: '계좌이체',
    credit_card: '신용카드',
    mobile_payment: '모바일결제',
    crypto: '암호화폐'
  }
  return texts[method as keyof typeof texts] || method
}

const getTransactionIcon = (type: string) => {
  const icons = {
    created: PlayIcon,
    processed: ArrowPathIcon,
    completed: CheckCircleIcon,
    failed: XCircleIcon,
    refunded: ArrowUturnLeftIcon
  }
  return icons[type as keyof typeof icons] || CheckCircleIcon
}

const getTransactionIconClass = (type: string): string => {
  const classes = {
    created: 'bg-blue-500',
    processed: 'bg-yellow-500',
    completed: 'bg-green-500',
    failed: 'bg-red-500',
    refunded: 'bg-gray-500'
  }
  return classes[type as keyof typeof classes] || 'bg-gray-500'
}

const canRefund = (payment: Payment): boolean => {
  return payment.status === 'completed' && payment.refundable
}

const canRetry = (payment: Payment): boolean => {
  return payment.status === 'failed'
}

// Actions
const loadPayment = async () => {
  loading.value = true
  error.value = null
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Load mock data
    payment.value = mockPayment
    transactionHistory.value = mockTransactionHistory
  } catch (err) {
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
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Update payment status
    payment.value.status = 'refunded'
    payment.value.refundable = false
    payment.value.refundReason = refundReason.value
    payment.value.updatedAt = new Date().toISOString()
    
    // Add refund transaction to history
    transactionHistory.value.push({
      id: `txn_refund_${Date.now()}`,
      type: 'refunded',
      description: '환불이 처리되었습니다',
      details: `환불 사유: ${refundReason.value}`,
      amount: payment.value.amount,
      createdAt: new Date().toISOString()
    })
    
    // Emit success event
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { 
        paymentId: payment.value.id, 
        type: 'refund',
        message: '환불이 성공적으로 처리되었습니다.'
      }
    }))
    
    closeRefundModal()
  } catch (error) {
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
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Update payment status
    payment.value.status = 'processing'
    payment.value.updatedAt = new Date().toISOString()
    
    // Add retry transaction to history
    transactionHistory.value.push({
      id: `txn_retry_${Date.now()}`,
      type: 'processed',
      description: '결제 재시도가 시작되었습니다',
      details: '이전 실패한 결제를 다시 처리합니다',
      createdAt: new Date().toISOString()
    })
    
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { 
        paymentId: payment.value.id, 
        type: 'retry',
        message: '결제 재시도가 시작되었습니다.'
      }
    }))
  } catch (error) {
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '결제 재시도 중 오류가 발생했습니다.' }
    }))
  } finally {
    processing.value = false
  }
}

const downloadReceipt = () => {
  showReceiptPreview.value = true
  
  // Simulate receipt download
  setTimeout(() => {
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { 
        paymentId: payment.value?.id, 
        type: 'receipt_download',
        message: '영수증 다운로드가 완료되었습니다.'
      }
    }))
  }, 1000)
}

onMounted(() => {
  loadPayment()
})
</script>