<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4 pb-24">
    <!-- 헤더 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-lg">
            YCS
          </div>
          <div>
            <h1 class="text-xl font-bold text-blue-900">YCS 물류 시스템</h1>
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
        <p class="text-blue-100">오늘도 YCS와 함께 안전하고 빠른 배송을 시작하세요.</p>
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
        <div class="text-sm text-gray-600">개인 배송 주문을 빠르게 접수하세요</div>
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
        <div class="text-sm text-gray-600">과거 주문 내역과 배송 기록을 확인하세요</div>
      </router-link>
    </div>

    <!-- 최근 주문 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-2">
          <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 7v10a2 2 0 002 2h10a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2 2v0z"/>
            <path d="M9 5a2 2 0 012-2h6a2 2 0 012 2v6a2 2 0 01-2 2H9a2 2 0 01-2-2V5z"/>
          </svg>
          <h2 class="text-xl font-semibold text-blue-900">최근 주문</h2>
        </div>
        <router-link to="/orders" class="text-blue-600 hover:text-blue-700 text-sm font-medium">전체보기</router-link>
      </div>
      
      <div class="space-y-3">
        <div v-if="loading.orders" class="text-center py-12">
          <div class="text-4xl mb-4">⏳</div>
          <div class="text-gray-500">로딩 중...</div>
        </div>
        <div v-else-if="recentOrders.length === 0" class="text-center py-12">
          <div class="text-4xl mb-4">📦</div>
          <div class="text-gray-900 font-medium mb-1">주문 내역이 없습니다</div>
          <div class="text-gray-500 text-sm">첫 번째 주문을 접수해보세요</div>
        </div>
        <div 
          v-else
          v-for="order in recentOrders" 
          :key="order.id"
          class="flex items-center justify-between p-4 bg-gray-50 rounded-xl border hover:bg-blue-50 hover:border-blue-200 cursor-pointer transition-all"
          @click="goToOrderDetail(order.orderNumber)"
        >
          <div class="flex-1">
            <div class="font-semibold text-blue-900 text-sm mb-1">#{{ order.orderNumber }}</div>
            <div class="text-sm text-gray-600">{{ getOrderDescription(order) }} • {{ order.origin || '태국' }} → {{ order.destination || '한국' }}</div>
          </div>
          <div class="text-right ml-4">
            <div class="text-xs text-gray-500 mb-1">{{ formatDate(order.createdAt) }}</div>
            <span :class="getStatusClass(order.status)" class="inline-block px-2 py-1 rounded-full text-xs font-medium">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>
      
    <!-- 창고 보관 현황 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-2 mb-6">
        <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
          <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
        </svg>
        <h2 class="text-xl font-semibold text-blue-900">창고 보관 현황</h2>
      </div>
      
      <div class="space-y-3">
        <div v-if="loading.warehouse" class="text-center py-12">
          <div class="text-4xl mb-4">⏳</div>
          <div class="text-gray-500">로딩 중...</div>
        </div>
        <div v-else-if="warehouseItems.length === 0" class="text-center py-12">
          <div class="text-4xl mb-4">🏪</div>
          <div class="text-gray-900 font-medium mb-1">보관 중인 상품이 없습니다</div>
          <div class="text-gray-500 text-sm">창고에 도착한 상품이 여기에 표시됩니다</div>
        </div>
        <div 
          v-else
          v-for="item in warehouseItems" 
          :key="item.id"
          class="flex items-center justify-between p-4 bg-gray-50 rounded-xl border border-l-4 border-l-blue-500 hover:bg-blue-50 hover:border-blue-200 cursor-pointer transition-all"
          @click="goToOrderDetail(item.orderNumber)"
        >
          <div class="flex-1">
            <div class="font-semibold text-blue-900 text-sm mb-1">#{{ item.orderNumber }}</div>
            <div class="text-sm text-gray-600 mb-2">{{ getOrderDescription(item) }} • {{ item.actualWeight || item.totalWeight }}kg</div>
            <div class="flex items-center gap-2">
              <span class="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-mono font-semibold">
                {{ item.storageLocation || 'A-01-03' }}
              </span>
              <span class="text-xs text-gray-500">{{ item.storageArea || 'A열 1행 3번' }}</span>
            </div>
          </div>
          <div class="text-right ml-4">
            <div class="text-xs text-gray-500 mb-1">입고일: {{ formatDate(item.arrivedAt || item.createdAt) }}</div>
            <span :class="getWarehouseStatusClass(item.status)" class="inline-block px-2 py-1 rounded-full text-xs font-medium">
              {{ getWarehouseStatusText(item.status) }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="text-center mt-6 pt-4 border-t border-gray-100">
        <p class="text-sm text-gray-600 mb-3">💡 창고 위치는 관리자가 물품 입고 시 할당해드립니다</p>
        <button 
          class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm"
          @click="$router.push('/orders')"
        >
          전체 보관 현황 보기
        </button>
      </div>
    </div>

    <!-- 하단 네비게이션 -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4 z-50">
      <div class="grid grid-cols-5 gap-2 max-w-md mx-auto">
        <button class="flex flex-col items-center py-2 text-blue-600" @click="$router.push('/dashboard')">
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
          </svg>
          <span class="text-xs">홈</span>
        </button>
        <button class="flex flex-col items-center py-2 text-gray-400" @click="$router.push('/orders/new')">
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
          </svg>
          <span class="text-xs">주문</span>
        </button>
        <button class="flex flex-col items-center py-2 text-gray-400" @click="$router.push('/orders')">
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <span class="text-xs">내역</span>
        </button>
        <button class="flex flex-col items-center py-2 text-gray-400" @click="$router.push('/profile')">
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
          </svg>
          <span class="text-xs">프로필</span>
        </button>
        <button class="flex flex-col items-center py-2 text-gray-400" @click="$router.push('/menu')">
          <svg class="w-6 h-6 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path>
          </svg>
          <span class="text-xs">메뉴</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { dashboardApi, ordersApi } from '@/utils/api'
import { USER_TYPE } from '@/types'
import type { Order } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = reactive({
  orders: false,
  warehouse: false
})

const stats = reactive({
  totalOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  monthlyShipping: 0
})

const recentOrders = ref<any[]>([])
const warehouseItems = ref<any[]>([])

const getUserTypeText = computed(() => {
  const userType = authStore.user?.userType
  switch(userType) {
    case USER_TYPE.GENERAL:
      return '개인 고객'
    case USER_TYPE.CORPORATE:
      return '기업 고객'
    case USER_TYPE.PARTNER:
      return '파트너'
    case USER_TYPE.ADMIN:
      return '관리자'
    default:
      return '일반 사용자'
  }
})

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\./g, '.').replace(/\s/g, '')
}

