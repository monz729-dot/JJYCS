package com.ycs.lms.service;

import com.ycs.lms.dto.AuthResponse;
import com.ycs.lms.dto.LoginRequest;
import com.ycs.lms.dto.SignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock User Service - 개발용 임시 구현
 * 실제 구현에서는 Database와 연동
 */
@Slf4j
@Service
public class MockUserService implements UserService {

    // Mock 사용자 데이터베이스
    private final Map<String, MockUser> users = new HashMap<>();
    
    public MockUserService() {
        // 테스트 사용자 초기화
        initTestUsers();
    }
    
    @Override
    public AuthResponse signup(SignupRequest request) {
        log.info("Mock signup: {}", request.getEmail());
        
        if (users.containsKey(request.getEmail())) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }
        
        MockUser user = new MockUser();
        user.id = (long) (users.size() + 1);
        user.email = request.getEmail();
        user.name = request.getName();
        user.phone = request.getPhone();
        user.role = request.getRole();
        user.status = "enterprise".equals(request.getRole()) || "partner".equals(request.getRole()) 
                     ? "pending_approval" : "active";
        user.emailVerified = false;
        user.twoFactorEnabled = false;
        user.createdAt = new java.util.Date().toString();
        
        users.put(request.getEmail(), user);
        
        return AuthResponse.builder()
                .accessToken("mock_access_token_" + user.id)
                .refreshToken("mock_refresh_token_" + user.id)
                .tokenType("Bearer")
                .expiresIn(86400)
                .user(user.toUserDto())
                .requiresEmailVerification(!user.emailVerified)
                .requiresApproval("pending_approval".equals(user.status))
                .requiresTwoFactor(false)
                .build();
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Mock login: {}", request.getEmail());
        
        MockUser user = users.get(request.getEmail());
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        // 비밀번호 검증 (Mock에서는 모든 비밀번호를 허용)
        
