<template>
  <div class="inline-flex items-center">
    <component 
      :is="getMethodIcon(method)" 
      :class="iconClass"
      class="flex-shrink-0"
    />
    <span v-if="showLabel" class="ml-2 text-sm text-gray-700">
      {{ getMethodText(method) }}
    </span>
  </div>
</template>

<script setup lang="ts">
import {
  CreditCardIcon,
  BanknotesIcon,
  DevicePhoneMobileIcon,
  CurrencyBitcoinIcon,
  QuestionMarkCircleIcon
} from '@heroicons/vue/24/outline'

interface Props {
  method: string
  showLabel?: boolean
  iconClass?: string
}

withDefaults(defineProps<Props>(), {
  showLabel: false,
  iconClass: 'h-4 w-4'
})

const getMethodIcon = (method: string) => {
  const icons = {
    credit_card: CreditCardIcon,
    bank_transfer: BanknotesIcon,
    mobile_payment: DevicePhoneMobileIcon,
    crypto: CurrencyBitcoinIcon,
    kakaopay: DevicePhoneMobileIcon,
    naverpay: DevicePhoneMobileIcon,
    payco: DevicePhoneMobileIcon,
    bitcoin: CurrencyBitcoinIcon,
    ethereum: CurrencyBitcoinIcon,
    tether: CurrencyBitcoinIcon
  }
  return icons[method as keyof typeof icons] || QuestionMarkCircleIcon
}

const getMethodText = (method: string): string => {
  const texts = {
    credit_card: '신용카드',
    bank_transfer: '계좌이체',
    mobile_payment: '모바일결제',
    crypto: '암호화폐',
    kakaopay: '카카오페이',
    naverpay: '네이버페이',
    payco: '페이코',
    bitcoin: '비트코인',
    ethereum: '이더리움',
    tether: '테더'
  }
  return texts[method as keyof typeof texts] || method
}
</script>