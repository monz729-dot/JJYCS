package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CBM 계산 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CBMCalculationResponse {

    private BigDecimal totalCBM;
    private List<BoxCBM> boxes;
    private String recommendedShippingMethod;
    private BigDecimal cbmThreshold;
    private boolean exceedsThreshold;

    /**
     * 박스별 CBM 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoxCBM {
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal depth;
        private BigDecimal cbm;
    }
} 