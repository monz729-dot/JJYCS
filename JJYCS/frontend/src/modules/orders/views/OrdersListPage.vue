<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4">
    <!-- 헤더 -->
    <div class="sticky top-0 z-40 bg-white border-b border-blue-100 p-4 shadow-sm mb-8">
      <div class="flex items-center justify-between max-w-6xl mx-auto">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white font-bold">
            YCS
          </div>
          <div>
            <h1 class="text-lg font-semibold text-blue-800">주문 이력</h1>
          </div>
        </div>
        <button @click="router.back()" class="px-3 py-2 text-sm border border-blue-200 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors">
          뒤로가기
        </button>
      </div>
    </div>

    <div class="max-w-6xl mx-auto space-y-8">
      <!-- 필터 섹션 -->
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-gray-700">기간</label>
            <select v-model="filters.period" class="p-3 border border-gray-200 rounded-lg text-sm min-w-[150px]">
              <option value="all">전체</option>
              <option value="1month">최근 1개월</option>
              <option value="3months">최근 3개월</option>
              <option value="6months">최근 6개월</option>
              <option value="1year">최근 1년</option>
            </select>
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-gray-700">상태</label>
            <select v-model="filters.status" class="p-3 border border-gray-200 rounded-lg text-sm min-w-[150px]">
              <option value="all">전체</option>
              <option value="requested">요청</option>
              <option value="preparing">준비중</option>
              <option value="shipping">배송중</option>
              <option value="delivered">배송완료</option>
              <option value="cancelled">취소</option>
            </select>
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-gray-700">주문번호</label>
            <input 
              v-model="filters.orderNumber" 
              type="text" 
              placeholder="주문번호 검색" 
              class="p-3 border border-gray-200 rounded-lg text-sm min-w-[150px]"
            />
          </div>
        </div>

        <button @click="applyFilters" class="bg-blue-500 text-white px-6 py-3 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors">
          검색
        </button>
      </div>

      <!-- 주문 목록 -->
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-semibold text-blue-800">주문 목록</h2>
          <div class="text-sm text-gray-600">총 {{ totalOrders }}건</div>
        </div>

        <div v-if="loading" class="space-y-4">
          <div v-for="i in 5" :key="i" class="animate-pulse border border-gray-200 rounded-xl p-6">
            <div class="space-y-3">
              <div class="flex justify-between">
                <div class="h-4 bg-gray-200 rounded w-32"></div>
                <div class="h-4 bg-gray-200 rounded w-16"></div>
              </div>
              <div class="h-3 bg-gray-200 rounded w-48"></div>
              <div class="h-3 bg-gray-200 rounded w-64"></div>
            </div>
          </div>
        </div>

        <div v-else-if="filteredOrders.length === 0" class="text-center py-12 text-gray-600">
          <div class="text-5xl mb-4 opacity-50">📦</div>
          <h3 class="text-lg font-medium mb-2">주문 내역이 없습니다</h3>
          <p class="text-sm">검색 조건을 변경하거나 새로운 주문을 생성해보세요.</p>
        </div>

        <div v-else class="space-y-4">
          <div 
            v-for="order in currentPageOrders" 
            :key="order.id" 
            class="border border-gray-200 rounded-xl p-6 hover:border-blue-300 hover:shadow-md transition-all"
          >
            <div class="flex flex-col md:flex-row md:items-start md:justify-between mb-4">
              <div>
                <div class="text-lg font-semibold text-blue-800 mb-1">{{ order.orderNumber }}</div>
                <div class="text-sm text-gray-600">{{ formatDate(order.date) }}</div>
              </div>
              <div class="mt-4 md:mt-0 text-right">
                <div :class="['inline-block px-3 py-1 rounded-full text-xs font-medium mb-2', getStatusClass(order.status)]">
                  {{ getStatusText(order.status) }}
                </div>
                <div class="text-sm text-gray-600">{{ formatCurrency(order.total) }}</div>
              </div>
            </div>

            <div class="mb-4 text-sm text-gray-700 leading-relaxed">
              <div class="font-medium mb-1">{{ getItemsSummary(order.items) }}</div>
              <div>수취인: {{ order.recipient }}</div>
              <div>배송지: {{ order.address }}</div>
            </div>

            <div class="flex gap-3 flex-wrap">
              <button 
                @click="viewOrderDetail(order.id)" 
                class="px-4 py-2 text-sm border border-blue-300 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors"
              >
                상세보기
              </button>
              <button 
                v-if="['shipping', 'preparing'].includes(order.status)" 
                @click="trackOrder(order.id)" 
                class="px-4 py-2 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                배송추적
              </button>
              <button 
                v-if="order.status === 'delivered'" 
                @click="reorder(order.id)" 
                class="px-4 py-2 text-sm bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                재주문
              </button>
            </div>
          </div>
        </div>

        <!-- 페이지네이션 -->
        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-8">
          <button 
            v-if="currentPage > 1"
            @click="changePage(currentPage - 1)" 
            class="px-3 py-2 text-sm border border-gray-300 bg-white text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            이전
          </button>

          <button 
            v-for="page in visiblePages" 
            :key="page" 
            @click="changePage(page)"
            :class="['px-3 py-2 text-sm rounded-lg transition-colors', page === currentPage ? 'bg-blue-500 text-white border-blue-500' : 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-50']"
          >
            {{ page }}
          </button>

          <button 
            v-if="currentPage < totalPages"
            @click="changePage(currentPage + 1)" 
            class="px-3 py-2 text-sm border border-gray-300 bg-white text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            다음
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(true)
const currentPage = ref(1)
const itemsPerPage = 5

