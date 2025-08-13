<template>
  <div class="max-w-6xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">사용자 승인 관리</h2>
      
      <!-- 필터 -->
      <div class="mb-6">
        <div class="flex gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">승인 상태</label>
            <select v-model="filter.status" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">전체</option>
              <option value="pending">승인 대기</option>
              <option value="approved">승인 완료</option>
              <option value="rejected">승인 거절</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">회원 종류</label>
            <select v-model="filter.userType" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">전체</option>
              <option value="corporate">기업회원</option>
              <option value="partner">파트너회원</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">검색</label>
            <input
              v-model="filter.search"
              type="text"
              placeholder="이름, 이메일, 아이디로 검색"
              class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </div>

      <!-- 통계 카드 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div class="bg-blue-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-blue-600">{{ stats.total }}</div>
          <div class="text-sm text-gray-600">전체 사용자</div>
        </div>
        <div class="bg-yellow-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-yellow-600">{{ stats.pending }}</div>
          <div class="text-sm text-gray-600">승인 대기</div>
        </div>
        <div class="bg-green-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-green-600">{{ stats.approved }}</div>
          <div class="text-sm text-gray-600">승인 완료</div>
        </div>
        <div class="bg-red-50 p-4 rounded-lg">
          <div class="text-2xl font-bold text-red-600">{{ stats.rejected }}</div>
          <div class="text-sm text-gray-600">승인 거절</div>
        </div>
      </div>

      <!-- 사용자 목록 -->
      <div class="overflow-x-auto">
        <table class="w-full min-w-[800px]">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-700">사용자 정보</th>
              <th class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-700">회원 종류</th>
              <th class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-700">승인 상태</th>
              <th class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-700">가입일</th>
              <th class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-700">사업자등록증</th>
              <th class="px-4 py-3 text-center text-xs font-medium uppercase tracking-wider text-gray-700">액션</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-4 py-4">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ user.name }}</div>
                  <div class="text-sm text-gray-500">{{ user.email }}</div>
                  <div class="text-sm text-gray-500">@{{ user.username }}</div>
                </div>
              </td>
              <td class="px-4 py-4">
                <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="{
                  'bg-blue-100 text-blue-800': user.user_type === 'corporate',
                  'bg-purple-100 text-purple-800': user.user_type === 'partner'
                }">
                  {{ getUserTypeLabel(user.user_type) }}
                </span>
              </td>
              <td class="px-4 py-4">
                <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full" :class="{
                  'bg-yellow-100 text-yellow-800': user.approval_status === 'pending',
                  'bg-green-100 text-green-800': user.approval_status === 'approved',
                  'bg-red-100 text-red-800': user.approval_status === 'rejected'
                }">
                  {{ getApprovalStatusLabel(user.approval_status) }}
                </span>
              </td>
              <td class="px-4 py-4 text-sm text-gray-500">
                {{ formatDate(user.created_at) }}
              </td>
              <td class="px-4 py-4">
                <a
                  v-if="user.business_license_url"
                  :href="user.business_license_url"
                  target="_blank"
                  class="text-blue-600 hover:text-blue-800 text-sm"
                >
                  보기
                </a>
                <span v-else class="text-gray-400 text-sm">없음</span>
              </td>
              <td class="px-4 py-4 text-center">
                <div v-if="user.approval_status === 'pending'" class="flex gap-2 justify-center">
                  <button
                    @click="approveUser(user.id)"
                    :disabled="loading"
                    class="px-3 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700 disabled:opacity-50"
                  >
                    승인
                  </button>
                  <button
                    @click="rejectUser(user.id)"
                    :disabled="loading"
                    class="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 disabled:opacity-50"
                  >
                    거절
                  </button>
                </div>
                <div v-else class="text-sm text-gray-500">
                  {{ user.approval_status === 'approved' ? '승인됨' : '거절됨' }}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 로딩 상태 -->
      <div v-if="loading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <div class="mt-2 text-gray-600">로딩 중...</div>
      </div>

      <!-- 빈 상태 -->
      <div v-if="!loading && filteredUsers.length === 0" class="text-center py-8">
        <div class="text-gray-500">해당하는 사용자가 없습니다.</div>
      </div>
    </div>

    <!-- 거절 사유 모달 -->
    <div v-if="showRejectModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-semibold mb-4">거절 사유</h3>
        <textarea
          v-model="rejectReason"
          placeholder="거절 사유를 입력하세요"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 h-32"
        ></textarea>
        <div class="flex gap-2 mt-4">
          <button
            @click="confirmReject"
            :disabled="!rejectReason.trim()"
            class="flex-1 bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 disabled:opacity-50"
          >
            거절
          </button>
          <button
            @click="showRejectModal = false"
            class="flex-1 bg-gray-500 text-white py-2 px-4 rounded-md hover:bg-gray-600"
          >
            취소
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { supabase } from '@/lib/supabase'
import type { UserProfile } from '@/lib/supabase'

