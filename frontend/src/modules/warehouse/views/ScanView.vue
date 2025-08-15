<template>
  <div class="scan-view">
    <!-- 헤더 -->
    <div class="header">
      <h1>{{ $t('warehouse.scan.title') }}</h1>
      <div class="header-actions">
        <button @click="toggleBatchMode" class="btn-toggle" :class="{ active: batchMode }">
          {{ $t(batchMode ? 'warehouse.scan.single_mode' : 'warehouse.scan.batch_mode') }}
        </button>
      </div>
    </div>

    <!-- 스캔 모드 선택 -->
    <div class="scan-mode-selector">
      <div class="mode-tabs">
        <button
          v-for="mode in scanModes"
          :key="mode.value"
          class="mode-tab"
          :class="{ active: selectedScanMode === mode.value }"
          @click="selectedScanMode = mode.value"
        >
          <i :class="mode.icon"></i>
          <span>{{ $t(mode.label) }}</span>
        </button>
      </div>
    </div>

    <!-- QR 스캐너 -->
    <div class="scanner-section">
      <div class="scanner-container" :class="{ scanning: isScanning }">
        <!-- 카메라 뷰 -->
        <div v-if="showCamera" class="camera-view">
          <QRScanner
            ref="qrScanner"
            @scan="handleScan"
            @error="handleScanError"
            :active="isScanning"
          />
          
          <!-- 스캔 오버레이 -->
          <div class="scan-overlay">
            <div class="scan-area">
              <div class="scan-corners"></div>
              <div class="scan-line" :class="{ animate: isScanning }"></div>
            </div>
            <div class="scan-instructions">
              {{ $t('warehouse.scan.instructions') }}
            </div>
          </div>
        </div>

        <!-- 수동 입력 -->
        <div v-else class="manual-input">
          <div class="input-group">
            <input
              v-model="manualLabelCode"
              type="text"
              :placeholder="$t('warehouse.scan.manual_placeholder')"
              class="label-input"
              @keyup.enter="handleManualScan"
            />
            <button @click="handleManualScan" class="btn-scan" :disabled="!manualLabelCode.trim()">
              {{ $t('warehouse.scan.manual_scan') }}
            </button>
          </div>
        </div>

        <!-- 스캐너 토글 -->
        <div class="scanner-controls">
          <button @click="toggleCamera" class="btn-camera">
            <i :class="showCamera ? 'icon-keyboard' : 'icon-camera'"></i>
            {{ $t(showCamera ? 'warehouse.scan.manual_mode' : 'warehouse.scan.camera_mode') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 일괄 모드 리스트 -->
    <div v-if="batchMode" class="batch-section">
      <div class="batch-header">
        <h3>{{ $t('warehouse.scan.batch_items') }}</h3>
        <div class="batch-count">
          {{ scannedItems.length }} {{ $t('warehouse.scan.items_selected') }}
        </div>
      </div>

      <div v-if="scannedItems.length > 0" class="batch-list">
        <div
          v-for="(item, index) in scannedItems"
          :key="item.labelCode"
          class="batch-item"
        >
          <div class="item-info">
            <div class="item-code">{{ item.labelCode }}</div>
            <div class="item-details">
              <span class="order-code">{{ item.orderCode }}</span>
              <span class="item-status" :class="item.status">
                {{ $t(`warehouse.status.${item.status}`) }}
              </span>
            </div>
          </div>
          <button @click="removeBatchItem(index)" class="btn-remove">
            <i class="icon-close"></i>
          </button>
        </div>
      </div>

      <div v-else class="empty-batch">
        <i class="icon-scan"></i>
        <p>{{ $t('warehouse.scan.batch_empty') }}</p>
      </div>

      <!-- 일괄 처리 버튼 -->
      <div v-if="scannedItems.length > 0" class="batch-actions">
        <button @click="clearBatch" class="btn btn-secondary">
          {{ $t('warehouse.scan.clear_batch') }}
        </button>
        <button @click="processBatch" class="btn btn-primary" :disabled="processing">
          <LoadingSpinner v-if="processing" size="small" />
          {{ $t('warehouse.scan.process_batch') }}
        </button>
      </div>
    </div>

    <!-- 최근 스캔 히스토리 -->
    <div class="history-section">
      <div class="history-header">
        <h3>{{ $t('warehouse.scan.recent_scans') }}</h3>
        <button @click="clearHistory" class="btn-clear">
          {{ $t('warehouse.scan.clear_history') }}
        </button>
      </div>

      <div v-if="recentScans.length > 0" class="history-list">
        <div
          v-for="scan in recentScans"
          :key="scan.id"
          class="history-item"
          @click="viewScanDetail(scan)"
        >
          <div class="scan-info">
            <div class="scan-code">{{ scan.labelCode }}</div>
            <div class="scan-details">
              <span class="scan-type" :class="scan.scanType">
                {{ $t(`warehouse.scan_types.${scan.scanType}`) }}
              </span>
              <span class="scan-time">{{ formatDateTime(scan.timestamp) }}</span>
            </div>
          </div>
          <div class="scan-result" :class="scan.success ? 'success' : 'error'">
            <i :class="scan.success ? 'icon-check' : 'icon-error'"></i>
          </div>
        </div>
      </div>

      <div v-else class="empty-history">
        <i class="icon-history"></i>
        <p>{{ $t('warehouse.scan.no_history') }}</p>
      </div>
    </div>

    <!-- 스캔 결과 모달 -->
    <div v-if="showResultModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeResultModal">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3 text-center">
          <h3 class="text-lg font-medium text-gray-900">스캔 결과</h3>
          <div class="mt-2 px-7 py-3">
            <p class="text-sm text-gray-500">스캔이 완료되었습니다.</p>
          </div>
          <div class="items-center px-4 py-3">
            <button @click="closeResultModal" class="px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md w-full">
              확인
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 일괄 처리 결과 모달 -->
    <div v-if="showBatchResultModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeBatchResultModal">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3 text-center">
          <h3 class="text-lg font-medium text-gray-900">일괄 처리 결과</h3>
          <div class="mt-2 px-7 py-3">
            <p class="text-sm text-gray-500">일괄 처리가 완료되었습니다.</p>
          </div>
          <div class="items-center px-4 py-3">
            <button @click="closeBatchResultModal" class="px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md w-full">
              확인
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 권한 요청 모달 -->
    <div v-if="showPermissionModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3 text-center">
          <h3 class="text-lg font-medium text-gray-900">카메라 권한 필요</h3>
          <div class="mt-2 px-7 py-3">
            <p class="text-sm text-gray-500">QR 코드 스캔을 위해 카메라 권한이 필요합니다.</p>
          </div>
          <div class="items-center px-4 py-3 flex space-x-2">
            <button @click="requestCameraPermission" class="px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md flex-1">
              허용
            </button>
            <button @click="closePermissionModal" class="px-4 py-2 bg-gray-500 text-white text-base font-medium rounded-md flex-1">
              거부
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '@/stores/warehouse'
import { useToast } from '@/composables/useToast'
import QRScanner from '../components/QRScanner.vue'
// Removed missing modal imports - using inline modals instead
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import type { ScanRequest, ScanResult, BatchScanItem } from '@/types/warehouse'

interface BatchProcessRequest {
  orderIds: string[]
  action: string
  warehouseCode: string
}

const { t } = useI18n()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 스캔 모드
const scanModes = [
  { value: 'inbound', label: 'warehouse.scan_types.inbound', icon: 'icon-inbox' },
  { value: 'outbound', label: 'warehouse.scan_types.outbound', icon: 'icon-outbox' },
  { value: 'hold', label: 'warehouse.scan_types.hold', icon: 'icon-pause' },
  { value: 'mixbox', label: 'warehouse.scan_types.mixbox', icon: 'icon-package' }
]

const selectedScanMode = ref<string>('inbound')
const batchMode = ref(false)
const showCamera = ref(true)
const isScanning = ref(false)
const processing = ref(false)

// 스캔 데이터
const manualLabelCode = ref('')
const scannedItems = ref<BatchScanItem[]>([])
const recentScans = ref<ScanResult[]>([])

// 모달 상태
const showResultModal = ref(false)
const showBatchResultModal = ref(false)
const showPermissionModal = ref(false)
const currentScanResult = ref<ScanResult | null>(null)
const batchResult = ref<any>(null)

// QR 스캐너 ref
const qrScanner = ref<InstanceType<typeof QRScanner> | null>(null)

// 카메라 권한 상태
const cameraPermission = ref<'granted' | 'denied' | 'prompt'>('prompt')

// 컴포넌트 마운트
onMounted(async () => {
  await checkCameraPermission()
  loadRecentScans()
  
  if (cameraPermission.value === 'granted') {
    startScanning()
  }
})

// 컴포넌트 언마운트
onUnmounted(() => {
  stopScanning()
})

// 카메라 권한 확인
const checkCameraPermission = async () => {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    showToast(t('warehouse.scan.camera_not_supported'), 'error')
    showCamera.value = false
    return
  }

  try {
    const permissions = await navigator.permissions.query({ name: 'camera' as PermissionName })
    cameraPermission.value = permissions.state
    
    if (permissions.state === 'prompt') {
      showPermissionModal.value = true
    }
  } catch (error) {
    console.warn('Permission API not supported')
    cameraPermission.value = 'prompt'
  }
}

// 카메라 권한 요청
const requestCameraPermission = async () => {
  showPermissionModal.value = false
  
  try {
    await navigator.mediaDevices.getUserMedia({ video: true })
    cameraPermission.value = 'granted'
    startScanning()
  } catch (error) {
    cameraPermission.value = 'denied'
    showToast(t('warehouse.scan.camera_permission_denied'), 'error')
    showCamera.value = false
  }
}

const closePermissionModal = () => {
  showPermissionModal.value = false
  showCamera.value = false
}

// 스캔 시작/중지
const startScanning = () => {
  if (showCamera.value && cameraPermission.value === 'granted') {
    isScanning.value = true
  }
}

const stopScanning = () => {
  isScanning.value = false
}

// 카메라/수동 모드 토글
const toggleCamera = async () => {
  if (!showCamera.value && cameraPermission.value !== 'granted') {
    await checkCameraPermission()
    if (cameraPermission.value === 'prompt') {
      showPermissionModal.value = true
      return
    }
  }
  
  showCamera.value = !showCamera.value
  manualLabelCode.value = ''
  
  if (showCamera.value) {
    startScanning()
  } else {
    stopScanning()
  }
}

// 일괄/단일 모드 토글
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    scannedItems.value = []
  }
}

