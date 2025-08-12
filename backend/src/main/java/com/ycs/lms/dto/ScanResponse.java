package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private Long scanId;
    private BoxInfo box;
    private WarehouseInfo warehouse;
    private LocalDateTime timestamp;
    private String nextAction;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoxInfo {
        private Long boxId;
        private String labelCode;
        private Long orderId;
        private String orderCode;
        private String status;
        private String previousStatus;
        private BigDecimal cbm;
        private BigDecimal weight;
        private List<ItemInfo> items;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        private String name;
        private int quantity;
        private String category;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseInfo {
        private Long id;
        private String name;
        private String location;
        private String code;
    }
}