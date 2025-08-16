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
    private UserRole role = UserRole.INDIVIDUAL;
    private UserStatus status = UserStatus.ACTIVE;
    private String memberCode;
    private boolean emailVerified = false;
    private String emailVerificationToken;
    private boolean twoFactorEnabled = false;
    private String twoFactorSecret;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiresAt;
    private LocalDateTime lastLoginAt;
    private int loginAttempts = 0;
    private LocalDateTime lockedUntil;
    private boolean agreeTerms = false;
    private boolean agreePrivacy = false;
    private boolean agreeMarketing = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Enum definitions
    public enum UserRole {
        INDIVIDUAL, ENTERPRISE, PARTNER, WAREHOUSE, ADMIN;
        
        @Override
        public String toString() {
            return name();
        }
        
        public String toUpperCase() {
            return name().toUpperCase();
        }
    }
    
    public enum UserStatus {
        ACTIVE, PENDING_APPROVAL, SUSPENDED, INACTIVE;
        
        @Override
        public String toString() {
            return name();
        }
    }
}