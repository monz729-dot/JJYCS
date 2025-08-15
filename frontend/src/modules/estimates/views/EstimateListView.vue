<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">{{ $t('estimates.list.title') }}</h1>
            <p class="mt-1 text-sm text-gray-600">{{ $t('estimates.list.subtitle') }}</p>
          </div>
          <div class="flex space-x-3">
            <button
              type="button"
              class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              @click="refreshEstimates"
              :disabled="isLoading"
            >
              <ArrowPathIcon class="h-4 w-4 mr-2" :class="{ 'animate-spin': isLoading }" />
              {{ $t('common.refresh') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <DocumentTextIcon class="h-6 w-6 text-gray-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">총 견적</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ stats.total }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <DocumentTextIcon class="h-6 w-6 text-yellow-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">승인 대기</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ stats.pending }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <CheckIcon class="h-6 w-6 text-green-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">승인 완료</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ stats.approved }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white overflow-hidden shadow rounded-lg">
          <div class="p-5">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <DocumentTextIcon class="h-6 w-6 text-blue-400" />
              </div>
              <div class="ml-5 w-0 flex-1">
                <dl>
                  <dt class="text-sm font-medium text-gray-500 truncate">총 견적액</dt>
                  <dd class="text-lg font-medium text-gray-900">{{ formatCurrency(stats.totalValue, 'THB') }}</dd>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white shadow rounded-lg mb-6 p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Search -->
          <div>
            <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('estimates.list.search') }}
            </label>
            <div class="relative">
              <input
                id="search"
                v-model="filters.search"
                type="text"
                class="block w-full pl-10 pr-3 py-2 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :placeholder="$t('estimates.list.search_placeholder')"
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
              {{ $t('estimates.list.status') }}
            </label>
            <select
              id="status"
              v-model="filters.status"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('estimates.list.all_statuses') }}</option>
              <option value="draft">{{ $t('estimates.status.draft') }}</option>
              <option value="pending">{{ $t('estimates.status.pending') }}</option>
              <option value="approved">{{ $t('estimates.status.approved') }}</option>
              <option value="revision_requested">{{ $t('estimates.status.revision_requested') }}</option>
              <option value="rejected">{{ $t('estimates.status.rejected') }}</option>
            </select>
          </div>

          <!-- Version Filter -->
          <div>
            <label for="version" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('estimates.list.version') }}
            </label>
            <select
              id="version"
              v-model="filters.version"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('estimates.list.all_versions') }}</option>
              <option value="1">{{ $t('estimates.version.first') }}</option>
              <option value="2">{{ $t('estimates.version.second') }}</option>
            </select>
          </div>

          <!-- Date Range Filter -->
          <div>
            <label for="dateRange" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('estimates.list.date_range') }}
            </label>
            <select
              id="dateRange"
              v-model="filters.dateRange"
              class="block w-full border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              @change="applyFilters"
            >
              <option value="">{{ $t('estimates.list.all_dates') }}</option>
              <option value="today">{{ $t('estimates.list.today') }}</option>
              <option value="week">{{ $t('estimates.list.this_week') }}</option>
              <option value="month">{{ $t('estimates.list.this_month') }}</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Estimates List -->
      <div class="bg-white shadow overflow-hidden sm:rounded-md">
        <!-- Loading State -->
        <div v-if="isLoading" class="flex justify-center items-center py-12">
          <div class="flex items-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span class="text-gray-600">{{ $t('estimates.list.loading') }}</span>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="estimates.length === 0" class="text-center py-12">
          <DocumentTextIcon class="mx-auto h-12 w-12 text-gray-400" />
          <h3 class="mt-2 text-sm font-medium text-gray-900">{{ $t('estimates.list.no_estimates') }}</h3>
          <p class="mt-1 text-sm text-gray-500">{{ $t('estimates.list.no_estimates_description') }}</p>
        </div>

        <!-- Estimates Table -->
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.estimate_info') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.order') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.customer') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.status') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.amount') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.valid_until') }}
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ $t('estimates.list.created') }}
                </th>
                <th scope="col" class="relative px-6 py-3">
                  <span class="sr-only">{{ $t('common.actions') }}</span>
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr
                v-for="estimate in estimates"
                :key="estimate.id"
                class="hover:bg-gray-50 cursor-pointer"
                @click="goToEstimate(estimate.id)"
              >
                <!-- Estimate Info -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div>
                      <div class="text-sm font-medium text-gray-900">
                        {{ estimate.estimateNumber }}
                      </div>
                      <div class="text-sm text-gray-500">
                        {{ $t(`estimates.version.${estimate.version === 1 ? 'first' : 'second'}`) }}
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Order -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{{ estimate.order?.orderCode || '-' }}</div>
                  <div class="text-sm text-gray-500">
                    {{ estimate.order?.orderType ? $t(`orders.shipping.${estimate.order.orderType}`) : '-' }}
                  </div>
                </td>

                <!-- Customer -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{{ estimate.customer?.name || '-' }}</div>
                  <div class="text-sm text-gray-500">{{ estimate.customer?.company || '-' }}</div>
                </td>

                <!-- Status -->
                <td class="px-6 py-4 whitespace-nowrap">
                  <span
                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                    :class="getStatusBadgeClass(estimate.status)"
                  >
                    {{ $t(`estimates.status.${estimate.status}`) }}
                  </span>
                  <div v-if="estimate.isExpired" class="mt-1">
                    <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                      {{ $t('estimates.list.expired') }}
                    </span>
                  </div>
                </td>

                <!-- Amount -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatCurrency(estimate.totalAmount, estimate.currency) }}
                </td>

                <!-- Valid Until -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(estimate.validUntil) }}
                </td>

                <!-- Created -->
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(estimate.createdAt) }}
                </td>

                <!-- Actions -->
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex items-center space-x-2">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      @click.stop="goToEstimate(estimate.id)"
                    >
                      {{ $t('common.view') }}
                    </button>
                    <button
                      v-if="canQuickApprove(estimate)"
                      type="button"
                      class="text-green-600 hover:text-green-900"
                      @click.stop="quickApproveEstimate(estimate)"
                    >
                      {{ $t('estimates.list.quick_approve') }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="bg-white px-4 sm:px-6 py-3 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="flex-1 flex justify-between sm:hidden">
              <button
                type="button"
                class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                :disabled="currentPage === 0"
                @click="prevPage"
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
                  <span class="font-medium">{{ Math.min((currentPage + 1) * pageSize, totalElements) }}</span>
                  {{ $t('common.of') }}
                  <span class="font-medium">{{ totalElements }}</span>
                  {{ $t('common.results') }}
                </p>
              </div>
              <div>
                <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                  <button
                    type="button"
                    class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                    :disabled="currentPage === 0"
                    @click="prevPage"
                  >
                    <ChevronLeftIcon class="h-5 w-5" />
                  </button>
                  
                  <button
                    v-for="page in visiblePages"
                    :key="page"
                    type="button"
                    class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                    :class="page === currentPage ? 'z-10 bg-blue-50 border-blue-500 text-blue-600' : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'"
                    @click="goToPage(page + 1)"
                  >
                    {{ page + 1 }}
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

    <!-- Quick Approve Modal -->
    <div
      v-if="showingQuickApproveModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
      @click="closeQuickApproveModal"
    >
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white" @click.stop>
        <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
          <div>
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
              <CheckIcon class="h-6 w-6 text-green-600" />
            </div>
            <div class="mt-3 text-center sm:mt-5">
              <h3 class="text-lg leading-6 font-medium text-gray-900">
                {{ $t('estimates.detail.approve_estimate') }}
              </h3>
              <div class="mt-2">
                <p class="text-sm text-gray-500">
                  {{ $t('estimates.messages.confirm_approve') }}
                </p>
              </div>
              <div class="mt-4">
                <textarea
                  v-model="approvalNote"
                  class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  rows="3"
                  :placeholder="$t('estimates.detail.approval_note_placeholder')"
                />
              </div>
            </div>
          </div>
          <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
            <button
              type="button"
              class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-green-600 text-base font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 sm:col-start-2 sm:text-sm"
              :disabled="isApproving"
              @click="confirmQuickApprove"
            >
              {{ isApproving ? $t('estimates.detail.approving') : $t('estimates.detail.approve_estimate') }}
            </button>
            <button
              type="button"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:col-start-1 sm:text-sm"
              @click="closeQuickApproveModal"
            >
              {{ $t('common.cancel') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import SpringBootEstimatesService, { type EstimateResponse } from '@/services/estimatesApiService'
import {
  ArrowPathIcon,
  MagnifyingGlassIcon,
  DocumentTextIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  CheckIcon
} from '@heroicons/vue/24/outline'

// Composables
const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// State
const estimates = ref<EstimateResponse[]>([])
const isLoading = ref(false)
const filters = ref({
  search: '',
  status: '',
  version: '',
  dateRange: ''
})

const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(20)

const showingQuickApproveModal = ref(false)
const approvingEstimate = ref<EstimateResponse | null>(null)
const approvalNote = ref('')
const isApproving = ref(false)

const searchTimeout = ref<number | null>(null)

// Computed
const stats = computed(() => {
  return {
    total: totalElements.value,
    pending: estimates.value.filter(e => e.status === 'pending').length,
    approved: estimates.value.filter(e => e.status === 'approved').length,
    totalValue: estimates.value.reduce((sum, e) => sum + (e.totalAmount || 0), 0)
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

// Load estimates from API
const loadEstimates = async () => {
  try {
    isLoading.value = true
    
    const result = await SpringBootEstimatesService.getEstimates({
      page: currentPage.value,
      size: pageSize.value,
      status: filters.value.status || undefined,
      version: filters.value.version ? parseInt(filters.value.version) : undefined,
      search: filters.value.search || undefined
    })
    
    if (result.success && result.data) {
      estimates.value = result.data.content
      totalElements.value = result.data.totalElements
      totalPages.value = result.data.totalPages
    } else {
      console.error('Failed to load estimates:', result.error)
      notificationStore.addNotification({
        type: 'error',
        title: '견적 로드 실패',
        message: result.error || '견적 목록을 불러오는데 실패했습니다.'
      })
    }
  } catch (error) {
    console.error('Load estimates error:', error)
    notificationStore.addNotification({
      type: 'error',
      title: '견적 로드 실패',
      message: '견적 목록을 불러오는데 실패했습니다.'
    })
  } finally {
    isLoading.value = false
  }
}

// Methods
const refreshEstimates = async () => {
  await loadEstimates()
}

const debounceSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = window.setTimeout(() => {
    currentPage.value = 0
    loadEstimates()
  }, 500)
}

const applyFilters = () => {
  currentPage.value = 0
  loadEstimates()
}

const goToEstimate = (estimateId: number) => {
  router.push({ name: 'EstimateDetail', params: { id: estimateId } })
}

const quickApproveEstimate = (estimate: EstimateResponse) => {
  approvingEstimate.value = estimate
  approvalNote.value = ''
  showingQuickApproveModal.value = true
}

const closeQuickApproveModal = () => {
  showingQuickApproveModal.value = false
  approvingEstimate.value = null
  approvalNote.value = ''
}

const confirmQuickApprove = async () => {
  if (!approvingEstimate.value) return
  
  isApproving.value = true
  
  try {
    const result = await SpringBootEstimatesService.approveEstimate({
      estimateId: approvingEstimate.value.id,
      note: approvalNote.value
    })
    
    if (result.success) {
      notificationStore.addNotification({
        type: 'success',
        title: '견적 승인 완료',
        message: '견적이 성공적으로 승인되었습니다.'
      })
      
      closeQuickApproveModal()
      await loadEstimates()
    } else {
      notificationStore.addNotification({
        type: 'error',
        title: '견적 승인 실패',
        message: result.error || '견적 승인 중 오류가 발생했습니다.'
      })
    }
  } catch (error) {
    console.error('Approve estimate error:', error)
    notificationStore.addNotification({
      type: 'error',
      title: '견적 승인 실패',
      message: '견적 승인 중 오류가 발생했습니다.'
    })
  } finally {
    isApproving.value = false
  }
}

// Pagination
const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadEstimates()
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    loadEstimates()
  }
}

const goToPage = (page: number) => {
  currentPage.value = page - 1 // Convert to 0-based index
  loadEstimates()
}

// Helper functions
const formatCurrency = (amount: number, currency: string): string => {
  const currencySymbols = {
    KRW: '₩',
    THB: '฿',
    USD: '$'
  }
  
  return `${currencySymbols[currency as keyof typeof currencySymbols] || currency} ${amount.toLocaleString()}`
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const getStatusBadgeClass = (status: string): string => {
  const classes = {
    draft: 'bg-gray-100 text-gray-800',
    pending: 'bg-yellow-100 text-yellow-800',
    approved: 'bg-green-100 text-green-800',
    revision_requested: 'bg-orange-100 text-orange-800',
    rejected: 'bg-red-100 text-red-800',
    expired: 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const canQuickApprove = (estimate: EstimateResponse): boolean => {
  return estimate.status === 'pending' && !estimate.isExpired
}

// Watchers - reload when filters change  
watch([() => filters.value.status, () => filters.value.version, () => filters.value.dateRange], () => {
  currentPage.value = 0
  loadEstimates()
})

// Lifecycle
onMounted(() => {
  loadEstimates()
})
</script>

<style scoped>
/* Custom styles */
</style>