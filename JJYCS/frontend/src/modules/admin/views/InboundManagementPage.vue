<template>
  <div class="inbound-management">
    <!-- 페이지 헤더 -->
    <div class="page-header">
      <h1 class="page-title">입고확인 관리</h1>
      <p class="page-description">택배 송장번호를 스캔하거나 입력하여 입고확인을 처리합니다.</p>
    </div>

    <!-- 입고확인 플로우 -->
    <div class="inbound-flow">
      <!-- 단계 표시 -->
      <div class="flow-steps">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          :class="['step', { 'active': currentStep === index, 'completed': currentStep > index }]"
        >
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-title">{{ step.title }}</div>
        </div>
      </div>

      <!-- 단계별 콘텐츠 -->
      <div class="flow-content">
        
        <!-- 1단계: 송장번호 입력/스캔 -->
        <div v-if="currentStep === 0" class="step-content">
          <div class="scan-section">
            <h3>송장번호 입력</h3>
            
            <!-- 입력 방식 선택 -->
            <div class="input-method-tabs">
              <button 
                v-for="method in inputMethods" 
                :key="method.value"
                :class="['tab-button', { 'active': inputMethod === method.value }]"
                @click="inputMethod = method.value"
              >
                <i :class="method.icon"></i>
                {{ method.label }}
              </button>
            </div>

            <!-- 수동 입력 -->
            <div v-if="inputMethod === 'manual'" class="manual-input">
              <div class="form-row">
                <div class="form-group">
                  <label>택배사</label>
                  <select v-model="trackingForm.carrier" class="form-select">
                    <option value="">택배사를 선택하세요</option>
                    <option value="CJ">CJ대한통운</option>
                    <option value="HANJIN">한진택배</option>
                    <option value="LOTTE">롯데택배</option>
                    <option value="POST">우체국택배</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>송장번호</label>
                  <input 
                    v-model="trackingForm.trackingNumber" 
                    type="text" 
                    placeholder="송장번호를 입력하세요"
                    class="form-input"
                    @keyup.enter="validateTracking"
                  />
                </div>
              </div>
            </div>

            <!-- 카메라 스캔 -->
            <div v-if="inputMethod === 'camera'" class="camera-scan">
              <div class="camera-container">
                <video ref="videoElement" class="camera-video" autoplay></video>
                <div class="scan-overlay">
                  <div class="scan-frame"></div>
                  <p>송장번호를 카메라에 맞춰주세요</p>
                </div>
              </div>
              <div class="camera-controls">
                <button @click="startCamera" class="btn btn-primary">카메라 시작</button>
                <button @click="stopCamera" class="btn btn-secondary">카메라 중지</button>
                <button @click="captureImage" class="btn btn-success">촬영</button>
              </div>
            </div>

            <!-- 바코드 스캔 -->
            <div v-if="inputMethod === 'barcode'" class="barcode-scan">
              <div class="barcode-reader" ref="barcodeReader">
                <p>바코드 스캔을 준비 중입니다...</p>
              </div>
              <div class="barcode-controls">
                <button @click="startBarcodeScanner" class="btn btn-primary">바코드 스캔 시작</button>
                <button @click="stopBarcodeScanner" class="btn btn-secondary">스캔 중지</button>
              </div>
            </div>

            <!-- 검증 버튼 -->
            <div class="action-buttons">
              <button 
                @click="validateTracking" 
                :disabled="!canValidate || isValidating"
                class="btn btn-primary btn-lg"
              >
                <i class="fas fa-search"></i>
                {{ isValidating ? '검증 중...' : '송장번호 검증' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 2단계: 검증 결과 및 정보 확인 -->
        <div v-if="currentStep === 1" class="step-content">
          <div class="validation-result">
            <h3>검증 결과</h3>
            
            <!-- 검증 성공 -->
            <div v-if="validationResult?.isValid" class="validation-success">
              <div class="success-header">
                <i class="fas fa-check-circle"></i>
                <span>유효한 송장번호입니다</span>
              </div>

              <!-- 기본 배송 정보 -->
              <div v-if="validationResult.basicInfo" class="shipping-info">
                <h4>배송 정보</h4>
                <div class="info-grid">
                  <div class="info-item">
                    <label>송장번호</label>
                    <span>{{ validationResult.basicInfo.trackingNumber }}</span>
                  </div>
                  <div class="info-item">
                    <label>택배사</label>
                    <span>{{ validationResult.basicInfo.carrierName }}</span>
                  </div>
                  <div class="info-item">
                    <label>현재 상태</label>
                    <span class="status-badge">{{ validationResult.basicInfo.status }}</span>
                  </div>
                  <div class="info-item">
                    <label>최종 위치</label>
                    <span>{{ validationResult.basicInfo.lastLocation || '정보 없음' }}</span>
                  </div>
                  <div class="info-item">
                    <label>수취인</label>
                    <span>{{ validationResult.basicInfo.recipientName || '정보 없음' }}</span>
                  </div>
                  <div class="info-item">
                    <label>연락처</label>
                    <span>{{ validationResult.basicInfo.recipientPhone || '정보 없음' }}</span>
                  </div>
                </div>
              </div>

              <!-- 매칭된 주문 정보 -->
              <div v-if="validationResult.matchedOrder" class="matched-order">
                <h4>매칭된 주문 정보</h4>
                <div class="order-card">
                  <div class="order-header">
                    <span class="order-number">{{ validationResult.matchedOrder.orderNumber }}</span>
                    <span :class="['order-status', validationResult.matchedOrder.orderStatus.toLowerCase()]">
                      {{ getOrderStatusLabel(validationResult.matchedOrder.orderStatus) }}
                    </span>
                  </div>
                  <div class="order-details">
                    <p><strong>고객:</strong> {{ validationResult.matchedOrder.customerName }}</p>
                    <p><strong>연락처:</strong> {{ validationResult.matchedOrder.customerPhone }}</p>
                    <p><strong>주문일:</strong> {{ formatDate(validationResult.matchedOrder.orderDate) }}</p>
                    <p><strong>품목 수:</strong> {{ validationResult.matchedOrder.itemCount }}개</p>
                    <p v-if="validationResult.matchedOrder.specialRequests">
                      <strong>특수 요청:</strong> {{ validationResult.matchedOrder.specialRequests }}
                    </p>
                  </div>
                  
                  <!-- 특수 처리 플래그 -->
                  <div class="special-flags">
                    <span v-if="validationResult.matchedOrder.requiresInspection" class="flag inspection">
                      <i class="fas fa-search"></i> 검수 필요
                    </span>
                    <span v-if="validationResult.matchedOrder.requiresPhotos" class="flag photos">
                      <i class="fas fa-camera"></i> 사진 촬영
                    </span>
                  </div>
                </div>
              </div>

              <!-- 매칭된 주문이 없는 경우 -->
              <div v-else class="no-match-warning">
                <i class="fas fa-exclamation-triangle"></i>
                <p>매칭되는 주문을 찾을 수 없습니다. 수동으로 주문을 선택하거나 새로운 입고 항목으로 처리할 수 있습니다.</p>
              </div>
            </div>

            <!-- 검증 실패 -->
            <div v-else class="validation-failed">
              <div class="error-header">
                <i class="fas fa-times-circle"></i>
                <span>송장번호 검증 실패</span>
              </div>
              <div class="error-list">
                <p v-for="error in validationResult?.errors" :key="error" class="error-message">
                  {{ error }}
                </p>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="action-buttons">
              <button @click="goToPreviousStep" class="btn btn-secondary">
                <i class="fas fa-arrow-left"></i> 이전 단계
              </button>
              <button 
                v-if="validationResult?.isValid" 
                @click="proceedToConfirmation"
                class="btn btn-primary"
              >
                입고확인 진행 <i class="fas fa-arrow-right"></i>
              </button>
              <button v-else @click="goToPreviousStep" class="btn btn-warning">
                다시 시도
              </button>
            </div>
          </div>
        </div>

        <!-- 3단계: 입고확인 정보 입력 -->
        <div v-if="currentStep === 2" class="step-content">
          <div class="confirmation-form">
            <h3>입고확인 정보 입력</h3>
            
            <form @submit.prevent="submitConfirmation">
              <!-- 실제 측정 정보 -->
              <div class="form-section">
                <h4>실제 측정 정보</h4>
                <div class="form-grid">
                  <div class="form-group">
                    <label>실제 중량 (kg)</label>
                    <input 
                      v-model.number="confirmationForm.actualWeight" 
                      type="number" 
                      step="0.1" 
                      min="0"
                      placeholder="0.0"
                      class="form-input"
                    />
                  </div>
                  <div class="form-group">
                    <label>가로 (cm)</label>
                    <input 
                      v-model.number="confirmationForm.actualWidth" 
                      type="number" 
                      step="0.1" 
                      min="0"
                      placeholder="0.0"
                      class="form-input"
                    />
                  </div>
                  <div class="form-group">
                    <label>세로 (cm)</label>
                    <input 
                      v-model.number="confirmationForm.actualHeight" 
                      type="number" 
                      step="0.1" 
                      min="0"
                      placeholder="0.0"
                      class="form-input"
                    />
                  </div>
                  <div class="form-group">
                    <label>높이 (cm)</label>
                    <input 
                      v-model.number="confirmationForm.actualDepth" 
                      type="number" 
                      step="0.1" 
                      min="0"
                      placeholder="0.0"
                      class="form-input"
                    />
                  </div>
                </div>
              </div>

              <!-- 패키지 상태 -->
              <div class="form-section">
                <h4>패키지 상태</h4>
                <div class="form-group">
                  <label>상태</label>
                  <select v-model="confirmationForm.packageCondition" class="form-select" required>
                    <option value="">패키지 상태를 선택하세요</option>
                    <option value="GOOD">양호</option>
                    <option value="DAMAGED">손상</option>
                    <option value="WET">습손</option>
                    <option value="OPENED">개봉</option>
                  </select>
                </div>
                
                <!-- 손상 정보 (손상된 경우) -->
                <div v-if="confirmationForm.packageCondition === 'DAMAGED'" class="damage-info">
                  <div class="form-group">
                    <label>손상 설명</label>
                    <textarea 
                      v-model="confirmationForm.damageDescription"
                      class="form-textarea"
                      rows="3"
                      placeholder="손상 상태를 자세히 설명해주세요"
                    ></textarea>
                  </div>
                  <div class="form-group">
                    <label>손상 사진</label>
                    <div class="photo-upload">
                      <button type="button" @click="takeDamagePhoto" class="btn btn-secondary">
                        <i class="fas fa-camera"></i> 사진 촬영
                      </button>
                      <div v-if="confirmationForm.damagePhotoPaths?.length" class="photo-list">
                        <div 
                          v-for="(photo, index) in confirmationForm.damagePhotoPaths" 
                          :key="index"
                          class="photo-item"
                        >
                          <img :src="photo" alt="손상 사진" class="photo-thumb" />
                          <button @click="removeDamagePhoto(index)" class="remove-btn">×</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 창고 위치 -->
              <div class="form-section">
                <h4>창고 위치</h4>
                <div class="form-grid">
                  <div class="form-group">
                    <label>보관 구역</label>
                    <select v-model="confirmationForm.storageZone" class="form-select">
                      <option value="">구역 선택</option>
                      <option value="A">A구역 (전자제품)</option>
                      <option value="B">B구역 (의류)</option>
                      <option value="C">C구역 (식품)</option>
                      <option value="D">D구역 (화학제품)</option>
                      <option value="F">F구역 (냉동)</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label>선반 위치</label>
                    <input 
                      v-model="confirmationForm.storageLocation" 
                      type="text" 
                      placeholder="예: A-01-03"
                      class="form-input"
                      required
                    />
                  </div>
                  <div class="form-group">
                    <label>선반 코드</label>
                    <input 
                      v-model="confirmationForm.shelfCode" 
                      type="text" 
                      placeholder="예: S001"
                      class="form-input"
                    />
                  </div>
                </div>
              </div>

              <!-- 특수 처리 옵션 -->
              <div class="form-section">
                <h4>특수 처리 옵션</h4>
                <div class="checkbox-grid">
                  <label class="checkbox-label">
                    <input v-model="confirmationForm.requiresInspection" type="checkbox" />
                    <span class="checkbox-text">검수 필요</span>
                  </label>
                  <label class="checkbox-label">
                    <input v-model="confirmationForm.requiresRepacking" type="checkbox" />
                    <span class="checkbox-text">재포장 필요</span>
                  </label>
                  <label class="checkbox-label">
                    <input v-model="confirmationForm.requiresPhotos" type="checkbox" />
                    <span class="checkbox-text">사진 촬영 필요</span>
                  </label>
                  <label class="checkbox-label">
                    <input v-model="confirmationForm.isFragile" type="checkbox" />
                    <span class="checkbox-text">파손 주의</span>
                  </label>
                  <label class="checkbox-label">
                    <input v-model="confirmationForm.isHazardous" type="checkbox" />
                    <span class="checkbox-text">위험 물품</span>
                  </label>
                </div>
              </div>

              <!-- 관리자 메모 -->
              <div class="form-section">
                <h4>관리자 메모</h4>
                <div class="form-group">
                  <textarea 
                    v-model="confirmationForm.adminNotes"
                    class="form-textarea"
                    rows="3"
                    placeholder="입고 처리 시 참고할 내용을 입력하세요"
                  ></textarea>
                </div>
              </div>

              <!-- 다음 처리 단계 -->
              <div class="form-section">
                <h4>다음 처리 단계</h4>
                <div class="form-grid">
                  <div class="form-group">
                    <label>다음 액션</label>
                    <select v-model="confirmationForm.nextAction" class="form-select">
                      <option value="INSPECT">검수</option>
                      <option value="REPACK">재포장</option>
                      <option value="SHIP">출고 대기</option>
                      <option value="HOLD">보류</option>
                      <option value="RETURN">반송</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label>담당자</label>
                    <select v-model="confirmationForm.assignedTo" class="form-select">
                      <option value="">담당자 선택</option>
                      <option value="admin">관리자</option>
                      <option value="warehouse1">창고직원1</option>
                      <option value="warehouse2">창고직원2</option>
                    </select>
                  </div>
                </div>
              </div>

              <!-- 고객 알림 -->
              <div class="form-section">
                <h4>고객 알림</h4>
                <label class="checkbox-label">
                  <input v-model="confirmationForm.notifyCustomer" type="checkbox" />
                  <span class="checkbox-text">고객에게 입고 완료 알림 발송</span>
                </label>
                <div v-if="confirmationForm.notifyCustomer" class="form-group">
                  <label>알림 메시지 (선택)</label>
                  <textarea 
                    v-model="confirmationForm.notificationMessage"
                    class="form-textarea"
                    rows="2"
                    placeholder="기본 메시지를 사용하려면 비워두세요"
                  ></textarea>
                </div>
              </div>

              <!-- 액션 버튼 -->
              <div class="form-actions">
                <button type="button" @click="goToPreviousStep" class="btn btn-secondary">
                  <i class="fas fa-arrow-left"></i> 이전 단계
                </button>
                <button type="submit" :disabled="isSubmitting" class="btn btn-success btn-lg">
                  <i class="fas fa-check"></i>
                  {{ isSubmitting ? '처리 중...' : '입고확인 완료' }}
                </button>
              </div>
            </form>
          </div>
        </div>

        <!-- 4단계: 완료 -->
        <div v-if="currentStep === 3" class="step-content">
          <div class="completion-result">
            <div class="success-animation">
              <i class="fas fa-check-circle"></i>
            </div>
            <h3>입고확인이 완료되었습니다!</h3>
            <p>송장번호 {{ trackingForm.trackingNumber }}의 입고확인이 성공적으로 처리되었습니다.</p>
            
            <!-- 처리 요약 -->
            <div class="completion-summary">
              <h4>처리 요약</h4>
              <div class="summary-grid">
                <div class="summary-item">
                  <label>송장번호</label>
                  <span>{{ trackingForm.trackingNumber }}</span>
                </div>
                <div class="summary-item">
                  <label>택배사</label>
                  <span>{{ getCarrierName(trackingForm.carrier) }}</span>
                </div>
                <div class="summary-item">
                  <label>보관 위치</label>
                  <span>{{ confirmationForm.storageZone }}-{{ confirmationForm.storageLocation }}</span>
                </div>
                <div class="summary-item">
                  <label>다음 처리</label>
                  <span>{{ getNextActionLabel(confirmationForm.nextAction) }}</span>
                </div>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="action-buttons">
              <button @click="resetForm" class="btn btn-primary">
                <i class="fas fa-plus"></i> 새로운 입고확인
              </button>
              <button @click="printLabel" class="btn btn-secondary">
                <i class="fas fa-print"></i> 라벨 출력
              </button>
              <button @click="viewHistory" class="btn btn-outline">
                <i class="fas fa-history"></i> 처리 이력
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="isValidating || isSubmitting" class="loading-overlay">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>{{ isValidating ? '송장번호 검증 중...' : '입고확인 처리 중...' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { useBarcodeScanner } from '@/composables/useBarcodeScanner'
import { useOcrScanner } from '@/composables/useOcrScanner'
import api from '@/utils/api'

// Router & Toast
const router = useRouter()
const toast = useToast()

// Barcode & OCR scanners
const barcodeScanner = useBarcodeScanner()
const ocrScanner = useOcrScanner()

// Reactive data
const currentStep = ref(0)
const inputMethod = ref('manual')
const isValidating = ref(false)
const isSubmitting = ref(false)

// 단계 정의
const steps = [
  { title: '송장번호 입력/스캔' },
  { title: '검증 결과 확인' },
  { title: '입고확인 정보 입력' },
  { title: '완료' }
]

// 입력 방법 옵션
const inputMethods = [
  { value: 'manual', label: '수동 입력', icon: 'fas fa-keyboard' },
  { value: 'camera', label: '카메라 스캔', icon: 'fas fa-camera' },
  { value: 'barcode', label: '바코드 스캔', icon: 'fas fa-barcode' }
]

// 폼 데이터
const trackingForm = ref({
  trackingNumber: '',
  carrier: '',
  scanMethod: 'manual',
  scannedBy: 'admin',
  scannedAt: null
})

const validationResult = ref(null)

const confirmationForm = ref({
  trackingNumber: '',
  carrier: '',
  orderId: null,
  actualWeight: null,
  actualWidth: null,
  actualHeight: null,
  actualDepth: null,
  packageCondition: '',
  damageDescription: '',
  damagePhotoPaths: [],
  storageLocation: '',
  storageZone: '',
  shelfCode: '',
  requiresInspection: false,
  requiresRepacking: false,
  requiresPhotos: false,
  isFragile: false,
  isHazardous: false,
  adminNotes: '',
  processedBy: 'admin',
  nextAction: 'INSPECT',
  assignedTo: '',
  notifyCustomer: true,
  notificationMessage: ''
})

// Computed
const canValidate = computed(() => {
  return trackingForm.value.trackingNumber.trim() && trackingForm.value.carrier
})

// Camera refs
const videoElement = ref(null)
const barcodeReader = ref(null)
let mediaStream = null

// Methods
const validateTracking = async () => {
  if (!canValidate.value) {
    toast.error('송장번호와 택배사를 모두 입력해주세요.')
    return
  }

  isValidating.value = true
  
  try {
    const request = {
      ...trackingForm.value,
      scanMethod: inputMethod.value,
      scannedAt: new Date().toISOString()
    }

    const response = await api.post('/admin/inbound/validate-tracking', request)
    
    if (response.data.success) {
      validationResult.value = response.data.data
      currentStep.value = 1
      
      if (validationResult.value.isValid) {
        toast.success('송장번호 검증이 완료되었습니다.')
      } else {
        toast.warning('송장번호 검증에 실패했습니다.')
      }
    } else {
      toast.error(response.data.message || '검증 중 오류가 발생했습니다.')
    }
  } catch (error) {
    console.error('Tracking validation error:', error)
    toast.error('송장번호 검증 중 오류가 발생했습니다.')
  } finally {
    isValidating.value = false
  }
}

const proceedToConfirmation = () => {
  // 검증 결과를 확인 폼에 복사
  confirmationForm.value.trackingNumber = trackingForm.value.trackingNumber
  confirmationForm.value.carrier = trackingForm.value.carrier
  
  if (validationResult.value?.matchedOrder) {
    confirmationForm.value.orderId = validationResult.value.matchedOrder.orderId
    confirmationForm.value.requiresInspection = validationResult.value.matchedOrder.requiresInspection
    confirmationForm.value.requiresPhotos = validationResult.value.matchedOrder.requiresPhotos
  }
  
  currentStep.value = 2
}

const submitConfirmation = async () => {
  if (!confirmationForm.value.packageCondition || !confirmationForm.value.storageLocation) {
    toast.error('필수 항목을 모두 입력해주세요.')
    return
  }

  isSubmitting.value = true
  
  try {
    const response = await api.post('/admin/inbound/confirm', {
      ...confirmationForm.value,
      processedAt: new Date().toISOString()
    })
    
    if (response.data.success) {
      toast.success('입고확인이 완료되었습니다.')
      currentStep.value = 3
    } else {
      toast.error(response.data.message || '입고확인 처리 중 오류가 발생했습니다.')
    }
  } catch (error) {
    console.error('Inbound confirmation error:', error)
    toast.error('입고확인 처리 중 오류가 발생했습니다.')
  } finally {
    isSubmitting.value = false
  }
}

const goToPreviousStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const resetForm = () => {
  currentStep.value = 0
  trackingForm.value = {
    trackingNumber: '',
    carrier: '',
    scanMethod: 'manual',
    scannedBy: 'admin',
    scannedAt: null
  }
  validationResult.value = null
  confirmationForm.value = {
    trackingNumber: '',
    carrier: '',
    orderId: null,
    actualWeight: null,
    actualWidth: null,
    actualHeight: null,
    actualDepth: null,
    packageCondition: '',
    damageDescription: '',
    damagePhotoPaths: [],
    storageLocation: '',
    storageZone: '',
    shelfCode: '',
    requiresInspection: false,
    requiresRepacking: false,
    requiresPhotos: false,
    isFragile: false,
    isHazardous: false,
    adminNotes: '',
    processedBy: 'admin',
    nextAction: 'INSPECT',
    assignedTo: '',
    notifyCustomer: true,
    notificationMessage: ''
  }
}

// Camera methods
const startCamera = async () => {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ 
      video: { 
        facingMode: 'environment',
        width: { ideal: 1280 },
        height: { ideal: 720 }
      } 
    })
    
    if (videoElement.value) {
      videoElement.value.srcObject = mediaStream
    }
    
    toast.success('카메라가 시작되었습니다.')
  } catch (error) {
    console.error('Camera start error:', error)
    toast.error('카메라를 시작할 수 없습니다.')
  }
}

const stopCamera = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
  
  if (videoElement.value) {
    videoElement.value.srcObject = null
  }
  
  toast.info('카메라가 중지되었습니다.')
}

