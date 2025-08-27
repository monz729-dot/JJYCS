<template>
  <div class="warehouse-dashboard">
    <!-- Header -->
    <div class="dashboard-header">
      <div class="header-info">
        <h1 class="page-title">창고 대시보드</h1>
        <p class="warehouse-info">
          <i class="icon-location"></i>
          창고 1호 - 방콕 메인 창고
        </p>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="refresh-btn" :disabled="loading">
          <i class="icon-refresh" :class="{ spinning: loading }"></i>
          새로고침
        </button>
        <button @click="navigateToScan" class="scan-btn">
          <i class="icon-scan"></i>
          스캔 모드
        </button>
      </div>
    </div>

    <!-- Quick Stats -->
    <div class="stats-overview">
      <div class="stat-card total">
        <div class="stat-icon">
          <i class="icon-warehouse"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.statistics.totalInventory }}</h3>
          <p>총 재고</p>
        </div>
        <div class="stat-trend">
          <span class="trend-value positive">+12%</span>
        </div>
      </div>

      <div class="stat-card inbound">
        <div class="stat-icon">
          <i class="icon-arrow-down"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.statistics.pendingInbound }}</h3>
          <p>입고 대기</p>
        </div>
        <div class="stat-trend">
          <span class="trend-value">{{ warehouseStore.statistics.dailyInbound }} 오늘</span>
        </div>
      </div>

      <div class="stat-card ready">
        <div class="stat-icon">
          <i class="icon-package"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.statistics.readyForShipping }}</h3>
          <p>출고 준비</p>
        </div>
        <div class="stat-trend">
          <span class="trend-value">{{ warehouseStore.statistics.dailyOutbound }} 오늘</span>
        </div>
      </div>

      <div class="stat-card issues">
        <div class="stat-icon">
          <i class="icon-alert"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.statistics.onHold + warehouseStore.statistics.damaged }}</h3>
          <p>이슈 항목</p>
        </div>
        <div class="stat-trend">
          <span class="trend-value negative">{{ warehouseStore.statistics.onHold }} 보류</span>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3>일일 입출고 현황</h3>
          <div class="chart-controls">
            <select v-model="chartPeriod" @change="updateChartData" class="period-select">
              <option value="7">최근 7일</option>
              <option value="30">최근 30일</option>
              <option value="90">최근 90일</option>
            </select>
          </div>
        </div>
        <div class="chart-container">
          <canvas ref="dailyFlowChart" width="400" height="200"></canvas>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>재고 상태 분포</h3>
        </div>
        <div class="chart-container">
          <canvas ref="statusPieChart" width="300" height="200"></canvas>
        </div>
        <div class="status-legend">
          <div class="legend-item" v-for="status in statusDistribution" :key="status.label">
            <span class="legend-color" :style="{ backgroundColor: status.color }"></span>
            <span class="legend-label">{{ status.label }}</span>
            <span class="legend-value">{{ status.count }}개</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Activities -->
    <div class="activities-section">
      <div class="section-header">
        <h3>최근 활동</h3>
        <button @click="showAllActivities = !showAllActivities" class="toggle-btn">
          {{ showAllActivities ? '간략히' : '전체 보기' }}
        </button>
      </div>
      
      <div class="activities-list">
        <div 
          v-for="(activity, index) in displayedActivities" 
          :key="index"
          class="activity-item"
        >
          <div class="activity-icon" :class="getActivityIconClass(activity.type)">
            <i :class="getActivityIcon(activity.type)"></i>
          </div>
          <div class="activity-content">
            <h4>{{ activity.title }}</h4>
            <p>{{ activity.description }}</p>
            <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
          </div>
          <div v-if="activity.status" class="activity-status" :class="activity.status">
            {{ getStatusText(activity.status) }}
          </div>
        </div>

        <div v-if="!recentActivities.length && !loading" class="empty-activities">
          <i class="icon-empty"></i>
          <p>최근 활동이 없습니다.</p>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <h3>빠른 작업</h3>
      <div class="actions-grid">
        <button @click="bulkInboundProcess" class="action-card inbound">
          <i class="icon-inbox"></i>
          <span>일괄 입고</span>
          <small>{{ warehouseStore.pendingReceiptInventory.length }}개 대기</small>
        </button>

        <button @click="bulkOutboundProcess" class="action-card outbound">
          <i class="icon-truck"></i>
          <span>일괄 출고</span>
          <small>{{ warehouseStore.readyForShippingInventory.length }}개 준비</small>
        </button>

        <button @click="generateLabels" class="action-card labels">
          <i class="icon-tag"></i>
          <span>라벨 생성</span>
          <small>QR/바코드</small>
        </button>

        <button @click="inspectionQueue" class="action-card inspection">
          <i class="icon-search"></i>
          <span>검품 대기</span>
          <small>{{ warehouseStore.inspectionPendingInventory.length }}개 항목</small>
        </button>
      </div>
    </div>

    <!-- Alerts -->
    <div v-if="alerts.length" class="alerts-section">
      <h3>알림</h3>
      <div class="alerts-list">
        <div 
          v-for="alert in alerts" 
          :key="alert.id"
          :class="['alert-item', alert.severity]"
        >
          <div class="alert-icon">
            <i :class="getAlertIcon(alert.severity)"></i>
          </div>
          <div class="alert-content">
            <h4>{{ alert.title }}</h4>
            <p>{{ alert.message }}</p>
            <span class="alert-time">{{ formatTime(alert.createdAt) }}</span>
          </div>
          <button @click="dismissAlert(alert.id)" class="alert-dismiss">
            <i class="icon-close"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Loading Overlay -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>데이터를 불러오는 중...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useWarehouseStore } from '../../../stores/warehouse'
