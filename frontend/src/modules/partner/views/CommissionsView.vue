<template>
  <div class="commissions-view">
    <!-- 헤더 -->
    <div class="view-header">
      <div class="header-info">
        <h1>수수료 관리</h1>
        <p>수수료 내역과 수익을 추적하고 관리하세요</p>
      </div>
      <div class="header-actions">
        <button @click="exportCommissions" class="btn-secondary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          내보내기
        </button>
        <router-link to="/partner/settlements" class="btn-primary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
          </svg>
          정산 요청
        </router-link>
      </div>
    </div>

    <!-- 수수료 요약 -->
    <div class="commission-summary">
      <div class="summary-card total">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
            </svg>
          </div>
          <h3>총 수수료</h3>
        </div>
        <div class="card-value">₩{{ stats.totalCommission.toLocaleString() }}</div>
        <div class="card-change positive">+₩{{ stats.thisMonthCommission.toLocaleString() }} 이번달</div>
      </div>

      <div class="summary-card pending">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h3>대기 중</h3>
        </div>
        <div class="card-value">₩{{ pendingCommissionAmount.toLocaleString() }}</div>
        <div class="card-change">{{ pendingCommissions.length }}건</div>
      </div>

      <div class="summary-card approved">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h3>승인됨</h3>
        </div>
        <div class="card-value">₩{{ approvedCommissionAmount.toLocaleString() }}</div>
        <div class="card-change">{{ approvedCommissions.length }}건</div>
      </div>

      <div class="summary-card paid">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h3>지급완료</h3>
        </div>
        <div class="card-value">₩{{ paidCommissionAmount.toLocaleString() }}</div>
        <div class="card-change">{{ paidCommissions.length }}건</div>
      </div>
    </div>

    <!-- 수수료 차트 -->
    <div class="chart-section">
      <div class="chart-header">
        <h2>월별 수수료 추이</h2>
        <select v-model="chartPeriod" class="period-select">
          <option value="6months">최근 6개월</option>
          <option value="12months">최근 12개월</option>
          <option value="ytd">올해</option>
        </select>
      </div>
      
      <div class="chart-container">
        <div class="chart-placeholder">
          <svg class="chart-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"></path>
          </svg>
          <p>수수료 차트 (차트 라이브러리 연동 예정)</p>
          <div class="chart-data">
            <div class="data-point">
              <span class="month">1월</span>
              <span class="amount">₩{{ Math.floor(stats.totalCommission * 0.15).toLocaleString() }}</span>
            </div>
            <div class="data-point">
              <span class="month">2월</span>
              <span class="amount">₩{{ Math.floor(stats.totalCommission * 0.18).toLocaleString() }}</span>
            </div>
            <div class="data-point">
              <span class="month">3월</span>
              <span class="amount">₩{{ Math.floor(stats.totalCommission * 0.22).toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 -->
    <div class="filters-section">
      <div class="filters-row">
        <div class="search-box">
          <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input 
            v-model="searchQuery"
            type="text" 
            placeholder="주문번호, 고객명으로 검색..."
            class="search-input"
          />
        </div>
        
        <select v-model="statusFilter" class="filter-select">
          <option value="">전체 상태</option>
          <option value="pending">대기</option>
          <option value="approved">승인</option>
          <option value="paid">지급완료</option>
        </select>
        
        <select v-model="periodFilter" class="filter-select">
          <option value="">전체 기간</option>
          <option value="thisMonth">이번 달</option>
          <option value="lastMonth">지난 달</option>
          <option value="thisQuarter">이번 분기</option>
          <option value="lastQuarter">지난 분기</option>
        </select>

        <select v-model="sortBy" class="filter-select">
          <option value="orderDate">주문일순</option>
          <option value="commissionAmount">수수료순</option>
          <option value="orderAmount">주문금액순</option>
        </select>

        <select v-model="sortOrder" class="filter-select">
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>
    </div>

    <!-- 수수료 테이블 -->
    <div class="commissions-table-section">
      <div v-if="loading" class="loading-state">
        <svg class="animate-spin h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p>데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="filteredCommissions.length === 0" class="empty-state">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
        </svg>
        <h3>수수료 내역이 없습니다</h3>
        <p>첫 번째 고객을 추천하여 수수료를 받아보세요!</p>
        <router-link to="/partner/links" class="btn-primary">추천 시작하기</router-link>
      </div>

      <div v-else class="commissions-table">
        <div class="table-header">
          <div class="table-cell order">주문 정보</div>
          <div class="table-cell customer">고객</div>
          <div class="table-cell date">주문일</div>
          <div class="table-cell amount">주문 금액</div>
          <div class="table-cell rate">수수료율</div>
          <div class="table-cell commission">수수료</div>
          <div class="table-cell status">상태</div>
          <div class="table-cell actions">작업</div>
        </div>

        <div v-for="commission in paginatedCommissions" :key="commission.id" class="table-row">
          <div class="table-cell order">
            <div class="order-info">
              <div class="order-id">{{ commission.orderId }}</div>
              <div class="referral-info">추천 ID: {{ commission.referralId.slice(-3) }}</div>
            </div>
          </div>

          <div class="table-cell customer">
            <div class="customer-name">{{ commission.customerName }}</div>
          </div>

          <div class="table-cell date">
            <div class="order-date">{{ formatDate(commission.orderDate) }}</div>
            <div class="date-period">{{ getTimePeriod(commission.orderDate) }}</div>
          </div>

          <div class="table-cell amount">
            <div class="order-amount">₩{{ commission.orderAmount.toLocaleString() }}</div>
          </div>

          <div class="table-cell rate">
            <div class="commission-rate">{{ commission.commissionRate }}%</div>
          </div>

          <div class="table-cell commission">
            <div class="commission-amount">₩{{ commission.commissionAmount.toLocaleString() }}</div>
          </div>

          <div class="table-cell status">
            <span :class="['status-badge', `status-${commission.status}`]">
              {{ getStatusText(commission.status) }}
            </span>
            <div v-if="commission.paidDate" class="paid-date">
              {{ formatDate(commission.paidDate) }}
            </div>
          </div>

          <div class="table-cell actions">
            <div class="action-buttons">
              <button 
                @click="viewCommissionDetail(commission)"
                class="btn-action"
                title="상세보기"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
              <button 
                v-if="commission.status === 'pending'"
                @click="requestApproval(commission)"
                class="btn-action request"
                title="승인 요청"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="currentPage = Math.max(1, currentPage - 1)"
          :disabled="currentPage === 1"
          class="pagination-btn"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </button>

        <div class="pagination-info">
          {{ currentPage }} / {{ totalPages }}
        </div>

        <button 
          @click="currentPage = Math.min(totalPages, currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="pagination-btn"
        >
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
          </svg>
        </button>
      </div>
    </div>

    <!-- 수수료 상세 모달 -->
    <div v-if="selectedCommission" class="modal-overlay" @click="closeCommissionModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>수수료 상세 정보</h3>
          <button @click="closeCommissionModal" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="commission-details-grid">
            <div class="detail-item">
              <label>주문 번호</label>
              <span>{{ selectedCommission.orderId }}</span>
            </div>
            <div class="detail-item">
              <label>고객명</label>
              <span>{{ selectedCommission.customerName }}</span>
            </div>
            <div class="detail-item">
              <label>주문일</label>
              <span>{{ formatDate(selectedCommission.orderDate) }}</span>
            </div>
            <div class="detail-item">
              <label>주문 금액</label>
              <span>₩{{ selectedCommission.orderAmount.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>수수료율</label>
              <span>{{ selectedCommission.commissionRate }}%</span>
            </div>
            <div class="detail-item">
              <label>수수료</label>
              <span class="commission-highlight">₩{{ selectedCommission.commissionAmount.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>상태</label>
              <span :class="['status-badge', `status-${selectedCommission.status}`]">
                {{ getStatusText(selectedCommission.status) }}
              </span>
            </div>
            <div class="detail-item" v-if="selectedCommission.paidDate">
              <label>지급일</label>
              <span>{{ formatDate(selectedCommission.paidDate) }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button 
            v-if="selectedCommission.status === 'pending'"
            @click="requestApproval(selectedCommission)"
            class="btn-primary"
          >
            승인 요청
          </button>
          <button @click="closeCommissionModal" class="btn-secondary">
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usePartnerStore, type Commission } from '../stores/partnerStore'

const partnerStore = usePartnerStore()

// Reactive data
const searchQuery = ref('')
const statusFilter = ref('')
const periodFilter = ref('')
const sortBy = ref('orderDate')
const sortOrder = ref('desc')
const currentPage = ref(1)
const itemsPerPage = ref(10)
const selectedCommission = ref<Commission | null>(null)
const chartPeriod = ref('6months')

// Store data
const { commissions, stats, loading } = partnerStore

// Computed properties
const pendingCommissions = computed(() => 
  commissions.filter(c => c.status === 'pending')
)

const approvedCommissions = computed(() =>
  commissions.filter(c => c.status === 'approved')
)

const paidCommissions = computed(() =>
  commissions.filter(c => c.status === 'paid')
)

const pendingCommissionAmount = computed(() =>
  pendingCommissions.value.reduce((sum, c) => sum + c.commissionAmount, 0)
)

const approvedCommissionAmount = computed(() =>
  approvedCommissions.value.reduce((sum, c) => sum + c.commissionAmount, 0)
)

const paidCommissionAmount = computed(() =>
  paidCommissions.value.reduce((sum, c) => sum + c.commissionAmount, 0)
)

const filteredCommissions = computed(() => {
  let filtered = commissions

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(commission => 
      commission.orderId.toLowerCase().includes(query) ||
      commission.customerName.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(commission => commission.status === statusFilter.value)
  }

  // Apply period filter
  if (periodFilter.value) {
    const now = new Date()
    const filterDate = new Date(now.getFullYear(), now.getMonth(), 1)
    
    switch (periodFilter.value) {
      case 'thisMonth':
        filtered = filtered.filter(commission => {
          const orderDate = new Date(commission.orderDate)
          return orderDate >= filterDate
        })
        break
      case 'lastMonth':
        filterDate.setMonth(filterDate.getMonth() - 1)
        const lastMonthEnd = new Date(now.getFullYear(), now.getMonth(), 0)
        filtered = filtered.filter(commission => {
          const orderDate = new Date(commission.orderDate)
          return orderDate >= filterDate && orderDate <= lastMonthEnd
        })
        break
      case 'thisQuarter':
        const quarterStart = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1)
        filtered = filtered.filter(commission => {
          const orderDate = new Date(commission.orderDate)
          return orderDate >= quarterStart
        })
        break
      case 'lastQuarter':
        const lastQuarterStart = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3 - 3, 1)
        const lastQuarterEnd = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 0)
        filtered = filtered.filter(commission => {
          const orderDate = new Date(commission.orderDate)
          return orderDate >= lastQuarterStart && orderDate <= lastQuarterEnd
        })
        break
    }
  }

  // Apply sorting
  filtered.sort((a, b) => {
    let aVal: any = a[sortBy.value as keyof Commission]
    let bVal: any = b[sortBy.value as keyof Commission]

    if (sortBy.value === 'orderDate') {
      aVal = new Date(aVal).getTime()
      bVal = new Date(bVal).getTime()
    }

    if (sortOrder.value === 'desc') {
      return bVal > aVal ? 1 : -1
    } else {
      return aVal > bVal ? 1 : -1
    }
  })

  return filtered
})

const totalPages = computed(() => 
  Math.ceil(filteredCommissions.value.length / itemsPerPage.value)
)

const paginatedCommissions = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredCommissions.value.slice(start, end)
})

// Methods
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const getTimePeriod = (dateString: string) => {
  const orderDate = new Date(dateString)
  const now = new Date()
  const diffInDays = Math.floor((now.getTime() - orderDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffInDays < 7) return '이번주'
  if (diffInDays < 30) return '이번달'
  if (diffInDays < 90) return '최근 3개월'
  return '3개월 이전'
}

const getStatusText = (status: string) => {
  const statusMap = {
    'pending': '대기',
    'approved': '승인',
    'paid': '지급완료'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const viewCommissionDetail = (commission: Commission) => {
  selectedCommission.value = commission
}

const closeCommissionModal = () => {
  selectedCommission.value = null
}

const requestApproval = async (commission: Commission) => {
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // Update commission status (in real app, this would be handled by the API)
    const index = commissions.findIndex(c => c.id === commission.id)
    if (index !== -1) {
      commissions[index].status = 'approved'
    }
    
    alert('승인 요청이 완료되었습니다.')
    closeCommissionModal()
  } catch (error) {
    alert('승인 요청에 실패했습니다.')
  }
}

const exportCommissions = () => {
  // Create CSV content
  const headers = ['주문번호', '고객명', '주문일', '주문금액', '수수료율', '수수료', '상태']
  const csvContent = [
    headers.join(','),
    ...filteredCommissions.value.map(commission => [
      commission.orderId,
      commission.customerName,
      commission.orderDate,
      commission.orderAmount,
      `${commission.commissionRate}%`,
      commission.commissionAmount,
      getStatusText(commission.status)
    ].join(','))
  ].join('\n')

  // Download CSV
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `commissions_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

onMounted(() => {
  // Data is already loaded from the store
})
</script>

<style scoped>
.commissions-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.header-info p {
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-primary, .btn-secondary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.btn-primary:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-secondary {
  background: white;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.commission-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.summary-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.summary-card.total .card-icon {
  background: #3b82f6;
}

.summary-card.pending .card-icon {
  background: #f59e0b;
}

.summary-card.approved .card-icon {
  background: #10b981;
}

.summary-card.paid .card-icon {
  background: #8b5cf6;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.card-change {
  font-size: 14px;
  color: #6b7280;
}

.card-change.positive {
  color: #059669;
}

.chart-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.period-select {
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
  flex-direction: column;
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

.chart-data {
  display: flex;
  gap: 32px;
  margin-top: 20px;
}

.data-point {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.month {
  font-size: 12px;
  color: #6b7280;
}

.amount {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.filters-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
}

.filters-row {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #9ca3af;
}

.search-input {
  width: 100%;
  padding: 8px 8px 8px 40px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.commissions-table-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #6b7280;
  margin: 0 0 24px 0;
}

.commissions-table {
  display: flex;
  flex-direction: column;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 1fr 0.8fr 0.8fr;
  gap: 16px;
  align-items: center;
}

.table-header {
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  color: #374151;
  font-size: 13px;
  padding: 16px 20px;
}

.table-row {
  border-bottom: 1px solid #f3f4f6;
  padding: 16px 20px;
  transition: background-color 0.2s;
}

.table-row:hover {
  background: #f9fafb;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-id {
  font-weight: 600;
  color: #111827;
}

.referral-info {
  font-size: 12px;
  color: #6b7280;
}

.customer-name {
  font-weight: 500;
  color: #111827;
}

.order-date {
  font-weight: 500;
  color: #111827;
  margin-bottom: 2px;
}

.date-period {
  font-size: 12px;
  color: #6b7280;
}

.order-amount, .commission-amount {
  font-weight: 600;
  color: #111827;
}

.commission-amount {
  color: #059669;
}

.commission-rate {
  font-weight: 600;
  color: #3b82f6;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 2px;
  display: inline-block;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-approved {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-paid {
  background: #dbeafe;
  color: #1e40af;
}

.paid-date {
  font-size: 11px;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-action {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action:hover {
  color: #3b82f6;
  border-color: #3b82f6;
}

.btn-action.request:hover {
  color: #059669;
  border-color: #059669;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #e5e7eb;
}

.pagination-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  color: #3b82f6;
  border-color: #3b82f6;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

/* Modal styles */
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
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #111827;
}

.modal-body {
  padding: 24px;
}

.commission-details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.detail-item span {
  font-size: 14px;
  color: #111827;
}

.commission-highlight {
  color: #059669;
  font-weight: 600;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px 24px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .commissions-view {
    padding: 16px;
  }
  
  .view-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .commission-summary {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .filters-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .table-header, .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .table-header {
    display: none;
  }
  
  .table-row {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 20px;
  }
  
  .commission-details-grid {
    grid-template-columns: 1fr;
  }

  .chart-data {
    flex-direction: column;
    gap: 16px;
  }
}
</style>