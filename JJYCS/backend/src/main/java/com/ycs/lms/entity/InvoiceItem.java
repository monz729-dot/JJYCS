package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @Column(nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;
    
    @Column(length = 10)
    private String unit = "EA";
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    // 무게 (kg)
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;
    
    // 부피 (CBM)
    @Column(precision = 8, scale = 6)
    private BigDecimal volume;
    
    // THB 가격 (태국 바트)
    @Column(precision = 10, scale = 2)
    private BigDecimal thbPrice;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    public enum ItemType {
        BASE_SHIPPING,      // 기본 배송료
        WEIGHT_SURCHARGE,   // 무게 추가료
        VOLUME_SURCHARGE,   // 부피 추가료
        REPACKING,          // 리패킹 비용
        AIRFREIGHT_SURCHARGE, // 항공료 추가비
        LOCAL_DELIVERY,     // 로컬 배송료
        INSURANCE,          // 보험료
        CUSTOMS_CLEARANCE,  // 통관 수수료
        STORAGE,            // 창고 보관료
        SPECIAL_HANDLING,   // 특수 처리비
        DISCOUNT,           // 할인
        VAT,                // 부가세
        MISCELLANEOUS       // 기타
    }
    
    // 총 가격 자동 계산
    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = quantity.multiply(unitPrice);
        }
    }
}