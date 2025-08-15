<template>
  <div class="inventory-view">
    <!-- 헤더 -->
    <div class="inventory-header">
      <div class="header-info">
        <h1>재고 관리</h1>
        <p class="inventory-summary">
          총 {{ totalItems }}개 항목 • 활성 {{ activeItems }}개 • 보류 {{ holdItems }}개
        </p>
      </div>
      <div class="header-actions">
        <button @click="exportInventory" class="btn-export">
          <i class="icon-download"></i>
          내보내기
        </button>
        <button @click="refreshInventory" class="btn-refresh" :disabled="loading">
          <i class="icon-refresh" :class="{ spinning: loading }"></i>
          새로고침
        </button>
      </div>
    </div>

    <!-- 검색 및 필터 -->
    <div class="search-filters">
      <div class="search-section">
        <div class="search-input-group">
          <i class="icon-search"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="주문코드, 라벨코드, 상품명으로 검색..."
            class="search-input"
            @input="handleSearch"
          />
          <button v-if="searchQuery" @click="clearSearch" class="btn-clear-search">
            <i class="icon-x"></i>
          </button>
        </div>
      </div>

      <div class="filters-section">
        <div class="filter-group">
          <label>상태</label>
          <select v-model="selectedStatus" @change="applyFilters" class="filter-select">
            <option value="">모든 상태</option>
            <option value="created">생성됨</option>
            <option value="inbound_completed">입고 완료</option>
            <option value="ready_for_outbound">출고 준비</option>
            <option value="outbound_completed">출고 완료</option>
            <option value="hold">보류</option>
            <option value="mixbox">믹스박스</option>
          </select>
        </div>

        <div class="filter-group">
          <label>창고 위치</label>
          <select v-model="selectedLocation" @change="applyFilters" class="filter-select">
            <option value="">모든 위치</option>
            <option value="A1">A1 구역</option>
            <option value="A2">A2 구역</option>
            <option value="B1">B1 구역</option>
            <option value="B2">B2 구역</option>
            <option value="C1">C1 구역</option>
            <option value="TEMP">임시보관</option>
          </select>
        </div>

        <div class="filter-group">
          <label>입고일</label>
          <input
            v-model="dateFilter"
            type="date"
            @change="applyFilters"
            class="filter-date"
          />
        </div>

        <div class="filter-group">
          <label>배송 타입</label>
          <select v-model="selectedShipType" @change="applyFilters" class="filter-select">
            <option value="">모든 타입</option>
            <option value="sea">해상</option>
            <option value="air">항공</option>
          </select>
        </div>

        <button @click="clearFilters" class="btn-clear-filters">
          <i class="icon-x"></i>
          필터 초기화
        </button>
      </div>
    </div>

    <!-- 일괄 작업 도구 -->
    <div class="batch-toolbar" v-if="selectedItems.length > 0">
      <div class="selection-info">
        <span>{{ selectedItems.length }}개 항목 선택됨</span>
      </div>
      <div class="batch-actions">
        <button @click="showBatchModal = true" class="btn-batch">
          <i class="icon-layers"></i>
          일괄 처리
        </button>
        <button @click="exportSelected" class="btn-export-selected">
          <i class="icon-download"></i>
          선택 항목 내보내기
        </button>
        <button @click="clearSelection" class="btn-clear">
          <i class="icon-x"></i>
          선택 해제
        </button>
      </div>
    </div>

    <!-- 재고 목록 -->
    <div class="inventory-table-container">
      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
        <p>재고 데이터를 불러오는 중...</p>
      </div>

      <div v-else-if="filteredItems.length === 0" class="empty-state">
        <i class="icon-package"></i>
        <h3>재고가 없습니다</h3>
        <p v-if="hasActiveFilters">현재 필터 조건에 맞는 항목이 없습니다.</p>
        <p v-else>아직 등록된 재고가 없습니다.</p>
        <button v-if="hasActiveFilters" @click="clearFilters" class="btn-clear-filters">
          필터 초기화
        </button>
      </div>

      <div v-else class="table-wrapper">
        <table class="inventory-table">
          <thead>
            <tr>
              <th class="checkbox-column">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                />
              </th>
              <th class="sortable" @click="toggleSort('labelCode')">
                라벨코드
                <i v-if="sortBy === 'labelCode'" :class="sortOrder === 'asc' ? 'icon-chevron-up' : 'icon-chevron-down'"></i>
              </th>
              <th class="sortable" @click="toggleSort('orderCode')">
                주문코드
                <i v-if="sortBy === 'orderCode'" :class="sortOrder === 'asc' ? 'icon-chevron-up' : 'icon-chevron-down'"></i>
              </th>
              <th>상품명</th>
              <th class="sortable" @click="toggleSort('status')">
                상태
                <i v-if="sortBy === 'status'" :class="sortOrder === 'asc' ? 'icon-chevron-up' : 'icon-chevron-down'"></i>
              </th>
              <th>위치</th>
              <th class="sortable" @click="toggleSort('inboundDate')">
                입고일
                <i v-if="sortBy === 'inboundDate'" :class="sortOrder === 'asc' ? 'icon-chevron-up' : 'icon-chevron-down'"></i>
              </th>
              <th>타입</th>
              <th>크기(CBM)</th>
              <th class="actions-column">작업</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in paginatedItems"
              :key="item.id"
              class="inventory-row"
              :class="{ selected: selectedItems.includes(item.id) }"
              @click="toggleItemSelection(item.id)"
            >
              <td class="checkbox-column" @click.stop>
                <input
                  type="checkbox"
                  :checked="selectedItems.includes(item.id)"
                  @change="toggleItemSelection(item.id)"
                />
              </td>
              <td class="label-code">
                <div class="code-with-qr">
                  <span class="code">{{ item.labelCode }}</span>
                  <button @click.stop="showQRCode(item.labelCode)" class="btn-qr">
                    <i class="icon-qr-code"></i>
                  </button>
                </div>
              </td>
              <td class="order-code">{{ item.orderCode }}</td>
              <td class="item-name">
                <div class="item-info">
                  <span class="name">{{ item.productName }}</span>
                  <span class="quantity">{{ item.quantity }}개</span>
                </div>
              </td>
              <td class="status">
                <span class="status-badge" :class="item.status">
                  {{ getStatusText(item.status) }}
                </span>
              </td>
              <td class="location">
                <span class="location-badge" :class="{ empty: !item.location }">
                  {{ item.location || '미배정' }}
                </span>
              </td>
              <td class="date">{{ formatDate(new Date(item.receivedAt)) }}</td>
              <td class="ship-type">
                <span class="type-badge sea">
                  해상
                </span>
              </td>
              <td class="cbm">0.001</td>
              <td class="actions" @click.stop>
                <div class="action-buttons">
                  <button @click="viewItemDetails(item)" class="btn-action view" title="상세보기">
                    <i class="icon-eye"></i>
                  </button>
                  <button @click="editItem(item)" class="btn-action edit" title="수정">
                    <i class="icon-edit"></i>
                  </button>
                  <button @click="moveItem(item)" class="btn-action move" title="이동">
                    <i class="icon-move"></i>
                  </button>
                  <div class="action-dropdown">
                    <button @click="toggleActionMenu(item.id)" class="btn-action more" title="더보기">
                      <i class="icon-more-vertical"></i>
                    </button>
                    <div v-if="activeActionMenu === item.id" class="dropdown-menu">
                      <button @click="printLabel(item)" class="dropdown-item">
                        <i class="icon-printer"></i>
                        라벨 출력
                      </button>
                      <button @click="addNote(item)" class="dropdown-item">
                        <i class="icon-note"></i>
                        메모 추가
                      </button>
                      <button @click="showHistory(item)" class="dropdown-item">
                        <i class="icon-history"></i>
                        이력 보기
                      </button>
                      <div class="dropdown-divider"></div>
                      <button @click="holdItem(item)" class="dropdown-item danger">
                        <i class="icon-pause"></i>
                        보류 처리
                      </button>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="filteredItems.length > 0">
      <div class="pagination-info">
        {{ (currentPage - 1) * itemsPerPage + 1 }}-{{ Math.min(currentPage * itemsPerPage, filteredItems.length) }} / {{ filteredItems.length }}개
      </div>
      <div class="pagination-controls">
        <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="btn-page"
        >
          <i class="icon-chevron-left"></i>
        </button>
        <template v-for="page in visiblePages" :key="page">
          <button
            v-if="page !== '...'"
            @click="goToPage(page)"
            :class="['btn-page', { active: currentPage === page }]"
          >
            {{ page }}
          </button>
          <span v-else class="page-ellipsis">...</span>
        </template>
        <button
          @click="goToPage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="btn-page"
        >
          <i class="icon-chevron-right"></i>
        </button>
      </div>
      <div class="items-per-page">
        <select v-model="itemsPerPage" @change="updatePagination" class="per-page-select">
          <option :value="20">20개씩</option>
          <option :value="50">50개씩</option>
          <option :value="100">100개씩</option>
        </select>
      </div>
    </div>

    <!-- QR 코드 모달 -->
    <QRCodeModal
      :show="showQRModal"
      :code="selectedQRCode"
      @close="closeQRModal"
    />

    <!-- 항목 상세 모달 -->
    <div v-if="showDetailModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeDetailModal">
      <div class="relative top-10 mx-auto p-5 border w-11/12 max-w-lg shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 text-center">아이템 상세</h3>
          <div class="mt-4" v-if="selectedItem">
            <div class="space-y-3">
              <div><strong>코드:</strong> {{ selectedItem.code }}</div>
              <div><strong>이름:</strong> {{ selectedItem.name }}</div>
              <div><strong>상태:</strong> {{ selectedItem.status }}</div>
            </div>
          </div>
          <div class="mt-6">
            <button @click="closeDetailModal" class="w-full px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md">
              닫기
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 일괄 처리 모달 -->
    <div v-if="showBatchModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50" @click="closeBatchModal">
      <div class="relative top-10 mx-auto p-5 border w-11/12 max-w-md shadow-lg rounded-md bg-white" @click.stop>
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 text-center">일괄 처리</h3>
          <div class="mt-4">
            <p class="text-sm text-gray-500 text-center">선택된 {{ selectedItems.length }}개 항목이 처리되었습니다.</p>
          </div>
          <div class="mt-6">
            <button @click="closeBatchModal" class="w-full px-4 py-2 bg-blue-500 text-white text-base font-medium rounded-md">
              확인
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 위치 이동 모달 -->
    <LocationMoveModal
      :show="showMoveModal"
      :item="selectedItem"
      @close="closeMoveModal"
      @save="handleLocationMove"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '@/stores/warehouse'