const filters = reactive({
  period: 'all',
  status: 'all',
  orderNumber: ''
})

// 주문 데이터 
const ordersData = ref([])
const filteredOrders = ref([])

// API 함수들
const fetchOrders = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/orders/user/me', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        'Content-Type': 'application/json'
      }
    })
    
    const result = await response.json()
    
    if (result.success && result.data) {
      // 백엔드 데이터를 프론트엔드 형식에 맞게 변환
      ordersData.value = result.data.map(order => ({
        id: order.id,
        orderNumber: order.orderNumber,
        date: order.createdAt.split('T')[0], // ISO 날짜에서 날짜 부분만
        status: mapBackendStatus(order.status),
        statusText: getStatusText(mapBackendStatus(order.status)),
        items: order.items || [], 
        total: calculateTotal(order.items || []),
        recipient: getRecipientName(order),
        address: getRecipientAddress(order),
        // 태국 전용 필드들
        shippingType: order.shippingType,
        destCountry: order.destCountry || 'TH',
        repacking: order.repacking || false,
        inboundMethod: order.inboundMethod,
        courierCompany: order.courierCompany,
        waybillNo: order.waybillNo,
        emsVerified: order.emsVerified || false,
        thailandPostalVerified: order.thailandPostalVerified || false
      }))
    } else {
      console.error('Failed to fetch orders:', result.message)
      ordersData.value = []
    }
  } catch (error) {
    console.error('Error fetching orders:', error)
    ordersData.value = []
  } finally {
    loading.value = false
  }
}

// 백엔드 상태를 프론트엔드 상태로 매핑
const mapBackendStatus = (backendStatus) => {
  const statusMap = {
    'RECEIVED': 'requested',
    'ARRIVED': 'preparing', 
    'REPACKING': 'preparing',
    'SHIPPING': 'shipping',
    'DELIVERED': 'delivered',
    'BILLING': 'preparing',
    'PAYMENT_PENDING': 'preparing',
    'PAYMENT_CONFIRMED': 'preparing',
    'COMPLETED': 'delivered'
  }
  return statusMap[backendStatus] || 'requested'
}

// 수취인 이름 추출 (다중 수취인 지원)
const getRecipientName = (order) => {
  if (order.recipientsJson && order.recipientsJson.length > 0) {
    const primary = order.recipientsJson.find(r => r.isPrimary) || order.recipientsJson[0]
    return primary.name
  }
  return order.recipientName || '수취인 정보 없음'
}

