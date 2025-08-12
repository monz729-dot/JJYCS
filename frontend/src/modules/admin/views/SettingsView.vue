<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">시스템 설정</h1>
        <p class="text-sm text-gray-600 mt-1">시스템의 전반적인 설정을 관리합니다</p>
      </div>
      <div class="mt-4 sm:mt-0">
        <button @click="saveAllSettings" 
                :disabled="loading"
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50">
          <CheckIcon class="h-4 w-4 mr-2" />
          {{ loading ? '저장 중...' : '모든 설정 저장' }}
        </button>
      </div>
    </div>

    <!-- Settings Tabs -->
    <div class="bg-white rounded-lg shadow">
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-8 px-6" aria-label="Tabs">
          <button
            v-for="tab in settingsTabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap"
            :class="activeTab === tab.key
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
          >
            <component :is="tab.icon" class="h-5 w-5 mr-2 inline" />
            {{ tab.label }}
          </button>
        </nav>
      </div>

      <!-- General Settings -->
      <div v-if="activeTab === 'general'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">일반 설정</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">시스템명</label>
              <input v-model="settings.general.systemName" 
                     type="text" 
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">관리자 이메일</label>
              <input v-model="settings.general.adminEmail" 
                     type="email" 
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">시스템 언어</label>
              <select v-model="settings.general.language" 
                      class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                <option value="ko">한국어</option>
                <option value="en">English</option>
                <option value="th">ภาษาไทย</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">타임존</label>
              <select v-model="settings.general.timezone" 
                      class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                <option value="Asia/Seoul">Asia/Seoul (KST)</option>
                <option value="Asia/Bangkok">Asia/Bangkok (ICT)</option>
                <option value="UTC">UTC</option>
              </select>
            </div>
          </div>
        </div>

        <div>
          <h4 class="text-md font-medium text-gray-900 mb-3">시스템 동작 설정</h4>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h5 class="text-sm font-medium text-gray-900">자동 승인</h5>
                <p class="text-sm text-gray-500">기업/파트너 계정의 자동 승인 여부</p>
              </div>
              <button @click="settings.general.autoApproval = !settings.general.autoApproval"
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                      :class="settings.general.autoApproval ? 'bg-blue-600' : 'bg-gray-200'">
                <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                      :class="settings.general.autoApproval ? 'translate-x-5' : 'translate-x-0'"></span>
              </button>
            </div>
            <div class="flex items-center justify-between">
              <div>
                <h5 class="text-sm font-medium text-gray-900">메인터넌스 모드</h5>
                <p class="text-sm text-gray-500">시스템 점검 모드 활성화</p>
              </div>
              <button @click="settings.general.maintenanceMode = !settings.general.maintenanceMode"
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                      :class="settings.general.maintenanceMode ? 'bg-red-600' : 'bg-gray-200'">
                <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                      :class="settings.general.maintenanceMode ? 'translate-x-5' : 'translate-x-0'"></span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Business Rules -->
      <div v-if="activeTab === 'business'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">비즈니스 규칙</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">CBM 임계치 (m³)</label>
              <input v-model.number="settings.business.cbmThreshold" 
                     type="number" 
                     step="0.1"
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              <p class="text-xs text-gray-500 mt-1">이 값을 초과하면 자동으로 항공 배송으로 전환됩니다</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">THB 임계치</label>
              <input v-model.number="settings.business.thbThreshold" 
                     type="number"
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              <p class="text-xs text-gray-500 mt-1">이 값을 초과하면 추가 수취인 정보가 필요합니다</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">승인 처리 시간 (일)</label>
              <input v-model.number="settings.business.approvalDays" 
                     type="number"
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              <p class="text-xs text-gray-500 mt-1">기업/파트너 계정 승인 예상 소요일</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">지연 알림 시간 (시간)</label>
              <input v-model.number="settings.business.delayNotificationHours" 
                     type="number"
                     class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              <p class="text-xs text-gray-500 mt-1">회원 코드 미기재 시 지연 알림 발송 시점</p>
            </div>
          </div>
        </div>

        <div>
          <h4 class="text-md font-medium text-gray-900 mb-3">자동화 규칙</h4>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h5 class="text-sm font-medium text-gray-900">자동 CBM 계산</h5>
                <p class="text-sm text-gray-500">박스 치수를 기반으로 CBM을 자동 계산합니다</p>
              </div>
              <button @click="settings.business.autoCbmCalculation = !settings.business.autoCbmCalculation"
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                      :class="settings.business.autoCbmCalculation ? 'bg-blue-600' : 'bg-gray-200'">
                <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                      :class="settings.business.autoCbmCalculation ? 'translate-x-5' : 'translate-x-0'"></span>
              </button>
            </div>
            <div class="flex items-center justify-between">
              <div>
                <h5 class="text-sm font-medium text-gray-900">EMS/HS 코드 검증</h5>
                <p class="text-sm text-gray-500">주문 시 EMS/HS 코드를 자동으로 검증합니다</p>
              </div>
              <button @click="settings.business.autoCodeValidation = !settings.business.autoCodeValidation"
                      class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                      :class="settings.business.autoCodeValidation ? 'bg-blue-600' : 'bg-gray-200'">
                <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                      :class="settings.business.autoCodeValidation ? 'translate-x-5' : 'translate-x-0'"></span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Notifications -->
      <div v-if="activeTab === 'notifications'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">알림 설정</h3>
          <div class="space-y-6">
            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">이메일 알림</h4>
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">신규 주문 알림</h5>
                    <p class="text-sm text-gray-500">새로운 주문이 들어올 때 관리자에게 알림</p>
                  </div>
                  <button @click="settings.notifications.newOrderEmail = !settings.notifications.newOrderEmail"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="settings.notifications.newOrderEmail ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="settings.notifications.newOrderEmail ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">승인 요청 알림</h5>
                    <p class="text-sm text-gray-500">기업/파트너 승인 요청 시 관리자에게 알림</p>
                  </div>
                  <button @click="settings.notifications.approvalRequestEmail = !settings.notifications.approvalRequestEmail"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="settings.notifications.approvalRequestEmail ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="settings.notifications.approvalRequestEmail ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">SMS 알림</h4>
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">배송 상태 알림</h5>
                    <p class="text-sm text-gray-500">고객에게 배송 상태 변경 시 SMS 발송</p>
                  </div>
                  <button @click="settings.notifications.deliveryStatusSms = !settings.notifications.deliveryStatusSms"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="settings.notifications.deliveryStatusSms ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="settings.notifications.deliveryStatusSms ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">알림 템플릿 설정</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">SMTP 서버</label>
                  <input v-model="settings.notifications.smtpHost" 
                         type="text" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">SMTP 포트</label>
                  <input v-model.number="settings.notifications.smtpPort" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">발신자 이메일</label>
                  <input v-model="settings.notifications.fromEmail" 
                         type="email" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">발신자 이름</label>
                  <input v-model="settings.notifications.fromName" 
                         type="text" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Security -->
      <div v-if="activeTab === 'security'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">보안 설정</h3>
          <div class="space-y-6">
            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">인증 설정</h4>
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">2단계 인증 강제</h5>
                    <p class="text-sm text-gray-500">모든 사용자에게 2FA 사용을 강제합니다</p>
                  </div>
                  <button @click="settings.security.force2FA = !settings.security.force2FA"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="settings.security.force2FA ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="settings.security.force2FA ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">세션 만료 시간 (분)</label>
                    <input v-model.number="settings.security.sessionTimeout" 
                           type="number" 
                           class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">최대 로그인 시도 횟수</label>
                    <input v-model.number="settings.security.maxLoginAttempts" 
                           type="number" 
                           class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">비밀번호 정책</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최소 길이</label>
                  <input v-model.number="settings.security.passwordMinLength" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호 만료 기간 (일)</label>
                  <input v-model.number="settings.security.passwordExpireDays" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
              <div class="space-y-2 mt-4">
                <div class="flex items-center">
                  <input v-model="settings.security.requireUppercase" 
                         type="checkbox" 
                         class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  <label class="ml-2 text-sm text-gray-700">대문자 필수</label>
                </div>
                <div class="flex items-center">
                  <input v-model="settings.security.requireLowercase" 
                         type="checkbox" 
                         class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  <label class="ml-2 text-sm text-gray-700">소문자 필수</label>
                </div>
                <div class="flex items-center">
                  <input v-model="settings.security.requireNumbers" 
                         type="checkbox" 
                         class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  <label class="ml-2 text-sm text-gray-700">숫자 필수</label>
                </div>
                <div class="flex items-center">
                  <input v-model="settings.security.requireSpecialChars" 
                         type="checkbox" 
                         class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  <label class="ml-2 text-sm text-gray-700">특수문자 필수</label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- API Settings -->
      <div v-if="activeTab === 'api'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">API 설정</h3>
          <div class="space-y-6">
            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">외부 API 키</h4>
              <div class="grid grid-cols-1 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">EMS API 키</label>
                  <div class="flex">
                    <input v-model="settings.api.emsApiKey" 
                           :type="showApiKeys.ems ? 'text' : 'password'"
                           class="flex-1 rounded-l-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                    <button @click="showApiKeys.ems = !showApiKeys.ems"
                            class="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md bg-gray-50 hover:bg-gray-100">
                      <EyeIcon v-if="!showApiKeys.ems" class="h-4 w-4" />
                      <EyeSlashIcon v-else class="h-4 w-4" />
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">HS 코드 API 키</label>
                  <div class="flex">
                    <input v-model="settings.api.hsApiKey" 
                           :type="showApiKeys.hs ? 'text' : 'password'"
                           class="flex-1 rounded-l-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                    <button @click="showApiKeys.hs = !showApiKeys.hs"
                            class="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md bg-gray-50 hover:bg-gray-100">
                      <EyeIcon v-if="!showApiKeys.hs" class="h-4 w-4" />
                      <EyeSlashIcon v-else class="h-4 w-4" />
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">환율 API 키</label>
                  <div class="flex">
                    <input v-model="settings.api.exchangeApiKey" 
                           :type="showApiKeys.exchange ? 'text' : 'password'"
                           class="flex-1 rounded-l-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                    <button @click="showApiKeys.exchange = !showApiKeys.exchange"
                            class="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md bg-gray-50 hover:bg-gray-100">
                      <EyeIcon v-if="!showApiKeys.exchange" class="h-4 w-4" />
                      <EyeSlashIcon v-else class="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">API 제한 설정</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">시간당 요청 한도</label>
                  <input v-model.number="settings.api.rateLimit" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">API 타임아웃 (초)</label>
                  <input v-model.number="settings.api.timeout" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>

            <div>
              <h4 class="text-md font-medium text-gray-900 mb-3">API 테스트</h4>
              <div class="space-y-3">
                <button @click="testApi('ems')" 
                        class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
                  EMS API 테스트
                </button>
                <button @click="testApi('hs')" 
                        class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 ml-3">
                  HS 코드 API 테스트
                </button>
                <button @click="testApi('exchange')" 
                        class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 ml-3">
                  환율 API 테스트
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  CheckIcon,
  Cog6ToothIcon,
  ShieldCheckIcon,
  BellIcon,
  GlobeAltIcon,
  KeyIcon,
  EyeIcon,
  EyeSlashIcon
} from '@heroicons/vue/24/outline'

