package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * YCS 접수지 정보 엔티티
 */
@Entity
@Table(name = "inbound_locations", indexes = {
    @Index(name = "idx_inbound_locations_is_active", columnList = "is_active"),
    @Index(name = "idx_inbound_locations_display_order", columnList = "display_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 접수지명
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 접수지 주소
     */
    @Column(name = "address", nullable = false, length = 500)
    private String address;
    
    /**
     * 우편번호 (한국 5자리)
     */
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    /**
     * 연락처
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * 담당자명
     */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    /**
     * 운영시간
     */
    @Column(name = "business_hours", length = 100)
    private String businessHours;
    
    /**
     * 특별 안내사항
     */
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    /**
     * 활성 상태
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * 표시 순서
     */
    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer displayOrder = 0;
    
    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티 생성 시 자동으로 현재 시각 설정
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    /**
     * 엔티티 수정 시 자동으로 현재 시각 설정
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 접수지 정보 요약
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (contactPerson != null && !contactPerson.trim().isEmpty()) {
            sb.append(" (").append(contactPerson).append(")");
        }
        if (phone != null && !phone.trim().isEmpty()) {
            sb.append(" ").append(phone);
        }
        return sb.toString();
    }
    
    /**
     * 운영 상태 확인
     */
    public boolean isOperational() {
        return isActive != null && isActive;
    }
    
    /**
     * 생성자 - 필수 필드만
     */
    public InboundLocation(String name, String address) {
        this.name = name;
        this.address = address;
        this.isActive = true;
        this.displayOrder = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}