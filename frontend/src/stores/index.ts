import { createPinia } from 'pinia'
import type { App } from 'vue'

// Create pinia instance
export const pinia = createPinia()

// Install pinia plugin
export const setupStore = (app: App) => {
  app.use(pinia)
}

// Re-export stores for convenience
export { useAuthStore } from './auth'
export { useOrdersStore } from './orders'
export { useWarehouseStore } from './warehouse'
export { useNotificationStore } from './notification'
export { useUiStore } from './ui'

// Store types
export type StoreNames = 
  | 'auth'
  | 'orders' 
  | 'warehouse'
  | 'notification'
  | 'ui'

// Global store state type
export interface RootState {
  auth: ReturnType<typeof useAuthStore>
  orders: ReturnType<typeof useOrdersStore>
  warehouse: ReturnType<typeof useWarehouseStore>
  notification: ReturnType<typeof useNotificationStore>
  ui: ReturnType<typeof useUiStore>
}