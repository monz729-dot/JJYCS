package com.ycs.lms.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CBM Calculator Tests")
class CBMCalculatorTest {

    private CBMCalculator cbmCalculator;

    @BeforeEach
    void setUp() {
        cbmCalculator = new CBMCalculator();
    }

    @Test
    @DisplayName("정확한 CBM 계산 - 100cm x 100cm x 100cm = 1.0 m³")
    void shouldCalculateCBMCorrectly() {
        // Given
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(100.0, 100.0, 100.0);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        assertEquals(new BigDecimal("1.000000"), result.cbm);
        assertFalse(result.exceedsThreshold);
        assertFalse(result.requiresAirConversion);
    }

    @Test
    @DisplayName("CBM 계산 - 소수점 정밀도 테스트")
    void shouldCalculateCBMWithPrecision() {
        // Given
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(50.5, 30.2, 25.8);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        BigDecimal expected = new BigDecimal("50.5")
                .multiply(new BigDecimal("30.2"))
                .multiply(new BigDecimal("25.8"))
                .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
        
        assertEquals(expected, result.cbm);
    }

    @Test
    @DisplayName("CBM 임계값 초과 테스트 - 29 m³ 초과")
    void shouldDetectCBMThresholdExceed() {
        // Given - 30m³가 되는 치수 (약 310cm x 310cm x 312cm)
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(400.0, 400.0, 200.0);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        assertTrue(result.cbm.compareTo(new BigDecimal("29.0")) > 0);
        assertTrue(result.exceedsThreshold);
        assertTrue(result.requiresAirConversion);
    }

    @Test
    @DisplayName("CBM 임계값 미만 테스트 - 29 m³ 미만")
    void shouldNotExceedCBMThreshold() {
        // Given - 20m³가 되는 치수
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(271.0, 271.0, 271.0);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        assertTrue(result.cbm.compareTo(new BigDecimal("29.0")) < 0);
        assertFalse(result.exceedsThreshold);
        assertFalse(result.requiresAirConversion);
    }

    @ParameterizedTest
    @CsvSource({
        "100, 100, 100, 1.000000",
        "200, 150, 100, 3.000000",
        "50, 50, 50, 0.125000",
        "300, 300, 100, 9.000000",
        "400, 400, 200, 32.000000"
    })
    @DisplayName("다양한 치수의 CBM 계산 테스트")
    void shouldCalculateVariousCBMs(double width, double height, double depth, String expectedCBM) {
        // Given
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(width, height, depth);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        assertEquals(new BigDecimal(expectedCBM), result.cbm);
    }

    @Test
    @DisplayName("여러 박스의 총 CBM 계산")
    void shouldCalculateTotalCBMForMultipleBoxes() {
        // Given
        List<CBMCalculator.BoxDimensions> boxes = Arrays.asList(
            new CBMCalculator.BoxDimensions(100.0, 100.0, 100.0), // 1.0 m³
            new CBMCalculator.BoxDimensions(200.0, 150.0, 100.0), // 3.0 m³
            new CBMCalculator.BoxDimensions(50.0, 50.0, 50.0)     // 0.125 m³
        );
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateTotalCBM(boxes);
        
        // Then
        assertEquals(new BigDecimal("4.125000"), result.cbm);
        assertFalse(result.exceedsThreshold);
    }

    @Test
    @DisplayName("여러 박스 총 CBM이 29 m³ 초과하는 경우")
    void shouldDetectTotalCBMExceedsThreshold() {
        // Given
        List<CBMCalculator.BoxDimensions> boxes = Arrays.asList(
            new CBMCalculator.BoxDimensions(300.0, 300.0, 100.0), // 9.0 m³
            new CBMCalculator.BoxDimensions(300.0, 300.0, 100.0), // 9.0 m³
            new CBMCalculator.BoxDimensions(300.0, 300.0, 100.0), // 9.0 m³
            new CBMCalculator.BoxDimensions(300.0, 300.0, 100.0)  // 9.0 m³
        ); // Total: 36.0 m³
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateTotalCBM(boxes);
        
        // Then
        assertEquals(new BigDecimal("36.000000"), result.cbm);
        assertTrue(result.exceedsThreshold);
        assertTrue(result.requiresAirConversion);
    }

