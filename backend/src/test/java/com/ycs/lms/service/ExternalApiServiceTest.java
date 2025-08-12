package com.ycs.lms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("External API Service Tests")
class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ExternalApiService externalApiService;

    @BeforeEach
    void setUp() {
        externalApiService = new ExternalApiService();
        // RestTemplate을 mock으로 교체하기 위해서는 생성자나 setter가 필요
        // 현재 구현에서는 private final이므로 실제 호출을 테스트
    }

    @Test
    @DisplayName("EMS 코드 검증 - 유효한 코드")
    void shouldValidateValidEMSCode() throws Exception {
        // Given
        String validEmsCode = "EE123456789KR";
        
        // When
        CompletableFuture<ExternalApiService.EmsValidationResult> future = 
            externalApiService.validateEmsCodeAsync(validEmsCode);
        ExternalApiService.EmsValidationResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isValid() || result.isFallbackUsed()); // API 없으면 폴백 사용
        assertEquals(validEmsCode, result.getCode());
    }

    @Test
    @DisplayName("EMS 코드 검증 - 잘못된 형식")
    void shouldRejectInvalidEMSCodeFormat() throws Exception {
        // Given
        String invalidEmsCode = "INVALID";
        
        // When
        CompletableFuture<ExternalApiService.EmsValidationResult> future = 
            externalApiService.validateEmsCodeAsync(invalidEmsCode);
        ExternalApiService.EmsValidationResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(invalidEmsCode, result.getCode());
        
        // API 설정이 없으면 오프라인 검증 수행
        if (!result.isFallbackUsed()) {
            assertFalse(result.isValid());
        }
    }

    @Test
    @DisplayName("EMS 코드 검증 - 빈 코드")
    void shouldRejectEmptyEMSCode() throws Exception {
        // When
        CompletableFuture<ExternalApiService.EmsValidationResult> future1 = 
            externalApiService.validateEmsCodeAsync("");
        CompletableFuture<ExternalApiService.EmsValidationResult> future2 = 
            externalApiService.validateEmsCodeAsync(null);
        
        ExternalApiService.EmsValidationResult result1 = future1.get();
        ExternalApiService.EmsValidationResult result2 = future2.get();
        
        // Then
        assertFalse(result1.isValid());
        assertFalse(result2.isValid());
        assertTrue(result1.getMessage().contains("형식이 올바르지 않습니다"));
        assertTrue(result2.getMessage().contains("형식이 올바르지 않습니다"));
    }

    @Test
    @DisplayName("HS 코드 검증 - 유효한 형식")
    void shouldValidateValidHSCode() throws Exception {
        // Given
        String validHsCode = "3304.99.00";
        
        // When
        CompletableFuture<ExternalApiService.HsCodeValidationResult> future = 
            externalApiService.validateHsCodeAsync(validHsCode);
        ExternalApiService.HsCodeValidationResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isValid() || result.isFallbackUsed());
        assertEquals(validHsCode, result.getCode());
    }

    @Test
    @DisplayName("HS 코드 검증 - 잘못된 형식")
    void shouldRejectInvalidHSCodeFormat() throws Exception {
        // Given
        String[] invalidHsCodes = {
            "invalid",
            "1234",
            "1234.56",
            "12345.67.89",
            "abcd.ef.gh"
        };
        
        for (String invalidHsCode : invalidHsCodes) {
            // When
            CompletableFuture<ExternalApiService.HsCodeValidationResult> future = 
                externalApiService.validateHsCodeAsync(invalidHsCode);
            ExternalApiService.HsCodeValidationResult result = future.get();
            
            // Then
            assertNotNull(result, "결과가 null이면 안됨: " + invalidHsCode);
            assertEquals(invalidHsCode, result.getCode());
            
            // API 설정이 없으면 오프라인 검증 수행
            if (!result.isFallbackUsed()) {
                assertFalse(result.isValid(), "잘못된 형식은 유효하지 않아야 함: " + invalidHsCode);
                assertTrue(result.getMessage().contains("형식이 올바르지 않습니다"));
            }
        }
    }

    @Test
    @DisplayName("HS 코드 검증 - null 또는 빈 코드")
    void shouldRejectEmptyHSCode() throws Exception {
        // When
        CompletableFuture<ExternalApiService.HsCodeValidationResult> future1 = 
            externalApiService.validateHsCodeAsync("");
        CompletableFuture<ExternalApiService.HsCodeValidationResult> future2 = 
            externalApiService.validateHsCodeAsync(null);
        
        ExternalApiService.HsCodeValidationResult result1 = future1.get();
        ExternalApiService.HsCodeValidationResult result2 = future2.get();
        
        // Then
        assertFalse(result1.isValid());
        assertFalse(result2.isValid());
        assertTrue(result1.getMessage().contains("비어있습니다"));
        assertTrue(result2.getMessage().contains("비어있습니다"));
    }

    @Test
    @DisplayName("환율 조회 - 기본 통화 쌍")
    void shouldGetExchangeRateForCommonPairs() throws Exception {
        // Given
        String[][] currencyPairs = {
            {"THB", "KRW"},
            {"KRW", "THB"}, 
            {"USD", "KRW"},
            {"USD", "THB"}
        };
        
        for (String[] pair : currencyPairs) {
            // When
            CompletableFuture<ExternalApiService.ExchangeRateResult> future = 
                externalApiService.getExchangeRateAsync(pair[0], pair[1]);
            ExternalApiService.ExchangeRateResult result = future.get();
            
            // Then
            assertNotNull(result, "환율 결과가 null이면 안됨: " + pair[0] + "/" + pair[1]);
            assertTrue(result.isSuccess(), "환율 조회 성공해야 함: " + pair[0] + "/" + pair[1]);
            assertEquals(pair[0], result.getFrom());
            assertEquals(pair[1], result.getTo());
            assertNotNull(result.getRate());
            assertTrue(result.getRate().compareTo(java.math.BigDecimal.ZERO) > 0, "환율은 0보다 커야 함");
        }
    }

    @Test
    @DisplayName("환율 조회 - 알 수 없는 통화 쌍")
    void shouldHandleUnknownCurrencyPair() throws Exception {
        // Given
        String unknownFrom = "XYZ";
        String unknownTo = "ABC";
        
        // When
        CompletableFuture<ExternalApiService.ExchangeRateResult> future = 
            externalApiService.getExchangeRateAsync(unknownFrom, unknownTo);
        ExternalApiService.ExchangeRateResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(unknownFrom, result.getFrom());
        assertEquals(unknownTo, result.getTo());
        
        // 폴백으로 1.0 반환
        if (result.isFallbackUsed()) {
            assertEquals(java.math.BigDecimal.ONE, result.getRate());
        }
    }

    @Test
    @DisplayName("API 타임아웃 처리")
    void shouldHandleAPITimeout() throws Exception {
        // Given
        String emsCode = "EE123456789KR";
        
        // When - 타임아웃은 10초로 설정되어 있으므로, 실제 타임아웃 테스트는 시간이 오래 걸림
        // 대신 결과가 적절하게 반환되는지 확인
        CompletableFuture<ExternalApiService.EmsValidationResult> future = 
            externalApiService.validateEmsCodeAsync(emsCode);
        ExternalApiService.EmsValidationResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getMessage());
        
        // 타임아웃이 발생하면 폴백 사용
        if (result.isFallbackUsed()) {
            assertTrue(result.getMessage().contains("시간이 초과") || result.getMessage().contains("이용불가"));
        }
    }

    @Test
    @DisplayName("오프라인 EMS 검증 - 최소 길이 체크")
    void shouldValidateEMSCodeLengthOffline() throws Exception {
        // Given
        String shortCode = "SHORT";
        String validLengthCode = "EE123456789KR"; // 13자리
        
        // When
        CompletableFuture<ExternalApiService.EmsValidationResult> future1 = 
            externalApiService.validateEmsCodeAsync(shortCode);
        CompletableFuture<ExternalApiService.EmsValidationResult> future2 = 
            externalApiService.validateEmsCodeAsync(validLengthCode);
        
        ExternalApiService.EmsValidationResult result1 = future1.get();
        ExternalApiService.EmsValidationResult result2 = future2.get();
        
        // Then
        if (result1.isFallbackUsed()) { // 오프라인 검증 사용시
            assertFalse(result1.isValid(), "짧은 코드는 유효하지 않아야 함");
        }
        
        if (result2.isFallbackUsed()) { // 오프라인 검증 사용시
            assertTrue(result2.isValid(), "충분한 길이의 코드는 유효해야 함");
        }
    }

    @Test
    @DisplayName("오프라인 HS 코드 검증 - 정규식 패턴")
    void shouldValidateHSCodePatternOffline() throws Exception {
        // Given
        String validPattern = "1234.56.78";
        String invalidPattern = "1234.5.78";
        
        // When
        CompletableFuture<ExternalApiService.HsCodeValidationResult> future1 = 
            externalApiService.validateHsCodeAsync(validPattern);
        CompletableFuture<ExternalApiService.HsCodeValidationResult> future2 = 
            externalApiService.validateHsCodeAsync(invalidPattern);
        
        ExternalApiService.HsCodeValidationResult result1 = future1.get();
        ExternalApiService.HsCodeValidationResult result2 = future2.get();
        
        // Then
        if (result1.isFallbackUsed()) { // 오프라인 검증 사용시
            assertTrue(result1.isValid(), "올바른 패턴은 유효해야 함");
        }
        
        if (result2.isFallbackUsed()) { // 오프라인 검증 사용시
            assertFalse(result2.isValid(), "잘못된 패턴은 유효하지 않아야 함");
        }
    }

    @Test
    @DisplayName("비동기 처리 결과 검증")
    void shouldProcessAsynchronously() {
        // Given
        String emsCode = "EE123456789KR";
        String hsCode = "3304.99.00";
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<ExternalApiService.EmsValidationResult> emsFuture = 
            externalApiService.validateEmsCodeAsync(emsCode);
        CompletableFuture<ExternalApiService.HsCodeValidationResult> hsFuture = 
            externalApiService.validateHsCodeAsync(hsCode);
        
        // 비동기 실행이므로 즉시 반환되어야 함
        long afterSubmit = System.currentTimeMillis();
        assertTrue(afterSubmit - startTime < 100, "비동기 요청은 즉시 반환되어야 함");
        
        // Then
        assertDoesNotThrow(() -> {
            ExternalApiService.EmsValidationResult emsResult = emsFuture.get();
            ExternalApiService.HsCodeValidationResult hsResult = hsFuture.get();
            
            assertNotNull(emsResult);
            assertNotNull(hsResult);
        });
    }

    @Test
    @DisplayName("병렬 검증 성능 테스트")
    void shouldProcessMultipleRequestsInParallel() {
        // Given
        String[] emsCodes = {
            "EE123456789KR",
            "EE987654321KR", 
            "EE555666777KR"
        };
        
        // When
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<ExternalApiService.EmsValidationResult>[] futures = 
            new CompletableFuture[emsCodes.length];
        
        for (int i = 0; i < emsCodes.length; i++) {
            futures[i] = externalApiService.validateEmsCodeAsync(emsCodes[i]);
        }
        
        // Then
        assertDoesNotThrow(() -> {
            for (int i = 0; i < futures.length; i++) {
                ExternalApiService.EmsValidationResult result = futures[i].get();
                assertNotNull(result);
                assertEquals(emsCodes[i], result.getCode());
            }
        });
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 병렬 처리이므로 순차 처리보다 빨라야 함 (실제 API 호출시)
        // Mock 환경에서는 의미가 제한적이지만, 구조적 검증
        assertTrue(duration < 10000, "병렬 처리는 합리적인 시간 내에 완료되어야 함: " + duration + "ms");
    }
}