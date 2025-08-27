<template>
  <div class="warehouse-inventory-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-title">
        <h1>재고 관리</h1>
        <p class="subtitle">창고 재고 현황 및 관리</p>
      </div>
      <div class="header-actions">
        <button @click="exportInventory" class="export-btn">
          <i class="icon-download"></i>
          내보내기
        </button>
        <button @click="refreshInventory" class="refresh-btn" :disabled="loading">
          <i class="icon-refresh" :class="{ spinning: loading }"></i>
          새로고침
        </button>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="inventory-stats">
      <div class="stat-card">
        <div class="stat-icon total">
          <i class="icon-package"></i>
        </div>
        <div class="stat-content">
          <h3>{{ totalItems }}</h3>
          <p>총 재고</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon pending">
          <i class="icon-clock"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.pendingReceiptInventory.length }}</h3>
          <p>입고 대기</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon ready">
          <i class="icon-truck"></i>
        </div>
        <div class="stat-content">
          <h3>{{ warehouseStore.readyForShippingInventory.length }}</h3>
          <p>출고 준비</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon issues">
          <i class="icon-warning"></i>
        </div>
        <div class="stat-content">
          <h3>{{ issueItems }}</h3>
          <p>이슈 항목</p>
        </div>
      </div>
    </div>

    <!-- Filters and Controls -->
    <div class="inventory-controls">
      <div class="filter-section">
        <div class="filter-group">
          <label>상태</label>
          <select v-model="filters.status" @change="applyFilters">
            <option value="">전체</option>
            <option value="PENDING_RECEIPT">입고 대기</option>
            <option value="RECEIVED">입고 완료</option>
            <option value="INSPECTING">검품 중</option>
            <option value="READY_FOR_SHIPPING">출고 준비</option>
            <option value="ON_HOLD">보류</option>
            <option value="SHIPPED">출고 완료</option>
            <option value="DAMAGED">손상</option>
          </select>
        </div>

        <div class="filter-group">
          <label>기간</label>
          <select v-model="filters.period" @change="applyFilters">
            <option value="">전체</option>
            <option value="today">오늘</option>
            <option value="week">이번 주</option>
            <option value="month">이번 달</option>
          </select>
        </div>

        <div class="filter-group">
          <label>위치</label>
          <select v-model="filters.location" @change="applyFilters">
            <option value="">전체 위치</option>
            <option value="A1">A1 구역</option>
            <option value="A2">A2 구역</option>
            <option value="B1">B1 구역</option>
            <option value="B2">B2 구역</option>
          </select>
        </div>

        <div class="search-group">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="주문번호, 라벨코드 검색..."
            @input="applyFilters"
            class="search-input"
          />
          <button @click="clearFilters" class="clear-btn" v-if="hasActiveFilters">
            <i class="icon-x"></i>
            필터 지우기
          </button>
        </div>
      </div>

      <!-- Bulk Actions -->
      <div class="bulk-actions" v-if="selectedItems.length > 0">
        <div class="selection-info">
          <span class="selected-count">{{ selectedItems.length }}개 선택됨</span>
          <button @click="selectAll" class="select-all-btn">
            {{ selectedItems.length === filteredInventory.length ? '전체 해제' : '전체 선택' }}
          </button>
        </div>
        
        <div class="action-buttons">
          <button 
            @click="bulkUpdateStatus('READY_FOR_SHIPPING')" 
            class="bulk-btn ready"
            :disabled="!canBulkShip"
          >
            <i class="icon-truck"></i>
            출고 준비
          </button>
          
          <button 
            @click="bulkUpdateStatus('ON_HOLD')" 
            class="bulk-btn hold"
          >
            <i class="icon-pause"></i>
            보류
          </button>
          
          <button 
            @click="bulkUpdateLocation" 
            class="bulk-btn location"
          >
            <i class="icon-map"></i>
            위치 변경
          </button>
          
          <button 
            @click="generateBulkLabels" 
            class="bulk-btn labels"
          >
            <i class="icon-tag"></i>
            라벨 생성
          </button>
        </div>
      </div>
    </div>

    <!-- Inventory Table -->
    <div class="inventory-table-container">
      <table class="inventory-table">
        <thead>
          <tr>
            <th>
              <input 
                type="checkbox" 
                :checked="selectedItems.length === filteredInventory.length && filteredInventory.length > 0"
                @change="toggleSelectAll"
                class="table-checkbox"
              />
            </th>
            <th @click="sortBy('orderNumber')" class="sortable">
              주문번호
              <i :class="getSortIcon('orderNumber')"></i>
            </th>
            <th @click="sortBy('labelCode')" class="sortable">
              라벨코드
              <i :class="getSortIcon('labelCode')"></i>
            </th>
            <th @click="sortBy('status')" class="sortable">
              상태
              <i :class="getSortIcon('status')"></i>
            </th>
            <th>위치</th>
            <th @click="sortBy('receivedAt')" class="sortable">
              입고일
              <i :class="getSortIcon('receivedAt')"></i>
            </th>
            <th>패키지 정보</th>
            <th>메모</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedInventory" :key="item.id" class="inventory-row">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedItems"
                class="table-checkbox"
              />
            </td>
            <td>
              <div class="order-info">
                <strong>{{ item.orderNumber }}</strong>
                <small v-if="item.customerName">{{ item.customerName }}</small>
              </div>
            </td>
            <td>
              <code class="label-code">{{ item.labelCode }}</code>
            </td>
            <td>
              <span :class="['status-badge', getStatusClass(item.status)]">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <div class="location-info">
                <span v-if="item.location" class="location-badge">
                  {{ item.location }}
                </span>
                <button 
                  @click="editLocation(item)" 
                  class="location-edit-btn"
                  title="위치 변경"
                >
                  <i class="icon-edit"></i>
                </button>
              </div>
            </td>
            <td>
              <div class="date-info">
                <span class="date">{{ formatDate(item.receivedAt) }}</span>
                <small v-if="item.receivedBy">by {{ item.receivedBy }}</small>
              </div>
            </td>
            <td>
              <div class="package-info">
                <div v-if="item.packageCount">{{ item.packageCount }}개 패키지</div>
                <div v-if="item.weight" class="package-detail">{{ item.weight }}kg</div>
                <div v-if="item.dimensions" class="package-detail">
                  {{ item.dimensions.length }}×{{ item.dimensions.width }}×{{ item.dimensions.height }}cm
                </div>
              </div>
            </td>
            <td>
              <div class="notes-cell">
                <span v-if="item.notes" class="notes-text" :title="item.notes">
                  {{ item.notes.substring(0, 30) }}{{ item.notes.length > 30 ? '...' : '' }}
                </span>
                <button 
                  @click="editNotes(item)" 
                  class="notes-edit-btn"
                  title="메모 편집"
                >
                  <i class="icon-edit"></i>
                </button>
              </div>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  @click="viewDetails(item)" 
                  class="action-btn detail"
                  title="상세보기"
                >
                  <i class="icon-eye"></i>
                </button>
                
                <button 
                  v-if="canInspect(item.status)"
                  @click="inspectItem(item)" 
                  class="action-btn inspect"
                  title="검품"
                >
                  <i class="icon-search"></i>
                </button>
                
                <button 
                  v-if="canShip(item.status)"
                  @click="shipItem(item)" 
                  class="action-btn ship"
                  title="출고"
                >
                  <i class="icon-truck"></i>
                </button>
                
                <button 
                  v-if="canHold(item.status)"
                  @click="holdItem(item)" 
                  class="action-btn hold"
                  title="보류"
                >
                  <i class="icon-pause"></i>
                </button>

                <div class="dropdown">
                  <button @click="toggleDropdown(item.id)" class="action-btn more">
                    <i class="icon-more-vertical"></i>
                  </button>
                  <div v-if="activeDropdown === item.id" class="dropdown-menu">
                    <button @click="generateLabel(item)" class="dropdown-item">
                      <i class="icon-tag"></i>
                      라벨 생성
                    </button>
                    <button @click="duplicateItem(item)" class="dropdown-item">
                      <i class="icon-copy"></i>
                      복제
                    </button>
                    <button @click="printInfo(item)" class="dropdown-item">
                      <i class="icon-printer"></i>
                      인쇄
                    </button>
                    <button @click="reportDamage(item)" class="dropdown-item danger">
                      <i class="icon-alert"></i>
                      손상 신고
                    </button>
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Empty State -->
      <div v-if="!filteredInventory.length && !loading" class="empty-state">
        <i class="icon-package-x"></i>
        <h3>재고가 없습니다</h3>
        <p>현재 조건에 맞는 재고 항목이 없습니다.</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>재고 정보를 불러오는 중...</p>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        @click="currentPage = 1" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        <i class="icon-chevron-double-left"></i>
      </button>
      
      <button 
        @click="currentPage--" 
        :disabled="currentPage === 1"
        class="page-btn"
      >
        <i class="icon-chevron-left"></i>
      </button>
      
      <div class="page-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="currentPage = page"
          :class="['page-number', { active: currentPage === page }]"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        @click="currentPage++" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        <i class="icon-chevron-right"></i>
      </button>
      
      <button 
        @click="currentPage = totalPages" 
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        <i class="icon-chevron-double-right"></i>
      </button>

      <div class="page-info">
        {{ startItem }} - {{ endItem }} of {{ totalItems }} items
      </div>
    </div>

    <!-- Location Edit Modal -->
    <div v-if="showLocationModal" class="modal-overlay" @click="showLocationModal = false">
      <div class="modal-content location-modal" @click.stop>
        <div class="modal-header">
          <h3>위치 변경</h3>
          <button @click="showLocationModal = false" class="close-btn">
            <i class="icon-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="location-form">
            <div class="form-group">
              <label>구역</label>
              <select v-model="locationForm.zone">
                <option value="">선택하세요</option>
                <option value="A1">A1</option>
                <option value="A2">A2</option>
                <option value="B1">B1</option>
                <option value="B2">B2</option>
              </select>
            </div>
            <div class="form-group">
              <label>선반</label>
              <input 
                v-model="locationForm.shelf" 
                type="text" 
                placeholder="01-05"
              />
            </div>
            <div class="form-group">
              <label>위치</label>
              <input 
                v-model="locationForm.position" 
                type="text" 
                placeholder="A"
              />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showLocationModal = false" class="btn secondary">
            취소
          </button>
          <button @click="saveLocation" class="btn primary">
            저장
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWarehouseStore } from '../../../stores/warehouse'
import type { WarehouseInventory, InventoryStatus } from '../../../types'

