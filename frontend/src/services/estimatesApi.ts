import { ApiResponse } from '@/types/api'

// Types
export interface EstimateRequest {
  orderId: number
  version: number
  costs: {
    shippingCost: number
    localDelivery: number
    handlingFee: number
    repackingFee?: number
    customsFee: number
    insurance?: number
  }
  additionalServices?: string[]
  specialNotes?: string
  validityDays: number
}

export interface EstimateResponse {
  id: number
  estimateNumber: string
  version: number
  status: 'draft' | 'pending' | 'approved' | 'rejected' | 'revision_requested' | 'expired' | 'cancelled'
  totalAmount: number
  subtotal: number
  tax: number
  discount: number
  currency: string
  validUntil: string
  createdAt: string
  updatedAt: string
  isExpired: boolean
  costs: {
    shippingCost: number
    localDelivery: number
    handlingFee: number
    repackingFee: number
    customsFee: number
    insurance: number
  }
  additionalServices?: Array<{
    name: string
    cost: number
  }>
  specialNotes?: string
  order: {
    id: number
    orderCode: string
    orderType: 'sea' | 'air'
    totalCbm: number
    totalWeight: number
  }
  customer: {
    name: string
    company?: string
    email: string
    phone: string
  }
  approvalHistory?: Array<{
    id: number
    action: 'created' | 'approved' | 'rejected' | 'revision_requested' | 'cancelled'
    userName: string
    createdAt: string
    note?: string
  }>
}

export interface EstimateCalculationRequest {
  orderId: number
  shippingMethod: 'sea' | 'air'
  carrier?: string
  serviceLevel?: string
}

export interface EstimateCalculationResponse {
  estimatedCosts: {
    shippingCost: number
    localDelivery: number
    handlingFee: number
    customsFee: number
    insurance?: number
    repackingFee?: number
  }
  totalAmount: number
  recommendedServices: string[]
  notes: string[]
}

export interface EstimateResponseRequest {
  action: 'approve' | 'reject' | 'request_revision'
  note?: string
}

export interface EstimateListParams {
  page?: number
  size?: number
  search?: string
  status?: string
  version?: number
  dateRange?: string
  orderId?: number
  customerId?: number
}

class EstimatesApiService {
  private baseURL = '/api'

  // Create new estimate
  async createEstimate(request: EstimateRequest): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/orders/${request.orderId}/estimates`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(request)
    })

    return response.json()
  }

  // Get estimates list
  async getEstimates(params: EstimateListParams = {}): Promise<ApiResponse<{
    content: EstimateResponse[]
    totalElements: number
    totalPages: number
    currentPage: number
  }>> {
    const queryParams = new URLSearchParams()
    
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        queryParams.append(key, value.toString())
      }
    })

    const response = await fetch(`${this.baseURL}/estimates?${queryParams}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Get estimates for specific order
  async getOrderEstimates(orderId: number): Promise<ApiResponse<EstimateResponse[]>> {
    const response = await fetch(`${this.baseURL}/orders/${orderId}/estimates`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Get single estimate
  async getEstimate(estimateId: number): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Update estimate
  async updateEstimate(estimateId: number, request: EstimateRequest): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(request)
    })

    return response.json()
  }

  // Respond to estimate (approve/reject/request revision)
  async respondToEstimate(estimateId: number, request: EstimateResponseRequest): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}/respond`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(request)
    })

    return response.json()
  }

  // Create second estimate
  async createSecondEstimate(baseEstimateId: number, request: EstimateRequest): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/${baseEstimateId}/second`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(request)
    })

    return response.json()
  }

  // Calculate estimate automatically
  async calculateEstimate(request: EstimateCalculationRequest): Promise<ApiResponse<EstimateCalculationResponse>> {
    const queryParams = new URLSearchParams({
      orderId: request.orderId.toString(),
      shippingMethod: request.shippingMethod,
      ...(request.carrier && { carrier: request.carrier }),
      ...(request.serviceLevel && { serviceLevel: request.serviceLevel })
    })

    const response = await fetch(`${this.baseURL}/estimates/calculate?${queryParams}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Cancel estimate
  async cancelEstimate(estimateId: number): Promise<ApiResponse<void>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Generate PDF
  async generatePDF(estimateId: number): Promise<Blob> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}/pdf`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.blob()
  }

  // Send estimate email
  async sendEmail(estimateId: number, recipientEmail?: string): Promise<ApiResponse<void>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}/email`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        recipientEmail
      })
    })

    return response.json()
  }

  // Get estimate statistics
  async getEstimateStats(params?: {
    dateFrom?: string
    dateTo?: string
    customerId?: number
  }): Promise<ApiResponse<{
    total: number
    pending: number
    approved: number
    rejected: number
    expired: number
    totalValue: number
    averageValue: number
  }>> {
    const queryParams = new URLSearchParams()
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams.append(key, value.toString())
        }
      })
    }

    const response = await fetch(`${this.baseURL}/estimates/stats?${queryParams}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Duplicate estimate
  async duplicateEstimate(estimateId: number): Promise<ApiResponse<EstimateResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/${estimateId}/duplicate`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Get estimate templates
  async getEstimateTemplates(): Promise<ApiResponse<Array<{
    id: number
    name: string
    description: string
    costs: {
      shippingCost: number
      localDelivery: number
      handlingFee: number
      customsFee: number
    }
    additionalServices: string[]
  }>>> {
    const response = await fetch(`${this.baseURL}/estimates/templates`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }

  // Apply estimate template
  async applyTemplate(templateId: number, orderId: number): Promise<ApiResponse<EstimateCalculationResponse>> {
    const response = await fetch(`${this.baseURL}/estimates/templates/${templateId}/apply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ orderId })
    })

    return response.json()
  }

  // Get pricing rules
  async getPricingRules(orderType: 'sea' | 'air'): Promise<ApiResponse<{
    baseRates: {
      sea: {
        perCbm: number
        minimumCharge: number
      }
      air: {
        perKg: number
        minimumCharge: number
      }
    }
    additionalFees: {
      localDelivery: number
      handlingFeeRate: number
      customsFeeRate: number
      insuranceRate: number
      repackingFeePerItem: number
    }
    serviceCharges: {
      urgent: number
      fragile: number
      photo: number
      consolidation: number
    }
  }>> {
    const response = await fetch(`${this.baseURL}/estimates/pricing-rules?orderType=${orderType}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    return response.json()
  }
}

export const estimatesApi = new EstimatesApiService()
export default estimatesApi