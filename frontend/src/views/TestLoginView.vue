<template>
  <div class="min-h-screen bg-gray-50 py-12 px-4">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-3xl font-bold text-center mb-4">역할별 테스트 로그인</h1>
      
      <!-- 테스트 데이터 생성 버튼 -->
      <div class="text-center mb-8">
        <button
          @click="createTestData"
          :disabled="creatingData"
          class="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <span v-if="creatingData">테스트 데이터 생성 중...</span>
          <span v-else>테스트 데이터 생성</span>
        </button>
        <p class="text-sm text-gray-600 mt-2">
          처음 사용 시 클릭하여 Supabase에 테스트 데이터를 생성하세요
        </p>
      </div>
      
      <!-- 테스트 계정 카드 그리드 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          v-for="account in testAccounts" 
          :key="account.email"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex items-center mb-4">
            <div :class="getRoleColor(account.userType)" class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold">
              {{ account.userType.charAt(0).toUpperCase() }}
            </div>
            <div class="ml-4">
              <h3 class="font-semibold text-lg">{{ account.name }}</h3>
              <p class="text-sm text-gray-600">{{ getRoleLabel(account.userType) }}</p>
            </div>
          </div>
          
          <div class="space-y-2 mb-4">
            <div class="text-sm">
              <span class="font-medium">이메일:</span> 
              <code class="bg-gray-100 px-2 py-1 rounded">{{ account.email }}</code>
            </div>
            <div class="text-sm">
              <span class="font-medium">비밀번호:</span> 
              <code class="bg-gray-100 px-2 py-1 rounded">{{ account.password }}</code>
            </div>
          </div>
          
          <div class="border-t pt-4 mb-4">
            <h4 class="text-sm font-medium mb-2">주요 기능:</h4>
            <ul class="text-xs text-gray-600 space-y-1">
              <li v-for="feature in account.features" :key="feature" class="flex items-start">
                <span class="text-green-500 mr-1">✓</span>
                <span>{{ feature }}</span>
              </li>
            </ul>
          </div>
          
          <button
            @click="testLogin(account)"
            :disabled="loading === account.email"
            class="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span v-if="loading === account.email">로그인 중...</span>
            <span v-else>테스트 로그인</span>
          </button>
        </div>
      </div>
      
      <!-- 테스트 결과 -->
      <div v-if="testResult" class="mt-8 bg-white rounded-lg shadow-md p-6">
        <h2 class="text-xl font-semibold mb-4">테스트 결과</h2>
        <div class="space-y-2">
          <div v-if="testResult.success" class="text-green-600">
            ✅ {{ testResult.message }}
          </div>
          <div v-else class="text-red-600">
            ❌ {{ testResult.message }}
          </div>
          <div v-if="testResult.redirectPath" class="text-sm text-gray-600">
            리다이렉트 경로: <code class="bg-gray-100 px-2 py-1 rounded">{{ testResult.redirectPath }}</code>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { AuthService } from '@/services/authService'
import { createTestData as createTestDataUtil } from '@/utils/createTestData'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref<string | null>(null)
const creatingData = ref(false)
const testResult = ref<{
  success: boolean
  message: string
  redirectPath?: string
} | null>(null)

// 테스트 계정 정보
const testAccounts = [
  {
    email: 'general@test.com',
    password: 'Test123!@#',
    name: '일반사용자',
    userType: 'general',
    features: [
      '개인 주문 관리',
      '수취인 등록',
      '배송 추적',
      '견적 확인'
    ]
  },
  {
    email: 'corporate@test.com',
    password: 'Test123!@#',
    name: '기업사용자',
    userType: 'corporate',
    features: [
      '대량 주문 업로드',
      '다중 수취인 관리',
      'VAT/영세율 설정',
      '세금계산서 발행'
    ]
  },
  {
    email: 'partner@test.com', 
    password: 'Test123!@#',
    name: '파트너사용자',
    userType: 'partner',
    features: [
      '추천 고객 관리',
      '수수료 정산',
      '추천 링크 생성',
      '실적 대시보드'
    ]
  },
  {
    email: 'warehouse@test.com',
    password: 'Test123!@#',
    name: '창고관리자',
    userType: 'warehouse',
    features: [
      'QR/바코드 스캔',
      '입출고 처리',
      '체적 측정 입력',
      '라벨 출력'
    ]
  },
  {
    email: 'admin@test.com',
    password: 'Test123!@#',
    name: '시스템관리자',
    userType: 'admin',
    features: [
      '전체 시스템 관리',
      '사용자 승인/관리',
      '주문 상태 전환',
      '리포트 생성'
    ]
  }
]

