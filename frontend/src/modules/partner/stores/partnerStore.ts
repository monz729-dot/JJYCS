import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface PartnerStats {
  totalReferrals: number
  activeCustomers: number
  totalCommission: number
  pendingPayment: number
  thisMonthReferrals: number
  thisMonthCommission: number
  conversionRate: number
  averageOrderValue: number
}

export interface Referral {
  id: string
  customerName: string
  customerEmail: string
  signupDate: string
  status: 'pending' | 'active' | 'inactive'
  ordersCount: number
  totalSpent: number
  commissionEarned: number
  lastOrderDate?: string
}

export interface Commission {
  id: string
  referralId: string
  customerName: string
  orderId: string
  orderDate: string
  orderAmount: number
  commissionRate: number
  commissionAmount: number
  status: 'pending' | 'approved' | 'paid'
  paidDate?: string
}

export interface Settlement {
  id: string
  period: string
  totalCommission: number
  deductions: number
  netAmount: number
  status: 'pending' | 'processing' | 'paid' | 'failed'
  requestDate: string
  paidDate?: string
  paymentMethod: string
  transactionId?: string
}

export interface ReferralLink {
  id: string
  code: string
  name: string
  url: string
  clicks: number
  conversions: number
  isActive: boolean
  createdDate: string
  expiryDate?: string
  description?: string
}

