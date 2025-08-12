<template>
  <div class="links-view">
    <!-- 헤더 -->
    <div class="view-header">
      <div class="header-info">
        <h1>추천 링크 관리</h1>
        <p>추천 링크를 생성하고 공유하여 수수료를 얻어보세요</p>
      </div>
      <div class="header-actions">
        <button @click="openMarketingKit" class="btn-secondary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10"></path>
          </svg>
          마케팅 키트
        </button>
        <button @click="openLinkCreator" class="btn-primary">
          <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          링크 생성
        </button>
      </div>
    </div>

    <!-- 통계 대시보드 -->
    <div class="stats-dashboard">
      <div class="stat-card clicks">
        <div class="stat-icon">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalClicks.toLocaleString() }}</div>
          <div class="stat-label">총 클릭수</div>
          <div class="stat-change positive">+{{ thisMonthClicks }} 이번달</div>
        </div>
      </div>

      <div class="stat-card conversions">
        <div class="stat-icon">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalConversions.toLocaleString() }}</div>
          <div class="stat-label">총 전환수</div>
          <div class="stat-change positive">{{ conversionRate.toFixed(1) }}% 전환율</div>
        </div>
      </div>

      <div class="stat-card active-links">
        <div class="stat-icon">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ activeReferralLinks.length }}</div>
          <div class="stat-label">활성 링크</div>
          <div class="stat-change">{{ referralLinks.length }}개 전체</div>
        </div>
      </div>

      <div class="stat-card earnings">
        <div class="stat-icon">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">₩{{ linkEarnings.toLocaleString() }}</div>
          <div class="stat-label">링크 수익</div>
          <div class="stat-change positive">이번달 ₩{{ thisMonthEarnings.toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <!-- 빠른 공유 -->
    <div class="quick-share-section">
      <div class="section-header">
        <h2>빠른 공유</h2>
        <p>주요 플랫폼에서 링크를 바로 공유하세요</p>
      </div>
      
      <div class="quick-share-grid">
        <button @click="shareToKakao" class="share-button kakao">
          <div class="share-icon">💬</div>
          <div class="share-info">
            <div class="share-title">카카오톡</div>
            <div class="share-desc">친구들에게 직접 공유</div>
          </div>
        </button>

        <button @click="shareToFacebook" class="share-button facebook">
          <div class="share-icon">📘</div>
          <div class="share-info">
            <div class="share-title">페이스북</div>
            <div class="share-desc">소셜미디어 공유</div>
          </div>
        </button>

        <button @click="shareToInstagram" class="share-button instagram">
          <div class="share-icon">📷</div>
          <div class="share-info">
            <div class="share-title">인스타그램</div>
            <div class="share-desc">스토리로 공유</div>
          </div>
        </button>

        <button @click="shareToEmail" class="share-button email">
          <div class="share-icon">✉️</div>
          <div class="share-info">
            <div class="share-title">이메일</div>
            <div class="share-desc">이메일로 발송</div>
          </div>
        </button>

        <button @click="copyToClipboard" class="share-button clipboard">
          <div class="share-icon">📋</div>
          <div class="share-info">
            <div class="share-title">링크 복사</div>
            <div class="share-desc">클립보드에 복사</div>
          </div>
        </button>

        <button @click="generateQR" class="share-button qr">
          <div class="share-icon">📱</div>
          <div class="share-info">
            <div class="share-title">QR 코드</div>
            <div class="share-desc">QR 코드 생성</div>
          </div>
        </button>
      </div>
    </div>

    <!-- 추천 링크 목록 -->
    <div class="links-section">
      <div class="section-header">
        <h2>내 추천 링크</h2>
        <div class="section-actions">
          <select v-model="filterStatus" class="filter-select">
            <option value="">전체 상태</option>
            <option value="active">활성</option>
            <option value="inactive">비활성</option>
          </select>
          <select v-model="sortBy" class="filter-select">
            <option value="createdDate">생성일순</option>
            <option value="clicks">클릭수순</option>
            <option value="conversions">전환수순</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <svg class="animate-spin h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p>링크를 불러오는 중...</p>
      </div>

      <div v-else-if="filteredLinks.length === 0" class="empty-state">
        <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"></path>
        </svg>
        <h3>추천 링크가 없습니다</h3>
        <p>첫 번째 추천 링크를 만들어보세요!</p>
        <button @click="openLinkCreator" class="btn-primary">링크 만들기</button>
      </div>

      <div v-else class="links-grid">
        <div v-for="link in filteredLinks" :key="link.id" class="link-card">
          <div class="link-header">
            <div class="link-info">
              <div class="link-name">{{ link.name }}</div>
              <div class="link-code">{{ link.code }}</div>
            </div>
            <div class="link-status">
              <span :class="['status-badge', link.isActive ? 'active' : 'inactive']">
                {{ link.isActive ? '활성' : '비활성' }}
              </span>
            </div>
          </div>

          <div class="link-url">
            <div class="url-display">
              <span class="url-text">{{ link.url }}</span>
              <button @click="copyLinkUrl(link.url)" class="copy-btn" title="복사">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                </svg>
              </button>
            </div>
          </div>

          <div class="link-stats">
            <div class="stat-item">
              <div class="stat-value">{{ link.clicks.toLocaleString() }}</div>
              <div class="stat-label">클릭</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ link.conversions.toLocaleString() }}</div>
              <div class="stat-label">전환</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ link.clicks > 0 ? ((link.conversions / link.clicks) * 100).toFixed(1) : 0 }}%</div>
              <div class="stat-label">전환율</div>
            </div>
          </div>

          <div class="link-meta">
            <div class="creation-date">
              생성일: {{ formatDate(link.createdDate) }}
            </div>
            <div v-if="link.expiryDate" class="expiry-date">
              만료일: {{ formatDate(link.expiryDate) }}
            </div>
          </div>

          <div v-if="link.description" class="link-description">
            {{ link.description }}
          </div>

          <div class="link-actions">
            <button @click="shareSingleLink(link)" class="action-btn share" title="공유">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z"></path>
              </svg>
            </button>
            <button @click="editLink(link)" class="action-btn edit" title="수정">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
              </svg>
            </button>
            <button @click="toggleLinkStatus(link)" class="action-btn toggle" :title="link.isActive ? '비활성화' : '활성화'">
              <svg v-if="link.isActive" class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728"></path>
              </svg>
              <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </button>
            <button @click="generateLinkAnalytics(link)" class="action-btn analytics" title="분석">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 링크 생성/수정 모달 -->
    <div v-if="showLinkModal" class="modal-overlay" @click="closeLinkModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingLink ? '링크 수정' : '새 추천 링크 생성' }}</h3>
          <button @click="closeLinkModal" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="link-form">
            <div class="form-row">
              <div class="form-item">
                <label>링크 이름 *</label>
                <input 
                  v-model="linkForm.name" 
                  type="text" 
                  placeholder="예: 겨울 특가 이벤트"
                  class="form-input"
                  required
                />
              </div>
              <div class="form-item">
                <label>링크 코드 *</label>
                <input 
                  v-model="linkForm.code" 
                  type="text" 
                  placeholder="예: WINTER2024"
                  class="form-input"
                  @input="validateCode"
                  required
                />
                <div v-if="codeError" class="error-message">{{ codeError }}</div>
              </div>
            </div>

            <div class="form-item">
              <label>설명</label>
              <textarea 
                v-model="linkForm.description" 
                placeholder="링크에 대한 간단한 설명을 입력하세요"
                class="form-textarea"
                rows="3"
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-item">
                <label>만료일</label>
                <input 
                  v-model="linkForm.expiryDate" 
                  type="date" 
                  class="form-input"
                  :min="tomorrow"
                />
              </div>
              <div class="form-item">
                <label>상태</label>
                <select v-model="linkForm.isActive" class="form-select">
                  <option :value="true">활성</option>
                  <option :value="false">비활성</option>
                </select>
              </div>
            </div>

            <div v-if="linkForm.code" class="preview-section">
              <label>링크 미리보기</label>
              <div class="preview-url">
                {{ generatePreviewUrl(linkForm.code) }}
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button 
            @click="saveLinkForm" 
            :disabled="!canSaveLink || savingLink"
            class="btn-primary"
          >
            <svg v-if="savingLink" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span v-if="savingLink">저장 중...</span>
            <span v-else>{{ editingLink ? '수정' : '생성' }}</span>
          </button>
          <button @click="closeLinkModal" class="btn-secondary" :disabled="savingLink">
            취소
          </button>
        </div>
      </div>
    </div>

    <!-- QR 코드 모달 -->
    <div v-if="showQRModal" class="modal-overlay" @click="closeQRModal">
      <div class="modal-content qr-modal" @click.stop>
        <div class="modal-header">
          <h3>QR 코드</h3>
          <button @click="closeQRModal" class="modal-close">
            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        
        <div class="modal-body text-center">
          <div class="qr-placeholder">
            <svg class="qr-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 16h4.01M16 8h4.01M12 8V4h4.01M4 12h4.01m0 0V8H4.01M4 8V4h4.01v4H4.01z"></path>
            </svg>
            <p>QR 코드가 여기에 표시됩니다</p>
            <p class="qr-url">{{ selectedQRUrl }}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="downloadQR" class="btn-primary">
            QR 코드 다운로드
          </button>
          <button @click="closeQRModal" class="btn-secondary">
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usePartnerStore, type ReferralLink } from '../stores/partnerStore'

