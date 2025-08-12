import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import type { 
  ScanRequest,
  ScanResponse,
  BatchProcessRequest,
  BatchProcessResponse,
  InventoryItem,
  ScanEvent,
  LabelResponse,
  Order,
  OrderBox
} from '@/types/warehouse'

// Mock data generation for development
const generateMockInventory = (count: number = 30, userId?: string): InventoryItem[] => {
  const statuses = ['stored', 'processing', 'shipped'] as const
  const locations = ['A-001', 'A-002', 'B-001', 'B-002', 'C-001', 'C-002', 'D-001', 'D-002']
  const products = [
    '삼성 갤럭시 폰', '애플 아이폰', '나이키 신발', '아디다스 운동화',
    '소니 헤드폰', 'LG TV', '노트북 컴퓨터', '게임 콘솔',
    '화장품 세트', '패션 가방', '시계', '향수',
    '주방용품', '홈데코', '도서', '건강보조식품'
  ]
  
  const inventory: InventoryItem[] = []
  
  for (let i = 1; i <= count; i++) {
    const receivedDate = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
    const lastScanned = Math.random() > 0.3 ? 
      new Date(receivedDate.getTime() + Math.random() * 10 * 24 * 60 * 60 * 1000) : 
      undefined
    
    inventory.push({
      id: `inv-${i}`,
      userId: userId || '1',
      orderId: String(i),
      orderCode: `YCS-${new Date().getFullYear()}-${String(i).padStart(5, '0')}`,
      boxId: `box-${i}`,
      productName: products[Math.floor(Math.random() * products.length)],
      quantity: Math.floor(Math.random() * 5) + 1,
      status: statuses[Math.floor(Math.random() * statuses.length)],
      location: locations[Math.floor(Math.random() * locations.length)],
      receivedAt: receivedDate.toISOString(),
      lastScannedAt: lastScanned?.toISOString()
    })
  }
  
  return inventory.sort((a, b) => new Date(b.receivedAt).getTime() - new Date(a.receivedAt).getTime())
}

const generateMockScanEvents = (count: number = 20): ScanEvent[] => {
  const scanTypes = ['inbound', 'outbound', 'hold', 'mixbox'] as const
  const warehouses = ['WH-ICN-001', 'WH-ICN-002', 'WH-BKK-001']
  const users = ['scanner001', 'scanner002', 'admin', 'warehouse_staff']
  
  const events: ScanEvent[] = []
  
  for (let i = 1; i <= count; i++) {
    const scannedDate = new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000)
    
    events.push({
      id: `scan-${i}`,
      orderId: String(Math.floor(Math.random() * 30) + 1),
      boxId: `box-${Math.floor(Math.random() * 30) + 1}`,
      labelCode: `LBL${String(i).padStart(8, '0')}`,
      scanType: scanTypes[Math.floor(Math.random() * scanTypes.length)],
      warehouseCode: warehouses[Math.floor(Math.random() * warehouses.length)],
      scannedBy: users[Math.floor(Math.random() * users.length)],
      scannedAt: scannedDate.toISOString()
    })
  }
  
  return events.sort((a, b) => new Date(b.scannedAt).getTime() - new Date(a.scannedAt).getTime())
}

