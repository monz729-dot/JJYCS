package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 요약 DTO (목록 조회용)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary {

    private Long orderId;
    private String orderCode;
    private String status;
    private String orderType;
    private BigDecimal totalAmount;
    private String currency;
    private String recipientName;
    private String recipientCountry;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryDate;
    private boolean requiresExtraRecipient;
} 