const partnerStore = usePartnerStore()

// Reactive data
const filterStatus = ref('')
const sortBy = ref('createdDate')
const showLinkModal = ref(false)
const showQRModal = ref(false)
const editingLink = ref<ReferralLink | null>(null)
const savingLink = ref(false)
const codeError = ref('')
const selectedQRUrl = ref('')

// Form data
const linkForm = ref({
  name: '',
  code: '',
  description: '',
  expiryDate: '',
  isActive: true
})

// Store data
const { referralLinks, loading, stats, thisMonthEarnings } = partnerStore

// Computed properties
const activeReferralLinks = computed(() => 
  referralLinks.filter(link => link.isActive)
)

const totalClicks = computed(() =>
  referralLinks.reduce((sum, link) => sum + link.clicks, 0)
)

const totalConversions = computed(() =>
  referralLinks.reduce((sum, link) => sum + link.conversions, 0)
)

const conversionRate = computed(() => 
  totalClicks.value > 0 ? (totalConversions.value / totalClicks.value) * 100 : 0
)

const thisMonthClicks = computed(() => {
  // Simulate this month's clicks
  return Math.floor(totalClicks.value * 0.15)
})

const linkEarnings = computed(() => {
  // Simulate earnings calculation
  return totalConversions.value * 25000 // Assuming ₩25,000 per conversion
})

