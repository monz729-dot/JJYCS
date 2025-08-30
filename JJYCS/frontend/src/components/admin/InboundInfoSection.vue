<template>
  <div class="bg-white shadow-sm border border-gray-200 rounded-lg">
    <!-- 섹션 헤더 -->
    <div class="px-6 py-4 border-b border-gray-100 bg-gray-50 rounded-t-lg">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
          <h3 class="text-lg font-semibold text-gray-900">배대지 접수 정보</h3>
        </div>
        <div class="flex items-center space-x-2">
          <!-- 접수 방법 배지 -->
          <span :class="getMethodBadgeClass(order.inboundMethod)" class="px-2 py-1 text-xs font-medium rounded-full">
            {{ getMethodText(order.inboundMethod) }}
          </span>
          <!-- 검증 상태 배지 -->
          <span v-if="order.emsVerified" class="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">
            EMS 검증완료
          </span>
        </div>
      </div>
    </div>

    <!-- 섹션 내용 -->
    <div class="p-6">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 접수 방법 상세 -->
        <div class="space-y-4">
          <h4 class="text-md font-medium text-gray-900 mb-3">접수 방법 상세</h4>
          
          <!-- 택배 접수 -->
          <div v-if="order.inboundMethod === 'COURIER'" class="space-y-3">
            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-3m-13 0h3m-3 0l3-3m0 0l3 3M9 21V9a2 2 0 012-2h2a2 2 0 012 2v12M9 21h6"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">택배사명</p>
                <p class="text-sm text-gray-600">{{ order.courierCompany || '미입력' }}</p>
              </div>
            </div>
            
            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">송장번호</p>
                <div class="flex items-center space-x-2">
                  <p class="text-sm text-gray-600 font-mono">{{ order.waybillNo || '미입력' }}</p>
                  <button 
                    v-if="order.waybillNo && trackingUrl"
                    @click="openTrackingUrl"
                    class="text-blue-600 hover:text-blue-800 text-sm font-medium"
                  >
                    추적하기
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 퀵서비스 접수 -->
          <div v-else-if="order.inboundMethod === 'QUICK'" class="space-y-3">
            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">퀵서비스 업체명</p>
                <p class="text-sm text-gray-600">{{ order.quickVendor || '미입력' }}</p>
              </div>
            </div>
            
            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">연락처</p>
                <p class="text-sm text-gray-600">{{ order.quickPhone || '미입력' }}</p>
              </div>
            </div>
          </div>

          <!-- 기타 접수 -->
          <div v-else class="text-sm text-gray-600">
            기타 접수 방법으로 설정되었습니다.
          </div>
        </div>

        <!-- YCS 접수지 정보 -->
        <div class="space-y-4">
          <h4 class="text-md font-medium text-gray-900 mb-3">YCS 접수지 정보</h4>
          
          <div v-if="inboundLocation" class="space-y-3">
            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">접수지명</p>
                <p class="text-sm text-gray-600">{{ inboundLocation.name }}</p>
              </div>
            </div>

            <div class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">주소</p>
                <p class="text-sm text-gray-600">{{ inboundLocation.address }}</p>
                <p v-if="inboundLocation.postalCode" class="text-xs text-gray-500 mt-1">
                  우편번호: {{ inboundLocation.postalCode }}
                </p>
              </div>
            </div>

            <div v-if="inboundLocation.contactPerson" class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">담당자</p>
                <div class="flex items-center space-x-2">
                  <p class="text-sm text-gray-600">{{ inboundLocation.contactPerson }}</p>
                  <p v-if="inboundLocation.phone" class="text-sm text-gray-500">{{ inboundLocation.phone }}</p>
                </div>
              </div>
            </div>

            <div v-if="inboundLocation.businessHours" class="flex items-start space-x-3">
              <svg class="w-5 h-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">운영시간</p>
                <p class="text-sm text-gray-600">{{ inboundLocation.businessHours }}</p>
              </div>
            </div>
          </div>

          <div v-else class="text-sm text-gray-500">
            접수지 정보가 설정되지 않았습니다.
          </div>
        </div>
      </div>

      <!-- 접수 관련 요청사항 -->
      <div v-if="order.inboundNote" class="mt-6 pt-6 border-t border-gray-100">
        <h4 class="text-md font-medium text-gray-900 mb-3">접수 관련 요청사항</h4>
        <div class="bg-gray-50 rounded-lg p-4">
          <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ order.inboundNote }}</p>
        </div>
      </div>

      <!-- 검증 정보 -->
      <div class="mt-6 pt-6 border-t border-gray-100">
        <h4 class="text-md font-medium text-gray-900 mb-3">검증 정보</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex items-center space-x-2">
            <div :class="order.thailandPostalVerified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'" 
                 class="px-2 py-1 text-xs font-medium rounded">
              {{ order.thailandPostalVerified ? '우편번호 검증완료' : '우편번호 미검증' }}
            </div>
          </div>
          <div class="flex items-center space-x-2">
            <div :class="order.emsVerified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'" 
                 class="px-2 py-1 text-xs font-medium rounded">
              {{ order.emsVerified ? 'EMS 검증완료' : 'EMS 미검증' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { Order, InboundLocation } from '@/types/order'

interface Props {
  order: Order
}

const props = defineProps<Props>()

// 반응형 데이터
const inboundLocation = ref<InboundLocation | null>(null)
const trackingUrl = ref<string | null>(null)

// 접수 방법 텍스트
const getMethodText = (method: string) => {
  switch (method) {
    case 'COURIER': return '택배 접수'
    case 'QUICK': return '퀵서비스'
    case 'OTHER': return '기타'
    default: return '미설정'
  }
}

// 접수 방법 배지 클래스
const getMethodBadgeClass = (method: string) => {
  const baseClass = 'px-2 py-1 text-xs font-medium rounded-full'
  switch (method) {
    case 'COURIER': return `${baseClass} bg-blue-100 text-blue-800`
    case 'QUICK': return `${baseClass} bg-green-100 text-green-800`
    case 'OTHER': return `${baseClass} bg-gray-100 text-gray-800`
    default: return `${baseClass} bg-gray-100 text-gray-600`
  }
}

// 송장 추적 URL 열기
const openTrackingUrl = () => {
  if (trackingUrl.value) {
    window.open(trackingUrl.value, '_blank')
  }
}

// 접수지 정보 로드
const loadInboundLocation = async () => {
  if (!props.order.inboundLocationId) return
  
  try {
    const response = await fetch(`/api/admin/inbound-locations/${props.order.inboundLocationId}`)
    const result = await response.json()
    
    if (result.success && result.data) {
      inboundLocation.value = result.data
    }
  } catch (error) {
    console.error('Failed to load inbound location:', error)
  }
}

// 송장 추적 URL 로드
const loadTrackingUrl = async () => {
  if (!props.order.courierCompany || !props.order.waybillNo) return
  
  try {
    const response = await fetch(
      `/api/admin/courier-companies/${props.order.courierCompany}/tracking-url?trackingNumber=${props.order.waybillNo}`
    )
    const result = await response.json()
    
    if (result.success && result.trackingUrl) {
      trackingUrl.value = result.trackingUrl
    }
  } catch (error) {
    console.error('Failed to load tracking URL:', error)
  }
}

onMounted(() => {
  loadInboundLocation()
  loadTrackingUrl()
})
</script>

<style scoped>
/* 필요한 경우 커스텀 스타일 추가 */
</style>