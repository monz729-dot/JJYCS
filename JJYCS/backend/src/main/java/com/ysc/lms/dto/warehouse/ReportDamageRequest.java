package com.ysc.lms.dto.warehouse;

import lombok.Data;

@Data
public class ReportDamageRequest {
    private String damageReport;
    private String reportedBy;
    private String severity; // LOW, MEDIUM, HIGH
    private String notes;
}