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
    private BigDecimal total = BigDecimal.ZERO; // 총 금액 (THB)

    // 환율 정보 (청구일 기준 네이버 환율 적용)
    @Column(precision = 10, scale = 4)
    private BigDecimal exchangeRate; // THB -> KRW 환율

    @Column
    private LocalDateTime exchangeRateDate; // 환율 적용일

    @Column(length = 20)
    private String exchangeRateSource = "NAVER"; // 환율 출처

    // KRW 표시용 금액들
    @Column(precision = 12, scale = 2)
    private BigDecimal totalKrw = BigDecimal.ZERO; // 총 금액 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingFeeKrw = BigDecimal.ZERO; // 기본 배송비 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal localDeliveryFeeKrw = BigDecimal.ZERO; // 현지 배송비 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal repackingFeeKrw = BigDecimal.ZERO; // 리패킹 비용 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal handlingFeeKrw = BigDecimal.ZERO; // 취급 수수료 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal insuranceFeeKrw = BigDecimal.ZERO; // 보험료 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal customsFeeKrw = BigDecimal.ZERO; // 통관 수수료 (KRW)

    @Column(precision = 12, scale = 2)
    private BigDecimal taxKrw = BigDecimal.ZERO; // TAX 7% (KRW)

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
        // KR 국내 결제 수단
        KR_BANK_TRANSFER("KR_BANK", "한국 무통장입금"),
        KR_CREDIT_CARD("KR_CARD", "한국 신용카드"),
        KR_PG_KAKAOPAY("KR_KAKAO", "카카오페이"),
        KR_PG_NAVERPAY("KR_NAVER", "네이버페이"),
        
        // TH 현지 결제 수단  
        TH_QR_PROMPTPAY("TH_QR", "태국 QR코드 결제"),
        TH_BANK_TRANSFER("TH_BANK", "태국 무통장입금"),
        TH_CREDIT_CARD("TH_CARD", "태국 신용카드"),
        TH_TRUE_MONEY("TH_TRUE", "TrueMoney Wallet"),
        
        // 기타 결제 수단
        PAYPAL("PAYPAL", "PayPal"),
        BANK_TRANSFER("BANK", "무통장입금"); // 기존 호환용

        private final String code;
        private final String description;

        PaymentMethod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        
        public boolean isKoreanMethod() {
            return name().startsWith("KR_");
        }
        
        public boolean isThaiMethod() {
            return name().startsWith("TH_");
        }
    }

    public enum PaymentStatus {
        PENDING,    // 입금대기
        CONFIRMED,  // 입금확인
        COMPLETED,  // 입금완료
        CANCELLED   // 취소
    }

    // 총 금액 자동 계산 (THB 및 KRW)
    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        // THB 계산
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
        
        // KRW 계산 (환율이 설정된 경우)
        if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            this.shippingFeeKrw = shippingFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.localDeliveryFeeKrw = localDeliveryFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.repackingFeeKrw = repackingFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.handlingFeeKrw = handlingFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.insuranceFeeKrw = insuranceFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.customsFeeKrw = customsFee.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.taxKrw = tax.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            this.totalKrw = total.multiply(exchangeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    // 환율 설정 및 KRW 계산
    public void updateExchangeRate(BigDecimal rate, LocalDateTime rateDate, String source) {
        this.exchangeRate = rate;
        this.exchangeRateDate = rateDate;
        this.exchangeRateSource = source;
        calculateTotal(); // KRW 금액 재계산
    }
}