<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-gray-900">HS Code 검색</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-gray-600">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

      <!-- 검색 타입 선택 -->
      <div class="mb-4">
        <div class="flex space-x-4">
          <label class="flex items-center">
            <input
              type="radio"
              v-model="searchType"
              value="product"
              class="mr-2 text-blue-600"
            />
            품목명으로 검색
          </label>
          <label class="flex items-center">
            <input
              type="radio"
              v-model="searchType"
              value="hscode"
              class="mr-2 text-blue-600"
            />
            HS Code로 검색
          </label>
        </div>
      </div>

      <!-- 검색 입력 -->
      <div class="mb-4">
        <div class="relative">
          <input
            v-model="searchQuery"
            :placeholder="searchType === 'product' ? '품목명을 입력하세요 (예: 화장품, 의류)' : 'HS Code를 입력하세요 (예: 3304.99)'"
            class="form-input"
            @keyup.enter="performSearch"
          />
          <button
            @click="performSearch"
            :disabled="loading || !searchQuery.trim()"
            class="absolute right-2 top-1/2 transform -translate-y-1/2 px-4 py-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            검색
          </button>
        </div>
      </div>

      <!-- 로딩 상태 -->
      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-red-800">{{ error }}</p>
      </div>

      <!-- 검색 결과 -->
      <div v-if="searchResults.length > 0" class="mb-4">
        <h4 class="text-md font-medium text-gray-900 mb-3">검색 결과</h4>
        <div class="space-y-2 max-h-64 overflow-y-auto">
          <div
            v-for="item in searchResults"
            :key="item.hsCode"
            @click="selectHSCode(item)"
            class="p-3 border border-gray-200 rounded-lg hover:bg-blue-50 cursor-pointer transition-colors"
          >
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <div class="font-medium text-gray-900">{{ item.hsCode }}</div>
                <div class="text-sm text-gray-600 mt-1">{{ item.koreanName }}</div>
                <div v-if="item.englishName" class="text-xs text-gray-500 mt-1">{{ item.englishName }}</div>
              </div>
              <div v-if="item.unit" class="text-xs text-gray-500 ml-2">{{ item.unit }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 선택된 HS Code -->
      <div v-if="selectedItem" class="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
        <h4 class="text-md font-medium text-green-900 mb-2">선택된 품목</h4>
        <div class="text-sm text-green-800">
          <div><strong>HS Code:</strong> {{ selectedItem.hsCode }}</div>
          <div><strong>품목명:</strong> {{ selectedItem.koreanName }}</div>
          <div v-if="selectedItem.englishName"><strong>영문명:</strong> {{ selectedItem.englishName }}</div>
        </div>
      </div>

      <!-- 관세율 정보 -->
      <div v-if="selectedItem && tariffInfo" class="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
        <h4 class="text-md font-medium text-blue-900 mb-2">관세율 정보</h4>
        <div class="grid grid-cols-2 gap-4 text-sm text-blue-800">
          <div><strong>기본 관세율:</strong> {{ tariffInfo.basicRate }}%</div>
          <div><strong>WTO 관세율:</strong> {{ tariffInfo.wtoRate }}%</div>
          <div><strong>특혜 관세율:</strong> {{ tariffInfo.specialRate }}%</div>
          <div><strong>적용 관세율:</strong> {{ getAppliedRate(tariffInfo) }}%</div>
        </div>
      </div>

      <!-- 관세 계산 -->
      <div v-if="selectedItem" class="mb-4">
        <h4 class="text-md font-medium text-gray-900 mb-3">관세 계산</h4>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">수량</label>
            <input
              v-model.number="dutyCalculation.quantity"
              type="number"
              min="1"
              step="1"
              class="form-input"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">신고가격 (USD)</label>
            <input
              v-model.number="dutyCalculation.value"
              type="number"
              min="0"
              step="0.01"
              class="form-input"
            />
          </div>
        </div>
        
        <button
          @click="calculateDuty"
          :disabled="!dutyCalculation.quantity || !dutyCalculation.value || calculatingDuty"
          class="mt-3 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
        >
          관세 계산
        </button>

        <!-- 관세 계산 결과 -->
        <div v-if="dutyResult" class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <h5 class="text-sm font-medium text-yellow-900 mb-2">관세 계산 결과</h5>
          <div class="grid grid-cols-2 gap-4 text-sm text-yellow-800">
            <div><strong>적용 관세율:</strong> {{ dutyResult.appliedRate }}%</div>
            <div><strong>관세액:</strong> ${{ dutyResult.dutyAmount }}</div>
            <div><strong>신고가격:</strong> ${{ dutyCalculation.value }}</div>
            <div><strong>총액:</strong> ${{ dutyResult.totalAmount }}</div>
          </div>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="flex justify-end space-x-3">
        <button
          @click="$emit('close')"
          class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          취소
        </button>
        <button
          @click="confirmSelection"
          :disabled="!selectedItem"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          선택 완료
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { hsCodeApi } from '@/utils/api'

interface Props {
  show: boolean
}

interface HSCodeItem {
  hsCode: string
  koreanName: string
  englishName?: string
  unit?: string
}

interface TariffInfo {
  basicRate: number
  wtoRate: number
  specialRate: number
}

interface DutyResult {
  appliedRate: number
  dutyAmount: number
  totalAmount: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  select: [item: HSCodeItem & { tariffInfo?: TariffInfo; dutyResult?: DutyResult }]
}>()

const searchType = ref<'product' | 'hscode'>('product')
const searchQuery = ref('')
const loading = ref(false)
const error = ref('')
const searchResults = ref<HSCodeItem[]>([])
const selectedItem = ref<HSCodeItem | null>(null)
const tariffInfo = ref<TariffInfo | null>(null)

const dutyCalculation = ref({
  quantity: 1,
  value: 0
})
const calculatingDuty = ref(false)
const dutyResult = ref<DutyResult | null>(null)

watch(() => props.show, (newShow) => {
  if (newShow) {
    // 모달이 열릴 때 초기화
    searchQuery.value = ''
    searchResults.value = []
    selectedItem.value = null
    tariffInfo.value = null
    dutyResult.value = null
    error.value = ''
  }
})

const performSearch = async () => {
  if (!searchQuery.value.trim()) return

  loading.value = true
  error.value = ''
  searchResults.value = []

  try {
    let response
    if (searchType.value === 'product') {
      response = await hsCodeApi.searchByProduct(searchQuery.value.trim())
    } else {
      response = await hsCodeApi.searchByHSCode(searchQuery.value.trim())
    }

    if (response.success && response.data.success) {
      searchResults.value = response.data.items || []
      if (searchResults.value.length === 0) {
        error.value = '검색 결과가 없습니다.'
      }
    } else {
      error.value = response.data?.error || '검색에 실패했습니다.'
    }
  } catch (err: any) {
    error.value = '검색 중 오류가 발생했습니다: ' + (err.message || '알 수 없는 오류')
  } finally {
    loading.value = false
  }
}

const selectHSCode = async (item: HSCodeItem) => {
  selectedItem.value = item
  dutyResult.value = null
  
  // 관세율 정보 조회
  try {
    const response = await hsCodeApi.getTariffRate(item.hsCode)
    if (response.success && response.data.success && response.data.items.length > 0) {
      const tariffItem = response.data.items[0]
      tariffInfo.value = {
        basicRate: tariffItem.basicRate || 0,
        wtoRate: tariffItem.wtoRate || 0,
        specialRate: tariffItem.specialRate || 0
      }
    }
  } catch (err) {
    console.error('Failed to get tariff info:', err)
  }
}

const getAppliedRate = (tariff: TariffInfo): number => {
  // WTO 관세율이 있으면 우선 적용, 없으면 기본 관세율
  return tariff.wtoRate > 0 ? tariff.wtoRate : tariff.basicRate
}

const calculateDuty = async () => {
  if (!selectedItem.value || !dutyCalculation.value.quantity || !dutyCalculation.value.value) return

  calculatingDuty.value = true
  
  try {
    const response = await hsCodeApi.calculateDuty({
      hsCode: selectedItem.value.hsCode,
      quantity: dutyCalculation.value.quantity,
      value: dutyCalculation.value.value,
      currency: 'USD'
    })

    if (response.success && response.data) {
      dutyResult.value = {
        appliedRate: response.data.appliedRate,
        dutyAmount: response.data.dutyAmount,
        totalAmount: response.data.totalAmount
      }
    }
  } catch (err) {
    console.error('Failed to calculate duty:', err)
  } finally {
    calculatingDuty.value = false
  }
}

const confirmSelection = () => {
  if (!selectedItem.value) return

  emit('select', {
    ...selectedItem.value,
    tariffInfo: tariffInfo.value || undefined,
    dutyResult: dutyResult.value || undefined
  })
}
</script>

<style scoped>
.form-input {
  width: 100%;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #dc2626;
  background-color: #fef2f2;
}

label {
  font-size: 0.875rem;
  color: #374151;
}
</style>