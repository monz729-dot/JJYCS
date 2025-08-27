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
      return this.success(`${action} Completed`, `Successfully processed ${orderNumber}`)
    } else {
      return error(`${action} Failed`, `Failed to process ${orderNumber}. Please try again.`)
    }
  }

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
    warehouseAction
  }
}