import type { WarehouseInventory } from '../../../types'

const router = useRouter()
const warehouseStore = useWarehouseStore()

// State
const loading = ref(false)
const chartPeriod = ref(7)
const showAllActivities = ref(false)
const alerts = ref([
  {
    id: 1,
    title: '보류 항목 증가',
    message: '지난 24시간 동안 보류된 항목이 15개 증가했습니다.',
    severity: 'warning',
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    title: '일일 목표 달성',
    message: '오늘 출고 목표(100개)를 달성했습니다.',
    severity: 'success',
    createdAt: new Date().toISOString()
  }
])

// Chart references
const dailyFlowChart = ref<HTMLCanvasElement | null>(null)
const statusPieChart = ref<HTMLCanvasElement | null>(null)

// Sample activity data
const recentActivities = ref([
  {
    type: 'inbound',
    title: '주문 YCS240827001 입고 완료',
    description: '위치: A1-02-15, 담당자: 김창고',
    timestamp: new Date().toISOString(),
    status: 'success'
  },
  {
    type: 'outbound',
    title: '일괄 출고 처리',
    description: '25개 항목 출고 완료',
    timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    status: 'success'
  },
  {
    type: 'inspection',
    title: '검품 이상 발견',
    description: '주문 YCS240827002 - 박스 손상 확인',
    timestamp: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    status: 'warning'
  },
  {
    type: 'hold',
    title: '항목 보류 처리',
    description: '문서 불일치로 3개 항목 보류',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    status: 'error'
  }
])

// Computed
const displayedActivities = computed(() => {
  return showAllActivities.value ? recentActivities.value : recentActivities.value.slice(0, 5)
})

const statusDistribution = computed(() => [
  {
    label: '입고 대기',
    count: warehouseStore.pendingReceiptInventory.length,
    color: '#fbbf24'
  },
  {
    label: '검품 대기',
    count: warehouseStore.inspectionPendingInventory.length,
    color: '#3b82f6'
  },
  {
    label: '출고 준비',
    count: warehouseStore.readyForShippingInventory.length,
    color: '#10b981'
  },
  {
    label: '보류',
    count: warehouseStore.onHoldInventory.length,
    color: '#ef4444'
  },
  {
    label: '손상',
    count: warehouseStore.damagedInventory.length,
    color: '#8b5cf6'
  }
])

// Methods
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      warehouseStore.fetchWarehouseInventory(),
      warehouseStore.fetchWarehouseStatistics(1, 
        new Date(Date.now() - chartPeriod.value * 24 * 60 * 60 * 1000).toISOString(),
        new Date().toISOString()
      )
    ])
    await updateChartData()
  } catch (error) {
    console.error('Failed to refresh data:', error)
  } finally {
    loading.value = false
  }
}

const navigateToScan = () => {
  router.push('/warehouse/scan')
}

const updateChartData = async () => {
  await nextTick()
  drawDailyFlowChart()
  drawStatusPieChart()
}

