package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long boxId;
    private String labelCode;
    private Long orderId;
    private String orderCode;
    private String status;
    private String location;
    private LocalDateTime inboundDate;
    private LocalDateTime expectedOutboundDate;
    private int daysInWarehouse;
    private BigDecimal cbm;
    private BigDecimal weight;
    private List<ItemSummary> items;
    private CustomerInfo customer;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemSummary {
        private String name;
        private int quantity;
        private String category;
        private BigDecimal weight;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String name;
        private String memberCode;
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalBoxes;
        private Map<String, Integer> statusCounts;
        private BigDecimal totalCbm;
        private BigDecimal averageDaysStored;
    }
}