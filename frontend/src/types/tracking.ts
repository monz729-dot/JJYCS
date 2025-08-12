// Tracking types for YCS LMS
export interface TrackingInfo {
  id: string
  userId: string
  orderId: string
  orderCode: string
  trackingNumber?: string
  carrierCode?: string
  carrierName?: string
  status: TrackingStatus
  shippingMethod: 'sea' | 'air'
  origin: string
  destination: string
  estimatedDelivery?: string
  actualDelivery?: string
  createdAt: string
  updatedAt: string
  events: TrackingEvent[]
  recipient: {
    name: string
    phone?: string
    address: string
    country: string
  }
  packages: TrackingPackage[]
}

export interface TrackingEvent {
  id: string
  trackingId: string
  eventType: TrackingEventType
  status: string
  description: string
  location?: string
  timestamp: string
  details?: Record<string, any>
}

export interface TrackingPackage {
  id: string
  trackingId: string
  packageNumber: string
  description?: string
  weight?: number
  dimensions?: {
    width: number
    height: number
    depth: number
  }
  cbm?: number
  status: PackageStatus
}

export type TrackingStatus = 
  | 'created'
  | 'picked_up'
  | 'in_transit'
  | 'customs_processing'
  | 'customs_cleared'
  | 'out_for_delivery'
  | 'delivered'
  | 'exception'
  | 'returned'

export type TrackingEventType =
  | 'created'
  | 'picked_up'
  | 'departed_origin'
  | 'in_transit'
  | 'arrived_hub'
  | 'departed_hub'
  | 'customs_entry'
  | 'customs_cleared'
  | 'customs_hold'
  | 'out_for_delivery'
  | 'delivery_attempt'
  | 'delivered'
  | 'exception'
  | 'returned'

export type PackageStatus =
  | 'processing'
  | 'picked_up'
  | 'in_transit'
  | 'delivered'
  | 'exception'

export interface TrackingSearchRequest {
  trackingNumber?: string
  orderCode?: string
  orderId?: string
  recipientName?: string
  dateRange?: {
    start: string
    end: string
  }
}

export interface TrackingListFilter {
  status?: TrackingStatus[]
  shippingMethod?: ('sea' | 'air')[]
  dateRange?: {
    start: string
    end: string
  }
  search?: string
}

export interface EMSTrackingResponse {
  success: boolean
  trackingNumber: string
  status: string
  events: Array<{
    date: string
    time: string
    location: string
    status: string
    description: string
  }>
  delivery?: {
    date: string
    time: string
    recipient: string
    signature?: string
  }
  error?: string
}

export interface CarrierInfo {
  code: string
  name: string
  website?: string
  trackingUrl?: string
  supportedServices: string[]
}

// API Response types
export interface TrackingApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  errors?: Record<string, string[]>
}

export interface TrackingListResponse {
  content: TrackingInfo[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}