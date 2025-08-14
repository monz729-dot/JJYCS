<template>
  <div class="warehouse-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <div class="header-info">
        <h1>창고 대시보드</h1>
        <p class="current-time">{{ currentDateTime }}</p>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="btn-refresh" :disabled="loading">
          <i class="icon-refresh" :class="{ spinning: loading }"></i>
          새로고침
        </button>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card pending">
        <div class="stat-icon">
          <i class="icon-inbox"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingInbound }}</div>
          <div class="stat-label">입고 대기</div>
          <div class="stat-change positive" v-if="stats.pendingInboundChange > 0">
            +{{ stats.pendingInboundChange }}
          </div>
        </div>
      </div>

      <div class="stat-card processing">
        <div class="stat-icon">
          <i class="icon-package"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.processing }}</div>
          <div class="stat-label">처리 중</div>
          <div class="stat-change" :class="stats.processingChange > 0 ? 'positive' : 'negative'">
            {{ stats.processingChange > 0 ? '+' : '' }}{{ stats.processingChange }}
          </div>
        </div>
      </div>

      <div class="stat-card ready">
        <div class="stat-icon">
          <i class="icon-outbox"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.readyForOutbound }}</div>
          <div class="stat-label">출고 준비</div>
          <div class="stat-change positive" v-if="stats.readyForOutboundChange > 0">
            +{{ stats.readyForOutboundChange }}
          </div>
        </div>
      </div>

      <div class="stat-card hold">
        <div class="stat-icon">
          <i class="icon-pause"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.hold }}</div>
          <div class="stat-label">보류</div>
          <div class="stat-change negative" v-if="stats.holdChange > 0">
            +{{ stats.holdChange }}
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 컨텐츠 그리드 -->
    <div class="main-grid">
      <!-- 퀵 액션 -->
      <div class="quick-actions-section">
        <div class="section-header">
          <h2>빠른 작업</h2>
        </div>
        <div class="quick-actions">
          <router-link to="/warehouse/scan" class="quick-action scan">
            <div class="action-icon">
              <i class="icon-qr-code"></i>
            </div>
            <div class="action-content">
              <div class="action-title">스캔</div>
              <div class="action-description">QR/바코드 스캔</div>
            </div>
            <i class="icon-arrow-right"></i>
          </router-link>

          <router-link to="/warehouse/batch-process" class="quick-action batch">
            <div class="action-icon">
              <i class="icon-layers"></i>
            </div>
            <div class="action-content">
              <div class="action-title">일괄 처리</div>
              <div class="action-description">여러 항목 처리</div>
            </div>
            <i class="icon-arrow-right"></i>
          </router-link>

          <router-link to="/warehouse/labels" class="quick-action labels">
            <div class="action-icon">
              <i class="icon-tag"></i>
            </div>
            <div class="action-content">
              <div class="action-title">라벨 관리</div>
              <div class="action-description">라벨 생성/출력</div>
            </div>
            <i class="icon-arrow-right"></i>
          </router-link>

          <router-link to="/warehouse/inventory" class="quick-action inventory">
            <div class="action-icon">
              <i class="icon-clipboard-list"></i>
            </div>
            <div class="action-content">
              <div class="action-title">재고 관리</div>
              <div class="action-description">재고 현황 조회</div>
            </div>
            <i class="icon-arrow-right"></i>
          </router-link>

          <router-link to="/warehouse/photos" class="quick-action photos">
            <div class="action-icon">
              <i class="icon-camera"></i>
            </div>
            <div class="action-content">
              <div class="action-title">사진 관리</div>
              <div class="action-description">리패킹 사진</div>
            </div>
            <i class="icon-arrow-right"></i>
          </router-link>
        </div>
      </div>

      <!-- 최근 활동 -->
      <div class="recent-activities-section">
        <div class="section-header">
          <h2>최근 활동</h2>
          <button @click="loadRecentActivities" class="btn-more">
            모든 활동 보기
          </button>
        </div>
        
        <div v-if="recentActivities.length > 0" class="activities-list">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="activity-item"
            :class="activity.type"
          >
            <div class="activity-icon">
              <i :class="getActivityIcon(activity.type)"></i>
            </div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-description">{{ activity.description }}</div>
              <div class="activity-time">{{ formatRelativeTime(activity.timestamp) }}</div>
            </div>
            <div class="activity-status" :class="activity.status">
              <i :class="activity.status === 'success' ? 'icon-check' : 'icon-x'"></i>
            </div>
          </div>
        </div>

        <div v-else-if="loading" class="loading-state">
          <LoadingSpinner />
          <p>활동 내역을 불러오는 중...</p>
        </div>

        <div v-else class="empty-activities">
          <i class="icon-activity"></i>
          <p>최근 활동이 없습니다</p>
        </div>
      </div>
    </div>

    <!-- 실시간 알림 -->
    <div class="alerts-section" v-if="alerts.length > 0">
      <div class="section-header">
        <h2>알림</h2>
        <button @click="dismissAllAlerts" class="btn-dismiss">
          모두 해제
        </button>
      </div>
      
      <div class="alerts-list">
        <div
          v-for="alert in alerts"
          :key="alert.id"
          class="alert-item"
          :class="alert.type"
        >
          <div class="alert-icon">
            <i :class="getAlertIcon(alert.type)"></i>
          </div>
          <div class="alert-content">
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-message">{{ alert.message }}</div>
          </div>
          <button @click="dismissAlert(alert.id)" class="btn-dismiss-single">
            <i class="icon-x"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 성능 메트릭 -->
    <div class="metrics-section">
      <div class="section-header">
        <h2>오늘의 성과</h2>
      </div>
      
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-value">{{ todayMetrics.processed }}</div>
          <div class="metric-label">처리 완료</div>
          <div class="metric-progress">
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :style="{ width: `${todayMetrics.processedProgress}%` }"
              ></div>
            </div>
            <span class="progress-text">목표: {{ todayMetrics.processedTarget }}</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-value">{{ todayMetrics.scanned }}</div>
          <div class="metric-label">스캔 완료</div>
          <div class="metric-progress">
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :style="{ width: `${todayMetrics.scannedProgress}%` }"
              ></div>
            </div>
            <span class="progress-text">목표: {{ todayMetrics.scannedTarget }}</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-value">{{ todayMetrics.efficiency }}%</div>
          <div class="metric-label">작업 효율성</div>
          <div class="metric-trend" :class="todayMetrics.efficiencyTrend">
            <i :class="todayMetrics.efficiencyTrend === 'up' ? 'icon-trending-up' : 'icon-trending-down'"></i>
            {{ todayMetrics.efficiencyChange }}%
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '../stores/warehouseStore'
import { useToast } from '@/composables/useToast'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 반응형 데이터
const loading = ref(false)
const currentTime = ref(new Date())
let timeInterval: number | null = null

