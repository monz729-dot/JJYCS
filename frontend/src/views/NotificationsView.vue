<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">알림</h1>
            <p class="mt-1 text-sm text-gray-600">
              시스템 알림과 업데이트를 확인하세요
            </p>
          </div>
          <div class="flex space-x-3">
            <button
              @click="markAllAsRead"
              :disabled="!hasUnreadNotifications"
              class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <CheckIcon class="h-4 w-4 mr-2" />
              모두 읽음 처리
            </button>
            <button
              @click="showFilterModal = true"
              class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
            >
              <FunnelIcon class="h-4 w-4 mr-2" />
              필터
            </button>
          </div>
        </div>
      </div>

      <!-- Filter Tabs -->
      <div class="mb-6">
        <nav class="flex space-x-8" aria-label="Tabs">
          <button
            v-for="filter in filterTabs"
            :key="filter.id"
            @click="currentFilter = filter.id"
            class="whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm"
            :class="{
              'border-blue-500 text-blue-600': currentFilter === filter.id,
              'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300': currentFilter !== filter.id
            }"
          >
            {{ filter.name }}
            <span
              v-if="filter.count > 0"
              class="ml-2 inline-block py-0.5 px-2 rounded-full text-xs font-medium"
              :class="{
                'bg-blue-100 text-blue-600': currentFilter === filter.id,
                'bg-gray-100 text-gray-600': currentFilter !== filter.id
              }"
            >
              {{ filter.count }}
            </span>
          </button>
        </nav>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>

      <!-- Notifications List -->
      <div v-else-if="filteredNotifications.length > 0" class="space-y-4">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          class="bg-white shadow rounded-lg hover:shadow-md transition-shadow duration-200"
          :class="{ 'border-l-4 border-blue-500': !notification.isRead }"
        >
          <div class="p-6">
            <div class="flex items-start justify-between">
              <div class="flex items-start space-x-4 flex-1">
                <!-- Icon -->
                <div class="flex-shrink-0">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center"
                       :class="getNotificationIconClass(notification.type)">
                    <component :is="getNotificationIcon(notification.type)" class="h-5 w-5" />
                  </div>
                </div>
                
                <!-- Content -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-1">
                    <h3 class="text-sm font-medium text-gray-900">
                      {{ notification.title }}
                    </h3>
                    <span v-if="!notification.isRead" class="inline-block w-2 h-2 bg-blue-500 rounded-full"></span>
                  </div>
                  <p class="text-sm text-gray-600 mb-2">
                    {{ notification.message }}
                  </p>
                  <div class="flex items-center space-x-4 text-xs text-gray-500">
                    <span>{{ formatRelativeTime(notification.createdAt) }}</span>
                    <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                          :class="getNotificationTypeClass(notification.type)">
                      {{ getNotificationTypeLabel(notification.type) }}
                    </span>
                  </div>
                  
                  <!-- Action Button -->
                  <div v-if="notification.actionUrl" class="mt-3">
                    <button
                      @click="handleNotificationAction(notification)"
                      class="text-blue-600 hover:text-blue-500 text-sm font-medium"
                    >
                      {{ notification.actionText || '자세히 보기' }}
                      <ArrowRightIcon class="inline ml-1 h-3 w-3" />
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- Actions -->
              <div class="flex items-center space-x-2 ml-4">
                <button
                  v-if="!notification.isRead"
                  @click="markAsRead(notification.id)"
                  class="text-gray-400 hover:text-gray-600"
                  title="읽음 처리"
                >
                  <CheckIcon class="h-4 w-4" />
                </button>
                <button
                  @click="deleteNotification(notification.id)"
                  class="text-gray-400 hover:text-red-600"
                  title="삭제"
                >
                  <XMarkIcon class="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <BellSlashIcon class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-sm font-medium text-gray-900">알림이 없습니다</h3>
        <p class="mt-1 text-sm text-gray-500">
          {{ currentFilter === 'all' ? '새로운 알림이 없습니다.' : '해당 유형의 알림이 없습니다.' }}
        </p>
      </div>

      <!-- Load More Button -->
      <div v-if="canLoadMore" class="mt-8 text-center">
        <button
          @click="loadMoreNotifications"
          :disabled="loadingMore"
          class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
        >
          <span v-if="!loadingMore">더 보기</span>
          <span v-else class="flex items-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-600 mr-2"></div>
            로딩 중...
          </span>
        </button>
      </div>
    </div>

    <!-- Filter Modal -->
    <div v-if="showFilterModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" @click="showFilterModal = false">
          <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
        </div>

        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="w-full">
                <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">
                  알림 필터
                </h3>
                
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium text-gray-700">알림 유형</label>
                    <div class="mt-2 space-y-2">
                      <label v-for="type in notificationTypes" :key="type.id" class="flex items-center">
                        <input
                          type="checkbox"
                          v-model="selectedTypes"
                          :value="type.id"
                          class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span class="ml-2 text-sm text-gray-700">{{ type.label }}</span>
                      </label>
                    </div>
                  </div>
                  
                  <div>
                    <label class="text-sm font-medium text-gray-700">읽음 상태</label>
                    <select v-model="readStatusFilter" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
                      <option value="all">전체</option>
                      <option value="unread">읽지 않음</option>
                      <option value="read">읽음</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              @click="applyFilters"
              type="button"
              class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              적용
            </button>
            <button
              @click="showFilterModal = false"
              type="button"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  BellIcon,
  BellSlashIcon,
  CheckIcon,
  XMarkIcon,
  FunnelIcon,
  ArrowRightIcon,
  ShoppingBagIcon,
  CreditCardIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  SpeakerWaveIcon,
  TruckIcon
} from '@heroicons/vue/24/outline'

