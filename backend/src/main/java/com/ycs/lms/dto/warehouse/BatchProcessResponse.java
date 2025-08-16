package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 일괄 처리 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchProcessResponse {

    private BatchProcessRequest.BatchAction action;
    private int total;
    private int processed;
    private int failed;
    private List<ProcessResult> results;
    private LocalDateTime processedAt;

    /**
     * 개별 처리 결과
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProcessResult {
        private String labelCode;
        private boolean success;
        private String message;
        private String errorCode;
    }
} 