package com.ysc.lms.dto.warehouse;

import lombok.Data;

@Data
public class WarehouseStatistics {
    private long totalInventory;
    private long receivedItems;
    private long shippedItems;
    private long pendingItems;
    private long heldItems;
    private long damagedItems;
    private double averageProcessingTime; // in hours
}