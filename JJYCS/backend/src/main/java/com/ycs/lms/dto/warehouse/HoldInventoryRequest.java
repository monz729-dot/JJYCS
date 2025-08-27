package com.ycs.lms.dto.warehouse;

import lombok.Data;

@Data
public class HoldInventoryRequest {
    private String reason;
    private String notes;
    private String heldBy;
}