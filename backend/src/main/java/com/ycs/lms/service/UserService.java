package com.ycs.lms.service;

import com.ycs.lms.dto.AuthResponse;
import com.ycs.lms.dto.LoginRequest;
import com.ycs.lms.dto.SignupRequest;
import com.ycs.lms.entity.EnterpriseProfile;
import com.ycs.lms.entity.PartnerProfile;
import com.ycs.lms.entity.User;
import com.ycs.lms.entity.WarehouseProfile;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.mapper.UserMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;
        
        // identifier가 숫자면 ID로, 아니면 이메일로 조회
        try {
            Long userId = Long.parseLong(identifier);
            user = userMapper.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + identifier));
        } catch (NumberFormatException e) {
            user = userMapper.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + identifier));
        }
        
        return UserPrincipal.create(user);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("이미 등록된 이메일입니다.");
        }
        
        // 사용자 생성
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .phone(request.getPhone())
            .role(request.getRole())
            .status(requiresApproval(request.getRole()) ? "pending_approval" : "active")
            .emailVerified(false)
            .emailVerificationToken(UUID.randomUUID().toString())
            .twoFactorEnabled(false)
            .agreeTerms(request.getAgreeTerms())
            .agreePrivacy(request.getAgreePrivacy())
            .agreeMarketing(request.getAgreeMarketing() != null ? request.getAgreeMarketing() : false)
            .build();
        
        userMapper.insertUser(user);
        
        // 역할별 추가 프로필 생성
        createAdditionalProfile(user, request.getAdditionalInfo());
        
        // 이메일 인증 발송
        emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
        
        // 응답 생성
        String approvalMessage = requiresApproval(request.getRole()) ? 
            getApprovalMessage(request.getRole()) : null;
            
        return AuthResponse.builder()
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .memberCode(user.getMemberCode())
                .emailVerified(user.isEmailVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .build())
            .build();
    }

    public AuthResponse login(LoginRequest request) {
        // ReCAPTCHA 검증 (실제 구현에서는 외부 서비스 호출)
        // validateRecaptcha(request.getRecaptchaToken());
        
        User user = userMapper.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("이메일 또는 비밀번호가 올바르지 않습니다."));
        
        // 계정 잠금 체크
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("계정이 잠겨있습니다. 잠시 후 다시 시도해주세요.");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 2FA 체크
            if (user.isTwoFactorEnabled() && request.getTwoFactorCode() == null) {
                return AuthResponse.builder()
                    .requiresTwoFactor(true)
                    .build();
            }
            
            // 2FA 코드 검증
            if (user.isTwoFactorEnabled() && request.getTwoFactorCode() != null) {
                // validateTwoFactorCode(user, request.getTwoFactorCode());
            }
            
            // JWT 토큰 생성
            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getId());
            
            // 로그인 성공 처리
            userMapper.updateLastLogin(user.getId());
            
            return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .expiresIn(3600000L) // 1시간
                .user(AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .memberCode(user.getMemberCode())
                    .emailVerified(user.isEmailVerified())
                    .twoFactorEnabled(user.isTwoFactorEnabled())
                    .build())
                .build();
                
        } catch (Exception e) {
            // 로그인 실패 처리
            userMapper.incrementLoginAttempts(user.getId());
            throw new BadRequestException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        // 실제 구현에서는 토큰으로 사용자 조회
        // User user = userMapper.findByEmailVerificationToken(token)...
        // userMapper.verifyEmail(user.getId(), true);
    }

    @Transactional
    public void approveUser(Long userId, String memberCode, String note) {
        User user = userMapper.findById(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        
        if (!"pending_approval".equals(user.getStatus())) {
            throw new BadRequestException("승인 대기 상태가 아닙니다.");
        }
        
        userMapper.approveUser(userId, memberCode);
        
        // 승인 완료 이메일 발송
        emailService.sendApprovalEmail(user.getEmail(), memberCode, note);
    }

    private boolean requiresApproval(String role) {
        return "enterprise".equals(role) || "partner".equals(role) || "warehouse".equals(role);
    }

    private String getApprovalMessage(String role) {
        return switch (role) {
            case "enterprise" -> "기업 회원 승인은 평일 1-2일이 소요됩니다.";
            case "partner" -> "파트너 회원 승인은 평일 1-2일이 소요됩니다.";
            case "warehouse" -> "창고 회원 승인은 평일 1-2일이 소요됩니다.";
            default -> null;
        };
    }

    private void createAdditionalProfile(User user, SignupRequest.AdditionalInfo info) {
        if (info == null) return;
        
        switch (user.getRole()) {
            case "enterprise" -> {
                EnterpriseProfile profile = EnterpriseProfile.builder()
                    .userId(user.getId())
                    .companyName(info.getCompanyName())
                    .businessNumber(info.getBusinessNumber())
                    .companyAddress(info.getCompanyAddress())
                    .ceoName(info.getCeoName())
                    .businessType(info.getBusinessType())
                    .employeeCount(info.getEmployeeCount())
                    .build();
                userMapper.insertEnterpriseProfile(profile);
            }
            case "partner" -> {
                PartnerProfile profile = PartnerProfile.builder()
                    .userId(user.getId())
                    .partnerType(info.getPartnerType())
                    .referralCode(generateReferralCode())
                    .bankName(info.getBankName())
                    .accountNumber(info.getAccountNumber())
                    .accountHolder(info.getAccountHolder())
                    .commissionRate(BigDecimal.valueOf(0.05)) // 5%
                    .totalReferrals(0)
                    .totalCommission(BigDecimal.ZERO)
                    .pendingCommission(BigDecimal.ZERO)
                    .build();
                userMapper.insertPartnerProfile(profile);
            }
            case "warehouse" -> {
                WarehouseProfile profile = WarehouseProfile.builder()
                    .userId(user.getId())
                    .warehouseName(info.getWarehouseName())
                    .warehouseAddress(info.getWarehouseAddress())
                    .capacityDescription(info.getCapacityDescription())
                    .operatingHours(info.getOperatingHours())
                    .contactPerson(info.getContactPerson())
                    .contactPhone(info.getContactPhone())
                    .build();
                userMapper.insertWarehouseProfile(profile);
            }
        }
    }

    private String generateReferralCode() {
        return "PART" + String.format("%05d", (int) (Math.random() * 100000));
    }
}