<template>
  <div class="min-h-screen bg-gray-50 py-4 sm:py-6">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Page header -->
      <div class="mb-6 sm:mb-8">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">관리자 대시보드</h1>
            <p class="mt-1 text-sm text-gray-600">시스템 현황 및 주요 지표</p>
          </div>
          <div class="flex-shrink-0">
            <select 
              v-model="selectedPeriod" 
              class="block w-full sm:w-auto border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            >
              <option value="today">오늘</option>
              <option value="week">이번 주</option>
              <option value="month">이번 달</option>
              <option value="quarter">이번 분기</option>
            </select>
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
        <!-- Revenue Chart -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-3 sm:space-y-0">
              <h3 class="text-lg font-medium text-gray-900">매출 추이</h3>
              <div class="flex space-x-1 sm:space-x-2">
                <button 
                  v-for="period in ['7일', '30일', '90일']" 
                  :key="period"
                  @click="selectedRevenuePeriod = period"
                  class="px-2 sm:px-3 py-1 text-xs font-medium rounded-full"
                  :class="selectedRevenuePeriod === period ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700'"
                >
                  {{ period }}
                </button>
              </div>
            </div>
          </div>
          <div class="p-4 sm:p-6">
            <div class="h-48 sm:h-64 flex items-center justify-center bg-gray-50 rounded-lg">
              <div class="text-center">
                <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                </svg>
                <p class="mt-2 text-sm text-gray-500">매출 차트</p>
              </div>
            </div>
          </div>
        </div>

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
      </div>

      <!-- Recent Activities -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
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
            <!-- Desktop Table -->
            <div class="hidden sm:block">
              <div v-for="order in recentOrders" :key="order.id" class="px-6 py-4">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium text-gray-900">#{{ order.orderNumber }}</p>
                    <p class="text-sm text-gray-500">{{ order.customer }}</p>
                  </div>
                  <div class="text-right">
                    <p class="text-sm font-medium text-gray-900">{{ order.amount }}</p>
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getStatusClass(order.status)">
                      {{ order.status }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Mobile Cards -->
            <div class="sm:hidden">
              <div v-for="order in recentOrders" :key="order.id" class="p-4">
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <div class="flex items-center space-x-2">
                      <p class="text-sm font-medium text-gray-900">#{{ order.orderNumber }}</p>
                      <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getStatusClass(order.status)">
                        {{ order.status }}
                      </span>
                    </div>
                    <p class="text-sm text-gray-500 mt-1">{{ order.customer }}</p>
                  </div>
                  <div class="text-right">
                    <p class="text-sm font-semibold text-gray-900">{{ order.amount }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Users -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-4 sm:px-6 py-4 sm:py-6 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-gray-900">최근 가입자</h3>
              <router-link to="/admin/users" class="text-sm font-medium text-blue-600 hover:text-blue-500">
                전체 보기 →
              </router-link>
            </div>
          </div>
          <div class="divide-y divide-gray-200">
            <!-- Desktop Table -->
            <div class="hidden sm:block">
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

            <!-- Mobile Cards -->
            <div class="sm:hidden">
              <div v-for="user in recentUsers" :key="user.id" class="p-4">
                <div class="flex items-start justify-between">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0 h-8 w-8">
                      <div class="h-8 w-8 rounded-full bg-gray-300 flex items-center justify-center">
                        <span class="text-sm font-medium text-gray-700">{{ user.name.charAt(0) }}</span>
                      </div>
                    </div>
                    <div>
                      <div class="flex items-center space-x-2">
                        <p class="text-sm font-medium text-gray-900">{{ user.name }}</p>
                        <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="getUserTypeClass(user.type)">
                          {{ user.type }}
                        </span>
                      </div>
                      <p class="text-sm text-gray-500 mt-1">{{ user.email }}</p>
                    </div>
                  </div>
                </div>
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
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
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

const selectedPeriod = ref('today')
const selectedRevenuePeriod = ref('30일')

// KPI data
const kpis = ref([
  {
    title: '총 사용자',
    value: '2,847',
    change: '12.3%',
    changeType: 'increase',
    icon: UsersIcon,
    iconColor: 'text-blue-600',
    bgColor: 'bg-blue-600'
  },
  {
    title: '금일 주문',
    value: '156',
    change: '8.1%',
    changeType: 'increase',
    icon: ShoppingCartIcon,
    iconColor: 'text-green-600',
    bgColor: 'bg-green-600'
  },
  {
    title: '금일 매출',
    value: '₩12.4M',
    change: '3.2%',
    changeType: 'decrease',
    icon: CurrencyDollarIcon,
    iconColor: 'text-yellow-600',
    bgColor: 'bg-yellow-600'
  },
  {
    title: '배송 완료',
    value: '89',
    change: '15.7%',
    changeType: 'increase',
    icon: ChartBarIcon,
    iconColor: 'text-purple-600',
    bgColor: 'bg-purple-600'
  }
])

// Order status data
const orderStatuses = ref([
  { label: '주문 접수', count: 45, percentage: 28.8, color: '#3B82F6' },
  { label: '처리 중', count: 32, percentage: 20.5, color: '#F59E0B' },
  { label: '배송 중', count: 28, percentage: 17.9, color: '#8B5CF6' },
  { label: '배송 완료', count: 51, percentage: 32.7, color: '#10B981' }
])

// Recent orders data
const recentOrders = ref([
  { id: 1, orderNumber: 'ORD-001', customer: '김철수', amount: '₩125,000', status: '주문 접수' },
  { id: 2, orderNumber: 'ORD-002', customer: '이영희', amount: '₩89,500', status: '처리 중' },
  { id: 3, orderNumber: 'ORD-003', customer: '박민수', amount: '₩234,000', status: '배송 중' },
  { id: 4, orderNumber: 'ORD-004', customer: '최지영', amount: '₩67,800', status: '배송 완료' },
  { id: 5, orderNumber: 'ORD-005', customer: '정현우', amount: '₩156,200', status: '주문 접수' }
])

// Recent users data
const recentUsers = ref([
  { id: 1, name: '김철수', email: 'kim@example.com', type: '일반' },
  { id: 2, name: '이영희', email: 'lee@example.com', type: '기업' },
  { id: 3, name: '박민수', email: 'park@example.com', type: '파트너' },
  { id: 4, name: '최지영', email: 'choi@example.com', type: '일반' },
  { id: 5, name: '정현우', email: 'jung@example.com', type: '기업' }
])

// System status data
const systemStatus = ref([
  { name: '웹 서버', status: 'online' },
  { name: '데이터베이스', status: 'online' },
  { name: '결제 시스템', status: 'online' },
  { name: '배송 추적', status: 'online' }
])

// Methods
const getStatusClass = (status: string) => {
  const classes = {
    '주문 접수': 'bg-blue-100 text-blue-800',
    '처리 중': 'bg-yellow-100 text-yellow-800',
    '배송 중': 'bg-purple-100 text-purple-800',
    '배송 완료': 'bg-green-100 text-green-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const getUserTypeClass = (type: string) => {
  const classes = {
    '일반': 'bg-gray-100 text-gray-800',
    '기업': 'bg-blue-100 text-blue-800',
    '파트너': 'bg-green-100 text-green-800',
    '관리자': 'bg-red-100 text-red-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

// Lifecycle
onMounted(() => {
  // Load dashboard data
  console.log('Dashboard mounted')
})
</script>