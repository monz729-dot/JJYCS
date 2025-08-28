package com.ycs.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
// import com.ycs.lms.listener.OrderEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String orderNumber; // YCS-240115-001 형태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "orders"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShippingType shippingType;

    @Column(nullable = false, length = 50)
    private String country; // 배송 국가

    @Column(length = 10)
    private String postalCode;

    // 수취인 정보
    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(length = 20)
    private String recipientPhone;

    @Column(length = 500)
    private String recipientAddress;

    @Column(length = 10)
    private String recipientPostalCode;

    // 송장 정보
    @Column(length = 50)
    private String trackingNumber;

    // CBM 정보
    @Column(precision = 10, scale = 6)
    private BigDecimal totalCbm = BigDecimal.ZERO;

    @Column(precision = 8, scale = 2)
    private BigDecimal totalWeight = BigDecimal.ZERO;

    // 비즈니스 규칙 플래그
    @Column(nullable = false)
    private Boolean requiresExtraRecipient = false; // THB 1500 초과

    @Column(nullable = false)
    private Boolean noMemberCode = false; // 회원코드 미기재

    // 창고 정보
    @Column(length = 20)
    private String storageLocation; // A-01-03

    @Column(length = 50)
    private String storageArea; // A열 1행 3번

    @Column
    private LocalDateTime arrivedAt; // 창고 도착일

    @Column(precision = 8, scale = 2)
    private BigDecimal actualWeight; // 실제 무게

    @Column(nullable = false)
    private Boolean repackingRequested = false;

    @Column(nullable = false)
    private Boolean repackingCompleted = false;

    @Column(length = 1000)
    private String warehouseNotes; // 창고 메모

    // 배송 정보
    @Column
    private LocalDateTime shippedAt; // 배송 시작일

    @Column
    private LocalDateTime deliveredAt; // 배송 완료일

    @Column
    private LocalDateTime estimatedDelivery; // 예상 배송일

    // 특별 요청사항
    @Column(length = 1000)
    private String specialRequests;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관 관계
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"order"})
    private List<OrderBox> boxes = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"order"})
    private Billing billing;
    
    // 비즈니스 로직 메서드들
    public List<OrderItem> getOrderItems() {
        return items;
    }
    
    public void setOrderItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public List<OrderBox> getOrderBoxes() {
        return boxes;
    }
    
    public void setOrderBoxes(List<OrderBox> boxes) {
        this.boxes = boxes;
    }
    
    public void setRequiresExtraRecipient(boolean requiresExtraRecipient) {
        this.requiresExtraRecipient = requiresExtraRecipient;
    }
    
    public void setHasNoMemberCode(boolean hasNoMemberCode) {
        this.noMemberCode = hasNoMemberCode;
    }

    public enum OrderStatus {
        RECEIVED,           // 접수완료
        ARRIVED,           // 창고도착
        REPACKING,         // 리패킹진행
        SHIPPING,          // 배송중
        DELIVERED,         // 배송완료
        BILLING,           // 청구서발행
        PAYMENT_PENDING,   // 입금대기
        PAYMENT_CONFIRMED, // 입금확인
        COMPLETED,         // 완료
        // 추가 상태 (프론트엔드 호환용)
        PENDING,           // 대기 중
        PROCESSING,        // 처리 중
        SHIPPED,           // 발송됨
        IN_TRANSIT,        // 운송 중
        CANCELLED,         // 취소됨
        DELAYED            // 지연됨
    }

    public enum ShippingType {
        SEA,  // 해상운송
        AIR   // 항공운송
    }
    
    // OrderBusinessRuleService 호환을 위한 OrderType enum (ShippingType과 동일)
    public enum OrderType {
        SEA,  // 해상운송
        AIR   // 항공운송
    }
    
    @Enumerated(EnumType.STRING)
    private OrderType orderType = OrderType.SEA;
    
    public OrderType getOrderType() {
        return orderType;
    }
    
    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
        // ShippingType과 동기화
        this.shippingType = orderType == OrderType.AIR ? ShippingType.AIR : ShippingType.SEA;
    }

    // CBM 29 초과 시 항공 전환 로직
    public void checkAndUpdateShippingType() {
        if (totalCbm != null && totalCbm.compareTo(new BigDecimal("29")) > 0) {
            this.shippingType = ShippingType.AIR;
        }
    }
}