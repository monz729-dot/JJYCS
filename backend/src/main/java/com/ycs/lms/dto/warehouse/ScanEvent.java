package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 스캔 이벤트 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanEvent {

    private Long eventId;
    private String labelCode;
    private Long boxId;
    private String orderCode;
    private ScanRequest.ScanType eventType;
    private String status;
    private String location;
    private Long userId;
    private String userName;
    private LocalDateTime scanTimestamp;
    private String memo;
} 