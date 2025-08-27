import type { RouteRecordRaw } from 'vue-router'

export const warehouseRoutes: RouteRecordRaw[] = [
  {
    path: '/warehouse',
    name: 'warehouse',
    redirect: '/warehouse/dashboard',
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN']
    }
  },
  {
    path: '/warehouse/dashboard',
    name: 'warehouse-dashboard',
    component: () => import('./views/WarehouseDashboard.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '창고 대시보드'
    }
  },
  {
    path: '/warehouse/scan',
    name: 'warehouse-scan',
    component: () => import('./views/WarehouseScanPage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '창고 스캔'
    }
  },
  {
    path: '/warehouse/inventory',
    name: 'warehouse-inventory',
    component: () => import('./views/WarehouseInventoryPage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '재고 관리'
    }
  },
  {
    path: '/warehouse/bulk-inbound',
    name: 'warehouse-bulk-inbound',
    component: () => import('./views/BulkInboundPage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '일괄 입고'
    }
  },
  {
    path: '/warehouse/bulk-outbound',
    name: 'warehouse-bulk-outbound',
    component: () => import('./views/BulkOutboundPage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '일괄 출고'
    }
  },
  {
    path: '/warehouse/labels',
    name: 'warehouse-labels',
    component: () => import('./views/LabelsPage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '라벨 관리'
    }
  },
  {
    path: '/warehouse/inspection',
    name: 'warehouse-inspection',
    component: () => import('./views/InspectionQueuePage.vue'),
    meta: {
      requiresAuth: true,
      roles: ['WAREHOUSE', 'ADMIN'],
      title: '검품 대기'
    }
  }
]