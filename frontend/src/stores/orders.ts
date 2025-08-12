import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { ordersApi } from '@/services/ordersApi'
import type { 
  Order, 
  OrderCreateRequest, 
  OrderItem, 
  OrderBox,
  BusinessRuleValidation,
  Estimate 
} from '@/types/orders'
import type { ApiResponse, PageResponse } from '@/types/api'

// Mock data generation for development
const generateMockOrders = (count: number = 20, userId?: string): Order[] => {
  const statuses: Order['status'][] = ['requested', 'confirmed', 'in_progress', 'shipped', 'delivered', 'cancelled']
  const countries = ['태국', '베트남', '캄보디아', '라오스', '미얀마']
  const mockOrders: Order[] = []
  
  for (let i = 1; i <= count; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const createdDate = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
    const country = countries[Math.floor(Math.random() * countries.length)]
    
    // Generate mock items
    const itemCount = Math.floor(Math.random() * 3) + 1
    const items: OrderItem[] = []
    for (let j = 1; j <= itemCount; j++) {
      items.push({
        id: `item-${i}-${j}`,
        orderId: String(i),
        productName: `제품 ${i}-${j}`,
        quantity: Math.floor(Math.random() * 5) + 1,
        unitPrice: Math.floor(Math.random() * 1000) + 100,
        amount: 0, // Will be calculated
        currency: 'THB',
        emsCode: `EMS${Math.floor(Math.random() * 9000) + 1000}`,
        hsCode: `${Math.floor(Math.random() * 9000) + 1000}.${Math.floor(Math.random() * 90) + 10}`,
        description: `제품 설명 ${i}-${j}`
      })
    }
    
    // Calculate amounts
    items.forEach(item => {
      item.amount = item.quantity * item.unitPrice
    })
    
    // Generate mock boxes
    const boxCount = Math.floor(Math.random() * 2) + 1
    const boxes: OrderBox[] = []
    for (let k = 1; k <= boxCount; k++) {
      const width = Math.floor(Math.random() * 50) + 20
      const height = Math.floor(Math.random() * 50) + 20
      const depth = Math.floor(Math.random() * 50) + 20
      
      boxes.push({
        id: `box-${i}-${k}`,
        orderId: String(i),
        width,
        height,
        depth,
        weight: Math.floor(Math.random() * 20) + 5,
        cbmM3: (width * height * depth) / 1000000,
        description: `Box ${i}-${k}`
      })
    }
    
    const totalAmount = items.reduce((sum, item) => sum + item.amount, 0)
    
    mockOrders.push({
      id: String(i),
      userId: userId || '1', // Default to user ID 1 if not specified
      orderCode: `YCS-${new Date().getFullYear()}-${String(i).padStart(5, '0')}`,
      status: status,
      orderType: Math.random() > 0.7 ? 'air' : 'sea',
      recipientName: `수취인 ${i}`,
      recipientPhone: `+66${Math.floor(Math.random() * 900000000 + 100000000)}`,
      recipientAddress: `주소 ${i}, ${country}`,
      recipientCountry: country,
      totalAmount,
      currency: 'THB',
      requiresExtraRecipient: totalAmount > 45000, // THB 1,500 ≈ $45 * 1000 = 45000 THB rough conversion
      createdAt: createdDate.toISOString(),
      updatedAt: createdDate.toISOString(),
      items,
      boxes
    })
  }
  
  return mockOrders.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}

