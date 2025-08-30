package com.ysc.lms.controller;

import com.ysc.lms.entity.Billing;
import com.ysc.lms.service.BillingService;
import com.ysc.lms.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * P0-3: 청구/결제 로직 및 환율 시스템 API
 * - 선청구 기능 (배송 완료 전 청구 가능)
 * - 청구서 미리보기 (고객 확인용)
 * - 이중 통화 표시 (THB/KRW)
 * - 다중 결제 수단 지원
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    
    private final BillingService billingService;
    private final ExchangeRateService exchangeRateService;
    
    /**
     * 청구서 미리보기 생성 (고객 결제 전 확인용)
     */
    @PostMapping("/preview")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<Map<String, Object>> generateBillingPreview(@RequestBody BillingPreviewRequest request) {
        try {
            BillingService.BillingPreview preview = billingService.generateBillingPreview(
                request.getOrderId(), 
                createBillingRequest(request)
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", preview,
                "message", "청구서 미리보기가 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error generating billing preview for order: {}", request.getOrderId(), e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 청구서 생성 (선청구 지원)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBilling(@RequestBody CreateBillingRequest request) {
        try {
            Billing billing = billingService.createBilling(
                request.getOrderId(), 
                createBillingRequest(request)
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", billing,
                "message", "청구서가 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error creating billing for order: {}", request.getOrderId(), e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 최종 청구서 발행
     */
    @PostMapping("/{billingId}/issue-final")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> issueFinalBilling(@PathVariable Long billingId) {
        try {
            Billing billing = billingService.issueFinalBilling(billingId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", billing,
                "message", "최종 청구서가 발행되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error issuing final billing: {}", billingId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 결제 확인 처리
     */
    @PostMapping("/{billingId}/confirm-payment")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable Long billingId, 
            @RequestBody BillingService.PaymentConfirmRequest request) {
        try {
            Billing billing = billingService.confirmPayment(billingId, request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", billing,
                "message", "결제가 확인되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error confirming payment for billing: {}", billingId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 현재 사용자의 미결제 청구서 조회
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<Map<String, Object>> getPendingBillings(@RequestParam Long userId) {
        try {
            List<Billing> pendingBillings = billingService.getPendingBillings(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", pendingBillings,
                "count", pendingBillings.size(),
                "message", "미결제 청구서가 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting pending billings for user: {}", userId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 현재 THB-KRW 환율 조회
     */
    @GetMapping("/exchange-rate/current")
    public ResponseEntity<Map<String, Object>> getCurrentExchangeRate() {
        try {
            ExchangeRateService.BillingExchangeRate exchangeRate = 
                exchangeRateService.getThbToKrwRateForBilling(LocalDateTime.now());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "rate", exchangeRate.getRate(),
                    "source", exchangeRate.getSource(),
                    "rateDate", exchangeRate.getRateDate(),
                    "isValid", exchangeRate.isValid()
                ),
                "message", exchangeRate.getMessage()
            ));
            
        } catch (Exception e) {
            log.error("Error getting current exchange rate", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", "환율 조회에 실패했습니다."
                ));
        }
    }
    
    /**
     * 특정 날짜의 환율 조회 (청구일 기준)
     */
    @GetMapping("/exchange-rate/date")
    public ResponseEntity<Map<String, Object>> getExchangeRateByDate(@RequestParam String date) {
        try {
            LocalDateTime targetDate = LocalDateTime.parse(date + "T00:00:00");
            ExchangeRateService.BillingExchangeRate exchangeRate = 
                exchangeRateService.getThbToKrwRateForBilling(targetDate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "rate", exchangeRate.getRate(),
                    "source", exchangeRate.getSource(),
                    "rateDate", exchangeRate.getRateDate(),
                    "isValid", exchangeRate.isValid()
                ),
                "message", exchangeRate.getMessage()
            ));
            
        } catch (Exception e) {
            log.error("Error getting exchange rate for date: {}", date, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", "지정된 날짜의 환율 조회에 실패했습니다."
                ));
        }
    }
    
    /**
     * 청구서 조회 (ID로)
     */
    @GetMapping("/{billingId}")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBilling(@PathVariable Long billingId) {
        try {
            // BillingRepository에서 직접 조회하거나 BillingService에 메서드 추가 필요
            // 현재는 간단하게 성공 응답만 반환
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "청구서 조회 API는 구현 예정입니다.",
                "billingId", billingId
            ));
            
        } catch (Exception e) {
            log.error("Error getting billing: {}", billingId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", e.getMessage()
                ));
        }
    }
    
    private BillingService.BillingRequest createBillingRequest(BillingRequestBase request) {
        BillingService.BillingRequest billingRequest = new BillingService.BillingRequest();
        billingRequest.setShippingFee(request.getShippingFee());
        billingRequest.setLocalDeliveryFee(request.getLocalDeliveryFee());
        billingRequest.setRepackingFee(request.getRepackingFee());
        billingRequest.setHandlingFee(request.getHandlingFee());
        billingRequest.setInsuranceFee(request.getInsuranceFee());
        billingRequest.setCustomsFee(request.getCustomsFee());
        return billingRequest;
    }
    
    // DTO 클래스들
    
    public static class BillingRequestBase {
        private Long orderId;
        private java.math.BigDecimal shippingFee = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal localDeliveryFee = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal repackingFee = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal handlingFee = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal insuranceFee = java.math.BigDecimal.ZERO;
        private java.math.BigDecimal customsFee = java.math.BigDecimal.ZERO;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public java.math.BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(java.math.BigDecimal shippingFee) { this.shippingFee = shippingFee; }
        
        public java.math.BigDecimal getLocalDeliveryFee() { return localDeliveryFee; }
        public void setLocalDeliveryFee(java.math.BigDecimal localDeliveryFee) { this.localDeliveryFee = localDeliveryFee; }
        
        public java.math.BigDecimal getRepackingFee() { return repackingFee; }
        public void setRepackingFee(java.math.BigDecimal repackingFee) { this.repackingFee = repackingFee; }
        
        public java.math.BigDecimal getHandlingFee() { return handlingFee; }
        public void setHandlingFee(java.math.BigDecimal handlingFee) { this.handlingFee = handlingFee; }
        
        public java.math.BigDecimal getInsuranceFee() { return insuranceFee; }
        public void setInsuranceFee(java.math.BigDecimal insuranceFee) { this.insuranceFee = insuranceFee; }
        
        public java.math.BigDecimal getCustomsFee() { return customsFee; }
        public void setCustomsFee(java.math.BigDecimal customsFee) { this.customsFee = customsFee; }
    }
    
    public static class BillingPreviewRequest extends BillingRequestBase {
        // 미리보기용 추가 필드들이 필요하면 여기에 추가
    }
    
    public static class CreateBillingRequest extends BillingRequestBase {
        // 실제 청구서 생성용 추가 필드들이 필요하면 여기에 추가
    }
}