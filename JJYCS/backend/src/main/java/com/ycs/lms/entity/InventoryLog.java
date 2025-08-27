package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    @Column(name = "previous_location")
    private String previousLocation;
    
    @Column(name = "new_location")
    private String newLocation;
    
    @Column(name = "quantity_before")
    private Integer quantityBefore;
    
    @Column(name = "quantity_after")
    private Integer quantityAfter;
    
    @Column(name = "performed_by")
    private String performedBy; // 작업자
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum ActionType {
        INBOUND,        // 입고
        OUTBOUND,       // 출고
        MOVE,           // 이동
        ADJUST,         // 재고 조정
        DAMAGE,         // 손상 처리
        HOLD,           // 보류 처리
        RELEASE         // 보류 해제
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}