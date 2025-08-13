<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <!-- Background Pattern -->
    <div class="absolute inset-0 bg-white/50 backdrop-blur-[2px]"></div>
    <div class="absolute top-0 left-0 w-full h-full">
      <div class="absolute top-10 left-10 w-72 h-72 bg-blue-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
      <div class="absolute top-10 right-10 w-72 h-72 bg-purple-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
      <div class="absolute bottom-10 left-20 w-72 h-72 bg-pink-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
    </div>
    
    <div class="relative max-w-4xl mx-auto">
      <div class="bg-white/80 backdrop-blur-md rounded-2xl shadow-2xl border border-white/50 p-8">
        <!-- Header -->
        <div class="text-center mb-8">
          <div class="mx-auto w-16 h-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl flex items-center justify-center mb-4 shadow-lg">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900 mb-2">회원가입</h1>
          <p class="text-gray-600 text-sm">안전하고 빠른 글로벌 배송 서비스를 시작하세요</p>
        </div>

        <!-- Registration Form -->
        <form @submit.prevent="handleSubmit" class="space-y-8">
          <!-- User Type Selection -->
          <div class="space-y-4">
            <h3 class="text-lg font-semibold text-gray-900 text-center">회원 유형 선택</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                v-for="type in userTypes"
                :key="type.value"
                @click="form.user_type = type.value"
                :class="[
                  'p-6 border-2 rounded-2xl cursor-pointer transition-all duration-300 hover:shadow-lg',
                  form.user_type === type.value
                    ? 'border-blue-500 bg-blue-50 shadow-lg transform scale-105'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                ]"
              >
                <div class="text-center">
                  <div class="w-12 h-12 mx-auto mb-3 rounded-xl flex items-center justify-center"
                       :class="form.user_type === type.value ? 'bg-blue-100' : 'bg-gray-100'">
                    <svg class="w-6 h-6" :class="form.user_type === type.value ? 'text-blue-600' : 'text-gray-600'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path v-if="type.value === 'general'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      <path v-else-if="type.value === 'corporate'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                      <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                  </div>
                  <div class="text-lg font-semibold text-gray-900 mb-2">{{ type.label }}</div>
                  <div class="text-sm text-gray-600">{{ type.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Basic Information -->
          <div class="space-y-6">
            <h3 class="text-lg font-semibold text-gray-900">기본 정보</h3>
            
            <!-- Username -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700 flex items-center">
                아이디 <span class="text-red-500 ml-1">*</span>
                <span v-if="usernameChecking" class="ml-2 text-xs text-blue-600">확인중...</span>
                <span v-else-if="usernameAvailable" class="ml-2 text-xs text-green-600">✓ 사용가능</span>
                <span v-else-if="usernameError" class="ml-2 text-xs text-red-600">✗ 사용불가</span>
              </label>
              <div class="flex gap-2">
                <input
                  v-model="form.username"
                  type="text"
                  required
                  class="flex-1 px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  :class="{
                    'border-red-300': usernameError || fieldErrors.username,
                    'border-green-300': usernameAvailable && !fieldErrors.username
                  }"
                  placeholder="아이디를 입력하세요"
                  @blur="validateAndCheckUsername"
                  @input="onUsernameInput"
                />
                <button
                  type="button"
                  @click="checkUsername"
                  :disabled="!form.username || usernameChecking || fieldErrors.username"
                  class="px-4 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {{ usernameChecking ? '확인중...' : '중복확인' }}
                </button>
              </div>
              <p v-if="fieldErrors.username" class="text-xs text-red-600 mt-1">{{ fieldErrors.username }}</p>
            </div>
            
            <!-- Email -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700 flex items-center">
                이메일 <span class="text-red-500 ml-1">*</span>
                <span v-if="emailVerified" class="ml-2 text-xs text-green-600">✓ 인증완료</span>
                <span v-else-if="emailSent" class="ml-2 text-xs text-blue-600">인증코드 발송됨</span>
              </label>
              <div class="flex gap-2">
                <input
                  v-model="form.email"
                  type="email"
                  required
                  :disabled="emailVerified"
                  class="flex-1 px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  :class="{ 
                    'border-red-300': fieldErrors.email,
                    'border-green-300': emailVerified,
                    'bg-gray-50': emailVerified
                  }"
                  placeholder="이메일을 입력하세요"
                  @blur="validateEmail"
                />
                <button
                  v-if="!emailVerified"
                  type="button"
                  @click="sendVerificationEmail"
                  :disabled="!form.email || fieldErrors.email || emailVerifying"
                  class="px-4 py-3 bg-green-600 text-white rounded-xl hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors whitespace-nowrap"
                >
                  {{ emailVerifying ? '발송중...' : emailSent ? '재발송' : '인증발송' }}
                </button>
              </div>
              <p v-if="fieldErrors.email" class="text-xs text-red-600 mt-1">{{ fieldErrors.email }}</p>
            </div>
            
            <!-- Email Verification Code (only show when email is sent) -->
            <div v-if="emailSent && !emailVerified" class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                인증코드 <span class="text-red-500">*</span>
              </label>
              <div class="flex gap-2">
                <input
                  v-model="verificationCode"
                  type="text"
                  required
                  class="flex-1 px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  placeholder="6자리 인증코드를 입력하세요"
                  maxlength="6"
                />
                <button
                  type="button"
                  @click="verifyEmail"
                  :disabled="!verificationCode || verificationCode.length !== 6 || emailVerifying"
                  class="px-4 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {{ emailVerifying ? '확인중...' : '인증확인' }}
                </button>
              </div>
              <p class="text-xs text-gray-500">이메일로 발송된 6자리 인증코드를 입력해주세요.</p>
            </div>
            
            <!-- Password -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">비밀번호 <span class="text-red-500">*</span></label>
              <div class="relative">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  class="w-full px-4 py-3 pr-12 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                  :class="{ 'border-red-300': fieldErrors.password }"
                  placeholder="8자 이상 입력하세요"
                  @blur="validatePassword"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-blue-600 transition-colors"
                >
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path v-if="!showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path v-if="!showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                  </svg>
                </button>
              </div>
              <p v-if="fieldErrors.password" class="text-xs text-red-600 mt-1">{{ fieldErrors.password }}</p>
              <p v-else class="text-xs text-gray-500">대소문자, 숫자, 특수문자 포함 8자 이상</p>
            </div>
            
            <!-- Confirm Password -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">비밀번호 확인 <span class="text-red-500">*</span></label>
              <input
                v-model="form.passwordConfirm"
                type="password"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.passwordConfirm || passwordMismatch }"
                placeholder="비밀번호를 다시 입력하세요"
                @blur="validatePasswordConfirm"
              />
              <p v-if="fieldErrors.passwordConfirm" class="text-xs text-red-600 mt-1">{{ fieldErrors.passwordConfirm }}</p>
            </div>
            
            <!-- Name -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">이름 <span class="text-red-500">*</span></label>
              <input
                v-model="form.name"
                type="text"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.name }"
                placeholder="이름을 입력하세요"
                @blur="validateName"
              />
              <p v-if="fieldErrors.name" class="text-xs text-red-600 mt-1">{{ fieldErrors.name }}</p>
            </div>
            
            <!-- Phone -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">연락처</label>
              <input
                v-model="form.phone"
                type="tel"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.phone }"
                placeholder="010-1234-5678"
                @blur="validatePhone"
              />
              <p v-if="fieldErrors.phone" class="text-xs text-red-600 mt-1">{{ fieldErrors.phone }}</p>
            </div>
            
            <!-- Address -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">주소</label>
              <input
                v-model="form.address"
                type="text"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                placeholder="주소를 입력하세요"
              />
            </div>
          </div>

          <!-- Corporate/Partner Additional Fields -->
          <div v-if="form.user_type !== 'general'" class="space-y-6">
            <h3 class="text-lg font-semibold text-gray-900">추가 정보</h3>
            
            <!-- Manager Name -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                담당자명 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.manager_name"
                type="text"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.manager_name }"
                placeholder="담당자 이름을 입력하세요"
                @blur="validateManagerName"
              />
              <p v-if="fieldErrors.manager_name" class="text-xs text-red-600 mt-1">{{ fieldErrors.manager_name }}</p>
            </div>
            
            <!-- Manager Contact -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                담당자 연락처 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.manager_contact"
                type="tel"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.manager_contact }"
                placeholder="010-1234-5678"
                @blur="validateManagerContact"
              />
              <p v-if="fieldErrors.manager_contact" class="text-xs text-red-600 mt-1">{{ fieldErrors.manager_contact }}</p>
            </div>

            <!-- Company Name (for corporate) or Organization (for partner) -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                {{ form.user_type === 'corporate' ? '회사명' : '소속/단체명' }} <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.company_name"
                type="text"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.company_name }"
                :placeholder="form.user_type === 'corporate' ? '회사명을 입력하세요' : '소속 단체명을 입력하세요'"
                @blur="validateCompanyName"
              />
              <p v-if="fieldErrors.company_name" class="text-xs text-red-600 mt-1">{{ fieldErrors.company_name }}</p>
            </div>

            <!-- Business Number (for corporate only) -->
            <div v-if="form.user_type === 'corporate'" class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                사업자등록번호 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.business_number"
                type="text"
                required
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                :class="{ 'border-red-300': fieldErrors.business_number }"
                placeholder="000-00-00000"
                maxlength="12"
                @blur="validateBusinessNumber"
              />
              <p v-if="fieldErrors.business_number" class="text-xs text-red-600 mt-1">{{ fieldErrors.business_number }}</p>
            </div>
            
            <!-- Business License Upload -->
            <div class="space-y-2">
              <label class="text-sm font-medium text-gray-700">
                {{ form.user_type === 'corporate' ? '사업자등록증' : '신분증 또는 증명서' }} <span class="text-red-500">*</span>
              </label>
              <div class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-200 border-dashed rounded-xl hover:border-gray-300 transition-colors"
                   :class="{ 'border-green-300 bg-green-50': form.business_license_url }">
                <div class="space-y-1 text-center">
                  <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                    <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <div v-if="!form.business_license_url" class="flex text-sm text-gray-600">
                    <label for="business-license" class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                      <span>파일을 업로드하거나</span>
                      <input id="business-license" name="business-license" type="file" class="sr-only" accept=".pdf,.jpg,.jpeg,.png" @change="handleFileUpload" />
                    </label>
                    <p class="pl-1">드래그 앤 드롭하세요</p>
                  </div>
                  <div v-else class="text-sm text-green-600">
                    <svg class="mx-auto h-8 w-8 text-green-500 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <p class="font-medium">파일이 업로드되었습니다</p>
                    <button type="button" @click="clearFile" class="text-xs text-blue-600 hover:text-blue-700 underline mt-1">
                      다시 업로드
                    </button>
                  </div>
                  <p class="text-xs text-gray-500">PDF, PNG, JPG 파일 (최대 10MB)</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Terms and Conditions -->
          <div class="space-y-4">
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="flex items-start space-x-3">
                <div class="flex items-center h-5">
                  <input
                    id="terms"
                    v-model="form.termsAccepted"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded transition-colors"
                    required
                  />
                </div>
                <div class="text-sm">
                  <label for="terms" class="font-medium text-gray-900">
                    서비스 이용약관 및 개인정보 처리방침에 동의합니다 <span class="text-red-500">*</span>
                  </label>
                  <p class="text-gray-600 mt-1">
                    <a href="#" class="text-blue-600 hover:text-blue-700 underline">이용약관</a> 및 
                    <a href="#" class="text-blue-600 hover:text-blue-700 underline">개인정보처리방침</a>을 확인하였으며 이에 동의합니다.
                  </p>
                </div>
              </div>
            </div>
            
            <div class="flex items-start space-x-3">
              <div class="flex items-center h-5">
                <input
                  id="marketing"
                  v-model="form.marketingConsent"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded transition-colors"
                />
              </div>
              <div class="text-sm">
                <label for="marketing" class="font-medium text-gray-700">
                  마케팅 정보 수신 동의 (선택)
                </label>
                <p class="text-gray-600 mt-1">이벤트, 할인 혜택 등 마케팅 정보를 이메일로 받아보시겠습니까?</p>
              </div>
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="p-4 rounded-xl bg-red-50 border border-red-200">
            <div class="flex">
              <svg class="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm text-red-800">{{ error }}</p>
              </div>
            </div>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            class="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium py-3 px-4 rounded-xl transition-all duration-200 transform hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none shadow-lg"
          >
            <div class="flex items-center justify-center">
              <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? '계정 생성 중...' : '회원가입' }}
            </div>
          </button>

          <!-- Additional Info for Corporate/Partner -->
          <div v-if="form.user_type !== 'general'" class="mt-4 p-4 bg-blue-50 rounded-xl border border-blue-200">
            <div class="flex">
              <svg class="h-5 w-5 text-blue-400 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div class="ml-3">
                <h4 class="text-sm font-medium text-blue-900">승인 안내</h4>
                <p class="mt-1 text-sm text-blue-800">
                  {{ form.user_type === 'corporate' ? '기업회원' : '파트너회원' }} 가입신청이 완료되면 
                  관리자 검토 후 1-2 영업일 내에 승인 결과를 이메일로 알려드립니다.
                </p>
              </div>
            </div>
          </div>
        </form>

        <!-- Sign In Link -->
        <div class="mt-6 text-center">
          <p class="text-gray-600 text-sm">
            이미 계정이 있으신가요?
            <router-link to="/login" class="font-medium text-blue-600 hover:text-blue-700 transition-colors">
              로그인하기
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'

