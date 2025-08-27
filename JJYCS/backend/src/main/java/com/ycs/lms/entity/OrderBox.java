package com.ycs.lms.entity;

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

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal depth;

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
            this.cbm = width.multiply(height)
                          .multiply(depth)
                          .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
        }
    }
}