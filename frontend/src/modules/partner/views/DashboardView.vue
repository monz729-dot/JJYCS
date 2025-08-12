<template>
  <div class="partner-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <div class="header-info">
        <h1>파트너 대시보드</h1>
        <p class="current-time">{{ currentDateTime }}</p>
      </div>
      <div class="header-actions">
        <button @click="refreshData" class="btn-refresh" :disabled="loading">
          <svg v-if="loading" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          새로고침
        </button>
      </div>
    </div>

    <!-- 통계 카드 그리드 -->
    <div class="stats-grid">
      <div class="stat-card total-referrals">
        <div class="stat-icon">
          <svg class="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalReferrals.toLocaleString() }}</div>
          <div class="stat-label">총 추천 고객</div>
          <div class="stat-change positive" v-if="stats.thisMonthReferrals > 0">
            이번달 +{{ stats.thisMonthReferrals }}
          </div>
        </div>
      </div>

      <div class="stat-card active-customers">
        <div class="stat-icon">
          <svg class="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeCustomers.toLocaleString() }}</div>
          <div class="stat-label">활성 고객</div>
          <div class="stat-change positive">
            전환율 {{ stats.conversionRate.toFixed(1) }}%
          </div>
        </div>
      </div>

      <div class="stat-card total-commission">
        <div class="stat-icon">
          <svg class="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">₩{{ stats.totalCommission.toLocaleString() }}</div>
          <div class="stat-label">총 수수료</div>
          <div class="stat-change positive">
            이번달 ₩{{ stats.thisMonthCommission.toLocaleString() }}
          </div>
        </div>
      </div>

      <div class="stat-card pending-payment">
        <div class="stat-icon">
          <svg class="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">₩{{ stats.pendingPayment.toLocaleString() }}</div>
          <div class="stat-label">정산 대기</div>
          <div class="stat-change neutral">
            {{ pendingCommissions.length }}건 대기 중
          </div>
        </div>
      </div>
    </div>

    <!-- 성과 차트 섹션 -->
    <div class="dashboard-section">
      <div class="section-header">
        <h2>월별 성과</h2>
        <div class="period-selector">
          <select v-model="selectedPeriod" class="select-period">
            <option value="6months">최근 6개월</option>
            <option value="3months">최근 3개월</option>
            <option value="1month">이번 달</option>
          </select>
        </div>
      </div>
      
      <div class="chart-container">
        <div class="chart-placeholder">
          <svg class="chart-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
          </svg>
          <p>성과 차트 (차트 라이브러리 연동 예정)</p>
        </div>
      </div>
    </div>

    <div class="dashboard-row">
      <!-- 최근 추천 고객 -->
      <div class="dashboard-section recent-referrals">
        <div class="section-header">
          <h2>최근 추천 고객</h2>
          <router-link to="/partner/referrals" class="view-all-link">
            전체 보기
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </router-link>
        </div>
        
        <div class="referrals-list">
          <div v-for="referral in recentReferrals" :key="referral.id" class="referral-item">
            <div class="referral-avatar">
              <span>{{ referral.customerName.charAt(0) }}</span>
            </div>
            <div class="referral-info">
              <div class="referral-name">{{ referral.customerName }}</div>
              <div class="referral-date">{{ formatDate(referral.signupDate) }}</div>
            </div>
            <div class="referral-stats">
              <div class="referral-orders">{{ referral.ordersCount }}건</div>
              <div class="referral-commission">₩{{ referral.commissionEarned.toLocaleString() }}</div>
            </div>
            <div class="referral-status">
              <span :class="['status-badge', `status-${referral.status}`]">
                {{ getStatusText(referral.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 최근 수수료 -->
      <div class="dashboard-section recent-commissions">
        <div class="section-header">
          <h2>최근 수수료</h2>
          <router-link to="/partner/commissions" class="view-all-link">
            전체 보기
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </router-link>
        </div>
        
        <div class="commissions-list">
          <div v-for="commission in recentCommissions" :key="commission.id" class="commission-item">
            <div class="commission-order">
              <div class="order-id">{{ commission.orderId }}</div>
              <div class="order-customer">{{ commission.customerName }}</div>
            </div>
            <div class="commission-amount">
              <div class="order-amount">₩{{ commission.orderAmount.toLocaleString() }}</div>
              <div class="commission-rate">{{ commission.commissionRate }}%</div>
            </div>
            <div class="commission-earned">
              ₩{{ commission.commissionAmount.toLocaleString() }}
            </div>
            <div class="commission-status">
              <span :class="['status-badge', `status-${commission.status}`]">
                {{ getCommissionStatusText(commission.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 빠른 작업 -->
    <div class="dashboard-section quick-actions">
      <div class="section-header">
        <h2>빠른 작업</h2>
      </div>
      
      <div class="quick-actions-grid">
        <router-link to="/partner/links" class="quick-action-card">
          <div class="action-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
            </svg>
          </div>
          <div class="action-content">
            <div class="action-title">추천 링크 생성</div>
            <div class="action-desc">새로운 추천 링크를 만들어 공유하세요</div>
          </div>
        </router-link>

        <router-link to="/partner/settlements" class="quick-action-card">
          <div class="action-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
            </svg>
          </div>
          <div class="action-content">
            <div class="action-title">정산 요청</div>
            <div class="action-desc">수수료 정산을 요청하세요</div>
          </div>
        </router-link>

        <a href="#" @click.prevent="downloadMarketingKit" class="quick-action-card">
          <div class="action-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
          </div>
          <div class="action-content">
            <div class="action-title">마케팅 키트</div>
            <div class="action-desc">배너, 이메일 템플릿 다운로드</div>
          </div>
        </a>

        <a href="#" @click.prevent="shareReferralLink" class="quick-action-card">
          <div class="action-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z"></path>
            </svg>
          </div>
          <div class="action-content">
            <div class="action-title">소셜 공유</div>
            <div class="action-desc">SNS에 추천 링크를 공유하세요</div>
          </div>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usePartnerStore } from '../stores/partnerStore'

const partnerStore = usePartnerStore()

const currentDateTime = ref('')
const selectedPeriod = ref('6months')

// Computed
const { stats, referrals, commissions, loading, pendingCommissions } = partnerStore

const recentReferrals = computed(() => 
  referrals.slice(0, 5).sort((a, b) => 
    new Date(b.signupDate).getTime() - new Date(a.signupDate).getTime()
  )
)

const recentCommissions = computed(() =>
  commissions.slice(0, 5).sort((a, b) => 
    new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime()
  )
)

// Methods
const updateDateTime = () => {
  currentDateTime.value = new Date().toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const refreshData = async () => {
  await partnerStore.fetchStats()
  updateDateTime()
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric'
  })
}

const getStatusText = (status: string) => {
  const statusMap = {
    'pending': '대기',
    'active': '활성',
    'inactive': '비활성'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const getCommissionStatusText = (status: string) => {
  const statusMap = {
    'pending': '대기',
    'approved': '승인',
    'paid': '지급완료'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const downloadMarketingKit = () => {
  // Simulate download
  const link = document.createElement('a')
  link.href = '#'
  link.download = 'ycs-marketing-kit.zip'
  link.click()
}

const shareReferralLink = () => {
  if (navigator.share) {
    navigator.share({
      title: 'YCS LMS 추천',
      text: 'YCS LMS로 물류를 더 스마트하게 관리하세요!',
      url: 'https://ycs.logistics.com/signup?ref=PARTNER001'
    })
  } else {
    // Fallback to copy to clipboard
    navigator.clipboard.writeText('https://ycs.logistics.com/signup?ref=PARTNER001')
    alert('추천 링크가 클립보드에 복사되었습니다!')
  }
}

onMounted(() => {
  updateDateTime()
  setInterval(updateDateTime, 60000) // Update every minute
})
</script>

<style scoped>
.partner-dashboard {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.current-time {
  color: #6b7280;
  font-size: 14px;
  margin: 4px 0 0 0;
}

.btn-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refresh:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.stat-card.total-referrals .stat-icon {
  background: #dbeafe;
  color: #1d4ed8;
}

.stat-card.active-customers .stat-icon {
  background: #d1fae5;
  color: #059669;
}

.stat-card.total-commission .stat-icon {
  background: #fef3c7;
  color: #d97706;
}

.stat-card.pending-payment .stat-icon {
  background: #e0e7ff;
  color: #5b21b6;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  font-weight: 600;
}

.stat-change.positive {
  color: #059669;
}

.stat-change.neutral {
  color: #6b7280;
}

.dashboard-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.view-all-link:hover {
  color: #1d4ed8;
}

.select-period {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
}

.chart-placeholder {
  text-align: center;
  color: #6b7280;
}

.chart-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
}

.dashboard-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .dashboard-row {
    grid-template-columns: 1fr;
  }
}

.referrals-list,
.commissions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.referral-item,
.commission-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.referral-avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
}

.referral-info,
.commission-order {
  flex: 1;
}

.referral-name,
.order-id {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.referral-date,
.order-customer {
  font-size: 12px;
  color: #6b7280;
}

.referral-stats {
  text-align: right;
  font-size: 12px;
}

.referral-orders {
  color: #6b7280;
}

.referral-commission,
.commission-earned {
  font-weight: 600;
  color: #059669;
}

.commission-amount {
  text-align: right;
  font-size: 12px;
}

.order-amount {
  font-weight: 600;
  color: #111827;
}

.commission-rate {
  color: #6b7280;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-active,
.status-badge.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-inactive {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.status-paid {
  background: #dbeafe;
  color: #1e40af;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.quick-action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
}

.quick-action-card:hover {
  background: #f3f4f6;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #6b7280;
}

@media (max-width: 768px) {
  .partner-dashboard {
    padding: 16px;
  }
  
  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>