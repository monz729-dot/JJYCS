<template>
  <div class="label-scanner">
    <div class="scanner-header">
      <h2>라벨 스캐너</h2>
      <div class="scanner-mode">
        <label class="radio-option">
          <input type="radio" v-model="scanMode" value="inbound" />
          입고 스캔
        </label>
        <label class="radio-option">
          <input type="radio" v-model="scanMode" value="outbound" />
          출고 스캔
        </label>
        <label class="radio-option">
          <input type="radio" v-model="scanMode" value="tracking" />
          추적 스캔
        </label>
      </div>
    </div>

    <!-- 스캔 입력 영역 -->
    <div class="scan-input-section">
      <div class="scan-input">
        <input
          ref="scanInput"
          type="text"
          v-model="currentScan"
          @keyup.enter="processScan"
          @input="handleScanInput"
          placeholder="QR 코드나 바코드를 스캔하세요"
          class="scan-field"
          :disabled="isProcessing"
        />
        <button 
          @click="processScan" 
          :disabled="!currentScan || isProcessing"
          class="btn-scan"
        >
          <span v-if="isProcessing">처리 중...</span>
          <span v-else>스캔 처리</span>
        </button>
      </div>
      
      <div class="scan-options">
        <label class="checkbox-option">
          <input type="checkbox" v-model="autoProcess" />
          자동 처리 (스캔 시 즉시 처리)
        </label>
        <label class="checkbox-option">
          <input type="checkbox" v-model="soundEnabled" />
          스캔 완료 사운드
        </label>
      </div>
    </div>

    <!-- 스캔 이력 -->
    <div class="scan-history">
      <div class="history-header">
        <h3>스캔 이력</h3>
        <div class="history-controls">
          <span class="scan-count">총 {{ scanHistory.length }}건</span>
          <button @click="exportScanHistory" class="btn-secondary">내보내기</button>
          <button @click="clearScanHistory" class="btn-danger">이력 삭제</button>
        </div>
      </div>

      <div class="history-filters">
        <select v-model="historyFilter" class="filter-select">
          <option value="">전체</option>
          <option value="success">성공</option>
          <option value="error">실패</option>
          <option value="order">주문</option>
          <option value="box">박스</option>
        </select>
        <input 
          type="text" 
          v-model="historySearch" 
          placeholder="검색..."
          class="search-input"
        />
      </div>

      <div class="history-list">
        <div 
          v-for="(scan, index) in filteredScanHistory" 
          :key="index"
          class="scan-item"
          :class="{ 'scan-success': scan.success, 'scan-error': !scan.success }"
        >
          <div class="scan-info">
            <div class="scan-header">
              <span class="scan-code">{{ scan.scannedCode }}</span>
              <span class="scan-time">{{ formatTime(scan.timestamp) }}</span>
            </div>
            <div class="scan-details">
              <span class="scan-mode-badge" :class="`mode-${scan.mode}`">{{ getModeLabel(scan.mode) }}</span>
              <span class="scan-type">{{ scan.result?.type || 'unknown' }}</span>
              <span v-if="scan.result?.orderNumber" class="scan-order">주문: {{ scan.result.orderNumber }}</span>
              <span v-if="scan.result?.boxId" class="scan-box">박스: {{ scan.result.boxId }}</span>
            </div>
            <div v-if="scan.message" class="scan-message">{{ scan.message }}</div>
            <div v-if="scan.error" class="scan-error">{{ scan.error }}</div>
          </div>
          <div class="scan-actions">
            <button @click="rescanItem(scan)" class="btn-small">재스캔</button>
            <button @click="viewScanDetails(scan)" class="btn-small">상세</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 스캔 통계 -->
    <div class="scan-statistics">
      <h3>오늘의 스캔 통계</h3>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-number">{{ todayStats.total }}</div>
          <div class="stat-label">총 스캔</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ todayStats.success }}</div>
          <div class="stat-label">성공</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ todayStats.error }}</div>
          <div class="stat-label">실패</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ Math.round(todayStats.successRate) }}%</div>
          <div class="stat-label">성공률</div>
        </div>
      </div>
    </div>

    <!-- 스캔 결과 모달 -->
    <div v-if="showResultModal" class="modal-overlay" @click="closeResultModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>스캔 결과</h3>
          <button @click="closeResultModal" class="modal-close">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="lastScanResult.success" class="result-success">
            <h4>✅ 스캔 성공</h4>
            <div class="result-details">
              <p><strong>코드:</strong> {{ lastScanResult.scannedCode }}</p>
              <p><strong>타입:</strong> {{ lastScanResult.result?.type }}</p>
              <p v-if="lastScanResult.result?.orderNumber"><strong>주문번호:</strong> {{ lastScanResult.result.orderNumber }}</p>
              <p v-if="lastScanResult.result?.boxId"><strong>박스 ID:</strong> {{ lastScanResult.result.boxId }}</p>
              <p><strong>처리 모드:</strong> {{ getModeLabel(lastScanResult.mode) }}</p>
            </div>
            <div class="result-actions">
              <button @click="continueScanning" class="btn-primary">계속 스캔</button>
              <button @click="viewOrderDetails" v-if="lastScanResult.result?.orderNumber" class="btn-secondary">주문 상세보기</button>
            </div>
          </div>
          <div v-else class="result-error">
            <h4>❌ 스캔 실패</h4>
            <p class="error-message">{{ lastScanResult.error }}</p>
            <div class="result-actions">
              <button @click="retryScan" class="btn-primary">다시 스캔</button>
              <button @click="continueScanning" class="btn-secondary">계속하기</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useToast } from '@/composables/useToast'