export const usePartnerStore = defineStore('partner', () => {
  const stats = ref<PartnerStats>({
    totalReferrals: 0,
    activeCustomers: 0,
    totalCommission: 0,
    pendingPayment: 0,
    thisMonthReferrals: 0,
    thisMonthCommission: 0,
    conversionRate: 0,
    averageOrderValue: 0
  })

  const referrals = ref<Referral[]>([])
  const commissions = ref<Commission[]>([])
  const settlements = ref<Settlement[]>([])
  const referralLinks = ref<ReferralLink[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Mock data
  const initializeMockData = () => {
    stats.value = {
      totalReferrals: 47,
      activeCustomers: 34,
      totalCommission: 2450000,
      pendingPayment: 850000,
      thisMonthReferrals: 8,
      thisMonthCommission: 320000,
      conversionRate: 72.3,
      averageOrderValue: 185000
    }

    referrals.value = [
      {
        id: 'ref001',
        customerName: '김철수',
        customerEmail: 'kim.cs@example.com',
        signupDate: '2024-01-15',
        status: 'active',
        ordersCount: 5,
        totalSpent: 850000,
        commissionEarned: 85000,
        lastOrderDate: '2024-02-10'
      },
      {
        id: 'ref002', 
        customerName: '이영희',
        customerEmail: 'lee.yh@example.com',
        signupDate: '2024-01-28',
        status: 'active',
        ordersCount: 3,
        totalSpent: 520000,
        commissionEarned: 52000,
        lastOrderDate: '2024-02-08'
      },
      {
        id: 'ref003',
        customerName: '박민수',
        customerEmail: 'park.ms@example.com',
        signupDate: '2024-02-05',
        status: 'pending',
        ordersCount: 0,
        totalSpent: 0,
        commissionEarned: 0
      }
    ]

    commissions.value = [
      {
        id: 'comm001',
        referralId: 'ref001',
        customerName: '김철수',
        orderId: 'ORD2024020001',
        orderDate: '2024-02-10',
        orderAmount: 180000,
        commissionRate: 10,
        commissionAmount: 18000,
        status: 'approved'
      },
      {
        id: 'comm002',
        referralId: 'ref002',
        customerName: '이영희', 
        orderId: 'ORD2024020805',
        orderDate: '2024-02-08',
        orderAmount: 220000,
        commissionRate: 10,
        commissionAmount: 22000,
        status: 'pending'
      }
    ]

    settlements.value = [
      {
        id: 'sett001',
        period: '2024-01',
        totalCommission: 450000,
        deductions: 15000,
        netAmount: 435000,
        status: 'paid',
        requestDate: '2024-02-01',
        paidDate: '2024-02-03',
        paymentMethod: '계좌이체',
        transactionId: 'TXN240203001'
      },
      {
        id: 'sett002',
        period: '2024-02',
        totalCommission: 320000,
        deductions: 12000,
        netAmount: 308000,
        status: 'pending',
        requestDate: '2024-03-01',
        paymentMethod: '계좌이체'
      }
    ]

    referralLinks.value = [
      {
        id: 'link001',
        code: 'PARTNER001',
        name: '기본 추천 링크',
        url: 'https://ycs.logistics.com/signup?ref=PARTNER001',
        clicks: 156,
        conversions: 12,
        isActive: true,
        createdDate: '2024-01-01',
        description: '일반 고객 대상 기본 추천 링크'
      },
      {
        id: 'link002',
        code: 'ENTERPRISE2024',
        name: '기업 고객 전용 링크',
        url: 'https://ycs.logistics.com/signup?ref=ENTERPRISE2024',
        clicks: 43,
        conversions: 8,
        isActive: true,
        createdDate: '2024-01-15',
        expiryDate: '2024-12-31',
        description: '기업 고객 대상 특별 프로모션 링크'
      }
    ]
  }

  // Computed
  const conversionRate = computed(() => {
    const total = referrals.value.length
    const active = referrals.value.filter(r => r.status === 'active').length
    return total > 0 ? (active / total) * 100 : 0
  })

  const thisMonthEarnings = computed(() => {
    const thisMonth = new Date().getMonth()
    const thisYear = new Date().getFullYear()
    
    return commissions.value
      .filter(c => {
        const commissionDate = new Date(c.orderDate)
        return commissionDate.getMonth() === thisMonth && 
               commissionDate.getFullYear() === thisYear
      })
      .reduce((total, c) => total + c.commissionAmount, 0)
  })

  const activeReferralLinks = computed(() => 
    referralLinks.value.filter(link => link.isActive)
  )

  const pendingCommissions = computed(() => 
    commissions.value.filter(c => c.status === 'pending')
  )

  // Actions
  const fetchStats = async () => {
    loading.value = true
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      initializeMockData()
    } catch (err) {
      error.value = '통계를 불러오는데 실패했습니다.'
    } finally {
      loading.value = false
    }
  }

  const createReferralLink = async (linkData: Partial<ReferralLink>) => {
    loading.value = true
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      const newLink: ReferralLink = {
        id: `link${Date.now()}`,
        code: linkData.code || `PARTNER${Date.now()}`,
        name: linkData.name || '새 추천 링크',
        url: `https://ycs.logistics.com/signup?ref=${linkData.code}`,
        clicks: 0,
        conversions: 0,
        isActive: true,
        createdDate: new Date().toISOString().split('T')[0],
        description: linkData.description
      }
      
      referralLinks.value.push(newLink)
      return newLink
    } catch (err) {
      error.value = '추천 링크 생성에 실패했습니다.'
      throw err
    } finally {
      loading.value = false
    }
  }

  const updateReferralLink = async (id: string, updates: Partial<ReferralLink>) => {
    loading.value = true
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      
      const index = referralLinks.value.findIndex(link => link.id === id)
      if (index !== -1) {
        referralLinks.value[index] = { ...referralLinks.value[index], ...updates }
      }
    } catch (err) {
      error.value = '추천 링크 업데이트에 실패했습니다.'
      throw err
    } finally {
      loading.value = false
    }
  }

  const requestSettlement = async (settlementData: Partial<Settlement>) => {
    loading.value = true
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      const newSettlement: Settlement = {
        id: `sett${Date.now()}`,
        period: settlementData.period || new Date().toISOString().slice(0, 7),
        totalCommission: settlementData.totalCommission || 0,
        deductions: settlementData.deductions || 0,
        netAmount: (settlementData.totalCommission || 0) - (settlementData.deductions || 0),
        status: 'pending',
        requestDate: new Date().toISOString().split('T')[0],
        paymentMethod: settlementData.paymentMethod || '계좌이체'
      }
      
      settlements.value.unshift(newSettlement)
      return newSettlement
    } catch (err) {
      error.value = '정산 요청에 실패했습니다.'
      throw err
    } finally {
      loading.value = false
    }
  }

  // Initialize mock data on store creation
  initializeMockData()

  return {
    // State
    stats,
    referrals,
    commissions,
    settlements,
    referralLinks,
    loading,
    error,
    
    // Computed
    conversionRate,
    thisMonthEarnings,
    activeReferralLinks,
    pendingCommissions,
    
    // Actions
    fetchStats,
    createReferralLink,
    updateReferralLink,
    requestSettlement
  }
})