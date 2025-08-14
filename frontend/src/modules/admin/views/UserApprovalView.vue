<template>
  <div class="user-approval-page">
    <!-- Header - PC Optimized -->
    <div class="space-y-6">
      <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">사용자 승인 관리</h1>
          <p class="text-lg text-gray-600 mt-2">기업 및 파트너 사용자의 승인을 관리합니다</p>
        </div>
        <div class="mt-4 lg:mt-0 flex space-x-3">
          <button @click="bulkApprove" 
                  class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700">
            <CheckIcon class="h-4 w-4 mr-2" />
            일괄 승인
          </button>
          <button @click="bulkReject" 
                  class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700">
            <XMarkIcon class="h-4 w-4 mr-2" />
            일괄 거절
          </button>
        </div>
      </div>
      
      <!-- Stats Cards - PC Optimized -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <ClockIcon class="h-8 w-8 text-yellow-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">승인 대기</dt>
                <dd class="text-3xl font-bold text-gray-900">{{ stats.pending }}</dd>
              </dl>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <CheckCircleIcon class="h-8 w-8 text-green-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">승인 완료</dt>
                <dd class="text-3xl font-bold text-gray-900">{{ stats.approved }}</dd>
              </dl>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <XCircleIcon class="h-8 w-8 text-red-600" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">승인 거절</dt>
                <dd class="text-3xl font-bold text-gray-900">{{ stats.rejected }}</dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters - PC Optimized -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">검색</label>
          <div class="relative">
            <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input v-model="searchQuery" 
                   type="text" 
                   placeholder="이름, 이메일, 회사명으로 검색..."
                   class="pl-10 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">사용자 유형</label>
          <select v-model="selectedRole" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="corporate">기업 사용자</option>
            <option value="partner">파트너</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">승인 상태</label>
          <select v-model="selectedStatus" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="pending">승인 대기</option>
            <option value="approved">승인 완료</option>
            <option value="rejected">승인 거절</option>
          </select>
        </div>
        <div class="flex items-end space-x-2">
          <button @click="resetFilters" 
                  class="flex-1 px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors">
            초기화
          </button>
          <button @click="applyFilters" 
                  class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 transition-colors">
            적용
          </button>
        </div>
      </div>
    </div>

    <!-- User List -->
    <div class="user-list">
             <div v-if="loading" class="loading-state">
         <LoadingSpinner />
         <p>사용자 목록을 불러오는 중...</p>
       </div>
       
       <div v-else-if="filteredUsers.length === 0" class="empty-state">
         <UserGroupIcon class="empty-icon" />
         <h3 class="empty-title">승인 대기 사용자가 없습니다</h3>
         <p class="empty-description">현재 승인 대기 중인 사용자가 없습니다.</p>
       </div>
      
      <div v-else class="user-cards">
        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="user-card"
          :class="`status-${user.approvalStatus}`"
        >
          <!-- User Header -->
          <div class="user-header">
            <div class="user-info">
              <div class="user-avatar">
                <UserIcon class="avatar-icon" />
              </div>
              <div class="user-details">
                <h3 class="user-name">{{ user.name }}</h3>
                <p class="user-email">{{ user.email }}</p>
                                 <div class="user-role-badge" :class="`role-${user.role}`">
                   {{ getRoleText(user.role) }}
                 </div>
              </div>
            </div>
            
            <div class="user-status">
                             <div class="status-badge" :class="`status-${user.approvalStatus}`">
                 <component :is="getStatusIcon(user.approvalStatus)" class="status-icon" />
                 {{ getStatusText(user.approvalStatus) }}
               </div>
              <div class="registration-date">
                {{ formatDate(user.registeredAt) }}
              </div>
            </div>
          </div>

          <!-- User Details -->
          <div class="user-content">
            <div class="detail-grid">
                             <div class="detail-item">
                 <label>전화번호:</label>
                 <span>{{ user.phone || '-' }}</span>
               </div>
               
               <div v-if="user.role === 'enterprise'" class="detail-item">
                 <label>회사명:</label>
                 <span>{{ user.enterpriseProfile?.companyName || '-' }}</span>
               </div>
               
               <div v-if="user.role === 'enterprise'" class="detail-item">
                 <label>사업자번호:</label>
                 <span>{{ user.enterpriseProfile?.businessNumber || '-' }}</span>
               </div>
               
               <div v-if="user.role === 'partner'" class="detail-item">
                 <label>파트너 유형:</label>
                 <span>{{ user.partnerProfile?.partnerType ? getPartnerTypeText(user.partnerProfile.partnerType) : '-' }}</span>
               </div>
               
               <div v-if="user.role === 'warehouse'" class="detail-item">
                 <label>창고명:</label>
                 <span>{{ user.warehouseProfile?.warehouseName || '-' }}</span>
               </div>
            </div>
            
                         <!-- Approval Notes -->
             <div v-if="user.approvalNotes" class="approval-notes">
               <h4 class="notes-title">승인 메모</h4>
               <p class="notes-content">{{ user.approvalNotes }}</p>
             </div>
          </div>

          <!-- Actions -->
          <div v-if="user.approvalStatus === 'pending'" class="user-actions">
                         <button
               type="button"
               class="action-btn reject-btn"
               @click="openRejectModal(user)"
             >
               <XCircleIcon class="btn-icon" />
               거절
             </button>
             
             <button
               type="button"
               class="action-btn approve-btn"
               @click="approveUser(user)"
               :disabled="processing === user.id"
             >
               <LoadingSpinner v-if="processing === user.id" size="small" />
               <CheckCircleIcon v-else class="btn-icon" />
               승인
             </button>
          </div>
          
          <div v-else class="approval-history">
                       <div class="approval-meta">
             <span class="approved-by">
               처리자: {{ user.approvedBy || 'System' }}
             </span>
             <span class="approved-at">
               {{ formatDate(user.approvedAt || '') }}
             </span>
           </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-container">
                 <div class="modal-header">
           <h3 class="modal-title">사용자 거절</h3>
           <button type="button" class="modal-close" @click="closeRejectModal">
             <XMarkIcon class="close-icon" />
           </button>
         </div>
         
         <div class="modal-body">
           <p class="reject-confirmation">
             {{ selectedUser?.name }}님의 가입을 거절하시겠습니까?
           </p>
           
           <div class="form-group">
             <label class="form-label">거절 사유</label>
             <textarea
               v-model="rejectReason"
               class="form-textarea"
               rows="4"
               placeholder="거절 사유를 입력해주세요..."
             ></textarea>
           </div>
         </div>
         
         <div class="modal-footer">
           <button type="button" class="btn-cancel" @click="closeRejectModal">
             취소
           </button>
           <button
             type="button"
             class="btn-reject"
             @click="rejectUser"
             :disabled="!rejectReason.trim() || !!processing"
           >
             <LoadingSpinner v-if="processing" size="small" />
             거절
           </button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import {
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  UserGroupIcon,
  UserIcon,
  MagnifyingGlassIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
// import { adminApi } from '@/services/adminApi'

interface User {
  id: string
  name: string
  email: string
  phone?: string
  role: 'enterprise' | 'partner' | 'warehouse' | 'individual'
  approvalStatus: 'pending' | 'approved' | 'rejected'
  registeredAt: string
  approvedAt?: string
  approvedBy?: string
  approvalNotes?: string
  enterpriseProfile?: {
    companyName: string
    businessNumber: string
    businessAddress: string
  }
  partnerProfile?: {
    partnerType: 'affiliate' | 'referral' | 'corporate'
    businessLicense: string
  }
  warehouseProfile?: {
    warehouseName: string
    location: string
    capacity: number
  }
}

// State
const users = ref<User[]>([])
const loading = ref(true)
const processing = ref<string | null>(null)
const selectedRole = ref('')
const selectedStatus = ref('pending')
const searchQuery = ref('')
const showRejectModal = ref(false)
const selectedUser = ref<User | null>(null)
const rejectReason = ref('')

const toast = useToast()

// Computed
const stats = computed(() => {
  return {
    pending: users.value.filter(u => u.approvalStatus === 'pending').length,
    approved: users.value.filter(u => u.approvalStatus === 'approved').length,
    rejected: users.value.filter(u => u.approvalStatus === 'rejected').length
  }
})

const filteredUsers = computed(() => {
  let filtered = users.value

  // Role filter
  if (selectedRole.value) {
    filtered = filtered.filter(user => user.role === selectedRole.value)
  }

  // Status filter  
  if (selectedStatus.value) {
    filtered = filtered.filter(user => user.approvalStatus === selectedStatus.value)
  }

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user =>
      user.name.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query) ||
      (user.enterpriseProfile?.companyName || '').toLowerCase().includes(query)
    )
  }

  return filtered
})

