package com.ysc.lms.controller;

import com.ysc.lms.service.HSCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hscode")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class HSCodeController {

    private final HSCodeService hsCodeService;

    /**
     * 품목명으로 HS Code 검색
     */
    @GetMapping("/search/by-product")
    public ResponseEntity<?> searchHSCodeByProduct(@RequestParam String productName) {
        log.info("Searching HS Code for product: {}", productName);
        
        if (productName == null || productName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("품목명을 입력해주세요.");
        }

        HSCodeService.HSCodeSearchResponse response = hsCodeService.searchHSCodeByProductName(productName.trim());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * HS Code로 품목명 검색
     */
    @GetMapping("/search/by-hscode")
    public ResponseEntity<?> searchProductNameByHSCode(@RequestParam String hsCode) {
        log.info("Searching product name for HS Code: {}", hsCode);
        
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("HS Code를 입력해주세요.");
        }

        HSCodeService.HSCodeSearchResponse response = hsCodeService.searchProductNameByHSCode(hsCode.trim());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * HS Code로 관세율 조회
     */
    @GetMapping("/tariff/{hsCode}")
    public ResponseEntity<?> getTariffRate(@PathVariable String hsCode) {
        log.info("Getting tariff rate for HS Code: {}", hsCode);
        
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("HS Code를 입력해주세요.");
        }

        HSCodeService.TariffRateResponse response = hsCodeService.getTariffRate(hsCode.trim());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 관세 환율 정보 조회
     */
    @GetMapping("/exchange-rate")
    public ResponseEntity<?> getTariffExchangeRate() {
        log.info("Getting tariff exchange rate");
        
        HSCodeService.TariffExchangeResponse response = hsCodeService.getTariffExchangeRate();
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 관세 계산 (HS Code, 수량, 가격 기준)
     */
    @PostMapping("/calculate-duty")
    public ResponseEntity<?> calculateCustomsDuty(@RequestBody CalculateDutyRequest request) {
        log.info("Calculating customs duty for HS Code: {}", request.getHsCode());
        
        if (request.getHsCode() == null || request.getHsCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("HS Code를 입력해주세요.");
        }

        try {
            // 관세율 조회
            HSCodeService.TariffRateResponse tariffResponse = hsCodeService.getTariffRate(request.getHsCode());
            
            if (!tariffResponse.isSuccess() || tariffResponse.getItems().isEmpty()) {
                return ResponseEntity.status(400).body("해당 HS Code의 관세율을 찾을 수 없습니다.");
            }

            // 환율 조회 (필요한 경우)
            HSCodeService.TariffExchangeResponse exchangeResponse = hsCodeService.getTariffExchangeRate();
            
            // 관세 계산 로직
            CalculateDutyResponse response = calculateDuty(request, tariffResponse, exchangeResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to calculate customs duty", e);
            return ResponseEntity.status(500).body("관세 계산에 실패했습니다: " + e.getMessage());
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