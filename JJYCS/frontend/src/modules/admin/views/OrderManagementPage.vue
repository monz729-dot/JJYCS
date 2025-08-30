<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">주문 관리</h1>
          <p class="text-gray-600">모든 주문을 조회하고 상태를 업데이트할 수 있습니다</p>
        </div>
        <div class="flex items-center space-x-4">
          <button
            @click="refreshOrders"
            :disabled="loading"
            class="btn-secondary"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            {{ loading ? '새로고침 중...' : '새로고침' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="label">주문번호</label>
          <input
            v-model="searchForm.orderNumber"
            type="text"
            class="input-field"
            placeholder="주문번호 검색"
          />
        </div>
        <div>
          <label class="label">수취인</label>
          <input
            v-model="searchForm.recipientName"
            type="text"
            class="input-field"
            placeholder="수취인 이름"
          />
        </div>
        <div>
          <label class="label">상태</label>
          <select v-model="searchForm.status" class="input-field">
            <option value="">모든 상태</option>
            <option value="PENDING">대기</option>
            <option value="PROCESSING">처리중</option>
            <option value="SHIPPED">발송완료</option>
            <option value="IN_TRANSIT">운송중</option>
            <option value="DELIVERED">배송완료</option>
            <option value="CANCELLED">취소</option>
          </select>
        </div>
        <div class="flex items-end space-x-2">
          <button
            @click="searchOrders"
            :disabled="searching"
            class="btn-primary flex-1"
          >
            {{ searching ? '검색 중...' : '검색' }}
          </button>
          <button
            @click="clearSearch"
            class="btn-secondary"
          >
            초기화
          </button>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="text-2xl font-bold text-gray-900">{{ stats.total }}</div>
        <div class="text-sm text-gray-600">전체 주문</div>
      </div>
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="text-2xl font-bold text-yellow-600">{{ stats.pending }}</div>
        <div class="text-sm text-gray-600">대기</div>
      </div>
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="text-2xl font-bold text-blue-600">{{ stats.processing }}</div>
        <div class="text-sm text-gray-600">처리중</div>
      </div>
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="text-2xl font-bold text-purple-600">{{ stats.shipped }}</div>
        <div class="text-sm text-gray-600">발송완료</div>
      </div>
      <div class="bg-white rounded-lg shadow-sm p-4">
        <div class="text-2xl font-bold text-green-600">{{ stats.delivered }}</div>
        <div class="text-sm text-gray-600">배송완료</div>
      </div>
    </div>

    <!-- Orders Table -->
    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">주문 목록</h3>
      </div>
      
      <div v-if="loading" class="p-6">
        <div class="animate-pulse space-y-4">
          <div v-for="i in 5" :key="i" class="flex space-x-4">
            <div class="h-4 bg-gray-200 rounded w-24"></div>
            <div class="h-4 bg-gray-200 rounded w-32"></div>
            <div class="h-4 bg-gray-200 rounded w-48"></div>
            <div class="h-4 bg-gray-200 rounded w-20"></div>
            <div class="h-4 bg-gray-200 rounded w-24"></div>
          </div>
        </div>
      </div>

      <div v-else-if="orders.length === 0" class="p-6 text-center text-gray-500">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
        </svg>
        <p>주문이 없습니다</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                주문번호
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                수취인
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                배송지
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                주문일
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in orders" :key="order.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ order.orderNumber }}</div>
                <div class="text-sm text-gray-500">{{ order.trackingNumber || 'N/A' }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">{{ order.recipientName }}</div>
                <div class="text-sm text-gray-500">{{ order.recipientPhone }}</div>
              </td>
              <td class="px-6 py-4">
                <div class="text-sm text-gray-900 max-w-xs truncate">{{ order.recipientAddress }}</div>
                <div class="text-sm text-gray-500">{{ order.recipientPostalCode }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusClass(order.status)" class="status-badge">
                  {{ getStatusLabel(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(order.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button
                  @click="viewOrder(order)"
                  class="text-primary-600 hover:text-primary-900"
                >
                  상세
                </button>
                <button
                  @click="showStatusModal(order)"
                  class="text-indigo-600 hover:text-indigo-900"
                >
                  상태변경
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="bg-white px-4 py-3 border-t border-gray-200 sm:px-6">
        <div class="flex items-center justify-between">
          <div class="flex-1 flex justify-between sm:hidden">
            <button
              @click="previousPage"
              :disabled="currentPage === 1"
              class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
            >
              이전
            </button>
            <button
              @click="nextPage"
              :disabled="currentPage >= totalPages"
              class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
            >
              다음
            </button>
          </div>
          <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p class="text-sm text-gray-700">
                <span class="font-medium">{{ (currentPage - 1) * pageSize + 1 }}</span>
                -
                <span class="font-medium">{{ Math.min(currentPage * pageSize, totalItems) }}</span>
                /
                <span class="font-medium">{{ totalItems }}</span>
                개 결과
              </p>
            </div>
            <div>
              <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                <button
                  @click="previousPage"
                  :disabled="currentPage === 1"
                  class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                >
                  <span class="sr-only">이전</span>
                  <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </button>
                <button
                  v-for="page in visiblePages"
                  :key="page"
                  @click="goToPage(page)"
                  :class="[
                    page === currentPage
                      ? 'bg-primary-50 border-primary-500 text-primary-600'
                      : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50',
                    'relative inline-flex items-center px-4 py-2 border text-sm font-medium'
                  ]"
                >
                  {{ page }}
                </button>
                <button
                  @click="nextPage"
                  :disabled="currentPage >= totalPages"
                  class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                >
                  <span class="sr-only">다음</span>
                  <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </button>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Status Update Modal -->
    <div v-if="showStatusModalFlag" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="closeStatusModal"></div>
        
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>
        
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-primary-100 sm:mx-0 sm:h-10 sm:w-10">
                <svg class="h-6 w-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
                <h3 class="text-lg leading-6 font-medium text-gray-900">주문 상태 변경</h3>
                <div class="mt-4 space-y-4">
                  <div>
                    <label class="label">주문번호</label>
                    <div class="text-sm text-gray-600">{{ selectedOrder?.orderNumber }}</div>
                  </div>
                  <div>
                    <label class="label">현재 상태</label>
                    <div class="text-sm text-gray-600">{{ getStatusLabel(selectedOrder?.status) }}</div>
                  </div>
                  <div>
                    <label class="label">새 상태</label>
                    <select v-model="newStatus" class="input-field">
                      <option value="PENDING">대기</option>
                      <option value="PROCESSING">처리중</option>
                      <option value="SHIPPED">발송완료</option>
                      <option value="IN_TRANSIT">운송중</option>
                      <option value="DELIVERED">배송완료</option>
                      <option value="CANCELLED">취소</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              @click="updateStatus"
              :disabled="statusUpdating || !newStatus || newStatus === selectedOrder?.status"
              class="btn-primary w-full sm:w-auto sm:ml-3"
            >
              {{ statusUpdating ? '업데이트 중...' : '상태 변경' }}
            </button>
            <button
              @click="closeStatusModal"
              class="btn-secondary w-full sm:w-auto mt-3 sm:mt-0"
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../../../utils/api'

interface Order {
  id: number
  orderNumber: string
  trackingNumber?: string
  recipientName: string
  recipientPhone: string
  recipientAddress: string
  recipientPostalCode: string
  status: string
  createdAt: string
}

const router = useRouter()

const loading = ref(true)
const searching = ref(false)
const statusUpdating = ref(false)
const showStatusModalFlag = ref(false)

const orders = ref<Order[]>([])
const selectedOrder = ref<Order | null>(null)
const newStatus = ref('')

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value))

// Search form
const searchForm = ref({
  orderNumber: '',
  recipientName: '',
  status: ''
})

// Stats
const stats = ref({
  total: 0,
  pending: 0,
  processing: 0,
  shipped: 0,
  delivered: 0
})

const visiblePages = computed(() => {
  const range = 2
  const start = Math.max(1, currentPage.value - range)
  const end = Math.min(totalPages.value, currentPage.value + range)
  const pages = []
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

const getStatusClass = (status: string) => {
  const statusClasses: Record<string, string> = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'PROCESSING': 'bg-blue-100 text-blue-800',
    'SHIPPED': 'bg-purple-100 text-purple-800',
    'IN_TRANSIT': 'bg-indigo-100 text-indigo-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return statusClasses[status] || 'bg-gray-100 text-gray-800'
}

const getStatusLabel = (status?: string) => {
  const statusLabels: Record<string, string> = {
    'PENDING': '대기',
    'PROCESSING': '처리중',
    'SHIPPED': '발송완료',
    'IN_TRANSIT': '운송중',
    'DELIVERED': '배송완료',
    'CANCELLED': '취소'
  }
  return statusLabels[status || ''] || status || '알 수 없음'
}

const formatDate = (dateString: string) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(dateString))
}

const loadOrders = async (page: number = 1) => {
  loading.value = true
  try {
    // Get orders from backend API
    const response = await adminApi.getAllOrders({
      page: page - 1, // Backend expects 0-based page
      size: pageSize.value,
      ...searchForm.value
    })
    
    if (response.success && response.data?.success) {
      orders.value = response.data.orders || []
      totalItems.value = response.data.totalElements || 0
      currentPage.value = page
      
      // Calculate stats
      calculateStats()
    } else {
      console.error('Failed to load orders:', response.error || response.data?.error)
      // Load mock data for now since backend might not be ready
      loadMockOrders(page)
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    // Load mock data as fallback
    loadMockOrders(page)
  } finally {
    loading.value = false
  }
}

const loadMockOrders = (page: number = 1) => {
  // Mock data for development
  const mockOrders: Order[] = [
    {
      id: 1,
      orderNumber: 'YSC202401001',
      trackingNumber: 'TRK001',
      recipientName: '김철수',
      recipientPhone: '010-1234-5678',
      recipientAddress: '서울특별시 강남구 테헤란로 123',
      recipientPostalCode: '06142',
      status: 'PENDING',
      createdAt: new Date().toISOString()
    },
    {
      id: 2,
      orderNumber: 'YSC202401002',
      trackingNumber: 'TRK002',
      recipientName: '이영희',
      recipientPhone: '010-2345-6789',
      recipientAddress: '부산광역시 해운대구 센텀로 456',
      recipientPostalCode: '48058',
      status: 'PROCESSING',
      createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      orderNumber: 'YSC202401003',
      trackingNumber: 'TRK003',
      recipientName: '박민준',
      recipientPhone: '010-3456-7890',
      recipientAddress: '대구광역시 수성구 동대구로 789',
      recipientPostalCode: '42190',
      status: 'SHIPPED',
      createdAt: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 4,
      orderNumber: 'YSC202401004',
      trackingNumber: 'TRK004',
      recipientName: '정수진',
      recipientPhone: '010-4567-8901',
      recipientAddress: '인천광역시 연수구 송도대로 321',
      recipientPostalCode: '21984',
      status: 'DELIVERED',
      createdAt: new Date(Date.now() - 72 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 5,
      orderNumber: 'YSC202401005',
      trackingNumber: 'TRK005',
      recipientName: '최동호',
      recipientPhone: '010-5678-9012',
      recipientAddress: '광주광역시 서구 상무대로 654',
      recipientPostalCode: '61945',
      status: 'CANCELLED',
      createdAt: new Date(Date.now() - 96 * 60 * 60 * 1000).toISOString()
    }
  ]
  
  orders.value = mockOrders
  totalItems.value = mockOrders.length
  currentPage.value = page
  calculateStats()
}

const calculateStats = () => {
  const statusCounts = orders.value.reduce((acc, order) => {
    acc[order.status] = (acc[order.status] || 0) + 1
    return acc
  }, {} as Record<string, number>)
  
  stats.value = {
    total: orders.value.length,
    pending: statusCounts.PENDING || 0,
    processing: statusCounts.PROCESSING || 0,
    shipped: statusCounts.SHIPPED || 0,
    delivered: statusCounts.DELIVERED || 0
  }
}

const searchOrders = async () => {
  searching.value = true
  try {
    await loadOrders(1)
  } finally {
    searching.value = false
  }
}

const clearSearch = () => {
  searchForm.value = {
    orderNumber: '',
    recipientName: '',
    status: ''
  }
  loadOrders(1)
}

const refreshOrders = () => {
  loadOrders(currentPage.value)
}

const viewOrder = (order: Order) => {
  router.push(`/orders/${order.orderNumber}`)
}

const showStatusModal = (order: Order) => {
  selectedOrder.value = order
  newStatus.value = order.status
  showStatusModalFlag.value = true
}

const closeStatusModal = () => {
  showStatusModalFlag.value = false
  selectedOrder.value = null
  newStatus.value = ''
}

const updateStatus = async () => {
  if (!selectedOrder.value || !newStatus.value) return
  
  statusUpdating.value = true
  try {
    const response = await adminApi.updateOrderStatus(selectedOrder.value.id, newStatus.value)
    
    if (response.success) {
      // Update the order in the local list
      const orderIndex = orders.value.findIndex(o => o.id === selectedOrder.value!.id)
      if (orderIndex !== -1) {
        orders.value[orderIndex].status = newStatus.value
      }
      
      // Recalculate stats
      calculateStats()
      
      // Close modal
      closeStatusModal()
      
      console.log('Order status updated successfully')
    } else {
      console.error('Failed to update order status:', response.error)
      alert('주문 상태 업데이트에 실패했습니다.')
    }
  } catch (error) {
    console.error('Error updating order status:', error)
    alert('주문 상태 업데이트 중 오류가 발생했습니다.')
  } finally {
    statusUpdating.value = false
  }
}

// Pagination methods
const previousPage = () => {
  if (currentPage.value > 1) {
    loadOrders(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    loadOrders(currentPage.value + 1)
  }
}

const goToPage = (page: number) => {
  loadOrders(page)
}

onMounted(() => {
  loadOrders()
})
</script>