// QR 스캔 처리
const handleScan = (labelCode: string) => {
  processScan(labelCode)
}

// 수동 스캔 처리
const handleManualScan = () => {
  if (!manualLabelCode.value.trim()) return
  
  processScan(manualLabelCode.value.trim())
  manualLabelCode.value = ''
}

// 스캔 처리 (공통)
const processScan = async (labelCode: string) => {
  // 중복 체크 (일괄 모드)
  if (batchMode.value && scannedItems.value.some(item => item.labelCode === labelCode)) {
    showToast(t('warehouse.scan.duplicate_item'), 'warning')
    return
  }

  try {
    const scanRequest: ScanRequest = {
      labelCode,
      scanType: selectedScanMode.value,
      warehouseCode: 'WH-ICN-001'
    }

    if (batchMode.value) {
      // 일괄 모드: 목록에 추가만 (실제 스캔은 일괄 처리시)
      scannedItems.value.push({
        labelCode,
        orderCode: `ORD-${labelCode}`,
        status: 'stored'
      })
      showToast(t('warehouse.scan.item_added'), 'success')
    } else {
      // 단일 모드: 즉시 처리
      const result = await warehouseStore.scanBox(scanRequest)
      currentScanResult.value = result.data
      showResultModal.value = true
      
      // 히스토리에 추가
      addToHistory({
        id: Date.now(),
        labelCode,
        scanType: selectedScanMode.value,
        timestamp: new Date(),
        success: result.success
      })
    }
  } catch (error: any) {
    console.error('Scan processing error:', error)
    
    if (batchMode.value) {
      showToast(error.message || t('warehouse.scan.error'), 'error')
    } else {
      currentScanResult.value = {
        success: false,
        error: error.message || t('warehouse.scan.error'),
        labelCode
      }
      showResultModal.value = true
      
      addToHistory({
        id: Date.now(),
        labelCode,
        scanType: selectedScanMode.value,
        timestamp: new Date(),
        success: false,
        error: error.message
      })
    }
  }
}

