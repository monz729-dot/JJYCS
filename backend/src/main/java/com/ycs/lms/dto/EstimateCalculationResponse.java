package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateCalculationResponse {
    
    private Long orderId;
    
    private BigDecimal totalCbm; // 총 CBM
    
    private BigDecimal totalWeight; // 총 무게 (KG)
    
    private String shippingMethod; // "air" or "sea"
    
    private String carrier; // 운송업체
    
    private String serviceLevel; // 서비스 레벨
    
    private BigDecimal shippingCost; // 배송비
    
    private BigDecimal localShippingCost; // 로컬 배송비
    
    private BigDecimal repackingCost; // 리패킹 비용
    
    private BigDecimal insuranceCost; // 보험료
    
    private BigDecimal totalCost; // 총 비용
    
    private String currency; // 통화
    
    private String notes; // 계산 메모
}