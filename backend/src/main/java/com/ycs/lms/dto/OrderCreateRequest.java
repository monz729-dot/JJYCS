package com.ycs.lms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    
    private Long userId;
    
    @Valid
    @NotNull(message = "수취인 정보는 필수입니다")
    private RecipientInfo recipient;
    
    @Valid
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    private List<ItemInfo> items;
    
    @Valid
    @NotEmpty(message = "박스 정보는 최소 1개 이상이어야 합니다")
    private List<BoxInfo> boxes;
    
    @Valid
    @NotNull(message = "배송 정보는 필수입니다")
    private ShippingInfo shipping;
    
    @Valid
    @NotNull(message = "결제 정보는 필수입니다")
    private PaymentInfo payment;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        @NotBlank(message = "수취인 이름은 필수입니다")
        @Size(max = 100, message = "수취인 이름은 100자 이하여야 합니다")
        private String name;
        
        @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "올바른 전화번호 형식이 아닙니다")
        private String phone;
        
        @NotBlank(message = "수취인 주소는 필수입니다")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다")
        private String address;
        
        @Size(max = 20, message = "우편번호는 20자 이하여야 합니다")
        private String zipCode;
        
        @NotBlank(message = "국가 코드는 필수입니다")
        @Pattern(regexp = "^[A-Z]{2}$", message = "올바른 국가 코드가 아닙니다")
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 500, message = "상품명은 500자 이하여야 합니다")
        private String name;
        
        @Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다")
        private String description;
        
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
        @Max(value = 1000, message = "수량은 1000개 이하여야 합니다")
        private int quantity;
        
        @DecimalMin(value = "0.001", message = "무게는 0.001kg 이상이어야 합니다")
        @DecimalMax(value = "1000.000", message = "무게는 1000kg 이하여야 합니다")
        private BigDecimal weight;
        
        @NotNull(message = "상품 금액은 필수입니다")
        @DecimalMin(value = "0.01", message = "금액은 0.01 이상이어야 합니다")
        private BigDecimal amount;
        
        @NotBlank(message = "통화는 필수입니다")
        @Pattern(regexp = "^(THB|KRW|USD|CNY|VND)$", message = "지원되지 않는 통화입니다")
        private String currency;
        
        @Size(max = 100, message = "카테고리는 100자 이하여야 합니다")
        private String category;
        
        @Pattern(regexp = "^[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}$", message = "올바른 HS 코드 형식이 아닙니다")
        private String hsCode;
        
        @Size(max = 50, message = "EMS 코드는 50자 이하여야 합니다")
        private String emsCode;
        
        @Size(max = 100, message = "브랜드는 100자 이하여야 합니다")
        private String brand;
        
        @Size(max = 100, message = "모델은 100자 이하여야 합니다")
        private String model;
        
        // Additional fields for compatibility
        private Integer itemOrder;
        private BigDecimal unitWeight;
        private BigDecimal unitPrice;
        private String countryOfOrigin;
        private boolean restricted;
        private String restrictionNote;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoxInfo {
        @NotNull(message = "박스 너비는 필수입니다")
        @DecimalMin(value = "1.0", message = "박스 너비는 1cm 이상이어야 합니다")
        @DecimalMax(value = "200.0", message = "박스 너비는 200cm 이하여야 합니다")
        private BigDecimal width; // cm
        
        @NotNull(message = "박스 높이는 필수입니다")
        @DecimalMin(value = "1.0", message = "박스 높이는 1cm 이상이어야 합니다")
        @DecimalMax(value = "200.0", message = "박스 높이는 200cm 이하여야 합니다")
        private BigDecimal height; // cm
        
        @NotNull(message = "박스 깊이는 필수입니다")
        @DecimalMin(value = "1.0", message = "박스 깊이는 1cm 이상이어야 합니다")
        @DecimalMax(value = "200.0", message = "박스 깊이는 200cm 이하여야 합니다")
        private BigDecimal depth; // cm
        
        @NotNull(message = "박스 무게는 필수입니다")
        @DecimalMin(value = "0.1", message = "박스 무게는 0.1kg 이상이어야 합니다")
        @DecimalMax(value = "50.0", message = "박스 무게는 50kg 이하여야 합니다")
        private BigDecimal weight; // kg
        
        @NotEmpty(message = "박스에 포함된 상품 인덱스는 필수입니다")
        private List<Integer> itemIndexes; // 아이템 배열의 인덱스
        
        // Additional fields for compatibility
        private Integer boxNumber;
        private BigDecimal widthCm;
        private BigDecimal heightCm;
        private BigDecimal depthCm;
        private BigDecimal weightKg;
        private String notes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        @Pattern(regexp = "^(sea|air)$", message = "배송 방식은 sea 또는 air만 가능합니다")
        private String preferredType = "sea";
        
        @Pattern(regexp = "^(normal|urgent)$", message = "긴급도는 normal 또는 urgent만 가능합니다")
        private String urgency = "normal";
        
        private boolean needsRepacking = false;
        
        @Size(max = 1000, message = "특별 요청사항은 1000자 이하여야 합니다")
        private String specialInstructions;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        @NotBlank(message = "결제 방식은 필수입니다")
        @Pattern(regexp = "^(prepaid|postpaid)$", message = "결제 방식은 prepaid 또는 postpaid만 가능합니다")
        private String method = "prepaid";
    }
    
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
}