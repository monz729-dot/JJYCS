<template>
  <div class="user-approval-page">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">{{ $t('admin.user_approval.title') }}</h1>
      <p class="page-subtitle">{{ $t('admin.user_approval.subtitle') }}</p>
      
      <!-- Stats -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon pending">
            <ClockIcon class="icon" />
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.pending }}</div>
            <div class="stat-label">{{ $t('admin.user_approval.pending') }}</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon approved">
            <CheckCircleIcon class="icon" />
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.approved }}</div>
            <div class="stat-label">{{ $t('admin.user_approval.approved') }}</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon rejected">
            <XCircleIcon class="icon" />
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.rejected }}</div>
            <div class="stat-label">{{ $t('admin.user_approval.rejected') }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-section">
      <div class="filter-controls">
        <select v-model="selectedRole" class="filter-select">
          <option value="">{{ $t('admin.user_approval.all_roles') }}</option>
          <option value="enterprise">{{ $t('roles.enterprise') }}</option>
          <option value="partner">{{ $t('roles.partner') }}</option>
          <option value="warehouse">{{ $t('roles.warehouse') }}</option>
        </select>
        
        <select v-model="selectedStatus" class="filter-select">
          <option value="">{{ $t('admin.user_approval.all_statuses') }}</option>
          <option value="pending">{{ $t('admin.user_approval.pending') }}</option>
          <option value="approved">{{ $t('admin.user_approval.approved') }}</option>
          <option value="rejected">{{ $t('admin.user_approval.rejected') }}</option>
        </select>
        
        <div class="search-box">
          <MagnifyingGlassIcon class="search-icon" />
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            :placeholder="$t('admin.user_approval.search_placeholder')"
          />
        </div>
      </div>
    </div>

    <!-- User List -->
    <div class="user-list">
      <div v-if="loading" class="loading-state">
        <LoadingSpinner />
        <p>{{ $t('admin.user_approval.loading') }}</p>
      </div>
      
      <div v-else-if="filteredUsers.length === 0" class="empty-state">
        <UserGroupIcon class="empty-icon" />
        <h3 class="empty-title">{{ $t('admin.user_approval.no_users') }}</h3>
        <p class="empty-description">{{ $t('admin.user_approval.no_users_desc') }}</p>
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
                  {{ $t(`roles.${user.role}`) }}
                </div>
              </div>
            </div>
            
            <div class="user-status">
              <div class="status-badge" :class="`status-${user.approvalStatus}`">
                <component :is="getStatusIcon(user.approvalStatus)" class="status-icon" />
                {{ $t(`admin.user_approval.${user.approvalStatus}`) }}
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
                <label>{{ $t('admin.user_approval.phone') }}:</label>
                <span>{{ user.phone || '-' }}</span>
              </div>
              
              <div v-if="user.role === 'enterprise'" class="detail-item">
                <label>{{ $t('admin.user_approval.company_name') }}:</label>
                <span>{{ user.enterpriseProfile?.companyName || '-' }}</span>
              </div>
              
              <div v-if="user.role === 'enterprise'" class="detail-item">
                <label>{{ $t('admin.user_approval.business_number') }}:</label>
                <span>{{ user.enterpriseProfile?.businessNumber || '-' }}</span>
              </div>
              
              <div v-if="user.role === 'partner'" class="detail-item">
                <label>{{ $t('admin.user_approval.partner_type') }}:</label>
                <span>{{ user.partnerProfile?.partnerType ? $t(`partner.types.${user.partnerProfile.partnerType}`) : '-' }}</span>
              </div>
              
              <div v-if="user.role === 'warehouse'" class="detail-item">
                <label>{{ $t('admin.user_approval.warehouse_name') }}:</label>
                <span>{{ user.warehouseProfile?.warehouseName || '-' }}</span>
              </div>
            </div>
            
            <!-- Approval Notes -->
            <div v-if="user.approvalNotes" class="approval-notes">
              <h4 class="notes-title">{{ $t('admin.user_approval.notes') }}</h4>
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
              {{ $t('admin.user_approval.reject') }}
            </button>
            
            <button
              type="button"
              class="action-btn approve-btn"
              @click="approveUser(user)"
              :disabled="processing === user.id"
            >
              <LoadingSpinner v-if="processing === user.id" size="small" />
              <CheckCircleIcon v-else class="btn-icon" />
              {{ $t('admin.user_approval.approve') }}
            </button>
          </div>
          
          <div v-else class="approval-history">
            <div class="approval-meta">
              <span class="approved-by">
                {{ $t('admin.user_approval.processed_by') }}: {{ user.approvedBy || 'System' }}
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
          <h3 class="modal-title">{{ $t('admin.user_approval.reject_user') }}</h3>
          <button type="button" class="modal-close" @click="closeRejectModal">
            <XMarkIcon class="close-icon" />
          </button>
        </div>
        
        <div class="modal-body">
          <p class="reject-confirmation">
            {{ $t('admin.user_approval.reject_confirmation', { name: selectedUser?.name }) }}
          </p>
          
          <div class="form-group">
            <label class="form-label">{{ $t('admin.user_approval.reject_reason') }}</label>
            <textarea
              v-model="rejectReason"
              class="form-textarea"
              rows="4"
              :placeholder="$t('admin.user_approval.reject_reason_placeholder')"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="closeRejectModal">
            {{ $t('common.cancel') }}
          </button>
          <button
            type="button"
            class="btn-reject"
            @click="rejectUser"
            :disabled="!rejectReason.trim() || !!processing"
          >
            <LoadingSpinner v-if="processing" size="small" />
            {{ $t('admin.user_approval.reject') }}
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

// Lifecycle
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-approval-page {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
  margin: 0 0 2rem 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 3rem;
  height: 3rem;
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
  width: 1.5rem;
  height: 1.5rem;
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
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  color: #6b7280;
  font-size: 0.875rem;
}

.filter-section {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.filter-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  min-width: 150px;
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
  padding: 0.75rem 1rem 0.75rem 3rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
}

.user-list {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
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
  padding: 2rem;
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
  margin-bottom: 1.5rem;
}

.user-info {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.user-avatar {
  width: 3rem;
  height: 3rem;
  background: #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #6b7280;
}

.user-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.user-email {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0 0 0.5rem 0;
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
  margin-bottom: 1.5rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
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