const getOrderDescription = (order: any) => {
  if (!order.items || order.items.length === 0) {
    return '상품 정보 없음'
  }
  const firstItem = order.items[0]
  if (order.items.length === 1) {
    return firstItem.description || firstItem.name || '상품'
  }
  return `${firstItem.description || firstItem.name || '상품'} 외 ${order.items.length - 1}개`
}

const getStatusClass = (status: string) => {
  const statusClasses: Record<string, string> = {
    'RECEIVED': 'bg-yellow-100 text-yellow-800',
    'ARRIVED': 'bg-blue-100 text-blue-800',
    'REPACKING': 'bg-blue-100 text-blue-800',
    'SHIPPING': 'bg-purple-100 text-purple-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'BILLING': 'bg-orange-100 text-orange-800',
    'PAYMENT_PENDING': 'bg-red-100 text-red-800',
    'PAYMENT_CONFIRMED': 'bg-green-100 text-green-800',
    'COMPLETED': 'bg-green-100 text-green-800'
  }
  return statusClasses[status] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const statusTexts: Record<string, string> = {
    'RECEIVED': '접수완료',
    'ARRIVED': '창고도착',
    'REPACKING': '리패킹진행',
    'SHIPPING': '배송중',
    'DELIVERED': '배송완료',
    'BILLING': '청구서발행',
    'PAYMENT_PENDING': '입금대기',
    'PAYMENT_CONFIRMED': '입금확인',
    'COMPLETED': '완료'
  }
  return statusTexts[status] || status
}

const getWarehouseStatusClass = (status: string) => {
  if (status === 'REPACKING') {
    return 'bg-orange-100 text-orange-800'
  }
  return 'bg-green-100 text-green-800'
}

const getWarehouseStatusText = (status: string) => {
  if (status === 'REPACKING') {
    return '리패킹완료'
  }
  return '보관중'
}

