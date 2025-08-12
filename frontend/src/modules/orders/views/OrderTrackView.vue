<template>
  <div class="order-track-view">
    <div class="container mx-auto p-6">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">주문 추적</h1>
            <p class="text-gray-600">실시간으로 주문 상태를 확인하세요</p>
          </div>
          <button
            @click="goBack"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
          >
            ← 돌아가기
          </button>
        </div>

        <div v-if="order" class="space-y-6">
          <!-- Order Summary -->
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center justify-between mb-4">
              <div>
                <h2 class="text-lg font-semibold">{{ order.orderCode }}</h2>
                <p class="text-gray-600">주문일: {{ formatDate(order.createdAt) }}</p>
              </div>
              <span
                class="px-4 py-2 text-sm rounded-full font-medium"
                :class="getStatusClass(order.status)"
              >
                {{ getStatusText(order.status) }}
              </span>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm text-gray-600">수취인</div>
                <div class="font-medium">{{ order.recipientName }}</div>
                <div class="text-sm text-gray-500">{{ order.recipientPhone }}</div>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm text-gray-600">배송 방식</div>
                <div class="font-medium">{{ order.orderType === 'air' ? '항공' : '해상' }}</div>
                <div class="text-sm text-gray-500">{{ order.estimatedDelivery ? `예상 도착: ${formatDate(order.estimatedDelivery)}` : '' }}</div>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm text-gray-600">송장번호</div>
                <div class="font-medium">{{ order.trackingNumber || '배정 대기' }}</div>
                <div class="text-sm text-gray-500">{{ order.carrier || '' }}</div>
              </div>
            </div>
          </div>

          <!-- Tracking Timeline -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-6">배송 진행 상황</h3>
            
            <div class="relative">
              <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>
              
              <div
                v-for="(event, index) in trackingEvents"
                :key="index"
                class="relative flex items-start pb-8"
              >
                <div
                  class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center border-4 border-white z-10"
                  :class="{
                    'bg-green-500': event.completed,
                    'bg-blue-500': event.current,
                    'bg-gray-300': !event.completed && !event.current
                  }"
                >
                  <CheckIcon v-if="event.completed" class="w-4 h-4 text-white" />
                  <ClockIcon v-else-if="event.current" class="w-4 h-4 text-white" />
                  <div v-else class="w-2 h-2 bg-white rounded-full"></div>
                </div>
                
                <div class="ml-6 flex-1">
                  <div class="flex items-center justify-between">
                    <h4
                      class="font-medium"
                      :class="{
                        'text-green-900': event.completed,
                        'text-blue-900': event.current,
                        'text-gray-500': !event.completed && !event.current
                      }"
                    >
                      {{ event.title }}
                    </h4>
                    <span
                      v-if="event.timestamp"
                      class="text-sm"
                      :class="{
                        'text-green-600': event.completed,
                        'text-blue-600': event.current,
                        'text-gray-400': !event.completed && !event.current
                      }"
                    >
                      {{ formatDateTime(event.timestamp) }}
                    </span>
                  </div>
                  <p
                    class="text-sm mt-1"
                    :class="{
                      'text-green-700': event.completed,
                      'text-blue-700': event.current,
                      'text-gray-400': !event.completed && !event.current
                    }"
                  >
                    {{ event.description }}
                  </p>
                  <div v-if="event.location" class="text-sm text-gray-500 mt-1">
                    📍 {{ event.location }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Shipment Details -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Origin -->
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-lg font-semibold mb-4">발송지</h3>
              <div class="space-y-3">
                <div>
                  <div class="text-sm text-gray-600">창고</div>
                  <div class="font-medium">{{ shipmentDetails.origin.warehouse }}</div>
                </div>
                <div>
                  <div class="text-sm text-gray-600">주소</div>
                  <div class="text-sm">{{ shipmentDetails.origin.address }}</div>
                </div>
                <div>
                  <div class="text-sm text-gray-600">발송일</div>
                  <div class="text-sm">{{ shipmentDetails.origin.departureDate ? formatDateTime(shipmentDetails.origin.departureDate) : '미정' }}</div>
                </div>
              </div>
            </div>

            <!-- Destination -->
            <div class="bg-white rounded-lg shadow p-6">
              <h3 class="text-lg font-semibold mb-4">배송지</h3>
              <div class="space-y-3">
                <div>
                  <div class="text-sm text-gray-600">수취인</div>
                  <div class="font-medium">{{ order.recipientName }}</div>
                </div>
                <div>
                  <div class="text-sm text-gray-600">주소</div>
                  <div class="text-sm">{{ order.recipientAddress }}</div>
                </div>
                <div>
                  <div class="text-sm text-gray-600">예상 도착일</div>
                  <div class="text-sm">{{ order.estimatedDelivery ? formatDate(order.estimatedDelivery) : '계산 중' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">추가 정보</h3>
            <div class="flex flex-wrap gap-4">
              <button
                v-if="order.trackingNumber"
                @click="openCarrierTracking"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                운송업체 추적
              </button>
              <button
                @click="downloadInvoice"
                class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              >
                인보이스 다운로드
              </button>
              <button
                @click="contactSupport"
                class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
              >
                고객 지원
              </button>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-else class="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import {
  CheckIcon,
  ClockIcon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()

// Props
const orderId = route.params.id as string

// State
const order = ref<any>(null)
const trackingEvents = ref<any[]>([])
const shipmentDetails = ref<any>({})

// Methods
const loadOrderTracking = async () => {
  try {
    // Mock order and tracking data
    order.value = {
      id: orderId,
      orderCode: `ORD-${orderId}`,
      status: 'shipped',
      orderType: 'sea',
      recipientName: '홍길동',
      recipientPhone: '010-1234-5678',
      recipientAddress: '태국 방콕 수쿰빗 소이 11, 123번지',
      createdAt: '2025-08-10T10:00:00Z',
      trackingNumber: 'TRK123456789',
      carrier: 'Thailand Post',
      estimatedDelivery: '2025-08-15T18:00:00Z'
    }

    trackingEvents.value = [
      {
        title: '주문 접수',
        description: '주문이 성공적으로 접수되었습니다.',
        timestamp: '2025-08-10T10:00:00Z',
        location: '서울 물류센터',
        completed: true,
        current: false
      },
      {
        title: '상품 준비',
        description: '주문하신 상품을 준비하고 있습니다.',
        timestamp: '2025-08-10T14:30:00Z',
        location: '서울 물류센터',
        completed: true,
        current: false
      },
      {
        title: '포장 완료',
        description: '상품 포장이 완료되었습니다.',
        timestamp: '2025-08-11T09:00:00Z',
        location: '서울 물류센터',
        completed: true,
        current: false
      },
      {
        title: '국제 발송',
        description: '국제 운송업체에 인계되어 태국으로 발송되었습니다.',
        timestamp: '2025-08-11T16:00:00Z',
        location: '인천국제공항',
        completed: true,
        current: false
      },
      {
        title: '현지 도착',
        description: '태국 현지에 도착하여 통관 절차를 진행하고 있습니다.',
        timestamp: '2025-08-12T08:00:00Z',
        location: '방콕 국제우체국',
        completed: false,
        current: true
      },
      {
        title: '배송 중',
        description: '현지 배송업체에서 배송을 진행합니다.',
        timestamp: null,
        location: '방콕',
        completed: false,
        current: false
      },
      {
        title: '배송 완료',
        description: '수취인에게 성공적으로 배송되었습니다.',
        timestamp: null,
        location: '배송 주소',
        completed: false,
        current: false
      }
    ]

    shipmentDetails.value = {
      origin: {
        warehouse: 'YCS 서울 물류센터',
        address: '서울시 강서구 마곡중앙로 161-17',
        departureDate: '2025-08-11T16:00:00Z'
      },
      destination: {
        address: order.value.recipientAddress,
        estimatedArrival: order.value.estimatedDelivery
      }
    }

  } catch (error) {
    toast.error('추적 정보를 불러오는데 실패했습니다.')
    console.error('Load tracking error:', error)
  }
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

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('ko-KR')
}

const openCarrierTracking = () => {
  // Mock carrier tracking URL
  const trackingUrl = `https://track.thailandpost.co.th/?trackNumber=${order.value.trackingNumber}`
  window.open(trackingUrl, '_blank')
}

const downloadInvoice = () => {
  toast.info('인보이스 다운로드 기능은 준비 중입니다.')
}

const contactSupport = () => {
  toast.info('고객 지원팀에 연결됩니다.')
}

const goBack = () => {
  router.go(-1)
}

// Lifecycle
onMounted(() => {
  loadOrderTracking()
})
</script>

<style scoped>
.order-track-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>