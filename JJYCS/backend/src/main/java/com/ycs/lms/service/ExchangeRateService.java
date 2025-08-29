package com.ycs.lms.service;

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
}