package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxResponse {
    private Long boxId;
    private String labelCode;
    private String qrCodeUrl;
    private String status;
    private String previousStatus;
    private BigDecimal cbm;
    private BigDecimal weight;
    private String location;
    private String trackingNumber;
    private LocalDateTime updatedAt;
    private String note;
}