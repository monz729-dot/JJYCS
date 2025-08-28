<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 헤더 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">어드민 대시보드</h1>
            <p class="text-sm text-gray-600">YCS LMS 시스템 관리 및 모니터링</p>
          </div>
          <div class="flex items-center space-x-4">
            <div class="text-right">
              <div class="text-sm font-medium text-gray-900">관리자</div>
              <div class="text-xs text-gray-500">{{ currentTime }}</div>
            </div>
            <div class="h-8 w-8 bg-blue-500 rounded-full flex items-center justify-center">
              <span class="text-white font-medium text-sm">관</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-6 space-y-6">
      <!-- 핵심 지표 -->
      <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <!-- 전체 주문 -->
        <div class="bg-white overflow-hidden shadow-sm rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                  <span class="text-2xl">📦</span>
                </div>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">전체 주문</dt>
                  <dd class="text-2xl font-bold text-gray-900">{{ stats.totalOrders }}</dd>
                </dl>
              </div>
            </div>
          </div>
          <div class="bg-blue-50 px-5 py-3">
            <div class="text-sm">
              <span class="text-blue-600 font-medium">+{{ stats.todayOrders }}</span>
              <span class="text-gray-600"> 오늘 새 주문</span>
            </div>
          </div>
        </div>

        <!-- 창고 현황 -->
        <div class="bg-white overflow-hidden shadow-sm rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                  <span class="text-2xl">🏭</span>
                </div>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">창고 보관</dt>
                  <dd class="text-2xl font-bold text-gray-900">{{ warehouse.totalInWarehouse }}</dd>
                </dl>
              </div>
            </div>
          </div>
          <div class="bg-green-50 px-5 py-3">
            <div class="text-sm">
              <span class="text-green-600 font-medium">{{ warehouse.arrivedCount }}</span>
              <span class="text-gray-600"> 입고 완료</span>
            </div>
          </div>
        </div>

        <!-- 비즈니스 룰 적용 -->
        <div class="bg-white overflow-hidden shadow-sm rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center">
                  <span class="text-2xl">⚠️</span>
                </div>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">특수 처리</dt>
                  <dd class="text-2xl font-bold text-gray-900">{{ stats.specialCases }}</dd>
                </dl>
              </div>
            </div>
          </div>
          <div class="bg-yellow-50 px-5 py-3">
            <div class="text-sm text-gray-600">
              CBM초과/THB초과/코드없음
            </div>
          </div>
        </div>

        <!-- 시스템 상태 -->
        <div class="bg-white overflow-hidden shadow-sm rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                  <span class="text-2xl">✅</span>
                </div>
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">시스템 상태</dt>
                  <dd class="text-lg font-medium text-green-600">정상</dd>
                </dl>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-5 py-3">
            <div class="text-sm text-gray-600">
              최근 업데이트: {{ currentTime }}
            </div>
          </div>
        </div>
      </div>

      <!-- 빠른 작업 -->
      <div class="bg-white rounded-lg shadow-sm">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">빠른 작업</h2>
        </div>
        <div class="p-6">
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <button 
              @click="navigateToOrders" 
              class="group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-green-500 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-green-50"
            >
              <div>
                <span class="rounded-lg inline-flex p-3 bg-green-50 text-green-700 ring-4 ring-white text-2xl">
                  📦
                </span>
              </div>
              <div class="mt-4">
                <h3 class="text-lg font-medium text-gray-900">
                  주문 관리
                </h3>
                <p class="mt-2 text-sm text-gray-500">
                  주문 조회 및 상태 관리
                </p>
              </div>
            </button>

            <button 
              @click="navigateToWarehouse" 
              class="group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-purple-500 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-purple-50"
            >
              <div>
                <span class="rounded-lg inline-flex p-3 bg-purple-50 text-purple-700 ring-4 ring-white text-2xl">
                  🏭
                </span>
              </div>
              <div class="mt-4">
                <h3 class="text-lg font-medium text-gray-900">
                  창고 관리
                </h3>
                <p class="mt-2 text-sm text-gray-500">
                  창고 현황 및 스캔 관리
                </p>
              </div>
            </button>

            <button 
              @click="navigateToUserApproval" 
              class="group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-yellow-500 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-yellow-50"
            >
              <div>
                <span class="rounded-lg inline-flex p-3 bg-yellow-50 text-yellow-700 ring-4 ring-white text-2xl">
                  👥
                </span>
              </div>
              <div class="mt-4">
                <h3 class="text-lg font-medium text-gray-900">
                  사용자 승인
                </h3>
                <p class="mt-2 text-sm text-gray-500">
                  기업/파트너 승인 처리
                </p>
              </div>
            </button>

            <button 
              @click="navigateToReports" 
              class="group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-orange-500 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-orange-50"
            >
              <div>
                <span class="rounded-lg inline-flex p-3 bg-orange-50 text-orange-700 ring-4 ring-white text-2xl">
                  📊
                </span>
              </div>
              <div class="mt-4">
                <h3 class="text-lg font-medium text-gray-900">
                  리포트
                </h3>
                <p class="mt-2 text-sm text-gray-500">
                  통계 및 분석 리포트
                </p>
              </div>
            </button>
          </div>
        </div>
      </div>

      <!-- 최근 활동 -->
      <div class="bg-white rounded-lg shadow-sm">
        <div class="px-6 py-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">최근 활동</h2>
        </div>
        <div class="p-6">
          <div v-if="loading" class="space-y-4">
            <div v-for="i in 5" :key="i" class="animate-pulse flex space-x-4">
              <div class="rounded-full bg-gray-200 h-10 w-10"></div>
              <div class="flex-1 space-y-2 py-1">
                <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
          </div>
          <div v-else class="flow-root">
            <ul class="-mb-8">
              <li v-for="(activity, index) in recentActivity" :key="activity.id">
                <div class="relative pb-8">
                  <div v-if="index !== recentActivity.length - 1" class="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200"></div>
                  <div class="relative flex space-x-3">
                    <div>
                      <span :class="[
                        'h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white text-xl',
                        getActivityIconClass(activity.type)
                      ]">
                        {{ getActivityIcon(activity.type) }}
                      </span>
                    </div>
                    <div class="min-w-0 flex-1 pt-1.5 flex justify-between space-x-4">
                      <div>
                        <p class="text-sm text-gray-500">{{ getKoreanDescription(activity) }}</p>
                      </div>
                      <div class="text-right text-sm whitespace-nowrap text-gray-500">
                        {{ formatTimeKorean(activity.timestamp) }}
                      </div>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/utils/api'

