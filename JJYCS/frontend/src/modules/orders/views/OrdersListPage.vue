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

// 데모 주문 데이터
const ordersData = ref([
  {
    id: 'YCS202401008',
    orderNumber: 'YCS202401008',
    date: '2024-01-20',
    status: 'delivered',
    statusText: '배송완료',
    items: [
      { name: '스마트폰 케이스', quantity: 2, price: 25000 },
      { name: '무선 이어폰', quantity: 1, price: 150000 }
    ],
    total: 200000,
    recipient: '홍길동',
    address: '서울시 강남구 테헤란로 123'
  },
  {
    id: 'YCS202401007',
    orderNumber: 'YCS202401007',
    date: '2024-01-18',
    status: 'shipping',
    statusText: '배송중',
    items: [
      { name: '화장품 세트', quantity: 1, price: 80000 },
      { name: '향수', quantity: 2, price: 120000 }
    ],
    total: 320000,
    recipient: '김영희',
    address: '부산시 해운대구 해운대로 456'
  },
  {
    id: 'YCS202401006',
    orderNumber: 'YCS202401006',
    date: '2024-01-15',
    status: 'preparing',
    statusText: '준비중',
    items: [
      { name: '운동화', quantity: 1, price: 180000 }
    ],
    total: 180000,
    recipient: '박민수',
    address: '대구시 중구 중앙로 789'
  },
  {
    id: 'YCS202401005',
    orderNumber: 'YCS202401005',
    date: '2024-01-12',
    status: 'delivered',
    statusText: '배송완료',
    items: [
      { name: '노트북 가방', quantity: 1, price: 90000 },
      { name: '마우스 패드', quantity: 3, price: 45000 }
    ],
    total: 135000,
    recipient: '이수진',
    address: '인천시 남동구 논현로 321'
  },
  {
    id: 'YCS202401004',
    orderNumber: 'YCS202401004',
    date: '2024-01-10',
    status: 'cancelled',
    statusText: '취소',
    items: [
      { name: '블루투스 스피커', quantity: 2, price: 200000 }
    ],
    total: 400000,
    recipient: '최민호',
    address: '광주시 서구 상무대로 654'
  },
  {
    id: 'YCS202401003',
    orderNumber: 'YCS202401003',
    date: '2024-01-08',
    status: 'delivered',
    statusText: '배송완료',
    items: [
      { name: '의류 세트', quantity: 3, price: 150000 }
    ],
    total: 450000,
    recipient: '정영우',
    address: '대전시 유성구 대학로 987'
  },
  {
    id: 'YCS202401002',
    orderNumber: 'YCS202401002',
    date: '2024-01-05',
    status: 'delivered',
    statusText: '배송완료',
    items: [
      { name: '건강 보조식품', quantity: 2, price: 120000 }
    ],
    total: 240000,
    recipient: '김영희',
    address: '울산시 남구 삼산로 147'
  },
  {
    id: 'YCS202401001',
    orderNumber: 'YCS202401001',
    date: '2024-01-03',
    status: 'delivered',
    statusText: '배송완료',
    items: [
      { name: '전자책 리더기', quantity: 1, price: 300000 }
    ],
    total: 300000,
    recipient: '홍길동',
    address: '세종시 한누리대로 258'
  }
])

const filteredOrders = ref([...ordersData.value])

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
  if (items.length > 1) {
    return `${items[0].name} 외 ${items.length - 1}건`
  }
  return items[0].name
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
  // 로딩 시뮬레이션
  await new Promise(resolve => setTimeout(resolve, 1000))
  loading.value = false
  applyFilters()
})
</script>