<template>
  <div class="label-generator">
    <div class="label-controls">
      <h3>라벨 생성</h3>
      
      <!-- 라벨 타입 선택 -->
      <div class="label-type-selection">
        <label class="radio-option">
          <input type="radio" v-model="labelType" value="order" />
          주문 라벨
        </label>
        <label class="radio-option">
          <input type="radio" v-model="labelType" value="box" />
          박스 라벨
        </label>
        <label class="radio-option">
          <input type="radio" v-model="labelType" value="custom" />
          커스텀 라벨
        </label>
      </div>

      <!-- 주문 라벨 폼 -->
      <div v-if="labelType === 'order'" class="label-form">
        <div class="form-group">
          <label>주문번호</label>
          <input 
            type="text" 
            v-model="orderNumber" 
            placeholder="주문번호를 입력하세요"
            class="form-input"
          />
        </div>
        <button @click="generateOrderLabel" :disabled="!orderNumber || isGenerating" class="btn-primary">
          <span v-if="isGenerating">생성 중...</span>
          <span v-else>주문 라벨 생성</span>
        </button>
      </div>

      <!-- 박스 라벨 폼 -->
      <div v-if="labelType === 'box'" class="label-form">
        <div class="form-group">
          <label>박스 ID</label>
          <input 
            type="text" 
            v-model="boxId" 
            placeholder="박스 ID를 입력하세요"
            class="form-input"
          />
        </div>
        <button @click="generateBoxLabel" :disabled="!boxId || isGenerating" class="btn-primary">
          <span v-if="isGenerating">생성 중...</span>
          <span v-else>박스 라벨 생성</span>
        </button>
      </div>

      <!-- 커스텀 라벨 폼 -->
      <div v-if="labelType === 'custom'" class="label-form">
        <div class="form-row">
          <div class="form-group">
            <label>주문번호</label>
            <input type="text" v-model="customLabel.orderNumber" class="form-input" />
          </div>
          <div class="form-group">
            <label>패키지 타입</label>
            <select v-model="customLabel.packageType" class="form-input">
              <option value="air">항공</option>
              <option value="sea">해상</option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>수취인</label>
          <input type="text" v-model="customLabel.recipientName" class="form-input" />
        </div>
        
        <div class="form-group">
          <label>주소</label>
          <textarea v-model="customLabel.recipientAddress" class="form-textarea"></textarea>
        </div>

        <div class="form-row" v-if="customLabel.isBoxLabel">
          <div class="form-group">
            <label>박스 번호</label>
            <input type="number" v-model="customLabel.boxNumber" class="form-input" />
          </div>
          <div class="form-group">
            <label>총 박스 수</label>
            <input type="number" v-model="customLabel.totalBoxes" class="form-input" />
          </div>
        </div>

        <div class="form-group">
          <label class="checkbox-option">
            <input type="checkbox" v-model="customLabel.isBoxLabel" />
            박스 라벨로 생성
          </label>
        </div>
        
        <button @click="generateCustomLabel" :disabled="isGenerating" class="btn-primary">
          <span v-if="isGenerating">생성 중...</span>
          <span v-else>커스텀 라벨 생성</span>
        </button>
      </div>

      <!-- 일괄 생성 -->
      <div class="batch-generation">
        <h4>일괄 라벨 생성</h4>
        <div class="form-group">
          <label>주문번호 목록 (줄바꿈으로 구분)</label>
          <textarea 
            v-model="batchOrderNumbers" 
            placeholder="YCS-2024-001&#10;YCS-2024-002&#10;YCS-2024-003"
            class="form-textarea"
          ></textarea>
        </div>
        <button @click="generateBatchLabels" :disabled="!batchOrderNumbers || isGenerating" class="btn-secondary">
          <span v-if="isGenerating">일괄 생성 중...</span>
          <span v-else>일괄 라벨 생성</span>
        </button>
      </div>
    </div>

    <!-- 생성된 라벨 표시 -->
    <div v-if="generatedLabel" class="label-result">
      <h4>생성된 라벨</h4>
      <div class="label-preview">
        <img 
          :src="`data:image/png;base64,${generatedLabel.labelImageBase64}`" 
          :alt="`라벨 - ${generatedLabel.orderNumber}`"
          class="label-image"
        />
        <div class="label-info">
          <p><strong>주문번호:</strong> {{ generatedLabel.orderNumber }}</p>
          <p v-if="generatedLabel.boxId"><strong>박스 ID:</strong> {{ generatedLabel.boxId }}</p>
          <p><strong>QR 코드:</strong> {{ generatedLabel.qrCode }}</p>
          <p><strong>생성 시간:</strong> {{ formatDateTime(generatedLabel.generatedAt) }}</p>
        </div>
      </div>
      <div class="label-actions">
        <button @click="printLabel" class="btn-primary">인쇄</button>
        <button @click="downloadLabel" class="btn-secondary">다운로드</button>
        <button @click="copyQRCode" class="btn-secondary">QR 코드 복사</button>
      </div>
    </div>

    <!-- 일괄 생성 결과 -->
    <div v-if="batchResults.length > 0" class="batch-results">
      <h4>일괄 생성 결과 ({{ batchResults.length }}개)</h4>
      <div class="batch-list">
        <div v-for="(result, index) in batchResults" :key="index" class="batch-item">
          <div class="batch-item-header">
            <span>{{ result.orderNumber }}</span>
            <button @click="selectBatchItem(result)" class="btn-small">보기</button>
          </div>
        </div>
      </div>
    </div>

    <!-- QR 스캐너 -->
    <div class="qr-scanner">
      <h4>QR 코드 스캔</h4>
      <div class="scanner-controls">
        <input 
          type="text" 
          v-model="scannedQRCode" 
          placeholder="QR 코드를 입력하거나 스캔하세요"
          class="form-input"
        />
        <button @click="processScannedQR" :disabled="!scannedQRCode" class="btn-primary">스캔 처리</button>
      </div>
      
      <div v-if="scanResult" class="scan-result">
        <h5>스캔 결과</h5>
        <pre>{{ JSON.stringify(scanResult, null, 2) }}</pre>
      </div>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="isGenerating" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>라벨을 생성하고 있습니다...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()

