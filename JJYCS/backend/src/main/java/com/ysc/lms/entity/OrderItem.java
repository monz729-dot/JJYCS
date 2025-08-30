package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Column(nullable = false, length = 20)
    private String hsCode; // HS 코드

    @Column(nullable = false, length = 500)
    private String description; // 품목 설명

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal weight; // 중량 (kg)

    // 치수 정보 (cm)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal depth;

    // CBM 자동 계산
    @Column(precision = 10, scale = 6)
    private BigDecimal cbm; // (W × H × D) / 1,000,000

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice; // 단가 (THB)

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice; // 총 가격 (THB)

    // HS Code 관련 정보
    @Column(length = 500)
    private String englishName; // 품목 영문명

    // 관세율 정보
    @Column(precision = 5, scale = 2)
    private BigDecimal basicTariffRate; // 기본 관세율

    @Column(precision = 5, scale = 2)
    private BigDecimal wtoTariffRate; // WTO 관세율

    @Column(precision = 5, scale = 2)
    private BigDecimal specialTariffRate; // 특혜 관세율

    @Column(precision = 5, scale = 2)
    private BigDecimal appliedTariffRate; // 적용된 관세율

    // 관세 계산 결과
    @Column(precision = 10, scale = 2)
    private BigDecimal customsDutyAmount; // 관세액 (USD)

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmountWithDuty; // 관세 포함 총액 (USD)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // CBM 자동 계산 메서드
    @PrePersist
    @PreUpdate
    public void calculateCbm() {
        if (width != null && height != null && depth != null) {
            this.cbm = width.multiply(height)
                          .multiply(depth)
                          .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
        }
        
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    // 부피무게 계산 (항공운송용)
    public BigDecimal getVolumetricWeight() {
        if (width != null && height != null && depth != null) {
            return width.multiply(height)
                       .multiply(depth)
                       .divide(new BigDecimal("6000"), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    // OrderBusinessRuleService 호환 메서드들
    public BigDecimal getThbValue() {
        return unitPrice; // THB 단가
    }
    
    public BigDecimal getQuantity() {
        return new BigDecimal(quantity != null ? quantity : 0);
    }
}