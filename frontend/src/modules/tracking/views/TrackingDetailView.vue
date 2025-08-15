<template>
  <div class="tracking-detail-view">
    <div class="detail-container">
      <!-- Header -->
      <div class="page-header">
        <button type="button" class="back-btn" @click="goBack">
          <ArrowLeftIcon class="back-icon" />
          목록으로
        </button>
        
        <div class="header-content">
          <h1 class="page-title">배송 상세 추적</h1>
          <div class="tracking-meta">
            <span class="tracking-number">{{ trackingNumber }}</span>
            <span class="status-badge" :class="`status-${trackingData?.status}`">
              <component :is="getStatusIcon(trackingData?.status)" class="status-icon" />
              {{ getStatusText(trackingData?.status) }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
        <p>배송 정보를 조회하는 중...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <ExclamationTriangleIcon class="error-icon" />
        <h3 class="error-title">조회 오류</h3>
        <p class="error-description">{{ error }}</p>
        <button type="button" class="retry-btn" @click="loadTrackingData">
          다시 시도
        </button>
      </div>

      <div v-else-if="trackingData" class="tracking-content">
        <!-- Progress Tracking -->
        <div class="progress-section">
          <h2 class="section-title">배송 진행 상황</h2>
          <div class="progress-timeline">
            <div
              v-for="(event, index) in trackingData.events"
              :key="event.id"
              class="timeline-item"
              :class="{ 'current': index === 0 }"
            >
              <div class="timeline-dot">
                <component :is="getEventIcon(event.eventType)" class="event-icon" />
              </div>
              <div class="timeline-content">
                <div class="event-header">
                  <h3 class="event-title">{{ event.title }}</h3>
                  <span class="event-time">{{ formatDateTime(event.timestamp) }}</span>
                </div>
                <p class="event-description">{{ event.description }}</p>
                <div v-if="event.location" class="event-location">
                  <MapPinIcon class="location-icon" />
                  <span>{{ event.location }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Shipping Info -->
        <div class="info-section">
          <h2 class="section-title">배송 정보</h2>
          <div class="info-grid">
            <div class="info-card">
              <h3 class="info-title">발송지</h3>
              <div class="info-content">
                <p class="info-name">{{ trackingData.sender.name }}</p>
                <p class="info-address">{{ trackingData.sender.address }}</p>
                <p class="info-contact">{{ trackingData.sender.phone }}</p>
              </div>
            </div>

            <div class="info-card">
              <h3 class="info-title">수취인</h3>
              <div class="info-content">
                <p class="info-name">{{ trackingData.recipient.name }}</p>
                <p class="info-address">{{ trackingData.recipient.address }}</p>
                <p class="info-contact">{{ trackingData.recipient.phone }}</p>
              </div>
            </div>

            <div class="info-card">
              <h3 class="info-title">배송 상세</h3>
              <div class="info-content">
                <div class="detail-item">
                  <span class="detail-label">배송 방법:</span>
                  <span class="detail-value">
                    {{ trackingData.shippingMethod === 'sea' ? '해상 운송' : '항공 운송' }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">운송업체:</span>
                  <span class="detail-value">{{ trackingData.carrierName }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">발송일:</span>
                  <span class="detail-value">{{ formatDate(trackingData.createdAt) }}</span>
                </div>
                <div v-if="trackingData.estimatedDelivery" class="detail-item">
                  <span class="detail-label">예상 배송일:</span>
                  <span class="detail-value">{{ formatDate(trackingData.estimatedDelivery) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Package Details -->
        <div v-if="trackingData.packages" class="package-section">
          <h2 class="section-title">패키지 정보</h2>
          <div class="package-grid">
            <div
              v-for="(pkg, index) in trackingData.packages"
              :key="index"
              class="package-card"
            >
              <div class="package-header">
                <h3 class="package-title">{{ pkg.packageNumber || `패키지 ${index + 1}` }}</h3>
                <span class="package-status" :class="`status-${pkg.status}`">
                  {{ getStatusText(pkg.status) }}
                </span>
              </div>
              <div class="package-details">
                <div class="package-info">
                  <span class="info-label">크기:</span>
                  <span>{{ pkg.dimensions?.width }} × {{ pkg.dimensions?.height }} × {{ pkg.dimensions?.depth }} cm</span>
                </div>
                <div class="package-info">
                  <span class="info-label">무게:</span>
                  <span>{{ pkg.weight }} kg</span>
                </div>
                <div class="package-info">
                  <span class="info-label">CBM:</span>
                  <span>{{ pkg.cbm?.toFixed(6) }} m³</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="actions-section">
          <div class="action-buttons">
            <button type="button" class="action-btn secondary" @click="refreshData">
              <ArrowPathIcon class="btn-icon" />
              새로고침
            </button>
            
            <button type="button" class="action-btn secondary" @click="shareTracking">
              <ShareIcon class="btn-icon" />
              공유하기
            </button>
            
            <button 
              v-if="trackingData.status === 'DELIVERED'" 
              type="button" 
              class="action-btn primary"
              @click="confirmDelivery"
            >
              <CheckCircleIcon class="btn-icon" />
              배송 확인
            </button>
            
            <button 
              v-if="canReportIssue" 
              type="button" 
              class="action-btn danger"
              @click="reportIssue"
            >
              <ExclamationTriangleIcon class="btn-icon" />
              문제 신고
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import SpringBootTrackingService, { type TrackingResponse, type TrackingEvent, type DeliveryConfirmationRequest, type IssueReportRequest } from '@/services/trackingApiService'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import {
  ArrowLeftIcon,
  ExclamationTriangleIcon,
  MapPinIcon,
  TruckIcon,
  CheckCircleIcon,
  ClockIcon,
  ArrowPathIcon,
  ShareIcon,
  BuildingStorefrontIcon,
  CubeIcon,
  PaperAirplaneIcon
} from '@heroicons/vue/24/outline'

// Composables
const router = useRouter()
const route = useRoute()

// Reactive state
const loading = ref(false)
const error = ref<string | null>(null)
const trackingData = ref<TrackingResponse | null>(null)

// Computed
const trackingNumber = computed(() => route.params.trackingNumber as string)

// Computed
const canReportIssue = computed(() => {
  return trackingData.value?.status !== 'DELIVERED'
})

// Methods
const loadTrackingData = async () => {
  try {
    loading.value = true
    error.value = null
    
    const result = await SpringBootTrackingService.getTrackingByNumber(trackingNumber.value)
    
    if (result.success && result.data) {
      trackingData.value = result.data
    } else {
      error.value = result.error || '배송 정보를 불러오는데 실패했습니다.'
    }
  } catch (err) {
    console.error('Load tracking data error:', err)
    error.value = '배송 정보를 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/tracking')
}

const getStatusIcon = (status?: string) => {
  const icons = {
    PENDING: ClockIcon,
    PROCESSING: ArrowPathIcon,
    SHIPPED: TruckIcon,
    IN_TRANSIT: TruckIcon,
    CUSTOMS: BuildingStorefrontIcon,
    OUT_FOR_DELIVERY: TruckIcon,
    DELIVERED: CheckCircleIcon,
    EXCEPTION: ExclamationTriangleIcon,
    RETURNED: ArrowLeftIcon
  }
  return icons[status as keyof typeof icons] || ClockIcon
}

const getStatusText = (status?: string) => {
  const texts = {
    PENDING: '대기 중',
    PROCESSING: '처리 중',
    SHIPPED: '발송됨',
    IN_TRANSIT: '배송 중',
    CUSTOMS: '통관 처리',
    OUT_FOR_DELIVERY: '배송 준비',
    DELIVERED: '배송 완료',
    EXCEPTION: '배송 문제',
    RETURNED: '반송됨'
  }
  return texts[status as keyof typeof texts] || '알 수 없음'
}

const getEventIcon = (eventType: string) => {
  const icons = {
    CREATED: ClockIcon,
    PROCESSING: ArrowPathIcon,
    COMPLETED: CheckCircleIcon,
    FAILED: ExclamationTriangleIcon,
    REFUNDED: ArrowLeftIcon,
    CANCELLED: ExclamationTriangleIcon
  }
  return icons[eventType as keyof typeof icons] || ClockIcon
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const refreshData = async () => {
  await loadTrackingData()
  
  // Emit custom event for success notification
  window.dispatchEvent(new CustomEvent('tracking-success', {
    detail: { message: '배송 정보가 업데이트되었습니다.' }
  }))
}

const shareTracking = async () => {
  const url = window.location.href
  
  if (navigator.share) {
    try {
      await navigator.share({
        title: `배송 추적 - ${trackingNumber.value}`,
        text: '배송 상태를 확인해보세요',
        url: url
      })
    } catch (err) {
      console.log('Share cancelled')
    }
  } else {
    try {
      await navigator.clipboard.writeText(url)
      window.dispatchEvent(new CustomEvent('tracking-success', {
        detail: { message: '링크가 클립보드에 복사되었습니다.' }
      }))
    } catch (err) {
      window.dispatchEvent(new CustomEvent('tracking-error', {
        detail: { message: '링크 복사에 실패했습니다.' }
      }))
    }
  }
}

const confirmDelivery = async () => {
  if (!trackingData.value) return
  
  try {
    const request: DeliveryConfirmationRequest = {
      trackingId: trackingData.value.id,
      recipientName: trackingData.value.recipient.name,
      notes: '사용자가 배송 완료를 확인했습니다.'
    }

    const result = await SpringBootTrackingService.confirmDelivery(request)
    
    if (result.success) {
      // Reload tracking data
      await loadTrackingData()
      
      window.dispatchEvent(new CustomEvent('tracking-success', {
        detail: { message: '배송 확인이 완료되었습니다.' }
      }))
    } else {
      window.dispatchEvent(new CustomEvent('tracking-error', {
        detail: { message: result.error || '배송 확인 처리 중 오류가 발생했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Delivery confirmation error:', error)
    window.dispatchEvent(new CustomEvent('tracking-error', {
      detail: { message: '배송 확인 처리 중 오류가 발생했습니다.' }
    }))
  }
}

const reportIssue = async () => {
  if (!trackingData.value) return
  
  try {
    const request: IssueReportRequest = {
      trackingId: trackingData.value.id,
      issueType: 'OTHER',
      description: '사용자가 배송 문제를 신고했습니다.',
      priority: 'MEDIUM',
      contactMethod: 'EMAIL'
    }

    const result = await SpringBootTrackingService.reportIssue(request)
    
    if (result.success && result.data) {
      window.dispatchEvent(new CustomEvent('tracking-success', {
        detail: { 
          message: `문제 신고가 접수되었습니다. 티켓 번호: ${result.data.ticketNumber}` 
        }
      }))
    } else {
      window.dispatchEvent(new CustomEvent('tracking-error', {
        detail: { message: result.error || '문제 신고 처리 중 오류가 발생했습니다.' }
      }))
    }
  } catch (error) {
    console.error('Issue report error:', error)
    window.dispatchEvent(new CustomEvent('tracking-error', {
      detail: { message: '문제 신고 처리 중 오류가 발생했습니다.' }
    }))
  }
}

// Lifecycle
onMounted(() => {
  loadTrackingData()
})
</script>

<style scoped>
.tracking-detail-view {
  min-height: 100vh;
  background: #f8fafc;
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: #6b7280;
  font-size: 0.875rem;
  cursor: pointer;
  margin-bottom: 1rem;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: all 0.2s;
}

.back-btn:hover {
  color: #374151;
  background: #f3f4f6;
}

.back-icon {
  width: 1rem;
  height: 1rem;
}

.header-content {
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.tracking-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.tracking-number {
  font-family: monospace;
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-badge.status-delivered {
  background: #d1fae5;
  color: #047857;
}

.status-badge.status-in_transit {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.status-shipped {
  background: #f3e8ff;
  color: #7c3aed;
}

.status-badge.status-issue {
  background: #fecaca;
  color: #b91c1c;
}

.status-icon {
  width: 1rem;
  height: 1rem;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.error-icon {
  width: 4rem;
  height: 4rem;
  color: #ef4444;
  margin-bottom: 1rem;
}

.error-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.error-description {
  color: #6b7280;
  margin: 0 0 2rem 0;
}

.retry-btn {
  padding: 0.75rem 1.5rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-weight: 500;
  cursor: pointer;
}

.retry-btn:hover {
  background: #2563eb;
}

.tracking-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1.5rem 0;
}

.progress-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.progress-timeline {
  position: relative;
}

.progress-timeline::before {
  content: '';
  position: absolute;
  left: 1.5rem;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e5e7eb;
}

.timeline-item {
  position: relative;
  padding-left: 4rem;
  padding-bottom: 2rem;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item.current .timeline-dot {
  background: #3b82f6;
  border-color: #3b82f6;
}

.timeline-dot {
  position: absolute;
  left: 0.75rem;
  top: 0;
  width: 3rem;
  height: 3rem;
  background: white;
  border: 3px solid #d1d5db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.event-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #6b7280;
}

.timeline-item.current .event-icon {
  color: white;
}

.timeline-content {
  padding-top: 0.5rem;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.event-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.event-time {
  font-size: 0.875rem;
  color: #6b7280;
}

.event-description {
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.event-location {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.location-icon {
  width: 1rem;
  height: 1rem;
}

.info-section,
.package-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.info-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
}

.info-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.info-name {
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.info-address,
.info-contact {
  color: #6b7280;
  margin: 0 0 0.25rem 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #6b7280;
  font-size: 0.875rem;
}

.detail-value {
  color: #1f2937;
  font-weight: 500;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.package-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
}

.package-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.package-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.package-status {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.package-status.status-in_transit {
  background: #dbeafe;
  color: #1e40af;
}

.package-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.package-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.info-label {
  color: #6b7280;
}

.actions-section {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.primary {
  background: #10b981;
  color: white;
}

.action-btn.primary:hover {
  background: #059669;
}

.action-btn.secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.action-btn.secondary:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.action-btn.danger {
  background: #ef4444;
  color: white;
}

.action-btn.danger:hover {
  background: #dc2626;
}

.btn-icon {
  width: 1rem;
  height: 1rem;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 1rem;
  }
  
  .tracking-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .package-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .event-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>