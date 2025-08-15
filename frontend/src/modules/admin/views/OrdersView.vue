<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">주문 관리</h1>
        <p class="text-sm text-gray-600 mt-1">시스템의 모든 주문을 관리하고 모니터링합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex space-x-3">
        <button @click="exportOrders" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <ArrowDownTrayIcon class="h-4 w-4 mr-2" />
          내보내기
        </button>
        <button @click="refreshOrders" 
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
          <ArrowPathIcon class="h-4 w-4 mr-2" />
          새로고침
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="stat in orderStats" :key="stat.title" class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <component :is="stat.icon" class="h-8 w-8" :class="stat.iconColor" />
          </div>
          <div class="ml-5">
            <p class="text-sm font-medium text-gray-500">{{ stat.title }}</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stat.value }}</p>
            <p class="text-xs text-gray-400">{{ stat.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-6 gap-4">
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-2">검색</label>
          <div class="relative">
            <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input v-model="filters.search" 
                   type="text" 
                   placeholder="주문번호, 고객명 검색..."
                   class="pl-10 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">주문 유형</label>
          <select v-model="filters.orderType" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="air">항공</option>
            <option value="sea">해상</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">상태</label>
          <select v-model="filters.status" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="pending">주문 접수</option>
            <option value="processing">처리중</option>
            <option value="warehouse">창고 도착</option>
            <option value="shipping">배송중</option>
            <option value="delivered">배송 완료</option>
            <option value="cancelled">취소</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">기간</label>
          <select v-model="filters.period" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="today">오늘</option>
            <option value="week">이번 주</option>
            <option value="month">이번 달</option>
            <option value="quarter">이번 분기</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="resetFilters" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
            초기화
          </button>
        </div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h3 class="text-lg font-medium text-gray-900">
          주문 목록 ({{ filteredOrders.length }}건)
        </h3>
        <div v-if="selectedOrders.length > 0" class="flex items-center space-x-2">
          <span class="text-sm text-gray-600">{{ selectedOrders.length }}건 선택됨</span>
          <button @click="bulkAction('process')" 
                  class="px-3 py-1 text-xs bg-blue-600 text-white rounded-md hover:bg-blue-700">
            일괄 처리
          </button>
          <button @click="bulkAction('cancel')" 
                  class="px-3 py-1 text-xs bg-red-600 text-white rounded-md hover:bg-red-700">
            일괄 취소
          </button>
          <button @click="bulkExport" 
                  class="px-3 py-1 text-xs bg-green-600 text-white rounded-md hover:bg-green-700">
            선택 내보내기
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left">
                <input type="checkbox" 
                       :checked="allSelected" 
                       @change="toggleSelectAll"
                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                주문정보
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                고객
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                유형/CBM
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                금액
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                주문일
              </th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in paginatedOrders" :key="order.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <input type="checkbox" 
                       :value="order.id" 
                       v-model="selectedOrders"
                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div>
                  <div class="text-sm font-medium text-gray-900">#{{ order.orderNumber }}</div>
                  <div class="text-sm text-gray-500">{{ order.itemCount }}개 품목</div>
                  <div v-if="order.memberCode" class="text-xs text-gray-400">회원: {{ order.memberCode }}</div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ order.customerName }}</div>
                  <div class="text-sm text-gray-500">{{ order.customerEmail }}</div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div>
                  <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                        :class="order.orderType === 'air' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'">
                    {{ order.orderType === 'air' ? '항공' : '해상' }}
                  </span>
                  <div class="text-xs text-gray-500 mt-1">
                    {{ order.totalCbm }} m³
                    <span v-if="order.totalCbm > 29" class="text-red-500 font-medium">⚠ 초과</span>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                      :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
                <div v-if="order.requiresExtraRecipient" class="text-xs text-orange-500 mt-1">
                  ⚠ 추가정보 필요
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ order.totalAmount }}</div>
                <div class="text-xs text-gray-500">예상: {{ order.estimatedCost }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(order.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end space-x-2">
                  <button @click="viewOrder(order)" 
                          class="text-blue-600 hover:text-blue-900">
                    상세
                  </button>
                  <button @click="editOrder(order)" 
                          class="text-indigo-600 hover:text-indigo-900">
                    편집
                  </button>
                  <div class="relative">
                    <button @click="toggleActionMenu(order.id)" 
                            class="text-gray-400 hover:text-gray-600">
                      <EllipsisVerticalIcon class="h-5 w-5" />
                    </button>
                    <div v-if="showActionMenu === order.id" 
                         class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10 border">
                      <div class="py-1">
                        <button @click="processOrder(order)" 
                                class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          주문 처리
                        </button>
                        <button @click="generateLabel(order)" 
                                class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          라벨 생성
                        </button>
                        <button @click="sendNotification(order)" 
                                class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                          고객 알림
                        </button>
                        <hr class="my-1">
                        <button @click="cancelOrder(order)" 
                                class="block w-full text-left px-4 py-2 text-sm text-red-700 hover:bg-red-50">
                          주문 취소
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="bg-white px-4 py-3 border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button @click="previousPage" 
                  :disabled="currentPage === 1"
                  class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">
            이전
          </button>
          <button @click="nextPage" 
                  :disabled="currentPage === totalPages"
                  class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">
            다음
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              <span class="font-medium">{{ (currentPage - 1) * itemsPerPage + 1 }}</span>
              -
              <span class="font-medium">{{ Math.min(currentPage * itemsPerPage, filteredOrders.length) }}</span>
              / 총 
              <span class="font-medium">{{ filteredOrders.length }}</span>
              건
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
              <button @click="previousPage" 
                      :disabled="currentPage === 1"
                      class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                <ChevronLeftIcon class="h-5 w-5" />
              </button>
              <button v-for="page in visiblePages" 
                      :key="page"
                      @click="goToPage(page)"
                      class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                      :class="currentPage === page ? 
                        'z-10 bg-blue-50 border-blue-500 text-blue-600' : 
                        'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'">
                {{ page }}
              </button>
              <button @click="nextPage" 
                      :disabled="currentPage === totalPages"
                      class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                <ChevronRightIcon class="h-5 w-5" />
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  MagnifyingGlassIcon,
  ArrowDownTrayIcon,
  ArrowPathIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  EllipsisVerticalIcon,
  ShoppingBagIcon,
  ClockIcon,
  TruckIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'

interface Order {
  id: number
  orderNumber: string
  customerName: string
  customerEmail: string
  orderType: 'air' | 'sea'
  status: string
  totalAmount: string
  estimatedCost: string
  itemCount: number
  totalCbm: number
  requiresExtraRecipient: boolean
  memberCode?: string
  createdAt: string
}

const router = useRouter()

// State
const loading = ref(false)
const selectedOrders = ref<number[]>([])
const currentPage = ref(1)
const itemsPerPage = ref(10)
const showActionMenu = ref<number | null>(null)

// Filters
const filters = ref({
  search: '',
  orderType: '',
  status: '',
  period: ''
})

// Stats
const orderStats = ref([
  {
    title: '총 주문',
    value: '1,247',
    description: '이번 달',
    icon: ShoppingBagIcon,
    iconColor: 'text-blue-600'
  },
  {
    title: '대기 주문',
    value: '89',
    description: '처리 필요',
    icon: ClockIcon,
    iconColor: 'text-yellow-600'
  },
  {
    title: '배송 중',
    value: '156',
    description: '진행 중',
    icon: TruckIcon,
    iconColor: 'text-purple-600'
  },
  {
    title: '완료',
    value: '1,002',
    description: '배송 완료',
    icon: CheckCircleIcon,
    iconColor: 'text-green-600'
  }
])

// Mock data
const orders = ref<Order[]>([
  {
    id: 1,
    orderNumber: 'YCS240001',
    customerName: '김철수',
    customerEmail: 'kim@example.com',
    orderType: 'air',
    status: 'processing',
    totalAmount: '₩485,000',
    estimatedCost: '₩520,000',
    itemCount: 3,
    totalCbm: 15.5,
    requiresExtraRecipient: false,
    memberCode: 'YCS001',
    createdAt: '2024-01-20'
  },
  {
    id: 2,
    orderNumber: 'YCS240002',
    customerName: '박영희',
    customerEmail: 'park@company.com',
    orderType: 'sea',
    status: 'warehouse',
    totalAmount: '₩1,250,000',
    estimatedCost: '₩1,350,000',
    itemCount: 8,
    totalCbm: 45.2,
    requiresExtraRecipient: true,
    memberCode: 'YCS002',
    createdAt: '2024-01-18'
  },
  {
    id: 3,
    orderNumber: 'YCS240003',
    customerName: '이민준',
    customerEmail: 'lee@partner.com',
    orderType: 'air',
    status: 'shipping',
    totalAmount: '₩750,000',
    estimatedCost: '₩780,000',
    itemCount: 5,
    totalCbm: 32.8,
    requiresExtraRecipient: false,
    createdAt: '2024-01-15'
  },
  {
    id: 4,
    orderNumber: 'YCS240004',
    customerName: '최서연',
    customerEmail: 'choi@example.com',
    orderType: 'sea',
    status: 'pending',
    totalAmount: '₩125,000',
    estimatedCost: '₩145,000',
    itemCount: 1,
    totalCbm: 8.2,
    requiresExtraRecipient: false,
    createdAt: '2024-01-22'
  },
  {
    id: 5,
    orderNumber: 'YCS240005',
    customerName: '정도현',
    customerEmail: 'jung@example.com',
    orderType: 'air',
    status: 'delivered',
    totalAmount: '₩950,000',
    estimatedCost: '₩980,000',
    itemCount: 6,
    totalCbm: 28.9,
    requiresExtraRecipient: true,
    createdAt: '2024-01-10'
  }
])

// Computed
const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const matchesSearch = !filters.value.search || 
      order.orderNumber.toLowerCase().includes(filters.value.search.toLowerCase()) ||
      order.customerName.toLowerCase().includes(filters.value.search.toLowerCase()) ||
      order.customerEmail.toLowerCase().includes(filters.value.search.toLowerCase())
    
    const matchesOrderType = !filters.value.orderType || order.orderType === filters.value.orderType
    const matchesStatus = !filters.value.status || order.status === filters.value.status
    
    return matchesSearch && matchesOrderType && matchesStatus
  })
})

