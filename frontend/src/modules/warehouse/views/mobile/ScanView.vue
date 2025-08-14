<template>
  <div class="mobile-scan-view">
    <!-- 모바일 헤더 -->
    <div class="mobile-header">
      <button @click="goBack" class="back-btn">
        <i class="icon-arrow-left"></i>
      </button>
      <h1>모바일 스캔</h1>
      <button @click="showSettings = true" class="settings-btn">
        <i class="icon-settings"></i>
      </button>
    </div>

    <!-- 스캔 모드 선택 -->
    <div class="scan-mode-bar">
      <div class="mode-selector">
        <button
          v-for="mode in scanModes"
          :key="mode.value"
          class="mode-btn"
          :class="{ active: selectedScanMode === mode.value }"
          @click="selectedScanMode = mode.value"
        >
          <i :class="mode.icon"></i>
          <span>{{ mode.label }}</span>
        </button>
      </div>
    </div>

    <!-- 배치 모드 토글 -->
    <div class="batch-toggle" v-if="!isScanning">
      <label class="toggle-switch">
        <input type="checkbox" v-model="batchMode" />
        <span class="toggle-slider"></span>
        <span class="toggle-label">일괄 모드</span>
      </label>
      <div class="batch-count" v-if="batchMode && scannedItems.length > 0">
        {{ scannedItems.length }}개 선택됨
      </div>
    </div>

    <!-- 메인 스캐너 영역 -->
    <div class="scanner-container">
      <div class="scanner-area" :class="{ 'full-screen': isFullScreen }">
        <!-- 카메라 뷰 -->
        <div v-if="showCamera && cameraActive" class="camera-view">
          <QRScanner
            ref="qrScanner"
            @scan="handleScan"
            @error="handleScanError"
            :active="isScanning"
            :mobile-optimized="true"
          />
          
          <!-- 스캔 오버레이 -->
          <div class="scan-overlay">
            <!-- 스캔 타겟 -->
            <div class="scan-target">
              <div class="target-corners">
                <div class="corner top-left"></div>
                <div class="corner top-right"></div>
                <div class="corner bottom-left"></div>
                <div class="corner bottom-right"></div>
              </div>
              <div class="scan-line" :class="{ animate: isScanning }"></div>
            </div>
            
            <!-- 스캔 지침 -->
            <div class="scan-instructions">
              <p>{{ getScanInstructions() }}</p>
            </div>
            
            <!-- 스캔 상태 -->
            <div class="scan-status" :class="scanStatus.type">
              <i :class="getScanStatusIcon()"></i>
              <span>{{ scanStatus.message }}</span>
            </div>
          </div>

          <!-- 카메라 컨트롤 -->
          <div class="camera-controls">
            <button @click="toggleFlashlight" class="control-btn" :class="{ active: flashlightOn }">
              <i class="icon-zap"></i>
            </button>
            <button @click="switchCamera" class="control-btn">
              <i class="icon-refresh-cw"></i>
            </button>
            <button @click="toggleFullScreen" class="control-btn" :class="{ active: isFullScreen }">
              <i class="icon-maximize"></i>
            </button>
          </div>
        </div>

        <!-- 수동 입력 모드 -->
        <div v-else class="manual-input-area">
          <div class="input-container">
            <div class="input-group">
              <i class="icon-edit"></i>
              <input
                v-model="manualInput"
                type="text"
                :placeholder="getManualInputPlaceholder()"
                class="manual-input"
                @keyup.enter="handleManualScan"
                ref="manualInputRef"
              />
              <button 
                @click="clearManualInput" 
                v-if="manualInput"
                class="clear-btn"
              >
                <i class="icon-x"></i>
              </button>
            </div>
            <button 
              @click="handleManualScan" 
              :disabled="!manualInput.trim()"
              class="add-btn"
            >
              추가
            </button>
          </div>

          <!-- 최근 스캔된 코드 -->
          <div class="recent-codes" v-if="recentCodes.length > 0">
            <h3>최근 코드</h3>
            <div class="codes-list">
              <button
                v-for="code in recentCodes.slice(0, 5)"
                :key="code.id"
                class="code-chip"
                @click="selectRecentCode(code)"
              >
                {{ code.value }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 입력 모드 전환 -->
      <div class="input-toggle">
        <button @click="toggleInputMode" class="toggle-mode-btn">
          <i :class="showCamera ? 'icon-keyboard' : 'icon-camera'"></i>
          <span>{{ showCamera ? '수동 입력' : '카메라 스캔' }}</span>
        </button>
      </div>
    </div>

    <!-- 스캔 결과 영역 -->
    <div class="results-area" v-if="!isFullScreen">
      <!-- 단일 모드 - 마지막 스캔 결과 -->
      <div v-if="!batchMode && lastScanResult" class="single-result">
        <div class="result-header">
          <h3>스캔 결과</h3>
          <button @click="clearLastResult" class="clear-result-btn">
            <i class="icon-x"></i>
          </button>
        </div>
        
        <div class="result-card" :class="lastScanResult.success ? 'success' : 'error'">
          <div class="result-icon">
            <i :class="lastScanResult.success ? 'icon-check' : 'icon-x'"></i>
          </div>
          <div class="result-content">
            <div class="result-code">{{ lastScanResult.labelCode }}</div>
            <div class="result-details">
              <div v-if="lastScanResult.success" class="success-details">
                <p><strong>주문:</strong> {{ lastScanResult.orderCode }}</p>
                <p><strong>상품:</strong> {{ lastScanResult.itemName }}</p>
                <p><strong>상태:</strong> {{ lastScanResult.status }}</p>
              </div>
              <div v-else class="error-details">
                <p>{{ lastScanResult.error }}</p>
              </div>
            </div>
          </div>
          <div class="result-actions" v-if="lastScanResult.success">
            <button @click="viewDetails(lastScanResult)" class="action-btn">
              <i class="icon-eye"></i>
            </button>
            <button @click="printLabel(lastScanResult)" class="action-btn">
              <i class="icon-printer"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 일괄 모드 - 스캔된 아이템 목록 -->
      <div v-if="batchMode" class="batch-results">
        <div class="batch-header">
          <div class="batch-info">
            <h3>일괄 스캔 ({{ scannedItems.length }}개)</h3>
            <span class="scan-mode-indicator">{{ getScanModeText(selectedScanMode) }}</span>
          </div>
          <div class="batch-actions">
            <button 
              @click="clearBatchItems" 
              v-if="scannedItems.length > 0"
              class="clear-batch-btn"
            >
              <i class="icon-trash"></i>
            </button>
          </div>
        </div>

        <div v-if="scannedItems.length === 0" class="empty-batch">
          <div class="empty-icon">
            <i class="icon-scan"></i>
          </div>
          <p>스캔된 항목이 없습니다</p>
          <p class="empty-hint">QR 코드나 바코드를 스캔하세요</p>
        </div>

        <div v-else class="batch-list">
          <div
            v-for="(item, index) in scannedItems"
            :key="item.id"
            class="batch-item"
            :class="{ 'recent': index === 0 }"
          >
            <div class="item-status" :class="item.success ? 'success' : 'error'">
              <i :class="item.success ? 'icon-check' : 'icon-x'"></i>
            </div>
            <div class="item-content">
              <div class="item-code">{{ item.labelCode }}</div>
              <div class="item-info">
                <span v-if="item.success">{{ item.orderCode }}</span>
                <span v-else class="error-text">{{ item.error }}</span>
              </div>
              <div class="item-time">{{ formatTime(item.timestamp) }}</div>
            </div>
            <button @click="removeBatchItem(index)" class="remove-btn">
              <i class="icon-x"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 액션 바 -->
    <div class="action-bar" v-if="!isFullScreen">
      <!-- 단일 모드 액션 -->
      <div v-if="!batchMode" class="single-actions">
        <button @click="openHistory" class="action-bar-btn">
          <i class="icon-history"></i>
          <span>이력</span>
        </button>
        <button @click="openSettings" class="action-bar-btn">
          <i class="icon-settings"></i>
          <span>설정</span>
        </button>
      </div>

      <!-- 일괄 모드 액션 -->
      <div v-if="batchMode" class="batch-actions">
        <div class="batch-summary">
          <span class="total-count">{{ scannedItems.length }}개</span>
          <span class="success-count">성공 {{ successCount }}개</span>
          <span class="error-count" v-if="errorCount > 0">실패 {{ errorCount }}개</span>
        </div>
        <button 
          @click="processBatch" 
          :disabled="scannedItems.length === 0 || processing"
          class="process-btn"
        >
          <LoadingSpinner v-if="processing" size="small" color="white" />
          <i v-else class="icon-play"></i>
          <span>{{ processing ? '처리 중...' : '일괄 처리' }}</span>
        </button>
      </div>
    </div>

    <!-- 스캔 피드백 -->
    <div class="scan-feedback" v-if="showFeedback">
      <div class="feedback-content" :class="feedbackType">
        <i :class="feedbackType === 'success' ? 'icon-check' : 'icon-x'"></i>
        <span>{{ feedbackMessage }}</span>
      </div>
    </div>

    <!-- 설정 모달 -->
    <div v-if="showSettings" class="settings-modal">
      <div class="modal-backdrop" @click="showSettings = false"></div>
      <div class="modal-content">
        <div class="modal-header">
          <h3>스캔 설정</h3>
          <button @click="showSettings = false" class="close-btn">
            <i class="icon-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="setting-group">
            <label class="setting-label">
              <input type="checkbox" v-model="settings.vibration" />
              <span>스캔 시 진동</span>
            </label>
          </div>
          <div class="setting-group">
            <label class="setting-label">
              <input type="checkbox" v-model="settings.sound" />
              <span>스캔 시 소리</span>
            </label>
          </div>
          <div class="setting-group">
            <label class="setting-label">
              <input type="checkbox" v-model="settings.autoFocus" />
              <span>자동 초점</span>
            </label>
          </div>
          <div class="setting-group">
            <label class="setting-label">연속 스캔 간격</label>
            <select v-model="settings.scanDelay" class="setting-select">
              <option :value="500">0.5초</option>
              <option :value="1000">1초</option>
              <option :value="2000">2초</option>
              <option :value="3000">3초</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="resetSettings" class="reset-btn">초기화</button>
          <button @click="saveSettings" class="save-btn">저장</button>
        </div>
      </div>
    </div>

    <!-- 스캔 상세 모달 -->
    <ScanDetailModal
      :show="showDetailModal"
      :scan-data="selectedScanData"
      @close="closeDetailModal"
      @action="handleScanAction"
    />

    <!-- 이력 모달 -->
    <ScanHistoryModal
      :show="showHistoryModal"
      :history="scanHistory"
      @close="closeHistoryModal"
      @rescan="rescanFromHistory"
    />

    <!-- 프로세싱 모달 -->
    <BatchProcessModal
      :show="showProcessModal"
      :items="scannedItems"
      :mode="selectedScanMode"
      @close="closeProcessModal"
      @complete="handleBatchComplete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '../../stores/warehouseStore'
import { useToast } from '@/composables/useToast'
import QRScanner from '../../components/QRScanner.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import ScanDetailModal from '../../components/ScanDetailModal.vue'
import ScanHistoryModal from '../../components/ScanHistoryModal.vue'
import BatchProcessModal from '../../components/BatchProcessModal.vue'

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// Refs
const qrScanner = ref<InstanceType<typeof QRScanner> | null>(null)
const manualInputRef = ref<HTMLInputElement | null>(null)

// 반응형 데이터
const selectedScanMode = ref('inbound')
const batchMode = ref(false)
const showCamera = ref(true)
const cameraActive = ref(false)
const isScanning = ref(false)
const isFullScreen = ref(false)
const flashlightOn = ref(false)
const processing = ref(false)
const manualInput = ref('')

// 모달 상태
const showSettings = ref(false)
const showDetailModal = ref(false)
const showHistoryModal = ref(false)
const showProcessModal = ref(false)
const selectedScanData = ref<any>(null)

// 피드백 상태
const showFeedbackModal = ref(false)
const feedbackType = ref<'success' | 'error'>('success')
const feedbackMessage = ref('')

// 스캔 상태
const scanStatus = ref({
  type: 'ready',
  message: '스캔 준비됨'
})

// 데이터
const scannedItems = ref<any[]>([])
const lastScanResult = ref<any>(null)
const recentCodes = ref<any[]>([])
const scanHistory = ref<any[]>([])

// 설정
const settings = ref({
  vibration: true,
  sound: true,
  autoFocus: true,
  scanDelay: 1000
})

// 스캔 모드 목록
const scanModes = [
  { value: 'inbound', label: '입고', icon: 'icon-inbox' },
  { value: 'outbound', label: '출고', icon: 'icon-outbox' },
  { value: 'hold', label: '보류', icon: 'icon-pause' },
  { value: 'mixbox', label: '믹스박스', icon: 'icon-package' }
]

// 계산된 속성
const successCount = computed(() => 
  scannedItems.value.filter(item => item.success).length
)

const errorCount = computed(() => 
  scannedItems.value.filter(item => !item.success).length
)

// 컴포넌트 마운트
onMounted(async () => {
  loadSettings()
  await requestCameraPermissions()
  startCamera()
  loadRecentCodes()
  loadScanHistory()
})

// 컴포넌트 언마운트
onUnmounted(() => {
  stopCamera()
})

// 메서드
const requestCameraPermissions = async () => {
  try {
    await navigator.mediaDevices.getUserMedia({ video: true })
    cameraActive.value = true
  } catch (error) {
    console.error('Camera permission denied:', error)
    showToast('카메라 권한이 필요합니다', 'error')
    showCamera.value = false
  }
}

const startCamera = () => {
  if (showCamera.value && cameraActive.value) {
    isScanning.value = true
    scanStatus.value = {
      type: 'scanning',
      message: '스캔 중...'
    }
  }
}

const stopCamera = () => {
  isScanning.value = false
  scanStatus.value = {
    type: 'ready',
    message: '스캔 준비됨'
  }
}

const toggleInputMode = async () => {
  showCamera.value = !showCamera.value
  manualInput.value = ''
  
  if (showCamera.value) {
    if (cameraActive.value) {
      startCamera()
    }
  } else {
    stopCamera()
    // 수동 입력 모드로 전환 시 키보드 포커스
    await nextTick()
    manualInputRef.value?.focus()
  }
}

const toggleFlashlight = async () => {
  try {
    if (qrScanner.value) {
      await qrScanner.value.toggleFlashlight()
      flashlightOn.value = !flashlightOn.value
    }
  } catch (error) {
    console.error('Flashlight toggle failed:', error)
    showToast('플래시 제어에 실패했습니다', 'error')
  }
}

const switchCamera = async () => {
  try {
    if (qrScanner.value) {
      await qrScanner.value.switchCamera()
      displayFeedback('카메라를 전환했습니다', 'success')
    }
  } catch (error) {
    console.error('Camera switch failed:', error)
    showToast('카메라 전환에 실패했습니다', 'error')
  }
}

const toggleFullScreen = () => {
  isFullScreen.value = !isFullScreen.value
}

const handleScan = (code: string) => {
  processScan(code, 'camera')
}

const handleManualScan = () => {
  if (!manualInput.value.trim()) return
  
  processScan(manualInput.value.trim(), 'manual')
  manualInput.value = ''
}

const processScan = async (code: string, source: 'camera' | 'manual') => {
  // 중복 체크 (일괄 모드)
  if (batchMode.value && scannedItems.value.some(item => item.labelCode === code)) {
    displayFeedback('이미 스캔된 코드입니다', 'error')
    return
  }

  // 스캔 딜레이 적용 (연속 스캔 방지)
  if (source === 'camera' && Date.now() - lastScanTime.value < settings.value.scanDelay) {
    return
  }

  try {
    scanStatus.value = {
      type: 'processing',
      message: '처리 중...'
    }

    // Mock API 호출
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const scanResult = {
      id: Date.now(),
      labelCode: code,
      orderCode: `ORDER-${Math.floor(Math.random() * 10000)}`,
      itemName: '샘플 상품',
      status: 'inbound_completed',
      success: Math.random() > 0.2, // 80% 성공률
      error: Math.random() > 0.2 ? null : '항목을 찾을 수 없습니다',
      timestamp: new Date(),
      source
    }

    if (batchMode.value) {
      // 일괄 모드: 목록에 추가
      scannedItems.value.unshift(scanResult)
      displayFeedback(
        scanResult.success ? '항목이 추가되었습니다' : scanResult.error,
        scanResult.success ? 'success' : 'error'
      )
    } else {
      // 단일 모드: 결과 표시
      lastScanResult.value = scanResult
      if (scanResult.success) {
        displayFeedback('스캔 완료', 'success')
      } else {
        displayFeedback(scanResult.error, 'error')
      }
    }

    // 이력에 추가
    addToHistory(scanResult)
    
    // 최근 코드에 추가
    addToRecentCodes(code)
    
    // 피드백 (진동, 소리)
    provideFeedback(scanResult.success)

    scanStatus.value = {
      type: 'ready',
      message: '스캔 준비됨'
    }

    lastScanTime.value = Date.now()

  } catch (error: any) {
    console.error('Scan processing error:', error)
    displayFeedback('스캔 처리 중 오류가 발생했습니다', 'error')
    
    scanStatus.value = {
      type: 'error',
      message: '오류 발생'
    }
  }
}

const lastScanTime = ref(0)

const handleScanError = (error: any) => {
  console.error('QR Scanner error:', error)
  scanStatus.value = {
    type: 'error',
    message: '스캔 오류'
  }
  displayFeedback('스캐너 오류가 발생했습니다', 'error')
}

const clearManualInput = () => {
  manualInput.value = ''
}

const selectRecentCode = (code: any) => {
  manualInput.value = code.value
}

const clearLastResult = () => {
  lastScanResult.value = null
}

const removeBatchItem = (index: number) => {
  scannedItems.value.splice(index, 1)
}

const clearBatchItems = () => {
  if (confirm('모든 스캔 항목을 삭제하시겠습니까?')) {
    scannedItems.value = []
    showToast('모든 항목이 삭제되었습니다', 'info')
  }
}

const processBatch = async () => {
  if (scannedItems.value.length === 0) return

  processing.value = true
  showProcessModal.value = true
}

const handleBatchComplete = (result: any) => {
  processing.value = false
  showProcessModal.value = false
  
  showToast(
    `일괄 처리 완료: ${result.successful}개 성공, ${result.failed}개 실패`,
    result.failed > 0 ? 'warning' : 'success'
  )
  
  // 성공한 항목들 제거
  scannedItems.value = scannedItems.value.filter(item => !item.success)
}

const closeProcessModal = () => {
  showProcessModal.value = false
  processing.value = false
}

// 피드백 및 유틸리티
const displayFeedback = (message: string, type: 'success' | 'error') => {
  feedbackMessage.value = message
  feedbackType.value = type
  showFeedback.value = true
  
  setTimeout(() => {
    showFeedback.value = false
  }, 2000)
}

const provideFeedback = (success: boolean) => {
  // 진동 피드백
  if (settings.value.vibration && 'vibrate' in navigator) {
    navigator.vibrate(success ? [100] : [200, 100, 200])
  }
  
  // 소리 피드백
  if (settings.value.sound) {
    playFeedbackSound(success)
  }
}

const playFeedbackSound = (success: boolean) => {
  const context = new (window.AudioContext || (window as any).webkitAudioContext)()
  const oscillator = context.createOscillator()
  const gainNode = context.createGain()
  
  oscillator.connect(gainNode)
  gainNode.connect(context.destination)
  
  oscillator.frequency.setValueAtTime(success ? 800 : 400, context.currentTime)
  oscillator.type = 'sine'
  
  gainNode.gain.setValueAtTime(0.3, context.currentTime)
  gainNode.gain.exponentialRampToValueAtTime(0.01, context.currentTime + 0.1)
  
  oscillator.start(context.currentTime)
  oscillator.stop(context.currentTime + 0.1)
}

const addToHistory = (scanResult: any) => {
  scanHistory.value.unshift(scanResult)
  if (scanHistory.value.length > 100) {
    scanHistory.value = scanHistory.value.slice(0, 100)
  }
  saveScanHistory()
}

const addToRecentCodes = (code: string) => {
  const existing = recentCodes.value.find(item => item.value === code)
  if (existing) {
    existing.lastUsed = new Date()
  } else {
    recentCodes.value.unshift({
      id: Date.now(),
      value: code,
      lastUsed: new Date()
    })
  }
  
  if (recentCodes.value.length > 20) {
    recentCodes.value = recentCodes.value.slice(0, 20)
  }
  
  saveRecentCodes()
}

// 모달 및 네비게이션
const viewDetails = (scanData: any) => {
  selectedScanData.value = scanData
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedScanData.value = null
}

const handleScanAction = (action: string, data?: any) => {
  console.log('Scan action:', action, data)
  closeDetailModal()
}

const openHistory = () => {
  showHistoryModal.value = true
}

const closeHistoryModal = () => {
  showHistoryModal.value = false
}

const rescanFromHistory = (historyItem: any) => {
  processScan(historyItem.labelCode, 'manual')
  closeHistoryModal()
}

const openSettings = () => {
  showSettings.value = true
}

const printLabel = (scanData: any) => {
  showToast(`${scanData.labelCode} 라벨을 출력합니다`, 'info')
}

const goBack = () => {
  router.back()
}

// 설정 관리
const loadSettings = () => {
  const saved = localStorage.getItem('mobile_scan_settings')
  if (saved) {
    try {
      settings.value = { ...settings.value, ...JSON.parse(saved) }
    } catch (error) {
      console.error('Failed to load settings:', error)
    }
  }
}

const saveSettings = () => {
  try {
    localStorage.setItem('mobile_scan_settings', JSON.stringify(settings.value))
    showToast('설정이 저장되었습니다', 'success')
    showSettings.value = false
  } catch (error) {
    console.error('Failed to save settings:', error)
    showToast('설정 저장에 실패했습니다', 'error')
  }
}

const resetSettings = () => {
  settings.value = {
    vibration: true,
    sound: true,
    autoFocus: true,
    scanDelay: 1000
  }
  showToast('설정이 초기화되었습니다', 'info')
}

const loadRecentCodes = () => {
  const saved = localStorage.getItem('mobile_recent_codes')
  if (saved) {
    try {
      recentCodes.value = JSON.parse(saved).map((item: any) => ({
        ...item,
        lastUsed: new Date(item.lastUsed)
      }))
    } catch (error) {
      console.error('Failed to load recent codes:', error)
    }
  }
}

const saveRecentCodes = () => {
  try {
    localStorage.setItem('mobile_recent_codes', JSON.stringify(recentCodes.value))
  } catch (error) {
    console.error('Failed to save recent codes:', error)
  }
}

const loadScanHistory = () => {
  const saved = localStorage.getItem('mobile_scan_history')
  if (saved) {
    try {
      scanHistory.value = JSON.parse(saved).map((item: any) => ({
        ...item,
        timestamp: new Date(item.timestamp)
      }))
    } catch (error) {
      console.error('Failed to load scan history:', error)
    }
  }
}

const saveScanHistory = () => {
  try {
    localStorage.setItem('mobile_scan_history', JSON.stringify(scanHistory.value))
  } catch (error) {
    console.error('Failed to save scan history:', error)
  }
}

// 유틸리티 함수
const getScanInstructions = (): string => {
  const modeInstructions = {
    inbound: 'QR 코드를 스캔하여 입고 처리하세요',
    outbound: '출고할 상품의 QR 코드를 스캔하세요',
    hold: '보류 처리할 상품을 스캔하세요',
    mixbox: '믹스박스에 포함할 상품을 스캔하세요'
  }
  return modeInstructions[selectedScanMode.value as keyof typeof modeInstructions] || 'QR 코드를 스캔하세요'
}

const getScanModeText = (mode: string): string => {
  const modeMap = {
    inbound: '입고',
    outbound: '출고',
    hold: '보류',
    mixbox: '믹스박스'
  }
  return modeMap[mode as keyof typeof modeMap] || mode
}

const getScanStatusIcon = (): string => {
  const iconMap = {
    ready: 'icon-camera',
    scanning: 'icon-search',
    processing: 'icon-loader',
    error: 'icon-alert-circle'
  }
  return iconMap[scanStatus.value.type as keyof typeof iconMap] || 'icon-circle'
}

const getManualInputPlaceholder = (): string => {
  return `${getScanModeText(selectedScanMode.value)} 코드를 입력하세요`
}

const formatTime = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}
</script>

