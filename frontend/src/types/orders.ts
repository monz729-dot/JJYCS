// Order related types

export interface Order {
  id: string
  userId: string
  orderCode: string
  status: 'requested' | 'confirmed' | 'in_progress' | 'shipped' | 'delivered' | 'cancelled'
  orderType: 'air' | 'sea'
  recipientName: string
  recipientPhone: string
  recipientAddress: string
  recipientCountry: string
  totalAmount: number
  currency: string
  requiresExtraRecipient?: boolean
  createdAt: string
  updatedAt: string
  items: OrderItem[]
  boxes: OrderBox[]
}

export interface OrderItem {
  id: string
  orderId: string
  productName: string
  quantity: number
  unitPrice: number
  amount: number
  currency: string
  emsCode?: string
  hsCode?: string
  description?: string
}

export interface OrderBox {
  id: string
  orderId: string
  width: number
  height: number  
  depth: number
  weight?: number
  cbmM3: number
  description?: string
}

export interface OrderCreateRequest {
  recipientName: string
  recipientPhone: string
  recipientAddress: string
  recipientCountry: string
  items: OrderItemCreate[]
  boxes: OrderBoxCreate[]
}

export interface OrderItemCreate {
  productName: string
  quantity: number
  unitPrice: number
  currency: string
  emsCode?: string
  hsCode?: string
  description?: string
}

export interface OrderBoxCreate {
  width: number
  height: number
  depth: number
  weight?: number
  description?: string
}

export interface OrderResponse {
  success: boolean
  data: Order
  warnings?: string[]
}

export interface BusinessRuleValidation {
  cbmExceedsLimit: boolean
  amountExceedsThb1500: boolean
  requiresExtraRecipient: boolean
  memberCodeMissing: boolean
  warnings: string[]
}