const totalPages = computed(() => Math.ceil(filteredOrders.value.length / itemsPerPage.value))

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredOrders.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  for (let i = Math.max(1, current - 2); i <= Math.min(total, current + 2); i++) {
    pages.push(i)
  }
  
  return pages
})

const allSelected = computed(() => {
  return paginatedOrders.value.length > 0 && 
    paginatedOrders.value.every(order => selectedOrders.value.includes(order.id))
})

// Methods
const resetFilters = () => {
  filters.value = {
    search: '',
    orderType: '',
    status: '',
    period: ''
  }
  currentPage.value = 1
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedOrders.value = []
  } else {
    selectedOrders.value = paginatedOrders.value.map(order => order.id)
  }
}

const bulkAction = async (action: string) => {
  if (!selectedOrders.value.length) return
  
  const actionText = action === 'process' ? '처리' : '취소'
  if (!confirm(`선택한 ${selectedOrders.value.length}건의 주문을 ${actionText}하시겠습니까?`)) {
    return
  }
  
  loading.value = true
  try {
    // Mock bulk action
    selectedOrders.value.forEach(orderId => {
      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = action === 'process' ? 'processing' : 'cancelled'
      }
    })
    selectedOrders.value = []
  } catch (error) {
    console.error('Error performing bulk action:', error)
  } finally {
    loading.value = false
  }
}

