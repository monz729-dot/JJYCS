<template>
  <div class="photos-view">
    <!-- 헤더 -->
    <div class="photos-header">
      <div class="header-info">
        <h1>사진 관리</h1>
        <p class="photos-description">리패킹 및 상품 검수 사진 관리</p>
      </div>
      <div class="header-actions">
        <button @click="showUploadModal = true" class="btn-upload">
          <i class="icon-upload"></i>
          사진 업로드
        </button>
        <button @click="capturePhoto" class="btn-capture">
          <i class="icon-camera"></i>
          사진 촬영
        </button>
      </div>
    </div>

    <!-- 빠른 통계 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon photos">
          <i class="icon-image"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalPhotos }}</div>
          <div class="stat-label">총 사진</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon pending">
          <i class="icon-clock"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingPhotos }}</div>
          <div class="stat-label">검토 대기</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon today">
          <i class="icon-calendar"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ todayPhotos }}</div>
          <div class="stat-label">오늘 업로드</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon storage">
          <i class="icon-hard-drive"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ storageUsed }}MB</div>
          <div class="stat-label">저장공간 사용</div>
        </div>
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
            placeholder="주문코드, 라벨코드로 검색..."
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
          <label>카테고리</label>
          <select v-model="selectedCategory" @change="applyFilters" class="filter-select">
            <option value="">모든 카테고리</option>
            <option value="repacking">리패킹</option>
            <option value="inspection">검수</option>
            <option value="damage">손상</option>
            <option value="before">작업 전</option>
            <option value="after">작업 후</option>
          </select>
        </div>

        <div class="filter-group">
          <label>상태</label>
          <select v-model="selectedStatus" @change="applyFilters" class="filter-select">
            <option value="">모든 상태</option>
            <option value="uploaded">업로드됨</option>
            <option value="reviewing">검토 중</option>
            <option value="approved">승인됨</option>
            <option value="rejected">거부됨</option>
          </select>
        </div>

        <div class="filter-group">
          <label>업로드 날짜</label>
          <input
            v-model="dateFilter"
            type="date"
            @change="applyFilters"
            class="filter-date"
          />
        </div>

        <div class="filter-group">
          <label>정렬</label>
          <select v-model="sortBy" @change="applySorting" class="filter-select">
            <option value="newest">최신순</option>
            <option value="oldest">오래된순</option>
            <option value="size">크기순</option>
            <option value="category">카테고리순</option>
          </select>
        </div>

        <button @click="clearFilters" class="btn-clear-filters">
          <i class="icon-x"></i>
          필터 초기화
        </button>
      </div>
    </div>

    <!-- 보기 모드 전환 -->
    <div class="view-controls">
      <div class="view-mode-toggle">
        <button
          class="view-mode-btn"
          :class="{ active: viewMode === 'grid' }"
          @click="viewMode = 'grid'"
        >
          <i class="icon-grid"></i>
          그리드
        </button>
        <button
          class="view-mode-btn"
          :class="{ active: viewMode === 'list' }"
          @click="viewMode = 'list'"
        >
          <i class="icon-list"></i>
          목록
        </button>
      </div>

      <div class="selection-actions" v-if="selectedPhotos.length > 0">
        <span class="selection-count">{{ selectedPhotos.length }}개 선택됨</span>
        <button @click="bulkApprove" class="btn-bulk approve">
          <i class="icon-check"></i>
          일괄 승인
        </button>
        <button @click="bulkReject" class="btn-bulk reject">
          <i class="icon-x"></i>
          일괄 거부
        </button>
        <button @click="bulkDelete" class="btn-bulk delete">
          <i class="icon-trash"></i>
          일괄 삭제
        </button>
        <button @click="clearSelection" class="btn-clear-selection">
          선택 해제
        </button>
      </div>
    </div>

    <!-- 사진 목록 - 그리드 모드 -->
    <div v-if="viewMode === 'grid'" class="photos-grid">
      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
        <p>사진을 불러오는 중...</p>
      </div>

      <div v-else-if="filteredPhotos.length === 0" class="empty-state">
        <i class="icon-image"></i>
        <h3>사진이 없습니다</h3>
        <p v-if="hasActiveFilters">현재 필터 조건에 맞는 사진이 없습니다.</p>
        <p v-else>아직 업로드된 사진이 없습니다.</p>
        <button v-if="hasActiveFilters" @click="clearFilters" class="btn-clear-filters">
          필터 초기화
        </button>
      </div>

      <div v-else class="grid-container">
        <div
          v-for="photo in paginatedPhotos"
          :key="photo.id"
          class="photo-card"
          :class="{ selected: selectedPhotos.includes(photo.id) }"
        >
          <!-- 선택 체크박스 -->
          <div class="photo-selection">
            <input
              type="checkbox"
              :checked="selectedPhotos.includes(photo.id)"
              @change="togglePhotoSelection(photo.id)"
              class="photo-checkbox"
            />
          </div>

          <!-- 사진 이미지 -->
          <div class="photo-image" @click="openPhotoModal(photo)">
            <img
              :src="photo.thumbnailUrl"
              :alt="photo.filename"
              @load="handleImageLoad"
              @error="handleImageError"
              loading="lazy"
            />
            <div class="photo-overlay">
              <button class="overlay-btn view">
                <i class="icon-eye"></i>
              </button>
              <button class="overlay-btn download" @click.stop="downloadPhoto(photo)">
                <i class="icon-download"></i>
              </button>
            </div>
          </div>

          <!-- 사진 정보 -->
          <div class="photo-info">
            <div class="info-header">
              <h4 class="photo-title">{{ photo.title || photo.filename }}</h4>
              <div class="photo-status" :class="photo.status">
                <i :class="getStatusIcon(photo.status)"></i>
                {{ getStatusText(photo.status) }}
              </div>
            </div>

            <div class="info-details">
              <div class="detail-row">
                <span class="label">주문:</span>
                <span class="value">{{ photo.orderCode }}</span>
              </div>
              <div class="detail-row">
                <span class="label">카테고리:</span>
                <span class="value category" :class="photo.category">
                  {{ getCategoryText(photo.category) }}
                </span>
              </div>
              <div class="detail-row">
                <span class="label">크기:</span>
                <span class="value">{{ formatFileSize(photo.fileSize) }}</span>
              </div>
              <div class="detail-row">
                <span class="label">업로드:</span>
                <span class="value">{{ formatDate(photo.uploadedAt) }}</span>
              </div>
            </div>

            <div class="photo-actions">
              <button @click="viewPhoto(photo)" class="action-btn view" title="보기">
                <i class="icon-eye"></i>
              </button>
              <button @click="editPhoto(photo)" class="action-btn edit" title="편집">
                <i class="icon-edit"></i>
              </button>
              <button @click="approvePhoto(photo)" class="action-btn approve" title="승인">
                <i class="icon-check"></i>
              </button>
              <button @click="rejectPhoto(photo)" class="action-btn reject" title="거부">
                <i class="icon-x"></i>
              </button>
              <div class="dropdown-menu">
                <button @click="togglePhotoMenu(photo.id)" class="action-btn more" title="더보기">
                  <i class="icon-more-vertical"></i>
                </button>
                <div v-if="activePhotoMenu === photo.id" class="dropdown-content">
                  <button @click="duplicatePhoto(photo)" class="dropdown-item">
                    <i class="icon-copy"></i>
                    복제
                  </button>
                  <button @click="addNote(photo)" class="dropdown-item">
                    <i class="icon-note"></i>
                    메모 추가
                  </button>
                  <button @click="sharePhoto(photo)" class="dropdown-item">
                    <i class="icon-share"></i>
                    공유
                  </button>
                  <div class="dropdown-divider"></div>
                  <button @click="deletePhoto(photo)" class="dropdown-item danger">
                    <i class="icon-trash"></i>
                    삭제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 사진 목록 - 리스트 모드 -->
    <div v-if="viewMode === 'list'" class="photos-list">
      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
        <p>사진을 불러오는 중...</p>
      </div>

      <div v-else-if="filteredPhotos.length === 0" class="empty-state">
        <i class="icon-image"></i>
        <h3>사진이 없습니다</h3>
        <p v-if="hasActiveFilters">현재 필터 조건에 맞는 사진이 없습니다.</p>
        <p v-else>아직 업로드된 사진이 없습니다.</p>
      </div>

      <div v-else class="list-container">
        <div class="list-header">
          <div class="header-cell checkbox">
            <input
              type="checkbox"
              :checked="isAllSelected"
              @change="toggleSelectAll"
            />
          </div>
          <div class="header-cell image">이미지</div>
          <div class="header-cell info">정보</div>
          <div class="header-cell category">카테고리</div>
          <div class="header-cell status">상태</div>
          <div class="header-cell date">업로드일</div>
          <div class="header-cell actions">작업</div>
        </div>

        <div
          v-for="photo in paginatedPhotos"
          :key="photo.id"
          class="list-item"
          :class="{ selected: selectedPhotos.includes(photo.id) }"
        >
          <div class="list-cell checkbox">
            <input
              type="checkbox"
              :checked="selectedPhotos.includes(photo.id)"
              @change="togglePhotoSelection(photo.id)"
            />
          </div>
          <div class="list-cell image">
            <div class="thumbnail" @click="openPhotoModal(photo)">
              <img :src="photo.thumbnailUrl" :alt="photo.filename" />
            </div>
          </div>
          <div class="list-cell info">
            <div class="item-title">{{ photo.title || photo.filename }}</div>
            <div class="item-details">
              <span>{{ photo.orderCode }}</span>
              <span>{{ formatFileSize(photo.fileSize) }}</span>
            </div>
          </div>
          <div class="list-cell category">
            <span class="category-badge" :class="photo.category">
              {{ getCategoryText(photo.category) }}
            </span>
          </div>
          <div class="list-cell status">
            <span class="status-badge" :class="photo.status">
              <i :class="getStatusIcon(photo.status)"></i>
              {{ getStatusText(photo.status) }}
            </span>
          </div>
          <div class="list-cell date">
            {{ formatDate(photo.uploadedAt) }}
          </div>
          <div class="list-cell actions">
            <div class="list-actions">
              <button @click="viewPhoto(photo)" class="list-action-btn view" title="보기">
                <i class="icon-eye"></i>
              </button>
              <button @click="editPhoto(photo)" class="list-action-btn edit" title="편집">
                <i class="icon-edit"></i>
              </button>
              <button @click="deletePhoto(photo)" class="list-action-btn delete" title="삭제">
                <i class="icon-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="filteredPhotos.length > 0">
      <div class="pagination-info">
        {{ (currentPage - 1) * itemsPerPage + 1 }}-{{ Math.min(currentPage * itemsPerPage, filteredPhotos.length) }} / {{ filteredPhotos.length }}개
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
          <option :value="12">12개씩</option>
          <option :value="24">24개씩</option>
          <option :value="48">48개씩</option>
        </select>
      </div>
    </div>

    <!-- 사진 업로드 모달 -->
    <PhotoUploadModal
      :show="showUploadModal"
      @close="closeUploadModal"
      @upload="handlePhotoUpload"
    />

    <!-- 사진 보기 모달 -->
    <PhotoViewModal
      :show="showPhotoModal"
      :photo="selectedPhoto"
      @close="closePhotoModal"
      @edit="editFromModal"
      @delete="deleteFromModal"
      @approve="approveFromModal"
      @reject="rejectFromModal"
    />

    <!-- 사진 편집 모달 -->
    <PhotoEditModal
      :show="showEditModal"
      :photo="editingPhoto"
      @close="closeEditModal"
      @save="savePhotoEdit"
    />

    <!-- 카메라 캡처 모달 -->
    <CameraCaptureModal
      :show="showCameraModal"
      @close="closeCameraModal"
      @capture="handleCameraCapture"
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
// Removed missing PhotoUploadModal import
// Removed missing PhotoViewModal import
// Removed missing PhotoEditModal import
// Removed missing CameraCaptureModal import

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 반응형 데이터
const loading = ref(false)
const viewMode = ref<'grid' | 'list'>('grid')
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const dateFilter = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const itemsPerPage = ref(24)
const selectedPhotos = ref<number[]>([])
const activePhotoMenu = ref<number | null>(null)

