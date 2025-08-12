package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProfile {
    private Long id;
    private Long userId;
    private String warehouseName;
    private String warehouseAddress;
    private String capacityDescription;
    private String operatingHours;
    private String contactPerson;
    private String contactPhone;
    private String facilities; // JSON format
    private String approvalNote;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}