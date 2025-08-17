package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;
    private boolean requiresEmailVerification;
    private boolean requiresApproval;
    private boolean requiresTwoFactor;
    private String message;
    
    // Getter methods for explicit access
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public boolean isRequiresTwoFactor() {
        return requiresTwoFactor;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String role;
        private String status;
        private String memberCode;
        private boolean emailVerified;
        private boolean twoFactorEnabled;
        private LocalDateTime createdAt;
        
        // Getter methods for explicit access
        public String getStatus() {
            return status;
        }
    }
}
