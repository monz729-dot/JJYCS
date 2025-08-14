<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="text-center">
        <!-- 성공 아이콘 -->
        <div class="mx-auto h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mb-6">
          <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
        </div>
        
        <!-- 제목 -->
        <h1 class="text-2xl font-bold text-gray-900 mb-4">
          이메일 인증이 완료되었습니다!
        </h1>
        
        <!-- 설명 -->
        <div class="bg-white shadow-lg rounded-lg p-6">
          <p class="text-gray-600 mb-4">
            이메일 인증이 성공적으로 완료되었습니다.
          </p>
          <p class="text-gray-600 mb-6">
            이제 회원가입 페이지로 돌아가서 <strong>"무료로 시작하기"</strong> 버튼을 다시 눌러주세요.
          </p>
          
          <!-- 안내 박스 -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-blue-800">
                  다음 단계
                </h3>
                <div class="mt-2 text-sm text-blue-700">
                  <p>1. 이 창을 닫으세요</p>
                  <p>2. 회원가입 페이지로 돌아가세요</p>
                  <p>3. "무료로 시작하기" 버튼을 다시 클릭하세요</p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 버튼들 -->
          <div class="space-y-3">
            <button
              @click="goToSignup"
              class="w-full py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
            >
              회원가입 페이지로 이동
            </button>
            
            <button
              @click="closeWindow"
              class="w-full py-3 px-4 border border-gray-300 text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
            >
              창 닫기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()

// 이메일 인증 처리
onMounted(async () => {
  try {
    // URL에서 인증 토큰 확인
    const urlParams = new URLSearchParams(window.location.search)
    const token = urlParams.get('token')
    const type = urlParams.get('type')
    
    if (token && type === 'signup') {
      // 이메일 인증 성공 메시지
      toast.success('이메일 인증이 완료되었습니다!')
    }
  } catch (error) {
    console.error('Auth callback error:', error)
    toast.error('인증 처리 중 오류가 발생했습니다.')
  }
})

// 회원가입 페이지로 이동
const goToSignup = () => {
  router.push({ 
    name: 'Register',
    query: { 
      emailVerified: 'true',
      message: '이메일 인증이 완료되었습니다. 회원가입을 완료해주세요.' 
    }
  })
}

// 창 닫기
const closeWindow = () => {
  window.close()
  // 창이 닫히지 않는 경우를 대비해 회원가입 페이지로 리다이렉트
  router.push({ name: 'Register' })
}
</script>

<style scoped>
/* Custom styles */
</style>
