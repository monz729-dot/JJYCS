<template>
  <div>
    <!-- Filters and Search -->
    <div class="bg-white shadow rounded-lg mb-6">
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between">
          <div class="flex-1 min-w-0">
            <!-- Search -->
            <div class="max-w-lg w-full lg:max-w-xs">
              <label for="search" class="sr-only">검색</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
                </div>
                <input
                  id="search"
                  v-model="searchQuery"
                  type="search"
                  placeholder="결제 번호, 주문 번호, 회원명으로 검색..."
                  class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
                >
              </div>
            </div>
          </div>
          <div class="mt-4 flex-shrink-0 flex lg:mt-0 lg:ml-4">
            <!-- Filters -->
            <div class="flex space-x-3">
              <select
                v-model="selectedStatus"
                class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-sm rounded-md"
              >
                <option value="">모든 상태</option>
                <option value="pending">대기중</option>
                <option value="processing">처리중</option>
                <option value="completed">완료</option>
                <option value="failed">실패</option>
                <option value="refunded">환불</option>
              </select>
              
              <select
                v-model="selectedMethod"
                class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-sm rounded-md"
              >
                <option value="">모든 결제수단</option>
                <option value="bank_transfer">계좌이체</option>
                <option value="credit_card">신용카드</option>
                <option value="mobile_payment">모바일결제</option>
                <option value="crypto">암호화폐</option>
              </select>

              <select
                v-model="selectedCurrency"
                class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-sm rounded-md"
              >
                <option value="">모든 통화</option>
                <option value="THB">THB</option>
                <option value="KRW">KRW</option>
                <option value="USD">USD</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Bulk Actions -->
      <div v-if="selectedPayments.length > 0" class="px-6 py-3 bg-blue-50 border-b border-blue-200">
        <div class="flex items-center justify-between">
          <span class="text-sm font-medium text-blue-800">
            {{ selectedPayments.length }}개 항목 선택됨
          </span>
          <div class="flex space-x-2">
            <button
              @click="bulkProcessPayments"
              :disabled="bulkProcessing"
              class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <span v-if="bulkProcessing" class="animate-spin rounded-full h-3 w-3 border-b-2 border-blue-600 mr-2"></span>
              일괄 처리
            </button>
            <button
              @click="exportSelected"
              class="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <DocumentArrowDownIcon class="h-3 w-3 mr-1" />
              내보내기
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment Stats -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <CreditCardIcon class="h-6 w-6 text-gray-400" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">총 결제액</dt>
                <dd class="text-lg font-medium text-gray-900">₩{{ formatNumber(totalAmount) }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <CheckCircleIcon class="h-6 w-6 text-green-400" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">완료된 결제</dt>
                <dd class="text-lg font-medium text-gray-900">{{ completedPayments }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <ClockIcon class="h-6 w-6 text-yellow-400" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">처리 대기</dt>
                <dd class="text-lg font-medium text-gray-900">{{ pendingPayments }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <XCircleIcon class="h-6 w-6 text-red-400" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">실패한 결제</dt>
                <dd class="text-lg font-medium text-gray-900">{{ failedPayments }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment List -->
    <div class="bg-white shadow overflow-hidden sm:rounded-md">
      <!-- Loading State -->
      <div v-if="loading" class="px-6 py-12 text-center">
        <div class="inline-flex items-center">
          <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span class="ml-2 text-gray-600">결제 내역을 불러오는 중...</span>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredPayments.length === 0" class="px-6 py-12 text-center">
        <CreditCardIcon class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-sm font-medium text-gray-900">결제 내역이 없습니다</h3>
        <p class="mt-1 text-sm text-gray-500">
          아직 결제 내역이 없거나 검색 조건과 일치하는 결제가 없습니다.
        </p>
      </div>

      <!-- Payment List -->
      <ul v-else class="divide-y divide-gray-200">
        <li v-for="payment in paginatedPayments" :key="payment.id">
          <div class="px-6 py-4 hover:bg-gray-50">
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <input
                  :id="`payment-${payment.id}`"
                  v-model="selectedPayments"
                  :value="payment.id"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                >
                <label :for="`payment-${payment.id}`" class="sr-only">결제 {{ payment.id }} 선택</label>
                
                <div class="ml-4 flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-sm font-medium text-gray-900">
                        결제번호: {{ payment.paymentNumber }}
                      </p>
                      <p class="text-sm text-gray-500">
                        주문번호: {{ payment.orderNumber }} | 회원: {{ payment.memberName }}
                      </p>
                    </div>
                    <div class="text-right">
                      <p class="text-sm font-medium text-gray-900">
                        {{ formatCurrency(payment.amount, payment.currency) }}
                      </p>
                      <p class="text-sm text-gray-500">
                        {{ formatDate(payment.createdAt) }}
                      </p>
                    </div>
                  </div>
                  
                  <div class="mt-2 flex items-center justify-between">
                    <div class="flex items-center space-x-4">
                      <span
                        :class="getStatusClass(payment.status)"
                        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      >
                        {{ getStatusText(payment.status) }}
                      </span>
                      
                      <span class="inline-flex items-center text-xs text-gray-500">
                        <component :is="getMethodIcon(payment.method)" class="h-4 w-4 mr-1" />
                        {{ getMethodText(payment.method) }}
                      </span>
                      
                      <span v-if="payment.installments > 1" class="text-xs text-gray-500">
                        {{ payment.installments }}개월 할부
                      </span>
                    </div>
                    
                    <div class="flex items-center space-x-2">
                      <button
                        @click="viewPayment(payment.id)"
                        class="text-blue-600 hover:text-blue-500 text-sm font-medium"
                      >
                        상세보기
                      </button>
                      
                      <button
                        v-if="canRefund(payment)"
                        @click="initiateRefund(payment.id)"
                        class="text-red-600 hover:text-red-500 text-sm font-medium"
                      >
                        환불
                      </button>
                      
                      <button
                        v-if="canRetry(payment)"
                        @click="retryPayment(payment.id)"
                        class="text-green-600 hover:text-green-500 text-sm font-medium"
                      >
                        재시도
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </li>
      </ul>

      <!-- Pagination -->
      <div v-if="filteredPayments.length > 0" class="bg-white px-4 py-3 border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            @click="previousPage"
            :disabled="currentPage === 1"
            class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            이전
          </button>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            다음
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              총 <span class="font-medium">{{ filteredPayments.length }}</span>개 중
              <span class="font-medium">{{ startItem }}</span>-<span class="font-medium">{{ endItem }}</span>개 표시
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
              <button
                @click="previousPage"
                :disabled="currentPage === 1"
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronLeftIcon class="h-5 w-5" />
              </button>
              
              <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="page === currentPage 
                  ? 'z-10 bg-blue-50 border-blue-500 text-blue-600' 
                  : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'"
                class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
              >
                {{ page }}
              </button>
              
              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <ChevronRightIcon class="h-5 w-5" />
              </button>
            </nav>
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
        <div class="mt-3 text-center">
          <ExclamationTriangleIcon class="mx-auto h-12 w-12 text-red-500" />
          <h3 class="text-lg font-medium text-gray-900 mt-2">환불 확인</h3>
          <div class="mt-2 px-7 py-3">
            <p class="text-sm text-gray-500">
              정말로 이 결제를 환불하시겠습니까?<br>
              이 작업은 되돌릴 수 없습니다.
            </p>
          </div>
          <div class="items-center px-4 py-3">
            <button
              @click="confirmRefund"
              :disabled="processing"
              class="px-4 py-2 bg-red-500 text-white text-base font-medium rounded-md w-24 mr-2 hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300 disabled:opacity-50"
            >
              <span v-if="processing" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mx-auto"></span>
              <span v-else>환불</span>
            </button>
            <button
              @click="closeRefundModal"
              :disabled="processing"
              class="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md w-24 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-300 disabled:opacity-50"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  MagnifyingGlassIcon,
  CreditCardIcon,
  CheckCircleIcon,
  ClockIcon,
  XCircleIcon,
  ExclamationTriangleIcon,
  DocumentArrowDownIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  BanknotesIcon,
  DevicePhoneMobileIcon,
  CurrencyBitcoinIcon
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
  createdAt: string
  updatedAt: string
  refundable: boolean
}

const router = useRouter()
const loading = ref(false)
const processing = ref(false)
const bulkProcessing = ref(false)

// Search and filters
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedMethod = ref('')
const selectedCurrency = ref('')
const selectedPayments = ref<string[]>([])

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(10)

// Modal states
const showRefundModal = ref(false)
const refundPaymentId = ref<string | null>(null)

// Mock data
const payments = ref<Payment[]>([
  {
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
    createdAt: '2024-01-15T10:30:00Z',
    updatedAt: '2024-01-15T10:35:00Z',
    refundable: true
  },
  {
    id: 'pay_002',
    paymentNumber: 'PAY-2024-002',
    orderNumber: 'ORD-2024-002',
    memberId: 'user_002',
    memberName: '박영희',
    amount: 2500,
    currency: 'THB',
    method: 'bank_transfer',
    status: 'pending',
    installments: 1,
    description: '현지 배송비',
    createdAt: '2024-01-14T14:20:00Z',
    updatedAt: '2024-01-14T14:20:00Z',
    refundable: false
  },
  {
    id: 'pay_003',
    paymentNumber: 'PAY-2024-003',
    orderNumber: 'ORD-2024-003',
    memberId: 'user_003',
    memberName: '이민수',
    amount: 85.50,
    currency: 'USD',
    method: 'mobile_payment',
    status: 'processing',
    installments: 3,
    description: '항공운임',
    createdAt: '2024-01-13T09:15:00Z',
    updatedAt: '2024-01-13T09:45:00Z',
    refundable: true
  },
  {
    id: 'pay_004',
    paymentNumber: 'PAY-2024-004',
    orderNumber: 'ORD-2024-004',
    memberId: 'user_004',
    memberName: '최지영',
    amount: 0.025,
    currency: 'BTC',
    method: 'crypto',
    status: 'failed',
    installments: 1,
    description: '보관료',
    createdAt: '2024-01-12T16:40:00Z',
    updatedAt: '2024-01-12T16:42:00Z',
    refundable: false
  },
  {
    id: 'pay_005',
    paymentNumber: 'PAY-2024-005',
    orderNumber: 'ORD-2024-005',
    memberId: 'user_005',
    memberName: '홍길동',
    amount: 320000,
    currency: 'KRW',
    method: 'credit_card',
    status: 'refunded',
    installments: 6,
    description: '대량 배송료',
    createdAt: '2024-01-11T11:25:00Z',
    updatedAt: '2024-01-11T15:10:00Z',
    refundable: false
  }
])

// Computed properties
const filteredPayments = computed(() => {
  let result = payments.value

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(payment =>
      payment.paymentNumber.toLowerCase().includes(query) ||
      payment.orderNumber.toLowerCase().includes(query) ||
      payment.memberName.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (selectedStatus.value) {
    result = result.filter(payment => payment.status === selectedStatus.value)
  }

  // Method filter
  if (selectedMethod.value) {
    result = result.filter(payment => payment.method === selectedMethod.value)
  }

  // Currency filter
  if (selectedCurrency.value) {
    result = result.filter(payment => payment.currency === selectedCurrency.value)
  }

  return result
})

const paginatedPayments = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredPayments.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredPayments.value.length / itemsPerPage.value)
})

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, filteredPayments.value.length)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// Statistics
const totalAmount = computed(() => {
  return payments.value
    .filter(p => p.status === 'completed')
    .reduce((sum, payment) => {
      // Convert to KRW for total (simplified conversion)
      const rate = payment.currency === 'THB' ? 40 : payment.currency === 'USD' ? 1350 : 1
      return sum + (payment.amount * rate)
    }, 0)
})