// 통계 데이터
const stats = ref({
  pendingInbound: 25,
  pendingInboundChange: 5,
  processing: 12,
  processingChange: -2,
  readyForOutbound: 38,
  readyForOutboundChange: 8,
  hold: 3,
  holdChange: 1
})

// 최근 활동 데이터
const recentActivities = ref([
  {
    id: 1,
    type: 'scan',
    title: 'QR 스캔 완료',
    description: 'YCS240812001 - 입고 처리',
    timestamp: new Date(Date.now() - 5 * 60 * 1000),
    status: 'success'
  },
  {
    id: 2,
    type: 'batch',
    title: '일괄 출고 처리',
    description: '15개 항목 처리 완료',
    timestamp: new Date(Date.now() - 15 * 60 * 1000),
    status: 'success'
  },
  {
    id: 3,
    type: 'label',
    title: '라벨 출력',
    description: 'YCS240812002 - 배송 라벨',
    timestamp: new Date(Date.now() - 30 * 60 * 1000),
    status: 'success'
  },
  {
    id: 4,
    type: 'photo',
    title: '리패킹 사진 업로드',
    description: 'YCS240812003 - 3장 업로드',
    timestamp: new Date(Date.now() - 45 * 60 * 1000),
    status: 'success'
  },
  {
    id: 5,
    type: 'scan',
    title: '스캔 오류',
    description: 'YCS240812004 - QR 코드 인식 실패',
    timestamp: new Date(Date.now() - 60 * 60 * 1000),
    status: 'error'
  }
])

