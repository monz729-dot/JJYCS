package com.ycs.lms.dto;

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
 * YCS 접수지 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundLocationDto {
    
    private Long id;
    
    /**
     * 접수지명 (필수)
     */
    @NotBlank(message = "접수지명은 필수입니다.")
    @Size(max = 100, message = "접수지명은 100자 이하로 입력해주세요.")
    private String name;
    
    /**
     * 접수지 주소 (필수)
     */
    @NotBlank(message = "접수지 주소는 필수입니다.")
    @Size(max = 500, message = "주소는 500자 이하로 입력해주세요.")
    private String address;
    
    /**
     * 우편번호 (한국 5자리)
     */
    @Pattern(
        regexp = "^[0-9]{5}$",
        message = "우편번호는 5자리 숫자입니다."
    )
    @JsonProperty("postalCode")
    private String postalCode;
    
    /**
     * 연락처
     */
    @Pattern(
        regexp = "^0[0-9]{1,2}-[0-9]{3,4}-[0-9]{4}$",
        message = "한국 전화번호 형식이 올바르지 않습니다. (예: 02-1234-5678)"
    )
    private String phone;
    
    /**
     * 담당자명
     */
    @Size(max = 100, message = "담당자명은 100자 이하로 입력해주세요.")
    @JsonProperty("contactPerson")
    private String contactPerson;
    
    /**
     * 운영시간
     */
    @Size(max = 100, message = "운영시간은 100자 이하로 입력해주세요.")
    @JsonProperty("businessHours")
    private String businessHours;
    
    /**
     * 특별 안내사항
     */
    @Size(max = 1000, message = "특별 안내사항은 1000자 이하로 입력해주세요.")
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    /**
     * 활성 상태
     */
    @JsonProperty("isActive")
    private Boolean isActive = true;
    
    /**
     * 표시 순서
     */
    @JsonProperty("displayOrder")
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
    public InboundLocationDto(String name, String address) {
        this.name = name;
        this.address = address;
        this.isActive = true;
        this.displayOrder = 0;
    }
    
    /**
     * 한국 우편번호 검증
     */
    public boolean isValidKoreanPostalCode() {
        return postalCode != null && postalCode.matches("^[0-9]{5}$");
    }
    
    /**
     * 한국 전화번호 검증
     */
    public boolean isValidKoreanPhone() {
        return phone != null && phone.matches("^0[0-9]{1,2}-[0-9]{3,4}-[0-9]{4}$");
    }
    
    /**
     * 필수 필드 검증
     */
    public boolean hasRequiredFields() {
        return name != null && !name.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }
    
    /**
     * 접수지 정보 요약 (선택 UI용)
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (contactPerson != null && !contactPerson.trim().isEmpty()) {
            sb.append(" (").append(contactPerson).append(")");
        }
        if (phone != null && !phone.trim().isEmpty()) {
            sb.append(" ").append(phone);
        }
        return sb.toString();
    }
    
    /**
     * 운영 상태 확인
     */
    public boolean isOperational() {
        return isActive != null && isActive;
    }
}