const handleLogout = async () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    await authStore.logout()
    router.push('/login')
  }
}

const goToOrderDetail = (orderNumber: string) => {
  router.push(`/orders/${orderNumber}`)
}

const loadDashboardData = async () => {
  try {
    loading.orders = true
    loading.warehouse = true
    
    // Load dashboard data from API
    const userId = authStore.user?.id
    if (!userId) {
      console.error('No user ID available')
      loadMockData()
      return
    }
    
    console.log('Loading dashboard data for userId:', userId)
    
    // Check user role to determine which API to call
    const userType = authStore.user?.userType || 'GENERAL'
    console.log('User type:', userType)
    
    if (userType === 'ADMIN') {
      // For admin, use admin stats API
      const response = await dashboardApi.getDashboardData(userType, userId)
      console.log('Admin Dashboard API response:', response)
      
      if (response.success) {
        const data = response.data
        stats.totalOrders = Number(data.totalOrders || 0)
        stats.pendingOrders = Number(data.pendingOrders || 0) + Number(data.processingOrders || 0)
        stats.completedOrders = Number(data.deliveredOrders || 0) + Number(data.shippedOrders || 0)
        stats.monthlyShipping = 0
        
        recentOrders.value = []
        warehouseItems.value = []
      } else {
        console.error('Admin Dashboard API response failed:', response.error)
        loadMockData()
      }
    } else {
      // For regular users, directly call orders API
      console.log('Loading user orders directly from orders API')
      const ordersResponse = await ordersApi.getOrders(1, 50) // Get first 50 orders
      console.log('Orders API response:', ordersResponse)
      
      if (ordersResponse.success && ordersResponse.data) {
        const orders = Array.isArray(ordersResponse.data) ? ordersResponse.data : 
                      (ordersResponse.data.content || [])
        
        console.log('User orders loaded:', orders.length)
        
        // Calculate stats from orders
        const total = orders.length
        const pending = orders.filter((order: any) => 
          ['RECEIVED', 'ARRIVED', 'REPACKING', 'SHIPPING', 'BILLING', 'PAYMENT_PENDING'].includes(order.status)
        ).length
        const completed = orders.filter((order: any) => 
          ['DELIVERED', 'COMPLETED', 'PAYMENT_CONFIRMED'].includes(order.status)
        ).length
        
        stats.totalOrders = total
        stats.pendingOrders = pending
        stats.completedOrders = completed
        stats.monthlyShipping = 0
        
        console.log('Calculated user stats:', {
          totalOrders: total,
          pendingOrders: pending,
          completedOrders: completed
        })
        
        // Update recent orders
        recentOrders.value = orders.slice(0, 10)
        
        // Filter warehouse items (ARRIVED or REPACKING status)
        warehouseItems.value = orders.filter((order: any) => 
          ['ARRIVED', 'REPACKING'].includes(order.status)
        ) || []
      } else {
        console.error('Orders API response failed:', ordersResponse.error)
        loadMockData()
      }
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    // Fallback to mock data if API fails
    loadMockData()
  } finally {
    loading.orders = false
    loading.warehouse = false
  }
}

const loadMockData = () => {
  // Fallback mock data - set to 0 to indicate no data loaded
  console.warn('Loading mock data due to API failure')
  stats.totalOrders = 0
  stats.pendingOrders = 0
  stats.completedOrders = 0
  stats.monthlyShipping = 0

  recentOrders.value = [
    {
      id: 1,
      orderNumber: 'YCS240822001',
      items: [{ description: '전자제품' }],
      origin: '태국',
      destination: '한국',
      status: 'BILLING',
      createdAt: '2024-08-22'
    },
    {
      id: 2,
      orderNumber: 'YCS240821002',
      items: [{ description: '의류' }, { description: '신발' }, { description: '가방' }],
      origin: '태국',
      destination: '한국',
      status: 'DELIVERED',
      createdAt: '2024-08-21'
    }
  ]

  warehouseItems.value = [
    {
      id: 1,
      orderNumber: 'YCS240115001',
      items: [{ description: '빼빼로, 초콜릿 과자' }],
      actualWeight: 2.5,
      status: 'REPACKING',
      storageLocation: 'A-01-03',
      storageArea: 'A열 1행 3번',
      arrivedAt: '2024-01-18'
    }
  ]
}

onMounted(() => {
  loadDashboardData()
})
</script>

