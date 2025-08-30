package com.ysc.lms.controller;

import com.ysc.lms.service.TariffCalculationService;
import com.ysc.lms.service.TariffCalculationService.TariffCalculationRequest;
import com.ysc.lms.service.TariffCalculationService.TariffCalculationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tariff")
@RequiredArgsConstructor
@Slf4j
public class TariffController {

    private final TariffCalculationService tariffCalculationService;
    
    /**
     * 관세 계산
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateTariff(@RequestBody TariffCalculationRequest request) {
        try {
            log.info("Calculating tariff for HS Code: {}, Currency: {}, Quantity: {}", 
                request.getHsCode(), request.getCurrency(), request.getQuantity());
            
            // 기본값 설정
            if (request.getCurrency() == null || request.getCurrency().isEmpty()) {
                request.setCurrency("USD");
            }
            
            TariffCalculationResult result = tariffCalculationService.calculateTariff(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("message", "관세 계산이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to calculate tariff", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false, 
                    "error", "관세 계산에 실패했습니다: " + e.getMessage()
                ));
        }
    }
    
    /**
     * 일괄 관세 계산 (여러 품목)
     */
    @PostMapping("/calculate-batch")
    public ResponseEntity<Map<String, Object>> calculateTariffBatch(@RequestBody BatchCalculationRequest request) {
        try {
            Map<String, TariffCalculationResult> results = new HashMap<>();
            
            for (TariffCalculationRequest item : request.getItems()) {
                if (item.getCurrency() == null || item.getCurrency().isEmpty()) {
                    item.setCurrency(request.getDefaultCurrency() != null ? request.getDefaultCurrency() : "USD");
                }
                
                TariffCalculationResult result = tariffCalculationService.calculateTariff(item);
                results.put(item.getHsCode() + "_" + System.currentTimeMillis(), result);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("itemCount", results.size());
            response.put("message", "일괄 관세 계산이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to calculate batch tariff", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false, 
                    "error", "일괄 관세 계산에 실패했습니다: " + e.getMessage()
                ));
        }
    }
    
    /**
     * 간편 관세 계산 (HS Code와 금액만으로)
     */
    @GetMapping("/quick-calculate")
    public ResponseEntity<Map<String, Object>> quickCalculate(
            @RequestParam String hsCode,
            @RequestParam String amount,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "1") String quantity) {
        try {
            TariffCalculationRequest request = new TariffCalculationRequest();
            request.setHsCode(hsCode);
            request.setUnitPrice(new java.math.BigDecimal(amount));
            request.setCurrency(currency);
            request.setQuantity(Integer.parseInt(quantity));
            
            TariffCalculationResult result = tariffCalculationService.calculateTariff(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to quick calculate tariff", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", "간편 관세 계산에 실패했습니다: " + e.getMessage()
                ));
        }
    }
    
    /**
     * 일괄 계산 요청 DTO
     */
    public static class BatchCalculationRequest {
        private java.util.List<TariffCalculationRequest> items;
        private String defaultCurrency;
        
        // Getters and Setters
        public java.util.List<TariffCalculationRequest> getItems() { return items; }
        public void setItems(java.util.List<TariffCalculationRequest> items) { this.items = items; }
        
        public String getDefaultCurrency() { return defaultCurrency; }
        public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    }
}