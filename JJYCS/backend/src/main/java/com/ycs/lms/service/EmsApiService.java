package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * EMS API 서비스 - 태국 전용 기능
 * - 우편번호 검색 및 검증
 * - HS 코드 검색 (HD API 실패 시 폴백)
 * - EMS 신청내역 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmsApiService {
    
    private final WebClient webClient;
    
    @Value("${app.api.ems.base-url:https://ems.epost.go.kr/api}")
    private String emsBaseUrl;
    
    @Value("${app.api.ems.api-key:}")
    private String emsApiKey;
    
    @Value("${app.api.ems.enabled:true}")
    private boolean emsEnabled;
    
    @Value("${app.api.ems.timeout-seconds:10}")
    private int timeoutSeconds;
    
    @Value("${app.api.ems.retry-count:3}")
    private int retryCount;

    /**
     * 태국 우편번호 검색 및 검증
     * @param query 검색어 (우편번호 일부 또는 전체)
     * @return 우편번호 목록
     */
    @Cacheable(value = "ems-postal-codes", key = "#query", unless = "#result == null or #result.isEmpty()")
    public List<PostalCodeResult> searchPostalCode(String query) {
        if (!emsEnabled || emsApiKey == null || emsApiKey.trim().isEmpty()) {
            log.warn("EMS API not configured, returning fallback postal codes");
            return getFallbackPostalCodes(query);
        }
        
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            log.info("Searching Thailand postal codes with query: {}", query);
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("ems.epost.go.kr")
                    .path("/api/postal/search")
                    .queryParam("apiKey", emsApiKey)
                    .queryParam("country", "TH")
                    .queryParam("query", query.trim())
                    .queryParam("limit", 10)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(Retry.backoff(retryCount, Duration.ofMillis(500)))
                .block();
                
            return parsePostalCodeResponse(response);
            
        } catch (WebClientResponseException e) {
            log.error("EMS postal code API error (status {}): {}", e.getStatusCode(), e.getMessage());
            return getFallbackPostalCodes(query);
        } catch (Exception e) {
            log.error("EMS postal code search error: {}", e.getMessage());
            return getFallbackPostalCodes(query);
        }
    }

    /**
     * HS 코드 검색 (HD API 폴백용)
     * @param keyword 검색 키워드
     * @return HS 코드 목록
     */
    public List<HsCodeResult> searchHsCode(String keyword) {
        if (!emsEnabled || emsApiKey == null || emsApiKey.trim().isEmpty()) {
            log.warn("EMS API not configured for HS code search");
            return Collections.emptyList();
        }
        
        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }
        
        try {
            log.info("Searching HS codes with keyword: {}", keyword);
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("ems.epost.go.kr")
                    .path("/api/hscode/search")
                    .queryParam("apiKey", emsApiKey)
                    .queryParam("keyword", keyword.trim())
                    .queryParam("country", "TH")
                    .queryParam("limit", 20)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(Retry.backoff(retryCount, Duration.ofMillis(500)))
                .block();
                
            return parseHsCodeResponse(response);
            
        } catch (WebClientResponseException e) {
            log.error("EMS HS code API error (status {}): {}", e.getStatusCode(), e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("EMS HS code search error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * EMS 신청내역 조회 (주문 상세 페이지용)
     * @param emsNumber EMS 번호
     * @param shipperName 발송인명
     * @return EMS 신청내역
     */
    @Cacheable(value = "ems-applications", key = "#emsNumber", unless = "#result == null")
    public EmsApplicationResult getEmsApplication(String emsNumber, String shipperName) {
        if (!emsEnabled || emsApiKey == null || emsApiKey.trim().isEmpty()) {
            log.warn("EMS API not configured for application lookup");
            return null;
        }
        
        try {
            log.info("Looking up EMS application: {}", emsNumber);
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("ems.epost.go.kr")
                    .path("/api/application/lookup")
                    .queryParam("apiKey", emsApiKey)
                    .queryParam("emsNumber", emsNumber)
                    .queryParam("shipperName", shipperName)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(Retry.backoff(retryCount, Duration.ofMillis(500)))
                .block();
                
            return parseEmsApplicationResponse(response);
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("EMS application not found: {}", emsNumber);
                return null;
            }
            log.error("EMS application API error (status {}): {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("EMS application lookup error: {}", e.getMessage());
            return null;
        }
    }

    // 폴백 우편번호 데이터 (태국 주요 도시)
    private List<PostalCodeResult> getFallbackPostalCodes(String query) {
        List<PostalCodeResult> fallbackCodes = Arrays.asList(
            new PostalCodeResult("10110", "Bangkok", "Pathum Wan"),
            new PostalCodeResult("10120", "Bangkok", "Dusit"),
            new PostalCodeResult("10200", "Bangkok", "Bang Rak"),
            new PostalCodeResult("20150", "Chonburi", "Mueang Chonburi"),
            new PostalCodeResult("50100", "Chiang Mai", "Mueang Chiang Mai"),
            new PostalCodeResult("80000", "Nakhon Si Thammarat", "Mueang Nakhon Si Thammarat"),
            new PostalCodeResult("90110", "Songkhla", "Hat Yai")
        );
        
        if (query == null || query.trim().isEmpty()) {
            return fallbackCodes;
        }
        
        String searchQuery = query.toLowerCase().trim();
        return fallbackCodes.stream()
            .filter(code -> 
                code.getPostalCode().contains(searchQuery) ||
                code.getCity().toLowerCase().contains(searchQuery) ||
                code.getDistrict().toLowerCase().contains(searchQuery))
            .toList();
    }

    private List<PostalCodeResult> parsePostalCodeResponse(String response) {
        // TODO: 실제 EMS API 응답 파싱 구현
        // 현재는 Mock 응답 처리
        log.debug("Parsing EMS postal code response: {}", response);
        return Collections.emptyList();
    }

    private List<HsCodeResult> parseHsCodeResponse(String response) {
        // TODO: 실제 EMS API 응답 파싱 구현
        // 현재는 Mock 응답 처리
        log.debug("Parsing EMS HS code response: {}", response);
        return Collections.emptyList();
    }

    private EmsApplicationResult parseEmsApplicationResponse(String response) {
        // TODO: 실제 EMS API 응답 파싱 구현
        // 현재는 Mock 응답 처리
        log.debug("Parsing EMS application response: {}", response);
        return null;
    }

    // DTO 클래스들
    public static class PostalCodeResult {
        private String postalCode;
        private String city;
        private String district;

        public PostalCodeResult(String postalCode, String city, String district) {
            this.postalCode = postalCode;
            this.city = city;
            this.district = district;
        }

        // Getters
        public String getPostalCode() { return postalCode; }
        public String getCity() { return city; }
        public String getDistrict() { return district; }
    }

    public static class HsCodeResult {
        private String hsCode;
        private String description;
        private String category;

        public HsCodeResult(String hsCode, String description, String category) {
            this.hsCode = hsCode;
            this.description = description;
            this.category = category;
        }

        // Getters
        public String getHsCode() { return hsCode; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
    }

    public static class EmsApplicationResult {
        private String emsNumber;
        private String status;
        private String applicationDate;
        private Map<String, Object> customsInfo;

        public EmsApplicationResult(String emsNumber, String status, String applicationDate) {
            this.emsNumber = emsNumber;
            this.status = status;
            this.applicationDate = applicationDate;
            this.customsInfo = new HashMap<>();
        }

        // Getters and Setters
        public String getEmsNumber() { return emsNumber; }
        public String getStatus() { return status; }
        public String getApplicationDate() { return applicationDate; }
        public Map<String, Object> getCustomsInfo() { return customsInfo; }
        public void setCustomsInfo(Map<String, Object> customsInfo) { this.customsInfo = customsInfo; }
    }
}