package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    
    private Long userId;

    @Valid
    @NotNull(message = "수취인 정보는 필수입니다.")
    private Recipient recipient;

    @Valid
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    private List<Item> items;

    @Valid
    @NotEmpty(message = "주문 박스는 최소 1개 이상이어야 합니다.")
    private List<Box> boxes;

    @Valid
    @NotNull(message = "배송 정보는 필수입니다.")
    private Shipping shipping;

    @Valid
    @NotNull(message = "결제 정보는 필수입니다.")
    private Payment payment;
    
    // Convenience methods
    public String getRecipientName() {
        return recipient != null ? recipient.getName() : null;
    }
    
    public String getRecipientPhone() {
        return recipient != null ? recipient.getPhone() : null;
    }
    
    public String getRecipientAddress() {
        return recipient != null ? recipient.getAddress() : null;
    }
    
    public String getRecipientCountry() {
        return recipient != null ? recipient.getCountry() : null;
    }
    
    public boolean isUrgent() {
        return shipping != null && "urgent".equals(shipping.getUrgency());
    }
    
    public boolean isNeedsRepacking() {
        return shipping != null && shipping.isNeedsRepacking();
    }
    
    public String getSpecialInstructions() {
        return shipping != null ? shipping.getSpecialInstructions() : null;
    }

    /**
     * 수취인 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {
        @NotNull(message = "수취인 이름은 필수입니다.")
        @Size(max = 100, message = "수취인 이름은 100자를 초과할 수 없습니다.")
        private String name;

        @NotNull(message = "수취인 전화번호는 필수입니다.")
        @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
        private String phone;

        @NotNull(message = "수취인 주소는 필수입니다.")
        @Size(max = 500, message = "주소는 500자를 초과할 수 없습니다.")
        private String address;

        @NotNull(message = "우편번호는 필수입니다.")
        @Size(max = 10, message = "우편번호는 10자를 초과할 수 없습니다.")
        private String zipCode;

        @NotNull(message = "국가는 필수입니다.")
        @Size(max = 3, message = "국가 코드는 3자를 초과할 수 없습니다.")
        private String country;
    }

    /**
     * 주문 상품 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        @NotNull(message = "상품명은 필수입니다.")
        @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다.")
        private String name;

        @Size(max = 1000, message = "상품 설명은 1000자를 초과할 수 없습니다.")
        private String description;

        @NotNull(message = "상품 카테고리는 필수입니다.")
        @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다.")
        private String category;

        @NotNull(message = "수량은 필수입니다.")
        private Integer quantity;

        @NotNull(message = "무게는 필수입니다.")
        private java.math.BigDecimal weight;

        @NotNull(message = "금액은 필수입니다.")
        private java.math.BigDecimal amount;

        @NotNull(message = "통화는 필수입니다.")
        @Size(max = 3, message = "통화 코드는 3자를 초과할 수 없습니다.")
        private String currency;

        @Size(max = 20, message = "HS 코드는 20자를 초과할 수 없습니다.")
        private String hsCode;
        
        private Integer itemOrder;
        private java.math.BigDecimal unitWeight;
        private java.math.BigDecimal unitPrice;
        private String countryOfOrigin;
        private boolean restricted;
        private String restrictionNote;
    }

    /**
     * 주문 박스 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Box {
        @NotNull(message = "박스 너비는 필수입니다.")
        private java.math.BigDecimal width;

        @NotNull(message = "박스 높이는 필수입니다.")
        private java.math.BigDecimal height;

        @NotNull(message = "박스 깊이는 필수입니다.")
        private java.math.BigDecimal depth;

        @NotNull(message = "박스 무게는 필수입니다.")
        private java.math.BigDecimal weight;
        
        private Integer boxNumber;
        private java.math.BigDecimal widthCm;
        private java.math.BigDecimal heightCm;
        private java.math.BigDecimal depthCm;
        private java.math.BigDecimal weightKg;
        private String notes;
    }

    /**
     * 배송 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Shipping {
        @NotNull(message = "선호 배송 방법은 필수입니다.")
        @Size(max = 10, message = "배송 방법은 10자를 초과할 수 없습니다.")
        private String preferredType; // "sea" 또는 "air"

        @Size(max = 20, message = "긴급도는 20자를 초과할 수 없습니다.")
        private String urgency; // "normal", "urgent", "express"

        private boolean needsRepacking;

        @Size(max = 1000, message = "특별 지시사항은 1000자를 초과할 수 없습니다.")
        private String specialInstructions;
        
        // Convenience methods
        public boolean getNeedsRepacking() {
            return needsRepacking;
        }
        
        public boolean isNeedsRepacking() {
            return needsRepacking;
        }
    }

    /**
     * 결제 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payment {
        @NotNull(message = "결제 방법은 필수입니다.")
        @Size(max = 20, message = "결제 방법은 20자를 초과할 수 없습니다.")
        private String method; // "credit_card", "bank_transfer", "paypal", etc.
    }
    
    // Aliases for backward compatibility
    public static class ItemInfo extends Item {}
    public static class BoxInfo extends Box {}
} 