// Methods
const loadUsers = async () => {
  try {
    loading.value = true
    // Mock data for demonstration
    users.value = [
      {
        id: '1',
        name: '김기업',
        email: 'kim.enterprise@company.com',
        phone: '010-1234-5678',
        role: 'enterprise',
        approvalStatus: 'pending',
        registeredAt: '2025-08-12T10:30:00Z',
        enterpriseProfile: {
          companyName: '(주)글로벌무역',
          businessNumber: '123-45-67890',
          businessAddress: '서울시 강남구 테헤란로 123'
        }
      },
      {
        id: '2', 
        name: '박파트너',
        email: 'park.partner@affiliate.com',
        phone: '010-9876-5432',
        role: 'partner',
        approvalStatus: 'pending',
        registeredAt: '2025-08-12T09:15:00Z',
        partnerProfile: {
          partnerType: 'affiliate',
          businessLicense: 'BL-2025-0001'
        }
      },
      {
        id: '3',
        name: '최창고',
        email: 'choi.warehouse@logistics.com',
        phone: '010-5555-6666',
        role: 'warehouse',
        approvalStatus: 'approved',
        registeredAt: '2025-08-11T14:20:00Z',
        approvedAt: '2025-08-12T08:30:00Z',
        approvedBy: 'admin@ycs.com',
        warehouseProfile: {
          warehouseName: 'YCS 방콕 창고',
          location: '방콕 라차다피세크',
          capacity: 1000
        }
      }
    ]
  } catch (error) {
    toast.error('사용자 목록을 불러오는데 실패했습니다.')
    console.error('Load users error:', error)
  } finally {
    loading.value = false
  }
}

