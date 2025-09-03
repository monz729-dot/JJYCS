package com.ysc.lms.entity;

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
@Table(name = "partner_commissions")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PartnerCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private User partner; // PARTNER 타입의 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // 연관된 주문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id", nullable = false)
    private User referredUser; // 추천받은 사용자

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount; // 수수료 금액

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate; // 수수료율 (%)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount; // 주문 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(length = 3)
    private String currency = "KRW"; // 통화

    @Column(length = 100)
    private String referralCode; // 추천 코드

    @Column(length = 500)
    private String notes; // 메모

    @Column
    private LocalDateTime settledAt; // 정산일

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum CommissionStatus {
        PENDING,     // 대기중
        CONFIRMED,   // 확정
        SETTLED,     // 정산완료
        CANCELLED    // 취소됨
    }

    // 수수료 계산 메서드
    public void calculateCommission(BigDecimal orderAmount, BigDecimal rate) {
        this.orderAmount = orderAmount;
        this.commissionRate = rate;
        this.commissionAmount = orderAmount.multiply(rate).divide(new BigDecimal("100"));
    }

    // 정산 처리
    public void settle() {
        this.status = CommissionStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }
}