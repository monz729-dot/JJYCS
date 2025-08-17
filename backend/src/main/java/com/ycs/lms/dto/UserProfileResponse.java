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
public class UserProfileResponse {
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
    private LocalDateTime lastLoginAt;
}
