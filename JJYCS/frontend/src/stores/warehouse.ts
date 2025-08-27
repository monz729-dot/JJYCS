import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { WarehouseInventory, ScanEvent, InventoryStatus } from '../types'
import { warehouseApi } from '../utils/api'

export const useWarehouseStore = defineStore('warehouse', () => {
  // State
  const inventories = ref<WarehouseInventory[]>([])
  const currentInventory = ref<WarehouseInventory | null>(null)
  const scanEvents = ref<ScanEvent[]>([])
  const loading = ref(false)
  const scanLoading = ref(false)
  const error = ref<string | null>(null)
  const scanHistory = ref<any[]>([])
  
  // Statistics
  const statistics = ref({
    totalInventory: 0,
    pendingInbound: 0,
    readyForShipping: 0,
    onHold: 0,
    damaged: 0,
    dailyInbound: 0,
    dailyOutbound: 0
  })

  // Getters
  const pendingReceiptInventory = computed(() => 
    inventories.value.filter(inv => inv.status === 'PENDING_RECEIPT')
  )
  
  const receivedInventory = computed(() => 
    inventories.value.filter(inv => inv.status === 'RECEIVED')
  )
  
  const inspectionPendingInventory = computed(() => 
    inventories.value.filter(inv => ['RECEIVED', 'INSPECTING'].includes(inv.status))
  )
  
  const readyForShippingInventory = computed(() => 
    inventories.value.filter(inv => inv.status === 'READY_FOR_SHIPPING')
  )
  
  const onHoldInventory = computed(() => 
    inventories.value.filter(inv => inv.status === 'ON_HOLD')
  )
  
  const damagedInventory = computed(() => 
    inventories.value.filter(inv => ['DAMAGED', 'MISSING'].includes(inv.status))
  )

  const recentScanEvents = computed(() => 
    [...scanEvents.value]
      .sort((a, b) => new Date(b.scannedAt).getTime() - new Date(a.scannedAt).getTime())
      .slice(0, 10)
  )

  // Actions
  const fetchWarehouseInventory = async (warehouseId?: number, status?: InventoryStatus) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.getInventory(warehouseId, status)
      
      if (response.success && response.data) {
        inventories.value = Array.isArray(response.data) ? response.data : response.data.inventory || []
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to fetch inventory'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch inventory'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const scanItem = async (scanData: {
    labelCode: string
    scanType: 'INBOUND' | 'OUTBOUND' | 'HOLD' | 'MIXBOX'
    location?: string
    notes?: string
    warehouseId?: number
  }) => {
    scanLoading.value = true
    error.value = null

    try {
      const response = await warehouseApi.scanItem(scanData)
      
      if (response.success) {
        // Add to scan history
        scanHistory.value.unshift({
          ...scanData,
          timestamp: new Date().toISOString(),
          result: response.data
        })
        
        // Refresh inventory if needed
        if (response.data?.order) {
          await fetchWarehouseInventory()
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Scan failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Scan failed'
      return { success: false, error: error.value }
    } finally {
      scanLoading.value = false
    }
  }

  const processInbound = async (data: {
    orderId: number
    warehouseId: number
    packageCount?: number
    weight?: number
    dimensions?: { length: number; width: number; height: number }
    location?: string
    notes?: string
    receivedBy: string
  }) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.receiveOrder(data)
      
      if (response.success && response.data) {
        // Add to inventories
        inventories.value.unshift(response.data)
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to process inbound'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to process inbound'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const processOutbound = async (inventoryId: number, data: {
    trackingNumber?: string
    shippedBy: string
    notes?: string
  }) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.shipInventory(inventoryId, data)
      
      if (response.success && response.data) {
        // Update inventory in list
        const index = inventories.value.findIndex(inv => inv.id === inventoryId)
        if (index !== -1) {
          inventories.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to process outbound'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to process outbound'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const batchProcessOutbound = async (inventoryIds: number[], shippedBy: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.batchShipInventories(inventoryIds, shippedBy)
      
      if (response.success && response.data) {
        // Update inventories in list
        response.data.forEach((updatedInv: WarehouseInventory) => {
          const index = inventories.value.findIndex(inv => inv.id === updatedInv.id)
          if (index !== -1) {
            inventories.value[index] = updatedInv
          }
        })
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to process batch outbound'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to process batch outbound'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const inspectInventory = async (inventoryId: number, data: {
    inspectedBy: string
    notes?: string
    repackRequired?: boolean
    repackReason?: string
    damageReport?: string
    photos?: string[]
  }) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.inspectInventory(inventoryId, data)
      
      if (response.success && response.data) {
        // Update inventory in list
        const index = inventories.value.findIndex(inv => inv.id === inventoryId)
        if (index !== -1) {
          inventories.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to inspect inventory'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to inspect inventory'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const holdInventory = async (inventoryId: number, reason: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.holdInventory(inventoryId, reason)
      
      if (response.success && response.data) {
        // Update inventory in list
        const index = inventories.value.findIndex(inv => inv.id === inventoryId)
        if (index !== -1) {
          inventories.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to hold inventory'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to hold inventory'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const reportDamage = async (inventoryId: number, damageReport: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.reportDamage(inventoryId, damageReport)
      
      if (response.success && response.data) {
        // Update inventory in list
        const index = inventories.value.findIndex(inv => inv.id === inventoryId)
        if (index !== -1) {
          inventories.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to report damage'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to report damage'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const searchInventory = async (code: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.searchInventory(code)
      
      if (response.success && response.data) {
        currentInventory.value = response.data
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Inventory not found'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to search inventory'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchWarehouseStatistics = async (warehouseId: number, startDate: string, endDate: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.getStatistics(warehouseId, startDate, endDate)
      
      if (response.success && response.data) {
        statistics.value = response.data
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to fetch statistics'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to fetch statistics'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const updateInventoryLocation = async (inventoryId: number, location: {
    zone?: string
    shelf?: string
    position?: string
  }) => {
    loading.value = true
    error.value = null

    try {
      const response = await warehouseApi.updateInventoryLocation(inventoryId, location)
      
      if (response.success && response.data) {
        // Update inventory in list
        const index = inventories.value.findIndex(inv => inv.id === inventoryId)
        if (index !== -1) {
          inventories.value[index] = response.data
        }
        
        return { success: true, data: response.data }
      } else {
        error.value = response.error || 'Failed to update location'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to update location'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const clearCurrentInventory = () => {
    currentInventory.value = null
  }

  const clearError = () => {
    error.value = null
  }

  const clearScanHistory = () => {
    scanHistory.value = []
  }

  return {
    // State
    inventories,
    currentInventory,
    scanEvents,
    loading,
    scanLoading,
    error,
    scanHistory,
    statistics,
    
    // Getters
    pendingReceiptInventory,
    receivedInventory,
    inspectionPendingInventory,
    readyForShippingInventory,
    onHoldInventory,
    damagedInventory,
    recentScanEvents,
    
    // Actions
    fetchWarehouseInventory,
    scanItem,
    processInbound,
    processOutbound,
    batchProcessOutbound,
    inspectInventory,
    holdInventory,
    reportDamage,
    searchInventory,
    fetchWarehouseStatistics,
    updateInventoryLocation,
    clearCurrentInventory,
    clearError,
    clearScanHistory
  }
})