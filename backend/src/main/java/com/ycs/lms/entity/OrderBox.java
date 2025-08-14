package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_boxes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "box_number", nullable = false)
    private int boxNumber;
    
    @Column(name = "label_code", unique = true)
    private String labelCode; // BOX-2024-001-01
    
    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl; // QR 코드 이미지 URL
    
    // Box dimensions (cm)
    @Column(name = "width_cm", precision = 8, scale = 2, nullable = false)
    private BigDecimal widthCm;
    
    @Column(name = "height_cm", precision = 8, scale = 2, nullable = false)
    private BigDecimal heightCm;
    
    @Column(name = "depth_cm", precision = 8, scale = 2, nullable = false)
    private BigDecimal depthCm;
    
    // CBM auto calculated
    @Column(name = "cbm_m3", precision = 10, scale = 6)
    private BigDecimal cbmM3; // (width * height * depth) / 1000000
    
    // Box weight
    @Column(name = "weight_kg", precision = 8, scale = 3)
    private BigDecimal weightKg;
    
    @Column(name = "weight", precision = 8, scale = 3) // For compatibility
    private BigDecimal weight;
    
    // Box status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoxStatus status = BoxStatus.CREATED;
    
    // Warehouse information
    @Column(name = "warehouse_id")
    private Long warehouseId;
    
    @Column(name = "warehouse_location")
    private String warehouseLocation; // A-01-15
    
    @Column(name = "inbound_date")
    private LocalDateTime inboundDate;
    
    @Column(name = "outbound_date")
    private LocalDateTime outboundDate;
    
    // Shipping information
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    private String carrier;
    
    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (cbmM3 == null && widthCm != null && heightCm != null && depthCm != null) {
            cbmM3 = widthCm.multiply(heightCm).multiply(depthCm).divide(BigDecimal.valueOf(1000000));
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (cbmM3 == null && widthCm != null && heightCm != null && depthCm != null) {
            cbmM3 = widthCm.multiply(heightCm).multiply(depthCm).divide(BigDecimal.valueOf(1000000));
        }
    }
    
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
        OUTBOUND_PENDING, OUTBOUND_COMPLETED, SHIPPED, DELIVERED
    }
}