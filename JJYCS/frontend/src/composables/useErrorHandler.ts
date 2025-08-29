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