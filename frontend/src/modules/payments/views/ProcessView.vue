<template>
  <div class="max-w-4xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-gray-900">결제 처리</h2>
      <p class="mt-1 text-sm text-gray-600">견적에 대한 결제를 처리하고 관리하세요</p>
    </div>

    <!-- Step Indicator -->
    <div class="mb-8">
      <nav aria-label="Progress">
        <ol role="list" class="flex items-center">
          <li v-for="(step, stepIndex) in steps" :key="step.id" :class="stepIndex !== steps.length - 1 ? 'pr-8 sm:pr-20' : ''" class="relative">
            <div v-if="stepIndex !== steps.length - 1" class="absolute inset-0 flex items-center" aria-hidden="true">
              <div class="h-0.5 w-full bg-gray-200"></div>
            </div>
            <div class="relative flex h-8 w-8 items-center justify-center rounded-full"
                 :class="currentStep >= stepIndex + 1 
                   ? 'bg-blue-600' 
                   : currentStep === stepIndex + 1 
                     ? 'border-2 border-blue-600 bg-white' 
                     : 'border-2 border-gray-300 bg-white'">
              <CheckIcon v-if="currentStep > stepIndex + 1" class="h-5 w-5 text-white" />
              <span v-else :class="currentStep >= stepIndex + 1 ? 'text-blue-600' : 'text-gray-500'" class="h-5 w-5 text-sm font-medium">
                {{ stepIndex + 1 }}
              </span>
            </div>
            <span class="absolute top-10 left-1/2 transform -translate-x-1/2 text-xs font-medium"
                  :class="currentStep >= stepIndex + 1 ? 'text-blue-600' : 'text-gray-500'">
              {{ step.name }}
            </span>
          </li>
        </ol>
      </nav>
    </div>

    <div class="space-y-6">
      <!-- Step 1: Select Estimate -->
      <div v-if="currentStep === 1" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">견적 선택</h3>
          
          <!-- Search Estimates -->
          <div class="mb-4">
            <div class="max-w-lg">
              <label for="estimate-search" class="sr-only">견적 검색</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
                </div>
                <input
                  id="estimate-search"
                  v-model="estimateSearchQuery"
                  type="search"
                  placeholder="주문번호, 회원명으로 검색..."
                  class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 text-sm"
                >
              </div>
            </div>
          </div>

          <!-- Estimates List -->
          <div class="border border-gray-200 rounded-md">
            <div v-if="loadingEstimates" class="p-6 text-center">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
              <span class="mt-2 text-sm text-gray-600">견적을 불러오는 중...</span>
            </div>
            
            <div v-else-if="filteredEstimates.length === 0" class="p-6 text-center">
              <DocumentTextIcon class="mx-auto h-12 w-12 text-gray-400" />
              <h3 class="mt-2 text-sm font-medium text-gray-900">견적이 없습니다</h3>
              <p class="mt-1 text-sm text-gray-500">결제 처리할 견적이 없습니다.</p>
            </div>
            
            <ul v-else class="divide-y divide-gray-200">
              <li v-for="estimate in filteredEstimates" :key="estimate.id">
                <div class="p-4 hover:bg-gray-50 cursor-pointer"
                     :class="selectedEstimate?.id === estimate.id ? 'bg-blue-50 border-blue-200' : ''"
                     @click="selectEstimate(estimate)">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center">
                      <input
                        :checked="selectedEstimate?.id === estimate.id"
                        type="radio"
                        class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                        @change="selectEstimate(estimate)"
                      >
                      <div class="ml-4">
                        <div class="flex items-center">
                          <p class="text-sm font-medium text-gray-900">{{ estimate.orderNumber }}</p>
                          <span class="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                                :class="estimate.status === 'approved' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'">
                            {{ estimate.status === 'approved' ? '승인됨' : '대기중' }}
                          </span>
                        </div>
                        <p class="text-sm text-gray-500">
                          {{ estimate.memberName }} | {{ formatDate(estimate.createdAt) }}
                        </p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-sm font-medium text-gray-900">
                        {{ formatCurrency(estimate.totalAmount, estimate.currency) }}
                      </p>
                      <p class="text-sm text-gray-500">{{ estimate.itemCount }}개 품목</p>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Step 2: Payment Method -->
      <div v-if="currentStep === 2" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">결제 수단 선택</h3>
          
          <!-- Selected Estimate Summary -->
          <div v-if="selectedEstimate" class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-md">
            <h4 class="text-sm font-medium text-blue-900">선택된 견적</h4>
            <div class="mt-2 text-sm text-blue-700">
              <p>주문번호: {{ selectedEstimate.orderNumber }}</p>
              <p>총 금액: {{ formatCurrency(selectedEstimate.totalAmount, selectedEstimate.currency) }}</p>
            </div>
          </div>

          <!-- Payment Methods -->
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div v-for="method in paymentMethods" :key="method.id"
                 class="relative rounded-lg border p-4 cursor-pointer focus:outline-none"
                 :class="selectedPaymentMethod?.id === method.id 
                   ? 'border-blue-600 ring-2 ring-blue-600 bg-blue-50' 
                   : 'border-gray-300 hover:border-gray-400'"
                 @click="selectPaymentMethod(method)">
              <div class="flex items-center">
                <input
                  :checked="selectedPaymentMethod?.id === method.id"
                  type="radio"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                >
                <div class="ml-3">
                  <component :is="method.icon" class="h-6 w-6 text-gray-600" />
                </div>
              </div>
              <div class="mt-2">
                <label class="block text-sm font-medium text-gray-900 cursor-pointer">
                  {{ method.name }}
                </label>
                <p class="text-xs text-gray-500">{{ method.description }}</p>
                <p v-if="method.fee" class="text-xs text-gray-400 mt-1">
                  수수료: {{ method.fee }}
                </p>
              </div>
            </div>
          </div>

          <!-- Installment Options (for credit card) -->
          <div v-if="selectedPaymentMethod?.id === 'credit_card'" class="mt-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">할부 개월</label>
            <select
              v-model="selectedInstallment"
              class="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
            >
              <option value="1">일시불</option>
              <option value="3">3개월 (수수료: 2.5%)</option>
              <option value="6">6개월 (수수료: 4.5%)</option>
              <option value="12">12개월 (수수료: 7.5%)</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Step 3: Payment Details -->
      <div v-if="currentStep === 3" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">결제 정보 입력</h3>
          
          <!-- Credit Card Form -->
          <div v-if="selectedPaymentMethod?.id === 'credit_card'" class="space-y-4">
            <div>
              <label for="card-number" class="block text-sm font-medium text-gray-700">카드번호</label>
              <input
                id="card-number"
                v-model="cardDetails.number"
                type="text"
                placeholder="0000-0000-0000-0000"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                @input="formatCardNumber"
              >
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label for="card-expiry" class="block text-sm font-medium text-gray-700">만료일</label>
                <input
                  id="card-expiry"
                  v-model="cardDetails.expiry"
                  type="text"
                  placeholder="MM/YY"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  @input="formatExpiry"
                >
              </div>
              <div>
                <label for="card-cvc" class="block text-sm font-medium text-gray-700">CVC</label>
                <input
                  id="card-cvc"
                  v-model="cardDetails.cvc"
                  type="text"
                  placeholder="123"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                >
              </div>
            </div>
            
            <div>
              <label for="card-holder" class="block text-sm font-medium text-gray-700">카드 소유자명</label>
              <input
                id="card-holder"
                v-model="cardDetails.holderName"
                type="text"
                placeholder="카드에 표시된 이름을 입력하세요"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              >
            </div>
          </div>

          <!-- Bank Transfer Info -->
          <div v-if="selectedPaymentMethod?.id === 'bank_transfer'" class="space-y-4">
            <div class="bg-yellow-50 border border-yellow-200 rounded-md p-4">
              <div class="flex">
                <InformationCircleIcon class="h-5 w-5 text-yellow-400" />
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-yellow-800">계좌이체 안내</h3>
                  <div class="mt-2 text-sm text-yellow-700">
                    <p>아래 계좌로 결제 금액을 입금해 주세요.</p>
                    <div class="mt-2 font-mono">
                      <p>은행: 국민은행</p>
                      <p>계좌번호: 123-456-789012</p>
                      <p>예금주: ㈜YCS물류</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Mobile Payment -->
          <div v-if="selectedPaymentMethod?.id === 'mobile_payment'" class="space-y-4">
            <div>
              <label for="phone-number" class="block text-sm font-medium text-gray-700">휴대폰 번호</label>
              <input
                id="phone-number"
                v-model="mobilePaymentDetails.phoneNumber"
                type="tel"
                placeholder="010-0000-0000"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              >
            </div>
          </div>

          <!-- Crypto Payment -->
          <div v-if="selectedPaymentMethod?.id === 'crypto'" class="space-y-4">
            <div>
              <label for="crypto-currency" class="block text-sm font-medium text-gray-700">암호화폐 선택</label>
              <select
                id="crypto-currency"
                v-model="cryptoDetails.currency"
                class="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
              >
                <option value="BTC">Bitcoin (BTC)</option>
                <option value="ETH">Ethereum (ETH)</option>
                <option value="USDT">Tether (USDT)</option>
              </select>
            </div>
            
            <div v-if="cryptoDetails.currency" class="bg-blue-50 border border-blue-200 rounded-md p-4">
              <h4 class="text-sm font-medium text-blue-900">{{ cryptoDetails.currency }} 송금 주소</h4>
              <p class="mt-1 text-sm text-blue-700 font-mono break-all">
                {{ getCryptoAddress(cryptoDetails.currency) }}
              </p>
              <p class="mt-2 text-xs text-blue-600">
                정확한 금액: {{ getCryptoAmount(selectedEstimate?.totalAmount || 0, cryptoDetails.currency) }} {{ cryptoDetails.currency }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 4: Review & Confirm -->
      <div v-if="currentStep === 4" class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">결제 확인</h3>
          
          <div class="space-y-6">
            <!-- Payment Summary -->
            <div class="border-t border-b border-gray-200 py-4">
              <dl class="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                <div>
                  <dt class="text-sm font-medium text-gray-500">주문번호</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ selectedEstimate?.orderNumber }}</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">회원명</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ selectedEstimate?.memberName }}</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">결제 수단</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ selectedPaymentMethod?.name }}</dd>
                </div>
                <div v-if="selectedInstallment > 1">
                  <dt class="text-sm font-medium text-gray-500">할부</dt>
                  <dd class="mt-1 text-sm text-gray-900">{{ selectedInstallment }}개월</dd>
                </div>
                <div>
                  <dt class="text-sm font-medium text-gray-500">결제 금액</dt>
                  <dd class="mt-1 text-lg font-bold text-gray-900">
                    {{ formatCurrency(totalAmount, selectedEstimate?.currency || 'KRW') }}
                  </dd>
                </div>
              </dl>
            </div>

            <!-- Terms and Conditions -->
            <div>
              <div class="flex items-start">
                <div class="flex items-center h-5">
                  <input
                    id="terms"
                    v-model="agreedToTerms"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  >
                </div>
                <div class="ml-3 text-sm">
                  <label for="terms" class="font-medium text-gray-700">
                    결제 약관에 동의합니다
                  </label>
                  <p class="text-gray-500">
                    <a href="#" class="text-blue-600 hover:text-blue-500">이용약관</a> 및 
                    <a href="#" class="text-blue-600 hover:text-blue-500">개인정보처리방침</a>에 동의합니다.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Navigation Buttons -->
      <div class="flex justify-between">
        <button
          v-if="currentStep > 1"
          @click="previousStep"
          :disabled="processing"
          class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
        >
          <ChevronLeftIcon class="h-4 w-4 mr-2" />
          이전
        </button>
        <div v-else></div>
        
        <button
          v-if="currentStep < steps.length"
          @click="nextStep"
          :disabled="!canProceedToNext || processing"
          class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          다음
          <ChevronRightIcon class="h-4 w-4 ml-2" />
        </button>
        
        <button
          v-else
          @click="processPayment"
          :disabled="!canProceedToNext || processing"
          class="inline-flex items-center px-6 py-3 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="processing" class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></span>
          {{ processing ? '결제 처리중...' : '결제하기' }}
        </button>
      </div>
    </div>

    <!-- Success Modal -->
    <div
      v-if="showSuccessModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
      @click="closeSuccessModal"
    >
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3 text-center">
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
            <CheckIcon class="h-6 w-6 text-green-600" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mt-2">결제 완료</h3>
          <div class="mt-2 px-7 py-3">
            <p class="text-sm text-gray-500">
              결제가 성공적으로 처리되었습니다.<br>
              결제번호: {{ paymentResult?.paymentNumber }}
            </p>
          </div>
          <div class="items-center px-4 py-3 space-y-2">
            <button
              @click="viewPaymentDetail"
              class="px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md w-full hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-300"
            >
              결제 상세보기
            </button>
            <button
              @click="closeSuccessModal"
              class="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md w-full hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-300"
            >
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckIcon,
  MagnifyingGlassIcon,
  DocumentTextIcon,
  CreditCardIcon,
  BanknotesIcon,
  DevicePhoneMobileIcon,
  CurrencyBitcoinIcon,
  InformationCircleIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

