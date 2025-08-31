package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_items")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BulkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "bulkItems"})
    private User user;

    @Column(nullable = false, length = 20)
    private String hsCode; // HS 코드

    @Column(nullable = false, length = 500)
    private String description; // 품목 설명 (한국어)

    @Column(length = 500)
    private String englishName; // 품목 영문명

    @Column(nullable = false)
    private Integer defaultQuantity = 1; // 기본 수량

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultWeight = BigDecimal.ZERO; // 기본 중량 (kg)

    // 기본 치수 정보 (cm)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultWidth = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultHeight = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal defaultDepth = BigDecimal.ZERO;

    // CBM 자동 계산 (읽기 전용)
    @Column(precision = 10, scale = 6, insertable = false, updatable = false)
    private BigDecimal defaultCbm; // (W × H × D) / 1,000,000

    @Column(precision = 10, scale = 2)
    private BigDecimal defaultUnitPrice; // 기본 단가 (THB)

    // HS Code 관련 정보
    @Column(precision = 5, scale = 2)
    private BigDecimal basicTariffRate; // 기본 관세율

    @Column(precision = 5, scale = 2)
    private BigDecimal wtoTariffRate; // WTO 관세율

    @Column(precision = 5, scale = 2)
    private BigDecimal specialTariffRate; // 특혜 관세율

    @Column(nullable = false)
    private Boolean isActive = true; // 활성 상태

    @Column(length = 200)
    private String notes; // 관리용 메모

    @Column(length = 100)
    private String category; // 품목 카테고리

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성자
    public BulkItem(User user, String hsCode, String description, String englishName) {
        this.user = user;
        this.hsCode = hsCode;
        this.description = description;
        this.englishName = englishName;
        this.defaultQuantity = 1;
        this.defaultWeight = BigDecimal.ZERO;
        this.defaultWidth = BigDecimal.ZERO;
        this.defaultHeight = BigDecimal.ZERO;
        this.defaultDepth = BigDecimal.ZERO;
        this.isActive = true;
    }

    // CBM 계산 메서드
    @PrePersist
    @PreUpdate
    public void calculateCbm() {
        if (defaultWidth != null && defaultHeight != null && defaultDepth != null) {
            this.defaultCbm = defaultWidth.multiply(defaultHeight)
                          .multiply(defaultDepth)
                          .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
        }
    }

    // 부피무게 계산 (항공운송용)
    public BigDecimal getVolumetricWeight() {
        if (defaultWidth != null && defaultHeight != null && defaultDepth != null) {
            return defaultWidth.multiply(defaultHeight)
                       .multiply(defaultDepth)
                       .divide(new BigDecimal("6000"), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // 비즈니스 메서드
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isOwnedBy(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }

    // THB 가치 반환 (OrderBusinessRuleService 호환)
    public BigDecimal getThbValue() {
        if (defaultUnitPrice != null && defaultQuantity != null) {
            return defaultUnitPrice.multiply(new BigDecimal(defaultQuantity));
        }
        return BigDecimal.ZERO;
    }
}