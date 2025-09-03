package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 택배사 정보 엔티티
 */
@Entity
@Table(name = "courier_companies", uniqueConstraints = {
    @UniqueConstraint(name = "uk_courier_companies_code", columnNames = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierCompany {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 택배사 코드 (고유)
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;
    
    /**
     * 택배사명
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 택배사명 (영문)
     */
    @Column(name = "name_en", length = 100)
    private String nameEn;
    
    /**
     * 웹사이트 URL
     */
    @Column(name = "website", length = 200)
    private String website;
    
    /**
     * 송장 추적 URL 템플릿
     */
    @Column(name = "tracking_url_template", length = 500)
    private String trackingUrlTemplate;
    
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
     * 송장번호로 추적 URL 생성
     */
    public String generateTrackingUrl(String trackingNumber) {
        if (trackingUrlTemplate == null || trackingUrlTemplate.trim().isEmpty() ||
            trackingNumber == null || trackingNumber.trim().isEmpty()) {
            return null;
        }
        return trackingUrlTemplate.replace("{trackingNumber}", trackingNumber.trim());
    }
    
    /**
     * 추적 기능 지원 여부
     */
    public boolean supportsTracking() {
        return trackingUrlTemplate != null && 
               !trackingUrlTemplate.trim().isEmpty() && 
               trackingUrlTemplate.contains("{trackingNumber}");
    }
    
    /**
     * 표시용 이름 (한/영 조합)
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (nameEn != null && !nameEn.trim().isEmpty() && !nameEn.equals(name)) {
            sb.append(" (").append(nameEn).append(")");
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
    public CourierCompany(String code, String name) {
        this.code = code;
        this.name = name;
        this.isActive = true;
        this.displayOrder = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 택배사 정보 요약
     */
    @Override
    public String toString() {
        return String.format("CourierCompany{id=%d, code='%s', name='%s', active=%s}", 
            id, code, name, isActive);
    }
}