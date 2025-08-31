<template>
  <div class="warehouse-scan-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">창고 스캔</h1>
      <div class="header-actions">
        <button 
          @click="toggleScanMode" 
          :class="['scan-mode-btn', { active: isScanMode }]"
        >
          <i class="icon-scan"></i>
          {{ isScanMode ? '스캔 모드 OFF' : '스캔 모드 ON' }}
        </button>
        <button @click="showScanHistory = true" class="history-btn">
          <i class="icon-history"></i>
          스캔 기록
        </button>
      </div>
    </div>

    <!-- Scan Mode Interface -->
    <div v-if="isScanMode" class="scan-interface">
      <div class="scan-modes">
        <button 
          v-for="mode in scanModes" 
          :key="mode.value"
          @click="selectedScanType = mode.value"
          :class="['scan-mode-card', { active: selectedScanType === mode.value }]"
        >
          <i :class="mode.icon"></i>
          <span>{{ mode.label }}</span>
        </button>
      </div>

      <!-- QR/Barcode Scanner -->
      <div class="scanner-container">
        <div v-if="!isCameraActive" class="scanner-placeholder">
          <i class="icon-camera-large"></i>
          <p>QR/바코드를 스캔하려면 카메라를 활성화하세요</p>
          <button @click="activateCamera" class="activate-camera-btn">
            카메라 시작
          </button>
        </div>
        
        <div v-else class="camera-view">
          <video ref="videoElement" autoplay playsinline></video>
          <canvas ref="canvasElement" style="display: none;"></canvas>
          <div class="scan-overlay">
            <div class="scan-frame"></div>
            <p class="scan-instruction">QR코드나 바코드를 프레임 안에 맞춰주세요</p>
          </div>
          <button @click="deactivateCamera" class="close-camera-btn">
            <i class="icon-close"></i>
          </button>
        </div>
      </div>

      <!-- Manual Input -->
      <div class="manual-input-section">
        <h3>수동 입력</h3>
        <div class="input-group">
          <label>라벨 코드</label>
          <input 
            v-model="manualLabelCode" 
            type="text" 
            placeholder="라벨 코드를 입력하세요"
            @keyup.enter="processScan(manualLabelCode)"
          />
        </div>
        <div class="input-group">
          <label>위치 (선택사항)</label>
          <input 
            v-model="scanLocation" 
            type="text" 
            placeholder="A1-01-05"
          />
        </div>
        <div class="input-group">
          <label>메모 (선택사항)</label>
          <textarea 
            v-model="scanNotes" 
            placeholder="추가 메모를 입력하세요"
          ></textarea>
        </div>
        <button 
          @click="processScan(manualLabelCode)"
          :disabled="!manualLabelCode || warehouseStore.scanLoading"
          class="process-scan-btn"
        >
          <i class="icon-check"></i>
          {{ warehouseStore.scanLoading ? '처리 중...' : '스캔 처리' }}
        </button>
      </div>
    </div>

    <!-- Inventory Management -->
    <div v-else class="inventory-management">
      <!-- Quick Stats -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon pending">
            <i class="icon-inbox"></i>
          </div>
          <div class="stat-content">
            <h3>{{ warehouseStore.pendingReceiptInventory.length }}</h3>
            <p>입고 대기</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon inspection">
            <i class="icon-search"></i>
          </div>
          <div class="stat-content">
            <h3>{{ warehouseStore.inspectionPendingInventory.length }}</h3>
            <p>검품 대기</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon ready">
            <i class="icon-package"></i>
          </div>
          <div class="stat-content">
            <h3>{{ warehouseStore.readyForShippingInventory.length }}</h3>
            <p>출고 준비</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon hold">
            <i class="icon-pause"></i>
          </div>
          <div class="stat-content">
            <h3>{{ warehouseStore.onHoldInventory.length }}</h3>
            <p>보류</p>
          </div>
        </div>
      </div>

      <!-- Filter and Search -->
      <div class="filter-section">
        <div class="filter-controls">
          <select v-model="statusFilter" class="status-filter">
            <option value="">전체 상태</option>
            <option value="PENDING_RECEIPT">입고 대기</option>
            <option value="RECEIVED">입고 완료</option>
            <option value="INSPECTING">검품 중</option>
            <option value="READY_FOR_SHIPPING">출고 준비</option>
            <option value="ON_HOLD">보류</option>
            <option value="SHIPPED">출고 완료</option>
          </select>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="주문번호, 라벨코드 검색..."
            class="search-input"
          />
        </div>
        <div class="bulk-actions" v-if="selectedInventories.length > 0">
          <span class="selected-count">{{ selectedInventories.length }}개 선택됨</span>
          <button @click="bulkProcess('SHIP')" class="bulk-btn ship">
            <i class="icon-truck"></i>
            일괄 출고
          </button>
          <button @click="bulkProcess('HOLD')" class="bulk-btn hold">
            <i class="icon-pause"></i>
            일괄 보류
          </button>
        </div>
      </div>

      <!-- Inventory List -->
      <div class="inventory-list">
        <div 
          v-for="inventory in filteredInventories" 
          :key="inventory.id"
          class="inventory-item"
        >
          <div class="item-checkbox">
            <input 
              type="checkbox" 
              :value="inventory.id"
              v-model="selectedInventories"
            />
          </div>
          
          <div class="item-info">
            <div class="item-header">
              <h3>{{ inventory.orderNumber }}</h3>
              <span :class="['status-badge', inventory.status.toLowerCase()]">
                {{ getStatusText(inventory.status) }}
              </span>
            </div>
            
            <div class="item-details">
              <p><strong>라벨코드:</strong> {{ inventory.labelCode }}</p>
              <p><strong>위치:</strong> {{ inventory.location || '미지정' }}</p>
              <p><strong>입고일:</strong> {{ formatDate(inventory.receivedAt) }}</p>
              <p v-if="inventory.notes"><strong>메모:</strong> {{ inventory.notes }}</p>
            </div>
          </div>

          <div class="item-actions">
            <button 
              @click="viewInventoryDetail(inventory)"
              class="action-btn detail"
            >
              <i class="icon-eye"></i>
            </button>
            <button 
              @click="processInventoryAction(inventory, 'INSPECT')"
              class="action-btn inspect"
              v-if="canInspect(inventory.status)"
            >
              <i class="icon-search"></i>
            </button>
            <button 
              @click="processInventoryAction(inventory, 'SHIP')"
              class="action-btn ship"
              v-if="canShip(inventory.status)"
            >
              <i class="icon-truck"></i>
            </button>
            <button 
              @click="processInventoryAction(inventory, 'HOLD')"
              class="action-btn hold"
              v-if="canHold(inventory.status)"
            >
              <i class="icon-pause"></i>
            </button>
          </div>
        </div>

        <div v-if="!filteredInventories.length && !warehouseStore.loading" class="empty-state">
          <i class="icon-empty-box"></i>
          <h3>재고가 없습니다</h3>
          <p>현재 조건에 맞는 재고 항목이 없습니다.</p>
        </div>
      </div>
    </div>

    <!-- Scan History Modal -->
    <div v-if="showScanHistory" class="modal-overlay" @click="showScanHistory = false">
      <div class="modal-content scan-history-modal" @click.stop>
        <div class="modal-header">
          <h3>스캔 기록</h3>
          <button @click="showScanHistory = false" class="close-btn">
            <i class="icon-close"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="scan-history-list">
            <div 
              v-for="(scan, index) in warehouseStore.scanHistory" 
              :key="index"
              class="scan-history-item"
            >
              <div class="scan-info">
                <h4>{{ scan.labelCode }}</h4>
                <p class="scan-type">{{ getScanTypeText(scan.scanType) }}</p>
                <p class="scan-time">{{ formatDateTime(scan.timestamp) }}</p>
              </div>
              <div class="scan-result">
                <span :class="['result-status', scan.result?.success ? 'success' : 'error']">
                  {{ scan.result?.success ? '성공' : '실패' }}
                </span>
              </div>
            </div>
          </div>
          <button @click="warehouseStore.clearScanHistory()" class="clear-history-btn">
            기록 지우기
          </button>
        </div>
      </div>
    </div>

    <!-- Processing Modal -->
    <div v-if="showProcessingModal" class="modal-overlay">
      <div class="modal-content processing-modal">
        <div class="processing-content">
          <div class="loading-spinner"></div>
          <h3>{{ processingMessage }}</h3>
          <p>잠시만 기다려주세요...</p>
        </div>
      </div>
    </div>

    <!-- Toast Messages -->
    <div v-if="toastMessage" :class="['toast', toastType]">
      <i :class="toastIcon"></i>
      <span>{{ toastMessage }}</span>
      <button @click="clearToast" class="toast-close">
        <i class="icon-close"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useWarehouseStore } from '../../../stores/warehouse'