const bulkExport = () => {
  
}

const exportOrders = () => {
  
}

const refreshOrders = () => {
  
}

const toggleActionMenu = (orderId: number) => {
  showActionMenu.value = showActionMenu.value === orderId ? null : orderId
}

const viewOrder = (order: Order) => {
  router.push(`/orders/${order.id}`)
}

const editOrder = (order: Order) => {
  
}

const processOrder = (order: Order) => {
  order.status = 'processing'
  showActionMenu.value = null
}

const generateLabel = (order: Order) => {
  
  showActionMenu.value = null
}

const sendNotification = (order: Order) => {
  
  showActionMenu.value = null
}

const cancelOrder = (order: Order) => {
  if (confirm(`주문 #${order.orderNumber}을(를) 취소하시겠습니까?`)) {
    order.status = 'cancelled'
  }
  showActionMenu.value = null
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR')
}

const getStatusClass = (status: string) => {
  const classes = {
    'pending': 'bg-yellow-100 text-yellow-800',
    'processing': 'bg-blue-100 text-blue-800',
    'warehouse': 'bg-indigo-100 text-indigo-800',
    'shipping': 'bg-purple-100 text-purple-800',
    'delivered': 'bg-green-100 text-green-800',
    'cancelled': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'pending': '주문 접수',
    'processing': '처리중',
    'warehouse': '창고 도착',
    'shipping': '배송중',
    'delivered': '배송 완료',
    'cancelled': '취소'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}

// Close action menu when clicking outside
document.addEventListener('click', (e) => {
  const target = e.target as HTMLElement
  if (!target.closest('.relative')) {
    showActionMenu.value = null
  }
})
</script>