        return AuthResponse.builder()
                .accessToken("mock_access_token_" + user.id)
                .refreshToken("mock_refresh_token_" + user.id)
                .tokenType("Bearer")
                .expiresIn(86400)
                .user(user.toUserDto())
                .requiresEmailVerification(!user.emailVerified)
                .requiresApproval("pending_approval".equals(user.status))
                .requiresTwoFactor(user.twoFactorEnabled)
                .build();
    }
    
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Mock refresh token");
        
        // Mock 구현 - 새로운 액세스 토큰 반환
        return AuthResponse.builder()
                .accessToken("mock_new_access_token_" + System.currentTimeMillis())
                .refreshToken(refreshToken) // 리프레시 토큰은 그대로 유지
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();
    }
    
    @Override
    public void verifyEmail(String token) {
        log.info("Mock email verification: {}", token);
        // Mock 구현 - 항상 성공
    }
    
    @Override
    public void requestPasswordReset(String email) {
        log.info("Mock password reset request: {}", email);
        // Mock 구현 - 항상 성공
    }
    
    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Mock password reset");
        // Mock 구현 - 항상 성공
    }
    
    @Override
    public Map<String, Object> setup2FA() {
        log.info("Mock 2FA setup");
        return Map.of(
            "qrCode", "mock_qr_code_url",
            "secret", "mock_secret_key",
            "backupCodes", new String[]{"123456", "789012"}
        );
    }
    
    @Override
    public void approveUser(Long userId, String memberCode, String note) {
        log.info("Mock user approval: userId={}, memberCode={}", userId, memberCode);
        // Mock 구현 - 항상 성공
    }
    
    @Override
    public Object getUserProfile(Long userId) {
        log.info("Mock get user profile: {}", userId);
        
        // userId로 사용자 찾기 (Mock)
        MockUser user = users.values().stream()
                .filter(u -> u.id.equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                
        return user.toUserDto();
    }
    
    @Override
    public Object updateProfile(Long userId, Object request) {
        log.info("Mock update profile: {}", userId);
        
        MockUser user = users.values().stream()
                .filter(u -> u.id.equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                
        return user.toUserDto();
    }
    
    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Mock change password: {}", userId);
        // Mock 구현 - 항상 성공
    }
    
    @Override
    public void resendEmailVerification(Long userId) {
        log.info("Mock resend email verification: {}", userId);
        // Mock 구현 - 항상 성공
    }
    
    private void initTestUsers() {
        // 관리자
        MockUser admin = new MockUser();
        admin.id = 1L;
        admin.email = "admin@ycs.com";
        admin.name = "관리자";
        admin.phone = "010-1111-1111";
        admin.role = "admin";
        admin.status = "active";
        admin.memberCode = "ADMIN001";
        admin.emailVerified = true;
        admin.twoFactorEnabled = false;
        admin.createdAt = new java.util.Date().toString();
        users.put(admin.email, admin);
        
        // 창고 관리자
        MockUser warehouse = new MockUser();
        warehouse.id = 2L;
        warehouse.email = "warehouse@ycs.com";
        warehouse.name = "창고관리자";
        warehouse.phone = "010-2222-2222";
        warehouse.role = "warehouse";
        warehouse.status = "active";
        warehouse.memberCode = "WH001";
        warehouse.emailVerified = true;
        warehouse.twoFactorEnabled = false;
        warehouse.createdAt = new java.util.Date().toString();
        users.put(warehouse.email, warehouse);
        
        // 개인 사용자
        MockUser individual = new MockUser();
        individual.id = 3L;
        individual.email = "user1@example.com";
        individual.name = "김개인";
        individual.phone = "010-3333-3333";
        individual.role = "individual";
        individual.status = "active";
        individual.memberCode = "IND001";
        individual.emailVerified = true;
        individual.twoFactorEnabled = false;
        individual.createdAt = new java.util.Date().toString();
        users.put(individual.email, individual);
        
        // 기업 사용자
        MockUser enterprise = new MockUser();
        enterprise.id = 4L;
        enterprise.email = "company@corp.com";
        enterprise.name = "이기업";
        enterprise.phone = "010-4444-4444";
        enterprise.role = "enterprise";
        enterprise.status = "active";
        enterprise.memberCode = "ENT001";
        enterprise.emailVerified = true;
        enterprise.twoFactorEnabled = false;
        enterprise.createdAt = new java.util.Date().toString();
        users.put(enterprise.email, enterprise);
        
        // 파트너
        MockUser partner = new MockUser();
        partner.id = 5L;
        partner.email = "partner@affiliate.com";
        partner.name = "박파트너";
        partner.phone = "010-5555-5555";
        partner.role = "partner";
        partner.status = "active";
        partner.memberCode = "PTN001";
        partner.emailVerified = true;
        partner.twoFactorEnabled = false;
        partner.createdAt = new java.util.Date().toString();
        users.put(partner.email, partner);
    }
    
    // Mock User 클래스
    static class MockUser {
        Long id;
        String email;
        String name;
        String phone;
        String role;
        String status;
        String memberCode;
        boolean emailVerified;
        boolean twoFactorEnabled;
        String createdAt;
        
        public Map<String, Object> toUserDto() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", id);
            dto.put("email", email);
            dto.put("name", name);
            dto.put("phone", phone);
            dto.put("role", role);
            dto.put("status", status);
            dto.put("memberCode", memberCode);
            dto.put("emailVerified", emailVerified);
            dto.put("twoFactorEnabled", twoFactorEnabled);
            dto.put("createdAt", createdAt);
            dto.put("updatedAt", createdAt);
            return dto;
        }
    }
}

// UserService 인터페이스
interface UserService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void verifyEmail(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    Map<String, Object> setup2FA();
    void approveUser(Long userId, String memberCode, String note);
    Object getUserProfile(Long userId);
    Object updateProfile(Long userId, Object request);
    void changePassword(Long userId, String currentPassword, String newPassword);
    void resendEmailVerification(Long userId);
}