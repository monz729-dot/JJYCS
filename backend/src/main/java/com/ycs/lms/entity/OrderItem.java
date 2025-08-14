package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "item_order")
    private int itemOrder;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String category;
    
    @Column(nullable = false)
    private int quantity;
    
    @Column(name = "unit_weight", precision = 10, scale = 3)
    private BigDecimal unitWeight; // kg
    
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(length = 3)
    private String currency = "THB";
    
    @Column(name = "hs_code")
    private String hsCode; // Harmonized System Code
    
    @Column(name = "ems_code")
    private String emsCode; // EMS 코드
    
    @Column(name = "country_of_origin")
    private String countryOfOrigin;
    
    private String brand;
    private String model;
    
    private boolean restricted = false;
    
    @Column(name = "restriction_note", length = 500)
    private String restrictionNote;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}