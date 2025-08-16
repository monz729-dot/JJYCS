package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * CBM 계산 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CBMCalculationRequest {

    @Valid
    @NotEmpty(message = "박스 정보는 최소 1개 이상이어야 합니다.")
    private List<Box> boxes;

    /**
     * 박스 정보
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
    }
} 