// 모달 상태
const showUploadModal = ref(false)
const showPhotoModal = ref(false)
const showEditModal = ref(false)
const showCameraModal = ref(false)
const selectedPhoto = ref<any>(null)
const editingPhoto = ref<any>(null)

// Mock 사진 데이터
const photos = ref([
  {
    id: 1,
    filename: 'repacking_YCS240812001.jpg',
    title: '리패킹 작업 사진',
    orderCode: 'YCS240812001',
    labelCode: 'YCS240812001-L1',
    category: 'repacking',
    status: 'approved',
    fileSize: 2048576, // 2MB
    thumbnailUrl: '/images/photos/thumb_1.jpg',
    fullUrl: '/images/photos/full_1.jpg',
    uploadedAt: new Date('2024-08-12T10:30:00'),
    uploadedBy: 'warehouse_user_1',
    note: '정상적으로 리패킹 완료',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'iPhone 14 Pro',
      location: 'A1-B2-C3'
    }
  },
  {
    id: 2,
    filename: 'inspection_YCS240812002.jpg',
    title: '상품 검수',
    orderCode: 'YCS240812002',
    labelCode: 'YCS240812002-L1',
    category: 'inspection',
    status: 'reviewing',
    fileSize: 1536000, // 1.5MB
    thumbnailUrl: '/images/photos/thumb_2.jpg',
    fullUrl: '/images/photos/full_2.jpg',
    uploadedAt: new Date('2024-08-12T09:15:00'),
    uploadedBy: 'warehouse_user_2',
    note: '검수 진행 중',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'Samsung Galaxy S23',
      location: 'A2-B1-C5'
    }
  },
  {
    id: 3,
    filename: 'damage_YCS240812003.jpg',
    title: '상품 손상',
    orderCode: 'YCS240812003',
    labelCode: 'YCS240812003-L1',
    category: 'damage',
    status: 'uploaded',
    fileSize: 3072000, // 3MB
    thumbnailUrl: '/images/photos/thumb_3.jpg',
    fullUrl: '/images/photos/full_3.jpg',
    uploadedAt: new Date('2024-08-11T16:45:00'),
    uploadedBy: 'warehouse_user_1',
    note: '포장 파손 확인됨',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'iPhone 14 Pro',
      location: 'TEMP'
    }
  },
  {
    id: 4,
    filename: 'before_YCS240812004.jpg',
    title: '작업 전',
    orderCode: 'YCS240812004',
    labelCode: 'YCS240812004-L1',
    category: 'before',
    status: 'approved',
    fileSize: 1792000, // 1.75MB
    thumbnailUrl: '/images/photos/thumb_4.jpg',
    fullUrl: '/images/photos/full_4.jpg',
    uploadedAt: new Date('2024-08-11T14:20:00'),
    uploadedBy: 'warehouse_user_2',
    note: '작업 시작 전 상태',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'Samsung Galaxy S23',
      location: 'B1-A3-C2'
    }
  }
])