// 상태 관리
const labelType = ref<'order' | 'box' | 'custom'>('order')
const isGenerating = ref(false)
const orderNumber = ref('')
const boxId = ref('')
const batchOrderNumbers = ref('')
const scannedQRCode = ref('')

// 커스텀 라벨 데이터
const customLabel = reactive({
  orderNumber: '',
  recipientName: '',
  recipientAddress: '',
  packageType: 'air',
  isBoxLabel: false,
  boxId: '',
  boxNumber: 1,
  totalBoxes: 1
})

// 결과 데이터
const generatedLabel = ref<any>(null)
const batchResults = ref<any[]>([])
const scanResult = ref<any>(null)

// API 호출 함수들
const generateOrderLabel = async () => {
  if (!orderNumber.value) return
  
  isGenerating.value = true
  try {
    const response = await fetch(`/api/labels/order/${orderNumber.value}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.success) {
      generatedLabel.value = data
      showToast('주문 라벨이 생성되었습니다.', 'success')
    } else {
      showToast(data.error || '라벨 생성에 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('Order label generation error:', error)
    showToast('라벨 생성 중 오류가 발생했습니다.', 'error')
  } finally {
    isGenerating.value = false
  }
}

const generateBoxLabel = async () => {
  if (!boxId.value) return
  
  isGenerating.value = true
  try {
    const response = await fetch(`/api/labels/box/${boxId.value}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.success) {
      generatedLabel.value = data
      showToast('박스 라벨이 생성되었습니다.', 'success')
    } else {
      showToast(data.error || '라벨 생성에 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('Box label generation error:', error)
    showToast('라벨 생성 중 오류가 발생했습니다.', 'error')
  } finally {
    isGenerating.value = false
  }
}

const generateCustomLabel = async () => {
  isGenerating.value = true
  try {
    const requestData = {
      orderNumber: customLabel.orderNumber,
      recipientName: customLabel.recipientName,
      recipientAddress: customLabel.recipientAddress,
      packageType: customLabel.packageType
    }
    
    if (customLabel.isBoxLabel) {
      requestData.boxId = customLabel.boxId
      requestData.boxNumber = customLabel.boxNumber
      requestData.totalBoxes = customLabel.totalBoxes
    }
    
    const response = await fetch('/api/labels/custom', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(requestData)
    })
    
    const data = await response.json()
    if (data.success) {
      generatedLabel.value = data
      showToast('커스텀 라벨이 생성되었습니다.', 'success')
    } else {
      showToast(data.error || '라벨 생성에 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('Custom label generation error:', error)
    showToast('라벨 생성 중 오류가 발생했습니다.', 'error')
  } finally {
    isGenerating.value = false
  }
}

const generateBatchLabels = async () => {
  const orderNumbers = batchOrderNumbers.value.split('\n').filter(num => num.trim())
  if (orderNumbers.length === 0) return
  
  isGenerating.value = true
  batchResults.value = []
  
  try {
    // 각 주문번호에 대해 순차 생성
    for (const orderNum of orderNumbers) {
      try {
        const response = await fetch(`/api/labels/order/${orderNum.trim()}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        const data = await response.json()
        if (data.success) {
          batchResults.value.push(data)
        }
      } catch (error) {
        console.error(`Failed to generate label for ${orderNum}:`, error)
      }
    }
    
    showToast(`${batchResults.value.length}개의 라벨이 생성되었습니다.`, 'success')
  } catch (error) {
    console.error('Batch label generation error:', error)
    showToast('일괄 라벨 생성 중 오류가 발생했습니다.', 'error')
  } finally {
    isGenerating.value = false
  }
}

const processScannedQR = async () => {
  if (!scannedQRCode.value) return
  
  try {
    const response = await fetch('/api/labels/scan', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ qrCode: scannedQRCode.value })
    })
    
    const data = await response.json()
    if (data.success) {
      scanResult.value = data.scanResult
      showToast('QR 코드 스캔이 완료되었습니다.', 'success')
    } else {
      showToast(data.error || 'QR 코드 스캔에 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('QR scan error:', error)
    showToast('QR 코드 스캔 중 오류가 발생했습니다.', 'error')
  }
}

// 유틸리티 함수들
const selectBatchItem = (item: any) => {
  generatedLabel.value = item
}

const printLabel = () => {
  if (!generatedLabel.value) return
  
  const printWindow = window.open('', '_blank')
  if (printWindow) {
    printWindow.document.write(`
      <html>
        <head><title>라벨 인쇄</title></head>
        <body style="margin: 0; padding: 20px;">
          <img src="data:image/png;base64,${generatedLabel.value.labelImageBase64}" 
               style="width: 100%; height: auto;" />
        </body>
      </html>
    `)
    printWindow.document.close()
    printWindow.print()
  }
}

const downloadLabel = () => {
  if (!generatedLabel.value) return
  
  const link = document.createElement('a')
  link.href = `data:image/png;base64,${generatedLabel.value.labelImageBase64}`
  link.download = `${generatedLabel.value.orderNumber}_label.png`
  link.click()
}

const copyQRCode = async () => {
  if (!generatedLabel.value?.qrCode) return
  
  try {
    await navigator.clipboard.writeText(generatedLabel.value.qrCode)
    showToast('QR 코드가 클립보드에 복사되었습니다.', 'success')
  } catch (error) {
    showToast('QR 코드 복사에 실패했습니다.', 'error')
  }
}

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('ko-KR')
}
</script>

<style scoped>
.label-generator {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.label-controls {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.label-type-selection {
  display: flex;
  gap: 20px;
  margin: 16px 0;
}

.radio-option, .checkbox-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.label-form {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  min-height: 100px;
  resize: vertical;
}

.btn-primary, .btn-secondary, .btn-small {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
  background: #e5e7eb;
  color: #374151;
}

.batch-generation {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.label-result, .batch-results, .qr-scanner {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.label-preview {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  margin: 16px 0;
}

.label-image {
  max-width: 400px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
}

.label-info p {
  margin: 8px 0;
}

.label-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.batch-list {
  display: grid;
  gap: 8px;
}

.batch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f9fafb;
  border-radius: 4px;
}

.scanner-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.scan-result {
  background: #f3f4f6;
  border-radius: 4px;
  padding: 16px;
  margin-top: 16px;
}

.scan-result pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-overlay p {
  color: white;
  font-weight: 500;
}

@media (max-width: 768px) {
  .label-preview {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .label-actions {
    flex-direction: column;
  }
  
  .scanner-controls {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>