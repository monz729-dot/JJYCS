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

export interface PaymentCreateRequest {
  orderId: number
  amount: number
  currency: string
  method: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'MOBILE_PAYMENT' | 'CRYPTO'
  description: string
  returnUrl?: string
  cancelUrl?: string
  customData?: Record<string, any>
}

export interface PaymentResponse {
  id: number
  paymentNumber: string
  orderId: number
  orderNumber: string
  memberId: number
  memberName: string
  amount: number
  currency: string
  method: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'CANCELLED'
  installments: number
  description: string
  gatewayTransactionId?: string
  gatewayResponse?: string
  failureReason?: string
  refundReason?: string
  refundAmount?: number
  refundable: boolean
  securityInfo?: {
    sslEncrypted: boolean
    pciCompliant: boolean
    threeDSecure: boolean
  }
  createdAt: string
  updatedAt: string
  expiresAt?: string
}

export interface PaymentTransaction {
  id: number
  paymentId: number
  type: 'CREATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'CANCELLED'
  description: string
  details?: string
  amount?: number
  gatewayReference?: string
  metadata?: Record<string, any>
  createdAt: string
}

export interface RefundRequest {
  paymentId: number
  amount?: number // partial refund amount, if not provided - full refund
  reason: string
  adminId?: number
}

export interface RefundResponse {
  id: number
  paymentId: number
  amount: number
  reason: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  gatewayRefundId?: string
  processedBy?: string
  createdAt: string
  completedAt?: string
}

export interface PaymentStats {
  totalTransactions: number
  totalAmount: number
  successRate: number
  averageTransactionAmount: number
  todayTransactions: number
  todayAmount: number
  weeklyGrowth: number
  monthlyGrowth: number
}

export interface PaymentMethodStats {
  method: string
  count: number
  amount: number
  percentage: number
}

export default class SpringBootPaymentService {
  /**
   * 결제 생성
   */
  static async createPayment(request: PaymentCreateRequest): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<PaymentResponse>('/payments', request)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment creation error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 상세 조회
   */
  static async getPayment(paymentId: number): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.get<PaymentResponse>(`/payments/${paymentId}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 목록 조회
   */
  static async getPayments(params: {
    page?: number
    size?: number
    status?: string
    method?: string
    startDate?: string
    endDate?: string
    memberId?: number
    orderId?: number
  } = {}): Promise<{
    success: boolean
    data?: {
      content: PaymentResponse[]
      page: number
      size: number
      totalElements: number
      totalPages: number
    }
    error?: string
  }> {
    try {
      const queryParams = new URLSearchParams()
      if (params.page !== undefined) queryParams.append('page', params.page.toString())
      if (params.size !== undefined) queryParams.append('size', params.size.toString())
      if (params.status) queryParams.append('status', params.status)
      if (params.method) queryParams.append('method', params.method)
      if (params.startDate) queryParams.append('startDate', params.startDate)
      if (params.endDate) queryParams.append('endDate', params.endDate)
      if (params.memberId) queryParams.append('memberId', params.memberId.toString())
      if (params.orderId) queryParams.append('orderId', params.orderId.toString())

      const response = await apiClient.get<{
        content: PaymentResponse[]
        page: number
        size: number
        totalElements: number
        totalPages: number
      }>(`/payments?${queryParams.toString()}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payments fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 상태 업데이트
   */
  static async updatePaymentStatus(paymentId: number, status: string): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.put<PaymentResponse>(`/payments/${paymentId}/status`, { status })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment status update error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 환불
   */
  static async refundPayment(request: RefundRequest): Promise<{
    success: boolean
    data?: RefundResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<RefundResponse>('/payments/refund', request)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment refund error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 재시도
   */
  static async retryPayment(paymentId: number): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<PaymentResponse>(`/payments/${paymentId}/retry`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment retry error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 취소
   */
  static async cancelPayment(paymentId: number, reason: string): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<PaymentResponse>(`/payments/${paymentId}/cancel`, { reason })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment cancellation error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 트랜잭션 이력 조회
   */
  static async getPaymentTransactions(paymentId: number): Promise<{
    success: boolean
    data?: PaymentTransaction[]
    error?: string
  }> {
    try {
      const response = await apiClient.get<PaymentTransaction[]>(`/payments/${paymentId}/transactions`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment transactions fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 영수증 다운로드
   */
  static async downloadReceipt(paymentId: number, format: 'pdf' | 'html' = 'pdf'): Promise<{
    success: boolean
    data?: Blob
    error?: string
  }> {
    try {
      const response = await apiClient.get(`/payments/${paymentId}/receipt?format=${format}`, {
        responseType: 'blob'
      })
      
      return {
        success: true,
        data: response.data as Blob
      }
    } catch (error: any) {
      console.error('Receipt download error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 통계 조회
   */
  static async getPaymentStats(period: string = 'month'): Promise<{
    success: boolean
    data?: PaymentStats
    error?: string
  }> {
    try {
      const response = await apiClient.get<PaymentStats>(`/payments/stats?period=${period}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment stats fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 수단별 통계 조회
   */
  static async getPaymentMethodStats(): Promise<{
    success: boolean
    data?: PaymentMethodStats[]
    error?: string
  }> {
    try {
      const response = await apiClient.get<PaymentMethodStats[]>('/payments/method-stats')
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment method stats fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 웹훅 처리 (결제 게이트웨이에서 오는 알림)
   */
  static async handleWebhook(webhookData: {
    gatewayTransactionId: string
    status: string
    amount?: number
    metadata?: Record<string, any>
  }): Promise<{
    success: boolean
    data?: PaymentResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<PaymentResponse>('/payments/webhook', webhookData)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Payment webhook error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 결제 게이트웨이 호출 (클라이언트 사이드)
   */
  static async initiateGatewayPayment(paymentId: number): Promise<{
    success: boolean
    data?: {
      gatewayUrl: string
      redirectMethod: 'GET' | 'POST'
      parameters: Record<string, string>
    }
    error?: string
  }> {
    try {
      const response = await apiClient.post<{
        gatewayUrl: string
        redirectMethod: 'GET' | 'POST'
        parameters: Record<string, string>
      }>(`/payments/${paymentId}/gateway-initiate`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Gateway initiation error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }
}