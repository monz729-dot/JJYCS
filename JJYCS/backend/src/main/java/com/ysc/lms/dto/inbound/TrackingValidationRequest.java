package com.ysc.lms.dto.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 송장번호 유효성 검사 요청 DTO
 */
@Data
public class TrackingValidationRequest {
    
    @NotBlank(message = "송장번호를 입력해주세요.")
    @Size(max = 50, message = "송장번호는 50자를 초과할 수 없습니다.")
    private String trackingNumber;
    
    @NotBlank(message = "택배사를 선택해주세요.")
    @Size(max = 20, message = "택배사 코드는 20자를 초과할 수 없습니다.")
    private String carrier;
    
    // 선택적 필드 - 스캔 방식으로 입력된 경우
    private String scanMethod; // "manual" | "camera" | "barcode"
    
    // 스캔 시간 (스캔으로 입력된 경우)
    private String scannedAt;
    
    // 스캔한 사용자 (관리자)
    private String scannedBy;
}