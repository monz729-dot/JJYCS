export type UserType = 'general' | 'corporate' | 'partner'

export interface RegistrationData {
    username: string
    name: string
    email: string
    phone: string
    password: string
    address?: string
    user_type: UserType
    company_name?: string
    business_number?: string
    manager_name?: string
    manager_contact?: string
    business_license_url?: string
    terms_agreed: boolean
    privacy_agreed: boolean
    referralCode?: string
}

export interface ApiResult<T = unknown> {
    success: boolean
    data?: T
    error?: string
}
