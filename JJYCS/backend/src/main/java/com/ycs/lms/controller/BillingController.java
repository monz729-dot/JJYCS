package com.ycs.lms.controller;

import com.ycs.lms.entity.Invoice;
import com.ycs.lms.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    /**
     * Proforma Invoice (1차 견적서) 생성
     */
    @PostMapping("/{orderId}/proforma")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> generateProformaInvoice(
            @PathVariable Long orderId,
            Authentication auth) {
        try {
            String createdBy = auth.getName();
            Invoice invoice = billingService.generateProformaInvoice(orderId, createdBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Proforma invoice generated successfully",
                "invoice", invoice
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to generate proforma invoice"));
        }
    }
    
    /**
     * 청구서 발행
     */
    @PostMapping("/invoices/{invoiceId}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> issueInvoice(@PathVariable Long invoiceId) {
        try {
            Invoice invoice = billingService.issueInvoice(invoiceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invoice issued successfully",
                "invoice", invoice
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to issue invoice"));
        }
    }
    
    /**
     * 결제 처리
     */
    @PostMapping("/invoices/{invoiceId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> processPayment(
            @PathVariable Long invoiceId,
            @RequestBody PaymentRequest request) {
        try {
            Invoice invoice = billingService.processPayment(invoiceId, request.getAmount());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment processed successfully",
                "invoice", invoice
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to process payment"));
        }
    }
    
    // DTO 클래스
    public static class PaymentRequest {
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;
        private String notes;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}