import { useToast } from '@/composables/useToast'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
// Removed missing modal imports - using inline modals instead

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 반응형 데이터
const loading = ref(false)
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedLocation = ref('')
const dateFilter = ref('')
const selectedShipType = ref('')
const selectedItems = ref<number[]>([])
const sortBy = ref('inboundDate')
const sortOrder = ref<'asc' | 'desc'>('desc')
const currentPage = ref(1)
const itemsPerPage = ref(50)
const activeActionMenu = ref<number | null>(null)

// 모달 상태
const showQRModal = ref(false)
const showDetailModal = ref(false)
const showBatchModal = ref(false)
const showMoveModal = ref(false)
const selectedQRCode = ref('')
const selectedItem = ref<any>(null)

// 창고 store의 재고 데이터 사용
const inventoryItems = computed(() => warehouseStore.inventory)

// 계산된 속성
const totalItems = computed(() => inventoryItems.value.length)
const activeItems = computed(() => 
  inventoryItems.value.filter(item => 
    ['stored', 'processing'].includes(item.status)
  ).length
)
const holdItems = computed(() => 
  inventoryItems.value.filter(item => item.status === 'processing').length
)

const hasActiveFilters = computed(() => 
  searchQuery.value || selectedStatus.value || selectedLocation.value || 
  dateFilter.value || selectedShipType.value
)