const warehouseStore = useWarehouseStore()

// State
const loading = ref(false)
const searchQuery = ref('')
const selectedItems = ref<number[]>([])
const activeDropdown = ref<number | null>(null)
const currentPage = ref(1)
const itemsPerPage = ref(20)

// Filters
const filters = ref({
  status: '' as InventoryStatus | '',
  period: '',
  location: ''
})

// Sorting
const sortField = ref<string>('receivedAt')
const sortDirection = ref<'asc' | 'desc'>('desc')

// Location Modal
const showLocationModal = ref(false)
const locationForm = ref({
  zone: '',
  shelf: '',
  position: ''
})
const editingLocationItem = ref<WarehouseInventory | null>(null)

// Computed Properties
const totalItems = computed(() => warehouseStore.inventories.length)
const issueItems = computed(() => 
  warehouseStore.onHoldInventory.length + warehouseStore.damagedInventory.length
)

const hasActiveFilters = computed(() => {
  return filters.value.status || filters.value.period || filters.value.location || searchQuery.value
})

const filteredInventory = computed(() => {
  let items = [...warehouseStore.inventories]
  
  // Status filter
  if (filters.value.status) {
    items = items.filter(item => item.status === filters.value.status)
  }
  
  // Period filter
  if (filters.value.period) {
    const now = new Date()
    const filterDate = new Date()
    
    switch (filters.value.period) {
      case 'today':
        filterDate.setHours(0, 0, 0, 0)
        break
      case 'week':
        filterDate.setDate(now.getDate() - 7)
        break
      case 'month':
        filterDate.setMonth(now.getMonth() - 1)
        break
    }
    
    items = items.filter(item => new Date(item.receivedAt) >= filterDate)
  }
  
  // Location filter
  if (filters.value.location) {
    items = items.filter(item => 
      item.location && item.location.startsWith(filters.value.location)
    )
  }
  
  // Search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    items = items.filter(item =>
      item.orderNumber.toLowerCase().includes(query) ||
      item.labelCode.toLowerCase().includes(query) ||
      (item.customerName && item.customerName.toLowerCase().includes(query))
    )
  }
  
  // Sorting
  items.sort((a, b) => {
    const aValue = a[sortField.value as keyof WarehouseInventory] || ''
    const bValue = b[sortField.value as keyof WarehouseInventory] || ''
    
    if (sortDirection.value === 'asc') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })
  
  return items
})

