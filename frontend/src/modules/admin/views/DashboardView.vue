<template>
  <div class="space-y-6">
    <!-- Page header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <h1 class="text-2xl font-bold text-gray-900">대시보드</h1>
      <div class="mt-3 sm:mt-0">
        <select v-model="selectedPeriod" class="rounded-md border border-gray-300 px-3 py-2 text-sm">
          <option value="today">오늘</option>
          <option value="week">이번 주</option>
          <option value="month">이번 달</option>
          <option value="quarter">이번 분기</option>
        </select>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="kpi in kpis" :key="kpi.title" class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <component :is="kpi.icon" class="h-8 w-8" :class="kpi.iconColor" />
          </div>
          <div class="ml-5 w-0 flex-1">
            <dl>
              <dt class="text-sm font-medium text-gray-500 truncate">{{ kpi.title }}</dt>
              <dd class="flex items-baseline">
                <div class="text-2xl font-semibold text-gray-900">{{ kpi.value }}</div>
                <div class="ml-2 flex items-baseline text-sm font-semibold" 
                     :class="kpi.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
                  <ArrowUpIcon v-if="kpi.changeType === 'increase'" class="h-4 w-4 self-center flex-shrink-0" />
                  <ArrowDownIcon v-else class="h-4 w-4 self-center flex-shrink-0" />
                  {{ kpi.change }}
                </div>
              </dd>
            </dl>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Revenue Chart -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium text-gray-900">매출 추이</h3>
          <div class="flex space-x-2">
            <button 
              v-for="period in ['7일', '30일', '90일']" 
              :key="period"
              @click="selectedRevenuePeriod = period"
              class="px-3 py-1 text-xs font-medium rounded-full"
              :class="selectedRevenuePeriod === period ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-700'"
            >
              {{ period }}
            </button>
          </div>
        </div>
        <div class="h-64 flex items-center justify-center bg-gray-50 rounded">
          <p class="text-gray-500">Revenue Chart Placeholder</p>
        </div>
      </div>

      <!-- Order Status Distribution -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-lg font-medium text-gray-900 mb-4">주문 상태 분포</h3>
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

    <!-- Recent Activities -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Orders -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-medium text-gray-900">최근 주문</h3>
            <router-link to="/admin/orders" class="text-sm text-blue-600 hover:text-blue-500">
              전체 보기
            </router-link>
          </div>
        </div>
        <div class="divide-y divide-gray-200">
          <div v-for="order in recentOrders" :key="order.id" class="px-6 py-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm font-medium text-gray-900">#{{ order.orderNumber }}</p>
                <p class="text-sm text-gray-500">{{ order.customer }}</p>
              </div>
              <div class="text-right">
                <p class="text-sm font-medium text-gray-900">{{ order.amount }}</p>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- System Alerts -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">시스템 알림</h3>
        </div>
        <div class="divide-y divide-gray-200">
          <div v-for="alert in systemAlerts" :key="alert.id" class="px-6 py-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <component :is="alert.icon" class="h-5 w-5" :class="alert.iconColor" />
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-gray-900">{{ alert.title }}</p>
                <p class="text-sm text-gray-500">{{ alert.message }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ alert.time }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Performance Metrics -->
    <div class="bg-white rounded-lg shadow p-6">
      <h3 class="text-lg font-medium text-gray-900 mb-4">시스템 성능</h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="metric in performanceMetrics" :key="metric.name" class="text-center">
          <div class="text-2xl font-bold" :class="metric.color">{{ metric.value }}</div>
          <div class="text-sm text-gray-500">{{ metric.name }}</div>
          <div class="mt-2">
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div class="h-2 rounded-full" 
                   :class="metric.barColor" 
                   :style="{ width: metric.percentage + '%' }">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  UsersIcon,
  ShoppingBagIcon,
  CurrencyDollarIcon,
  TruckIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  CheckCircleIcon
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
    iconColor: 'text-blue-600'
  },
  {
    title: '금일 주문',
    value: '156',
    change: '8.1%',
    changeType: 'increase',
    icon: ShoppingBagIcon,
    iconColor: 'text-green-600'
  },
  {
    title: '금일 매출',
    value: '₩12.4M',
    change: '3.2%',
    changeType: 'decrease',
    icon: CurrencyDollarIcon,
    iconColor: 'text-yellow-600'
  },
  {
    title: '배송 완료',
    value: '89',
    change: '15.7%',
    changeType: 'increase',
    icon: TruckIcon,
    iconColor: 'text-purple-600'
  }
])

// Order status data
const orderStatuses = ref([
  { label: '주문 접수', count: 45, percentage: 28.1, color: '#3B82F6' },
  { label: '입고 완료', count: 32, percentage: 20.0, color: '#10B981' },
  { label: '출고 준비', count: 28, percentage: 17.5, color: '#F59E0B' },
  { label: '배송 중', count: 35, percentage: 21.9, color: '#8B5CF6' },
  { label: '배송 완료', count: 20, percentage: 12.5, color: '#6B7280' }
])

// Recent orders
const recentOrders = ref([
  { id: 1, orderNumber: 'YCS240001', customer: '김철수', amount: '₩485,000', status: 'processing' },
  { id: 2, orderNumber: 'YCS240002', customer: '박영희', amount: '₩320,000', status: 'completed' },
  { id: 3, orderNumber: 'YCS240003', customer: '이민준', amount: '₩750,000', status: 'shipping' },
  { id: 4, orderNumber: 'YCS240004', customer: '최서연', amount: '₩125,000', status: 'pending' },
  { id: 5, orderNumber: 'YCS240005', customer: '정도현', amount: '₩950,000', status: 'processing' }
])

// System alerts
const systemAlerts = ref([
  {
    id: 1,
    title: '서버 리소스 경고',
    message: 'CPU 사용률이 85%를 초과했습니다.',
    time: '5분 전',
    icon: ExclamationTriangleIcon,
    iconColor: 'text-yellow-500'
  },
  {
    id: 2,
    title: '데이터베이스 백업 완료',
    message: '일일 데이터베이스 백업이 성공적으로 완료되었습니다.',
    time: '1시간 전',
    icon: CheckCircleIcon,
    iconColor: 'text-green-500'
  },
  {
    id: 3,
    title: '새로운 업데이트 알림',
    message: '시스템 업데이트 v2.4.1이 사용 가능합니다.',
    time: '2시간 전',
    icon: InformationCircleIcon,
    iconColor: 'text-blue-500'
  }
])

// Performance metrics
const performanceMetrics = ref([
  { name: 'CPU 사용률', value: '72%', color: 'text-yellow-600', barColor: 'bg-yellow-500', percentage: 72 },
  { name: '메모리 사용률', value: '58%', color: 'text-green-600', barColor: 'bg-green-500', percentage: 58 },
  { name: '디스크 사용률', value: '34%', color: 'text-blue-600', barColor: 'bg-blue-500', percentage: 34 },
  { name: '네트워크 I/O', value: '91%', color: 'text-red-600', barColor: 'bg-red-500', percentage: 91 }
])

const getStatusClass = (status: string) => {
  const classes = {
    'pending': 'bg-yellow-100 text-yellow-800',
    'processing': 'bg-blue-100 text-blue-800',
    'shipping': 'bg-purple-100 text-purple-800',
    'completed': 'bg-green-100 text-green-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'pending': '대기',
    'processing': '처리중',
    'shipping': '배송중',
    'completed': '완료'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}
</script>