const filteredLinks = computed(() => {
  let filtered = referralLinks

  if (filterStatus.value) {
    filtered = filtered.filter(link => 
      filterStatus.value === 'active' ? link.isActive : !link.isActive
    )
  }

  // Apply sorting
  filtered.sort((a, b) => {
    if (sortBy.value === 'createdDate') {
      return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime()
    } else if (sortBy.value === 'clicks') {
      return b.clicks - a.clicks
    } else if (sortBy.value === 'conversions') {
      return b.conversions - a.conversions
    }
    return 0
  })

  return filtered
})

const tomorrow = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return date.toISOString().split('T')[0]
})

const canSaveLink = computed(() => {
  return linkForm.value.name.trim() && 
         linkForm.value.code.trim() && 
         !codeError.value
})

// Methods
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const openLinkCreator = () => {
  editingLink.value = null
  linkForm.value = {
    name: '',
    code: '',
    description: '',
    expiryDate: '',
    isActive: true
  }
  codeError.value = ''
  showLinkModal.value = true
}

const editLink = (link: ReferralLink) => {
  editingLink.value = link
  linkForm.value = {
    name: link.name,
    code: link.code,
    description: link.description || '',
    expiryDate: link.expiryDate || '',
    isActive: link.isActive
  }
  codeError.value = ''
  showLinkModal.value = true
}

const closeLinkModal = () => {
  showLinkModal.value = false
  editingLink.value = null
}

