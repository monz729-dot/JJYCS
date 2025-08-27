import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Order, CreateOrderForm, OrderItem, OrderStatus } from '../types'
import { ordersApi } from '../utils/api'

export const useOrdersStore = defineStore('orders', () => {
  // State
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const pagination = ref({
    current: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0
  })

  // Getters
  const pendingOrders = computed(() => 
    orders.value.filter(order => order.status === 'PENDING' || order.status === 'PREPARING')
  )
  
  const shippedOrders = computed(() => 
    orders.value.filter(order => order.status === 'SHIPPED')
  )
  
  const deliveredOrders = computed(() => 
    orders.value.filter(order => order.status === 'DELIVERED')
  )

  const recentOrders = computed(() => 
    [...orders.value]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5)
  )

  // Actions
  const fetchOrders = async (page: number = 1, pageSize: number = 20, filters?: any) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.getOrders(page, pageSize, filters)
      
      if (response.success && response.data) {
        orders.value = response.data.orders || response.data.content || []
        pagination.value = {
          current: page,
          pageSize,
          total: response.data.total || response.data.totalElements || 0,
          totalPages: response.data.totalPages || 0
        }
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to fetch orders'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch orders'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchOrderById = async (orderId: number) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.getOrderById(orderId)
      
      if (response.success && response.data) {
        currentOrder.value = response.data
        
        // Update order in list if exists
        const index = orders.value.findIndex(order => order.id === orderId)
        if (index !== -1) {
          orders.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to fetch order'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch order'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const createOrder = async (orderForm: CreateOrderForm) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.createOrder(orderForm)
      
      if (response.success && response.data) {
        // Add to beginning of orders list
        orders.value.unshift(response.data)
        currentOrder.value = response.data
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to create order'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to create order'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const updateOrder = async (orderId: number, updates: Partial<Order>) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.updateOrder(orderId, updates)
      
      if (response.success && response.data) {
        const index = orders.value.findIndex(order => order.id === orderId)
        if (index !== -1) {
          orders.value[index] = { ...orders.value[index], ...response.data }
        }
        
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = { ...currentOrder.value, ...response.data }
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to update order'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to update order'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const updateOrderStatus = async (orderId: number, status: OrderStatus) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.updateOrderStatus(orderId, status)
      
      if (response.success) {
        const index = orders.value.findIndex(order => order.id === orderId)
        if (index !== -1) {
          orders.value[index].status = status
          orders.value[index].updatedAt = new Date().toISOString()
        }
        
        if (currentOrder.value?.id === orderId) {
          currentOrder.value.status = status
          currentOrder.value.updatedAt = new Date().toISOString()
        }
        
        return { success: true }
      } else {
        error.value = response.error || 'Failed to update order status'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to update order status'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const calculateOrderTotals = (items: OrderItem[]) => {
    const subtotal = items.reduce((sum, item) => {
      return sum + (item.unitPrice * item.quantity)
    }, 0)
    
    const totalWeight = items.reduce((sum, item) => {
      return sum + (item.weight * item.quantity)
    }, 0)
    
    const totalCBM = items.reduce((sum, item) => {
      return sum + (item.cbm * item.quantity)
    }, 0)

    // CBM 29 초과 시 항공으로 자동 전환
    const shippingType = totalCBM > 29 ? 'air' : 'sea'
    
    // 기본 배송비 계산 (실제로는 더 복잡한 로직)
    const baseShippingFee = shippingType === 'air' ? totalWeight * 15000 : totalCBM * 50000
    
    // THB 1500 초과 시 추가 수수료
    const thbValue = subtotal * 35 // 임시 환율
    const additionalFee = thbValue > 1500 ? subtotal * 0.02 : 0

    const tax = subtotal * 0.1 // 10% 세금
    const total = subtotal + baseShippingFee + additionalFee + tax

    return {
      subtotal,
      totalWeight,
      totalCBM,
      shippingType,
      baseShippingFee,
      additionalFee,
      tax,
      total,
      requiresExtraRecipientInfo: thbValue > 1500,
      cbmExceeded: totalCBM > 29
    }
  }

  const trackOrder = async (trackingNumber: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.trackOrder(trackingNumber)
      
      if (response.success && response.data) {
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to track order'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to track order'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const getOrderEstimate = async (orderItems: OrderItem[]) => {
    loading.value = true
    error.value = null

    try {
      const response = await ordersApi.getEstimate(orderItems)
      
      if (response.success && response.data) {
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to get estimate'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to get estimate'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const clearCurrentOrder = () => {
    currentOrder.value = null
  }

  const clearError = () => {
    error.value = null
  }

  const clearOrders = () => {
    orders.value = []
    currentOrder.value = null
    pagination.value = {
      current: 1,
      pageSize: 20,
      total: 0,
      totalPages: 0
    }
  }

  return {
    // State
    orders,
    currentOrder,
    loading,
    error,
    pagination,
    
    // Getters
    pendingOrders,
    shippedOrders,
    deliveredOrders,
    recentOrders,
    
    // Actions
    fetchOrders,
    fetchOrderById,
    createOrder,
    updateOrder,
    updateOrderStatus,
    calculateOrderTotals,
    trackOrder,
    getOrderEstimate,
    clearCurrentOrder,
    clearError,
    clearOrders
  }
})