import type { WarehouseInventory, InventoryStatus } from '../../../types'

const warehouseStore = useWarehouseStore()

// Scan Mode State
const isScanMode = ref(false)
const selectedScanType = ref<'INBOUND' | 'OUTBOUND' | 'HOLD' | 'MIXBOX'>('INBOUND')
const manualLabelCode = ref('')
const scanLocation = ref('')
const scanNotes = ref('')

// Camera State
const isCameraActive = ref(false)
const videoElement = ref<HTMLVideoElement | null>(null)
const canvasElement = ref<HTMLCanvasElement | null>(null)
const mediaStream = ref<MediaStream | null>(null)

// Inventory Management State
const statusFilter = ref<InventoryStatus | ''>('')
const searchQuery = ref('')
const selectedInventories = ref<number[]>([])

// Modal State
const showScanHistory = ref(false)
const showProcessingModal = ref(false)
const processingMessage = ref('')

// Toast State
const toastMessage = ref('')
const toastType = ref<'success' | 'error' | 'info' | 'warning'>('info')

const scanModes = [
  { value: 'INBOUND', label: '입고', icon: 'icon-arrow-down' },
  { value: 'OUTBOUND', label: '출고', icon: 'icon-arrow-up' },
  { value: 'HOLD', label: '보류', icon: 'icon-pause' },
  { value: 'MIXBOX', label: '믹스박스', icon: 'icon-package-mixed' }
]

