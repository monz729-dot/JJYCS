import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import type {
  TrackingInfo,
  TrackingEvent,
  TrackingListFilter,
  TrackingSearchRequest,
  EMSTrackingResponse,
  TrackingStatus,
  TrackingEventType
} from '@/types/tracking'

// Mock data generation for development
const generateMockTrackingData = (count: number = 20, userId?: string): TrackingInfo[] => {
  const statuses: TrackingStatus[] = [
    'created', 'picked_up', 'in_transit', 'customs_processing', 
    'customs_cleared', 'out_for_delivery', 'delivered', 'exception'
  ]
  
  const eventTypes: TrackingEventType[] = [
    'created', 'picked_up', 'departed_origin', 'in_transit', 
    'arrived_hub', 'customs_entry', 'customs_cleared', 'delivered'
  ]
  
  const countries = ['태국', '베트남', '캄보디아', '라오스', '미얀마']
  const carriers = [
    { code: 'EMS', name: 'EMS Thailand' },
    { code: 'DHL', name: 'DHL Express' },
    { code: 'FED', name: 'FedEx' },
    { code: 'UPS', name: 'UPS' },
    { code: 'TNT', name: 'TNT Express' }
  ]
  
  const mockTracking: TrackingInfo[] = []
  
  for (let i = 1; i <= count; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const shippingMethod = Math.random() > 0.7 ? 'air' : 'sea'
    const carrier = carriers[Math.floor(Math.random() * carriers.length)]
    const country = countries[Math.floor(Math.random() * countries.length)]
    const createdDate = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
    
    // Generate tracking events
    const events: TrackingEvent[] = []
    const eventCount = Math.min(eventTypes.length, Math.floor(Math.random() * 6) + 2)
    
    for (let j = 0; j < eventCount; j++) {
      const eventDate = new Date(createdDate.getTime() + j * 24 * 60 * 60 * 1000)
      events.push({
        id: `event-${i}-${j}`,
        trackingId: String(i),
        eventType: eventTypes[j],
        status: eventTypes[j].replace('_', ' ').toUpperCase(),
        description: getEventDescription(eventTypes[j]),
        location: j === 0 ? 'Seoul, KR' : getRandomLocation(country),
        timestamp: eventDate.toISOString(),
        details: {}
      })
    }
    
    // Generate packages
    const packageCount = Math.floor(Math.random() * 3) + 1
    const packages = []
    for (let k = 1; k <= packageCount; k++) {
      packages.push({
        id: `pkg-${i}-${k}`,
        trackingId: String(i),
        packageNumber: `PKG-${String(i).padStart(5, '0')}-${k}`,
        description: `Package ${k}`,
        weight: Math.floor(Math.random() * 20) + 1,
        dimensions: {
          width: Math.floor(Math.random() * 50) + 20,
          height: Math.floor(Math.random() * 50) + 20,
          depth: Math.floor(Math.random() * 50) + 20
        },
        cbm: Math.random() * 0.1,
        status: status === 'delivered' ? 'delivered' : 'in_transit'
      })
    }
    
    const estimatedDelivery = new Date(createdDate.getTime() + (shippingMethod === 'air' ? 7 : 21) * 24 * 60 * 60 * 1000)
    
    mockTracking.push({
      id: String(i),
      userId: userId || '1',
      orderId: String(i),
      orderCode: `YCS-${new Date().getFullYear()}-${String(i).padStart(5, '0')}`,
      trackingNumber: `${carrier.code}${String(i).padStart(10, '0')}`,
      carrierCode: carrier.code,
      carrierName: carrier.name,
      status,
      shippingMethod,
      origin: '서울, 대한민국',
      destination: `${country}`,
      estimatedDelivery: estimatedDelivery.toISOString(),
      actualDelivery: status === 'delivered' ? 
        new Date(estimatedDelivery.getTime() - Math.random() * 3 * 24 * 60 * 60 * 1000).toISOString() : 
        undefined,
      createdAt: createdDate.toISOString(),
      updatedAt: new Date(createdDate.getTime() + Math.random() * 10 * 24 * 60 * 60 * 1000).toISOString(),
      events: events.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()),
      recipient: {
        name: `수취인 ${i}`,
        phone: `+66${Math.floor(Math.random() * 900000000 + 100000000)}`,
        address: `주소 ${i}, ${country}`,
        country
      },
      packages
    })
  }
  
  return mockTracking.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
}

function getEventDescription(eventType: TrackingEventType): string {
  const descriptions = {
    created: '배송 라벨 생성됨',
    picked_up: '택배사에서 픽업 완료',
    departed_origin: '출발지 터미널에서 출발',
    in_transit: '운송 중',
    arrived_hub: '허브 터미널 도착',
    departed_hub: '허브 터미널에서 출발',
    customs_entry: '세관 신고',
    customs_cleared: '세관 통관 완료',
    customs_hold: '세관 보류',
    out_for_delivery: '배송 중',
    delivery_attempt: '배송 시도',
    delivered: '배송 완료',
    exception: '배송 예외',
    returned: '송하인에게 반송'
  }
  return descriptions[eventType] || '알 수 없는 이벤트'
}

