package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String orderCode;
    private Long userId;
    private OrderStatus status = OrderStatus.REQUESTED;
    private OrderType orderType = OrderType.SEA;
    
    // Recipient information
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientZipCode;
    private String recipientCountry;
    
    // Shipping options
    private OrderUrgency urgency = OrderUrgency.NORMAL;
    private boolean needsRepacking = false;
    private String specialInstructions;
    
    // Amounts and calculations
    private BigDecimal totalAmount;
    private String currency = "THB";
    private BigDecimal totalCbmM3;
    
    // Business rule flags
    private boolean requiresExtraRecipient = false;
    
    // Delivery information
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private PaymentMethod paymentMethod = PaymentMethod.PREPAID;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    // System information
    private Long createdBy;
    private Long assignedWarehouseId;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities (will be loaded separately in MyBatis)
    private List<OrderItem> items;
    private List<OrderBox> boxes;
    private User user;
    
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