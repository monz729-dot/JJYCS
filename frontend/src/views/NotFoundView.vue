<template>
  <div class="not-found">
    <div class="error-container">
      <div class="error-icon">
        <ExclamationTriangleIcon class="icon" />
      </div>
      
      <h1 class="error-title">404 - 페이지를 찾을 수 없습니다</h1>
      <p class="error-description">
        요청하신 페이지가 존재하지 않거나 이동되었습니다.
      </p>
      
      <div class="error-actions">
        <router-link :to="{ name: 'Dashboard' }" class="btn-primary">
          <HomeIcon class="btn-icon" />
          홈으로 돌아가기
        </router-link>
        
        <button type="button" class="btn-secondary" @click="goBack">
          <ArrowLeftIcon class="btn-icon" />
          이전 페이지
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  ExclamationTriangleIcon,
  HomeIcon,
  ArrowLeftIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push({ name: 'Dashboard' })
  }
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.error-container {
  text-align: center;
  padding: 2rem;
  max-width: 400px;
}

.error-icon {
  width: 5rem;
  height: 5rem;
  background: #fef3c7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 2rem;
}

.icon {
  width: 2.5rem;
  height: 2.5rem;
  color: #f59e0b;
}

.error-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.error-description {
  color: #6b7280;
  margin: 0 0 2rem 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.btn-primary,
.btn-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-secondary {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #f9fafb;
}

.btn-icon {
  width: 1rem;
  height: 1rem;
}
</style>