const completedPayments = computed(() => {
  return payments.value.filter(p => p.status === 'completed').length
})

const pendingPayments = computed(() => {
  return payments.value.filter(p => p.status === 'pending' || p.status === 'processing').length
})

const failedPayments = computed(() => {
  return payments.value.filter(p => p.status === 'failed').length
})

// Helper functions
const formatNumber = (num: number): string => {
  return new Intl.NumberFormat('ko-KR').format(num)
}

const formatCurrency = (amount: number, currency: string): string => {
  const currencySymbols = {
    KRW: '₩',
    THB: '฿',
    USD: '$',
    BTC: '₿'
  }
  
  return `${currencySymbols[currency as keyof typeof currencySymbols] || currency} ${formatNumber(amount)}`
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
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
    pending: '대기중',
    processing: '처리중',
    completed: '완료',
    failed: '실패',
    refunded: '환불'
  }
  return texts[status as keyof typeof texts] || status
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

const canRefund = (payment: Payment): boolean => {
  return payment.status === 'completed' && payment.refundable
}

const canRetry = (payment: Payment): boolean => {
  return payment.status === 'failed'
}

// Actions
const viewPayment = (paymentId: string) => {
  router.push(`/payments/${paymentId}`)
}

const initiateRefund = (paymentId: string) => {
  refundPaymentId.value = paymentId
  showRefundModal.value = true
}