function getRandomLocation(country: string): string {
  const locations = {
    Thailand: ['Bangkok, TH', 'Chiang Mai, TH', 'Phuket, TH'],
    Vietnam: ['Ho Chi Minh City, VN', 'Hanoi, VN', 'Da Nang, VN'],
    Cambodia: ['Phnom Penh, KH', 'Siem Reap, KH'],
    Laos: ['Vientiane, LA', 'Luang Prabang, LA'],
    Myanmar: ['Yangon, MM', 'Mandalay, MM']
  }
  const countryLocations = locations[country] || [`${country} Hub`]
  return countryLocations[Math.floor(Math.random() * countryLocations.length)]
}

export const useTrackingStore = defineStore('tracking', () => {
  // State
  const trackingList = ref<TrackingInfo[]>([])
  const currentTracking = ref<TrackingInfo | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  
  // Pagination state
  const currentPage = ref(0)
  const pageSize = ref(20)
  const totalElements = ref(0)
  const totalPages = ref(0)
  
  // Filter state
  const filters = ref<TrackingListFilter>({
    status: undefined,
    shippingMethod: undefined,
    dateRange: undefined,
    search: ''
  })
  
  // Getters
  const hasTracking = computed(() => trackingList.value.length > 0)
  
  const trackingByStatus = computed(() => {
    const grouped: Record<string, TrackingInfo[]> = {}
    trackingList.value.forEach(tracking => {
      if (!grouped[tracking.status]) {
        grouped[tracking.status] = []
      }
      grouped[tracking.status].push(tracking)
    })
    return grouped
  })
  
  const trackingStats = computed(() => {
    const total = trackingList.value.length
    const delivered = trackingList.value.filter(t => t.status === 'delivered').length
    const inTransit = trackingList.value.filter(t => 
      ['picked_up', 'in_transit', 'customs_processing', 'out_for_delivery'].includes(t.status)
    ).length
    const exceptions = trackingList.value.filter(t => t.status === 'exception').length
    
    return {
      total,
      delivered,
      inTransit,
      exceptions,
      deliveryRate: total > 0 ? Math.round((delivered / total) * 100) : 0
    }
  })
  
  // Actions
  const fetchTrackingList = async (params?: {
    page?: number
    size?: number
    filters?: TrackingListFilter
  }) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Generate mock data if not already loaded
      if (trackingList.value.length === 0) {
        const currentUserId = localStorage.getItem('current_user_id') || '1'
        trackingList.value = generateMockTrackingData(50, currentUserId)
      }
      
      // Apply filters including user filtering
      const currentUserId = localStorage.getItem('current_user_id') || '1'
      let filteredTracking = trackingList.value.filter(t => t.userId === currentUserId)
      
      const activeFilters = params?.filters || filters.value
      
      if (activeFilters.status && activeFilters.status.length > 0) {
        filteredTracking = filteredTracking.filter(t => activeFilters.status!.includes(t.status))
      }
      
      if (activeFilters.shippingMethod && activeFilters.shippingMethod.length > 0) {
        filteredTracking = filteredTracking.filter(t => 
          activeFilters.shippingMethod!.includes(t.shippingMethod)
        )
      }
      
      if (activeFilters.search) {
        const searchTerm = activeFilters.search.toLowerCase()
        filteredTracking = filteredTracking.filter(t =>
          t.trackingNumber?.toLowerCase().includes(searchTerm) ||
          t.orderCode.toLowerCase().includes(searchTerm) ||
          t.recipient.name.toLowerCase().includes(searchTerm) ||
          t.destination.toLowerCase().includes(searchTerm)
        )
      }
      
      if (activeFilters.dateRange) {
        const startDate = new Date(activeFilters.dateRange.start)
        const endDate = new Date(activeFilters.dateRange.end)
        filteredTracking = filteredTracking.filter(t => {
          const trackingDate = new Date(t.createdAt)
          return trackingDate >= startDate && trackingDate <= endDate
        })
      }
      
      // Apply pagination
      const page = params?.page ?? currentPage.value
      const size = params?.size ?? pageSize.value
      const start = page * size
      const end = start + size
      const paginatedTracking = filteredTracking.slice(start, end)
      
      // Update state
      currentPage.value = page
      totalElements.value = filteredTracking.length
      totalPages.value = Math.ceil(filteredTracking.length / size)
      
      return {
        success: true,
        data: {
          content: paginatedTracking,
          page: page,
          totalElements: filteredTracking.length,
          totalPages: Math.ceil(filteredTracking.length / size)
        }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch tracking list'
      throw err
    } finally {
      isLoading.value = false
    }
  }
  
  const fetchTrackingByNumber = async (trackingNumber: string) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Ensure we have tracking data
      if (trackingList.value.length === 0) {
        trackingList.value = generateMockTrackingData(50)
      }
      
      const tracking = trackingList.value.find(t => t.trackingNumber === trackingNumber)
      
      if (tracking) {
        currentTracking.value = tracking
        return { success: true, data: tracking }
      } else {
        throw new Error('Tracking number not found')
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch tracking information'
      throw err
    } finally {
      isLoading.value = false
    }
  }
  
  const fetchTrackingByOrderCode = async (orderCode: string) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300))
      
      // Ensure we have tracking data
      if (trackingList.value.length === 0) {
        trackingList.value = generateMockTrackingData(50)
      }
      
      const tracking = trackingList.value.find(t => t.orderCode === orderCode)
      
      if (tracking) {
        currentTracking.value = tracking
        return { success: true, data: tracking }
      } else {
        throw new Error('Order not found')
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch tracking information'
      throw err
    } finally {
      isLoading.value = false
    }
  }
  
  const searchTracking = async (searchRequest: TrackingSearchRequest) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      if (searchRequest.trackingNumber) {
        return await fetchTrackingByNumber(searchRequest.trackingNumber)
      }
      
      if (searchRequest.orderCode) {
        return await fetchTrackingByOrderCode(searchRequest.orderCode)
      }
      
      // For other search criteria, filter the tracking list
      let results = trackingList.value
      
      if (searchRequest.orderId) {
        results = results.filter(t => t.orderId === searchRequest.orderId)
      }
      
      if (searchRequest.recipientName) {
        const name = searchRequest.recipientName.toLowerCase()
        results = results.filter(t => t.recipient.name.toLowerCase().includes(name))
      }
      
      if (searchRequest.dateRange) {
        const startDate = new Date(searchRequest.dateRange.start)
        const endDate = new Date(searchRequest.dateRange.end)
        results = results.filter(t => {
          const trackingDate = new Date(t.createdAt)
          return trackingDate >= startDate && trackingDate <= endDate
        })
      }
      
      return { success: true, data: results }
    } catch (err: any) {
      error.value = err.message || 'Search failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }
  
  const syncEMSTracking = async (trackingNumber: string): Promise<EMSTrackingResponse> => {
    try {
      // Simulate EMS API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Mock EMS response
      const mockResponse: EMSTrackingResponse = {
        success: true,
        trackingNumber,
        status: 'DELIVERED',
        events: [
          {
            date: '2024-01-15',
            time: '14:30',
            location: 'Bangkok, Thailand',
            status: 'DELIVERED',
            description: 'Package delivered to recipient'
          },
          {
            date: '2024-01-15',
            time: '09:00',
            location: 'Bangkok, Thailand',
            status: 'OUT_FOR_DELIVERY',
            description: 'Out for delivery'
          },
          {
            date: '2024-01-14',
            time: '16:20',
            location: 'Bangkok Sorting Center',
            status: 'ARRIVED',
            description: 'Arrived at destination facility'
          }
        ],
        delivery: {
          date: '2024-01-15',
          time: '14:30',
          recipient: 'John Doe'
        }
      }
      
      return mockResponse
    } catch (error) {
      return {
        success: false,
        trackingNumber,
        status: 'ERROR',
        events: [],
        error: 'Failed to sync with EMS API'
      }
    }
  }
  
  // Filter and search actions
  const setFilters = (newFilters: Partial<TrackingListFilter>) => {
    filters.value = { ...filters.value, ...newFilters }
    currentPage.value = 0 // Reset to first page when filters change
  }
  
  const clearFilters = () => {
    filters.value = {
      status: undefined,
      shippingMethod: undefined,
      dateRange: undefined,
      search: ''
    }
    currentPage.value = 0
  }
  
  // Pagination actions
  const setPage = (page: number) => {
    currentPage.value = page
  }
  
  const nextPage = () => {
    if (currentPage.value < totalPages.value - 1) {
      currentPage.value += 1
    }
  }
  
  const prevPage = () => {
    if (currentPage.value > 0) {
      currentPage.value -= 1
    }
  }
  
  // Utility actions
  const clearError = () => {
    error.value = null
  }
  
  const clearCurrentTracking = () => {
    currentTracking.value = null
  }
  
  const getTrackingById = (id: string) => {
    return trackingList.value.find(tracking => tracking.id === id)
  }
  
  const getTrackingByOrderCode = (orderCode: string) => {
    return trackingList.value.find(tracking => tracking.orderCode === orderCode)
  }
  
  const getRecentTracking = (limit: number = 5) => {
    return trackingList.value.slice(0, limit)
  }
  
  return {
    // State
    trackingList: readonly(trackingList),
    currentTracking: readonly(currentTracking),
    isLoading: readonly(isLoading),
    error: readonly(error),
    
    // Pagination state
    currentPage: readonly(currentPage),
    pageSize: readonly(pageSize),
    totalElements: readonly(totalElements),
    totalPages: readonly(totalPages),
    filters: readonly(filters),
    
    // Getters
    hasTracking,
    trackingByStatus,
    trackingStats,
    
    // Actions
    fetchTrackingList,
    fetchTrackingByNumber,
    fetchTrackingByOrderCode,
    searchTracking,
    syncEMSTracking,
    
    // Filter and search
    setFilters,
    clearFilters,
    
    // Pagination
    setPage,
    nextPage,
    prevPage,
    
    // Utilities
    clearError,
    clearCurrentTracking,
    getTrackingById,
    getTrackingByOrderCode,
    getRecentTracking
  }
})