<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Mobile Header -->
    <div class="bg-white shadow-sm sticky top-0 z-10">
      <div class="px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div class="h-8 w-12 bg-blue-600 rounded flex items-center justify-center">
              <span class="text-white font-bold text-sm">YCS</span>
            </div>
            <div>
              <h1 class="text-lg font-bold text-gray-900">
                YCS 물류시스템
              </h1>
              <p class="text-xs text-gray-500">
                {{ authStore.user?.name || '사용자' }}님, 안녕하세요
              </p>
            </div>
          </div>
          <div class="flex items-center space-x-2">
            <!-- Notifications -->
            <button
              @click="$router.push('/notifications')"
              class="relative p-2 rounded-full text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <BellIcon class="h-6 w-6" />
              <span v-if="unreadCount > 0" 
                    class="absolute -top-1 -right-1 h-5 w-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center font-medium">
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </button>
            
            <!-- Profile Menu -->
            <button
              @click="showProfileMenu = !showProfileMenu"
              class="p-1 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <div class="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center">
                <span class="text-white text-sm font-medium">
                  {{ (authStore.user?.name || 'U').charAt(0).toUpperCase() }}
                </span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Profile Dropdown (Mobile) -->
    <div v-if="showProfileMenu" 
         class="fixed inset-0 z-50 bg-black bg-opacity-25"
         @click="showProfileMenu = false">
      <div class="absolute top-16 right-4 w-64 bg-white rounded-lg shadow-lg border border-gray-200">
        <div class="p-4 border-b border-gray-200">
          <div class="flex items-center space-x-3">
            <div class="h-10 w-10 rounded-full bg-blue-600 flex items-center justify-center">
              <span class="text-white font-medium">
                {{ (authStore.user?.name || 'U').charAt(0).toUpperCase() }}
              </span>
            </div>
            <div>
              <p class="font-medium text-gray-900">{{ authStore.user?.name || '사용자' }}</p>
              <p class="text-sm text-gray-500">{{ authStore.user?.email }}</p>
            </div>
          </div>
        </div>
        <div class="py-2">
          <router-link to="/profile" class="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            <UserIcon class="h-4 w-4 mr-3" />
            프로필 설정
          </router-link>
          <router-link to="/settings" class="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            <CogIcon class="h-4 w-4 mr-3" />
            설정
          </router-link>
          <button @click="logout" class="w-full flex items-center px-4 py-2 text-sm text-red-700 hover:bg-red-50">
            <ArrowRightOnRectangleIcon class="h-4 w-4 mr-3" />
            로그아웃
          </button>
        </div>
      </div>
    </div>

    <!-- Account Status Banner -->
    <div v-if="authStore.isPending" class="mx-4 mt-4 rounded-lg bg-yellow-50 p-4 border border-yellow-200">
      <div class="flex items-start">
        <ClockIcon class="h-5 w-5 text-yellow-400 mt-0.5" />
        <div class="ml-3 flex-1">
          <h3 class="text-sm font-medium text-yellow-800">계정 승인 대기 중</h3>
          <p class="mt-1 text-xs text-yellow-700">
            기업/파트너 계정 승인까지 1-2일 소요됩니다
          </p>
          <button
            @click="$router.push('/profile')"
            class="mt-2 text-xs font-medium text-yellow-800 hover:underline"
          >
            프로필 완성하기
          </button>
        </div>
      </div>
    </div>

    <!-- Member Code Warning -->
    <div v-if="!authStore.user?.memberCode && authStore.isActive" 
         class="mx-4 mt-4 rounded-lg bg-orange-50 p-4 border border-orange-200">
      <div class="flex items-start">
        <ExclamationTriangleIcon class="h-5 w-5 text-orange-400 mt-0.5" />
        <div class="ml-3 flex-1">
          <h3 class="text-sm font-medium text-orange-800">회원코드 미등록</h3>
          <p class="mt-1 text-xs text-orange-700">
            발송 지연을 방지하려면 회원코드를 등록해주세요
          </p>
          <button
            @click="$router.push('/profile')"
            class="mt-2 text-xs font-medium text-orange-800 hover:underline"
          >
            회원코드 등록하기
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="px-4 py-6 space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-2 gap-4">
        <div class="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-2xl font-bold text-gray-900">{{ stats.totalOrders }}</p>
              <p class="text-sm text-gray-600">총 주문</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <DocumentTextIcon class="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div v-if="stats.ordersTrend !== 0" class="mt-2 flex items-center text-xs">
            <ArrowUpIcon v-if="stats.ordersTrend > 0" class="h-3 w-3 text-green-500 mr-1" />
            <ArrowDownIcon v-else class="h-3 w-3 text-red-500 mr-1" />
            <span :class="stats.ordersTrend > 0 ? 'text-green-600' : 'text-red-600'">
              {{ Math.abs(stats.ordersTrend) }}%
            </span>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-2xl font-bold text-gray-900">{{ stats.pendingOrders }}</p>
              <p class="text-sm text-gray-600">대기 중</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <ClockIcon class="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <router-link 
            :to="{ name: 'OrderList', query: { status: 'pending' } }"
            class="mt-2 text-xs text-yellow-600 hover:underline"
          >
            확인하기
          </router-link>
        </div>

        <div class="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-2xl font-bold text-gray-900">{{ stats.inTransitOrders }}</p>
              <p class="text-sm text-gray-600">배송 중</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <TruckIcon class="h-6 w-6 text-purple-600" />
            </div>
          </div>
          <router-link 
            to="/tracking"
            class="mt-2 text-xs text-purple-600 hover:underline"
          >
            추적하기
          </router-link>
        </div>

        <div class="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-lg font-bold text-gray-900">{{ formatCurrency(stats.totalSpent) }}</p>
              <p class="text-sm text-gray-600">총 지출</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <CurrencyDollarIcon class="h-6 w-6 text-green-600" />
            </div>
          </div>
          <router-link 
            to="/payments"
            class="mt-2 text-xs text-green-600 hover:underline"
          >
            내역보기
          </router-link>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="p-4 border-b border-gray-100">
          <h2 class="text-lg font-semibold text-gray-900">빠른 작업</h2>
        </div>
        <div class="p-4 grid grid-cols-2 gap-4">
          <button
            v-if="authStore.canCreateOrder"
            @click="$router.push('/orders/create')"
            class="flex flex-col items-center justify-center p-6 rounded-xl bg-blue-50 border-2 border-blue-100 hover:bg-blue-100 hover:border-blue-200 transition-colors"
          >
            <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mb-3">
              <PlusIcon class="h-6 w-6 text-white" />
            </div>
            <span class="text-sm font-medium text-blue-900">새 주문</span>
          </button>

          <button
            @click="startQRScan"
            class="flex flex-col items-center justify-center p-6 rounded-xl bg-green-50 border-2 border-green-100 hover:bg-green-100 hover:border-green-200 transition-colors"
          >
            <div class="w-12 h-12 bg-green-600 rounded-xl flex items-center justify-center mb-3">
              <QrCodeIcon class="h-6 w-6 text-white" />
            </div>
            <span class="text-sm font-medium text-green-900">QR 스캔</span>
          </button>

          <button
            @click="$router.push('/tracking')"
            class="flex flex-col items-center justify-center p-6 rounded-xl bg-purple-50 border-2 border-purple-100 hover:bg-purple-100 hover:border-purple-200 transition-colors"
          >
            <div class="w-12 h-12 bg-purple-600 rounded-xl flex items-center justify-center mb-3">
              <MagnifyingGlassIcon class="h-6 w-6 text-white" />
            </div>
            <span class="text-sm font-medium text-purple-900">추적</span>
          </button>

          <button
            @click="$router.push('/orders')"
            class="flex flex-col items-center justify-center p-6 rounded-xl bg-orange-50 border-2 border-orange-100 hover:bg-orange-100 hover:border-orange-200 transition-colors"
          >
            <div class="w-12 h-12 bg-orange-600 rounded-xl flex items-center justify-center mb-3">
              <DocumentTextIcon class="h-6 w-6 text-white" />
            </div>
            <span class="text-sm font-medium text-orange-900">주문목록</span>
          </button>
        </div>
      </div>

      <!-- Recent Orders -->
      <div v-if="recentOrders.length > 0" class="bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="p-4 border-b border-gray-100 flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900">최근 주문</h2>
          <router-link to="/orders" class="text-sm text-blue-600 hover:text-blue-500">
            전체보기
          </router-link>
        </div>
        <div class="divide-y divide-gray-100">
          <div
            v-for="order in recentOrders.slice(0, 3)"
            :key="order.id"
            @click="$router.push(`/orders/${order.id}`)"
            class="p-4 hover:bg-gray-50 transition-colors"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">
                  {{ order.orderCode }}
                </p>
                <p class="text-xs text-gray-600 mt-1">
                  {{ order.recipientName }} · {{ order.recipientCountry }}
                </p>
                <div class="flex items-center mt-2">
                  <span
                    class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                    :class="getStatusBadgeClass(order.status)"
                  >
                    {{ getStatusLabel(order.status) }}
                  </span>
                  <span class="ml-2 text-xs text-gray-500">
                    {{ formatRelativeTime(order.createdAt) }}
                  </span>
                </div>
              </div>
              <div class="text-right ml-4">
                <p class="text-sm font-medium text-gray-900">
                  {{ formatCurrency(order.totalAmount, order.currency) }}
                </p>
                <ChevronRightIcon class="h-4 w-4 text-gray-400 mt-1" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activities -->
      <div v-if="recentActivities.length > 0" class="bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="p-4 border-b border-gray-100">
          <h2 class="text-lg font-semibold text-gray-900">최근 활동</h2>
        </div>
        <div class="divide-y divide-gray-100">
          <div
            v-for="activity in recentActivities.slice(0, 3)"
            :key="activity.id"
            class="p-4"
          >
            <div class="flex items-start space-x-3">
              <div class="flex-shrink-0">
                <div class="w-8 h-8 rounded-full flex items-center justify-center"
                     :class="getActivityIconClass(activity.type)">
                  <component :is="getActivityIcon(activity.type)" class="h-4 w-4" />
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900">{{ activity.message }}</p>
                <p class="text-xs text-gray-500 mt-1">
                  {{ formatRelativeTime(activity.createdAt) }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Warehouse Stats (for warehouse users) -->
      <div v-if="authStore.hasRole(['WAREHOUSE', 'ADMIN'])" 
           class="bg-white rounded-xl shadow-sm border border-gray-100">
        <div class="p-4 border-b border-gray-100">
          <h2 class="text-lg font-semibold text-gray-900">창고 현황</h2>
        </div>
        <div class="p-4 grid grid-cols-2 gap-4">
          <div class="text-center">
            <p class="text-xl font-bold text-blue-600">{{ warehouseStats.totalBoxes }}</p>
            <p class="text-xs text-gray-600 mt-1">총 박스</p>
          </div>
          <div class="text-center">
            <p class="text-xl font-bold text-yellow-600">{{ warehouseStats.pendingInbound }}</p>
            <p class="text-xs text-gray-600 mt-1">입고 대기</p>
          </div>
          <div class="text-center">
            <p class="text-xl font-bold text-green-600">{{ warehouseStats.readyForOutbound }}</p>
            <p class="text-xs text-gray-600 mt-1">출고 준비</p>
          </div>
          <div class="text-center">
            <p class="text-xl font-bold text-red-600">{{ warehouseStats.onHold }}</p>
            <p class="text-xs text-gray-600 mt-1">보류</p>
          </div>
        </div>
      </div>
    </div>

    <!-- QR Scanner Modal -->
    <div v-if="showQRScanner" class="fixed inset-0 z-50 bg-black">
      <div class="flex flex-col h-full">
        <!-- QR Scanner Header -->
        <div class="bg-black text-white p-4 flex items-center justify-between">
          <h2 class="text-lg font-medium">QR 코드 스캔</h2>
          <button @click="stopQRScan" class="text-white hover:text-gray-300">
            <XMarkIcon class="h-6 w-6" />
          </button>
        </div>
        
        <!-- Camera View -->
        <div class="flex-1 relative">
          <video ref="videoElement" class="w-full h-full object-cover" autoplay playsinline></video>
          
          <!-- Scanning Overlay -->
          <div class="absolute inset-0 flex items-center justify-center">
            <div class="w-64 h-64 border-2 border-white border-dashed rounded-lg"></div>
          </div>
          
          <!-- Instructions -->
          <div class="absolute bottom-8 left-0 right-0 text-center">
            <p class="text-white text-sm px-4">
              QR 코드를 카메라 중앙에 맞춰주세요
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Navigation (if needed) -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 px-4 py-2 safe-area-pb">
      <div class="flex justify-around">
        <button
          @click="$router.push('/')"
          class="flex flex-col items-center py-2"
          :class="$route.path === '/' ? 'text-blue-600' : 'text-gray-400'"
        >
          <HomeIcon class="h-5 w-5" />
          <span class="text-xs mt-1">홈</span>
        </button>
        
        <button
          @click="$router.push('/orders')"
          class="flex flex-col items-center py-2"
          :class="$route.path.startsWith('/orders') ? 'text-blue-600' : 'text-gray-400'"
        >
          <DocumentTextIcon class="h-5 w-5" />
          <span class="text-xs mt-1">주문</span>
        </button>
        
        <button
          @click="$router.push('/tracking')"
          class="flex flex-col items-center py-2"
          :class="$route.path.startsWith('/tracking') ? 'text-blue-600' : 'text-gray-400'"
        >
          <TruckIcon class="h-5 w-5" />
          <span class="text-xs mt-1">추적</span>
        </button>
        
        <button
          @click="$router.push('/profile')"
          class="flex flex-col items-center py-2"
          :class="$route.path.startsWith('/profile') ? 'text-blue-600' : 'text-gray-400'"
        >
          <UserIcon class="h-5 w-5" />
          <span class="text-xs mt-1">내정보</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useOrdersStore } from '@/stores/orders'