const closeRefundModal = () => {
  if (!processing.value) {
    showRefundModal.value = false
    refundPaymentId.value = null
  }
}

const confirmRefund = async () => {
  if (!refundPaymentId.value) return
  
  processing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Update payment status
    const payment = payments.value.find(p => p.id === refundPaymentId.value)
    if (payment) {
      payment.status = 'refunded'
      payment.refundable = false
      payment.updatedAt = new Date().toISOString()
    }
    
    // Emit success event
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { paymentId: refundPaymentId.value, type: 'refund' }
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

const retryPayment = async (paymentId: string) => {
  processing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Update payment status
    const payment = payments.value.find(p => p.id === paymentId)
    if (payment) {
      payment.status = 'processing'
      payment.updatedAt = new Date().toISOString()
    }
    
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { paymentId, type: 'retry' }
    }))
  } catch (error) {
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '결제 재시도 중 오류가 발생했습니다.' }
    }))
  } finally {
    processing.value = false
  }
}

const bulkProcessPayments = async () => {
  bulkProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Update selected payments
    selectedPayments.value.forEach(paymentId => {
      const payment = payments.value.find(p => p.id === paymentId)
      if (payment && payment.status === 'pending') {
        payment.status = 'processing'
        payment.updatedAt = new Date().toISOString()
      }
    })
    
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { count: selectedPayments.value.length, type: 'bulk_process' }
    }))
    
    selectedPayments.value = []
  } catch (error) {
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '일괄 처리 중 오류가 발생했습니다.' }
    }))
  } finally {
    bulkProcessing.value = false
  }
}

const exportSelected = () => {
  // Simulate export functionality
  const data = selectedPayments.value.map(id => 
    payments.value.find(p => p.id === id)
  ).filter(Boolean)
  
  console.log('Exporting payments:', data)
  
  window.dispatchEvent(new CustomEvent('payment-success', {
    detail: { count: data.length, type: 'export' }
  }))
}

// Pagination functions
const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
}

// Watch for filter changes to reset pagination
watch([searchQuery, selectedStatus, selectedMethod, selectedCurrency], () => {
  currentPage.value = 1
})

onMounted(() => {
  // Load initial data
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
})
</script>