package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "orders"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 100)
    private String actionUrl; // 클릭 시 이동할 URL

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isEmailSent = false;

    @Column(nullable = false)
    private Boolean isSmsRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "items", "billing"})
    private Order relatedOrder; // 주문 관련 알림인 경우

    @Column
    private LocalDateTime emailSentAt;

    @Column
    private LocalDateTime readAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        ORDER_STATUS_CHANGED,    // 주문 상태 변경
        ORDER_ARRIVED,          // 창고 도착
        ORDER_SHIPPED,          // 배송 시작
        ORDER_DELIVERED,        // 배송 완료
        PAYMENT_REQUIRED,       // 결제 필요
        PAYMENT_CONFIRMED,      // 입금 확인
        ACCOUNT_APPROVED,       // 계정 승인
        ACCOUNT_REJECTED,       // 계정 거부
        USER_APPROVED,          // 사용자 승인
        USER_REJECTED,          // 사용자 거부
        USER_STATUS_CHANGED,    // 사용자 상태 변경
        SYSTEM_MAINTENANCE,     // 시스템 점검
        PROMOTION,              // 프로모션
        CBM_THRESHOLD_EXCEEDED, // CBM 29 초과
        THB_THRESHOLD_EXCEEDED, // THB 1500 초과
        NO_MEMBER_CODE_WARNING, // 회원코드 미기재 경고
        GENERAL                 // 일반 알림
    }

    public Notification(User user, NotificationType type, String title, String message) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public Notification(User user, NotificationType type, String title, String message, Order relatedOrder) {
        this(user, type, title, message);
        this.relatedOrder = relatedOrder;
        this.actionUrl = "/orders/" + relatedOrder.getId();
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markEmailSent() {
        this.isEmailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }
}