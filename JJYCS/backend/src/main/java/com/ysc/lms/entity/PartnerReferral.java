package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_referrals")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PartnerReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private User partner; // PARTNER 타입의 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id")
    private User referredUser; // 추천받은 사용자 (가입 후 설정됨)

    @Column(name = "referral_code", nullable = false, length = 50)
    private String referralCode; // 추천 코드 (예: PART001-REF123)

    @Column(length = 100)
    private String referralUrl; // 추천 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status = ReferralStatus.ACTIVE;

    @Column
    private Integer totalClicks = 0; // 클릭 수

    @Column
    private Integer totalSignups = 0; // 가입 수

    @Column
    private Integer totalOrders = 0; // 주문 수

    @Column(length = 500)
    private String description; // 설명

    @Column
    private LocalDateTime expiresAt; // 만료일

    @Column
    private LocalDateTime lastUsedAt; // 마지막 사용일

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum ReferralStatus {
        ACTIVE,      // 활성
        INACTIVE,    // 비활성
        EXPIRED,     // 만료됨
        SUSPENDED    // 정지됨
    }

    // 추천 코드 생성
    public static String generateReferralCode(User partner) {
        return partner.getMemberCode() + "-REF" + System.currentTimeMillis() % 10000;
    }

    // 클릭 증가
    public void incrementClicks() {
        this.totalClicks++;
        this.lastUsedAt = LocalDateTime.now();
    }

    // 가입 증가
    public void incrementSignups(User newUser) {
        this.totalSignups++;
        this.referredUser = newUser;
        this.lastUsedAt = LocalDateTime.now();
    }

    // 주문 증가
    public void incrementOrders() {
        this.totalOrders++;
    }

    // 만료 확인
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // 활성 상태 확인
    public boolean isActive() {
        return status == ReferralStatus.ACTIVE && !isExpired();
    }
}