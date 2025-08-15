/**
 * 비즈니스 규칙 관련 유틸리티 함수들
 * CBM 계산, THB 금액 체크, 회원코드 검증 등
 */

/**
 * CBM 계산 상수
 */
export const BUSINESS_RULES = {
  CBM_THRESHOLD: 29.0,           // CBM 임계값 (m³)
  THB_AMOUNT_THRESHOLD: 1500,    // THB 금액 임계값
  CM_TO_M_DIVISOR: 1_000_000     // cm³를 m³로 변환하는 나누기 값
} as const

/**
 * 박스 치수 인터페이스
 */
export interface BoxDimensions {
  width: number   // 폭 (cm)
  height: number  // 높이 (cm)
  depth: number   // 깊이 (cm)
}

/**
 * CBM 계산 결과
 */
export interface CBMResult {
  cbm: number                // 계산된 CBM 값 (m³)
  exceedsThreshold: boolean  // 임계값 초과 여부
  requiresAirConversion: boolean // 항공 배송 전환 필요 여부
}

/**
 * 비즈니스 규칙 검증 결과
 */
export interface BusinessRuleValidation {
  cbmExceedsLimit: boolean
  amountExceedsThb1500: boolean
  requiresExtraRecipient: boolean
  memberCodeMissing: boolean
  warnings: string[]
}

/**
 * 단일 박스의 CBM 계산
 * CBM = (width × height × depth) / 1,000,000
 */
export function calculateSingleCBM(dimensions: BoxDimensions): CBMResult {
  if (!dimensions.width || !dimensions.height || !dimensions.depth) {
    throw new Error('박스 치수가 누락되었습니다.')
  }

  if (dimensions.width <= 0 || dimensions.height <= 0 || dimensions.depth <= 0) {
    throw new Error('박스 치수는 0보다 커야 합니다.')
  }

  const cbm = (dimensions.width * dimensions.height * dimensions.depth) / BUSINESS_RULES.CM_TO_M_DIVISOR
  const exceedsThreshold = cbm > BUSINESS_RULES.CBM_THRESHOLD
  
  return {
    cbm: Number(cbm.toFixed(6)), // 6자리 소수점으로 반올림
    exceedsThreshold,
    requiresAirConversion: exceedsThreshold
  }
}

/**
 * 여러 박스의 총 CBM 계산
 */
export function calculateTotalCBM(boxes: BoxDimensions[]): CBMResult {
  if (!boxes || boxes.length === 0) {
    return {
      cbm: 0,
      exceedsThreshold: false,
      requiresAirConversion: false
    }
  }

  const totalCBM = boxes.reduce((sum, box) => {
    const result = calculateSingleCBM(box)
    return sum + result.cbm
  }, 0)

  const exceedsThreshold = totalCBM > BUSINESS_RULES.CBM_THRESHOLD

  return {
    cbm: Number(totalCBM.toFixed(6)),
    exceedsThreshold,
    requiresAirConversion: exceedsThreshold
  }
}

/**
 * CBM 기반 배송 타입 결정
 */
export function determineShippingType(cbm: number): 'air' | 'sea' {
  return cbm > BUSINESS_RULES.CBM_THRESHOLD ? 'air' : 'sea'
}

/**
 * THB 금액이 1,500을 초과하는지 체크
 */
export function checkTHBAmountThreshold(totalAmount: number, currency: string): boolean {
  if (currency !== 'THB') {
    return false
  }
  return totalAmount > BUSINESS_RULES.THB_AMOUNT_THRESHOLD
}

/**
 * 회원코드 누락으로 인한 지연 처리 여부
 */
export function checkMemberCodeDelay(memberCode?: string | null): boolean {
  return !memberCode || memberCode.trim() === ''
}

/**
 * 주문 생성 시 비즈니스 규칙 검증
 */
export function validateOrderBusinessRules(
  boxes: BoxDimensions[],
  items: Array<{ amount: number; currency: string }>,
  memberCode?: string | null
): BusinessRuleValidation {
  // CBM 검증
  const cbmResult = calculateTotalCBM(boxes)
  
  // THB 금액 검증
  const totalTHB = items
    .filter(item => item.currency === 'THB')
    .reduce((sum, item) => sum + item.amount, 0)
  
  const amountExceedsThb1500 = checkTHBAmountThreshold(totalTHB, 'THB')
  const memberCodeMissing = checkMemberCodeDelay(memberCode)

  // 경고 메시지 생성
  const warnings: string[] = []
  
  if (cbmResult.exceedsThreshold) {
    warnings.push(
      `총 CBM ${cbmResult.cbm.toFixed(3)} m³가 임계값 ${BUSINESS_RULES.CBM_THRESHOLD} m³를 초과하여 항공배송으로 자동 전환됩니다.`
    )
  }
  
  if (amountExceedsThb1500) {
    warnings.push(
      `THB 총액 ${totalTHB.toLocaleString()}이 임계값 ${BUSINESS_RULES.THB_AMOUNT_THRESHOLD.toLocaleString()}을 초과합니다. 수취인 추가 정보를 입력해주세요.`
    )
  }
  
  if (memberCodeMissing) {
    warnings.push(
      '회원코드가 없어 주문이 지연 처리됩니다. 회원코드를 입력해주세요.'
    )
  }

  return {
    cbmExceedsLimit: cbmResult.exceedsThreshold,
    amountExceedsThb1500,
    requiresExtraRecipient: amountExceedsThb1500,
    memberCodeMissing,
    warnings
  }
}

/**
 * CBM 초과 경고 메시지 생성
 */
export function generateCBMWarningMessage(cbm: number): string | null {
  if (cbm > BUSINESS_RULES.CBM_THRESHOLD) {
    return `CBM이 ${cbm.toFixed(3)} m³로 임계값 ${BUSINESS_RULES.CBM_THRESHOLD} m³를 초과하여 항공배송으로 자동 전환됩니다.`
  }
  return null
}

/**
 * THB 금액 초과 경고 메시지 생성
 */
export function generateTHBWarningMessage(amount: number): string | null {
  if (amount > BUSINESS_RULES.THB_AMOUNT_THRESHOLD) {
    return `금액이 THB ${amount.toLocaleString()}로 임계값 THB ${BUSINESS_RULES.THB_AMOUNT_THRESHOLD.toLocaleString()}을 초과합니다. 수취인 추가 정보를 입력해주세요.`
  }
  return null
}