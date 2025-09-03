package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
// import com.ysc.lms.listener.OrderEntityListener;
import jakarta.persistence.*;
import lombok.*;
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
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 20)
    private String orderNumber; // 주문번호

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.DRAFT;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";
    
    @Column(name = "note", length = 1000)
    private String note;

    // 연관관계 매핑 (Lazy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "orders"})
    private User user;

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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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
    
    // 경고 및 검증 결과 저장 필드들
    @Column(length = 2000)
    private String validationWarnings; // 검증 경고 메시지들 (JSON 또는 구분자로 구분)
    
    @Column(length = 1000)
    private String validationErrors; // 검증 오류 메시지들
    
    @Column
    private Boolean hsCodeValidated = false; // HS Code 검증 완료 여부
    
    @Column
    private LocalDateTime lastValidatedAt; // 마지막 검증 시간
    
    @Column(length = 500)
    private String cbmWarningMessage; // CBM 관련 경고 메시지
    
    @Column(length = 500)
    private String thbWarningMessage; // THB 임계값 관련 경고 메시지
    
    @Column(length = 500)
    private String memberCodeWarningMessage; // 회원코드 관련 경고 메시지
    
    // Getter/Setter 메서드들
    public String getValidationWarnings() {
        return validationWarnings;
    }
    
    public void setValidationWarnings(String validationWarnings) {
        this.validationWarnings = validationWarnings;
    }
    
    public String getValidationErrors() {
        return validationErrors;
    }
    
    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }
    
    public Boolean getHsCodeValidated() {
        return hsCodeValidated;
    }
    
    public void setHsCodeValidated(Boolean hsCodeValidated) {
        this.hsCodeValidated = hsCodeValidated;
    }
    
    public LocalDateTime getLastValidatedAt() {
        return lastValidatedAt;
    }
    
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }
    
    public String getCbmWarningMessage() {
        return cbmWarningMessage;
    }
    
    public void setCbmWarningMessage(String cbmWarningMessage) {
        this.cbmWarningMessage = cbmWarningMessage;
    }
    
    public String getThbWarningMessage() {
        return thbWarningMessage;
    }
    
    public void setThbWarningMessage(String thbWarningMessage) {
        this.thbWarningMessage = thbWarningMessage;
    }
    
    public String getMemberCodeWarningMessage() {
        return memberCodeWarningMessage;
    }
    
    public void setMemberCodeWarningMessage(String memberCodeWarningMessage) {
        this.memberCodeWarningMessage = memberCodeWarningMessage;
    }
    
    /**
     * 모든 경고 메시지를 하나로 합치는 유틸리티 메서드
     */
    public String getAllWarningMessages() {
        StringBuilder warnings = new StringBuilder();
        
        if (cbmWarningMessage != null && !cbmWarningMessage.trim().isEmpty()) {
            warnings.append(cbmWarningMessage).append(" ");
        }
        
        if (thbWarningMessage != null && !thbWarningMessage.trim().isEmpty()) {
            warnings.append(thbWarningMessage).append(" ");
        }
        
        if (memberCodeWarningMessage != null && !memberCodeWarningMessage.trim().isEmpty()) {
            warnings.append(memberCodeWarningMessage).append(" ");
        }
        
        if (validationWarnings != null && !validationWarnings.trim().isEmpty()) {
            warnings.append(validationWarnings).append(" ");
        }
        
        return warnings.toString().trim();
    }
    
    /**
     * 경고가 있는지 확인
     */
    public boolean hasWarnings() {
        return !getAllWarningMessages().isEmpty();
    }

    public enum OrderStatus {
        DRAFT("임시저장"),          // 임시저장 상태
        SUBMITTED("제출완료"),       // 제출완료 상태
        CANCELLED("취소됨"),         // 취소됨 상태
        IN_WAREHOUSE("창고입고"),     // 창고입고 상태
        SHIPPED("배송완료"),         // 배송완료 상태
        RECEIVED("접수완료"),        // 접수완료 상태
        ARRIVED("창고도착"),         // 창고도착 상태
        REPACKING("리패킹"),         // 리패킹 상태
        SHIPPING("배송중"),          // 배송중 상태
        DELIVERED("배송완료"),       // 배송완료 상태
        COMPLETED("완료"),           // 완료 상태
        BILLING("정산중"),           // 정산중 상태
        PAYMENT_PENDING("결제대기"),  // 결제대기 상태
        PAYMENT_CONFIRMED("결제완료"), // 결제완료 상태
        PENDING("대기중"),           // 대기중 상태
        CONFIRMED("확인완료"),       // 확인완료 상태
        PROCESSING("처리중"),        // 처리중 상태
        IN_TRANSIT("운송중"),        // 운송중 상태
        HOLD("보류");               // 보류 상태
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 상태 전환 검증 메서드
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this.status) {
            case DRAFT:
                return newStatus == OrderStatus.SUBMITTED || newStatus == OrderStatus.CANCELLED;
            case SUBMITTED:
                return newStatus == OrderStatus.RECEIVED || newStatus == OrderStatus.CANCELLED;
            case RECEIVED:
                return newStatus == OrderStatus.ARRIVED || newStatus == OrderStatus.CANCELLED;
            case ARRIVED:
                return newStatus == OrderStatus.REPACKING || newStatus == OrderStatus.IN_WAREHOUSE || newStatus == OrderStatus.CANCELLED;
            case REPACKING:
                return newStatus == OrderStatus.IN_WAREHOUSE || newStatus == OrderStatus.CANCELLED;
            case IN_WAREHOUSE:
                return newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPING:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.BILLING;
            case DELIVERED:
                return newStatus == OrderStatus.BILLING || newStatus == OrderStatus.COMPLETED;
            case BILLING:
                return newStatus == OrderStatus.PAYMENT_PENDING || newStatus == OrderStatus.COMPLETED;
            case PAYMENT_PENDING:
                return newStatus == OrderStatus.PAYMENT_CONFIRMED || newStatus == OrderStatus.BILLING;
            case PAYMENT_CONFIRMED:
                return newStatus == OrderStatus.COMPLETED;
            case COMPLETED:
                return false; // 완료된 주문은 상태 변경 불가
            case CANCELLED:
                return false; // 취소된 주문은 상태 변경 불가
            default:
                return false;
        }
    }
    

    public enum ShippingType {
        SEA,  // 해상운송
        AIR   // 항공운송
    }
    
    // Deprecated: OrderType enum 제거됨 - ShippingType 사용
    // 호환성을 위한 메서드들
    public ShippingType getOrderType() {
        return this.shippingType;
    }
    
    public void setOrderType(ShippingType orderType) {
        this.shippingType = orderType;
    }

    // CBM 29 초과 시 항공 전환 로직
    public void checkAndUpdateShippingType() {
        if (totalCbm != null && totalCbm.compareTo(new BigDecimal("29")) > 0) {
            this.shippingType = ShippingType.AIR;
        }
    }
}