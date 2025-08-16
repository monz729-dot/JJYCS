package com.ycs.lms.entity;

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
    
    // CBM auto calculated
    private BigDecimal cbmM3; // (width * height * depth) / 1000000
    
    // Box weight
    private BigDecimal weightKg;
    private BigDecimal weight; // For compatibility
    
    // Box status
    private BoxStatus status = BoxStatus.CREATED;
    
    // Warehouse information
    private Long warehouseId;
    private String warehouseLocation; // A-01-15
    private LocalDateTime inboundDate;
    private LocalDateTime outboundDate;
    
    // Shipping information
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities (will be loaded separately in MyBatis)
    private Order order;
    
    // CBM 계산 메서드
    public double getCbmM3() {
        if (cbmM3 != null) {
            return cbmM3.doubleValue();
        }
        if (widthCm != null && heightCm != null && depthCm != null) {
            return widthCm.multiply(heightCm).multiply(depthCm).divide(BigDecimal.valueOf(1000000)).doubleValue();
        }
        return 0.0;
    }
    
    // Weight getter for compatibility
    public BigDecimal getWeight() {
        return weightKg != null ? weightKg : weight;
    }
    
    // Enum definitions
    public enum BoxStatus {
        CREATED, INBOUND_PENDING, INBOUND_COMPLETED, READY_FOR_OUTBOUND, 
        OUTBOUND_PENDING, OUTBOUND_COMPLETED, SHIPPED, DELIVERED;
        
        @Override
        public String toString() {
            return name();
        }
    }
}