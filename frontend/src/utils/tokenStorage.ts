/**
 * 토큰 저장 및 관리 유틸리티
 * 보안을 위해 httpOnly 쿠키 사용 권장, localStorage는 개발 환경용
 */

import { ref } from 'vue'

/**
 * 토큰 저장 방식 설정
 */
const USE_SECURE_STORAGE = import.meta.env.PROD // 프로덕션에서는 보안 저장소 사용

/**
 * 토큰 정보 인터페이스
 */
export interface TokenInfo {
  accessToken: string
  refreshToken: string
  expiresAt: number
  tokenType: string
}

/**
 * 토큰 저장소 키
 */
const TOKEN_KEYS = {
  ACCESS_TOKEN: 'ycs_access_token',
  REFRESH_TOKEN: 'ycs_refresh_token',
  TOKEN_INFO: 'ycs_token_info'
} as const

/**
 * 쿠키 옵션
 */
const COOKIE_OPTIONS = {
  httpOnly: false, // 클라이언트 사이드에서는 httpOnly 불가
  secure: import.meta.env.PROD, // HTTPS에서만 전송
  sameSite: 'strict' as const,
  path: '/',
  maxAge: 7 * 24 * 60 * 60 // 7일
}

/**
 * 보안 쿠키 설정 (서버에서 설정되어야 함)
 */
export function setSecureCookie(name: string, value: string, maxAge: number = COOKIE_OPTIONS.maxAge): void {
  if (typeof document === 'undefined') return

  const options = [
    `${name}=${value}`,
    `Max-Age=${maxAge}`,
    `Path=${COOKIE_OPTIONS.path}`,
    `SameSite=${COOKIE_OPTIONS.sameSite}`
  ]

  if (COOKIE_OPTIONS.secure) {
    options.push('Secure')
  }

  document.cookie = options.join('; ')
}

/**
 * 쿠키에서 값 읽기
 */
export function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null

  const cookies = document.cookie.split(';')
  for (const cookie of cookies) {
    const [key, value] = cookie.trim().split('=')
    if (key === name) {
      return decodeURIComponent(value)
    }
  }
  return null
}

/**
 * 쿠키 삭제
 */
export function deleteCookie(name: string): void {
  if (typeof document === 'undefined') return
  
  document.cookie = `${name}=; Max-Age=0; Path=${COOKIE_OPTIONS.path}; SameSite=${COOKIE_OPTIONS.sameSite}`
}

/**
 * 토큰 저장 (환경에 따라 localStorage 또는 쿠키 사용)
 */
export function saveTokens(tokenInfo: TokenInfo): void {
  if (USE_SECURE_STORAGE) {
    // 프로덕션: 쿠키 사용 (서버에서 httpOnly 설정 권장)
    setSecureCookie(TOKEN_KEYS.ACCESS_TOKEN, tokenInfo.accessToken, 60 * 60) // 1시간
    setSecureCookie(TOKEN_KEYS.REFRESH_TOKEN, tokenInfo.refreshToken, 7 * 24 * 60 * 60) // 7일
    setSecureCookie(TOKEN_KEYS.TOKEN_INFO, JSON.stringify({
      expiresAt: tokenInfo.expiresAt,
      tokenType: tokenInfo.tokenType
    }))
  } else {
    // 개발: localStorage 사용 (편의성)
    localStorage.setItem(TOKEN_KEYS.ACCESS_TOKEN, tokenInfo.accessToken)
    localStorage.setItem(TOKEN_KEYS.REFRESH_TOKEN, tokenInfo.refreshToken)
    localStorage.setItem(TOKEN_KEYS.TOKEN_INFO, JSON.stringify(tokenInfo))
  }
}

/**
 * 액세스 토큰 조회
 */
export function getAccessToken(): string | null {
  if (USE_SECURE_STORAGE) {
    return getCookie(TOKEN_KEYS.ACCESS_TOKEN)
  } else {
    return localStorage.getItem(TOKEN_KEYS.ACCESS_TOKEN)
  }
}

/**
 * 리프레시 토큰 조회
 */
export function getRefreshToken(): string | null {
  if (USE_SECURE_STORAGE) {
    return getCookie(TOKEN_KEYS.REFRESH_TOKEN)
  } else {
    return localStorage.getItem(TOKEN_KEYS.REFRESH_TOKEN)
  }
}

/**
 * 토큰 정보 조회
 */
