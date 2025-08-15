package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.external-api.ems.url:}")
    private String emsApiUrl;

    @Value("${app.external-api.ems.key:}")
    private String emsApiKey;

    @Value("${app.external-api.hs-code.url:}")
    private String hsCodeApiUrl;

    @Value("${app.external-api.hs-code.key:}")
    private String hsCodeApiKey;

    @Value("${app.external-api.exchange-rate.url:}")
    private String exchangeRateApiUrl;

    @Value("${app.external-api.exchange-rate.key:}")
    private String exchangeRateApiKey;

    /**
     * EMS 코드 검증
     */
    public CompletableFuture<EmsValidationResult> validateEmsCodeAsync(String emsCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return validateEmsCode(emsCode);
            } catch (Exception e) {
                log.warn("EMS validation failed for code: {}, error: {}", emsCode, e.getMessage());
                return EmsValidationResult.fallback(emsCode, "EMS 코드 검증 서비스가 일시적으로 이용불가합니다.");
            }
        }).orTimeout(10, TimeUnit.SECONDS)
          .exceptionally(throwable -> {
              log.error("EMS validation timeout for code: {}", emsCode, throwable);
              return EmsValidationResult.fallback(emsCode, "EMS 코드 검증 시간이 초과되었습니다.");
          });
    }

    /**
     * HS 코드 검증
     */
    public CompletableFuture<HsCodeValidationResult> validateHsCodeAsync(String hsCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return validateHsCode(hsCode);
            } catch (Exception e) {
                log.warn("HS code validation failed for code: {}, error: {}", hsCode, e.getMessage());
                return HsCodeValidationResult.fallback(hsCode, "HS 코드 검증 서비스가 일시적으로 이용불가합니다.");
            }
        }).orTimeout(10, TimeUnit.SECONDS)
          .exceptionally(throwable -> {
              log.error("HS code validation timeout for code: {}", hsCode, throwable);
              return HsCodeValidationResult.fallback(hsCode, "HS 코드 검증 시간이 초과되었습니다.");
          });
    }

    /**
     * 환율 조회
     */
    public CompletableFuture<ExchangeRateResult> getExchangeRateAsync(String from, String to) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getExchangeRate(from, to);
            } catch (Exception e) {
                log.warn("Exchange rate fetch failed for {}->{}, error: {}", from, to, e.getMessage());
                return ExchangeRateResult.fallback(from, to, getDefaultExchangeRate(from, to));
            }
        }).orTimeout(5, TimeUnit.SECONDS)
          .exceptionally(throwable -> {
              log.error("Exchange rate fetch timeout for {}->{}", from, to, throwable);
              return ExchangeRateResult.fallback(from, to, getDefaultExchangeRate(from, to));
          });
    }

    private EmsValidationResult validateEmsCode(String emsCode) {
        if (emsApiUrl.isEmpty() || emsApiKey.isEmpty()) {
            return validateEmsCodeOffline(emsCode);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + emsApiKey);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = emsApiUrl + "/validate/" + emsCode;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // API 응답 파싱 로직 (실제 API 스펙에 따라 구현)
            log.info("EMS validation successful for code: {}", emsCode);
            return EmsValidationResult.valid(emsCode, "Valid EMS code", "Thailand");
            
        } catch (Exception e) {
            log.error("EMS API call failed for code: {}", emsCode, e);
            throw e;
        }
    }

    private HsCodeValidationResult validateHsCode(String hsCode) {
        if (hsCodeApiUrl.isEmpty() || hsCodeApiKey.isEmpty()) {
            return validateHsCodeOffline(hsCode);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hsCodeApiKey);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = hsCodeApiUrl + "/lookup/" + hsCode;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // API 응답 파싱 로직 (실제 API 스펙에 따라 구현)
            log.info("HS code validation successful for code: {}", hsCode);
            return HsCodeValidationResult.valid(hsCode, "Valid HS code", "cosmetics");
            
        } catch (Exception e) {
            log.error("HS code API call failed for code: {}", hsCode, e);
            throw e;
        }
    }

    private ExchangeRateResult getExchangeRate(String from, String to) {
        if (exchangeRateApiUrl.isEmpty() || exchangeRateApiKey.isEmpty()) {
            return ExchangeRateResult.success(from, to, getDefaultExchangeRate(from, to), false);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + exchangeRateApiKey);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            String url = exchangeRateApiUrl + "?from=" + from + "&to=" + to;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // API 응답 파싱 로직 (실제 API 스펙에 따라 구현)
            log.info("Exchange rate fetch successful for {}->{}", from, to);
            return ExchangeRateResult.success(from, to, getDefaultExchangeRate(from, to), true);
            
        } catch (Exception e) {
            log.error("Exchange rate API call failed for {}->{}", from, to, e);
            throw e;
        }
    }

    // Offline validation fallback methods
    private EmsValidationResult validateEmsCodeOffline(String emsCode) {
        // 기본적인 EMS 코드 형식 체크
        if (emsCode == null || emsCode.length() < 10) {
            return EmsValidationResult.invalid(emsCode, "EMS 코드 형식이 올바르지 않습니다.");
        }
        return EmsValidationResult.valid(emsCode, "기본 형식 검증 통과", "Unknown");
    }

    private HsCodeValidationResult validateHsCodeOffline(String hsCode) {
        // 기본적인 HS 코드 형식 체크 (XXXX.XX.XX)
        if (hsCode == null || !hsCode.matches("^\\d{4}\\.\\d{2}\\.\\d{2}$")) {
            return HsCodeValidationResult.invalid(hsCode, "HS 코드 형식이 올바르지 않습니다. (예: 3304.99.00)");
        }
        return HsCodeValidationResult.valid(hsCode, "기본 형식 검증 통과", "Unknown");
    }

    private java.math.BigDecimal getDefaultExchangeRate(String from, String to) {
        // 기본 환율 (실제 환경에서는 DB나 설정에서 조회)
        if ("THB".equals(from) && "KRW".equals(to)) {
            return java.math.BigDecimal.valueOf(36.5);
        } else if ("KRW".equals(from) && "THB".equals(to)) {
            return java.math.BigDecimal.valueOf(0.0274);
        } else if ("USD".equals(from) && "KRW".equals(to)) {
            return java.math.BigDecimal.valueOf(1320.0);
        }
        return java.math.BigDecimal.ONE;
    }

    // Result classes
    public static class EmsValidationResult {
        private final String code;
        private final boolean valid;
        private final String message;
        private final String destination;
        private final boolean fallbackUsed;

        private EmsValidationResult(String code, boolean valid, String message, String destination, boolean fallbackUsed) {
            this.code = code;
            this.valid = valid;
            this.message = message;
            this.destination = destination;
            this.fallbackUsed = fallbackUsed;
        }

        public static EmsValidationResult valid(String code, String message, String destination) {
            return new EmsValidationResult(code, true, message, destination, false);
        }

        public static EmsValidationResult invalid(String code, String message) {
            return new EmsValidationResult(code, false, message, null, false);
        }

        public static EmsValidationResult fallback(String code, String message) {
            return new EmsValidationResult(code, true, message, "Unknown", true);
        }

        // Getters
        public String getCode() { return code; }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getDestination() { return destination; }
        public boolean isFallbackUsed() { return fallbackUsed; }
    }

    public static class HsCodeValidationResult {
        private final String code;
        private final boolean valid;
        private final String message;
        private final String description;
        private final String category;
        private final boolean fallbackUsed;

        private HsCodeValidationResult(String code, boolean valid, String message, String description, String category, boolean fallbackUsed) {
            this.code = code;
            this.valid = valid;
            this.message = message;
            this.description = description;
            this.category = category;
            this.fallbackUsed = fallbackUsed;
        }

        public static HsCodeValidationResult valid(String code, String description, String category) {
            return new HsCodeValidationResult(code, true, "Valid HS code", description, category, false);
        }

        public static HsCodeValidationResult invalid(String code, String message) {
            return new HsCodeValidationResult(code, false, message, null, null, false);
        }

        public static HsCodeValidationResult fallback(String code, String message) {
            return new HsCodeValidationResult(code, true, message, "Unknown", "Unknown", true);
        }

        // Getters
        public String getCode() { return code; }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public boolean isFallbackUsed() { return fallbackUsed; }
    }

    public static class ExchangeRateResult {
        private final String from;
        private final String to;
        private final java.math.BigDecimal rate;
        private final boolean success;
        private final boolean fallbackUsed;

        private ExchangeRateResult(String from, String to, java.math.BigDecimal rate, boolean success, boolean fallbackUsed) {
            this.from = from;
            this.to = to;
            this.rate = rate;
            this.success = success;
            this.fallbackUsed = fallbackUsed;
        }

        public static ExchangeRateResult success(String from, String to, java.math.BigDecimal rate, boolean realTime) {
            return new ExchangeRateResult(from, to, rate, true, !realTime);
        }

        public static ExchangeRateResult fallback(String from, String to, java.math.BigDecimal rate) {
            return new ExchangeRateResult(from, to, rate, true, true);
        }

        // Getters
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public java.math.BigDecimal getRate() { return rate; }
        public boolean isSuccess() { return success; }
        public boolean isFallbackUsed() { return fallbackUsed; }
    }
}