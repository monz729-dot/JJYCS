package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 아이템 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItem {

    private Long boxId;
    private String labelCode;
    private String orderCode;
    private String status;
    private String location;
    private String zone;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal depthCm;
    private BigDecimal weightKg;
    private BigDecimal cbm;
    private LocalDateTime inboundDate;
    private LocalDateTime outboundDate;
    private String recipientName;
    private String recipientCountry;
} 