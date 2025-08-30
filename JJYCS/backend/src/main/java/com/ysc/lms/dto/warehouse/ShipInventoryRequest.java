package com.ysc.lms.dto.warehouse;

import lombok.Data;

@Data
public class ShipInventoryRequest {
    private String shippedBy;
    private String trackingNumber;
    private String carrier;
    private String notes;
}