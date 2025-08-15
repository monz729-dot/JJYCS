import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { RouteRecordRaw } from 'vue-router'

// 레이아웃 컴포넌트
const AuthLayout = () => import('@/layouts/AuthLayout.vue')
const MainLayout = () => import('@/layouts/MainLayout.vue')
const MobileLayout = () => import('@/layouts/MobileLayout.vue')

// 라우트 정의
const routes: RouteRecordRaw[] = [
  // 인증 라우트
  {
    path: '/auth',
    component: AuthLayout,
    redirect: '/auth/login',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/modules/auth/views/LoginView.vue'),
        meta: { 
          title: 'auth.login.title',
          requiresGuest: true 
        }
      },
      {
        path: 'register',
        name: 'Register', 
        component: () => import('@/modules/auth/views/PublicSignupView.vue'),
        meta: { 
          title: 'auth.register.title',
          requiresGuest: true 
        }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/modules/auth/views/ApprovalView.vue'),
        meta: { 
          title: 'auth.approval.title',
          requiresAuth: true,
          requiresRole: ['enterprise', 'partner']
        }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/modules/auth/views/ForgotPasswordView.vue'),
        meta: { 
          title: 'auth.forgot_password.title',
          requiresGuest: true 
        }
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        component: () => import('@/modules/auth/views/ResetPasswordView.vue'),
        meta: { 
          title: 'auth.reset_password.title',
          requiresGuest: true 
        }
      },
      {
        path: 'verify-email',
        name: 'VerifyEmail',
        component: () => import('@/modules/auth/views/VerifyEmailView.vue'),
        meta: { 
          title: 'auth.verify_email.title'
        }
      },
      {
        path: 'two-factor',
        name: 'TwoFactor',
        component: () => import('@/modules/auth/views/TwoFactorView.vue'),
        meta: { 
          title: 'auth.two_factor.title',
          requiresAuth: true 
        }
      },
      {
        path: 'callback',
        name: 'AuthCallback',
        component: () => import('@/modules/auth/views/AuthCallbackView.vue'),
        meta: { 
          title: '이메일 인증 완료'
        }
      }
    ]
  },

  // 루트 경로를 로그인으로 리다이렉트
  {
    path: '/',
    redirect: '/auth/login'
  },

  // 회원가입 페이지 직접 접근
  {
    path: '/signup',
    name: 'Signup',
    component: () => import('@/modules/auth/views/PublicSignupView.vue'),
    meta: { 
      title: '회원가입',
      requiresGuest: true 
    }
  },

  // 메인 애플리케이션 라우트
  {
    path: '/app',
    component: MainLayout,
    redirect: '/app/dashboard',
    meta: { requiresAuth: true },
    children: [
      // 대시보드
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { 
          title: 'navigation.dashboard',
          icon: 'icon-dashboard'
        }
      },

      // 주문 관리
      {
        path: 'orders',
        component: () => import('@/modules/orders/OrdersLayout.vue'),
        meta: { 
          title: 'navigation.orders',
          icon: 'icon-orders',
          requiresRole: ['general', 'corporate']
        },
        children: [
          {
            path: '',
            name: 'OrderList',
            component: () => import('@/modules/orders/views/OrderListView.vue'),
            meta: { title: 'orders.list' }
          },
          {
            path: 'create',
            name: 'OrderCreate',
            component: () => import('@/modules/orders/views/OrderCreateView.vue'),
            meta: { title: 'orders.create.title' }
          },
          {
            path: ':id',
            name: 'OrderDetail',
            component: () => import('@/modules/orders/views/OrderDetailView.vue'),
            props: true,
            meta: { title: 'orders.detail' }
          },
          {
            path: ':id/edit',
            name: 'OrderEdit',
            component: () => import('@/modules/orders/views/OrderEditView.vue'),
            props: true,
            meta: { title: 'orders.edit' }
          },
          {
            path: ':id/track',
            name: 'OrderTrack',
            component: () => import('@/modules/orders/views/OrderTrackView.vue'),
            props: true,
            meta: { title: 'orders.track' }
          }
        ]
      },

      // 창고 관리
      {
        path: 'warehouse',
        component: () => import('@/modules/warehouse/WarehouseLayout.vue'),
        meta: { 
          title: 'navigation.warehouse',
          icon: 'icon-warehouse',
          requiresRole: ['warehouse', 'admin']
        },
        children: [
          {
            path: '',
            name: 'WarehouseDashboard',
            component: () => import('@/modules/warehouse/views/DashboardView.vue'),
            meta: { title: 'warehouse.title' }
          },
          {
            path: 'scan',
            name: 'WarehouseScan',
            component: () => import('@/modules/warehouse/views/ScanView.vue'),
            meta: { title: 'warehouse.scan.title' }
          },
          {
            path: 'inventory',
            name: 'WarehouseInventory',
            component: () => import('@/modules/warehouse/views/InventoryView.vue'),
            meta: { title: 'warehouse.inventory.title' }
          },
          {
            path: 'batch-process',
            name: 'WarehouseBatch',
            component: () => import('@/modules/warehouse/views/BatchProcessView.vue'),
            meta: { title: 'warehouse.batch.title' }
          },
          {
            path: 'labels',
            name: 'WarehouseLabels',
            component: () => import('@/modules/warehouse/views/LabelsView.vue'),
            meta: { title: 'warehouse.labels.title' }
          },
          {
            path: 'photos',
            name: 'WarehousePhotos',
            component: () => import('@/modules/warehouse/views/PhotosView.vue'),
            meta: { title: 'warehouse.photos.title' }
          }
        ]
      },

      // 배송 추적
      {
        path: 'tracking',
        component: () => import('@/modules/tracking/TrackingLayout.vue'),
        meta: { 
          title: 'navigation.tracking',
          icon: 'icon-tracking'
        },
        children: [
          {
            path: '',
            name: 'TrackingList',
            component: () => import('@/modules/tracking/views/TrackingListView.vue'),
            meta: { title: 'tracking.title' }
          },
          {
            path: ':trackingNumber',
            name: 'TrackingDetail',
            component: () => import('@/modules/tracking/views/TrackingDetailView.vue'),
            props: true,
            meta: { title: 'tracking.detail' }
          },
          {
            path: 'sync',
            name: 'TrackingSync',
            component: () => import('@/modules/tracking/views/SyncView.vue'),
            meta: { 
              title: 'tracking.sync',
              requiresRole: ['warehouse', 'admin']
            }
          }
        ]
      },

      // 견적 관리
      {
        path: 'estimates',
        component: () => import('@/modules/estimates/EstimatesLayout.vue'),
        meta: { 
          title: 'navigation.estimates',
          icon: 'icon-estimates'
        },
        children: [
          {
            path: '',
            name: 'EstimateList',
            component: () => import('@/modules/estimates/views/EstimateListView.vue'),
            meta: { title: 'estimates.title' }
          },
          {
            path: 'create/:orderId',
            name: 'EstimateCreate',
            component: () => import('@/modules/estimates/views/EstimateCreateView.vue'),
            props: true,
            meta: { 
              title: 'estimates.create',
              requiresRole: ['admin', 'warehouse']
            }
          },
          {
            path: ':id',
            name: 'EstimateDetail',
            component: () => import('@/modules/estimates/views/EstimateDetailView.vue'),
            props: true,
            meta: { title: 'estimates.detail' }
          }
        ]
      },

      // 결제 관리
      {
        path: 'payments',
        component: () => import('@/modules/payments/PaymentsLayout.vue'),
        meta: { 
          title: 'navigation.payments',
          icon: 'icon-payments'
        },
        children: [
          {
            path: '',
            name: 'PaymentList',
            component: () => import('@/modules/payments/views/PaymentListView.vue'),
            meta: { title: 'payments.title' }
          },
          {
            path: ':id',
            name: 'PaymentDetail',
            component: () => import('@/modules/payments/views/PaymentDetailView.vue'),
            props: true,
            meta: { title: 'payments.detail' }
          },
          {
            path: 'process/:estimateId',
            name: 'PaymentProcess',
            component: () => import('@/modules/payments/views/ProcessView.vue'),
            props: true,
            meta: { title: 'payments.process' }
          }
        ]
      },

      // 파트너 센터
      {
        path: 'partner',
        component: () => import('@/modules/partner/PartnerLayout.vue'),
        meta: { 
          title: 'navigation.partner',
          icon: 'icon-partner',
          requiresRole: ['partner']
        },
        children: [
          {
            path: '',
            name: 'PartnerDashboard',
            component: () => import('@/modules/partner/views/DashboardView.vue'),
            meta: { title: 'partner.dashboard' }
          },
          {
            path: 'referrals',
            name: 'PartnerReferrals',
            component: () => import('@/modules/partner/views/ReferralsView.vue'),
            meta: { title: 'partner.referrals' }
          },
          {
            path: 'commissions',
            name: 'PartnerCommissions',
            component: () => import('@/modules/partner/views/CommissionsView.vue'),
            meta: { title: 'partner.commissions' }
          },
          {
            path: 'settlements',
            name: 'PartnerSettlements',
            component: () => import('@/modules/partner/views/SettlementsView.vue'),
            meta: { title: 'partner.settlements' }
          },
          {
            path: 'links',
            name: 'PartnerLinks',
            component: () => import('@/modules/partner/views/LinksView.vue'),
            meta: { title: 'partner.links' }
          }
        ]
      },

      // 관리자
      {
        path: 'admin',
        component: () => import('@/modules/admin/AdminLayout.vue'),
        meta: { 
          title: 'navigation.admin',
          icon: 'icon-admin',
          requiresRole: ['admin']
        },
        children: [
          {
            path: '',
            name: 'AdminDashboard',
            component: () => import('@/modules/admin/views/DashboardView.vue'),
            meta: { title: 'admin.dashboard' }
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('@/modules/admin/views/UsersView.vue'),
            meta: { title: 'admin.users' }
          },
          {
            path: 'users/:id',
            name: 'AdminUserDetail',
            component: () => import('@/modules/admin/views/UserDetailView.vue'),
            props: true,
            meta: { title: 'admin.user_detail' }
          },
          {
            path: 'approvals',
            name: 'AdminApprovals',
            component: () => import('@/modules/admin/views/ApprovalsView.vue'),
            meta: { title: 'admin.approvals' }
          },
          {
            path: 'orders',
            name: 'AdminOrders',
            component: () => import('@/modules/admin/views/OrdersView.vue'),
            meta: { title: 'admin.orders' }
          },
          {
            path: 'warehouse',
            name: 'AdminWarehouse',
            component: () => import('@/modules/admin/views/WarehouseView.vue'),
            meta: { title: 'admin.warehouse' }
          },
          {
            path: 'settings',
            name: 'AdminSettings',
            component: () => import('@/modules/admin/views/SettingsView.vue'),
            meta: { title: 'admin.settings' }
          },
          {
            path: 'reports',
            name: 'AdminReports',
            component: () => import('@/modules/admin/views/ReportsView.vue'),
            meta: { title: 'admin.reports' }
          },
          {
            path: 'config',
            name: 'AdminConfig',
            component: () => import('@/modules/admin/views/ConfigView.vue'),
            meta: { title: 'admin.config' }
          }
        ]
      },

      // 프로필
      {
        path: 'profile',
        component: () => import('@/modules/profile/ProfileLayout.vue'),
        meta: { 
          title: 'navigation.profile',
          icon: 'icon-profile'
        },
        children: [
          {
            path: '',
            name: 'Profile',
            component: () => import('@/modules/profile/components/ProfileForm.vue'),
            meta: { title: 'profile.title' }
          },
          {
            path: 'security',
            name: 'ProfileSecurity',
            component: () => import('@/modules/profile/views/SecurityView.vue'),
            meta: { title: 'profile.security' }
          },
          {
            path: 'preferences',
            name: 'ProfilePreferences',
            component: () => import('@/modules/profile/views/PreferencesView.vue'),
            meta: { title: 'profile.preferences' }
          },
          {
            path: 'notifications',
            name: 'ProfileNotifications',
            component: () => import('@/modules/profile/views/NotificationsView.vue'),
            meta: { title: 'profile.notifications' }
          }
        ]
      },

      // 알림
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/NotificationsView.vue'),
        meta: { 
          title: 'navigation.notifications',
          icon: 'icon-notifications'
        }
      }
    ]
  },

  // 모바일 전용 라우트 (PWA)
  {
    path: '/mobile',
    component: MobileLayout,
    meta: { requiresAuth: true, mobile: true },
    children: [
      {
        path: '',
        name: 'MobileDashboard',
        component: () => import('@/views/mobile/DashboardView.vue'),
        meta: { title: 'navigation.dashboard' }
      },
      {
        path: 'scan',
        name: 'MobileScan',
        component: () => import('@/modules/warehouse/views/mobile/ScanView.vue'),
        meta: { 
          title: 'warehouse.scan.title',
          requiresRole: ['warehouse', 'admin']
        }
      },
      {
        path: 'orders',
        name: 'MobileOrders',
        component: () => import('@/modules/orders/views/mobile/OrderListView.vue'),
        meta: { title: 'orders.title' }
      }
    ]
  },

  // 공용 라우트 (추천 링크 등)
  {
    path: '/signup',
    name: 'PublicSignup',
    component: () => import('@/modules/auth/views/PublicSignupView.vue'),
    meta: { 
      title: 'auth.register.title',
      requiresGuest: true 
    }
  },
  {
    path: '/login',
    name: 'PublicLogin',
    component: () => import('@/modules/auth/views/LoginView.vue'),
    meta: { 
      title: 'auth.login.title',
      requiresGuest: true 
    }
  },
  {
    path: '/email-verification',
    name: 'EmailVerification',
    component: () => import('@/modules/auth/views/EmailVerificationView.vue'),
    meta: { 
      title: 'auth.email_verification.title',
      requiresAuth: true 
    }
  },
  {
    path: '/approval',
    name: 'Approval',
    component: () => import('@/modules/auth/views/ApprovalView.vue'),
    meta: { 
      title: 'auth.approval.title',
      requiresAuth: true 
    }
  },

  // 테스트 페이지
  {
    path: '/test-login',
    name: 'TestLogin',
    component: () => import('@/views/TestLoginView.vue'),
    meta: { title: '역할별 테스트 로그인' }
  },

  // 에러 페이지
  {
    path: '/error',
    name: 'Error',
    component: () => import('@/views/ErrorView.vue'),
    props: route => ({ 
      code: route.query.code, 
      message: route.query.message 
    })
  },

  // 404 페이지
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: 'errors.404' }
  }
]

