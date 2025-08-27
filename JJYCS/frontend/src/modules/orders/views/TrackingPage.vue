<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4 pb-24">
    <!-- 헤더 -->
    <div class="sticky top-0 z-40 bg-white border-b border-blue-100 p-4 shadow-sm mb-8">
      <div class="flex items-center justify-between max-w-6xl mx-auto">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center text-white font-bold">
            YCS
          </div>
          <div>
            <h1 class="text-lg font-semibold text-blue-800">주문 추적</h1>
          </div>
        </div>
        <button @click="router.back()" class="px-3 py-2 text-sm border border-blue-200 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors">
          뒤로가기
        </button>
      </div>
    </div>

    <div class="max-w-6xl mx-auto space-y-8">
      <!-- 검색 섹션 -->
      <div class="bg-white rounded-2xl p-8 shadow-blue-100 shadow-lg border border-blue-100">
        <h2 class="text-xl font-semibold text-blue-800 mb-6">주문번호로 추적하기</h2>
        <form @submit="trackOrder" class="flex gap-4 flex-col md:flex-row">
          <input 
            v-model="orderNumber"
            type="text" 
            placeholder="주문번호를 입력하세요 (예: YCS202401001)" 
            class="flex-1 p-4 border border-gray-200 rounded-xl text-base"
            required
          />
          <button 
            type="submit" 
            :disabled="loading"
            :class="[
              'px-8 py-4 rounded-xl text-base font-semibold transition-colors',
              loading 
                ? 'bg-gray-400 text-gray-200 cursor-not-allowed' 
                : 'bg-blue-500 text-white hover:bg-blue-600'
            ]"
          >
            {{ loading ? '추적 중...' : '추적하기' }}
          </button>
        </form>
      </div>

      <!-- 추적 결과 -->
      <div v-if="showResult && trackingInfo" class="bg-white rounded-2xl p-8 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex flex-col md:flex-row md:items-start md:justify-between mb-8 pb-6 border-b border-gray-200">
          <div>
            <h2 class="text-xl font-semibold text-blue-800 mb-2">주문번호: {{ trackingInfo.title }}</h2>
            <div class="text-gray-600 text-sm leading-relaxed">
              <div>수취인: {{ trackingInfo.recipient }}</div>
              <div>배송지: {{ trackingInfo.address }}</div>
              <div>주문일: {{ trackingInfo.orderDate }}</div>
            </div>
          </div>
          <div class="mt-4 md:mt-0 text-right">
            <div :class="['inline-block px-4 py-2 rounded-full text-sm font-medium mb-2', getStatusClass(trackingInfo.status)]">
              {{ trackingInfo.statusText }}
            </div>
            <div class="text-xs text-gray-600">{{ getEstimatedTimeText() }}</div>
          </div>
        </div>

        <div class="relative pl-8">
          <div 
            v-for="(item, index) in trackingInfo.timeline" 
            :key="index" 
            :class="['relative pb-8', {'pb-0': index === trackingInfo.timeline.length - 1}]"
          >
            <!-- Timeline dot -->
            <div 
              :class="[
                'absolute left-[-2rem] top-2 w-3 h-3 rounded-full border-3 border-white',
                item.active ? 'bg-blue-500 shadow-blue-200 shadow-lg' : 'bg-gray-300'
              ]"
            ></div>

            <!-- Timeline line -->
            <div 
              v-if="index !== trackingInfo.timeline.length - 1"
              class="absolute left-[-1.625rem] top-5 bottom-[-1rem] w-0.5 bg-gray-200"
            ></div>

            <!-- Timeline content -->
            <div class="bg-gray-50 rounded-xl p-6">
              <div class="text-base font-semibold text-blue-800 mb-2">{{ item.title }}</div>
              <div class="text-xs text-gray-600 mb-3">{{ item.time }}</div>
              <div class="text-sm text-gray-700 leading-relaxed" v-html="item.description"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 결과 없음 -->
      <div v-else-if="showResult && !trackingInfo" class="bg-white rounded-2xl p-8 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="text-center py-12 text-gray-600">
          <div class="text-5xl mb-4 opacity-50">📦</div>
          <h3 class="text-lg font-medium mb-2">추적 결과를 찾을 수 없습니다</h3>
          <p class="text-sm">입력하신 주문번호를 다시 확인해주세요.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/utils/api'

