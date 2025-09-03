package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Supabase user_profiles 테이블 매핑 엔티티
 * auth.users 테이블과 1:1 관계로 연결
 */
@Entity
@Table(name = "user_profiles", schema = "public")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id; // auth.users.id와 연결
    
    private String username;
    
    private String name;
    
    private String phone;
    
    @Column(columnDefinition = "text")
    private String address;
    
    @Column(name = "user_type")
    private String userType; // GENERAL, CORPORATE, PARTNER, WAREHOUSE, ADMIN
    
    @Column(name = "business_license_url", columnDefinition = "text")
    private String businessLicenseUrl;
    
    @Column(name = "terms_agreed")
    private Boolean termsAgreed;
    
    @Column(name = "privacy_agreed")
    private Boolean privacyAgreed;
    
    @Column(name = "approval_status")
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    
    @Column(name = "email_verified")
    private Boolean emailVerified;
    
    @CreatedDate
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    private String email;
    
    @Column(name = "manager_name")
    private String managerName;
    
    @Column(name = "manager_contact")
    private String managerContact;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "business_number")
    private String businessNumber;
    
    // 편의 메서드들
    public boolean isApproved() {
        return "APPROVED".equals(this.approvalStatus);
    }
    
    public boolean isPending() {
        return "PENDING".equals(this.approvalStatus);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(this.approvalStatus);
    }
    
    public boolean isCorporate() {
        return "CORPORATE".equals(this.userType);
    }
    
    public boolean isPartner() {
        return "PARTNER".equals(this.userType);
    }
    
    public boolean isGeneral() {
        return "GENERAL".equals(this.userType);
    }
}