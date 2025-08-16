import { useToast as useToastification } from 'vue-toastification'

export function useToast() {
  const toast = useToastification()

  return {
    success: (message: string) => toast.success(message),
    error: (message: string) => toast.error(message),
    warning: (message: string) => toast.warning(message),
    info: (message: string) => toast.info(message),
    showToast: (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
      switch (type) {
        case 'success': return toast.success(message)
        case 'error': return toast.error(message)
        case 'warning': return toast.warning(message)
        default: return toast.info(message)
      }
    }
  }
}