// 일괄 처리
const processBatch = async () => {
  if (scannedItems.value.length === 0) return

  processing.value = true

  try {
    const batchRequest: BatchProcessRequest = {
      orderIds: scannedItems.value.map(item => item.labelCode),
      action: selectedScanMode.value,
      warehouseCode: 'WH-ICN-001'
    }

    const result = await warehouseStore.batchProcess(batchRequest)
    batchResult.value = result.data
    showBatchResultModal.value = true

    // 성공한 항목들을 히스토리에 추가
    result.data?.results?.forEach((item: any) => {
      addToHistory({
        id: Date.now() + Math.random(),
        labelCode: item.orderId,
        scanType: selectedScanMode.value,
        timestamp: new Date(),
        success: item.success
      })
    })

    // 일괄 목록 클리어
    scannedItems.value = []
    
    showToast(
      t('warehouse.scan.batch_completed', { 
        processed: result.data?.processedCount || 0, 
        failed: result.data?.failedCount || 0
      }),
      (result.data?.failedCount || 0) > 0 ? 'warning' : 'success'
    )

  } catch (error: any) {
    console.error('Batch processing error:', error)
    showToast(error.message || t('warehouse.scan.batch_error'), 'error')
  } finally {
    processing.value = false
  }
}

// 일괄 모드 아이템 제거
const removeBatchItem = (index: number) => {
  scannedItems.value.splice(index, 1)
}

