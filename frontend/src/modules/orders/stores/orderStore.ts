import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ordersApi } from '@/services/ordersApi'
import type { Order, OrderCreateRequest, BusinessRuleValidation } from '@/types/orders'

export const useOrderStore = defineStore('orders', () => {
  // State
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Pagination
  const currentPage = ref(0)
  const pageSize = ref(20)
  const totalElements = ref(0)
  
  // Filters
  const statusFilter = ref<string>('')
  const typeFilter = ref<string>('')
  const dateRange = ref<{start: string, end: string}>({ start: '', end: '' })

  // Getters
  const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))
  const hasNextPage = computed(() => currentPage.value < totalPages.value - 1)
  const hasPrevPage = computed(() => currentPage.value > 0)
  
  const pendingOrders = computed(() => 
    orders.value.filter(order => ['requested', 'confirmed'].includes(order.status))
  )
  
  const delayedOrders = computed(() =>
    orders.value.filter(order => order.status === 'delayed')
  )
  
  const ordersByStatus = computed(() => {
    const grouped: Record<string, Order[]> = {}
    orders.value.forEach(order => {
      if (!grouped[order.status]) {
        grouped[order.status] = []
      }
      grouped[order.status].push(order)
    })
    return grouped
  })

  // Actions
  const fetchOrders = async (page = 0, userId?: string) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await ordersApi.getOrders({
        page,
        userId, // 사용자별 필터링
        size: pageSize.value,
        status: statusFilter.value || undefined,
        type: typeFilter.value || undefined,
        startDate: dateRange.value.start || undefined,
        endDate: dateRange.value.end || undefined
      })
      
      if (response.success) {
        orders.value = response.data.content
        totalElements.value = response.data.totalElements
        currentPage.value = page
      } else {
        throw new Error(response.message || 'Failed to fetch orders')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to fetch orders:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchOrder = async (id: string) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await ordersApi.getOrder(id)
      
      if (response.success) {
        currentOrder.value = response.data
        return response.data
      } else {
        throw new Error(response.message || 'Failed to fetch order')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to fetch order:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const createOrder = async (orderData: OrderCreateRequest) => {
    loading.value = true
    error.value = null
    
    try {
      // 1. 비즈니스 룰 사전 검증
      const validation = await validateBusinessRules(orderData)
      
      // 2. 주문 생성 요청
      const response = await ordersApi.createOrder(orderData)
      
      if (response.success) {
        // 3. 생성된 주문을 목록에 추가
        orders.value.unshift(response.data)
        
        // 4. 현재 주문으로 설정
        currentOrder.value = response.data
        
        return {
          success: true,
          data: response.data,
          validation,
          warnings: response.warnings || []
        }
      } else {
        throw new Error(response.message || 'Failed to create order')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to create order:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateOrder = async (id: string, orderData: OrderCreateRequest) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await ordersApi.updateOrder(id, orderData)
      
      if (response.success) {
        // Update in list
        const index = orders.value.findIndex(order => order.id === id)
        if (index !== -1) {
          orders.value[index] = response.data
        }
        
        // Update current order
        if (currentOrder.value?.id === id) {
          currentOrder.value = response.data
        }
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to update order')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to update order:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const cancelOrder = async (id: string) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await ordersApi.cancelOrder(id)
      
      if (response.success) {
        // Update status in list
        const order = orders.value.find(order => order.id === id)
        if (order) {
          order.status = 'cancelled'
        }
        
        // Update current order
        if (currentOrder.value?.id === id) {
          currentOrder.value.status = 'cancelled'
        }
        
        return true
      } else {
        throw new Error(response.message || 'Failed to cancel order')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to cancel order:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const validateBusinessRules = async (orderData: OrderCreateRequest): Promise<BusinessRuleValidation> => {
    // CBM 계산
    const totalCBM = orderData.boxes.reduce((sum, box) => {
      return sum + (box.width * box.height * box.depth) / 1_000_000
    }, 0)
    
    // THB 총액 계산
    const totalTHB = orderData.items
      .filter(item => item.currency === 'THB')
      .reduce((sum, item) => sum + item.amount, 0)
    
    // 검증 결과
    const validation: BusinessRuleValidation = {
      cbmExceedsLimit: totalCBM > 29,
      amountExceedsThb1500: totalTHB > 1500,
      requiresExtraRecipient: totalTHB > 1500,
      memberCodeMissing: false, // This should come from user info
      warnings: []
    }
    
    // 경고 메시지 추가
    if (validation.cbmExceedsLimit) {
      validation.warnings.push(
        `총 CBM ${totalCBM.toFixed(3)} m³가 임계값 29 m³를 초과하여 항공배송으로 자동 전환됩니다.`
      )
    }
    
    if (validation.amountExceedsThb1500) {
      validation.warnings.push(
        `THB 총액 ${totalTHB.toLocaleString()}이 임계값 1,500을 초과합니다. 수취인 추가 정보를 입력해주세요.`
      )
    }
    
    return validation
  }

  const requestEstimate = async (orderId: string) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await ordersApi.requestEstimate(orderId)
      
      if (response.success) {
        // Update order with estimate info
        const order = orders.value.find(order => order.id === orderId)
        if (order) {
          order.status = 'estimating'
        }
        
        if (currentOrder.value?.id === orderId) {
          currentOrder.value.status = 'estimating'
        }
        
        return response.data
      } else {
        throw new Error(response.message || 'Failed to request estimate')
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error occurred'
      console.error('Failed to request estimate:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getEstimates = async (orderId: string) => {
    try {
      const response = await ordersApi.getEstimates(orderId)
      
      if (response.success) {
        return response.data
      } else {
        throw new Error(response.message || 'Failed to get estimates')
      }
    } catch (err) {
      console.error('Failed to get estimates:', err)
      throw err
    }
  }

  // Filter actions
  const setStatusFilter = (status: string) => {
    statusFilter.value = status
  }
  
  const setTypeFilter = (type: string) => {
    typeFilter.value = type
  }
  
  const setDateRange = (start: string, end: string) => {
    dateRange.value = { start, end }
  }
  
  const clearFilters = () => {
    statusFilter.value = ''
    typeFilter.value = ''
    dateRange.value = { start: '', end: '' }
  }

  // Pagination actions
  const nextPage = async () => {
    if (hasNextPage.value) {
      await fetchOrders(currentPage.value + 1)
    }
  }
  
  const prevPage = async () => {
    if (hasPrevPage.value) {
      await fetchOrders(currentPage.value - 1)
    }
  }
  
  const goToPage = async (page: number) => {
    if (page >= 0 && page < totalPages.value) {
      await fetchOrders(page)
    }
  }

  // Utility functions
  const calculateCBM = (boxes: Array<{width: number, height: number, depth: number}>) => {
    return boxes.reduce((sum, box) => {
      return sum + (box.width * box.height * box.depth) / 1_000_000
    }, 0)
  }
  
  const calculateTotalAmount = (items: Array<{amount: number}>) => {
    return items.reduce((sum, item) => sum + item.amount, 0)
  }
  
  const getStatusColor = (status: string) => {
    const statusColors: Record<string, string> = {
      requested: 'text-blue-600 bg-blue-100',
      confirmed: 'text-green-600 bg-green-100', 
      in_progress: 'text-yellow-600 bg-yellow-100',
      shipped: 'text-purple-600 bg-purple-100',
      delivered: 'text-green-600 bg-green-100',
      cancelled: 'text-red-600 bg-red-100',
      delayed: 'text-orange-600 bg-orange-100'
    }
    return statusColors[status] || 'text-gray-600 bg-gray-100'
  }
  
  const getStatusText = (status: string) => {
    const statusTexts: Record<string, string> = {
      requested: '요청',
      confirmed: '확인',
      in_progress: '진행중',
      shipped: '발송',
      delivered: '배송완료',
      cancelled: '취소',
      delayed: '지연'
    }
    return statusTexts[status] || status
  }

  return {
    // State
    orders,
    currentOrder,
    loading,
    error,
    currentPage,
    pageSize,
    totalElements,
    statusFilter,
    typeFilter,
    dateRange,
    
    // Getters
    totalPages,
    hasNextPage,
    hasPrevPage,
    pendingOrders,
    delayedOrders,
    ordersByStatus,
    
    // Actions
    fetchOrders,
    fetchOrder,
    createOrder,
    updateOrder,
    cancelOrder,
    validateBusinessRules,
    requestEstimate,
    getEstimates,
    
    // Filter actions
    setStatusFilter,
    setTypeFilter,
    setDateRange,
    clearFilters,
    
    // Pagination
    nextPage,
    prevPage,
    goToPage,
    
    // Utilities
    calculateCBM,
    calculateTotalAmount,
    getStatusColor,
    getStatusText
  }
})