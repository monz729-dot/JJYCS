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

// 응답 인터셉터: 토큰 만료 시 자동 처리
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      // 리프레시 토큰으로 새 액세스 토큰 요청
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const refreshResponse = await apiClient.post('/auth/refresh', { refreshToken })
          const data = refreshResponse.data
          
          if (data.success && data.data) {
            const { accessToken } = data.data
            localStorage.setItem('accessToken', accessToken)
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            return apiClient(originalRequest)
          }
        } catch (refreshError) {
          // 리프레시 실패 시 로그아웃
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          window.location.href = '/auth/login'
        }
      } else {
        // 리프레시 토큰이 없으면 로그아웃
        localStorage.removeItem('accessToken')
        localStorage.removeItem('user')
        window.location.href = '/auth/login'
      }
    }
    return Promise.reject(error)
  }
)

// API 응답 타입 정의
interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  error?: string
  code?: string
}

export interface SignUpRequest {
  email: string
  password: string
  name: string
  phone?: string
  role: string // 'individual' | 'enterprise' | 'partner' | 'warehouse'
  memberCode?: string
  companyName?: string
  businessNumber?: string
  businessAddress?: string
  agreeTerms: boolean
  agreePrivacy: boolean
  agreeMarketing?: boolean
}

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: {
    id: number
    email: string
    name: string
    phone?: string
    role: string
    status: string
    memberCode?: string
    emailVerified: boolean
    twoFactorEnabled: boolean
    createdAt: string
  }
  requiresEmailVerification: boolean
  requiresApproval: boolean
  requiresTwoFactor: boolean
  message: string
}

export interface UserProfile {
  id: number
  email: string
  name: string
  phone?: string
  role: string
  status: string
  memberCode?: string
  emailVerified: boolean
  twoFactorEnabled: boolean
  createdAt: string
  updatedAt: string
}

export class SpringBootAuthService {
  
  // 회원가입
  static async signUp(data: SignUpRequest): Promise<{ success: boolean; data?: AuthResponse; error?: string }> {
    try {
      console.log('Sending signup request:', { ...data, password: '[HIDDEN]' })
      
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/signup', data)
      
      console.log('Signup response:', response.data)
      
      if (response.data.success && response.data.data) {
        const authData = response.data.data
        
        // JWT 토큰 저장
        if (authData.accessToken) {
          localStorage.setItem('accessToken', authData.accessToken)
          if (authData.refreshToken) {
            localStorage.setItem('refreshToken', authData.refreshToken)
          }
          localStorage.setItem('user', JSON.stringify(authData.user))
        }
        
        return { 
          success: true, 
          data: authData 
        }
      } else {
        return {
          success: false,
          error: response.data.message || response.data.error || '회원가입에 실패했습니다.'
        }
      }
    } catch (error: any) {
      console.error('회원가입 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '회원가입 중 오류가 발생했습니다.' 
      }
    }
  }

