package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String name;
    private String phone;
    private String role; // individual, enterprise, partner, warehouse, admin
    private String status; // active, pending_approval, suspended, inactive
    private String memberCode;
    private boolean emailVerified;
    private String emailVerificationToken;
    private boolean twoFactorEnabled;
    private String twoFactorSecret;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiresAt;
    private LocalDateTime lastLoginAt;
    private int loginAttempts;
    private LocalDateTime lockedUntil;
    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeMarketing;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional profile based on role
    private EnterpriseProfile enterpriseProfile;
    private PartnerProfile partnerProfile;
    private WarehouseProfile warehouseProfile;
}