// 알림 데이터
const alerts = ref([
  {
    id: 1,
    type: 'warning',
    title: '보류 항목 증가',
    message: '보류 상태 항목이 3개로 증가했습니다. 확인이 필요합니다.'
  },
  {
    id: 2,
    type: 'info',
    title: '대량 입고 예정',
    message: '오후 2시경 대량 입고가 예정되어 있습니다.'
  }
])

// 오늘의 성과 데이터
const todayMetrics = ref({
  processed: 187,
  processedTarget: 200,
  processedProgress: 93.5,
  scanned: 245,
  scannedTarget: 250,
  scannedProgress: 98,
  efficiency: 94,
  efficiencyTrend: 'up',
  efficiencyChange: 2.3
})

// 현재 날짜/시간 계산
const currentDateTime = computed(() => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(currentTime.value)
})

// 컴포넌트 마운트
onMounted(() => {
  loadDashboardData()
  
  // 1초마다 시간 업데이트
  timeInterval = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)

  // 30초마다 데이터 갱신
  setInterval(() => {
    if (!loading.value) {
      refreshData()
    }
  }, 30000)
})

// 컴포넌트 언마운트
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})

// 대시보드 데이터 로드
const loadDashboardData = async () => {
  loading.value = true
  
  try {
    // TODO: API 호출로 실제 데이터 가져오기
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Mock data는 이미 위에서 설정됨
    showToast('대시보드 데이터를 불러왔습니다', 'success')
  } catch (error: any) {
    console.error('Dashboard data loading error:', error)
    showToast('데이터를 불러오는 중 오류가 발생했습니다', 'error')
  } finally {
    loading.value = false
  }
}

// 데이터 새로고침
const refreshData = async () => {
  await loadDashboardData()
}

// 최근 활동 더 보기
const loadRecentActivities = () => {
  router.push('/warehouse/activities')
}

// 활동 아이콘 반환
const getActivityIcon = (type: string): string => {
  const iconMap: Record<string, string> = {
    scan: 'icon-qr-code',
    batch: 'icon-layers',
    label: 'icon-tag',
    photo: 'icon-camera',
    inventory: 'icon-clipboard-list'
  }
  return iconMap[type] || 'icon-activity'
}

// 알림 아이콘 반환
const getAlertIcon = (type: string): string => {
  const iconMap: Record<string, string> = {
    warning: 'icon-alert-triangle',
    error: 'icon-alert-circle',
    info: 'icon-info',
    success: 'icon-check-circle'
  }
  return iconMap[type] || 'icon-bell'
}