// 계산된 속성
const totalPhotos = computed(() => photos.value.length)
const pendingPhotos = computed(() => 
  photos.value.filter(photo => ['uploaded', 'reviewing'].includes(photo.status)).length
)
const todayPhotos = computed(() => {
  const today = new Date()
  return photos.value.filter(photo => {
    const uploadDate = new Date(photo.uploadedAt)
    return uploadDate.toDateString() === today.toDateString()
  }).length
})
const storageUsed = computed(() => {
  const totalBytes = photos.value.reduce((sum, photo) => sum + photo.fileSize, 0)
  return Math.round(totalBytes / (1024 * 1024)) // MB 단위
})

const hasActiveFilters = computed(() => 
  searchQuery.value || selectedCategory.value || selectedStatus.value || dateFilter.value
)

const filteredPhotos = computed(() => {
  let filtered = [...photos.value]

  // 검색 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(photo =>
      photo.orderCode.toLowerCase().includes(query) ||
      photo.labelCode.toLowerCase().includes(query) ||
      photo.filename.toLowerCase().includes(query) ||
      (photo.title && photo.title.toLowerCase().includes(query))
    )
  }

  // 카테고리 필터
  if (selectedCategory.value) {
    filtered = filtered.filter(photo => photo.category === selectedCategory.value)
  }

  // 상태 필터
  if (selectedStatus.value) {
    filtered = filtered.filter(photo => photo.status === selectedStatus.value)
  }

  // 날짜 필터
  if (dateFilter.value) {
    const filterDate = new Date(dateFilter.value)
    filtered = filtered.filter(photo => {
      const photoDate = new Date(photo.uploadedAt)
      return photoDate.toDateString() === filterDate.toDateString()
    })
  }

  // 정렬
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.uploadedAt).getTime() - new Date(a.uploadedAt).getTime()
      case 'oldest':
        return new Date(a.uploadedAt).getTime() - new Date(b.uploadedAt).getTime()
      case 'size':
        return b.fileSize - a.fileSize
      case 'category':
        return a.category.localeCompare(b.category)
      default:
        return 0
    }
  })

  return filtered
})