const router = useRouter()
const toast = useToast()

// 상태
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const usernameChecking = ref(false)
const usernameAvailable = ref(false)
const usernameError = ref(false)
const emailVerifying = ref(false)
const emailVerified = ref(false)
const emailSent = ref(false)
const verificationCode = ref('')

// 사용자 유형 옵션
const userTypes = [
  {
    value: 'general',
    label: '일반회원',
    description: '개인 이용자, 즉시 가입 후 이용 가능'
  },
  {
    value: 'corporate',
    label: '기업회원',
    description: '사업자등록증 필요, 승인 후 이용 가능'
  },
  {
    value: 'partner',
    label: '파트너회원',
    description: '리셀러/추천인, 승인 후 커미션 수익'
  }
]

// 폼 데이터
const form = reactive({
  user_type: 'general',
  username: '',
  email: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  address: '',
  manager_name: '',
  manager_contact: '',
  company_name: '',
  business_number: '',
  business_license_url: '',
  termsAccepted: false,
  marketingConsent: false
})

// 계산된 속성
const passwordMismatch = computed(() => {
  return form.password && form.passwordConfirm && form.password !== form.passwordConfirm
})

const isFormValid = computed(() => {
  const basicValid = form.username && 
                    form.email && 
                    form.password && 
                    form.passwordConfirm &&
                    form.name &&
                    form.termsAccepted &&
                    !passwordMismatch.value &&
                    usernameAvailable.value &&
                    emailVerified.value

  if (form.user_type !== 'general') {
    const additionalValid = form.manager_name && 
                           form.manager_contact && 
                           form.company_name &&
                           form.business_license_url

    if (form.user_type === 'corporate') {
      return basicValid && additionalValid && form.business_number
    }

    return basicValid && additionalValid
  }

  return basicValid
})

