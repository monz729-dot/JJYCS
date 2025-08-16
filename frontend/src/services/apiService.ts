// Spring Boot 백엔드 API 서비스
const API_BASE_URL = 'http://localhost:8080/api'

interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: {
    message: string
    code: string
    details?: any
  }
  timestamp?: number
}

interface LoginRequest {
  email: string
  password: string
}

interface LoginResponse {
  token: string
  refreshToken: string
  user: {
    id: number
    email: string
    name: string
    role: string
    memberCode?: string
  }
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
    const token = localStorage.getItem('accessToken')
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

      const data = await response.json()
      return data
    } catch (error) {
      console.error('API 요청 실패:', error)
      return {
        success: false,
        error: {
          message: '네트워크 오류가 발생했습니다.',
          code: 'NETWORK_ERROR'
        }
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
}

export { ApiService, type LoginRequest, type LoginResponse, type User, type Order, type ApiResponse }