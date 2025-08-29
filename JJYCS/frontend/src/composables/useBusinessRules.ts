import { ref, computed, reactive, watch, type Ref } from 'vue'
import { useToast } from './useToast'

export interface BusinessRuleWarning {
  id: string
  type: 'cbm' | 'thb' | 'memberCode' | 'weight' | 'dimensions'
  severity: 'warning' | 'error' | 'info'
  title: string
  message: string
  details?: string
  actions?: Array<{
    label: string
    action: () => void
    primary?: boolean
  }>
  timestamp: Date
}

export interface OrderItem {
  id?: string
  name: string
  quantity: number
  unitPrice: number
  unitPriceCurrency: 'THB' | 'KRW' | 'USD'
  width: number
  height: number
  depth: number
  weight: number
  cbm?: number
}

export interface ShippingInfo {
  memberCode?: string
  shippingType?: 'sea' | 'air'
}

export interface BusinessRuleThresholds {
  cbm: {
    seaToAirLimit: number // 29m³
    warningThreshold: number // 25m³
  }
  thb: {
    extraInfoRequired: number // 1500 THB
    warningThreshold: number // 1200 THB
  }
  weight: {
    maxWeight: number // 30kg per item
    warningThreshold: number // 25kg
  }
}

export const DEFAULT_THRESHOLDS: BusinessRuleThresholds = {
  cbm: {
    seaToAirLimit: 29.0,
    warningThreshold: 25.0
  },
  thb: {
    extraInfoRequired: 1500.0,
    warningThreshold: 1200.0
  },
  weight: {
    maxWeight: 30.0,
    warningThreshold: 25.0
  }
}

