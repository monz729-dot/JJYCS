<template>
  <div class="container mx-auto px-4 py-6 max-w-4xl">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">자주 묻는 질문</h1>
      <p class="mt-2 text-gray-600">고객님들이 자주 묻는 질문과 답변을 확인하세요.</p>
    </div>

    <div class="space-y-4">
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
import { ref } from 'vue'

const openFaqs = ref<number[]>([])

const toggleFaq = (id: number) => {
  const index = openFaqs.value.indexOf(id)
  if (index > -1) {
    openFaqs.value.splice(index, 1)
  } else {
    openFaqs.value.push(id)
  }
}

// 샘플 데이터
const faqs = ref([
  {
    id: 1,
    question: '배송 기간은 얼마나 걸리나요?',
    answer: '일반적으로 태국에서 한국까지 항공 배송은 3-5일, 해상 배송은 15-20일이 소요됩니다. 통관 절차에 따라 추가 시간이 소요될 수 있습니다.'
  },
  {
    id: 2,
    question: '배송료는 어떻게 계산되나요?',
    answer: '배송료는 상품의 무게와 부피(CBM)를 기준으로 계산됩니다. 실측 무게와 부피 무게 중 더 큰 값을 기준으로 요금이 책정됩니다.'
  },
  {
    id: 3,
    question: '통관 서류는 어떻게 준비하나요?',
    answer: '저희가 통관에 필요한 모든 서류를 대행 처리해드립니다. 고객님은 상품 정보와 수취인 정보만 정확히 입력해주시면 됩니다.'
  },
  {
    id: 4,
    question: '배송 상태는 어떻게 확인하나요?',
    answer: '마이페이지에서 주문번호를 통해 실시간으로 배송 상태를 확인할 수 있습니다. 주요 진행 상황은 이메일과 SMS로도 안내드립니다.'
  },
  {
    id: 5,
    question: '파손이나 분실 시 보상은 어떻게 되나요?',
    answer: '배송 중 파손이나 분실이 발생한 경우, 보험 가입 여부에 따라 보상이 진행됩니다. 자세한 보상 기준은 고객센터로 문의해주세요.'
  }
])
</script>

<style scoped>
</style>