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
  const { error: showError, warning: showWarning, businessRule: showBusinessRule } = useToast()

  const handleApiError = (error: ApiError, context?: string) => {
    console.error('API Error:', error)

    let title = 'An error occurred'
    let message = 'Please try again later'

    if (error.response) {
      const status = error.response.status
      const errorData = error.response.data

      // Handle specific HTTP status codes
      switch (status) {
        case 400:
          title = 'Invalid Request'
          message = errorData?.message || errorData?.error || 'Please check your input and try again'
          break
        case 401:
          title = 'Authentication Required'
          message = 'Please log in again to continue'
          // Redirect to login could be handled here
          break
        case 403:
          title = 'Access Denied'
          message = 'You do not have permission to perform this action'
          break
        case 404:
          title = 'Not Found'
          message = errorData?.message || 'The requested resource was not found'
          break
        case 409:
          title = 'Conflict'
          message = errorData?.message || 'This action conflicts with existing data'
          break
        case 422:
          title = 'Validation Error'
          message = errorData?.message || 'Please correct the highlighted fields'
          break
        case 429:
          title = 'Too Many Requests'
          message = 'Please wait a moment before trying again'
          break
        case 500:
          title = 'Server Error'
          message = 'A server error occurred. Our team has been notified.'
          break
        default:
          title = `Error ${status}`
          message = errorData?.message || errorData?.error || 'An unexpected error occurred'
      }

      // Add context if provided
      if (context) {
        title = `${context}: ${title}`
      }
    } else if (error.code === 'ECONNABORTED') {
      title = 'Request Timeout'
      message = 'The request took too long to complete. Please try again.'
    } else if (error.code === 'NETWORK_ERROR' || !navigator.onLine) {
      title = 'Network Error'
      message = 'Please check your internet connection and try again.'
    } else if (error.message) {
      message = error.message
    }

    showError(title, message)
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

  // Business rule specific handlers
  const handleCBMExceeded = (actualCBM: number, threshold: number = 29) => {
    handleBusinessRuleError({
      rule: 'CBM Limit Exceeded',
      message: `Total CBM (${actualCBM}m³) exceeds sea shipping limit (${threshold}m³).`,
      suggestion: 'Order has been automatically converted to air shipping.'
    })
  }

  const handleTHBThresholdExceeded = (amount: number, threshold: number = 1500) => {
    handleBusinessRuleError({
      rule: 'THB Value Threshold',
      message: `Item value (${amount} THB) exceeds ${threshold} THB limit.`,
      suggestion: 'Additional recipient information may be required.'
    })
  }

  const handleMemberCodeMissing = () => {
    handleBusinessRuleError({
      rule: 'Member Code Required',
      message: 'Member code is missing or invalid.',
      suggestion: 'This may cause shipping delays. Please provide a valid member code.'
    })
  }

  const handleEMSHSValidationFailed = (code: string) => {
    handleBusinessRuleError({
      rule: 'EMS/HS Code Validation',
      message: `Code "${code}" is not valid or not found.`,
      suggestion: 'Please verify the code with the Korea Trade-Investment Promotion Agency.'
    })
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
    handleEMSHSValidationFailed
  }
}