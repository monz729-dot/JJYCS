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

export interface ScanRequest {
  labelCode: string
  scanType: string // 'inbound' | 'outbound' | 'hold' | 'mixbox' | 'inventory'
  warehouseId: number
  location?: string
  note?: string
  photos?: File[]
}

export interface ScanResponse {
  box: {
    boxId: number
    labelCode: string
    orderId: number
    status: string
    previousStatus: string
    cbm: number
    weight: number
  }
  warehouse: {
    id: number
    name: string
    code: string
  }
  timestamp: string
  nextAction: string
}

export interface BatchProcessRequest {
  labelCodes: string[]
  action: string // 'inbound' | 'outbound' | 'hold'
  warehouseId: number
  location?: string
  note?: string
}

export interface BatchProcessResponse {
  batchId: string
  processed: number
  failed: number
  results: ProcessResult[]
  summary: BatchSummary
}

export interface ProcessResult {
  labelCode: string
  status: string // 'success' | 'failed' | 'skipped'
  reason?: string
  newStatus?: string
  previousStatus?: string
}

export interface BatchSummary {
  totalBoxes: number
  totalOrders: number
  totalCbm: number
  totalWeight: number
  estimatedShippingCost: number
}

export interface InventoryResponse {
  boxId: number
  labelCode: string
  orderId: number
  orderCode: string
  status: string
  location: string
  inboundDate: string
  expectedOutboundDate?: string
  daysInWarehouse: number
  cbm: number
  weight: number
  customer: {
    name: string
    memberCode?: string
    role: string
  }
}

export interface ScanEventResponse {
  eventId: number
  eventType: string
  labelCode: string
  orderId: number
  orderCode: string
  previousStatus: string
  newStatus: string
  location: string
  batchId?: string
  deviceInfo: string
  notes?: string
  scanTimestamp: string
  scannedBy: {
    name: string
    role: string
  }
  warehouse: {
    name: string
    code: string
  }
}

export interface LabelResponse {
  boxId: number
  qrCodeUrl: string
  qrCodeData: string
  printable: boolean
  format: string
}

export interface BoxResponse {
  boxId: number
  labelCode: string
  status: string
  previousStatus: string
  note?: string
  updatedAt: string
}

export interface MixboxRequest {
  originalBoxIds: number[]
  newBoxDimensions: {
    widthCm: number
    heightCm: number
    depthCm: number
    weightKg: number
  }
  warehouseId: number
  location: string
  note?: string
}

export interface MixboxResponse {
  newBoxId: number
  newLabelCode: string
  originalBoxes: string[]
  spaceSaved: number
  costSaved: number
  success: boolean
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

export class SpringBootWarehouseService {
  
