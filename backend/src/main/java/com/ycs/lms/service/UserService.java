package com.ycs.lms.service;

import com.ycs.lms.dto.AuthResponse;
import com.ycs.lms.dto.ChangePasswordRequest;
import com.ycs.lms.dto.LoginRequest;
import com.ycs.lms.dto.SignupRequest;
import com.ycs.lms.dto.UpdateProfileRequest;
import com.ycs.lms.dto.UserProfileResponse;
import com.ycs.lms.entity.User;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.InvalidPasswordException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.mapper.UserMapper;
import com.ycs.lms.security.JwtTokenProvider;
import com.ycs.lms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    /**
     * 회원가입
     */
    public AuthResponse signup(SignupRequest request) {
        log.info("Creating user with email: {}, username: {}", request.getEmail(), request.getUsername());

        // 아이디 중복
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BadRequestException("이미 사용 중인 아이디입니다: " + request.getUsername());
        }
        // 이메일 중복
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new BadRequestException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }
        // 회원코드 중복(있을 때만)
        if (request.getMemberCode() != null && userMapper.existsByMemberCode(request.getMemberCode())) {
            throw new BadRequestException("이미 사용 중인 회원 코드입니다: " + request.getMemberCode());
        }

        User user = User.builder()
                .username(request.getUsername())                 // ★ 추가
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(User.UserRole.valueOf(request.getRole().toUpperCase()))
                .status(needsApproval(request.getRole()) ? User.UserStatus.PENDING_APPROVAL : User.UserStatus.ACTIVE)
                .memberCode(request.getMemberCode())
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .twoFactorEnabled(false)
                .agreeTerms(request.isAgreeTerms())
                .agreePrivacy(request.isAgreePrivacy())
                .agreeMarketing(request.isAgreeMarketing())
                .build();

        user = userMapper.save(user);

        sendVerificationEmail(user);

        // JWT 토큰 생성 (이메일 인증 전이라도 로그인 가능)
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .user(convertToUserInfo(user))
            .requiresEmailVerification(!user.isEmailVerified())
            .requiresApproval(user.getStatus() == User.UserStatus.PENDING_APPROVAL)
            .message(getSignupMessage(user))
            .build();
    }

    /**
     * 로그인
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userMapper.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found for email: {}", request.getEmail());
                return new BadRequestException("잘못된 이메일 또는 비밀번호입니다");
            });
        
        log.info("User found: id={}, email={}, password_hash={}", user.getId(), user.getEmail(), 
                user.getPasswordHash() != null ? "***HASH_EXISTS***" : "***NULL***");

        // 비밀번호 확인
        log.info("Password verification - input: '{}', stored hash: '{}'", request.getPassword(), user.getPasswordHash());
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        log.info("Password matches: {}", passwordMatches);
        
        if (!passwordMatches) {
            // 로그인 실패 횟수 증가
            incrementLoginAttempts(user);
            throw new BadRequestException("잘못된 이메일 또는 비밀번호입니다");
        }

        // 계정 잠금 확인
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("계정이 일시적으로 잠겼습니다. 나중에 다시 시도해주세요.");
        }

        // 계정 상태 확인
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new BadRequestException("정지된 계정입니다. 고객센터에 문의해주세요.");
        }

        if (user.getStatus() == User.UserStatus.INACTIVE) {
            throw new BadRequestException("비활성화된 계정입니다. 계정을 활성화해주세요.");
        }

        // 로그인 성공 - 실패 횟수 초기화 및 마지막 로그인 시간 업데이트
        user.setLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLockedUntil(null);
        userMapper.save(user);

        // JWT 토큰 생성
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .user(convertToUserInfo(user))
            .requiresEmailVerification(!user.isEmailVerified())
            .requiresApproval(user.getStatus() == User.UserStatus.PENDING_APPROVAL)
            .message("로그인 성공")
            .build();
    }

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userMapper.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .memberCode(user.getMemberCode())
            .emailVerified(user.isEmailVerified())
            .twoFactorEnabled(user.isTwoFactorEnabled())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    /**
     * 사용자 프로필 업데이트
     */
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getMemberCode() != null && !request.getMemberCode().equals(user.getMemberCode())) {
            if (userMapper.existsByMemberCode(request.getMemberCode())) {
                throw new BadRequestException("이미 사용 중인 회원 코드입니다");
            }
            user.setMemberCode(request.getMemberCode());
        }

        user = userMapper.save(user);
        return getUserProfile(user.getId());
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("현재 비밀번호가 올바르지 않습니다");
        }

        // 새 비밀번호 설정
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.save(user);
    }

    /**
     * 이메일 인증 재발송
     */
    public void resendEmailVerification(Long userId) {
        User user = getUserById(userId);

        if (user.isEmailVerified()) {
            throw new BadRequestException("이미 인증된 이메일입니다");
        }

        // 새 인증 토큰 생성
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        userMapper.save(user);

        // 인증 이메일 재발송
        sendVerificationEmail(user);
    }

    /**
     * 사용자 정보 조회 (ID로)
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userMapper.findById(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * 사용자 정보 업데이트
     */
    public User updateUser(Long userId, User updateRequest) {
        User user = getUserById(userId);

        if (updateRequest.getName() != null) user.setName(updateRequest.getName());
        if (updateRequest.getPhone() != null) user.setPhone(updateRequest.getPhone());
        if (updateRequest.getMemberCode() != null && !updateRequest.getMemberCode().equals(user.getMemberCode())) {
            if (userMapper.existsByMemberCode(updateRequest.getMemberCode())) {
                throw new BadRequestException("이미 사용 중인 회원 코드입니다");
            }
            user.setMemberCode(updateRequest.getMemberCode());
        }

        return userMapper.save(user);
    }

    /**
     * 사용자 승인 (관리자용) - 오버로드
     */
    public void approveUser(Long userId, String memberCode, String note) {
        User user = getUserById(userId);
        
        if (user.getStatus() != User.UserStatus.PENDING_APPROVAL) {
            throw new BadRequestException("승인 대기 상태가 아닙니다");
        }

        if (memberCode != null && !memberCode.trim().isEmpty()) {
            if (userMapper.existsByMemberCode(memberCode)) {
                throw new BadRequestException("이미 사용 중인 회원 코드입니다");
            }
            user.setMemberCode(memberCode);
        }

        user.setStatus(User.UserStatus.ACTIVE);
        userMapper.save(user);

        // 승인 완료 이메일 발송
        sendApprovalEmail(user);
    }
    
    /**
     * 사용자 승인 (관리자용)
     */
    public User approveUser(Long userId) {
        log.info("Approving user: {}", userId);

        User user = getUserById(userId);
        
        if (user.getStatus() != User.UserStatus.PENDING_APPROVAL) {
            throw new BadRequestException("승인 대기 상태가 아닙니다");
        }

        user.setStatus(User.UserStatus.ACTIVE);
        user = userMapper.save(user);

        // 승인 완료 이메일 발송
        sendApprovalEmail(user);

        return user;
    }

    /**
     * 사용자 거부 (관리자용)
     */
    public User rejectUser(Long userId, String reason) {
        log.info("Rejecting user: {} with reason: {}", userId, reason);

        User user = getUserById(userId);
        user.setStatus(User.UserStatus.INACTIVE);
        user = userMapper.save(user);

        // 거부 이메일 발송
        sendRejectionEmail(user, reason);

        return user;
    }

    /**
     * 승인 대기 중인 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {
        return userMapper.findByStatus(User.UserStatus.PENDING_APPROVAL.toString());
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public User verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        User user = userMapper.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BadRequestException("잘못된 인증 토큰입니다"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);

        return userMapper.save(user);
    }

    /**
     * 비밀번호 재설정 요청
     */
    public void requestPasswordReset(String email) {
        User user = userMapper.findByEmail(email).orElse(null);
        
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(24));
            userMapper.save(user);

            // 비밀번호 재설정 이메일 발송
            sendPasswordResetEmail(user, resetToken);
        }
        
        // 보안상 사용자 존재 여부와 관계없이 성공 메시지 반환
        log.info("Password reset requested for email: {}", email);
    }

    /**
     * 비밀번호 재설정
     */
    public User resetPassword(String token, String newPassword) {
        User user = userMapper.findByPasswordResetToken(token)
            .orElseThrow(() -> new BadRequestException("잘못된 재설정 토큰입니다"));

        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("재설정 토큰이 만료되었습니다");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        return userMapper.save(user);
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰 발급
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        // 리프레시 토큰 검증
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new BadRequestException("유효하지 않거나 만료된 리프레시 토큰입니다");
        }
        
        // 리프레시 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        
        // 사용자 조회
        User user = userMapper.findById(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));
        
        // 계정 상태 확인
        if (user.getStatus() == User.UserStatus.SUSPENDED || 
            user.getStatus() == User.UserStatus.INACTIVE) {
            throw new BadRequestException("비활성화된 계정입니다");
        }
        
        // 새 토큰 생성
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities());
        String newAccessToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId);
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600) // 1시간
            .user(convertToUserInfo(user))
            .message("토큰 갱신 성공")
            .build();
    }

    /**
     * 2FA 설정 (QR 코드 생성)
     */
    public Map<String, Object> setup2FA() {
        User currentUser = getCurrentUser();
        
        // 2FA 시크릿 생성 (Base32 인코딩)
        String secret = generateRandomBase32();
        
        // QR 코드 URL 생성 (Google Authenticator 호환)
        String qrCodeUrl = String.format(
            "otpauth://totp/YCS-LMS:%s?secret=%s&issuer=YCS-LMS",
            currentUser.getEmail(),
            secret
        );
        
        // 백업 코드 생성
        String[] backupCodes = generateBackupCodes();
        
        // 사용자 2FA 시크릿 저장 (실제 활성화는 verify2FA에서)
        currentUser.setTwoFactorSecret(secret);
        userMapper.save(currentUser);
        
        return java.util.Map.of(
            "qrCodeUrl", qrCodeUrl,
            "secret", secret,
            "backupCodes", backupCodes
        );
    }

    /**
     * 2FA 인증 코드 검증 및 활성화
     */
    public boolean verify2FA(String code) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getTwoFactorSecret() == null) {
            throw new BadRequestException("2FA가 설정되지 않았습니다");
        }
        
        // TOTP 코드 검증 (실제 구현에서는 TOTP 라이브러리 사용)
        boolean isValid = verifyTOTPCode(currentUser.getTwoFactorSecret(), code);
        
        if (isValid) {
            currentUser.setTwoFactorEnabled(true);
            userMapper.save(currentUser);
        }
        
        return isValid;
    }

    /**
     * 2FA 비활성화
     */
    public void disable2FA(String code) {
        User currentUser = getCurrentUser();
        
        if (!currentUser.isTwoFactorEnabled()) {
            throw new BadRequestException("2FA가 활성화되지 않았습니다");
        }
        
        // 현재 코드 검증
        if (!verifyTOTPCode(currentUser.getTwoFactorSecret(), code)) {
            throw new BadRequestException("잘못된 인증 코드입니다");
        }
        
        currentUser.setTwoFactorEnabled(false);
        currentUser.setTwoFactorSecret(null);
        userMapper.save(currentUser);
    }

    // Private helper methods for 2FA
    
    private String generateRandomBase32() {
        // 32자리 Base32 문자열 생성 (실제 구현에서는 Apache Commons Codec 등 사용)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
    
    private String[] generateBackupCodes() {
        String[] codes = new String[8];
        for (int i = 0; i < 8; i++) {
            codes[i] = String.format("%08d", (int) (Math.random() * 100000000));
        }
        return codes;
    }
    
    private boolean verifyTOTPCode(String secret, String code) {
        // 간단한 Mock 검증 (실제 구현에서는 TOTP 라이브러리 사용)
        // 개발용으로 '123456'은 항상 유효하게 처리
        return "123456".equals(code) || code.startsWith("1");
    }


    // Private helper methods

    private boolean needsApproval(String role) {
        return "ENTERPRISE".equalsIgnoreCase(role) || "PARTNER".equalsIgnoreCase(role);
    }

    private String getSignupMessage(User user) {
        if (user.getStatus() == User.UserStatus.PENDING_APPROVAL) {
            return "회원가입이 완료되었습니다. 관리자 승인 후 이용 가능합니다. (평일 1-2일 소요)";
        }
        return "회원가입이 완료되었습니다. 이메일 인증을 완료해주세요.";
    }

    private void incrementLoginAttempts(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        
        // 5회 실패 시 30분 잠금
        if (user.getLoginAttempts() >= 5) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("User account locked due to multiple failed login attempts: {}", user.getEmail());
        }
        
        userMapper.save(user);
    }

    private void sendVerificationEmail(User user) {
        try {
            String verificationLink = "http://localhost:8080/api/auth/verify-email?token=" + user.getEmailVerificationToken();
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationLink);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    private void sendApprovalEmail(User user) {
        try {
            emailService.sendApprovalEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", user.getEmail(), e);
        }
    }

    private void sendRejectionEmail(User user, String reason) {
        try {
            emailService.sendRejectionEmail(user.getEmail(), user.getName(), reason);
        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", user.getEmail(), e);
        }
    }

    private void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    private AuthResponse.UserInfo convertToUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .memberCode(user.getMemberCode())
            .emailVerified(user.isEmailVerified())
            .twoFactorEnabled(user.isTwoFactorEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }

    public boolean isUsernameAvailable(String username) {
        return !userMapper.existsByUsername(username);
    }

    public void resendEmailVerificationByEmail(String email) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("이미 인증된 이메일입니다");
        }

        user.setEmailVerificationToken(UUID.randomUUID().toString());
        userMapper.save(user);

        sendVerificationEmail(user);
    }

}