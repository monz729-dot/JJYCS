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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "역할별 회원가입 처리. 기업/파트너는 승인 대기 상태")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("User signup attempt: email={}, role={}", request.getEmail(), request.getRole());
        
        try {
            AuthResponse response = userService.signup(request);
            
            String message = "회원가입이 완료되었습니다.";
            if ("pending_approval".equals(response.getUser().getStatus())) {
                message += " 승인은 평일 1-2일이 소요됩니다.";
            } else {
                message += " 이메일 인증을 진행해주세요.";
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, message));
        } catch (Exception e) {
            log.error("Signup failed", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호 로그인. 2FA 지원")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: email={}", request.getEmail());
        
        try {
            AuthResponse response = userService.login(request);
            
            if (response.isRequiresTwoFactor()) {
                return ResponseEntity.ok(ApiResponse.success(response, "2단계 인증이 필요합니다."));
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 갱신")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("리프레시 토큰이 필요합니다."));
        }
        
        try {
            AuthResponse response = userService.refreshToken(refreshToken);
            
            Map<String, String> tokens = Map.of(
                "accessToken", response.getAccessToken(),
                "refreshToken", response.getRefreshToken() != null ? response.getRefreshToken() : refreshToken,
                "expiresIn", String.valueOf(response.getExpiresIn())
            );
            
            return ResponseEntity.ok(ApiResponse.success(tokens, "토큰이 갱신되었습니다."));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("토큰 갱신에 실패했습니다. 다시 로그인해주세요."));
        }
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "이메일 인증 토큰으로 계정 활성화")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt: token={}", token.substring(0, 8) + "...");
        
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다."));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 재설정 이메일 발송")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        log.info("Password reset request: email={}", email);
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("이메일이 필요합니다."));
        }
        
        try {
            userService.requestPasswordReset(email);
            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호 재설정 이메일을 발송했습니다."));
        } catch (Exception e) {
            log.error("Password reset request failed", e);
            // 보안상 항상 성공 메시지 반환
            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호 재설정 이메일을 발송했습니다."));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "재설정 토큰으로 비밀번호 변경")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("Password reset attempt: token={}", token.substring(0, 8) + "...");
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("재설정 토큰이 필요합니다."));
        }
        
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("비밀번호는 최소 8자 이상이어야 합니다."));
        }
        
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 재설정되었습니다."));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "2FA 설정", description = "2단계 인증 설정")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setup2FA() {
        try {
            Map<String, Object> setup = userService.setup2FA();
            return ResponseEntity.ok(ApiResponse.success(setup, "2FA 설정 정보가 생성되었습니다."));
        } catch (Exception e) {
            log.error("2FA setup failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("2FA 설정에 실패했습니다."));
        }
    }

    @PostMapping("/approve/{userId}")
    @Operation(summary = "사용자 승인", description = "관리자용 사용자 승인 처리")
    public ResponseEntity<ApiResponse<Void>> approveUser(
            @PathVariable Long userId,
            @RequestParam String memberCode,
            @RequestParam(required = false) String note) {
        log.info("User approval: userId={}, memberCode={}", userId, memberCode);
        
        try {
            userService.approveUser(userId, memberCode, note);
            return ResponseEntity.ok(ApiResponse.success(null, "사용자 승인이 완료되었습니다."));
        } catch (Exception e) {
            log.error("User approval failed", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-username")
    @Operation(summary = "아이디 중복확인", description = "username(아이디) 사용 가능 여부 확인")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkUsername(@RequestParam String username) {
        boolean available = false;
        try {
            if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("아이디는 영문/숫자/밑줄 3~20자"));
            }
            available = userService.isUsernameAvailable(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("available", available)));
    }

    @PostMapping(value = "/verify-email", consumes = "application/json")
    @Operation(summary = "이메일 인증(JSON)", description = "본문 { token } 또는 { email, token }")
    public ResponseEntity<ApiResponse<Void>> verifyEmailJson(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("인증 토큰이 필요합니다."));
        }
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다."));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/verify-email/resend", consumes = "application/json")
    @Operation(summary = "이메일 인증 재발송", description = "본문 { email } 로 인증 메일 재발송")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("이메일이 필요합니다."));
        }
        try {
            userService.resendEmailVerificationByEmail(email);
            return ResponseEntity.ok(ApiResponse.success(null, "인증 이메일을 재발송했습니다."));
        } catch (Exception e) {
            log.error("Resend verification failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/complete")
    @Operation(summary = "가입 완료 훅", description = "프론트 호환용 마무리 엔드포인트(서버 작업 없으면 No-Op)")
    public ResponseEntity<ApiResponse<Void>> completeSignup(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(null, "가입 완료"));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "클라이언트 토큰 폐기용 엔드포인트")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 완료"));
    }
}