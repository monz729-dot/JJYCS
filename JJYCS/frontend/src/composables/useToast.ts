import { ref, reactive } from 'vue'

export interface Toast {
  id: string
  type: 'success' | 'error' | 'warning' | 'info'
  title: string
  message?: string
  duration?: number
  actions?: Array<{
    label: string
    action: () => void
    style?: 'primary' | 'secondary'
  }>
}

const toasts = ref<Toast[]>([])
let toastIdCounter = 0

export function useToast() {
  const addToast = (toast: Omit<Toast, 'id'>) => {
    const id = `toast-${++toastIdCounter}`
    const duration = toast.duration ?? (toast.type === 'error' ? 8000 : 4000)
    
    const newToast: Toast = {
      ...toast,
      id,
      duration
    }
    
    toasts.value.push(newToast)
    
    // Auto remove after duration
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }
    
    return id
  }

  const removeToast = (id: string) => {
    const index = toasts.value.findIndex(toast => toast.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  const clearAllToasts = () => {
    toasts.value.length = 0
  }

  // Convenience methods
  const success = (title: string, message?: string, options?: Partial<Toast>) => {
    return addToast({ type: 'success', title, message, ...options })
  }

  const error = (title: string, message?: string, options?: Partial<Toast>) => {
    return addToast({ type: 'error', title, message, ...options })
  }

  const warning = (title: string, message?: string, options?: Partial<Toast>) => {
    return addToast({ type: 'warning', title, message, ...options })
  }

  const info = (title: string, message?: string, options?: Partial<Toast>) => {
    return addToast({ type: 'info', title, message, ...options })
  }

  // Business-specific toast methods
  const businessRule = (ruleName: string, message: string) => {
    return warning(`Business Rule: ${ruleName}`, message, {
      duration: 6000,
      actions: [
        {
          label: 'Learn More',
          action: () => {
            // TODO: Link to documentation
            console.log('Opening business rule documentation for:', ruleName)
          },
          style: 'secondary'
        }
      ]
    })
  }

  const orderUpdate = (orderNumber: string, status: string, details?: string) => {
    return success(`Order ${orderNumber}`, `Status updated to: ${status}${details ? ` - ${details}` : ''}`)
  }

  const warehouseAction = (action: string, orderNumber: string, success: boolean = true) => {
    if (success) {
      return successToast(`${action} Completed`, `Successfully processed ${orderNumber}`)
    } else {
      return error(`${action} Failed`, `Failed to process ${orderNumber}. Please try again.`)
    }
  }

  const showToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info', options?: Partial<Toast>) => {
    return addToast({ type, title: message, ...options })
  }

  const networkError = (message?: string) => {
    return error(
      '네트워크 오류',
      message || '서버와의 연결에 문제가 발생했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.',
      {
        duration: 8000,
        actions: [
          {
            label: '다시 시도',
            action: () => window.location.reload(),
            style: 'primary'
          }
        ]
      }
    )
  }

  const validationError = (errors: string[] | string) => {
    const errorList = Array.isArray(errors) ? errors : [errors]
    const message = errorList.length === 1 
      ? errorList[0] 
      : errorList.map((err, idx) => `${idx + 1}. ${err}`).join('\n')
    
    return error(
      '입력 확인',
      message,
      {
        duration: 6000
      }
    )
  }

  const businessWarning = (type: 'cbm' | 'thb' | 'memberCode', details?: string) => {
    const messages = {
      cbm: {
        title: 'CBM 임계값 초과',
        message: `총 CBM이 29m³를 초과하여 해상운송에서 항공운송으로 자동 전환됩니다.${details ? ` (${details})` : ''}`
      },
      thb: {
        title: 'THB 임계값 초과',
        message: `품목 가액이 1,500 THB를 초과하여 수취인 추가 정보 입력이 필요합니다.${details ? ` (${details})` : ''}`
      },
      memberCode: {
        title: '회원코드 미기재',
        message: `회원코드가 없어 발송이 지연될 수 있습니다. 관리자에게 문의해주세요.${details ? ` (${details})` : ''}`
      }
    }
    
    const config = messages[type]
    return warning(config.title, config.message, {
      duration: 8000,
      actions: [
        {
          label: '자세히 보기',
          action: () => console.log(`Business rule details for: ${type}`),
          style: 'secondary'
        }
      ]
    })
  }

  // Alias for backward compatibility
  const successToast = success

  return {
    toasts: toasts.value,
    addToast,
    removeToast,
    clearAllToasts,
    success,
    error,
    warning,
    info,
    businessRule,
    orderUpdate,
    warehouseAction,
    showToast,
    networkError,
    validationError,
    businessWarning,
    successToast
  }
}