const validateCode = () => {
  const code = linkForm.value.code.trim().toUpperCase()
  linkForm.value.code = code

  if (!code) {
    codeError.value = ''
    return
  }

  // Check for invalid characters
  if (!/^[A-Z0-9]+$/.test(code)) {
    codeError.value = '영문 대문자와 숫자만 사용 가능합니다.'
    return
  }

  // Check length
  if (code.length < 3 || code.length > 20) {
    codeError.value = '3-20자 사이로 입력해주세요.'
    return
  }

  // Check for duplicate (excluding current link if editing)
  const existingLink = referralLinks.find(link => 
    link.code === code && link.id !== editingLink.value?.id
  )
  
  if (existingLink) {
    codeError.value = '이미 사용 중인 코드입니다.'
    return
  }

  codeError.value = ''
}

const generatePreviewUrl = (code: string) => {
  return `https://ycs.logistics.com/signup?ref=${code}`
}

const saveLinkForm = async () => {
  if (!canSaveLink.value) return

  savingLink.value = true

  try {
    if (editingLink.value) {
      // Update existing link
      await partnerStore.updateReferralLink(editingLink.value.id, linkForm.value)
    } else {
      // Create new link
      await partnerStore.createReferralLink(linkForm.value)
    }

    closeLinkModal()
    alert(editingLink.value ? '링크가 수정되었습니다.' : '새 링크가 생성되었습니다.')
  } catch (error) {
    alert('링크 저장에 실패했습니다.')
  } finally {
    savingLink.value = false
  }
}

const copyLinkUrl = async (url: string) => {
  try {
    await navigator.clipboard.writeText(url)
    alert('링크가 클립보드에 복사되었습니다!')
  } catch (error) {
    console.error('Copy failed:', error)
    alert('링크 복사에 실패했습니다.')
  }
}

const toggleLinkStatus = async (link: ReferralLink) => {
  try {
    await partnerStore.updateReferralLink(link.id, { 
      isActive: !link.isActive 
    })
    alert(`링크가 ${!link.isActive ? '활성화' : '비활성화'}되었습니다.`)
  } catch (error) {
    alert('링크 상태 변경에 실패했습니다.')
  }
}

const shareSingleLink = (link: ReferralLink) => {
  if (navigator.share) {
    navigator.share({
      title: `YCS LMS - ${link.name}`,
      text: 'YCS LMS로 물류를 더 스마트하게 관리하세요!',
      url: link.url
    })
  } else {
    copyLinkUrl(link.url)
  }
}

const generateLinkAnalytics = (link: ReferralLink) => {
  alert(`${link.name} 링크 분석:\n클릭: ${link.clicks}회\n전환: ${link.conversions}회\n전환율: ${link.clicks > 0 ? ((link.conversions / link.clicks) * 100).toFixed(1) : 0}%`)
}

// Social sharing methods
const shareToKakao = () => {
  const defaultLink = activeReferralLinks.value[0]
  if (!defaultLink) {
    alert('활성화된 링크가 없습니다.')
    return
  }
  
  // Simulate Kakao share
  window.open(`https://story.kakao.com/share?url=${encodeURIComponent(defaultLink.url)}`)
}

