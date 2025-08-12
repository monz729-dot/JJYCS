<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">{{ $t('tracking.title') }}</h1>
        <p class="mt-1 text-sm text-gray-600">{{ $t('tracking.subtitle') }}</p>
      </div>

      <!-- Quick Search -->
      <div class="bg-white shadow rounded-lg p-6 mb-6">
        <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('tracking.quick_search') }}</h2>
        <div class="flex space-x-4">
          <div class="flex-1">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="$t('tracking.search_placeholder')"
              class="block w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
              @keyup.enter="searchTracking"
            />
          </div>
          <button
            type="button"
            class="inline-flex items-center px-6 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            :disabled="isLoading || !searchQuery.trim()"
            @click="searchTracking"
          >
            <MagnifyingGlassIcon v-if="!isLoading" class="h-4 w-4 mr-2" />
            <div v-else class="animate-spin h-4 w-4 mr-2 border-2 border-white border-t-transparent rounded-full"></div>
            {{ $t('common.search') }}
          </button>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <TruckIcon class="h-8 w-8 text-blue-600" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">{{ $t('tracking.stats.total') }}</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ trackingStats.total }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <ArrowPathIcon class="h-8 w-8 text-orange-600" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">{{ $t('tracking.stats.in_transit') }}</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ trackingStats.inTransit }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <CheckCircleIcon class="h-8 w-8 text-green-600" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">{{ $t('tracking.stats.delivered') }}</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ trackingStats.delivered }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <ExclamationTriangleIcon class="h-8 w-8 text-red-600" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">{{ $t('tracking.stats.exceptions') }}</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ trackingStats.exceptions }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white shadow rounded-lg p-6 mb-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Status Filter -->
          <div>
            <label for="status" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('tracking.filters.status') }}
            </label>
            <select
              id="status"
              v-model="selectedStatus"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('tracking.filters.all_statuses') }}</option>
              <option value="created">{{ $t('tracking.status.created') }}</option>
              <option value="picked_up">{{ $t('tracking.status.picked_up') }}</option>
              <option value="in_transit">{{ $t('tracking.status.in_transit') }}</option>
              <option value="customs_processing">{{ $t('tracking.status.customs_processing') }}</option>
              <option value="customs_cleared">{{ $t('tracking.status.customs_cleared') }}</option>
              <option value="out_for_delivery">{{ $t('tracking.status.out_for_delivery') }}</option>
              <option value="delivered">{{ $t('tracking.status.delivered') }}</option>
              <option value="exception">{{ $t('tracking.status.exception') }}</option>
            </select>
          </div>

          <!-- Shipping Method Filter -->
          <div>
            <label for="shippingMethod" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('tracking.filters.shipping_method') }}
            </label>
            <select
              id="shippingMethod"
              v-model="selectedShippingMethod"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('tracking.filters.all_methods') }}</option>
              <option value="sea">{{ $t('orders.shipping.sea') }}</option>
              <option value="air">{{ $t('orders.shipping.air') }}</option>
            </select>
          </div>

          <!-- Search Filter -->
          <div>
            <label for="listSearch" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('tracking.filters.search') }}
            </label>
            <input
              id="listSearch"
              v-model="listSearchQuery"
              type="text"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              :placeholder="$t('tracking.filters.search_placeholder')"
              @input="debounceSearch"
            />
          </div>

          <!-- Action Buttons -->
          <div class="flex items-end space-x-2">
            <button
              type="button"
              class="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              @click="clearFilters"
            >
              {{ $t('common.clear') }}
            </button>
            <button
              type="button"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              @click="refreshTrackingList"
              :disabled="isLoading"
            >
              <ArrowPathIcon class="h-4 w-4 mr-2" :class="{ 'animate-spin': isLoading }" />
              {{ $t('common.refresh') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Tracking List -->
      <div class="bg-white shadow rounded-lg overflow-hidden">
        <!-- Loading State -->
        <div v-if="isLoading && trackingList.length === 0" class="flex justify-center items-center py-12">
          <div class="flex items-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="text-gray-600">{{ $t('tracking.loading') }}</span>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="!isLoading && trackingList.length === 0" class="text-center py-12">
          <TruckIcon class="mx-auto h-12 w-12 text-gray-400" />
          <h3 class="mt-2 text-sm font-medium text-gray-900">{{ $t('tracking.no_tracking') }}</h3>
          <p class="mt-1 text-sm text-gray-500">{{ $t('tracking.no_tracking_description') }}</p>
        </div>

        <!-- Tracking Table -->
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('tracking.table.order_info') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('tracking.table.tracking_number') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('tracking.table.status') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('tracking.table.destination') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('tracking.table.estimated_delivery') }}
                </th>
                <th scope="col" class="relative px-6 py-3">
                  <span class="sr-only">{{ $t('common.actions') }}</span>
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr
                v-for="tracking in trackingList"
                :key="tracking.id"
                class="hover:bg-gray-50 cursor-pointer"
                @click="goToTrackingDetail(tracking.trackingNumber!)"
              >
                <!-- Order Info -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div>
                      <div class="text-sm font-medium text-gray-900">
                        {{ tracking.orderCode }}
                      </div>
                      <div class="text-sm text-gray-500">
                        {{ tracking.recipient.name }}
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Tracking Number -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900 font-mono">{{ tracking.trackingNumber }}</div>
                  <div class="text-sm text-gray-500">{{ tracking.carrierName }}</div>
                </td>

                <!-- Status -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                    :class="getStatusBadgeClass(tracking.status)"
                  >
                    {{ $t(`tracking.status.${tracking.status}`) }}
                  </span>
                  <div class="flex items-center mt-1">
                    <TruckIcon v-if="tracking.shippingMethod === 'sea'" class="h-3 w-3 text-blue-500 mr-1" />
                    <PaperAirplaneIcon v-else class="h-3 w-3 text-purple-500 mr-1" />
                    <span class="text-xs text-gray-500">
                      {{ tracking.shippingMethod === 'sea' ? $t('orders.shipping.sea') : $t('orders.shipping.air') }}
                    </span>
                  </div>
                </td>

                <!-- Destination -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ tracking.destination }}
                </td>

                <!-- Estimated Delivery -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ tracking.estimatedDelivery ? formatDate(tracking.estimatedDelivery) : '-' }}
                </td>

                <!-- Actions -->
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    type="button"
                    class="text-blue-600 hover:text-blue-900"
                    @click.stop="goToTrackingDetail(tracking.trackingNumber!)"
                  >
                    {{ $t('tracking.table.view_details') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="bg-white px-6 py-3 border-t border-gray-200">
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
                  {{ $t('tracking.showing_results', {
                    from: currentPage * pageSize + 1,
                    to: Math.min((currentPage + 1) * pageSize, totalElements),
                    total: totalElements
                  }) }}
                </p>
              </div>
              <div>
                <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                  <button
                    type="button"
                    class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    :disabled="currentPage === 0"
                    @click="previousPage"
                  >
                    <ChevronLeftIcon class="h-5 w-5" />
                  </button>
                  <button
                    v-for="page in visiblePages"
                    :key="page"
                    type="button"
                    class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    :class="{
                      'z-10 bg-blue-50 border-blue-500 text-blue-600': page - 1 === currentPage,
                      'bg-white border-gray-300 text-gray-500 hover:bg-gray-50': page - 1 !== currentPage
                    }"
                    @click="goToPage(page - 1)"
                  >
                    {{ page }}
                  </button>
                  <button
                    type="button"
                    class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    :disabled="currentPage >= totalPages - 1"
                    @click="nextPage"
                  >
                    <ChevronRightIcon class="h-5 w-5" />
                  </button>
                </nav>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTrackingStore } from '@/stores/tracking'
