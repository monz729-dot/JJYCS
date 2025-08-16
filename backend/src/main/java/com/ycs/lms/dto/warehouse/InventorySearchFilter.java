package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 재고 검색 필터 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventorySearchFilter {

    private Long warehouseId;
    private String status;
    private String location;
    private String zone;
    private String search;
    private String orderCode;
    private String recipientName;
} 