package com.ysc.lms.controller;

import com.ysc.lms.service.HSCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hscode")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class HSCodeController {

    private final HSCodeService hsCodeService;

    /**
     * 품목명으로 HS Code 검색
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER') or hasRole('CORPORATE')")
    public ResponseEntity<?> searchHSCodeByProduct(@RequestParam String productName) {
        try {
            log.info("Searching HS Code for product: {}", productName);
            
            if (productName == null || productName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "품목명을 입력해주세요."));
            }

            HSCodeService.HSCodeSearchResponse response = hsCodeService.searchHSCodeByProductName(productName.trim());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "HS 코드 검색이 완료되었습니다.",
                    "data", response
                ));
            } else {
                // 외부 API 오류 시 샘플 데이터로 응답 (개발/테스트 목적)
                log.warn("External API failed, returning fallback data for: {}", productName);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "HS 코드 검색이 완료되었습니다. (외부 API 연결 제한으로 샘플 데이터)",
                    "data", createFallbackHSCodeResponse(productName),
                    "fallback", true
                ));
            }
        } catch (Exception e) {
            log.error("Error searching HS code for product: {}", productName, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "HS 코드 검색 중 오류가 발생했습니다."));
        }
    }

    /**
     * HS Code로 품목명 검색
     */
    @GetMapping("/search/by-hscode")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER') or hasRole('CORPORATE')")
    public ResponseEntity<?> searchProductNameByHSCode(@RequestParam String hsCode) {
        try {
            log.info("Searching product name for HS Code: {}", hsCode);
            
            if (hsCode == null || hsCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "HS Code를 입력해주세요."));
            }

            HSCodeService.HSCodeSearchResponse response = hsCodeService.searchProductNameByHSCode(hsCode.trim());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "품목명 검색이 완료되었습니다.",
                    "data", response
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", response.getErrorMessage()));
            }
        } catch (Exception e) {
            log.error("Error searching product name for HS code: {}", hsCode, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "품목명 검색 중 오류가 발생했습니다."));
        }
    }

    /**
     * HS Code로 관세율 조회
     */
    @GetMapping("/tariff/{hsCode}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER') or hasRole('CORPORATE')")
    public ResponseEntity<?> getTariffRate(@PathVariable String hsCode) {
        try {
            log.info("Getting tariff rate for HS Code: {}", hsCode);
            
            if (hsCode == null || hsCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "HS Code를 입력해주세요."));
            }

            HSCodeService.TariffRateResponse response = hsCodeService.getTariffRate(hsCode.trim());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "관세율 조회가 완료되었습니다.",
                    "data", response
                ));
            } else {
                // 외부 API 오류 시 샘플 관세율 데이터로 응답
                log.warn("External tariff API failed, returning fallback data for HS Code: {}", hsCode);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "관세율 조회가 완료되었습니다. (외부 API 연결 제한으로 샘플 데이터)",
                    "data", createFallbackTariffRateResponse(hsCode),
                    "fallback", true
                ));
            }
        } catch (Exception e) {
            log.error("Error getting tariff rate for HS code: {}", hsCode, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "관세율 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 관세 환율 정보 조회
     */
    @GetMapping("/exchange-rate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER') or hasRole('CORPORATE')")
    public ResponseEntity<?> getTariffExchangeRate() {
        try {
            log.info("Getting tariff exchange rate");
            
            HSCodeService.TariffExchangeResponse response = hsCodeService.getTariffExchangeRate();
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "환율 정보 조회가 완료되었습니다.",
                    "data", response
                ));
            } else {
                // 외부 API 오류 시 샘플 환율 데이터로 응답
                log.warn("External exchange rate API failed, returning fallback data");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "환율 정보 조회가 완료되었습니다. (외부 API 연결 제한으로 샘플 데이터)",
                    "data", createFallbackExchangeRateResponse(),
                    "fallback", true
                ));
            }
        } catch (Exception e) {
            log.error("Error getting tariff exchange rate", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "환율 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 관세 계산 (HS Code, 수량, 가격 기준)
     */
    @PostMapping("/calculate-duty")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER') or hasRole('CORPORATE')")
    public ResponseEntity<?> calculateCustomsDuty(@RequestBody CalculateDutyRequest request) {
        try {
            log.info("Calculating customs duty for HS Code: {}", request.getHsCode());
            
            if (request.getHsCode() == null || request.getHsCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "HS Code를 입력해주세요."));
            }

            if (request.getValue() == null || request.getValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효한 신고가격을 입력해주세요."));
            }

            // 관세율 조회
            HSCodeService.TariffRateResponse tariffResponse = hsCodeService.getTariffRate(request.getHsCode());
            
            if (!tariffResponse.isSuccess() || tariffResponse.getItems().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "해당 HS Code의 관세율을 찾을 수 없습니다."));
            }

            // 환율 조회 (필요한 경우)
            HSCodeService.TariffExchangeResponse exchangeResponse = hsCodeService.getTariffExchangeRate();
            
            // 관세 계산 로직
            CalculateDutyResponse response = calculateDuty(request, tariffResponse, exchangeResponse);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관세 계산이 완료되었습니다.",
                "data", response
            ));
        } catch (Exception e) {
            log.error("Failed to calculate customs duty for HS code: {}", request.getHsCode(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "관세 계산 중 오류가 발생했습니다."));
        }
    }

    private CalculateDutyResponse calculateDuty(
            CalculateDutyRequest request, 
            HSCodeService.TariffRateResponse tariffResponse,
            HSCodeService.TariffExchangeResponse exchangeResponse) {
        
        HSCodeService.TariffRateItem tariffItem = tariffResponse.getItems().get(0);
        
        CalculateDutyResponse response = new CalculateDutyResponse();
        response.setHsCode(request.getHsCode());
        response.setProductName(tariffItem.getKoreanName());
        response.setBasicRate(tariffItem.getBasicRate());
        response.setWtoRate(tariffItem.getWtoRate());
        response.setSpecialRate(tariffItem.getSpecialRate());
        
        // 기본 관세율로 계산 (실제로는 협정 관세율 등을 고려해야 함)
        java.math.BigDecimal dutyRate = tariffItem.getWtoRate().compareTo(java.math.BigDecimal.ZERO) > 0 
                ? tariffItem.getWtoRate() : tariffItem.getBasicRate();
        
        java.math.BigDecimal dutyAmount = request.getValue()
                .multiply(dutyRate)
                .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        
        response.setAppliedRate(dutyRate);
        response.setDutyAmount(dutyAmount);
        response.setTotalAmount(request.getValue().add(dutyAmount));
        
        return response;
    }

    /**
     * 캐시 통계 조회 (관리자 전용)
     */
    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCacheStats() {
        try {
            Map<String, Object> stats = hsCodeService.getCacheStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "캐시 통계 조회가 완료되었습니다.",
                "data", stats
            ));
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "캐시 통계 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 캐시 초기화 (관리자 전용)
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearCache() {
        try {
            hsCodeService.clearAllCache();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "모든 캐시가 성공적으로 초기화되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "캐시 초기화 중 오류가 발생했습니다."));
        }
    }

    /**
     * 만료된 캐시 정리 (관리자 전용)
     */
    @PostMapping("/cache/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupCache() {
        try {
            hsCodeService.cleanupExpiredCache();
            Map<String, Object> stats = hsCodeService.getCacheStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "만료된 캐시가 정리되었습니다.",
                "data", stats
            ));
        } catch (Exception e) {
            log.error("Error cleaning up cache", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "캐시 정리 중 오류가 발생했습니다."));
        }
    }

    // Fallback data methods for external API failures
    private Map<String, Object> createFallbackHSCodeResponse(String productName) {
        return Map.of(
            "success", true,
            "items", java.util.List.of(Map.of(
                "hsCode", "8471600000",
                "koreanName", "샘플 품목명: " + productName,
                "englishName", "Sample Product: " + productName,
                "description", "외부 API 연결 제한으로 인한 샘플 데이터입니다."
            ))
        );
    }
    
    private Map<String, Object> createFallbackTariffRateResponse(String hsCode) {
        return Map.of(
            "success", true,
            "items", java.util.List.of(Map.of(
                "hsCode", hsCode,
                "basicRate", "8.0",
                "wtoRate", "5.0", 
                "specialRate", "0.0",
                "koreanName", "샘플 관세율 정보",
                "description", "외부 API 연결 제한으로 인한 샘플 데이터입니다."
            ))
        );
    }
    
    private Map<String, Object> createFallbackExchangeRateResponse() {
        return Map.of(
            "success", true,
            "items", java.util.List.of(Map.of(
                "currency", "USD",
                "rate", "1350.00",
                "date", java.time.LocalDate.now().toString(),
                "description", "외부 API 연결 제한으로 인한 샘플 환율입니다."
            ))
        );
    }

    // Request/Response DTOs
    public static class CalculateDutyRequest {
        private String hsCode;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal value; // 신고가격
        private String currency = "USD";

        // Getters and setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public java.math.BigDecimal getQuantity() { return quantity; }
        public void setQuantity(java.math.BigDecimal quantity) { this.quantity = quantity; }
        public java.math.BigDecimal getValue() { return value; }
        public void setValue(java.math.BigDecimal value) { this.value = value; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class CalculateDutyResponse {
        private String hsCode;
        private String productName;
        private java.math.BigDecimal basicRate;
        private java.math.BigDecimal wtoRate;
        private java.math.BigDecimal specialRate;
        private java.math.BigDecimal appliedRate;
        private java.math.BigDecimal dutyAmount;
        private java.math.BigDecimal totalAmount;

        // Getters and setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public java.math.BigDecimal getBasicRate() { return basicRate; }
        public void setBasicRate(java.math.BigDecimal basicRate) { this.basicRate = basicRate; }
        public java.math.BigDecimal getWtoRate() { return wtoRate; }
        public void setWtoRate(java.math.BigDecimal wtoRate) { this.wtoRate = wtoRate; }
        public java.math.BigDecimal getSpecialRate() { return specialRate; }
        public void setSpecialRate(java.math.BigDecimal specialRate) { this.specialRate = specialRate; }
        public java.math.BigDecimal getAppliedRate() { return appliedRate; }
        public void setAppliedRate(java.math.BigDecimal appliedRate) { this.appliedRate = appliedRate; }
        public java.math.BigDecimal getDutyAmount() { return dutyAmount; }
        public void setDutyAmount(java.math.BigDecimal dutyAmount) { this.dutyAmount = dutyAmount; }
        public java.math.BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}