<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-6">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">결제 관리</h1>
            <p class="mt-1 text-sm text-gray-500">결제 내역 및 처리 상태를 관리하세요</p>
          </div>
          <div class="flex items-center space-x-4">
            <!-- Quick Actions -->
            <button
              @click="refreshData"
              :disabled="loading"
              class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <ArrowPathIcon class="h-4 w-4 mr-2" :class="{ 'animate-spin': loading }" />
              새로고침
            </button>
            <router-link
              to="/payments/process"
              class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <PlusIcon class="h-4 w-4 mr-2" />
              결제 처리
            </router-link>
          </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="border-t border-gray-200">
          <nav class="-mb-px flex space-x-8" aria-label="Tabs">
            <router-link
              v-for="tab in tabs"
              :key="tab.name"
              :to="tab.to"
              class="py-2 px-1 border-b-2 font-medium text-sm whitespace-nowrap"
              :class="isActiveTab(tab.to) 
                ? 'border-blue-500 text-blue-600' 
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
            >
              <component :is="tab.icon" class="h-5 w-5 inline mr-2" />
              {{ tab.name }}
              <span
                v-if="tab.count"
                class="ml-2 py-0.5 px-2 rounded-full text-xs"
                :class="isActiveTab(tab.to) 
                  ? 'bg-blue-100 text-blue-600' 
                  : 'bg-gray-100 text-gray-900'"
              >
                {{ tab.count }}
              </span>
            </router-link>
          </nav>
        </div>
      </div>
    </div>

    <!-- Content Area -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span class="ml-2 text-gray-600">로딩 중...</span>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="rounded-md bg-red-50 p-4 mb-6">
        <div class="flex">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">오류 발생</h3>
            <p class="mt-1 text-sm text-red-700">{{ error }}</p>
            <button
              @click="refreshData"
              class="mt-2 text-sm font-medium text-red-600 hover:text-red-500"
            >
              다시 시도
            </button>
          </div>
        </div>
      </div>

      <!-- Router View -->
      <router-view v-else />
    </main>

    <!-- Toast Notifications -->
    <div
      v-if="notifications.length > 0"
      class="fixed inset-0 flex items-end justify-center px-4 py-6 pointer-events-none sm:p-6 sm:items-start sm:justify-end z-50"
    >
      <div class="w-full flex flex-col items-center space-y-4 sm:items-end">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden"
        >
          <div class="p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <CheckCircleIcon
                  v-if="notification.type === 'success'"
                  class="h-6 w-6 text-green-400"
                />
                <ExclamationTriangleIcon
                  v-else-if="notification.type === 'warning'"
                  class="h-6 w-6 text-yellow-400"
                />
                <XCircleIcon v-else class="h-6 w-6 text-red-400" />
              </div>
              <div class="ml-3 w-0 flex-1 pt-0.5">
                <p class="text-sm font-medium text-gray-900">{{ notification.title }}</p>
                <p class="mt-1 text-sm text-gray-500">{{ notification.message }}</p>
              </div>
              <div class="ml-4 flex-shrink-0 flex">
                <button
                  @click="removeNotification(notification.id)"
                  class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <XMarkIcon class="h-5 w-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  ArrowPathIcon,
  PlusIcon,
  CreditCardIcon,
  ClockIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  XCircleIcon,
  XMarkIcon,
  ListBulletIcon,
  CogIcon
} from '@heroicons/vue/24/outline'

interface Notification {
  id: string
  type: 'success' | 'error' | 'warning'
  title: string
  message: string
  duration?: number
}

const route = useRoute()
const loading = ref(false)
const error = ref<string | null>(null)
const notifications = ref<Notification[]>([])

// Navigation tabs
const tabs = ref([
  {
    name: '결제 내역',
    to: '/payments',
    icon: ListBulletIcon,
    count: 0
  },
  {
    name: '처리 대기',
    to: '/payments?status=pending',
    icon: ClockIcon,
    count: 0
  },
  {
    name: '결제 처리',
    to: '/payments/process',
    icon: CreditCardIcon,
    count: 0
  }
])

const isActiveTab = (tabTo: string): boolean => {
  const currentPath = route.path
  const currentQuery = route.query
  
  if (tabTo.includes('?')) {
    const [path, queryString] = tabTo.split('?')
    const queryParams = new URLSearchParams(queryString)
    return currentPath === path && currentQuery.status === queryParams.get('status')
  }
  
  return currentPath === tabTo
}

const refreshData = async () => {
  loading.value = true
  error.value = null
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Update tab counts (mock data)
    tabs.value[0].count = 156 // 전체 결제
    tabs.value[1].count = 23  // 처리 대기
    tabs.value[2].count = 5   // 처리 중
    
    addNotification({
      type: 'success',
      title: '데이터 새로고침 완료',
      message: '최신 결제 정보를 불러왔습니다.'
    })
  } catch (err) {
    error.value = '데이터를 불러오는 중 오류가 발생했습니다.'
    addNotification({
      type: 'error',
      title: '새로고침 실패',
      message: '데이터를 불러오는 중 오류가 발생했습니다.'
    })
  } finally {
    loading.value = false
  }
}

const addNotification = (notification: Omit<Notification, 'id'>) => {
  const id = Date.now().toString()
  const newNotification: Notification = {
    id,
    duration: 5000,
    ...notification
  }
  
  notifications.value.push(newNotification)
  
  // Auto remove after duration
  if (newNotification.duration) {
    setTimeout(() => {
      removeNotification(id)
    }, newNotification.duration)
  }
}

const removeNotification = (id: string) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
  }
}

// Global event listeners for notifications
const handlePaymentSuccess = (event: CustomEvent) => {
  addNotification({
    type: 'success',
    title: '결제 완료',
    message: `결제가 성공적으로 완료되었습니다. (${event.detail.paymentId})`
  })
}

const handlePaymentError = (event: CustomEvent) => {
  addNotification({
    type: 'error',
    title: '결제 실패',
    message: event.detail.message || '결제 처리 중 오류가 발생했습니다.'
  })
}

onMounted(() => {
  refreshData()
  
  // Listen for global payment events
  window.addEventListener('payment-success', handlePaymentSuccess as EventListener)
  window.addEventListener('payment-error', handlePaymentError as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('payment-success', handlePaymentSuccess as EventListener)
  window.removeEventListener('payment-error', handlePaymentError as EventListener)
})

// Expose methods for child components
defineExpose({
  addNotification,
  refreshData
})
</script>