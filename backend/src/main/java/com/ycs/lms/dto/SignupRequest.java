package com.ycs.lms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignupRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
             message = "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다")
    private String name;
    
    @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{3,4}-\\d{4}$", 
             message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phone;
    
    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "^(individual|enterprise|partner|warehouse)$",
             message = "올바른 역할이 아닙니다")
    private String role;
    
    @Size(max = 50, message = "회원코드는 50자 이하여야 합니다")
    private String memberCode;
    
    @NotNull(message = "이용약관 동의는 필수입니다")
    @AssertTrue(message = "이용약관에 동의해야 합니다")
    private Boolean agreeTerms;
    
    @NotNull(message = "개인정보처리방침 동의는 필수입니다")
    @AssertTrue(message = "개인정보처리방침에 동의해야 합니다")
    private Boolean agreePrivacy;
    
    private Boolean agreeMarketing = false;
    
    @Valid
    private AdditionalInfo additionalInfo;
    
    // Convenience methods
    public String getMemberCode() {
        return memberCode;
    }
    
    public boolean isAgreeTerms() {
        return agreeTerms != null && agreeTerms;
    }
    
    public boolean isAgreePrivacy() {
        return agreePrivacy != null && agreePrivacy;
    }
    
    public boolean isAgreeMarketing() {
        return agreeMarketing != null && agreeMarketing;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalInfo {
        // Enterprise fields
        private String companyName;
        private String businessNumber;
        private String companyAddress;
        private String ceoName;
        private String businessType;
        private Integer employeeCount;
        
        // Partner fields
        private String partnerType; // referral, campaign
        private String bankName;
        private String accountNumber;
        private String accountHolder;
        
        // Warehouse fields
        private String warehouseName;
        private String warehouseAddress;
        private String capacityDescription;
        private String operatingHours;
        private String contactPerson;
        private String contactPhone;
    }
}