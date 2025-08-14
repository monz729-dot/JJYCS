// 유틸리티 헬퍼 함수들

/**
 * 통화 포맷팅
 */
export const formatCurrency = (
  amount: number,
  currency: string = 'KRW',
  locale: string = 'ko-KR'
): string => {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

/**
 * 날짜 포맷팅
 */
export const formatDate = (
  date: string | Date,
  options: Intl.DateTimeFormatOptions = {},
  locale: string = 'ko-KR'
): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    ...options
  }
  
  return new Intl.DateTimeFormat(locale, defaultOptions).format(dateObj)
}

/**
 * 상대적 시간 포맷팅 (몇 분 전, 몇 시간 전 등)
 */
export const formatRelativeTime = (
  date: string | Date,
  locale: string = 'ko-KR'
): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diff = now.getTime() - dateObj.getTime()
  
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' })
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const weeks = Math.floor(days / 7)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)
  
  if (years > 0) return rtf.format(-years, 'year')
  if (months > 0) return rtf.format(-months, 'month')
  if (weeks > 0) return rtf.format(-weeks, 'week')
  if (days > 0) return rtf.format(-days, 'day')
  if (hours > 0) return rtf.format(-hours, 'hour')
  if (minutes > 0) return rtf.format(-minutes, 'minute')
  return rtf.format(-seconds, 'second')
}

/**
 * 숫자 포맷팅 (천단위 콤마)
 */
export const formatNumber = (
  num: number,
  locale: string = 'ko-KR'
): string => {
  return new Intl.NumberFormat(locale).format(num)
}

/**
 * 파일 크기 포맷팅
 */
export const formatFileSize = (bytes: number): string => {
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  if (bytes === 0) return '0 Byte'
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 문자열 자르기
 */
export const truncate = (
  str: string,
  length: number,
  suffix: string = '...'
): string => {
  if (str.length <= length) return str
  return str.slice(0, length - suffix.length) + suffix
}

/**
 * 전화번호 포맷팅
 */
export const formatPhoneNumber = (phone: string): string => {
  const cleaned = phone.replace(/\D/g, '')
  const match = cleaned.match(/^(\d{3})(\d{4})(\d{4})$/)
  if (match) {
    return `${match[1]}-${match[2]}-${match[3]}`
  }
  return phone
}

/**
 * 이메일 마스킹
 */
export const maskEmail = (email: string): string => {
  const [username, domain] = email.split('@')
  if (!username || !domain) return email
  
  const maskedUsername = username.length > 2 
    ? username.slice(0, 2) + '*'.repeat(username.length - 2)
    : username
  
  return `${maskedUsername}@${domain}`
}

/**
 * CBM 계산
 */
export const calculateCBM = (
  width: number,
  height: number,
  depth: number
): number => {
  return (width * height * depth) / 1000000 // cm³ to m³
}

/**
 * 무게를 kg으로 변환
 */
export const convertToKg = (
  weight: number,
  unit: 'g' | 'kg' = 'kg'
): number => {
  return unit === 'g' ? weight / 1000 : weight
}

/**
 * 배송비 계산 (간단한 예시)
 */
export const calculateShippingFee = (
  cbm: number,
  weight: number,
  type: 'air' | 'sea'
): number => {
  const baseRate = type === 'air' ? 50000 : 30000
  const cbmRate = type === 'air' ? 100000 : 60000
  const weightRate = type === 'air' ? 5000 : 3000
  
  return baseRate + (cbm * cbmRate) + (weight * weightRate)
}

/**
 * 색상 클래스 생성
 */
export const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    pending: 'bg-yellow-100 text-yellow-800',
    confirmed: 'bg-blue-100 text-blue-800',
    processing: 'bg-purple-100 text-purple-800',
    shipped: 'bg-green-100 text-green-800',
    delivered: 'bg-green-100 text-green-800',
    cancelled: 'bg-red-100 text-red-800',
    delayed: 'bg-orange-100 text-orange-800'
  }
  return colors[status] || 'bg-gray-100 text-gray-800'
}

/**
 * URL 쿼리 파라미터를 객체로 변환
 */
export const parseQueryParams = (url: string): Record<string, string> => {
  const params: Record<string, string> = {}
  const urlObj = new URL(url)
  urlObj.searchParams.forEach((value, key) => {
    params[key] = value
  })
  return params
}

/**
 * 객체를 URL 쿼리 파라미터로 변환
 */
export const buildQueryParams = (params: Record<string, any>): string => {
  const searchParams = new URLSearchParams()
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      searchParams.set(key, String(value))
    }
  })
  
  return searchParams.toString()
}