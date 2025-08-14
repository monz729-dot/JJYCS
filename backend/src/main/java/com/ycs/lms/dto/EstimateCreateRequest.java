package com.ycs.lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateCreateRequest {
    
    @NotBlank(message = "배송 방법은 필수입니다")
    private String shippingMethod; // "air" or "sea"
    
    private String carrier; // "DHL", "FedEx", "EMS", etc.
    
    private String serviceLevel; // "standard", "express", "economy"
    
    private String notes; // 견적 메모
    
    private Boolean includeInsurance = false; // 보험 포함 여부
    
    private Boolean includeRepacking = false; // 리패킹 포함 여부
}