// 필드별 유효성 검사 메시지
const validateField = (fieldName: string) => {
  switch (fieldName) {
    case 'username':
      if (!form.username) {
        return '아이디를 입력해주세요.'
      }
      if (form.username.length < 4) {
        return '아이디는 4자 이상 입력해주세요.'
      }
      if (!/^[a-zA-Z0-9_]+$/.test(form.username)) {
        return '아이디는 영문, 숫자, 언더스코어만 사용 가능합니다.'
      }
      return ''
    
    case 'email':
      if (!form.email) {
        return '이메일을 입력해주세요.'
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
        return '올바른 이메일 형식이 아닙니다.'
      }
      return ''
    
    case 'password':
      if (!form.password) {
        return '비밀번호를 입력해주세요.'
      }
      if (form.password.length < 8) {
        return '비밀번호는 8자 이상 입력해주세요.'
      }
      if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.password)) {
        return '비밀번호는 대문자, 소문자, 숫자를 각각 1개 이상 포함해야 합니다.'
      }
      return ''
    
    case 'passwordConfirm':
      if (!form.passwordConfirm) {
        return '비밀번호 확인을 입력해주세요.'
      }
      if (form.password !== form.passwordConfirm) {
        return '비밀번호가 일치하지 않습니다.'
      }
      return ''
    
    case 'name':
      if (!form.name) {
        return '이름을 입력해주세요.'
      }
      if (form.name.length < 2) {
        return '이름은 2자 이상 입력해주세요.'
      }
      return ''
    
    case 'phone':
      if (form.phone && !/^01[0-9]-?\d{3,4}-?\d{4}$/.test(form.phone)) {
        return '올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)'
      }
      return ''
    
    case 'manager_name':
      if (form.user_type !== 'general' && !form.manager_name) {
        return '담당자명을 입력해주세요.'
      }
      return ''
    
    case 'manager_contact':
      if (form.user_type !== 'general' && !form.manager_contact) {
        return '담당자 연락처를 입력해주세요.'
      }
      if (form.manager_contact && !/^01[0-9]-?\d{3,4}-?\d{4}$/.test(form.manager_contact)) {
        return '올바른 연락처 형식이 아닙니다. (예: 010-1234-5678)'
      }
      return ''
    
    case 'company_name':
      if (form.user_type !== 'general' && !form.company_name) {
        return form.user_type === 'corporate' ? '회사명을 입력해주세요.' : '소속/단체명을 입력해주세요.'
      }
      return ''
    
    case 'business_number':
      if (form.user_type === 'corporate') {
        if (!form.business_number) {
          return '사업자등록번호를 입력해주세요.'
        }
        if (!/^\d{3}-?\d{2}-?\d{5}$/.test(form.business_number)) {
          return '올바른 사업자등록번호 형식이 아닙니다. (예: 123-45-67890)'
        }
      }
      return ''
  }
  return ''
}

