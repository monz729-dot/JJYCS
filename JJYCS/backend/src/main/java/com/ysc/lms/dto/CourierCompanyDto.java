package com.ysc.lms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 택배사 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierCompanyDto {
    
    private Long id;
    
    /**
     * 택배사 코드 (필수, 고유)
     */
    @NotBlank(message = "택배사 코드는 필수입니다.")
    @Size(min = 2, max = 20, message = "택배사 코드는 2-20자 사이여야 합니다.")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "택배사 코드는 영문 대문자, 숫자, 밑줄(_)만 사용 가능합니다.")
    private String code;
    
    /**
     * 택배사명 (필수)
     */
    @NotBlank(message = "택배사명은 필수입니다.")
    @Size(max = 100, message = "택배사명은 100자 이하로 입력해주세요.")
    private String name;
    
    /**
     * 택배사명 (영문)
     */
    @Size(max = 100, message = "영문 택배사명은 100자 이하로 입력해주세요.")
    @JsonProperty("nameEn")
    private String nameEn;
    
    /**
     * 웹사이트 URL
     */
    @Pattern(
        regexp = "^$|^https?://[\\w\\-._~:/?#[\\]@!$&'()*+,;=%]+$",
        message = "올바른 웹사이트 URL을 입력해주세요."
    )
    @Size(max = 200, message = "웹사이트 URL은 200자 이하로 입력해주세요.")
    private String website;
    
    /**
     * 송장 추적 URL 템플릿
     * {trackingNumber} 플레이스홀더 사용
     */
    @Size(max = 500, message = "추적 URL 템플릿은 500자 이하로 입력해주세요.")
    @JsonProperty("trackingUrlTemplate")
    private String trackingUrlTemplate;
    
    /**
     * 활성 상태
     */
    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * 표시 순서
     */
    @JsonProperty("displayOrder")
    @Builder.Default
    private Integer displayOrder = 0;
    
    /**
     * 생성일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    /**
     * 수정일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    /**
     * 생성자 - 필수 필드만
     */
    public CourierCompanyDto(String code, String name) {
        this.code = code;
        this.name = name;
        this.isActive = true;
        this.displayOrder = 0;
    }
    
    /**
     * 택배사 코드 검증
     */
    public boolean isValidCode() {
        return code != null && code.matches("^[A-Z0-9_]+$") && code.length() >= 2 && code.length() <= 20;
    }
    
    /**
     * 웹사이트 URL 검증
     */
    public boolean isValidWebsite() {
        return website == null || website.trim().isEmpty() || 
               website.matches("^https?://[\\w\\-._~:/?#[\\]@!$&'()*+,;=%]+$");
    }
    
    /**
     * 추적 URL 템플릿 유효성 검사
     */
    public boolean hasTrackingUrlTemplate() {
        return trackingUrlTemplate != null && 
               !trackingUrlTemplate.trim().isEmpty() && 
               trackingUrlTemplate.contains("{trackingNumber}");
    }
    
    /**
     * 송장번호로 추적 URL 생성
     */
    public String generateTrackingUrl(String trackingNumber) {
        if (!hasTrackingUrlTemplate() || trackingNumber == null || trackingNumber.trim().isEmpty()) {
            return null;
        }
        return trackingUrlTemplate.replace("{trackingNumber}", trackingNumber.trim());
    }
    
    /**
     * 필수 필드 검증
     */
    public boolean hasRequiredFields() {
        return code != null && !code.trim().isEmpty() &&
               name != null && !name.trim().isEmpty();
    }
    
    /**
     * 표시용 이름 (한/영 조합)
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (nameEn != null && !nameEn.trim().isEmpty() && !nameEn.equals(name)) {
            sb.append(" (").append(nameEn).append(")");
        }
        return sb.toString();
    }
    
    /**
     * 운영 상태 확인
     */
    public boolean isOperational() {
        return isActive != null && isActive;
    }
    
    /**
     * 택배사 정보 요약
     */
    public String getSummary() {
        return String.format("%s [%s] - %s", 
            name, 
            code, 
            isOperational() ? "운영중" : "중단");
    }
    
    /**
     * 추적 기능 지원 여부
     */
    public boolean supportsTracking() {
        return hasTrackingUrlTemplate();
    }
}