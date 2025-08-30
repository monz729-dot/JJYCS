package com.ysc.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.time.Duration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessLogicService {
    
    private final WebClient webClient;
    
    @Value("${app.api.data-go-kr.service-key:}")
    private String dataGoKrServiceKey;
    
    @Value("${app.api.customs.api-key:}")
    private String customsApiKey;
    
    @Value("${app.api.data-go-kr.base-url:http://apis.data.go.kr/1390802/ems}")
    private String dataGoKrBaseUrl;
    
    @Value("${app.api.customs.base-url:https://unipass.customs.go.kr/ext/rest}")
    private String customsBaseUrl;
    
    // 비즈니스 규칙 상수들
    private static final BigDecimal CBM_THRESHOLD = new BigDecimal("29.0");
    private static final BigDecimal THB_THRESHOLD = new BigDecimal("1500.0");
    
    /**
     * EMS 코드 검증 - data.go.kr API 연동
     */
    public boolean validateEmsCode(String emsCode) {
        if (emsCode == null || emsCode.trim().isEmpty()) {
            return false;
        }
        
        // EMS 코드 형식 체크 (예: EE123456789KR)
        String pattern = "^[A-Z]{2}\\d{9}[A-Z]{2}$";
        boolean isValidFormat = emsCode.matches(pattern);
        
        if (!isValidFormat) {
            log.warn("Invalid EMS code format: {}", emsCode);
            return false;
        }
        
        // 실제 data.go.kr API 호출
        if (dataGoKrServiceKey == null || dataGoKrServiceKey.trim().isEmpty()) {
            log.warn("data.go.kr service key not configured, using format validation only");
            return isValidFormat;
        }
        
        try {
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("http")
                    .host("apis.data.go.kr")
                    .path("/1390802/ems/getEmsTracking")
                    .queryParam("serviceKey", dataGoKrServiceKey)
                    .queryParam("ems_number", emsCode)
                    .queryParam("type", "json")
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            // API 응답 처리
            if (response != null && !response.contains("ERROR")) {
                log.info("EMS code validation successful: {}", emsCode);
                return true;
            } else {
                log.warn("EMS code not found in tracking system: {}", emsCode);
                return false;
            }
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("EMS code not found: {}", emsCode);
                return false;
            } else {
                log.error("EMS API error (status {}): {}", e.getStatusCode(), e.getMessage());
                // API 오류 시 형식 검증 결과 반환
                return isValidFormat;
            }
        } catch (Exception e) {
            log.error("EMS validation error for {}: {}", emsCode, e.getMessage());
            // 오류 시 형식 검증 결과 반환
            return isValidFormat;
        }
    }
    
    /**
     * HS 코드 검증 - 관세청 API 연동
     */
    public boolean validateHsCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return false;
        }
        
        // HS 코드 형식 체크 (4-10자리 숫자, 점 포함 가능)
        String pattern = "^\\d{4}(\\.\\d{1,6})?$";
        boolean isValidFormat = hsCode.matches(pattern);
        
        if (!isValidFormat) {
            log.warn("Invalid HS code format: {}", hsCode);
            return false;
        }
        
        // 관세청 API 호출
        if (customsApiKey == null || customsApiKey.trim().isEmpty()) {
            log.warn("Customs API key not configured, using format validation only");
            return isValidFormat;
        }
        
        try {
            // HS 코드에서 점 제거 (API는 10자리 숫자만 받음)
            String hsCodeForApi = hsCode.replace(".", "");
            if (hsCodeForApi.length() < 10) {
                hsCodeForApi = hsCodeForApi + "0000000000".substring(hsCodeForApi.length());
            }
            final String finalHsCodeForApi = hsCodeForApi; // 람다에서 사용할 final 변수
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("unipass.customs.go.kr")
                    .path("/ext/rest/hsCodeInqry/hsCodeInqry.do")
                    .queryParam("apiKey", customsApiKey)
                    .queryParam("hsCode", finalHsCodeForApi.substring(0, 10))
                    .queryParam("responseType", "JSON")
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            // API 응답 처리
            if (response != null && response.contains("\"resultCode\":\"SUCCESS\"")) {
                log.info("HS code validation successful: {}", hsCode);
                return true;
            } else if (response != null && response.contains("\"resultCode\":\"ERROR\"")) {
                log.warn("HS code not found in customs database: {}", hsCode);
                return false;
            } else {
                log.warn("Invalid HS code response for: {}", hsCode);
                return false;
            }
            
        } catch (WebClientResponseException e) {
            log.error("Customs API error (status {}): {}", e.getStatusCode(), e.getMessage());
            // API 오류 시 형식 검증 결과 반환
            return isValidFormat;
        } catch (Exception e) {
            log.error("HS code validation error for {}: {}", hsCode, e.getMessage());
            // 오류 시 형식 검증 결과 반환
            return isValidFormat;
        }
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