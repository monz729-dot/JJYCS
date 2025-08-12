<template>
  <div class="settlements-view">
    <!-- 헤더 -->
    <div class="view-header">
      <div class="header-info">
        <h1>정산 관리</h1>
        <p>수수료 정산 내역과 지급 현황을 관리하세요</p>
      </div>
      <div class="header-actions">
        <button @click="exportSettlements" class="btn-secondary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          내보내기
        </button>
        <button @click="openSettlementRequest" class="btn-primary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          정산 요청
        </button>
      </div>
    </div>

    <!-- 정산 요약 -->
    <div class="settlement-summary">
      <div class="summary-card available">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
            </svg>
          </div>
          <h3>정산 가능 금액</h3>
        </div>
        <div class="card-value">₩{{ availableAmount.toLocaleString() }}</div>
        <div class="card-change">{{ availableCommissionsCount }}건의 승인된 수수료</div>
      </div>

      <div class="summary-card pending">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <h3>정산 처리 중</h3>
        </div>
        <div class="card-value">₩{{ pendingAmount.toLocaleString() }}</div>
        <div class="card-change">{{ pendingSettlements.length }}건 처리 중</div>
      </div>

      <div class="summary-card completed">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h3>완료된 정산</h3>
        </div>
        <div class="card-value">₩{{ completedAmount.toLocaleString() }}</div>
        <div class="card-change">총 {{ completedSettlements.length }}회 정산</div>
      </div>

      <div class="summary-card total">
        <div class="card-header">
          <div class="card-icon">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
          </div>
          <h3>누적 정산액</h3>
        </div>
        <div class="card-value">₩{{ stats.totalCommission.toLocaleString() }}</div>
        <div class="card-change positive">+12.5% 전월 대비</div>
      </div>
    </div>

    <!-- 정산 규정 안내 -->
    <div class="settlement-policy">
      <div class="policy-header">
        <svg class="policy-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
        <h3>정산 규정 안내</h3>
      </div>
      <div class="policy-content">
        <div class="policy-item">
          <strong>정산 주기:</strong> 매월 말일 기준으로 정산 요청 가능 (승인된 수수료만 해당)
        </div>
        <div class="policy-item">
          <strong>최소 정산액:</strong> 50,000원 이상부터 정산 가능
        </div>
        <div class="policy-item">
          <strong>처리 기간:</strong> 정산 요청 후 영업일 기준 3-5일 소요
        </div>
        <div class="policy-item">
          <strong>수수료 공제:</strong> 정산 시 제세공과금 3.3% 공제 (부가가치세 포함)
        </div>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filters-section">
      <div class="filters-row">
        <select v-model="statusFilter" class="filter-select">
          <option value="">전체 상태</option>
          <option value="pending">대기</option>
          <option value="processing">처리 중</option>
          <option value="paid">지급완료</option>
          <option value="failed">실패</option>
        </select>
        
        <select v-model="periodFilter" class="filter-select">
          <option value="">전체 기간</option>
          <option value="thisYear">올해</option>
          <option value="lastYear">작년</option>
          <option value="thisQuarter">이번 분기</option>
          <option value="lastQuarter">지난 분기</option>
        </select>

        <select v-model="sortBy" class="filter-select">
          <option value="requestDate">요청일순</option>
          <option value="netAmount">금액순</option>
          <option value="status">상태순</option>
        </select>

        <select v-model="sortOrder" class="filter-select">
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>
    </div>

    <!-- 정산 내역 테이블 -->
    <div class="settlements-table-section">
      <div v-if="loading" class="loading-state">
        <svg class="animate-spin h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p>데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="filteredSettlements.length === 0" class="empty-state">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
        </svg>
        <h3>정산 내역이 없습니다</h3>
        <p>첫 번째 정산을 요청해보세요!</p>
        <button @click="openSettlementRequest" class="btn-primary">정산 요청하기</button>
      </div>

      <div v-else class="settlements-table">
        <div class="table-header">
          <div class="table-cell period">정산 기간</div>
          <div class="table-cell amounts">정산 금액</div>
          <div class="table-cell method">지급 방법</div>
          <div class="table-cell dates">처리 일자</div>
          <div class="table-cell status">상태</div>
          <div class="table-cell actions">작업</div>
        </div>

        <div v-for="settlement in paginatedSettlements" :key="settlement.id" class="table-row">
          <div class="table-cell period">
            <div class="period-text">{{ settlement.period }}</div>
            <div class="period-format">{{ formatPeriod(settlement.period) }}</div>
          </div>

          <div class="table-cell amounts">
            <div class="amount-breakdown">
              <div class="gross-amount">총액: ₩{{ settlement.totalCommission.toLocaleString() }}</div>
              <div class="deductions">공제: ₩{{ settlement.deductions.toLocaleString() }}</div>
              <div class="net-amount">실지급: ₩{{ settlement.netAmount.toLocaleString() }}</div>
            </div>
          </div>

          <div class="table-cell method">
            <div class="payment-method">{{ settlement.paymentMethod }}</div>
          </div>

          <div class="table-cell dates">
            <div class="request-date">
              요청: {{ formatDate(settlement.requestDate) }}
            </div>
            <div class="paid-date" v-if="settlement.paidDate">
              완료: {{ formatDate(settlement.paidDate) }}
            </div>
            <div class="processing-time" v-else-if="settlement.status === 'processing'">
              처리 중...
            </div>
          </div>

          <div class="table-cell status">
            <span :class="['status-badge', `status-${settlement.status}`]">
              {{ getStatusText(settlement.status) }}
            </span>
            <div v-if="settlement.transactionId" class="transaction-id">
              거래 ID: {{ settlement.transactionId }}
            </div>
          </div>

          <div class="table-cell actions">
            <div class="action-buttons">
              <button 
                @click="viewSettlementDetail(settlement)"
                class="btn-action"
                title="상세보기"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </button>
              <button 
                v-if="settlement.status === 'paid'"
                @click="downloadReceipt(settlement)"
                class="btn-action"
                title="영수증 다운로드"
              >
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
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

    <!-- 정산 요청 모달 -->
    <div v-if="showSettlementRequest" class="modal-overlay" @click="closeSettlementRequest">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>정산 요청</h3>
          <button @click="closeSettlementRequest" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="settlement-request-form">
            <div class="form-section">
              <h4>정산 정보</h4>
              <div class="form-grid">
                <div class="form-item">
                  <label>정산 기간</label>
                  <select v-model="settlementForm.period" class="form-select">
                    <option value="">기간을 선택하세요</option>
                    <option v-for="period in availablePeriods" :key="period.value" :value="period.value">
                      {{ period.label }}
                    </option>
                  </select>
                </div>
                <div class="form-item">
                  <label>지급 방법</label>
                  <select v-model="settlementForm.paymentMethod" class="form-select">
                    <option value="">지급 방법을 선택하세요</option>
                    <option value="계좌이체">계좌이체</option>
                    <option value="가상계좌">가상계좌</option>
                  </select>
                </div>
              </div>
            </div>

            <div class="form-section" v-if="settlementForm.period">
              <h4>정산 금액</h4>
              <div class="amount-summary">
                <div class="amount-row">
                  <span>총 수수료</span>
                  <span>₩{{ settlementForm.totalCommission.toLocaleString() }}</span>
                </div>
                <div class="amount-row deduction">
                  <span>제세공과금 (3.3%)</span>
                  <span>-₩{{ settlementForm.deductions.toLocaleString() }}</span>
                </div>
                <div class="amount-row total">
                  <span>실지급액</span>
                  <span>₩{{ settlementForm.netAmount.toLocaleString() }}</span>
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>유의사항</h4>
              <div class="notice-list">
                <div class="notice-item">
                  <svg class="notice-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span>최소 정산액 50,000원 이상만 요청 가능합니다.</span>
                </div>
                <div class="notice-item">
                  <svg class="notice-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span>정산 처리는 영업일 기준 3-5일 소요됩니다.</span>
                </div>
                <div class="notice-item">
                  <svg class="notice-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span>제세공과금은 관련 법령에 따라 자동 공제됩니다.</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button 
            @click="submitSettlementRequest" 
            :disabled="!canSubmitRequest || submittingRequest"
            class="btn-primary"
          >
            <svg v-if="submittingRequest" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span v-if="submittingRequest">요청 중...</span>
            <span v-else>정산 요청</span>
          </button>
          <button @click="closeSettlementRequest" class="btn-secondary" :disabled="submittingRequest">
            취소
          </button>
        </div>
      </div>
    </div>

    <!-- 정산 상세 모달 -->
    <div v-if="selectedSettlement" class="modal-overlay" @click="closeSettlementDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>정산 상세 정보</h3>
          <button @click="closeSettlementDetail" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="settlement-details-grid">
            <div class="detail-item">
              <label>정산 기간</label>
              <span>{{ selectedSettlement.period }} ({{ formatPeriod(selectedSettlement.period) }})</span>
            </div>
            <div class="detail-item">
              <label>총 수수료</label>
              <span>₩{{ selectedSettlement.totalCommission.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>공제액</label>
              <span>₩{{ selectedSettlement.deductions.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>실지급액</label>
              <span class="amount-highlight">₩{{ selectedSettlement.netAmount.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <label>지급 방법</label>
              <span>{{ selectedSettlement.paymentMethod }}</span>
            </div>
            <div class="detail-item">
              <label>요청일</label>
              <span>{{ formatDate(selectedSettlement.requestDate) }}</span>
            </div>
            <div class="detail-item">
              <label>상태</label>
              <span :class="['status-badge', `status-${selectedSettlement.status}`]">
                {{ getStatusText(selectedSettlement.status) }}
              </span>
            </div>
            <div class="detail-item" v-if="selectedSettlement.paidDate">
              <label>지급일</label>
              <span>{{ formatDate(selectedSettlement.paidDate) }}</span>
            </div>
            <div class="detail-item" v-if="selectedSettlement.transactionId">
              <label>거래 ID</label>
              <span>{{ selectedSettlement.transactionId }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button 
            v-if="selectedSettlement.status === 'paid'"
            @click="downloadReceipt(selectedSettlement)"
            class="btn-primary"
          >
            영수증 다운로드
          </button>
          <button @click="closeSettlementDetail" class="btn-secondary">
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { usePartnerStore, type Settlement } from '../stores/partnerStore'

const partnerStore = usePartnerStore()

// Reactive data
const statusFilter = ref('')
const periodFilter = ref('')
const sortBy = ref('requestDate')
const sortOrder = ref('desc')
const currentPage = ref(1)
const itemsPerPage = ref(10)

// Modal states
const showSettlementRequest = ref(false)
const selectedSettlement = ref<Settlement | null>(null)
const submittingRequest = ref(false)

// Settlement request form
const settlementForm = ref({
  period: '',
  totalCommission: 0,
  deductions: 0,
  netAmount: 0,
  paymentMethod: ''
})

// Store data
const { settlements, stats, loading, commissions } = partnerStore

// Computed properties
const pendingSettlements = computed(() => 
  settlements.filter(s => s.status === 'pending' || s.status === 'processing')
)

const completedSettlements = computed(() =>
  settlements.filter(s => s.status === 'paid')
)

const availableCommissions = computed(() =>
  commissions.filter(c => c.status === 'approved')
)

const availableAmount = computed(() =>
  availableCommissions.value.reduce((sum, c) => sum + c.commissionAmount, 0)
)

const availableCommissionsCount = computed(() =>
  availableCommissions.value.length
)

const pendingAmount = computed(() =>
  pendingSettlements.value.reduce((sum, s) => sum + s.netAmount, 0)
)

const completedAmount = computed(() =>
  completedSettlements.value.reduce((sum, s) => sum + s.netAmount, 0)
)

const availablePeriods = computed(() => {
  const periods = []
  const now = new Date()
  
  // Generate last 12 months
  for (let i = 0; i < 12; i++) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    const yearMonth = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}`
    periods.push({
      value: yearMonth,
      label: `${date.getFullYear()}년 ${date.getMonth() + 1}월`
    })
  }
  
  return periods
})

const filteredSettlements = computed(() => {
  let filtered = settlements

  // Apply status filter
  if (statusFilter.value) {
    filtered = filtered.filter(settlement => settlement.status === statusFilter.value)
  }

  // Apply period filter
  if (periodFilter.value) {
    const now = new Date()
    
    switch (periodFilter.value) {
      case 'thisYear':
        filtered = filtered.filter(settlement => {
          const requestDate = new Date(settlement.requestDate)
          return requestDate.getFullYear() === now.getFullYear()
        })
        break
      case 'lastYear':
        filtered = filtered.filter(settlement => {
          const requestDate = new Date(settlement.requestDate)
          return requestDate.getFullYear() === now.getFullYear() - 1
        })
        break
      case 'thisQuarter':
        const quarterStart = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1)
        filtered = filtered.filter(settlement => {
          const requestDate = new Date(settlement.requestDate)
          return requestDate >= quarterStart
        })
        break
      case 'lastQuarter':
        const lastQuarterStart = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3 - 3, 1)
        const lastQuarterEnd = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 0)
        filtered = filtered.filter(settlement => {
          const requestDate = new Date(settlement.requestDate)
          return requestDate >= lastQuarterStart && requestDate <= lastQuarterEnd
        })
        break
    }
  }

  // Apply sorting
  filtered.sort((a, b) => {
    let aVal: any = a[sortBy.value as keyof Settlement]
    let bVal: any = b[sortBy.value as keyof Settlement]

    if (sortBy.value === 'requestDate') {
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
  Math.ceil(filteredSettlements.value.length / itemsPerPage.value)
)

const paginatedSettlements = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredSettlements.value.slice(start, end)
})

const canSubmitRequest = computed(() => {
  return settlementForm.value.period && 
         settlementForm.value.paymentMethod && 
         settlementForm.value.netAmount >= 50000
})

// Methods
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatPeriod = (period: string) => {
  const [year, month] = period.split('-')
  return `${year}년 ${parseInt(month)}월`
}

const getStatusText = (status: string) => {
  const statusMap = {
    'pending': '대기',
    'processing': '처리 중',
    'paid': '지급완료',
    'failed': '실패'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const openSettlementRequest = () => {
  // Reset form
  settlementForm.value = {
    period: '',
    totalCommission: 0,
    deductions: 0,
    netAmount: 0,
    paymentMethod: ''
  }
  showSettlementRequest.value = true
}

const closeSettlementRequest = () => {
  showSettlementRequest.value = false
}

const viewSettlementDetail = (settlement: Settlement) => {
  selectedSettlement.value = settlement
}

const closeSettlementDetail = () => {
  selectedSettlement.value = null
}

const submitSettlementRequest = async () => {
  if (!canSubmitRequest.value) return

  submittingRequest.value = true
  
  try {
    await partnerStore.requestSettlement(settlementForm.value)
    alert('정산 요청이 완료되었습니다.')
    closeSettlementRequest()
  } catch (error) {
    alert('정산 요청에 실패했습니다.')
  } finally {
    submittingRequest.value = false
  }
}

const downloadReceipt = (settlement: Settlement) => {
  // Simulate receipt download
  const receiptContent = `
정산 영수증
---------
정산 기간: ${settlement.period}
총 수수료: ₩${settlement.totalCommission.toLocaleString()}
공제액: ₩${settlement.deductions.toLocaleString()}
실지급액: ₩${settlement.netAmount.toLocaleString()}
지급일: ${settlement.paidDate}
거래 ID: ${settlement.transactionId}
  `
  
  const blob = new Blob([receiptContent], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `settlement_receipt_${settlement.period}.txt`
  link.click()
  URL.revokeObjectURL(url)
}

const exportSettlements = () => {
  // Create CSV content
  const headers = ['정산기간', '총수수료', '공제액', '실지급액', '지급방법', '요청일', '상태', '지급일']
  const csvContent = [
    headers.join(','),
    ...filteredSettlements.value.map(settlement => [
      settlement.period,
      settlement.totalCommission,
      settlement.deductions,
      settlement.netAmount,
      settlement.paymentMethod,
      settlement.requestDate,
      getStatusText(settlement.status),
      settlement.paidDate || ''
    ].join(','))
  ].join('\n')

  // Download CSV
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `settlements_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

// Watch for period changes to calculate amounts
watch(() => settlementForm.value.period, (newPeriod) => {
  if (!newPeriod) {
    settlementForm.value.totalCommission = 0
    settlementForm.value.deductions = 0
    settlementForm.value.netAmount = 0
    return
  }

  // Calculate settlement amounts for the selected period
  const [year, month] = newPeriod.split('-')
  const periodCommissions = availableCommissions.value.filter(commission => {
    const commissionDate = new Date(commission.orderDate)
    return commissionDate.getFullYear() === parseInt(year) && 
           commissionDate.getMonth() === parseInt(month) - 1
  })

  const totalCommission = periodCommissions.reduce((sum, c) => sum + c.commissionAmount, 0)
  const deductions = Math.round(totalCommission * 0.033) // 3.3% tax
  const netAmount = totalCommission - deductions

  settlementForm.value.totalCommission = totalCommission
  settlementForm.value.deductions = deductions
  settlementForm.value.netAmount = netAmount
})

onMounted(() => {
  // Data is already loaded from the store
})
</script>

<style scoped>
.settlements-view {
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

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.settlement-summary {
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

.summary-card.available .card-icon {
  background: #10b981;
}

.summary-card.pending .card-icon {
  background: #f59e0b;
}

.summary-card.completed .card-icon {
  background: #3b82f6;
}

.summary-card.total .card-icon {
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

.settlement-policy {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.policy-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.policy-icon {
  width: 20px;
  height: 20px;
  color: #0ea5e9;
  flex-shrink: 0;
}

.policy-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #0c4a6e;
  margin: 0;
}

.policy-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.policy-item {
  font-size: 14px;
  color: #0c4a6e;
  line-height: 1.5;
}

.policy-item strong {
  color: #0369a1;
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

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.settlements-table-section {
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

.settlements-table {
  display: flex;
  flex-direction: column;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 1fr 1.5fr 1fr 1.2fr 1fr 0.8fr;
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

.period-text {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.period-format {
  font-size: 12px;
  color: #6b7280;
}

.amount-breakdown {
  font-size: 12px;
}

.gross-amount, .net-amount {
  color: #111827;
  margin-bottom: 2px;
}

.net-amount {
  font-weight: 600;
  color: #059669;
}

.deductions {
  color: #ef4444;
  margin-bottom: 2px;
}

.payment-method {
  font-weight: 500;
  color: #111827;
}

.request-date, .paid-date {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 2px;
}

.processing-time {
  font-size: 12px;
  color: #f59e0b;
  font-style: italic;
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

.status-badge.status-processing {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.status-paid {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.status-failed {
  background: #fee2e2;
  color: #991b1b;
}

.transaction-id {
  font-size: 10px;
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
  max-width: 600px;
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

.settlement-request-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.amount-summary {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 14px;
}

.amount-row.deduction {
  color: #ef4444;
}

.amount-row.total {
  border-top: 1px solid #e5e7eb;
  margin-top: 8px;
  padding-top: 12px;
  font-weight: 600;
  color: #059669;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.notice-icon {
  width: 16px;
  height: 16px;
  color: #3b82f6;
  flex-shrink: 0;
  margin-top: 1px;
}

.settlement-details-grid {
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

.amount-highlight {
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
  .settlements-view {
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
  
  .settlement-summary {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .filters-row {
    flex-direction: column;
    align-items: stretch;
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
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .settlement-details-grid {
    grid-template-columns: 1fr;
  }
}
</style>