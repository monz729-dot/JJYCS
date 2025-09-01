import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { USER_TYPE } from '@/types'

export function useRoleCheck() {
  const authStore = useAuthStore()
  
  const hasRole = (role: string | string[]) => {
    if (!authStore.user) return false
    
    const roles = Array.isArray(role) ? role : [role]
    return roles.includes(authStore.user.userType)
  }
  
  const hasAnyRole = (...roles: string[]) => {
    if (!authStore.user) return false
    return roles.includes(authStore.user.userType)
  }
  
  const hasAllRoles = (...roles: string[]) => {
    if (!authStore.user) return false
    return roles.every(role => role === authStore.user.userType)
  }
  
  const canAccessAdmin = computed(() => 
    authStore.user?.userType === USER_TYPE.ADMIN
  )
  
  const canAccessWarehouse = computed(() => 
    authStore.user && [USER_TYPE.ADMIN, USER_TYPE.WAREHOUSE].includes(authStore.user.userType)
  )
  
  const canAccessPartner = computed(() => 
    authStore.user && [USER_TYPE.ADMIN, USER_TYPE.PARTNER].includes(authStore.user.userType)
  )
  
  const canAccessCorporate = computed(() => 
    authStore.user && [USER_TYPE.ADMIN, USER_TYPE.CORPORATE].includes(authStore.user.userType)
  )
  
  const canManageOrders = computed(() => 
    authStore.user && [USER_TYPE.ADMIN, USER_TYPE.WAREHOUSE, USER_TYPE.CORPORATE].includes(authStore.user.userType)
  )
  
  return {
    hasRole,
    hasAnyRole,
    hasAllRoles,
    canAccessAdmin,
    canAccessWarehouse,
    canAccessPartner,
    canAccessCorporate,
    canManageOrders,
    isAdmin: authStore.isAdmin,
    isWarehouse: authStore.isWarehouse,
    isPartner: authStore.isPartner,
    isCorporate: authStore.isCorporate,
    isAuthenticated: authStore.isAuthenticated,
    userType: computed(() => authStore.user?.userType)
  }
}