const toast = useToast()

// 상태
const loading = ref(false)
const users = ref<UserProfile[]>([])
const showRejectModal = ref(false)
const rejectReason = ref('')
const selectedUserId = ref('')

// 필터
const filter = reactive({
  status: '',
  userType: '',
  search: ''
})

// 통계
const stats = computed(() => {
  const total = users.value.length
  const pending = users.value.filter(u => u.approval_status === 'pending').length
  const approved = users.value.filter(u => u.approval_status === 'approved').length
  const rejected = users.value.filter(u => u.approval_status === 'rejected').length

  return { total, pending, approved, rejected }
})

// 필터링된 사용자 목록
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    // 승인 상태 필터
    if (filter.status && user.approval_status !== filter.status) {
      return false
    }

    // 회원 종류 필터
    if (filter.userType && user.user_type !== filter.userType) {
      return false
    }

    // 검색 필터
    if (filter.search) {
      const searchTerm = filter.search.toLowerCase()
      return (
        user.name.toLowerCase().includes(searchTerm) ||
        user.email.toLowerCase().includes(searchTerm) ||
        user.username.toLowerCase().includes(searchTerm)
      )
    }

    return true
  })
})

// 사용자 목록 조회
const fetchUsers = async () => {
  loading.value = true
  try {
    const { data, error } = await supabase
      .from('user_profiles')
      .select('*')
      .order('created_at', { ascending: false })

    if (error) {
      throw new Error(error.message)
    }

    users.value = data || []
  } catch (error) {
    toast.error('사용자 목록을 불러오는데 실패했습니다.')
    console.error('Error fetching users:', error)
  } finally {
    loading.value = false
  }
}

// 사용자 승인
const approveUser = async (userId: string) => {
  loading.value = true
  try {
    const { error } = await supabase
      .from('user_profiles')
      .update({ 
        approval_status: 'approved',
        updated_at: new Date().toISOString()
      })
      .eq('id', userId)

    if (error) {
      throw new Error(error.message)
    }

    toast.success('사용자가 승인되었습니다.')
    await fetchUsers()
  } catch (error) {
    toast.error('사용자 승인에 실패했습니다.')
    console.error('Error approving user:', error)
  } finally {
    loading.value = false
  }
}

// 사용자 거절
const rejectUser = (userId: string) => {
  selectedUserId.value = userId
  showRejectModal.value = true
}

// 거절 확인
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    toast.error('거절 사유를 입력해주세요.')
    return
  }

  loading.value = true
  try {
    const { error } = await supabase
      .from('user_profiles')
      .update({ 
        approval_status: 'rejected',
        updated_at: new Date().toISOString()
      })
      .eq('id', selectedUserId.value)

    if (error) {
      throw new Error(error.message)
    }

    toast.success('사용자가 거절되었습니다.')
    showRejectModal.value = false
    rejectReason.value = ''
    selectedUserId.value = ''
    await fetchUsers()
  } catch (error) {
    toast.error('사용자 거절에 실패했습니다.')
    console.error('Error rejecting user:', error)
  } finally {
    loading.value = false
  }
}

// 유틸리티 함수들
const getUserTypeLabel = (userType: string) => {
  switch (userType) {
    case 'corporate': return '기업회원'
    case 'partner': return '파트너회원'
    default: return userType
  }
}

const getApprovalStatusLabel = (status: string) => {
  switch (status) {
    case 'pending': return '승인 대기'
    case 'approved': return '승인 완료'
    case 'rejected': return '승인 거절'
    default: return status
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 컴포넌트 마운트 시 사용자 목록 조회
onMounted(() => {
  fetchUsers()
})
</script>