// Computed Properties
const filteredInventories = computed(() => {
  let inventories = warehouseStore.inventories
  
  if (statusFilter.value) {
    inventories = inventories.filter(inv => inv.status === statusFilter.value)
  }
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    inventories = inventories.filter(inv => 
      inv.orderNumber.toLowerCase().includes(query) ||
      inv.labelCode.toLowerCase().includes(query)
    )
  }
  
  return inventories
})

const toastIcon = computed(() => {
  switch (toastType.value) {
    case 'success': return 'icon-check'
    case 'error': return 'icon-alert'
    case 'warning': return 'icon-warning'
    default: return 'icon-info'
  }
})

// Methods
const toggleScanMode = () => {
  isScanMode.value = !isScanMode.value
  if (!isScanMode.value && isCameraActive.value) {
    deactivateCamera()
  }
}

const activateCamera = async () => {
  try {
    const constraints = {
      video: {
        facingMode: 'environment', // Use back camera if available
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    }

    mediaStream.value = await navigator.mediaDevices.getUserMedia(constraints)
    
    if (videoElement.value) {
      videoElement.value.srcObject = mediaStream.value
      isCameraActive.value = true
      
      // Start QR/Barcode detection
      startBarcodeDetection()
    }
  } catch (error) {
    console.error('Camera access error:', error)
    showToast('카메라에 접근할 수 없습니다.', 'error')
  }
}

const deactivateCamera = () => {
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach(track => track.stop())
    mediaStream.value = null
  }
  isCameraActive.value = false
}

const startBarcodeDetection = () => {
  // Implement barcode/QR code detection logic here
  // This would typically use a library like jsQR or zxing-js
  const detectCode = () => {
    if (!isCameraActive.value || !videoElement.value || !canvasElement.value) return

    const video = videoElement.value
    const canvas = canvasElement.value
    const ctx = canvas.getContext('2d')

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    ctx?.drawImage(video, 0, 0, canvas.width, canvas.height)

    // Here you would implement actual barcode/QR detection
    // For now, we'll just continue the detection loop
    setTimeout(detectCode, 100)
  }

  if (isCameraActive.value) {
    setTimeout(detectCode, 500) // Start after camera is ready
  }
}

const processScan = async (labelCode: string) => {
  if (!labelCode.trim()) {
    showToast('라벨 코드를 입력하세요.', 'warning')
    return
  }

  showProcessingModal.value = true
  processingMessage.value = '스캔을 처리하고 있습니다'

  try {
    const result = await warehouseStore.scanItem({
      labelCode: labelCode.trim(),
      scanType: selectedScanType.value,
      location: scanLocation.value,
      notes: scanNotes.value,
      warehouseId: 1
    })

    if (result.success) {
      showToast(`${getScanTypeText(selectedScanType.value)} 스캔이 완료되었습니다.`, 'success')
      
      // Clear form
      manualLabelCode.value = ''
      scanLocation.value = ''
      scanNotes.value = ''
      
      // Refresh inventory
      await warehouseStore.fetchWarehouseInventory()
    } else {
      showToast(result.error || '스캔 처리에 실패했습니다.', 'error')
    }
  } catch (error) {
    showToast('스캔 처리 중 오류가 발생했습니다.', 'error')
  } finally {
    showProcessingModal.value = false
  }
}

