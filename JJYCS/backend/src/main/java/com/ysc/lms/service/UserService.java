package com.ysc.lms.service;

import com.ysc.lms.dto.user.*;
import com.ysc.lms.entity.User;
import com.ysc.lms.entity.EmailVerificationToken;
import com.ysc.lms.exception.ResourceNotFoundException;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CodeGenerationService codeGenerationService;
    
    public UserService(UserRepository userRepository, 
                      EmailVerificationTokenRepository tokenRepository,
                      @Lazy PasswordEncoder passwordEncoder,
                      @Lazy EmailService emailService,
                      CodeGenerationService codeGenerationService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.codeGenerationService = codeGenerationService;
    }
    
    public User createUser(User user) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // 회원 고유코드 자동 생성 (국가와 타입에 따라)
        String country = user.getAddress() != null && user.getAddress().contains("태국") ? "THAILAND" : "KOREA";
        String memberCode = codeGenerationService.generateMemberCode(country, user.getUserType());
        user.setMemberCode(memberCode);
        
        // 기업/파트너는 승인 대기 상태로 설정
        if (user.getUserType() == User.UserType.CORPORATE || 
            user.getUserType() == User.UserType.PARTNER) {
            user.setStatus(User.UserStatus.PENDING);
        } else {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEmailVerified(false); // 실제로는 이메일 인증 로직 필요
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User approveUser(Long userId, String approvedBy, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new IllegalArgumentException("User is not in pending status");
        }
        
        user.setStatus(User.UserStatus.ACTIVE);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(approvedBy);
        user.setApprovalNotes(notes);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 멤버 코드가 없는 경우 생성 (승인 시점에서)
        if (user.getMemberCode() == null) {
            String country = user.getAddress() != null && user.getAddress().contains("태국") ? "THAILAND" : "KOREA";
            String memberCode = codeGenerationService.generateMemberCode(country, user.getUserType());
            user.setMemberCode(memberCode);
        }
        
        return userRepository.save(user);
    }
    
    public User rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new IllegalArgumentException("User is not in pending status");
        }
        
        user.setStatus(User.UserStatus.REJECTED);
        user.setRejectedAt(LocalDateTime.now());
        user.setRejectionReason(reason);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    private String generateMemberCode(String prefix) {
        // 해당 타입의 기존 멤버 코드 중 최대값 조회하여 +1
        // 간단한 구현 - 실제로는 더 정교한 로직 필요
        long count = userRepository.count();
        return String.format("%s%03d", prefix, count + 1);
    }
    
    // 프로필 관련 메소드들
    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        
        return UserProfileDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .userType(user.getUserType())
            .zipCode(user.getZipCode())
            .address(user.getAddress())
            .addressDetail(user.getAddressDetail())
            .notifications(parseNotifications(user.getNotificationSettings()))
            .emailVerified(user.getEmailVerified())
            .approved(user.getStatus() == User.UserStatus.ACTIVE)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
    
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getZipCode() != null) {
            user.setZipCode(request.getZipCode());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAddressDetail() != null) {
            user.setAddressDetail(request.getAddressDetail());
        }
        if (request.getNotifications() != null) {
            user.setNotificationSettings(convertNotificationsToString(request.getNotifications()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        
        return getUserProfile(updated.getEmail());
    }
    
    public void requestEmailChange(String currentEmail, String newEmail) {
        // 현재 사용자 확인
        User user = userRepository.findByEmail(currentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        
        // 새 이메일 중복 확인
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }
        
        // 기존 토큰 무효화 (이메일 변경 관련 토큰 삭제)
        tokenRepository.deleteByUserIdAndTokenType(user.getId(), EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
        
        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(user.getId());
        verificationToken.setEmail(newEmail);
        verificationToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(verificationToken);
        
        // 이메일 발송
        emailService.sendEmailChangeVerification(newEmail, token);
    }
    
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }
        
        // 새 비밀번호 설정
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public void deleteAccount(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다"));
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        
        // 사용자 상태를 DELETED로 변경 (실제 삭제 대신 소프트 삭제)
        user.setStatus(User.UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    private Map<String, Boolean> parseNotifications(String settings) {
        Map<String, Boolean> notifications = new HashMap<>();
        if (settings != null && !settings.isEmpty()) {
            String[] pairs = settings.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    notifications.put(keyValue[0], Boolean.parseBoolean(keyValue[1]));
                }
            }
        } else {
            // 기본값
            notifications.put("order", true);
            notifications.put("shipping", true);
            notifications.put("marketing", false);
        }
        return notifications;
    }
    
    private String convertNotificationsToString(Map<String, Boolean> notifications) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : notifications.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }
}