// Warehouse related types

export interface ScanRequest {
  labelCode: string
  scanType: 'inbound' | 'outbound' | 'hold' | 'mixbox'
  warehouseCode?: string
}

export interface ScanResponse {
  success: boolean
  order: Order
  box?: OrderBox  
  message: string
  warnings?: string[]
}

export interface BatchProcessRequest {
  orderIds: string[]
  action: 'inbound' | 'outbound' | 'hold'
  warehouseCode?: string
}

export interface BatchProcessResponse {
  success: boolean
  processedCount: number
  failedCount: number
  results: BatchProcessResult[]
  message: string
}

export interface BatchProcessResult {
  orderId: string
  success: boolean
  message: string
}

export interface InventoryItem {
  id: string
  userId: string
  orderId: string
  orderCode: string
  boxId?: string
  productName: string
  quantity: number
  status: 'stored' | 'processing' | 'shipped'
  location?: string
  receivedAt: string
  lastScannedAt?: string
}

export interface ScanEvent {
  id: string
  orderId: string
  boxId?: string
  labelCode: string
  scanType: string
  warehouseCode: string
  scannedBy: string
  scannedAt: string
}

export interface LabelResponse {
  success: boolean
  labelCode: string
  qrCodeUrl: string
  printUrl?: string
}

// Re-export from orders if needed
export interface Order {
  id: string
  orderCode: string
  status: string
  recipientName: string
  totalAmount: number
  currency: string
}

export interface OrderBox {
  id: string
  orderId: string
  width: number
  height: number
  depth: number
  cbmM3: number
}