package com.ycs.lms.service;

import com.ycs.lms.dto.CreateOrderRequest;
import com.ycs.lms.dto.OrderItemDto;
import com.ycs.lms.dto.RecipientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 주문 검증 서비스 - 태국 전용 비즈니스 규칙
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderValidationService {
    
    @Value("${app.business.cbm-threshold:29.0}")
    private BigDecimal cbmThreshold;
    
    @Value("${app.business.customs-threshold-thb:1500.0}")
    private BigDecimal customsThresholdThb;
    
    @Value("${app.business.max-recipients:5}")
    private int maxRecipients;
    
    private final EmsApiService emsApiService;
    
    /**
     * 주문 생성 전 전체 검증
     */
    public ValidationResult validateCreateOrder(CreateOrderRequest request) {
        ValidationResult result = new ValidationResult();
        
        // 1. 기본 필드 검증
        validateBasicFields(request, result);
        
        // 2. 수취인 검증
        validateRecipients(request.getRecipients(), result);
        
        // 3. 품목 검증
        validateOrderItems(request.getItems(), result);
        
        // 4. 배대지 접수 정보 검증
        validateInboundInfo(request, result);
        
        // 5. 비즈니스 규칙 검증
        validateBusinessRules(request, result);
        
        log.info("Order validation completed: {} errors, {} warnings", 
            result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }
    
    /**
     * 기본 필드 검증
     */
    private void validateBasicFields(CreateOrderRequest request, ValidationResult result) {
        // 도착 국가 검증 (태국 고정)
        if (!"TH".equals(request.getDestCountry())) {
            result.addError("INVALID_DEST_COUNTRY", "현재 태국(TH)만 지원됩니다.");
        }
        
        // 배송 유형 검증
        if (!"SEA".equals(request.getShippingType()) && !"AIR".equals(request.getShippingType())) {
            result.addError("INVALID_SHIPPING_TYPE", "배송 유형은 SEA 또는 AIR만 가능합니다.");
        }
        
        // 태국 우편번호 검증
        if (request.getDestZip() != null && !request.getDestZip().matches("^[0-9]{5}$")) {
            result.addError("INVALID_THAI_POSTAL", "태국 우편번호는 5자리 숫자입니다.");
        }
    }
    
    /**
     * 수취인 정보 검증
     */
    private void validateRecipients(List<RecipientDto> recipients, ValidationResult result) {
        if (recipients == null || recipients.isEmpty()) {
            result.addError("NO_RECIPIENTS", "최소 1명의 수취인은 필요합니다.");
            return;
        }
        
        if (recipients.size() > maxRecipients) {
            result.addError("TOO_MANY_RECIPIENTS", 
                String.format("수취인은 최대 %d명까지 등록 가능합니다.", maxRecipients));
        }
        
        // 기본 수취인 확인
        boolean hasPrimary = recipients.stream()
            .anyMatch(r -> r.getIsPrimary() != null && r.getIsPrimary());
        
        if (!hasPrimary) {
            result.addWarning("NO_PRIMARY_RECIPIENT", "기본 수취인이 지정되지 않았습니다. 첫 번째 수취인을 기본으로 설정합니다.");
        }
        
        // 각 수취인별 검증
        for (int i = 0; i < recipients.size(); i++) {
            RecipientDto recipient = recipients.get(i);
            String prefix = String.format("수취인 #%d", i + 1);
            
            if (!recipient.hasRequiredFields()) {
                result.addError("INCOMPLETE_RECIPIENT", 
                    prefix + ": 이름, 전화번호, 주소는 필수입니다.");
            }
            
            if (!recipient.isValidThailandPhone()) {
                result.addError("INVALID_THAI_PHONE", 
                    prefix + ": 태국 전화번호 형식이 올바르지 않습니다.");
            }
            
            if (recipient.getPostalCode() != null && !recipient.isValidThailandPostalCode()) {
                result.addError("INVALID_RECIPIENT_POSTAL", 
                    prefix + ": 우편번호는 5자리 숫자입니다.");
            }
        }
    }
    
    /**
     * 주문 품목 검증
     */
    private void validateOrderItems(List<OrderItemDto> items, ValidationResult result) {
        if (items == null || items.isEmpty()) {
            result.addError("NO_ORDER_ITEMS", "최소 1개의 주문 품목은 필요합니다.");
            return;
        }
        
        BigDecimal totalCbm = BigDecimal.ZERO;
        BigDecimal totalThb = BigDecimal.ZERO;
        
        for (int i = 0; i < items.size(); i++) {
            OrderItemDto item = items.get(i);
            String prefix = String.format("품목 #%d", i + 1);
            
            // 필수 필드 검증
            if (!item.isValidHsCode()) {
                result.addError("INVALID_HS_CODE", 
                    prefix + ": HS 코드 형식이 올바르지 않습니다. (예: 6101.20)");
            }
            
            if (!item.hasDimensionInfo()) {
                result.addError("MISSING_DIMENSIONS", 
                    prefix + ": 치수 정보(가로×세로×높이)는 필수입니다.");
            }
            
            // CBM 누적
            totalCbm = totalCbm.add(item.getTotalCbm());
            
            // 가격 누적
            if (item.getTotalPrice() != null) {
                totalThb = totalThb.add(item.getTotalPrice());
            }
            
            // 개별 품목별 검증
            if (item.exceedsCustomsThreshold()) {
                result.addWarning("HIGH_VALUE_ITEM", 
                    prefix + String.format(": 가격이 THB %.2f로 세관 신고 임계값(THB %.2f)을 초과합니다.", 
                        item.getTotalPrice().doubleValue(), customsThresholdThb.doubleValue()));
            }
        }
        
        // 전체 합계 기준 검증 저장
        result.getMetadata().put("totalCbm", totalCbm);
        result.getMetadata().put("totalThb", totalThb);
    }
    
    /**
     * 배대지 접수 정보 검증
     */
    private void validateInboundInfo(CreateOrderRequest request, ValidationResult result) {
        String method = request.getInboundMethod();
        
        if ("COURIER".equals(method)) {
            if (!request.isValidCourierInfo()) {
                result.addError("INCOMPLETE_COURIER_INFO", 
                    "택배 접수 시 택배사명과 송장번호는 필수입니다.");
            }
        } else if ("QUICK".equals(method)) {
            if (!request.isValidQuickInfo()) {
                result.addError("INCOMPLETE_QUICK_INFO", 
                    "퀵서비스 접수 시 업체명은 필수입니다.");
            }
        }
        
        if (request.getInboundLocationId() == null) {
            result.addWarning("NO_INBOUND_LOCATION", 
                "YCS 접수지가 지정되지 않았습니다. 기본 접수지로 설정됩니다.");
        }
    }
    
    /**
     * 비즈니스 규칙 검증 (CBM 29 초과, THB 1500 초과, No Code 처리)
     */
    private void validateBusinessRules(CreateOrderRequest request, ValidationResult result) {
        BigDecimal totalCbm = (BigDecimal) result.getMetadata().get("totalCbm");
        BigDecimal totalThb = (BigDecimal) result.getMetadata().get("totalThb");
        
        // 1. CBM 29 초과 → 항공 전환 규칙
        if (totalCbm != null && totalCbm.compareTo(cbmThreshold) > 0) {
            if ("SEA".equals(request.getShippingType())) {
                if (request.getAgreeAirConversion() == null || !request.getAgreeAirConversion()) {
                    result.addError("CBM_EXCEEDS_THRESHOLD", 
                        String.format("총 CBM(%.3fm³)이 임계값(%.1fm³)을 초과하여 항공 배송으로 전환이 필요합니다. 동의해주세요.", 
                            totalCbm.doubleValue(), cbmThreshold.doubleValue()));
                } else {
                    result.addWarning("AUTO_AIR_CONVERSION", 
                        String.format("CBM 초과로 해상→항공 배송으로 자동 전환됩니다. (%.3fm³ > %.1fm³)", 
                            totalCbm.doubleValue(), cbmThreshold.doubleValue()));
                    // 자동 전환 표시
                    result.getMetadata().put("autoConvertToAir", true);
                }
            }
        }
        
        // 2. THB 1,500 초과 → 수취인 추가 정보 필요
        if (totalThb != null && totalThb.compareTo(customsThresholdThb) > 0) {
            if (request.getAgreeExtraRecipient() == null || !request.getAgreeExtraRecipient()) {
                result.addWarning("CUSTOMS_THRESHOLD_EXCEEDED", 
                    String.format("총 상품 가격(THB %.2f)이 세관 신고 임계값(THB %.2f)을 초과합니다. 수취인 추가 정보가 필요할 수 있습니다.", 
                        totalThb.doubleValue(), customsThresholdThb.doubleValue()));
            }
            result.getMetadata().put("requiresExtraRecipient", true);
        }
        
        // 3. 회원코드 미기재 처리 (User 정보에서 확인 - 여기서는 플래그만 확인)
        // 실제로는 User 엔티티에서 member_code 필드를 확인해야 함
        if (request.getAgreeDelayProcessing() != null && request.getAgreeDelayProcessing()) {
            result.addWarning("NO_MEMBER_CODE_DELAY", 
                "회원코드 미기재로 발송이 지연될 수 있습니다.");
            result.getMetadata().put("delayProcessing", true);
        }
    }
    
    /**
     * 태국 우편번호 실시간 검증
     */
    public ValidationResult validateThailandPostalCode(String postalCode) {
        ValidationResult result = new ValidationResult();
        
        if (postalCode == null || !postalCode.matches("^[0-9]{5}$")) {
            result.addError("INVALID_FORMAT", "태국 우편번호는 5자리 숫자입니다.");
            return result;
        }
        
        try {
            // EMS API를 통한 실제 우편번호 검증
            var postalResults = emsApiService.searchPostalCode(postalCode);
            
            if (postalResults.isEmpty()) {
                result.addWarning("POSTAL_NOT_FOUND", 
                    "입력한 우편번호를 찾을 수 없습니다. 정확한지 확인해주세요.");
            } else {
                result.addInfo("POSTAL_FOUND", 
                    String.format("우편번호 확인: %s (%s)", 
                        postalResults.get(0).getCity(), postalResults.get(0).getDistrict()));
            }
        } catch (Exception e) {
            log.warn("EMS postal code validation failed: {}", e.getMessage());
            result.addWarning("POSTAL_API_ERROR", 
                "우편번호 검증 서비스 연결에 실패했지만, 형식은 올바릅니다.");
        }
        
        return result;
    }
    
    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private List<ValidationMessage> errors = new ArrayList<>();
        private List<ValidationMessage> warnings = new ArrayList<>();
        private List<ValidationMessage> info = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();
        
        public void addError(String code, String message) {
            errors.add(new ValidationMessage(code, message, "ERROR"));
        }
        
        public void addWarning(String code, String message) {
            warnings.add(new ValidationMessage(code, message, "WARNING"));
        }
        
        public void addInfo(String code, String message) {
            info.add(new ValidationMessage(code, message, "INFO"));
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        // Getters
        public List<ValidationMessage> getErrors() { return errors; }
        public List<ValidationMessage> getWarnings() { return warnings; }
        public List<ValidationMessage> getInfo() { return info; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
    
    /**
     * 검증 메시지 클래스
     */
    public static class ValidationMessage {
        private String code;
        private String message;
        private String level;
        
        public ValidationMessage(String code, String message, String level) {
            this.code = code;
            this.message = message;
            this.level = level;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getLevel() { return level; }
    }
}