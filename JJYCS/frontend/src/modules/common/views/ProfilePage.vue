<template>
  <div class="min-h-screen bg-gray-50">
    <AppLayout>
      <div class="max-w-4xl mx-auto py-6">
        <div class="bg-white shadow rounded-lg p-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-6">프로필</h1>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
              <input 
                v-model="profile.name"
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                readonly
              >
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
              <input 
                v-model="profile.email"
                type="email" 
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                readonly
              >
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
              <input 
                v-model="profile.phone"
                type="text" 
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">사용자 역할</label>
              <span class="inline-flex px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
                {{ profile.userType }}
              </span>
            </div>
          </div>
          
          <div class="mt-6">
            <button 
              @click="updateProfile"
              class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
            >
              프로필 업데이트
            </button>
          </div>
        </div>
      </div>
    </AppLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/ui/AppLayout.vue'

const authStore = useAuthStore()
const { addToast } = useToast()

const profile = ref({
  name: '',
  email: '',
  phone: '',
  userType: ''
})

const updateProfile = () => {
  addToast({
    type: 'success',
    title: '프로필 업데이트',
    message: '프로필이 성공적으로 업데이트되었습니다.'
  })
}

onMounted(() => {
  if (authStore.user) {
    profile.value = {
      name: authStore.user.name || '',
      email: authStore.user.email || '',
      phone: authStore.user.phone || '',
      userType: authStore.user.userType || ''
    }
  }
})
</script>