// 수취인 주소 추출 (다중 수취인 지원)
const getRecipientAddress = (order) => {
  if (order.recipientsJson && order.recipientsJson.length > 0) {
    const primary = order.recipientsJson.find(r => r.isPrimary) || order.recipientsJson[0]
    return primary.address
  }
  return order.recipientAddress || '주소 정보 없음'
}

// 총 금액 계산
const calculateTotal = (items) => {
  return items.reduce((total, item) => {
    return total + (item.totalPrice || item.unitPrice * item.quantity || 0)
  }, 0)
}

// Computed properties
const totalOrders = computed(() => filteredOrders.value.length)
const totalPages = computed(() => Math.ceil(filteredOrders.value.length / itemsPerPage))

const currentPageOrders = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return filteredOrders.value.slice(startIndex, endIndex)
})

const visiblePages = computed(() => {
  const pages = []
  for (let i = 1; i <= totalPages.value; i++) {
    pages.push(i)
  }
  return pages
})

// Status helpers
const getStatusClass = (status: string) => {
  const statusClasses = {
    'requested': 'bg-yellow-100 text-yellow-700',
    'preparing': 'bg-blue-100 text-blue-700', 
    'shipping': 'bg-purple-100 text-purple-700',
    'delivered': 'bg-green-100 text-green-700',
    'cancelled': 'bg-red-100 text-red-700'
  }
  return statusClasses[status as keyof typeof statusClasses] || 'bg-gray-100 text-gray-700'
}

const getStatusText = (status: string) => {
  const statusTexts = {
    'requested': '요청',
    'preparing': '준비중',
    'shipping': '배송중',
    'delivered': '배송완료',
    'cancelled': '취소'
  }
  return statusTexts[status as keyof typeof statusTexts] || status
}

// Utility functions
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR')
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR').format(amount) + '원'
}

const getItemsSummary = (items: any[]) => {
  if (!items || items.length === 0) {
    return '품목 없음'
  }
  if (items.length > 1) {
    const firstItemName = items[0].description || items[0].name || '품목'
    return `${firstItemName} 외 ${items.length - 1}건`
  }
  return items[0].description || items[0].name || '품목'
}

// Filter and pagination functions
const applyFilters = () => {
  filteredOrders.value = ordersData.value.filter(order => {
    // 기간 필터
    if (filters.period !== 'all') {
      const orderDate = new Date(order.date)
      const now = new Date()
      const monthsAgo = filters.period === '1month' ? 1 : 
                       filters.period === '3months' ? 3 : 
                       filters.period === '6months' ? 6 : 12
      
      const filterDate = new Date()
      filterDate.setMonth(filterDate.getMonth() - monthsAgo)
      
      if (orderDate < filterDate) return false
    }
    
    // 상태 필터
    if (filters.status !== 'all' && order.status !== filters.status) return false
    
    // 주문번호 필터
    if (filters.orderNumber && !order.orderNumber.toLowerCase().includes(filters.orderNumber.toLowerCase())) return false
    
    return true
  })
  
  currentPage.value = 1
}

const changePage = (page: number) => {
  currentPage.value = page
}

// Action handlers
const viewOrderDetail = (orderId: string) => {
  router.push(`/orders/${orderId}`)
}

const trackOrder = (orderId: string) => {
  router.push(`/orders/${orderId}/tracking`)
}

const reorder = async (orderId: string) => {
  const order = ordersData.value.find(o => o.id === orderId)
  if (order) {
    const confirmed = confirm(`${order.items[0].name} ${order.items.length > 1 ? '외 ' + (order.items.length - 1) + '건' : ''}을 재주문하시겠습니까?`)
    if (confirmed) {
      alert('재주문이 처리되었습니다.')
    }
  }
}

onMounted(async () => {
  await fetchOrders()
  applyFilters()
})
</script>