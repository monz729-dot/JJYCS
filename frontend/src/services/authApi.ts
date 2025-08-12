// Mock API service for authentication
// This will be replaced with actual API calls when backend is ready

export interface User {
  id: string
  email: string
  name: string
  phone?: string
  role: 'individual' | 'enterprise' | 'partner' | 'warehouse' | 'admin'
  status: 'active' | 'pending_approval' | 'suspended'
  emailVerified: boolean
  twoFactorEnabled: boolean
  memberCode?: string
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  email: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
  phone: string
  role: 'individual' | 'enterprise' | 'partner'
  companyName?: string
  businessNumber?: string
  businessAddress?: string
  referralCode?: string
  partnerType?: 'affiliate' | 'referral' | 'corporate'
  termsAccepted: boolean
  marketingConsent?: boolean
}

export interface AuthResponse {
  success: boolean
  message?: string
  data?: {
    token: string
    user: User
    expiresIn?: number
  }
  errors?: { [key: string]: string[] }
}

export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  errors?: { [key: string]: string[] }
}

// Mock users data for development
const mockUsers: User[] = [
  {
    id: '1',
    email: 'admin@ycs.com',
    name: '관리자',
    phone: '010-1234-5678',
    role: 'admin',
    status: 'active',
    emailVerified: true,
    twoFactorEnabled: false,
    memberCode: 'ADM001',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '2',
    email: 'user@example.com',
    name: '개인사용자',
    phone: '010-2345-6789',
    role: 'individual',
    status: 'active',
    emailVerified: true,
    twoFactorEnabled: false,
    memberCode: 'IND001',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '3',
    email: 'enterprise@company.com',
    name: '기업사용자',
    phone: '010-3456-7890',
    role: 'enterprise',
    status: 'pending_approval',
    emailVerified: true,
    twoFactorEnabled: false,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '4',
    email: 'warehouse@ycs.com',
    name: '창고관리자',
    phone: '010-4567-8901',
    role: 'warehouse',
    status: 'active',
    emailVerified: true,
    twoFactorEnabled: false,
    memberCode: 'WH001',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }
]

// Helper function to simulate API delay
const delay = (ms: number = 500) => new Promise(resolve => setTimeout(resolve, ms))

// Helper function to generate JWT-like token (mock)
const generateMockToken = (userId: string) => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = btoa(JSON.stringify({ sub: userId, iat: Date.now(), exp: Date.now() + 24 * 60 * 60 * 1000 }))
  const signature = 'mock-signature'
  return `${header}.${payload}.${signature}`
}