interface Estimate {
  id: string
  orderNumber: string
  memberId: string
  memberName: string
  totalAmount: number
  currency: string
  itemCount: number
  status: string
  createdAt: string
}

interface PaymentMethod {
  id: string
  name: string
  description: string
  icon: any
  fee?: string
}

interface PaymentResult {
  id: string
  paymentNumber: string
  status: string
}

const router = useRouter()

const currentStep = ref(1)
const processing = ref(false)
const loadingEstimates = ref(false)
const showSuccessModal = ref(false)

// Step definitions
const steps = ref([
  { id: 1, name: '견적 선택' },
  { id: 2, name: '결제수단' },
  { id: 3, name: '결제정보' },
  { id: 4, name: '확인' }
])

// Step 1: Estimate selection
const estimateSearchQuery = ref('')
const selectedEstimate = ref<Estimate | null>(null)
const estimates = ref<Estimate[]>([])

// Step 2: Payment method selection
const selectedPaymentMethod = ref<PaymentMethod | null>(null)
const selectedInstallment = ref(1)

const paymentMethods: PaymentMethod[] = [
  {
    id: 'credit_card',
    name: '신용카드',
    description: '국내외 신용카드',
    icon: CreditCardIcon,
    fee: '2.5%'
  },
  {
    id: 'bank_transfer',
    name: '계좌이체',
    description: '실시간 계좌이체',
    icon: BanknotesIcon
  },
  {
    id: 'mobile_payment',
    name: '모바일결제',
    description: '카카오페이, 네이버페이',
    icon: DevicePhoneMobileIcon,
    fee: '2.0%'
  },
  {
    id: 'crypto',
    name: '암호화폐',
    description: 'Bitcoin, Ethereum',
    icon: CurrencyBitcoinIcon
  }
]

