package com.ycs.lms.entity;

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
@Table(name = "billing")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 청구서 발행 정보
    @Column(nullable = false)
    private Boolean proformaIssued = false;

    @Column
    private LocalDateTime proformaDate;

    @Column(nullable = false)
    private Boolean finalIssued = false;

    @Column
    private LocalDateTime finalDate;

    // 비용 항목 (THB)
    @Column(precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO; // 기본 배송비

    @Column(precision = 10, scale = 2)
    private BigDecimal localDeliveryFee = BigDecimal.ZERO; // 현지 배송비

    @Column(precision = 10, scale = 2)
    private BigDecimal repackingFee = BigDecimal.ZERO; // 리패킹 비용

    @Column(precision = 10, scale = 2)
    private BigDecimal handlingFee = BigDecimal.ZERO; // 취급 수수료

    @Column(precision = 10, scale = 2)
    private BigDecimal insuranceFee = BigDecimal.ZERO; // 보험료

    @Column(precision = 10, scale = 2)
    private BigDecimal customsFee = BigDecimal.ZERO; // 통관 수수료

    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO; // TAX 7%

    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO; // 총 금액

    // 결제 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 100)
    private String depositorName; // 입금자명

    @Column
    private LocalDateTime paymentDate; // 입금일

    @Column(length = 100)
    private String paymentReference; // 입금 참조번호

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        BANK_TRANSFER,  // 무통장입금
        CREDIT_CARD,    // 신용카드
        PAYPAL          // 페이팔
    }

    public enum PaymentStatus {
        PENDING,    // 입금대기
        CONFIRMED,  // 입금확인
        COMPLETED,  // 입금완료
        CANCELLED   // 취소
    }

    // 총 금액 자동 계산
    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        BigDecimal subtotal = shippingFee
            .add(localDeliveryFee)
            .add(repackingFee)
            .add(handlingFee)
            .add(insuranceFee)
            .add(customsFee);
            
        // TAX 7% 계산
        this.tax = subtotal.multiply(new BigDecimal("0.07"))
                          .setScale(2, BigDecimal.ROUND_HALF_UP);
                          
        this.total = subtotal.add(tax);
    }
}