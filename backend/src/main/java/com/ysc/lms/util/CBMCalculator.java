package com.ysc.lms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CBM(Cubic Meter) 계산 유틸리티
 * 
 * 비즈니스 룰:
 * - CBM = (Width × Height × Depth) / 1,000,000
 * - 단위: cm → m³ 변환
 * - 총 CBM이 29 초과시 자동으로 air 배송으로 전환
 */
@Component
@Slf4j
public class CBMCalculator {

    private static final BigDecimal CBM_CONVERSION_FACTOR = new BigDecimal("1000000"); // cm³ to m³
    private static final int CBM_SCALE = 6; // 소수점 6자리까지
    
    /**
     * 단일 박스 CBM 계산
     * 
     * @param widthCm 폭 (cm)
     * @param heightCm 높이 (cm)  
     * @param depthCm 깊이 (cm)
     * @return CBM (m³)
     */
    public BigDecimal calculateCBM(BigDecimal widthCm, BigDecimal heightCm, BigDecimal depthCm) {
        if (widthCm == null || heightCm == null || depthCm == null) {
            throw new IllegalArgumentException("박스 치수가 null일 수 없습니다.");
        }
        
        if (widthCm.compareTo(BigDecimal.ZERO) <= 0 || 
            heightCm.compareTo(BigDecimal.ZERO) <= 0 || 
            depthCm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("박스 치수는 0보다 커야 합니다.");
        }
        
        // CBM = (W × H × D) / 1,000,000
        BigDecimal volumeCm3 = widthCm.multiply(heightCm).multiply(depthCm);
        BigDecimal cbmM3 = volumeCm3.divide(CBM_CONVERSION_FACTOR, CBM_SCALE, RoundingMode.HALF_UP);
        
                 widthCm, heightCm, depthCm, volumeCm3, cbmM3);
        