import { useNotificationStore } from '@/stores/notification'
import {
  MagnifyingGlassIcon,
  TruckIcon,
  PaperAirplaneIcon,
  ArrowPathIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const trackingStore = useTrackingStore()
const notificationStore = useNotificationStore()

// State
const searchQuery = ref('')
const listSearchQuery = ref('')
const selectedStatus = ref('')
const selectedShippingMethod = ref('')
const searchTimeout = ref<NodeJS.Timeout | null>(null)

// Computed
const trackingList = computed(() => trackingStore.trackingList)
const isLoading = computed(() => trackingStore.isLoading)
const trackingStats = computed(() => trackingStore.trackingStats)
const currentPage = computed(() => trackingStore.currentPage)
const totalPages = computed(() => trackingStore.totalPages)
const totalElements = computed(() => trackingStore.totalElements)
const pageSize = computed(() => trackingStore.pageSize)

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// Methods
const loadTrackingList = async () => {
  try {
    await trackingStore.fetchTrackingList({
      page: currentPage.value,
      size: pageSize.value,
      filters: {
        status: selectedStatus.value ? [selectedStatus.value as any] : undefined,
        shippingMethod: selectedShippingMethod.value ? [selectedShippingMethod.value as any] : undefined,
        search: listSearchQuery.value
      }
    })
  } catch (error) {
    notificationStore.error('오류', '배송 추적 목록을 불러오는데 실패했습니다.')
  }
}

const searchTracking = async () => {
  if (!searchQuery.value.trim()) {
    notificationStore.warning('경고', '검색어를 입력해주세요.')
    return
  }
  
  try {
    const result = await trackingStore.searchTracking({
      trackingNumber: searchQuery.value.trim(),
      orderCode: searchQuery.value.trim()
    })
    
    if (result.success && result.data) {
      if (Array.isArray(result.data)) {
        notificationStore.success('검색 완료', `${result.data.length}개의 결과를 찾았습니다.`)
        // Update filters to show search results
        listSearchQuery.value = searchQuery.value
        applyFilters()
      } else {
        // Single tracking result, navigate to detail
        router.push(`/tracking/${result.data.trackingNumber}`)
      }
    } else {
      notificationStore.warning('검색 결과 없음', '해당하는 배송 정보를 찾을 수 없습니다.')
    }
  } catch (error: any) {
    notificationStore.error('검색 실패', error.message || '검색 중 오류가 발생했습니다.')
  }
}

const refreshTrackingList = () => {
  loadTrackingList()
}

const applyFilters = () => {
  trackingStore.setPage(0) // Reset to first page
  loadTrackingList()
}

const clearFilters = () => {
  selectedStatus.value = ''
  selectedShippingMethod.value = ''
  listSearchQuery.value = ''
  trackingStore.clearFilters()
  loadTrackingList()
}

const debounceSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = setTimeout(() => {
    applyFilters()
  }, 500)
}

