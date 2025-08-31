import { useToast } from './useToast'

interface ApiError {
  response?: {
    data?: {
      message?: string
      error?: string
      details?: string
      code?: string
    }
    status?: number
  }
  message?: string
  code?: string
}

interface BusinessRuleError {
  rule: string
  message: string
  suggestion?: string
}

export function useErrorHandler() {
  const { 
    error: showError, 
    warning: showWarning, 
    businessRule: showBusinessRule,
    networkError,
    validationError,
    businessWarning
  } = useToast()

  const handleApiError = (error: ApiError, context?: string) => {
    console.error('API Error:', error)

    let title = '오류가 발생했습니다'
    let message = '잠시 후 다시 시도해주세요'

    if (error.response) {
      const status = error.response.status
      const errorData = error.response.data

      // Handle specific HTTP status codes
      switch (status) {
        case 400:
          title = '잘못된 요청'
          message = errorData?.message || errorData?.error || '입력 내용을 확인하고 다시 시도해주세요'
          return validationError(message)
        case 401:
          title = '인증 필요'
          message = '로그인이 만료되었습니다. 다시 로그인해주세요'
          localStorage.removeItem('auth-token')
          window.location.href = '/login'
          break
        case 403:
          title = '접근 권한 없음'
          message = '해당 기능에 접근할 권한이 없습니다'
          break
        case 404:
          title = '리소스 없음'
          message = errorData?.message || '요청하신 리소스를 찾을 수 없습니다'
          break
        case 409:
          title = '데이터 충돌'
          message = errorData?.message || '이미 존재하는 데이터입니다'
          break
        case 422:
          title = '입력 오류'
          message = errorData?.message || '입력 내용을 확인해주세요'
          return validationError(message)
        case 429:
          title = '요청 제한'
          message = '잠시 후 다시 시도해주세요'
          break
        case 500:
          title = '서버 오류'
          message = '서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요'
          break
        default:
          title = `오류 ${status}`
          message = errorData?.message || errorData?.error || '알 수 없는 오류가 발생했습니다'
      }

      // Add context if provided
      if (context) {
        title = `${context}: ${title}`
      }
    } else if (error.code === 'ECONNABORTED') {
      return networkError('요청 시간이 초과되었습니다. 다시 시도해주세요.')
    } else if (error.code === 'NETWORK_ERROR' || !navigator.onLine) {
      return networkError()
    } else if (error.message) {
      message = error.message
    }

    return showError(title, message)
  }

  const handleBusinessRuleError = (rule: BusinessRuleError) => {
    showBusinessRule(rule.rule, `${rule.message}${rule.suggestion ? ` ${rule.suggestion}` : ''}`)
  }

  const handleValidationError = (field: string, message: string) => {
    showWarning(`Validation Error: ${field}`, message)
  }

  const handleFormError = (errors: Record<string, string>) => {
    const errorMessages = Object.entries(errors)
      .map(([field, message]) => `${field}: ${message}`)
      .join('\n')
    
    showError('Form Validation Failed', errorMessages)
  }

  // Specific error handlers for common scenarios
  const handleOrderError = (error: ApiError, orderNumber?: string) => {
    const context = orderNumber ? `Order ${orderNumber}` : 'Order Processing'
    handleApiError(error, context)
  }

  const handleWarehouseError = (error: ApiError, action?: string) => {
    const context = action ? `Warehouse ${action}` : 'Warehouse Operation'
    handleApiError(error, context)
  }

  const handleAuthError = (error: ApiError) => {
    handleApiError(error, 'Authentication')
  }

  const handleUploadError = (error: ApiError, fileName?: string) => {
    const context = fileName ? `Upload ${fileName}` : 'File Upload'
    handleApiError(error, context)
  }

  // Business rule specific handlers with Korean messages
  const handleCBMExceeded = (actualCBM: number, threshold: number = 29) => {
    return businessWarning('cbm', `실제 CBM: ${actualCBM}m³, 임계값: ${threshold}m³`)
  }

  const handleTHBThresholdExceeded = (amount: number, threshold: number = 1500) => {
    return businessWarning('thb', `품목 가액: ${amount} THB, 임계값: ${threshold} THB`)
  }

  const handleMemberCodeMissing = () => {
    return businessWarning('memberCode', '회원코드를 확인하고 업데이트해주세요')
  }

  const handleEMSHSValidationFailed = (code: string) => {
    return showError('HS 코드 검증 실패', `코드 "${code}"를 확인할 수 없습니다. 정확한 코드를 입력해주세요.`)
  }

  // Enhanced form validation with Korean messages
  const validateForm = (formData: Record<string, any>, rules: Record<string, ValidationRule[]>): { isValid: boolean; errors: Record<string, string> } => {
    const errors: Record<string, string> = {}
    
    for (const [field, fieldRules] of Object.entries(rules)) {
      const value = formData[field]
      for (const rule of fieldRules) {
        const result = rule.validator(value)
        if (result !== true) {
          errors[field] = typeof result === 'string' ? result : rule.message
          break // Stop at first error for this field
        }
      }
    }
    
    return {
      isValid: Object.keys(errors).length === 0,
      errors
    }
  }

  // Common validation rules with Korean messages
  const validationRules = {
    required: (message = '필수 입력 항목입니다.'): ValidationRule => ({
      validator: (value) => {
        if (value === null || value === undefined || value === '') return message
        if (Array.isArray(value) && value.length === 0) return message
        return true
      },
      message
    }),

    email: (message = '올바른 이메일 주소를 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        return emailRegex.test(value) ? true : message
      },
      message
    }),

    phone: (message = '올바른 전화번호를 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const phoneRegex = /^\d{3}-\d{4}-\d{4}$/
        return phoneRegex.test(value) ? true : message
      },
      message
    }),

    minLength: (min: number, message?: string): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const actualMessage = message || `최소 ${min}자 이상 입력해주세요.`
        return value.length >= min ? true : actualMessage
      },
      message: message || `최소 ${min}자 이상 입력해주세요.`
    }),

    numeric: (message = '숫자만 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        return !isNaN(Number(value)) && !isNaN(parseFloat(value)) ? true : message
      },
      message
    }),

    positive: (message = '0보다 큰 값을 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        return Number(value) > 0 ? true : message
      },
      message
    }),

    pattern: (regex: RegExp | string, message = '올바른 형식이 아닙니다.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        
        // RegExp 객체이거나 문자열 패턴을 RegExp로 변환
        let regexPattern: RegExp
        if (typeof regex === 'string') {
          regexPattern = new RegExp(regex)
        } else if (regex instanceof RegExp) {
          regexPattern = regex
        } else {
          // 패턴이 함수나 기타 형태일 경우 안전 가드
          console.warn('Invalid pattern type:', typeof regex, regex)
          return true
        }
        
        return regexPattern.test(value) ? true : message
      },
      message
    }),

    // 태국 전용 검증 룰들
    thailandPostalCode: (message = '태국 우편번호는 5자리 숫자입니다.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const pattern = /^[1-9][0-9]{4}$/  // 태국 우편번호는 0으로 시작하지 않음
        if (!pattern.test(value)) return message
        
        // 태국 우편번호 지역별 검증
        const firstTwo = parseInt(value.substring(0, 2))
        if (firstTwo < 10 || firstTwo > 96) {
          return '유효한 태국 우편번호 범위가 아닙니다. (10xxx ~ 96xxx)'
        }
        
        return true
      },
      message
    }),

    thailandPhone: (message = '올바른 태국 전화번호를 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        
        // 공백, 하이픈, 괄호 제거
        const cleanedNumber = value.replace(/[\s\-\(\)]/g, '')
        
        // 태국 전화번호 형식:
        // 모바일: 06/08/09 + 7-8자리 (총 9-10자리)
        // 방콕 지역번호: 02 + 7자리 (총 9자리)
        // 기타 지역번호: 03/04/05/07 + 7자리 (총 9자리)
        // 국제번호: +66 뒤에 0을 제외한 번호
        
        const thaiMobilePattern = /^(0[689]\d{7,8}|\+66[689]\d{7,8})$/
        const thaiBangkokPattern = /^(02\d{7}|\+662\d{7})$/
        const thaiLandlinePattern = /^(0[3457]\d{7}|\+66[3457]\d{7})$/
        
        const isValid = thaiMobilePattern.test(cleanedNumber) || 
                       thaiBangkokPattern.test(cleanedNumber) || 
                       thaiLandlinePattern.test(cleanedNumber)
        
        if (!isValid) {
          return '태국 전화번호 형식: 08-xxxx-xxxx 또는 +66-8-xxxx-xxxx'
        }
        
        return true
      },
      message
    }),

    emsTrackingNumber: (message = 'EMS 송장번호는 13자리 형식입니다. (예: EE123456789KR)'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const pattern = /^[A-Z]{2}[0-9]{9}[A-Z]{2}$/
        return pattern.test(value) ? true : message
      },
      message
    }),

    hsCode: (message = '올바른 HS Code 형식이 아닙니다.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        // HS Code: 4-10자리 숫자, 점 포함 가능
        const pattern = /^\d{4}(\.\d{1,6})?$/
        return pattern.test(value) ? true : message
      },
      message
    }),

    thbAmount: (message = 'THB 금액을 올바르게 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        const num = parseFloat(value)
        return !isNaN(num) && num > 0 ? true : message
      },
      message
    }),

    courierTrackingNumber: (message = '송장번호를 입력해주세요.'): ValidationRule => ({
      validator: (value) => {
        if (!value) return true
        // 택배 송장번호: 숫자와 하이픈 허용, 최소 8자리
        const pattern = /^[0-9\-]{8,}$/
        return pattern.test(value) ? true : message
      },
      message
    })
  }

  interface ValidationRule {
    validator: (value: any) => true | string
    message: string
  }

  return {
    handleApiError,
    handleBusinessRuleError,
    handleValidationError,
    handleFormError,
    handleOrderError,
    handleWarehouseError,
    handleAuthError,
    handleUploadError,
    handleCBMExceeded,
    handleTHBThresholdExceeded,
    handleMemberCodeMissing,
    handleEMSHSValidationFailed,
    validateForm,
    validationRules
  }
}