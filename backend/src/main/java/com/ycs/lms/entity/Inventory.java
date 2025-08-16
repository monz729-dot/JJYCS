package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    private Long id;
    private Long orderBoxId;
    private String labelCode;
    private Long warehouseId;
    private String warehouseLocation;
    private InventoryStatus status;
    private String qrCodeUrl;
    private BigDecimal weightKg;
    private BigDecimal cbmM3;
    private String notes;
    private LocalDateTime inboundDate;
    private LocalDateTime outboundDate;
    private LocalDateTime lastScannedAt;
    private Long lastScannedBy;
    private String lastScanType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum InventoryStatus {
        INBOUND,      // 입고됨
        STORED,       // 보관중
        PREPARED,     // 출고준비
        OUTBOUND,     // 출고됨
        HOLD,         // 보류
        MIXBOX,       // 믹스박스
        MISSING,      // 분실
        DAMAGED       // 손상
    }
}