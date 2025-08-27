package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessLogicService {
    
    // 비즈니스 규칙 상수들
    private static final BigDecimal CBM_THRESHOLD = new BigDecimal("29.0");
    private static final BigDecimal THB_THRESHOLD = new BigDecimal("1500.0");
    
    /**
     * EMS 코드 검증 (Mock 구현)
     * 실제로는 data.go.kr API 연동 필요
     */
    public boolean validateEmsCode(String emsCode) {
        if (emsCode == null || emsCode.trim().isEmpty()) {
            return false;
        }
        
        // EMS 코드 형식 체크 (예: EE123456789KR)
        String pattern = "^[A-Z]{2}\\d{9}[A-Z]{2}$";
        boolean isValidFormat = emsCode.matches(pattern);
        
        log.info("EMS code validation: {} -> {}", emsCode, isValidFormat);
        
        // TODO: 실제 data.go.kr API 호출하여 검증
        return isValidFormat;
    }
    
    /**
     * HS 코드 검증 (Mock 구현)
     * 실제로는 관세청 API 연동 필요
     */
    public boolean validateHsCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return false;
        }
        
        // HS 코드 형식 체크 (4-10자리 숫자, 점 포함 가능)
        String pattern = "^\\d{4}(\\.\\d{1,6})?$";
        boolean isValidFormat = hsCode.matches(pattern);
        
        log.info("HS code validation: {} -> {}", hsCode, isValidFormat);
        
        // TODO: 실제 관세청 API 호출하여 검증
        return isValidFormat;
    }
    
    /**
     * CBM 계산 및 임계값 체크
     */
    public Map<String, Object> calculateCbmAndCheck(BigDecimal width, BigDecimal height, 
                                                   BigDecimal depth, Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        
        // CBM 계산: (W × H × D × 수량) / 1,000,000
        BigDecimal cbm = width.multiply(height).multiply(depth)
                             .multiply(new BigDecimal(quantity))
                             .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
        
        result.put("cbm", cbm);
        result.put("exceedsThreshold", cbm.compareTo(CBM_THRESHOLD) > 0);
        result.put("threshold", CBM_THRESHOLD);
        
        if (cbm.compareTo(CBM_THRESHOLD) > 0) {
            result.put("warning", String.format("CBM %.6f m³가 임계값 %.1f m³를 초과하여 자동으로 항공 배송으로 전환됩니다.", 
                                               cbm.doubleValue(), CBM_THRESHOLD.doubleValue()));
        }
        
        return result;
    }
    
    /**
     * THB 가격 체크 및 수취인 정보 필요 여부 판단
     */
    public Map<String, Object> checkThbThreshold(BigDecimal totalPrice) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("totalPrice", totalPrice);
        result.put("exceedsThreshold", totalPrice.compareTo(THB_THRESHOLD) > 0);
        result.put("threshold", THB_THRESHOLD);
        result.put("requiresExtraRecipient", totalPrice.compareTo(THB_THRESHOLD) > 0);
        
        if (totalPrice.compareTo(THB_THRESHOLD) > 0) {
            result.put("warning", String.format("총 금액 %.2f THB가 임계값 %.0f THB를 초과하여 수취인 추가 정보가 필요합니다.", 
                                               totalPrice.doubleValue(), THB_THRESHOLD.doubleValue()));
        }
        
        return result;
    }
    
    /**
     * 회원코드 체크
     */
    public Map<String, Object> checkMemberCode(String memberCode) {
        Map<String, Object> result = new HashMap<>();
        
        boolean isValid = memberCode != null && !memberCode.trim().isEmpty();
        
        result.put("memberCode", memberCode);
        result.put("isValid", isValid);
        result.put("causesDelay", !isValid);
        
        if (!isValid) {
            result.put("warning", "회원코드가 미기재되어 발송이 지연될 수 있습니다. 코드를 입력해주세요.");
        }
        
        return result;
    }
    
    /**
     * 환율 정보 조회 (Mock 구현)
     * 실제로는 환율 API 연동 필요
     */
    public Map<String, BigDecimal> getExchangeRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        // Mock 환율 정보
        rates.put("USD_TO_KRW", new BigDecimal("1320.50"));
        rates.put("THB_TO_KRW", new BigDecimal("38.75"));
        rates.put("USD_TO_THB", new BigDecimal("34.08"));
        
        log.info("Exchange rates retrieved: {}", rates);
        
        // TODO: 실제 환율 API 연동
        return rates;
    }
    
    /**
     * 종합 비즈니스 규칙 검증
     */
    public Map<String, Object> validateBusinessRules(String hsCode, String emsCode, 
                                                    BigDecimal width, BigDecimal height, BigDecimal depth,
                                                    Integer quantity, BigDecimal totalPrice, String memberCode) {
        Map<String, Object> result = new HashMap<>();
        
        // 각 규칙별 검증
        result.put("hsCodeValidation", validateHsCode(hsCode));
        result.put("emsCodeValidation", validateEmsCode(emsCode));
        result.put("cbmCheck", calculateCbmAndCheck(width, height, depth, quantity));
        result.put("thbCheck", checkThbThreshold(totalPrice));
        result.put("memberCodeCheck", checkMemberCode(memberCode));
        
        // 종합 상태
        boolean hasWarnings = false;
        if (!(Boolean)result.get("hsCodeValidation")) hasWarnings = true;
        if (!(Boolean)result.get("emsCodeValidation")) hasWarnings = true;
        if ((Boolean)((Map)result.get("cbmCheck")).get("exceedsThreshold")) hasWarnings = true;
        if ((Boolean)((Map)result.get("thbCheck")).get("exceedsThreshold")) hasWarnings = true;
        if ((Boolean)((Map)result.get("memberCodeCheck")).get("causesDelay")) hasWarnings = true;
        
        result.put("hasWarnings", hasWarnings);
        result.put("validationTimestamp", System.currentTimeMillis());
        
        return result;
    }
}