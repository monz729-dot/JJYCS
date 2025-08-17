// types.ts

export type UserRole = 'INDIVIDUAL' | 'ENTERPRISE' | 'PARTNER' | 'WAREHOUSE' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'PENDING_APPROVAL' | 'SUSPENDED' | 'INACTIVE'

export interface RegistrationData {
    username: string
    name: string
    email: string
    phone: string
    password: string
    address?: string
    role: UserRole

    // 기업/파트너 전용 필드
    company_name?: string
    business_number?: string
    manager_name?: string
    manager_contact?: string
    business_license_url?: string

    // 약관 동의
    terms_agreed: boolean
    privacy_agreed: boolean
    referralCode?: string
}



export interface ApiResult<T = unknown> {
    success: boolean
    data?: T
    error?: string
}