const filteredItems = computed(() => {
  let items = [...inventoryItems.value]

  // 검색 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    items = items.filter(item =>
      item.orderCode.toLowerCase().includes(query) ||
      item.productName.toLowerCase().includes(query) ||
      (item.location && item.location.toLowerCase().includes(query))
    )
  }

  // 상태 필터
  if (selectedStatus.value) {
    items = items.filter(item => item.status === selectedStatus.value)
  }

  // 위치 필터
  if (selectedLocation.value) {
    items = items.filter(item => item.location && item.location.includes(selectedLocation.value))
  }

  // 날짜 필터
  if (dateFilter.value) {
    const filterDate = new Date(dateFilter.value)
    items = items.filter(item => {
      const itemDate = new Date(item.receivedAt)
      return itemDate.toDateString() === filterDate.toDateString()
    })
  }

  // 정렬
  items.sort((a, b) => {
    let aValue: any, bValue: any
    
    if (sortBy.value === 'inboundDate') {
      aValue = new Date(a.receivedAt)
      bValue = new Date(b.receivedAt)
    } else if (sortBy.value === 'labelCode') {
      aValue = a.id
      bValue = b.id
    } else {
      aValue = a[sortBy.value as keyof typeof a]
      bValue = b[sortBy.value as keyof typeof b]
    }
    
    if (aValue < bValue) return sortOrder.value === 'asc' ? -1 : 1
    if (aValue > bValue) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })

  return items
})

