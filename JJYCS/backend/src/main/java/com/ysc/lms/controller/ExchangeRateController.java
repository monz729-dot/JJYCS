package com.ysc.lms.controller;

import com.ysc.lms.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/exchange-rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    
    /**
     * 모든 환율 정보 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllExchangeRates() {
        try {
            Map<String, BigDecimal> rates = exchangeRateService.getAllExchangeRates();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rates", rates);
            response.put("baseCurrency", "KRW");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get exchange rates", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "환율 정보 조회에 실패했습니다."));
        }
    }
    
    /**
     * 특정 통화 환율 조회
     */
    @GetMapping("/{currency}")
    public ResponseEntity<Map<String, Object>> getExchangeRate(@PathVariable String currency) {
        try {
            BigDecimal rate = exchangeRateService.getExchangeRate(currency);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("currency", currency.toUpperCase());
            response.put("rate", rate);
            response.put("baseCurrency", "KRW");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get exchange rate for {}", currency, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "환율 정보 조회에 실패했습니다."));
        }
    }
    
    /**
     * 통화 변환
     */
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(@RequestBody ConvertRequest request) {
        try {
            BigDecimal convertedAmount = exchangeRateService.convert(
                request.getAmount(),
                request.getFromCurrency(),
                request.getToCurrency()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("originalAmount", request.getAmount());
            response.put("fromCurrency", request.getFromCurrency());
            response.put("convertedAmount", convertedAmount);
            response.put("toCurrency", request.getToCurrency());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to convert currency", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "통화 변환에 실패했습니다."));
        }
    }
    
    /**
     * 환율 업데이트 (관리자용)
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateExchangeRates() {
        try {
            exchangeRateService.updateExchangeRates();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "환율 정보가 업데이트되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Failed to update exchange rates", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "환율 업데이트에 실패했습니다."));
        }
    }
    
    /**
     * 통화 변환 요청 DTO
     */
    public static class ConvertRequest {
        private BigDecimal amount;
        private String fromCurrency;
        private String toCurrency;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
        
        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    }
}