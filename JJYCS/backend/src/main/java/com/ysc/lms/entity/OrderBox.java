package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_boxes")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 50)
    private String boxNumber;

    // 치수 정보 (단위: mm)
    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private Integer depth;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 6)
    private BigDecimal cbm;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void calculateCbm() {
        if (width != null && height != null && depth != null) {
            // CBM = (width(mm) × height(mm) × depth(mm)) / 1,000,000,000
            BigDecimal widthM = new BigDecimal(width).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal heightM = new BigDecimal(height).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal depthM = new BigDecimal(depth).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            
            this.cbm = widthM.multiply(heightM)
                          .multiply(depthM)
                          .setScale(6, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    // Utility methods for CBM calculation
    public BigDecimal getCbmCalculated() {
        if (width != null && height != null && depth != null) {
            BigDecimal widthM = new BigDecimal(width).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal heightM = new BigDecimal(height).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal depthM = new BigDecimal(depth).divide(new BigDecimal("1000"), 6, BigDecimal.ROUND_HALF_UP);
            
            return widthM.multiply(heightM)
                        .multiply(depthM)
                        .setScale(6, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public void setCbmM3(BigDecimal cbmM3) {
        this.cbm = cbmM3;
    }
}