  // QR 스캔 처리
  static async scanBox(scanData: ScanRequest): Promise<{ success: boolean; data?: ScanResponse; error?: string }> {
    try {
      console.log('=== QR 스캔 시작 ===')
      console.log('스캔 데이터:', scanData)
      
      // FormData 사용 (사진 업로드 가능)
      const formData = new FormData()
      formData.append('labelCode', scanData.labelCode)
      formData.append('scanType', scanData.scanType)
      formData.append('warehouseId', scanData.warehouseId.toString())
      if (scanData.location) formData.append('location', scanData.location)
      if (scanData.note) formData.append('note', scanData.note)
      
      // 사진 파일들 추가
      if (scanData.photos && scanData.photos.length > 0) {
        scanData.photos.forEach((photo, index) => {
          formData.append(`photos`, photo)
        })
      }
      
      const response = await apiClient.post<ScanResponse>('/warehouse/scan', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      console.log('스캔 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('QR 스캔 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || 'QR 스캔 중 오류가 발생했습니다.' 
      }
    }
  }

  // 일괄 처리
  static async batchProcess(batchData: BatchProcessRequest): Promise<{ success: boolean; data?: BatchProcessResponse; error?: string }> {
    try {
      console.log('=== 일괄 처리 시작 ===')
      console.log('일괄 처리 데이터:', batchData)
      
      const response = await apiClient.post<BatchProcessResponse>('/warehouse/batch-process', batchData)
      
      console.log('일괄 처리 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('일괄 처리 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '일괄 처리 중 오류가 발생했습니다.' 
      }
    }
  }

  // 재고 조회
  static async getInventory(warehouseId?: number, status?: string, page: number = 0, size: number = 20): Promise<{ success: boolean; data?: PageResponse<InventoryResponse>; error?: string }> {
    try {
      console.log('=== 재고 조회 시작 ===')
      console.log('파라미터:', { warehouseId, status, page, size })
      
      const params = new URLSearchParams()
      if (warehouseId) params.append('warehouseId', warehouseId.toString())
      if (status) params.append('status', status)
      params.append('page', page.toString())
      params.append('size', size.toString())
      
      const response = await apiClient.get<PageResponse<InventoryResponse>>(`/warehouse/inventory?${params}`)
      
      console.log('재고 조회 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('재고 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '재고 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 박스 보류 처리
  static async holdBox(boxId: number, reason: string): Promise<{ success: boolean; data?: BoxResponse; error?: string }> {
    try {
      console.log('=== 박스 보류 시작 ===')
      console.log('박스 ID:', boxId, '사유:', reason)
      
      const response = await apiClient.post<BoxResponse>(`/warehouse/boxes/${boxId}/hold`, { reason })
      
      console.log('박스 보류 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('박스 보류 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '박스 보류 처리 중 오류가 발생했습니다.' 
      }
    }
  }

  // 믹스박스 생성
  static async createMixbox(mixboxData: MixboxRequest): Promise<{ success: boolean; data?: MixboxResponse; error?: string }> {
    try {
      console.log('=== 믹스박스 생성 시작 ===')
      console.log('믹스박스 데이터:', mixboxData)
      
      const response = await apiClient.post<MixboxResponse>('/warehouse/mixbox', mixboxData)
      
      console.log('믹스박스 생성 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('믹스박스 생성 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '믹스박스 생성 중 오류가 발생했습니다.' 
      }
    }
  }

  // 스캔 이력 조회
  static async getScanHistory(labelCode?: string, startDate?: string, endDate?: string, page: number = 0, size: number = 20): Promise<{ success: boolean; data?: PageResponse<ScanEventResponse>; error?: string }> {
    try {
      console.log('=== 스캔 이력 조회 시작 ===')
      console.log('파라미터:', { labelCode, startDate, endDate, page, size })
      
      const params = new URLSearchParams()
      if (labelCode) params.append('labelCode', labelCode)
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)
      params.append('page', page.toString())
      params.append('size', size.toString())
      
      const response = await apiClient.get<PageResponse<ScanEventResponse>>(`/warehouse/scan-history?${params}`)
      
      console.log('스캔 이력 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('스캔 이력 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '스캔 이력 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 라벨 생성
  static async generateLabel(boxId: number): Promise<{ success: boolean; data?: LabelResponse; error?: string }> {
    try {
      console.log('=== 라벨 생성 시작 ===')
      console.log('박스 ID:', boxId)
      
      const response = await apiClient.post<LabelResponse>(`/warehouse/labels/${boxId}`)
      
      console.log('라벨 생성 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('라벨 생성 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '라벨 생성 중 오류가 발생했습니다.' 
      }
    }
  }

  // 라벨 인쇄
  static async printLabel(boxId: number): Promise<{ success: boolean; error?: string }> {
    try {
      console.log('=== 라벨 인쇄 시작 ===')
      console.log('박스 ID:', boxId)
      
      await apiClient.post(`/warehouse/labels/${boxId}/print`)
      
      console.log('라벨 인쇄 요청 완료')
      
      return { 
        success: true
      }
    } catch (error: any) {
      console.error('라벨 인쇄 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '라벨 인쇄 중 오류가 발생했습니다.' 
      }
    }
  }

  // QR 코드 스캔 (카메라/파일 업로드)
  static async scanQRCode(file: File): Promise<{ success: boolean; data?: string; error?: string }> {
    try {
      console.log('=== QR 코드 스캔 시작 ===')
      
      const formData = new FormData()
      formData.append('qrImage', file)
      
      const response = await apiClient.post<{ qrData: string }>('/warehouse/scan-qr', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      console.log('QR 코드 스캔 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data.qrData 
      }
    } catch (error: any) {
      console.error('QR 코드 스캔 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || 'QR 코드 스캔 중 오류가 발생했습니다.' 
      }
    }
  }

  // 창고 대시보드 통계
  static async getWarehouseStats(warehouseId?: number): Promise<{ success: boolean; data?: any; error?: string }> {
    try {
      console.log('=== 창고 통계 조회 시작 ===')
      console.log('창고 ID:', warehouseId)
      
      const url = warehouseId ? `/warehouse/stats?warehouseId=${warehouseId}` : '/warehouse/stats'
      const response = await apiClient.get(url)
      
      console.log('창고 통계 응답:', response.data)
      
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error: any) {
      console.error('창고 통계 조회 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '창고 통계 조회 중 오류가 발생했습니다.' 
      }
    }
  }
}

// 기본 export
export default SpringBootWarehouseService