import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse, PaginatedResponse } from '../types'
import { tokenStorage } from './tokenStorage'

class ApiClient {
  private client: AxiosInstance
  private isRefreshing = false
  private failedQueue: { resolve: (value?: any) => void; reject: (reason?: any) => void }[] = []

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 15000,
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
    this.setupDebugInterceptor()
  }

  private setupInterceptors() {
    // Request interceptor for auth token
    this.client.interceptors.request.use(
      (config) => {
        const token = tokenStorage.getToken()
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // Response interceptor for error handling with token refresh
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        return response
      },
      async (error) => {
        const originalRequest = error.config
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          if (this.isRefreshing) {
            // If already refreshing, queue this request
            return new Promise((resolve, reject) => {
              this.failedQueue.push({ resolve, reject })
            }).then(token => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              return this.client(originalRequest)
            }).catch(err => {
              return Promise.reject(err)
            })
          }

          originalRequest._retry = true
          this.isRefreshing = true

          try {
            const refreshToken = tokenStorage.getRefreshToken()
            if (!refreshToken) {
              throw new Error('No refresh token available')
            }

            const response = await this.client.post('/auth/refresh', { refreshToken })
            const { accessToken } = response.data
            
            // Store new tokens
            tokenStorage.setToken(accessToken)
            
            // Update default header
            this.client.defaults.headers.Authorization = `Bearer ${accessToken}`
            
            // Process failed queue
            this.failedQueue.forEach(({ resolve }) => resolve(accessToken))
            this.failedQueue = []
            
            // Retry original request
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            return this.client(originalRequest)
            
          } catch (refreshError) {
            // Refresh failed, logout user
            this.failedQueue.forEach(({ reject }) => reject(refreshError))
            this.failedQueue = []
            
            tokenStorage.removeTokens()
            localStorage.removeItem('user')
            
            // Redirect to login
            if (typeof window !== 'undefined') {
              window.location.href = '/login'
            }
            
            return Promise.reject(refreshError)
          } finally {
            this.isRefreshing = false
          }
        }
        
        return Promise.reject(error)
      }
    )
  }

  private setupDebugInterceptor() {
    this.client.interceptors.request.use((cfg) => {
      console.log('[API]', cfg.method?.toUpperCase(), (this.client.defaults.baseURL || '') + (cfg.url || ''))
      return cfg
    })
  }

  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.get(url, config)
      // Return the Axios response data directly, which contains the backend's response
      return {
        success: true,
        data: response.data,
        error: null
      }
    } catch (error: any) {
      return this.handleError(error)
    }
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.post(url, data, config)
      return {
        success: true,
        data: response.data,
        error: null
      }
    } catch (error: any) {
      return this.handleError(error)
    }
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.put(url, data, config)
      return {
        success: true,
        data: response.data,
        error: null
      }
    } catch (error: any) {
      return this.handleError(error)
    }
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.patch(url, data, config)
      return {
        success: true,
        data: response.data,
        error: null
      }
    } catch (error: any) {
      return this.handleError(error)
    }
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.client.delete(url, config)
      return {
        success: true,
        data: response.data,
        error: null
      }
    } catch (error: any) {
      return this.handleError(error)
    }
  }

  private handleError(error: any): ApiResponse {
    const response = error.response
    
    if (response?.data) {
      return {
        success: false,
        error: response.data.error || response.data.message || 'An error occurred',
        data: null
      }
    }

    if (error.code === 'ECONNABORTED') {
      return {
        success: false,
        error: 'Request timeout. Please try again.',
        data: null
      }
    }

    if (!navigator.onLine) {
      return {
        success: false,
        error: 'No internet connection. Please check your network.',
        data: null
      }
    }

    return {
      success: false,
      error: error.message || 'Network error occurred',
      data: null
    }
  }
}

// Create singleton instance
const api = new ApiClient()
export default api



// Auth API
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),
  
  register: (data: any) =>
    api.post('/auth/signup', data),
  
  logout: () =>
    api.post('/auth/logout'),
  
  getCurrentUser: () =>
    api.get('/auth/me'),
  
  refreshToken: (refreshToken: string) =>
    api.post('/auth/refresh', { refreshToken }),
  
  verifyEmail: (token: string) =>
    api.post(`/auth/verify-email/${token}`),
  
  forgotPassword: (email: string) =>
    api.post('/auth/forgot-password', { email }),
  
  resetPassword: (token: string, password: string) =>
    api.post('/auth/reset-password', { token, password })
}