export const useOrdersStore = defineStore('orders', () => {
  // State
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const orderItems = ref<OrderItem[]>([])
  const orderBoxes = ref<OrderBox[]>([])
  const estimates = ref<Estimate[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const validationResult = ref<BusinessRuleValidation | null>(null)

  // Pagination state
  const currentPage = ref(0)
  const pageSize = ref(20)
  const totalElements = ref(0)
  const totalPages = ref(0)

  // Filter state
  const filters = ref({
    status: '',
    orderType: '',
    startDate: '',
    endDate: '',
    search: ''
  })

  // Getters
  const hasOrders = computed(() => orders.value.length > 0)
  const hasValidationWarnings = computed(() => 
    validationResult.value && (
      validationResult.value.requiresAirShipping ||
      validationResult.value.requiresExtraRecipientInfo ||
      validationResult.value.hasDelayWarning
    )
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

  // Dashboard statistics
  const dashboardStats = computed(() => {
    const totalOrders = orders.value.length
    const pendingOrders = orders.value.filter(o => o.status === 'requested').length
    const inTransitOrders = orders.value.filter(o => ['in_progress', 'shipped'].includes(o.status)).length
    const totalSpent = orders.value.reduce((sum, o) => sum + (o.totalAmount || 0), 0)
    
    return {
      totalOrders,
      pendingOrders,
      inTransitOrders,
      totalSpent,
      ordersTrend: Math.floor(Math.random() * 20) - 10 // Random trend between -10% and +10%
    }
  })

  const recentOrders = computed(() => {
    return orders.value.slice(0, 5) // Get most recent 5 orders
  })

  // Actions
  const fetchOrders = async (params?: {
    page?: number
    size?: number
    status?: string
    orderType?: string
    startDate?: string
    endDate?: string
    search?: string
  }) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Generate mock data if not already loaded
      if (orders.value.length === 0) {
        // Get current user ID from localStorage or default to '1'
        const currentUserId = localStorage.getItem('current_user_id') || '1'
        orders.value = generateMockOrders(50, currentUserId) // Generate 50 mock orders for current user
      }
      
      // Apply filters to mock data
      const currentUserId = localStorage.getItem('current_user_id') || '1'
      let filteredOrders = orders.value.filter(o => o.userId === currentUserId)
      
      if (params?.status) {
        filteredOrders = filteredOrders.filter(o => o.status === params.status)
      }
      
      if (params?.orderType) {
        filteredOrders = filteredOrders.filter(o => o.orderType === params.orderType)
      }
      
      if (params?.search) {
        const searchTerm = params.search.toLowerCase()
        filteredOrders = filteredOrders.filter(o => 
          o.orderCode.toLowerCase().includes(searchTerm) ||
          o.recipientName.toLowerCase().includes(searchTerm) ||
          o.recipientCountry.toLowerCase().includes(searchTerm)
        )
      }
      
      // Apply pagination
      const page = params?.page ?? currentPage.value
      const size = params?.size ?? pageSize.value
      const start = page * size
      const end = start + size
      const paginatedOrders = filteredOrders.slice(start, end)
      
      // Update state
      currentPage.value = page
      totalElements.value = filteredOrders.length
      totalPages.value = Math.ceil(filteredOrders.length / size)
      
      // For dashboard, we want all orders available for stats
      if (!params || Object.keys(params).length === 0) {
        // No filters applied, return all orders
        orders.value = generateMockOrders(50)
      }

      return {
        success: true,
        data: {
          content: paginatedOrders,
          page: page,
          totalElements: filteredOrders.length,
          totalPages: Math.ceil(filteredOrders.length / size)
        }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch orders'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchOrder = async (orderId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.getOrder(orderId)

      if (response.success && response.data) {
        currentOrder.value = response.data
        
        // Update order in list if exists
        const index = orders.value.findIndex(o => o.id === orderId)
        if (index !== -1) {
          orders.value[index] = response.data
        }
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch order'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const createOrder = async (orderData: any) => {
    isLoading.value = true
    error.value = null
    validationResult.value = null

    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Calculate totals
      const totalAmount = orderData.items?.reduce((sum: number, item: any) => sum + (item.amount || 0), 0) || 0
      const totalCBM = orderData.boxes?.reduce((sum: number, box: any) => {
        return sum + ((box.width * box.height * box.depth) / 1000000)
      }, 0) || 0
      
      // Generate order
      const orderId = `ORD${Date.now()}`
      const newOrder: Order = {
        id: orderId,
        orderCode: `YCS-${new Date().getFullYear()}-${String(orders.value.length + 1).padStart(5, '0')}`,
        status: 'requested',
        orderType: totalCBM > 29 ? 'air' : 'sea',
        recipientName: orderData.recipient?.name || '',
        recipientPhone: orderData.recipient?.phone || '',
        recipientAddress: orderData.recipient?.address || '',
        recipientCountry: orderData.recipient?.country || '',
        totalAmount,
        currency: orderData.items?.[0]?.currency || 'THB',
        requiresExtraRecipient: totalAmount > 45000, // THB conversion
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        items: orderData.items?.map((item: any, index: number) => ({
          id: `item-${Date.now()}-${index}`,
          orderId,
          productName: item.name,
          quantity: item.quantity,
          unitPrice: item.amount / item.quantity,
          amount: item.amount,
          currency: item.currency,
          hsCode: item.hsCode,
          emsCode: item.emsCode,
          description: item.description
        })) || [],
        boxes: orderData.boxes?.map((box: any, index: number) => ({
          id: `box-${Date.now()}-${index}`,
          orderId,
          width: box.width,
          height: box.height,
          depth: box.depth,
          weight: box.weight,
          cbmM3: (box.width * box.height * box.depth) / 1000000,
          description: `Box ${index + 1}`
        })) || []
      }
      
      // Add to beginning of orders list
      orders.value.unshift(newOrder)
      currentOrder.value = newOrder
      totalElements.value += 1
      
      return {
        success: true,
        orderId: newOrder.id,
        order: newOrder
      }
      
    } catch (err: any) {
      error.value = err.message || 'Failed to create order'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const updateOrder = async (orderId: number, updateData: Partial<Order>) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.updateOrder(orderId, updateData)

      if (response.success && response.data) {
        const updatedOrder = response.data
        
        // Update in orders list
        const index = orders.value.findIndex(o => o.id === orderId)
        if (index !== -1) {
          orders.value[index] = updatedOrder
        }
        
        // Update current order if it's the same
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = updatedOrder
        }
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to update order'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const cancelOrder = async (orderId: number, reason?: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.cancelOrder(orderId, reason)

      if (response.success && response.data) {
        const cancelledOrder = response.data
        
        // Update status in orders list
        const index = orders.value.findIndex(o => o.id === orderId)
        if (index !== -1) {
          orders.value[index] = cancelledOrder
        }
        
        // Update current order if it's the same
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = cancelledOrder
        }
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to cancel order'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchOrderItems = async (orderId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.getOrderItems(orderId)

      if (response.success && response.data) {
        orderItems.value = response.data
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch order items'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchOrderBoxes = async (orderId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.getOrderBoxes(orderId)

      if (response.success && response.data) {
        orderBoxes.value = response.data
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch order boxes'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const validateBusinessRules = async (orderData: any) => {
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300))
      
      const warnings: any[] = []
      const totalCBM = orderData.boxes?.reduce((sum: number, box: any) => {
        return sum + ((box.width * box.height * box.depth) / 1000000)
      }, 0) || 0
      
      // CBM > 29 check
      if (totalCBM > 29) {
        warnings.push({
          type: 'cbm_exceeded',
          message: 'CBM이 29m³를 초과했습니다. 항공 배송이 권장됩니다.',
          severity: 'warning'
        })
      }
      
      // THB > 1500 check (assuming conversion for demo)
      if (orderData.totalAmount > 1500) {
        warnings.push({
          type: 'amount_exceeded',
          message: 'THB 1,500을 초과하는 품목이 있습니다. 수취인 추가 정보가 필요할 수 있습니다.',
          severity: 'warning'
        })
      }
      
      // HS Code validation mock
      if (orderData.items?.some((item: any) => item.hsCode && !item.hsCode.match(/^\d{4}\.\d{2}$/))) {
        warnings.push({
          type: 'invalid_hs_code',
          message: 'HS 코드 형식이 올바르지 않습니다.',
          severity: 'error'
        })
      }
      
      const result = {
        valid: warnings.filter(w => w.severity === 'error').length === 0,
        warnings
      }
      
      validationResult.value = result
      return { success: true, data: result }
      
    } catch (err: any) {
      console.error('Business rule validation failed:', err)
      return { success: false, message: 'Validation failed' }
    }
  }

  const requestEstimate = async (orderId: number, version: number = 1) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.requestEstimate(orderId, version)

      if (response.success && response.data) {
        estimates.value = [...estimates.value, response.data]
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to request estimate'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchEstimates = async (orderId: number) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await ordersApi.getEstimates(orderId)

      if (response.success && response.data) {
        estimates.value = response.data
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch estimates'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  // Filter and search actions
  const setFilters = (newFilters: Partial<typeof filters.value>) => {
    filters.value = { ...filters.value, ...newFilters }
    currentPage.value = 0 // Reset to first page when filters change
  }

  const clearFilters = () => {
    filters.value = {
      status: '',
      orderType: '',
      startDate: '',
      endDate: '',
      search: ''
    }
    currentPage.value = 0
  }

  const searchOrders = async (searchTerm: string) => {
    setFilters({ search: searchTerm })
    await fetchOrders()
  }

  // Pagination actions
  const setPage = (page: number) => {
    currentPage.value = page
  }

  const nextPage = () => {
    if (currentPage.value < totalPages.value - 1) {
      currentPage.value += 1
    }
  }

  const prevPage = () => {
    if (currentPage.value > 0) {
      currentPage.value -= 1
    }
  }

  // Utility actions
  const clearError = () => {
    error.value = null
  }

  const clearValidationResult = () => {
    validationResult.value = null
  }

  const clearCurrentOrder = () => {
    currentOrder.value = null
    orderItems.value = []
    orderBoxes.value = []
    estimates.value = []
  }

  const getOrderById = (orderId: number) => {
    return orders.value.find(order => order.id === orderId)
  }

  const getOrdersByStatus = (status: string) => {
    return orders.value.filter(order => order.status === status)
  }

  const getOrdersRequiringAction = () => {
    const actionRequiredStatuses = ['pending_approval', 'ready_for_pickup', 'estimate_required']
    return orders.value.filter(order => actionRequiredStatuses.includes(order.status))
  }

  // Real-time updates helper
  const updateOrderStatus = (orderId: number, newStatus: string) => {
    const order = getOrderById(orderId)
    if (order) {
      order.status = newStatus
      order.updatedAt = new Date().toISOString()
    }
  }

  const addOrder = (order: Order) => {
    const existingIndex = orders.value.findIndex(o => o.id === order.id)
    if (existingIndex !== -1) {
      orders.value[existingIndex] = order
    } else {
      orders.value.unshift(order)
      totalElements.value += 1
    }
  }

  return {
    // State
    orders: readonly(orders),
    currentOrder: readonly(currentOrder),
    orderItems: readonly(orderItems),
    orderBoxes: readonly(orderBoxes),
    estimates: readonly(estimates),
    isLoading: readonly(isLoading),
    error: readonly(error),
    validationResult: readonly(validationResult),
    
    // Pagination state
    currentPage: readonly(currentPage),
    pageSize: readonly(pageSize),
    totalElements: readonly(totalElements),
    totalPages: readonly(totalPages),
    filters: readonly(filters),
    
    // Getters
    hasOrders,
    hasValidationWarnings,
    ordersByStatus,
    dashboardStats,
    recentOrders,
    
    // Actions
    fetchOrders,
    fetchOrder,
    createOrder,
    updateOrder,
    cancelOrder,
    fetchOrderItems,
    fetchOrderBoxes,
    validateBusinessRules,
    requestEstimate,
    fetchEstimates,
    
    // Filter and search
    setFilters,
    clearFilters,
    searchOrders,
    
    // Pagination
    setPage,
    nextPage,
    prevPage,
    
    // Utilities
    clearError,
    clearValidationResult,
    clearCurrentOrder,
    getOrderById,
    getOrdersByStatus,
    getOrdersRequiringAction,
    updateOrderStatus,
    addOrder
  }
})