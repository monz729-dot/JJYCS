package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long orderId;
    private String orderCode;
    private String status;
    private String orderType;
    private BigDecimal totalAmount;
    private String currency;
    private boolean requiresExtraRecipient;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryDate;
    
    // 수취인 정보
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientZipCode;
    private String recipientCountry;
    
    // 배송 정보
    private String urgency;
    private boolean needsRepacking;
    private String specialInstructions;
    
    // 결제 정보
    private String paymentMethod;
    private String paymentStatus;
    
    // 상품 정보
    private List<OrderItemResponse> items;
    
    // 박스 정보
    private List<OrderBoxResponse> boxes;
    
    // 비즈니스 룰 경고
    private List<BusinessWarning> warnings;

    /**
     * 주문 상품 응답
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemResponse {
        private Long itemId;
        private Integer itemOrder;
        private String name;
        private String description;
        private String category;
        private Integer quantity;
        private BigDecimal unitWeight;
        private BigDecimal totalAmount;
        private String currency;
        private String hsCode;
        private String countryOfOrigin;
    }

    /**
     * 주문 박스 응답
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderBoxResponse {
        private Long boxId;
        private Integer boxNumber;
        private String labelCode;
        private BigDecimal widthCm;
        private BigDecimal heightCm;
        private BigDecimal depthCm;
        private BigDecimal weightKg;
        private BigDecimal cbm;
        private String status;
    }

    /**
     * 비즈니스 룰 경고
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessWarning {
        private String type;
        private String message;
        private java.util.Map<String, Object> details;
    }
} 