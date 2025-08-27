package com.ycs.lms.dto.warehouse;

import lombok.Data;
import java.util.List;

@Data
public class BatchShipRequest {
    private List<Long> inventoryIds;
    private String shippedBy;
    private String carrier;
    private String notes;
}