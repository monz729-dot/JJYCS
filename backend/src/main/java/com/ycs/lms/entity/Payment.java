package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private Long orderId;
    private Long userId;
    private String paymentCode;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String transactionId;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        CREDIT_CARD,  // 신용카드
        BANK_TRANSFER, // 계좌이체
        PAYPAL,       // 페이팔
        CASH,         // 현금
        CHECK         // 수표
    }

    public enum PaymentStatus {
        PENDING,      // 대기중
        PAID,         // 결제완료
        FAILED,       // 결제실패
        CANCELLED,    // 취소됨
        REFUNDED,     // 환불됨
        PARTIAL_REFUND // 부분환불
    }
}