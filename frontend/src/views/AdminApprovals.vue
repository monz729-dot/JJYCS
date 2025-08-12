<template>
  <div class="admin-approvals">
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">계정 승인 관리</h1>
          <p class="text-gray-600 mt-1">기업 및 파트너 계정 승인을 처리하세요</p>
        </div>
        <router-link :to="{ name: 'AdminDashboard' }" class="btn btn-secondary">
          ← 관리자 대시보드
        </router-link>
      </div>
    </div>

    <!-- Summary Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="bg-orange-50 p-6 rounded-lg border border-orange-200">
        <div class="flex items-center">
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center mr-4">
            <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-orange-600">승인 대기</p>
            <p class="text-3xl font-bold text-orange-900">{{ pendingApprovals.length }}</p>
          </div>
        </div>
      </div>

      <div class="bg-green-50 p-6 rounded-lg border border-green-200">
        <div class="flex items-center">
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mr-4">
            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-green-600">오늘 승인</p>
            <p class="text-3xl font-bold text-green-900">{{ todayApproved }}</p>
          </div>
        </div>
      </div>

      <div class="bg-blue-50 p-6 rounded-lg border border-blue-200">
        <div class="flex items-center">
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mr-4">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-blue-600">평균 처리시간</p>
            <p class="text-3xl font-bold text-blue-900">1.2일</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Batch Actions -->
    <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mb-6" v-if="selectedApprovals.length > 0">
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <span class="text-sm font-medium text-gray-700">{{ selectedApprovals.length }}개 선택됨</span>
        </div>
        <div class="flex space-x-3">
          <button 
            @click="batchApprove"
            class="btn btn-success"
          >
            일괄 승인
          </button>
          <button 
            @click="batchReject"
            class="btn btn-error"
          >
            일괄 거부
          </button>
          <button 
            @click="clearSelection"
            class="btn btn-secondary"
          >
            선택 해제
          </button>
        </div>
      </div>
    </div>

    <!-- Pending Approvals -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200">
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900">승인 대기 목록</h2>
          <div class="flex items-center space-x-2">
            <label class="inline-flex items-center">
              <input 
                type="checkbox" 
                :checked="allSelected"
                @change="toggleAllSelection"
                class="form-checkbox h-4 w-4 text-blue-600"
              />
              <span class="ml-2 text-sm text-gray-700">전체 선택</span>
            </label>
          </div>
        </div>
      </div>

      <div class="divide-y divide-gray-200">
        <div 
          v-for="approval in pendingApprovals" 
          :key="approval.id"
          class="p-6 hover:bg-gray-50"
        >
          <div class="flex items-start space-x-4">
            <div class="flex items-center">
              <input 
                type="checkbox" 
                :value="approval.id"
                v-model="selectedApprovals"
                class="form-checkbox h-4 w-4 text-blue-600"
              />
            </div>
            
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                  <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-sm font-medium text-blue-600">
                      {{ getUserInitials(approval.name) }}
                    </span>
                  </div>
                  <div>
                    <h3 class="text-lg font-medium text-gray-900">{{ approval.name }}</h3>
                    <p class="text-sm text-gray-500">{{ approval.email }}</p>
                  </div>
                  <span :class="getRoleBadgeClass(approval.role)" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                    {{ getRoleText(approval.role) }}
                  </span>
                </div>
                <div class="text-right">
                  <p class="text-sm text-gray-500">신청일</p>
                  <p class="text-sm font-medium text-gray-900">{{ formatDate(approval.createdAt) }}</p>
                  <p class="text-xs text-gray-400">{{ getDaysAgo(approval.createdAt) }}일 전</p>
                </div>
              </div>

              <!-- Company Info for Enterprise/Partner -->
              <div v-if="approval.companyName" class="mt-4 bg-gray-50 p-4 rounded-lg">
                <h4 class="text-sm font-medium text-gray-900 mb-2">회사 정보</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p class="text-xs text-gray-500">회사명</p>
                    <p class="text-sm font-medium text-gray-900">{{ approval.companyName }}</p>
                  </div>
                  <div v-if="approval.businessNumber">
                    <p class="text-xs text-gray-500">사업자등록번호</p>
                    <p class="text-sm font-medium text-gray-900">{{ approval.businessNumber }}</p>
                  </div>
                  <div v-if="approval.businessAddress" class="md:col-span-2">
                    <p class="text-xs text-gray-500">사업장 주소</p>
                    <p class="text-sm font-medium text-gray-900">{{ approval.businessAddress }}</p>
                  </div>
                </div>
              </div>

              <!-- Partner Info -->
              <div v-if="approval.role === 'partner'" class="mt-4 bg-green-50 p-4 rounded-lg">
                <h4 class="text-sm font-medium text-gray-900 mb-2">파트너 정보</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-if="approval.partnerType">
                    <p class="text-xs text-gray-500">파트너 유형</p>
                    <p class="text-sm font-medium text-gray-900">{{ getPartnerTypeText(approval.partnerType) }}</p>
                  </div>
                  <div v-if="approval.referralCode">
                    <p class="text-xs text-gray-500">추천 코드</p>
                    <p class="text-sm font-medium text-gray-900">{{ approval.referralCode }}</p>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="mt-6 flex items-center justify-between">
                <div class="flex items-center space-x-4">
                  <div class="flex items-center text-sm text-gray-500">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                    </svg>
                    {{ approval.phone }}
                  </div>
                  <div v-if="approval.emailVerified" class="flex items-center text-sm text-green-600">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    이메일 인증됨
                  </div>
                </div>
                
                <div class="flex space-x-3">
                  <button 
                    @click="showApprovalModal(approval)"
                    class="btn btn-success"
                  >
                    승인
                  </button>
                  <button 
                    @click="showRejectModal(approval)"
                    class="btn btn-error"
                  >
                    거부
                  </button>
                  <button class="btn btn-secondary">상세</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="pendingApprovals.length === 0" class="p-12 text-center">
          <svg class="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <h3 class="text-lg font-medium text-gray-900 mb-2">승인 대기 중인 계정이 없습니다</h3>
          <p class="text-gray-500">모든 계정이 처리되었습니다.</p>
        </div>
      </div>
    </div>

    <!-- Approval Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white p-6 rounded-lg max-w-md w-full mx-4">
        <h3 class="text-lg font-medium text-gray-900 mb-4">
          {{ modalType === 'approve' ? '계정 승인' : '계정 거부' }}
        </h3>
        <p class="text-gray-600 mb-4">
          <strong>{{ selectedUser?.name }}</strong>{{ modalType === 'approve' ? '의 계정을 승인하시겠습니까?' : '의 계정을 거부하시겠습니까?' }}
        </p>
        
        <div v-if="modalType === 'reject'" class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2">거부 사유</label>
          <textarea 
            v-model="rejectReason"
            class="form-input w-full"
            rows="3"
            placeholder="거부 사유를 입력하세요..."
          ></textarea>
        </div>

        <div class="flex justify-end space-x-3">
          <button @click="closeModal" class="btn btn-secondary">취소</button>
          <button 
            @click="confirmAction"
            :class="modalType === 'approve' ? 'btn btn-success' : 'btn btn-error'"
          >
            {{ modalType === 'approve' ? '승인' : '거부' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface PendingApproval {
  id: string
  name: string
  email: string
  phone: string
  role: 'enterprise' | 'partner'
  status: 'pending_approval'
  createdAt: string
  emailVerified: boolean
  companyName?: string
  businessNumber?: string
  businessAddress?: string
  partnerType?: 'affiliate' | 'referral' | 'corporate'
  referralCode?: string
}

const pendingApprovals = ref<PendingApproval[]>([
  {
    id: '1',
    name: '김기업',
    email: 'enterprise1@company.com',
    phone: '02-1234-5678',
    role: 'enterprise',
    status: 'pending_approval',
    createdAt: '2024-01-10',
    emailVerified: true,
    companyName: '(주)테크놀로지',
    businessNumber: '123-45-67890',
    businessAddress: '서울시 강남구 테헤란로 123'
  },
  {
    id: '2',
    name: '박파트너',
    email: 'partner1@logistics.com',
    phone: '010-9876-5432',
    role: 'partner',
    status: 'pending_approval',
    createdAt: '2024-01-12',
    emailVerified: true,
    companyName: '로지스틱 파트너스',
    businessNumber: '987-65-43210',
    businessAddress: '부산시 해운대구 마린시티',
    partnerType: 'affiliate',
    referralCode: 'REF001'
  },
  {
    id: '3',
    name: '이상사',
    email: 'enterprise2@business.co.kr',
    phone: '031-555-7777',
    role: 'enterprise',
    status: 'pending_approval',
    createdAt: '2024-01-14',
    emailVerified: false,
    companyName: '비즈니스 솔루션',
    businessNumber: '555-77-88899',
    businessAddress: '경기도 성남시 분당구 판교로 100'
  }
])

const selectedApprovals = ref<string[]>([])
const todayApproved = ref(5)
const showModal = ref(false)
const modalType = ref<'approve' | 'reject'>('approve')
const selectedUser = ref<PendingApproval | null>(null)
const rejectReason = ref('')

const allSelected = computed(() => {
  return pendingApprovals.value.length > 0 && selectedApprovals.value.length === pendingApprovals.value.length
})

const getUserInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const getRoleBadgeClass = (role: string) => {
  const classes = {
    enterprise: 'bg-purple-100 text-purple-800',
    partner: 'bg-green-100 text-green-800'
  }
  return classes[role as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getRoleText = (role: string) => {
  const texts = {
    enterprise: '기업',
    partner: '파트너'
  }
  return texts[role as keyof typeof texts] || role
}

const getPartnerTypeText = (type?: string) => {
  const texts = {
    affiliate: '제휴 파트너',
    referral: '추천 파트너',
    corporate: '기업 파트너'
  }
  return texts[type as keyof typeof texts] || type
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const getDaysAgo = (dateString: string) => {
  const days = Math.floor((new Date().getTime() - new Date(dateString).getTime()) / (1000 * 60 * 60 * 24))
  return days
}

const toggleAllSelection = () => {
  if (allSelected.value) {
    selectedApprovals.value = []
  } else {
    selectedApprovals.value = pendingApprovals.value.map(a => a.id)
  }
}

const clearSelection = () => {
  selectedApprovals.value = []
}

const batchApprove = () => {
  if (confirm(`선택한 ${selectedApprovals.value.length}개의 계정을 승인하시겠습니까?`)) {
    selectedApprovals.value.forEach(id => {
      const index = pendingApprovals.value.findIndex(a => a.id === id)
      if (index !== -1) {
        pendingApprovals.value.splice(index, 1)
      }
    })
    todayApproved.value += selectedApprovals.value.length
    selectedApprovals.value = []
    alert('선택한 계정들이 승인되었습니다.')
  }
}

const batchReject = () => {
  if (confirm(`선택한 ${selectedApprovals.value.length}개의 계정을 거부하시겠습니까?`)) {
    selectedApprovals.value.forEach(id => {
      const index = pendingApprovals.value.findIndex(a => a.id === id)
      if (index !== -1) {
        pendingApprovals.value.splice(index, 1)
      }
    })
    selectedApprovals.value = []
    alert('선택한 계정들이 거부되었습니다.')
  }
}

const showApprovalModal = (user: PendingApproval) => {
  selectedUser.value = user
  modalType.value = 'approve'
  showModal.value = true
}

const showRejectModal = (user: PendingApproval) => {
  selectedUser.value = user
  modalType.value = 'reject'
  rejectReason.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedUser.value = null
  rejectReason.value = ''
}

const confirmAction = () => {
  if (!selectedUser.value) return

  if (modalType.value === 'approve') {
    const index = pendingApprovals.value.findIndex(a => a.id === selectedUser.value!.id)
    if (index !== -1) {
      pendingApprovals.value.splice(index, 1)
      todayApproved.value++
    }
    alert(`${selectedUser.value.name} 계정이 승인되었습니다.`)
  } else {
    if (!rejectReason.value.trim()) {
      alert('거부 사유를 입력해주세요.')
      return
    }
    const index = pendingApprovals.value.findIndex(a => a.id === selectedUser.value!.id)
    if (index !== -1) {
      pendingApprovals.value.splice(index, 1)
    }
    alert(`${selectedUser.value.name} 계정이 거부되었습니다.`)
  }
  
  closeModal()
}

onMounted(() => {
  console.log('관리자 승인 관리 화면 로드됨')
})
</script>

<style scoped>
.admin-approvals {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .admin-approvals {
    padding: 1rem;
  }
}
</style>