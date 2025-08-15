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

export interface TrackingEvent {
  id: number
  trackingId: number
  eventType: 'CREATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'CANCELLED'
  title: string
  description: string
  details?: string
  location?: string
  timestamp: string
  metadata?: Record<string, any>
}

export interface TrackingRecipient {
  name: string
  address: string
  phone: string
  email?: string
}

export interface TrackingSender {
  name: string
  address: string
  phone: string
  email?: string
}

export interface TrackingPackage {
  id: number
  packageNumber?: string
  status: string
  dimensions: {
    width: number
    height: number
    depth: number
  }
  weight: number
  cbm: number
  items?: Array<{
    description: string
    quantity: number
    value: number
    currency: string
  }>
}

export interface TrackingResponse {
  id: number
  orderNumber: string
  trackingNumber: string
  emsTrackingNumber?: string
  status: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'IN_TRANSIT' | 'CUSTOMS' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'EXCEPTION' | 'RETURNED'
  shippingMethod: 'air' | 'sea'
  carrierName: string
  origin: string
  destination: string
  sender: TrackingSender
  recipient: TrackingRecipient
  packages: TrackingPackage[]
  events: TrackingEvent[]
  totalWeight: number
  totalCbm: number
  estimatedDelivery?: string
  actualDelivery?: string
  createdAt: string
  updatedAt: string
  metadata?: Record<string, any>
}

export interface TrackingListItem {
  id: number
  orderNumber: string
  trackingNumber: string
  emsTrackingNumber?: string
  status: string
  recipientName: string
  destination: string
  shippingMethod: string
  carrierName: string
  estimatedDelivery?: string
  createdAt: string
  updatedAt: string
}

export interface TrackingSearchParams {
  page?: number
  size?: number
  status?: string
  shippingMethod?: string
  carrierName?: string
  startDate?: string
  endDate?: string
  recipientName?: string
  destination?: string
}

export interface TrackingStats {
  totalShipments: number
  inTransit: number
  delivered: number
  exceptions: number
  averageDeliveryTime: number // in days
  onTimeDeliveryRate: number // percentage
}

export interface DeliveryConfirmationRequest {
  trackingId: number
  recipientName: string
  signature?: string
  photos?: string[]
  notes?: string
}

export interface IssueReportRequest {
  trackingId: number
  issueType: 'DELAYED' | 'DAMAGED' | 'LOST' | 'INCORRECT_ADDRESS' | 'CUSTOMS_ISSUE' | 'OTHER'
  description: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  photos?: string[]
  contactMethod: 'EMAIL' | 'PHONE' | 'SMS'
}

export default class SpringBootTrackingService {
  /**
   * 배송 추적 정보 조회
   */
  static async getTrackingByNumber(trackingNumber: string): Promise<{
    success: boolean
    data?: TrackingResponse
    error?: string
  }> {
    try {
      const response = await apiClient.get<TrackingResponse>(`/tracking/${trackingNumber}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Tracking fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 추적 목록 조회
   */
  static async getTrackingList(params: TrackingSearchParams = {}): Promise<{
    success: boolean
    data?: {
      content: TrackingListItem[]
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
      if (params.shippingMethod) queryParams.append('shippingMethod', params.shippingMethod)
      if (params.carrierName) queryParams.append('carrierName', params.carrierName)
      if (params.startDate) queryParams.append('startDate', params.startDate)
      if (params.endDate) queryParams.append('endDate', params.endDate)
      if (params.recipientName) queryParams.append('recipientName', params.recipientName)
      if (params.destination) queryParams.append('destination', params.destination)

      const response = await apiClient.get<{
        content: TrackingListItem[]
        page: number
        size: number
        totalElements: number
        totalPages: number
      }>(`/tracking?${queryParams.toString()}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Tracking list fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * EMS 추적 정보 동기화
   */
  static async syncEmsTracking(trackingId: number): Promise<{
    success: boolean
    data?: TrackingResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<TrackingResponse>(`/tracking/${trackingId}/sync-ems`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('EMS sync error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 상태 업데이트
   */
  static async updateTrackingStatus(trackingId: number, status: string, notes?: string): Promise<{
    success: boolean
    data?: TrackingResponse
    error?: string
  }> {
    try {
      const response = await apiClient.put<TrackingResponse>(`/tracking/${trackingId}/status`, { 
        status, 
        notes 
      })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Tracking status update error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 이벤트 추가
   */
  static async addTrackingEvent(trackingId: number, event: {
    eventType: string
    title: string
    description: string
    location?: string
    details?: string
  }): Promise<{
    success: boolean
    data?: TrackingEvent
    error?: string
  }> {
    try {
      const response = await apiClient.post<TrackingEvent>(`/tracking/${trackingId}/events`, event)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Tracking event add error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 완료 확인
   */
  static async confirmDelivery(request: DeliveryConfirmationRequest): Promise<{
    success: boolean
    data?: TrackingResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<TrackingResponse>('/tracking/confirm-delivery', request)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Delivery confirmation error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 문제 신고
   */
  static async reportIssue(request: IssueReportRequest): Promise<{
    success: boolean
    data?: {
      issueId: number
      status: string
      ticketNumber: string
    }
    error?: string
  }> {
    try {
      const response = await apiClient.post<{
        issueId: number
        status: string
        ticketNumber: string
      }>('/tracking/report-issue', request)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Issue report error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 통계 조회
   */
  static async getTrackingStats(period: string = 'month'): Promise<{
    success: boolean
    data?: TrackingStats
    error?: string
  }> {
    try {
      const response = await apiClient.get<TrackingStats>(`/tracking/stats?period=${period}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Tracking stats fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 배송 예상 시간 계산
   */
  static async calculateEstimatedDelivery(params: {
    origin: string
    destination: string
    shippingMethod: 'air' | 'sea'
    weight: number
    cbm: number
  }): Promise<{
    success: boolean
    data?: {
      estimatedDays: number
      estimatedDelivery: string
      confidence: 'HIGH' | 'MEDIUM' | 'LOW'
      factors: string[]
    }
    error?: string
  }> {
    try {
      const response = await apiClient.post<{
        estimatedDays: number
        estimatedDelivery: string
        confidence: 'HIGH' | 'MEDIUM' | 'LOW'
        factors: string[]
      }>('/tracking/estimate-delivery', params)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Delivery estimation error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 대량 추적 번호 조회
   */
  static async bulkTrackingLookup(trackingNumbers: string[]): Promise<{
    success: boolean
    data?: Array<{
      trackingNumber: string
      found: boolean
      data?: TrackingResponse
      error?: string
    }>
    error?: string
  }> {
    try {
      const response = await apiClient.post<Array<{
        trackingNumber: string
        found: boolean
        data?: TrackingResponse
        error?: string
      }>>('/tracking/bulk-lookup', { trackingNumbers })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Bulk tracking lookup error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 추적 알림 설정
   */
  static async setTrackingNotification(trackingId: number, settings: {
    emailNotifications: boolean
    smsNotifications: boolean
    pushNotifications: boolean
    notifyOnStatusChange: boolean
    notifyOnDelivery: boolean
    notifyOnException: boolean
  }): Promise<{
    success: boolean
    error?: string
  }> {
    try {
      await apiClient.put(`/tracking/${trackingId}/notifications`, settings)
      
      return {
        success: true
      }
    } catch (error: any) {
      console.error('Tracking notification setting error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }
}