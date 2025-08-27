package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventory")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WarehouseInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "inventory_code", unique = true, length = 100)
    private String inventoryCode;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryStatus status;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "received_by", length = 100)
    private String receivedBy;
    
    @Column(name = "inspected_by", length = 100)
    private String inspectedBy;
    
    @Column(name = "shipped_by", length = 100)
    private String shippedBy;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum InventoryStatus {
        PENDING("Pending Receive"),
        RECEIVED("Received"),
        INSPECTED("Inspected"),
        HELD("Held"),
        DAMAGED("Damaged"),
        READY_TO_SHIP("Ready to Ship"),
        SHIPPED("Shipped");
        
        private final String description;
        
        InventoryStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = InventoryStatus.PENDING;
        }
        if (inventoryCode == null) {
            inventoryCode = generateInventoryCode();
        }
    }
    
    private String generateInventoryCode() {
        // Generate a unique inventory code
        return "INV" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    public boolean isReceived() {
        return status != InventoryStatus.PENDING;
    }
    
    public boolean isShipped() {
        return status == InventoryStatus.SHIPPED;
    }
    
    public boolean canBeShipped() {
        return status == InventoryStatus.READY_TO_SHIP || status == InventoryStatus.INSPECTED;
    }
}