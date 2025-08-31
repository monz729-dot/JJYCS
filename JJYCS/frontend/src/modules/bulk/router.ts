import type { RouteRecordRaw } from 'vue-router'

const bulkRoutes: RouteRecordRaw[] = [
  {
    path: '/bulk',
    name: 'BulkManagement',
    component: () => import('./views/BulkManagementPage.vue'),
    meta: { 
      requiresAuth: true,
      roles: ['CORPORATE', 'PARTNER'], // 기업과 파트너만 접근 가능
      title: '일괄 등록 관리'
    }
  }
]

export default bulkRoutes