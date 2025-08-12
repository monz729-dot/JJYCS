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
public class EnterpriseProfile {
    private Long id;
    private Long userId;
    private String companyName;
    private String businessNumber;
    private String companyAddress;
    private String ceoName;
    private String businessType;
    private Integer employeeCount;
    private BigDecimal annualRevenue;
    private String businessRegistrationFile; // S3 URL
    private String taxCertificateFile; // S3 URL
    private String approvalNote;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}