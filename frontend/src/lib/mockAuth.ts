// Mock 인증 시스템 - Supabase 없이 로컬에서 동작하는 인증
export interface MockUser {
  id: string
  email: string
  username: string
  name: string
  user_type: 'general' | 'corporate' | 'partner' | 'warehouse' | 'admin'
  approval_status: 'pending' | 'approved' | 'rejected'
  email_verified: boolean
  created_at: string
}

// 로컬 스토리지 키들
const STORAGE_KEYS = {
  USERS: 'ycs_mock_users',
  CURRENT_USER: 'ycs_current_user',
  SESSION: 'ycs_session'
}

// 기본 테스트 사용자들
const DEFAULT_USERS: MockUser[] = [
  {
    id: '1',
    email: 'admin@ycs.com',
    username: 'admin',
    name: '관리자',
    user_type: 'admin',
    approval_status: 'approved',
    email_verified: true,
    created_at: new Date().toISOString()
  },
  {
    id: '2',
    email: 'user@test.com',
    username: 'testuser',
    name: '테스트 사용자',
    user_type: 'general',
    approval_status: 'approved',
    email_verified: true,
    created_at: new Date().toISOString()
  },
  {
    id: '3',
    email: 'corp@test.com',
    username: 'corpuser',
    name: '기업 사용자',
    user_type: 'corporate',
    approval_status: 'approved',
    email_verified: true,
    created_at: new Date().toISOString()
  }
]

class MockAuthService {
  constructor() {
    this.initializeUsers()
  }

  private initializeUsers() {
    const existingUsers = localStorage.getItem(STORAGE_KEYS.USERS)
    if (!existingUsers) {
      localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(DEFAULT_USERS))
    }
  }

  private getUsers(): MockUser[] {
    const users = localStorage.getItem(STORAGE_KEYS.USERS)
    return users ? JSON.parse(users) : DEFAULT_USERS
  }

  private saveUsers(users: MockUser[]) {
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users))
  }

  private generateId(): string {
    return Date.now().toString() + Math.random().toString(36).substr(2, 9)
  }

  // 로그인
  async signIn(email: string, password: string) {
    await new Promise(resolve => setTimeout(resolve, 500)) // 실제 API 호출 시뮬레이션

    const users = this.getUsers()
    const user = users.find(u => u.email === email)
    
    if (!user) {
      throw new Error('사용자를 찾을 수 없습니다')
    }

    // Mock에서는 모든 비밀번호를 허용 (개발용)
    const session = {
      user: user,
      access_token: 'mock_token_' + this.generateId(),
      expires_at: Date.now() + (24 * 60 * 60 * 1000) // 24시간
    }

    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(user))
    localStorage.setItem(STORAGE_KEYS.SESSION, JSON.stringify(session))

    return { data: { user, session }, error: null }
  }

  // 회원가입
  async signUp(userData: {
    email: string
    password: string
    username: string
    name: string
    user_type: 'general' | 'corporate' | 'partner' | 'warehouse'
    phone?: string
    company_name?: string
    business_number?: string
  }) {
    await new Promise(resolve => setTimeout(resolve, 1000))

    const users = this.getUsers()
    
    // 중복 확인
    if (users.find(u => u.email === userData.email)) {
      throw new Error('이미 등록된 이메일입니다')
    }
    if (users.find(u => u.username === userData.username)) {
      throw new Error('이미 사용 중인 사용자명입니다')
    }

    // 새 사용자 생성
    const newUser: MockUser = {
      id: this.generateId(),
      email: userData.email,
      username: userData.username,
      name: userData.name,
      user_type: userData.user_type,
      approval_status: userData.user_type === 'general' ? 'approved' : 'pending',
      email_verified: false,
      created_at: new Date().toISOString()
    }

    users.push(newUser)
    this.saveUsers(users)

    return { 
      data: { 
        user: newUser,
        session: null // 회원가입 후 바로 로그인하지 않음
      }, 
      error: null 
    }
  }

  // 로그아웃
  async signOut() {
    localStorage.removeItem(STORAGE_KEYS.CURRENT_USER)
    localStorage.removeItem(STORAGE_KEYS.SESSION)
    return { error: null }
  }

  // 현재 사용자 가져오기
  getCurrentUser(): MockUser | null {
    const userData = localStorage.getItem(STORAGE_KEYS.CURRENT_USER)
    return userData ? JSON.parse(userData) : null
  }

  // 세션 확인
  getSession() {
    const sessionData = localStorage.getItem(STORAGE_KEYS.SESSION)
    if (!sessionData) return { data: { session: null }, error: null }

    const session = JSON.parse(sessionData)
    
    // 만료 확인
    if (Date.now() > session.expires_at) {
      this.signOut()
      return { data: { session: null }, error: null }
    }

    return { data: { session }, error: null }
  }

  // 사용자 프로필 업데이트
  async updateProfile(userId: string, updates: Partial<MockUser>) {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const users = this.getUsers()
    const userIndex = users.findIndex(u => u.id === userId)
    
    if (userIndex === -1) {
      throw new Error('사용자를 찾을 수 없습니다')
    }

    users[userIndex] = { ...users[userIndex], ...updates }
    this.saveUsers(users)

    // 현재 사용자 업데이트
    const currentUser = this.getCurrentUser()
    if (currentUser && currentUser.id === userId) {
      localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(users[userIndex]))
    }

    return { data: users[userIndex], error: null }
  }

  // 모든 사용자 가져오기 (관리자용)
  getAllUsers(): MockUser[] {
    return this.getUsers()
  }

  // 사용자 승인 (관리자용)
  async approveUser(userId: string, approved: boolean) {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const users = this.getUsers()
    const userIndex = users.findIndex(u => u.id === userId)
    
    if (userIndex === -1) {
      throw new Error('사용자를 찾을 수 없습니다')
    }

    users[userIndex].approval_status = approved ? 'approved' : 'rejected'
    this.saveUsers(users)

    return { data: users[userIndex], error: null }
  }
}

