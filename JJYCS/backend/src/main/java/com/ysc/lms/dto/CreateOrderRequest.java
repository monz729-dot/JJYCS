package com.ysc.lms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * 주문 생성 요청 DTO - 태국 전용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    
    // === 기본 주문 정보 ===
    
    /**
     * 배송 유형 (필수: 해상/항공)
     */
    @NotBlank(message = "배송 유형은 필수입니다.")
    @Pattern(regexp = "^(SEA|AIR)$", message = "배송 유형은 SEA 또는 AIR만 가능합니다.")
    @JsonProperty("shippingType")
    private String shippingType = "SEA";
    
    /**
     * 도착 국가 (고정: TH)
     */
    @Pattern(regexp = "^TH$", message = "현재 태국(TH)만 지원됩니다.")
    @JsonProperty("destCountry")
    private String destCountry = "TH";
    
    /**
     * 태국 우편번호 (5자리)
     */
    @Pattern(regexp = "^[0-9]{5}$", message = "태국 우편번호는 5자리 숫자입니다.")
    @JsonProperty("destZip")
    private String destZip;
    
    /**
     * 리패킹 서비스 신청 여부
     */
    @JsonProperty("repacking")
    private Boolean repacking = false;
    
    /**
     * 특별 요청사항
     */
    @Size(max = 1000, message = "특별 요청사항은 1000자 이하로 입력해주세요.")
    @JsonProperty("specialRequests")
    private String specialRequests;
    
    // === 수취인 정보 (다중) ===
    
    /**
     * 수취인 목록 (최소 1명, 최대 5명)
     */
    @Valid
    @NotEmpty(message = "최소 1명의 수취인은 필요합니다.")
    @Size(max = 5, message = "수취인은 최대 5명까지 등록 가능합니다.")
    private List<RecipientDto> recipients = new ArrayList<>();
    
    // === 배대지 접수 정보 ===
    
    /**
     * 접수 방법 (택배/퀵/기타)
     */
    @NotBlank(message = "접수 방법은 필수입니다.")
    @Pattern(regexp = "^(COURIER|QUICK|OTHER)$", message = "접수 방법이 올바르지 않습니다.")
    @JsonProperty("inboundMethod")
    private String inboundMethod = "COURIER";
    
    /**
     * 택배사명 (COURIER 선택 시 필수)
     */
    @JsonProperty("courierCompany")
    private String courierCompany;
    
    /**
     * 택배 송장번호 (COURIER 선택 시 필수)
     */
    @JsonProperty("waybillNo")
    private String waybillNo;
    
    /**
     * 퀵서비스 업체명 (QUICK 선택 시)
     */
    @JsonProperty("quickVendor")
    private String quickVendor;
    
    /**
     * 퀵서비스 연락처 (QUICK 선택 시)
     */
    @Pattern(
        regexp = "^$|^0[0-9]{1,2}-[0-9]{3,4}-[0-9]{4}$",
        message = "한국 전화번호 형식이 올바르지 않습니다."
    )
    @JsonProperty("quickPhone")
    private String quickPhone;
    
    /**
     * YCS 접수지 ID
     */
    @JsonProperty("inboundLocationId")
    private Long inboundLocationId;
    
    /**
     * 접수 관련 요청사항
     */
    @Size(max = 1000, message = "접수 요청사항은 1000자 이하로 입력해주세요.")
    @JsonProperty("inboundNote")
    private String inboundNote;
    
    // === 주문 품목 정보 ===
    
    /**
     * 주문 품목 목록 (필수)
     */
    @Valid
    @NotEmpty(message = "최소 1개의 주문 품목은 필요합니다.")
    private List<OrderItemDto> items = new ArrayList<>();
    
    // === 비즈니스 로직용 플래그 ===
    
    /**
     * CBM 자동 계산 후 29 초과 시 항공 전환 동의
     */
    @JsonProperty("agreeAirConversion")
    private Boolean agreeAirConversion = false;
    
    /**
     * THB 1,500 초과 시 수취인 추가 정보 입력 동의
     */
    @JsonProperty("agreeExtraRecipient")
    private Boolean agreeExtraRecipient = false;
    
    /**
     * 회원코드 미기재(No Code) 지연 처리 동의
     */
    @JsonProperty("agreeDelayProcessing")
    private Boolean agreeDelayProcessing = false;
    
    // === Validation Methods ===
    
    /**
     * 택배 접수 시 필수 정보 검증
     */
    public boolean isValidCourierInfo() {
        if (!"COURIER".equals(inboundMethod)) {
            return true; // 택배가 아니면 검증 통과
        }
        return courierCompany != null && !courierCompany.trim().isEmpty() &&
               waybillNo != null && !waybillNo.trim().isEmpty();
    }
    
    /**
     * 퀵서비스 접수 시 필수 정보 검증
     */
    public boolean isValidQuickInfo() {
        if (!"QUICK".equals(inboundMethod)) {
            return true; // 퀵서비스가 아니면 검증 통과
        }
        return quickVendor != null && !quickVendor.trim().isEmpty();
    }
    
    /**
     * 기본 수취인 확인
     */
    public RecipientDto getPrimaryRecipient() {
        if (recipients == null || recipients.isEmpty()) {
            return null;
        }
        return recipients.stream()
            .filter(r -> r.getIsPrimary() != null && r.getIsPrimary())
            .findFirst()
            .orElse(recipients.get(0)); // 기본 수취인이 없으면 첫 번째
    }
    
    /**
     * 총 품목 개수
     */
    public int getTotalItemCount() {
        return items != null ? items.size() : 0;
    }
    
    /**
     * 총 CBM 계산
     */
    public BigDecimal getTotalCbm() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
            .map(OrderItemDto::getCbm)
            .filter(cbm -> cbm != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * CBM 29 초과 여부
     */
    public boolean isCbmExceedsThreshold() {
        return getTotalCbm().compareTo(new BigDecimal("29.0")) > 0;
    }
    
    /**
     * 리패킹 요청 여부 (별칭 메서드)
     */
    public Boolean getRepackingRequested() {
        return repacking;
    }
    
    /**
     * 주문 요약 정보
     */
    public String getSummary() {
        RecipientDto primary = getPrimaryRecipient();
        return String.format("%s → %s (%d품목, CBM:%.3f)", 
            shippingType,
            primary != null ? primary.getName() : "수취인미정",
            getTotalItemCount(),
            getTotalCbm().doubleValue()
        );
    }
}