interface ScanRecord {
  scannedCode: string
  timestamp: number
  mode: string
  success: boolean
  result?: any
  message?: string
  error?: string
}

const { showToast } = useToast()

// 상태 관리
const scanMode = ref<'inbound' | 'outbound' | 'tracking'>('inbound')
const currentScan = ref('')
const isProcessing = ref(false)
const autoProcess = ref(true)
const soundEnabled = ref(true)
const showResultModal = ref(false)
const scanInput = ref<HTMLInputElement>()

// 스캔 이력
const scanHistory = ref<ScanRecord[]>([])
const historyFilter = ref('')
const historySearch = ref('')
const lastScanResult = ref<ScanRecord>({} as ScanRecord)

// 자동 스캔 타이머
let autoScanTimer: ReturnType<typeof setTimeout> | null = null

// 컴포넌트 마운트 시 포커스
onMounted(() => {
  nextTick(() => {
    if (scanInput.value) {
      scanInput.value.focus()
    }
  })
  
  // 로컬 스토리지에서 이력 복원
  const savedHistory = localStorage.getItem('scan-history')
  if (savedHistory) {
    try {
      scanHistory.value = JSON.parse(savedHistory)
    } catch (error) {
      console.error('Failed to parse scan history:', error)
    }
  }
})

// 컴포넌트 언마운트 시 정리
onUnmounted(() => {
  if (autoScanTimer) {
    clearTimeout(autoScanTimer)
  }
})

// 필터링된 스캔 이력
const filteredScanHistory = computed(() => {
  let filtered = scanHistory.value

  if (historyFilter.value) {
    if (historyFilter.value === 'success') {
      filtered = filtered.filter(scan => scan.success)
    } else if (historyFilter.value === 'error') {
      filtered = filtered.filter(scan => !scan.success)
    } else if (historyFilter.value === 'order') {
      filtered = filtered.filter(scan => scan.result?.type === 'order')
    } else if (historyFilter.value === 'box') {
      filtered = filtered.filter(scan => scan.result?.type === 'box')
    }
  }

  if (historySearch.value) {
    const search = historySearch.value.toLowerCase()
    filtered = filtered.filter(scan => 
      scan.scannedCode.toLowerCase().includes(search) ||
      scan.result?.orderNumber?.toLowerCase().includes(search) ||
      scan.result?.boxId?.toLowerCase().includes(search)
    )
  }

  return filtered.reverse() // 최신순 정렬
})

// 오늘의 통계
const todayStats = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const todayTimestamp = today.getTime()
  
  const todayScans = scanHistory.value.filter(scan => scan.timestamp >= todayTimestamp)
  const total = todayScans.length
  const success = todayScans.filter(scan => scan.success).length
  const error = total - success
  const successRate = total > 0 ? (success / total) * 100 : 0

  return { total, success, error, successRate }
})