const approveUser = async (user: User) => {
  try {
    processing.value = user.id
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Update user status
    const userIndex = users.value.findIndex(u => u.id === user.id)
    if (userIndex !== -1) {
      users.value[userIndex] = {
        ...users.value[userIndex],
        approvalStatus: 'approved',
        approvedAt: new Date().toISOString(),
        approvedBy: 'admin@ycs.com'
      }
    }
    
    toast.success(`${user.name}님의 가입이 승인되었습니다.`)
    
    // Show notification about 1-2 business days
    setTimeout(() => {
      toast.info('승인 완료 알림이 사용자에게 발송되었습니다. (평일 1~2일 내 처리)')
    }, 1000)
    
  } catch (error) {
    toast.error('승인 처리 중 오류가 발생했습니다.')
    console.error('Approve user error:', error)
  } finally {
    processing.value = null
  }
}

const openRejectModal = (user: User) => {
  selectedUser.value = user
  showRejectModal.value = true
  rejectReason.value = ''
}

const closeRejectModal = () => {
  showRejectModal.value = false
  selectedUser.value = null
  rejectReason.value = ''
}

const rejectUser = async () => {
  if (!selectedUser.value) return
  
  try {
    processing.value = selectedUser.value.id
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Update user status
    const userIndex = users.value.findIndex(u => u.id === selectedUser.value!.id)
    if (userIndex !== -1) {
      users.value[userIndex] = {
        ...users.value[userIndex],
        approvalStatus: 'rejected',
        approvedAt: new Date().toISOString(),
        approvedBy: 'admin@ycs.com',
        approvalNotes: rejectReason.value
      }
    }
    
    toast.success(`${selectedUser.value.name}님의 가입이 거절되었습니다.`)
    closeRejectModal()
    
  } catch (error) {
    toast.error('거절 처리 중 오류가 발생했습니다.')
    console.error('Reject user error:', error)
  } finally {
    processing.value = null
  }
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'pending': return ClockIcon
    case 'approved': return CheckCircleIcon
    case 'rejected': return XCircleIcon
    default: return ClockIcon
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Helper functions
const getRoleText = (role: string) => {
  const texts = {
    'enterprise': '기업 사용자',
    'partner': '파트너',
    'warehouse': '창고',
    'individual': '일반 사용자'
  }
  return texts[role as keyof typeof texts] || '알수없음'
}

const getStatusText = (status: string) => {
  const texts = {
    'pending': '승인 대기',
    'approved': '승인 완료',
    'rejected': '승인 거절'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}

const getPartnerTypeText = (type: string) => {
  const texts = {
    'affiliate': '제휴 파트너',
    'referral': '추천 파트너',
    'corporate': '기업 파트너'
  }
  return texts[type as keyof typeof texts] || '알수없음'
}

const bulkApprove = () => {
  // 일괄 승인 로직
  console.log('일괄 승인')
}

const bulkReject = () => {
  // 일괄 거절 로직
  console.log('일괄 거절')
}

const resetFilters = () => {
  selectedRole.value = ''
  selectedStatus.value = 'pending'
  searchQuery.value = ''
}

const applyFilters = () => {
  // 필터 적용 로직
  console.log('필터 적용')
}

// Lifecycle
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-approval-page {
  padding: 0;
  max-width: 100%;
  margin: 0;
}

.page-header {
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.page-subtitle {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0 0 1rem 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.pending {
  background: #fef3c7;
}

.stat-icon.approved {
  background: #d1fae5;
}

.stat-icon.rejected {
  background: #fecaca;
}

.stat-icon .icon {
  width: 1.25rem;
  height: 1.25rem;
}

.stat-icon.pending .icon {
  color: #f59e0b;
}

.stat-icon.approved .icon {
  color: #10b981;
}

.stat-icon.rejected .icon {
  color: #ef4444;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  color: #6b7280;
  font-size: 0.75rem;
}

.filter-section {
  background: white;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.filter-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  min-width: 150px;
  height: 36px;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1.25rem;
  height: 1.25rem;
  color: #9ca3af;
}

.search-input {
  width: 100%;
  padding: 0.5rem 0.75rem 0.5rem 2.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  height: 36px;
}

.user-list {
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-icon {
  width: 4rem;
  height: 4rem;
  color: #9ca3af;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.empty-description {
  color: #6b7280;
  margin: 0;
}

.user-cards {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #f3f4f6;
}

.user-card {
  background: white;
  padding: 1.25rem;
  transition: all 0.2s;
}

.user-card:hover {
  background: #fafafa;
}

.user-card.status-pending {
  border-left: 4px solid #f59e0b;
}

.user-card.status-approved {
  border-left: 4px solid #10b981;
}

.user-card.status-rejected {
  border-left: 4px solid #ef4444;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.user-info {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.user-avatar {
  width: 2.5rem;
  height: 2.5rem;
  background: #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #6b7280;
}

.user-name {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.125rem 0;
}

.user-email {
  color: #6b7280;
  font-size: 0.75rem;
  margin: 0 0 0.25rem 0;
}

.user-role-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: 9999px;
  text-transform: uppercase;
}

.user-role-badge.role-enterprise {
  background: #dbeafe;
  color: #1e40af;
}

.user-role-badge.role-partner {
  background: #f3e8ff;
  color: #7c3aed;
}

.user-role-badge.role-warehouse {
  background: #d1fae5;
  color: #047857;
}

.user-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 9999px;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.status-approved {
  background: #d1fae5;
  color: #047857;
}

.status-badge.status-rejected {
  background: #fecaca;
  color: #b91c1c;
}

.status-icon {
  width: 1rem;
  height: 1rem;
}

.registration-date {
  font-size: 0.75rem;
  color: #9ca3af;
}

.user-content {
  margin-bottom: 1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.detail-item span {
  color: #6b7280;
}

.approval-notes {
  background: #f9fafb;
  border-radius: 0.5rem;
  padding: 1rem;
}

.notes-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.notes-content {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
  line-height: 1.6;
}

.user-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.reject-btn {
  background: #fee2e2;
  color: #b91c1c;
}

.reject-btn:hover {
  background: #fecaca;
}

.approve-btn {
  background: #10b981;
  color: white;
}

.approve-btn:hover {
  background: #059669;
}

.approve-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  width: 1rem;
  height: 1rem;
}

.approval-history {
  display: flex;
  justify-content: flex-end;
}

.approval-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

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
  z-index: 50;
  padding: 1rem;
}

.modal-container {
  background: white;
  border-radius: 1rem;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  color: #9ca3af;
  transition: color 0.2s;
}

.modal-close:hover {
  color: #6b7280;
}

.close-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.modal-body {
  padding: 1.5rem;
}

.reject-confirmation {
  color: #374151;
  margin: 0 0 1.5rem 0;
  line-height: 1.6;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  resize: vertical;
  min-height: 100px;
}

.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1rem 1.5rem 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-reject {
  padding: 0.75rem 1.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #f9fafb;
}

.btn-reject {
  background: #ef4444;
  color: white;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-reject:hover {
  background: #dc2626;
}

.btn-reject:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .user-approval-page {
    padding: 1rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .user-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .user-info {
    width: 100%;
  }
  
  .user-status {
    align-items: flex-start;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .user-actions {
    flex-direction: column;
  }
  
  .modal-footer {
    flex-direction: column;
  }
}
</style>