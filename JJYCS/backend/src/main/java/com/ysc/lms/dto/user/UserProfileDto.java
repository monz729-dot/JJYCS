package com.ysc.lms.dto.user;

import com.ysc.lms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private User.UserType userType;
    private String zipCode;
    private String address;
    private String addressDetail;
    private Map<String, Boolean> notifications;
    private boolean emailVerified;
    private boolean approved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}