const totalPages = computed(() => Math.ceil(filteredPhotos.value.length / itemsPerPage.value))

const paginatedPhotos = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredPhotos.value.slice(start, end)
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
  paginatedPhotos.value.length > 0 && 
  paginatedPhotos.value.every(photo => selectedPhotos.value.includes(photo.id))
)

// 컴포넌트 마운트
onMounted(() => {
  loadPhotos()
})

// 감시자
watch([currentPage, itemsPerPage], () => {
  selectedPhotos.value = []
})

// 메서드
const loadPhotos = async () => {
  loading.value = true
  
  try {
    // TODO: API 호출로 실제 데이터 가져오기
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showToast('사진을 불러왔습니다', 'success')
  } catch (error: any) {
    console.error('Photos loading error:', error)
    showToast('사진을 불러오는 중 오류가 발생했습니다', 'error')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  selectedPhotos.value = []
}

const clearSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
}

const applyFilters = () => {
  currentPage.value = 1
  selectedPhotos.value = []
}

const applySorting = () => {
  currentPage.value = 1
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
  dateFilter.value = ''
  sortBy.value = 'newest'
  currentPage.value = 1
  selectedPhotos.value = []
}

const togglePhotoSelection = (photoId: number) => {
  const index = selectedPhotos.value.indexOf(photoId)
  if (index === -1) {
    selectedPhotos.value.push(photoId)
  } else {
    selectedPhotos.value.splice(index, 1)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedPhotos.value = selectedPhotos.value.filter(id => 
      !paginatedPhotos.value.find(photo => photo.id === id)
    )
  } else {
    paginatedPhotos.value.forEach(photo => {
      if (!selectedPhotos.value.includes(photo.id)) {
        selectedPhotos.value.push(photo.id)
      }
    })
  }
}