import {
  BellIcon,
  UserIcon,
  CogIcon,
  ArrowRightOnRectangleIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon,
  TruckIcon,
  CurrencyDollarIcon,
  PlusIcon,
  QrCodeIcon,
  MagnifyingGlassIcon,
  ChevronRightIcon,
  XMarkIcon,
  HomeIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  CheckCircleIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

interface Activity {
  id: number
  type: 'order' | 'payment' | 'shipping' | 'system'
  message: string
  createdAt: string
}

// Composables
const router = useRouter()
const authStore = useAuthStore()
const ordersStore = useOrdersStore()

// State
const loading = ref(false)
const showProfileMenu = ref(false)
const showQRScanner = ref(false)
const videoElement = ref<HTMLVideoElement | null>(null)
const unreadCount = ref(5) // Mock notification count

// Mock activities data
const recentActivities = ref<Activity[]>([
  {
    id: 1,
    type: 'order',
    message: '주문 YCS2024001이 접수되었습니다',
    createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString()
  },
  {
    id: 2,
    type: 'shipping',
    message: '주문 YCS2024002가 배송을 시작했습니다',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
  },
  {
    id: 3,
    type: 'payment',
    message: '배송비 ₩45,000 결제가 완료되었습니다',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString()
  }
])

// Computed
const recentOrders = computed(() => {
  return ordersStore.orders.slice(0, 5)
})

const stats = computed(() => {
  const orders = ordersStore.orders
  return {
    totalOrders: orders.length,
    pendingOrders: orders.filter(o => o.status === 'pending').length,
    inTransitOrders: orders.filter(o => o.status === 'in_transit').length,
    totalSpent: orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0),
    ordersTrend: 12 // Mock trend data
  }
})