const captureImage = () => {
  // TODO: OCR을 통한 송장번호 추출 구현
  toast.info('이미지 인식 기능은 개발 중입니다.')
}

// Barcode scanner methods
const startBarcodeScanner = () => {
  // TODO: 바코드 스캐너 라이브러리 연동
  toast.info('바코드 스캔 기능은 개발 중입니다.')
}

const stopBarcodeScanner = () => {
  toast.info('바코드 스캔이 중지되었습니다.')
}

// Helper methods
const getOrderStatusLabel = (status: string) => {
  const statusLabels = {
    'PENDING': '접수 대기',
    'CONFIRMED': '접수 완료',
    'WAREHOUSE_IN': '입고 완료',
    'PROCESSING': '처리 중',
    'READY_TO_SHIP': '출고 준비',
    'SHIPPED': '출고 완료',
    'DELIVERED': '배송 완료',
    'CANCELLED': '취소',
    'RETURNED': '반송'
  }
  return statusLabels[status] || status
}

const getCarrierName = (carrier: string) => {
  const carrierNames = {
    'CJ': 'CJ대한통운',
    'HANJIN': '한진택배',
    'LOTTE': '롯데택배',
    'POST': '우체국택배'
  }
  return carrierNames[carrier] || carrier
}

const getNextActionLabel = (action: string) => {
  const actionLabels = {
    'INSPECT': '검수',
    'REPACK': '재포장',
    'SHIP': '출고 대기',
    'HOLD': '보류',
    'RETURN': '반송'
  }
  return actionLabels[action] || action
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const takeDamagePhoto = () => {
  // TODO: 카메라를 통한 손상 사진 촬영 구현
  toast.info('사진 촬영 기능은 개발 중입니다.')
}

const removeDamagePhoto = (index: number) => {
  confirmationForm.value.damagePhotoPaths.splice(index, 1)
}

const printLabel = () => {
  toast.info('라벨 출력 기능은 개발 중입니다.')
}

const viewHistory = () => {
  router.push('/admin/inbound/history')
}

// Lifecycle
onMounted(() => {
  // 초기화 코드
})

onUnmounted(() => {
  // 카메라 정리
  if (mediaStream) {
    stopCamera()
  }
})
</script>

<style scoped>
.inbound-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
  text-align: center;
}

