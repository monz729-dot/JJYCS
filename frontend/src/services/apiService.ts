// Spring Boot 백엔드 API 서비스
const API_BASE_URL = 'http://localhost:8080/api'

interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
}

interface LoginRequest {
  email: string
  password: string
}

interface LoginResponse {
  accessToken: string
  refreshToken?: string
  tokenType: string
  expiresIn: number
  user: {
    id: number
    email: string
    name: string
    role: string
    status: string
    phone: string
    memberCode: string
    emailVerified: boolean
    twoFactorEnabled: boolean
    createdAt: string
  }
  requiresTwoFactor: boolean
  requiresEmailVerification: boolean
  requiresApproval: boolean
  message: string
}

interface User {
  id: number
  email: string
  name: string
  phone?: string
  role: string
  status: string
  memberCode?: string
  createdAt: string
  emailVerified: boolean
}

interface Order {
  orderId: number
  orderCode: string
  status: string
  orderType: string
  totalAmount: number
  currency: string
  recipientName: string
  recipientCountry: string
  createdAt: string
  requiresExtraRecipient: boolean
}

class ApiService {
  private static getAuthHeaders(): Record<string, string> {
    const token = localStorage.getItem('access_token')
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    }
  }

  private static async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
          ...this.getAuthHeaders(),
          ...options.headers
        }
      })

      const result = await response.json()
      
      // 백엔드에서 항상 { success, data?, error? } 구조로 응답
      if (result.success) {
        return {
          success: true,
          data: result.data
        }
      } else {
        return {
          success: false,
          error: result.error?.message || result.message || 'Unknown error'
        }
      }
    } catch (error) {
      console.error('API 요청 실패:', error)
      return {
        success: false,
        error: '네트워크 오류가 발생했습니다.'
      }
    }
  }

  // 인증 관련
  static async login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    })
  }

  static async logout(): Promise<ApiResponse<void>> {
    return this.request<void>('/auth/logout', {
      method: 'POST'
    })
  }

  static async getCurrentUser(): Promise<ApiResponse<User>> {
    return this.request<User>('/auth/me')
  }

  // 사용자 관리 (관리자용)
  static async getUsers(page: number = 0, size: number = 20): Promise<ApiResponse<{
    content: User[]
    totalElements: number
    totalPages: number
    currentPage: number
  }>> {
    return this.request<any>(`/admin/users?page=${page}&size=${size}`)
  }

  // 주문 관리
  static async getOrders(page: number = 0, size: number = 20): Promise<ApiResponse<{
    content: Order[]
    totalElements: number
    totalPages: number
    currentPage: number
  }>> {
    return this.request<any>(`/orders?page=${page}&size=${size}`)
  }

  static async getOrderById(orderId: number): Promise<ApiResponse<Order>> {
    return this.request<Order>(`/orders/${orderId}`)
  }

  // 관리자 주문 조회
  static async getAdminOrders(page: number = 0, size: number = 20): Promise<ApiResponse<{
    content: Order[]
    totalElements: number
    totalPages: number
    currentPage: number
  }>> {
    return this.request<any>(`/admin/orders?page=${page}&size=${size}`)
  }

  // 편의 메서드들
  static async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'GET' })
  }

  static async post<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  static async put<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined
    })
  }

  static async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'DELETE' })
  }
}

export { ApiService, type LoginRequest, type LoginResponse, type User, type Order, type ApiResponse }