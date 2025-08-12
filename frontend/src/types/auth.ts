// Authentication related types

export interface User {
  id: string
  email: string
  name: string
  role: 'individual' | 'enterprise' | 'partner' | 'warehouse' | 'admin'
  status: 'active' | 'pending_approval' | 'suspended'
  emailVerified: boolean
  twoFactorEnabled: boolean
  memberCode?: string
  phone?: string
  createdAt: string
  updatedAt: string
  
  // Role-specific fields
  companyName?: string // for enterprise/partner
  businessNumber?: string // for enterprise/partner
  businessAddress?: string // for enterprise/partner
  partnerType?: 'affiliate' | 'referral' | 'corporate' // for partner
  referralCode?: string // for partner
  commissionRate?: number // for partner
}

export interface LoginRequest {
  email: string
  password: string
  rememberMe?: boolean
  twoFactorCode?: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
  phone: string
  role: 'individual' | 'enterprise' | 'partner'
  
  // Enterprise fields
  companyName?: string
  businessNumber?: string
  businessAddress?: string
  
  // Partner fields
  referralCode?: string
  partnerType?: 'affiliate' | 'referral' | 'corporate'
  
  // Agreements
  agreeTerms: boolean
  agreeMarketing?: boolean
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

export interface PasswordChangeRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface PasswordResetRequest {
  email: string
}

export interface PasswordResetConfirm {
  token: string
  newPassword: string
  confirmPassword: string
}

export interface EmailVerification {
  token: string
}

export interface TwoFactorSetup {
  qrCode: string
  secret: string
  backupCodes: string[]
}

export interface TwoFactorVerification {
  code: string
}

// Permission and role related types
export type Permission = 
  | 'orders:create'
  | 'orders:read'
  | 'orders:update'
  | 'orders:delete'
  | 'warehouse:scan'
  | 'warehouse:process'
  | 'admin:users'
  | 'admin:approvals'
  | 'admin:settings'
  | 'partner:dashboard'
  | 'partner:referrals'

export interface RolePermissions {
  [key: string]: Permission[]
}

export const ROLE_PERMISSIONS: RolePermissions = {
  individual: ['orders:create', 'orders:read', 'orders:update'],
  enterprise: ['orders:create', 'orders:read', 'orders:update'],
  partner: ['orders:read', 'partner:dashboard', 'partner:referrals'],
  warehouse: ['orders:read', 'warehouse:scan', 'warehouse:process'],
  admin: [
    'orders:create', 'orders:read', 'orders:update', 'orders:delete',
    'warehouse:scan', 'warehouse:process',
    'admin:users', 'admin:approvals', 'admin:settings',
    'partner:dashboard', 'partner:referrals'
  ]
}

// Session and token types
export interface AuthSession {
  token: string
  refreshToken?: string
  expiresAt: Date
  user: User
}

export interface TokenPayload {
  sub: string // user id
  email: string
  role: string
  iat: number // issued at
  exp: number // expires at
  jti?: string // JWT ID
}

// API Response types
export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  errors?: { [key: string]: string[] }
  meta?: {
    total?: number
    page?: number
    limit?: number
    hasNext?: boolean
    hasPrev?: boolean
  }
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  limit: number
  hasNext: boolean
  hasPrev: boolean
}

// Form validation types
export interface ValidationRule {
  required?: boolean
  minLength?: number
  maxLength?: number
  pattern?: RegExp
  custom?: (value: any) => boolean | string
}

export interface ValidationErrors {
  [field: string]: string[]
}

export interface FormState<T> {
  data: T
  errors: ValidationErrors
  loading: boolean
  touched: { [K in keyof T]?: boolean }
}

// Auth store state interface
export interface AuthState {
  user: User | null
  token: string | null
  isInitialized: boolean
  isLoading: boolean
  error: string | null
  loginAttempts: number
  lastLoginAttempt: Date | null
}

// Route meta interface extension
declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresGuest?: boolean
    requiresRole?: string | string[]
    requiresPermission?: Permission | Permission[]
    title?: string
    description?: string
    icon?: string
    hideInNav?: boolean
    mobile?: boolean
    breadcrumb?: string[]
  }
}