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

// 응답 인터셉터: 토큰 만료 시 자동 로그아웃
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

export interface SignUpRequest {
  email: string
  password: string
  name: string
  phone?: string
  role: string // 'INDIVIDUAL' | 'ENTERPRISE' | 'PARTNER' | 'WAREHOUSE'
  memberCode?: string
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
  tokenType: string
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
      console.log('=== Spring Boot 회원가입 시작 ===')
      console.log('회원가입 데이터:', data)
      
      const response = await apiClient.post<AuthResponse>('/auth/signup', data)
      
      console.log('회원가입 응답:', response.data)
      
      // JWT 토큰 저장
      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken)
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }
      
      return { 
        success: true, 
        data: response.data 
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
      console.log('=== Spring Boot 로그인 시작 ===')
      console.log('로그인 데이터:', { email: data.email, password: '***' })
      
      const response = await apiClient.post<AuthResponse>('/auth/login', data)
      
      console.log('로그인 응답:', response.data)
      
      // JWT 토큰 저장
      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken)
        localStorage.setItem('user', JSON.stringify(response.data.user))
      }
      
      return { 
        success: true, 
        data: response.data 
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
      localStorage.removeItem('user')
      
      return { success: true }
    } catch (error: any) {
      // 로컬 데이터는 무조건 클리어
      localStorage.removeItem('accessToken')
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
      const response = await apiClient.get<UserProfile>('/users/me')
      return { data: response.data }
    } catch (error: any) {
      return { 
        error: error.response?.data?.message || '프로필 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 프로필 업데이트
  static async updateProfile(profileData: Partial<UserProfile>): Promise<{ success: boolean; error?: string }> {
    try {
      const response = await apiClient.put('/users/me', profileData)
      
      // 로컬 저장된 사용자 정보 업데이트
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      const updatedUser = { ...currentUser, ...profileData }
      localStorage.setItem('user', JSON.stringify(updatedUser))
      
      return { success: true }
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
      await apiClient.get(`/auth/verify-email?token=${token}`)
      
      // 로컬 사용자 정보 업데이트
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
      currentUser.emailVerified = true
      localStorage.setItem('user', JSON.stringify(currentUser))
      
      return { success: true }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '이메일 인증 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 재설정 요청
  static async requestPasswordReset(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      await apiClient.post('/auth/password-reset-request', { email })
      return { success: true }
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
      await apiClient.post('/auth/password-reset', { token, newPassword })
      return { success: true }
    } catch (error: any) {
      return { 
        success: false, 
        error: error.response?.data?.message || '비밀번호 재설정 중 오류가 발생했습니다.' 
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
    return user?.status === 'ACTIVE'
  }

  // 이메일 인증 상태 확인
  static isEmailVerified(): boolean {
    const user = this.getCurrentUser()
    return user?.emailVerified === true
  }
}

// 기본 export
export default SpringBootAuthService