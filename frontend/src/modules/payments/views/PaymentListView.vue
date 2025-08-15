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
                <option value="PENDING">대기중</option>
                <option value="PROCESSING">처리중</option>
                <option value="COMPLETED">완료</option>
                <option value="FAILED">실패</option>
                <option value="REFUNDED">환불</option>
              </select>
              
              <select
                v-model="selectedMethod"
                class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 text-sm rounded-md"
              >
                <option value="">모든 결제수단</option>
                <option value="BANK_TRANSFER">계좌이체</option>
                <option value="CREDIT_CARD">신용카드</option>
                <option value="MOBILE_PAYMENT">모바일결제</option>
                <option value="CRYPTO">암호화폐</option>
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
import SpringBootPaymentService, { type PaymentResponse, type RefundRequest } from '@/services/paymentApiService'
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
  CurrencyDollarIcon
} from '@heroicons/vue/24/outline'

// Use PaymentResponse from API service
type Payment = PaymentResponse

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
const refundPaymentId = ref<number | null>(null)

// Data
const payments = ref<Payment[]>([])
const totalElements = ref(0)
const totalPages = ref(0)

// Computed properties - now use server-side pagination
const filteredPayments = computed(() => {
  return payments.value
})

const paginatedPayments = computed(() => {
  return payments.value
})

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, totalElements.value)
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

// Statistics - use actual API data
const totalAmount = computed(() => {
  return payments.value
    .filter(p => p.status === 'COMPLETED')
    .reduce((sum, payment) => {
      // Convert to KRW for total (simplified conversion)
      const rate = payment.currency === 'THB' ? 40 : payment.currency === 'USD' ? 1350 : 1
      return sum + (payment.amount * rate)
    }, 0)
})

const completedPayments = computed(() => {
  return payments.value.filter(p => p.status === 'COMPLETED').length
})

const pendingPayments = computed(() => {
  return payments.value.filter(p => p.status === 'PENDING' || p.status === 'PROCESSING').length
})

const failedPayments = computed(() => {
  return payments.value.filter(p => p.status === 'FAILED').length
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
    PENDING: '대기중',
    PROCESSING: '처리중',
    COMPLETED: '완료',
    FAILED: '실패',
    REFUNDED: '환불',
    CANCELLED: '취소'
  }
  return texts[status as keyof typeof texts] || status
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

const canRefund = (payment: Payment): boolean => {
  return payment.status === 'COMPLETED' && payment.refundable
}

const canRetry = (payment: Payment): boolean => {
  return payment.status === 'FAILED'
}

// Load payments from API
const loadPayments = async () => {
  try {
    loading.value = true
    
    const result = await SpringBootPaymentService.getPayments({
      page: currentPage.value - 1, // Spring Boot uses 0-based pages
      size: itemsPerPage.value,
      status: selectedStatus.value || undefined,
      method: selectedMethod.value || undefined
    })
    
    if (result.success && result.data) {
      payments.value = result.data.content
      totalElements.value = result.data.totalElements
      totalPages.value = result.data.totalPages
    } else {
      console.error('Failed to load payments:', result.error)
      window.dispatchEvent(new CustomEvent('payment-error', {
        detail: { message: result.error || '결제 목록을 불러오는데 실패했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Load payments error:', error)
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '결제 목록을 불러오는데 실패했습니다.' }
    }))
  } finally {
    loading.value = false
  }
}

// Actions
const viewPayment = (paymentId: number) => {
  router.push(`/app/payments/${paymentId}`)
}

const initiateRefund = (paymentId: number) => {
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
    const refundRequest: RefundRequest = {
      paymentId: refundPaymentId.value,
      reason: '사용자 요청에 의한 환불'
    }

    const result = await SpringBootPaymentService.refundPayment(refundRequest)
    
    if (result.success) {
      // Reload payments to get updated data
      await loadPayments()
      
      window.dispatchEvent(new CustomEvent('payment-success', {
        detail: { 
          paymentId: refundPaymentId.value, 
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

const retryPayment = async (paymentId: number) => {
  processing.value = true
  
  try {
    const result = await SpringBootPaymentService.retryPayment(paymentId)
    
    if (result.success) {
      // Reload payments to get updated data
      await loadPayments()
      
      window.dispatchEvent(new CustomEvent('payment-success', {
        detail: { 
          paymentId, 
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
  
  
  
  window.dispatchEvent(new CustomEvent('payment-success', {
    detail: { count: data.length, type: 'export' }
  }))
}

// Pagination functions
const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadPayments()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadPayments()
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
  loadPayments()
}

// Watch for filter changes to reset pagination and reload
watch([searchQuery, selectedStatus, selectedMethod, selectedCurrency], () => {
  currentPage.value = 1
  loadPayments()
})

onMounted(() => {
  loadPayments()
})
</script>