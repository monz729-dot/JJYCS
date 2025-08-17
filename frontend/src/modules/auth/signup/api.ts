import type { ApiResult, RegistrationData } from './types'

const BASE = (import.meta as any).env?.VITE_API_BASE_URL || '/api'

function headers() {
    return { 'Content-Type': 'application/json' }
}

async function request(path: string, init: RequestInit): Promise<ApiResult<any>> {
    try {
        const res = await fetch(BASE + path, init)
        const isJson = (res.headers.get('content-type') || '').includes('application/json')
        const body = isJson ? await res.json() : null
        if (!res.ok) {
            return { success: false, error: (body && (body.message || body.error)) || res.statusText }
        }
        return { success: true, data: body }
    } catch (e: any) {
        return { success: false, error: e?.message || '네트워크 오류' }
    }
}

export async function checkUsername(username: string): Promise<ApiResult<{ available: boolean }>> {
    return request('/auth/check-username?username=' + encodeURIComponent(username), { method: 'GET', credentials: 'include' })
}

export async function signUp(payload: RegistrationData): Promise<ApiResult<{ requiresEmailVerification?: boolean }>> {
    return request('/auth/signup', {
        method: 'POST',
        credentials: 'include',
        headers: headers(),
        body: JSON.stringify(payload)
    })
}

export async function verifyEmail(email: string, token: string): Promise<ApiResult<void>> {
    return request('/auth/verify-email', {
        method: 'POST',
        credentials: 'include',
        headers: headers(),
        body: JSON.stringify({ email, token })
    })
}

export async function resendVerificationToken(email: string): Promise<ApiResult<void>> {
    return request('/auth/verify-email/resend', {
        method: 'POST',
        credentials: 'include',
        headers: headers(),
        body: JSON.stringify({ email })
    })
}

export async function completeSignUp(payload: RegistrationData): Promise<ApiResult<void>> {
    // 백엔드가 별도의 완료 API가 없으면 200 처리 가정
    // 필요 시 경로 수정
    return request('/auth/complete', {
        method: 'POST',
        credentials: 'include',
        headers: headers(),
        body: JSON.stringify(payload)
    })
}

export async function signOut(): Promise<ApiResult<void>> {
    return request('/auth/logout', { method: 'POST', credentials: 'include' })
}

// 원본 컴포넌트 호환을 위해 객체 형태도 제공
export const AuthService = {
    checkUsername,
    signUp,
    verifyEmail,
    resendVerificationToken,
    completeSignUp,
    signOut
}
