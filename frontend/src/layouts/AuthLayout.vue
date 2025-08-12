<template>
  <div class="auth-layout min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo/Brand -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">YCS LMS</h1>
        <p class="text-gray-600">물류관리 시스템</p>
      </div>
      
      <!-- Main Content -->
      <div class="bg-white rounded-lg shadow-lg p-8">
        <router-view />
      </div>
      
      <!-- Footer -->
      <div class="text-center mt-8">
        <p class="text-sm text-gray-500">
          © 2024 YCS. All rights reserved.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Authentication layout for login, register, etc.
</script>

<style scoped>
.auth-layout {
  background-image: 
    linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(147, 51, 234, 0.1) 100%),
    url('data:image/svg+xml,<svg width="60" height="60" viewBox="0 0 60 60" xmlns="http://www.w3.org/2000/svg"><g fill="none" fill-rule="evenodd"><g fill="%239C92AC" fill-opacity="0.05" fill-rule="nonzero"><circle cx="30" cy="30" r="2"/></g></g></svg>');
}
</style>