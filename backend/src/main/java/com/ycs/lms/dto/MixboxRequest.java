package com.ycs.lms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class MixboxRequest {
    
    @NotEmpty(message = "박스 ID 목록은 필수입니다")
    private List<Long> boxIds;
    
    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;
    
    @NotNull(message = "새 박스 치수는 필수입니다")
    private BoxDimensions newBoxDimensions;
    
    private String note;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoxDimensions {
        @NotNull(message = "박스 너비는 필수입니다")
        private BigDecimal width;
        
        @NotNull(message = "박스 높이는 필수입니다")
        private BigDecimal height;
        
        @NotNull(message = "박스 깊이는 필수입니다")
        private BigDecimal depth;
        
        @NotNull(message = "박스 무게는 필수입니다")
        private BigDecimal weight;
    }
}