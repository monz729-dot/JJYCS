// Mock Orders API service
export interface Order {
  id: string
  orderCode: string
  status: 'pending' | 'approved' | 'in_transit' | 'delivered' | 'cancelled' | 'delayed'
  orderType: 'sea' | 'air'
  recipient: {
    name: string
    phone: string
    address: string
    email?: string
    idNumber?: string
  }
  items: OrderItem[]
  boxes: OrderBox[]
  totalAmount: number
  totalCBM: number
  requiresExtraRecipientInfo: boolean
  specialInstructions?: string
  createdAt: string
  updatedAt: string
}

export interface OrderItem {
  id: string
  name: string
  quantity: number
  unitPrice: number
  totalPrice: number
  emsCode: string
  hsCode: string
}

export interface OrderBox {
  id: string
  boxNumber: string
  widthCm: number
  heightCm: number
  depthCm: number
  weightKg: number
  cbmM3: number
  labelCode?: string
}

export interface OrderCreateRequest {
  recipient: {
    name: string
    phone: string
    address: string
    email?: string
    idNumber?: string
  }
  items: Omit<OrderItem, 'id' | 'totalPrice'>[]
  boxes: Omit<OrderBox, 'id' | 'cbmM3' | 'labelCode'>[]
  orderType?: 'sea' | 'air'
  specialInstructions?: string
}

// Mock data
const mockOrders: Order[] = [
  {
    id: '1',
    orderCode: 'ORD-2024-001',
    status: 'pending',
    orderType: 'sea',
    recipient: {
      name: '김철수',
      phone: '010-1234-5678',
      address: '서울시 강남구 테헤란로 123'
    },
    items: [{
      id: '1',
      name: '전자제품',
      quantity: 2,
      unitPrice: 500000,
      totalPrice: 1000000,
      emsCode: 'EMS001',
      hsCode: 'HS001'
    }],
    boxes: [{
      id: '1',
      boxNumber: 'BOX001',
      widthCm: 50,
      heightCm: 40,
      depthCm: 30,
      weightKg: 5,
      cbmM3: 0.06,
      labelCode: 'LBL001'
    }],
    totalAmount: 1000000,
    totalCBM: 0.06,
    requiresExtraRecipientInfo: false,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }
]

const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

export const ordersApi = {
  async getOrders() {
    await delay()
    return {
      success: true,
      data: mockOrders
    }
  },

  async getOrder(id: string) {
    await delay()
    const order = mockOrders.find(o => o.id === id)
    return {
      success: !!order,
      data: order
    }
  },

  async createOrder(orderData: OrderCreateRequest) {
    await delay()
    
    const newOrder: Order = {
      id: String(mockOrders.length + 1),
      orderCode: `ORD-2024-${String(mockOrders.length + 1).padStart(3, '0')}`,
      status: 'pending',
      orderType: orderData.orderType || 'sea',
      recipient: orderData.recipient,
      items: orderData.items.map((item, index) => ({
        ...item,
        id: String(index + 1),
        totalPrice: item.quantity * item.unitPrice
      })),
      boxes: orderData.boxes.map((box, index) => ({
        ...box,
        id: String(index + 1),
        cbmM3: (box.widthCm * box.heightCm * box.depthCm) / 1000000,
        labelCode: `LBL${String(index + 1).padStart(3, '0')}`
      })),
      totalAmount: orderData.items.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0),
      totalCBM: orderData.boxes.reduce((sum, box) => sum + ((box.widthCm * box.heightCm * box.depthCm) / 1000000), 0),
      requiresExtraRecipientInfo: false,
      specialInstructions: orderData.specialInstructions,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }

    mockOrders.push(newOrder)
    
    return {
      success: true,
      data: newOrder
    }
  }
}