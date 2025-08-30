package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scan_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scan_code", nullable = false)
    private String scanCode; // QR/바코드에서 읽어온 코드
    
    @Column(name = "scan_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ScanType scanType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "location")
    private String location; // 창고 위치
    
    @Column(name = "scanned_by")
    private String scannedBy; // 스캔 작업자
    
    @Column(name = "notes")
    private String notes; // 스캔 시 메모
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed")
    private Boolean processed = false; // 처리 완료 여부
    
    public enum ScanType {
        INBOUND,    // 입고
        OUTBOUND,   // 출고  
        INVENTORY,  // 재고 확인
        MOVE,       // 위치 이동
        HOLD        // 보류
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.processed == null) {
            this.processed = false;
        }
    }
}