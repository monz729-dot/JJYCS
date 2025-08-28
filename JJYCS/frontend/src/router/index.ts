import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { USER_TYPE } from '../types'
import type { UserType } from '../types'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Public routes
    {
      path: '/',
      name: 'home',
      component: () => import('@/modules/common/views/HomePage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/modules/auth/views/LoginPage.vue'),
      meta: { requiresAuth: false, hideForAuth: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/modules/auth/views/RegisterPage.vue'),
      meta: { requiresAuth: false, hideForAuth: true }
    },
    {
      path: '/verify-email/:token',
      name: 'verify-email',
      component: () => import('@/modules/auth/views/VerifyEmailPage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/modules/auth/views/ForgotPasswordPage.vue'),
      meta: { requiresAuth: false, hideForAuth: true }
    },
    {
      path: '/reset-password/:token',
      name: 'reset-password',
      component: () => import('@/modules/auth/views/ResetPasswordPage.vue'),
      meta: { requiresAuth: false }
    },

    // User Dashboard routes
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/modules/dashboard/views/DashboardPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE]
      }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/modules/orders/views/OrdersListPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE, USER_TYPE.ADMIN, USER_TYPE.WAREHOUSE]
      }
    },
    {
      path: '/orders/new',
      name: 'order-create',
      component: () => import('@/modules/orders/views/CreateOrderPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE]
      }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('@/modules/orders/views/OrderDetailPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE, USER_TYPE.ADMIN, USER_TYPE.WAREHOUSE]
      }
    },
    {
      path: '/tracking',
      name: 'tracking',
      component: () => import('@/modules/orders/views/TrackingPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE]
      }
    },

    // Partner Dashboard routes
    {
      path: '/partner',
      name: 'partner-dashboard',
      component: () => import('@/modules/partner/views/PartnerDashboardPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.PARTNER]
      }
    },

    // Warehouse routes
    {
      path: '/warehouse',
      name: 'warehouse-dashboard',
      component: () => import('@/modules/warehouse/views/WarehouseDashboard.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.WAREHOUSE, USER_TYPE.ADMIN]
      }
    },
    {
      path: '/warehouse/scan',
      name: 'warehouse-scan',
      component: () => import('@/modules/warehouse/views/WarehouseScanPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.WAREHOUSE, USER_TYPE.ADMIN]
      }
    },
    {
      path: '/warehouse/inventory',
      name: 'warehouse-inventory',
      component: () => import('@/modules/warehouse/views/WarehouseInventoryPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.WAREHOUSE, USER_TYPE.ADMIN]
      }
    },
    // 파트너는 이제 주문 관리만 사용하므로 추천/수수료 기능 제거
    // {
    //   path: '/partner/referrals',
    //   name: 'partner-referrals',
    //   component: () => import('@/modules/partner/views/ReferralsPage.vue'),
    //   meta: { 
    //     requiresAuth: true,
    //     allowedRoles: [USER_TYPE.PARTNER]
    //   }
    // },
    // {
    //   path: '/partner/commissions',
    //   name: 'partner-commissions',
    //   component: () => import('@/modules/partner/views/CommissionsPage.vue'),
    //   meta: { 
    //     requiresAuth: true,
    //     allowedRoles: [USER_TYPE.PARTNER]
    //   }
    // },

    // Admin routes
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('@/modules/admin/views/AdminDashboard.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.ADMIN]
      }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/modules/admin/views/UserManagementPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.ADMIN]
      }
    },
    {
      path: '/admin/approvals',
      name: 'admin-approvals',
      component: () => import('@/modules/admin/views/ApprovalDashboard.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.ADMIN]
      }
    },
    {
      path: '/admin/orders',
      name: 'admin-orders',
      component: () => import('@/modules/admin/views/OrderManagementPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.ADMIN]
      }
    },

    // Profile routes
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/modules/common/views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/modules/common/views/SettingsPage.vue'),
      meta: { requiresAuth: true }
    },

    // Error routes
    {
      path: '/unauthorized',
      name: 'unauthorized',
      component: () => import('@/modules/common/views/UnauthorizedPage.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/modules/common/views/NotFoundPage.vue')
    }
  ]
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Try to refresh token if available
      const refreshed = await authStore.refreshToken()
      if (!refreshed) {
        next({
          name: 'login',
          query: { redirect: to.fullPath }
        })
        return
      }
    }

    // Check role-based access
    if (to.meta.allowedRoles && authStore.user) {
      const hasPermission = (to.meta.allowedRoles as UserType[]).includes(authStore.user.userType)
      if (!hasPermission) {
        next({ name: 'unauthorized' })
        return
      }
    }
  }

  // Redirect authenticated users away from auth pages
  if (to.meta.hideForAuth && authStore.isAuthenticated) {
    // Redirect to appropriate dashboard based on user type
    const redirectRoute = getDashboardRoute(authStore.user?.userType)
    next({ name: redirectRoute })
    return
  }

  next()
})

// Helper function to get dashboard route based on user type
function getDashboardRoute(userType?: UserType): string {
  switch (userType) {
    case USER_TYPE.PARTNER:
      return 'partner-dashboard'
    case USER_TYPE.ADMIN:
      return 'admin-dashboard'
    case USER_TYPE.WAREHOUSE:
      return 'warehouse-dashboard'
    case USER_TYPE.GENERAL:
    case USER_TYPE.CORPORATE:
    default:
      return 'dashboard'
  }
}

export default router