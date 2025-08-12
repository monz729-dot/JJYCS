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
public class PartnerProfile {
    private Long id;
    private Long userId;
    private String partnerType; // referral, campaign
    private String referralCode;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private BigDecimal commissionRate; // 5%
    private Integer totalReferrals;
    private BigDecimal totalCommission;
    private BigDecimal pendingCommission;
    private LocalDateTime lastSettlementAt;
    private String approvalNote;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}