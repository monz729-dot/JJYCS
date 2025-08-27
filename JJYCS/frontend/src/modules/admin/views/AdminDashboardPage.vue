<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
          <p class="text-gray-600">시스템 개요 및 관리</p>
        </div>
        <div class="hidden sm:block">
          <div class="text-center">
            <div class="text-2xl font-bold text-primary-600">{{ stats.systemHealth }}%</div>
            <div class="text-sm text-gray-500">시스템 상태</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white rounded-lg shadow-sm p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
            </svg>
          </div>
          <div class="ml-5 w-0 flex-1">
            <dl>
              <dt class="text-sm font-medium text-gray-500 truncate">전체 사용자</dt>
              <dd class="text-lg font-medium text-gray-900">{{ stats.totalUsers }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <svg class="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
            </svg>
          </div>
          <div class="ml-5 w-0 flex-1">
            <dl>
              <dt class="text-sm font-medium text-gray-500 truncate">전체 주문</dt>
              <dd class="text-lg font-medium text-gray-900">{{ stats.totalOrders }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <svg class="h-8 w-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div class="ml-5 w-0 flex-1">
            <dl>
              <dt class="text-sm font-medium text-gray-500 truncate">승인 대기</dt>
              <dd class="text-lg font-medium text-gray-900">{{ stats.pendingApprovals }}</dd>
            </dl>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <svg class="h-8 w-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
            </svg>
          </div>
          <div class="ml-5 w-0 flex-1">
            <dl>
              <dt class="text-sm font-medium text-gray-500 truncate">활성 파트너</dt>
              <dd class="text-lg font-medium text-gray-900">{{ stats.activePartners }}</dd>
            </dl>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">빠른 작업</h2>
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        <router-link 
          to="/admin/users" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">사용자 관리</span>
        </router-link>

        <router-link 
          to="/admin/approvals" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">승인 관리</span>
          <span v-if="stats.pendingApprovals > 0" class="mt-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            {{ stats.pendingApprovals }}
          </span>
        </router-link>

        <router-link 
          to="/admin/orders" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">주문 관리</span>
        </router-link>

        <router-link 
          to="/admin/partners" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">파트너 관리</span>
        </router-link>

        <router-link 
          to="/admin/settings" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">설정</span>
        </router-link>

        <router-link 
          to="/admin/reports" 
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">보고서</span>
        </router-link>

        <button
          @click="runSystemCheck"
          :disabled="systemCheckLoading"
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">
            {{ systemCheckLoading ? '검사 중...' : '시스템 검사' }}
          </span>
        </button>

        <button
          @click="clearCache"
          :disabled="cacheLoading"
          class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors"
        >
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <span class="text-sm font-medium text-gray-700">
            {{ cacheLoading ? '삭제 중...' : '캐시 삭제' }}
          </span>
        </button>
      </div>
    </div>

    <!-- System Alerts -->
    <div v-if="systemAlerts.length > 0" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">시스템 알림</h2>
      <div class="space-y-3">
        <div
          v-for="alert in systemAlerts"
          :key="alert.id"
          class="flex items-start space-x-3 p-3 rounded-lg"
          :class="getAlertClass(alert.level)"
        >
          <svg
            class="w-5 h-5 mt-0.5"
            :class="getAlertIconClass(alert.level)"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              v-if="alert.level === 'error'"
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clip-rule="evenodd"
            />
            <path
              v-else-if="alert.level === 'warning'"
              fill-rule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clip-rule="evenodd"
            />
            <path
              v-else
              fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
              clip-rule="evenodd"
            />
          </svg>
          <div class="flex-1">
            <h4 class="text-sm font-medium" :class="getAlertTextClass(alert.level)">
              {{ alert.title }}
            </h4>
            <p class="mt-1 text-sm" :class="getAlertDescClass(alert.level)">
              {{ alert.message }}
            </p>
            <p class="mt-1 text-xs text-gray-500">
              {{ formatDateTime(alert.timestamp) }}
            </p>
          </div>
          <button
            @click="dismissAlert(alert.id)"
            class="text-gray-400 hover:text-gray-600"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="bg-white rounded-lg shadow-sm">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">Recent Activity</h2>
      </div>
      <div class="p-6">
        <div v-if="loading" class="space-y-4">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="flex items-center space-x-4">
              <div class="h-4 bg-gray-200 rounded w-32"></div>
              <div class="h-4 bg-gray-200 rounded w-48"></div>
              <div class="h-4 bg-gray-200 rounded w-24"></div>
            </div>
          </div>
        </div>
        <div v-else class="space-y-4">
          <div 
            v-for="activity in recentActivity" 
            :key="activity.id"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <div class="flex-1">
              <div class="text-sm font-medium text-gray-900">{{ activity.action }}</div>
              <div class="text-sm text-gray-600">
                {{ activity.description }} • {{ formatDateTime(activity.timestamp) }}
              </div>
            </div>
            <span :class="getActivityClass(activity.type)" class="status-badge text-xs">
              {{ activity.type }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface SystemAlert {
  id: string
  level: 'info' | 'warning' | 'error'
  title: string
  message: string
  timestamp: Date
}

interface RecentActivity {
  id: string
  action: string
  description: string
  type: string
  timestamp: Date
}

const loading = ref(true)
const systemCheckLoading = ref(false)
const cacheLoading = ref(false)

const stats = ref({
  totalUsers: 0,
  totalOrders: 0,
  pendingApprovals: 0,
  activePartners: 0,
  systemHealth: 0
})

const systemAlerts = ref<SystemAlert[]>([])
const recentActivity = ref<RecentActivity[]>([])

const getAlertClass = (level: string) => {
  const classes = {
    'info': 'bg-blue-50 border border-blue-200',
    'warning': 'bg-yellow-50 border border-yellow-200',
    'error': 'bg-red-50 border border-red-200'
  }
  return classes[level as keyof typeof classes] || classes.info
}

const getAlertIconClass = (level: string) => {
  const classes = {
    'info': 'text-blue-400',
    'warning': 'text-yellow-400',
    'error': 'text-red-400'
  }
  return classes[level as keyof typeof classes] || classes.info
}

const getAlertTextClass = (level: string) => {
  const classes = {
    'info': 'text-blue-800',
    'warning': 'text-yellow-800',
    'error': 'text-red-800'
  }
  return classes[level as keyof typeof classes] || classes.info
}

const getAlertDescClass = (level: string) => {
  const classes = {
    'info': 'text-blue-700',
    'warning': 'text-yellow-700',
    'error': 'text-red-700'
  }
  return classes[level as keyof typeof classes] || classes.info
}

const getActivityClass = (type: string) => {
  const classes = {
    'USER': 'bg-blue-100 text-blue-800',
    'ORDER': 'bg-green-100 text-green-800',
    'SYSTEM': 'bg-purple-100 text-purple-800',
    'ERROR': 'bg-red-100 text-red-800'
  }
  return classes[type as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const formatDateTime = (date: Date) => {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    // TODO: Load actual admin dashboard data from API
    await new Promise(resolve => setTimeout(resolve, 1000)) // Mock loading
    
    stats.value = {
      totalUsers: 1247,
      totalOrders: 3856,
      pendingApprovals: 8,
      activePartners: 23,
      systemHealth: 98
    }

    systemAlerts.value = [
      {
        id: '1',
        level: 'warning',
        title: 'High Memory Usage',
        message: 'Server memory usage is at 85%. Consider restarting services.',
        timestamp: new Date(Date.now() - 30 * 60 * 1000) // 30 minutes ago
      },
      {
        id: '2',
        level: 'info',
        title: 'Maintenance Scheduled',
        message: 'System maintenance scheduled for tomorrow at 2 AM KST.',
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000) // 2 hours ago
      }
    ]

    recentActivity.value = [
      {
        id: '1',
        action: 'User Registration',
        description: 'New corporate account created: TechCorp Ltd.',
        type: 'USER',
        timestamp: new Date(Date.now() - 15 * 60 * 1000)
      },
      {
        id: '2',
        action: 'Order Created',
        description: 'Order ORD-2024-001 created by John Doe',
        type: 'ORDER',
        timestamp: new Date(Date.now() - 25 * 60 * 1000)
      },
      {
        id: '3',
        action: 'Partner Approved',
        description: 'Partner application approved: Seoul Logistics',
        type: 'USER',
        timestamp: new Date(Date.now() - 45 * 60 * 1000)
      }
    ]
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

const runSystemCheck = async () => {
  systemCheckLoading.value = true
  try {
    // TODO: Call API to run system check
    await new Promise(resolve => setTimeout(resolve, 2000)) // Mock API call
    console.log('System check completed')
    
    // Update system health
    stats.value.systemHealth = Math.min(100, stats.value.systemHealth + 1)
  } catch (error) {
    console.error('Failed to run system check:', error)
  } finally {
    systemCheckLoading.value = false
  }
}

const clearCache = async () => {
  cacheLoading.value = true
  try {
    // TODO: Call API to clear cache
    await new Promise(resolve => setTimeout(resolve, 1500)) // Mock API call
    console.log('Cache cleared')
  } catch (error) {
    console.error('Failed to clear cache:', error)
  } finally {
    cacheLoading.value = false
  }
}

const dismissAlert = (alertId: string) => {
  systemAlerts.value = systemAlerts.value.filter(alert => alert.id !== alertId)
}

onMounted(() => {
  loadDashboardData()
})
</script>