// 상대 시간 포맷팅
const formatRelativeTime = (date: Date): string => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days}일 전`
  } else if (hours > 0) {
    return `${hours}시간 전`
  } else if (minutes > 0) {
    return `${minutes}분 전`
  } else {
    return '방금 전'
  }
}

// 알림 해제
const dismissAlert = (alertId: number) => {
  const index = alerts.value.findIndex(alert => alert.id === alertId)
  if (index !== -1) {
    alerts.value.splice(index, 1)
  }
}

const dismissAllAlerts = () => {
  alerts.value = []
  showToast('모든 알림을 해제했습니다', 'info')
}
</script>

<style scoped>
.warehouse-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
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

.current-time {
  font-size: 14px;
  color: #6b7280;
}

.btn-refresh {
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

.btn-refresh:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon-refresh.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.15s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card.pending .stat-icon {
  background: #f59e0b;
}

.stat-card.processing .stat-icon {
  background: #3b82f6;
}

.stat-card.ready .stat-icon {
  background: #10b981;
}

.stat-card.hold .stat-icon {
  background: #ef4444;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.stat-change.positive {
  background: #dcfce7;
  color: #166534;
}

.stat-change.negative {
  background: #fef2f2;
  color: #dc2626;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.btn-more, .btn-dismiss {
  color: #6b7280;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.15s ease;
}

.btn-more:hover, .btn-dismiss:hover {
  color: #374151;
}

.quick-actions-section,
.recent-activities-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.quick-actions {
  display: grid;
  gap: 12px;
}

.quick-action {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: all 0.15s ease;
}

.quick-action:hover {
  border-color: #3b82f6;
  background: #f8fafc;
  transform: translateX(4px);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.quick-action.scan .action-icon {
  background: #3b82f6;
}

.quick-action.batch .action-icon {
  background: #8b5cf6;
}

.quick-action.labels .action-icon {
  background: #f59e0b;
}

.quick-action.inventory .action-icon {
  background: #10b981;
}

.quick-action.photos .action-icon {
  background: #ef4444;
}

.action-content {
  flex: 1;
}

.action-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.action-description {
  font-size: 12px;
  color: #6b7280;
}

.activities-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
  flex-shrink: 0;
}

.activity-item.scan .activity-icon {
  background: #3b82f6;
}

.activity-item.batch .activity-icon {
  background: #8b5cf6;
}

.activity-item.label .activity-icon {
  background: #f59e0b;
}

.activity-item.photo .activity-icon {
  background: #ef4444;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.activity-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #9ca3af;
}

.activity-status {
  display: flex;
  align-items: center;
  font-size: 16px;
  flex-shrink: 0;
}

.activity-status.success {
  color: #10b981;
}

.activity-status.error {
  color: #ef4444;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-activities {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-activities i {
  font-size: 48px;
  margin-bottom: 12px;
}

.alerts-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
}

.alert-item.warning {
  background: #fffbeb;
  border-left: 4px solid #f59e0b;
}

.alert-item.info {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
}

.alert-item.error {
  background: #fef2f2;
  border-left: 4px solid #ef4444;
}

.alert-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.alert-item.warning .alert-icon {
  color: #f59e0b;
}

.alert-item.info .alert-icon {
  color: #3b82f6;
}

.alert-item.error .alert-icon {
  color: #ef4444;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 14px;
  color: #6b7280;
}

.btn-dismiss-single {
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.15s ease;
}

.btn-dismiss-single:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #6b7280;
}

.metrics-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.metric-card {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
}

.metric-progress {
  margin-bottom: 12px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f3f4f6;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: #3b82f6;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #9ca3af;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.metric-trend.up {
  color: #10b981;
}

.metric-trend.down {
  color: #ef4444;
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .main-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .warehouse-dashboard {
    padding: 16px;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }

  .quick-actions-section,
  .recent-activities-section,
  .alerts-section,
  .metrics-section {
    padding: 20px;
  }
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { formatDate, formatNumber } from '@/utils/helpers'

// 상태
const loading = ref(false)
const currentDateTime = ref('')

// 통계 데이터
const stats = ref({
  pendingInbound: 24,
  pendingInboundChange: 5,
  processing: 18,
  processingChange: -2,
  readyForOutbound: 31,
  readyForOutboundChange: 8,
  onHold: 6,
  onHoldChange: 1,
  totalBoxes: 1247,
  totalBoxesChange: 45,
  dailyProcessed: 89,
  dailyProcessedChange: 12
})

// 최근 활동
const recentActivities = ref([
  {
    id: 1,
    type: 'inbound',
    description: 'TEST-GEN-001 입고 처리 완료',
    user: '최창고',
    timestamp: '2024-01-15T14:30:00Z',
    status: 'completed'
  },
  {
    id: 2,
    type: 'scan',
    description: 'YCS240815001-01 스캔 완료',
    user: '최창고',
    timestamp: '2024-01-15T14:25:00Z',
    status: 'completed'
  },
  {
    id: 3,
    type: 'outbound',
    description: 'TEST-COR-001 출고 처리',
    user: '최창고',
    timestamp: '2024-01-15T13:45:00Z',
    status: 'completed'
  },
  {
    id: 4,
    type: 'hold',
    description: 'TEST-DEL-001 보류 처리 (회원코드 누락)',
    user: '최창고',
    timestamp: '2024-01-15T13:20:00Z',
    status: 'hold'
  }
])

// 알림
const alerts = ref([
  {
    id: 1,
    type: 'warning',
    title: '보류 박스 증가',
    message: '회원코드 미기재로 보류된 박스가 6개 있습니다.',
    timestamp: '2024-01-15T14:00:00Z',
    urgent: true
  },
  {
    id: 2,
    type: 'info',
    title: '일일 처리량 증가',
    message: '오늘 처리량이 평균보다 15% 증가했습니다.',
    timestamp: '2024-01-15T12:00:00Z',
    urgent: false
  }
])

// 성능 지표
const metrics = ref({
  dailyProcessingRate: 94.2,
  averageProcessingTime: 12.5,
  errorRate: 1.3,
  customerSatisfaction: 98.7
})

// 계산된 속성
const totalProcessingTasks = computed(() => {
  return stats.value.pendingInbound + stats.value.processing + stats.value.readyForOutbound
})

const processingEfficiency = computed(() => {
  const total = totalProcessingTasks.value
  const completed = stats.value.dailyProcessed
  return total > 0 ? Math.round((completed / (completed + total)) * 100) : 0
})

// 메서드
const refreshData = async () => {
  loading.value = true
  try {
    // 실제 데이터 새로고침 로직
    await new Promise(resolve => setTimeout(resolve, 1000))
    console.log('창고 데이터 새로고침 완료')
  } catch (error) {
    console.error('데이터 새로고침 실패:', error)
  } finally {
    loading.value = false
  }
}

const getActivityIcon = (type: string) => {
  const icons: Record<string, string> = {
    'inbound': 'icon-inbox',
    'outbound': 'icon-outbox',
    'scan': 'icon-scan',
    'hold': 'icon-hold'
  }
  return icons[type] || 'icon-package'
}

const getActivityColor = (type: string) => {
  const colors: Record<string, string> = {
    'inbound': 'text-blue-600',
    'outbound': 'text-green-600',
    'scan': 'text-purple-600',
    'hold': 'text-orange-600'
  }
  return colors[type] || 'text-gray-600'
}

const getAlertIcon = (type: string) => {
  const icons: Record<string, string> = {
    'warning': 'icon-warning',
    'error': 'icon-error',
    'info': 'icon-info'
  }
  return icons[type] || 'icon-info'
}

const getAlertColor = (type: string, urgent: boolean) => {
  if (urgent) return 'text-red-600'
  
  const colors: Record<string, string> = {
    'warning': 'text-orange-600',
    'error': 'text-red-600',
    'info': 'text-blue-600'
  }
  return colors[type] || 'text-gray-600'
}

const updateDateTime = () => {
  const now = new Date()
  currentDateTime.value = now.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'long'
  })
}

const handleQuickAction = (action: string) => {
  console.log('Quick action:', action)
  // 실제 액션 처리 로직
}

// 라이프사이클
onMounted(() => {
  updateDateTime()
  // 1분마다 시간 업데이트
  setInterval(updateDateTime, 60000)
  
  // 초기 데이터 로드
  refreshData()
})
</script>