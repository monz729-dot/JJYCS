import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'

export interface Notification {
  id: string
  type: 'success' | 'error' | 'warning' | 'info'
  title: string
  message: string
  duration?: number
  persistent?: boolean
  actions?: NotificationAction[]
  createdAt: Date
}

export interface NotificationAction {
  label: string
  handler: () => void
  style?: 'primary' | 'secondary'
}

export const useNotificationStore = defineStore('notification', () => {
  // State
  const notifications = ref<Notification[]>([])

  // Getters
  const hasNotifications = computed(() => notifications.value.length > 0)
  const unreadCount = computed(() => notifications.value.length)

  // Actions
  const addNotification = (notification: Omit<Notification, 'id' | 'createdAt'>) => {
    const id = Date.now().toString()
    const newNotification: Notification = {
      ...notification,
      id,
      createdAt: new Date(),
      duration: notification.duration ?? (notification.type === 'error' ? 0 : 5000)
    }

    notifications.value.push(newNotification)

    // Auto remove after duration
    if (newNotification.duration && newNotification.duration > 0) {
      setTimeout(() => {
        removeNotification(id)
      }, newNotification.duration)
    }

    return id
  }

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  const clearAll = () => {
    notifications.value = []
  }

  // Convenience methods
  const success = (title: string, message: string, options?: Partial<Notification>) => {
    return addNotification({
      type: 'success',
      title,
      message,
      ...options
    })
  }

  const error = (title: string, message: string, options?: Partial<Notification>) => {
    return addNotification({
      type: 'error',
      title,
      message,
      persistent: true,
      ...options
    })
  }

  const warning = (title: string, message: string, options?: Partial<Notification>) => {
    return addNotification({
      type: 'warning',
      title,
      message,
      ...options
    })
  }

  const info = (title: string, message: string, options?: Partial<Notification>) => {
    return addNotification({
      type: 'info',
      title,
      message,
      ...options
    })
  }

  return {
    // State
    notifications: readonly(notifications),
    
    // Getters
    hasNotifications,
    unreadCount,
    
    // Actions
    addNotification,
    removeNotification,
    clearAll,
    
    // Convenience methods
    success,
    error,
    warning,
    info
  }
})