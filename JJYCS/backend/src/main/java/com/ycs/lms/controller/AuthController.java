package com.ycs.lms.controller;

import com.ycs.lms.entity.EmailVerificationToken;
import com.ycs.lms.entity.User;
import com.ycs.lms.repository.EmailVerificationTokenRepository;
import com.ycs.lms.service.EmailService;
import com.ycs.lms.service.UserService;
import com.ycs.lms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        try {
            // 사용자 생성
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setUserType(User.UserType.valueOf(request.getUserType().toUpperCase()));
            
            // 기업 회원인 경우 추가 정보 설정
            if (user.getUserType() == User.UserType.CORPORATE) {
                user.setCompanyName(request.getCompanyName());
                user.setBusinessNumber(request.getBusinessNumber());
                user.setCompanyAddress(request.getCompanyAddress());
            }
            
            // 파트너인 경우 추가 설정 (파트너 지역 기능은 제거됨)
            
            User savedUser = userService.createUser(user);
            
            // 이메일 인증 토큰 생성 및 발송
            String verificationToken = java.util.UUID.randomUUID().toString();
            EmailVerificationToken emailToken = new EmailVerificationToken();
            emailToken.setToken(verificationToken);
            emailToken.setEmail(savedUser.getEmail());
            emailToken.setUserId(savedUser.getId());
            emailToken.setTokenType(EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
            emailToken.setCreatedAt(LocalDateTime.now());
            emailToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24시간 유효
            
            tokenRepository.save(emailToken);
            
            // 이메일 전송
            try {
                emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verificationToken);
                System.out.println("Email verification sent to: " + savedUser.getEmail());
            } catch (Exception e) {
                // 이메일 전송 실패해도 회원가입은 성공으로 처리
                System.err.println("Failed to send verification email: " + e.getMessage());
            }
            
            // 비밀번호는 응답에서 제외
            savedUser.setPassword(null);
            
            String message;
            if (savedUser.getStatus() == User.UserStatus.PENDING) {
                message = "회원가입이 완료되었습니다. 기업/파트너 계정은 관리자 승인 후 이용 가능합니다. (평일 1~2일 소요)";
            } else {
                message = "회원가입이 완료되었습니다.";
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "user", savedUser,
                "isPending", savedUser.getStatus() == User.UserStatus.PENDING
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            
            var userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일 또는 비밀번호가 올바르지 않습니다."));
            }
            
            User user = userOpt.get();
            
            // 비밀번호 확인
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일 또는 비밀번호가 올바르지 않습니다."));
            }
            
            // 계정 상태 확인
            if (user.getStatus() == User.UserStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "계정이 승인 대기 중입니다. 관리자 승인 후 이용 가능합니다."));
            }
            
            if (user.getStatus() == User.UserStatus.REJECTED) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "계정이 거부되었습니다. 고객센터에 문의하세요."));
            }
            
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "계정이 비활성화되었습니다."));
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            
            // 비밀번호는 응답에서 제외
            user.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", user);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("token", accessToken); // 기존 호환성을 위해 유지
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400); // 24시간 (초 단위)
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "로그인 처리 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            // Refresh 토큰 유효성 검증
            if (refreshToken == null || !jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효하지 않은 refresh token입니다."));
            }
            
            String email = jwtUtil.getUsernameFromToken(refreshToken);
            if (!jwtUtil.validateToken(refreshToken, email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "만료된 refresh token입니다."));
            }
            
            // 사용자 정보 조회
            var userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "사용자를 찾을 수 없습니다."));
            }
            
            User user = userOpt.get();
            
            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "토큰 새로고침 성공");
            response.put("accessToken", newAccessToken);
            response.put("token", newAccessToken); // 기존 호환성을 위해 유지
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400); // 24시간 (초 단위)
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "토큰 새로고침 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증 토큰이 없습니다."));
            }
            
            String token = authHeader.substring(7);
            String email = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효하지 않은 토큰입니다."));
            }
            
            var userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "사용자를 찾을 수 없습니다."));
            }
            
            User user = userOpt.get();
            user.setPassword(null); // 비밀번호는 응답에서 제외
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", user,
                "permissions", getUserPermissions(user)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    private Map<String, Boolean> getUserPermissions(User user) {
        Map<String, Boolean> permissions = new HashMap<>();
        
        // 기본 권한
        permissions.put("canCreateOrder", true);
        permissions.put("canViewOwnOrders", true);
        
        // 역할별 권한
        if (user.getUserType() == User.UserType.ADMIN) {
            permissions.put("canViewAllOrders", true);
            permissions.put("canManageUsers", true);
            permissions.put("canApproveUsers", true);
            permissions.put("canViewReports", true);
            permissions.put("canManageWarehouse", true);
        } else if (user.getUserType() == User.UserType.CORPORATE) {
            permissions.put("canViewCompanyOrders", true);
            permissions.put("canManageEmployees", true);
        } else if (user.getUserType() == User.UserType.PARTNER) {
            permissions.put("canViewPartnerOrders", true);
            permissions.put("canViewCommission", true);
        }
        // GENERAL은 기본 권한만 가짐
        
        return permissions;
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 실제 구현에서는 토큰을 블랙리스트에 추가하거나 Redis에서 제거할 수 있습니다
            // 현재는 단순히 성공 응답만 반환
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/verify-email/{token}")
    public ResponseEntity<Map<String, Object>> verifyEmail(@PathVariable String token) {
        try {
            var tokenOpt = tokenRepository.findByTokenAndTokenTypeAndUsedFalse(
                token, EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효하지 않은 인증 토큰입니다."));
            }
            
            EmailVerificationToken emailToken = tokenOpt.get();
            
            if (emailToken.isExpired()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증 토큰이 만료되었습니다."));
            }
            
            // 사용자 이메일 인증 상태 업데이트
            var userOpt = userService.findById(emailToken.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "사용자를 찾을 수 없습니다."));
            }
            
            User user = userOpt.get();
            user.setEmailVerified(true);
            userService.updateUser(user);
            
            // 토큰 사용 처리
            emailToken.setUsed(true);
            emailToken.setUsedAt(LocalDateTime.now());
            tokenRepository.save(emailToken);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이메일 인증이 완료되었습니다."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "이메일 인증 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            var userOpt = userService.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                // 보안상 사용자가 존재하지 않아도 성공 응답
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."
                ));
            }
            
            User user = userOpt.get();
            
            // 기존 비밀번호 재설정 토큰들 삭제
            tokenRepository.deleteByUserIdAndTokenType(user.getId(), 
                EmailVerificationToken.TokenType.PASSWORD_RESET);
            
            // 새 토큰 생성
            String resetToken = java.util.UUID.randomUUID().toString();
            EmailVerificationToken emailToken = new EmailVerificationToken();
            emailToken.setToken(resetToken);
            emailToken.setEmail(user.getEmail());
            emailToken.setUserId(user.getId());
            emailToken.setTokenType(EmailVerificationToken.TokenType.PASSWORD_RESET);
            emailToken.setCreatedAt(LocalDateTime.now());
            emailToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 1시간 유효
            
            tokenRepository.save(emailToken);
            
            // 이메일 전송
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetToken);
                System.out.println("Password reset email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send password reset email: " + e.getMessage());
                // 나중에 메일 전송 실패 시 오류를 반환할 수 있지만, 다음과 같은 경우에는 성공으로 처리
                // return ResponseEntity.internalServerError()
                //     .body(Map.of("success", false, "error", "이메일 전송에 실패했습니다."));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "비밀번호 재설정 요청 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            var tokenOpt = tokenRepository.findByTokenAndTokenTypeAndUsedFalse(
                request.getToken(), EmailVerificationToken.TokenType.PASSWORD_RESET);
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효하지 않은 재설정 토큰입니다."));
            }
            
            EmailVerificationToken emailToken = tokenOpt.get();
            
            if (emailToken.isExpired()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "재설정 토큰이 만료되었습니다."));
            }
            
            // 사용자 비밀번호 업데이트
            var userOpt = userService.findById(emailToken.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "사용자를 찾을 수 없습니다."));
            }
            
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userService.updateUser(user);
            
            // 토큰 사용 처리
            emailToken.setUsed(true);
            emailToken.setUsedAt(LocalDateTime.now());
            tokenRepository.save(emailToken);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 성공적으로 변경되었습니다."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }
    
    // DTO 클래스들
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private String phone;
        private String userType;
        private String companyName;
        private String businessNumber;
        private String companyAddress;
        // partnerRegion 필드 제거됨 (추천/수수료 기능 제거)
        
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getBusinessNumber() { return businessNumber; }
        public void setBusinessNumber(String businessNumber) { this.businessNumber = businessNumber; }
        public String getCompanyAddress() { return companyAddress; }
        public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        @Override
        public String toString() {
            return "LoginRequest{email='" + email + "', password='***'}";
        }
    }
    
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
    
    public static class ForgotPasswordRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class ResetPasswordRequest {
        private String token;
        private String password;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}