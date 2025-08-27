package com.ycs.lms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveOrderRequest {
    
    private Long orderId;
    private Long warehouseId;
    private String warehouseCode;
    private String location;
    private String notes;
    private String receivedBy;
}