const warehouseStats = computed(() => ({
  totalBoxes: 156,
  pendingInbound: 23,
  readyForOutbound: 45,
  onHold: 8
}))

// Methods
const logout = async () => {
  await authStore.logout()
  router.push('/login')
}

const startQRScan = async () => {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    alert('카메라를 사용할 수 없습니다. 브라우저가 카메라 접근을 지원하는지 확인해주세요.')
    return
  }

  try {
    showQRScanner.value = true
    await nextTick()
    
    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'environment', // Use back camera
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    })
    
    if (videoElement.value) {
      videoElement.value.srcObject = stream
    }
    
    // TODO: Implement QR code detection
    // You would typically use a library like @zxing/library or jsqr here
    
  } catch (error) {
    console.error('카메라 접근 실패:', error)
    alert('카메라에 접근할 수 없습니다. 권한을 확인해주세요.')
    showQRScanner.value = false
  }
}

const stopQRScan = () => {
  if (videoElement.value && videoElement.value.srcObject) {
    const tracks = (videoElement.value.srcObject as MediaStream).getTracks()
    tracks.forEach(track => track.stop())
  }
  showQRScanner.value = false
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    pending: 'bg-yellow-100 text-yellow-800',
    approved: 'bg-blue-100 text-blue-800',
    in_transit: 'bg-purple-100 text-purple-800',
    delivered: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const getStatusLabel = (status: string) => {
  const labels = {
    pending: '대기',
    approved: '승인',
    in_transit: '배송중',
    delivered: '완료',
    cancelled: '취소'
  }
  return labels[status] || status
}

const getActivityIcon = (type: string) => {
  const icons = {
    order: DocumentTextIcon,
    payment: CurrencyDollarIcon,
    shipping: TruckIcon,
    system: InformationCircleIcon
  }
  return icons[type] || CheckCircleIcon
}

const getActivityIconClass = (type: string) => {
  const classes = {
    order: 'bg-blue-100 text-blue-600',
    payment: 'bg-green-100 text-green-600',
    shipping: 'bg-purple-100 text-purple-600',
    system: 'bg-gray-100 text-gray-600'
  }
  return classes[type] || 'bg-gray-100 text-gray-600'
}

const formatCurrency = (amount: number, currency: string = 'THB') => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
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
    month: 'short',
    day: 'numeric'
  }).format(date)
}