  // 로그인
  static async signIn(data: LoginRequest): Promise<{ success: boolean; data?: AuthResponse; error?: string }> {
    try {
      console.log('Sending login request:', { ...data, password: '[HIDDEN]' })
      
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data)
      
      console.log('Login response:', response.data)
      
      if (response.data.success && response.data.data) {
        const authData = response.data.data
        
        // JWT 토큰 저장
        if (authData.accessToken) {
          localStorage.setItem('accessToken', authData.accessToken)
          if (authData.refreshToken) {
            localStorage.setItem('refreshToken', authData.refreshToken)
          }
          localStorage.setItem('user', JSON.stringify(authData.user))
        }
        
        return { 
          success: true, 
          data: authData 
        }
      } else {
        return {
          success: false,
          error: response.data.message || response.data.error || '로그인에 실패했습니다.'
        }
      }
    } catch (error: any) {
      console.error('로그인 실패:', error)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || '로그인 중 오류가 발생했습니다.' 
      }
    }
  }

  // 로그아웃
  static async signOut(): Promise<{ success: boolean; error?: string }> {
    try {
      // 서버에 로그아웃 요청은 선택사항 (JWT는 stateless)
      // await apiClient.post('/auth/logout')
      
      // 로컬 스토리지 클리어
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      
      return { success: true }
    } catch (error: any) {
      // 로컬 데이터는 무조건 클리어
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      
      return { 
        success: false, 
        error: error.response?.data?.message || '로그아웃 중 오류가 발생했습니다.' 
      }
    }
  }

  // 현재 사용자 프로필 조회
  static async getCurrentUserProfile(): Promise<{ data?: UserProfile; error?: string }> {
    try {
      const response = await apiClient.get<ApiResponse<UserProfile>>('/users/me')
      
      if (response.data.success && response.data.data) {
        // 로컬 사용자 정보 업데이트
        localStorage.setItem('user', JSON.stringify(response.data.data))
        return { data: response.data.data }
      } else {
        return { error: response.data.message || '프로필 조회에 실패했습니다.' }
      }
    } catch (error: any) {
      return { 
        error: error.response?.data?.message || '프로필 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 프로필 업데이트
  static async updateProfile(profileData: { name?: string; phone?: string }): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.put<ApiResponse<UserProfile>>('/users/me', profileData)
      
      if (response.data.success && response.data.data) {
        // 로컬 저장된 사용자 정보 업데이트
        localStorage.setItem('user', JSON.stringify(response.data.data))
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '프로필 업데이트에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '프로필 업데이트 중 오류가 발생했습니다.' 
      }
    }
  }

  // 이메일 인증
  static async verifyEmail(token: string): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(`/auth/verify-email?token=${token}`)
      
      if (response.data.success) {
        // 로컬 사용자 정보 업데이트
        const currentUser = this.getCurrentUser()
        if (currentUser) {
          currentUser.emailVerified = true
          localStorage.setItem('user', JSON.stringify(currentUser))
        }
        
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '이메일 인증에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '이메일 인증 중 오류가 발생했습니다.' 
      }
    }
  }

  // 이메일 인증 재발송
  static async resendEmailVerification(): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<void>>('/users/me/resend-verification')
      
      if (response.data.success) {
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '이메일 재발송에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '이메일 재발송 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 재설정 요청
  static async requestPasswordReset(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(`/auth/forgot-password?email=${email}`)
      
      if (response.data.success) {
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '비밀번호 재설정 요청에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '비밀번호 재설정 요청 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 재설정
  static async resetPassword(token: string, newPassword: string): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(`/auth/reset-password?token=${token}&newPassword=${newPassword}`)
      
      if (response.data.success) {
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '비밀번호 재설정에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '비밀번호 재설정 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 변경
  static async changePassword(currentPassword: string, newPassword: string): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<void>>('/users/me/change-password', {
        currentPassword,
        newPassword
      })
      
      if (response.data.success) {
        return { success: true }
      } else {
        return { 
          success: false, 
          error: response.data.message || '비밀번호 변경에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '비밀번호 변경 중 오류가 발생했습니다.' 
      }
    }
  }

  // 2FA 설정
  static async setup2FA(): Promise<{ success: boolean; data?: any; error?: string }> {
    try {
      const response = await apiClient.post<ApiResponse<any>>('/auth/2fa/setup')
      
      if (response.data.success) {
        return { success: true, data: response.data.data }
      } else {
        return { 
          success: false, 
          error: response.data.message || '2FA 설정에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '2FA 설정 중 오류가 발생했습니다.' 
      }
    }
  }

  // 토큰 새로고침
  static async refreshToken(): Promise<{ success: boolean; data?: any; error?: string }> {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        return { success: false, error: '리프레시 토큰이 없습니다.' }
      }

      const response = await apiClient.post<ApiResponse<any>>('/auth/refresh', { refreshToken })
      
      if (response.data.success && response.data.data) {
        const { accessToken, refreshToken: newRefreshToken } = response.data.data
        
        localStorage.setItem('accessToken', accessToken)
        if (newRefreshToken) {
          localStorage.setItem('refreshToken', newRefreshToken)
        }
        
        return { success: true, data: response.data.data }
      } else {
        return { 
          success: false, 
          error: response.data.message || '토큰 새로고침에 실패했습니다.' 
        }
      }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '토큰 새로고침 중 오류가 발생했습니다.' 
      }
    }
  }

  // 로컬 저장된 사용자 정보 조회
  static getCurrentUser(): UserProfile | null {
    try {
      const userStr = localStorage.getItem('user')
      return userStr ? JSON.parse(userStr) : null
    } catch {
      return null
    }
  }

  // 토큰 유효성 검증
  static isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken')
    const user = this.getCurrentUser()
    
    if (!token || !user) {
      return false
    }
    
    // JWT 토큰 만료 검증은 서버에서 처리 (인터셉터에서 401 처리)
    return true
  }

  // 사용자 역할 확인
  static hasRole(role: string): boolean {
    const user = this.getCurrentUser()
    return user?.role === role
  }

  // 사용자 상태 확인
  static isApproved(): boolean {
    const user = this.getCurrentUser()
    return user?.status === 'active'
  }

  // 이메일 인증 상태 확인
  static isEmailVerified(): boolean {
    const user = this.getCurrentUser()
    return user?.emailVerified === true
  }

  // 사용자 타입별 대시보드 경로 반환
  static getDashboardPath(): string {
    const user = this.getCurrentUser()
    if (!user) return '/auth/login'
    
    switch (user.role) {
      case 'admin':
        return '/app/admin'
      case 'warehouse':
        return '/app/warehouse'
      case 'partner':
        return '/app/partner'
      default:
        return '/app/dashboard'
    }
  }

  // 승인 대기 상태 확인
  static isPendingApproval(): boolean {
    const user = this.getCurrentUser()
    return user?.status === 'pending_approval'
  }

  // 계정 상태 메시지 반환
  static getStatusMessage(): string {
    const user = this.getCurrentUser()
    if (!user) return ''
    
    switch (user.status) {
      case 'pending_approval':
        return '계정 승인 대기 중입니다. 승인까지 평일 1-2일이 소요됩니다.'
      case 'active':
        return user.emailVerified ? '' : '이메일 인증을 완료해주세요.'
      case 'suspended':
        return '계정이 일시 정지되었습니다. 관리자에게 문의하세요.'
      case 'deactivated':
        return '비활성화된 계정입니다.'
      default:
        return ''
    }
  }
}

// 기본 export
export default SpringBootAuthService