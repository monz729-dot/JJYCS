<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Loading State -->
      <div v-if="isLoading && !order" class="flex justify-center items-center py-12">
        <div class="flex items-center">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="text-gray-600">{{ $t('orders.detail.loading') }}</span>
        </div>
      </div>

      <!-- Order Not Found -->
      <div v-else-if="!isLoading && !order" class="text-center py-12">
        <ExclamationTriangleIcon class="mx-auto h-12 w-12 text-red-400" />
        <h3 class="mt-2 text-sm font-medium text-gray-900">{{ $t('orders.detail.not_found') }}</h3>
        <p class="mt-1 text-sm text-gray-500">{{ $t('orders.detail.not_found_description') }}</p>
        <div class="mt-6">
          <router-link
            :to="{ name: 'OrderList' }"
            class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            <ArrowLeftIcon class="h-4 w-4 mr-2" />
            {{ $t('orders.detail.back_to_list') }}
          </router-link>
        </div>
      </div>

      <!-- Order Detail -->
      <div v-else class="space-y-6">
        <!-- Header -->
        <div class="bg-white shadow rounded-lg p-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <button
                type="button"
                class="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
                @click="$router.go(-1)"
              >
                <ArrowLeftIcon class="h-4 w-4 mr-1" />
                {{ $t('common.back') }}
              </button>
              <div>
                <h1 class="text-2xl font-bold text-gray-900">{{ order.orderCode }}</h1>
                <p class="text-sm text-gray-600">
                  {{ $t('orders.detail.created') }} {{ formatDate(order.createdAt) }}
                </p>
              </div>
            </div>
            <div class="flex items-center space-x-3">
              <span
                class="inline-flex px-3 py-1 text-sm font-semibold rounded-full"
                :class="getStatusBadgeClass(order.status)"
              >
                {{ $t(`orders.status.${order.status}`) }}
              </span>
              <div class="relative" v-if="canPerformActions">
                <button
                  type="button"
                  class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  @click="showActionsMenu = !showActionsMenu"
                >
                  {{ $t('common.actions') }}
                  <ChevronDownIcon class="ml-2 -mr-0.5 h-4 w-4" />
                </button>
                <!-- Actions Dropdown -->
                <div
                  v-if="showActionsMenu"
                  class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-10"
                >
                  <div class="py-1">
                    <button
                      v-if="canRequestEstimate"
                      class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      @click="requestEstimate"
                    >
                      {{ $t('orders.detail.request_estimate') }}
                    </button>
                    <button
                      v-if="canCancelOrder"
                      class="block w-full text-left px-4 py-2 text-sm text-red-700 hover:bg-gray-100"
                      @click="showCancelModal = true"
                    >
                      {{ $t('orders.detail.cancel_order') }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Business Rule Warnings -->
          <div v-if="order.validationWarnings && order.validationWarnings.length > 0" class="mt-4 space-y-2">
            <div
              v-for="warning in order.validationWarnings"
              :key="warning.type"
              class="rounded-md p-4"
              :class="{
                'bg-red-50 border border-red-200': warning.severity === 'error',
                'bg-yellow-50 border border-yellow-200': warning.severity === 'warning',
                'bg-blue-50 border border-blue-200': warning.severity === 'info'
              }"
            >
              <div class="flex">
                <div class="flex-shrink-0">
                  <ExclamationTriangleIcon 
                    v-if="warning.severity === 'error'" 
                    class="h-5 w-5 text-red-400" 
                  />
                  <ExclamationTriangleIcon 
                    v-else-if="warning.severity === 'warning'" 
                    class="h-5 w-5 text-yellow-400" 
                  />
                  <InformationCircleIcon 
                    v-else 
                    class="h-5 w-5 text-blue-400" 
                  />
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium" :class="{
                    'text-red-800': warning.severity === 'error',
                    'text-yellow-800': warning.severity === 'warning',
                    'text-blue-800': warning.severity === 'info'
                  }">
                    {{ warning.title }}
                  </h3>
                  <div class="mt-2 text-sm" :class="{
                    'text-red-700': warning.severity === 'error',
                    'text-yellow-700': warning.severity === 'warning',
                    'text-blue-700': warning.severity === 'info'
                  }">
                    <p>{{ warning.message }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Summary Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Recipient Info -->
          <div class="bg-white shadow rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.recipient_info') }}</h3>
            <div class="space-y-2">
              <div>
                <span class="text-sm text-gray-600">{{ $t('orders.detail.name') }}:</span>
                <span class="text-sm font-medium text-gray-900 ml-2">{{ order.recipientName }}</span>
              </div>
              <div>
                <span class="text-sm text-gray-600">{{ $t('orders.detail.phone') }}:</span>
                <span class="text-sm font-medium text-gray-900 ml-2">{{ order.recipientPhone }}</span>
              </div>
              <div>
                <span class="text-sm text-gray-600">{{ $t('orders.detail.address') }}:</span>
                <p class="text-sm font-medium text-gray-900 mt-1">{{ order.recipientAddress }}</p>
              </div>
            </div>
          </div>

          <!-- Shipping Info -->
          <div class="bg-white shadow rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.shipping_info') }}</h3>
            <div class="space-y-2">
              <div class="flex items-center">
                <TruckIcon v-if="order.orderType === 'sea'" class="h-4 w-4 text-blue-500 mr-2" />
                <PaperAirplaneIcon v-else class="h-4 w-4 text-purple-500 mr-2" />
                <span class="text-sm font-medium text-gray-900">
                  {{ order.orderType === 'sea' ? $t('orders.shipping.sea') : $t('orders.shipping.air') }}
                </span>
              </div>
              <div v-if="order.totalCbm">
                <span class="text-sm text-gray-600">{{ $t('orders.detail.total_cbm') }}:</span>
                <span class="text-sm font-medium text-gray-900 ml-2">{{ order.totalCbm.toFixed(6) }} m³</span>
              </div>
              <div v-if="order.trackingNumber">
                <span class="text-sm text-gray-600">{{ $t('orders.detail.tracking_number') }}:</span>
                <span class="text-sm font-medium text-gray-900 ml-2">{{ order.trackingNumber }}</span>
              </div>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="bg-white shadow rounded-lg p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.order_summary') }}</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">{{ $t('orders.detail.items_count') }}:</span>
                <span class="text-sm font-medium text-gray-900">{{ order.items?.length || 0 }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">{{ $t('orders.detail.boxes_count') }}:</span>
                <span class="text-sm font-medium text-gray-900">{{ order.boxes?.length || 0 }}</span>
              </div>
              <div class="flex justify-between font-medium">
                <span class="text-sm text-gray-900">{{ $t('orders.detail.total_amount') }}:</span>
                <span class="text-sm text-gray-900">{{ formatCurrency(order.totalAmount, order.currency) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="bg-white shadow rounded-lg p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.order_items') }}</h3>
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('orders.detail.item_name') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('orders.detail.quantity') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('orders.detail.unit_price') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('orders.detail.total_price') }}
                  </th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {{ $t('orders.detail.hs_code') }}
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="item in order.items" :key="item.id">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {{ item.productName }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ item.quantity }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatCurrency(item.unitPrice, item.currency) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ formatCurrency(item.amount, item.currency) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {{ item.hsCode || '-' }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Order Boxes -->
        <div class="bg-white shadow rounded-lg p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.shipping_boxes') }}</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="(box, index) in order.boxes"
              :key="box.id"
              class="border border-gray-200 rounded-lg p-4"
            >
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-gray-900">{{ $t('orders.detail.box_number', { number: index + 1 }) }}</h4>
              </div>
              <div class="space-y-1 text-sm">
                <div>
                  <span class="text-gray-600">{{ $t('orders.detail.dimensions') }}:</span>
                  <span class="text-gray-900 ml-1">{{ box.width }}×{{ box.height }}×{{ box.depth }} cm</span>
                </div>
                <div>
                  <span class="text-gray-600">{{ $t('orders.detail.weight') }}:</span>
                  <span class="text-gray-900 ml-1">{{ box.weight }} kg</span>
                </div>
                <div>
                  <span class="text-gray-600">{{ $t('orders.detail.cbm') }}:</span>
                  <span class="text-gray-900 ml-1">{{ box.cbmM3?.toFixed(6) || 0 }} m³</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Estimates -->
        <div v-if="estimates.length > 0" class="bg-white shadow rounded-lg p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.estimates') }}</h3>
          <div class="space-y-4">
            <div
              v-for="estimate in estimates"
              :key="estimate.id"
              class="border border-gray-200 rounded-lg p-4"
            >
              <div class="flex justify-between items-start">
                <div>
                  <h4 class="font-medium text-gray-900">
                    {{ $t('orders.detail.estimate_version', { version: estimate.version }) }}
                  </h4>
                  <p class="text-sm text-gray-600">{{ formatDate(estimate.createdAt) }}</p>
                </div>
                <span
                  class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                  :class="{
                    'bg-yellow-100 text-yellow-800': estimate.status === 'pending',
                    'bg-green-100 text-green-800': estimate.status === 'approved',
                    'bg-red-100 text-red-800': estimate.status === 'rejected'
                  }"
                >
                  {{ $t(`orders.estimate_status.${estimate.status}`) }}
                </span>
              </div>
              <div class="mt-2 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <span class="text-gray-600">{{ $t('orders.detail.shipping_cost') }}:</span>
                  <span class="text-gray-900 ml-1">{{ formatCurrency(estimate.shippingCost, estimate.currency) }}</span>
                </div>
                <div>
                  <span class="text-gray-600">{{ $t('orders.detail.handling_fee') }}:</span>
                  <span class="text-gray-900 ml-1">{{ formatCurrency(estimate.handlingFee, estimate.currency) }}</span>
                </div>
                <div v-if="estimate.repackingFee > 0">
                  <span class="text-gray-600">{{ $t('orders.detail.repacking_fee') }}:</span>
                  <span class="text-gray-900 ml-1">{{ formatCurrency(estimate.repackingFee, estimate.currency) }}</span>
                </div>
                <div>
                  <span class="text-gray-600 font-medium">{{ $t('orders.detail.total_estimate') }}:</span>
                  <span class="text-gray-900 font-medium ml-1">{{ formatCurrency(estimate.totalAmount, estimate.currency) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Status History -->
        <div class="bg-white shadow rounded-lg p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">{{ $t('orders.detail.status_history') }}</h3>
          <div class="flow-root">
            <ul class="-mb-8">
              <li
                v-for="(event, eventIdx) in statusHistory"
                :key="event.id"
                class="relative pb-8"
              >
                <div v-if="eventIdx !== statusHistory.length - 1" class="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200"></div>
                <div class="relative flex space-x-3">
                  <div>
                    <span
                      class="h-8 w-8 rounded-full flex items-center justify-center ring-8 ring-white"
                      :class="{
                        'bg-green-500': event.type === 'status_change' && event.newStatus === 'delivered',
                        'bg-blue-500': event.type === 'status_change' && event.newStatus !== 'delivered',
                        'bg-gray-400': event.type !== 'status_change'
                      }"
                    >
                      <CheckIcon v-if="event.type === 'status_change' && event.newStatus === 'delivered'" class="w-5 h-5 text-white" />
                      <ClockIcon v-else class="w-5 h-5 text-white" />
                    </span>
                  </div>
                  <div class="min-w-0 flex-1">
                    <div>
                      <div class="text-sm">
                        <span class="font-medium text-gray-900">{{ event.title }}</span>
                      </div>
                      <p class="mt-0.5 text-sm text-gray-500">{{ event.description }}</p>
                    </div>
                    <div class="mt-2 text-sm text-gray-700">
                      <time :datetime="event.createdAt">{{ formatDate(event.createdAt) }}</time>
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Cancel Order Modal -->
      <div v-if="showCancelModal" class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
          <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="showCancelModal = false"></div>
          <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>
          <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
            <div>
              <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                <ExclamationTriangleIcon class="h-6 w-6 text-red-600" />
              </div>
              <div class="mt-3 text-center sm:mt-5">
                <h3 class="text-lg leading-6 font-medium text-gray-900">
                  {{ $t('orders.detail.cancel_order_title') }}
                </h3>
                <div class="mt-2">
                  <p class="text-sm text-gray-500">
                    {{ $t('orders.detail.cancel_order_message') }}
                  </p>
                </div>
                <div class="mt-4">
                  <textarea
                    v-model="cancelReason"
                    class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    rows="3"
                    :placeholder="$t('orders.detail.cancel_reason_placeholder')"
                  />
                </div>
              </div>
            </div>
            <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
              <button
                type="button"
                class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 sm:col-start-2 sm:text-sm"
                :disabled="isCancelling"
                @click="confirmCancelOrder"
              >
                {{ isCancelling ? $t('orders.detail.cancelling') : $t('orders.detail.confirm_cancel') }}
              </button>
              <button
                type="button"
                class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:col-start-1 sm:text-sm"
                @click="showCancelModal = false"
              >
                {{ $t('common.cancel') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrdersStore } from '@/stores/orders'
import { useNotificationStore } from '@/stores/notification'
import {
  ArrowLeftIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  ChevronDownIcon,
  TruckIcon,
  PaperAirplaneIcon,
  CheckIcon,
  ClockIcon
} from '@heroicons/vue/24/outline'

// Composables
const route = useRoute()
const router = useRouter()
const ordersStore = useOrdersStore()
const notificationStore = useNotificationStore()

// Props
const orderId = computed(() => route.params.id as string)

// State
const showActionsMenu = ref(false)
const showCancelModal = ref(false)
const cancelReason = ref('')
const isCancelling = ref(false)

// Computed
const order = computed(() => ordersStore.currentOrder)
const orderItems = computed(() => ordersStore.orderItems)
const orderBoxes = computed(() => ordersStore.orderBoxes)
const estimates = computed(() => ordersStore.estimates)
const isLoading = computed(() => ordersStore.isLoading)

const canPerformActions = computed(() => {
  return order.value && ['requested', 'confirmed'].includes(order.value.status)
})

const canRequestEstimate = computed(() => {
  return order.value && order.value.status === 'confirmed' && !estimates.value.length
})

const canCancelOrder = computed(() => {
  return order.value && ['requested', 'confirmed'].includes(order.value.status)
})

const statusHistory = computed(() => {
  if (!order.value) return []
  
  const history = [
    {
      id: 1,
      type: 'status_change',
      title: '주문 생성',
      description: '주문이 생성되었습니다.',
      newStatus: 'pending',
      createdAt: order.value.createdAt
    }
  ]
  
  if (order.value.status !== 'pending') {
    history.push({
      id: 2,
      type: 'status_change',
      title: '주문 승인',
      description: '주문이 승인되었습니다.',
      newStatus: 'approved',
      createdAt: order.value.updatedAt
    })
  }
  
  if (order.value.status === 'delivered') {
    history.push({
      id: 3,
      type: 'status_change',
      title: '배송 완료',
      description: '주문이 성공적으로 배송되었습니다.',
      newStatus: 'delivered',
      createdAt: order.value.updatedAt
    })
  }
  
  return history.reverse()
})

// Methods
const loadOrderDetails = async () => {
  try {
    // For mock implementation, find order from the list
    await ordersStore.fetchOrders()
    const foundOrder = ordersStore.orders.find(o => o.id === orderId.value)
    if (foundOrder) {
      ordersStore.currentOrder = foundOrder
    } else {
      throw new Error('Order not found')
    }
  } catch (error) {
    console.error('Failed to load order details:', error)
    notificationStore.error('오류', '주문 정보를 불러오는데 실패했습니다.')
  }
}

const requestEstimate = async () => {
  try {
    await ordersStore.requestEstimate(orderId.value, 1)
    notificationStore.success('견적 요청', '견적 요청이 완료되었습니다.')
    showActionsMenu.value = false
  } catch (error) {
    notificationStore.error('오류', '견적 요청에 실패했습니다.')
  }
}

const confirmCancelOrder = async () => {
  if (!order.value) return

  isCancelling.value = true
  
  try {
    // Mock cancel order implementation
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Update order status in store
    if (order.value) {
      order.value.status = 'cancelled'
      order.value.updatedAt = new Date().toISOString()
    }
    
    notificationStore.success(
      '주문 취소 완료',
      '주문이 취소되었습니다.'
    )
    
    showCancelModal.value = false
    showActionsMenu.value = false
  } catch (error) {
    notificationStore.error('오류', '주문 취소에 실패했습니다.')
  } finally {
    isCancelling.value = false
  }
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    requested: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-blue-100 text-blue-800',
    in_progress: 'bg-purple-100 text-purple-800',
    shipped: 'bg-indigo-100 text-indigo-800',
    delivered: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatCurrency = (amount: number, currency: string = 'THB') => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2
  }).format(amount)
}

const formatDate = (dateString: string) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(dateString))
}

// Watchers
watch(orderId, (newId) => {
  if (newId) {
    loadOrderDetails()
  }
})

// Lifecycle
onMounted(() => {
  if (orderId.value) {
    loadOrderDetails()
  }
})
</script>

<style scoped>
/* Custom styles */
</style>