const totalPages = computed(() => Math.ceil(filteredInventory.value.length / itemsPerPage.value))

const paginatedInventory = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredInventory.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  const half = Math.floor(maxVisible / 2)
  
  let start = Math.max(1, currentPage.value - half)
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, filteredInventory.value.length))

const canBulkShip = computed(() => {
  return selectedItems.value.every(id => {
    const item = warehouseStore.inventories.find(inv => inv.id === id)
    return item && ['RECEIVED', 'INSPECTING'].includes(item.status)
  })
})

// Methods
const refreshInventory = async () => {
  loading.value = true
  try {
    await warehouseStore.fetchWarehouseInventory()
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  currentPage.value = 1
  selectedItems.value = []
}

const clearFilters = () => {
  filters.value = {
    status: '',
    period: '',
    location: ''
  }
  searchQuery.value = ''
  applyFilters()
}

const sortBy = (field: string) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'asc'
  }
}

const getSortIcon = (field: string) => {
  if (sortField.value !== field) return 'icon-sort'
  return sortDirection.value === 'asc' ? 'icon-sort-up' : 'icon-sort-down'
}

const toggleSelectAll = () => {
  if (selectedItems.value.length === filteredInventory.value.length) {
    selectedItems.value = []
  } else {
    selectedItems.value = filteredInventory.value.map(item => item.id)
  }
}

