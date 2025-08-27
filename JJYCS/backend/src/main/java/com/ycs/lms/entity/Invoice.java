package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String invoiceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType invoiceType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    
    // 기본 배송료
    @Column(precision = 10, scale = 2)
    private BigDecimal baseShippingFee = BigDecimal.ZERO;
    
    // 무게 기반 추가료
    @Column(precision = 10, scale = 2)
    private BigDecimal weightSurcharge = BigDecimal.ZERO;
    
    // CBM 기반 추가료
    @Column(precision = 10, scale = 2)
    private BigDecimal volumeSurcharge = BigDecimal.ZERO;
    
    // 리패킹 비용
    @Column(precision = 10, scale = 2)
    private BigDecimal repackingFee = BigDecimal.ZERO;
    
    // 항공료 추가비 (CBM 초과시)
    @Column(precision = 10, scale = 2)
    private BigDecimal airfreightSurcharge = BigDecimal.ZERO;
    
    // 로컬 배송료
    @Column(precision = 10, scale = 2)
    private BigDecimal localDeliveryFee = BigDecimal.ZERO;
    
    // 기타 수수료
    @Column(precision = 10, scale = 2)
    private BigDecimal miscellaneousFee = BigDecimal.ZERO;
    
    // 할인 금액
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    // VAT
    @Column(precision = 10, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;
    
    // 총액 (VAT 포함)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // 결제된 금액
    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    // 통화 (기본: KRW)
    @Column(length = 3)
    private String currency = "KRW";
    
    // THB 환율 (태국 바트 기준)
    @Column(precision = 8, scale = 4)
    private BigDecimal exchangeRate;
    
    // THB 총액
    @Column(precision = 10, scale = 2)
    private BigDecimal thbAmount;
    
    // 발행일
    @Column
    private LocalDateTime issuedAt;
    
    // 만료일 (결제 기한)
    @Column
    private LocalDateTime dueDate;
    
    // 결제 완료일
    @Column
    private LocalDateTime paidAt;
    
    // 생성일시
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // 수정일시
    @Column
    private LocalDateTime updatedAt;
    
    // 생성자
    @Column
    private String createdBy;
    
    // 비고
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems;
    
    public enum InvoiceType {
        PROFORMA,    // 1차 견적서 (Proforma Invoice)
        ADDITIONAL,  // 2차 견적서 (Additional Charges)
        FINAL        // 최종 청구서
    }
    
    public enum InvoiceStatus {
        DRAFT,           // 임시저장
        ISSUED,          // 발행완료
        SENT,            // 고객 전송
        PAYMENT_PENDING, // 결제 대기
        PARTIALLY_PAID,  // 부분 결제
        FULLY_PAID,      // 결제 완료
        OVERDUE,         // 연체
        CANCELLED        // 취소
    }
    
    // 결제 잔액 계산
    public BigDecimal getBalanceAmount() {
        return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }
    
    // 결제 완료 여부
    public boolean isFullyPaid() {
        return paidAmount != null && paidAmount.compareTo(totalAmount) >= 0;
    }
    
    // 연체 여부 확인
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !isFullyPaid();
    }
}