package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EstimateResponse {
    
    private Long id;
    
    private Long orderId;
    
    private String orderCode;
    
    private Integer version; // 1차, 2차 견적 구분
    
    private String status; // "pending", "approved", "rejected", "cancelled"
    
    private String shippingMethod; // "air" or "sea"
    
    private String carrier; // 운송업체
    
    private String serviceLevel; // 서비스 레벨
    
    private BigDecimal shippingCost; // 배송비
    
    private BigDecimal localShippingCost; // 로컬 배송비
    
    private BigDecimal repackingCost; // 리패킹 비용
    
    private BigDecimal insuranceCost; // 보험료
    
    private BigDecimal totalCost; // 총 비용
    
    private String currency; // 통화
    
    private LocalDateTime validUntil; // 견적 유효기간
    
    private String notes; // 견적 메모
    
    private String responseNotes; // 고객 응답 메모
    
    private LocalDateTime createdAt; // 생성일시
    
    private LocalDateTime updatedAt; // 수정일시
    
    private LocalDateTime respondedAt; // 응답일시
}