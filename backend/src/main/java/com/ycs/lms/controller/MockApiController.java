package com.ycs.lms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mock")
@Profile({"dev", "local"})
@Slf4j
public class MockApiController {

    /**
     * EMS 코드 검증 Mock API
     */
    @GetMapping("/ems/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateEMS(@PathVariable String code) {
        log.info("Mock EMS validation request for code: {}", code);
        
        Map<String, Object> response = new HashMap<>();
        
        // 인위적인 지연 시뮬레이션
        try {
            Thread.sleep(500 + (long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock validation logic
        if (code == null || code.length() < 10) {
            response.put("valid", false);
            response.put("message", "EMS 코드 형식이 올바르지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 90% 성공률
        boolean valid = Math.random() > 0.1;
        
        response.put("valid", valid);
        
        if (valid) {
            response.put("status", "active");
            response.put("destination", "Thailand");
            response.put("estimatedDelivery", LocalDateTime.now().plusDays(7).toString());
            response.put("serviceType", "express");
            response.put("message", "EMS 코드가 유효합니다.");
        } else {
            response.put("message", "EMS 코드를 찾을 수 없거나 비활성 상태입니다.");
        }
        
        log.info("Mock EMS validation response: valid={}, code={}", valid, code);
        return ResponseEntity.ok(response);
    }

    /**
     * HS 코드 조회 Mock API
     */
    @GetMapping("/hs/lookup/{code}")
    public ResponseEntity<Map<String, Object>> lookupHS(@PathVariable String code) {
        log.info("Mock HS code lookup request for code: {}", code);
        
        Map<String, Object> response = new HashMap<>();
        
        // 인위적인 지연 시뮬레이션
        try {
            Thread.sleep(300 + (long)(Math.random() * 700));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock validation logic
        if (code == null || !code.matches("^\\d{4}\\.\\d{2}\\.\\d{2}$")) {
            response.put("valid", false);
            response.put("message", "HS 코드 형식이 올바르지 않습니다. (예: 3304.99.00)");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 95% 성공률
        boolean valid = Math.random() > 0.05;
        
        response.put("code", code);
        response.put("valid", valid);
        
        if (valid) {
            // Mock HS code categories
            Map<String, String[]> categories = new HashMap<>();
            categories.put("3304", new String[]{"화장품", "미용용품", "cosmetics"});
            categories.put("6109", new String[]{"티셔츠", "의류", "apparel"});
            categories.put("8517", new String[]{"전자제품", "통신기기", "electronics"});
            categories.put("9999", new String[]{"기타", "일반상품", "general"});
            
            String prefix = code.substring(0, 4);
            String[] categoryInfo = categories.getOrDefault(prefix, categories.get("9999"));
            
            response.put("description", categoryInfo[0] + " 관련 제품");
            response.put("category", categoryInfo[2]);
            
            // Mock restrictions
            List<Map<String, Object>> restrictions = new ArrayList<>();
            
            if (Math.random() > 0.7) { // 30% 확률로 제한 사항 추가
                Map<String, Object> restriction = new HashMap<>();
                restriction.put("country", "TH");
                restriction.put("type", "quantity_limit");
                restriction.put("value", "개인용 소량만 허용");
                restriction.put("note", "상업적 목적의 대량 수입 시 별도 허가 필요");
                restrictions.add(restriction);
            }
            
            response.put("restrictions", restrictions);
            response.put("tariffRate", new BigDecimal(Math.random() * 15).setScale(2, BigDecimal.ROUND_HALF_UP));
            response.put("message", "HS 코드 정보를 성공적으로 조회했습니다.");
            
        } else {
            response.put("message", "HS 코드를 찾을 수 없습니다.");
        }
        
        log.info("Mock HS code lookup response: valid={}, code={}", valid, code);
        return ResponseEntity.ok(response);
    }

    /**
     * 환율 조회 Mock API
     */
    @GetMapping("/exchange")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @RequestParam String from, 
            @RequestParam String to) {
        
        log.info("Mock exchange rate request: {} -> {}", from, to);
        
        Map<String, Object> response = new HashMap<>();
        
        // 인위적인 지연 시뮬레이션
        try {
            Thread.sleep(200 + (long)(Math.random() * 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        response.put("from", from);
        response.put("to", to);
        response.put("timestamp", System.currentTimeMillis());
        
        // Mock exchange rates (approximate real rates with some variation)
        double baseRate = getBaseExchangeRate(from, to);
        double variation = (Math.random() - 0.5) * 0.05; // ±2.5% 변동
        double finalRate = baseRate * (1 + variation);
        
        response.put("rate", BigDecimal.valueOf(finalRate).setScale(4, BigDecimal.ROUND_HALF_UP));
        response.put("source", "mock-api");
        response.put("lastUpdated", LocalDateTime.now().toString());
        
        log.info("Mock exchange rate response: {} -> {} = {}", from, to, finalRate);
        return ResponseEntity.ok(response);
    }

    /**
     * 배송 추적 Mock API
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Map<String, Object>> trackShipment(@PathVariable String trackingNumber) {
        log.info("Mock tracking request for: {}", trackingNumber);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Thread.sleep(400 + (long)(Math.random() * 600));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        response.put("trackingNumber", trackingNumber);
        response.put("carrier", "Mock Express");
        
        // Mock tracking statuses
        String[] statuses = {"in_transit", "customs", "out_for_delivery", "delivered"};
        String currentStatus = statuses[(int)(Math.random() * statuses.length)];
        
        response.put("status", currentStatus);
        response.put("currentLocation", "Bangkok Distribution Center");
        response.put("estimatedDelivery", LocalDateTime.now().plusDays(2).toString());
        
        // Mock tracking events
        List<Map<String, Object>> events = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> event = new HashMap<>();
            event.put("timestamp", LocalDateTime.now().minusHours(i * 6).toString());
            event.put("location", "Processing Center " + (i + 1));
            event.put("description", "Package processed at facility");
            event.put("status", i == 0 ? currentStatus : "in_transit");
            events.add(event);
        }
        
        response.put("events", events);
        response.put("lastUpdated", LocalDateTime.now().toString());
        
        log.info("Mock tracking response: {} -> {}", trackingNumber, currentStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * 알림 전송 Mock API
     */
    @PostMapping("/notifications/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> request) {
        log.info("Mock notification send request: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Thread.sleep(100 + (long)(Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 95% 성공률
        boolean success = Math.random() > 0.05;
        
        response.put("success", success);
        response.put("messageId", "mock-msg-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now().toString());
        
        if (success) {
            response.put("message", "알림이 성공적으로 전송되었습니다.");
        } else {
            response.put("message", "알림 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
        
        log.info("Mock notification response: success={}", success);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 검증 Mock API
     */
    @PostMapping("/payment/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
        log.info("Mock payment verification request: {}", request.get("transactionId"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Thread.sleep(800 + (long)(Math.random() * 1200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String transactionId = (String) request.get("transactionId");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        // 90% 성공률
        boolean success = Math.random() > 0.1;
        
        response.put("transactionId", transactionId);
        response.put("verified", success);
        response.put("amount", amount);
        response.put("currency", request.get("currency"));
        response.put("timestamp", LocalDateTime.now().toString());
        
        if (success) {
            response.put("status", "completed");
            response.put("message", "결제가 성공적으로 확인되었습니다.");
            response.put("receiptUrl", "https://mock-payment.com/receipt/" + transactionId);
        } else {
            response.put("status", "failed");
            response.put("message", "결제 확인에 실패했습니다. 거래 정보를 다시 확인해주세요.");
        }
        
        log.info("Mock payment verification response: verified={}, id={}", success, transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 기본 환율 정보 (실제 환율 근사값)
     */
    private double getBaseExchangeRate(String from, String to) {
        String pair = from + "/" + to;
        
        Map<String, Double> rates = new HashMap<>();
        rates.put("THB/KRW", 36.5);
        rates.put("KRW/THB", 0.0274);
        rates.put("USD/KRW", 1320.0);
        rates.put("KRW/USD", 0.000758);
        rates.put("USD/THB", 35.8);
        rates.put("THB/USD", 0.0279);
        rates.put("EUR/KRW", 1450.0);
        rates.put("KRW/EUR", 0.00069);
        rates.put("JPY/KRW", 8.85);
        rates.put("KRW/JPY", 0.113);
        
        return rates.getOrDefault(pair, 1.0);
    }
}