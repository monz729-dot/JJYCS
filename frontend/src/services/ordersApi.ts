import { supabase } from '@/lib/supabase'
import type { Order } from '@/types/orders'

export const ordersApi = {
  // 주문 목록 조회 (사용자별 필터링 포함)
  async getOrders(params: {
    page?: number
    size?: number
    status?: string
    type?: string
    startDate?: string
    endDate?: string
    userId?: string
  } = {}) {
    try {
      console.log('주문 목록 조회 시작:', params)
      
      let query = supabase
        .from('orders')
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name, email),
          order_items(*),
          order_boxes(*),
          estimates(*)
        `)
        .order('created_at', { ascending: false })

      // 사용자 필터링 (일반/기업회원은 본인 주문만)
      if (params.userId) {
        query = query.eq('user_id', params.userId)
      }

      // 상태 필터
      if (params.status) {
        query = query.eq('status', params.status)
      }

      // 배송 타입 필터
      if (params.type) {
        query = query.eq('order_type', params.type)
      }

      // 날짜 필터
      if (params.startDate) {
        query = query.gte('created_at', params.startDate)
      }

      if (params.endDate) {
        query = query.lte('created_at', params.endDate)
      }

      // 페이징
      const page = params.page || 0
      const size = params.size || 20
      const from = page * size
      const to = from + size - 1

      const { data, error, count } = await query.range(from, to)

      console.log('주문 목록 조회 결과:', { data, error, count })

      if (error) {
        console.error('주문 목록 조회 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data: data || [],
        pagination: {
          page,
          size,
          totalElements: count || 0,
          totalPages: Math.ceil((count || 0) / size)
        }
      }
    } catch (error) {
      console.error('주문 목록 조회 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '주문 목록 조회에 실패했습니다.',
        data: [],
        pagination: {
          page: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0
        }
      }
    }
  },

  // 주문 상세 조회
  async getOrder(id: string) {
    try {
      console.log('주문 상세 조회:', id)
      
      const { data, error } = await supabase
        .from('orders')
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name, email, phone),
          order_items(*),
          order_boxes(*),
          shipment_tracking(*),
          estimates(*),
          payments(*)
        `)
        .eq('id', id)
        .single()

      console.log('주문 상세 조회 결과:', { data, error })

      if (error) {
        console.error('주문 상세 조회 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data
      }
    } catch (error) {
      console.error('주문 상세 조회 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '주문 조회에 실패했습니다.',
        data: null
      }
    }
  },

  // 주문 생성
  async createOrder(orderData: any) {
    try {
      console.log('주문 생성 시작:', orderData)
      
      const { data, error } = await supabase
        .from('orders')
        .insert([orderData])
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name)
        `)
        .single()

      console.log('주문 생성 결과:', { data, error })

      if (error) {
        console.error('주문 생성 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data
      }
    } catch (error) {
      console.error('주문 생성 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '주문 생성에 실패했습니다.',
        data: null
      }
    }
  },

  // 주문 취소
  async cancelOrder(id: string, reason: string, details?: string) {
    try {
      console.log('주문 취소 시작:', { id, reason, details })
      
      const { data, error } = await supabase
        .from('orders')
        .update({
          status: 'cancelled',
          cancel_reason: reason,
          cancel_details: details,
          cancelled_at: new Date().toISOString(),
          updated_at: new Date().toISOString()
        })
        .eq('id', id)
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name)
        `)
        .single()

      console.log('주문 취소 결과:', { data, error })

      if (error) {
        console.error('주문 취소 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data
      }
    } catch (error) {
      console.error('주문 취소 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '주문 취소에 실패했습니다.',
        data: null
      }
    }
  },

  // 주문 상태 변경 (어드민/창고 관리자용)
  async updateOrderStatus(id: string, status: string) {
    try {
      console.log('주문 상태 변경:', { id, status })
      
      const { data, error } = await supabase
        .from('orders')
        .update({ 
          status,
          updated_at: new Date().toISOString()
        })
        .eq('id', id)
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name)
        `)
        .single()

      console.log('주문 상태 변경 결과:', { data, error })

      if (error) {
        console.error('주문 상태 변경 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data
      }
    } catch (error) {
      console.error('주문 상태 변경 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '주문 상태 변경에 실패했습니다.',
        data: null
      }
    }
  },

  // 일괄 상태 업데이트
  async batchUpdateOrders(orderIds: string[], updates: Partial<Order>) {
    try {
      console.log('일괄 주문 업데이트:', { orderIds, updates })
      
      const { data, error } = await supabase
        .from('orders')
        .update({
          ...updates,
          updated_at: new Date().toISOString()
        })
        .in('id', orderIds)
        .select(`
          *,
          user_profiles!orders_user_id_fkey(username, name)
        `)

      console.log('일괄 주문 업데이트 결과:', { data, error })

      if (error) {
        console.error('일괄 주문 업데이트 에러:', error)
        throw new Error(error.message)
      }

      return {
        success: true,
        data
      }
    } catch (error) {
      console.error('일괄 주문 업데이트 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '일괄 업데이트에 실패했습니다.',
        data: []
      }
    }
  },

  // 주문 통계 (대시보드용)
  async getOrderStats(userId?: string) {
    try {
      console.log('주문 통계 조회:', userId)
      
      let query = supabase.from('orders').select('status')
      
      if (userId) {
        query = query.eq('user_id', userId)
      }

      const { data, error } = await query

      if (error) {
        console.error('주문 통계 조회 에러:', error)
        throw new Error(error.message)
      }

      // 상태별 집계
      const stats = {
        total: data?.length || 0,
        pending: data?.filter(o => o.status === 'pending').length || 0,
        confirmed: data?.filter(o => o.status === 'confirmed').length || 0,
        processing: data?.filter(o => o.status === 'processing').length || 0,
        shipped: data?.filter(o => o.status === 'shipped').length || 0,
        delivered: data?.filter(o => o.status === 'delivered').length || 0,
        cancelled: data?.filter(o => o.status === 'cancelled').length || 0,
        delayed: data?.filter(o => o.status === 'delayed').length || 0
      }

      console.log('주문 통계 결과:', stats)

      return {
        success: true,
        data: stats
      }
    } catch (error) {
      console.error('주문 통계 조회 실패:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : '통계 조회에 실패했습니다.',
        data: {
          total: 0,
          pending: 0,
          confirmed: 0,
          processing: 0,
          shipped: 0,
          delivered: 0,
          cancelled: 0,
          delayed: 0
        }
      }
    }
  }
}