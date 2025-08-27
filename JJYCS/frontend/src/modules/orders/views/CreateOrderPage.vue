<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4 pb-24">
    <!-- Success/Error Messages -->
    <div v-if="success || error" class="fixed top-4 right-4 z-50">
      <div :class="error ? 'bg-red-50 border border-red-200 text-red-700' : 'bg-green-50 border border-green-200 text-green-700'" class="px-4 py-3 rounded-xl">
        {{ success || error }}
      </div>
    </div>

    <!-- Validation Messages -->
    <div v-if="validationMessages.length > 0" class="mb-4">
      <div 
        v-for="(msg, index) in validationMessages" 
        :key="index" 
        :class="'bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-xl mb-2'"
      >
        {{ msg.message }}
      </div>
    </div>

    <!-- Header -->
    <div class="flex items-center gap-4 mb-6">
      <button @click="goBack" class="text-blue-600 hover:text-blue-700 hover:bg-blue-50 p-2 rounded-lg transition-all flex items-center gap-2">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"/>
        </svg>
        돌아가기
      </button>
      <div>
        <h1 class="text-xl font-bold text-blue-900">새 주문 접수</h1>
        <p class="text-sm text-blue-600">주문 정보를 입력해주세요</p>
      </div>
    </div>
    <!-- Progress Steps -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-center gap-4">
        <div class="flex flex-col items-center">
          <div class="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-semibold">1</div>
          <span class="text-xs text-blue-700 mt-2">기본 정보</span>
        </div>
        <div class="w-8 h-1 bg-gray-200 rounded"></div>
        <div class="flex flex-col items-center">
          <div class="w-8 h-8 bg-gray-200 text-gray-500 rounded-full flex items-center justify-center text-sm font-semibold">2</div>
          <span class="text-xs text-gray-500 mt-2">품목 등록</span>
        </div>
        <div class="w-8 h-1 bg-gray-200 rounded"></div>
        <div class="flex flex-col items-center">
          <div class="w-8 h-8 bg-gray-200 text-gray-500 rounded-full flex items-center justify-center text-sm font-semibold">3</div>
          <span class="text-xs text-gray-500 mt-2">접수 완료</span>
        </div>
      </div>
    </div>

    <!-- User Info Display -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
          <svg class="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/>
          </svg>
        </div>
        <div>
          <h2 class="text-lg font-semibold text-blue-900">{{ authStore.user?.name }}</h2>
          <p class="text-sm text-blue-600">{{ getUserTypeText }}</p>
          <p class="text-xs text-gray-500">회원코드: {{ authStore.user?.memberCode || 'No code' }}</p>
        </div>
      </div>
    </div>

    <!-- Order Form -->
    <form @submit.prevent="submitOrder">
      <!-- Basic Information -->
      <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 7v10a2 2 0 002 2h10a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2 2v0z"/>
            <path d="M9 5a2 2 0 012-2h6a2 2 0 012 2v6a2 2 0 01-2 2H9a2 2 0 01-2-2V5z"/>
          </svg>
          기본 정보
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">
              우체국 송장번호 <span class="text-red-500">*</span>
            </label>
            <input 
              type="text" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              v-model="orderForm.trackingNumber"
              placeholder="EE123456789KR 형식으로 입력해주세요" 
              required
              pattern="^[A-Z]{2}[0-9]{9}[A-Z]{2}$"
            />
            <p class="text-xs text-gray-500 mt-1">
              우체국 송장번호가 없으면 주문을 접수할 수 없습니다.
            </p>
          </div>
        </div>
      </div>

      <!-- Shipping Information -->
      <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"/>
            <path d="M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1V5a1 1 0 00-1-1H3zM14 7a1 1 0 00-1 1v6.05A2.5 2.5 0 0115.95 16H17a1 1 0 001-1V8a1 1 0 00-1-1h-3z"/>
          </svg>
          배송 정보
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">배송 유형</label>
            <div class="grid grid-cols-2 gap-4">
              <label :class="['p-3 border rounded-lg cursor-pointer transition-all', orderForm.shippingType === 'sea' ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-300']">
                <input 
                  type="radio" 
                  name="shippingType" 
                  value="sea" 
                  v-model="orderForm.shippingType"
                  class="sr-only"
                />
                <div class="font-medium text-gray-900">해상운송</div>
                <div class="text-sm text-gray-600">경제적, 15-30일</div>
              </label>
              <label :class="['p-3 border rounded-lg cursor-pointer transition-all', orderForm.shippingType === 'air' ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-300']">
                <input 
                  type="radio" 
                  name="shippingType" 
                  value="air" 
                  v-model="orderForm.shippingType"
                  class="sr-only"
                />
                <div class="font-medium text-gray-900">항공운송</div>
                <div class="text-sm text-gray-600">신속, 3-7일</div>
              </label>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-blue-700 mb-2">도착 국가</label>
              <select class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" v-model="orderForm.country">
                <option value="thailand">🇹🇭 태국</option>
                <option value="vietnam">🇻🇳 베트남</option>
                <option value="philippines">🇵🇭 필리핀</option>
                <option value="indonesia">🇮🇩 인도네시아</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-blue-700 mb-2">우편번호</label>
              <input 
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                v-model="orderForm.postalCode"
                :placeholder="getPostalCodeGuide(orderForm.country)"
              />
              <p class="text-xs text-gray-500 mt-1">
                {{ getPostalCodeGuide(orderForm.country) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Recipient Information -->
      <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"/>
          </svg>
          수취인 정보
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">
              수취인 이름 <span class="text-red-500">*</span>
            </label>
            <input 
              type="text" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              v-model="orderForm.recipientName"
              placeholder="수취인 이름을 입력해주세요" 
              required
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">
              수취인 연락처 <span class="text-red-500">*</span>
            </label>
            <input 
              type="tel" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              v-model="orderForm.recipientPhone"
              placeholder="수취인 연락처를 입력해주세요" 
              required
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">
              수취인 주소 <span class="text-red-500">*</span>
            </label>
            <textarea 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-vertical min-h-[4rem]" 
              v-model="orderForm.recipientAddress"
              placeholder="수취인 주소를 입력해주세요"
              required
            ></textarea>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">수취인 우편번호</label>
            <input 
              type="text" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              v-model="orderForm.recipientPostalCode"
              placeholder="수취인 우편번호"
            />
          </div>
        </div>
      </div>

      <!-- Items Section -->
      <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4z"/>
          </svg>
          품목 정보
        </div>
        <div class="space-y-4">
          <div v-for="(item, index) in orderForm.items" :key="index" class="border border-gray-200 rounded-xl p-4">
            <div class="flex justify-between items-center mb-4">
              <div class="font-semibold text-blue-900">품목 {{ index + 1 }}</div>
              <button 
                v-if="orderForm.items.length > 1"
                type="button" 
                class="text-red-500 hover:text-red-700 p-1"
                @click="removeItem(index)"
              >
                <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                </svg>
              </button>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  HS Code <span class="text-red-500">*</span>
                </label>
                <div class="flex gap-2">
                  <input 
                    type="text" 
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                    v-model="item.hsCode"
                    placeholder="HS Code를 입력하세요"
                    required
                  />
                  <button 
                    type="button" 
                    class="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    @click="openHSCodeSearch(index)"
                  >
                    검색
                  </button>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  품목 설명 <span class="text-red-500">*</span>
                </label>
                <input 
                  type="text" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model="item.description"
                  placeholder="품목 설명을 입력하세요"
                  required
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  수량 <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.quantity"
                  min="1"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  중량 (kg) <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.weight"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  가로 (cm) <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.width"
                  step="0.1"
                  min="0.1"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  세로 (cm) <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.height"
                  step="0.1"
                  min="0.1"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  높이 (cm) <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.depth"
                  step="0.1"
                  min="0.1"
                  required
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">
                  단가 (THB) <span class="text-red-500">*</span>
                </label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                  v-model.number="item.unitPrice"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-blue-700 mb-2">총 금액 (THB)</label>
                <input 
                  type="number" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50" 
                  :value="item.quantity * item.unitPrice"
                  readonly
                />
              </div>
            </div>

            <!-- CBM Display -->
            <div v-if="item.width && item.height && item.depth" class="bg-blue-50 rounded-lg p-3 flex items-center justify-between">
              <div class="text-sm text-gray-600">
                {{ item.width }} × {{ item.height }} × {{ item.depth }} = {{ item.cbm }} m³
              </div>
              <div class="text-lg font-semibold text-blue-700">{{ item.cbm }} m³</div>
            </div>

            <!-- Tariff Information Display -->
            <div v-if="item.tariffInfo" class="bg-green-50 border border-green-200 rounded-lg p-3 mt-3">
              <h5 class="text-sm font-medium text-green-900 mb-2">관세율 정보</h5>
              <div class="grid grid-cols-2 gap-2 text-xs text-green-800">
                <div><strong>기본 관세율:</strong> {{ item.tariffInfo.basicRate }}%</div>
                <div><strong>WTO 관세율:</strong> {{ item.tariffInfo.wtoRate }}%</div>
                <div><strong>특혜 관세율:</strong> {{ item.tariffInfo.specialRate }}%</div>
                <div><strong>적용 관세율:</strong> {{ getAppliedTariffRate(item.tariffInfo) }}%</div>
              </div>
            </div>

            <!-- Duty Calculation Display -->
            <div v-if="item.dutyResult" class="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mt-3">
              <h5 class="text-sm font-medium text-yellow-900 mb-2">관세 계산 결과</h5>
              <div class="grid grid-cols-2 gap-2 text-xs text-yellow-800">
                <div><strong>적용 관세율:</strong> {{ item.dutyResult.appliedRate }}%</div>
                <div><strong>관세액:</strong> ${{ item.dutyResult.dutyAmount }}</div>
                <div><strong>신고가격:</strong> ${{ item.unitPrice * item.quantity }}</div>
                <div><strong>총액:</strong> ${{ item.dutyResult.totalAmount }}</div>
              </div>
            </div>
          </div>

          <button 
            type="button" 
            class="w-full p-3 border-2 border-dashed border-gray-300 rounded-xl text-gray-500 hover:border-blue-500 hover:text-blue-600 flex items-center justify-center gap-2 transition-all"
            @click="addItem"
          >
            <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
            품목 추가
          </button>
        </div>
      </div>

      <!-- Form Actions -->
      <div class="flex gap-4 pt-6">
        <button 
          type="button" 
          class="flex-1 h-14 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 font-medium"
          @click="saveDraft"
          :disabled="loading"
        >
          {{ loading ? '저장 중...' : '임시저장' }}
        </button>
        <button 
          type="submit" 
          class="flex-1 h-14 bg-blue-600 text-white rounded-xl hover:bg-blue-700 font-medium"
          :disabled="loading"
        >
          {{ loading ? '처리 중...' : '접수완료' }}
        </button>
      </div>
    </form>

    <!-- HS Code Search Modal -->
    <HSCodeSearchModal 
      :show="showHSModal" 
      @close="showHSModal = false"
      @select="selectHSCodeFromModal"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/utils/api'
import { USER_TYPE } from '@/types'
import HSCodeSearchModal from '@/components/order/HSCodeSearchModal.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const success = ref('')
const error = ref('')
const validationMessages = ref([])

const showHSModal = ref(false)
const hsSearchTerm = ref('')
const currentItemIndex = ref<number>(-1)

const orderForm = reactive({
  trackingNumber: '',
  shippingType: 'sea',
  country: 'thailand',
  postalCode: '',
  recipientName: '',
  recipientPhone: '',
  recipientAddress: '',
  recipientPostalCode: '',
  items: [{
    hsCode: '',
    description: '',
    quantity: 1,
    weight: 0,
    width: 0,
    height: 0,
    depth: 0,
    unitPrice: 0,
    cbm: 0
  }],
  specialRequests: ''
})

const hsCodes = [
  { code: '1905.31', description: 'Korean Chocolate Sticks' },
  { code: '1806.32', description: 'Chocolate Snacks' },
  { code: '3304.10', description: 'Lipstick Set' },
  { code: '3304.20', description: 'Foundation' },
  { code: '0902.30', description: 'Korean Traditional Tea Set' },
  { code: '6204.42', description: 'Women Clothing Set' },
  { code: '3401.11', description: 'Toiletries Set' },
  { code: '0902.10', description: 'Green Tea Gift Set' }
]

const filteredHSCodes = computed(() => {
  if (!hsSearchTerm.value) return hsCodes
  return hsCodes.filter(code => 
    code.description.toLowerCase().includes(hsSearchTerm.value.toLowerCase()) ||
    code.code.includes(hsSearchTerm.value)
  )
})

const getUserTypeText = computed(() => {
  const userType = authStore.user?.userType
  switch(userType) {
    case USER_TYPE.GENERAL:
      return '개인 고객'
    case USER_TYPE.CORPORATE:
      return '기업 고객'
    case USER_TYPE.PARTNER:
      return '파트너'
    case USER_TYPE.ADMIN:
      return '관리자'
    default:
      return '일반 사용자'
  }
})

const totalCBM = computed(() => {
  return orderForm.items.reduce((sum, item) => sum + (item.cbm || 0), 0)
})

const getPostalCodeGuide = (country) => {
  const guides = {
    thailand: '5자리 숫자 (예: 10110)',
    vietnam: '6자리 숫자 (예: 700000)',
    philippines: '4자리 숫자 (예: 1000)',
    indonesia: '5자리 숫자 (예: 10110)'
  }
  return guides[country] || '우편번호를 입력하세요'
}

const calculateCBM = (item) => {
  if (item.width && item.height && item.depth) {
    return ((item.width * item.height * item.depth) / 1000000).toFixed(6)
  }
  return 0
}

const addItem = () => {
  orderForm.items.push({
    hsCode: '',
    description: '',
    quantity: 1,
    weight: 0,
    width: 0,
    height: 0,
    depth: 0,
    unitPrice: 0,
    cbm: 0
  })
}

const removeItem = (index) => {
  if (orderForm.items.length > 1) {
    orderForm.items.splice(index, 1)
  }
}

const openHSCodeSearch = (itemIndex: number) => {
  currentItemIndex.value = itemIndex
  showHSModal.value = true
}

const selectHSCodeFromModal = (selectedItem: any) => {
  if (currentItemIndex.value >= 0 && currentItemIndex.value < orderForm.items.length) {
    const item = orderForm.items[currentItemIndex.value]
    
    // HS Code 정보 업데이트
    item.hsCode = selectedItem.hsCode
    item.description = selectedItem.koreanName || selectedItem.description
    
    // 관세율 정보가 있으면 저장
    if (selectedItem.tariffInfo) {
      item.tariffInfo = selectedItem.tariffInfo
    }
    
    // 관세 계산 결과가 있으면 저장
    if (selectedItem.dutyResult) {
      item.dutyResult = selectedItem.dutyResult
    }
    
    // 영문명이 있으면 저장
    if (selectedItem.englishName) {
      item.englishName = selectedItem.englishName
    }
  }
  
  showHSModal.value = false
  currentItemIndex.value = -1
}

const getAppliedTariffRate = (tariffInfo: any): number => {
  // WTO 관세율이 있으면 우선 적용, 없으면 기본 관세율
  return tariffInfo.wtoRate > 0 ? tariffInfo.wtoRate : tariffInfo.basicRate
}

const submitOrder = async () => {
  loading.value = true
  error.value = ''
  
  try {
    // Calculate CBM for all items
    orderForm.items.forEach(item => {
      item.cbm = parseFloat(calculateCBM(item))
    })
    
    // Check CBM limit (29 m³)
    if (totalCBM.value > 29) {
      const confirm = window.confirm(
        'CBM이 ' + totalCBM.value.toFixed(3) + 'm³로 29를 초과하여 항공 배송이 적용됩니다.\n' +
        '항공 배송료가 추가로 부과될 수 있습니다. 계속 진행하시겠습니까?'
      )
      if (!confirm) {
        loading.value = false
        return
      }
    }

    // Check high value items (THB 1,500)
    const highValueItems = orderForm.items.filter(item => item.unitPrice > 1500)
    if (highValueItems.length > 0) {
      const confirm = window.confirm(
        'THB 1,500을 초과하는 품목이 ' + highValueItems.length + '개 있습니다.\n' +
        '수취인 추가 정보가 필요할 수 있으며, 관세가 부과될 수 있습니다.\n' +
        '계속 진행하시겠습니까?'
      )
      if (!confirm) {
        loading.value = false
        return
      }
    }

    // Prepare order data for backend API
    const orderData = {
      userId: authStore.user?.id,
      trackingNumber: orderForm.trackingNumber,
      shippingType: orderForm.shippingType,
      country: orderForm.country,
      postalCode: orderForm.postalCode,
      recipientName: orderForm.recipientName,
      recipientPhone: orderForm.recipientPhone,
      recipientAddress: orderForm.recipientAddress,
      recipientPostalCode: orderForm.recipientPostalCode,
      items: orderForm.items.map(item => ({
        hsCode: item.hsCode,
        description: item.description,
        englishName: item.englishName,
        quantity: item.quantity,
        weight: item.weight,
        width: item.width,
        height: item.height,
        depth: item.depth,
        unitPrice: item.unitPrice,
        cbm: item.cbm,
        tariffInfo: item.tariffInfo,
        dutyResult: item.dutyResult
      })),
      specialRequests: orderForm.specialRequests || ''
    }

    console.log('Submitting order data:', orderData)

    // Submit order to backend
    const response = await ordersApi.createOrder(orderData)
    
    console.log('Order creation response:', response)

    if (response.success) {
      success.value = '주문이 성공적으로 접수되었습니다.'
      
      // Show warnings if any
      if (response.warnings) {
        setTimeout(() => {
          alert('주의사항: ' + response.warnings)
        }, 500)
      }
      
      setTimeout(() => {
        // Navigate to order detail page using order number
        if (response.order && response.order.orderNumber) {
          router.push('/orders/' + response.order.orderNumber)
        } else {
          router.push('/orders')
        }
      }, 2000)
    } else {
      error.value = response.error || '주문 접수 중 오류가 발생했습니다.'
    }
  } catch (err) {
    console.error('Order submission error:', err)
    error.value = '네트워크 오류가 발생했습니다: ' + (err.message || '알 수 없는 오류')
  } finally {
    loading.value = false
  }
}

const saveDraft = async () => {
  try {
    localStorage.setItem('orderDraft', JSON.stringify(orderForm))
    success.value = '임시저장되었습니다.'
    setTimeout(() => success.value = '', 3000)
  } catch (error) {
    console.error('임시저장 실패:', error)
  }
}

const goBack = () => {
  router.go(-1)
}

// Calculate CBM on mounted and when dimensions change
const updateItemCBM = () => {
  orderForm.items.forEach(item => {
    if (item.width && item.height && item.depth) {
      item.cbm = parseFloat(calculateCBM(item))
    }
  })
}

onMounted(() => {
  // Restore draft if exists
  try {
    const draftData = localStorage.getItem('orderDraft')
    if (draftData) {
      const draft = JSON.parse(draftData)
      Object.assign(orderForm, draft)
    }
  } catch (error) {
    console.error('임시저장 데이터 복원 실패:', error)
  }
  
  // Initial CBM calculation
  updateItemCBM()
})
</script>

