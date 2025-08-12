package com.ycs.lms.service;

import com.ycs.lms.util.CBMCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Business Rule Service Tests")
class BusinessRuleServiceTest {

    @Mock
    private CBMCalculator cbmCalculator;

    private BusinessRuleService businessRuleService;

    @BeforeEach
    void setUp() {
        businessRuleService = new BusinessRuleService(cbmCalculator);
    }

    @Test
    @DisplayName("CBM 계산 테스트 - 정상 케이스")
    void shouldCalculateCBMCorrectly() {
        // Given
        BigDecimal width = new BigDecimal("100");
        BigDecimal height = new BigDecimal("100");  
        BigDecimal depth = new BigDecimal("100");
        
        // When
        BigDecimal result = businessRuleService.calculateCBM(width, height, depth);
        
        // Then
        BigDecimal expected = new BigDecimal("1.000000");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("CBM null 값 처리")
    void shouldHandleNullCBMValues() {
        // Given & When
        BigDecimal result1 = businessRuleService.calculateCBM(null, new BigDecimal("100"), new BigDecimal("100"));
        BigDecimal result2 = businessRuleService.calculateCBM(new BigDecimal("100"), null, new BigDecimal("100"));
        BigDecimal result3 = businessRuleService.calculateCBM(new BigDecimal("100"), new BigDecimal("100"), null);
        
        // Then
        assertEquals(BigDecimal.ZERO, result1);
        assertEquals(BigDecimal.ZERO, result2);
        assertEquals(BigDecimal.ZERO, result3);
    }

    @Test
    @DisplayName("CBM 29 초과시 항공 전환 테스트")
    void shouldConvertToAirWhenCBMExceeds29() {
        // Given
        BigDecimal totalCbmOver29 = new BigDecimal("30.5");
        BigDecimal totalCbmUnder29 = new BigDecimal("25.0");
        
        // When & Then
        assertTrue(businessRuleService.shouldConvertToAir(totalCbmOver29));
        assertFalse(businessRuleService.shouldConvertToAir(totalCbmUnder29));
    }

    @Test
    @DisplayName("CBM null값 또는 자동전환 비활성화시 항공전환 안함")
    void shouldNotConvertToAirWhenDisabledOrNull() {
        // Given - BusinessRuleService의 autoAirConversion이 기본 true이므로 별도 설정 필요 없음
        
        // When & Then
        assertFalse(businessRuleService.shouldConvertToAir(null));
    }

    @Test
    @DisplayName("THB 1,500 초과시 추가 수취인 정보 필요")
    void shouldRequireExtraRecipientForTHBOver1500() {
        // Given
        BigDecimal amountOver1500 = new BigDecimal("2000.00");
        BigDecimal amountUnder1500 = new BigDecimal("1000.00");
        String thbCurrency = "THB";
        String otherCurrency = "USD";
        
        // When & Then
        assertTrue(businessRuleService.requiresExtraRecipientInfo(amountOver1500, thbCurrency));
        assertFalse(businessRuleService.requiresExtraRecipientInfo(amountUnder1500, thbCurrency));
        assertFalse(businessRuleService.requiresExtraRecipientInfo(amountOver1500, otherCurrency));
    }

    @Test
    @DisplayName("THB가 아닌 통화는 추가 정보 불필요")
    void shouldNotRequireExtraRecipientForNonTHB() {
        // Given
        BigDecimal largeAmount = new BigDecimal("5000.00");
        
        // When & Then
        assertFalse(businessRuleService.requiresExtraRecipientInfo(largeAmount, "USD"));
        assertFalse(businessRuleService.requiresExtraRecipientInfo(largeAmount, "KRW"));
        assertFalse(businessRuleService.requiresExtraRecipientInfo(largeAmount, null));
    }

    @Test
    @DisplayName("THB null 금액 처리")
    void shouldHandleNullTHBAmount() {
        // When & Then
        assertFalse(businessRuleService.requiresExtraRecipientInfo(null, "THB"));
    }

    @Test
    @DisplayName("회원코드 미기재시 지연 처리")
    void shouldDelayProcessingWhenMemberCodeMissing() {
        // When & Then
        assertTrue(businessRuleService.hasDelayedProcessing(null));
        assertTrue(businessRuleService.hasDelayedProcessing(""));
        assertTrue(businessRuleService.hasDelayedProcessing("   "));
        assertFalse(businessRuleService.hasDelayedProcessing("MEMBER001"));
    }

    @Test
    @DisplayName("주문 상태 결정 - 회원코드 지연")
    void shouldDetermineOrderStatusForMemberCodeIssue() {
        // Given
        String noMemberCode = null;
        String validMemberCode = "MEMBER001";
        boolean isApproved = true;
        boolean isNotApproved = false;
        
        // When & Then
        assertEquals("delayed", businessRuleService.determineOrderStatus(noMemberCode, isApproved));
        assertEquals("pending_approval", businessRuleService.determineOrderStatus(validMemberCode, isNotApproved));
        assertEquals("requested", businessRuleService.determineOrderStatus(validMemberCode, isApproved));
    }

    @Test
    @DisplayName("배송 방식 결정 - CBM 기준")
    void shouldDetermineShippingTypeBasedOnCBM() {
        // Given
        BigDecimal cbmOver29 = new BigDecimal("32.0");
        BigDecimal cbmUnder29 = new BigDecimal("25.0");
        String preferredSea = "sea";
        String preferredAir = "air";
        
        // When & Then
        assertEquals("air", businessRuleService.determineShippingType(cbmOver29, preferredSea));
        assertEquals("air", businessRuleService.determineShippingType(cbmOver29, preferredAir));
        assertEquals("sea", businessRuleService.determineShippingType(cbmUnder29, preferredSea));
        assertEquals("air", businessRuleService.determineShippingType(cbmUnder29, preferredAir));
    }

    @Test
    @DisplayName("배송 방식 결정 - 선호 타입이 null인 경우")
    void shouldDefaultToSeaWhenPreferredTypeIsNull() {
        // Given
        BigDecimal cbmUnder29 = new BigDecimal("25.0");
        
        // When
        String result = businessRuleService.determineShippingType(cbmUnder29, null);
        
        // Then
        assertEquals("sea", result);
    }

    @Test
    @DisplayName("주문 검증 - 모든 규칙 통과")
    void shouldPassAllValidationRules() {
        // Given
        BigDecimal totalCbm = new BigDecimal("20.0");
        BigDecimal totalAmount = new BigDecimal("1000.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApprovedUser = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertFalse(result.isConvertToAir());
        assertFalse(result.isRequiresExtraRecipient());
        assertFalse(result.isHasDelayedProcessing());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("주문 검증 - CBM 초과 경고")
    void shouldTriggerCBMExceedWarning() {
        // Given
        BigDecimal totalCbm = new BigDecimal("35.0");
        BigDecimal totalAmount = new BigDecimal("1000.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApprovedUser = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertTrue(result.isConvertToAir());
        assertFalse(result.isRequiresExtraRecipient());
        assertFalse(result.isHasDelayedProcessing());
        assertEquals(1, result.getWarnings().size());
        assertEquals("CBM_EXCEEDED", result.getWarnings().get(0).getType());
        assertTrue(result.getWarnings().get(0).getMessage().contains("항공 배송"));
    }

    @Test
    @DisplayName("주문 검증 - THB 1,500 초과 경고")
    void shouldTriggerTHBExceedWarning() {
        // Given
        BigDecimal totalCbm = new BigDecimal("20.0");
        BigDecimal totalAmount = new BigDecimal("2500.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApprovedUser = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertFalse(result.isConvertToAir());
        assertTrue(result.isRequiresExtraRecipient());
        assertFalse(result.isHasDelayedProcessing());
        assertEquals(1, result.getWarnings().size());
        assertEquals("AMOUNT_EXCEEDED_THB_1500", result.getWarnings().get(0).getType());
        assertTrue(result.getWarnings().get(0).getMessage().contains("수취인 추가 정보"));
    }

    @Test
    @DisplayName("주문 검증 - 회원코드 누락 경고")
    void shouldTriggerMemberCodeMissingWarning() {
        // Given
        BigDecimal totalCbm = new BigDecimal("20.0");
        BigDecimal totalAmount = new BigDecimal("1000.0");
        String currency = "THB";
        String memberCode = null;
        boolean isApprovedUser = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertFalse(result.isConvertToAir());
        assertFalse(result.isRequiresExtraRecipient());
        assertTrue(result.isHasDelayedProcessing());
        assertEquals(1, result.getWarnings().size());
        assertEquals("MEMBER_CODE_REQUIRED", result.getWarnings().get(0).getType());
        assertTrue(result.getWarnings().get(0).getMessage().contains("지연 처리"));
    }

    @Test
    @DisplayName("주문 검증 - 승인 대기 경고")
    void shouldTriggerApprovalRequiredWarning() {
        // Given
        BigDecimal totalCbm = new BigDecimal("20.0");
        BigDecimal totalAmount = new BigDecimal("1000.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApprovedUser = false;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertFalse(result.isConvertToAir());
        assertFalse(result.isRequiresExtraRecipient());
        assertFalse(result.isHasDelayedProcessing());
        assertEquals(1, result.getWarnings().size());
        assertEquals("APPROVAL_REQUIRED", result.getWarnings().get(0).getType());
        assertTrue(result.getWarnings().get(0).getMessage().contains("승인"));
    }

    @Test
    @DisplayName("주문 검증 - 모든 경고 발생")
    void shouldTriggerAllWarnings() {
        // Given
        BigDecimal totalCbm = new BigDecimal("35.0");      // CBM 초과
        BigDecimal totalAmount = new BigDecimal("2500.0");  // THB 1,500 초과
        String currency = "THB";
        String memberCode = null;                           // 회원코드 누락
        boolean isApprovedUser = false;                     // 승인 안됨
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            totalCbm, totalAmount, currency, memberCode, isApprovedUser
        );
        
        // Then
        assertTrue(result.isConvertToAir());
        assertTrue(result.isRequiresExtraRecipient());
        assertTrue(result.isHasDelayedProcessing());
        assertEquals(4, result.getWarnings().size());
        
        // 경고 타입 확인
        boolean hasCBMWarning = result.getWarnings().stream()
            .anyMatch(w -> "CBM_EXCEEDED".equals(w.getType()));
        boolean hasAmountWarning = result.getWarnings().stream()
            .anyMatch(w -> "AMOUNT_EXCEEDED_THB_1500".equals(w.getType()));
        boolean hasMemberCodeWarning = result.getWarnings().stream()
            .anyMatch(w -> "MEMBER_CODE_REQUIRED".equals(w.getType()));
        boolean hasApprovalWarning = result.getWarnings().stream()
            .anyMatch(w -> "APPROVAL_REQUIRED".equals(w.getType()));
        
        assertTrue(hasCBMWarning);
        assertTrue(hasAmountWarning);
        assertTrue(hasMemberCodeWarning);
        assertTrue(hasApprovalWarning);
    }

    @Test
    @DisplayName("경계값 테스트 - CBM 정확히 29.0")
    void shouldNotExceedAtExactCBMThreshold() {
        // Given
        BigDecimal exactThreshold = new BigDecimal("29.0");
        BigDecimal amount = new BigDecimal("1000.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApproved = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            exactThreshold, amount, currency, memberCode, isApproved
        );
        
        // Then
        assertFalse(result.isConvertToAir(), "정확히 29.0 m³는 초과하지 않음");
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("경계값 테스트 - THB 정확히 1500.0")
    void shouldNotExceedAtExactTHBThreshold() {
        // Given
        BigDecimal cbm = new BigDecimal("20.0");
        BigDecimal exactThreshold = new BigDecimal("1500.0");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApproved = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            cbm, exactThreshold, currency, memberCode, isApproved
        );
        
        // Then
        assertFalse(result.isRequiresExtraRecipient(), "정확히 1500.0 THB는 초과하지 않음");
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("경계값 테스트 - 미세한 초과값")
    void shouldDetectMinimalExceedance() {
        // Given
        BigDecimal cbmSlightlyOver = new BigDecimal("29.000001");
        BigDecimal thbSlightlyOver = new BigDecimal("1500.01");
        String currency = "THB";
        String memberCode = "MEMBER001";
        boolean isApproved = true;
        
        // When
        BusinessRuleService.OrderValidationResult result = businessRuleService.validateOrder(
            cbmSlightlyOver, thbSlightlyOver, currency, memberCode, isApproved
        );
        
        // Then
        assertTrue(result.isConvertToAir(), "미세하게라도 초과하면 항공 전환");
        assertTrue(result.isRequiresExtraRecipient(), "미세하게라도 초과하면 추가 정보 필요");
        assertEquals(2, result.getWarnings().size());
    }
}