// 실시간 유효성 검사를 위한 reactive errors
const fieldErrors = reactive({
  username: '',
  email: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  manager_name: '',
  manager_contact: '',
  company_name: '',
  business_number: ''
})

// 실시간 검증 메서드들
const validateAndCheckUsername = () => {
  fieldErrors.username = validateField('username')
  if (!fieldErrors.username) {
    checkUsername()
  } else {
    usernameAvailable.value = false
    usernameError.value = false
  }
}

const onUsernameInput = () => {
  usernameAvailable.value = false
  usernameError.value = false
  if (form.username) {
    fieldErrors.username = validateField('username')
  } else {
    fieldErrors.username = ''
  }
}

const validateEmail = () => {
  fieldErrors.email = validateField('email')
}

const validatePassword = () => {
  fieldErrors.password = validateField('password')
  if (form.passwordConfirm) {
    validatePasswordConfirm() // 비밀번호 변경시 확인도 재검증
  }
}

const validatePasswordConfirm = () => {
  fieldErrors.passwordConfirm = validateField('passwordConfirm')
}

const validateName = () => {
  fieldErrors.name = validateField('name')
}

const validatePhone = () => {
  fieldErrors.phone = validateField('phone')
}

const validateManagerName = () => {
  fieldErrors.manager_name = validateField('manager_name')
}