const drawDailyFlowChart = () => {
  const canvas = dailyFlowChart.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // Clear canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // Sample data - replace with real data
  const days = Array.from({ length: chartPeriod.value }, (_, i) => {
    const date = new Date()
    date.setDate(date.getDate() - (chartPeriod.value - 1 - i))
    return date.getDate()
  })

  const inboundData = Array.from({ length: chartPeriod.value }, () => 
    Math.floor(Math.random() * 50) + 10
  )
  const outboundData = Array.from({ length: chartPeriod.value }, () => 
    Math.floor(Math.random() * 40) + 15
  )

  // Chart dimensions
  const padding = 40
  const chartWidth = canvas.width - 2 * padding
  const chartHeight = canvas.height - 2 * padding
  const maxValue = Math.max(...inboundData, ...outboundData)

  // Draw axes
  ctx.strokeStyle = '#e5e7eb'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(padding, padding)
  ctx.lineTo(padding, canvas.height - padding)
  ctx.lineTo(canvas.width - padding, canvas.height - padding)
  ctx.stroke()

  // Draw bars
  const barWidth = chartWidth / (days.length * 2)
  
  days.forEach((day, index) => {
    const x = padding + (index * 2 + 0.5) * barWidth
    const inboundHeight = (inboundData[index] / maxValue) * chartHeight
    const outboundHeight = (outboundData[index] / maxValue) * chartHeight

    // Inbound bars
    ctx.fillStyle = '#3b82f6'
    ctx.fillRect(x, canvas.height - padding - inboundHeight, barWidth * 0.8, inboundHeight)

    // Outbound bars
    ctx.fillStyle = '#10b981'
    ctx.fillRect(x + barWidth, canvas.height - padding - outboundHeight, barWidth * 0.8, outboundHeight)

    // Day labels
    ctx.fillStyle = '#6b7280'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText(day.toString(), x + barWidth, canvas.height - padding + 20)
  })

  // Legend
  ctx.fillStyle = '#3b82f6'
  ctx.fillRect(padding, 20, 15, 15)
  ctx.fillStyle = '#000'
  ctx.font = '12px sans-serif'
  ctx.textAlign = 'left'
  ctx.fillText('입고', padding + 20, 32)

  ctx.fillStyle = '#10b981'
  ctx.fillRect(padding + 60, 20, 15, 15)
  ctx.fillText('출고', padding + 80, 32)
}

const drawStatusPieChart = () => {
  const canvas = statusPieChart.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  const total = statusDistribution.value.reduce((sum, item) => sum + item.count, 0)
  if (total === 0) return

  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  const radius = Math.min(centerX, centerY) - 20

  let currentAngle = -Math.PI / 2

  statusDistribution.value.forEach(item => {
    const sliceAngle = (item.count / total) * 2 * Math.PI

    ctx.fillStyle = item.color
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle)
    ctx.closePath()
    ctx.fill()

    currentAngle += sliceAngle
  })
}

const bulkInboundProcess = () => {
  // Navigate to bulk inbound processing
  router.push('/warehouse/bulk-inbound')
}

const bulkOutboundProcess = () => {
  // Navigate to bulk outbound processing
  router.push('/warehouse/bulk-outbound')
}

const generateLabels = () => {
  // Navigate to label generation
  router.push('/warehouse/labels')
}

const inspectionQueue = () => {
  // Navigate to inspection queue
  router.push('/warehouse/inspection')
}

const dismissAlert = (alertId: number) => {
  const index = alerts.value.findIndex(alert => alert.id === alertId)
  if (index !== -1) {
    alerts.value.splice(index, 1)
  }
}

const getActivityIconClass = (type: string) => {
  const classes: Record<string, string> = {
    'inbound': 'inbound',
    'outbound': 'outbound', 
    'inspection': 'inspection',
    'hold': 'hold',
    'damage': 'damage'
  }
  return classes[type] || 'default'
}

const getActivityIcon = (type: string) => {
  const icons: Record<string, string> = {
    'inbound': 'icon-arrow-down',
    'outbound': 'icon-arrow-up',
    'inspection': 'icon-search',
    'hold': 'icon-pause',
    'damage': 'icon-alert'
  }
  return icons[type] || 'icon-info'
}

const getAlertIcon = (severity: string) => {
  const icons: Record<string, string> = {
    'success': 'icon-check-circle',
    'warning': 'icon-warning',
    'error': 'icon-alert-circle',
    'info': 'icon-info-circle'
  }
  return icons[severity] || 'icon-info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'success': '완료',
    'warning': '주의',
    'error': '오류',
    'pending': '대기'
  }
  return statusMap[status] || status
}

