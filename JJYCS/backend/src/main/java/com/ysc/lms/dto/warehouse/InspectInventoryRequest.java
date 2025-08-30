package com.ysc.lms.dto.warehouse;

import lombok.Data;

@Data
public class InspectInventoryRequest {
    private String inspectorName;
    private String notes;
    private boolean passed;
    private String damageReport;
}