        return cbmM3;
    }
    
    /**
     * 여러 박스의 총 CBM 계산
     * 
     * @param boxes 박스 치수 목록
     * @return 총 CBM (m³)
     */
    public BigDecimal calculateTotalCBM(java.util.List<BoxDimension> boxes) {
        if (boxes == null || boxes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCBM = boxes.stream()
                .map(box -> calculateCBM(box.getWidth(), box.getHeight(), box.getDepth()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        
        return totalCBM;
    }
    
    /**
     * CBM 기반 배송 방법 추천
     * 
     * @param totalCBM 총 CBM
     * @param threshold CBM 임계값 (기본 29.0)
     * @return 추천 배송 방법 ("sea" 또는 "air")
     */
    public String recommendShippingMethod(BigDecimal totalCBM, BigDecimal threshold) {
        if (totalCBM.compareTo(threshold) > 0) {
            log.info("CBM {} m³이 임계값 {} m³을 초과하여 항공 배송 추천", totalCBM, threshold);
            return "air";
        } else {
            return "sea";
        }
    }
    
    /**
     * CBM 기반 배송 방법 추천 (기본 임계값 29 사용)
     */
    public String recommendShippingMethod(BigDecimal totalCBM) {
        return recommendShippingMethod(totalCBM, new BigDecimal("29.0"));
    }
    
    /**
     * CBM을 다른 단위로 변환
     * 
     * @param cbmM3 CBM (m³)
     * @return 변환 결과
     */
    public CBMConversionResult convertCBMUnits(BigDecimal cbmM3) {
        // m³ → cm³
        BigDecimal cm3 = cbmM3.multiply(CBM_CONVERSION_FACTOR);
        
        // m³ → ft³ (1 m³ = 35.3147 ft³)
        BigDecimal ft3 = cbmM3.multiply(new BigDecimal("35.3147"))
                .setScale(3, RoundingMode.HALF_UP);
        
        // m³ → 리터 (1 m³ = 1000 L)
        BigDecimal liters = cbmM3.multiply(new BigDecimal("1000"))
                .setScale(2, RoundingMode.HALF_UP);
        
        return CBMConversionResult.builder()
                .m3(cbmM3)
                .cm3(cm3)
                .ft3(ft3)
                .liters(liters)
                .build();
    }
    
    /**
     * 표준 박스 크기별 CBM 미리 계산된 값들
     */
    public static class StandardBoxes {
        public static final BigDecimal SMALL_BOX_CBM = new BigDecimal("0.008000");  // 20×20×20 cm
        public static final BigDecimal MEDIUM_BOX_CBM = new BigDecimal("0.027000"); // 30×30×30 cm  
        public static final BigDecimal LARGE_BOX_CBM = new BigDecimal("0.064000");  // 40×40×40 cm
        public static final BigDecimal XLARGE_BOX_CBM = new BigDecimal("0.125000"); // 50×50×50 cm
    }
    
    /**
     * 박스 치수 최적화 추천
     * CBM을 최소화하면서 내용물을 담을 수 있는 최적 치수 계산
     * 
     * @param requiredVolumeCm3 필요한 부피 (cm³)
     * @param aspectRatio 가로:세로:높이 비율 (예: "2:2:1")
     * @return 최적화된 박스 치수
     */
    public BoxDimension optimizeBoxDimensions(BigDecimal requiredVolumeCm3, String aspectRatio) {
        String[] ratios = aspectRatio.split(":");
        if (ratios.length != 3) {
            throw new IllegalArgumentException("종횡비는 'W:H:D' 형태여야 합니다. 예: '2:2:1'");
        }
        
        double wRatio = Double.parseDouble(ratios[0]);
        double hRatio = Double.parseDouble(ratios[1]);
        double dRatio = Double.parseDouble(ratios[2]);
        
        // V = W × H × D = k³ × wRatio × hRatio × dRatio
        // k = ³√(V / (wRatio × hRatio × dRatio))
        double k = Math.cbrt(requiredVolumeCm3.doubleValue() / (wRatio * hRatio * dRatio));
        
        BigDecimal width = BigDecimal.valueOf(k * wRatio).setScale(1, RoundingMode.HALF_UP);
        BigDecimal height = BigDecimal.valueOf(k * hRatio).setScale(1, RoundingMode.HALF_UP);
        BigDecimal depth = BigDecimal.valueOf(k * dRatio).setScale(1, RoundingMode.HALF_UP);
        
        return BoxDimension.builder()
                .width(width)
                .height(height)
                .depth(depth)
                .cbm(calculateCBM(width, height, depth))
                .build();
    }
    
    /**
     * 박스 포장 효율성 계산
     * 
     * @param itemsVolume 실제 아이템들의 부피
     * @param boxVolume 박스의 부피
     * @return 포장 효율성 (0.0 ~ 1.0)
     */
    public BigDecimal calculatePackingEfficiency(BigDecimal itemsVolume, BigDecimal boxVolume) {
        if (boxVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return itemsVolume.divide(boxVolume, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * 다중 박스 조합 최적화
     * 여러 개의 작은 박스 vs 하나의 큰 박스 CBM 비교
     */
    public CBMOptimizationResult optimizeMultipleBoxes(
            java.util.List<BoxDimension> currentBoxes,
            BoxDimension alternativeBox) {
        
        BigDecimal currentTotalCBM = calculateTotalCBM(currentBoxes);
        BigDecimal alternativeCBM = calculateCBM(
            alternativeBox.getWidth(),
            alternativeBox.getHeight(), 
            alternativeBox.getDepth()
        );
        
        boolean isAlternativeBetter = alternativeCBM.compareTo(currentTotalCBM) < 0;
        BigDecimal savings = currentTotalCBM.subtract(alternativeCBM);
        
        return CBMOptimizationResult.builder()
                .currentTotalCBM(currentTotalCBM)
                .alternativeCBM(alternativeCBM)
                .isAlternativeBetter(isAlternativeBetter)
                .cbmSavings(savings)
                .savingsPercentage(
                    currentTotalCBM.compareTo(BigDecimal.ZERO) > 0 
                        ? savings.divide(currentTotalCBM, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO
                )
                .recommendation(isAlternativeBetter ? 
                    "하나의 큰 박스 사용을 권장합니다." : 
                    "현재 박스 구성이 더 효율적입니다.")
                .build();
    }
}

// DTO 클래스들
@lombok.Data
@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
class BoxDimension {
    private BigDecimal width;   // cm
    private BigDecimal height;  // cm
    private BigDecimal depth;   // cm
    private BigDecimal cbm;     // m³ (계산됨)
}

@lombok.Data
@lombok.Builder
class CBMConversionResult {
    private BigDecimal m3;      // 세제곱미터
    private BigDecimal cm3;     // 세제곱센티미터
    private BigDecimal ft3;     // 세제곱피트
    private BigDecimal liters;  // 리터
}

@lombok.Data
@lombok.Builder
class CBMOptimizationResult {
    private BigDecimal currentTotalCBM;
    private BigDecimal alternativeCBM;
    private boolean isAlternativeBetter;
    private BigDecimal cbmSavings;
    private BigDecimal savingsPercentage;
    private String recommendation;
}