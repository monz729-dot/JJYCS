package com.ycs.lms.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateProfileRequest {
    
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;
    
    private String zipCode;
    
    private String address;
    
    private String addressDetail;
    
    private Map<String, Boolean> notifications;
}