export function useBusinessRules(thresholds: BusinessRuleThresholds = DEFAULT_THRESHOLDS) {
  const { businessWarning, error: showError, info: showInfo } = useToast()
  
  // State
  const warnings = ref<BusinessRuleWarning[]>([])
  const isValidating = ref(false)
  const validationResults = reactive({
    cbmValid: true,
    thbValid: true,
    memberCodeValid: true,
    weightValid: true,
    overallValid: true
  })

  // Helper functions
  const generateWarningId = () => `warning_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

  const clearWarnings = (type?: BusinessRuleWarning['type']) => {
    if (type) {
      warnings.value = warnings.value.filter(w => w.type !== type)
    } else {
      warnings.value = []
    }
  }

  const addWarning = (warning: Omit<BusinessRuleWarning, 'id' | 'timestamp'>) => {
    // Remove existing warnings of the same type
    clearWarnings(warning.type)
    
    const newWarning: BusinessRuleWarning = {
      ...warning,
      id: generateWarningId(),
      timestamp: new Date()
    }
    
    warnings.value.push(newWarning)
    
    // Show toast notification
    switch (warning.severity) {
      case 'error':
        showError(warning.title, warning.message)
        break
      case 'warning':
        businessWarning(warning.type, warning.message)
        break
      case 'info':
        showInfo(warning.title, warning.message)
        break
    }
    
    return newWarning.id
  }

  // CBM Validation
  const calculateItemCBM = (item: OrderItem): number => {
    if (!item.width || !item.height || !item.depth) return 0
    return (item.width * item.height * item.depth) / 1000000 // Convert cm³ to m³
  }

  const calculateTotalCBM = (items: OrderItem[]): number => {
    return items.reduce((total, item) => {
      const itemCBM = calculateItemCBM(item)
      return total + (itemCBM * (item.quantity || 1))
    }, 0)
  }

  const validateCBM = (items: OrderItem[], currentShippingType?: string): boolean => {
    const totalCBM = calculateTotalCBM(items)
    
    clearWarnings('cbm')
    
    if (totalCBM > thresholds.cbm.seaToAirLimit) {
      addWarning({
        type: 'cbm',
        severity: 'error',
        title: 'CBM 임계값 초과',
        message: `총 CBM이 ${totalCBM.toFixed(2)}m³로 ${thresholds.cbm.seaToAirLimit}m³를 초과했습니다.`,
        details: '해상운송이 불가능하여 항공운송으로 자동 전환됩니다. 추가 비용이 발생할 수 있습니다.',
        actions: [
          {
            label: '항목 줄이기',
            action: () => showInfo('도움말', '품목 수량을 줄이거나 크기가 작은 품목으로 변경해보세요'),
            primary: true
          },
          {
            label: '항공운송 진행',
            action: () => showInfo('안내', '항공운송으로 진행하시면 더 빠른 배송이 가능합니다')
          }
        ]
      })
      validationResults.cbmValid = false
      return false
    } else if (totalCBM > thresholds.cbm.warningThreshold) {
      addWarning({
        type: 'cbm',
        severity: 'warning',
        title: 'CBM 주의',
        message: `총 CBM이 ${totalCBM.toFixed(2)}m³입니다.`,
        details: `${thresholds.cbm.seaToAirLimit}m³에 가까워지고 있습니다. 추가 품목 추가 시 항공운송으로 전환될 수 있습니다.`
      })
    }
    
    validationResults.cbmValid = true
    return true
  }

  // THB Validation
  const calculateTotalTHB = (items: OrderItem[]): number => {
    return items.reduce((total, item) => {
      if (item.unitPriceCurrency === 'THB') {
        return total + ((item.unitPrice || 0) * (item.quantity || 1))
      }
      // TODO: Add currency conversion for KRW, USD
      return total
    }, 0)
  }

  const validateTHB = (items: OrderItem[]): boolean => {
    const totalTHB = calculateTotalTHB(items)
    
    clearWarnings('thb')
    
    if (totalTHB > thresholds.thb.extraInfoRequired) {
      addWarning({
        type: 'thb',
        severity: 'warning',
        title: 'THB 임계값 초과',
        message: `총 상품가액이 ${totalTHB.toLocaleString()} THB로 ${thresholds.thb.extraInfoRequired} THB를 초과했습니다.`,
        details: '태국 세관 통관을 위해 수취인의 추가 정보(여권번호, 주민등록번호 등)가 필요할 수 있습니다.',
        actions: [
          {
            label: '수취인 정보 추가',
            action: () => showInfo('안내', '수취인 정보 섹션에서 추가 정보를 입력해주세요'),
            primary: true
          }
        ]
      })
      validationResults.thbValid = false
      return false
    } else if (totalTHB > thresholds.thb.warningThreshold) {
      addWarning({
        type: 'thb',
        severity: 'info',
        title: 'THB 주의',
        message: `총 상품가액이 ${totalTHB.toLocaleString()} THB입니다.`,
        details: `${thresholds.thb.extraInfoRequired} THB에 가까워지고 있습니다.`
      })
    }
    
    validationResults.thbValid = true
    return true
  }

  // Member Code Validation
  const validateMemberCode = (shippingInfo: ShippingInfo): boolean => {
    clearWarnings('memberCode')
    
    if (!shippingInfo.memberCode || shippingInfo.memberCode.toLowerCase() === 'no code') {
      addWarning({
        type: 'memberCode',
        severity: 'warning',
        title: '회원코드 미기재',
        message: '회원코드가 기재되지 않았습니다.',
        details: '회원코드 미기재 시 발송이 지연될 수 있습니다. 정확한 회원코드를 기재해주세요.',
        actions: [
          {
            label: '회원코드 입력',
            action: () => showInfo('안내', '회원가입 시 받은 회원코드를 입력해주세요'),
            primary: true
          },
          {
            label: '지연 승인하고 진행',
            action: () => showInfo('확인', '발송 지연을 승인하고 주문을 진행합니다')
          }
        ]
      })
      validationResults.memberCodeValid = false
      return false
    }
    
    validationResults.memberCodeValid = true
    return true
  }

  // Weight Validation
  const validateWeight = (items: OrderItem[]): boolean => {
    clearWarnings('weight')
    
    let hasWeightIssue = false
    
    items.forEach((item, index) => {
      if (item.weight > thresholds.weight.maxWeight) {
        addWarning({
          type: 'weight',
          severity: 'error',
          title: '중량 초과',
          message: `품목 ${index + 1}의 중량이 ${item.weight}kg으로 최대 허용 중량 ${thresholds.weight.maxWeight}kg을 초과했습니다.`,
          details: '개별 품목의 중량이 너무 무겁습니다. 품목을 나누거나 중량을 줄여주세요.'
        })
        hasWeightIssue = true
      } else if (item.weight > thresholds.weight.warningThreshold) {
        addWarning({
          type: 'weight',
          severity: 'warning',
          title: '중량 주의',
          message: `품목 ${index + 1}의 중량이 ${item.weight}kg으로 높습니다.`,
          details: '무거운 품목은 추가 비용이 발생할 수 있습니다.'
        })
      }
    })
    
    validationResults.weightValid = !hasWeightIssue
    return !hasWeightIssue
  }

  // Comprehensive validation
  const validateAllRules = (items: OrderItem[], shippingInfo: ShippingInfo): boolean => {
    isValidating.value = true
    
    try {
      // Update all items with calculated CBM
      items.forEach(item => {
        item.cbm = calculateItemCBM(item)
      })
      
      const cbmValid = validateCBM(items, shippingInfo.shippingType)
      const thbValid = validateTHB(items)
      const memberCodeValid = validateMemberCode(shippingInfo)
      const weightValid = validateWeight(items)
      
      validationResults.overallValid = cbmValid && thbValid && memberCodeValid && weightValid
      
      return validationResults.overallValid
    } finally {
      isValidating.value = false
    }
  }

  // Auto-validation with debounce
  const createAutoValidator = (items: Ref<OrderItem[]>, shippingInfo: Ref<ShippingInfo>, delay = 500) => {
    let timeoutId: number | null = null
    
    const debouncedValidate = () => {
      if (timeoutId) clearTimeout(timeoutId)
      timeoutId = window.setTimeout(() => {
        validateAllRules(items.value, shippingInfo.value)
      }, delay)
    }
    
    // Watch for changes
    watch([items, shippingInfo], debouncedValidate, { deep: true })
    
    return {
      validate: () => validateAllRules(items.value, shippingInfo.value),
      validateImmediate: debouncedValidate
    }
  }

  // Computed properties
  const totalCBM = computed(() => (items: OrderItem[]) => calculateTotalCBM(items))
  const totalTHB = computed(() => (items: OrderItem[]) => calculateTotalTHB(items))
  const hasWarnings = computed(() => warnings.value.length > 0)
  const hasErrors = computed(() => warnings.value.some(w => w.severity === 'error'))
  const warningsByType = computed(() => {
    const grouped: Record<string, BusinessRuleWarning[]> = {}
    warnings.value.forEach(warning => {
      if (!grouped[warning.type]) grouped[warning.type] = []
      grouped[warning.type].push(warning)
    })
    return grouped
  })

  return {
    // State
    warnings: readonly(warnings),
    isValidating: readonly(isValidating),
    validationResults: readonly(validationResults),
    
    // Computed
    hasWarnings,
    hasErrors,
    warningsByType,
    totalCBM,
    totalTHB,
    
    // Functions
    validateAllRules,
    validateCBM,
    validateTHB,
    validateMemberCode,
    validateWeight,
    calculateItemCBM,
    calculateTotalCBM,
    calculateTotalTHB,
    clearWarnings,
    addWarning,
    
    // Auto-validation
    createAutoValidator
  }
}