// 스캔 입력 처리
const handleScanInput = () => {
  if (autoProcess.value && autoScanTimer) {
    clearTimeout(autoScanTimer)
  }
  
  if (autoProcess.value && currentScan.value) {
    autoScanTimer = setTimeout(() => {
      processScan()
    }, 500) // 0.5초 후 자동 처리
  }
}

// 스캔 처리
const processScan = async () => {
  if (!currentScan.value || isProcessing.value) return

  isProcessing.value = true
  const scannedCode = currentScan.value.trim()

  try {
    // API 호출
    const response = await fetch('/api/labels/scan', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ qrCode: scannedCode })
    })

    const data = await response.json()
    
    const scanRecord: ScanRecord = {
      scannedCode,
      timestamp: Date.now(),
      mode: scanMode.value,
      success: data.success,
      result: data.scanResult,
      message: data.message
    }

    if (!data.success) {
      scanRecord.error = data.error
    }

    // 스캔 기록 추가
    scanHistory.value.push(scanRecord)
    lastScanResult.value = scanRecord
    
    // 로컬 스토리지에 저장 (최대 1000개만 유지)
    const historyToSave = scanHistory.value.slice(-1000)
    localStorage.setItem('scan-history', JSON.stringify(historyToSave))

    // 사운드 재생
    if (soundEnabled.value) {
      playSound(data.success ? 'success' : 'error')
    }

    // 결과 모달 표시
    showResultModal.value = true

    // 성공 시 토스트 표시
    if (data.success) {
      showToast(`${getModeLabel(scanMode.value)} 스캔 완료`, 'success')
      
      // 창고 작업 API 호출 (입고/출고 처리)
      await processWarehouseOperation(scanRecord)
    } else {
      showToast(`스캔 실패: ${data.error}`, 'error')
    }

    // 입력 필드 클리어 및 포커스
    currentScan.value = ''
    nextTick(() => {
      if (scanInput.value) {
        scanInput.value.focus()
      }
    })

  } catch (error) {
    console.error('Scan processing error:', error)
    
    const errorRecord: ScanRecord = {
      scannedCode,
      timestamp: Date.now(),
      mode: scanMode.value,
      success: false,
      error: '스캔 처리 중 오류가 발생했습니다.'
    }
    
    scanHistory.value.push(errorRecord)
    lastScanResult.value = errorRecord
    showResultModal.value = true
    
    showToast('스캔 처리 중 오류가 발생했습니다.', 'error')
    
    if (soundEnabled.value) {
      playSound('error')
    }
  } finally {
    isProcessing.value = false
  }
}

// 창고 작업 처리 (입고/출고)
const processWarehouseOperation = async (scanRecord: ScanRecord) => {
  if (!scanRecord.success || !scanRecord.result) return

  try {
    const operation = {
      labelCode: scanRecord.scannedCode,
      scanType: scanRecord.mode,
      orderNumber: scanRecord.result.orderNumber,
      boxId: scanRecord.result.boxId,
      timestamp: scanRecord.timestamp
    }

    const response = await fetch('/api/warehouse/scan', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(operation)
    })

    if (!response.ok) {
      console.error('Failed to process warehouse operation')
    }
  } catch (error) {
    console.error('Warehouse operation processing error:', error)
  }
}

// 사운드 재생
const playSound = (type: 'success' | 'error') => {
  try {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    if (type === 'success') {
      oscillator.frequency.setValueAtTime(800, audioContext.currentTime)
      oscillator.frequency.setValueAtTime(1000, audioContext.currentTime + 0.1)
    } else {
      oscillator.frequency.setValueAtTime(400, audioContext.currentTime)
      oscillator.frequency.setValueAtTime(300, audioContext.currentTime + 0.1)
    }
    
    gainNode.gain.setValueAtTime(0.1, audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2)
    
    oscillator.start()
    oscillator.stop(audioContext.currentTime + 0.2)
  } catch (error) {
    console.error('Failed to play sound:', error)
  }
}

// 유틸리티 함수들
const getModeLabel = (mode: string) => {
  const labels = {
    inbound: '입고',
    outbound: '출고',
    tracking: '추적'
  }
  return labels[mode] || mode
}

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('ko-KR')
}

// 모달 관련 함수들
const closeResultModal = () => {
  showResultModal.value = false
}

const continueScanning = () => {
  closeResultModal()
  nextTick(() => {
    if (scanInput.value) {
      scanInput.value.focus()
    }
  })
}

