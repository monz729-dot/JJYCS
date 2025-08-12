<template>
  <div class="mobile-order-list">
    <!-- Header -->
    <div class="sticky top-0 bg-white border-b border-gray-200 px-4 py-3 z-10">
      <h1 class="text-lg font-semibold text-gray-900">주문 목록</h1>
    </div>

    <!-- Filters -->
    <div class="bg-white border-b border-gray-200 px-4 py-3">
      <div class="flex space-x-2 overflow-x-auto">
        <button
          v-for="filter in statusFilters"
          :key="filter.value"
          @click="selectedStatus = filter.value"
          class="flex-shrink-0 px-3 py-1 text-sm rounded-full whitespace-nowrap"
          :class="{
            'bg-blue-600 text-white': selectedStatus === filter.value,
            'bg-gray-100 text-gray-700': selectedStatus !== filter.value
          }"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>

    <!-- Order List -->
    <div class="pb-20">
      <div v-if="loading" class="flex justify-center py-8">
        <LoadingSpinner />
      </div>

      <div v-else-if="filteredOrders.length === 0" class="text-center py-12 px-4">
        <div class="text-gray-400 text-4xl mb-4">📦</div>
        <p class="text-gray-500">주문이 없습니다.</p>
        <router-link
          :to="{ name: 'OrderCreate' }"
          class="inline-block mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg"
        >
          첫 주문하기
        </router-link>
      </div>

      <div v-else class="divide-y divide-gray-200">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="bg-white px-4 py-4"
          @click="viewOrder(order.id)"
        >
          <div class="flex justify-between items-start mb-2">
            <div>
              <h3 class="font-medium text-gray-900">{{ order.orderCode }}</h3>
              <p class="text-sm text-gray-600">{{ formatDate(order.createdAt) }}</p>
            </div>
            <span
              class="px-2 py-1 text-xs rounded-full"
              :class="getStatusClass(order.status)"
            >
              {{ getStatusText(order.status) }}
            </span>
          </div>

          <div class="mb-3">
            <p class="text-sm text-gray-800">{{ order.recipientName }}</p>
            <p class="text-xs text-gray-500 line-clamp-2">{{ order.recipientAddress }}</p>
          </div>

          <div class="flex justify-between items-center text-xs text-gray-500">
            <div class="flex items-center space-x-4">
              <span>{{ order.orderType === 'air' ? '✈️ 항공' : '🚢 해상' }}</span>
              <span>{{ order.items.length }}개 상품</span>
              <span>{{ calculateTotalCBM(order) }}m³</span>
            </div>
            <ChevronRightIcon class="w-4 h-4" />
          </div>
        </div>
      </div>
    </div>

    <!-- Floating Action Button -->
    <div class="fixed bottom-6 right-4 z-20">
      <router-link
        :to="{ name: 'OrderCreate' }"
        class="w-14 h-14 bg-blue-600 text-white rounded-full shadow-lg flex items-center justify-center"
      >
        <PlusIcon class="w-6 h-6" />
      </router-link>
    </div>

    <!-- Quick Actions Bottom Sheet -->
    <div
      v-if="showQuickActions"
      class="fixed inset-x-0 bottom-0 bg-white border-t border-gray-200 rounded-t-2xl p-4 z-30"
      @click.self="showQuickActions = false"
    >
      <div class="w-12 h-1 bg-gray-300 rounded-full mx-auto mb-4"></div>
      <h3 class="font-medium text-gray-900 mb-4">빠른 실행</h3>
      
      <div class="grid grid-cols-2 gap-3">
        <button
          @click="quickTrack"
          class="flex flex-col items-center p-4 bg-gray-50 rounded-lg"
        >
          <MagnifyingGlassIcon class="w-6 h-6 text-gray-600 mb-2" />
          <span class="text-sm text-gray-900">빠른 추적</span>
        </button>
        
        <button
          @click="scanQR"
          class="flex flex-col items-center p-4 bg-gray-50 rounded-lg"
        >
          <QrCodeIcon class="w-6 h-6 text-gray-600 mb-2" />
          <span class="text-sm text-gray-900">QR 스캔</span>
        </button>
        
        <button
          @click="contactSupport"
          class="flex flex-col items-center p-4 bg-gray-50 rounded-lg"
        >
          <ChatBubbleLeftIcon class="w-6 h-6 text-gray-600 mb-2" />
          <span class="text-sm text-gray-900">고객 지원</span>
        </button>
        
        <button
          @click="viewNotifications"
          class="flex flex-col items-center p-4 bg-gray-50 rounded-lg"
        >
          <BellIcon class="w-6 h-6 text-gray-600 mb-2" />
          <span class="text-sm text-gray-900">알림</span>
        </button>
      </div>
    </div>

    <!-- Quick Actions Trigger -->
    <div class="fixed bottom-6 left-4 z-20">
      <button
        @click="showQuickActions = !showQuickActions"
        class="w-12 h-12 bg-gray-800 text-white rounded-full shadow-lg flex items-center justify-center"
      >
        <Bars3Icon class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import {
  ChevronRightIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  QrCodeIcon,
  ChatBubbleLeftIcon,
  BellIcon,
  Bars3Icon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const router = useRouter()
const toast = useToast()

// State
const loading = ref(true)
const orders = ref<any[]>([])
const selectedStatus = ref('')
const showQuickActions = ref(false)

const statusFilters = [
  { label: '전체', value: '' },
  { label: '요청', value: 'requested' },
  { label: '확인', value: 'confirmed' },
  { label: '처리중', value: 'in_progress' },
  { label: '배송중', value: 'shipped' },
  { label: '완료', value: 'delivered' }
]

// Computed
const filteredOrders = computed(() => {
  if (!selectedStatus.value) return orders.value
  return orders.value.filter(order => order.status === selectedStatus.value)
})

// Methods
const loadOrders = async () => {
  try {
    loading.value = true
    
    // Mock data
    orders.value = [
      {
        id: '1',
        orderCode: 'ORD-2025-001',
        status: 'shipped',
        orderType: 'sea',
        recipientName: '홍길동',
        recipientAddress: '태국 방콕 수쿰빗 소이 11, 123번지',
        createdAt: '2025-08-10T10:00:00Z',
        items: [{ id: '1', productName: '한국 과자' }],
        boxes: [{ width: 30, height: 20, depth: 15 }]
      },
      {
        id: '2',
        orderCode: 'ORD-2025-002',
        status: 'confirmed',
        orderType: 'air',
        recipientName: '김철수',
        recipientAddress: '태국 치앙마이 님만해민 로드 456',
        createdAt: '2025-08-11T14:30:00Z',
        items: [{ id: '1', productName: '한국 라면' }, { id: '2', productName: '김치' }],
        boxes: [{ width: 40, height: 30, depth: 25 }]
      },
      {
        id: '3',
        orderCode: 'ORD-2025-003',
        status: 'requested',
        orderType: 'sea',
        recipientName: '박영희',
        recipientAddress: '태국 푸켓 파통 비치 로드 789',
        createdAt: '2025-08-12T09:15:00Z',
        items: [{ id: '1', productName: '한국 화장품' }],
        boxes: [{ width: 25, height: 15, depth: 10 }]
      }
    ]
    
  } catch (error) {
    toast.error('주문 목록을 불러오는데 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const viewOrder = (orderId: string) => {
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

const calculateTotalCBM = (order: any) => {
  return order.boxes.reduce((total: number, box: any) => {
    return total + (box.width * box.height * box.depth / 1000000)
  }, 0).toFixed(3)
}

const getStatusClass = (status: string) => {
  const classes = {
    'requested': 'bg-blue-100 text-blue-800',
    'confirmed': 'bg-green-100 text-green-800',
    'in_progress': 'bg-yellow-100 text-yellow-800',
    'shipped': 'bg-purple-100 text-purple-800',
    'delivered': 'bg-green-100 text-green-800',
    'cancelled': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'requested': '요청',
    'confirmed': '확인',
    'in_progress': '처리중',
    'shipped': '배송중',
    'delivered': '배송완료',
    'cancelled': '취소'
  }
  return texts[status as keyof typeof texts] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

// Quick Actions
const quickTrack = () => {
  showQuickActions.value = false
  router.push('/tracking')
}

const scanQR = () => {
  showQuickActions.value = false
  toast.info('QR 스캐너를 실행합니다.')
}

const contactSupport = () => {
  showQuickActions.value = false
  toast.info('고객 지원팀에 연결됩니다.')
}

const viewNotifications = () => {
  showQuickActions.value = false
  router.push({ name: 'Notifications' })
}

// Lifecycle
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.mobile-order-list {
  min-height: 100vh;
  background-color: #f8fafc;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>