const viewInventoryDetail = (inventory: WarehouseInventory) => {
  // Navigate to inventory detail view
  warehouseStore.currentInventory = inventory
  // You would typically use router.push here
}

const processInventoryAction = async (inventory: WarehouseInventory, action: string) => {
  showProcessingModal.value = true
  
  try {
    let result
    const actionText = action === 'INSPECT' ? '검품' : action === 'SHIP' ? '출고' : '보류'
    processingMessage.value = `${actionText} 처리 중입니다`

    switch (action) {
      case 'INSPECT':
        result = await warehouseStore.inspectInventory(inventory.id, {
          inspectedBy: 'current-user', // Would get from auth store
          notes: '검품 완료'
        })
        break
      case 'SHIP':
        result = await warehouseStore.processOutbound(inventory.id, {
          shippedBy: 'current-user'
        })
        break
      case 'HOLD':
        result = await warehouseStore.holdInventory(inventory.id, '보류 처리')
        break
    }

    if (result?.success) {
      showToast(`${actionText} 처리가 완료되었습니다.`, 'success')
    } else {
      showToast(result?.error || `${actionText} 처리에 실패했습니다.`, 'error')
    }
  } catch (error) {
    showToast('처리 중 오류가 발생했습니다.', 'error')
  } finally {
    showProcessingModal.value = false
  }
}

const bulkProcess = async (action: 'SHIP' | 'HOLD') => {
  if (selectedInventories.value.length === 0) return

  showProcessingModal.value = true
  processingMessage.value = `${action === 'SHIP' ? '일괄 출고' : '일괄 보류'} 처리 중입니다`

  try {
    let result
    if (action === 'SHIP') {
      result = await warehouseStore.batchProcessOutbound(selectedInventories.value, 'current-user')
    } else {
      // Implement bulk hold if needed
      showToast('일괄 보류 기능은 준비 중입니다.', 'info')
      return
    }

    if (result?.success) {
      showToast(`${selectedInventories.value.length}개 항목이 처리되었습니다.`, 'success')
      selectedInventories.value = []
    } else {
      showToast(result?.error || '일괄 처리에 실패했습니다.', 'error')
    }
  } catch (error) {
    showToast('일괄 처리 중 오류가 발생했습니다.', 'error')
  } finally {
    showProcessingModal.value = false
  }
}

const canInspect = (status: string) => ['RECEIVED'].includes(status)
const canShip = (status: string) => ['READY_FOR_SHIPPING'].includes(status)
const canHold = (status: string) => !['SHIPPED', 'ON_HOLD'].includes(status)

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING_RECEIPT': '입고 대기',
    'RECEIVED': '입고 완료',
    'INSPECTING': '검품 중',
    'READY_FOR_SHIPPING': '출고 준비',
    'ON_HOLD': '보류',
    'SHIPPED': '출고 완료',
    'DAMAGED': '손상',
    'MISSING': '분실'
  }
  return statusMap[status] || status
}

const getScanTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'INBOUND': '입고',
    'OUTBOUND': '출고',
    'HOLD': '보류',
    'MIXBOX': '믹스박스'
  }
  return typeMap[type] || type
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('ko-KR')
}

const showToast = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(clearToast, 5000)
}

const clearToast = () => {
  toastMessage.value = ''
}

// Lifecycle
onMounted(async () => {
  await warehouseStore.fetchWarehouseInventory()
})

onUnmounted(() => {
  deactivateCamera()
})
</script>

