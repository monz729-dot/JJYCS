<template>
  <div class="min-h-screen bg-gray-50 py-4 sm:py-6">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Page header -->
      <div class="mb-6 sm:mb-8">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">관리자 대시보드</h1>
            <p class="mt-1 text-sm text-gray-600">실제 연동 데이터 기반 시스템 현황</p>
          </div>
          <div class="flex-shrink-0">
            <button @click="refreshData" class="btn btn-md btn-primary">
              <span class="mdi mdi-refresh mr-2"></span>
              새로고침
            </button>
          </div>
        </div>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-8">
        <div v-for="kpi in kpis" :key="kpi.title" class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-4 sm:p-6">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 rounded-md flex items-center justify-center" :class="kpi.bgColor">
                  <component :is="kpi.icon" class="h-5 w-5 text-white" />
                </div>
              </div>
              <div class="ml-4 flex-1">
                <div class="text-sm font-medium text-gray-500 truncate">{{ kpi.title }}</div>
                <div class="flex items-baseline">
                  <div class="text-xl sm:text-2xl font-semibold text-gray-900">{{ kpi.value }}</div>
                  <div class="ml-2 flex items-baseline text-xs sm:text-sm font-semibold" 
                       :class="kpi.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
                    <ArrowUpIcon v-if="kpi.changeType === 'increase'" class="h-3 w-3 sm:h-4 sm:w-4 self-center flex-shrink-0" />
                    <ArrowDownIcon v-else class="h-3 w-3 sm:h-4 sm:w-4 self-center flex-shrink-0" />
                    {{ kpi.change }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <!-- Order Status Distribution -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">주문 상태 분포</h3>
          </div>
          <div class="p-4 sm:p-6">
            <div class="space-y-3">
              <div v-for="status in orderStatuses" :key="status.label" class="flex items-center justify-between">
                <div class="flex items-center">
                  <div class="w-3 h-3 rounded-full mr-3" :style="{ backgroundColor: status.color }"></div>
                  <span class="text-sm text-gray-700">{{ status.label }}</span>
                </div>
                <div class="text-right">
                  <div class="text-sm font-medium text-gray-900">{{ status.count }}</div>
                  <div class="text-xs text-gray-500">{{ status.percentage }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- System Status -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">시스템 상태</h3>
          </div>
          <div class="p-4 sm:p-6">
            <div class="grid grid-cols-1 gap-4">
              <div v-for="status in systemStatus" :key="status.name" class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <div class="w-3 h-3 rounded-full" :class="status.status === 'online' ? 'bg-green-400' : 'bg-red-400'"></div>
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ status.name }}</p>
                  <p class="text-xs text-gray-500">{{ status.status === 'online' ? '정상' : '오프라인' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Data Tables Row -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
        <!-- Recent Orders -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-gray-900">최근 주문</h3>
              <router-link to="/admin/orders" class="text-sm font-medium text-blue-600 hover:text-blue-500">
                전체 보기 →
              </router-link>
            </div>
          </div>
          <div class="divide-y divide-gray-200">
            <div v-for="order in recentOrders" :key="order.id" class="px-6 py-4">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ order.orderNumber }}</p>
                  <p class="text-sm text-gray-500">{{ order.customer }}</p>
                </div>
                <div class="text-right">
                  <div class="text-sm font-medium text-gray-900">{{ order.amount }}</div>
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getStatusClass(order.status)">
                    {{ order.status }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Users -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-gray-900">등록된 사용자</h3>
              <router-link to="/admin/users" class="text-sm font-medium text-blue-600 hover:text-blue-500">
                전체 보기 →
              </router-link>
            </div>
          </div>
          <div class="divide-y divide-gray-200">
            <div v-for="user in recentUsers" :key="user.id" class="px-6 py-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-8 w-8">
                    <div class="h-8 w-8 rounded-full bg-gray-300 flex items-center justify-center">
                      <span class="text-sm font-medium text-gray-700">{{ user.name.charAt(0) }}</span>
                    </div>
                  </div>
                  <div class="ml-4">
                    <p class="text-sm font-medium text-gray-900">{{ user.name }}</p>
                    <p class="text-sm text-gray-500">{{ user.email }}</p>
                  </div>
                </div>
                <div class="text-right">
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getUserTypeClass(user.type)">
                    {{ user.type }}
                  </span>
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
import {
  ArrowUpIcon,
  ArrowDownIcon,
  UsersIcon,
  ShoppingCartIcon,
  CurrencyDollarIcon,
  ChartBarIcon
} from '@heroicons/vue/24/outline'
import { useToast } from '@/composables/useToast'

// Reactive data
const dashboardData = ref({
  stats: null,
  orderStatusCounts: [],
  recentOrders: [],
  recentUsers: [],
  systemStatus: []
})

const loading = ref(false)
const { success: showSuccess, error: showError } = useToast()

// Real KPI data from API
const kpis = computed(() => {
  if (!dashboardData.value.stats) return []
  
  const stats = dashboardData.value.stats
  return [
    {
      title: '총 사용자',
      value: stats.totalUsers?.toLocaleString() || '0',
      change: `${stats.userGrowthRate || 0}%`,
      changeType: (stats.userGrowthRate || 0) >= 0 ? 'increase' : 'decrease',
      icon: UsersIcon,
      iconColor: 'text-blue-600',
      bgColor: 'bg-blue-600'
    },
    {
      title: '총 주문',
      value: stats.totalOrders?.toLocaleString() || '0',
      change: `${stats.orderGrowthRate || 0}%`,
      changeType: (stats.orderGrowthRate || 0) >= 0 ? 'increase' : 'decrease',
      icon: ShoppingCartIcon,
      iconColor: 'text-green-600',
      bgColor: 'bg-green-600'
    },
    {
      title: '총 매출',
      value: stats.totalRevenue ? `₩${(stats.totalRevenue / 1000000).toFixed(1)}M` : '₩0',
      change: `${Math.abs(stats.revenueGrowthRate || 0)}%`,
      changeType: (stats.revenueGrowthRate || 0) >= 0 ? 'increase' : 'decrease',
      icon: CurrencyDollarIcon,
      iconColor: 'text-yellow-600',
      bgColor: 'bg-yellow-600'
    },
    {
      title: '배송 완료',
      value: stats.completedDeliveries?.toLocaleString() || '0',
      change: `${stats.deliveryGrowthRate || 0}%`,
      changeType: (stats.deliveryGrowthRate || 0) >= 0 ? 'increase' : 'decrease',
      icon: ChartBarIcon,
      iconColor: 'text-purple-600',
      bgColor: 'bg-purple-600'
    }
  ]
})

// Order status data from API
const orderStatuses = computed(() => {
  return dashboardData.value.orderStatusCounts.map(status => {
    const statusLabels: Record<string, string> = {
      'pending': '대기중',
      'processing': '처리중',
      'shipped': '배송중',
      'delivered': '배송완료'
    }
    
    const statusColors: Record<string, string> = {
      'pending': '#3B82F6',
      'processing': '#F59E0B',
      'shipped': '#8B5CF6',
      'delivered': '#10B981'
    }
    
    return {
      label: statusLabels[status.status] || status.status,
      count: status.count,
      percentage: status.percentage,
      color: statusColors[status.status] || '#6B7280'
    }
  })
})

// Recent orders from API
const recentOrders = computed(() => {
  const statusLabels: Record<string, string> = {
    'pending': '대기중',
    'processing': '처리중',
    'shipped': '배송중',
    'delivered': '배송완료'
  }
  
  return dashboardData.value.recentOrders.map(order => ({
    id: order.id,
    orderNumber: order.orderNumber,
    customer: order.customerName,
    amount: `₩${order.totalAmount?.toLocaleString() || '0'}`,
    status: statusLabels[order.status] || order.status
  }))
})

const recentUsers = computed(() => {
  const roleLabels: Record<string, string> = {
    'general': '일반회원',
    'corporate': '기업회원',
    'partner': '파트너',
    'warehouse': '창고관리자',
    'admin': '관리자'
  }
  
  return dashboardData.value.recentUsers.map(user => ({
    id: user.id,
    name: user.name,
    email: user.email,
    type: roleLabels[user.role] || user.role
  }))
})

// System status from API
const systemStatus = computed(() => dashboardData.value.systemStatus || [])

// Methods
const getStatusClass = (status: string) => {
  const classes = {
    '대기중': 'bg-blue-100 text-blue-800',
    '처리중': 'bg-yellow-100 text-yellow-800',
    '배송중': 'bg-purple-100 text-purple-800',
    '배송완료': 'bg-green-100 text-green-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const getUserTypeClass = (type: string) => {
  const classes = {
    '일반회원': 'bg-gray-100 text-gray-800',
    '기업회원': 'bg-blue-100 text-blue-800',
    '파트너': 'bg-green-100 text-green-800',
    '창고관리자': 'bg-purple-100 text-purple-800',
    '관리자': 'bg-red-100 text-red-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

// API calls
const loadDashboardStats = async () => {
  try {
    const response = await fetch('/api/admin/dashboard/stats', {
      headers: {
        'x-user-email': 'admin@test.com' // Admin always sees all data
      }
    })
    const result = await response.json()
    if (result.success) {
      dashboardData.value.stats = result.data
    }
  } catch (error) {
    console.error('Failed to load dashboard stats:', error)
  }
}

const loadOrderStatus = async () => {
  try {
    const response = await fetch('/api/admin/dashboard/order-status')
    const result = await response.json()
    if (result.success) {
      dashboardData.value.orderStatusCounts = result.data
    }
  } catch (error) {
    console.error('Failed to load order status:', error)
  }
}

const loadRecentOrders = async () => {
  try {
    const response = await fetch('/api/admin/dashboard/recent-orders')
    const result = await response.json()
    if (result.success) {
      dashboardData.value.recentOrders = result.data
    }
  } catch (error) {
    console.error('Failed to load recent orders:', error)
  }
}

const loadRecentUsers = async () => {
  try {
    const response = await fetch('/api/admin/dashboard/recent-users')
    const result = await response.json()
    if (result.success) {
      dashboardData.value.recentUsers = result.data
    }
  } catch (error) {
    console.error('Failed to load recent users:', error)
  }
}

const loadSystemStatus = async () => {
  try {
    const response = await fetch('/api/admin/dashboard/system-status')
    const result = await response.json()
    if (result.success) {
      dashboardData.value.systemStatus = result.data
    }
  } catch (error) {
    console.error('Failed to load system status:', error)
  }
}

// Load all dashboard data
const loadDashboard = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadDashboardStats(),
      loadOrderStatus(),
      loadRecentOrders(),
      loadRecentUsers(),
      loadSystemStatus()
    ])
    showSuccess('대시보드 데이터를 성공적으로 로드했습니다')
  } catch (error) {
    showError('대시보드 데이터 로드에 실패했습니다')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadDashboard()
}

// Lifecycle
onMounted(() => {
  loadDashboard()
})
</script>