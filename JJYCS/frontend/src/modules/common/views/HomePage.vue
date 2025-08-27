<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100">
    <div class="container mx-auto px-4 py-12">
      <div class="text-center">
        <div class="mx-auto h-20 w-auto flex justify-center mb-8">
          <img src="/ycs-logo.svg" alt="YCS LMS" class="h-20 w-auto" />
        </div>
        
        <h1 class="text-4xl md:text-6xl font-bold text-gray-900 mb-4">
          YCS <span class="text-primary-600">LMS</span>
        </h1>
        
        <p class="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          Comprehensive Logistics Management System for seamless order processing, 
          warehouse management, and partner collaboration.
        </p>
        
        <div class="flex flex-col sm:flex-row gap-4 justify-center mb-12">
          <router-link to="/login" class="btn-primary px-8 py-3 text-lg">
            Sign In
          </router-link>
          <router-link to="/register" class="btn-secondary px-8 py-3 text-lg">
            Get Started
          </router-link>
        </div>
      </div>
      
      <!-- Features Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-16">
        <div class="card text-center">
          <div class="mx-auto w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Order Management</h3>
          <p class="text-gray-600">Streamlined order processing with automated CBM calculations and smart shipping type selection.</p>
        </div>
        
        <div class="card text-center">
          <div class="mx-auto w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M12 12h-4.01M12 12v4m6-4h2M6 12H4m6 0c0-.65.13-1.27.36-1.84"></path>
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Smart Warehouse</h3>
          <p class="text-gray-600">QR code scanning, automated location assignment, and real-time inventory tracking.</p>
        </div>
        
        <div class="card text-center">
          <div class="mx-auto w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center mb-4">
            <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Partner Network</h3>
          <p class="text-gray-600">Comprehensive partner management with referral tracking and commission settlements.</p>
        </div>
      </div>
      
      <!-- Business Rules Highlights -->
      <div class="mt-16 bg-white rounded-lg shadow-sm p-8">
        <h2 class="text-2xl font-bold text-center text-gray-900 mb-8">Smart Business Logic</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="text-center">
            <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <span class="text-2xl font-bold text-blue-600">CBM</span>
            </div>
            <h4 class="font-medium text-gray-900 mb-1">Auto CBM Calculation</h4>
            <p class="text-sm text-gray-600">Automatic cubic meter calculation with sea-to-air conversion at 29+ CBM</p>
          </div>
          
          <div class="text-center">
            <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <span class="text-lg font-bold text-yellow-600">₿1,500</span>
            </div>
            <h4 class="font-medium text-gray-900 mb-1">THB Threshold</h4>
            <p class="text-sm text-gray-600">Smart alerts for items exceeding THB 1,500 value limit</p>
          </div>
          
          <div class="text-center">
            <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <h4 class="font-medium text-gray-900 mb-1">EMS/HS Validation</h4>
            <p class="text-sm text-gray-600">Real-time validation against Korea Trade-Investment Promotion Agency</p>
          </div>
          
          <div class="text-center">
            <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <h4 class="font-medium text-gray-900 mb-1">Member Code Check</h4>
            <p class="text-sm text-gray-600">Automated delay handling for missing member codes</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  document.title = 'YCS LMS - Logistics Management System'
})
</script>