interface Notification {
  id: number
  type: 'order' | 'payment' | 'system' | 'marketing' | 'shipping' | 'warning'
  title: string
  message: string
  isRead: boolean
  createdAt: string
  actionUrl?: string
  actionText?: string
  metadata?: Record<string, any>
}

// Composables
const router = useRouter()

// State
const loading = ref(false)
const loadingMore = ref(false)
const showFilterModal = ref(false)
const currentFilter = ref('all')
const selectedTypes = ref<string[]>([])
const readStatusFilter = ref('all')
const currentPage = ref(1)
const pageSize = 20

// Mock notifications data
const notifications = ref<Notification[]>([
  {
    id: 1,
    type: 'order',
    title: '주문이 접수되었습니다',
    message: '주문번호 YCS2024001이 성공적으로 접수되었습니다. 상품 검수 후 포장 작업이 진행됩니다.',
    isRead: false,
    createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30 minutes ago
    actionUrl: '/orders/1',
    actionText: '주문 상세보기'
  },
  {
    id: 2,
    type: 'shipping',
    title: '배송이 시작되었습니다',
    message: '주문번호 YCS2024002가 창고에서 출고되어 배송을 시작했습니다.',
    isRead: false,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), // 2 hours ago
    actionUrl: '/tracking/YCS2024002',
    actionText: '배송 추적'
  },
  {
    id: 3,
    type: 'payment',
    title: '결제가 완료되었습니다',
    message: '주문번호 YCS2024003의 배송비 ₩45,000이 결제 완료되었습니다.',
    isRead: true,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(), // 5 hours ago
    actionUrl: '/payments/3',
    actionText: '결제 내역'
  },
  {
    id: 4,
    type: 'warning',
    title: 'CBM 초과 경고',
    message: '주문번호 YCS2024004의 총 CBM이 29m³를 초과하여 자동으로 항공 배송으로 변경되었습니다.',
    isRead: false,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 8).toISOString(), // 8 hours ago
    actionUrl: '/orders/4',
    actionText: '주문 확인'
  },
  {
    id: 5,
    type: 'system',
    title: '시스템 업데이트 완료',
    message: '주문 관리 시스템이 v2.1.0으로 업데이트되었습니다. 새로운 기능을 확인해보세요.',
    isRead: true,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 1 day ago
    actionUrl: '/help/updates',
    actionText: '업데이트 내용'
  },
  {
    id: 6,
    type: 'marketing',
    title: '특별 할인 이벤트',
    message: '신규 고객 대상 배송비 20% 할인 이벤트가 진행중입니다. 이번 기회를 놓치지 마세요!',
    isRead: true,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2).toISOString(), // 2 days ago
    actionUrl: '/promotions/shipping-discount',
    actionText: '이벤트 참여'
  },
  {
    id: 7,
    type: 'order',
    title: '상품 검수 완료',
    message: '주문번호 YCS2024005의 모든 상품 검수가 완료되었습니다. 포장 작업을 진행합니다.',
    isRead: true,
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3).toISOString(), // 3 days ago
    actionUrl: '/orders/5',
    actionText: '주문 상태 확인'
  }
])

