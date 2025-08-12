<template>
  <div class="sync-view">
    <div class="sync-container">
      <!-- Header -->
      <div class="page-header">
        <h1 class="page-title">배송 정보 동기화</h1>
        <p class="page-subtitle">외부 배송업체와 배송 정보를 동기화합니다</p>
      </div>

      <!-- Sync Status -->
      <div class="sync-status">
        <div class="status-card">
          <div class="status-header">
            <h2 class="status-title">동기화 상태</h2>
            <span class="last-sync">마지막 동기화: {{ lastSyncTime }}</span>
          </div>
          <div class="status-indicator" :class="syncStatusClass">
            <component :is="syncStatusIcon" class="status-icon" />
            <span class="status-text">{{ syncStatusText }}</span>
          </div>
        </div>
      </div>

      <!-- Manual Sync -->
      <div class="manual-sync">
        <div class="sync-card">
          <h2 class="card-title">수동 동기화</h2>
          <p class="card-description">특정 배송업체 또는 전체 배송 정보를 수동으로 동기화할 수 있습니다.</p>
          
          <div class="sync-form">
            <div class="form-group">
              <label class="form-label">배송업체 선택</label>
              <select v-model="selectedCarrier" class="form-select">
                <option value="">전체 업체</option>
                <option value="ems">EMS</option>
                <option value="dhl">DHL</option>
                <option value="fedex">FedEx</option>
                <option value="ups">UPS</option>
                <option value="tnt">TNT</option>
                <option value="korea_post">우체국택배</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">동기화 옵션</label>
              <div class="option-group">
                <label class="option-item">
                  <input v-model="syncOptions.updateStatus" type="checkbox" />
                  <span>배송 상태 업데이트</span>
                </label>
                <label class="option-item">
                  <input v-model="syncOptions.updateLocation" type="checkbox" />
                  <span>위치 정보 업데이트</span>
                </label>
                <label class="option-item">
                  <input v-model="syncOptions.updateEvents" type="checkbox" />
                  <span>배송 이벤트 업데이트</span>
                </label>
              </div>
            </div>
            
            <button 
              type="button" 
              class="sync-btn"
              @click="startManualSync"
              :disabled="syncing"
            >
              <LoadingSpinner v-if="syncing" size="small" />
              <ArrowPathIcon v-else class="btn-icon" />
              {{ syncing ? '동기화 중...' : '동기화 시작' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Auto Sync Settings -->
      <div class="auto-sync">
        <div class="sync-card">
          <h2 class="card-title">자동 동기화 설정</h2>
          <p class="card-description">배송 정보를 자동으로 동기화하는 주기를 설정합니다.</p>
          
          <div class="settings-form">
            <div class="form-group">
              <label class="form-label">동기화 주기</label>
              <select v-model="autoSyncInterval" class="form-select">
                <option value="0">비활성화</option>
                <option value="5">5분마다</option>
                <option value="15">15분마다</option>
                <option value="30">30분마다</option>
                <option value="60">1시간마다</option>
                <option value="180">3시간마다</option>
                <option value="360">6시간마다</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">동기화 시간대</label>
              <div class="time-range">
                <input v-model="syncTimeStart" type="time" class="time-input" />
                <span class="time-separator">~</span>
                <input v-model="syncTimeEnd" type="time" class="time-input" />
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">알림 설정</label>
              <div class="option-group">
                <label class="option-item">
                  <input v-model="notifications.syncComplete" type="checkbox" />
                  <span>동기화 완료시 알림</span>
                </label>
                <label class="option-item">
                  <input v-model="notifications.syncError" type="checkbox" />
                  <span>동기화 오류시 알림</span>
                </label>
                <label class="option-item">
                  <input v-model="notifications.statusChange" type="checkbox" />
                  <span>배송 상태 변경시 알림</span>
                </label>
              </div>
            </div>
            
            <button type="button" class="save-btn" @click="saveSettings">
              <CheckIcon class="btn-icon" />
              설정 저장
            </button>
          </div>
        </div>
      </div>

      <!-- Sync History -->
      <div class="sync-history">
        <div class="sync-card">
          <h2 class="card-title">동기화 기록</h2>
          
          <div class="history-list">
            <div
              v-for="record in syncHistory"
              :key="record.id"
              class="history-item"
              :class="record.status"
            >
              <div class="history-icon">
                <component :is="getHistoryIcon(record.status)" class="icon" />
              </div>
              <div class="history-content">
                <div class="history-header">
                  <h3 class="history-title">{{ record.title }}</h3>
                  <span class="history-time">{{ formatDateTime(record.timestamp) }}</span>
                </div>
                <p class="history-description">{{ record.description }}</p>
                <div v-if="record.details" class="history-details">
                  <span class="detail-item">처리된 항목: {{ record.details.processed }}</span>
                  <span class="detail-item">성공: {{ record.details.success }}</span>
                  <span v-if="record.details.failed > 0" class="detail-item error">
                    실패: {{ record.details.failed }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import {
  ArrowPathIcon,
  CheckIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  ClockIcon,
  XCircleIcon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

interface SyncRecord {
  id: string
  title: string
  description: string
  status: 'success' | 'error' | 'warning'
  timestamp: string
  details?: {
    processed: number
    success: number
    failed: number
  }
}

// State
const toast = useToast()

const syncing = ref(false)
const selectedCarrier = ref('')
const lastSyncTime = ref('2025-08-12 14:30:00')
const autoSyncInterval = ref(30)
const syncTimeStart = ref('09:00')
const syncTimeEnd = ref('18:00')

const syncOptions = ref({
  updateStatus: true,
  updateLocation: true,
  updateEvents: true
})

const notifications = ref({
  syncComplete: true,
  syncError: true,
  statusChange: false
})

const syncHistory = ref<SyncRecord[]>([
  {
    id: '1',
    title: '전체 동기화 완료',
    description: 'EMS, DHL, FedEx 배송 정보가 성공적으로 동기화되었습니다.',
    status: 'success',
    timestamp: '2025-08-12T14:30:00Z',
    details: {
      processed: 125,
      success: 123,
      failed: 2
    }
  },
  {
    id: '2',
    title: 'UPS API 연결 오류',
    description: 'UPS API 서버에 일시적으로 연결할 수 없습니다.',
    status: 'error',
    timestamp: '2025-08-12T13:15:00Z',
    details: {
      processed: 0,
      success: 0,
      failed: 45
    }
  },
  {
    id: '3',
    title: 'EMS 부분 동기화',
    description: '일부 EMS 배송 정보가 업데이트되지 않았습니다.',
    status: 'warning',
    timestamp: '2025-08-12T12:00:00Z',
    details: {
      processed: 67,
      success: 64,
      failed: 3
    }
  }
])

// Computed
const syncStatusClass = computed(() => {
  // 최근 동기화 상태를 기준으로 판단
  const latestRecord = syncHistory.value[0]
  return `status-${latestRecord?.status || 'unknown'}`
})

const syncStatusIcon = computed(() => {
  const latestRecord = syncHistory.value[0]
  const icons = {
    success: CheckCircleIcon,
    error: XCircleIcon,
    warning: ExclamationTriangleIcon
  }
  return icons[latestRecord?.status as keyof typeof icons] || ClockIcon
})

const syncStatusText = computed(() => {
  const latestRecord = syncHistory.value[0]
  const texts = {
    success: '정상',
    error: '오류',
    warning: '부분 성공'
  }
  return texts[latestRecord?.status as keyof typeof texts] || '알 수 없음'
})

// Methods
const startManualSync = async () => {
  try {
    syncing.value = true
    
    // 동기화 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    // 새로운 동기화 기록 추가
    const newRecord: SyncRecord = {
      id: Date.now().toString(),
      title: selectedCarrier.value ? `${getCarrierName(selectedCarrier.value)} 동기화 완료` : '전체 동기화 완료',
      description: '배송 정보가 성공적으로 동기화되었습니다.',
      status: 'success',
      timestamp: new Date().toISOString(),
      details: {
        processed: Math.floor(Math.random() * 100) + 50,
        success: Math.floor(Math.random() * 95) + 45,
        failed: Math.floor(Math.random() * 5)
      }
    }
    
    syncHistory.value.unshift(newRecord)
    lastSyncTime.value = new Date().toLocaleString('ko-KR')
    
    toast.success('동기화가 완료되었습니다.')
    
  } catch (error) {
    console.error('Sync error:', error)
    toast.error('동기화 중 오류가 발생했습니다.')
  } finally {
    syncing.value = false
  }
}

const saveSettings = () => {
  // 설정 저장 로직
  toast.success('설정이 저장되었습니다.')
}

const getCarrierName = (carrier: string) => {
  const names: Record<string, string> = {
    ems: 'EMS',
    dhl: 'DHL',
    fedex: 'FedEx',
    ups: 'UPS',
    tnt: 'TNT',
    korea_post: '우체국택배'
  }
  return names[carrier] || carrier
}

const getHistoryIcon = (status: string) => {
  const icons = {
    success: CheckCircleIcon,
    error: XCircleIcon,
    warning: ExclamationTriangleIcon
  }
  return icons[status as keyof typeof icons] || ClockIcon
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Lifecycle
onMounted(() => {
  // 자동 동기화 설정 로드
})
</script>

<style scoped>
.sync-view {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
  background: #f8fafc;
  min-height: 100vh;
}

.sync-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
  margin: 0;
}

.sync-status {
  display: flex;
  justify-content: center;
}

.status-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  max-width: 400px;
  width: 100%;
  text-align: center;
}

.status-header {
  margin-bottom: 1.5rem;
}

.status-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.last-sync {
  font-size: 0.875rem;
  color: #6b7280;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 0.75rem;
}

.status-indicator.status-success {
  background: #d1fae5;
  color: #047857;
}

.status-indicator.status-error {
  background: #fecaca;
  color: #b91c1c;
}

.status-indicator.status-warning {
  background: #fef3c7;
  color: #92400e;
}

.status-icon {
  width: 1.5rem;
  height: 1.5rem;
}

.status-text {
  font-weight: 600;
  font-size: 1.125rem;
}

.sync-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.card-description {
  color: #6b7280;
  margin: 0 0 2rem 0;
  line-height: 1.6;
}

.sync-form,
.settings-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-select {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
  background: white;
}

.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.option-item input[type="checkbox"] {
  width: 1.25rem;
  height: 1.25rem;
  accent-color: #3b82f6;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.time-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
}

.time-separator {
  color: #6b7280;
}

.sync-btn,
.save-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  font-weight: 500;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  align-self: flex-start;
}

.sync-btn {
  background: #3b82f6;
  color: white;
}

.sync-btn:hover:not(:disabled) {
  background: #2563eb;
}

.sync-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.save-btn {
  background: #10b981;
  color: white;
}

.save-btn:hover {
  background: #059669;
}

.btn-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 0.75rem;
  border: 1px solid #e5e7eb;
}

.history-item.success {
  border-left: 4px solid #10b981;
}

.history-item.error {
  border-left: 4px solid #ef4444;
}

.history-item.warning {
  border-left: 4px solid #f59e0b;
}

.history-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-item.success .history-icon {
  background: #d1fae5;
}

.history-item.error .history-icon {
  background: #fecaca;
}

.history-item.warning .history-icon {
  background: #fef3c7;
}

.history-icon .icon {
  width: 1.25rem;
  height: 1.25rem;
}

.history-item.success .icon {
  color: #047857;
}

.history-item.error .icon {
  color: #b91c1c;
}

.history-item.warning .icon {
  color: #92400e;
}

.history-content {
  flex: 1;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.history-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.history-time {
  font-size: 0.875rem;
  color: #6b7280;
}

.history-description {
  color: #374151;
  margin: 0 0 0.75rem 0;
  line-height: 1.5;
}

.history-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.875rem;
}

.detail-item {
  color: #6b7280;
}

.detail-item.error {
  color: #ef4444;
  font-weight: 500;
}

@media (max-width: 768px) {
  .sync-view {
    padding: 1rem;
  }
  
  .sync-card {
    padding: 1.5rem;
  }
  
  .time-range {
    flex-direction: column;
    align-items: stretch;
  }
  
  .history-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .history-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>