// Step 3: Payment details
const cardDetails = ref({
  number: '',
  expiry: '',
  cvc: '',
  holderName: ''
})

const mobilePaymentDetails = ref({
  phoneNumber: ''
})

const cryptoDetails = ref({
  currency: 'BTC'
})

// Step 4: Confirmation
const agreedToTerms = ref(false)
const paymentResult = ref<PaymentResult | null>(null)

// Mock data
const mockEstimates: Estimate[] = [
  {
    id: 'est_001',
    orderNumber: 'ORD-2024-001',
    memberId: 'user_001',
    memberName: '김철수',
    totalAmount: 150000,
    currency: 'KRW',
    itemCount: 5,
    status: 'approved',
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: 'est_002',
    orderNumber: 'ORD-2024-002',
    memberId: 'user_002',
    memberName: '박영희',
    totalAmount: 2500,
    currency: 'THB',
    itemCount: 3,
    status: 'pending',
    createdAt: '2024-01-14T14:20:00Z'
  },
  {
    id: 'est_003',
    orderNumber: 'ORD-2024-003',
    memberId: 'user_003',
    memberName: '이민수',
    totalAmount: 85.50,
    currency: 'USD',
    itemCount: 8,
    status: 'approved',
    createdAt: '2024-01-13T09:15:00Z'
  }
]

