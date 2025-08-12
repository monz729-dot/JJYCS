package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanEventResponse {
    private Long eventId;
    private String eventType;
    private String labelCode;
    private Long orderId;
    private String orderCode;
    private String previousStatus;
    private String newStatus;
    private String location;
    private String batchId;
    private String deviceInfo;
    private String notes;
    private LocalDateTime scanTimestamp;
    private UserInfo scannedBy;
    private WarehouseInfo warehouse;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseInfo {
        private Long id;
        private String name;
        private String code;
    }
}