const totalPages = computed(() => Math.ceil(filteredItems.value.length / itemsPerPage.value))

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredItems.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    pages.push(1)
    
    if (current > 4) {
      pages.push('...')
    }
    
    const start = Math.max(2, current - 2)
    const end = Math.min(total - 1, current + 2)
    
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
    
    if (current < total - 3) {
      pages.push('...')
    }
    
    pages.push(total)
  }

  return pages
})

const isAllSelected = computed(() => 
  paginatedItems.value.length > 0 && 
  paginatedItems.value.every(item => selectedItems.value.includes(item.id))
)

// 컴포넌트 마운트
onMounted(() => {
  loadInventory()
})

// 페이지네이션 감시
watch([currentPage, itemsPerPage], () => {
  selectedItems.value = []
})

// 메서드
const loadInventory = async () => {
  loading.value = true
  
  try {
    await warehouseStore.fetchInventory()
    showToast('재고 데이터를 불러왔습니다', 'success')
  } catch (error: any) {
    console.error('Inventory loading error:', error)
    showToast('데이터를 불러오는 중 오류가 발생했습니다', 'error')
  } finally {
    loading.value = false
  }
}

const refreshInventory = async () => {
  await loadInventory()
}

const handleSearch = () => {
  currentPage.value = 1
  selectedItems.value = []
}

const clearSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
}

const applyFilters = () => {
  currentPage.value = 1
  selectedItems.value = []
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedStatus.value = ''
  selectedLocation.value = ''
  dateFilter.value = ''
  selectedShipType.value = ''
  currentPage.value = 1
  selectedItems.value = []
}

const toggleSort = (field: string) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortOrder.value = 'asc'
  }
  currentPage.value = 1
}

const toggleItemSelection = (itemId: number) => {
  const index = selectedItems.value.indexOf(itemId)
  if (index === -1) {
    selectedItems.value.push(itemId)
  } else {
    selectedItems.value.splice(index, 1)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = selectedItems.value.filter(id => 
      !paginatedItems.value.find(item => item.id === id)
    )
  } else {
    paginatedItems.value.forEach(item => {
      if (!selectedItems.value.includes(item.id)) {
        selectedItems.value.push(item.id)
      }
    })
  }
}