const retryScan = () => {
  currentScan.value = lastScanResult.value.scannedCode
  closeResultModal()
  nextTick(() => {
    if (scanInput.value) {
      scanInput.value.focus()
      scanInput.value.select()
    }
  })
}

const viewOrderDetails = () => {
  const orderNumber = lastScanResult.value.result?.orderNumber
  if (orderNumber) {
    // 주문 상세 페이지로 이동 (라우터 사용)
    window.open(`/orders/${orderNumber}`, '_blank')
  }
  closeResultModal()
}

// 기타 함수들
const rescanItem = (scan: ScanRecord) => {
  currentScan.value = scan.scannedCode
  nextTick(() => {
    if (scanInput.value) {
      scanInput.value.focus()
      scanInput.value.select()
    }
  })
}

const viewScanDetails = (scan: ScanRecord) => {
  lastScanResult.value = scan
  showResultModal.value = true
}

const exportScanHistory = () => {
  const dataStr = JSON.stringify(filteredScanHistory.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  
  const link = document.createElement('a')
  link.href = URL.createObjectURL(dataBlob)
  link.download = `scan-history-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  
  showToast('스캔 이력이 내보내기 되었습니다.', 'success')
}

const clearScanHistory = () => {
  if (confirm('스캔 이력을 모두 삭제하시겠습니까?')) {
    scanHistory.value = []
    localStorage.removeItem('scan-history')
    showToast('스캔 이력이 삭제되었습니다.', 'success')
  }
}
</script>

<style scoped>
.label-scanner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.scanner-header {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.scanner-header h2 {
  margin: 0 0 16px 0;
  color: #1f2937;
}

.scanner-mode {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.radio-option, .checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.scan-input-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.scan-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.scan-field {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  font-family: 'Courier New', monospace;
}

.scan-field:focus {
  border-color: #3b82f6;
  outline: none;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-scan {
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-scan:hover {
  background: #2563eb;
}

.btn-scan:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.scan-options {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.scan-history {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.history-header h3 {
  margin: 0;
  color: #1f2937;
}

.history-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.scan-count {
  color: #6b7280;
  font-size: 14px;
}

.history-filters {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-select, .search-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.scan-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: border-color 0.2s;
}

.scan-item.scan-success {
  border-left: 4px solid #10b981;
}

.scan-item.scan-error {
  border-left: 4px solid #ef4444;
}

.scan-info {
  flex: 1;
}

.scan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.scan-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #1f2937;
}

.scan-time {
  color: #6b7280;
  font-size: 12px;
}

.scan-details {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.scan-mode-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.mode-inbound { background: #10b981; }
.mode-outbound { background: #f59e0b; }
.mode-tracking { background: #3b82f6; }

.scan-type, .scan-order, .scan-box {
  color: #6b7280;
  font-size: 12px;
}

.scan-message {
  color: #10b981;
  font-size: 12px;
  margin-top: 4px;
}

.scan-error {
  color: #ef4444;
  font-size: 12px;
  margin-top: 4px;
}

.scan-actions {
  display: flex;
  gap: 8px;
}

.btn-small, .btn-secondary, .btn-danger, .btn-primary {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-small {
  background: #e5e7eb;
  color: #374151;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  padding: 8px 16px;
}

.scan-statistics {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.scan-statistics h3 {
  margin: 0 0 20px 0;
  color: #1f2937;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  color: #6b7280;
  font-size: 12px;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  color: #1f2937;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
}

.result-success {
  text-align: center;
}

.result-success h4 {
  color: #10b981;
  margin: 0 0 16px 0;
}

.result-error {
  text-align: center;
}

.result-error h4 {
  color: #ef4444;
  margin: 0 0 16px 0;
}

.result-details {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  margin: 16px 0;
  text-align: left;
}

.result-details p {
  margin: 8px 0;
  color: #374151;
}

.error-message {
  color: #ef4444;
  margin: 16px 0;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .scanner-mode {
    flex-direction: column;
    gap: 12px;
  }
  
  .scan-input {
    flex-direction: column;
  }
  
  .history-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .history-controls {
    justify-content: space-between;
  }
  
  .history-filters {
    flex-direction: column;
  }
  
  .scan-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .scan-actions {
    align-self: stretch;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .result-actions {
    flex-direction: column;
  }
}
</style>