<style scoped>
.mobile-scan-view {
  height: 100vh;
  background: #000;
  color: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
}

.mobile-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0;
}

.back-btn,
.settings-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.15s ease;
}

.back-btn:hover,
.settings-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.scan-mode-bar {
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
}

.mode-selector {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.mode-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
  min-width: 70px;
}

.mode-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
}

.mode-btn i {
  font-size: 20px;
}

.mode-btn span {
  font-size: 12px;
  font-weight: 500;
}

.batch-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.toggle-switch {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.toggle-switch input {
  display: none;
}

.toggle-slider {
  width: 48px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  position: relative;
  transition: all 0.3s ease;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: all 0.3s ease;
}

.toggle-switch input:checked + .toggle-slider {
  background: #3b82f6;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(24px);
}

.toggle-label {
  font-size: 14px;
  font-weight: 500;
}

.batch-count {
  font-size: 12px;
  color: #10b981;
  font-weight: 500;
}

.scanner-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.scanner-area {
  height: 100%;
  position: relative;
  background: #000;
}

.scanner-area.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
}

.camera-view {
  height: 100%;
  position: relative;
}

.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  z-index: 2;
}

.scan-target {
  width: 250px;
  height: 250px;
  position: relative;
  margin-bottom: 40px;
}

.target-corners {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 3px solid #10b981;
}

