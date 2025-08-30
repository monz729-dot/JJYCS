// 주문 관련 타입 정의

export interface Order {
  id: number
  orderNumber: string
  userId: number
  status: OrderStatus
  shippingType: 'SEA' | 'AIR'
  
  // 기본 배송 정보
  country: string
  postalCode?: string
  destCountry: string
  destZip?: string
  
  // 수취인 정보 (기본)
  recipientName: string
  recipientPhone?: string
  recipientAddress?: string
  recipientPostalCode?: string
  
  // 다중 수취인 정보 (새로 추가)
  recipientsJson?: RecipientInfo[]
  
  // 송장 정보
  trackingNumber?: string
  
  // CBM 및 무게 정보
  totalCbm: number
  totalWeight: number
  
  // 비즈니스 규칙 플래그
  requiresExtraRecipient: boolean
  noMemberCode: boolean
  
  // 창고 정보
  storageLocation?: string
  storageArea?: string
  arrivedAt?: string
  actualWeight?: number
  
  // 리패킹 정보
  repacking: boolean
  repackingCompleted: boolean
  
  // 배송 정보
  shippedAt?: string
  deliveredAt?: string
  
  // 태국 전용 필드들
  inboundMethod: 'COURIER' | 'QUICK' | 'OTHER'
  courierCompany?: string
  waybillNo?: string
  quickVendor?: string
  quickPhone?: string
  inboundLocationId?: number
  inboundNote?: string
  thailandPostalVerified: boolean
  emsVerified: boolean
  
  // 기타
  specialRequests?: string
  warehouseNotes?: string
  
  // 타임스탬프
  createdAt: string
  updatedAt?: string
}

export type OrderStatus = 
  | 'RECEIVED' 
  | 'ARRIVED' 
  | 'REPACKING' 
  | 'SHIPPING' 
  | 'DELIVERED'
  | 'BILLING' 
  | 'PAYMENT_PENDING' 
  | 'PAYMENT_CONFIRMED' 
  | 'COMPLETED'

export interface RecipientInfo {
  order: number
  name: string
  phone: string
  address: string
  postalCode?: string
  isPrimary?: boolean
  notes?: string
}

export interface OrderItem {
  id?: number
  orderId?: number
  hsCode: string
  description: string
  quantity: number
  weight: number
  width: number
  height: number
  depth: number
  cbm?: number
  unitPrice?: number
  totalPrice?: number
  createdAt?: string
}

export interface InboundLocation {
  id: number
  name: string
  address: string
  postalCode?: string
  phone?: string
  contactPerson?: string
  businessHours?: string
  specialInstructions?: string
  isActive: boolean
  displayOrder: number
  createdAt: string
  updatedAt?: string
}

export interface CourierCompany {
  id: number
  code: string
  name: string
  nameEn?: string
  website?: string
  trackingUrlTemplate?: string
  isActive: boolean
  displayOrder: number
  createdAt: string
  updatedAt?: string
}

// 주문 생성 요청
export interface CreateOrderRequest {
  shippingType: 'SEA' | 'AIR'
  destCountry: string
  destZip?: string
  repacking?: boolean
  specialRequests?: string
  
  // 수취인 정보 (다중)
  recipients: RecipientInfo[]
  
  // 배대지 접수 정보
  inboundMethod: 'COURIER' | 'QUICK' | 'OTHER'
  courierCompany?: string
  waybillNo?: string
  quickVendor?: string
  quickPhone?: string
  inboundLocationId?: number
  inboundNote?: string
  
  // 주문 품목
  items: OrderItem[]
  
  // 비즈니스 로직 동의
  agreeAirConversion?: boolean
  agreeExtraRecipient?: boolean
  agreeDelayProcessing?: boolean
}

// API 응답 타입
export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  error?: string
  errors?: string[]
}

export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  pagination: {
    page: number
    size: number
    totalElements: number
    totalPages: number
    hasNext: boolean
    hasPrevious: boolean
  }
}

// 유틸리티 타입
export interface OrderSummary {
  totalOrders: number
  pendingOrders: number
  shippedOrders: number
  completedOrders: number
}

export interface OrderStats {
  today: number
  thisWeek: number
  thisMonth: number
  totalCbm: number
  averageWeight: number
}