// 일괄 모드 클리어
const clearBatch = () => {
  scannedItems.value = []
  showToast(t('warehouse.scan.batch_cleared'), 'info')
}

// 히스토리 관리
const addToHistory = (scanResult: ScanResult) => {
  recentScans.value.unshift(scanResult)
  if (recentScans.value.length > 50) {
    recentScans.value = recentScans.value.slice(0, 50)
  }
  saveRecentScans()
}

const loadRecentScans = () => {
  const saved = localStorage.getItem('warehouse_scan_history')
  if (saved) {
    try {
      recentScans.value = JSON.parse(saved).map((item: any) => ({
        ...item,
        timestamp: new Date(item.timestamp)
      }))
    } catch (error) {
      console.error('Failed to load scan history:', error)
    }
  }
}

const saveRecentScans = () => {
  try {
    localStorage.setItem('warehouse_scan_history', JSON.stringify(recentScans.value))
  } catch (error) {
    console.error('Failed to save scan history:', error)
  }
}

const clearHistory = () => {
  recentScans.value = []
  localStorage.removeItem('warehouse_scan_history')
  showToast(t('warehouse.scan.history_cleared'), 'info')
}

// 스캔 결과 처리
const handleScanAction = (action: string, data?: any) => {
  // TODO: 스캔 후 추가 액션 처리 (라벨 출력, 사진 업로드 등)
  console.log('Scan action:', action, data)
  closeResultModal()
}

// 모달 닫기
const closeResultModal = () => {
  showResultModal.value = false
  currentScanResult.value = null
}

