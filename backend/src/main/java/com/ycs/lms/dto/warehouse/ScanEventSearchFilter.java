package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 스캔 이벤트 검색 필터 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanEventSearchFilter {

    private Long warehouseId;
    private Long boxId;
    private String eventType;
    private String startDate;
    private String endDate;
    private Long userId;
    private String labelCode;
    private String orderCode;
} 