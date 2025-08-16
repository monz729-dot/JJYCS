package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanEvent {
    private Long id;
    private String labelCode;
    private Long orderId;
    private Long orderBoxId;
    private Long warehouseId;
    private Long userId;
    private ScanType scanType;
    private EventStatus status;
    private String scanLocation;
    private String notes;
    private String metadata; // JSON 형태의 추가 정보
    private LocalDateTime scanTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ScanType {
        INBOUND,      // 입고
        OUTBOUND,     // 출고
        HOLD,         // 보류
        MIXBOX,       // 믹스박스
        INVENTORY,    // 재고조사
        TRANSFER,     // 이동
        QUALITY_CHECK // 품질검사
    }

    public enum EventStatus {
        SUCCESS,      // 성공
        PENDING,      // 대기중
        FAILED,       // 실패
        CANCELLED     // 취소됨
    }
}