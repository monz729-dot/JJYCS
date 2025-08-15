import { defineStore } from 'pinia'
import { ref } from 'vue'
import SpringBootAdminService from '@/services/adminApiService'
import type { 
  DashboardData, 
  DashboardStats, 
  OrderStatusCount, 
  RecentOrder, 
  RecentUser, 
  SystemStatus 
} from '@/services/adminApiService'

export const useAdminApiStore = defineStore('adminApi', () => {
  // State
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Dashboard data
  const stats = ref<DashboardStats | null>(null)
  const orderStatusCounts = ref<OrderStatusCount[]>([])
  const recentOrders = ref<RecentOrder[]>([])
  const recentUsers = ref<RecentUser[]>([])
  const systemStatus = ref<SystemStatus[]>([])
  const revenueChartData = ref<Array<{ date: string; revenue: number }>>([])

  // Actions

  /**
   * 대시보드 전체 데이터 로드
   */
  async function loadDashboardData(period: string = 'today') {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAdminService.getDashboardData(period)
      
      if (result.success && result.data) {
        stats.value = result.data.stats
        orderStatusCounts.value = result.data.orderStatusCounts
        recentOrders.value = result.data.recentOrders
        recentUsers.value = result.data.recentUsers
        systemStatus.value = result.data.systemStatus
      } else {
        throw new Error(result.error || '대시보드 데이터 로드에 실패했습니다')
      }
    } catch (err: any) {
      error.value = err.message
      // Fallback to mock data in case of error
      await loadMockData()
    } finally {
      loading.value = false
    }
  }

  /**
   * 시스템 통계 조회
   */
  async function loadSystemStats(period: string = 'today') {
    try {
      const result = await SpringBootAdminService.getSystemStats(period)
      
      if (result.success && result.data) {
        stats.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load system stats:', err)
    }
  }

  /**
   * 주문 상태 분포 조회
   */
  async function loadOrderStatusDistribution() {
    try {
      const result = await SpringBootAdminService.getOrderStatusDistribution()
      
      if (result.success && result.data) {
        orderStatusCounts.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load order status distribution:', err)
    }
  }

  /**
   * 최근 주문 목록 조회
   */
  async function loadRecentOrders(limit: number = 5) {
    try {
      const result = await SpringBootAdminService.getRecentOrders(limit)
      
      if (result.success && result.data) {
        recentOrders.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load recent orders:', err)
    }
  }

  /**
   * 최근 사용자 목록 조회
   */
  async function loadRecentUsers(limit: number = 5) {
    try {
      const result = await SpringBootAdminService.getRecentUsers(limit)
      
      if (result.success && result.data) {
        recentUsers.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load recent users:', err)
    }
  }

  /**
   * 시스템 상태 조회
   */
  async function loadSystemStatus() {
    try {
      const result = await SpringBootAdminService.getSystemStatus()
      
      if (result.success && result.data) {
        systemStatus.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load system status:', err)
    }
  }

  /**
   * 매출 차트 데이터 조회
   */
  async function loadRevenueChartData(period: string = '30일') {
    try {
      const result = await SpringBootAdminService.getRevenueChartData(period)
      
      if (result.success && result.data) {
        revenueChartData.value = result.data
      }
    } catch (err: any) {
      console.error('Failed to load revenue chart data:', err)
    }
  }

  /**
   * Mock 데이터 로드 (API 실패시 폴백)
   */
  async function loadMockData() {
    stats.value = {
      totalUsers: 2847,
      todayOrders: 156,
      todayRevenue: 12400000,
      completedDeliveries: 89,
      userGrowthRate: 12.3,
      orderGrowthRate: 8.1,
      revenueGrowthRate: -3.2,
      deliveryGrowthRate: 15.7
    }

    orderStatusCounts.value = [
      { status: 'REQUESTED', count: 45, percentage: 28.8 },
      { status: 'PROCESSING', count: 32, percentage: 20.5 },
      { status: 'SHIPPING', count: 28, percentage: 17.9 },
      { status: 'DELIVERED', count: 51, percentage: 32.7 }
    ]

    recentOrders.value = [
      { 
        id: 1, 
        orderNumber: 'ORD-001', 
        customerName: '김철수', 
        totalAmount: 125000, 
        currency: 'KRW', 
        status: 'REQUESTED',
        createdAt: new Date().toISOString()
      },
      { 
        id: 2, 
        orderNumber: 'ORD-002', 
        customerName: '이영희', 
        totalAmount: 89500, 
        currency: 'KRW', 
        status: 'PROCESSING',
        createdAt: new Date().toISOString()
      },
      { 
        id: 3, 
        orderNumber: 'ORD-003', 
        customerName: '박민수', 
        totalAmount: 234000, 
        currency: 'KRW', 
        status: 'SHIPPING',
        createdAt: new Date().toISOString()
      },
      { 
        id: 4, 
        orderNumber: 'ORD-004', 
        customerName: '최지영', 
        totalAmount: 67800, 
        currency: 'KRW', 
        status: 'DELIVERED',
        createdAt: new Date().toISOString()
      },
      { 
        id: 5, 
        orderNumber: 'ORD-005', 
        customerName: '정현우', 
        totalAmount: 156200, 
        currency: 'KRW', 
        status: 'REQUESTED',
        createdAt: new Date().toISOString()
      }
    ]

    recentUsers.value = [
      { id: 1, name: '김철수', email: 'kim@example.com', role: 'INDIVIDUAL', createdAt: new Date().toISOString() },
      { id: 2, name: '이영희', email: 'lee@example.com', role: 'ENTERPRISE', createdAt: new Date().toISOString() },
      { id: 3, name: '박민수', email: 'park@example.com', role: 'PARTNER', createdAt: new Date().toISOString() },
      { id: 4, name: '최지영', email: 'choi@example.com', role: 'INDIVIDUAL', createdAt: new Date().toISOString() },
      { id: 5, name: '정현우', email: 'jung@example.com', role: 'ENTERPRISE', createdAt: new Date().toISOString() }
    ]

    systemStatus.value = [
      { name: '웹 서버', status: 'online', lastChecked: new Date().toISOString() },
      { name: '데이터베이스', status: 'online', lastChecked: new Date().toISOString() },
      { name: '결제 시스템', status: 'online', lastChecked: new Date().toISOString() },
      { name: '배송 추적', status: 'online', lastChecked: new Date().toISOString() }
    ]
  }

  /**
   * 상태 초기화
   */
  function reset() {
    stats.value = null
    orderStatusCounts.value = []
    recentOrders.value = []
    recentUsers.value = []
    systemStatus.value = []
    revenueChartData.value = []
    loading.value = false
    error.value = null
  }

  return {
    // State
    loading,
    error,
    stats,
    orderStatusCounts,
    recentOrders,
    recentUsers,
    systemStatus,
    revenueChartData,

    // Actions
    loadDashboardData,
    loadSystemStats,
    loadOrderStatusDistribution,
    loadRecentOrders,
    loadRecentUsers,
    loadSystemStatus,
    loadRevenueChartData,
    loadMockData,
    reset
  }
})