const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  return `${days}일 전`
}

// Lifecycle
onMounted(async () => {
  await refreshData()
})

// Auto refresh every 5 minutes
const refreshInterval = setInterval(refreshData, 5 * 60 * 1000)

onUnmounted(() => {
  clearInterval(refreshInterval)
})
</script>

<style scoped>
.warehouse-dashboard {
  padding: 1rem;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.header-info .page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: var(--text-primary);
}

.warehouse-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.refresh-btn,
.scan-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
  font-weight: 500;
}

.refresh-btn:hover,
.scan-btn:hover {
  background: var(--bg-secondary);
}

.scan-btn {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.scan-btn:hover {
  background: var(--primary-dark);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Stats Overview */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.75rem;
  font-size: 1.5rem;
  color: white;
}

.stat-card.total .stat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card.inbound .stat-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-card.ready .stat-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-card.issues .stat-icon { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.stat-content {
  flex: 1;
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
  color: var(--text-primary);
}

.stat-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.stat-trend {
  text-align: right;
}

.trend-value {
  font-size: 0.8rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.trend-value.positive {
  background: var(--success-light);
  color: var(--success-dark);
}

.trend-value.negative {
  background: var(--danger-light);
  color: var(--danger-dark);
}

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.chart-card {
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.chart-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.period-select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.chart-container canvas {
  max-width: 100%;
  height: auto;
}

.status-legend {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 0.25rem;
}

.legend-label {
  flex: 1;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.legend-value {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

/* Activities Section */
.activities-section {
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.toggle-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 0.5rem;
  border-left: 4px solid transparent;
}

.activity-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 1.1rem;
  color: white;
}

.activity-icon.inbound { background: var(--info-color); }
.activity-icon.outbound { background: var(--success-color); }
.activity-icon.inspection { background: var(--warning-color); }
.activity-icon.hold { background: var(--danger-color); }
.activity-icon.default { background: var(--secondary-color); }

.activity-content {
  flex: 1;
}

.activity-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.activity-content p {
  margin: 0 0 0.25rem 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.activity-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.activity-status {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.activity-status.success {
  background: var(--success-light);
  color: var(--success-dark);
}

.activity-status.warning {
  background: var(--warning-light);
  color: var(--warning-dark);
}

.activity-status.error {
  background: var(--danger-light);
  color: var(--danger-dark);
}

.empty-activities {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.empty-activities i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

/* Quick Actions */
.quick-actions h3 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  background: white;
  border: 2px solid var(--border-color);
  border-radius: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.action-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-card i {
  font-size: 1.5rem;
  color: var(--text-secondary);
}

.action-card span {
  font-weight: 500;
  color: var(--text-primary);
}

.action-card small {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.action-card.inbound:hover { border-color: var(--info-color); }
.action-card.outbound:hover { border-color: var(--success-color); }
.action-card.labels:hover { border-color: var(--warning-color); }
.action-card.inspection:hover { border-color: var(--primary-color); }

/* Alerts Section */
.alerts-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 0.5rem;
  border-left: 4px solid;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.alert-item.success { border-left-color: var(--success-color); background: var(--success-light); }
.alert-item.warning { border-left-color: var(--warning-color); background: var(--warning-light); }
.alert-item.error { border-left-color: var(--danger-color); background: var(--danger-light); }
.alert-item.info { border-left-color: var(--info-color); background: var(--info-light); }

.alert-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 1.1rem;
}

.alert-item.success .alert-icon { background: var(--success-color); color: white; }
.alert-item.warning .alert-icon { background: var(--warning-color); color: white; }
.alert-item.error .alert-icon { background: var(--danger-color); color: white; }
.alert-item.info .alert-icon { background: var(--info-color); color: white; }

.alert-content {
  flex: 1;
}

.alert-content h4 {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.alert-content p {
  margin: 0 0 0.25rem 0;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.alert-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.alert-dismiss {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.25rem;
}

.alert-dismiss:hover {
  background: rgba(0, 0, 0, 0.1);
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .warehouse-dashboard {
    padding: 0.5rem;
    gap: 1.5rem;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .stat-card {
    padding: 1rem;
  }

  .stat-content h3 {
    font-size: 1.5rem;
  }

  .chart-card {
    padding: 1rem;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-card {
    padding: 1rem;
  }

  .activities-section,
  .quick-actions,
  .alerts-section {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>