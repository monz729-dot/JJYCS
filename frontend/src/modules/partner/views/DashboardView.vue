<template>
  <div class="partner-dashboard min-h-screen bg-gray-50">
    <!-- 헤더 -->
    <div class="bg-white shadow-sm border-b border-gray-200 px-4 sm:px-6 lg:px-8 py-4 sm:py-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
        <div class="flex-1">
          <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">파트너 대시보드</h1>
          <p class="mt-1 text-sm text-gray-600">{{ currentDateTime }}</p>
        </div>
        <div class="flex-shrink-0">
          <button 
            @click="refreshData" 
            class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            :disabled="loading"
          >
            <svg v-if="loading" class="animate-spin h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <svg v-else class="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            새로고침
          </button>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- 통계 카드 그리드 -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-8">
        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-4 sm:p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                  <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4 flex-1">
                <div class="text-2xl font-bold text-gray-900">{{ stats.totalReferrals.toLocaleString() }}</div>
                <div class="text-sm font-medium text-gray-500">총 추천 고객</div>
                <div class="text-xs text-green-600 mt-1" v-if="stats.thisMonthReferrals > 0">
                  이번달 +{{ stats.thisMonthReferrals }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-4 sm:p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                  <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4 flex-1">
                <div class="text-2xl font-bold text-gray-900">{{ stats.activeCustomers.toLocaleString() }}</div>
                <div class="text-sm font-medium text-gray-500">활성 고객</div>
                <div class="text-xs text-green-600 mt-1">
                  전환율 {{ stats.conversionRate.toFixed(1) }}%
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-4 sm:p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                  <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4 flex-1">
                <div class="text-2xl font-bold text-gray-900">₩{{ stats.totalCommission.toLocaleString() }}</div>
                <div class="text-sm font-medium text-gray-500">총 수수료</div>
                <div class="text-xs text-green-600 mt-1">
                  이번달 ₩{{ stats.thisMonthCommission.toLocaleString() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-4 sm:p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-purple-500 rounded-md flex items-center justify-center">
                  <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
              </div>
              <div class="ml-4 flex-1">
                <div class="text-2xl font-bold text-gray-900">₩{{ stats.pendingPayment.toLocaleString() }}</div>
                <div class="text-sm font-medium text-gray-500">정산 대기</div>
                <div class="text-xs text-gray-600 mt-1">
                  {{ pendingCommissions.length }}건 대기 중
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 성과 차트 섹션 -->
      <div class="bg-white shadow rounded-lg mb-8">
        <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
            <h2 class="text-lg sm:text-xl font-semibold text-gray-900">월별 성과</h2>
            <div class="flex-shrink-0">
              <select 
                v-model="selectedPeriod" 
                class="block w-full sm:w-auto border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              >
                <option value="6months">최근 6개월</option>
                <option value="3months">최근 3개월</option>
                <option value="1month">이번 달</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="p-4 sm:p-6">
          <div class="h-64 sm:h-80 bg-gray-50 rounded-lg flex items-center justify-center">
            <div class="text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
              <p class="mt-2 text-sm text-gray-500">차트가 여기에 표시됩니다</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 최근 추천 고객 섹션 -->
      <div class="bg-white shadow rounded-lg mb-8">
        <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h2 class="text-lg sm:text-xl font-semibold text-gray-900">최근 추천 고객</h2>
            <router-link 
              to="/partner/referrals" 
              class="text-sm font-medium text-blue-600 hover:text-blue-500"
            >
              전체보기 →
            </router-link>
          </div>
        </div>
        
        <div class="overflow-hidden">
          <!-- Desktop Table -->
          <div class="hidden lg:block">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">고객명</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수수료</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">액션</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="referral in recentReferrals" :key="referral.id">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <div class="flex-shrink-0 h-10 w-10">
                        <div class="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                          <span class="text-sm font-medium text-gray-700">{{ referral.name.charAt(0) }}</span>
                        </div>
                      </div>
                      <div class="ml-4">
                        <div class="text-sm font-medium text-gray-900">{{ referral.name }}</div>
                        <div class="text-sm text-gray-500">{{ referral.email }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatDate(referral.createdAt) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getStatusClass(referral.status)">
                      {{ getStatusText(referral.status) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">₩{{ referral.commission.toLocaleString() }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button class="text-blue-600 hover:text-blue-900">상세보기</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Mobile Cards -->
          <div class="lg:hidden">
            <div class="divide-y divide-gray-200">
              <div v-for="referral in recentReferrals" :key="referral.id" class="p-4">
                <div class="flex items-start justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                        <span class="text-sm font-medium text-gray-700">{{ referral.name.charAt(0) }}</span>
                      </div>
                    </div>
                    <div>
                      <div class="text-sm font-medium text-gray-900">{{ referral.name }}</div>
                      <div class="text-sm text-gray-500">{{ referral.email }}</div>
                      <div class="text-xs text-gray-500 mt-1">{{ formatDate(referral.createdAt) }}</div>
                    </div>
                  </div>
                  <div class="text-right">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full mb-2" :class="getStatusClass(referral.status)">
                      {{ getStatusText(referral.status) }}
                    </span>
                    <div class="text-sm font-semibold text-gray-900">₩{{ referral.commission.toLocaleString() }}</div>
                  </div>
                </div>
                <div class="mt-3 pt-3 border-t border-gray-100">
                  <button class="text-sm text-blue-600 hover:text-blue-900 font-medium">상세보기</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 정산 대기 내역 섹션 -->
      <div class="bg-white shadow rounded-lg">
        <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h2 class="text-lg sm:text-xl font-semibold text-gray-900">정산 대기 내역</h2>
            <router-link 
              to="/partner/commissions" 
              class="text-sm font-medium text-blue-600 hover:text-blue-500"
            >
              전체보기 →
            </router-link>
          </div>
        </div>
        
        <div class="overflow-hidden">
          <!-- Desktop Table -->
          <div class="hidden lg:block">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주문번호</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">고객명</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주문금액</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수수료율</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수수료</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="commission in pendingCommissions" :key="commission.id">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ commission.orderNumber }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ commission.customerName }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">₩{{ commission.orderAmount.toLocaleString() }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ commission.rate }}%</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">₩{{ commission.amount.toLocaleString() }}</td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                      정산 대기
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Mobile Cards -->
          <div class="lg:hidden">
            <div class="divide-y divide-gray-200">
              <div v-for="commission in pendingCommissions" :key="commission.id" class="p-4">
                <div class="flex items-start justify-between mb-3">
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ commission.orderNumber }}</div>
                    <div class="text-sm text-gray-500">{{ commission.customerName }}</div>
                  </div>
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                    정산 대기
                  </span>
                </div>
                <div class="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <div class="text-gray-500">주문금액</div>
                    <div class="font-medium text-gray-900">₩{{ commission.orderAmount.toLocaleString() }}</div>
                  </div>
                  <div>
                    <div class="text-gray-500">수수료율</div>
                    <div class="font-medium text-gray-900">{{ commission.rate }}%</div>
                  </div>
                  <div class="col-span-2">
                    <div class="text-gray-500">수수료</div>
                    <div class="font-semibold text-lg text-gray-900">₩{{ commission.amount.toLocaleString() }}</div>
                  </div>
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

const getStatusClass = (status: string) => {
  switch (status) {
    case 'pending':
      return 'bg-yellow-100 text-yellow-800'
    case 'active':
      return 'bg-green-100 text-green-800'
    case 'inactive':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
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

