package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBox {
    private Long id;
    private Long orderId;
    private int boxNumber;
    private String labelCode; // BOX-2024-001-01
    private String qrCodeUrl; // QR 코드 이미지 URL
    
    // Box dimensions (cm)
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal depthCm;
    
    // CBM auto calculated (stored as virtual column in DB)
    private BigDecimal cbmM3; // (width * height * depth) / 1000000
    
    // Box weight
    private BigDecimal weightKg;
    
    // Box status
    private String status; // created, inbound_pending, inbound_completed, etc.
    
    // Warehouse information
    private Long warehouseId;
    private String warehouseLocation; // A-01-15
    private LocalDateTime inboundDate;
    private LocalDateTime outboundDate;
    
    // Shipping information
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedDate;
    
    // Item IDs contained in this box (JSON format in DB)
    private List<Long> itemIds;
    private String itemIdsJson; // For MyBatis mapping
    
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private List<OrderItem> items;
}