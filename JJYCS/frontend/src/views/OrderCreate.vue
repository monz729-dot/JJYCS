<template>
  <div class="create-order-page">
    <div class="page-header">
      <h1 class="page-title">새 주문 등록</h1>
      <p class="page-description">
        주문할 품목들을 등록하고 제출하세요. 임시저장 후 나중에 수정할 수도 있습니다.
      </p>
    </div>
    
    <OrderForm />
  </div>
</template>

<script setup>
import OrderForm from '@/components/forms/OrderForm.vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  // 로그인 체크
  if (!authStore.user) {
    router.push('/login')
    return
  }
  
  // 권한 체크 - 일반/기업/파트너만 주문 생성 가능
  const allowedRoles = ['GENERAL', 'CORPORATE', 'PARTNER']
  if (!allowedRoles.includes(authStore.user.userType)) {
    alert('주문 생성 권한이 없습니다.')
    router.push('/dashboard')
    return
  }
})
</script>

<style scoped>
.create-order-page {
  min-height: calc(100vh - 64px);
  background: #f8f9fa;
  padding: 20px;
}

.page-header {
  max-width: 800px;
  margin: 0 auto 30px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .create-order-page {
    padding: 10px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-description {
    font-size: 14px;
  }
}
</style>