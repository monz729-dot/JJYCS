import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import estimatesApi, { 
  type EstimateResponse, 
  type EstimateRequest, 
  type EstimateListParams,
  type EstimateCalculationResponse,
  type EstimateResponseRequest 
} from '@/services/estimatesApi'
import { useNotificationStore } from '@/stores/notification'

export const useEstimateStore = defineStore('estimate', () => {
  // State
  const estimates = ref<EstimateResponse[]>([])
  const currentEstimate = ref<EstimateResponse | null>(null)
  const isLoading = ref(false)
  const isCreating = ref(false)
  const isUpdating = ref(false)
  const isDeleting = ref(false)
  const error = ref<string | null>(null)

  // Pagination
  const currentPage = ref(0)
  const pageSize = ref(20)
  const totalPages = ref(0)
  const totalElements = ref(0)

  // Filters
  const filters = ref<EstimateListParams>({
    search: '',
    status: '',
    version: undefined,
    dateRange: ''
  })

  // Calculation
  const calculationResult = ref<EstimateCalculationResponse | null>(null)
  const isCalculating = ref(false)

  // Statistics
  const stats = ref({
    total: 0,
    pending: 0,
    approved: 0,
    rejected: 0,
    expired: 0,
    totalValue: 0,
    averageValue: 0
  })

  // Templates and pricing
  const templates = ref([])
  const pricingRules = ref(null)

  // Getters
  const estimateById = computed(() => (id: number) => 
    estimates.value.find(estimate => estimate.id === id)
  )

  const pendingEstimates = computed(() => 
    estimates.value.filter(estimate => estimate.status === 'pending')
  )

  const approvedEstimates = computed(() => 
    estimates.value.filter(estimate => estimate.status === 'approved')
  )

  const expiredEstimates = computed(() => 
    estimates.value.filter(estimate => estimate.isExpired)
  )

  const estimatesByStatus = computed(() => (status: string) => 
    estimates.value.filter(estimate => estimate.status === status)
  )

  const estimatesByVersion = computed(() => (version: number) => 
    estimates.value.filter(estimate => estimate.version === version)
  )

  const hasNextPage = computed(() => currentPage.value < totalPages.value - 1)
  const hasPrevPage = computed(() => currentPage.value > 0)

  // Actions
  const fetchEstimates = async (params: EstimateListParams = {}) => {
    isLoading.value = true
    error.value = null

    try {
      const requestParams = {
        ...filters.value,
        ...params,
        page: currentPage.value,
        size: pageSize.value
      }

      const response = await estimatesApi.getEstimates(requestParams)

      if (response.success && response.data) {
        estimates.value = response.data.content
        totalElements.value = response.data.totalElements
        totalPages.value = response.data.totalPages
        currentPage.value = response.data.currentPage
      } else {
        throw new Error(response.message || 'Failed to fetch estimates')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적 목록을 불러오는데 실패했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  const fetchOrderEstimates = async (orderId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await estimatesApi.getOrderEstimates(orderId)

      if (response.success && response.data) {
        estimates.value = response.data
      } else {
        throw new Error(response.message || 'Failed to fetch order estimates')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '주문 견적을 불러오는데 실패했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  const fetchEstimate = async (estimateId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await estimatesApi.getEstimate(estimateId)

      if (response.success && response.data) {
        currentEstimate.value = response.data
        return response.data
      } else {
        throw new Error(response.message || 'Failed to fetch estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서를 불러오는데 실패했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  const createEstimate = async (estimateData: EstimateRequest) => {
    isCreating.value = true
    error.value = null

    try {
      const response = await estimatesApi.createEstimate(estimateData)

      if (response.success && response.data) {
        estimates.value.unshift(response.data)
        totalElements.value += 1
        
        const notificationStore = useNotificationStore()
        notificationStore.success('견적 생성 완료', '견적서가 성공적으로 생성되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to create estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서 생성에 실패했습니다.')
      throw err
    } finally {
      isCreating.value = false
    }
  }

  const updateEstimate = async (estimateId: number, estimateData: EstimateRequest) => {
    isUpdating.value = true
    error.value = null

    try {
      const response = await estimatesApi.updateEstimate(estimateId, estimateData)

      if (response.success && response.data) {
        const index = estimates.value.findIndex(e => e.id === estimateId)
        if (index !== -1) {
          estimates.value[index] = response.data
        }
        
        if (currentEstimate.value?.id === estimateId) {
          currentEstimate.value = response.data
        }
        
        const notificationStore = useNotificationStore()
        notificationStore.success('견적 수정 완료', '견적서가 성공적으로 수정되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to update estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서 수정에 실패했습니다.')
      throw err
    } finally {
      isUpdating.value = false
    }
  }

  const respondToEstimate = async (estimateId: number, responseData: EstimateResponseRequest) => {
    isUpdating.value = true
    error.value = null

    try {
      const response = await estimatesApi.respondToEstimate(estimateId, responseData)

      if (response.success && response.data) {
        const index = estimates.value.findIndex(e => e.id === estimateId)
        if (index !== -1) {
          estimates.value[index] = response.data
        }
        
        if (currentEstimate.value?.id === estimateId) {
          currentEstimate.value = response.data
        }
        
        const notificationStore = useNotificationStore()
        const actionMessages = {
          approve: '견적서를 승인했습니다.',
          reject: '견적서를 거부했습니다.',
          request_revision: '수정을 요청했습니다.'
        }
        
        notificationStore.success('처리 완료', actionMessages[responseData.action])
        return response.data
      } else {
        throw new Error(response.message || 'Failed to respond to estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서 처리에 실패했습니다.')
      throw err
    } finally {
      isUpdating.value = false
    }
  }

  const createSecondEstimate = async (baseEstimateId: number, estimateData: EstimateRequest) => {
    isCreating.value = true
    error.value = null

    try {
      const response = await estimatesApi.createSecondEstimate(baseEstimateId, estimateData)

      if (response.success && response.data) {
        estimates.value.unshift(response.data)
        totalElements.value += 1
        
        const notificationStore = useNotificationStore()
        notificationStore.success('2차 견적 생성 완료', '2차 견적서가 성공적으로 생성되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to create second estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '2차 견적서 생성에 실패했습니다.')
      throw err
    } finally {
      isCreating.value = false
    }
  }

  const calculateEstimate = async (params: { orderId: number; shippingMethod: 'sea' | 'air'; carrier?: string; serviceLevel?: string }) => {
    isCalculating.value = true
    error.value = null

    try {
      const response = await estimatesApi.calculateEstimate(params)

      if (response.success && response.data) {
        calculationResult.value = response.data
        
        const notificationStore = useNotificationStore()
        notificationStore.success('계산 완료', '견적이 자동으로 계산되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to calculate estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적 계산에 실패했습니다.')
      throw err
    } finally {
      isCalculating.value = false
    }
  }

  const cancelEstimate = async (estimateId: number) => {
    isDeleting.value = true
    error.value = null

    try {
      const response = await estimatesApi.cancelEstimate(estimateId)

      if (response.success) {
        const index = estimates.value.findIndex(e => e.id === estimateId)
        if (index !== -1) {
          estimates.value[index].status = 'cancelled'
        }
        
        if (currentEstimate.value?.id === estimateId) {
          currentEstimate.value.status = 'cancelled'
        }
        
        const notificationStore = useNotificationStore()
        notificationStore.success('취소 완료', '견적서가 취소되었습니다.')
        
        return true
      } else {
        throw new Error(response.message || 'Failed to cancel estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서 취소에 실패했습니다.')
      throw err
    } finally {
      isDeleting.value = false
    }
  }

  const duplicateEstimate = async (estimateId: number) => {
    isCreating.value = true
    error.value = null

    try {
      const response = await estimatesApi.duplicateEstimate(estimateId)

      if (response.success && response.data) {
        estimates.value.unshift(response.data)
        totalElements.value += 1
        
        const notificationStore = useNotificationStore()
        notificationStore.success('복사 완료', '견적서가 복사되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to duplicate estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '견적서 복사에 실패했습니다.')
      throw err
    } finally {
      isCreating.value = false
    }
  }

  const generatePDF = async (estimateId: number) => {
    try {
      const blob = await estimatesApi.generatePDF(estimateId)
      
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `estimate-${estimateId}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      const notificationStore = useNotificationStore()
      notificationStore.success('PDF 생성 완료', 'PDF 파일이 다운로드됩니다.')
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', 'PDF 생성에 실패했습니다.')
      throw err
    }
  }

  const sendEmail = async (estimateId: number, recipientEmail?: string) => {
    try {
      const response = await estimatesApi.sendEmail(estimateId, recipientEmail)

      if (response.success) {
        const notificationStore = useNotificationStore()
        notificationStore.success('전송 완료', '이메일이 발송되었습니다.')
      } else {
        throw new Error(response.message || 'Failed to send email')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '이메일 발송에 실패했습니다.')
      throw err
    }
  }

  const fetchStats = async (params?: { dateFrom?: string; dateTo?: string; customerId?: number }) => {
    try {
      const response = await estimatesApi.getEstimateStats(params)

      if (response.success && response.data) {
        stats.value = response.data
        return response.data
      } else {
        throw new Error(response.message || 'Failed to fetch stats')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to fetch estimate stats:', err)
    }
  }

  const fetchTemplates = async () => {
    try {
      const response = await estimatesApi.getEstimateTemplates()

      if (response.success && response.data) {
        templates.value = response.data
        return response.data
      } else {
        throw new Error(response.message || 'Failed to fetch templates')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to fetch estimate templates:', err)
    }
  }

  const applyTemplate = async (templateId: number, orderId: number) => {
    isCalculating.value = true
    error.value = null

    try {
      const response = await estimatesApi.applyTemplate(templateId, orderId)

      if (response.success && response.data) {
        calculationResult.value = response.data
        
        const notificationStore = useNotificationStore()
        notificationStore.success('템플릿 적용 완료', '견적 템플릿이 적용되었습니다.')
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to apply template')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      const notificationStore = useNotificationStore()
      notificationStore.error('오류', '템플릿 적용에 실패했습니다.')
      throw err
    } finally {
      isCalculating.value = false
    }
  }

  const fetchPricingRules = async (orderType: 'sea' | 'air') => {
    try {
      const response = await estimatesApi.getPricingRules(orderType)

      if (response.success && response.data) {
        pricingRules.value = response.data
        return response.data
      } else {
        throw new Error(response.message || 'Failed to fetch pricing rules')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to fetch pricing rules:', err)
    }
  }

  // Pagination actions
  const setPage = (page: number) => {
    currentPage.value = page
  }

  const nextPage = () => {
    if (hasNextPage.value) {
      currentPage.value += 1
    }
  }

  const prevPage = () => {
    if (hasPrevPage.value) {
      currentPage.value -= 1
    }
  }

  const setPageSize = (size: number) => {
    pageSize.value = size
    currentPage.value = 0 // Reset to first page
  }

  // Filter actions
  const setFilters = (newFilters: Partial<EstimateListParams>) => {
    filters.value = { ...filters.value, ...newFilters }
    currentPage.value = 0 // Reset to first page when filters change
  }

  const clearFilters = () => {
    filters.value = {
      search: '',
      status: '',
      version: undefined,
      dateRange: ''
    }
    currentPage.value = 0
  }

  // Clear actions
  const clearError = () => {
    error.value = null
  }

  const clearCurrentEstimate = () => {
    currentEstimate.value = null
  }

  const clearCalculationResult = () => {
    calculationResult.value = null
  }

  return {
    // State
    estimates,
    currentEstimate,
    isLoading,
    isCreating,
    isUpdating,
    isDeleting,
    isCalculating,
    error,
    currentPage,
    pageSize,
    totalPages,
    totalElements,
    filters,
    calculationResult,
    stats,
    templates,
    pricingRules,

    // Getters
    estimateById,
    pendingEstimates,
    approvedEstimates,
    expiredEstimates,
    estimatesByStatus,
    estimatesByVersion,
    hasNextPage,
    hasPrevPage,

    // Actions
    fetchEstimates,
    fetchOrderEstimates,
    fetchEstimate,
    createEstimate,
    updateEstimate,
    respondToEstimate,
    createSecondEstimate,
    calculateEstimate,
    cancelEstimate,
    duplicateEstimate,
    generatePDF,
    sendEmail,
    fetchStats,
    fetchTemplates,
    applyTemplate,
    fetchPricingRules,
    setPage,
    nextPage,
    prevPage,
    setPageSize,
    setFilters,
    clearFilters,
    clearError,
    clearCurrentEstimate,
    clearCalculationResult
  }
})