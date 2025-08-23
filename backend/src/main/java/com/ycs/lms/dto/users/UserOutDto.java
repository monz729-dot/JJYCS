package com.ycs.lms.dto.users;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 사용자 출력 DTO (API 응답/조회용)
 * <p>민감 정보(비밀번호 해시, 토큰, 2FA 시크릿 등)는 제외했습니다.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOutDto {

    /** 회원 ID */
    private Long id; // ID (BIGINT)

    /** 이메일 */
    private String email; // EMAIL (CHARACTER VARYING(255))

    /** 이름 */
    private String name; // NAME (CHARACTER VARYING(100))

    /** 휴대폰 번호 */
    private String phone; // PHONE (CHARACTER VARYING(20))

    /** 역할 */
    private String role; // ROLE (CHARACTER VARYING(20))

    /** 상태 */
    private String status; // STATUS (CHARACTER VARYING(20))

    /** 회원 코드 */
    private String memberCode; // MEMBER_CODE (CHARACTER VARYING(20))

    /** 이메일 인증 여부 */
    private Boolean emailVerified; // EMAIL_VERIFIED (BOOLEAN)

    /** 이용약관 동의 */
    private Boolean agreeTerms; // AGREE_TERMS (BOOLEAN)

    /** 개인정보처리방침 동의 */
    private Boolean agreePrivacy; // AGREE_PRIVACY (BOOLEAN)

    /** 마케팅 수신 동의 */
    private Boolean agreeMarketing; // AGREE_MARKETING (BOOLEAN)

    /** 생성 일시 */
    private LocalDateTime createdAt; // CREATED_AT (TIMESTAMP)

    /** 수정 일시 */
    private LocalDateTime updatedAt; // UPDATED_AT (TIMESTAMP)

    /** 마지막 로그인 일시 */
    private LocalDateTime lastLoginAt; // LAST_LOGIN_AT (TIMESTAMP)

    /** 사용자명 */
    private String username; // USERNAME (CHARACTER VARYING(50))

    /** 2단계 인증 사용 여부 */
    private Boolean twoFactorEnabled; // TWO_FACTOR_ENABLED (BOOLEAN)

    /** 로그인 시도 횟수 */
    private Integer loginAttempts; // LOGIN_ATTEMPTS (INTEGER)

    /** 잠금 만료 일시 */
    private LocalDateTime lockedUntil; // LOCKED_UNTIL (TIMESTAMP)
}
