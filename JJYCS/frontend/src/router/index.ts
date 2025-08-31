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
      redirect: () => {
        const authStore = useAuthStore()
        if (authStore.isAuthenticated && authStore.user) {
          // 사용자 타입에 따라 적절한 대시보드로 리다이렉트
          switch (authStore.user.userType) {
            case USER_TYPE.ADMIN:
              return '/admin'
            case USER_TYPE.PARTNER:
              return '/partner'
            case USER_TYPE.WAREHOUSE:
              return '/warehouse'
            case USER_TYPE.GENERAL:
            case USER_TYPE.CORPORATE:
            default:
              return '/dashboard'
          }
        }
        return '/login'
      }
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
        requiresEmailVerified: true, // 이메일 인증 필수
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE, USER_TYPE.ADMIN]
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
        allowedRoles: [USER_TYPE.GENERAL, USER_TYPE.CORPORATE, USER_TYPE.ADMIN]
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

    // Bulk Management routes (for Corporate and Partner users)
    {
      path: '/bulk',
      name: 'bulk-management',
      component: () => import('@/modules/bulk/views/BulkManagementPage.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.CORPORATE, USER_TYPE.PARTNER]
      }
    },

    // Admin routes
    {
      path: '/admin',
      component: () => import('@/modules/admin/layout/AdminLayout.vue'),
      meta: { 
        requiresAuth: true,
        allowedRoles: [USER_TYPE.ADMIN],
        admin: true               // ★ 관리자 화면 표시용 플래그
      },
      children: [
        { 
          path: '', 
          name: 'admin-dashboard',
          component: () => import('@/modules/admin/views/AdminDashboard.vue') 
        },
        { 
          path: 'users', 
          name: 'admin-users',
          component: () => import('@/modules/admin/views/UserManagementPage.vue') 
        },
        { 
          path: 'approvals', 
          name: 'admin-approvals',
          component: () => import('@/modules/admin/views/ApprovalDashboard.vue') 
        },
        { 
          path: 'orders', 
          name: 'admin-orders',
          component: () => import('@/modules/admin/views/OrderManagementPage.vue') 
        },
        { 
          path: 'inbound', 
          name: 'admin-inbound',
          component: () => import('@/modules/admin/views/InboundManagementPage.vue') 
        },
      ],
    },

    // Profile routes
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('@/modules/profile/views/MyPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      redirect: '/mypage'
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/modules/common/views/SettingsPage.vue'),
      meta: { requiresAuth: true }
    },

    // Additional routes
    {
      path: '/notices',
      name: 'notices',
      component: () => import('@/modules/etc/views/NoticeListPage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/faq',
      name: 'faq',
      component: () => import('@/modules/etc/views/FaqPage.vue'),
      meta: { requiresAuth: false }
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

    // Check email verification requirement (특히 주문 작성 페이지)
    // TEMPORARY: 테스트를 위해 이메일 인증 체크 비활성화
    /*
    if (to.meta.requiresEmailVerified && authStore.user) {
      if (!authStore.user.emailVerified) {
        // 이메일 미인증 시 대시보드로 리다이렉트하고 경고 메시지 표시
        const dashboardRoute = getDashboardRoute(authStore.user.userType)
        next({ 
          name: dashboardRoute, 
          query: { 
            message: 'email_verification_required',
            returnUrl: to.fullPath
          } 
        })
        return
      }
    }
    */

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