// User Dashboard API
export const dashboardApi = {
  getUserDashboard: (userId: number) =>
    api.get(`/dashboard/user/${userId}`),
  
  getUserOrders: (userId: number, params: any = {}) =>
    api.get(`/orders/user/${userId}`, { params }),
  
  getOrderTracking: (userId: number, orderNumber: string) =>
    api.get(`/dashboard/user/${userId}/tracking/${orderNumber}`),
  
  getUserStats: (userId: number) =>
    api.get(`/dashboard/user/${userId}/stats`),
  
  getUserSummary: (userId: number) =>
    api.get(`/dashboard/user/${userId}/summary`),
  
  getUserAlerts: (userId: number) =>
    api.get(`/dashboard/user/${userId}/alerts`),
  
  getRecentActivity: (userId: number) =>
    api.get(`/dashboard/user/${userId}/recent-activity`),

  // Role-based dashboard API
  getDashboardData: (userType: string, userId?: number) => {
    if (userType === 'ADMIN') {
      return api.get('/admin/stats')
    } else {
      return api.get(`/dashboard/user/${userId}`)
    }
  }
}

// Partner Dashboard API - 파트너는 이제 일반 주문 API 사용
// partnerApi 제거됨 - 파트너도 ordersApi와 dashboardApi 사용

// Orders API
export const ordersApi = {
  createOrder: (data: any) =>
    api.post('/orders', data),
  
  getOrders: (page: number = 1, pageSize: number = 20, filters?: any) =>
    api.get('/orders', { params: { page, pageSize, ...filters } }),
  
  getOrderById: (id: number) =>
    api.get(`/orders/${id}`),
  
  getOrder: (id: number) =>
    api.get(`/orders/${id}`),
  
  getOrderByNumber: (orderNumber: string) =>
    api.get(`/orders/number/${orderNumber}`),
  
  getUserOrders: (userId: number, page: number = 0, size: number = 50) =>
    api.get(`/orders/user/${userId}`, { params: { page, size } }),
  
  getMyOrders: (page: number = 0, size: number = 50) =>
    api.get('/orders/user/me', { params: { page, size } }),
  
  getMyOrderStats: () =>
    api.get('/orders/user/me/stats'),
  
  updateOrder: (id: number, data: any) =>
    api.put(`/orders/${id}`, data),
  
  updateOrderStatus: (id: number, status: string) =>
    api.put(`/orders/${id}/status`, { status }),
  
  deleteOrder: (id: number) =>
    api.delete(`/orders/${id}`),
  
  searchOrders: (params: any = {}) =>
    api.get('/orders/search', { params }),
  
  trackOrder: (trackingNumber: string) =>
    api.get(`/tracking/orders/${trackingNumber}`),
  
  getOrderTracking: (orderNumber: string) =>
    api.get(`/orders/tracking/${orderNumber}`),
  
  getEstimate: (orderItems: any[]) =>
    api.post('/orders/estimate', { orderItems }),
  
  validateBusinessRules: (data: any) =>
    api.post('/orders/validate', data)
}

// Warehouse API
export const warehouseApi = {
  // Scanning
  scanInbound: (data: any) =>
    api.post('/warehouse/scan/inbound', data),
  
  scanOutbound: (data: any) =>
    api.post('/warehouse/scan/outbound', data),
  
  scanItem: (data: any) =>
    api.post('/warehouse/scan', data),
  
  batchProcess: (data: any) =>
    api.post('/warehouse/batch', data),

  // Inventory Management
  getInventory: (warehouseId?: number, status?: string) =>
    api.get(`/warehouse/${warehouseId || 1}/inventory`, { params: { status } }),
  
  receiveOrder: (data: any) =>
    api.post('/warehouse/management/receive', data),
  
  inspectInventory: (inventoryId: number, data: any) =>
    api.post(`/warehouse/management/inspect/${inventoryId}`, data),
  
  shipInventory: (inventoryId: number, data: any) =>
    api.post(`/warehouse/management/ship/${inventoryId}`, data),
  
  batchShipInventories: (inventoryIds: number[], shippedBy: string) =>
    api.post('/warehouse/management/ship/batch', { inventoryIds, shippedBy }),
  
  holdInventory: (inventoryId: number, reason: string) =>
    api.post(`/warehouse/management/hold/${inventoryId}`, { reason }),
  
  reportDamage: (inventoryId: number, damageReport: string) =>
    api.post(`/warehouse/management/damage/${inventoryId}`, { damageReport }),
  
  searchInventory: (code: string) =>
    api.get('/warehouse/management/inventory/search', { params: { code } }),
  
  updateInventoryLocation: (inventoryId: number, location: any) =>
    api.put(`/warehouse/inventory/${inventoryId}/location`, location),
  
  getStatistics: (warehouseId: number, startDate: string, endDate: string) =>
    api.get(`/warehouse/management/${warehouseId}/statistics`, { 
      params: { startDate, endDate } 
    }),

  // Legacy methods
  getLocations: () =>
    api.get('/warehouse/locations'),
  
  assignLocation: (orderNumber: string, location: string) =>
    api.post('/warehouse/assign-location', { orderNumber, location })
}