// Computed
const filterTabs = computed(() => [
  {
    id: 'all',
    name: '전체',
    count: notifications.value.length
  },
  {
    id: 'unread',
    name: '읽지 않음',
    count: notifications.value.filter(n => !n.isRead).length
  },
  {
    id: 'order',
    name: '주문',
    count: notifications.value.filter(n => n.type === 'order').length
  },
  {
    id: 'payment',
    name: '결제',
    count: notifications.value.filter(n => n.type === 'payment').length
  },
  {
    id: 'shipping',
    name: '배송',
    count: notifications.value.filter(n => n.type === 'shipping').length
  },
  {
    id: 'system',
    name: '시스템',
    count: notifications.value.filter(n => n.type === 'system').length
  }
])

const filteredNotifications = computed(() => {
  let filtered = notifications.value

  // Filter by current tab
  if (currentFilter.value === 'unread') {
    filtered = filtered.filter(n => !n.isRead)
  } else if (currentFilter.value !== 'all') {
    filtered = filtered.filter(n => n.type === currentFilter.value)
  }

  // Sort by creation date (newest first)
  return filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
})

const hasUnreadNotifications = computed(() => {
  return notifications.value.some(n => !n.isRead)
})

const canLoadMore = computed(() => {
  return filteredNotifications.value.length >= currentPage.value * pageSize
})

const notificationTypes = [
  { id: 'order', label: '주문' },
  { id: 'payment', label: '결제' },
  { id: 'shipping', label: '배송' },
  { id: 'system', label: '시스템' },
  { id: 'marketing', label: '마케팅' },
  { id: 'warning', label: '경고' }
]

// Methods
const markAsRead = async (notificationId: number) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (notification) {
    notification.isRead = true
    // TODO: API call to mark as read
  }
}

const markAllAsRead = async () => {
  notifications.value.forEach(n => n.isRead = true)
  // TODO: API call to mark all as read
}

const deleteNotification = async (notificationId: number) => {
  const index = notifications.value.findIndex(n => n.id === notificationId)
  if (index !== -1) {
    notifications.value.splice(index, 1)
    // TODO: API call to delete notification
  }
}

const handleNotificationAction = (notification: Notification) => {
  if (notification.actionUrl) {
    // Mark as read when clicking action
    if (!notification.isRead) {
      markAsRead(notification.id)
    }
    
    // Navigate to action URL
    if (notification.actionUrl.startsWith('http')) {
      window.open(notification.actionUrl, '_blank')
    } else {
      router.push(notification.actionUrl)
    }
  }
}

const loadMoreNotifications = async () => {
  loadingMore.value = true
  currentPage.value++
  
  try {
    // TODO: API call to load more notifications
    await new Promise(resolve => setTimeout(resolve, 1000)) // Mock delay
  } catch (error) {
    console.error('Failed to load more notifications:', error)
  } finally {
    loadingMore.value = false
  }
}

const applyFilters = () => {
  showFilterModal.value = false
  // TODO: Apply advanced filters
}

const getNotificationIcon = (type: string) => {
  const icons = {
    order: ShoppingBagIcon,
    payment: CreditCardIcon,
    shipping: TruckIcon,
    system: InformationCircleIcon,
    marketing: SpeakerWaveIcon,
    warning: ExclamationTriangleIcon
  }
  return icons[type] || BellIcon
}

const getNotificationIconClass = (type: string) => {
  const classes = {
    order: 'bg-blue-100 text-blue-600',
    payment: 'bg-green-100 text-green-600',
    shipping: 'bg-purple-100 text-purple-600',
    system: 'bg-gray-100 text-gray-600',
    marketing: 'bg-pink-100 text-pink-600',
    warning: 'bg-yellow-100 text-yellow-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const getNotificationTypeClass = (type: string) => {
  const classes = {
    order: 'bg-blue-100 text-blue-800',
    payment: 'bg-green-100 text-green-800',
    shipping: 'bg-purple-100 text-purple-800',
    system: 'bg-gray-100 text-gray-800',
    marketing: 'bg-pink-100 text-pink-800',
    warning: 'bg-yellow-100 text-yellow-800'
  }
  return classes[type] || 'bg-gray-100 text-gray-800'
}

const getNotificationTypeLabel = (type: string) => {
  const labels = {
    order: '주문',
    payment: '결제',
    shipping: '배송',
    system: '시스템',
    marketing: '마케팅',
    warning: '경고'
  }
  return labels[type] || type
}

const formatRelativeTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000)
  
  if (diffInSeconds < 60) return '방금 전'
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}분 전`
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}시간 전`
  if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)}일 전`
  
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date)
}

const loadNotifications = async () => {
  loading.value = true
  
  try {
    // TODO: API call to load notifications
    await new Promise(resolve => setTimeout(resolve, 1000)) // Mock delay
  } catch (error) {
    console.error('Failed to load notifications:', error)
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
/* Custom scrollbar for modal */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.5);
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.7);
}
</style>