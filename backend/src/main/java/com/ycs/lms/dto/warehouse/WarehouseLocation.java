package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 창고 위치 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseLocation {

    private String location;
    private String zone;
    private int boxCount;
    private BigDecimal totalCBM;
    private BigDecimal totalWeight;
    private String status; // "available", "occupied", "reserved"
    private int capacity;
    private int availableCapacity;
} 