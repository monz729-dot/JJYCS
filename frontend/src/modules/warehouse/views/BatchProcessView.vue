<template>
  <div class="batch-process-view">
    <!-- 헤더 -->
    <div class="batch-header">
      <div class="header-info">
        <h1>일괄 처리</h1>
        <p class="batch-description">여러 항목을 한 번에 처리할 수 있습니다</p>
      </div>
      <div class="header-actions">
        <button @click="loadPresets" class="btn-preset">
          <i class="icon-bookmark"></i>
          프리셋 불러오기
        </button>
        <button @click="saveAsPreset" :disabled="selectedItems.length === 0" class="btn-save-preset">
          <i class="icon-save"></i>
          프리셋 저장
        </button>
      </div>
    </div>

    <!-- 처리 모드 선택 -->
    <div class="mode-selection">
      <h2>처리 모드</h2>
      <div class="mode-grid">
        <div
          v-for="mode in processingModes"
          :key="mode.value"
          class="mode-card"
          :class="{ active: selectedMode === mode.value }"
          @click="selectedMode = mode.value"
        >
          <div class="mode-icon" :class="mode.value">
            <i :class="mode.icon"></i>
          </div>
          <div class="mode-content">
            <h3>{{ mode.title }}</h3>
            <p>{{ mode.description }}</p>
            <div class="mode-stats">
              <span class="stat">{{ mode.count }}개 대기</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 항목 선택 방법 -->
    <div class="selection-methods">
      <h2>항목 선택</h2>
      <div class="method-tabs">
        <button
          v-for="method in selectionMethods"
          :key="method.value"
          class="method-tab"
          :class="{ active: selectedMethod === method.value }"
          @click="selectedMethod = method.value"
        >
          <i :class="method.icon"></i>
          {{ method.label }}
        </button>
      </div>
    </div>

    <!-- 스캔 모드 -->
    <div v-if="selectedMethod === 'scan'" class="scan-section">
      <div class="scan-container">
        <!-- 스캐너 -->
        <div class="scanner-area">
          <div class="scanner-header">
            <h3>QR/바코드 스캔</h3>
            <div class="scanner-controls">
              <button @click="toggleCamera" class="btn-toggle-camera">
                <i :class="showCamera ? 'icon-keyboard' : 'icon-camera'"></i>
                {{ showCamera ? '수동 입력' : '카메라' }}
              </button>
            </div>
          </div>

          <!-- 카메라 스캔 -->
          <div v-if="showCamera" class="camera-scanner">
            <QRScanner
              ref="qrScanner"
              @scan="handleScan"
              @error="handleScanError"
              :active="isScanning"
            />
            <div class="scan-overlay">
              <div class="scan-status" :class="{ scanning: isScanning }">
                {{ isScanning ? '스캔 중...' : '준비됨' }}
              </div>
            </div>
          </div>

          <!-- 수동 입력 -->
          <div v-else class="manual-input">
            <div class="input-group">
              <input
                v-model="manualInput"
                type="text"
                placeholder="라벨 코드 또는 주문 코드 입력..."
                class="manual-input-field"
                @keyup.enter="handleManualScan"
              />
              <button
                @click="handleManualScan"
                :disabled="!manualInput.trim()"
                class="btn-manual-add"
              >
                추가
              </button>
            </div>
          </div>
        </div>

        <!-- 스캔 통계 -->
        <div class="scan-stats">
          <div class="stat-item">
            <div class="stat-value">{{ scannedCount }}</div>
            <div class="stat-label">스캔됨</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ selectedItems.length }}</div>
            <div class="stat-label">선택됨</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ errorCount }}</div>
            <div class="stat-label">오류</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 필터 선택 모드 -->
    <div v-if="selectedMethod === 'filter'" class="filter-section">
      <div class="filter-controls">
        <div class="filter-row">
          <div class="filter-group">
            <label>상태</label>
            <select v-model="filterCriteria.status" @change="applyFilters" multiple class="filter-multiselect">
              <option value="created">생성됨</option>
              <option value="inbound_completed">입고 완료</option>
              <option value="ready_for_outbound">출고 준비</option>
              <option value="hold">보류</option>
            </select>
          </div>

          <div class="filter-group">
            <label>위치</label>
            <select v-model="filterCriteria.location" @change="applyFilters" multiple class="filter-multiselect">
              <option value="A1">A1 구역</option>
              <option value="A2">A2 구역</option>
              <option value="B1">B1 구역</option>
              <option value="B2">B2 구역</option>
              <option value="TEMP">임시보관</option>
            </select>
          </div>

          <div class="filter-group">
            <label>배송 타입</label>
            <select v-model="filterCriteria.shipType" @change="applyFilters" multiple class="filter-multiselect">
              <option value="sea">해상</option>
              <option value="air">항공</option>
            </select>
          </div>

          <div class="filter-group">
            <label>날짜 범위</label>
            <div class="date-range">
              <input
                v-model="filterCriteria.dateFrom"
                type="date"
                @change="applyFilters"
                class="filter-date"
              />
              <span>~</span>
              <input
                v-model="filterCriteria.dateTo"
                type="date"
                @change="applyFilters"
                class="filter-date"
              />
            </div>
          </div>
        </div>

        <div class="filter-actions">
          <button @click="selectAllFiltered" class="btn-select-all">
            모두 선택 ({{ filteredItems.length }}개)
          </button>
          <button @click="clearFilters" class="btn-clear-filters">
            필터 초기화
          </button>
        </div>
      </div>

      <!-- 필터링된 항목 미리보기 -->
      <div class="filtered-preview">
        <div class="preview-header">
          <h3>필터 결과 ({{ filteredItems.length }}개)</h3>
        </div>
        <div class="preview-list">
          <div
            v-for="item in filteredItems.slice(0, 10)"
            :key="item.id"
            class="preview-item"
            :class="{ selected: selectedItems.some(si => si.id === item.id) }"
            @click="toggleItemSelection(item)"
          >
            <div class="item-info">
              <span class="item-code">{{ item.labelCode }}</span>
              <span class="item-status" :class="item.status">{{ getStatusText(item.status) }}</span>
            </div>
            <div class="item-meta">
              <span class="item-location">{{ item.location || '미배정' }}</span>
              <span class="item-date">{{ formatDate(item.inboundDate) }}</span>
            </div>
          </div>
          <div v-if="filteredItems.length > 10" class="preview-more">
            외 {{ filteredItems.length - 10 }}개 더...
          </div>
        </div>
      </div>
    </div>

    <!-- 선택된 항목 목록 -->
    <div class="selected-items-section" v-if="selectedItems.length > 0">
      <div class="section-header">
        <h2>선택된 항목 ({{ selectedItems.length }}개)</h2>
        <div class="header-actions">
          <button @click="exportSelectedItems" class="btn-export">
            <i class="icon-download"></i>
            내보내기
          </button>
          <button @click="clearSelection" class="btn-clear">
            <i class="icon-x"></i>
            모두 제거
          </button>
        </div>
      </div>

      <div class="selected-items-grid">
        <div
          v-for="item in selectedItems"
          :key="item.id"
          class="selected-item-card"
        >
          <div class="item-header">
            <span class="item-code">{{ item.labelCode }}</span>
            <button @click="removeFromSelection(item)" class="btn-remove">
              <i class="icon-x"></i>
            </button>
          </div>
          <div class="item-details">
            <div class="detail-row">
              <span class="label">주문:</span>
              <span class="value">{{ item.orderCode }}</span>
            </div>
            <div class="detail-row">
              <span class="label">상품:</span>
              <span class="value">{{ item.itemName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">상태:</span>
              <span class="value status-badge" :class="item.status">
                {{ getStatusText(item.status) }}
              </span>
            </div>
            <div class="detail-row">
              <span class="label">위치:</span>
              <span class="value">{{ item.location || '미배정' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 처리 옵션 -->
    <div class="processing-options" v-if="selectedItems.length > 0">
      <h2>처리 옵션</h2>
      
      <div class="options-grid">
        <!-- 위치 설정 -->
        <div class="option-group" v-if="selectedMode === 'inbound'">
          <label>창고 위치</label>
          <div class="location-selector">
            <select v-model="processingOptions.targetLocation" class="location-select">
              <option value="">자동 배정</option>
              <option value="A1">A1 구역</option>
              <option value="A2">A2 구역</option>
              <option value="B1">B1 구역</option>
              <option value="B2">B2 구역</option>
              <option value="C1">C1 구역</option>
            </select>
            <button @click="showLocationMap" class="btn-map">
              <i class="icon-map"></i>
              위치 보기
            </button>
          </div>
        </div>

        <!-- 출고 옵션 -->
        <div class="option-group" v-if="selectedMode === 'outbound'">
          <label>출고 방법</label>
          <div class="radio-group">
            <label class="radio-option">
              <input type="radio" v-model="processingOptions.outboundMethod" value="normal" />
              <span>일반 출고</span>
            </label>
            <label class="radio-option">
              <input type="radio" v-model="processingOptions.outboundMethod" value="expedited" />
              <span>긴급 출고</span>
            </label>
          </div>
        </div>

        <!-- 보류 사유 -->
        <div class="option-group" v-if="selectedMode === 'hold'">
          <label>보류 사유</label>
          <select v-model="processingOptions.holdReason" class="hold-reason-select">
            <option value="">사유 선택</option>
            <option value="damaged">상품 손상</option>
            <option value="incomplete_address">주소 불완전</option>
            <option value="customs_issue">통관 문제</option>
            <option value="customer_request">고객 요청</option>
            <option value="other">기타</option>
          </select>
          <textarea
            v-if="processingOptions.holdReason === 'other'"
            v-model="processingOptions.holdNote"
            placeholder="보류 사유를 상세히 입력하세요..."
            class="hold-note-textarea"
          ></textarea>
        </div>

        <!-- 공통 메모 -->
        <div class="option-group">
          <label>처리 메모</label>
          <textarea
            v-model="processingOptions.note"
            placeholder="일괄 처리에 대한 메모를 입력하세요..."
            class="processing-note"
          ></textarea>
        </div>

        <!-- 알림 설정 -->
        <div class="option-group">
          <label class="checkbox-label">
            <input type="checkbox" v-model="processingOptions.sendNotification" />
            <span>처리 완료 시 알림 전송</span>
          </label>
          <label class="checkbox-label">
            <input type="checkbox" v-model="processingOptions.generateReport" />
            <span>처리 리포트 생성</span>
          </label>
        </div>
      </div>
    </div>

    <!-- 실행 버튼 -->
    <div class="execution-section" v-if="selectedItems.length > 0">
      <div class="execution-summary">
        <div class="summary-item">
          <span class="label">처리 모드:</span>
          <span class="value">{{ getModeTitle(selectedMode) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">선택 항목:</span>
          <span class="value">{{ selectedItems.length }}개</span>
        </div>
        <div class="summary-item" v-if="processingOptions.targetLocation">
          <span class="label">대상 위치:</span>
          <span class="value">{{ processingOptions.targetLocation }}</span>
        </div>
      </div>

      <div class="execution-actions">
        <button @click="previewProcessing" class="btn-preview">
          <i class="icon-eye"></i>
          미리보기
        </button>
        <button
          @click="executeProcessing"
          :disabled="processing || !canProcess"
          class="btn-execute"
        >
          <LoadingSpinner v-if="processing" size="small" />
          <i v-else class="icon-play"></i>
          {{ processing ? '처리 중...' : '일괄 처리 실행' }}
        </button>
      </div>
    </div>

    <!-- 진행 상태 -->
    <div class="progress-section" v-if="processing">
      <div class="progress-header">
        <h3>처리 진행 상황</h3>
        <span class="progress-stats">{{ processedCount }} / {{ selectedItems.length }}</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${progressPercentage}%` }"></div>
      </div>
      <div class="current-processing" v-if="currentProcessingItem">
        처리 중: {{ currentProcessingItem.labelCode }}
      </div>
    </div>

    <!-- 결과 모달 -->
    <ProcessingResultModal
      :show="showResultModal"
      :result="processingResult"
      @close="closeResultModal"
      @retry="retryFailed"
      @export="exportResult"
    />

    <!-- 미리보기 모달 -->
    <ProcessingPreviewModal
      :show="showPreviewModal"
      :items="selectedItems"
      :mode="selectedMode"
      :options="processingOptions"
      @close="closePreviewModal"
      @confirm="executeProcessing"
    />

    <!-- 프리셋 모달 -->
    <PresetModal
      :show="showPresetModal"
      :presets="savedPresets"
      @close="closePresetModal"
      @load="loadPreset"
      @save="savePreset"
      @delete="deletePreset"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '@/stores/warehouse'
import { useToast } from '@/composables/useToast'
import QRScanner from '../components/QRScanner.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
// Removed missing modal imports - using inline modals instead

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 반응형 데이터
const selectedMode = ref('inbound')
const selectedMethod = ref('scan')
const showCamera = ref(true)
const isScanning = ref(false)
const manualInput = ref('')
const processing = ref(false)
const processedCount = ref(0)
const scannedCount = ref(0)
const errorCount = ref(0)
const currentProcessingItem = ref<any>(null)

// 선택된 항목
const selectedItems = ref<any[]>([])

// 필터 조건
const filterCriteria = ref({
  status: [],
  location: [],
  shipType: [],
  dateFrom: '',
  dateTo: ''
})

// 처리 옵션
const processingOptions = ref({
  targetLocation: '',
  outboundMethod: 'normal',
  holdReason: '',
  holdNote: '',
  note: '',
  sendNotification: true,
  generateReport: false
})

// 모달 상태
const showResultModal = ref(false)
const showPreviewModal = ref(false)
const showPresetModal = ref(false)
const processingResult = ref<any>(null)

// 저장된 프리셋
const savedPresets = ref([
  {
    id: 1,
    name: '입고 처리 - A1 구역',
    mode: 'inbound',
    options: { targetLocation: 'A1', sendNotification: true }
  },
  {
    id: 2,
    name: '긴급 출고',
    mode: 'outbound',
    options: { outboundMethod: 'expedited', sendNotification: true }
  }
])

// 처리 모드
const processingModes = [
  {
    value: 'inbound',
    title: '입고 처리',
    description: '창고로 입고된 상품을 처리합니다',
    icon: 'icon-inbox',
    count: 25
  },
  {
    value: 'outbound',
    title: '출고 처리',
    description: '배송을 위한 출고 처리를 합니다',
    icon: 'icon-outbox',
    count: 38
  },
  {
    value: 'hold',
    title: '보류 처리',
    description: '문제가 있는 상품을 보류 처리합니다',
    icon: 'icon-pause',
    count: 3
  },
  {
    value: 'mixbox',
    title: '믹스박스',
    description: '여러 주문을 하나로 합칩니다',
    icon: 'icon-package',
    count: 12
  }
]

// 선택 방법
const selectionMethods = [
  {
    value: 'scan',
    label: '스캔',
    icon: 'icon-qr-code'
  },
  {
    value: 'filter',
    label: '필터',
    icon: 'icon-filter'
  }
]

// Mock 데이터
const availableItems = ref([
  {
    id: 1,
    labelCode: 'YCS240812001-L1',
    orderCode: 'YCS240812001',
    itemName: '아이폰 케이스',
    quantity: 2,
    status: 'created',
    location: '',
    inboundDate: new Date('2024-08-12T10:30:00'),
    shipType: 'sea',
    cbm: '0.001'
  },
  {
    id: 2,
    labelCode: 'YCS240812002-L1',
    orderCode: 'YCS240812002',
    itemName: '에어팟 충전기',
    quantity: 1,
    status: 'inbound_completed',
    location: 'A2-B1-C5',
    inboundDate: new Date('2024-08-12T09:15:00'),
    shipType: 'air',
    cbm: '0.0005'
  }
])

// 계산된 속성
const filteredItems = computed(() => {
  let items = [...availableItems.value]

  // 상태 필터
  if (filterCriteria.value.status.length > 0) {
    items = items.filter(item => filterCriteria.value.status.includes(item.status))
  }

  // 위치 필터
  if (filterCriteria.value.location.length > 0) {
    items = items.filter(item => 
      filterCriteria.value.location.some(loc => item.location.includes(loc))
    )
  }

  // 배송 타입 필터
  if (filterCriteria.value.shipType.length > 0) {
    items = items.filter(item => filterCriteria.value.shipType.includes(item.shipType))
  }

  // 날짜 필터
  if (filterCriteria.value.dateFrom) {
    const fromDate = new Date(filterCriteria.value.dateFrom)
    items = items.filter(item => new Date(item.inboundDate) >= fromDate)
  }

  if (filterCriteria.value.dateTo) {
    const toDate = new Date(filterCriteria.value.dateTo)
    toDate.setHours(23, 59, 59)
    items = items.filter(item => new Date(item.inboundDate) <= toDate)
  }

  return items
})

const progressPercentage = computed(() => {
  if (selectedItems.value.length === 0) return 0
  return (processedCount.value / selectedItems.value.length) * 100
})

const canProcess = computed(() => {
  if (selectedItems.value.length === 0) return false
  
  if (selectedMode.value === 'hold' && !processingOptions.value.holdReason) {
    return false
  }
  
  return true
})

// 컴포넌트 마운트
onMounted(() => {
  loadAvailableItems()
  startScanning()
})

// 메서드
const loadAvailableItems = async () => {
  // TODO: API 호출로 실제 데이터 가져오기
}

const startScanning = () => {
  if (showCamera.value) {
    isScanning.value = true
  }
}

const toggleCamera = () => {
  showCamera.value = !showCamera.value
  manualInput.value = ''
  
  if (showCamera.value) {
    startScanning()
  } else {
    isScanning.value = false
  }
}

const handleScan = (code: string) => {
  addItemByCode(code)
}

const handleManualScan = () => {
  if (!manualInput.value.trim()) return
  
  addItemByCode(manualInput.value.trim())
  manualInput.value = ''
}

const addItemByCode = (code: string) => {
  // 중복 체크
  if (selectedItems.value.some(item => item.labelCode === code || item.orderCode === code)) {
    showToast('이미 선택된 항목입니다', 'warning')
    return
  }

  // 해당 코드로 항목 찾기
  const item = availableItems.value.find(item => 
    item.labelCode === code || item.orderCode === code
  )

  if (item) {
    selectedItems.value.push({ ...item })
    scannedCount.value++
    showToast(`${item.labelCode} 추가됨`, 'success')
  } else {
    errorCount.value++
    showToast(`${code}를 찾을 수 없습니다`, 'error')
  }
}

const handleScanError = (error: any) => {
  console.error('QR Scanner error:', error)
  showToast('스캐너 오류가 발생했습니다', 'error')
}

const applyFilters = () => {
  // 필터 적용 시 자동으로 계산된 속성이 업데이트됨
}

const clearFilters = () => {
  filterCriteria.value = {
    status: [],
    location: [],
    shipType: [],
    dateFrom: '',
    dateTo: ''
  }
}

const selectAllFiltered = () => {
  filteredItems.value.forEach(item => {
    if (!selectedItems.value.some(si => si.id === item.id)) {
      selectedItems.value.push({ ...item })
    }
  })
  showToast(`${filteredItems.value.length}개 항목을 선택했습니다`, 'success')
}

const toggleItemSelection = (item: any) => {
  const index = selectedItems.value.findIndex(si => si.id === item.id)
  if (index === -1) {
    selectedItems.value.push({ ...item })
  } else {
    selectedItems.value.splice(index, 1)
  }
}

const removeFromSelection = (item: any) => {
  const index = selectedItems.value.findIndex(si => si.id === item.id)
  if (index !== -1) {
    selectedItems.value.splice(index, 1)
  }
}

const clearSelection = () => {
  selectedItems.value = []
  scannedCount.value = 0
  errorCount.value = 0
}

const previewProcessing = () => {
  showPreviewModal.value = true
}

const closePreviewModal = () => {
  showPreviewModal.value = false
}

const executeProcessing = async () => {
  if (!canProcess.value) return

  processing.value = true
  processedCount.value = 0
  currentProcessingItem.value = null

  try {
    const results = []

    for (let i = 0; i < selectedItems.value.length; i++) {
      const item = selectedItems.value[i]
      currentProcessingItem.value = item

      try {
        // TODO: 실제 API 호출
        await new Promise(resolve => setTimeout(resolve, 500))
        
        results.push({
          item,
          status: 'success',
          message: '처리 완료'
        })
        
        processedCount.value++
      } catch (error) {
        results.push({
          item,
          status: 'error',
          message: '처리 실패'
        })
      }
    }

    processingResult.value = {
      total: selectedItems.value.length,
      successful: results.filter(r => r.status === 'success').length,
      failed: results.filter(r => r.status === 'error').length,
      results
    }

    showResultModal.value = true
    
    if (processingOptions.value.generateReport) {
      generateReport()
    }
    
    if (processingOptions.value.sendNotification) {
      sendNotification()
    }

  } catch (error: any) {
    console.error('Batch processing error:', error)
    showToast('일괄 처리 중 오류가 발생했습니다', 'error')
  } finally {
    processing.value = false
    currentProcessingItem.value = null
  }
}

const closeResultModal = () => {
  showResultModal.value = false
  processingResult.value = null
  clearSelection()
}

const retryFailed = (failedItems: any[]) => {
  selectedItems.value = failedItems.map(item => item.item)
  closeResultModal()
  showToast(`${failedItems.length}개 실패 항목을 다시 선택했습니다`, 'info')
}

const exportResult = () => {
  showToast('처리 결과를 내보내는 중...', 'info')
  // TODO: 결과 내보내기 구현
}

const exportSelectedItems = () => {
  showToast(`선택된 ${selectedItems.value.length}개 항목을 내보내는 중...`, 'info')
  // TODO: 선택 항목 내보내기 구현
}

const generateReport = () => {
  // TODO: 리포트 생성 구현
  showToast('처리 리포트가 생성되었습니다', 'success')
}

const sendNotification = () => {
  // TODO: 알림 전송 구현
  showToast('처리 완료 알림이 전송되었습니다', 'success')
}

const showLocationMap = () => {
  showToast('위치 맵을 표시합니다', 'info')
  // TODO: 위치 맵 모달 구현
}

// 프리셋 관련
const loadPresets = () => {
  showPresetModal.value = true
}

const closePresetModal = () => {
  showPresetModal.value = false
}

const saveAsPreset = () => {
  const name = prompt('프리셋 이름을 입력하세요:')
  if (name) {
    const preset = {
      id: Date.now(),
      name,
      mode: selectedMode.value,
      options: { ...processingOptions.value }
    }
    savedPresets.value.push(preset)
    showToast('프리셋이 저장되었습니다', 'success')
  }
}

const loadPreset = (preset: any) => {
  selectedMode.value = preset.mode
  processingOptions.value = { ...preset.options }
  showToast(`프리셋 "${preset.name}"을 불러왔습니다`, 'success')
  closePresetModal()
}

const savePreset = (preset: any) => {
  const index = savedPresets.value.findIndex(p => p.id === preset.id)
  if (index !== -1) {
    savedPresets.value[index] = preset
    showToast('프리셋이 업데이트되었습니다', 'success')
  }
}

const deletePreset = (presetId: number) => {
  const index = savedPresets.value.findIndex(p => p.id === presetId)
  if (index !== -1) {
    savedPresets.value.splice(index, 1)
    showToast('프리셋이 삭제되었습니다', 'info')
  }
}

// 유틸리티 함수
const getModeTitle = (mode: string): string => {
  const modeObj = processingModes.find(m => m.value === mode)
  return modeObj ? modeObj.title : mode
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    created: '생성됨',
    inbound_completed: '입고완료',
    ready_for_outbound: '출고준비',
    outbound_completed: '출고완료',
    hold: '보류',
    mixbox: '믹스박스'
  }
  return statusMap[status] || status
}

const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<style scoped>
.batch-process-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.batch-description {
  font-size: 14px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-preset,
.btn-save-preset {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-preset:hover,
.btn-save-preset:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-save-preset:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mode-selection,
.selection-methods,
.selected-items-section,
.processing-options {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.mode-selection h2,
.selection-methods h2,
.selected-items-section h2,
.processing-options h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.mode-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.mode-card:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.mode-card.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.mode-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.mode-icon.inbound {
  background: #3b82f6;
}

.mode-icon.outbound {
  background: #10b981;
}

.mode-icon.hold {
  background: #f59e0b;
}

.mode-icon.mixbox {
  background: #8b5cf6;
}

.mode-content {
  flex: 1;
}

.mode-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.mode-content p {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.mode-stats .stat {
  font-size: 12px;
  color: #9ca3af;
}

.method-tabs {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
  width: fit-content;
}

.method-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  color: #6b7280;
}

.method-tab.active {
  background: white;
  color: #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.scan-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.scan-container {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: 30px;
}

.scanner-area {
  min-height: 400px;
}

.scanner-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.scanner-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.btn-toggle-camera {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-toggle-camera:hover {
  background: #f9fafb;
}

.camera-scanner {
  position: relative;
  height: 300px;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.scan-overlay {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
}

.scan-status.scanning {
  background: rgba(16, 185, 129, 0.9);
}

.manual-input {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.input-group {
  display: flex;
  gap: 12px;
  max-width: 400px;
  width: 100%;
}

.manual-input-field {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 16px;
}

.manual-input-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-manual-add {
  padding: 12px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.btn-manual-add:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.scan-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.filter-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-controls {
  margin-bottom: 20px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
}

.filter-multiselect {
  min-height: 80px;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-date {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.btn-select-all,
.btn-clear-filters {
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-select-all {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-clear-filters {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.filtered-preview {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.preview-header {
  background: #f9fafb;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.preview-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.preview-list {
  max-height: 300px;
  overflow-y: auto;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.preview-item:hover {
  background: #f8fafc;
}

.preview-item.selected {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-code {
  font-weight: 500;
  color: #1f2937;
}

.item-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

.item-location,
.item-date {
  font-size: 12px;
  color: #6b7280;
}

.preview-more {
  padding: 12px 20px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header .header-actions {
  display: flex;
  gap: 12px;
}

.btn-export,
.btn-clear {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-export {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-clear {
  background: white;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.selected-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.selected-item-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-header .item-code {
  font-weight: 600;
  color: #1f2937;
}

.btn-remove {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  background: #fef2f2;
  color: #dc2626;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-remove:hover {
  background: #fee2e2;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.detail-row .label {
  color: #6b7280;
}

.detail-row .value {
  color: #1f2937;
  font-weight: 500;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.created {
  background: #f3f4f6;
  color: #374151;
}

.status-badge.inbound_completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.ready_for_outbound {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.hold {
  background: #fef3c7;
  color: #92400e;
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.location-selector {
  display: flex;
  gap: 8px;
}

.location-select,
.hold-reason-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.btn-map {
  padding: 8px 12px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
}

.btn-map:hover {
  background: #f9fafb;
  color: #374151;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.radio-option input[type="radio"] {
  margin: 0;
}

.hold-note-textarea,
.processing-note {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
}

.checkbox-label input[type="checkbox"] {
  margin: 0;
}

.execution-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.execution-summary {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-item .label {
  font-size: 12px;
  color: #6b7280;
}

.summary-item .value {
  font-weight: 600;
  color: #1f2937;
}

.execution-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.btn-preview,
.btn-execute {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-preview {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-preview:hover {
  background: #f9fafb;
}

.btn-execute {
  background: #10b981;
  color: white;
  border: 1px solid #10b981;
  font-size: 16px;
}

.btn-execute:hover:not(:disabled) {
  background: #059669;
}

.btn-execute:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.progress-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.progress-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.progress-stats {
  font-size: 14px;
  color: #6b7280;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: #10b981;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.current-processing {
  font-size: 14px;
  color: #6b7280;
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .batch-process-view {
    padding: 16px;
  }

  .batch-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .mode-grid {
    grid-template-columns: 1fr;
  }

  .scan-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .selected-items-grid {
    grid-template-columns: 1fr;
  }

  .options-grid {
    grid-template-columns: 1fr;
  }

  .execution-summary {
    flex-direction: column;
    gap: 12px;
  }

  .execution-actions {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .camera-scanner {
    height: 250px;
  }

  .method-tabs {
    width: 100%;
  }

  .method-tab {
    flex: 1;
    justify-content: center;
  }
}
</style>