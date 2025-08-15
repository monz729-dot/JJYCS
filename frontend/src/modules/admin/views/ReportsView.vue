<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">시스템 리포트</h1>
        <p class="text-sm text-gray-600 mt-1">시스템 성과와 분석 데이터를 확인합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex space-x-3">
        <div class="flex items-center space-x-2">
          <label class="text-sm text-gray-700">기간:</label>
          <select v-model="selectedPeriod" @change="loadReports" 
                  class="rounded-md border border-gray-300 px-3 py-2 text-sm">
            <option value="7">최근 7일</option>
            <option value="30">최근 30일</option>
            <option value="90">최근 90일</option>
            <option value="365">최근 1년</option>
          </select>
        </div>
        <button @click="exportAllReports" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <ArrowDownTrayIcon class="h-4 w-4 mr-2" />
          전체 내보내기
        </button>
      </div>
    </div>

    <!-- Report Tabs -->
    <div class="bg-white rounded-lg shadow">
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-8 px-6" aria-label="Tabs">
          <button
            v-for="tab in reportTabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap"
            :class="activeTab === tab.key
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
          >
            <component :is="tab.icon" class="h-5 w-5 mr-2 inline" />
            {{ tab.label }}
          </button>
        </nav>
      </div>

      <!-- Revenue Report -->
      <div v-if="activeTab === 'revenue'" class="p-6">
        <div class="space-y-6">
          <!-- Revenue Summary -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="metric in revenueMetrics" :key="metric.title" class="bg-gray-50 rounded-lg p-4">
              <div class="flex items-center">
                <component :is="metric.icon" class="h-8 w-8" :class="metric.iconColor" />
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500">{{ metric.title }}</p>
                  <p class="text-2xl font-semibold text-gray-900">{{ metric.value }}</p>
                  <p class="text-xs" :class="metric.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
                    {{ metric.change }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Revenue Chart -->
          <div class="bg-gray-50 rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">매출 추이</h3>
            <div class="h-64 flex items-center justify-center bg-white rounded border-2 border-dashed border-gray-300">
              <p class="text-gray-500">Revenue Chart Placeholder</p>
            </div>
          </div>

          <!-- Revenue Table -->
          <div class="bg-gray-50 rounded-lg p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-medium text-gray-900">일별 매출 내역</h3>
              <button @click="exportReport('revenue')" 
                      class="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700">
                내보내기
              </button>
            </div>
            <div class="overflow-x-auto">
              <table class="w-full divide-y divide-gray-200">
                <thead class="bg-white">
                  <tr>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">날짜</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주문 수</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">매출</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수수료</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">순이익</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="item in revenueData" :key="item.date">
                    <td class="px-4 py-2 text-sm text-gray-900">{{ formatDate(item.date) }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ item.orders }}</td>
                    <td class="px-4 py-2 text-sm text-gray-900">{{ item.revenue }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ item.fees }}</td>
                    <td class="px-4 py-2 text-sm font-medium text-green-600">{{ item.profit }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Order Report -->
      <div v-if="activeTab === 'orders'" class="p-6">
        <div class="space-y-6">
          <!-- Order Summary -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="metric in orderMetrics" :key="metric.title" class="bg-gray-50 rounded-lg p-4">
              <div class="flex items-center">
                <component :is="metric.icon" class="h-8 w-8" :class="metric.iconColor" />
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500">{{ metric.title }}</p>
                  <p class="text-2xl font-semibold text-gray-900">{{ metric.value }}</p>
                  <p class="text-xs" :class="metric.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
                    {{ metric.change }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Status Distribution -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-gray-50 rounded-lg p-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">주문 상태 분포</h3>
              <div class="space-y-3">
                <div v-for="status in orderStatusData" :key="status.label" 
                     class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-3 h-3 rounded-full mr-3" :style="{ backgroundColor: status.color }"></div>
                    <span class="text-sm text-gray-700">{{ status.label }}</span>
                  </div>
                  <div class="text-right">
                    <div class="text-sm font-medium text-gray-900">{{ status.count }}건</div>
                    <div class="text-xs text-gray-500">{{ status.percentage }}%</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 rounded-lg p-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">배송 방식 분포</h3>
              <div class="space-y-3">
                <div v-for="method in shippingMethodData" :key="method.label" 
                     class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-3 h-3 rounded-full mr-3" :style="{ backgroundColor: method.color }"></div>
                    <span class="text-sm text-gray-700">{{ method.label }}</span>
                  </div>
                  <div class="text-right">
                    <div class="text-sm font-medium text-gray-900">{{ method.count }}건</div>
                    <div class="text-xs text-gray-500">{{ method.percentage }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Top Customers -->
          <div class="bg-gray-50 rounded-lg p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-medium text-gray-900">주요 고객</h3>
              <button @click="exportReport('customers')" 
                      class="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700">
                내보내기
              </button>
            </div>
            <div class="overflow-x-auto">
              <table class="w-full divide-y divide-gray-200">
                <thead class="bg-white">
                  <tr>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">순위</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">고객명</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주문 수</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">총 금액</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">평균 주문</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="(customer, index) in topCustomers" :key="customer.id">
                    <td class="px-4 py-2 text-sm text-gray-900">{{ index + 1 }}</td>
                    <td class="px-4 py-2 text-sm text-gray-900">{{ customer.name }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ customer.orderCount }}</td>
                    <td class="px-4 py-2 text-sm text-gray-900">{{ customer.totalAmount }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ customer.averageOrder }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- User Report -->
      <div v-if="activeTab === 'users'" class="p-6">
        <div class="space-y-6">
          <!-- User Summary -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="metric in userMetrics" :key="metric.title" class="bg-gray-50 rounded-lg p-4">
              <div class="flex items-center">
                <component :is="metric.icon" class="h-8 w-8" :class="metric.iconColor" />
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500">{{ metric.title }}</p>
                  <p class="text-2xl font-semibold text-gray-900">{{ metric.value }}</p>
                  <p class="text-xs" :class="metric.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
                    {{ metric.change }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- User Registration Trend -->
          <div class="bg-gray-50 rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">신규 가입자 추이</h3>
            <div class="h-64 flex items-center justify-center bg-white rounded border-2 border-dashed border-gray-300">
              <p class="text-gray-500">User Registration Chart Placeholder</p>
            </div>
          </div>

          <!-- User Role Distribution -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div class="bg-gray-50 rounded-lg p-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">사용자 역할 분포</h3>
              <div class="space-y-3">
                <div v-for="role in userRoleData" :key="role.label" 
                     class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-3 h-3 rounded-full mr-3" :style="{ backgroundColor: role.color }"></div>
                    <span class="text-sm text-gray-700">{{ role.label }}</span>
                  </div>
                  <div class="text-right">
                    <div class="text-sm font-medium text-gray-900">{{ role.count }}명</div>
                    <div class="text-xs text-gray-500">{{ role.percentage }}%</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 rounded-lg p-6">
              <h3 class="text-lg font-medium text-gray-900 mb-4">활성 사용자</h3>
              <div class="space-y-4">
                <div>
                  <div class="flex justify-between text-sm mb-1">
                    <span>일간 활성 사용자</span>
                    <span class="font-medium">847명</span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div class="bg-green-500 h-2 rounded-full" style="width: 68%"></div>
                  </div>
                </div>
                <div>
                  <div class="flex justify-between text-sm mb-1">
                    <span>주간 활성 사용자</span>
                    <span class="font-medium">1,245명</span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div class="bg-blue-500 h-2 rounded-full" style="width: 84%"></div>
                  </div>
                </div>
                <div>
                  <div class="flex justify-between text-sm mb-1">
                    <span>월간 활성 사용자</span>
                    <span class="font-medium">2,847명</span>
                  </div>
                  <div class="w-full bg-gray-200 rounded-full h-2">
                    <div class="bg-purple-500 h-2 rounded-full" style="width: 96%"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Performance Report -->
      <div v-if="activeTab === 'performance'" class="p-6">
        <div class="space-y-6">
          <!-- System Performance -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div v-for="metric in performanceMetrics" :key="metric.title" class="bg-gray-50 rounded-lg p-4">
              <div class="flex items-center">
                <component :is="metric.icon" class="h-8 w-8" :class="metric.iconColor" />
                <div class="ml-4">
                  <p class="text-sm font-medium text-gray-500">{{ metric.title }}</p>
                  <p class="text-2xl font-semibold text-gray-900">{{ metric.value }}</p>
                  <div class="w-full bg-gray-200 rounded-full h-1 mt-2">
                    <div class="h-1 rounded-full" 
                         :class="metric.barColor" 
                         :style="{ width: metric.percentage + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- API Performance -->
          <div class="bg-gray-50 rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">API 성능</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div v-for="api in apiPerformance" :key="api.name" 
                   class="bg-white rounded-lg p-4 border">
                <h4 class="font-medium text-gray-900 mb-2">{{ api.name }}</h4>
                <div class="space-y-2">
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-500">평균 응답시간</span>
                    <span class="font-medium">{{ api.responseTime }}ms</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-500">성공률</span>
                    <span class="font-medium text-green-600">{{ api.successRate }}%</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-gray-500">일일 호출</span>
                    <span class="font-medium">{{ api.dailyCalls }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Error Logs -->
          <div class="bg-gray-50 rounded-lg p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-medium text-gray-900">최근 오류 로그</h3>
              <button @click="exportReport('errors')" 
                      class="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700">
                내보내기
              </button>
            </div>
            <div class="overflow-x-auto">
              <table class="w-full divide-y divide-gray-200">
                <thead class="bg-white">
                  <tr>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">시간</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">레벨</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">메시지</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">모듈</th>
                    <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="error in errorLogs" :key="error.id">
                    <td class="px-4 py-2 text-sm text-gray-500">{{ formatDateTime(error.timestamp) }}</td>
                    <td class="px-4 py-2">
                      <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                            :class="getLogLevelClass(error.level)">
                        {{ error.level }}
                      </span>
                    </td>
                    <td class="px-4 py-2 text-sm text-gray-900">{{ error.message }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ error.module }}</td>
                    <td class="px-4 py-2 text-sm text-gray-500">{{ error.user || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  ArrowDownTrayIcon,
  CurrencyDollarIcon,
  ShoppingBagIcon,
  UsersIcon,
  ChartBarIcon,
  ServerIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'

// State
const loading = ref(false)
const activeTab = ref('revenue')
const selectedPeriod = ref('30')

const reportTabs = [
  { key: 'revenue', label: '매출 분석', icon: CurrencyDollarIcon },
  { key: 'orders', label: '주문 분석', icon: ShoppingBagIcon },
  { key: 'users', label: '사용자 분석', icon: UsersIcon },
  { key: 'performance', label: '성능 분석', icon: ChartBarIcon }
]

// Mock data
const revenueMetrics = ref([
  {
    title: '총 매출',
    value: '₩45.2M',
    change: '+12.3%',
    changeType: 'increase',
    icon: CurrencyDollarIcon,
    iconColor: 'text-green-600'
  },
  {
    title: '평균 주문액',
    value: '₩125,000',
    change: '+8.1%',
    changeType: 'increase',
    icon: ChartBarIcon,
    iconColor: 'text-blue-600'
  },
  {
    title: '수수료 수익',
    value: '₩2.8M',
    change: '+15.7%',
    changeType: 'increase',
    icon: CurrencyDollarIcon,
    iconColor: 'text-purple-600'
  },
  {
    title: '순이익',
    value: '₩8.4M',
    change: '+9.2%',
    changeType: 'increase',
    icon: ChartBarIcon,
    iconColor: 'text-yellow-600'
  }
])

const revenueData = ref([
  { date: '2024-01-20', orders: 45, revenue: '₩1.2M', fees: '₩120K', profit: '₩280K' },
  { date: '2024-01-19', orders: 38, revenue: '₩950K', fees: '₩95K', profit: '₩220K' },
  { date: '2024-01-18', orders: 52, revenue: '₩1.4M', fees: '₩140K', profit: '₩320K' },
  { date: '2024-01-17', orders: 41, revenue: '₩1.1M', fees: '₩110K', profit: '₩260K' },
  { date: '2024-01-16', orders: 35, revenue: '₩890K', fees: '₩89K', profit: '₩195K' }
])

const orderMetrics = ref([
  {
    title: '총 주문',
    value: '1,247',
    change: '+18.5%',
    changeType: 'increase',
    icon: ShoppingBagIcon,
    iconColor: 'text-blue-600'
  },
  {
    title: '완료 주문',
    value: '1,089',
    change: '+22.1%',
    changeType: 'increase',
    icon: CheckCircleIcon,
    iconColor: 'text-green-600'
  },
  {
    title: '평균 처리시간',
    value: '2.4일',
    change: '-12%',
    changeType: 'decrease',
    icon: ClockIcon,
    iconColor: 'text-yellow-600'
  },
  {
    title: '완료율',
    value: '87.3%',
    change: '+3.2%',
    changeType: 'increase',
    icon: ChartBarIcon,
    iconColor: 'text-purple-600'
  }
])

const orderStatusData = ref([
  { label: '주문 접수', count: 156, percentage: 12.5, color: '#3B82F6' },
  { label: '처리중', count: 234, percentage: 18.8, color: '#8B5CF6' },
  { label: '창고 도착', count: 189, percentage: 15.2, color: '#F59E0B' },
  { label: '배송중', count: 312, percentage: 25.0, color: '#10B981' },
  { label: '배송 완료', count: 356, percentage: 28.5, color: '#6B7280' }
])

const shippingMethodData = ref([
  { label: '항공', count: 745, percentage: 59.7, color: '#3B82F6' },
  { label: '해상', count: 502, percentage: 40.3, color: '#10B981' }
])

const topCustomers = ref([
  { id: 1, name: '김철수', orderCount: 24, totalAmount: '₩2.4M', averageOrder: '₩100K' },
  { id: 2, name: '박영희', orderCount: 18, totalAmount: '₩1.8M', averageOrder: '₩100K' },
  { id: 3, name: '이민준', orderCount: 22, totalAmount: '₩1.6M', averageOrder: '₩73K' },
  { id: 4, name: '최서연', orderCount: 15, totalAmount: '₩1.5M', averageOrder: '₩100K' },
  { id: 5, name: '정도현', orderCount: 12, totalAmount: '₩1.2M', averageOrder: '₩100K' }
])

const userMetrics = ref([
  {
    title: '총 사용자',
    value: '2,847',
    change: '+156',
    changeType: 'increase',
    icon: UsersIcon,
    iconColor: 'text-blue-600'
  },
  {
    title: '활성 사용자',
    value: '1,245',
    change: '+89',
    changeType: 'increase',
    icon: CheckCircleIcon,
    iconColor: 'text-green-600'
  },
  {
    title: '신규 가입',
    value: '67',
    change: '+12',
    changeType: 'increase',
    icon: UsersIcon,
    iconColor: 'text-purple-600'
  },
  {
    title: '승인 대기',
    value: '23',
    change: '+5',
    changeType: 'increase',
    icon: ClockIcon,
    iconColor: 'text-yellow-600'
  }
])

const userRoleData = ref([
  { label: '일반 사용자', count: 1847, percentage: 64.9, color: '#6B7280' },
  { label: '기업 사용자', count: 543, percentage: 19.1, color: '#3B82F6' },
  { label: '파트너', count: 234, percentage: 8.2, color: '#10B981' },
  { label: '창고', count: 156, percentage: 5.5, color: '#8B5CF6' },
  { label: '관리자', count: 67, percentage: 2.4, color: '#EF4444' }
])

const performanceMetrics = ref([
  {
    title: 'CPU 사용률',
    value: '72%',
    percentage: 72,
    icon: ServerIcon,
    iconColor: 'text-yellow-600',
    barColor: 'bg-yellow-500'
  },
  {
    title: '메모리 사용률',
    value: '58%',
    percentage: 58,
    icon: ServerIcon,
    iconColor: 'text-green-600',
    barColor: 'bg-green-500'
  },
  {
    title: '응답 시간',
    value: '245ms',
    percentage: 85,
    icon: ClockIcon,
    iconColor: 'text-blue-600',
    barColor: 'bg-blue-500'
  },
  {
    title: '가동률',
    value: '99.8%',
    percentage: 99,
    icon: CheckCircleIcon,
    iconColor: 'text-green-600',
    barColor: 'bg-green-500'
  }
])

const apiPerformance = ref([
  {
    name: 'EMS API',
    responseTime: 145,
    successRate: 99.2,
    dailyCalls: '2,847'
  },
  {
    name: 'HS 코드 API',
    responseTime: 89,
    successRate: 98.7,
    dailyCalls: '1,234'
  },
  {
    name: '환율 API',
    responseTime: 234,
    successRate: 97.5,
    dailyCalls: '567'
  }
])

const errorLogs = ref([
  {
    id: 1,
    timestamp: '2024-01-20T14:30:00',
    level: 'ERROR',
    message: 'Database connection timeout',
    module: 'OrderService',
    user: 'user123'
  },
  {
    id: 2,
    timestamp: '2024-01-20T14:25:00',
    level: 'WARN',
    message: 'EMS API rate limit exceeded',
    module: 'ExternalAPI',
    user: null
  },
  {
    id: 3,
    timestamp: '2024-01-20T14:20:00',
    level: 'ERROR',
    message: 'Failed to process payment',
    module: 'PaymentService',
    user: 'user456'
  }
])

// Methods
const loadReports = () => {
  
}

const exportAllReports = () => {
  
}

const exportReport = (type: string) => {
  
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric'
  })
}

const formatDateTime = (date: string) => {
  return new Date(date).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getLogLevelClass = (level: string) => {
  const classes = {
    'ERROR': 'bg-red-100 text-red-800',
    'WARN': 'bg-yellow-100 text-yellow-800',
    'INFO': 'bg-blue-100 text-blue-800',
    'DEBUG': 'bg-gray-100 text-gray-800'
  }
  return classes[level as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}
</script>