// 역할별 색상
const getRoleColor = (userType: string) => {
  const colors: Record<string, string> = {
    general: 'bg-blue-500',
    corporate: 'bg-green-500',
    partner: 'bg-purple-500',
    warehouse: 'bg-orange-500',
    admin: 'bg-red-500'
  }
  return colors[userType] || 'bg-gray-500'
}

// 역할 라벨
const getRoleLabel = (userType: string) => {
  const labels: Record<string, string> = {
    general: '일반회원',
    corporate: '기업회원',
    partner: '파트너',
    warehouse: '현장관리자',
    admin: '어드민'
  }
  return labels[userType] || userType
}

// 테스트 데이터 생성
const createTestData = async () => {
  creatingData.value = true
  testResult.value = null
  
  try {
    console.log('🚀 테스트 데이터 생성 시작...')
    const result = await createTestDataUtil()
    
    if (result.success) {
      testResult.value = {
        success: true,
        message: '테스트 데이터가 성공적으로 생성되었습니다! 이제 각 역할별로 로그인 테스트를 해보세요.'
      }
    } else {
      testResult.value = {
        success: false,
        message: `테스트 데이터 생성 실패: ${result.error}`
      }
    }
  } catch (error) {
    console.error('테스트 데이터 생성 오류:', error)
    testResult.value = {
      success: false,
      message: `테스트 데이터 생성 중 오류가 발생했습니다: ${error}`
    }
  } finally {
    creatingData.value = false
  }
}

// 테스트 로그인
const testLogin = async (account: any) => {
  loading.value = account.email
  testResult.value = null
  
  try {
    // Mock 로그인 (실제 Supabase 없이 테스트)
    
    
    // 실제 테스트 사용자 ID 매핑
    const userIdMap: Record<string, string> = {
      'general': '00000000-0000-0000-0000-000000000001',
      'corporate': '00000000-0000-0000-0000-000000000002', 
      'partner': '00000000-0000-0000-0000-000000000003',
      'warehouse': '00000000-0000-0000-0000-000000000004',
      'admin': '00000000-0000-0000-0000-000000000005'
    }
    
    // 실제 사용자 프로필 설정
    const mockProfile = {
      id: userIdMap[account.userType],
      email: account.email,
      username: account.userType + '_user',
      name: account.name,
      user_type: account.userType,
      approval_status: 'approved',
      email_verified: true,
      phone: '010-0000-0000',
      terms_agreed: true,
      privacy_agreed: true,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    }
    
    // Store에 Mock 프로필 설정
    authStore.user = mockProfile as any
    
    // 역할별 리다이렉트 경로 결정
    let redirectPath = '/app/dashboard'
    switch (account.userType) {
      case 'general':
      case 'corporate':
        redirectPath = '/app/orders'
        break
      case 'partner':
        redirectPath = '/app/partner'
        break
      case 'warehouse':
        redirectPath = '/app/warehouse'
        break
      case 'admin':
        redirectPath = '/app/admin'
        break
    }
    
    testResult.value = {
      success: true,
      message: `${account.name} (${getRoleLabel(account.userType)})로 로그인 성공!`,
      redirectPath
    }
    
    // 2초 후 해당 페이지로 이동
    setTimeout(() => {
      router.push(redirectPath)
    }, 2000)
    
  } catch (error) {
    console.error('테스트 로그인 실패:', error)
    testResult.value = {
      success: false,
      message: `로그인 실패: ${error instanceof Error ? error.message : '알 수 없는 오류'}`
    }
  } finally {
    loading.value = null
  }
}
</script>