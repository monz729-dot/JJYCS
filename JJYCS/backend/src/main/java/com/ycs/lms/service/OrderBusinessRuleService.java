package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderBusinessRuleService {
    
    @Value("${business-rules.cbm-threshold:29.0}")
    private BigDecimal cbmThreshold;
    
    @Value("${business-rules.thb-value-threshold:1500.0}")
    private BigDecimal thbValueThreshold;
    
    @Value("${business-rules.min-cbm-rate:0.000001}")
    private BigDecimal minCbmRate;
    
    /**
     * 주문에 대한 모든 비즈니스 룰을 적용
     */
    public OrderBusinessRuleResult applyBusinessRules(Order order) {
        OrderBusinessRuleResult result = new OrderBusinessRuleResult();
        
        // 1. CBM 계산 및 검증
        calculateAndValidateCBM(order, result);
        
        // 2. THB 가치 검증
        validateThbValue(order, result);
        
        // 3. 회원 코드 검증
        validateMemberCode(order, result);
        
        // 4. 배송 방식 자동 결정
        determineShippingMethod(order, result);
        
        // 5. 추가 정보 필요 여부 결정
        determineAdditionalInfoRequirements(order, result);
        
        log.info("Applied business rules for order {}: CBM={}, THB={}, RequiresExtra={}, HasNoCode={}, ShippingMethod={}", 
            order.getId(), result.getTotalCbm(), result.getTotalThbValue(), 
            result.isRequiresExtraRecipientInfo(), result.isHasNoMemberCode(), result.getRecommendedShippingMethod());
        
        return result;
    }
    
    /**
     * CBM 계산 및 검증
     */
    private void calculateAndValidateCBM(Order order, OrderBusinessRuleResult result) {
        BigDecimal totalCbm = BigDecimal.ZERO;
        
        if (order.getOrderBoxes() != null && !order.getOrderBoxes().isEmpty()) {
            for (OrderBox box : order.getOrderBoxes()) {
                BigDecimal cbm = calculateBoxCBM(box);
                box.setCbmM3(cbm);
                totalCbm = totalCbm.add(cbm);
            }
        } else {
            // 박스 정보가 없으면 아이템 기반으로 예상 CBM 계산
            totalCbm = estimateCbmFromItems(order);
        }
        
        result.setTotalCbm(totalCbm);
        result.setCbmExceedsThreshold(totalCbm.compareTo(cbmThreshold) > 0);
        
        // 주문 엔티티에도 업데이트
        order.setTotalCbm(totalCbm);
        
        if (result.isCbmExceedsThreshold()) {
            result.getWarnings().add("CBM이 " + cbmThreshold + "를 초과합니다 (" + totalCbm + "). 항공 배송으로 전환됩니다.");
        }
    }
    
    /**
     * 박스 CBM 계산 (가로 × 세로 × 높이 / 1,000,000)
     */
    private BigDecimal calculateBoxCBM(OrderBox box) {
        if (box.getWidthCm() == null || box.getHeightCm() == null || box.getDepthCm() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal width = box.getWidthCm();
        BigDecimal height = box.getHeightCm();
        BigDecimal depth = box.getDepthCm();
        
        BigDecimal cbm = width.multiply(height).multiply(depth)
                        .divide(BigDecimal.valueOf(1000000), 6, RoundingMode.HALF_UP);
        
        // 최소 CBM 적용 (너무 작은 값 방지)
        if (cbm.compareTo(minCbmRate) < 0) {
            cbm = minCbmRate;
        }
        
        log.debug("Calculated CBM for box {}x{}x{}: {}", width, height, depth, cbm);
        
        return cbm;
    }
    
    /**
     * 아이템 기반 예상 CBM 계산
     */
    private BigDecimal estimateCbmFromItems(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 실제 계산: 아이템들의 CBM을 합산
        BigDecimal totalCbm = BigDecimal.ZERO;
        
        for (OrderItem item : order.getOrderItems()) {
            if (item.getWidth() != null && item.getHeight() != null && item.getDepth() != null && item.getQuantity() != null) {
                // CBM = (W × H × D × Quantity) / 1,000,000
                BigDecimal itemCbm = item.getWidth()
                    .multiply(item.getHeight())
                    .multiply(item.getDepth())
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .divide(BigDecimal.valueOf(1000000), 6, RoundingMode.HALF_UP);
                
                totalCbm = totalCbm.add(itemCbm);
                
                log.debug("Item CBM: {}x{}x{}x{} = {} m³", 
                    item.getWidth(), item.getHeight(), item.getDepth(), item.getQuantity(), itemCbm);
            }
        }
        
        log.debug("Total estimated CBM from {} items: {} m³", order.getOrderItems().size(), totalCbm);
        
        return totalCbm;
    }
    
    /**
     * THB 가치 검증 (1,500 THB 초과시 수취인 추가 정보 필요)
     */
    private void validateThbValue(Order order, OrderBusinessRuleResult result) {
        BigDecimal totalThbValue = BigDecimal.ZERO;
        
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getThbValue() != null) {
                    totalThbValue = totalThbValue.add(item.getThbValue().multiply(item.getQuantity()));
                }
            }
        }
        
        result.setTotalThbValue(totalThbValue);
        result.setThbValueExceedsThreshold(totalThbValue.compareTo(thbValueThreshold) > 0);
        
        if (result.isThbValueExceedsThreshold()) {
            result.getWarnings().add("총 THB 가치가 " + thbValueThreshold + "를 초과합니다 (" + totalThbValue + "). 수취인 추가 정보가 필요합니다.");
        }
    }
    
    /**
     * 회원 코드 검증
     */
    private void validateMemberCode(Order order, OrderBusinessRuleResult result) {
        boolean hasNoMemberCode = order.getUser() == null || 
                                 order.getUser().getMemberCode() == null || 
                                 order.getUser().getMemberCode().trim().isEmpty();
        
        result.setHasNoMemberCode(hasNoMemberCode);
        
        if (hasNoMemberCode) {
            result.getWarnings().add("회원 코드가 없습니다. 발송이 지연될 수 있습니다.");
        }
    }
    
    /**
     * 배송 방식 자동 결정
     */
    private void determineShippingMethod(Order order, OrderBusinessRuleResult result) {
        String recommendedMethod;
        
        if (result.isCbmExceedsThreshold()) {
            recommendedMethod = "AIR"; // 항공 배송
        } else {
            recommendedMethod = "SEA"; // 해상 배송 (기본)
        }
        
        result.setRecommendedShippingMethod(recommendedMethod);
        
        // 주문 타입 업데이트
        if ("AIR".equals(recommendedMethod)) {
            order.setOrderType(Order.OrderType.AIR);
        } else {
            order.setOrderType(Order.OrderType.SEA);
        }
    }
    
    /**
     * 추가 정보 필요 여부 결정
     */
    private void determineAdditionalInfoRequirements(Order order, OrderBusinessRuleResult result) {
        boolean requiresExtraRecipientInfo = result.isThbValueExceedsThreshold();
        
        result.setRequiresExtraRecipientInfo(requiresExtraRecipientInfo);
        
        // 주문 엔티티에도 업데이트
        order.setRequiresExtraRecipient(requiresExtraRecipientInfo);
        order.setHasNoMemberCode(result.isHasNoMemberCode());
    }
    
    /**
     * 비즈니스 룰 적용 결과
     */
    public static class OrderBusinessRuleResult {
        private BigDecimal totalCbm = BigDecimal.ZERO;
        private boolean cbmExceedsThreshold = false;
        private BigDecimal totalThbValue = BigDecimal.ZERO;
        private boolean thbValueExceedsThreshold = false;
        private boolean hasNoMemberCode = false;
        private String recommendedShippingMethod = "SEA";
        private boolean requiresExtraRecipientInfo = false;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        
        // Getters and Setters
        public BigDecimal getTotalCbm() { return totalCbm; }
        public void setTotalCbm(BigDecimal totalCbm) { this.totalCbm = totalCbm; }
        
        public boolean isCbmExceedsThreshold() { return cbmExceedsThreshold; }
        public void setCbmExceedsThreshold(boolean cbmExceedsThreshold) { this.cbmExceedsThreshold = cbmExceedsThreshold; }
        
        public BigDecimal getTotalThbValue() { return totalThbValue; }
        public void setTotalThbValue(BigDecimal totalThbValue) { this.totalThbValue = totalThbValue; }
        
        public boolean isThbValueExceedsThreshold() { return thbValueExceedsThreshold; }
        public void setThbValueExceedsThreshold(boolean thbValueExceedsThreshold) { this.thbValueExceedsThreshold = thbValueExceedsThreshold; }
        
        public boolean isHasNoMemberCode() { return hasNoMemberCode; }
        public void setHasNoMemberCode(boolean hasNoMemberCode) { this.hasNoMemberCode = hasNoMemberCode; }
        
        public String getRecommendedShippingMethod() { return recommendedShippingMethod; }
        public void setRecommendedShippingMethod(String recommendedShippingMethod) { this.recommendedShippingMethod = recommendedShippingMethod; }
        
        public boolean isRequiresExtraRecipientInfo() { return requiresExtraRecipientInfo; }
        public void setRequiresExtraRecipientInfo(boolean requiresExtraRecipientInfo) { this.requiresExtraRecipientInfo = requiresExtraRecipientInfo; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasErrors() { return !errors.isEmpty(); }
    }
}