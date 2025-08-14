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

export interface EstimateCreateRequest {
  shippingMethod: string // 'sea' | 'air'
  carrier?: string
  serviceLevel?: string // 'standard' | 'express' | 'urgent'
  notes?: string
}

export interface EstimateResponse {
  id: number
  orderId: number
  orderCode: string
  version: number // 1차, 2차 견적
  status: string // 'pending' | 'approved' | 'rejected' | 'cancelled'
  shippingMethod: string
  carrier?: string
  serviceLevel?: string
  shippingCost: number
  localShippingCost: number
  repackingCost: number
  totalCost: number
  currency: string
  validUntil: string
  notes?: string
  responseNotes?: string
  createdAt: string
  updatedAt: string
  respondedAt?: string
}

export interface EstimateCalculationResponse {
  orderId: number
  totalCbm: number
  totalWeight: number
  shippingMethod: string
  carrier?: string
  serviceLevel?: string
  shippingCost: number
  localShippingCost: number
  repackingCost: number
  totalCost: number
  currency: string
}

export interface EstimateResponseRequest {
  action: string // 'approved' | 'rejected'
  notes?: string
}

export class SpringBootEstimatesService {
  
  // 견적 생성
  static async createEstimate(orderId: number, estimateData: EstimateCreateRequest): Promise<{ success: boolean; data?: EstimateResponse; error?: string }> {
    try {
      console.log('=== 견적 생성 시작 ===')
      console.log('주문 ID:', orderId, '견적 데이터:', estimateData)
      
      const response = await apiClient.post<EstimateResponse>(`/orders/${orderId}/estimates`, estimateData)
      
      console.log('견적 생성 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 생성 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 생성 중 오류가 발생했습니다.' 
      }
    }
  }

  // 주문별 견적 목록 조회
  static async getEstimates(orderId: number): Promise<{ success: boolean; data?: EstimateResponse[]; error?: string }> {
    try {
      console.log('=== 견적 목록 조회 시작 ===')
      console.log('주문 ID:', orderId)
      
      const response = await apiClient.get<EstimateResponse[]>(`/orders/${orderId}/estimates`)
      
      console.log('견적 목록 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 목록 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 목록 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 상세 조회
  static async getEstimate(estimateId: number): Promise<{ success: boolean; data?: EstimateResponse; error?: string }> {
    try {
      console.log('=== 견적 상세 조회 시작 ===')
      console.log('견적 ID:', estimateId)
      
      const response = await apiClient.get<EstimateResponse>(`/estimates/${estimateId}`)
      
      console.log('견적 상세 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 상세 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 상세 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 응답 (승인/거부)
  static async respondToEstimate(estimateId: number, responseData: EstimateResponseRequest): Promise<{ success: boolean; data?: EstimateResponse; error?: string }> {
    try {
      console.log('=== 견적 응답 시작 ===')
      console.log('견적 ID:', estimateId, '응답 데이터:', responseData)
      
      const response = await apiClient.post<EstimateResponse>(`/estimates/${estimateId}/respond`, responseData)
      
      console.log('견적 응답 결과:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 응답 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 응답 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 수정
  static async updateEstimate(estimateId: number, estimateData: EstimateCreateRequest): Promise<{ success: boolean; data?: EstimateResponse; error?: string }> {
    try {
      console.log('=== 견적 수정 시작 ===')
      console.log('견적 ID:', estimateId, '견적 데이터:', estimateData)
      
      const response = await apiClient.put<EstimateResponse>(`/estimates/${estimateId}`, estimateData)
      
      console.log('견적 수정 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 수정 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 수정 중 오류가 발생했습니다.' 
      }
    }
  }

  // 2차 견적 생성
  static async createSecondEstimate(firstEstimateId: number, estimateData: EstimateCreateRequest): Promise<{ success: boolean; data?: EstimateResponse; error?: string }> {
    try {
      console.log('=== 2차 견적 생성 시작 ===')
      console.log('1차 견적 ID:', firstEstimateId, '견적 데이터:', estimateData)
      
      const response = await apiClient.post<EstimateResponse>(`/estimates/${firstEstimateId}/second-estimate`, estimateData)
      
      console.log('2차 견적 생성 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('2차 견적 생성 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '2차 견적 생성 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 자동 계산 (시뮬레이션)
  static async calculateEstimate(orderId: number, shippingMethod: string, carrier?: string, serviceLevel?: string): Promise<{ success: boolean; data?: EstimateCalculationResponse; error?: string }> {
    try {
      console.log('=== 견적 계산 시작 ===')
      console.log('파라미터:', { orderId, shippingMethod, carrier, serviceLevel })
      
      const params = new URLSearchParams()
      params.append('shippingMethod', shippingMethod)
      if (carrier) params.append('carrier', carrier)
      if (serviceLevel) params.append('serviceLevel', serviceLevel)
      
      const response = await apiClient.get<EstimateCalculationResponse>(`/orders/${orderId}/estimate-calculation?${params}`)
      
      console.log('견적 계산 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 계산 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 계산 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 취소
  static async cancelEstimate(estimateId: number): Promise<{ success: boolean; error?: string }> {
    try {
      console.log('=== 견적 취소 시작 ===')
      console.log('견적 ID:', estimateId)
      
      await apiClient.delete(`/estimates/${estimateId}`)
      
      console.log('견적 취소 완료')
      
      return { 
        success: true
      }
    } catch (error: any) {
      console.error('견적 취소 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 취소 중 오류가 발생했습니다.' 
      }
    }
  }

  // 견적 상태별 통계
  static async getEstimateStats(): Promise<{ success: boolean; data?: any; error?: string }> {
    try {
      console.log('=== 견적 통계 조회 시작 ===')
      
      const response = await apiClient.get('/estimates/stats')
      
      console.log('견적 통계 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('견적 통계 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '견적 통계 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 배송비 견적 시뮬레이션 (클라이언트 사이드 유틸리티)
  static simulateShippingCost(cbm: number, weight: number, method: string = 'sea'): number {
    const baseCost = method === 'air' ? 500 * 1.5 : 500 // 기본 배송비
    const cbmCost = cbm * 200 // CBM당 요금
    const weightCost = weight * 10 // 무게당 요금
    
    return baseCost + cbmCost + weightCost
  }

  // 로컬 배송비 계산
  static calculateLocalShippingCost(): number {
    return 150 // 고정 로컬 배송비 THB
  }

  // 리패킹 비용 계산
  static calculateRepackingCost(needsRepacking: boolean): number {
    return needsRepacking ? 100 : 0 // 리패킹 필요시 THB 100
  }

  // 총 견적 비용 계산
  static calculateTotalCost(shippingCost: number, localShippingCost: number, repackingCost: number): number {
    return shippingCost + localShippingCost + repackingCost
  }
}

// 기본 export
export default SpringBootEstimatesService