const selectAll = () => {
  selectedItems.value = filteredInventory.value.map(item => item.id)
}

const toggleDropdown = (itemId: number) => {
  activeDropdown.value = activeDropdown.value === itemId ? null : itemId
}

// Item Actions
const viewDetails = (item: WarehouseInventory) => {
  // Navigate to detail page or open modal
  console.log('View details for:', item.orderNumber)
}

const inspectItem = async (item: WarehouseInventory) => {
  try {
    await warehouseStore.inspectInventory(item.id, {
      inspectedBy: 'current-user',
      notes: '검품 완료'
    })
  } catch (error) {
    console.error('Inspection failed:', error)
  }
}

const shipItem = async (item: WarehouseInventory) => {
  try {
    await warehouseStore.processOutbound(item.id, {
      shippedBy: 'current-user'
    })
  } catch (error) {
    console.error('Shipping failed:', error)
  }
}

const holdItem = async (item: WarehouseInventory) => {
  const reason = prompt('보류 사유를 입력하세요:')
  if (reason) {
    try {
      await warehouseStore.holdInventory(item.id, reason)
    } catch (error) {
      console.error('Hold failed:', error)
    }
  }
}

const editLocation = (item: WarehouseInventory) => {
  editingLocationItem.value = item
  const location = item.location?.split('-') || []
  locationForm.value = {
    zone: location[0] || '',
    shelf: location[1] || '',
    position: location[2] || ''
  }
  showLocationModal.value = true
}

const saveLocation = async () => {
  if (!editingLocationItem.value) return
  
  const locationString = [
    locationForm.value.zone,
    locationForm.value.shelf,
    locationForm.value.position
  ].filter(Boolean).join('-')
  
  try {
    await warehouseStore.updateInventoryLocation(editingLocationItem.value.id, {
      zone: locationForm.value.zone,
      shelf: locationForm.value.shelf,
      position: locationForm.value.position
    })
    showLocationModal.value = false
  } catch (error) {
    console.error('Location update failed:', error)
  }
}