// Computed properties
const filteredEstimates = computed(() => {
  if (!estimateSearchQuery.value) return estimates.value
  
  const query = estimateSearchQuery.value.toLowerCase()
  return estimates.value.filter(estimate =>
    estimate.orderNumber.toLowerCase().includes(query) ||
    estimate.memberName.toLowerCase().includes(query)
  )
})

const canProceedToNext = computed(() => {
  switch (currentStep.value) {
    case 1:
      return selectedEstimate.value !== null
    case 2:
      return selectedPaymentMethod.value !== null
    case 3:
      if (selectedPaymentMethod.value?.id === 'credit_card') {
        return cardDetails.value.number && cardDetails.value.expiry && 
               cardDetails.value.cvc && cardDetails.value.holderName
      } else if (selectedPaymentMethod.value?.id === 'mobile_payment') {
        return mobilePaymentDetails.value.phoneNumber
      } else if (selectedPaymentMethod.value?.id === 'crypto') {
        return cryptoDetails.value.currency
      }
      return true // bank_transfer
    case 4:
      return agreedToTerms.value
    default:
      return false
  }
})

const totalAmount = computed(() => {
  if (!selectedEstimate.value) return 0
  
  let amount = selectedEstimate.value.totalAmount
  
  // Add installment fee for credit card
  if (selectedPaymentMethod.value?.id === 'credit_card' && selectedInstallment.value > 1) {
    const feeRates = { 3: 0.025, 6: 0.045, 12: 0.075 }
    const feeRate = feeRates[selectedInstallment.value as keyof typeof feeRates] || 0
    amount += amount * feeRate
  }
  
  return amount
})

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
    day: '2-digit'
  })
}

