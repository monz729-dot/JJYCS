package com.ycs.lms.service;

import com.ycs.lms.dto.AuthResponse;
import com.ycs.lms.dto.LoginRequest;
import com.ycs.lms.dto.SignupRequest;
import com.ycs.lms.entity.User;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.repository.UserRepository;
import com.ycs.lms.security.JwtTokenProvider;
import com.ycs.lms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    /**
     * 회원가입
     */
    public AuthResponse signup(SignupRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 회원 코드 중복 검사 (있는 경우)
        if (request.getMemberCode() != null && userRepository.existsByMemberCode(request.getMemberCode())) {
            throw new BadRequestException("이미 사용 중인 회원 코드입니다: " + request.getMemberCode());
        }

        // 사용자 생성
        User user = User.builder()
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

        user = userRepository.save(user);

        // 이메일 인증 발송
        sendVerificationEmail(user);

        // JWT 토큰 생성 (이메일 인증 전이라도 로그인 가능)
        String token = tokenProvider.generateToken(user.getEmail());

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

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("잘못된 이메일 또는 비밀번호입니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
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
        userRepository.save(user);

        // JWT 토큰 생성
        String token = tokenProvider.generateToken(user.getEmail());

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

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 사용자 정보 조회 (ID로)
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
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
            if (userRepository.existsByMemberCode(updateRequest.getMemberCode())) {
                throw new BadRequestException("이미 사용 중인 회원 코드입니다");
            }
            user.setMemberCode(updateRequest.getMemberCode());
        }

        return userRepository.save(user);
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
        user = userRepository.save(user);

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
        user = userRepository.save(user);

        // 거부 이메일 발송
        sendRejectionEmail(user, reason);

        return user;
    }

    /**
     * 승인 대기 중인 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {
        return userRepository.findByStatus(User.UserStatus.PENDING_APPROVAL);
    }

    /**
     * 이메일 인증 토큰 검증
     */
    public User verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BadRequestException("잘못된 인증 토큰입니다"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);

        return userRepository.save(user);
    }

    /**
     * 비밀번호 재설정 요청
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

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
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new BadRequestException("잘못된 재설정 토큰입니다"));

        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("재설정 토큰이 만료되었습니다");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        return userRepository.save(user);
    }

    /**
     * Spring Security UserDetailsService 구현
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
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
        
        userRepository.save(user);
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
}