export const authApi = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    await delay()
    
    const { email, password } = credentials
    
    // Mock validation
    if (!email || !password) {
      return {
        success: false,
        message: 'Email and password are required',
        errors: {
          email: !email ? ['Email is required'] : [],
          password: !password ? ['Password is required'] : []
        }
      }
    }

    // Find user by email
    const user = mockUsers.find(u => u.email === email)
    
    if (!user) {
      return {
        success: false,
        message: 'Invalid email or password',
        errors: {
          general: ['Invalid email or password']
        }
      }
    }

    // Mock password validation (in real implementation, this would be done server-side)
    if (password !== 'password123') {
      return {
        success: false,
        message: 'Invalid email or password',
        errors: {
          general: ['Invalid email or password']
        }
      }
    }

    const token = generateMockToken(user.id)

    return {
      success: true,
      message: 'Login successful',
      data: {
        token,
        user,
        expiresIn: 24 * 60 * 60 // 24 hours in seconds
      }
    }
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    await delay()
    
    // Mock validation
    if (!userData.email || !userData.password || !userData.name || !userData.termsAccepted) {
      return {
        success: false,
        message: 'Required fields are missing',
        errors: {
          email: !userData.email ? ['Email is required'] : [],
          password: !userData.password ? ['Password is required'] : [],
          name: !userData.name ? ['Name is required'] : [],
          termsAccepted: !userData.termsAccepted ? ['You must accept the terms and conditions'] : []
        }
      }
    }

    // Check if user already exists
    if (mockUsers.some(u => u.email === userData.email)) {
      return {
        success: false,
        message: 'User already exists',
        errors: {
          email: ['Email is already registered']
        }
      }
    }

    // Create new user
    const newUser: User = {
      id: String(mockUsers.length + 1),
      email: userData.email,
      name: userData.name,
      phone: userData.phone,
      role: userData.role,
      status: ['enterprise', 'partner'].includes(userData.role) ? 'pending_approval' : 'active',
      emailVerified: false,
      twoFactorEnabled: false,
      memberCode: userData.role === 'individual' ? `IND${String(mockUsers.length + 1).padStart(3, '0')}` : undefined,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }

    mockUsers.push(newUser)
    const token = generateMockToken(newUser.id)

    return {
      success: true,
      message: 'Registration successful',
      data: {
        token,
        user: newUser,
        expiresIn: 24 * 60 * 60
      }
    }
  },

  async logout(): Promise<ApiResponse> {
    await delay(200)
    
    return {
      success: true,
      message: 'Logout successful'
    }
  },

  async getCurrentUser(): Promise<ApiResponse<User>> {
    await delay()
    
    // In real implementation, this would validate the token and return user data
    // For mock, we'll use localStorage to get the current user
    const token = localStorage.getItem('auth_token')
    const currentUserId = localStorage.getItem('current_user_id')
    
    if (!token) {
      return {
        success: false,
        message: 'No token provided'
      }
    }

    // Mock user lookup - in real app this would validate token server-side
    try {
      let user: User | undefined
      
      if (currentUserId) {
        user = mockUsers.find(u => u.id === currentUserId)
      }
      
      // Fallback to admin user if no specific user is stored
      if (!user) {
        user = mockUsers.find(u => u.role === 'admin')
      }
      
      if (!user) {
        return {
          success: false,
          message: 'User not found'
        }
      }

      return {
        success: true,
        data: user
      }
    } catch (error) {
      return {
        success: false,
        message: 'Failed to get current user'
      }
    }
  },

  async updateProfile(profileData: Partial<User>): Promise<ApiResponse<User>> {
    await delay()
    
    const token = localStorage.getItem('auth_token')
    if (!token) {
      return {
        success: false,
        message: 'Not authenticated'
      }
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const userId = payload.sub
      const userIndex = mockUsers.findIndex(u => u.id === userId)
      
      if (userIndex === -1) {
        return {
          success: false,
          message: 'User not found'
        }
      }

      // Update user data
      mockUsers[userIndex] = {
        ...mockUsers[userIndex],
        ...profileData,
        updatedAt: new Date().toISOString()
      }

      return {
        success: true,
        message: 'Profile updated successfully',
        data: mockUsers[userIndex]
      }
    } catch (error) {
      return {
        success: false,
        message: 'Failed to update profile'
      }
    }
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<ApiResponse> {
    await delay()
    
    // Mock password change - in real implementation this would be properly validated
    if (currentPassword !== 'password123') {
      return {
        success: false,
        message: 'Current password is incorrect',
        errors: {
          currentPassword: ['Current password is incorrect']
        }
      }
    }

    if (newPassword.length < 8) {
      return {
        success: false,
        message: 'New password must be at least 8 characters',
        errors: {
          newPassword: ['Password must be at least 8 characters']
        }
      }
    }

    return {
      success: true,
      message: 'Password changed successfully'
    }
  },

  async requestPasswordReset(email: string): Promise<ApiResponse> {
    await delay()
    
    const user = mockUsers.find(u => u.email === email)
    
    if (!user) {
      // For security, we don't reveal if email exists or not
      return {
        success: true,
        message: 'If the email exists, a password reset link has been sent'
      }
    }

    return {
      success: true,
      message: 'Password reset email sent successfully'
    }
  },

  async resetPassword(token: string, newPassword: string): Promise<ApiResponse> {
    await delay()
    
    if (!token || !newPassword) {
      return {
        success: false,
        message: 'Token and new password are required'
      }
    }

    if (newPassword.length < 8) {
      return {
        success: false,
        message: 'Password must be at least 8 characters',
        errors: {
          password: ['Password must be at least 8 characters']
        }
      }
    }

    return {
      success: true,
      message: 'Password reset successfully'
    }
  },

  async verifyEmail(token: string): Promise<ApiResponse> {
    await delay()
    
    if (!token) {
      return {
        success: false,
        message: 'Verification token is required'
      }
    }

    return {
      success: true,
      message: 'Email verified successfully'
    }
  },

  async enable2FA(): Promise<ApiResponse<{ qrCode: string; secret: string }>> {
    await delay()
    
    return {
      success: true,
      message: '2FA setup initiated',
      data: {
        qrCode: 'data:image/png;base64,mock-qr-code',
        secret: 'MOCK2FASECRET123'
      }
    }
  },

  async disable2FA(code: string): Promise<ApiResponse> {
    await delay()
    
    if (!code || code.length !== 6) {
      return {
        success: false,
        message: 'Invalid verification code',
        errors: {
          code: ['Verification code must be 6 digits']
        }
      }
    }

    return {
      success: true,
      message: '2FA disabled successfully'
    }
  },

  async verify2FA(code: string): Promise<AuthResponse> {
    await delay()
    
    if (!code || code.length !== 6) {
      return {
        success: false,
        message: 'Invalid verification code',
        errors: {
          code: ['Verification code must be 6 digits']
        }
      }
    }

    // Mock 2FA verification - accept any 6-digit code starting with "1"
    if (!code.startsWith('1')) {
      return {
        success: false,
        message: 'Invalid verification code',
        errors: {
          code: ['Invalid verification code']
        }
      }
    }

    const user = mockUsers[0] // Return admin user for demo
    const token = generateMockToken(user.id)

    return {
      success: true,
      message: '2FA verification successful',
      data: {
        token,
        user,
        expiresIn: 24 * 60 * 60
      }
    }
  },

  async getAllUsers(): Promise<ApiResponse<User[]>> {
    await delay(300)
    
    return {
      success: true,
      data: [...mockUsers] // Return copy of all users
    }
  },

  async approveUser(userId: string): Promise<ApiResponse<User>> {
    await delay(500)
    
    const userIndex = mockUsers.findIndex(u => u.id === userId)
    if (userIndex === -1) {
      return {
        success: false,
        message: 'User not found'
      }
    }

    mockUsers[userIndex] = {
      ...mockUsers[userIndex],
      status: 'active',
      updatedAt: new Date().toISOString()
    }

    return {
      success: true,
      data: mockUsers[userIndex],
      message: 'User approved successfully'
    }
  },

  async rejectUser(userId: string): Promise<ApiResponse<User>> {
    await delay(500)
    
    const userIndex = mockUsers.findIndex(u => u.id === userId)
    if (userIndex === -1) {
      return {
        success: false,
        message: 'User not found'
      }
    }

    mockUsers[userIndex] = {
      ...mockUsers[userIndex],
      status: 'suspended',
      updatedAt: new Date().toISOString()
    }

    return {
      success: true,
      data: mockUsers[userIndex],
      message: 'User rejected successfully'
    }
  }
}