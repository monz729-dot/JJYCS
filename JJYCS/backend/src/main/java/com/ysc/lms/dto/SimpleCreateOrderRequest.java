package com.ysc.lms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 주문 생성 요청 DTO - MVP용
 * 최소 운영셋: 주문번호, 사용자ID, 상태, 총액, 화폐, 메모, 품목들
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleCreateOrderRequest {
    
    @Size(max = 1000, message = "주문 메모는 1000자를 초과할 수 없습니다")
    private String note;
    
    @NotNull(message = "주문 품목은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 품목이 필요합니다")
    @Valid
    private List<OrderItemRequest> items = new ArrayList<>();
    
    // 클라이언트에서 계산한 총액 (서버 검증용)
    @DecimalMin(value = "0.00", message = "총액은 0 이상이어야 합니다")
    private BigDecimal totalAmount;
    
    @Pattern(regexp = "KRW|USD|THB", message = "화폐는 KRW, USD, THB 중 하나여야 합니다")
    private String currency = "KRW";
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다")
        private String name;
        
        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        private Integer qty;
        
        @NotNull(message = "단가는 필수입니다")
        @DecimalMin(value = "0.00", message = "단가는 0 이상이어야 합니다")
        private BigDecimal unitPrice;
        
        private Long productId; // 선택사항, 향후 상품 마스터 연동용
    }
}