const clearSelection = () => {
  selectedPhotos.value = []
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const updatePagination = () => {
  currentPage.value = 1
  selectedPhotos.value = []
}

// 사진 액션
const openPhotoModal = (photo: any) => {
  selectedPhoto.value = photo
  showPhotoModal.value = true
}

const closePhotoModal = () => {
  showPhotoModal.value = false
  selectedPhoto.value = null
}

const viewPhoto = (photo: any) => {
  openPhotoModal(photo)
}

const editPhoto = (photo: any) => {
  editingPhoto.value = { ...photo }
  showEditModal.value = true
}

const editFromModal = () => {
  if (selectedPhoto.value) {
    editPhoto(selectedPhoto.value)
  }
  closePhotoModal()
}

const closeEditModal = () => {
  showEditModal.value = false
  editingPhoto.value = null
}

const savePhotoEdit = (updatedPhoto: any) => {
  const index = photos.value.findIndex(photo => photo.id === updatedPhoto.id)
  if (index !== -1) {
    photos.value[index] = { ...updatedPhoto }
    showToast('사진 정보가 업데이트되었습니다', 'success')
  }
  closeEditModal()
}

const approvePhoto = (photo: any) => {
  const index = photos.value.findIndex(p => p.id === photo.id)
  if (index !== -1) {
    photos.value[index].status = 'approved'
    showToast(`${photo.filename}이 승인되었습니다`, 'success')
  }
}

const approveFromModal = () => {
  if (selectedPhoto.value) {
    approvePhoto(selectedPhoto.value)
  }
  closePhotoModal()
}

const rejectPhoto = (photo: any) => {
  const index = photos.value.findIndex(p => p.id === photo.id)
  if (index !== -1) {
    photos.value[index].status = 'rejected'
    showToast(`${photo.filename}이 거부되었습니다`, 'warning')
  }
}

const rejectFromModal = () => {
  if (selectedPhoto.value) {
    rejectPhoto(selectedPhoto.value)
  }
  closePhotoModal()
}

const deletePhoto = (photo: any) => {
  if (confirm('이 사진을 삭제하시겠습니까?')) {
    const index = photos.value.findIndex(p => p.id === photo.id)
    if (index !== -1) {
      photos.value.splice(index, 1)
      showToast('사진이 삭제되었습니다', 'success')
    }
  }
}

const deleteFromModal = () => {
  if (selectedPhoto.value) {
    deletePhoto(selectedPhoto.value)
  }
  closePhotoModal()
}

const downloadPhoto = (photo: any) => {
  // 실제 구현에서는 파일 다운로드 API 호출
  showToast(`${photo.filename}을 다운로드합니다`, 'info')
}

const duplicatePhoto = (photo: any) => {
  const duplicated = {
    ...photo,
    id: Date.now(),
    filename: `copy_${photo.filename}`,
    title: `${photo.title} (복사본)`,
    uploadedAt: new Date()
  }
  photos.value.unshift(duplicated)
  showToast('사진이 복제되었습니다', 'success')
  activePhotoMenu.value = null
}

const addNote = (photo: any) => {
  const note = prompt('메모를 입력하세요:', photo.note || '')
  if (note !== null) {
    const index = photos.value.findIndex(p => p.id === photo.id)
    if (index !== -1) {
      photos.value[index].note = note
      showToast('메모가 저장되었습니다', 'success')
    }
  }
  activePhotoMenu.value = null
}

const sharePhoto = (photo: any) => {
  showToast(`${photo.filename}의 공유 링크를 생성합니다`, 'info')
  activePhotoMenu.value = null
}

const togglePhotoMenu = (photoId: number) => {
  activePhotoMenu.value = activePhotoMenu.value === photoId ? null : photoId
}

// 일괄 작업
const bulkApprove = () => {
  if (selectedPhotos.value.length === 0) return
  
  selectedPhotos.value.forEach(photoId => {
    const index = photos.value.findIndex(p => p.id === photoId)
    if (index !== -1) {
      photos.value[index].status = 'approved'
    }
  })
  
  showToast(`${selectedPhotos.value.length}개 사진이 승인되었습니다`, 'success')
  clearSelection()
}

const bulkReject = () => {
  if (selectedPhotos.value.length === 0) return
  
  selectedPhotos.value.forEach(photoId => {
    const index = photos.value.findIndex(p => p.id === photoId)
    if (index !== -1) {
      photos.value[index].status = 'rejected'
    }
  })
  
  showToast(`${selectedPhotos.value.length}개 사진이 거부되었습니다`, 'warning')
  clearSelection()
}

const bulkDelete = () => {
  if (selectedPhotos.value.length === 0) return
  
  if (confirm(`선택된 ${selectedPhotos.value.length}개 사진을 삭제하시겠습니까?`)) {
    selectedPhotos.value.forEach(photoId => {
      const index = photos.value.findIndex(p => p.id === photoId)
      if (index !== -1) {
        photos.value.splice(index, 1)
      }
    })
    
    showToast(`${selectedPhotos.value.length}개 사진이 삭제되었습니다`, 'success')
    clearSelection()
  }
}

// 업로드 관련
const closeUploadModal = () => {
  showUploadModal.value = false
}

const handlePhotoUpload = (uploadData: any) => {
  // Mock 업로드된 사진 추가
  const newPhotos = uploadData.files.map((file: any, index: number) => ({
    id: Date.now() + index,
    filename: file.name,
    title: file.title || file.name,
    orderCode: uploadData.orderCode,
    labelCode: uploadData.labelCode,
    category: uploadData.category,
    status: 'uploaded',
    fileSize: file.size,
    thumbnailUrl: file.thumbnailUrl,
    fullUrl: file.fullUrl,
    uploadedAt: new Date(),
    uploadedBy: 'current_user',
    note: uploadData.note || '',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'Unknown',
      location: uploadData.location || ''
    }
  }))

  photos.value.unshift(...newPhotos)
  showToast(`${newPhotos.length}개 사진이 업로드되었습니다`, 'success')
  closeUploadModal()
}

