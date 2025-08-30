package com.ysc.lms.dto.inbound;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 입고확인 요청 DTO
 */
@Data
public class InboundConfirmationRequest {
    
    @NotBlank(message = "송장번호를 입력해주세요.")
    private String trackingNumber;
    
    @NotBlank(message = "택배사를 선택해주세요.")
    private String carrier;
    
    @NotNull(message = "주문 ID를 입력해주세요.")
    private Long orderId;
    
    // 실제 측정 정보
    @Min(value = 0, message = "실제 중량은 0 이상이어야 합니다.")
    private Double actualWeight;
    
    @Min(value = 0, message = "실제 가로는 0 이상이어야 합니다.")
    private Double actualWidth;
    
    @Min(value = 0, message = "실제 세로는 0 이상이어야 합니다.")
    private Double actualHeight;
    
    @Min(value = 0, message = "실제 높이는 0 이상이어야 합니다.")
    private Double actualDepth;
    
    // 패키지 상태 정보
    @NotBlank(message = "패키지 상태를 선택해주세요.")
    private String packageCondition; // "GOOD" | "DAMAGED" | "WET" | "OPENED"
    
    // 손상 정보 (손상된 경우)
    @Size(max = 1000, message = "손상 설명은 1000자를 초과할 수 없습니다.")
    private String damageDescription;
    
    // 손상 사진 경로들
    private List<String> damagePhotoPaths;
    
    // 창고 위치 정보
    @NotBlank(message = "보관 위치를 입력해주세요.")
    private String storageLocation;
    
    @Size(max = 50, message = "구역 코드는 50자를 초과할 수 없습니다.")
    private String storageZone;
    
    @Size(max = 20, message = "선반 코드는 20자를 초과할 수 없습니다.")
    private String shelfCode;
    
    // 특수 처리 요청
    private boolean requiresInspection;
    private boolean requiresRepacking;
    private boolean requiresPhotos;
    private boolean isFragile;
    private boolean isHazardous;
    
    // 관리자 입력 정보
    @Size(max = 1000, message = "관리자 메모는 1000자를 초과할 수 없습니다.")
    private String adminNotes;
    
    @NotBlank(message = "처리한 관리자 정보가 필요합니다.")
    private String processedBy;
    
    private LocalDateTime processedAt;
    
    // 다음 처리 단계 정보
    private String nextAction; // "INSPECT" | "REPACK" | "SHIP" | "HOLD" | "RETURN"
    private LocalDateTime scheduledProcessAt;
    private String assignedTo;
    
    // 고객 알림 설정
    private boolean notifyCustomer;
    private String notificationMessage;
}