<style scoped>
.warehouse-scan-page {
  padding: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.scan-mode-btn, .history-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.scan-mode-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.scan-mode-btn:hover, .history-btn:hover {
  background: var(--primary-light);
}

/* Scan Interface */
.scan-interface {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.scan-modes {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.scan-mode-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  border: 2px solid var(--border-color);
  border-radius: 0.75rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.scan-mode-card.active {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.scan-mode-card i {
  font-size: 1.5rem;
  color: var(--text-secondary);
}

.scan-mode-card.active i {
  color: var(--primary-color);
}

/* Scanner */
.scanner-container {
  background: var(--bg-secondary);
  border-radius: 0.75rem;
  overflow: hidden;
  position: relative;
  min-height: 300px;
}

.scanner-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  gap: 1rem;
  color: var(--text-secondary);
}

.scanner-placeholder i {
  font-size: 3rem;
}

.activate-camera-btn {
  padding: 0.75rem 1.5rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
}

.camera-view {
  position: relative;
}

.camera-view video {
  width: 100%;
  height: 300px;
  object-fit: cover;
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
  pointer-events: none;
}

.scan-frame {
  width: 200px;
  height: 200px;
  border: 2px solid var(--primary-color);
  border-radius: 0.5rem;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.3);
}

.scan-instruction {
  margin-top: 1rem;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
}

.close-camera-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  cursor: pointer;
}

/* Manual Input */
.manual-input-section {
  background: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
}

.manual-input-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.input-group {
  margin-bottom: 1rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.input-group input,
.input-group textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.input-group input:focus,
.input-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.status-filter:focus,
.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-group textarea {
  resize: vertical;
  height: 80px;
}

.process-scan-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
}

.process-scan-btn:hover:not(:disabled) {
  background: var(--primary-dark);
}

.process-scan-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
}

.stat-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  font-size: 1.5rem;
  color: white;
}

.stat-icon.pending { background: var(--warning-color); }
.stat-icon.inspection { background: var(--info-color); }
.stat-icon.ready { background: var(--success-color); }
.stat-icon.hold { background: var(--danger-color); }

.stat-content h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

/* Filter Section */
.filter-section {
  background: white;
  padding: 1rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  margin-bottom: 1rem;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.status-filter,
.search-input {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.search-input {
  flex: 1;
}

.bulk-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.selected-count {
  font-weight: 500;
  color: var(--primary-color);
}

.bulk-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
}

.bulk-btn.ship {
  background: var(--success-color);
  color: white;
}

.bulk-btn.hold {
  background: var(--warning-color);
  color: white;
}

/* Inventory List */
.inventory-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.inventory-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
}

.item-checkbox input {
  width: 18px;
  height: 18px;
}

.item-info {
  flex: 1;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.item-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.pending_receipt { background: var(--warning-light); color: var(--warning-dark); }
.status-badge.received { background: var(--info-light); color: var(--info-dark); }
.status-badge.inspecting { background: var(--primary-light); color: var(--primary-dark); }
.status-badge.ready_for_shipping { background: var(--success-light); color: var(--success-dark); }
.status-badge.on_hold { background: var(--danger-light); color: var(--danger-dark); }
.status-badge.shipped { background: var(--secondary-light); color: var(--secondary-dark); }

.item-details p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.item-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn.detail { background: var(--secondary-light); color: var(--secondary-dark); }
.action-btn.inspect { background: var(--primary-light); color: var(--primary-dark); }
.action-btn.ship { background: var(--success-light); color: var(--success-dark); }
.action-btn.hold { background: var(--warning-light); color: var(--warning-dark); }

.action-btn:hover {
  opacity: 0.8;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
}

.empty-state p {
  margin: 0;
}

/* Modals */
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
  border-radius: 0.75rem;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
}

.modal-body {
  padding: 1rem;
  max-height: 60vh;
  overflow-y: auto;
}

/* Scan History Modal */
.scan-history-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.scan-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--bg-secondary);
  border-radius: 0.5rem;
}

.scan-info h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.scan-info p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.result-status {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.result-status.success {
  background: var(--success-light);
  color: var(--success-dark);
}

.result-status.error {
  background: var(--danger-light);
  color: var(--danger-dark);
}

.clear-history-btn {
  width: 100%;
  padding: 0.75rem;
  background: var(--danger-color);
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
}

/* Processing Modal */
.processing-modal {
  text-align: center;
}

.processing-content {
  padding: 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Toast */
.toast {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  color: white;
  font-weight: 500;
  z-index: 1001;
  animation: slideInRight 0.3s ease;
}

.toast.success { background: var(--success-color); }
.toast.error { background: var(--danger-color); }
.toast.warning { background: var(--warning-color); }
.toast.info { background: var(--info-color); }

.toast-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 0.25rem;
  margin-left: 0.5rem;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .scan-modes {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-controls {
    flex-direction: column;
  }

  .inventory-item {
    flex-direction: column;
    align-items: stretch;
  }

  .item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .item-actions {
    justify-content: center;
    margin-top: 0.5rem;
  }

  .bulk-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .toast {
    bottom: 1rem;
    right: 1rem;
    left: 1rem;
  }
}
</style>