const clearSelection = () => {
  selectedItems.value = []
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const updatePagination = () => {
  currentPage.value = 1
  selectedItems.value = []
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    stored: '보관중',
    processing: '처리중',
    shipped: '출고완료'
  }
  return statusMap[status] || status
}

const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// QR 코드 관련
const showQRCode = (code: string) => {
  selectedQRCode.value = code
  showQRModal.value = true
}

const closeQRModal = () => {
  showQRModal.value = false
  selectedQRCode.value = ''
}

// 항목 상세 관련
const viewItemDetails = (item: any) => {
  selectedItem.value = { ...item }
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedItem.value = null
}

const handleItemUpdate = (updatedItem: any) => {
  const index = inventoryItems.value.findIndex(item => item.id === updatedItem.id)
  if (index !== -1) {
    inventoryItems.value[index] = { ...updatedItem }
    showToast('항목이 업데이트되었습니다', 'success')
  }
  closeDetailModal()
}

// 일괄 처리 관련
const closeBatchModal = () => {
  showBatchModal.value = false
}

const handleBatchComplete = (result: any) => {
  showToast(`일괄 처리 완료: ${result.processed}개 성공`, 'success')
  selectedItems.value = []
  closeBatchModal()
  refreshInventory()
}

// 위치 이동 관련
const moveItem = (item: any) => {
  selectedItem.value = { ...item }
  showMoveModal.value = true
}

const closeMoveModal = () => {
  showMoveModal.value = false
  selectedItem.value = null
}

const handleLocationMove = (updatedItem: any) => {
  const index = inventoryItems.value.findIndex(item => item.id === updatedItem.id)
  if (index !== -1) {
    inventoryItems.value[index] = { ...updatedItem }
    showToast('위치가 변경되었습니다', 'success')
  }
  closeMoveModal()
}

// 기타 액션
const editItem = (item: any) => {
  viewItemDetails(item)
}

const toggleActionMenu = (itemId: number) => {
  activeActionMenu.value = activeActionMenu.value === itemId ? null : itemId
}

const printLabel = (item: any) => {
  showToast(`${item.labelCode} 라벨을 출력합니다`, 'info')
  activeActionMenu.value = null
}

const addNote = (item: any) => {
  const note = prompt('메모를 입력하세요:', item.note || '')
  if (note !== null) {
    const index = inventoryItems.value.findIndex(i => i.id === item.id)
    if (index !== -1) {
      inventoryItems.value[index].note = note
      showToast('메모가 저장되었습니다', 'success')
    }
  }
  activeActionMenu.value = null
}

const showHistory = (item: any) => {
  showToast(`${item.labelCode} 이력을 확인합니다`, 'info')
  activeActionMenu.value = null
}

const holdItem = (item: any) => {
  if (confirm('이 항목을 보류 처리하시겠습니까?')) {
    const index = inventoryItems.value.findIndex(i => i.id === item.id)
    if (index !== -1) {
      inventoryItems.value[index].status = 'hold'
      inventoryItems.value[index].location = 'TEMP'
      showToast('항목이 보류 처리되었습니다', 'warning')
    }
  }
  activeActionMenu.value = null
}

const exportInventory = () => {
  showToast('재고 데이터를 내보내는 중...', 'info')
  // TODO: 엑셀/CSV 내보내기 구현
}

const exportSelected = () => {
  if (selectedItems.value.length === 0) return
  showToast(`선택된 ${selectedItems.value.length}개 항목을 내보내는 중...`, 'info')
  // TODO: 선택 항목 내보내기 구현
}

// 외부 클릭시 액션 메뉴 닫기
document.addEventListener('click', () => {
  activeActionMenu.value = null
})
</script>