.corner.top-left {
  top: 0;
  left: 0;
  border-right: none;
  border-bottom: none;
}

.corner.top-right {
  top: 0;
  right: 0;
  border-left: none;
  border-bottom: none;
}

.corner.bottom-left {
  bottom: 0;
  left: 0;
  border-right: none;
  border-top: none;
}

.corner.bottom-right {
  bottom: 0;
  right: 0;
  border-left: none;
  border-top: none;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #10b981;
  box-shadow: 0 0 10px #10b981;
}

.scan-line.animate {
  animation: scanLine 2s linear infinite;
}

@keyframes scanLine {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(250px); }
}

.scan-instructions {
  text-align: center;
  margin-bottom: 20px;
}

.scan-instructions p {
  font-size: 16px;
  color: white;
  margin: 0;
}

.scan-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.scan-status.ready {
  color: #10b981;
}

.scan-status.scanning {
  color: #3b82f6;
}

.scan-status.processing {
  color: #f59e0b;
}

.scan-status.error {
  color: #ef4444;
}

.camera-controls {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 16px;
  z-index: 3;
}

.control-btn {
  width: 50px;
  height: 50px;
  border: none;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 25px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.15s ease;
}

.control-btn:hover {
  background: rgba(0, 0, 0, 0.9);
}

