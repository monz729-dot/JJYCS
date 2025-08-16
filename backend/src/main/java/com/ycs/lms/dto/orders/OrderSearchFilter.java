package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 주문 검색 필터 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchFilter {

    private Long userId;
    private String status;
    private String orderType;
    private String startDate;
    private String endDate;
    private String userRole;
    private String searchKeyword;
} 