const shareToFacebook = () => {
  const defaultLink = activeReferralLinks.value[0]
  if (!defaultLink) {
    alert('활성화된 링크가 없습니다.')
    return
  }
  
  window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(defaultLink.url)}`)
}

const shareToInstagram = () => {
  alert('인스타그램 스토리에 직접 공유하거나, 링크를 복사하여 바이오에 추가하세요.')
}

const shareToEmail = () => {
  const defaultLink = activeReferralLinks.value[0]
  if (!defaultLink) {
    alert('활성화된 링크가 없습니다.')
    return
  }

  const subject = encodeURIComponent('YCS LMS 추천')
  const body = encodeURIComponent(`안녕하세요!\n\nYCS LMS를 추천드립니다. 아래 링크를 통해 가입하시면 특별 혜택을 받으실 수 있습니다.\n\n${defaultLink.url}\n\n감사합니다!`)
  
  window.open(`mailto:?subject=${subject}&body=${body}`)
}

const copyToClipboard = () => {
  const defaultLink = activeReferralLinks.value[0]
  if (!defaultLink) {
    alert('활성화된 링크가 없습니다.')
    return
  }
  
  copyLinkUrl(defaultLink.url)
}

const generateQR = () => {
  const defaultLink = activeReferralLinks.value[0]
  if (!defaultLink) {
    alert('활성화된 링크가 없습니다.')
    return
  }
  
  selectedQRUrl.value = defaultLink.url
  showQRModal.value = true
}

const closeQRModal = () => {
  showQRModal.value = false
  selectedQRUrl.value = ''
}

const downloadQR = () => {
  // Simulate QR code download
  alert('QR 코드가 다운로드됩니다.')
  closeQRModal()
}

const openMarketingKit = () => {
  // Simulate marketing kit download
  alert('마케팅 키트가 다운로드됩니다.')
}

onMounted(() => {
  // Data is already loaded from the store
})
</script>

<style scoped>
.links-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.header-info p {
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-primary, .btn-secondary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #374151;
  border-color: #d1d5db;
}

.btn-secondary:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #9ca3af;
}

.stats-dashboard {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-card.clicks .stat-icon {
  background: #3b82f6;
}

.stat-card.conversions .stat-icon {
  background: #10b981;
}

.stat-card.active-links .stat-icon {
  background: #f59e0b;
}

.stat-card.earnings .stat-icon {
  background: #8b5cf6;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  color: #6b7280;
}

.stat-change.positive {
  color: #059669;
}

.quick-share-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.section-header p {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.quick-share-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.share-button {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.share-button:hover {
  background: #f3f4f6;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.share-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
  flex-shrink: 0;
}

.share-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.share-desc {
  font-size: 13px;
  color: #6b7280;
}

.links-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #6b7280;
  margin: 0 0 24px 0;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.link-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  background: #f9fafb;
  transition: all 0.2s;
}

.link-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.link-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.link-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.link-code {
  font-size: 12px;
  color: #6b7280;
  font-family: monospace;
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 3px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
}

.link-url {
  margin-bottom: 16px;
}

.url-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
}

.url-text {
  flex: 1;
  color: #3b82f6;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}

.copy-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f3f4f6;
  border-radius: 4px;
  color: #6b7280;
  cursor: pointer;
  flex-shrink: 0;
}

.copy-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.link-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
}

.stat-item {
  text-align: center;
}

.stat-item .stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.stat-item .stat-label {
  font-size: 11px;
  color: #6b7280;
}

.link-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #6b7280;
  margin-bottom: 8px;
}

.link-description {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.4;
}

.link-actions {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  color: #3b82f6;
  border-color: #3b82f6;
}

.action-btn.share:hover {
  color: #10b981;
  border-color: #10b981;
}

.action-btn.edit:hover {
  color: #f59e0b;
  border-color: #f59e0b;
}

.action-btn.toggle:hover {
  color: #ef4444;
  border-color: #ef4444;
}

.action-btn.analytics:hover {
  color: #8b5cf6;
  border-color: #8b5cf6;
}

/* Modal styles */
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
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content.qr-modal {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #6b7280;
  cursor: pointer;
  border-radius: 6px;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #111827;
}

.modal-body {
  padding: 24px;
}

.link-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-input, .form-textarea, .form-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.form-input:focus, .form-textarea:focus, .form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.error-message {
  font-size: 12px;
  color: #ef4444;
}

.preview-section {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 16px;
}

.preview-section label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
  display: block;
}

.preview-url {
  font-family: monospace;
  font-size: 12px;
  color: #3b82f6;
  background: white;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-all;
}

.qr-placeholder {
  padding: 40px 20px;
  text-align: center;
}

.qr-icon {
  width: 120px;
  height: 120px;
  color: #9ca3af;
  margin: 0 auto 16px;
}

.qr-url {
  font-family: monospace;
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-all;
  margin-top: 12px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px 24px;
  border-top: 1px solid #e5e7eb;
}

.text-center {
  text-align: center;
}

@media (max-width: 768px) {
  .links-view {
    padding: 16px;
  }
  
  .view-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .stats-dashboard {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .quick-share-grid {
    grid-template-columns: 1fr;
  }
  
  .links-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>