// Labels API
export const labelsApi = {
  // Legacy methods
  generateOrderQR: (orderNumber: string) =>
    api.post(`/labels/generate/order/${orderNumber}`),
  
  generateBatchQR: (orderNumbers: string[]) =>
    api.post('/labels/generate/batch', { orderNumbers }),
  
  parseQR: (qrString: string) =>
    api.post('/labels/parse', { qrString }),
  
  printLabels: (data: any) =>
    api.post('/labels/print', data),
  
  // New label generation methods
  generateOrderQRImage: (data: { orderId: number; orderCode: string }) =>
    api.post('/labels/api/qr/order', data),
  
  generateInventoryQRImage: (data: { inventoryCode: string }) =>
    api.post('/labels/api/qr/inventory', data),
  
  generateShippingLabel: (data: any) =>
    api.post('/labels/api/shipping/label', data),
  
  generateWarehouseInboundLabel: (data: any) =>
    api.post('/labels/api/warehouse/inbound', data),
  
  generateWarehouseOutboundLabel: (data: any) =>
    api.post('/labels/api/warehouse/outbound', data),
  
  generateInventoryTagLabel: (data: { inventoryCode: string }) =>
    api.post('/labels/api/inventory/tag', data),
  
  generateBarcode: (data: { data: string; barcodeType?: string }) =>
    api.post('/labels/api/barcode', data)
}

// Billing API
export const billingApi = {
  generateEstimate: (orderId: number, type: 'PROFORMA' | 'ADDITIONAL' = 'PROFORMA') =>
    api.post(`/billing/${orderId}/estimate`, { type }),
  
  previewEstimate: (orderId: number, data: any) =>
    api.post(`/billing/${orderId}/preview`, data),
  
  confirmPayment: (orderId: number, data: any) =>
    api.post(`/billing/${orderId}/payment`, data),
  
  getBillingHistory: (userId: number, params: any = {}) =>
    api.get(`/billing/history/${userId}`, { params })
}

// Admin API
export const adminApi = {
  getUsers: (params: any = {}) =>
    api.get('/admin/users', { params }),
  
  approveUser: (userId: number, data: any) =>
    api.post(`/admin/users/${userId}/approve`, data),
  
  rejectUser: (userId: number, data: any) =>
    api.post(`/admin/users/${userId}/reject`, data),
  
  updateUser: (userId: number, data: any) =>
    api.put(`/admin/users/${userId}`, data),
  
  getPendingApprovals: () =>
    api.get('/admin/users/pending'),
  
  getSystemStats: () =>
    api.get('/admin/stats'),

  // Order Management
  getAllOrders: (params: any = {}) =>
    api.get('/admin/orders', { params }),
  
  updateOrderStatus: (orderId: number, status: string) =>
    api.put(`/admin/orders/${orderId}/status`, { status }),
  
  getOrderDetails: (orderId: number) =>
    api.get(`/admin/orders/${orderId}`),
  
  searchOrders: (params: any = {}) =>
    api.get('/admin/orders/search', { params })
}

// HS Code API
export const hsCodeApi = {
  searchByProduct: (productName: string) =>
    api.get('/hscode/search/by-product', { params: { productName } }),
  
  searchByHSCode: (hsCode: string) =>
    api.get('/hscode/search/by-hscode', { params: { hsCode } }),
  
  getTariffRate: (hsCode: string) =>
    api.get(`/hscode/tariff/${hsCode}`),
  
  getExchangeRate: () =>
    api.get('/hscode/exchange-rate'),
  
  calculateDuty: (data: {
    hsCode: string;
    quantity: number;
    value: number;
    currency?: string;
  }) =>
    api.post('/hscode/calculate-duty', data)
}