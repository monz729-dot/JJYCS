import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Payment {
  id: string
  paymentNumber: string
  orderNumber: string
  memberId: string
  memberName: string
  amount: number
  currency: string
  method: string
  status: string
  installments: number
  description: string
  gatewayTransactionId?: string
  gatewayResponse?: string
  failureReason?: string
  refundReason?: string
  refundable: boolean
  securityInfo?: {
    sslEncrypted: boolean
    pciCompliant: boolean
    threeDSecure: boolean
  }
  createdAt: string
  updatedAt: string
}

export interface Transaction {
  id: string
  paymentId: string
  type: 'created' | 'processed' | 'completed' | 'failed' | 'refunded' | 'retry'
  description: string
  details?: string
  amount?: number
  createdAt: string
}

export interface PaymentMethod {
  id: string
  name: string
  description: string
  fee?: string
  enabled: boolean
  config?: Record<string, any>
}

export interface PaymentStats {
  totalAmount: number
  completedCount: number
  pendingCount: number
  failedCount: number
  refundedCount: number
}

export interface PaymentFilters {
  status?: string
  method?: string
  currency?: string
  dateFrom?: string
  dateTo?: string
  memberName?: string
}

export const usePaymentStore = defineStore('payment', () => {
  // State
  const payments = ref<Payment[]>([])
  const transactions = ref<Transaction[]>([])
  const paymentMethods = ref<PaymentMethod[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const selectedPayment = ref<Payment | null>(null)

  // Computed
  const paymentStats = computed((): PaymentStats => {
    const stats = payments.value.reduce(
      (acc, payment) => {
        if (payment.status === 'completed') {
          acc.completedCount++
          // Convert to KRW for total calculation (simplified)
          const rate = payment.currency === 'THB' ? 40 : payment.currency === 'USD' ? 1350 : 1
          acc.totalAmount += payment.amount * rate
        } else if (payment.status === 'pending' || payment.status === 'processing') {
          acc.pendingCount++
        } else if (payment.status === 'failed') {
          acc.failedCount++
        } else if (payment.status === 'refunded') {
          acc.refundedCount++
        }
        return acc
      },
      {
        totalAmount: 0,
        completedCount: 0,
        pendingCount: 0,
        failedCount: 0,
        refundedCount: 0
      }
    )
    return stats
  })

  const availablePaymentMethods = computed(() => 
    paymentMethods.value.filter(method => method.enabled)
  )

  // Actions
  const fetchPayments = async (filters?: PaymentFilters) => {
    loading.value = true
    error.value = null

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))

      // Mock data
      const mockPayments: Payment[] = [
        {
          id: 'pay_001',
          paymentNumber: 'PAY-2024-001',
          orderNumber: 'ORD-2024-001',
          memberId: 'user_001',
          memberName: '김철수',
          amount: 150000,
          currency: 'KRW',
          method: 'credit_card',
          status: 'completed',
          installments: 1,
          description: '태국 배송료',
          gatewayTransactionId: 'TXN-GW-2024-001',
          gatewayResponse: '승인완료',
          refundable: true,
          securityInfo: {
            sslEncrypted: true,
            pciCompliant: true,
            threeDSecure: true
          },
          createdAt: '2024-01-15T10:30:00Z',
          updatedAt: '2024-01-15T10:35:00Z'
        },
        {
          id: 'pay_002',
          paymentNumber: 'PAY-2024-002',
          orderNumber: 'ORD-2024-002',
          memberId: 'user_002',
          memberName: '박영희',
          amount: 2500,
          currency: 'THB',
          method: 'bank_transfer',
          status: 'pending',
          installments: 1,
          description: '현지 배송비',
          refundable: false,
          createdAt: '2024-01-14T14:20:00Z',
          updatedAt: '2024-01-14T14:20:00Z'
        },
        {
          id: 'pay_003',
          paymentNumber: 'PAY-2024-003',
          orderNumber: 'ORD-2024-003',
          memberId: 'user_003',
          memberName: '이민수',
          amount: 85.50,
          currency: 'USD',
          method: 'mobile_payment',
          status: 'processing',
          installments: 3,
          description: '항공운임',
          refundable: true,
          createdAt: '2024-01-13T09:15:00Z',
          updatedAt: '2024-01-13T09:45:00Z'
        },
        {
          id: 'pay_004',
          paymentNumber: 'PAY-2024-004',
          orderNumber: 'ORD-2024-004',
          memberId: 'user_004',
          memberName: '최지영',
          amount: 0.025,
          currency: 'BTC',
          method: 'crypto',
          status: 'failed',
          installments: 1,
          description: '보관료',
          failureReason: '네트워크 오류로 인한 전송 실패',
          refundable: false,
          createdAt: '2024-01-12T16:40:00Z',
          updatedAt: '2024-01-12T16:42:00Z'
        },
        {
          id: 'pay_005',
          paymentNumber: 'PAY-2024-005',
          orderNumber: 'ORD-2024-005',
          memberId: 'user_005',
          memberName: '홍길동',
          amount: 320000,
          currency: 'KRW',
          method: 'credit_card',
          status: 'refunded',
          installments: 6,
          description: '대량 배송료',
          refundReason: '고객 요청에 의한 환불',
          refundable: false,
          createdAt: '2024-01-11T11:25:00Z',
          updatedAt: '2024-01-11T15:10:00Z'
        }
      ]

      // Apply filters
      let filteredPayments = mockPayments
      if (filters) {
        if (filters.status) {
          filteredPayments = filteredPayments.filter(p => p.status === filters.status)
        }
        if (filters.method) {
          filteredPayments = filteredPayments.filter(p => p.method === filters.method)
        }
        if (filters.currency) {
          filteredPayments = filteredPayments.filter(p => p.currency === filters.currency)
        }
        if (filters.memberName) {
          const query = filters.memberName.toLowerCase()
          filteredPayments = filteredPayments.filter(p => 
            p.memberName.toLowerCase().includes(query) ||
            p.paymentNumber.toLowerCase().includes(query) ||
            p.orderNumber.toLowerCase().includes(query)
          )
        }
      }

      payments.value = filteredPayments
    } catch (err) {
      error.value = '결제 내역을 불러오는 중 오류가 발생했습니다.'
      console.error('Failed to fetch payments:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchPaymentById = async (paymentId: string): Promise<Payment | null> => {
    loading.value = true
    error.value = null

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))

      const payment = payments.value.find(p => p.id === paymentId)
      if (payment) {
        selectedPayment.value = payment
        return payment
      } else {
        throw new Error('Payment not found')
      }
    } catch (err) {
      error.value = '결제 정보를 불러오는 중 오류가 발생했습니다.'
      console.error('Failed to fetch payment:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  const fetchTransactionHistory = async (paymentId: string): Promise<Transaction[]> => {
    loading.value = true

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))

      // Mock transaction data
      const mockTransactions: Transaction[] = [
        {
          id: 'txn_001',
          paymentId,
          type: 'created',
          description: '결제 요청이 생성되었습니다',
          details: '사용자가 결제를 시작했습니다',
          createdAt: '2024-01-15T10:30:00Z'
        },
        {
          id: 'txn_002',
          paymentId,
          type: 'processed',
          description: '결제 처리가 시작되었습니다',
          details: '결제 게이트웨이로 전송됨',
          createdAt: '2024-01-15T10:32:00Z'
        },
        {
          id: 'txn_003',
          paymentId,
          type: 'completed',
          description: '결제가 성공적으로 완료되었습니다',
          details: '승인번호: 12345678',
          amount: 150000,
          createdAt: '2024-01-15T10:35:00Z'
        }
      ]

      const paymentTransactions = mockTransactions.filter(t => t.paymentId === paymentId)
      transactions.value = paymentTransactions
      return paymentTransactions
    } catch (err) {
      error.value = '거래 내역을 불러오는 중 오류가 발생했습니다.'
      console.error('Failed to fetch transactions:', err)
      return []
    } finally {
      loading.value = false
    }
  }

  const processPayment = async (paymentData: {
    estimateId: string
    amount: number
    currency: string
    method: string
    installments?: number
    cardDetails?: any
    phoneNumber?: string
    cryptoCurrency?: string
  }): Promise<{ success: boolean; paymentId?: string; error?: string }> => {
    loading.value = true
    error.value = null

    try {
      // Simulate payment processing
      await new Promise(resolve => setTimeout(resolve, 2000))

      // Mock success/failure logic
      const isSuccess = Math.random() > 0.1 // 90% success rate

      if (isSuccess) {
        const newPayment: Payment = {
          id: 'pay_' + Date.now(),
          paymentNumber: 'PAY-2024-' + Date.now().toString().slice(-6),
          orderNumber: 'ORD-2024-' + Date.now().toString().slice(-6),
          memberId: 'current_user',
          memberName: '현재 사용자',
          amount: paymentData.amount,
          currency: paymentData.currency,
          method: paymentData.method,
          status: 'completed',
          installments: paymentData.installments || 1,
          description: '견적 결제',
          gatewayTransactionId: 'TXN-' + Date.now(),
          gatewayResponse: '승인완료',
          refundable: true,
          securityInfo: {
            sslEncrypted: true,
            pciCompliant: true,
            threeDSecure: true
          },
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }

        payments.value.unshift(newPayment)
        selectedPayment.value = newPayment

        return { success: true, paymentId: newPayment.id }
      } else {
        throw new Error('결제 승인이 거절되었습니다.')
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '결제 처리 중 오류가 발생했습니다.'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  const refundPayment = async (paymentId: string, reason: string): Promise<boolean> => {
    loading.value = true
    error.value = null

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1500))

      const payment = payments.value.find(p => p.id === paymentId)
      if (!payment) {
        throw new Error('Payment not found')
      }

      if (payment.status !== 'completed' || !payment.refundable) {
        throw new Error('This payment cannot be refunded')
      }

      // Update payment status
      payment.status = 'refunded'
      payment.refundable = false
      payment.refundReason = reason
      payment.updatedAt = new Date().toISOString()

      // Add refund transaction
      const refundTransaction: Transaction = {
        id: 'txn_refund_' + Date.now(),
        paymentId,
        type: 'refunded',
        description: '환불이 처리되었습니다',
        details: `환불 사유: ${reason}`,
        amount: payment.amount,
        createdAt: new Date().toISOString()
      }

      transactions.value.push(refundTransaction)

      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : '환불 처리 중 오류가 발생했습니다.'
      return false
    } finally {
      loading.value = false
    }
  }

  const retryPayment = async (paymentId: string): Promise<boolean> => {
    loading.value = true
    error.value = null

    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))

      const payment = payments.value.find(p => p.id === paymentId)
      if (!payment) {
        throw new Error('Payment not found')
      }

      if (payment.status !== 'failed') {
        throw new Error('Only failed payments can be retried')
      }

      // Update payment status
      payment.status = 'processing'
      payment.failureReason = undefined
      payment.updatedAt = new Date().toISOString()

      // Add retry transaction
      const retryTransaction: Transaction = {
        id: 'txn_retry_' + Date.now(),
        paymentId,
        type: 'retry',
        description: '결제 재시도가 시작되었습니다',
        details: '이전 실패한 결제를 다시 처리합니다',
        createdAt: new Date().toISOString()
      }

      transactions.value.push(retryTransaction)

      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : '결제 재시도 중 오류가 발생했습니다.'
      return false
    } finally {
      loading.value = false
    }
  }

  const bulkProcessPayments = async (paymentIds: string[]): Promise<{ success: number; failed: number }> => {
    loading.value = true
    error.value = null

    try {
      // Simulate bulk processing
      await new Promise(resolve => setTimeout(resolve, 2000))

      let success = 0
      let failed = 0

      paymentIds.forEach(paymentId => {
        const payment = payments.value.find(p => p.id === paymentId)
        if (payment && payment.status === 'pending') {
          if (Math.random() > 0.2) { // 80% success rate
            payment.status = 'processing'
            payment.updatedAt = new Date().toISOString()
            success++
          } else {
            payment.status = 'failed'
            payment.failureReason = '자동 처리 실패'
            payment.updatedAt = new Date().toISOString()
            failed++
          }
        } else {
          failed++
        }
      })

      return { success, failed }
    } catch (err) {
      error.value = '일괄 처리 중 오류가 발생했습니다.'
      return { success: 0, failed: paymentIds.length }
    } finally {
      loading.value = false
    }
  }

  const fetchPaymentMethods = async () => {
    try {
      // Mock payment methods
      const mockMethods: PaymentMethod[] = [
        {
          id: 'credit_card',
          name: '신용카드',
          description: '국내외 신용카드 결제',
          fee: '2.5%',
          enabled: true,
          config: {
            supports3DS: true,
            maxInstallments: 12,
            supportedCards: ['VISA', 'MASTERCARD', 'JCB']
          }
        },
        {
          id: 'bank_transfer',
          name: '계좌이체',
          description: '실시간 계좌이체',
          enabled: true,
          config: {
            supportedBanks: ['KB', 'SH', 'WR', 'KEB', 'NH']
          }
        },
        {
          id: 'mobile_payment',
          name: '모바일결제',
          description: '카카오페이, 네이버페이',
          fee: '2.0%',
          enabled: true,
          config: {
            providers: ['kakaopay', 'naverpay', 'payco']
          }
        },
        {
          id: 'crypto',
          name: '암호화폐',
          description: 'Bitcoin, Ethereum, USDT',
          enabled: true,
          config: {
            supportedCurrencies: ['BTC', 'ETH', 'USDT'],
            confirmationsRequired: { BTC: 2, ETH: 12, USDT: 12 }
          }
        }
      ]

      paymentMethods.value = mockMethods
    } catch (err) {
      console.error('Failed to fetch payment methods:', err)
    }
  }

  // Initialize
  const initialize = async () => {
    await Promise.all([
      fetchPayments(),
      fetchPaymentMethods()
    ])
  }

  return {
    // State
    payments,
    transactions,
    paymentMethods,
    loading,
    error,
    selectedPayment,

    // Computed
    paymentStats,
    availablePaymentMethods,

    // Actions
    fetchPayments,
    fetchPaymentById,
    fetchTransactionHistory,
    processPayment,
    refundPayment,
    retryPayment,
    bulkProcessPayments,
    fetchPaymentMethods,
    initialize
  }
})