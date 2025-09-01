<template>
  <div class="container mx-auto px-4 py-6 max-w-4xl">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">자주 묻는 질문</h1>
      <p class="mt-2 text-gray-600">고객님들이 자주 묻는 질문과 답변을 확인하세요.</p>
    </div>

    <div v-if="loading" class="text-center py-8">
      <span class="text-gray-500">FAQ를 불러오는 중...</span>
    </div>
    
    <div v-else class="space-y-4">
      <div v-for="faq in faqs" :key="faq.id" class="bg-white rounded-lg shadow">
        <button
          @click="toggleFaq(faq.id)"
          class="w-full px-4 py-4 text-left flex items-center justify-between hover:bg-gray-50"
        >
          <span class="font-medium text-gray-900">{{ faq.question }}</span>
          <svg 
            class="w-5 h-5 text-gray-500 transition-transform"
            :class="{ 'rotate-180': openFaqs.includes(faq.id) }"
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
        <div v-if="openFaqs.includes(faq.id)" class="px-4 pb-4 text-gray-600">
          <div class="pt-2 border-t">{{ faq.answer }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const openFaqs = ref<number[]>([])
const faqs = ref<any[]>([])
const loading = ref(true)

const toggleFaq = (id: number) => {
  const index = openFaqs.value.indexOf(id)
  if (index > -1) {
    openFaqs.value.splice(index, 1)
  } else {
    openFaqs.value.push(id)
  }
}

const fetchFaqs = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/faqs')
    if (response.data.success) {
      faqs.value = response.data.data
    } else {
      showToast('FAQ 목록을 불러오는데 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('FAQ 로딩 오류:', error)
    showToast('FAQ 목록을 불러오는데 실패했습니다.', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFaqs()
})
</script>

<style scoped>
</style>