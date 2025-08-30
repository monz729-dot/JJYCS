package com.ysc.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private final WebClient webClient;
    
    // 환율 캐시 (통화코드 -> 환율정보)
    private final Map<String, ExchangeRate> rateCache = new ConcurrentHashMap<>();
    
    // 마지막 업데이트 시간
    private LocalDateTime lastUpdateTime;
    
    @Value("${app.api.exchange.key:test-key}")
    private String apiKey;
    
    @Value("${app.api.exchange.enabled:false}")
    private boolean apiEnabled;
    
    @Value("${naver.exchange.api.key:}")
    private String naverApiKey;
    
    @Value("${naver.exchange.api.secret:}")
    private String naverApiSecret;
    
    // 기본 환율 (API 사용 불가시)
    private static final Map<String, BigDecimal> DEFAULT_RATES = Map.of(
        "USD", new BigDecimal("1100.00"),
        "JPY", new BigDecimal("8.50"),
        "CNY", new BigDecimal("160.00"),
        "EUR", new BigDecimal("1300.00"),
        "THB", new BigDecimal("32.00"),
        "VND", new BigDecimal("0.047"),
        "SGD", new BigDecimal("850.00"),
        "HKD", new BigDecimal("140.00")
    );
    
    /**
     * 환율 조회
     */
    public BigDecimal getExchangeRate(String currency) {
        // 캐시에서 먼저 조회
        ExchangeRate cached = rateCache.get(currency.toUpperCase());
        if (cached != null && cached.isValid()) {
            return cached.getRate();
        }
        
        // API 사용 가능한 경우
        if (apiEnabled && apiKey != null && !apiKey.equals("test-key")) {
            try {
                return fetchExchangeRateFromAPI(currency);
            } catch (Exception e) {
                log.error("Failed to fetch exchange rate from API for {}: {}", currency, e.getMessage());
            }
        }
        
        // 기본 환율 반환
        BigDecimal defaultRate = DEFAULT_RATES.get(currency.toUpperCase());
        if (defaultRate != null) {
            log.info("Using default exchange rate for {}: {}", currency, defaultRate);
            cacheRate(currency, defaultRate);
            return defaultRate;
        }
        
        log.warn("No exchange rate found for currency: {}", currency);
        return BigDecimal.ONE;
    }
    
    /**
     * 모든 환율 조회
     */
    public Map<String, BigDecimal> getAllExchangeRates() {
        Map<String, BigDecimal> rates = new HashMap<>();
        
        // 주요 통화들의 환율 조회
        for (String currency : DEFAULT_RATES.keySet()) {
            rates.put(currency, getExchangeRate(currency));
        }
        
        return rates;
    }
    
    /**
     * 통화 변환
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        // KRW로 변환
        BigDecimal krwAmount = amount;
        if (!"KRW".equals(fromCurrency)) {
            BigDecimal fromRate = getExchangeRate(fromCurrency);
            krwAmount = amount.multiply(fromRate);
        }
        
        // 목표 통화로 변환
        if ("KRW".equals(toCurrency)) {
            return krwAmount.setScale(0, RoundingMode.HALF_UP);
        } else {
            BigDecimal toRate = getExchangeRate(toCurrency);
            return krwAmount.divide(toRate, 2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * P0-3: THB -> KRW 환율 조회 (청구일 기준 네이버 환율 적용)
     */
    public BillingExchangeRate getThbToKrwRateForBilling(LocalDateTime billingDate) {
        try {
            log.info("Getting THB to KRW exchange rate for billing date: {}", billingDate);
            
            // 네이버 API 사용 가능한 경우
            if (naverApiKey != null && !naverApiKey.isEmpty()) {
                BigDecimal rate = fetchNaverThbKrwRate(billingDate);
                if (rate != null) {
                    return BillingExchangeRate.success(rate, billingDate, "NAVER");
                }
            }
            
            // 폴백: 기본 THB 환율 사용
            BigDecimal fallbackRate = getExchangeRate("THB");
            log.warn("Using fallback THB-KRW rate for billing: {}", fallbackRate);
            
            return BillingExchangeRate.fallback(fallbackRate, billingDate, "DEFAULT");
            
        } catch (Exception e) {
            log.error("Error getting THB-KRW rate for billing", e);
            return BillingExchangeRate.error("환율 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 청구서용 THB -> KRW 변환 (소수점 둘째 자리까지)
     */
    public BigDecimal convertThbToKrwForBilling(BigDecimal thbAmount, BigDecimal exchangeRate) {
        if (thbAmount == null || exchangeRate == null) {
            return BigDecimal.ZERO;
        }
        
        return thbAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 네이버 API에서 THB-KRW 환율 조회
     */
    private BigDecimal fetchNaverThbKrwRate(LocalDateTime targetDate) {
        try {
            log.info("Fetching THB-KRW rate from Naver API for date: {}", targetDate);
            
            // TODO: 실제 네이버 Open API 연동
            // 현재는 목업 응답 (실제 구현 시 네이버 금융 API 사용)
            
            // 목업 환율 (THB 1 = KRW 38.50 기준)
            BigDecimal mockRate = new BigDecimal("38.50");
            
            // 날짜에 따른 약간의 변동성 시뮬레이션
            int dayVariation = targetDate.getDayOfMonth() % 10;
            BigDecimal variation = new BigDecimal(String.valueOf(dayVariation * 0.1));
            
            return mockRate.add(variation).setScale(4, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            log.error("Error fetching THB-KRW rate from Naver API", e);
            return null;
        }
    }
    
    /**
     * API에서 환율 가져오기
     */
    private BigDecimal fetchExchangeRateFromAPI(String currency) {
        // 실제 API 구현 (한국은행 API 등)
        // 현재는 mock 데이터 반환
        return DEFAULT_RATES.getOrDefault(currency.toUpperCase(), BigDecimal.ONE);
    }
    
    /**
     * 환율 캐싱
     */
    private void cacheRate(String currency, BigDecimal rate) {
        rateCache.put(currency.toUpperCase(), new ExchangeRate(currency, rate));
    }
    
    /**
     * 매일 오전 9시에 환율 업데이트
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void updateExchangeRates() {
        log.info("Updating exchange rates...");
        
        try {
            for (String currency : DEFAULT_RATES.keySet()) {
                BigDecimal rate = fetchExchangeRateFromAPI(currency);
                cacheRate(currency, rate);
            }
            
            lastUpdateTime = LocalDateTime.now();
            log.info("Exchange rates updated successfully at {}", lastUpdateTime);
            
        } catch (Exception e) {
            log.error("Failed to update exchange rates: {}", e.getMessage());
        }
    }
    
    /**
     * 환율 정보 클래스
     */
    private static class ExchangeRate {
        private final String currency;
        private final BigDecimal rate;
        private final LocalDateTime timestamp;
        private static final long CACHE_DURATION_HOURS = 24;
        
        public ExchangeRate(String currency, BigDecimal rate) {
            this.currency = currency;
            this.rate = rate;
            this.timestamp = LocalDateTime.now();
        }
        
        public boolean isValid() {
            return timestamp.plusHours(CACHE_DURATION_HOURS).isAfter(LocalDateTime.now());
        }
        
        public BigDecimal getRate() {
            return rate;
        }
    }
    
    /**
     * 환율 정보 DTO
     */
    public static class ExchangeRateInfo {
        private String currency;
        private String currencyName;
        private BigDecimal rate;
        private String symbol;
        private LocalDateTime updateTime;
        
        // Getters and Setters
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getCurrencyName() { return currencyName; }
        public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
        
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }
    
    /**
     * P0-3: 청구서용 환율 정보 클래스
     */
    public static class BillingExchangeRate {
        private final boolean success;
        private final BigDecimal rate;
        private final LocalDateTime rateDate;
        private final String source;
        private final String message;
        
        private BillingExchangeRate(boolean success, BigDecimal rate, LocalDateTime rateDate, 
                                   String source, String message) {
            this.success = success;
            this.rate = rate;
            this.rateDate = rateDate;
            this.source = source;
            this.message = message;
        }
        
        public static BillingExchangeRate success(BigDecimal rate, LocalDateTime rateDate, String source) {
            return new BillingExchangeRate(true, rate, rateDate, source, "환율 조회 성공");
        }
        
        public static BillingExchangeRate fallback(BigDecimal rate, LocalDateTime rateDate, String source) {
            return new BillingExchangeRate(false, rate, rateDate, source, "폴백 환율 사용");
        }
        
        public static BillingExchangeRate error(String message) {
            return new BillingExchangeRate(false, null, null, "ERROR", message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public BigDecimal getRate() { return rate; }
        public LocalDateTime getRateDate() { return rateDate; }
        public String getSource() { return source; }
        public String getMessage() { return message; }
        
        public boolean isValid() { 
            return rate != null && rate.compareTo(BigDecimal.ZERO) > 0; 
        }
    }
}