// State
const loading = ref(false)
const activeTab = ref('general')

const showApiKeys = ref({
  ems: false,
  hs: false,
  exchange: false
})

const settingsTabs = [
  { key: 'general', label: '일반', icon: Cog6ToothIcon },
  { key: 'business', label: '비즈니스 규칙', icon: GlobeAltIcon },
  { key: 'notifications', label: '알림', icon: BellIcon },
  { key: 'security', label: '보안', icon: ShieldCheckIcon },
  { key: 'api', label: 'API', icon: KeyIcon }
]

const settings = ref({
  general: {
    systemName: 'YCS 물류관리시스템',
    adminEmail: 'admin@ycs.com',
    language: 'ko',
    timezone: 'Asia/Seoul',
    autoApproval: false,
    maintenanceMode: false
  },
  business: {
    cbmThreshold: 29.0,
    thbThreshold: 1500,
    approvalDays: 2,
    delayNotificationHours: 24,
    autoCbmCalculation: true,
    autoCodeValidation: true
  },
  notifications: {
    newOrderEmail: true,
    approvalRequestEmail: true,
    deliveryStatusSms: true,
    smtpHost: 'smtp.gmail.com',
    smtpPort: 587,
    fromEmail: 'noreply@ycs.com',
    fromName: 'YCS 물류시스템'
  },
  security: {
    force2FA: false,
    sessionTimeout: 120,
    maxLoginAttempts: 5,
    passwordMinLength: 8,
    passwordExpireDays: 90,
    requireUppercase: true,
    requireLowercase: true,
    requireNumbers: true,
    requireSpecialChars: true
  },
  api: {
    emsApiKey: '••••••••••••••••',
    hsApiKey: '••••••••••••••••',
    exchangeApiKey: '••••••••••••••••',
    rateLimit: 1000,
    timeout: 30
  }
})

// Methods
const saveAllSettings = async () => {
  loading.value = true
  try {
    // Mock save - replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    console.log('Settings saved:', settings.value)
    // Show success message
  } catch (error) {
    console.error('Error saving settings:', error)
    // Show error message
  } finally {
    loading.value = false
  }
}

const testApi = async (apiType: string) => {
  console.log(`Testing ${apiType} API...`)
  
  try {
    // Mock API test
    await new Promise(resolve => setTimeout(resolve, 2000))
    alert(`${apiType.toUpperCase()} API 테스트가 성공했습니다.`)
  } catch (error) {
    alert(`${apiType.toUpperCase()} API 테스트가 실패했습니다.`)
  }
}
</script>