package com.ycs.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.ycs.lms.listener.UserEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(length = 20)
    private String memberCode; // 회원 코드

    // 기업회원 정보
    @Column(length = 200)
    private String companyName;

    @Column(length = 50)
    private String businessNumber;

    @Column(length = 100)
    private String companyAddress;

    // 파트너 정보 (추천/수수료 기능 제거됨)
    // partnerRegion, commissionRate, referredBy 필드 제거됨

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    // 승인 관련 필드
    @Column(length = 100)
    private String approvedBy; // 승인자

    @Column
    private LocalDateTime approvedAt; // 승인 시간

    @Column(length = 500)
    private String approvalNotes; // 승인/거부 사유

    @Column
    private LocalDateTime rejectedAt; // 거부 시간

    @Column(length = 500)
    private String rejectionReason; // 거부 사유

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum UserType {
        GENERAL,    // 일반 회원
        CORPORATE,  // 기업 회원
        PARTNER,    // 파트너
        ADMIN       // 관리자
    }

    public enum UserStatus {
        ACTIVE,     // 활성
        PENDING,    // 승인 대기
        REJECTED,   // 승인 거부
        SUSPENDED,  // 정지
        WITHDRAWN   // 탈퇴
    }
    
    // Approval helper methods
    public void approve(String approverName, String notes) {
        this.status = UserStatus.ACTIVE;
        this.approvedBy = approverName;
        this.approvedAt = LocalDateTime.now();
        this.approvalNotes = notes;
    }
    
    public void reject(String approverName, String reason) {
        this.status = UserStatus.REJECTED;
        this.approvedBy = approverName;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    public boolean isPendingApproval() {
        return this.status == UserStatus.PENDING;
    }
    
    public boolean needsApproval() {
        return (this.userType == UserType.CORPORATE || this.userType == UserType.PARTNER) 
               && this.status == UserStatus.PENDING;
    }
}