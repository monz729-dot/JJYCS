<template>
  <div class="min-h-screen bg-gray-50 py-4 sm:py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-6 sm:mb-8">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-gray-900">{{ $t('orders.list.title') }}</h1>
            <p class="mt-1 text-sm text-gray-600">{{ $t('orders.list.subtitle') }}</p>
          </div>
          <div class="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-3">
            <button
              type="button"
              class="inline-flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              @click="refreshOrders"
              :disabled="isLoading"
            >
              <ArrowPathIcon class="h-4 w-4 mr-2" :class="{ 'animate-spin': isLoading }" />
              {{ $t('common.refresh') }}
            </button>
            <router-link
              :to="{ name: 'OrderCreate' }"
              class="inline-flex items-center justify-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
            >
              <PlusIcon class="h-4 w-4 mr-2" />
              {{ $t('orders.list.create_order') }}
            </router-link>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white shadow rounded-lg mb-6 p-4 sm:p-6">
        <!-- Mobile Filter Toggle -->
        <div class="sm:hidden mb-4">
          <button
            @click="showMobileFilters = !showMobileFilters"
            class="w-full flex items-center justify-between px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
          >
            <span>{{ $t('orders.list.filters') }}</span>
            <ChevronDownIcon class="h-4 w-4" :class="{ 'rotate-180': showMobileFilters }" />
          </button>
        </div>

        <!-- Filter Grid -->
        <div 
          class="grid grid-cols-1 gap-4"
          :class="{
            'sm:grid-cols-2 lg:grid-cols-4': true,
            'hidden sm:grid': !showMobileFilters
          }"
        >
          <!-- Search -->
          <div>
            <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('orders.list.search') }}
            </label>
            <div class="relative">
              <input
                id="search"
                v-model="filters.search"
                type="text"
                class="block w-full pl-10 pr-3 py-2 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :placeholder="$t('orders.list.search_placeholder')"
                @input="debounceSearch"
              />
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
              </div>
            </div>
          </div>

          <!-- Status Filter -->
          <div>
            <label for="status" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('orders.list.status') }}
            </label>
            <select
              id="status"
              v-model="filters.status"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('orders.list.all_statuses') }}</option>
              <option value="requested">{{ $t('orders.status.requested') }}</option>
              <option value="confirmed">{{ $t('orders.status.confirmed') }}</option>
              <option value="in_progress">{{ $t('orders.status.in_progress') }}</option>
              <option value="shipped">{{ $t('orders.status.shipped') }}</option>
              <option value="delivered">{{ $t('orders.status.delivered') }}</option>
              <option value="cancelled">{{ $t('orders.status.cancelled') }}</option>
            </select>
          </div>

          <!-- Order Type Filter -->
          <div>
            <label for="orderType" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('orders.list.shipping_type') }}
            </label>
            <select
              id="orderType"
              v-model="filters.orderType"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('orders.list.all_types') }}</option>
              <option value="sea">{{ $t('orders.shipping.sea') }}</option>
              <option value="air">{{ $t('orders.shipping.air') }}</option>
            </select>
          </div>

          <!-- Date Range -->
          <div>
            <label for="dateRange" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('orders.list.date_range') }}
            </label>
            <select
              id="dateRange"
              v-model="filters.dateRange"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('orders.list.all_dates') }}</option>
              <option value="today">{{ $t('orders.list.today') }}</option>
              <option value="week">{{ $t('orders.list.this_week') }}</option>
              <option value="month">{{ $t('orders.list.this_month') }}</option>
              <option value="quarter">{{ $t('orders.list.this_quarter') }}</option>
            </select>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="mt-6 grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
          <div class="text-center p-3 bg-blue-50 rounded-lg">
            <div class="text-xl sm:text-2xl font-bold text-blue-600">{{ stats.total }}</div>
            <div class="text-xs sm:text-sm text-gray-600">{{ $t('orders.list.total_orders') }}</div>
          </div>
          <div class="text-center p-3 bg-yellow-50 rounded-lg">
            <div class="text-xl sm:text-2xl font-bold text-yellow-600">{{ stats.pending }}</div>
            <div class="text-xs sm:text-sm text-gray-600">{{ $t('orders.list.pending_orders') }}</div>
          </div>
          <div class="text-center p-3 bg-green-50 rounded-lg">
            <div class="text-xl sm:text-2xl font-bold text-green-600">{{ stats.delivered }}</div>
            <div class="text-xs sm:text-sm text-gray-600">{{ $t('orders.list.delivered_orders') }}</div>
          </div>
          <div class="text-center p-3 bg-purple-50 rounded-lg">
            <div class="text-lg sm:text-2xl font-bold text-purple-600">{{ formatCurrency(stats.totalValue) }}</div>
            <div class="text-xs sm:text-sm text-gray-600">{{ $t('orders.list.total_value') }}</div>
          </div>
        </div>
      </div>

      <!-- Orders List -->
      <div class="bg-white shadow rounded-lg overflow-hidden">
        <!-- Loading State -->
        <div v-if="isLoading && orders.length === 0" class="flex justify-center items-center py-12">
          <div class="flex items-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="text-gray-600">{{ $t('orders.list.loading') }}</span>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="!isLoading && orders.length === 0" class="text-center py-12">
          <InboxIcon class="mx-auto h-12 w-12 text-gray-400" />
          <h3 class="mt-2 text-sm font-medium text-gray-900">{{ $t('orders.list.no_orders') }}</h3>
          <p class="mt-1 text-sm text-gray-500">{{ $t('orders.list.no_orders_description') }}</p>
          <div class="mt-6">
            <router-link
              :to="{ name: 'OrderCreate' }"
              class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
            >
              <PlusIcon class="h-4 w-4 mr-2" />
              {{ $t('orders.list.create_first_order') }}
            </router-link>
          </div>
        </div>

        <!-- Desktop Table -->
        <div v-else class="hidden lg:block overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.order_info') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.recipient') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.shipping') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.status') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.amount') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('orders.list.created') }}
                </th>
                <th scope="col" class="relative px-6 py-3">
                  <span class="sr-only">{{ $t('common.actions') }}</span>
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr
                v-for="order in orders"
                :key="order.id"
                class="hover:bg-gray-50 cursor-pointer"
                @click="goToOrder(order.id)"
              >
                <!-- Order Info -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div>
                      <div class="text-sm font-medium text-gray-900">
                        {{ order.orderCode }}
                      </div>
                      <div class="text-sm text-gray-500">
                        {{ $t('orders.list.items_count', { count: order.items?.length || 0 }) }}
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Recipient -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{{ order.recipientName }}</div>
                  <div class="text-sm text-gray-500">{{ order.recipientCountry }}</div>
                </td>

                <!-- Shipping -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <TruckIcon v-if="order.orderType === 'sea'" class="h-4 w-4 text-blue-500 mr-2" />
                    <PaperAirplaneIcon v-else class="h-4 w-4 text-purple-500 mr-2" />
                    <div>
                      <div class="text-sm text-gray-900">
                        {{ order.orderType === 'sea' ? $t('orders.shipping.sea') : $t('orders.shipping.air') }}
                      </div>
                      <div v-if="order.cbm" class="text-sm text-gray-500">
                        {{ order.cbm.toFixed(3) }} m³
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Status -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                    :class="getStatusBadgeClass(order.status)"
                  >
                    {{ $t(`orders.status.${order.status}`) }}
                  </span>
                  <div v-if="order.requiresExtraRecipientInfo" class="mt-1">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                      {{ $t('orders.list.extra_info_required') }}
                    </span>
                  </div>
                </td>

                <!-- Amount -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatCurrency(order.totalAmount, order.currency) }}
                </td>

                <!-- Created -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(order.createdAt) }}
                </td>

                <!-- Actions -->
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex items-center space-x-2">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      @click.stop="goToOrder(order.id)"
                    >
                      {{ $t('common.view') }}
                    </button>
                    <button
                      v-if="canCancelOrder(order)"
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      @click.stop="showCancelModal(order)"
                    >
                      {{ $t('common.cancel') }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Mobile Cards -->
        <div v-else class="lg:hidden">
          <div class="divide-y divide-gray-200">
            <div
              v-for="order in orders"
              :key="order.id"
              class="p-4 hover:bg-gray-50 cursor-pointer"
              @click="goToOrder(order.id)"
            >
              <!-- Order Header -->
              <div class="flex items-start justify-between mb-3">
                <div class="flex-1">
                  <div class="flex items-center space-x-2">
                    <h3 class="text-lg font-semibold text-gray-900">{{ order.orderCode }}</h3>
                    <span
                      class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                      :class="getStatusBadgeClass(order.status)"
                    >
                      {{ $t(`orders.status.${order.status}`) }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500 mt-1">
                    {{ $t('orders.list.items_count', { count: order.items?.length || 0 }) }}
                  </p>
                </div>
                <div class="text-right">
                  <div class="text-lg font-semibold text-gray-900">
                    {{ formatCurrency(order.totalAmount, order.currency) }}
                  </div>
                  <div class="text-sm text-gray-500">
                    {{ formatDate(order.createdAt) }}
                  </div>
                </div>
              </div>

              <!-- Order Details -->
              <div class="space-y-2">
                <!-- Recipient -->
                <div class="flex items-center space-x-2">
                  <UserIcon class="h-4 w-4 text-gray-400" />
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ order.recipientName }}</div>
                    <div class="text-sm text-gray-500">{{ order.recipientCountry }}</div>
                  </div>
                </div>

                <!-- Shipping -->
                <div class="flex items-center space-x-2">
                  <TruckIcon v-if="order.orderType === 'sea'" class="h-4 w-4 text-blue-500" />
                  <PaperAirplaneIcon v-else class="h-4 w-4 text-purple-500" />
                  <div>
                    <div class="text-sm font-medium text-gray-900">
                      {{ order.orderType === 'sea' ? $t('orders.shipping.sea') : $t('orders.shipping.air') }}
                    </div>
                    <div v-if="order.cbm" class="text-sm text-gray-500">
                      {{ order.cbm.toFixed(3) }} m³
                    </div>
                  </div>
                </div>

                <!-- Extra Info Required -->
                <div v-if="order.requiresExtraRecipientInfo" class="flex items-center space-x-2">
                  <ExclamationTriangleIcon class="h-4 w-4 text-yellow-500" />
                  <span class="text-sm text-yellow-700">{{ $t('orders.list.extra_info_required') }}</span>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex items-center justify-end space-x-3 mt-4 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  class="text-blue-600 hover:text-blue-900 text-sm font-medium"
                  @click.stop="goToOrder(order.id)"
                >
                  {{ $t('common.view') }}
                </button>
                <button
                  v-if="canCancelOrder(order)"
                  type="button"
                  class="text-red-600 hover:text-red-900 text-sm font-medium"
                  @click.stop="showCancelModal(order)"
                >
                  {{ $t('common.cancel') }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="bg-white px-4 sm:px-6 py-3 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="flex-1 flex justify-between sm:hidden">
              <button
                type="button"
                class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                :disabled="currentPage === 0"
                @click="previousPage"
              >
                {{ $t('common.previous') }}
              </button>
              <button
                type="button"
                class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                :disabled="currentPage >= totalPages - 1"
                @click="nextPage"
              >
                {{ $t('common.next') }}
              </button>
            </div>
            <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p class="text-sm text-gray-700">
                  {{ $t('common.showing') }}
                  <span class="font-medium">{{ (currentPage * pageSize) + 1 }}</span>
                  {{ $t('common.to') }}
                  <span class="font-medium">{{ Math.min((currentPage + 1) * pageSize, totalItems) }}</span>
                  {{ $t('common.of') }}
                  <span class="font-medium">{{ totalItems }}</span>
                  {{ $t('common.results') }}
                </p>
              </div>
              <div>
                <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    type="button"
                    class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    :disabled="currentPage === 0"
                    @click="previousPage"
                  >
                    <span class="sr-only">{{ $t('common.previous') }}</span>
                    <ChevronLeftIcon class="h-5 w-5" />
                  </button>
                  <button
                    v-for="page in visiblePages"
                    :key="page"
                    type="button"
                    class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    :class="page === currentPage ? 'z-10 bg-blue-50 border-blue-500 text-blue-600' : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'"
                    @click="goToPage(page)"
                  >
                    {{ page + 1 }}
                  </button>
                  <button
                    type="button"
                    class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    :disabled="currentPage >= totalPages - 1"
                    @click="nextPage"
                  >
                    <span class="sr-only">{{ $t('common.next') }}</span>
                    <ChevronRightIcon class="h-5 w-5" />
                  </button>
                </nav>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Cancel Order Modal -->
    <CancelOrderModal
      v-if="showCancelModalFlag"
      :order="selectedOrder"
      @close="closeCancelModal"
      @confirm="cancelOrder"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowPathIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  InboxIcon,
  TruckIcon,
  PaperAirplaneIcon,
  ChevronDownIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  UserIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'
import CancelOrderModal from '../components/CancelOrderModal.vue'
import { useOrderStore } from '../stores/orderStore'
import { useAuthStore } from '@/stores/auth'
import { formatCurrency, formatDate } from '@/utils/helpers'

// Composables
const router = useRouter()
const { t } = useI18n()
const ordersStore = useOrderStore()
const authStore = useAuthStore()

// State
const showMobileFilters = ref(false)
const filters = ref({
  search: '',
  status: '',
  orderType: '',
  dateRange: ''
})

const showCancelModalFlag = ref(false)
const selectedOrder = ref(null)
const cancelReason = ref('')
const isCancelling = ref(false)
const searchTimeout = ref(null)

// Computed
const orders = computed(() => ordersStore.orders)
const isLoading = computed(() => ordersStore.isLoading)
const currentPage = computed(() => ordersStore.currentPage)
const totalPages = computed(() => ordersStore.totalPages)
const totalItems = computed(() => ordersStore.totalElements)
const pageSize = computed(() => ordersStore.pageSize)

const stats = computed(() => {
  return {
    total: totalItems.value,
    pending: orders.value.filter(o => o.status === 'requested').length,
    delivered: orders.value.filter(o => o.status === 'delivered').length,
    totalValue: orders.value.reduce((sum, o) => sum + (o.totalAmount || 0), 0)
  }
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// Methods
const loadOrders = async () => {
  try {
    await ordersStore.fetchOrders({
      page: currentPage.value,
      size: pageSize.value,
      ...filters.value
    })
  } catch (error) {
    console.error('Failed to load orders:', error)
  }
}

const refreshOrders = () => {
  loadOrders()
}

const debounceSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = setTimeout(() => {
    applyFilters()
  }, 500)
}

const applyFilters = () => {
  ordersStore.setPage(0) // Reset to first page
  loadOrders()
}

const goToOrder = (orderId: string) => {
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

const goToPage = (page: number) => {
  ordersStore.setPage(page)
  loadOrders()
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    ordersStore.nextPage()
    loadOrders()
  }
}

const previousPage = () => {
  if (currentPage.value > 0) {
    ordersStore.prevPage()
    loadOrders()
  }
}

const canCancelOrder = (order: any) => {
  return ['requested', 'confirmed'].includes(order.status)
}

const showCancelModal = (order: any) => {
  selectedOrder.value = order
  cancelReason.value = ''
  showCancelModalFlag.value = true
}

const closeCancelModal = () => {
  showCancelModalFlag.value = false
  selectedOrder.value = null
  cancelReason.value = ''
}

const cancelOrder = async () => {
  if (!selectedOrder.value || !cancelReason.value.trim()) return
  
  isCancelling.value = true
  try {
    await ordersStore.cancelOrder(selectedOrder.value.id, cancelReason.value)
    closeCancelModal()
    loadOrders()
  } catch (error) {
    console.error('Failed to cancel order:', error)
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

// Lifecycle
onMounted(() => {
  loadOrders()
})

// Watchers
watch(filters, () => {
  applyFilters()
}, { deep: true })
</script>

<style scoped>
/* Custom styles */
</style>