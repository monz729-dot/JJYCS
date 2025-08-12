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
    private String status; // requested, confirmed, payment_pending, etc.
    private String orderType; // sea, air
    
    // Recipient information
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientZipCode;
    private String recipientCountry;
    
    // Shipping options
    private String urgency; // normal, urgent
    private boolean needsRepacking;
    private String specialInstructions;
    
    // Amounts and calculations
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal totalCbmM3; // Calculated from order_boxes
    
    // Business rule flags
    private boolean requiresExtraRecipient; // THB 1,500 초과
    
    // Delivery information
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String paymentMethod; // prepaid, postpaid
    private String paymentStatus; // pending, completed, failed, refunded
    
    // System information
    private Long createdBy;
    private Long assignedWarehouseId;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities
    private List<OrderItem> items;
    private List<OrderBox> boxes;
    private User user;
}