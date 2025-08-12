package com.ycs.lms.dto;

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
public class BatchProcessResponse {
    private String batchId;
    private int processed;
    private int failed;
    private List<ProcessResult> results;
    private Summary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessResult {
        private String labelCode;
        private String status; // success, failed, skipped
        private String reason;
        private String newStatus;
        private String previousStatus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalBoxes;
        private int totalOrders;
        private BigDecimal totalCbm;
        private BigDecimal totalWeight;
        private BigDecimal estimatedShippingCost;
    }
}