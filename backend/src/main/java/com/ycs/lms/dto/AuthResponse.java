package com.ycs.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private Map<String, Object> user;
    private boolean requiresEmailVerification;
    private boolean requiresApproval;
    private boolean requiresTwoFactor;
}