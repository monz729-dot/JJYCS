package com.ycs.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "items", "billing"})
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderStatus newStatus;

    @Column(length = 1000)
    private String changeReason;

    @Column(length = 100)
    private String changedBy; // 변경한 사용자/시스템

    @Column(length = 500)
    private String notes;

    // 창고 관련 변경사항
    @Column(length = 20)
    private String storageLocation;

    @Column(length = 50)
    private String storageArea;

    // 배송 관련 변경사항
    @Column(length = 50)
    private String trackingNumber;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderHistory(Order order, Order.OrderStatus previousStatus, Order.OrderStatus newStatus, 
                       String changeReason, String changedBy) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
    }

    public enum ChangeType {
        STATUS_CHANGE,      // 상태 변경
        WAREHOUSE_ARRIVAL,  // 창고 도착
        REPACKING_START,    // 리패킹 시작
        REPACKING_COMPLETE, // 리패킹 완료
        SHIPPING_START,     // 배송 시작
        DELIVERY_COMPLETE,  // 배송 완료
        BILLING_ISSUED,     // 청구서 발행
        PAYMENT_RECEIVED,   // 입금 확인
        SYSTEM_UPDATE,      // 시스템 자동 업데이트
        MANUAL_UPDATE       // 수동 업데이트
    }
}