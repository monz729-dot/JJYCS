<template>
  <div class="referrals-view">
    <!-- 헤더 -->
    <div class="view-header">
      <div class="header-info">
        <h1>추천 관리</h1>
        <p>고객 추천 현황과 전환 정보를 관리하세요</p>
      </div>
      <div class="header-actions">
        <button @click="exportReferrals" class="btn-secondary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          내보내기
        </button>
        <router-link to="/partner/links" class="btn-primary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
          </svg>
          추천 링크 관리
        </router-link>
      </div>
    </div>

    <!-- 요약 통계 -->
    <div class="summary-stats">
      <div class="stat-item">
        <div class="stat-label">총 추천 고객</div>
        <div class="stat-value">{{ stats.totalReferrals }}</div>
        <div class="stat-change positive">+{{ stats.thisMonthReferrals }} 이번달</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">활성 고객</div>
        <div class="stat-value">{{ stats.activeCustomers }}</div>
        <div class="stat-change">{{ conversionRate.toFixed(1) }}% 전환율</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">평균 주문 금액</div>
        <div class="stat-value">₩{{ stats.averageOrderValue.toLocaleString() }}</div>
        <div class="stat-change positive">+12% 전월 대비</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">이번달 수수료</div>
        <div class="stat-value">₩{{ thisMonthEarnings.toLocaleString() }}</div>
        <div class="stat-change positive">+{{ ((thisMonthEarnings / (stats.totalCommission - thisMonthEarnings)) * 100).toFixed(1) }}%</div>
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
            placeholder="고객명, 이메일로 검색..."
            class="search-input"
          />
        </div>
        
        <select v-model="statusFilter" class="filter-select">
          <option value="">전체 상태</option>
          <option value="active">활성</option>
          <option value="pending">대기</option>
          <option value="inactive">비활성</option>
        </select>
        
        <select v-model="sortBy" class="filter-select">
          <option value="signupDate">가입일순</option>
          <option value="commissionEarned">수수료순</option>
          <option value="ordersCount">주문수순</option>
          <option value="lastOrderDate">최근주문순</option>
        </select>

        <select v-model="sortOrder" class="filter-select">
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>
    </div>

    <!-- 추천 고객 테이블 -->
    <div class="referrals-table-section">
      <div v-if="loading" class="loading-state">
        <svg class="animate-spin h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p>데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="filteredReferrals.length === 0" class="empty-state">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
        </svg>
        <h3>추천 고객이 없습니다</h3>
        <p>첫 번째 고객을 추천해보세요!</p>
        <router-link to="/partner/links" class="btn-primary">추천 링크 만들기</router-link>
      </div>

      <div v-else class="referrals-table">
        <div class="table-header">
          <div class="table-cell customer">고객 정보</div>
          <div class="table-cell signup">가입일</div>
          <div class="table-cell orders">주문 현황</div>
          <div class="table-cell spending">총 구매액</div>
          <div class="table-cell commission">수수료</div>
          <div class="table-cell status">상태</div>
          <div class="table-cell actions">작업</div>
        </div>

        <div v-for="referral in paginatedReferrals" :key="referral.id" class="table-row">
          <div class="table-cell customer">
            <div class="customer-info">
              <div class="customer-avatar">
                <span>{{ referral.customerName.charAt(0) }}</span>
              </div>
              <div class="customer-details">
                <div class="customer-name">{{ referral.customerName }}</div>
                <div class="customer-email">{{ referral.customerEmail }}</div>
              </div>
            </div>
          </div>

          <div class="table-cell signup">
            <div class="signup-date">{{ formatDate(referral.signupDate) }}</div>
            <div class="signup-period">{{ getTimePeriod(referral.signupDate) }}</div>
          </div>

          <div class="table-cell orders">
            <div class="orders-count">{{ referral.ordersCount }}건</div>
            <div class="last-order" v-if="referral.lastOrderDate">
              최근: {{ formatDate(referral.lastOrderDate) }}
            </div>
            <div class="last-order" v-else>주문 없음</div>
          </div>

          <div class="table-cell spending">
            <div class="total-spent">₩{{ referral.totalSpent.toLocaleString() }}</div>
            <div class="avg-order">
              평균 ₩{{ referral.ordersCount > 0 ? Math.round(referral.totalSpent / referral.ordersCount).toLocaleString() : '0' }}
            </div>
          </div>

          <div class="table-cell commission">
            <div class="commission-earned">₩{{ referral.commissionEarned.toLocaleString() }}</div>
            <div class="commission-rate">10% 수수료율</div>
          </div>

          <div class="table-cell status">
            <span :class="['status-badge', `status-${referral.status}`]">
              {{ getStatusText(referral.status) }}
            </span>
          </div>

          <div class="table-cell actions">
            <div class="action-buttons">
              <button 
                @click="viewCustomerDetail(referral)"
                class="btn-action"
                title="상세보기"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
              <button 
                @click="contactCustomer(referral)"
                class="btn-action"
                title="연락하기"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
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

    <!-- 고객 상세 모달 -->
    <div v-if="selectedCustomer" class="modal-overlay" @click="closeCustomerModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedCustomer.customerName }} 상세 정보</h3>
          <button @click="closeCustomerModal" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="customer-details-grid">
            <div class="detail-item">
              <label>이메일</label>
              <span>{{ selectedCustomer.customerEmail }}</span>
            </div>
            <div class="detail-item">
              <label>가입일</label>
              <span>{{ formatDate(selectedCustomer.signupDate) }}</span>
            </div>
            <div class="detail-item">
              <label>상태</label>
              <span :class="['status-badge', `status-${selectedCustomer.status}`]">
                {{ getStatusText(selectedCustomer.status) }}
              </span>
            </div>
            <div class="detail-item">
              <label>총 주문수</label>
              <span>{{ selectedCustomer.ordersCount }}건</span>
            </div>
            <div class="detail-item">
              <label>총 구매액</label>
              <span>₩{{ selectedCustomer.totalSpent.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>수수료</label>
              <span>₩{{ selectedCustomer.commissionEarned.toLocaleString() }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="contactCustomer(selectedCustomer)" class="btn-primary">
            이메일 보내기
          </button>
          <button @click="closeCustomerModal" class="btn-secondary">
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usePartnerStore, type Referral } from '../stores/partnerStore'

const partnerStore = usePartnerStore()

// Reactive data
const searchQuery = ref('')
const statusFilter = ref('')
const sortBy = ref('signupDate')
const sortOrder = ref('desc')
const currentPage = ref(1)
const itemsPerPage = ref(10)
const selectedCustomer = ref<Referral | null>(null)

// Store data
const { referrals, stats, loading, conversionRate, thisMonthEarnings } = partnerStore

// Computed properties
const filteredReferrals = computed(() => {
  let filtered = referrals

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(referral => 
      referral.customerName.toLowerCase().includes(query) ||
      referral.customerEmail.toLowerCase().includes(query)
    )
  }

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(referral => referral.status === statusFilter.value)
  }

  // Apply sorting
  filtered.sort((a, b) => {
    let aVal: any = a[sortBy.value as keyof Referral]
    let bVal: any = b[sortBy.value as keyof Referral]

    if (sortBy.value === 'signupDate' || sortBy.value === 'lastOrderDate') {
      aVal = new Date(aVal || 0).getTime()
      bVal = new Date(bVal || 0).getTime()
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
  Math.ceil(filteredReferrals.value.length / itemsPerPage.value)
)

const paginatedReferrals = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredReferrals.value.slice(start, end)
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
  const signupDate = new Date(dateString)
  const now = new Date()
  const diffInDays = Math.floor((now.getTime() - signupDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffInDays < 7) return '이번주'
  if (diffInDays < 30) return '이번달'
  if (diffInDays < 90) return '최근 3개월'
  return '3개월 이전'
}

const getStatusText = (status: string) => {
  const statusMap = {
    'pending': '대기',
    'active': '활성',
    'inactive': '비활성'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const viewCustomerDetail = (referral: Referral) => {
  selectedCustomer.value = referral
}

const closeCustomerModal = () => {
  selectedCustomer.value = null
}

const contactCustomer = (referral: Referral) => {
  const subject = encodeURIComponent('YCS LMS 서비스 관련 안내')
  const body = encodeURIComponent(`안녕하세요 ${referral.customerName}님,\n\nYCS LMS를 이용해주셔서 감사합니다.\n\n`)
  window.open(`mailto:${referral.customerEmail}?subject=${subject}&body=${body}`)
}

const exportReferrals = () => {
  // Create CSV content
  const headers = ['고객명', '이메일', '가입일', '상태', '주문수', '총구매액', '수수료']
  const csvContent = [
    headers.join(','),
    ...filteredReferrals.value.map(referral => [
      referral.customerName,
      referral.customerEmail,
      referral.signupDate,
      getStatusText(referral.status),
      referral.ordersCount,
      referral.totalSpent,
      referral.commissionEarned
    ].join(','))
  ].join('\n')

  // Download CSV
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `referrals_${new Date().toISOString().split('T')[0]}.csv`)
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
.referrals-view {
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

.summary-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  color: #6b7280;
}

.stat-change.positive {
  color: #059669;
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

.referrals-table-section {
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

.referrals-table {
  display: flex;
  flex-direction: column;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 0.8fr 0.8fr;
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

.customer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.customer-name {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.customer-email {
  font-size: 13px;
  color: #6b7280;
}

.signup-date {
  font-weight: 500;
  color: #111827;
  margin-bottom: 2px;
}

.signup-period {
  font-size: 12px;
  color: #6b7280;
}

.orders-count {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.last-order {
  font-size: 12px;
  color: #6b7280;
}

.total-spent {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.avg-order {
  font-size: 12px;
  color: #6b7280;
}

.commission-earned {
  font-weight: 600;
  color: #059669;
  margin-bottom: 2px;
}

.commission-rate {
  font-size: 12px;
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

.status-badge.status-active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-inactive {
  background: #fee2e2;
  color: #991b1b;
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
  justify-content: between;
  align-items: center;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  flex: 1;
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

.customer-details-grid {
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

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px 24px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .referrals-view {
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
  
  .summary-stats {
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
  
  .customer-details-grid {
    grid-template-columns: 1fr;
  }
}
</style>