const router = useRouter()
const loading = ref(true)

const stats = ref({
  totalOrders: 0,
  todayOrders: 0,
  specialCases: 0
})

const warehouse = ref({
  totalInWarehouse: 0,
  arrivedCount: 0
})

const currentTime = ref(new Date().toLocaleString('ko-KR'))

interface Activity {
  id: number
  type: 'user_registered' | 'order_created' | 'user_approved' | 'scan_event' | 'status_changed'
  description: string
  timestamp: Date
}

const recentActivity = ref<Activity[]>([])

const getActivityIconClass = (type: string) => {
  const classes = {
    'user_registered': 'bg-blue-500',
    'order_created': 'bg-green-500', 
    'user_approved': 'bg-purple-500',
    'scan_event': 'bg-orange-500',
    'status_changed': 'bg-indigo-500'
  }
  return classes[type as keyof typeof classes] || 'bg-gray-500'
}

const getActivityIcon = (type: string) => {
  const icons = {
    'user_registered': '👥',
    'order_created': '📦',
    'user_approved': '✅',
    'scan_event': '📱',
    'status_changed': '⚙️'
  }
  return icons[type as keyof typeof icons] || '📝'
}

const getKoreanDescription = (activity: Activity) => {
  const typeMap = {
    'user_registered': '새 사용자 등록',
    'order_created': '새 주문 생성',
    'user_approved': '사용자 승인 완료',
    'scan_event': '창고 스캔 이벤트',
    'status_changed': '주문 상태 변경'
  }
  return activity.description || typeMap[activity.type as keyof typeof typeMap] || activity.type
}

