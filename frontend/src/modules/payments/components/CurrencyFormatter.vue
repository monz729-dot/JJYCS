<template>
  <span :class="amountClass">
    {{ formattedAmount }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  amount: number
  currency: string
  showSymbol?: boolean
  locale?: string
  amountClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  showSymbol: true,
  locale: 'ko-KR',
  amountClass: 'font-medium'
})

const currencySymbols = {
  KRW: '₩',
  THB: '฿',
  USD: '$',
  EUR: '€',
  JPY: '¥',
  CNY: '¥',
  BTC: '₿',
  ETH: 'Ξ',
  USDT: '₮'
}

const currencyNames = {
  KRW: '원',
  THB: '바트',
  USD: '달러',
  EUR: '유로',
  JPY: '엔',
  CNY: '위안',
  BTC: 'BTC',
  ETH: 'ETH',
  USDT: 'USDT'
}

const formattedAmount = computed(() => {
  const symbol = currencySymbols[props.currency as keyof typeof currencySymbols]
  const name = currencyNames[props.currency as keyof typeof currencyNames]
  
  // Format number based on currency
  let formattedNumber: string
  
  if (props.currency === 'BTC' || props.currency === 'ETH') {
    // Crypto currencies - show more decimal places
    formattedNumber = props.amount.toFixed(8).replace(/\.?0+$/, '')
  } else if (props.currency === 'USDT') {
    // USDT - show 2-6 decimal places
    formattedNumber = props.amount.toFixed(6).replace(/\.?0+$/, '')
  } else {
    // Fiat currencies
    formattedNumber = new Intl.NumberFormat(props.locale, {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(props.amount)
  }
  
  if (props.showSymbol && symbol) {
    return `${symbol}${formattedNumber}`
  } else if (props.showSymbol && name) {
    return `${formattedNumber} ${name}`
  } else {
    return formattedNumber
  }
})
</script>