const loadDashboardData = async () => {
  loading.value = true
  
  try {
    if (authStore.isActive) {
      await ordersStore.fetchOrders({ page: 0, size: 5 })
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

// Close profile menu when clicking outside
const handleClickOutside = (event: Event) => {
  const target = event.target as Element
  if (!target.closest('.profile-menu-container')) {
    showProfileMenu.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadDashboardData()
  document.addEventListener('click', handleClickOutside)
})

// Add PWA meta tags for mobile
onMounted(() => {
  // Add viewport meta tag for mobile
  const viewportMeta = document.querySelector('meta[name="viewport"]')
  if (!viewportMeta) {
    const meta = document.createElement('meta')
    meta.name = 'viewport'
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover'
    document.head.appendChild(meta)
  }
  
  // Set theme color
  const themeColorMeta = document.querySelector('meta[name="theme-color"]')
  if (!themeColorMeta) {
    const meta = document.createElement('meta')
    meta.name = 'theme-color'
    meta.content = '#2563eb'
    document.head.appendChild(meta)
  }
})
</script>

<style scoped>
/* PWA safe area support */
.safe-area-pb {
  padding-bottom: env(safe-area-inset-bottom);
}

/* Mobile-first responsive design */
@media (max-width: 640px) {
  /* Ensure touch targets are at least 44px */
  button {
    min-height: 44px;
    min-width: 44px;
  }
  
  /* Improve readability on mobile */
  .text-xs {
    font-size: 0.75rem;
    line-height: 1rem;
  }
  
  /* Better spacing for mobile */
  .grid-cols-2 > * {
    min-height: 120px;
  }
}

/* Custom mobile animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out;
}

/* QR Scanner specific styles */
video {
  transform: scaleX(-1); /* Mirror the video for better UX */
}

/* Bottom navigation safe area */
.fixed.bottom-0 {
  padding-bottom: max(env(safe-area-inset-bottom), 8px);
}
</style>