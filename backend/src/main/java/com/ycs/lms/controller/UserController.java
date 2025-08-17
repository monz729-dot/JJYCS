package com.ycs.lms.controller;

import com.ycs.lms.dto.UserProfileResponse;
import com.ycs.lms.dto.UpdateProfileRequest;
import com.ycs.lms.service.UserService;
import com.ycs.lms.util.ApiResponse;
import com.ycs.lms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 프로필 관리 컨트롤러
 * 프론트엔드에서 요구하는 /api/users/me 엔드포인트 구현
 */
@Tag(name = "Users", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 현재 사용자 프로필 조회
     * 프론트엔드 authApiService.getCurrentUserProfile()에서 호출
     */
    @Operation(summary = "현재 사용자 프로필 조회", description = "JWT 토큰으로 현재 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting current user profile for user: {}", userPrincipal.getId());
        
        try {
            UserProfileResponse profile = userService.getUserProfile(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(profile));
            
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("프로필 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 프로필 업데이트
     * 프론트엔드 authApiService.updateProfile()에서 호출
     */
    @Operation(summary = "사용자 프로필 업데이트", description = "사용자의 프로필 정보를 업데이트합니다.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Updating profile for user: {}", userPrincipal.getId());
        
        try {
            UserProfileResponse updatedProfile = userService.updateProfile(userPrincipal.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(updatedProfile, "프로필이 성공적으로 업데이트되었습니다."));
            
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("프로필 업데이트 중 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 변경
     */
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.")
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Changing password for user: {}", userPrincipal.getId());
        
        try {
            userService.changePassword(userPrincipal.getId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 성공적으로 변경되었습니다."));
            
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("현재 비밀번호가 올바르지 않습니다."));
                    
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다."));
        }
    }

    /**
     * 이메일 인증 재발송
     */
    @Operation(summary = "이메일 인증 재발송", description = "이메일 인증 메일을 다시 발송합니다.")
    @PostMapping("/me/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Resending email verification for user: {}", userPrincipal.getId());
        
        try {
            userService.resendEmailVerification(userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "인증 이메일을 재발송했습니다."));
            
        } catch (Exception e) {
            log.error("Error resending email verification", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("이메일 재발송 중 오류가 발생했습니다."));
        }
    }
}

// DTO 클래스들
class UserProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String status;
    private String memberCode;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    private String createdAt;
    private String updatedAt;
    
    // getters, setters, constructors
}

class UpdateProfileRequest {
    private String name;
    private String phone;
    
    // getters, setters, validation
}

class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    
    // getters, setters
}

class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}