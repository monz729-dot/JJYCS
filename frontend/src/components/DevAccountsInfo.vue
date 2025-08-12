<template>
  <div v-if="showDevInfo" class="fixed top-4 right-4 z-50">
    <div class="bg-gray-800 text-white p-4 rounded-lg shadow-lg max-w-md">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-sm font-semibold">🚀 개발용 계정</h3>
        <button @click="toggleInfo" class="text-gray-400 hover:text-white">
          <ChevronUpIcon v-if="expanded" class="h-4 w-4" />
          <ChevronDownIcon v-else class="h-4 w-4" />
        </button>
      </div>
      
      <div v-if="expanded" class="space-y-3 text-xs">
        <div class="border-b border-gray-600 pb-2">
          <div class="font-semibold text-blue-300">일반 사용자</div>
          <div class="font-mono">user@ycs.com / password123</div>
          <div class="text-gray-400">회원코드: USR001</div>
        </div>
        
        <div class="border-b border-gray-600 pb-2">
          <div class="font-semibold text-green-300">기업 사용자</div>
          <div class="font-mono">enterprise@ycs.com / password123</div>
          <div class="text-gray-400">회원코드: ENT001 (승인완료)</div>
        </div>
        
        <div class="border-b border-gray-600 pb-2">
          <div class="font-semibold text-purple-300">파트너</div>
          <div class="font-mono">partner@ycs.com / password123</div>
          <div class="text-gray-400">회원코드: PTN001 (승인완료)</div>
        </div>
        
        <div class="border-b border-gray-600 pb-2">
          <div class="font-semibold text-yellow-300">창고 관리자</div>
          <div class="font-mono">warehouse@ycs.com / password123</div>
          <div class="text-gray-400">회원코드: WHS001</div>
        </div>
        
        <div class="pb-2">
          <div class="font-semibold text-red-300">시스템 관리자</div>
          <div class="font-mono">admin@ycs.com / password123</div>
          <div class="text-gray-400">회원코드: ADM001</div>
        </div>
        
        <div class="text-center pt-2 border-t border-gray-600">
          <button 
            @click="fillLogin('user@ycs.com')" 
            class="text-blue-300 hover:text-blue-100 mr-2"
          >일반</button>
          <button 
            @click="fillLogin('enterprise@ycs.com')" 
            class="text-green-300 hover:text-green-100 mr-2"
          >기업</button>
          <button 
            @click="fillLogin('warehouse@ycs.com')" 
            class="text-yellow-300 hover:text-yellow-100 mr-2"
          >창고</button>
          <button 
            @click="fillLogin('admin@ycs.com')" 
            class="text-red-300 hover:text-red-100"
          >관리자</button>
        </div>
      </div>
      
      <div v-else class="text-xs text-gray-400">
        클릭하여 개발용 계정 정보 보기
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ChevronUpIcon, ChevronDownIcon } from '@heroicons/vue/24/outline'

const showDevInfo = ref(false)
const expanded = ref(false)

onMounted(() => {
  // Only show in development mode
  showDevInfo.value = import.meta.env.DEV && import.meta.env.VITE_DEV_ACCOUNTS_INFO === 'true'
})

const toggleInfo = () => {
  expanded.value = !expanded.value
}

const fillLogin = (email: string) => {
  // Find login form inputs and fill them
  const emailInput = document.querySelector('input[type="email"]') as HTMLInputElement
  const passwordInput = document.querySelector('input[type="password"]') as HTMLInputElement
  
  if (emailInput && passwordInput) {
    emailInput.value = email
    passwordInput.value = 'password123'
    
    // Trigger input events to notify Vue
    emailInput.dispatchEvent(new Event('input', { bubbles: true }))
    passwordInput.dispatchEvent(new Event('input', { bubbles: true }))
    
    // Focus on the submit button
    const submitButton = document.querySelector('button[type="submit"]') as HTMLButtonElement
    if (submitButton) {
      submitButton.focus()
    }
  }
}
</script>

<style scoped>
/* Component specific styles */
</style>