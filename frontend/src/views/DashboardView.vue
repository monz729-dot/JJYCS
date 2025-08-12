<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Welcome Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">
          {{ $t('dashboard.welcome', { name: authStore.user?.name || 'User' }) }}
        </h1>
        <p class="mt-1 text-sm text-gray-600">
          {{ $t('dashboard.subtitle') }}
        </p>
      </div>

      <!-- Account Status Alert (for pending users) -->
      <div v-if="authStore.isPending" class="mb-6 rounded-md bg-yellow-50 p-4 border border-yellow-200">
        <div class="flex">
          <div class="flex-shrink-0">
            <ClockIcon class="h-5 w-5 text-yellow-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-yellow-800">
              {{ $t('dashboard.account_pending_title') }}
            </h3>
            <div class="mt-2 text-sm text-yellow-700">
              <p>{{ $t('dashboard.account_pending_message') }}</p>
            </div>
            <div class="mt-3">
              <div class="-mx-2 -my-1.5 flex">
                <router-link
                  to="/profile"
                  class="bg-yellow-50 px-2 py-1.5 rounded-md text-sm font-medium text-yellow-800 hover:bg-yellow-100"
                >
                  {{ $t('dashboard.complete_profile') }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Member Code Warning -->
      <div v-if="!authStore.user?.memberCode && authStore.isActive" class="mb-6 rounded-md bg-orange-50 p-4 border border-orange-200">
        <div class="flex">
          <div class="flex-shrink-0">
            <ExclamationTriangleIcon class="h-5 w-5 text-orange-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-orange-800">
              {{ $t('dashboard.no_member_code_title') }}
            </h3>
            <div class="mt-2 text-sm text-orange-700">
              <p>{{ $t('dashboard.no_member_code_message') }}</p>
            </div>
            <div class="mt-3">
              <div class="-mx-2 -my-1.5 flex">
                <router-link
                  to="/profile"
                  class="bg-orange-50 px-2 py-1.5 rounded-md text-sm font-medium text-orange-800 hover:bg-orange-100"
                >
                  {{ $t('dashboard.register_member_code') }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Admin Quick Access -->
      <div v-if="authStore.user?.role === 'admin'" class="mb-8">
        <h2 class="text-xl font-semibold text-gray-900 mb-4">🔐 관리자 메뉴</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <router-link
            :to="{ name: 'AdminDashboard' }"
            class="bg-gradient-to-br from-red-50 to-red-100 p-6 rounded-lg border border-red-200 hover:shadow-md transition-all duration-200 group"
          >
            <div class="flex items-center">
              <div class="w-12 h-12 bg-red-200 rounded-lg flex items-center justify-center mr-4 group-hover:bg-red-300 transition-colors">
                📊
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">관리자 대시보드</h3>
                <p class="text-sm text-gray-600">시스템 전체 현황</p>
              </div>
            </div>
          </router-link>
          
          <router-link
            :to="{ name: 'AdminUsers' }"
            class="bg-gradient-to-br from-blue-50 to-blue-100 p-6 rounded-lg border border-blue-200 hover:shadow-md transition-all duration-200 group"
          >
            <div class="flex items-center">
              <div class="w-12 h-12 bg-blue-200 rounded-lg flex items-center justify-center mr-4 group-hover:bg-blue-300 transition-colors">
                👥
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">사용자 관리</h3>
                <p class="text-sm text-gray-600">전체 계정 관리</p>
              </div>
            </div>
          </router-link>
          
          <router-link
            :to="{ name: 'AdminApprovals' }"
            class="bg-gradient-to-br from-orange-50 to-orange-100 p-6 rounded-lg border border-orange-200 hover:shadow-md transition-all duration-200 group relative"
          >
            <div class="flex items-center">
              <div class="w-12 h-12 bg-orange-200 rounded-lg flex items-center justify-center mr-4 group-hover:bg-orange-300 transition-colors">
                ✅
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">계정 승인</h3>
                <p class="text-sm text-gray-600">승인 대기 관리</p>
              </div>
            </div>
            <div class="absolute -top-2 -right-2">
              <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-orange-200 text-orange-800">
                8개 대기
              </span>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Quick Actions -->
      <div v-if="authStore.isActive" class="mb-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <router-link
            v-if="authStore.canCreateOrder"
            :to="{ name: 'OrderCreate' }"
            class="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div>
              <span class="rounded-lg inline-flex p-3 bg-blue-50 text-blue-700 ring-4 ring-white">
                <PlusIcon class="h-6 w-6" />
              </span>
            </div>
            <div class="mt-4">
              <h3 class="text-lg font-medium">
                <span class="absolute inset-0" />
                {{ $t('dashboard.create_order') }}
              </h3>
              <p class="mt-2 text-sm text-gray-500">
                {{ $t('dashboard.create_order_desc') }}
              </p>
            </div>
          </router-link>

          <router-link
            :to="{ name: 'OrderList' }"
            class="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div>
              <span class="rounded-lg inline-flex p-3 bg-green-50 text-green-700 ring-4 ring-white">
                <DocumentTextIcon class="h-6 w-6" />
              </span>
            </div>
            <div class="mt-4">
              <h3 class="text-lg font-medium">
                <span class="absolute inset-0" />
                {{ $t('dashboard.view_orders') }}
              </h3>
              <p class="mt-2 text-sm text-gray-500">
                {{ $t('dashboard.view_orders_desc') }}
              </p>
            </div>
          </router-link>

          <router-link
            to="/tracking"
            class="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div>
              <span class="rounded-lg inline-flex p-3 bg-purple-50 text-purple-700 ring-4 ring-white">
                <MapPinIcon class="h-6 w-6" />
              </span>
            </div>
            <div class="mt-4">
              <h3 class="text-lg font-medium">
                <span class="absolute inset-0" />
                {{ $t('dashboard.track_shipment') }}
              </h3>
              <p class="mt-2 text-sm text-gray-500">
                {{ $t('dashboard.track_shipment_desc') }}
              </p>
            </div>
          </router-link>

          <router-link
            v-if="authStore.hasRole(['WAREHOUSE', 'ADMIN'])"
            :to="{ name: 'WarehouseScan' }"
            class="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-blue-500 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div>
              <span class="rounded-lg inline-flex p-3 bg-orange-50 text-orange-700 ring-4 ring-white">
                <QrCodeIcon class="h-6 w-6" />
              </span>
            </div>
            <div class="mt-4">
              <h3 class="text-lg font-medium">
                <span class="absolute inset-0" />
                {{ $t('dashboard.warehouse_scan') }}
              </h3>
              <p class="mt-2 text-sm text-gray-500">
                {{ $t('dashboard.warehouse_scan_desc') }}
              </p>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Stats Overview -->
      <div v-if="authStore.isActive" class="mb-8">
        <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('dashboard.overview') }}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <!-- Total Orders -->
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="p-5">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <DocumentTextIcon class="h-6 w-6 text-gray-400" />
                </div>
                <div class="ml-5 w-0 flex-1">
                  <dl>
                    <dt class="text-sm font-medium text-gray-500 truncate">
                      {{ $t('dashboard.total_orders') }}
                    </dt>
                    <dd class="flex items-baseline">
                      <div class="text-2xl font-semibold text-gray-900">
                        {{ stats.totalOrders }}
                      </div>
                      <div v-if="stats.ordersTrend !== 0" class="ml-2 flex items-baseline text-sm font-semibold" :class="{
                        'text-green-600': stats.ordersTrend > 0,
                        'text-red-600': stats.ordersTrend < 0
                      }">
                        <ArrowUpIcon v-if="stats.ordersTrend > 0" class="self-center flex-shrink-0 h-4 w-4" />
                        <ArrowDownIcon v-else class="self-center flex-shrink-0 h-4 w-4" />
                        {{ Math.abs(stats.ordersTrend) }}%
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-5 py-3">
              <div class="text-sm">
                <router-link :to="{ name: 'OrderList' }" class="font-medium text-blue-700 hover:text-blue-900">
                  {{ $t('dashboard.view_all') }}
                </router-link>
              </div>
            </div>
          </div>

          <!-- Pending Orders -->
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="p-5">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <ClockIcon class="h-6 w-6 text-yellow-400" />
                </div>
                <div class="ml-5 w-0 flex-1">
                  <dl>
                    <dt class="text-sm font-medium text-gray-500 truncate">
                      {{ $t('dashboard.pending_orders') }}
                    </dt>
                    <dd class="flex items-baseline">
                      <div class="text-2xl font-semibold text-gray-900">
                        {{ stats.pendingOrders }}
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-5 py-3">
              <div class="text-sm">
                <router-link :to="{ name: 'OrderList', query: { status: 'pending' } }" class="font-medium text-yellow-700 hover:text-yellow-900">
                  {{ $t('dashboard.review_pending') }}
                </router-link>
              </div>
            </div>
          </div>

          <!-- In Transit -->
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="p-5">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <TruckIcon class="h-6 w-6 text-blue-400" />
                </div>
                <div class="ml-5 w-0 flex-1">
                  <dl>
                    <dt class="text-sm font-medium text-gray-500 truncate">
                      {{ $t('dashboard.in_transit') }}
                    </dt>
                    <dd class="flex items-baseline">
                      <div class="text-2xl font-semibold text-gray-900">
                        {{ stats.inTransitOrders }}
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-5 py-3">
              <div class="text-sm">
                <router-link to="/tracking" class="font-medium text-blue-700 hover:text-blue-900">
                  {{ $t('dashboard.track_shipments') }}
                </router-link>
              </div>
            </div>
          </div>

          <!-- Total Spent -->
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="p-5">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <CurrencyDollarIcon class="h-6 w-6 text-green-400" />
                </div>
                <div class="ml-5 w-0 flex-1">
                  <dl>
                    <dt class="text-sm font-medium text-gray-500 truncate">
                      {{ $t('dashboard.total_spent') }}
                    </dt>
                    <dd class="flex items-baseline">
                      <div class="text-2xl font-semibold text-gray-900">
                        {{ formatCurrency(stats.totalSpent) }}
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-5 py-3">
              <div class="text-sm">
                <router-link :to="{ name: 'PaymentList' }" class="font-medium text-green-700 hover:text-green-900">
                  {{ $t('dashboard.view_payments') }}
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Orders -->
      <div v-if="authStore.isActive && recentOrders.length > 0" class="mb-8">
        <div class="bg-white shadow rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-medium text-gray-900">{{ $t('dashboard.recent_orders') }}</h2>
              <router-link
                :to="{ name: 'OrderList' }"
                class="text-sm font-medium text-blue-600 hover:text-blue-500"
              >
                {{ $t('dashboard.view_all_orders') }}
              </router-link>
            </div>
          </div>
          <div class="overflow-hidden">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('dashboard.order') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('dashboard.recipient') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('dashboard.status') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('dashboard.amount') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('dashboard.date') }}
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr
                  v-for="order in recentOrders"
                  :key="order.id"
                  class="hover:bg-gray-50 cursor-pointer"
                  @click="goToOrder(order.id)"
                >
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <div>
                        <div class="text-sm font-medium text-gray-900">
                          {{ order.orderCode }}
                        </div>
                        <div class="text-sm text-gray-500">
                          {{ $t('dashboard.items_count', { count: order.items?.length || 0 }) }}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">{{ order.recipientName }}</div>
                    <div class="text-sm text-gray-500">{{ order.recipientCountry }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span
                      class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                      :class="getStatusBadgeClass(order.status)"
                    >
                      {{ $t(`orders.status.${order.status}`) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {{ formatCurrency(order.totalAmount, order.currency) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatDate(order.createdAt) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Warehouse Dashboard (for warehouse users) -->
      <div v-if="authStore.hasRole(['WAREHOUSE', 'ADMIN'])" class="mb-8">
        <div class="bg-white shadow rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-medium text-gray-900">{{ $t('dashboard.warehouse_overview') }}</h2>
              <router-link
                :to="{ name: 'WarehouseInventory' }"
                class="text-sm font-medium text-blue-600 hover:text-blue-500"
              >
                {{ $t('dashboard.view_full_inventory') }}
              </router-link>
            </div>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600">{{ warehouseStats.totalBoxes }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.total_boxes') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-yellow-600">{{ warehouseStats.pendingInbound }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.pending_inbound') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600">{{ warehouseStats.readyForOutbound }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.ready_outbound') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-red-600">{{ warehouseStats.onHold }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.on_hold') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Admin Dashboard (for admin users) -->
      <div v-if="authStore.hasRole('ADMIN')" class="mb-8">
        <div class="bg-white shadow rounded-lg">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-medium text-gray-900">{{ $t('dashboard.admin_overview') }}</h2>
              <router-link
                :to="{ name: 'AdminDashboard' }"
                class="text-sm font-medium text-blue-600 hover:text-blue-500"
              >
                {{ $t('dashboard.admin_panel') }}
              </router-link>
            </div>
          </div>
          <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="text-center">
                <div class="text-2xl font-bold text-yellow-600">{{ adminStats.pendingApprovals }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.pending_approvals') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600">{{ adminStats.totalUsers }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.total_users') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600">{{ formatCurrency(adminStats.monthlyRevenue) }}</div>
                <div class="text-sm text-gray-600">{{ $t('dashboard.monthly_revenue') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Tips -->
      <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div class="flex">
          <div class="flex-shrink-0">
            <LightBulbIcon class="h-5 w-5 text-blue-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-blue-800">
              {{ $t('dashboard.tips_title') }}
            </h3>
            <div class="mt-2 text-sm text-blue-700">
              <ul class="list-disc list-inside space-y-1">
                <li>{{ $t('dashboard.tip_1') }}</li>
                <li>{{ $t('dashboard.tip_2') }}</li>
                <li>{{ $t('dashboard.tip_3') }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useOrdersStore } from '@/stores/orders'
import { useWarehouseStore } from '@/stores/warehouse'
import {
  PlusIcon,
  DocumentTextIcon,
  MapPinIcon,
  QrCodeIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  TruckIcon,
  CurrencyDollarIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  LightBulbIcon
} from '@heroicons/vue/24/outline'

// Composables
const router = useRouter()
const authStore = useAuthStore()
const ordersStore = useOrdersStore()
const warehouseStore = useWarehouseStore()

// State
const loading = ref(false)

// Computed
const recentOrders = computed(() => {
  return ordersStore.recentOrders
})

const stats = computed(() => {
  return ordersStore.dashboardStats
})

const warehouseStats = computed(() => ({
  totalBoxes: 156,
  pendingInbound: 23,
  readyForOutbound: 45,
  onHold: 8
}))

const adminStats = computed(() => ({
  pendingApprovals: 12,
  totalUsers: 1247,
  monthlyRevenue: 2450000
}))

// Methods
const loadDashboardData = async () => {
  loading.value = true
  
  try {
    // Load recent orders for active users
    if (authStore.isActive) {
      await ordersStore.fetchOrders({ page: 0, size: 5 })
    }
    
    // Load warehouse data for warehouse users
    if (authStore.hasRole(['WAREHOUSE', 'ADMIN'])) {
      // await warehouseStore.fetchInventory(1) // Mock warehouse ID
    }
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

const goToOrder = (orderId: string) => {
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    requested: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-blue-100 text-blue-800',
    in_progress: 'bg-purple-100 text-purple-800',
    shipped: 'bg-orange-100 text-orange-800',
    delivered: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatCurrency = (amount: number, currency: string = 'THB') => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

const formatDate = (dateString: string) => {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(dateString))
}

// Lifecycle
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
/* Custom styles */
</style>