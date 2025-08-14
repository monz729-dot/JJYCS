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
public class EstimateResponseRequest {
    
    @NotBlank(message = "응답 액션은 필수입니다")
    private String action; // "approved" or "rejected"
    
    private String notes; // 응답 메모
}