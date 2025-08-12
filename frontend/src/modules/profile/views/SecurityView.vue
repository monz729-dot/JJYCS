<template>
  <div class="security-view">
    <div class="container mx-auto p-6">
      <div class="max-w-2xl mx-auto">
        <!-- Header -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">보안 설정</h1>
          <p class="text-gray-600">계정 보안을 강화하고 비밀번호를 관리하세요.</p>
        </div>

        <!-- Password Change -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">비밀번호 변경</h2>
          
          <form @submit.prevent="changePassword">
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">현재 비밀번호</label>
                <input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">새 비밀번호</label>
                <input
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">새 비밀번호 확인</label>
                <input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>
            
            <button
              type="submit"
              class="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              비밀번호 변경
            </button>
          </form>
        </div>

        <!-- Two Factor Authentication -->
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">2단계 인증 (2FA)</h2>
          
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-medium text-gray-900">앱 기반 인증</h3>
              <p class="text-sm text-gray-600">Google Authenticator 또는 유사한 앱 사용</p>
            </div>
            <button
              @click="toggle2FA"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
            >
              {{ is2FAEnabled ? '비활성화' : '활성화' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useToast } from 'vue-toastification'

const toast = useToast()

// State
const is2FAEnabled = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Methods
const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.error('새 비밀번호가 일치하지 않습니다.')
    return
  }
  
  try {
    // TODO: Implement actual password change
    toast.success('비밀번호가 성공적으로 변경되었습니다.')
    // Clear form
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    toast.error('비밀번호 변경 중 오류가 발생했습니다.')
  }
}

const toggle2FA = async () => {
  try {
    // TODO: Implement actual 2FA toggle
    is2FAEnabled.value = !is2FAEnabled.value
    toast.success(is2FAEnabled.value ? '2FA가 활성화되었습니다.' : '2FA가 비활성화되었습니다.')
  } catch (error) {
    toast.error('2FA 설정 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.security-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>