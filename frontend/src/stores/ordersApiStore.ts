import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import SpringBootOrdersService from '@/services/ordersApiService'
import SpringBootAuthService from '@/services/authApiService'
import type { 
  OrderCreateRequest, 
  OrderResponse, 
  OrderListParams,
  PageResponse,
  OrderStatsResponse 
} from '@/services/ordersApiService'

export interface BusinessWarning {
  type: string
  message: string
  severity: 'warning' | 'error' | 'info'
  details?: any
}

export interface ValidationResult {
  valid: boolean
  warnings?: BusinessWarning[]
  message?: string
}

export const useOrdersApiStore = defineStore('ordersApi', () => {
  // State
  const orders = ref<OrderResponse[]>([])
  const currentOrder = ref<OrderResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const stats = ref<OrderStatsResponse | null>(null)
  const pagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  })

  // Getters
  const hasOrders = computed(() => orders.value.length > 0)
  const pendingOrders = computed(() => 
    orders.value.filter(o => o.status === 'REQUESTED')
  )
  const completedOrders = computed(() => 
    orders.value.filter(o => o.status === 'DELIVERED')
  )

  // Actions
  
  /**
   * 주문 생성
   */
  async function createOrder(orderData: any): Promise<OrderResponse> {
    loading.value = true
    error.value = null

    try {
      // 현재 로그인한 사용자 정보 가져오기
      const currentUser = SpringBootAuthService.getCurrentUser()
      
      // Spring Boot API 형식으로 변환
      const request: OrderCreateRequest = {
        userId: currentUser?.id,
        recipientName: orderData.recipient.name,
        recipientPhone: orderData.recipient.phone || '',
        recipientAddress: orderData.recipient.address,
        recipientCountry: orderData.recipient.country,
        urgent: orderData.shipping.urgency === 'urgent',
        needsRepacking: orderData.shipping.needsRepacking || false,
        specialInstructions: orderData.shipping.specialInstructions || '',
        items: orderData.items.map((item: any, index: number) => ({
          itemOrder: index + 1,
          name: item.name,
          description: item.description || '',
          category: item.category || 'general',
          quantity: item.quantity,
          unitWeight: item.weight || 0,
          unitPrice: item.amount / item.quantity,
          currency: item.currency,
          hsCode: item.hsCode || '',
          emsCode: item.emsCode || '',
          countryOfOrigin: item.countryOfOrigin || '',
          brand: item.brand || '',
          restricted: false,
          restrictionNote: ''
        })),
        boxes: orderData.boxes.map((box: any, index: number) => ({
          boxNumber: index + 1,
          widthCm: box.width,
          heightCm: box.height,
          depthCm: box.depth,
          weightKg: box.weight || 0,
          notes: ''
        }))
      }

      const result = await SpringBootOrdersService.createOrder(request)
      
      if (result.success && result.data) {
        // 주문 목록에 추가
        orders.value.unshift(result.data)
        currentOrder.value = result.data
        
        // 통계 업데이트
        await fetchStats()
        
        return result.data
      } else {
        throw new Error(result.error || '주문 생성에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 주문 목록 조회
   */
  async function fetchOrders(params: OrderListParams = {}) {
    loading.value = true
    error.value = null

    try {
      const currentUser = SpringBootAuthService.getCurrentUser()
      
      // 일반 사용자는 자신의 주문만 조회
      if (currentUser?.role === 'INDIVIDUAL' || currentUser?.role === 'ENTERPRISE') {
        params.userId = currentUser.id
      }

      const result = await SpringBootOrdersService.getOrders(params)
      
      if (result.success && result.data) {
        orders.value = result.data.content
        pagination.value = {
          page: result.data.page,
          size: result.data.size,
          totalElements: result.data.totalElements,
          totalPages: result.data.totalPages
        }
      } else {
        throw new Error(result.error || '주문 목록 조회에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 주문 상세 조회
   */
  async function fetchOrder(orderId: number) {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootOrdersService.getOrder(orderId)
      
      if (result.success && result.data) {
        currentOrder.value = result.data
        return result.data
      } else {
        throw new Error(result.error || '주문 조회에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 주문 상태 업데이트
   */
  async function updateOrderStatus(orderId: number, status: string) {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootOrdersService.updateOrderStatus(orderId, status)
      
      if (result.success && result.data) {
        // 목록에서 업데이트
        const index = orders.value.findIndex(o => o.id === orderId)
        if (index !== -1) {
          orders.value[index] = result.data
        }
        
        // 현재 주문이면 업데이트
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = result.data
        }
        
        return result.data
      } else {
        throw new Error(result.error || '주문 상태 업데이트에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 주문 취소
   */
  async function cancelOrder(orderId: number, reason: string) {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootOrdersService.cancelOrder(orderId, reason)
      
      if (result.success && result.data) {
        // 목록에서 업데이트
        const index = orders.value.findIndex(o => o.id === orderId)
        if (index !== -1) {
          orders.value[index] = result.data
        }
        
        // 현재 주문이면 업데이트
        if (currentOrder.value?.id === orderId) {
          currentOrder.value = result.data
        }
        
        return result.data
      } else {
        throw new Error(result.error || '주문 취소에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 통계 조회
   */
  async function fetchStats() {
    try {
      const currentUser = SpringBootAuthService.getCurrentUser()
      const result = await SpringBootOrdersService.getUserOrderStats(currentUser?.id)
      
      if (result.success && result.data) {
        stats.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to fetch stats:', err)
    }
  }

  /**
   * 최근 주문 조회
   */
  async function fetchRecentOrders(limit: number = 5) {
    try {
      const currentUser = SpringBootAuthService.getCurrentUser()
      const result = await SpringBootOrdersService.getRecentOrders(currentUser?.id, limit)
      
      if (result.success && result.data) {
        return result.data
      }
      return []
    } catch (err: any) {
      console.error('Failed to fetch recent orders:', err)
      return []
    }
  }

  /**
   * 비즈니스 룰 검증
   */
  async function validateBusinessRules(data: any): Promise<ValidationResult> {
    const warnings: BusinessWarning[] = []
    
    // CBM 계산 및 검증
    if (data.boxes) {
      const totalCbm = SpringBootOrdersService.calculateTotalCBM(data.boxes)
      
      if (SpringBootOrdersService.shouldConvertToAir(totalCbm)) {
        warnings.push({
          type: 'CBM_EXCEEDED',
          message: `총 CBM(${totalCbm.toFixed(2)}m³)이 29m³를 초과하여 항공 배송으로 자동 전환됩니다.`,
          severity: 'warning',
          details: { cbm: totalCbm, threshold: 29 }
        })
      }
    }
    
    // THB 1,500 초과 검증
    if (data.items && data.currency === 'THB') {
      const totalAmount = SpringBootOrdersService.calculateTotalAmount(data.items)
      
      if (SpringBootOrdersService.requiresExtraRecipientInfo(totalAmount, 'THB')) {
        warnings.push({
          type: 'AMOUNT_EXCEEDED',
          message: `총 금액(${totalAmount} THB)이 1,500 THB를 초과합니다. 추가 수취인 정보가 필요할 수 있습니다.`,
          severity: 'warning',
          details: { amount: totalAmount, threshold: 1500 }
        })
      }
    }
    
    // 회원 코드 검증
    const currentUser = SpringBootAuthService.getCurrentUser()
    if (!currentUser?.memberCode) {
      warnings.push({
        type: 'NO_MEMBER_CODE',
        message: '회원 코드가 없어 주문 처리가 지연될 수 있습니다.',
        severity: 'info',
        details: {}
      })
    }
    
    return {
      valid: warnings.filter(w => w.severity === 'error').length === 0,
      warnings: warnings.length > 0 ? warnings : undefined
    }
  }

  /**
   * 상태 초기화
   */
  function reset() {
    orders.value = []
    currentOrder.value = null
    loading.value = false
    error.value = null
    stats.value = null
  }

  return {
    // State
    orders,
    currentOrder,
    loading,
    error,
    stats,
    pagination,
    
    // Getters
    hasOrders,
    pendingOrders,
    completedOrders,
    
    // Actions
    createOrder,
    fetchOrders,
    fetchOrder,
    updateOrderStatus,
    cancelOrder,
    fetchStats,
    fetchRecentOrders,
    validateBusinessRules,
    reset
  }
})