export const mockAuth = new MockAuthService()

// Supabase 호환 API
export const mockSupabase = {
  auth: {
    signInWithPassword: async ({ email, password }: { email: string, password: string }) => {
      return await mockAuth.signIn(email, password)
    },
    signUp: async ({ email, password, options }: { 
      email: string, 
      password: string, 
      options?: { data?: any } 
    }) => {
      const userData = {
        email,
        password,
        username: options?.data?.username || email.split('@')[0],
        name: options?.data?.name || '사용자',
        user_type: options?.data?.user_type || 'general',
        phone: options?.data?.phone,
        company_name: options?.data?.company_name,
        business_number: options?.data?.business_number
      }
      return await mockAuth.signUp(userData)
    },
    signOut: async () => {
      return await mockAuth.signOut()
    },
    getSession: () => {
      return mockAuth.getSession()
    },
    getUser: () => {
      const user = mockAuth.getCurrentUser()
      return { data: { user }, error: null }
    },
    onAuthStateChange: (callback: (event: string, session: any) => void) => {
      // Mock implementation - 실제로는 이벤트 리스너를 등록해야 함
      const session = mockAuth.getSession().data.session
      callback(session ? 'SIGNED_IN' : 'SIGNED_OUT', session)
      
      // 구독 해제 함수 반환
      return { data: { subscription: { unsubscribe: () => {} } } }
    }
  },
  from: (table: string) => ({
    select: (columns = '*') => ({
      eq: (column: string, value: any) => ({
        single: async () => {
          if (table === 'profiles') {
            const users = mockAuth.getAllUsers()
            const user = users.find(u => u[column as keyof MockUser] === value)
            return user ? { data: user, error: null } : { data: null, error: new Error('Not found') }
          }
          return { data: null, error: null }
        }
      })
    }),
    insert: async (data: any) => {
      console.log(`Mock insert to ${table}:`, data)
      return { data, error: null }
    },
    update: async (data: any) => {
      console.log(`Mock update to ${table}:`, data)
      return { data, error: null }
    }
  })
}