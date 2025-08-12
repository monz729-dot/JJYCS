package com.ycs.lms.controller;

import com.ycs.lms.dto.AuthResponse;
import com.ycs.lms.dto.LoginRequest;
import com.ycs.lms.dto.SignupRequest;
import com.ycs.lms.service.UserService;
import com.ycs.lms.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "역할별 회원가입 처리. 기업/파트너는 승인 대기 상태")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("User signup attempt: email={}, role={}", request.getEmail(), request.getRole());
        
        AuthResponse response = userService.signup(request);
        
        String message = "회원가입이 완료되었습니다.";
        if ("pending_approval".equals(response.getUser().getStatus())) {
            message += " 승인은 평일 1-2일이 소요됩니다.";
        } else {
            message += " 이메일 인증을 진행해주세요.";
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, message));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호 로그인. 2FA 지원")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: email={}", request.getEmail());
        
        AuthResponse response = userService.login(request);
        
        if (response.isRequiresTwoFactor()) {
            return ResponseEntity.ok(ApiResponse.success(response, "2단계 인증이 필요합니다."));
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 갱신")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        // TODO: 리프레시 토큰 검증 및 새 액세스 토큰 발급
        
        Map<String, String> tokens = Map.of(
            "accessToken", "new-access-token",
            "refreshToken", refreshToken
        );
        
        return ResponseEntity.ok(ApiResponse.success(tokens, "토큰이 갱신되었습니다."));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰으로 계정 활성화")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt: token={}", token.substring(0, 8) + "...");
        
        userService.verifyEmail(token);
        
        return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 재설정 이메일 발송")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Password reset request: email={}", email);
        
        // TODO: 비밀번호 재설정 이메일 발송
        
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호 재설정 이메일을 발송했습니다."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "재설정 토큰으로 비밀번호 변경")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Password reset attempt: token={}", token.substring(0, 8) + "...");
        
        // TODO: 비밀번호 재설정 처리
        
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 재설정되었습니다."));
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "2FA 설정", description = "2단계 인증 설정")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setup2FA() {
        // TODO: 2FA QR 코드 및 백업 코드 생성
        
        Map<String, Object> setup = Map.of(
            "qrCodeUrl", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
            "secret", "JBSWY3DPEHPK3PXP",
            "backupCodes", new String[]{"12345678", "87654321", "13579246"}
        );
        
        return ResponseEntity.ok(ApiResponse.success(setup, "2FA 설정 정보가 생성되었습니다."));
    }

    @PostMapping("/approve/{userId}")
    @Operation(summary = "사용자 승인", description = "관리자용 사용자 승인 처리")
    public ResponseEntity<ApiResponse<Void>> approveUser(
            @PathVariable Long userId,
            @RequestParam String memberCode,
            @RequestParam(required = false) String note) {
        log.info("User approval: userId={}, memberCode={}", userId, memberCode);
        
        userService.approveUser(userId, memberCode, note);
        
        return ResponseEntity.ok(ApiResponse.success(null, "사용자 승인이 완료되었습니다."));
    }
}