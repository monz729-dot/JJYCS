<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4">
    <!-- 헤더 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-lg">
            YSC
          </div>
          <div>
            <h1 class="text-xl font-bold text-blue-900">YSC 물류 시스템</h1>
            <p class="text-sm text-blue-600">{{ getUserTypeText }}</p>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-sm font-medium text-blue-900">{{ authStore.user?.name || '사용자' }}</div>
            <div class="text-xs text-gray-500">{{ getUserTypeText }}</div>
          </div>
          <button 
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm" 
            @click="handleLogout"
          >
            로그아웃
          </button>
        </div>
      </div>
    </div>
    <!-- 환영 섹션 -->
    <div class="bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-6 mb-6 text-white shadow-blue-100 shadow-lg">
      <div class="relative">
        <h1 class="text-2xl font-bold mb-2">안녕하세요, {{ authStore.user?.name || '사용자' }}님!</h1>
        <p class="text-blue-100">오늘도 YSC와 함께 고객을 위한 안전하고 빠른 배송을 시작하세요.</p>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 text-center">
        <div class="text-2xl font-bold text-blue-600 mb-2">{{ stats.totalOrders }}</div>
        <div class="text-sm text-gray-600">총 주문</div>
      </div>
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 text-center">
        <div class="text-2xl font-bold text-blue-600 mb-2">{{ stats.pendingOrders }}</div>
        <div class="text-sm text-gray-600">진행중인 주문</div>
      </div>
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 text-center">
        <div class="text-2xl font-bold text-blue-600 mb-2">{{ stats.completedOrders }}</div>
        <div class="text-sm text-gray-600">완료된 배송</div>
      </div>
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 text-center">
        <div class="text-2xl font-bold text-blue-600 mb-2">₩{{ stats.monthlyShipping.toLocaleString() }}</div>
        <div class="text-sm text-gray-600">이번 달 배송비</div>
      </div>
    </div>

    <!-- 빠른 작업 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <router-link to="/orders/new" class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 hover:shadow-xl transition-all text-center block">
        <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
          </svg>
        </div>
        <div class="text-lg font-semibold text-blue-900 mb-2">새 주문 접수</div>
        <div class="text-sm text-gray-600">고객 주문을 빠르게 접수하세요</div>
      </router-link>
      
      <router-link to="/tracking" class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 hover:shadow-xl transition-all text-center block">
        <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
          </svg>
        </div>
        <div class="text-lg font-semibold text-blue-900 mb-2">배송 추적</div>
        <div class="text-sm text-gray-600">실시간으로 배송 상태를 확인하세요</div>
      </router-link>
      
      <router-link to="/orders" class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 hover:shadow-xl transition-all text-center block">
        <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
        </div>
        <div class="text-lg font-semibold text-blue-900 mb-2">주문 내역</div>
        <div class="text-sm text-gray-600">모든 주문을 관리하고 조회하세요</div>
      </router-link>
    </div>

    <!-- 최근 주문 -->
    <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100 mb-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-bold text-blue-900">최근 주문</h2>
        <router-link to="/orders" class="text-blue-600 hover:text-blue-700 text-sm font-medium">
          전체 보기 →
        </router-link>
      </div>
      
      <div class="space-y-4">
        <div v-if="recentOrders.length === 0" class="text-center py-8 text-gray-500">
          <svg class="w-12 h-12 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
          </svg>
          <p>아직 주문이 없습니다.</p>
          <router-link to="/orders/new" class="text-blue-600 hover:text-blue-700 font-medium">첫 주문을 만들어보세요</router-link>
        </div>
        
        <div v-for="order in recentOrders" :key="order.id" class="border border-gray-200 rounded-xl p-4 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-between mb-2">
            <div class="font-semibold text-blue-900"># {{ order.orderNumber }}</div>
            <div :class="getStatusClass(order.status)" class="px-2 py-1 rounded-full text-xs font-medium">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          <div class="text-sm text-gray-600 mb-2">
            수취인: {{ order.recipientName }} | {{ order.recipientAddress }}
          </div>
          <div class="flex items-center justify-between text-xs text-gray-500">
            <span>{{ formatDate(order.createdAt) }}</span>
            <span>{{ order.shippingType === 'SEA' ? '해상운송' : '항공운송' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 도움말 및 공지사항 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <h3 class="text-lg font-semibold text-blue-900 mb-4">📢 공지사항</h3>
        <div class="space-y-3">
          <div class="border-l-4 border-blue-500 pl-4">
            <div class="text-sm font-medium text-gray-900">시스템 업데이트 안내</div>
            <div class="text-xs text-gray-500">2025-08-27</div>
            <div class="text-sm text-gray-600 mt-1">새로운 주문 관리 기능이 추가되었습니다.</div>
          </div>
          <div class="border-l-4 border-green-500 pl-4">
            <div class="text-sm font-medium text-gray-900">배송료 안내</div>
            <div class="text-xs text-gray-500">2025-08-25</div>
            <div class="text-sm text-gray-600 mt-1">9월부터 새로운 배송료 체계가 적용됩니다.</div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <h3 class="text-lg font-semibold text-blue-900 mb-4">❓ 도움말</h3>
        <div class="space-y-3">
          <div class="text-sm">
            <div class="font-medium text-gray-900 mb-1">주문 접수 방법</div>
            <div class="text-gray-600">'새 주문 접수' 버튼을 클릭하여 고객 주문을 등록하세요.</div>
          </div>
          <div class="text-sm">
            <div class="font-medium text-gray-900 mb-1">배송 추적</div>
            <div class="text-gray-600">주문번호로 실시간 배송 상태를 확인할 수 있습니다.</div>
          </div>
          <div class="text-sm">
            <div class="font-medium text-gray-900 mb-1">문의사항</div>
            <div class="text-gray-600">고객센터: 02-1234-5678 (평일 09:00-18:00)</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/utils/api'
import { USER_TYPE } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

// 반응형 상태
const stats = ref({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  monthlyShipping: 0
})

const recentOrders = ref([])
const loading = ref(true)

// 계산된 속성
const getUserTypeText = computed(() => {
  const userType = authStore.user?.userType
  switch (userType) {
    case USER_TYPE.PARTNER:
      return '파트너 회원'
    case USER_TYPE.CORPORATE:
      return '기업 회원'
    case USER_TYPE.GENERAL:
      return '일반 회원'
    default:
      return '회원'
  }
})

// 주문 상태 스타일
const getStatusClass = (status: string) => {
  const classes = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'RECEIVED': 'bg-blue-100 text-blue-800',
    'PROCESSING': 'bg-orange-100 text-orange-800',
    'SHIPPED': 'bg-green-100 text-green-800',
    'DELIVERED': 'bg-gray-100 text-gray-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

// 주문 상태 텍스트
const getStatusText = (status: string) => {
  const statusMap = {
    'PENDING': '대기중',
    'RECEIVED': '접수됨', 
    'PROCESSING': '처리중',
    'SHIPPED': '배송중',
    'DELIVERED': '배송완료',
    'CANCELLED': '취소됨'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

// 데이터 로딩
const loadDashboardData = async () => {
  try {
    loading.value = true
    const userId = authStore.user?.id
    
    if (!userId) return

    // 사용자 주문 데이터 로딩
    const ordersResponse = await ordersApi.getUserOrders(userId)
    
    if (ordersResponse.success && ordersResponse.data?.orders) {
      const orders = ordersResponse.data.orders
      
      // 통계 계산
      stats.value.totalOrders = orders.length
      stats.value.pendingOrders = orders.filter((order: any) => 
        ['PENDING', 'RECEIVED', 'PROCESSING'].includes(order.status)
      ).length
      stats.value.completedOrders = orders.filter((order: any) => 
        order.status === 'DELIVERED'
      ).length
      
      // 이번 달 배송비 계산 (임시 계산)
      stats.value.monthlyShipping = orders
        .filter((order: any) => {
          const orderDate = new Date(order.createdAt)
          const now = new Date()
          return orderDate.getMonth() === now.getMonth() && orderDate.getFullYear() === now.getFullYear()
        })
        .reduce((total: number, order: any) => total + (order.shippingCost || 50000), 0)
      
      // 최근 주문 (최대 5개)
      recentOrders.value = orders
        .sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5)
    }
  } catch (error) {
    console.error('대시보드 데이터 로딩 실패:', error)
  } finally {
    loading.value = false
  }
}

// 로그아웃
const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 컴포넌트 마운트
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
/* 기본 스타일은 Tailwind CSS를 사용하므로 추가 스타일 불필요 */
</style>