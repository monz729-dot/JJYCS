package com.ycs.lms.service;

import com.ycs.lms.util.CBMCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessRuleService {

    @Value("${app.business-rules.cbm-threshold:29.0}")
    private BigDecimal cbmThreshold;

    @Value("${app.business-rules.thb-amount-threshold:1500.0}")
    private BigDecimal thbAmountThreshold;

    @Value("${app.business-rules.auto-air-conversion:true}")
    private boolean autoAirConversion;

    @Value("${app.business-rules.require-member-code:true}")
    private boolean requireMemberCode;
    
    private final CBMCalculator cbmCalculator;

    /**
     * CBM 계산: CBMCalculator 위임 (중복 제거)
     */
    public BigDecimal calculateCBM(BigDecimal widthCm, BigDecimal heightCm, BigDecimal depthCm) {
        if (widthCm == null || heightCm == null || depthCm == null) {
            return BigDecimal.ZERO;
        }
        
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(widthCm, heightCm, depthCm);
        return cbmCalculator.calculateCBM(dimensions).cbm;
    }

    /**
     * 총 CBM이 29m³를 초과하는지 체크
     */
    public boolean shouldConvertToAir(BigDecimal totalCbm) {
        if (!autoAirConversion || totalCbm == null) {
            return false;
        }
        return totalCbm.compareTo(cbmThreshold) > 0;
    }

    /**
     * THB 금액이 1,500을 초과하는지 체크
     */
    public boolean requiresExtraRecipientInfo(BigDecimal totalAmount, String currency) {
        if (!"THB".equals(currency) || totalAmount == null) {
            return false;
        }
        return totalAmount.compareTo(thbAmountThreshold) > 0;
    }

    /**
     * 회원코드 미기재로 인한 지연 처리 여부
     */
    public boolean hasDelayedProcessing(String memberCode) {
        if (!requireMemberCode) {
            return false;
        }
        return memberCode == null || memberCode.trim().isEmpty();
    }

    /**
     * 주문 상태 결정
     */
    public String determineOrderStatus(String memberCode, boolean isApprovedUser) {
        if (hasDelayedProcessing(memberCode)) {
            return "delayed";
        }
        if (!isApprovedUser) {
            return "pending_approval";
        }
        return "requested";
    }

    /**
     * 배송 방식 결정
     */
    public String determineShippingType(BigDecimal totalCbm, String preferredType) {
        if (shouldConvertToAir(totalCbm)) {
            log.info("Converting order to air shipping due to CBM: {} > {}", totalCbm, cbmThreshold);
            return "air";
        }
        return preferredType != null ? preferredType : "sea";
    }

    /**
     * 주문 생성 시 검증
     */
    public OrderValidationResult validateOrder(BigDecimal totalCbm, BigDecimal totalAmount, 
                                             String currency, String memberCode, boolean isApprovedUser) {
        OrderValidationResult result = new OrderValidationResult();
        
        // CBM 초과 체크
        if (shouldConvertToAir(totalCbm)) {
            result.setConvertToAir(true);
            result.addWarning("CBM_EXCEEDED", 
                "총 CBM이 " + cbmThreshold + "m³를 초과하여 항공 배송으로 자동 전환됩니다.");
        }
        
        // THB 초과 체크
        if (requiresExtraRecipientInfo(totalAmount, currency)) {
            result.setRequiresExtraRecipient(true);
            result.addWarning("AMOUNT_EXCEEDED_THB_1500", 
                "금액이 THB 1,500을 초과합니다. 수취인 추가 정보를 입력해주세요.");
        }
        
        // 회원코드 체크
        if (hasDelayedProcessing(memberCode)) {
            result.setHasDelayedProcessing(true);
            result.addWarning("MEMBER_CODE_REQUIRED", 
                "회원코드가 없어 주문이 지연 처리됩니다. 회원코드를 입력해주세요.");
        }
        
        // 승인 상태 체크
        if (!isApprovedUser) {
            result.addWarning("APPROVAL_REQUIRED", 
                "계정 승인이 완료되지 않았습니다. 승인 후 주문이 처리됩니다.");
        }
        
        return result;
    }

    /**
     * 주문 검증 결과
     */
    public static class OrderValidationResult {
        private boolean convertToAir = false;
        private boolean requiresExtraRecipient = false;
        private boolean hasDelayedProcessing = false;
        private java.util.List<Warning> warnings = new java.util.ArrayList<>();
        
        public void addWarning(String type, String message) {
            warnings.add(new Warning(type, message));
        }
        
        // Getters and Setters
        public boolean isConvertToAir() { return convertToAir; }
        public void setConvertToAir(boolean convertToAir) { this.convertToAir = convertToAir; }
        
        public boolean isRequiresExtraRecipient() { return requiresExtraRecipient; }
        public void setRequiresExtraRecipient(boolean requiresExtraRecipient) { this.requiresExtraRecipient = requiresExtraRecipient; }
        
        public boolean isHasDelayedProcessing() { return hasDelayedProcessing; }
        public void setHasDelayedProcessing(boolean hasDelayedProcessing) { this.hasDelayedProcessing = hasDelayedProcessing; }
        
        public java.util.List<Warning> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<Warning> warnings) { this.warnings = warnings; }
    }

    /**
     * 경고 정보
     */
    public static class Warning {
        private String type;
        private String message;
        
        public Warning(String type, String message) {
            this.type = type;
            this.message = message;
        }
        
        public String getType() { return type; }
        public String getMessage() { return message; }
    }
}