    @Test
    @DisplayName("빈 박스 리스트 처리")
    void shouldHandleEmptyBoxList() {
        // Given
        List<CBMCalculator.BoxDimensions> emptyBoxes = Arrays.asList();
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateTotalCBM(emptyBoxes);
        
        // Then
        assertEquals(BigDecimal.ZERO, result.cbm);
        assertFalse(result.exceedsThreshold);
    }

    @Test
    @DisplayName("null 박스 리스트 처리")
    void shouldHandleNullBoxList() {
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateTotalCBM(null);
        
        // Then
        assertEquals(BigDecimal.ZERO, result.cbm);
        assertFalse(result.exceedsThreshold);
    }

    @Test
    @DisplayName("잘못된 치수 입력 처리 - null 값")
    void shouldThrowExceptionForNullDimensions() {
        // Given
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(null, new BigDecimal("100"), new BigDecimal("100"));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cbmCalculator.calculateCBM(dimensions)
        );
        
        assertEquals("박스 치수가 누락되었습니다.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    @DisplayName("잘못된 치수 입력 처리 - 0 또는 음수")
    void shouldThrowExceptionForInvalidDimensions(double invalidValue) {
        // Given
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(invalidValue, 100.0, 100.0);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cbmCalculator.calculateCBM(dimensions)
        );
        
        assertEquals("박스 치수는 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("배송 타입 결정 - CBM 기준")
    void shouldDetermineShippingTypeBasedOnCBM() {
        // Given
        BigDecimal cbmUnder29 = new BigDecimal("25.5");
        BigDecimal cbmOver29 = new BigDecimal("30.2");
        
        // When & Then
        assertEquals("sea", cbmCalculator.determineShippingType(cbmUnder29));
        assertEquals("air", cbmCalculator.determineShippingType(cbmOver29));
    }

    @Test
    @DisplayName("CBM 경고 메시지 생성")
    void shouldGenerateWarningMessageForExceededCBM() {
        // Given
        BigDecimal cbmOver29 = new BigDecimal("32.456");
        BigDecimal cbmUnder29 = new BigDecimal("25.5");
        
        // When
        String warningMessage = cbmCalculator.generateWarningMessage(cbmOver29);
        String noWarning = cbmCalculator.generateWarningMessage(cbmUnder29);
        
        // Then
        assertNotNull(warningMessage);
        assertTrue(warningMessage.contains("32.456 m³"));
        assertTrue(warningMessage.contains("29.0 m³"));
        assertTrue(warningMessage.contains("항공배송"));
        
        assertNull(noWarning);
    }

    @Test
    @DisplayName("경계값 테스트 - 정확히 29 m³")
    void shouldHandleExactThresholdValue() {
        // Given - 정확히 29m³
        BigDecimal exactThreshold = new BigDecimal("29.000000");
        CBMCalculator.BoxDimensions dimensions = findDimensionsForCBM(29.0);
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then - 29.0은 포함하지 않으므로 false여야 함
        assertFalse(result.exceedsThreshold, "정확히 29m³는 임계값을 초과하지 않아야 함");
        assertFalse(result.requiresAirConversion);
    }

    @Test
    @DisplayName("경계값 테스트 - 29 m³ + 0.000001")
    void shouldDetectMinimalExceedance() {
        // Given - 29m³를 아주 조금 초과
        CBMCalculator.BoxDimensions dimensions = new CBMCalculator.BoxDimensions(
            new BigDecimal("310.0001"), 
            new BigDecimal("310"), 
            new BigDecimal("301")
        );
        
        // When
        CBMCalculator.CBMResult result = cbmCalculator.calculateCBM(dimensions);
        
        // Then
        assertTrue(result.cbm.compareTo(new BigDecimal("29")) > 0);
        assertTrue(result.exceedsThreshold);
        assertTrue(result.requiresAirConversion);
    }

    // 유틸리티 메서드: 특정 CBM에 대한 치수 계산
    private CBMCalculator.BoxDimensions findDimensionsForCBM(double targetCBM) {
        // 정육면체로 계산: CBM = (side)³ / 1,000,000
        // side³ = CBM * 1,000,000
        double side = Math.cbrt(targetCBM * 1_000_000);
        return new CBMCalculator.BoxDimensions(side, side, side);
    }
}