const editNotes = (item: WarehouseInventory) => {
  const newNotes = prompt('메모를 입력하세요:', item.notes || '')
  if (newNotes !== null) {
    // Update notes API call would go here
    console.log('Update notes for:', item.orderNumber, newNotes)
  }
}

const generateLabel = (item: WarehouseInventory) => {
  // Generate label logic
  console.log('Generate label for:', item.orderNumber)
}

const duplicateItem = (item: WarehouseInventory) => {
  // Duplicate item logic
  console.log('Duplicate item:', item.orderNumber)
}

const printInfo = (item: WarehouseInventory) => {
  // Print item info
  console.log('Print info for:', item.orderNumber)
}

const reportDamage = async (item: WarehouseInventory) => {
  const damageReport = prompt('손상 내용을 입력하세요:')
  if (damageReport) {
    try {
      await warehouseStore.reportDamage(item.id, damageReport)
    } catch (error) {
      console.error('Damage report failed:', error)
    }
  }
}

// Bulk Actions
const bulkUpdateStatus = async (status: InventoryStatus) => {
  // Implement bulk status update
  console.log('Bulk update status:', status, selectedItems.value)
}

const bulkUpdateLocation = () => {
  // Implement bulk location update
  console.log('Bulk update location:', selectedItems.value)
}

const generateBulkLabels = () => {
  // Implement bulk label generation
  console.log('Generate bulk labels:', selectedItems.value)
}

const exportInventory = () => {
  // Export inventory to CSV/Excel
  console.log('Export inventory')
}

// Utility Functions
const canInspect = (status: string) => ['RECEIVED'].includes(status)
const canShip = (status: string) => ['READY_FOR_SHIPPING'].includes(status)
const canHold = (status: string) => !['SHIPPED', 'ON_HOLD'].includes(status)

const getStatusClass = (status: string) => {
  return status.toLowerCase().replace(/_/g, '-')
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING_RECEIPT': '입고 대기',
    'RECEIVED': '입고 완료',
    'INSPECTING': '검품 중',
    'READY_FOR_SHIPPING': '출고 준비',
    'ON_HOLD': '보류',
    'SHIPPED': '출고 완료',
    'DAMAGED': '손상',
    'MISSING': '분실'
  }
  return statusMap[status] || status
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('ko-KR')
}

// Watch for page changes
watch(filteredInventory, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = Math.max(1, totalPages.value)
  }
})

// Close dropdown when clicking outside
const handleClickOutside = () => {
  activeDropdown.value = null
}