.page-header .page-title {
  font-size: 2rem;
  font-weight: bold;
  color: #1a365d;
  margin-bottom: 0.5rem;
}

.page-header .page-description {
  color: #718096;
  font-size: 1rem;
}

.inbound-flow {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.flow-steps {
  display: flex;
  background: linear-gradient(90deg, #1e40af 0%, #2563eb 100%);
  padding: 1rem 0;
}

.flow-steps .step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  position: relative;
}

.flow-steps .step:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -1px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 10px solid rgba(255, 255, 255, 0.3);
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
}

.flow-steps .step.active .step-number {
  background: #3b82f6;
  transform: scale(1.1);
}

.flow-steps .step.completed .step-number {
  background: #1d4ed8;
}

.flow-steps .step.completed .step-number::after {
  content: '✓';
  font-size: 0.8rem;
}

.flow-steps .step .step-number {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.flow-steps .step .step-title {
  font-size: 0.9rem;
  text-align: center;
}

.flow-content {
  padding: 2rem;
}

.step-content {
  min-height: 500px;
}

.scan-section h3 {
  color: #2d3748;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.input-method-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #e2e8f0;
}

.input-method-tabs .tab-button {
  padding: 0.75rem 1.5rem;
  border: none;
  background: transparent;
  color: #718096;
  cursor: pointer;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease;
  font-weight: 500;
}

.input-method-tabs .tab-button i {
  margin-right: 0.5rem;
}

.input-method-tabs .tab-button.active {
  background: #1e40af;
  color: white;
  transform: translateY(-2px);
}

.input-method-tabs .tab-button:hover:not(.active) {
  background: #edf2f7;
  color: #2d3748;
}

.manual-input .form-row {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 1rem;
  margin-bottom: 2rem;
}

.camera-scan .camera-container {
  position: relative;
  max-width: 640px;
  margin: 0 auto 1rem;
  border-radius: 12px;
  overflow: hidden;
}

.camera-scan .camera-container .camera-video {
  width: 100%;
  height: 320px;
  object-fit: cover;
  background: #000;
}

.camera-scan .camera-container .scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.camera-scan .camera-container .scan-overlay .scan-frame {
  width: 200px;
  height: 100px;
  border: 2px solid #48bb78;
  border-radius: 8px;
  position: relative;
}

.camera-scan .camera-container .scan-overlay .scan-frame::before,
.camera-scan .camera-container .scan-overlay .scan-frame::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #48bb78;
}

