<template>
  <div v-if="referralInfo" class="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-8">
    <div class="flex items-center justify-center mb-4">
      <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mr-4">
        <UserIcon class="w-6 h-6 text-blue-600" />
      </div>
      <div>
        <h3 class="font-semibold text-blue-900">{{ referralInfo.partnerName }}님이 초대했습니다</h3>
        <p class="text-sm text-blue-600">파트너 추천을 통한 특별 혜택</p>
      </div>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="text-center p-4 bg-white rounded-lg">
        <div class="text-2xl font-bold text-blue-600">10%</div>
        <div class="text-sm text-gray-600">수수료 할인</div>
      </div>
      <div class="text-center p-4 bg-white rounded-lg">
        <div class="text-2xl font-bold text-green-600">무료</div>
        <div class="text-sm text-gray-600">첫 3개월 기본료</div>
      </div>
      <div class="text-center p-4 bg-white rounded-lg">
        <div class="text-2xl font-bold text-purple-600">전담</div>
        <div class="text-sm text-gray-600">고객 지원</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UserIcon } from '@heroicons/vue/24/outline'

const props = defineProps<{ referralInfo: { partnerName: string; partnerCode: string } | null }>()
</script>
