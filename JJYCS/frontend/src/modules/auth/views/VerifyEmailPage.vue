<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <div class="mx-auto h-12 w-auto flex justify-center">
          <img src="/ysc-logo.svg" alt="YSC LMS" class="h-12 w-auto" />
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Verify your email
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          We've sent a verification link to your email address
        </p>
      </div>
      
      <div class="bg-white p-6 rounded-lg shadow">
        <div class="text-center">
          <svg class="mx-auto h-12 w-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
          </svg>
          <h3 class="mt-4 text-lg font-medium text-gray-900">Check your email</h3>
          <p class="mt-2 text-sm text-gray-500">
            Please check your email and click the verification link to activate your account.
          </p>
        </div>
        
        <div class="mt-6 space-y-4">
          <button
            @click="resendEmail"
            :disabled="resending"
            class="w-full btn-secondary"
          >
            {{ resending ? 'Sending...' : 'Resend verification email' }}
          </button>
          
          <router-link to="/login" class="block w-full btn-primary text-center">
            Back to sign in
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const resending = ref(false)

const resendEmail = async () => {
  resending.value = true
  // TODO: Implement email resend logic
  setTimeout(() => {
    resending.value = false
  }, 2000)
}

onMounted(() => {
  const token = route.query.token
  if (token) {
    // TODO: Verify email with token
    console.log('Verifying email with token:', token)
  }
})
</script>