const validateManagerContact = () => {
  fieldErrors.manager_contact = validateField('manager_contact')
}

const validateCompanyName = () => {
  fieldErrors.company_name = validateField('company_name')
}

const validateBusinessNumber = () => {
  fieldErrors.business_number = validateField('business_number')
}

// 아이디 중복확인
const checkUsername = async () => {
  if (!form.username || fieldErrors.username) return

  usernameChecking.value = true
  usernameError.value = false
  usernameAvailable.value = false

  try {
    const result = await AuthService.checkUsername(form.username)
    if (result.available) {
      usernameAvailable.value = true
    } else {
      usernameError.value = true
    }
  } catch (err) {
    usernameError.value = true
  } finally {
    usernameChecking.value = false
  }
}

// 이메일 인증 코드 발송
const sendVerificationEmail = async () => {
  if (!form.email || fieldErrors.email) return

  emailVerifying.value = true

  try {
    // 실제 구현에서는 이메일 서비스를 사용
    // 여기서는 임시로 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    emailSent.value = true
    toast.success('인증코드가 이메일로 발송되었습니다. 메일함을 확인해주세요.')
  } catch (err) {
    toast.error('인증코드 발송에 실패했습니다. 다시 시도해주세요.')
  } finally {
    emailVerifying.value = false
  }
}