.control-btn.active {
  background: #3b82f6;
}

.manual-input-area {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  background: #1f2937;
}

.input-container {
  width: 100%;
  max-width: 400px;
  margin-bottom: 40px;
}

.input-group {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 4px 4px 4px 16px;
  margin-bottom: 16px;
}

.input-group i {
  color: #6b7280;
  margin-right: 12px;
}

.manual-input {
  flex: 1;
  border: none;
  background: none;
  font-size: 16px;
  color: #1f2937;
  padding: 12px 0;
}

.manual-input:focus {
  outline: none;
}

.clear-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-btn {
  width: 100%;
  padding: 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.add-btn:hover:not(:disabled) {
  background: #2563eb;
}

.add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.recent-codes {
  width: 100%;
  max-width: 400px;
}

.recent-codes h3 {
  font-size: 16px;
  color: white;
  margin-bottom: 16px;
  text-align: center;
}

.codes-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.code-chip {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.15s ease;
}

.code-chip:hover {
  background: rgba(255, 255, 255, 0.2);
}

.input-toggle {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 4;
}

.toggle-mode-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

.results-area {
  background: #1f2937;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  max-height: 40vh;
  overflow-y: auto;
}

.single-result {
  padding: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 {
  font-size: 16px;
  color: white;
  margin: 0;
}

.clear-result-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid transparent;
}

.result-card.success {
  background: rgba(16, 185, 129, 0.1);
  border-left-color: #10b981;
}

.result-card.error {
  background: rgba(239, 68, 68, 0.1);
  border-left-color: #ef4444;
}

.result-icon {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.result-card.success .result-icon {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.result-card.error .result-icon {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.result-content {
  flex: 1;
}

.result-code {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.result-details {
  font-size: 14px;
  color: #9ca3af;
}

.success-details p {
  margin: 2px 0;
}

.error-details {
  color: #fca5a5;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.15s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.batch-results {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.batch-info h3 {
  font-size: 16px;
  color: white;
  margin: 0 0 4px 0;
}

.scan-mode-indicator {
  font-size: 12px;
  color: #3b82f6;
  font-weight: 500;
}

.clear-batch-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-batch {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #6b7280;
  flex: 1;
}

.empty-icon i {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-batch p {
  margin: 8px 0;
  text-align: center;
}

.empty-hint {
  font-size: 14px;
}

.batch-list {
  flex: 1;
  overflow-y: auto;
}

.batch-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  animation: slideIn 0.3s ease;
}

.batch-item.recent {
  background: rgba(59, 130, 246, 0.1);
}

@keyframes slideIn {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.item-status {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.item-status.success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.item-status.error {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.item-content {
  flex: 1;
}

.item-code {
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.item-info {
  font-size: 12px;
  color: #9ca3af;
}

.error-text {
  color: #fca5a5;
}

.item-time {
  font-size: 11px;
  color: #6b7280;
}

.remove-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.action-bar {
  background: #1f2937;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 16px;
}

.single-actions {
  display: flex;
  justify-content: space-around;
}

.action-bar-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.15s ease;
}

.action-bar-btn:hover {
  color: white;
}

.action-bar-btn i {
  font-size: 24px;
}

.action-bar-btn span {
  font-size: 12px;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.batch-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.total-count {
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.success-count {
  font-size: 12px;
  color: #10b981;
}

.error-count {
  font-size: 12px;
  color: #ef4444;
}

.process-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.process-btn:hover:not(:disabled) {
  background: #059669;
}

.process-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.scan-feedback {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 30;
  pointer-events: none;
}

.feedback-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 25px;
  font-weight: 500;
  backdrop-filter: blur(10px);
  animation: feedbackAnimation 2s ease;
}

.feedback-content.success {
  background: rgba(16, 185, 129, 0.9);
  color: white;
}

.feedback-content.error {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

@keyframes feedbackAnimation {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0;
  }
  20% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  80% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0;
  }
}

.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(4px);
}

.modal-content {
  background: #1f2937;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  position: relative;
  color: white;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h3 {
  font-size: 18px;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-size: 14px;
}

.setting-label input[type="checkbox"] {
  accent-color: #3b82f6;
}

.setting-select {
  width: 100%;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  margin-top: 8px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.reset-btn,
.save-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.save-btn {
  background: #3b82f6;
  color: white;
}

.save-btn:hover {
  background: #2563eb;
}

/* PWA 및 모바일 최적화 */
@media (max-width: 768px) {
  .mobile-scan-view {
    user-select: none;
    -webkit-user-select: none;
    -webkit-touch-callout: none;
  }
  
  .mode-selector {
    scrollbar-width: none;
    -ms-overflow-style: none;
  }
  
  .mode-selector::-webkit-scrollbar {
    display: none;
  }
  
  .scan-target {
    width: 200px;
    height: 200px;
  }
  
  .scan-line.animate {
    animation-duration: 1.5s;
  }
  
  @keyframes scanLine {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(200px); }
  }
}

/* 풀스크린 모드 */
.mobile-scan-view:-webkit-full-screen {
  background: #000;
}

.mobile-scan-view:-moz-full-screen {
  background: #000;
}

.mobile-scan-view:fullscreen {
  background: #000;
}
</style>