const capturePhoto = () => {
  showCameraModal.value = true
}

const closeCameraModal = () => {
  showCameraModal.value = false
}

const handleCameraCapture = (captureData: any) => {
  const newPhoto = {
    id: Date.now(),
    filename: `capture_${Date.now()}.jpg`,
    title: '촬영된 사진',
    orderCode: captureData.orderCode,
    labelCode: captureData.labelCode,
    category: captureData.category,
    status: 'uploaded',
    fileSize: captureData.fileSize || 2048576,
    thumbnailUrl: captureData.thumbnailUrl,
    fullUrl: captureData.fullUrl,
    uploadedAt: new Date(),
    uploadedBy: 'current_user',
    note: captureData.note || '',
    metadata: {
      width: 1920,
      height: 1080,
      camera: 'Camera',
      location: captureData.location || ''
    }
  }

  photos.value.unshift(newPhoto)
  showToast('사진이 촬영되어 추가되었습니다', 'success')
  closeCameraModal()
}

// 이미지 로딩 처리
const handleImageLoad = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.classList.add('loaded')
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/placeholder.png' // 기본 이미지로 대체
}

// 유틸리티 함수
const getCategoryText = (category: string): string => {
  const categoryMap: Record<string, string> = {
    repacking: '리패킹',
    inspection: '검수',
    damage: '손상',
    before: '작업 전',
    after: '작업 후'
  }
  return categoryMap[category] || category
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    uploaded: '업로드됨',
    reviewing: '검토 중',
    approved: '승인됨',
    rejected: '거부됨'
  }
  return statusMap[status] || status
}