// 이메일 인증 확인
const verifyEmail = async () => {
  if (!verificationCode.value || verificationCode.value.length !== 6) return

  emailVerifying.value = true

  try {
    // 실제 구현에서는 서버에서 코드 검증
    // 여기서는 임시로 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 데모용: 123456이면 성공, 그 외는 실패
    if (verificationCode.value === '123456') {
      emailVerified.value = true
      toast.success('이메일 인증이 완료되었습니다!')
    } else {
      toast.error('인증코드가 올바르지 않습니다. 다시 확인해주세요.')
    }
  } catch (err) {
    toast.error('인증 확인에 실패했습니다. 다시 시도해주세요.')
  } finally {
    emailVerifying.value = false
  }
}

// 파일 업로드 처리
const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    // 실제 구현에서는 파일을 서버에 업로드하고 URL을 받아옴
    form.business_license_url = `uploaded_${file.name}`
    toast.success('사업자등록증이 업로드되었습니다.')
  }
}

// 파일 삭제
const clearFile = () => {
  form.business_license_url = ''
  // 파일 입력 초기화
  const fileInput = document.getElementById('business-license') as HTMLInputElement
  if (fileInput) {
    fileInput.value = ''
  }
}

// 전체 폼 검증
const validateAllFields = () => {
  fieldErrors.username = validateField('username')
  fieldErrors.email = validateField('email')
  fieldErrors.password = validateField('password')
  fieldErrors.passwordConfirm = validateField('passwordConfirm')
  fieldErrors.name = validateField('name')
  fieldErrors.phone = validateField('phone')
  
  if (form.user_type !== 'general') {
    fieldErrors.manager_name = validateField('manager_name')
    fieldErrors.manager_contact = validateField('manager_contact')
    fieldErrors.company_name = validateField('company_name')
    
    if (form.user_type === 'corporate') {
      fieldErrors.business_number = validateField('business_number')
    }
  }

  // 에러가 있는지 확인
  const hasErrors = Object.values(fieldErrors).some(error => error !== '')
  return !hasErrors
}

// 회원가입 처리
const handleSubmit = async () => {
  // 전체 폼 검증
  if (!validateAllFields()) {
    error.value = '입력 정보를 다시 확인해주세요.'
    return
  }

  if (!usernameAvailable.value) {
    error.value = '아이디 중복확인을 완료해주세요.'
    return
  }

  if (!emailVerified.value) {
    error.value = '이메일 인증을 완료해주세요.'
    return
  }

  if (!form.termsAccepted) {
    error.value = '이용약관 및 개인정보처리방침에 동의해주세요.'
    return
  }

  if (form.user_type !== 'general' && !form.business_license_url) {
    error.value = form.user_type === 'corporate' ? '사업자등록증을 업로드해주세요.' : '증명서를 업로드해주세요.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const signupData = {
      user_type: form.user_type,
      username: form.username,
      email: form.email,
      password: form.password,
      name: form.name,
      phone: form.phone,
      address: form.address,
      manager_name: form.user_type !== 'general' ? form.manager_name : undefined,
      manager_contact: form.user_type !== 'general' ? form.manager_contact : undefined,
      company_name: form.user_type !== 'general' ? form.company_name : undefined,
      business_number: form.user_type === 'corporate' ? form.business_number : undefined,
      business_license_url: form.user_type !== 'general' ? form.business_license_url : undefined,
      terms_agreed: form.termsAccepted,
      privacy_agreed: form.termsAccepted
    }

    const result = await AuthService.signUp(signupData)

    if (result.success) {
      if (form.user_type === 'general') {
        toast.success('회원가입이 완료되었습니다! 이메일 인증을 완료해주세요.')
        router.push('/auth/verify-email')
      } else {
        toast.info('회원가입 신청이 완료되었습니다. 승인까지 1-2 영업일이 소요됩니다.')
        router.push('/auth/approval')
      }
    } else {
      error.value = result.error || '회원가입에 실패했습니다.'
    }
  } catch (err) {
    console.error('회원가입 오류:', err)
    error.value = '회원가입 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}
</style>