const goToTrackingDetail = (trackingNumber: string) => {
  router.push(`/tracking/${trackingNumber}`)
}

const goToPage = (page: number) => {
  trackingStore.setPage(page)
  loadTrackingList()
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    trackingStore.nextPage()
    loadTrackingList()
  }
}

const previousPage = () => {
  if (currentPage.value > 0) {
    trackingStore.prevPage()
    loadTrackingList()
  }
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    created: 'bg-gray-100 text-gray-800',
    picked_up: 'bg-blue-100 text-blue-800',
    in_transit: 'bg-orange-100 text-orange-800',
    customs_processing: 'bg-purple-100 text-purple-800',
    customs_cleared: 'bg-indigo-100 text-indigo-800',
    out_for_delivery: 'bg-yellow-100 text-yellow-800',
    delivered: 'bg-green-100 text-green-800',
    exception: 'bg-red-100 text-red-800',
    returned: 'bg-gray-100 text-gray-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatDate = (dateString: string) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(new Date(dateString))
}

// Watchers
watch(() => trackingStore.filters, (newFilters) => {
  selectedStatus.value = newFilters.status?.[0] || ''
  selectedShippingMethod.value = newFilters.shippingMethod?.[0] || ''
  listSearchQuery.value = newFilters.search || ''
}, { deep: true })

// Lifecycle
onMounted(() => {
  loadTrackingList()
})
</script>

<style scoped>
/* Custom styles */
</style>