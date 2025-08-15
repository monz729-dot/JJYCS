<template>
  <div>
    <!-- Welcome Header -->
    <div class="spacing-lg">
      <h1 class="text-heading-xl">
        안녕하세요, {{ authStore.user?.name || '사용자' }}님! 👋
      </h1>
      <p class="text-body text-muted spacing-xs">
        오늘의 물류 현황을 한눈에 확인하세요
      </p>
    </div>

    <!-- Account Status Alert (for pending users) -->
    <Card v-if="authStore.isPending" class="spacing-lg border-yellow-200 bg-yellow-50">
      <div class="flex-start gap-md">
        <div class="w-10 h-10 rounded-lg bg-yellow-100 flex-center">
          <span class="mdi mdi-clock-outline text-yellow-600 text-xl" />
        </div>
        <div class="flex-1">
          <h3 class="text-heading-sm text-yellow-800">
            계정 승인 대기 중
          </h3>
          <p class="text-body-sm text-yellow-700 spacing-xs">
            관리자의 승인을 기다리고 있습니다. 평일 1-2일 내에 처리됩니다.
          </p>
          <div class="spacing-md">
            <Button variant="warning" size="sm" text="프로필 완성하기" @click="router.push('/app/profile')" />
          </div>
        </div>
      </div>
    </Card>

    <!-- Member Code Warning -->
    <Card v-if="!authStore.user?.memberCode && authStore.isApproved" class="mb-6 border-orange-200 bg-orange-50">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
          <span class="mdi mdi-alert-outline text-orange-600 text-xl" />
        </div>
        <div class="flex-1">
          <h3 class="font-medium text-orange-800">
            회원 코드 미등록
          </h3>
          <p class="mt-1 text-sm text-orange-700">
            원활한 배송을 위해 회원 코드를 등록해주세요. 미등록 시 배송이 지연될 수 있습니다.
          </p>
          <div class="mt-3">
            <Button variant="warning" size="sm" text="회원 코드 등록하기" @click="router.push('/app/profile')" />
          </div>
        </div>
      </div>
    </Card>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <Card 
        v-for="stat in stats" 
        :key="stat.title"
        :hoverable="true"
        class="relative overflow-hidden"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">{{ stat.title }}</p>
            <p class="mt-2 text-3xl font-bold text-gray-900">{{ stat.value }}</p>
            <div class="mt-2 flex items-center text-sm">
              <span 
                :class="[
                  'font-medium',
                  stat.trend === 'up' ? 'text-green-600' : 'text-red-600'
                ]"
              >
                <span :class="stat.trend === 'up' ? 'mdi mdi-trending-up' : 'mdi mdi-trending-down'" />
                {{ stat.change }}%
              </span>
              <span class="ml-2 text-gray-500">전일 대비</span>
            </div>
          </div>
          <div :class="[
            'p-3 rounded-lg',
            stat.iconBg
          ]">
            <span :class="['mdi', stat.icon, 'text-2xl', stat.iconColor]" />
          </div>
        </div>
      </Card>
    </div>

    <!-- Quick Actions -->
    <div class="mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">빠른 작업</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card 
          v-for="action in quickActions" 
          :key="action.title"
          :hoverable="true"
          :clickable="true"
          @click="handleQuickAction(action.route)"
          class="text-center"
        >
          <div :class="[
            'w-16 h-16 rounded-lg mx-auto mb-3 flex items-center justify-center',
            action.bgColor
          ]">
            <span :class="['mdi', action.icon, 'text-2xl', action.iconColor]" />
          </div>
          <h3 class="font-medium text-gray-900">{{ action.title }}</h3>
          <p class="text-xs text-gray-500 mt-1">{{ action.description }}</p>
        </Card>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
      <!-- Recent Orders -->
      <div class="lg:col-span-2">
        <Card title="최근 주문" subtitle="최근 7일간의 주문 내역">
          <template #action>
            <Button variant="ghost" size="sm" text="전체보기" icon="mdi-arrow-right" @click="router.push('/app/orders')" />
          </template>
          
          <Table
            :columns="orderColumns"
            :data="recentOrdersData"
            :hoverable="true"
          >
            <template #cell-status="{ value }">
              <Badge :variant="getStatusVariant(value)" :text="getStatusLabel(value)" />
            </template>
            <template #cell-amount="{ value }">
              <span class="font-medium">{{ formatCurrency(value) }}</span>
            </template>
            <template #actions="{ row }">
              <Button variant="ghost" size="xs" icon="mdi-eye" @click="viewOrder(row)" />
            </template>
          </Table>
        </Card>
      </div>

      <!-- Activity Feed -->
      <div>
        <Card title="최근 활동" subtitle="실시간 업데이트">
          <div class="space-y-4">
            <div v-for="activity in activities" :key="activity.id" class="flex gap-3">
              <div :class="[
                'w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0',
                activity.iconBg
              ]">
                <span :class="['mdi', activity.icon, 'text-lg', activity.iconColor]" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900">{{ activity.title }}</p>
                <p class="text-xs text-gray-500">{{ activity.description }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ activity.time }}</p>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Order Trends -->
      <Card title="주문 추이" subtitle="최근 30일">
        <template #action>
          <select class="text-sm border-gray-300 rounded-lg">
            <option>일별</option>
            <option>주별</option>
            <option>월별</option>
          </select>
        </template>
        <div class="h-64 flex items-center justify-center text-gray-400">
          <div class="text-center">
            <span class="mdi mdi-chart-line text-4xl mb-2 block" />
            <p>차트 컴포넌트 준비 중</p>
          </div>
        </div>
      </Card>

      <!-- Status Distribution -->
      <Card title="상태별 분포" subtitle="전체 주문 기준">
        <div class="h-64 flex items-center justify-center text-gray-400">
          <div class="text-center">
            <span class="mdi mdi-chart-donut text-4xl mb-2 block" />
            <p>차트 컴포넌트 준비 중</p>
          </div>
        </div>
      </Card>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'

