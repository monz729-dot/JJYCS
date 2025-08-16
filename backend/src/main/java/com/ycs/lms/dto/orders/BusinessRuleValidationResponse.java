package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 비즈니스 룰 검증 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRuleValidationResponse {

    private boolean valid;
    private List<BusinessWarning> warnings;
    private BigDecimal totalCBM;
    private String recommendedShippingMethod;

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