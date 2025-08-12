// Mock Warehouse API service

export interface ScanRequest {
  labelCode: string
  scanType: 'inbound' | 'outbound' | 'hold' | 'mixbox'
  location?: string
  notes?: string
}

export interface ScanResponse {
  success: boolean
  orderInfo?: {
    orderCode: string
    recipient: string
    status: string
  }
  message?: string
}

export interface BatchProcessRequest {
  labelCodes: string[]
  action: 'inbound' | 'outbound' | 'hold'
  location?: string
}

const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

export const warehouseApi = {
  async scanLabel(request: ScanRequest): Promise<ScanResponse> {
    await delay()
    
    return {
      success: true,
      orderInfo: {
        orderCode: 'ORD-2024-001',
        recipient: '김철수',
        status: 'pending'
      },
      message: 'Scan successful'
    }
  },

  async batchProcess(request: BatchProcessRequest) {
    await delay()
    
    return {
      success: true,
      message: `${request.labelCodes.length} items processed successfully`
    }
  }
}