package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스캔 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanResponse {

    private String labelCode;
    private ScanRequest.ScanType scanType;
    private String status;
    private String message;
    private LocalDateTime scanTimestamp;
    private Long boxId;
    private String orderCode;
    private String location;
} 