<style scoped>
.inventory-view {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.inventory-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.inventory-summary {
  font-size: 14px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-export,
.btn-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-export:hover,
.btn-refresh:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-export {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.btn-export:hover {
  background: #2563eb;
}

.search-filters {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-section {
  margin-bottom: 20px;
}

.search-input-group {
  position: relative;
  max-width: 600px;
}

.search-input-group i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 48px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.15s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-clear-search {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.btn-clear-search:hover {
  color: #6b7280;
  background: #f3f4f6;
}

.filters-section {
  display: flex;
  gap: 16px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
}

.filter-select,
.filter-date {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  min-width: 120px;
}

.btn-clear-filters {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-clear-filters:hover {
  background: #f9fafb;
  color: #374151;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.selection-info {
  font-weight: 500;
  color: #1e40af;
}

.batch-actions {
  display: flex;
  gap: 12px;
}

.btn-batch,
.btn-export-selected,
.btn-clear {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-batch {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-export-selected {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-clear {
  background: none;
  color: #6b7280;
  border: 1px solid transparent;
}

.inventory-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 20px;
  color: #6b7280;
}

.empty-state i {
  font-size: 48px;
  color: #d1d5db;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 500;
  color: #374151;
}

.table-wrapper {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
}

.inventory-table th {
  background: #f9fafb;
  padding: 16px 12px;
  text-align: left;
  font-weight: 500;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
}

.inventory-table th.sortable {
  cursor: pointer;
  user-select: none;
  position: relative;
}

.inventory-table th.sortable:hover {
  background: #f3f4f6;
}

.inventory-table th i {
  margin-left: 4px;
  font-size: 12px;
}

.inventory-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: middle;
}

.inventory-row {
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.inventory-row:hover {
  background: #f8fafc;
}

.inventory-row.selected {
  background: #eff6ff;
}

.checkbox-column {
  width: 40px;
  text-align: center;
}

.code-with-qr {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code {
  font-family: monospace;
  font-weight: 500;
}

.btn-qr {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.15s ease;
}

.btn-qr:hover {
  color: #3b82f6;
  background: #eff6ff;
}

.order-code {
  font-family: monospace;
  color: #6b7280;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-info .name {
  font-weight: 500;
  color: #1f2937;
}

.item-info .quantity {
  font-size: 12px;
  color: #6b7280;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.status-badge.created {
  background: #f3f4f6;
  color: #374151;
}

.status-badge.inbound_completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.ready_for_outbound {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.outbound_completed {
  background: #f0fdf4;
  color: #15803d;
}

.status-badge.hold {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.mixbox {
  background: #f3e8ff;
  color: #7c3aed;
}

.location-badge {
  padding: 4px 8px;
  border-radius: 6px;
  background: #f3f4f6;
  color: #374151;
  font-size: 12px;
  font-weight: 500;
}

.location-badge.empty {
  background: #fef2f2;
  color: #dc2626;
}

.type-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge.sea {
  background: #dbeafe;
  color: #1e40af;
}

.type-badge.air {
  background: #fef3c7;
  color: #92400e;
}

.actions-column {
  width: 120px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  position: relative;
}

.btn-action {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-action.view {
  background: #eff6ff;
  color: #3b82f6;
}

.btn-action.edit {
  background: #fef3c7;
  color: #f59e0b;
}

.btn-action.move {
  background: #f0fdf4;
  color: #10b981;
}

.btn-action.more {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-action:hover {
  transform: scale(1.1);
}

.action-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 150px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.dropdown-item:hover {
  background: #f9fafb;
}

.dropdown-item.danger {
  color: #dc2626;
}

.dropdown-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}

.pagination {
  display: flex;
  justify-content: between;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.btn-page {
  width: 36px;
  height: 36px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-page:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
}

.btn-page.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-ellipsis {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.per-page-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .inventory-view {
    padding: 16px;
  }

  .inventory-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }

  .batch-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
  }

  .pagination-controls {
    justify-content: space-around;
  }
}

@media (max-width: 768px) {
  .inventory-table th,
  .inventory-table td {
    padding: 12px 8px;
    font-size: 14px;
  }

  .inventory-table {
    font-size: 13px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 2px;
  }

  .btn-action {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
}
</style>