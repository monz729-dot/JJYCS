package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id")
    private Long productId; // 향후 상품 마스터 연동용 (nullable)
    
    @Column(name = "name", nullable = false, length = 200)
    private String name; // 상품명
    
    @Column(name = "qty", nullable = false)
    private Integer qty = 1;
    
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    // amount는 DB에서 Generated Column으로 처리되므로 읽기전용
    @Column(name = "amount", precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal amount;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계 매핑 (Lazy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonBackReference
    private Order order;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 편의 메서드 - 금액 계산 (클라이언트 사이드 검증용)
    public BigDecimal calculateAmount() {
        if (qty == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }
    
    // 유효성 검증 메서드
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() 
               && qty != null && qty > 0 
               && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    // 하위 호환성을 위한 메서드들
    public Integer getQuantity() {
        return this.qty;
    }
    
    public void setQuantity(Integer quantity) {
        this.qty = quantity;
    }
    
    /**
     * @deprecated 현재 미지원. 사용 금지. NPE 위험 있음
     */
    @Deprecated
    public BigDecimal getWidth() {
        return BigDecimal.ZERO; // NPE 방지를 위해 0 반환
    }
    
    public void setWidth(BigDecimal width) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    /**
     * @deprecated 현재 미지원. 사용 금지. NPE 위험 있음
     */
    @Deprecated
    public BigDecimal getHeight() {
        return BigDecimal.ZERO; // NPE 방지를 위해 0 반환
    }
    
    public void setHeight(BigDecimal height) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    /**
     * @deprecated 현재 미지원. 사용 금지. NPE 위험 있음
     */
    @Deprecated
    public BigDecimal getDepth() {
        return BigDecimal.ZERO; // NPE 방지를 위해 0 반환
    }
    
    public void setDepth(BigDecimal depth) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    // 추가 필드들 (현재는 기본값/null 반환, 필요시 확장)
    public String getDescription() {
        return this.name; // name을 description으로 사용
    }
    
    public void setDescription(String description) {
        this.name = description;
    }
    
    /**
     * @deprecated 현재 미지원. 사용 금지.
     */
    @Deprecated
    public String getHsCode() {
        return ""; // NPE 방지를 위해 빈 문자열 반환
    }
    
    public void setHsCode(String hsCode) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    // OrderService에서 필요한 추가 setter 메서드들 (현재는 스텁)
    public void setWeight(BigDecimal weight) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setCbm(BigDecimal cbm) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setEnglishName(String englishName) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setBasicTariffRate(BigDecimal basicTariffRate) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setWtoTariffRate(BigDecimal wtoTariffRate) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setSpecialTariffRate(BigDecimal specialTariffRate) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setAppliedTariffRate(BigDecimal appliedTariffRate) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setCustomsDutyAmount(BigDecimal customsDutyAmount) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
    
    public void setTotalAmountWithDuty(BigDecimal totalAmountWithDuty) {
        // 현재 구조에서는 미지원, 확장 시 필드 추가 필요
    }
}