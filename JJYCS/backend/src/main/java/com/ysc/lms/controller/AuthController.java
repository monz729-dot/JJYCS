package com.ysc.lms.controller;

import com.ysc.lms.entity.EmailVerificationToken;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.EmailVerificationTokenRepository;
import com.ysc.lms.service.EmailService;
import com.ysc.lms.service.UserService;
import com.ysc.lms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        try {
            System.out.println("=== Signup request received ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("UserType: " + request.getUserType());
            
            // userType 유효성 검사
            if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "회원 유형을 선택해주세요."));
            }
            
            User.UserType userType;
            try {
                userType = User.UserType.valueOf(request.getUserType().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid userType: " + request.getUserType());
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "잘못된 회원 유형입니다: " + request.getUserType()));
            }
            
            // 사용자 생성
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setUserType(userType);
            
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
            
            // JWT 토큰 생성 (승인 대기 상태가 아닌 경우)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("user", savedUser);
            response.put("isPending", savedUser.getStatus() == User.UserStatus.PENDING);
            
            if (savedUser.getStatus() != User.UserStatus.PENDING) {
                String accessToken = jwtUtil.generateToken(savedUser);
                String refreshToken = jwtUtil.generateRefreshToken(savedUser);
                
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);
                response.put("token", accessToken); // 기존 호환성을 위해 유지
                response.put("tokenType", "Bearer");
                response.put("expiresIn", 86400); // 24시간 (초 단위)
            }
            
            return ResponseEntity.ok(response);
            
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
            // 입력값 유효성 검사
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일을 입력해주세요.", "field", "email"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "비밀번호를 입력해주세요.", "field", "password"));
            }
            
            // 이메일 형식 검증
            if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "올바른 이메일 형식을 입력해주세요.", "field", "email"));
            }
            
            var userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "user not found", "field", "email", 
                                "message", "등록되지 않은 이메일입니다."));
            }
            
            User user = userOpt.get();
            
            // 비밀번호 확인
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "invalid password", "field", "password",
                                "message", "비밀번호가 일치하지 않습니다."));
            }
            
            // 계정 상태 확인
            if (user.getStatus() == User.UserStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "pending approval", "field", "email",
                                "message", "승인 대기 중인 계정입니다. 관리자 승인 후 이용 가능합니다."));
            }
            
            if (user.getStatus() == User.UserStatus.REJECTED) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "account rejected", "field", "email",
                                "message", "계정이 거부되었습니다. 고객센터에 문의하세요."));
            }
            
            if (user.getStatus() == User.UserStatus.SUSPENDED) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "account suspended", "field", "email",
                                "message", "정지된 계정입니다. 고객센터에 문의하세요."));
            }
            
            if (!user.getEmailVerified()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "email not verified", "field", "email",
                                "message", "이메일 인증이 필요합니다. 이메일을 확인해주세요."));
            }
            
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "account inactive", "field", "email",
                                "message", "비활성화된 계정입니다. 관리자에게 문의해주세요."));
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
            log.error("Login error for email: " + request.getEmail(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "field", "general"));
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
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        try {
            String token = null;
            
            // Authorization 헤더에서 토큰 추출
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            // 리프레시 토큰 처리
            String refreshToken = null;
            if (request != null && request.getRefreshToken() != null) {
                refreshToken = request.getRefreshToken();
            }
            
            // 토큰 블랙리스트 추가 (현재는 JWT 무상태이므로 실제 블랙리스트 구현은 향후 Redis 등으로)
            if (token != null) {
                // TODO: 실제 구현에서는 Redis 등에 토큰을 블랙리스트로 저장
                System.out.println("Adding access token to blacklist: " + token.substring(0, Math.min(20, token.length())) + "...");
            }
            
            if (refreshToken != null) {
                // TODO: 리프레시 토큰도 블랙리스트에 추가
                System.out.println("Adding refresh token to blacklist: " + refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");
            }
            
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
    
    // 새로운 비밀번호 찾기 API - 이름, 연락처, 이메일로 사용자 확인 후 인증번호 발송
    @PostMapping("/find-password")
    @Transactional
    public ResponseEntity<Map<String, Object>> findPassword(@RequestBody FindPasswordRequest request) {
        try {
            log.info("비밀번호 찾기 요청: name={}, phone={}, email={}", 
                request.getName(), request.getPhone(), request.getEmail());
                
            // 입력값 검증
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이름을 입력해주세요.", "field", "name"));
            }
            
            if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "연락처를 입력해주세요.", "field", "phone"));
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일을 입력해주세요.", "field", "email"));
            }
            
            // 이메일 형식 검증
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!request.getEmail().matches(emailRegex)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "올바른 이메일 형식을 입력해주세요.", "field", "email"));
            }
            
            // 사용자 정보로 계정 찾기
            var userOpt = userService.findByEmailAndNameAndPhone(
                request.getEmail(), request.getName(), request.getPhone());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "입력하신 정보와 일치하는 계정을 찾을 수 없습니다.", "field", "general"));
            }
            
            User user = userOpt.get();
            
            // 계정 상태 확인
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                String statusMessage = switch(user.getStatus()) {
                    case PENDING -> "승인 대기 중인 계정입니다.";
                    case REJECTED -> "거부된 계정입니다. 고객센터에 문의해주세요.";
                    case SUSPENDED -> "정지된 계정입니다. 고객센터에 문의해주세요.";
                    case WITHDRAWN -> "탈퇴된 계정입니다.";
                    case DELETED -> "삭제된 계정입니다.";
                    default -> "비활성화된 계정입니다.";
                };
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", statusMessage, "field", "general"));
            }
            
            // 기존 비밀번호 재설정 토큰들 삭제
            tokenRepository.deleteByUserIdAndTokenType(user.getId(), 
                EmailVerificationToken.TokenType.PASSWORD_RESET);
            
            // 6자리 인증번호 생성
            String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
            
            // 인증번호 토큰 저장
            EmailVerificationToken emailToken = new EmailVerificationToken();
            emailToken.setToken(verificationCode);
            emailToken.setEmail(user.getEmail());
            emailToken.setUserId(user.getId());
            emailToken.setTokenType(EmailVerificationToken.TokenType.PASSWORD_RESET);
            emailToken.setCreatedAt(LocalDateTime.now());
            emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // 10분 유효
            
            tokenRepository.save(emailToken);
            
            // 인증번호 이메일 전송
            try {
                emailService.sendPasswordResetVerificationCode(user.getEmail(), user.getName(), verificationCode);
                log.info("비밀번호 찾기 인증번호 발송 완료: {}", user.getEmail());
            } catch (Exception e) {
                log.error("비밀번호 찾기 인증번호 이메일 전송 실패: ", e);
                return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", "field", "general"));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "입력하신 이메일로 인증번호가 발송되었습니다. 10분 이내에 인증해주세요.",
                "email", user.getEmail()
            ));
            
        } catch (Exception e) {
            log.error("비밀번호 찾기 중 오류: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "비밀번호 찾기 중 오류가 발생했습니다.", "field", "general"));
        }
    }
    
    // 비밀번호 재설정 인증번호 검증
    @PostMapping("/verify-password-reset")
    public ResponseEntity<Map<String, Object>> verifyPasswordReset(@RequestBody VerifyPasswordResetRequest request) {
        try {
            log.info("비밀번호 재설정 인증번호 검증: email={}", request.getEmail());
            
            // 입력값 검증
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일을 입력해주세요.", "field", "email"));
            }
            
            if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증번호를 입력해주세요.", "field", "verificationCode"));
            }
            
            // 인증번호 검증
            var tokenOpt = tokenRepository.findByTokenAndTokenTypeAndUsedFalse(
                request.getVerificationCode(), EmailVerificationToken.TokenType.PASSWORD_RESET);
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "올바르지 않은 인증번호입니다.", "field", "verificationCode"));
            }
            
            EmailVerificationToken token = tokenOpt.get();
            
            // 이메일 일치 확인
            if (!token.getEmail().equals(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일 정보가 일치하지 않습니다.", "field", "email"));
            }
            
            // 토큰 만료 확인
            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증번호가 만료되었습니다. 새로운 인증번호를 요청해주세요.", "field", "verificationCode"));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증번호가 확인되었습니다. 새로운 비밀번호를 설정해주세요.",
                "verified", true
            ));
            
        } catch (Exception e) {
            log.error("인증번호 검증 중 오류: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "인증번호 검증 중 오류가 발생했습니다.", "field", "general"));
        }
    }
    
    // 비밀번호 직접 재설정
    @PostMapping("/reset-password-direct")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetPasswordDirect(@RequestBody ResetPasswordDirectRequest request) {
        try {
            log.info("비밀번호 직접 재설정: email={}", request.getEmail());
            
            // 입력값 검증
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일을 입력해주세요.", "field", "email"));
            }
            
            if (request.getVerificationCode() == null || request.getVerificationCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증번호를 입력해주세요.", "field", "verificationCode"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "새 비밀번호를 입력해주세요.", "field", "newPassword"));
            }
            
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "비밀번호 확인을 입력해주세요.", "field", "confirmPassword"));
            }
            
            // 비밀번호 일치 확인
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", "field", "confirmPassword"));
            }
            
            // 비밀번호 길이 검증
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "비밀번호는 6자 이상이어야 합니다.", "field", "newPassword"));
            }
            
            // 인증번호 검증
            var tokenOpt = tokenRepository.findByTokenAndTokenTypeAndUsedFalse(
                request.getVerificationCode(), EmailVerificationToken.TokenType.PASSWORD_RESET);
            
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "올바르지 않은 인증번호입니다.", "field", "verificationCode"));
            }
            
            EmailVerificationToken token = tokenOpt.get();
            
            // 이메일 일치 확인
            if (!token.getEmail().equals(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일 정보가 일치하지 않습니다.", "field", "email"));
            }
            
            // 토큰 만료 확인
            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증번호가 만료되었습니다. 처음부터 다시 진행해주세요.", "field", "verificationCode"));
            }
            
            // 사용자 조회
            var userOpt = userService.findById(token.getUserId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "사용자 정보를 찾을 수 없습니다.", "field", "general"));
            }
            
            User user = userOpt.get();
            
            // 비밀번호 변경
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            userService.updatePassword(user.getId(), encodedPassword);
            
            // 토큰 사용 처리
            token.setUsed(true);
            token.setUsedAt(LocalDateTime.now());
            tokenRepository.save(token);
            
            log.info("비밀번호 재설정 완료: userId={}, email={}", user.getId(), user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 로그인해주세요."
            ));
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "비밀번호 재설정 중 오류가 발생했습니다.", "field", "general"));
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
    
    /**
     * 이메일 인증 코드 전송 (회원가입용)
     */
    @PostMapping("/send-verification")
    @Transactional
    public ResponseEntity<Map<String, Object>> sendVerificationCode(@RequestBody SendVerificationRequest request) {
        try {
            // 이미 가입된 이메일인지 확인
            if (userService.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이미 가입된 이메일입니다."));
            }
            
            // 기존 인증 토큰 삭제
            tokenRepository.deleteByEmailAndTokenType(request.getEmail(), 
                EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
            
            // 6자리 인증 코드 생성
            String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
            
            // 토큰 저장 (코드를 토큰으로 사용)
            EmailVerificationToken emailToken = new EmailVerificationToken();
            emailToken.setToken(verificationCode);
            emailToken.setEmail(request.getEmail());
            emailToken.setUserId(-1L); // 회원가입 전이므로 임시값 -1
            emailToken.setTokenType(EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
            emailToken.setCreatedAt(LocalDateTime.now());
            emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5분 유효
            
            tokenRepository.save(emailToken);
            
            // 이메일 전송
            try {
                emailService.sendVerificationEmail(request.getEmail(), "고객님", verificationCode);
                System.out.println("Verification code sent to: " + request.getEmail() + ", code: " + verificationCode);
            } catch (Exception e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
                return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증코드가 이메일로 전송되었습니다.",
                "expiresIn", 300 // 5분 = 300초
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "인증코드 전송 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 이메일 인증 코드 확인 (회원가입용)
     */
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody CheckEmailRequest request) {
        try {
            // 입력값 유효성 검사
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일을 입력해주세요."));
            }
            
            // 이메일 형식 검증
            if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "올바른 이메일 형식을 입력해주세요."));
            }
            
            var userOpt = userService.findByEmail(request.getEmail());
            boolean exists = userOpt.isPresent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            
            if (exists) {
                User user = userOpt.get();
                // 계정 상태 정보도 함께 반환 (로그인 시 미리 확인 가능)
                response.put("status", user.getStatus().toString());
                response.put("emailVerified", user.getEmailVerified());
                response.put("userType", user.getUserType().toString());
            } else {
                response.put("message", "등록되지 않은 이메일입니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Email check error: ", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "이메일 확인 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            log.info("Verifying code: {} for email: {}", request.getCode(), request.getEmail());
            
            var tokenOpt = tokenRepository.findByTokenAndTokenTypeAndUsedFalse(
                request.getCode(), EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
            
            if (tokenOpt.isEmpty()) {
                log.warn("Token not found for code: {} and email: {}", request.getCode(), request.getEmail());
                
                // 추가 디버깅: 해당 이메일의 모든 토큰 확인
                var allTokens = tokenRepository.findByEmailAndTokenType(
                    request.getEmail(), EmailVerificationToken.TokenType.EMAIL_VERIFICATION);
                log.info("All tokens for email {}: {}", request.getEmail(), 
                    allTokens.stream().map(t -> "token=" + t.getToken() + ", used=" + t.isUsed() + ", expired=" + t.isExpired()).toList());
                
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "잘못된 인증코드입니다."));
            }
            
            EmailVerificationToken emailToken = tokenOpt.get();
            
            // 이메일 일치 확인
            if (!emailToken.getEmail().equals(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "이메일과 인증코드가 일치하지 않습니다."));
            }
            
            if (emailToken.isExpired()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "인증코드가 만료되었습니다. 다시 요청해주세요."));
            }
            
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
                .body(Map.of("success", false, "error", "인증코드 확인 중 오류가 발생했습니다."));
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
    
    public static class LogoutRequest {
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
    
    public static class SendVerificationRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class VerifyCodeRequest {
        private String email;
        private String code;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class CheckEmailRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    // 새로운 비밀번호 찾기 방식을 위한 Request DTO들
    public static class FindPasswordRequest {
        private String name;
        private String phone;
        private String email;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class VerifyPasswordResetRequest {
        private String email;
        private String verificationCode;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    }
    
    public static class ResetPasswordDirectRequest {
        private String email;
        private String verificationCode;
        private String newPassword;
        private String confirmPassword;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getVerificationCode() { return verificationCode; }
        public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}