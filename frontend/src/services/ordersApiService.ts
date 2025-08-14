import axios from 'axios'

const API_BASE_URL = '/api'

// API 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터: JWT 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터: 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      window.location.href = '/auth/login'
    }
    return Promise.reject(error)
  }
)

export interface OrderItem {
  itemOrder: number
  name: string
  description: string
  category: string
  quantity: number
  unitWeight: number
  unitPrice: number
  currency: string
  hsCode?: string
  emsCode?: string
  countryOfOrigin?: string
  brand?: string
  restricted: boolean
  restrictionNote?: string
}

export interface OrderBox {
  boxNumber: number
  widthCm: number
  heightCm: number
  depthCm: number
  weightKg: number
  notes?: string
}

export interface OrderCreateRequest {
  userId?: number // 백엔드에서 JWT에서 추출하므로 선택사항
  recipientName: string
  recipientPhone: string
  recipientAddress: string
  recipientCountry: string
  urgent: boolean
  needsRepacking: boolean
  specialInstructions?: string
  items: OrderItem[]
  boxes: OrderBox[]
}

export interface OrderResponse {
  id: number
  orderCode: string
  userId: number
  status: string
  orderType: string
  recipientName: string
  recipientPhone: string
  recipientAddress: string
  recipientCountry: string
  urgency: string
  needsRepacking: boolean
  specialInstructions?: string
  totalAmount?: number
  currency: string
  totalCbmM3?: number
  requiresExtraRecipient: boolean
  estimatedDeliveryDate?: string
  actualDeliveryDate?: string
  paymentMethod: string
  paymentStatus: string
  estimatedCost?: number
  actualCost?: number
  notes?: string
  createdAt: string
  updatedAt: string
  itemCount: number
  boxCount: number
}

export interface OrderStatsResponse {
  totalOrders: number
  pendingOrders: number
  inTransitOrders: number
  totalSpent: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface OrderListParams {
  userId?: number
  status?: string
  orderType?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sort?: string
}

export class SpringBootOrdersService {
  
  // 주문 생성
  static async createOrder(orderData: OrderCreateRequest): Promise<{ success: boolean; data?: OrderResponse; error?: string }> {
    try {
      console.log('=== 주문 생성 시작 ===')
      console.log('주문 데이터:', orderData)
      
      const response = await apiClient.post<OrderResponse>('/orders', orderData)
      
      console.log('주문 생성 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 생성 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 생성 중 오류가 발생했습니다.' 
      }
    }
  }

  // 주문 목록 조회
  static async getOrders(params: OrderListParams = {}): Promise<{ success: boolean; data?: PageResponse<OrderResponse>; error?: string }> {
    try {
      console.log('=== 주문 목록 조회 시작 ===')
      console.log('조회 파라미터:', params)
      
      // 쿼리 파라미터 구성
      const queryParams = new URLSearchParams()
      if (params.userId) queryParams.append('userId', params.userId.toString())
      if (params.status) queryParams.append('status', params.status)
      if (params.orderType) queryParams.append('orderType', params.orderType)
      if (params.startDate) queryParams.append('startDate', params.startDate)
      if (params.endDate) queryParams.append('endDate', params.endDate)
      queryParams.append('page', (params.page || 0).toString())
      queryParams.append('size', (params.size || 20).toString())
      if (params.sort) queryParams.append('sort', params.sort)
      
      const response = await apiClient.get<PageResponse<OrderResponse>>(`/orders?${queryParams}`)
      
      console.log('주문 목록 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 목록 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 목록 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 주문 상세 조회
  static async getOrder(orderId: number): Promise<{ success: boolean; data?: OrderResponse; error?: string }> {
    try {
      console.log('=== 주문 상세 조회 시작 ===')
      console.log('주문 ID:', orderId)
      
      const response = await apiClient.get<OrderResponse>(`/orders/${orderId}`)
      
      console.log('주문 상세 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 상세 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 상세 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 주문 상태 업데이트
  static async updateOrderStatus(orderId: number, status: string): Promise<{ success: boolean; data?: OrderResponse; error?: string }> {
    try {
      console.log('=== 주문 상태 업데이트 시작 ===')
      console.log('주문 ID:', orderId, '상태:', status)
      
      const response = await apiClient.put<OrderResponse>(`/orders/${orderId}/status`, { status })
      
      console.log('상태 업데이트 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 상태 업데이트 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 상태 업데이트 중 오류가 발생했습니다.' 
      }
    }
  }

  // 주문 취소
  static async cancelOrder(orderId: number, reason: string): Promise<{ success: boolean; data?: OrderResponse; error?: string }> {
    try {
      console.log('=== 주문 취소 시작 ===')
      console.log('주문 ID:', orderId, '취소 사유:', reason)
      
      const response = await apiClient.put<OrderResponse>(`/orders/${orderId}/cancel`, { reason })
      
      console.log('주문 취소 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 취소 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 취소 중 오류가 발생했습니다.' 
      }
    }
  }

  // 사용자별 주문 통계 조회
  static async getUserOrderStats(userId?: number): Promise<{ success: boolean; data?: OrderStatsResponse; error?: string }> {
    try {
      console.log('=== 주문 통계 조회 시작 ===')
      console.log('사용자 ID:', userId)
      
      const url = userId ? `/orders/stats?userId=${userId}` : '/orders/stats'
      const response = await apiClient.get<OrderStatsResponse>(url)
      
      console.log('주문 통계 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('주문 통계 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '주문 통계 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 최근 주문 조회
  static async getRecentOrders(userId?: number, limit: number = 5): Promise<{ success: boolean; data?: OrderResponse[]; error?: string }> {
    try {
      console.log('=== 최근 주문 조회 시작 ===')
      console.log('사용자 ID:', userId, '개수:', limit)
      
      const params = new URLSearchParams()
      if (userId) params.append('userId', userId.toString())
      params.append('limit', limit.toString())
      
      const response = await apiClient.get<OrderResponse[]>(`/orders/recent?${params}`)
      
      console.log('최근 주문 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('최근 주문 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '최근 주문 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // CBM 계산 (클라이언트 사이드 유틸리티)
  static calculateCBM(widthCm: number, heightCm: number, depthCm: number): number {
    return (widthCm * heightCm * depthCm) / 1_000_000
  }

  // 여러 박스의 총 CBM 계산
  static calculateTotalCBM(boxes: OrderBox[]): number {
    return boxes.reduce((total, box) => {
      return total + this.calculateCBM(box.widthCm, box.heightCm, box.depthCm)
    }, 0)
  }

  // 총 무게 계산
  static calculateTotalWeight(boxes: OrderBox[]): number {
    return boxes.reduce((total, box) => total + box.weightKg, 0)
  }

  // 총 금액 계산
  static calculateTotalAmount(items: OrderItem[]): number {
    return items.reduce((total, item) => total + (item.unitPrice * item.quantity), 0)
  }

  // CBM 29m³ 초과 체크
  static shouldConvertToAir(totalCbm: number): boolean {
    return totalCbm > 29
  }

  // THB 1,500 초과 체크
  static requiresExtraRecipientInfo(totalAmount: number, currency: string = 'THB'): boolean {
    return currency === 'THB' && totalAmount > 1500
  }
}

// 기본 export
export default SpringBootOrdersService