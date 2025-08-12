package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MixboxResponse {
    private Long newBoxId;
    private String newLabelCode;
    private String newQrCodeUrl;
    private List<Long> originalBoxIds;
    private List<String> originalLabelCodes;
    private BigDecimal totalCbmBefore;
    private BigDecimal newCbm;
    private BigDecimal spaceSaved;
    private int itemsCount;
    private String status;
}