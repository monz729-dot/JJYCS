package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 재고 통계 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryStats {

    private Long warehouseId;
    private String warehouseName;
    private int totalBoxes;
    private int inboundBoxes;
    private int outboundBoxes;
    private int holdBoxes;
    private BigDecimal totalCBM;
    private BigDecimal totalWeight;
    private int totalOrders;
    private int activeOrders;
} 