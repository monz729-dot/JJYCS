package com.ycs.lms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Slf4j
public class CBMCalculator {
    
    private static final BigDecimal CBM_THRESHOLD = new BigDecimal("29.0");
    private static final BigDecimal CM_TO_M_DIVISOR = new BigDecimal("1000000");
    
    public static class BoxDimensions {
        public BigDecimal width;
        public BigDecimal height;
        public BigDecimal depth;
        
        public BoxDimensions(BigDecimal width, BigDecimal height, BigDecimal depth) {
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
        
        public BoxDimensions(double width, double height, double depth) {
            this.width = BigDecimal.valueOf(width);
            this.height = BigDecimal.valueOf(height);
            this.depth = BigDecimal.valueOf(depth);
        }
    }
    
    public static class CBMResult {
        public BigDecimal cbm;
        public boolean exceedsThreshold;
        public boolean requiresAirConversion;
        
        public CBMResult(BigDecimal cbm, boolean exceedsThreshold) {
            this.cbm = cbm;
            this.exceedsThreshold = exceedsThreshold;
            this.requiresAirConversion = exceedsThreshold;
        }
    }
    
    /**
     * 단일 박스의 CBM 계산
     * CBM = (width × height × depth) / 1,000,000
     */
    public CBMResult calculateCBM(BoxDimensions dimensions) {
        if (dimensions.width == null || dimensions.height == null || dimensions.depth == null) {
            throw new IllegalArgumentException("박스 치수가 누락되었습니다.");
        }
        
        if (dimensions.width.compareTo(BigDecimal.ZERO) <= 0 || 
            dimensions.height.compareTo(BigDecimal.ZERO) <= 0 || 
            dimensions.depth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("박스 치수는 0보다 커야 합니다.");
        }
        
        BigDecimal cbm = dimensions.width
            .multiply(dimensions.height)
            .multiply(dimensions.depth)
            .divide(CM_TO_M_DIVISOR, 6, RoundingMode.HALF_UP);
        
        boolean exceedsThreshold = cbm.compareTo(CBM_THRESHOLD) > 0;
        
        
        return new CBMResult(cbm, exceedsThreshold);
    }
    
    /**
     * 여러 박스의 총 CBM 계산
     */
    public CBMResult calculateTotalCBM(List<BoxDimensions> boxes) {
        if (boxes == null || boxes.isEmpty()) {
            return new CBMResult(BigDecimal.ZERO, false);
        }
        
        BigDecimal totalCBM = boxes.stream()
            .map(this::calculateCBM)
            .map(result -> result.cbm)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean exceedsThreshold = totalCBM.compareTo(CBM_THRESHOLD) > 0;
        
        log.info("총 CBM 계산: {} 개 박스 = {} m³ (임계값: {} m³, 초과: {})", 
                boxes.size(), totalCBM, CBM_THRESHOLD, exceedsThreshold);
        
        return new CBMResult(totalCBM, exceedsThreshold);
    }
    
    /**
     * CBM 기반 배송 타입 결정
     */
    public String determineShippingType(BigDecimal cbm) {
        return cbm.compareTo(CBM_THRESHOLD) > 0 ? "air" : "sea";
    }
    
    /**
     * CBM 임계값 초과 경고 메시지 생성
     */
    public String generateWarningMessage(BigDecimal cbm) {
        if (cbm.compareTo(CBM_THRESHOLD) > 0) {
            return String.format("CBM이 %.3f m³로 임계값 %.1f m³를 초과하여 항공배송으로 자동 전환됩니다.", 
                    cbm.doubleValue(), CBM_THRESHOLD.doubleValue());
        }
        return null;
    }
}