// 라우터 생성
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 라우터 가드
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 인증 상태 확인 (세션이 있는데 사용자 정보가 없으면 가져옴)
  if (!authStore.user && !authStore.loading) {
    try {
      await authStore.fetchUserProfile()
    } catch (error) {
      
    }
  }
  
  const isAuthenticated = authStore.isAuthenticated
  const userType = authStore.userType
  const userStatus = authStore.user?.approval_status

  // 게스트만 접근 가능한 페이지 (로그인, 회원가입 등)
  if (to.meta.requiresGuest && isAuthenticated) {
    return next({ name: 'Dashboard' })
  }

  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ 
      name: 'Login', 
      query: { redirect: to.fullPath } 
    })
  }

  // 역할 기반 접근 제어
  if (to.meta.requiresRole && userType) {
    const requiredRoles = Array.isArray(to.meta.requiresRole) 
      ? to.meta.requiresRole 
      : [to.meta.requiresRole]
    
    if (!requiredRoles.includes(userType)) {
      return next({ name: 'Dashboard' })
    }
  }

  // 페이지 제목 설정
  if (to.meta.title) {
    document.title = `${to.meta.title} - YSC LMS`
  }

  next()
})

// 라우터 후처리
router.afterEach((to, from) => {
  // 페이지 뷰 추적 (Google Analytics 등)
  if (typeof gtag !== 'undefined') {
    gtag('config', 'GA_MEASUREMENT_ID', {
      page_path: to.path,
      page_title: to.meta.title
    })
  }

  // 로딩 상태 해제
  const loadingElement = document.querySelector('.route-loading')
  if (loadingElement) {
    loadingElement.remove()
  }
})

