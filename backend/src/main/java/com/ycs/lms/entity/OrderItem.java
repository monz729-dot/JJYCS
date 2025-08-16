package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private int itemOrder;
    private String name;
    private String description;
    private String category;
    private int quantity;
    private BigDecimal unitWeight; // kg
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String currency = "THB";
    private String hsCode; // Harmonized System Code
    private String emsCode; // EMS 코드
    private String countryOfOrigin;
    private String brand;
    private String model;
    private boolean restricted = false;
    private String restrictionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Related entities (will be loaded separately in MyBatis)
    private Order order;
}