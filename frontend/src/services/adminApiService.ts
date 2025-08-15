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

export interface AdminUserApprovalRequest {
  userId: number
  action: 'approve' | 'reject'
  notes?: string
  adminId?: number
}

export interface AdminUserResponse {
  id: number
  name: string
  email: string
  phone?: string
  role: 'INDIVIDUAL' | 'ENTERPRISE' | 'PARTNER' | 'WAREHOUSE' | 'ADMIN'
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  emailVerified: boolean
  memberCode?: string
  registeredAt: string
  approvedAt?: string
  approvedBy?: string
  approvalNotes?: string
  enterpriseProfile?: {
    companyName: string
    businessNumber: string
    businessAddress: string
    contactPerson: string
    website?: string
  }
  partnerProfile?: {
    partnerType: 'AFFILIATE' | 'REFERRAL' | 'CORPORATE'
    businessLicense: string
    referralCode: string
    commissionRate: number
  }
  warehouseProfile?: {
    warehouseName: string
    location: string
    capacity: number
    managerName: string
    operatingHours: string
  }
}

export interface DashboardStats {
  totalUsers: number
  todayOrders: number
  todayRevenue: number
  completedDeliveries: number
  userGrowthRate: number
  orderGrowthRate: number
  revenueGrowthRate: number
  deliveryGrowthRate: number
}

export interface OrderStatusCount {
  status: string
  count: number
  percentage: number
}

export interface RecentOrder {
  id: number
  orderNumber: string
  customerName: string
  totalAmount: number
  currency: string
  status: string
  createdAt: string
}

export interface RecentUser {
  id: number
  name: string
  email: string
  role: string
  createdAt: string
}

export interface SystemStatus {
  name: string
  status: 'online' | 'offline'
  lastChecked: string
}

export interface DashboardData {
  stats: DashboardStats
  orderStatusCounts: OrderStatusCount[]
  recentOrders: RecentOrder[]
  recentUsers: RecentUser[]
  systemStatus: SystemStatus[]
}

export interface UserApprovalStats {
  pending: number
  approved: number
  rejected: number
  todayApproved: number
  averageApprovalTime: number // in hours
}

export default class SpringBootAdminService {
  /**
   * 사용자 승인 관리 - 승인 대기 목록 조회
   */
  static async getPendingUsers(params: {
    page?: number
    size?: number
    role?: string
    status?: string
    search?: string
  } = {}): Promise<{
    success: boolean
    data?: {
      content: AdminUserResponse[]
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
      if (params.role) queryParams.append('role', params.role)
      if (params.status) queryParams.append('status', params.status)
      if (params.search) queryParams.append('search', params.search)

      const response = await apiClient.get<{
        content: AdminUserResponse[]
        page: number
        size: number
        totalElements: number
        totalPages: number
      }>(`/admin/users/pending?${queryParams.toString()}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Pending users fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 사용자 승인/거절
   */
  static async processUserApproval(request: AdminUserApprovalRequest): Promise<{
    success: boolean
    data?: AdminUserResponse
    error?: string
  }> {
    try {
      const response = await apiClient.post<AdminUserResponse>('/admin/users/approval', request)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('User approval processing error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 일괄 승인
   */
  static async bulkApproveUsers(userIds: number[], notes?: string): Promise<{
    success: boolean
    data?: {
      successful: number
      failed: number
      results: Array<{ userId: number; success: boolean; error?: string }>
    }
    error?: string
  }> {
    try {
      const response = await apiClient.post<{
        processedCount: number
        results: AdminUserResponse[]
      }>('/admin/users/bulk-approval', {
        userIds,
        action: 'approve',
        notes
      })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Bulk approval error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 일괄 거절
   */
  static async bulkRejectUsers(userIds: number[], notes: string): Promise<{
    success: boolean
    data?: {
      successful: number
      failed: number
      results: Array<{ userId: number; success: boolean; error?: string }>
    }
    error?: string
  }> {
    try {
      const response = await apiClient.post<{
        processedCount: number
        results: AdminUserResponse[]
      }>('/admin/users/bulk-approval', {
        userIds,
        action: 'reject',
        notes
      })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Bulk rejection error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 사용자 승인 통계 조회
   */
  static async getUserApprovalStats(): Promise<{
    success: boolean
    data?: UserApprovalStats
    error?: string
  }> {
    try {
      const response = await apiClient.get<{
        pendingCount: number
        approvedCount: number
        rejectedCount: number
        todayApprovals: number
      }>('/admin/users/approval-stats')
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Approval stats fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 사용자 상세 정보 조회 (관리자용)
   */
  static async getAdminUserDetail(userId: number): Promise<{
    success: boolean
    data?: AdminUserResponse
    error?: string
  }> {
    try {
      const response = await apiClient.get<AdminUserResponse>(`/admin/users/${userId}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('User detail fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 사용자 역할 변경
   */
  static async updateUserRole(userId: number, newRole: string): Promise<{
    success: boolean
    data?: AdminUserResponse
    error?: string
  }> {
    try {
      const response = await apiClient.put<AdminUserResponse>(`/admin/users/${userId}/role`, { role: newRole })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('User role update error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 사용자 계정 비활성화/활성화
   */
  static async toggleUserStatus(userId: number, active: boolean): Promise<{
    success: boolean
    data?: AdminUserResponse
    error?: string
  }> {
    try {
      const response = await apiClient.put<AdminUserResponse>(`/admin/users/${userId}/status`, { active })
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('User status update error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 승인 이력 조회
   */
  static async getApprovalHistory(params: {
    page?: number
    size?: number
    startDate?: string
    endDate?: string
    adminId?: number
  } = {}): Promise<{
    success: boolean
    data?: {
      content: Array<{
        id: number
        userId: number
        userName: string
        userEmail: string
        action: string
        notes?: string
        processedBy: string
        processedAt: string
      }>
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
      if (params.startDate) queryParams.append('startDate', params.startDate)
      if (params.endDate) queryParams.append('endDate', params.endDate)
      if (params.adminId) queryParams.append('adminId', params.adminId.toString())

      const response = await apiClient.get<{
        content: any[]
        page: number
        size: number
        totalElements: number
        totalPages: number
      }>(`/admin/users/approval-history?${queryParams.toString()}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Approval history fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 승인 알림 발송
   */
  static async sendApprovalNotification(userId: number, type: 'approved' | 'rejected'): Promise<{
    success: boolean
    error?: string
  }> {
    try {
      await apiClient.post(`/admin/users/${userId}/send-approval-notification`, { type })
      
      return {
        success: true
      }
    } catch (error: any) {
      console.error('Notification send error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }

  /**
   * 대시보드 전체 데이터 로드
   */
  async getDashboardData(period: string = 'today'): Promise<{
    success: boolean
    data?: DashboardData
    error?: string
  }> {
    try {
      const response = await apiClient.get<DashboardStats>(`/admin/dashboard?period=${period}`)
      
      return {
        success: true,
        data: response.data
      }
    } catch (error: any) {
      console.error('Dashboard data fetch error:', error)
      return {
        success: false,
        error: error.message || 'Network error occurred'
      }
    }
  }
}