const getStatusIcon = (status: string): string => {
  const iconMap: Record<string, string> = {
    uploaded: 'icon-upload',
    reviewing: 'icon-clock',
    approved: 'icon-check',
    rejected: 'icon-x'
  }
  return iconMap[status] || 'icon-circle'
}

const formatFileSize = (bytes: number): string => {
  const sizes = ['B', 'KB', 'MB', 'GB']
  if (bytes === 0) return '0 B'
  
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 외부 클릭 처리
document.addEventListener('click', () => {
  activePhotoMenu.value = null
})
</script>

<style scoped>
.photos-view {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.photos-header {
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

.photos-description {
  font-size: 14px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-upload,
.btn-capture {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-weight: 500;
}

.btn-upload {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-upload:hover {
  background: #2563eb;
}

.btn-capture {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-capture:hover {
  background: #f9fafb;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.photos {
  background: #3b82f6;
}

.stat-icon.pending {
  background: #f59e0b;
}

.stat-icon.today {
  background: #10b981;
}

.stat-icon.storage {
  background: #8b5cf6;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
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
  max-width: 500px;
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

.view-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-mode-toggle {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
}

.view-mode-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  color: #6b7280;
}

.view-mode-btn.active {
  background: white;
  color: #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.selection-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selection-count {
  font-size: 14px;
  color: #6b7280;
}

.btn-bulk {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.15s ease;
}

.btn-bulk.approve {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.btn-bulk.reject {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-bulk.delete {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-clear-selection {
  background: none;
  border: 1px solid #d1d5db;
  color: #6b7280;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.photos-grid,
.photos-list {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
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

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.photo-card {
  position: relative;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.15s ease;
}

.photo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.photo-card.selected {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.photo-selection {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
}

.photo-checkbox {
  width: 20px;
  height: 20px;
  accent-color: #3b82f6;
}

.photo-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
}

.photo-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.15s ease;
}

.photo-image:hover img {
  transform: scale(1.05);
}

.photo-image img.loaded {
  opacity: 1;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.15s ease;
}

.photo-image:hover .photo-overlay {
  opacity: 1;
}

.overlay-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.15s ease;
}

.overlay-btn:hover {
  background: white;
  transform: scale(1.1);
}

.photo-info {
  padding: 16px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.photo-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
  margin-right: 8px;
}

.photo-status {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.photo-status.uploaded {
  background: #f3f4f6;
  color: #374151;
}

.photo-status.reviewing {
  background: #fef3c7;
  color: #92400e;
}

.photo-status.approved {
  background: #dcfce7;
  color: #166534;
}

.photo-status.rejected {
  background: #fef2f2;
  color: #dc2626;
}

.info-details {
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
}

.detail-row .label {
  color: #6b7280;
}

.detail-row .value {
  color: #1f2937;
  font-weight: 500;
}

.detail-row .value.category {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.value.category.repacking {
  background: #dbeafe;
  color: #1e40af;
}

.value.category.inspection {
  background: #dcfce7;
  color: #166534;
}

.value.category.damage {
  background: #fef2f2;
  color: #dc2626;
}

.value.category.before,
.value.category.after {
  background: #f3e8ff;
  color: #7c3aed;
}

.photo-actions {
  display: flex;
  gap: 4px;
  position: relative;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s ease;
}

.action-btn.view {
  background: #eff6ff;
  color: #3b82f6;
}

.action-btn.edit {
  background: #fef3c7;
  color: #f59e0b;
}

.action-btn.approve {
  background: #dcfce7;
  color: #166534;
}

.action-btn.reject {
  background: #fef2f2;
  color: #dc2626;
}

.action-btn.more {
  background: #f3f4f6;
  color: #6b7280;
}

.action-btn:hover {
  transform: scale(1.1);
}

.dropdown-menu {
  position: relative;
}

.dropdown-content {
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
  font-size: 14px;
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

.list-container {
  display: flex;
  flex-direction: column;
}

.list-header {
  display: grid;
  grid-template-columns: 40px 100px 2fr 120px 120px 140px 120px;
  gap: 12px;
  padding: 16px 12px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.list-item {
  display: grid;
  grid-template-columns: 40px 100px 2fr 120px 120px 140px 120px;
  gap: 12px;
  padding: 16px 12px;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.15s ease;
  align-items: center;
}

.list-item:hover {
  background: #f8fafc;
}

.list-item.selected {
  background: #eff6ff;
}

.list-cell {
  display: flex;
  align-items: center;
}

.list-cell.checkbox {
  justify-content: center;
}

.thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.item-details {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
}

.category-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.category-badge.repacking {
  background: #dbeafe;
  color: #1e40af;
}

.category-badge.inspection {
  background: #dcfce7;
  color: #166534;
}

.category-badge.damage {
  background: #fef2f2;
  color: #dc2626;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.uploaded {
  background: #f3f4f6;
  color: #374151;
}

.status-badge.reviewing {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.approved {
  background: #dcfce7;
  color: #166534;
}

.status-badge.rejected {
  background: #fef2f2;
  color: #dc2626;
}

.list-actions {
  display: flex;
  gap: 4px;
}

.list-action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.15s ease;
}

.list-action-btn.view {
  background: #eff6ff;
  color: #3b82f6;
}

.list-action-btn.edit {
  background: #fef3c7;
  color: #f59e0b;
}

.list-action-btn.delete {
  background: #fef2f2;
  color: #dc2626;
}

.pagination {
  display: flex;
  justify-content: space-between;
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
  .photos-view {
    padding: 16px;
  }

  .photos-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }

  .view-controls {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }

  .list-header,
  .list-item {
    grid-template-columns: 40px 80px 1fr 100px 80px;
    gap: 8px;
  }

  .list-cell.date,
  .header-cell.date {
    display: none;
  }

  .list-cell.actions,
  .header-cell.actions {
    display: none;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .grid-container {
    grid-template-columns: 1fr;
  }

  .photo-actions {
    justify-content: center;
  }

  .list-header,
  .list-item {
    grid-template-columns: 40px 60px 1fr 80px;
  }

  .list-cell.category,
  .header-cell.category {
    display: none;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
  }

  .pagination-controls {
    justify-content: space-around;
  }
}
</style>