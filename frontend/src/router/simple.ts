import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { RouteRecordRaw } from 'vue-router'

// 간단한 라우트 정의 (테스트용)
const routes: RouteRecordRaw[] = [
  // 인증 라우트
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/modules/auth/views/LoginView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/auth/register', 
    name: 'Register',
    component: () => import('@/modules/auth/views/RegisterView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/auth/approval',
    name: 'Approval', 
    component: () => import('@/modules/auth/views/ApprovalView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/auth/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/modules/auth/views/ApprovalView.vue'), // 임시로 ApprovalView 사용
    meta: { requiresGuest: true }
  },
  
  // 메인 앱
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  
  // 주문
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/modules/orders/views/OrderListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/create',
    name: 'OrderCreate',
    component: () => import('@/modules/orders/views/OrderCreateView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/modules/orders/views/OrderDetailView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },

  // 관리자 전용 라우트
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { requiresAuth: true, requiresRole: ['admin'] }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/AdminUsers.vue'),
    meta: { requiresAuth: true, requiresRole: ['admin'] }
  },
  {
    path: '/admin/approvals',
    name: 'AdminApprovals',
    component: () => import('@/views/AdminApprovals.vue'),
    meta: { requiresAuth: true, requiresRole: ['admin'] }
  },

  // 기본 리다이렉트
  {
    path: '/login',
    redirect: '/auth/login'
  },
  {
    path: '/register', 
    redirect: '/auth/register'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 간단한 인증 가드
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 인증 상태 확인
  if (!authStore.isInitialized) {
    await authStore.initialize()
  }

  const isAuthenticated = authStore.isAuthenticated

  // 게스트만 접근 가능
  if (to.meta.requiresGuest && isAuthenticated) {
    return next({ name: 'Dashboard' })
  }

  // 인증 필요
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: 'Login' })
  }

  // 역할 기반 접근 제어
  if (to.meta.requiresRole && isAuthenticated) {
    const userRole = authStore.user?.role
    const requiredRoles = Array.isArray(to.meta.requiresRole) ? to.meta.requiresRole : [to.meta.requiresRole]
    
    if (!userRole || !requiredRoles.includes(userRole)) {
      // 권한이 없으면 대시보드로 리다이렉트
      return next({ name: 'Dashboard' })
    }
  }

  next()
})

export default router