const route = useRoute()
const router = useRouter()

const orderNumber = ref('')
const trackingInfo = ref<any>(null)
const showResult = ref(false)
const loading = ref(false)
const updateInterval = ref<NodeJS.Timeout | null>(null)

const trackOrder = async (event: Event) => {
  event.preventDefault()
  
  const orderNum = orderNumber.value.trim().toUpperCase()
  if (!orderNum) return
  
  loading.value = true
  showResult.value = false
  
  try {
    // 주문 추적 API 호출
    const response = await ordersApi.getOrderTracking(orderNum)
    
    if (response.success && response.data) {
      trackingInfo.value = formatTrackingData(response.data)
    } else {
      trackingInfo.value = null
    }
    
    showResult.value = true
    
    // 실시간 업데이트 시작
    startRealTimeUpdates(orderNum)
    
  } catch (error) {
    console.error('Order tracking failed:', error)
    trackingInfo.value = null
    showResult.value = true
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status: string) => {
  const statusClasses = {
    'preparing': 'bg-blue-100 text-blue-700',
    'shipping': 'bg-purple-100 text-purple-700', 
    'delivered': 'bg-green-100 text-green-700'
  }
  return statusClasses[status as keyof typeof statusClasses] || 'bg-gray-100 text-gray-700'
}

const getEstimatedTimeText = () => {
  if (!trackingInfo.value) return ''
  
  return trackingInfo.value.status === 'delivered' 
    ? '배송 완료' 
    : `예상 도착: ${trackingInfo.value.estimatedDelivery}`
}

// 추적 데이터 포맷 변환
const formatTrackingData = (apiData: any) => {
  return {
    title: apiData.orderNumber,
    recipient: apiData.recipientName || '수취인',
    address: apiData.recipientAddress || '배송 주소',
    orderDate: formatDate(apiData.createdAt),
    status: apiData.status?.toLowerCase() || 'preparing',
    statusText: getStatusText(apiData.status),
    estimatedDelivery: formatDate(apiData.estimatedDelivery) || '미정',
    timeline: apiData.timeline?.map((item: any, index: number) => ({
      title: item.eventName,
      time: formatDateTime(item.eventTime),
      description: item.description || '',
      active: index <= (apiData.currentStage || 0)
    })) || []
  }
}

// 상태 텍스트 변환
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'preparing': '준비 중',
    'processing': '처리 중',
    'shipped': '배송 중',
    'in_transit': '운송 중',
    'delivered': '배송 완료',
    'cancelled': '취소됨',
    'delayed': '지연'
  }
  return statusMap[status?.toLowerCase()] || '상태 확인 중'
}

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 날짜시간 포맷팅
const formatDateTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.toLocaleDateString('ko-KR')} ${date.toLocaleTimeString('ko-KR', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })}`
}

// 실시간 업데이트 시작
const startRealTimeUpdates = (orderNumber: string) => {
  // 기존 인터벌 클리어
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }
  
  // 30초마다 업데이트
  updateInterval.value = setInterval(async () => {
    try {
      const response = await ordersApi.getOrderTracking(orderNumber)
      if (response.success && response.data) {
        trackingInfo.value = formatTrackingData(response.data)
      }
    } catch (error) {
      console.error('Real-time update failed:', error)
    }
  }, 30000)
}

// 실시간 업데이트 중지
const stopRealTimeUpdates = () => {
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
    updateInterval.value = null
  }
}

onMounted(() => {
  // URL 파라미터에서 주문번호 확인
  const orderParam = route.query.order as string
  if (orderParam) {
    orderNumber.value = orderParam
    // 자동으로 추적 실행
    const event = new Event('submit')
    trackOrder(event)
  }
})

onUnmounted(() => {
  // 컴포넌트 언마운트 시 실시간 업데이트 중지
  stopRealTimeUpdates()
})
</script>