export function getTokenInfo(): TokenInfo | null {
  try {
    let tokenInfoStr: string | null
    
    if (USE_SECURE_STORAGE) {
      tokenInfoStr = getCookie(TOKEN_KEYS.TOKEN_INFO)
    } else {
      tokenInfoStr = localStorage.getItem(TOKEN_KEYS.TOKEN_INFO)
    }
    
    if (!tokenInfoStr) return null
    
    const tokenInfo = JSON.parse(tokenInfoStr)
    
    // localStorage 사용 시 전체 정보 반환
    if (!USE_SECURE_STORAGE) {
      return tokenInfo
    }
    
    // 쿠키 사용 시 별도 저장된 토큰들과 합치기
    return {
      accessToken: getAccessToken() || '',
      refreshToken: getRefreshToken() || '',
      expiresAt: tokenInfo.expiresAt,
      tokenType: tokenInfo.tokenType || 'Bearer'
    }
  } catch {
    return null
  }
}

/**
 * 토큰 만료 여부 확인
 */
export function isTokenExpired(): boolean {
  const tokenInfo = getTokenInfo()
  if (!tokenInfo) return true
  
  return Date.now() >= tokenInfo.expiresAt
}

/**
 * 토큰이 곧 만료되는지 확인 (5분 전)
 */
export function isTokenExpiringSoon(): boolean {
  const tokenInfo = getTokenInfo()
  if (!tokenInfo) return true
  
  const fiveMinutesFromNow = Date.now() + (5 * 60 * 1000)
  return fiveMinutesFromNow >= tokenInfo.expiresAt
}

/**
 * 토큰 삭제 (로그아웃 시)
 */
export function clearTokens(): void {
  if (USE_SECURE_STORAGE) {
    deleteCookie(TOKEN_KEYS.ACCESS_TOKEN)
    deleteCookie(TOKEN_KEYS.REFRESH_TOKEN)
    deleteCookie(TOKEN_KEYS.TOKEN_INFO)
  } else {
    localStorage.removeItem(TOKEN_KEYS.ACCESS_TOKEN)
    localStorage.removeItem(TOKEN_KEYS.REFRESH_TOKEN)
    localStorage.removeItem(TOKEN_KEYS.TOKEN_INFO)
    localStorage.removeItem('current_user_id') // 기존 호환성
    localStorage.removeItem('auth_token') // 기존 호환성
  }
}

/**
 * Authorization 헤더 생성
 */
export function getAuthorizationHeader(): string | null {
  const accessToken = getAccessToken()
  if (!accessToken) return null
  
  const tokenInfo = getTokenInfo()
  const tokenType = tokenInfo?.tokenType || 'Bearer'
  
  return `${tokenType} ${accessToken}`
}

/**
 * 토큰 자동 갱신을 위한 리액티브 상태
 */
export const tokenState = ref({
  isAuthenticated: false,
  isExpiring: false,
  needsRefresh: false
})

/**
 * 토큰 상태 업데이트
 */
export function updateTokenState(): void {
  const hasToken = !!getAccessToken()
  const expired = isTokenExpired()
  const expiring = isTokenExpiringSoon()
  
  tokenState.value = {
    isAuthenticated: hasToken && !expired,
    isExpiring: hasToken && expiring && !expired,
    needsRefresh: hasToken && expired
  }
}

/**
 * 주기적 토큰 상태 확인 (1분마다)
 */
export function startTokenMonitoring(): void {
  updateTokenState()
  
  setInterval(() => {
    updateTokenState()
  }, 60 * 1000) // 1분마다 확인
}

/**
 * 토큰 갱신 요청 시 임시 저장
 */
export function saveTemporaryTokens(accessToken: string, refreshToken: string, expiresIn: number): void {
  const expiresAt = Date.now() + (expiresIn * 1000)
  
  saveTokens({
    accessToken,
    refreshToken,
    expiresAt,
    tokenType: 'Bearer'
  })
  
  updateTokenState()
}

/**
 * 레거시 토큰 마이그레이션 (기존 localStorage 토큰을 새 형식으로)
 */
export function migrateLegacyTokens(): void {
  try {
    const legacyToken = localStorage.getItem('auth_token')
    const legacyUserId = localStorage.getItem('current_user_id')
    
    if (legacyToken && !getAccessToken()) {
      // 레거시 토큰을 새 형식으로 마이그레이션
      const tokenInfo: TokenInfo = {
        accessToken: legacyToken,
        refreshToken: '', // 레거시에는 refresh token이 없음
        expiresAt: Date.now() + (24 * 60 * 60 * 1000), // 24시간 후 만료로 설정
        tokenType: 'Bearer'
      }
      
      saveTokens(tokenInfo)
      
      // 레거시 토큰 정리
      localStorage.removeItem('auth_token')
      if (legacyUserId) {
        localStorage.removeItem('current_user_id')
      }
      
      console.log('토큰이 새로운 형식으로 마이그레이션되었습니다.')
    }
  } catch (error) {
    console.error('토큰 마이그레이션 중 오류:', error)
  }
}