export const useWarehouseStore = defineStore('warehouse', () => {
  // State
  const inventory = ref<InventoryItem[]>([])
  const scanHistory = ref<ScanEvent[]>([])
  const currentScan = ref<ScanResponse | null>(null)
  const lastBatchProcess = ref<BatchProcessResponse | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const isScanning = ref(false)

  // Scanner state
  const scannerMode = ref<'single' | 'batch'>('single')
  const selectedBoxes = ref<string[]>([])
  const scanResults = ref<ScanResponse[]>([])

  // Getters
  const inventoryByStatus = computed(() => {
    const grouped: Record<string, InventoryItem[]> = {}
    inventory.value.forEach(item => {
      if (!grouped[item.status]) {
        grouped[item.status] = []
      }
      grouped[item.status].push(item)
    })
    return grouped
  })

  const inventoryStats = computed(() => {
    const stats = {
      total: inventory.value.length,
      stored: 0,
      ready: 0,
      shipped: 0,
      hold: 0
    }

    inventory.value.forEach(item => {
      switch (item.status) {
        case 'stored':
          stats.stored += 1
          break
        case 'ready_for_outbound':
          stats.ready += 1
          break
        case 'shipped':
          stats.shipped += 1
          break
        case 'hold':
          stats.hold += 1
          break
      }
    })

    return stats
  })

  const hasSelectedBoxes = computed(() => selectedBoxes.value.length > 0)

  // Actions
  const scanBox = async (scanData: ScanRequest) => {
    isScanning.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 800))
      
      // Generate mock scan response
      const mockOrder: Order = {
        id: String(Math.floor(Math.random() * 1000) + 1),
        orderCode: `YCS-${new Date().getFullYear()}-${String(Math.floor(Math.random() * 99999) + 1).padStart(5, '0')}`,
        status: scanData.scanType === 'inbound' ? 'in_warehouse' : 'shipped',
        recipientName: `수취인 ${Math.floor(Math.random() * 100) + 1}`,
        totalAmount: Math.floor(Math.random() * 100000) + 10000,
        currency: 'THB'
      }
      
      const mockBox: OrderBox = {
        id: `box-${Math.floor(Math.random() * 1000) + 1}`,
        orderId: mockOrder.id,
        width: Math.floor(Math.random() * 50) + 20,
        height: Math.floor(Math.random() * 50) + 20,
        depth: Math.floor(Math.random() * 50) + 20,
        cbmM3: Math.random() * 0.1
      }
      
      const scanResponse: ScanResponse = {
        success: true,
        order: mockOrder,
        box: mockBox,
        message: `Successfully scanned for ${scanData.scanType}`,
        warnings: scanData.scanType === 'hold' ? ['Package placed on hold'] : undefined
      }

      currentScan.value = scanResponse
      
      // Add to scan results for batch mode
      if (scannerMode.value === 'batch') {
        scanResults.value.push(scanResponse)
      }
      
      // Create scan event
      const scanEvent: ScanEvent = {
        id: `scan-${Date.now()}`,
        orderId: mockOrder.id,
        boxId: mockBox.id,
        labelCode: scanData.labelCode,
        scanType: scanData.scanType,
        warehouseCode: scanData.warehouseCode || 'WH-ICN-001',
        scannedBy: 'current_user',
        scannedAt: new Date().toISOString()
      }
      
      // Add to scan history
      scanHistory.value.unshift(scanEvent)
      
      // Update inventory if inbound
      if (scanData.scanType === 'inbound') {
        const inventoryItem: InventoryItem = {
          id: `inv-${Date.now()}`,
          userId: localStorage.getItem('current_user_id') || '1',
          orderId: mockOrder.id,
          orderCode: mockOrder.orderCode,
          boxId: mockBox.id,
          productName: '스캔된 제품',
          quantity: 1,
          status: 'stored',
          location: 'A-001',
          receivedAt: new Date().toISOString(),
          lastScannedAt: new Date().toISOString()
        }
        
        inventory.value.unshift(inventoryItem)
      }

      return { success: true, data: scanResponse }
    } catch (err: any) {
      error.value = err.message || 'Scan failed'
      throw err
    } finally {
      isScanning.value = false
    }
  }

  const batchProcess = async (processData: BatchProcessRequest) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      // Mock batch processing
      const results = processData.orderIds.map(orderId => {
        const success = Math.random() > 0.1 // 90% success rate
        return {
          orderId,
          success,
          message: success ? `Successfully processed ${processData.action}` : 'Processing failed'
        }
      })
      
      const successCount = results.filter(r => r.success).length
      const failedCount = results.length - successCount
      
      const batchResponse: BatchProcessResponse = {
        success: true,
        processedCount: successCount,
        failedCount,
        results,
        message: `Processed ${successCount}/${results.length} orders successfully`
      }

      lastBatchProcess.value = batchResponse
      
      // Clear selected boxes after successful batch process
      clearSelectedBoxes()

      return { success: true, data: batchResponse }
    } catch (err: any) {
      error.value = err.message || 'Batch process failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchInventory = async (warehouseId?: number, status?: string) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Generate mock data if not already loaded
      if (inventory.value.length === 0) {
        const currentUserId = localStorage.getItem('current_user_id') || '1'
        inventory.value = generateMockInventory(50, currentUserId)
      }
      
      // Filter by user and status
      const currentUserId = localStorage.getItem('current_user_id') || '1'
      let filteredInventory = inventory.value.filter(item => item.userId === currentUserId)
      
      if (status) {
        filteredInventory = filteredInventory.filter(item => item.status === status)
      }

      return { 
        success: true, 
        data: { 
          content: filteredInventory,
          totalElements: filteredInventory.length
        }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch inventory'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const fetchScanHistory = async (params?: {
    labelCode?: string
    startDate?: string
    endDate?: string
  }) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300))
      
      // Generate mock data if not already loaded
      if (scanHistory.value.length === 0) {
        scanHistory.value = generateMockScanEvents(100)
      }
      
      // Apply filters
      let filteredHistory = [...scanHistory.value]
      
      if (params?.labelCode) {
        filteredHistory = filteredHistory.filter(event => 
          event.labelCode.includes(params.labelCode!)
        )
      }
      
      if (params?.startDate && params?.endDate) {
        const startDate = new Date(params.startDate)
        const endDate = new Date(params.endDate)
        filteredHistory = filteredHistory.filter(event => {
          const eventDate = new Date(event.scannedAt)
          return eventDate >= startDate && eventDate <= endDate
        })
      }

      return { 
        success: true, 
        data: { 
          content: filteredHistory,
          totalElements: filteredHistory.length
        }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch scan history'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const holdBox = async (boxId: string, reason: string) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // Update inventory item status
      const item = inventory.value.find(item => item.boxId === boxId)
      if (item) {
        item.status = 'processing' // Using 'processing' as hold status
      }

      return { success: true, data: { boxId, status: 'hold', reason } }
    } catch (err: any) {
      error.value = err.message || 'Failed to hold box'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const generateLabel = async (boxId: string): Promise<{ success: boolean; data?: LabelResponse }> => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Mock label generation
      const labelCode = `LBL${String(Date.now()).slice(-8)}`
      const qrCodeUrl = `data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==`
      
      const labelResponse: LabelResponse = {
        success: true,
        labelCode,
        qrCodeUrl,
        printUrl: `/api/labels/${labelCode}/print`
      }

      return { success: true, data: labelResponse }
    } catch (err: any) {
      error.value = err.message || 'Failed to generate label'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const printLabel = async (boxId: string) => {
    isLoading.value = true
    error.value = null

    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 800))
      
      return { success: true, data: { printed: true, boxId } }
    } catch (err: any) {
      error.value = err.message || 'Failed to print label'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  // Scanner management
  const setScannerMode = (mode: 'single' | 'batch') => {
    scannerMode.value = mode
    if (mode === 'single') {
      clearSelectedBoxes()
      clearScanResults()
    }
  }

  const toggleBoxSelection = (labelCode: string) => {
    const index = selectedBoxes.value.indexOf(labelCode)
    if (index === -1) {
      selectedBoxes.value.push(labelCode)
    } else {
      selectedBoxes.value.splice(index, 1)
    }
  }

  const selectAllBoxes = (labelCodes: string[]) => {
    selectedBoxes.value = [...labelCodes]
  }

  const clearSelectedBoxes = () => {
    selectedBoxes.value = []
  }

  const clearScanResults = () => {
    scanResults.value = []
  }

  // Utility functions
  const updateInventoryItem = (labelCode: string, newStatus: string) => {
    const item = inventory.value.find(item => item.orderCode === labelCode)
    if (item) {
      item.status = newStatus
      item.lastScannedAt = new Date().toISOString()
    }
  }

  const getInventoryByStatus = (status: string) => {
    return inventory.value.filter(item => item.status === status)
  }

  const getInventoryItem = (labelCode: string) => {
    return inventory.value.find(item => item.orderCode === labelCode)
  }

  const isBoxSelected = (labelCode: string) => {
    return selectedBoxes.value.includes(labelCode)
  }

  // Clear functions
  const clearError = () => {
    error.value = null
  }

  const clearCurrentScan = () => {
    currentScan.value = null
  }

  const clearLastBatchProcess = () => {
    lastBatchProcess.value = null
  }

  return {
    // State
    inventory: readonly(inventory),
    scanHistory: readonly(scanHistory),
    currentScan: readonly(currentScan),
    lastBatchProcess: readonly(lastBatchProcess),
    isLoading: readonly(isLoading),
    error: readonly(error),
    isScanning: readonly(isScanning),
    
    // Scanner state
    scannerMode: readonly(scannerMode),
    selectedBoxes: readonly(selectedBoxes),
    scanResults: readonly(scanResults),
    
    // Getters
    inventoryByStatus,
    inventoryStats,
    hasSelectedBoxes,
    
    // Actions
    scanBox,
    batchProcess,
    fetchInventory,
    fetchScanHistory,
    holdBox,
    generateLabel,
    printLabel,
    
    // Scanner management
    setScannerMode,
    toggleBoxSelection,
    selectAllBoxes,
    clearSelectedBoxes,
    clearScanResults,
    
    // Utilities
    getInventoryByStatus,
    getInventoryItem,
    isBoxSelected,
    clearError,
    clearCurrentScan,
    clearLastBatchProcess
  }
})