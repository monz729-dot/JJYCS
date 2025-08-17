package com.ycs.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 아이디: 영문 시작, 5~20자, 영문/숫자/밑줄
    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{4,19}$", message = "아이디는 영문으로 시작하고, 5~20자의 영문/숫자/밑줄만 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String phone;

    @NotBlank(message = "역할은 필수입니다.")
    private String role;

    private String memberCode;
    private String companyName;
    private String businessNumber;
    private String businessAddress;

    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeMarketing;

    // 명시적 게터 (일부 환경 호환성)
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getMemberCode() { return memberCode; }
    public boolean isAgreeTerms() { return agreeTerms; }
    public boolean isAgreePrivacy() { return agreePrivacy; }
    public boolean isAgreeMarketing() { return agreeMarketing; }
}