// Lifecycle
onMounted(async () => {
  await refreshInventory()
  document.addEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.warehouse-inventory-page {
  padding: 1rem;
  max-width: 1400px;
  margin: 0 auto;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.header-title h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
}

.subtitle {
  color: var(--text-secondary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.export-btn,
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.export-btn:hover,
.refresh-btn:hover:not(:disabled) {
  background: var(--bg-secondary);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

/* Statistics */
.inventory-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  font-size: 1.2rem;
  color: white;
}

.stat-icon.total { background: var(--primary-color); }
.stat-icon.pending { background: var(--warning-color); }
.stat-icon.ready { background: var(--success-color); }
.stat-icon.issues { background: var(--danger-color); }

.stat-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
}

.stat-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

/* Controls */
.inventory-controls {
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.filter-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.filter-group select,
.search-input {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.search-group {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

.search-input {
  flex: 1;
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  background: var(--danger-light);
  color: var(--danger-dark);
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.8rem;
}

/* Bulk Actions */
.bulk-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.selected-count {
  font-weight: 500;
  color: var(--primary-color);
}

.select-all-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  text-decoration: underline;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.bulk-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
}

.bulk-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bulk-btn.ready { background: var(--success-color); color: white; }
.bulk-btn.hold { background: var(--warning-color); color: white; }
.bulk-btn.location { background: var(--info-color); color: white; }
.bulk-btn.labels { background: var(--primary-color); color: white; }

/* Table */
.inventory-table-container {
  background: white;
  border-radius: 0.75rem;
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
}

.inventory-table th {
  background: var(--bg-secondary);
  padding: 1rem 0.75rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.inventory-table th.sortable {
  cursor: pointer;
  user-select: none;
}

.inventory-table th.sortable:hover {
  background: var(--bg-hover);
}

.inventory-table td {
  padding: 1rem 0.75rem;
  border-bottom: 1px solid var(--border-light);
  font-size: 0.9rem;
}

.inventory-row:hover {
  background: var(--bg-light);
}

.table-checkbox {
  width: 16px;
  height: 16px;
}

.order-info strong {
  display: block;
  margin-bottom: 0.25rem;
}

.order-info small {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.label-code {
  font-family: 'Courier New', monospace;
  background: var(--bg-secondary);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.pending-receipt { background: var(--warning-light); color: var(--warning-dark); }
.status-badge.received { background: var(--info-light); color: var(--info-dark); }
.status-badge.inspecting { background: var(--primary-light); color: var(--primary-dark); }
.status-badge.ready-for-shipping { background: var(--success-light); color: var(--success-dark); }
.status-badge.on-hold { background: var(--danger-light); color: var(--danger-dark); }
.status-badge.shipped { background: var(--secondary-light); color: var(--secondary-dark); }
.status-badge.damaged { background: var(--danger-light); color: var(--danger-dark); }

.location-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.location-badge {
  background: var(--info-light);
  color: var(--info-dark);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.8rem;
  font-weight: 500;
}

.location-edit-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
}

.location-edit-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.date-info .date {
  display: block;
  margin-bottom: 0.25rem;
}

.date-info small {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.package-info {
  font-size: 0.8rem;
}

.package-info .package-detail {
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.notes-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.notes-text {
  flex: 1;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.notes-edit-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
}

.notes-edit-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
  position: relative;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.detail { background: var(--secondary-light); color: var(--secondary-dark); }
.action-btn.inspect { background: var(--primary-light); color: var(--primary-dark); }
.action-btn.ship { background: var(--success-light); color: var(--success-dark); }
.action-btn.hold { background: var(--warning-light); color: var(--warning-dark); }
.action-btn.more { background: var(--bg-secondary); color: var(--text-secondary); }

.action-btn:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  min-width: 150px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
}

.dropdown-item:hover {
  background: var(--bg-secondary);
}

.dropdown-item.danger {
  color: var(--danger-color);
}

/* Empty/Loading States */
.empty-state,
.loading-state {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--text-secondary);
}

.empty-state i,
.loading-state .loading-spinner {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 2rem;
  padding: 1rem;
}

.page-btn,
.page-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-color);
  background: white;
  cursor: pointer;
  border-radius: 0.25rem;
  transition: all 0.2s;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:hover:not(:disabled),
.page-number:hover {
  background: var(--bg-secondary);
}

.page-number.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.page-info {
  margin-left: 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 0.75rem;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.25rem;
}

.close-btn:hover {
  background: var(--bg-secondary);
}

.modal-body {
  padding: 1rem;
}

.location-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  font-size: 0.9rem;
}

.form-group select,
.form-group input {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-weight: 500;
}

.btn.primary {
  background: var(--primary-color);
  color: white;
}

.btn.secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* Responsive */
@media (max-width: 1200px) {
  .inventory-table {
    font-size: 0.8rem;
  }
  
  .inventory-table th,
  .inventory-table td {
    padding: 0.75rem 0.5rem;
  }
}

@media (max-width: 768px) {
  .warehouse-inventory-page {
    padding: 0.5rem;
  }

  .page-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .inventory-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-section {
    grid-template-columns: 1fr;
  }

  .bulk-actions {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .inventory-table-container {
    overflow-x: auto;
  }

  .inventory-table {
    min-width: 800px;
  }

  .pagination {
    flex-wrap: wrap;
    gap: 0.25rem;
  }

  .page-info {
    width: 100%;
    text-align: center;
    margin: 0.5rem 0 0 0;
  }
}
</style>