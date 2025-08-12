package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderCode;
    private String status;
    private String orderType;
    private BigDecimal totalCBM;
    private BigDecimal totalWeight;
    private BigDecimal totalAmount;
    private String currency;
    private boolean requiresExtraRecipient;
    private boolean hasMemberCodeIssue;
    
    private RecipientInfo recipient;
    private List<ItemInfo> items;
    private List<BoxInfo> boxes;
    private ShippingInfo shipping;
    
    private LocalDate estimatedDelivery;
    private LocalDate actualDelivery;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private UserInfo user;
    private List<StatusTimeline> timeline;
    private List<Warning> warnings;
    
    public void addWarning(String type, String message, Object details) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(Warning.builder()
            .type(type)
            .message(message)
            .details(details)
            .build());
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        private String name;
        private String phone;
        private String address;
        private String zipCode;
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        private Long itemId;
        private String name;
        private String description;
        private String category;
        private int quantity;
        private BigDecimal unitWeight;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String currency;
        private String hsCode;
        private String emsCode;
        private String brand;
        private String model;
        private boolean restricted;
        private String restrictionNote;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoxInfo {
        private Long boxId;
        private int boxNumber;
        private String labelCode;
        private String qrCodeUrl;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal depth;
        private BigDecimal cbm;
        private BigDecimal weight;
        private String status;
        private String trackingNumber;
        private String carrier;
        private LocalDateTime shippedDate;
        private List<Long> itemIds;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        private String urgency;
        private boolean needsRepacking;
        private String specialInstructions;
        private String paymentMethod;
        private String paymentStatus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String memberCode;
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusTimeline {
        private String status;
        private LocalDateTime timestamp;
        private String note;
        private String updatedBy;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Warning {
        private String type;
        private String message;
        private Object details;
    }
}