const formatCardNumber = (event: Event) => {
  const input = event.target as HTMLInputElement
  const value = input.value.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim()
  cardDetails.value.number = value.substring(0, 19) // Max 16 digits + 3 spaces
}

const formatExpiry = (event: Event) => {
  const input = event.target as HTMLInputElement
  const value = input.value.replace(/\D/g, '').replace(/(\d{2})(\d)/, '$1/$2')
  cardDetails.value.expiry = value.substring(0, 5) // MM/YY
}

const getCryptoAddress = (currency: string): string => {
  const addresses = {
    BTC: '1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa',
    ETH: '0x742d35Cc6634C0532925a3b8D44dC7De0318Bbb9',
    USDT: '0x742d35Cc6634C0532925a3b8D44dC7De0318Bbb9'
  }
  return addresses[currency as keyof typeof addresses] || ''
}

const getCryptoAmount = (amount: number, currency: string): string => {
  // Mock conversion rates
  const rates = { BTC: 0.0000234, ETH: 0.000456, USDT: 0.034 }
  const cryptoAmount = amount * (rates[currency as keyof typeof rates] || 1)
  return cryptoAmount.toFixed(8)
}

// Actions
const selectEstimate = (estimate: Estimate) => {
  selectedEstimate.value = estimate
}

const selectPaymentMethod = (method: PaymentMethod) => {
  selectedPaymentMethod.value = method
}

const nextStep = () => {
  if (canProceedToNext.value && currentStep.value < steps.value.length) {
    currentStep.value++
  }
}

const previousStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const processPayment = async () => {
  if (!canProceedToNext.value || !selectedEstimate.value || !selectedPaymentMethod.value) return
  
  processing.value = true
  
  try {
    // Simulate payment processing
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    // Mock payment result
    paymentResult.value = {
      id: 'pay_' + Date.now(),
      paymentNumber: 'PAY-2024-' + Date.now().toString().slice(-6),
      status: 'completed'
    }
    
    showSuccessModal.value = true
    
    // Emit success event
    window.dispatchEvent(new CustomEvent('payment-success', {
      detail: { 
        paymentId: paymentResult.value.id,
        paymentNumber: paymentResult.value.paymentNumber,
        type: 'new_payment'
      }
    }))
    
  } catch (error) {
    window.dispatchEvent(new CustomEvent('payment-error', {
      detail: { message: '결제 처리 중 오류가 발생했습니다.' }
    }))
  } finally {
    processing.value = false
  }
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
  // Reset form
  currentStep.value = 1
  selectedEstimate.value = null
  selectedPaymentMethod.value = null
  selectedInstallment.value = 1
  cardDetails.value = { number: '', expiry: '', cvc: '', holderName: '' }
  mobilePaymentDetails.value = { phoneNumber: '' }
  cryptoDetails.value = { currency: 'BTC' }
  agreedToTerms.value = false
  paymentResult.value = null
}

const viewPaymentDetail = () => {
  if (paymentResult.value) {
    router.push(`/payments/${paymentResult.value.id}`)
  }
}

const loadEstimates = async () => {
  loadingEstimates.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    estimates.value = mockEstimates
  } finally {
    loadingEstimates.value = false
  }
}

onMounted(() => {
  loadEstimates()
})
</script>