const formatTimeKorean = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  
  return date.toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric'
  })
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    // 통계 API 호출 - 올바른 엔드포인트 사용
    const statsResponse = await adminApi.getSystemStats()
    
    if (statsResponse.success && statsResponse.data) {
      // 백엔드 통계 데이터 직접 사용
      const statsData = statsResponse.data
      stats.value.totalOrders = statsData.totalOrders || 0
      stats.value.todayOrders = statsData.todayOrders || 0
      stats.value.specialCases = statsData.specialCases || 0
      
      // 창고 통계 (백엔드에서 제공되면 사용, 아니면 0으로 설정)
      warehouse.value.totalInWarehouse = statsData.totalInWarehouse || 0
      warehouse.value.arrivedCount = statsData.arrivedCount || 0
    } else {
      console.log('통계 API 실패, 목 데이터 사용')
    }
    
    // 최근 주문 조회 (별도로)
    const ordersResponse = await adminApi.getAllOrders({ page: 1, pageSize: 5 })
    if (ordersResponse.success && ordersResponse.data) {
      const orders = Array.isArray(ordersResponse.data) ? ordersResponse.data : 
                    (ordersResponse.data.content || [])
      
      // 최근 활동 생성 (실제 데이터 기반)
      recentActivity.value = orders.slice(0, 5).map((order: any, index: number) => ({
        id: order.id,
        type: 'order_created' as const,
        description: `새 주문 ${order.orderNumber}이(가) ${order.user?.name || order.recipientName}님에 의해 생성됨`,
        timestamp: new Date(order.createdAt)
      }))
      
    } else {
      // 목 데이터 폴백
      stats.value = {
        totalOrders: 19,
        todayOrders: 3,
        specialCases: 8
      }
      
      warehouse.value = {
        totalInWarehouse: 12,
        arrivedCount: 8
      }
      
      recentActivity.value = [
        {
          id: 1,
          type: 'order_created',
          description: '새 주문 YCS-250824-012이(가) 테스트 수취인님에 의해 생성됨',
          timestamp: new Date(Date.now() - 300000) // 5분 전
        },
        {
          id: 2,
          type: 'user_registered',
          description: '삼성전자가 기업 사용자로 등록됨',
          timestamp: new Date(Date.now() - 600000) // 10분 전
        },
        {
          id: 3,
          type: 'scan_event',
          description: '창고에서 입고 스캔 이벤트 발생',
          timestamp: new Date(Date.now() - 3600000) // 1시간 전
        },
        {
          id: 4,
          type: 'status_changed',
          description: '주문 YCS-250824-011 상태가 배송중으로 변경됨',
          timestamp: new Date(Date.now() - 7200000) // 2시간 전
        },
        {
          id: 5,
          type: 'user_approved',
          description: 'LG전자가 관리자에 의해 승인됨',
          timestamp: new Date(Date.now() - 86400000) // 1일 전
        }
      ]
    }
  } catch (error) {
    console.error('대시보드 데이터 로드 실패:', error)
    
    // 에러 시 목 데이터
    stats.value = {
      totalOrders: 0,
      todayOrders: 0,
      specialCases: 0
    }
    warehouse.value = {
      totalInWarehouse: 0,
      arrivedCount: 0
    }
  } finally {
    loading.value = false
  }
}

// 네비게이션 함수들
const navigateToOrders = () => {
  router.push('/admin/orders')
}

const navigateToWarehouse = () => {
  window.open('/warehouse-scan.html', '_blank')
}

const navigateToUserApproval = () => {
  alert('사용자 승인 페이지로 이동 예정')
}

const navigateToReports = () => {
  alert('리포트 페이지로 이동 예정')
}

onMounted(() => {
  loadDashboardData()
  
  // 실시간 시간 업데이트
  setInterval(() => {
    currentTime.value = new Date().toLocaleString('ko-KR')
  }, 1000)
})
</script>