// 라우터 에러 핸들링
router.onError((error, to, from) => {
  console.error('Router error:', error)
  
  // 네트워크 에러 또는 청크 로드 실패시
  if (error.message.includes('Loading chunk') || error.message.includes('Loading CSS chunk')) {
    window.location.reload()
    return
  }

  // 기타 에러는 에러 페이지로
  router.push({ 
    name: 'Error', 
    query: { 
      code: '500', 
      message: error.message 
    } 
  })
})

export default router

// 라우터 유틸리티 함수들
export const getRoutesByRole = (role: string) => {
  return routes[1].children?.filter(route => {
    if (!route.meta?.requiresRole) return true
    const requiredRoles = Array.isArray(route.meta.requiresRole) 
      ? route.meta.requiresRole 
      : [route.meta.requiresRole]
    return requiredRoles.includes(role)
  }) || []
}

export const isRouteAccessible = (routeName: string, role: string) => {
  const route = router.resolve({ name: routeName }).matched[0]
  if (!route?.meta?.requiresRole) return true
  
  const requiredRoles = Array.isArray(route.meta.requiresRole) 
    ? route.meta.requiresRole 
    : [route.meta.requiresRole]
  return requiredRoles.includes(role)
}

export const getNavigationItems = (role: string) => {
  const items = [
    { name: 'Dashboard', title: 'navigation.dashboard', icon: 'mdi-view-dashboard' },
    { name: 'OrderList', title: 'navigation.orders', icon: 'mdi-package-variant', roles: ['general', 'corporate'] },
    { name: 'WarehouseDashboard', title: 'navigation.warehouse', icon: 'mdi-warehouse', roles: ['warehouse', 'admin'] },
    { name: 'TrackingList', title: 'navigation.tracking', icon: 'mdi-truck-delivery' },
    { name: 'EstimateList', title: 'navigation.estimates', icon: 'mdi-calculator' },
    { name: 'PaymentList', title: 'navigation.payments', icon: 'mdi-credit-card' },
    { name: 'PartnerDashboard', title: 'navigation.partner', icon: 'mdi-handshake', roles: ['partner'] },
    { name: 'AdminDashboard', title: 'navigation.admin', icon: 'mdi-shield-account', roles: ['admin'] },
    { name: 'Profile', title: 'navigation.profile', icon: 'mdi-account' },
    { name: 'Notifications', title: 'navigation.notifications', icon: 'mdi-bell' }
  ]

  return items.filter(item => {
    if (!item.roles) return true
    return item.roles.includes(role)
  })
}

// 타입 확장
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    requiresGuest?: boolean
    requiresRole?: string | string[]
    mobile?: boolean
    layout?: string
  }
}