.camera-scan .camera-container .scan-overlay .scan-frame::before {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.camera-scan .camera-container .scan-overlay .scan-frame::after {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
}

.camera-scan .camera-container .scan-overlay p {
  color: white;
  background: rgba(0, 0, 0, 0.7);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.camera-scan .camera-controls {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.barcode-scan .barcode-reader {
  height: 300px;
  background: #f7fafc;
  border: 2px dashed #cbd5e0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.barcode-scan .barcode-reader p {
  color: #718096;
  font-size: 1.1rem;
}

.barcode-scan .barcode-controls {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.validation-result h3 {
  color: #2d3748;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.validation-success .success-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #38a169;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.validation-success .success-header i {
  font-size: 1.5rem;
}

.validation-failed .error-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #e53e3e;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.validation-failed .error-header i {
  font-size: 1.5rem;
}

.validation-failed .error-list {
  background: #fed7d7;
  border: 1px solid #feb2b2;
  border-radius: 8px;
  padding: 1rem;
}

.validation-failed .error-list .error-message {
  color: #c53030;
  margin: 0.5rem 0;
}

.validation-failed .error-list .error-message:last-child {
  margin-bottom: 0;
}

.shipping-info,
.matched-order {
  background: #f7fafc;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.shipping-info h4,
.matched-order h4 {
  color: #2d3748;
  font-size: 1.2rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.info-grid .info-item {
  display: flex;
  flex-direction: column;
}

.info-grid .info-item label {
  font-size: 0.9rem;
  color: #718096;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.info-grid .info-item span {
  color: #2d3748;
  font-weight: 500;
}

.info-grid .info-item .status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #48bb78;
  color: white;
  border-radius: 20px;
  font-size: 0.8rem;
  width: fit-content;
}

.order-card .order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-card .order-header .order-number {
  font-size: 1.1rem;
  font-weight: bold;
  color: #2d3748;
}

.order-card .order-header .order-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.order-card .order-header .order-status.pending {
  background: #fed7b7;
  color: #c05621;
}

.order-card .order-header .order-status.confirmed {
  background: #bee3f8;
  color: #2c5282;
}

.order-card .order-header .order-status.warehouse_in {
  background: #c6f6d5;
  color: #276749;
}

.order-card .order-header .order-status.processing {
  background: #fbb6ce;
  color: #97266d;
}

.order-card .order-details p {
  margin: 0.5rem 0;
  color: #4a5568;
}

.order-card .order-details p strong {
  color: #2d3748;
  font-weight: 600;
  margin-right: 0.5rem;
}

.order-card .special-flags {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.order-card .special-flags .flag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.order-card .special-flags .flag.inspection {
  background: #fef5e7;
  color: #c05621;
}

.order-card .special-flags .flag.photos {
  background: #e6fffa;
  color: #285e61;
}

.no-match-warning {
  background: #fef5e7;
  border: 1px solid #f6e05e;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.no-match-warning i {
  color: #d69e2e;
  font-size: 1.2rem;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.no-match-warning p {
  color: #744210;
  margin: 0;
}

.confirmation-form h3 {
  color: #2d3748;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.form-section {
  background: #f7fafc;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-section h4 {
  color: #2d3748;
  font-size: 1.2rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-group .form-input,
.form-group .form-select,
.form-group .form-textarea {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-group .form-input:focus,
.form-group .form-select:focus,
.form-group .form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group .form-textarea {
  resize: vertical;
  min-height: 80px;
}

.damage-info {
  border-left: 4px solid #f56565;
  padding-left: 1rem;
  margin-left: 1rem;
  margin-top: 1rem;
}

.photo-upload .photo-list {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.photo-upload .photo-list .photo-item {
  position: relative;
}

.photo-upload .photo-list .photo-item .photo-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
}

.photo-upload .photo-list .photo-item .remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f56565;
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.photo-upload .photo-list .photo-item .remove-btn:hover {
  background: #e53e3e;
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.checkbox-grid .checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-grid .checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #1e40af;
}

.checkbox-grid .checkbox-label .checkbox-text {
  color: #4a5568;
  font-weight: 500;
}

.completion-result {
  text-align: center;
  padding: 2rem;
}

.completion-result .success-animation {
  margin-bottom: 1.5rem;
}

.completion-result .success-animation i {
  font-size: 4rem;
  color: #38a169;
  animation: bounce 2s infinite;
}

.completion-result h3 {
  color: #2d3748;
  font-size: 1.8rem;
  margin-bottom: 1rem;
}

.completion-result > p {
  color: #718096;
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.completion-summary {
  background: #f7fafc;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
}

.completion-summary h4 {
  color: #2d3748;
  font-size: 1.2rem;
  margin-bottom: 1rem;
  font-weight: 600;
  text-align: center;
}

.completion-summary .summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.completion-summary .summary-grid .summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.completion-summary .summary-grid .summary-item label {
  font-size: 0.9rem;
  color: #718096;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.completion-summary .summary-grid .summary-item span {
  color: #2d3748;
  font-weight: 600;
  font-size: 1rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  margin-top: 2rem;
  align-items: center;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn.btn-primary {
  background: #1e40af;
  color: white;
}

.btn.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn.btn-secondary {
  background: #718096;
  color: white;
}

.btn.btn-secondary:hover:not(:disabled) {
  background: #4a5568;
  transform: translateY(-1px);
}

.btn.btn-success {
  background: #48bb78;
  color: white;
}

.btn.btn-success:hover:not(:disabled) {
  background: #38a169;
  transform: translateY(-1px);
}

.btn.btn-warning {
  background: #ed8936;
  color: white;
}

.btn.btn-warning:hover:not(:disabled) {
  background: #dd6b20;
  transform: translateY(-1px);
}

.btn.btn-outline {
  background: transparent;
  color: #1e40af;
  border: 2px solid #1e40af;
}

.btn.btn-outline:hover:not(:disabled) {
  background: #1e40af;
  color: white;
  transform: translateY(-1px);
}

.btn.btn-lg {
  padding: 1rem 2rem;
  font-size: 1.1rem;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-overlay .loading-spinner {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
}

.loading-overlay .loading-spinner i {
  font-size: 2rem;
  color: #1e40af;
  margin-bottom: 1rem;
}

.loading-overlay .loading-spinner p {
  color: #4a5568;
  font-weight: 500;
  margin: 0;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@media (max-width: 768px) {
  .inbound-management {
    padding: 1rem;
    margin: 0;
    max-width: 100%;
  }
  
  .page-header {
    text-align: left;
    margin-bottom: 1.5rem;
  }
  
  .page-header .page-title {
    font-size: 1.5rem;
  }
  
  .flow-steps {
    padding: 0.75rem 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .flow-steps .step {
    min-width: 80px;
    flex-shrink: 0;
  }
  
  .flow-steps .step .step-number {
    width: 1.5rem;
    height: 1.5rem;
    font-size: 0.75rem;
  }
  
  .flow-steps .step .step-title {
    font-size: 0.7rem;
    line-height: 1.2;
    padding: 0 0.25rem;
  }
  
  .flow-content {
    padding: 1rem;
  }
  
  .input-method-tabs {
    flex-direction: column;
    gap: 0.25rem;
    margin-bottom: 1.5rem;
    border: none;
    background: #f8fafc;
    padding: 0.5rem;
    border-radius: 8px;
  }
  
  .input-method-tabs .tab-button {
    border-radius: 6px;
    padding: 0.75rem 1rem;
    text-align: center;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .manual-input .form-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.75rem;
    position: sticky;
    bottom: 0;
    background: white;
    padding: 1rem;
    border-top: 1px solid #e2e8f0;
    margin: 0 -1rem -1rem;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
    position: sticky;
    bottom: 0;
    background: white;
    padding: 1rem;
    border-top: 1px solid #e2e8f0;
    margin: 1rem -1rem -1rem;
  }
  
  .btn {
    min-height: 48px;
    font-size: 1rem;
  }
  
  .camera-scan .camera-container {
    max-width: 100%;
    margin-bottom: 1rem;
  }
  
  .camera-controls,
  .barcode-controls {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .order-card {
    margin-bottom: 1rem;
  }
  
  .order-card .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .checkbox-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .completion-summary .summary-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .photo-upload .photo-list {
    gap: 0.5rem;
  }
  
  .photo-upload .photo-list .photo-item .photo-thumb {
    width: 60px;
    height: 60px;
  }
}

/* 아주 작은 화면 (iPhone SE 등) */
@media (max-width: 375px) {
  .page-header .page-title {
    font-size: 1.25rem;
  }
  
  .flow-steps .step {
    min-width: 70px;
  }
  
  .flow-steps .step .step-title {
    font-size: 0.625rem;
  }
  
  .form-section {
    padding: 1rem;
  }
  
  .btn {
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
  }
  
  .camera-scan .camera-container .scan-overlay p {
    font-size: 0.8rem;
    padding: 0.25rem 0.75rem;
  }
  
  .input-method-tabs .tab-button {
    padding: 0.625rem 0.75rem;
    font-size: 0.85rem;
  }
}

/* 가로 모드 최적화 */
@media (max-width: 768px) and (orientation: landscape) {
  .flow-steps {
    padding: 0.5rem 0;
  }
  
  .flow-steps .step .step-number {
    width: 1.25rem;
    height: 1.25rem;
    font-size: 0.7rem;
  }
  
  .flow-steps .step .step-title {
    font-size: 0.6rem;
  }
  
  .form-section {
    margin-bottom: 1rem;
  }
  
  .action-buttons,
  .form-actions {
    flex-direction: row;
    position: static;
    margin: 1rem 0;
    border-top: none;
    padding: 0;
  }
}

/* 터치 디바이스 최적화 */
@media (hover: none) and (pointer: coarse) {
  .btn,
  .tab-button,
  .form-input,
  .form-select,
  .form-textarea {
    min-height: 44px;
  }
  
  .checkbox-grid .checkbox-label input[type="checkbox"] {
    width: 24px;
    height: 24px;
    margin-right: 0.5rem;
  }
  
  .flow-steps .step .step-number {
    min-width: 44px;
    min-height: 44px;
  }
  
  .input-method-tabs .tab-button {
    min-height: 48px;
  }
  
  /* 스와이프 가능한 영역 표시 */
  .flow-steps::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 20px;
    background: linear-gradient(to left, rgba(30, 64, 175, 0.1), transparent);
    pointer-events: none;
  }
  
  /* 터치 피드백 강화 */
  .btn:active {
    transform: scale(0.98);
  }
  
  .tab-button:active {
    transform: scale(0.95);
  }
}
</style>