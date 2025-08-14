// 주문 관련 타입 정의
export interface Order {
  id: string
  order_number: string
  status: 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled' | 'delayed'
  order_type: 'air' | 'sea'
  total_amount?: number
  created_at: string
  updated_at?: string
  cancelled_at?: string
  cancel_reason?: string
  cancel_details?: string
  user_id?: string
  user_profiles?: {
    username: string
    name: string
    email?: string
    phone?: string
  }
  order_items?: OrderItem[]
  order_boxes?: OrderBox[]
  shipment_tracking?: ShipmentTracking[]
}

export interface OrderItem {
  id: string
  order_id?: string
  name: string
  description?: string
  quantity: number
  unit_price: number
  total_price?: number
  ems_code?: string
  hs_code?: string
  weight?: number
  created_at?: string
}

export interface OrderBox {
  id: string
  order_id?: string
  box_number: number
  width?: number
  height?: number
  depth?: number
  weight: number
  cbm_m3: number
  label_code?: string
  status?: 'pending' | 'received' | 'processed' | 'shipped'
  created_at?: string
}

export interface ShipmentTracking {
  id: string
  order_id: string
  tracking_number: string
  carrier: string
  status: string
  location?: string
  updated_at: string
}

// 주문 생성 요청 타입
export interface OrderCreateRequest {
  order_type: 'air' | 'sea'
  recipient_name: string
  recipient_phone: string
  recipient_address: string
  shipping_notes?: string
  requires_repack?: boolean
  requires_extra_recipient?: boolean
  items: OrderItemCreate[]
  boxes: OrderBoxCreate[]
}

export interface OrderItemCreate {
  name: string
  description?: string
  quantity: number
  unit_price: number
  ems_code?: string
  hs_code?: string
  weight?: number
}

export interface OrderBoxCreate {
  box_number: number
  width?: number
  height?: number
  depth?: number
  weight: number
}

// 주문 수정 요청 타입
export interface OrderUpdateRequest {
  status?: Order['status']
  recipient_name?: string
  recipient_phone?: string
  recipient_address?: string
  shipping_notes?: string
  requires_repack?: boolean
  requires_extra_recipient?: boolean
}

// 비즈니스 룰 검증 결과
export interface BusinessRuleValidation {
  cbmExceeded: boolean
  thbExceeded: boolean
  missingMemberCode: boolean
  totalCbm: number
  totalThb: number
  warnings: string[]
  errors: string[]
}