const closeBatchResultModal = () => {
  showBatchResultModal.value = false
  batchResult.value = null
}

// 스캔 상세 보기
const viewScanDetail = (scan: ScanResult) => {
  currentScanResult.value = scan
  showResultModal.value = true
}

// 스캔 에러 처리
const handleScanError = (error: any) => {
  console.error('QR Scanner error:', error)
  showToast(t('warehouse.scan.scanner_error'), 'error')
}

// 날짜/시간 포맷팅
const formatDateTime = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 스캔 모드 변경시 일괄 목록 클리어
watch(selectedScanMode, () => {
  if (batchMode.value) {
    scannedItems.value = []
  }
})
</script>

<style scoped>
.scan-view {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.btn-toggle {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-toggle.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.scan-mode-selector {
  margin-bottom: 20px;
}

.mode-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px;
}

.mode-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 14px;
}

.mode-tab:hover {
  border-color: #3b82f6;
}

.mode-tab.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.mode-tab i {
  font-size: 20px;
}

.scanner-section {
  margin-bottom: 30px;
}

.scanner-container {
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.scanner-container.scanning {
  border-color: #10b981;
  background: #f0fdf4;
}

.camera-view {
  position: relative;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  color: white;
}

.scan-area {
  width: 200px;
  height: 200px;
  position: relative;
  border: 2px solid #10b981;
  border-radius: 8px;
}

.scan-corners::before,
.scan-corners::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #10b981;
}

.scan-corners::before {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.scan-corners::after {
  top: -3px;
  right: -3px;
  border-left: none;
  border-bottom: none;
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
  50% { transform: translateY(200px); }
}

.scan-instructions {
  margin-top: 20px;
  font-size: 14px;
  text-align: center;
}

.manual-input {
  padding: 40px 20px;
  text-align: center;
}

.input-group {
  display: flex;
  gap: 12px;
  max-width: 400px;
  margin: 0 auto;
}

.label-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 16px;
}

.label-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-scan {
  padding: 12px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.btn-scan:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.scanner-controls {
  padding: 16px;
  text-align: center;
  border-top: 1px solid #e5e7eb;
  background: white;
}

.btn-camera {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  margin: 0 auto;
}

.batch-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.batch-count {
  font-size: 14px;
  color: #6b7280;
}

.batch-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 20px;
}

.batch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 8px;
}

.item-info {
  flex: 1;
}

.item-code {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.item-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
}

.item-status {
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.item-status.created { background: #f3f4f6; color: #374151; }
.item-status.inbound_completed { background: #dcfce7; color: #166534; }
.item-status.ready_for_outbound { background: #dbeafe; color: #1e40af; }
.item-status.hold { background: #fef3c7; color: #92400e; }

.btn-remove {
  color: #ef4444;
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
}

.btn-remove:hover {
  background: #fef2f2;
}

.empty-batch {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-batch i {
  font-size: 48px;
  margin-bottom: 12px;
}

.batch-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.history-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.history-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.btn-clear {
  color: #6b7280;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s ease;
  margin-bottom: 4px;
}

.history-item:hover {
  background: #f9fafb;
}

.scan-info {
  flex: 1;
}

.scan-code {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.scan-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
}

.scan-type {
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.scan-type.inbound { background: #dcfce7; color: #166534; }
.scan-type.outbound { background: #dbeafe; color: #1e40af; }
.scan-type.hold { background: #fef3c7; color: #92400e; }
.scan-type.mixbox { background: #f3e8ff; color: #7c3aed; }

.scan-result {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.scan-result.success { color: #10b981; }
.scan-result.error { color: #ef4444; }

.empty-history {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-history i {
  font-size: 48px;
  margin-bottom: 12px;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.15s ease;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #f9fafb;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .scan-view {
    padding: 16px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .mode-tabs {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .camera-view {
    height: 250px;
  }
  
  .scan-area {
    width: 150px;
    height: 150px;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .batch-actions {
    flex-direction: column;
  }
}
</style>