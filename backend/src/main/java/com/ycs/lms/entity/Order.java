package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.REQUESTED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType = OrderType.SEA;
    
    // Recipient information
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;
    
    @Column(name = "recipient_phone")
    private String recipientPhone;
    
    @Column(name = "recipient_address", nullable = false, length = 500)
    private String recipientAddress;
    
    @Column(name = "recipient_zip_code")
    private String recipientZipCode;
    
    @Column(name = "recipient_country", nullable = false)
    private String recipientCountry;
    
    // Shipping options
    @Enumerated(EnumType.STRING)
    private OrderUrgency urgency = OrderUrgency.NORMAL;
    
    @Column(name = "needs_repacking")
    private boolean needsRepacking = false;
    
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;
    
    // Amounts and calculations
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(length = 3)
    private String currency = "THB";
    
    @Column(name = "total_cbm_m3", precision = 10, scale = 6)
    private BigDecimal totalCbmM3;
    
    // Business rule flags
    @Column(name = "requires_extra_recipient")
    private boolean requiresExtraRecipient = false;
    
    // Delivery information
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod = PaymentMethod.PREPAID;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    // System information
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "assigned_warehouse_id")
    private Long assignedWarehouseId;
    
    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Related entities (mappings will be added later)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderBox> boxes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Enum definitions
    public enum OrderStatus {
        REQUESTED, CONFIRMED, IN_PROGRESS, SHIPPED, DELIVERED, CANCELLED;
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    public enum OrderType {
        SEA, AIR;
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    public enum OrderUrgency {
        NORMAL, URGENT;
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    public enum PaymentMethod {
        PREPAID, POSTPAID;
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED;
        
        @Override
        public String toString() {
            return name();
        }
    }
    
    // Convenience methods
    public boolean isRequiresRepacking() {
        return this.needsRepacking;
    }
    
    public boolean getRequiresExtraRecipient() {
        return this.requiresExtraRecipient;
    }
    
    public void appendNote(String note) {
        if (this.notes == null || this.notes.isEmpty()) {
            this.notes = note;
        } else {
            this.notes += "; " + note;
        }
    }
}