const router = useRouter()
const authStore = useAuthStore()

// Dashboard stats
const stats = ref([
  {
    title: '신규 주문',
    value: '24',
    change: 12,
    trend: 'up',
    icon: 'mdi-package-variant',
    iconBg: 'bg-blue-50',
    iconColor: 'text-blue-600'
  },
  {
    title: '처리 대기',
    value: '8',
    change: 5,
    trend: 'down',
    icon: 'mdi-clock-outline',
    iconBg: 'bg-yellow-50',
    iconColor: 'text-yellow-600'
  },
  {
    title: '배송 중',
    value: '156',
    change: 8,
    trend: 'up',
    icon: 'mdi-truck-delivery',
    iconBg: 'bg-green-50',
    iconColor: 'text-green-600'
  },
  {
    title: '완료',
    value: '1,247',
    change: 15,
    trend: 'up',
    icon: 'mdi-check-circle',
    iconBg: 'bg-purple-50',
    iconColor: 'text-purple-600'
  }
])

// Recent orders table
const orderColumns = [
  { key: 'orderNo', label: '주문번호', width: '120px' },
  { key: 'customer', label: '고객명' },
  { key: 'status', label: '상태' },
  { key: 'amount', label: '금액', align: 'right' as const },
  { key: 'date', label: '주문일' }
]

const recentOrdersData = ref([
  { id: 1, orderNo: 'ORD-2024-001', customer: '김철수', status: 'pending', amount: 125000, date: '2024-01-15' },
  { id: 2, orderNo: 'ORD-2024-002', customer: '이영희', status: 'processing', amount: 89000, date: '2024-01-15' },
  { id: 3, orderNo: 'ORD-2024-003', customer: '박민수', status: 'shipped', amount: 234000, date: '2024-01-14' },
  { id: 4, orderNo: 'ORD-2024-004', customer: '최지은', status: 'delivered', amount: 156000, date: '2024-01-14' },
  { id: 5, orderNo: 'ORD-2024-005', customer: '정대호', status: 'pending', amount: 78000, date: '2024-01-13' }
])

// Activity feed
const activities = ref([
  {
    id: 1,
    title: '새 주문 접수',
    description: 'ORD-2024-006 주문이 접수되었습니다',
    time: '5분 전',
    icon: 'mdi-package-variant',
    iconBg: 'bg-blue-50',
    iconColor: 'text-blue-600'
  },
  {
    id: 2,
    title: '배송 완료',
    description: 'ORD-2024-003 배송이 완료되었습니다',
    time: '30분 전',
    icon: 'mdi-check-circle',
    iconBg: 'bg-green-50',
    iconColor: 'text-green-600'
  },
  {
    id: 3,
    title: '재고 부족 알림',
    description: 'SKU-12345 재고가 10개 이하입니다',
    time: '1시간 전',
    icon: 'mdi-alert',
    iconBg: 'bg-yellow-50',
    iconColor: 'text-yellow-600'
  },
  {
    id: 4,
    title: '견적 요청',
    description: '김철수님이 견적을 요청했습니다',
    time: '2시간 전',
    icon: 'mdi-calculator',
    iconBg: 'bg-purple-50',
    iconColor: 'text-purple-600'
  }
])

// Quick actions
const quickActions = computed(() => {
  const userType = authStore.userType || 'general'
  const actions = []

  if (['general', 'corporate'].includes(userType)) {
    actions.push({
      title: '새 주문',
      description: '주문 접수하기',
      icon: 'mdi-plus-circle',
      bgColor: 'bg-blue-50',
      iconColor: 'text-blue-600',
      route: '/app/orders/create'
    })
  }

  if (['warehouse', 'admin'].includes(userType)) {
    actions.push({
      title: 'QR 스캔',
      description: '상품 스캔하기',
      icon: 'mdi-qrcode-scan',
      bgColor: 'bg-green-50',
      iconColor: 'text-green-600',
      route: '/app/warehouse/scan'
    })
  }

  actions.push(
    {
      title: '배송 추적',
      description: '배송 상태 확인',
      icon: 'mdi-truck-fast',
      bgColor: 'bg-purple-50',
      iconColor: 'text-purple-600',
      route: '/app/tracking'
    },
    {
      title: '견적 확인',
      description: '견적서 조회',
      icon: 'mdi-file-document',
      bgColor: 'bg-yellow-50',
      iconColor: 'text-yellow-600',
      route: '/app/estimates'
    }
  )

  return actions
})

// Methods
const getStatusVariant = (status: string) => {
  const variants: Record<string, any> = {
    pending: 'warning',
    processing: 'info',
    shipped: 'primary',
    delivered: 'success'
  }
  return variants[status] || 'default'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    pending: '대기중',
    processing: '처리중',
    shipped: '배송중',
    delivered: '완료'
  }
  return labels[status] || status
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

const viewOrder = (order: any) => {
  router.push(`/app/orders/${order.id}`)
}

const handleQuickAction = (route: string) => {
  router.push(route)
}
</script>

<style scoped>
/* Custom styles */
</style>