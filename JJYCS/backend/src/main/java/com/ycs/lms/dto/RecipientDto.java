package com.ycs.lms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 수취인 정보 DTO - 태국 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientDto {
    
    /**
     * 수취인 순서 (1부터 시작)
     */
    @JsonProperty("order")
    private Integer recipientOrder = 1;
    
    /**
     * 수취인명 (필수)
     */
    @NotBlank(message = "수취인명은 필수입니다.")
    @Size(max = 100, message = "수취인명은 100자 이하로 입력해주세요.")
    private String name;
    
    /**
     * 수취인 전화번호 (필수)
     * 태국 전화번호 형식: +66-XXXXXXXXX 또는 0X-XXXX-XXXX
     */
    @NotBlank(message = "수취인 전화번호는 필수입니다.")
    @Pattern(
        regexp = "^(\\+66|0)[0-9]{8,9}$|^0[0-9]-[0-9]{4}-[0-9]{4}$",
        message = "태국 전화번호 형식이 올바르지 않습니다. (예: +66812345678, 02-1234-5678)"
    )
    @Size(max = 20, message = "전화번호는 20자 이하로 입력해주세요.")
    private String phone;
    
    /**
     * 수취인 주소 (필수)
     */
    @NotBlank(message = "수취인 주소는 필수입니다.")
    @Size(max = 500, message = "주소는 500자 이하로 입력해주세요.")
    private String address;
    
    /**
     * 태국 우편번호 (5자리 숫자)
     */
    @Pattern(
        regexp = "^[0-9]{5}$",
        message = "태국 우편번호는 5자리 숫자입니다. (예: 10110)"
    )
    @JsonProperty("postalCode")
    private String postalCode;
    
    /**
     * 기본 수취인 여부 (첫 번째 수취인이 기본)
     */
    @JsonProperty("isPrimary")
    private Boolean isPrimary = false;
    
    /**
     * 추가 요청사항
     */
    @Size(max = 500, message = "추가 요청사항은 500자 이하로 입력해주세요.")
    private String notes;
    
    /**
     * 생성자 - 필수 필드만
     */
    public RecipientDto(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.recipientOrder = 1;
        this.isPrimary = true;
    }
    
    /**
     * 태국 우편번호 검증
     */
    public boolean isValidThailandPostalCode() {
        return postalCode != null && postalCode.matches("^[0-9]{5}$");
    }
    
    /**
     * 태국 전화번호 검증
     */
    public boolean isValidThailandPhone() {
        return phone != null && (
            phone.matches("^(\\+66|0)[0-9]{8,9}$") ||
            phone.matches("^0[0-9]-[0-9]{4}-[0-9]{4}$")
        );
    }
    
    /**
     * 필수 필드 검증
     */
    public boolean hasRequiredFields() {
        return name != null && !name.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }
    
    /**
     * 수취인 정보 요약 (로그용)
     */
    public String getSummary() {
        return String.format("수취인 #%d: %s (%s)", 
            recipientOrder != null ? recipientOrder : 1, 
            name != null ? name : "미입력", 
            postalCode != null ? postalCode : "우편번호 미입력");
    }
}