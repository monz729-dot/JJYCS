<template>
  <div class="space-y-3">
    <h4 class="text-sm font-medium text-gray-900 mb-3">보안 정보</h4>
    
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
      <div class="flex items-center">
        <ShieldCheckIcon 
          :class="securityInfo.sslEncrypted ? 'text-green-500' : 'text-gray-400'" 
          class="h-5 w-5 mr-2"
        />
        <div>
          <span class="text-sm font-medium text-gray-900">SSL 암호화</span>
          <p class="text-xs text-gray-500">
            {{ securityInfo.sslEncrypted ? '적용됨' : '미적용' }}
          </p>
        </div>
      </div>
      
      <div class="flex items-center">
        <LockClosedIcon 
          :class="securityInfo.pciCompliant ? 'text-green-500' : 'text-gray-400'" 
          class="h-5 w-5 mr-2"
        />
        <div>
          <span class="text-sm font-medium text-gray-900">PCI DSS</span>
          <p class="text-xs text-gray-500">
            {{ securityInfo.pciCompliant ? '준수함' : '미준수' }}
          </p>
        </div>
      </div>
      
      <div class="flex items-center">
        <CheckCircleIcon 
          :class="securityInfo.threeDSecure ? 'text-green-500' : 'text-gray-400'" 
          class="h-5 w-5 mr-2"
        />
        <div>
          <span class="text-sm font-medium text-gray-900">3D Secure</span>
          <p class="text-xs text-gray-500">
            {{ securityInfo.threeDSecure ? '인증됨' : '미인증' }}
          </p>
        </div>
      </div>
      
      <div v-if="securityInfo.tokenized" class="flex items-center">
        <KeyIcon class="h-5 w-5 text-green-500 mr-2" />
        <div>
          <span class="text-sm font-medium text-gray-900">토큰화</span>
          <p class="text-xs text-gray-500">카드정보 보호</p>
        </div>
      </div>
      
      <div v-if="securityInfo.encrypted" class="flex items-center">
        <LockClosedIcon class="h-5 w-5 text-green-500 mr-2" />
        <div>
          <span class="text-sm font-medium text-gray-900">End-to-End 암호화</span>
          <p class="text-xs text-gray-500">전송 구간 보안</p>
        </div>
      </div>
      
      <div v-if="securityInfo.fraud_detection" class="flex items-center">
        <EyeIcon class="h-5 w-5 text-green-500 mr-2" />
        <div>
          <span class="text-sm font-medium text-gray-900">사기 탐지</span>
          <p class="text-xs text-gray-500">실시간 모니터링</p>
        </div>
      </div>
    </div>
    
    <!-- Security Score -->
    <div class="mt-4 p-3 bg-green-50 rounded-lg border border-green-200">
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <ShieldCheckIcon class="h-5 w-5 text-green-600 mr-2" />
          <span class="text-sm font-medium text-green-800">보안 점수</span>
        </div>
        <div class="flex items-center">
          <div class="w-24 bg-green-200 rounded-full h-2 mr-2">
            <div 
              class="bg-green-600 h-2 rounded-full transition-all duration-300"
              :style="{ width: `${securityScore}%` }"
            ></div>
          </div>
          <span class="text-sm font-bold text-green-800">{{ securityScore }}/100</span>
        </div>
      </div>
      <p class="text-xs text-green-700 mt-1">
        {{ getSecurityMessage(securityScore) }}
      </p>
    </div>
    
    <!-- Compliance Badges -->
    <div class="flex flex-wrap gap-2 mt-3">
      <span 
        v-for="compliance in activeCompliances" 
        :key="compliance.name"
        class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
      >
        <component :is="compliance.icon" class="h-3 w-3 mr-1" />
        {{ compliance.name }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ShieldCheckIcon,
  LockClosedIcon,
  CheckCircleIcon,
  KeyIcon,
  EyeIcon,
  GlobeAltIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'

interface SecurityInfo {
  sslEncrypted: boolean
  pciCompliant: boolean
  threeDSecure: boolean
  tokenized?: boolean
  encrypted?: boolean
  fraud_detection?: boolean
  gdpr_compliant?: boolean
  iso27001?: boolean
  sox_compliant?: boolean
}

interface Props {
  securityInfo: SecurityInfo
}

const props = defineProps<Props>()

const securityScore = computed(() => {
  let score = 0
  const maxScore = 100
  const checks = [
    { key: 'sslEncrypted', weight: 15 },
    { key: 'pciCompliant', weight: 20 },
    { key: 'threeDSecure', weight: 15 },
    { key: 'tokenized', weight: 15 },
    { key: 'encrypted', weight: 15 },
    { key: 'fraud_detection', weight: 10 },
    { key: 'gdpr_compliant', weight: 5 },
    { key: 'iso27001', weight: 3 },
    { key: 'sox_compliant', weight: 2 }
  ]
  
  checks.forEach(check => {
    if (props.securityInfo[check.key as keyof SecurityInfo]) {
      score += check.weight
    }
  })
  
  return Math.min(score, maxScore)
})

const activeCompliances = computed(() => {
  const compliances = []
  
  if (props.securityInfo.pciCompliant) {
    compliances.push({ name: 'PCI DSS', icon: ShieldCheckIcon })
  }
  
  if (props.securityInfo.gdpr_compliant) {
    compliances.push({ name: 'GDPR', icon: GlobeAltIcon })
  }
  
  if (props.securityInfo.iso27001) {
    compliances.push({ name: 'ISO 27001', icon: DocumentTextIcon })
  }
  
  if (props.securityInfo.sox_compliant) {
    compliances.push({ name: 'SOX', icon: DocumentTextIcon })
  }
  
  return compliances
})

const getSecurityMessage = (score: number): string => {
  if (score >= 90) {
    return '매우 안전한 결제 환경입니다.'
  } else if (score >= 75) {
    return '안전한 결제 환경입니다.'
  } else if (score >= 60) {
    return '기본적인 보안이 적용되었습니다.'
  } else {
    return '보안 강화가 필요합니다.'
  }
}
</script>