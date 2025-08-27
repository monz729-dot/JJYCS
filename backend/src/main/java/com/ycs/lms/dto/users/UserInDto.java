// File: src/main/java/com/example/user/dto/UserInDto.java
package com.ycs.lms.dto.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 사용자 입력 DTO (회원 생성/수정 요청에 사용)
 * <p>컬럼명에서 한글 주석을 유추하여 기입했습니다.</p>
 * <p>비밀번호는 평문으로 입력받아 서버에서 해시되어 PASSWORD_HASH 컬럼에 저장됩니다.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInDto {

    /** 이메일 */
    private String email; // EMAIL (CHARACTER VARYING(255))

    /** 비밀번호(평문 입력, 서버 해시 처리) */
    private String password; // PASSWORD_HASH (CHARACTER VARYING(255))에 해시 저장

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

    /** 이용약관 동의 */
    private Boolean agreeTerms; // AGREE_TERMS (BOOLEAN)

    /** 개인정보처리방침 동의 */
    private Boolean agreePrivacy; // AGREE_PRIVACY (BOOLEAN)

    /** 마케팅 수신 동의 */
    private Boolean agreeMarketing; // AGREE_MARKETING (BOOLEAN)

    /** 사용자명 */
    private String username; // USERNAME (CHARACTER VARYING(50))

    /** 2단계 인증 사용 여부 */
    private Boolean twoFactorEnabled; // TWO_FACTOR_ENABLED (BOOLEAN)
}


