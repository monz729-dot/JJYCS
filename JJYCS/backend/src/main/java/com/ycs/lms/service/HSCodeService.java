package com.ycs.lms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeoutException;
import java.time.Duration;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class HSCodeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // TTL 캐시를 위한 맵
    private final Map<String, CacheEntry<HSCodeSearchResponse>> hsCodeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<TariffRateResponse>> tariffCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<TariffExchangeResponse>> exchangeCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 3600000; // 1시간
    
    // 캐시 통계를 위한 카운터
    private volatile long cacheHits = 0;
    private volatile long cacheMisses = 0;
    
    // 캐시 엔트리 클래스
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        
        public CacheEntry(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getData() { return data; }
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    // HS부호검색 API
    @Value("${app.api.hscode.search.key:s240o275s078n237g000a070s0}")
    private String hsSearchApiKey;

    // 관세율기본조회 API
    @Value("${app.api.hscode.tariff.key:s240o275s078n237g000a070s0}")
    private String tariffBasicApiKey;

    // 관세환율정보조회 API
    @Value("${app.api.hscode.exchange.key:s240o275s078n237g000a070s0}")
    private String tariffExchangeApiKey;

    private static final String HS_SEARCH_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/hsCodeSearchService/search";
    private static final String TARIFF_BASIC_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/tariffBasicService/search";
    private static final String TARIFF_EXCHANGE_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/tariffExchangeService/search";

    /**
     * 품목명으로 HS Code 검색
     */
    public HSCodeSearchResponse searchHSCodeByProductName(String productName) {
        try {
            // 캐시에서 먼저 확인
            String cacheKey = "search_product:" + productName.toLowerCase().trim();
            CacheEntry<HSCodeSearchResponse> cachedEntry = hsCodeCache.get(cacheKey);
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                cacheHits++;
                log.info("Cache hit for HS Code search: {} (hits: {}, misses: {})", productName, cacheHits, cacheMisses);
                return cachedEntry.getData();
            }
            cacheMisses++;
            
            // Check if API key is test/default - use mock data
            if (hsSearchApiKey.equals("s240o275s078n237g000a070s0") || hsSearchApiKey.equals("test-key-default")) {
                log.info("Using mock data for HS Code search: {}", productName);
                HSCodeSearchResponse response = createMockHSCodeResponse(productName);
                hsCodeCache.put(cacheKey, new CacheEntry<>(response));
                return response;
            }
            
            String encodedProductName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            
            String url = UriComponentsBuilder.fromUriString(HS_SEARCH_BASE_URL)
                    .queryParam("key", hsSearchApiKey)
                    .queryParam("type", "json")
                    .queryParam("mKorNm", encodedProductName)
                    .queryParam("returnType", "JSON")
                    .build()
                    .toUriString();

            log.info("Searching HS Code for product: {}", productName);
            
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            HSCodeSearchResponse result = parseHSCodeSearchResponse(response);
            // 성공한 응답만 캐시에 저장
            if (result.isSuccess()) {
                hsCodeCache.put(cacheKey, new CacheEntry<>(result));
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("HTTP error searching HS Code for product: {} - Status: {}, Body: {}", 
                    productName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().is4xxClientError()) {
                return HSCodeSearchResponse.error("API 요청 오류: " + 
                    (e.getStatusCode().value() == 400 ? "잘못된 요청입니다. 검색어를 확인해주세요." : 
                     e.getStatusCode().value() == 401 ? "API 인증에 실패했습니다. 관리자에게 문의하세요." : 
                     e.getStatusCode().value() == 429 ? "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요." : 
                     "클라이언트 오류가 발생했습니다."));
            } else if (e.getStatusCode().is5xxServerError()) {
                return HSCodeSearchResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
            return HSCodeSearchResponse.error("HS Code 검색 중 오류가 발생했습니다: " + e.getMessage());
        } catch (WebClientRequestException e) {
            log.error("Network error searching HS Code for product: {}", productName, e);
            if (e.getCause() instanceof java.net.ConnectException) {
                return HSCodeSearchResponse.error("네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
            } else if (e.getCause() instanceof java.net.SocketTimeoutException || 
                      e.getMessage().contains("timeout")) {
                return HSCodeSearchResponse.error("요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            return HSCodeSearchResponse.error("네트워크 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error searching HS Code for product: {}", productName, e);
            return HSCodeSearchResponse.error("HS Code 검색 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * HS Code로 품목명 검색
     */
    public HSCodeSearchResponse searchProductNameByHSCode(String hsCode) {
        try {
            // 캐시에서 먼저 확인
            String cacheKey = "search_hscode:" + hsCode.toLowerCase().trim();
            CacheEntry<HSCodeSearchResponse> cachedEntry = hsCodeCache.get(cacheKey);
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                log.info("Cache hit for HS Code lookup: {}", hsCode);
                return cachedEntry.getData();
            }
            
            // Check if API key is test/default - use mock data
            if (hsSearchApiKey.equals("s240o275s078n237g000a070s0") || hsSearchApiKey.equals("test-key-default")) {
                log.info("Using mock data for HS Code: {}", hsCode);
                HSCodeSearchResponse response = createMockHSCodeResponseByCode(hsCode);
                hsCodeCache.put(cacheKey, new CacheEntry<>(response));
                return response;
            }
            
            String url = UriComponentsBuilder.fromUriString(HS_SEARCH_BASE_URL)
                    .queryParam("key", hsSearchApiKey)
                    .queryParam("type", "json")
                    .queryParam("hsCode", hsCode)
                    .queryParam("returnType", "JSON")
                    .build()
                    .toUriString();

            log.info("Searching product name for HS Code: {}", hsCode);
            
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            HSCodeSearchResponse result = parseHSCodeSearchResponse(response);
            // 성공한 응답만 캐시에 저장
            if (result.isSuccess()) {
                hsCodeCache.put(cacheKey, new CacheEntry<>(result));
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("HTTP error searching product for HS Code: {} - Status: {}, Body: {}", 
                    hsCode, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().is4xxClientError()) {
                return HSCodeSearchResponse.error("API 요청 오류: " + 
                    (e.getStatusCode().value() == 400 ? "잘못된 HS Code입니다. 형식을 확인해주세요." : 
                     e.getStatusCode().value() == 401 ? "API 인증에 실패했습니다. 관리자에게 문의하세요." : 
                     e.getStatusCode().value() == 429 ? "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요." : 
                     "클라이언트 오류가 발생했습니다."));
            } else if (e.getStatusCode().is5xxServerError()) {
                return HSCodeSearchResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
            return HSCodeSearchResponse.error("품목명 검색 중 오류가 발생했습니다: " + e.getMessage());
        } catch (WebClientRequestException e) {
            log.error("Network error searching product for HS Code: {}", hsCode, e);
            if (e.getCause() instanceof java.net.ConnectException) {
                return HSCodeSearchResponse.error("네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
            } else if (e.getCause() instanceof java.net.SocketTimeoutException || 
                      e.getMessage().contains("timeout")) {
                return HSCodeSearchResponse.error("요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            return HSCodeSearchResponse.error("네트워크 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error searching product for HS Code: {}", hsCode, e);
            return HSCodeSearchResponse.error("품목명 검색 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * HS Code로 기본 관세율 조회
     */
    public TariffRateResponse getTariffRate(String hsCode) {
        try {
            // 캐시에서 먼저 확인
            String cacheKey = "tariff:" + hsCode.toLowerCase().trim();
            CacheEntry<TariffRateResponse> cachedEntry = tariffCache.get(cacheKey);
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                log.info("Cache hit for tariff rate: {}", hsCode);
                return cachedEntry.getData();
            }
            
            String url = UriComponentsBuilder.fromUriString(TARIFF_BASIC_BASE_URL)
                    .queryParam("key", tariffBasicApiKey)
                    .queryParam("type", "json")
                    .queryParam("hsCode", hsCode)
                    .queryParam("returnType", "JSON")
                    .build()
                    .toUriString();

            log.info("Getting tariff rate for HS Code: {}", hsCode);
            
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            TariffRateResponse result = parseTariffRateResponse(response);
            // 성공한 응답만 캐시에 저장
            if (result.isSuccess()) {
                tariffCache.put(cacheKey, new CacheEntry<>(result));
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("HTTP error getting tariff rate for HS Code: {} - Status: {}, Body: {}", 
                    hsCode, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().is4xxClientError()) {
                return TariffRateResponse.error("관세율 조회 오류: " + 
                    (e.getStatusCode().value() == 400 ? "잘못된 HS Code입니다. 형식을 확인해주세요." : 
                     e.getStatusCode().value() == 401 ? "API 인증에 실패했습니다. 관리자에게 문의하세요." : 
                     e.getStatusCode().value() == 429 ? "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요." : 
                     "클라이언트 오류가 발생했습니다."));
            } else if (e.getStatusCode().is5xxServerError()) {
                return TariffRateResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
            return TariffRateResponse.error("관세율 조회 중 오류가 발생했습니다: " + e.getMessage());
        } catch (WebClientRequestException e) {
            log.error("Network error getting tariff rate for HS Code: {}", hsCode, e);
            if (e.getCause() instanceof java.net.ConnectException) {
                return TariffRateResponse.error("네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
            } else if (e.getCause() instanceof java.net.SocketTimeoutException || 
                      e.getMessage().contains("timeout")) {
                return TariffRateResponse.error("요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            return TariffRateResponse.error("네트워크 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting tariff rate for HS Code: {}", hsCode, e);
            return TariffRateResponse.error("관세율 조회 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * 관세 환율 정보 조회
     */
    public TariffExchangeResponse getTariffExchangeRate() {
        try {
            // 캐시에서 먼저 확인 - 환율은 전역적으로 동일하므로 고정 키 사용
            String cacheKey = "exchange_rate";
            CacheEntry<TariffExchangeResponse> cachedEntry = exchangeCache.get(cacheKey);
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                log.info("Cache hit for exchange rate");
                return cachedEntry.getData();
            }
            
            String url = UriComponentsBuilder.fromUriString(TARIFF_EXCHANGE_BASE_URL)
                    .queryParam("key", tariffExchangeApiKey)
                    .queryParam("type", "json")
                    .queryParam("returnType", "JSON")
                    .build()
                    .toUriString();

            log.info("Getting tariff exchange rate");
            
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            TariffExchangeResponse result = parseTariffExchangeResponse(response);
            // 성공한 응답만 캐시에 저장
            if (result.isSuccess()) {
                exchangeCache.put(cacheKey, new CacheEntry<>(result));
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("HTTP error getting tariff exchange rate - Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().is4xxClientError()) {
                return TariffExchangeResponse.error("관세환율 조회 오류: " + 
                    (e.getStatusCode().value() == 401 ? "API 인증에 실패했습니다. 관리자에게 문의하세요." : 
                     e.getStatusCode().value() == 429 ? "API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요." : 
                     "클라이언트 오류가 발생했습니다."));
            } else if (e.getStatusCode().is5xxServerError()) {
                return TariffExchangeResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
            return TariffExchangeResponse.error("관세환율 조회 중 오류가 발생했습니다: " + e.getMessage());
        } catch (WebClientRequestException e) {
            log.error("Network error getting tariff exchange rate", e);
            if (e.getCause() instanceof java.net.ConnectException) {
                return TariffExchangeResponse.error("네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
            } else if (e.getCause() instanceof java.net.SocketTimeoutException || 
                      e.getMessage().contains("timeout")) {
                return TariffExchangeResponse.error("요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            return TariffExchangeResponse.error("네트워크 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting tariff exchange rate", e);
            return TariffExchangeResponse.error("관세환율 조회 중 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.");
        }
    }

    private HSCodeSearchResponse parseHSCodeSearchResponse(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            if (responseMap.containsKey("error")) {
                return HSCodeSearchResponse.error((String) responseMap.get("error"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) responseMap.get("hsCodeSearchList");
            
            if (itemList == null || itemList.isEmpty()) {
                return HSCodeSearchResponse.empty();
            }

            return HSCodeSearchResponse.success(itemList);
        } catch (Exception e) {
            log.error("Failed to parse HS Code search response", e);
            return HSCodeSearchResponse.error("응답 파싱 실패: " + e.getMessage());
        }
    }

    private TariffRateResponse parseTariffRateResponse(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            if (responseMap.containsKey("error")) {
                return TariffRateResponse.error((String) responseMap.get("error"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) responseMap.get("tariffBasicList");
            
            if (itemList == null || itemList.isEmpty()) {
                return TariffRateResponse.empty();
            }

            return TariffRateResponse.success(itemList);
        } catch (Exception e) {
            log.error("Failed to parse tariff rate response", e);
            return TariffRateResponse.error("응답 파싱 실패: " + e.getMessage());
        }
    }

    private TariffExchangeResponse parseTariffExchangeResponse(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            if (responseMap.containsKey("error")) {
                return TariffExchangeResponse.error((String) responseMap.get("error"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) responseMap.get("tariffExchangeList");
            
            if (itemList == null || itemList.isEmpty()) {
                return TariffExchangeResponse.empty();
            }

            return TariffExchangeResponse.success(itemList);
        } catch (Exception e) {
            log.error("Failed to parse tariff exchange response", e);
            return TariffExchangeResponse.error("응답 파싱 실패: " + e.getMessage());
        }
    }

    // Response DTOs
    public static class HSCodeSearchResponse {
        private boolean success;
        private String error;
        private List<HSCodeItem> items;

        public static HSCodeSearchResponse success(List<Map<String, Object>> rawItems) {
            HSCodeSearchResponse response = new HSCodeSearchResponse();
            response.success = true;
            response.items = rawItems.stream()
                    .map(HSCodeItem::fromMap)
                    .toList();
            return response;
        }

        public static HSCodeSearchResponse error(String error) {
            HSCodeSearchResponse response = new HSCodeSearchResponse();
            response.success = false;
            response.error = error;
            return response;
        }

        public static HSCodeSearchResponse empty() {
            HSCodeSearchResponse response = new HSCodeSearchResponse();
            response.success = true;
            response.items = List.of();
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public List<HSCodeItem> getItems() { return items; }
    }

    public static class HSCodeItem {
        private String hsCode;
        private String koreanName;
        private String englishName;
        private String unit;

        public static HSCodeItem fromMap(Map<String, Object> map) {
            HSCodeItem item = new HSCodeItem();
            item.hsCode = (String) map.get("hsCode");
            item.koreanName = (String) map.get("korNm");
            item.englishName = (String) map.get("engNm");
            item.unit = (String) map.get("unit");
            return item;
        }

        // Getters and setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getKoreanName() { return koreanName; }
        public void setKoreanName(String koreanName) { this.koreanName = koreanName; }
        public String getEnglishName() { return englishName; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class TariffRateResponse {
        private boolean success;
        private String error;
        private List<TariffRateItem> items;

        public static TariffRateResponse success(List<Map<String, Object>> rawItems) {
            TariffRateResponse response = new TariffRateResponse();
            response.success = true;
            response.items = rawItems.stream()
                    .map(TariffRateItem::fromMap)
                    .toList();
            return response;
        }

        public static TariffRateResponse error(String error) {
            TariffRateResponse response = new TariffRateResponse();
            response.success = false;
            response.error = error;
            return response;
        }

        public static TariffRateResponse empty() {
            TariffRateResponse response = new TariffRateResponse();
            response.success = true;
            response.items = List.of();
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public List<TariffRateItem> getItems() { return items; }
    }

    public static class TariffRateItem {
        private String hsCode;
        private String koreanName;
        private BigDecimal basicRate;
        private BigDecimal wtoRate;
        private BigDecimal specialRate;
        private String unit;

        public static TariffRateItem fromMap(Map<String, Object> map) {
            TariffRateItem item = new TariffRateItem();
            item.hsCode = (String) map.get("hsCode");
            item.koreanName = (String) map.get("korNm");
            item.basicRate = parseRate((String) map.get("basicRate"));
            item.wtoRate = parseRate((String) map.get("wtoRate"));
            item.specialRate = parseRate((String) map.get("specialRate"));
            item.unit = (String) map.get("unit");
            return item;
        }

        private static BigDecimal parseRate(String rateStr) {
            if (rateStr == null || rateStr.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            try {
                // Remove % sign if present
                String cleanRate = rateStr.replace("%", "").trim();
                return new BigDecimal(cleanRate);
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }

        // Getters and setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getKoreanName() { return koreanName; }
        public void setKoreanName(String koreanName) { this.koreanName = koreanName; }
        public BigDecimal getBasicRate() { return basicRate; }
        public void setBasicRate(BigDecimal basicRate) { this.basicRate = basicRate; }
        public BigDecimal getWtoRate() { return wtoRate; }
        public void setWtoRate(BigDecimal wtoRate) { this.wtoRate = wtoRate; }
        public BigDecimal getSpecialRate() { return specialRate; }
        public void setSpecialRate(BigDecimal specialRate) { this.specialRate = specialRate; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class TariffExchangeResponse {
        private boolean success;
        private String error;
        private List<ExchangeRateItem> items;

        public static TariffExchangeResponse success(List<Map<String, Object>> rawItems) {
            TariffExchangeResponse response = new TariffExchangeResponse();
            response.success = true;
            response.items = rawItems.stream()
                    .map(ExchangeRateItem::fromMap)
                    .toList();
            return response;
        }

        public static TariffExchangeResponse error(String error) {
            TariffExchangeResponse response = new TariffExchangeResponse();
            response.success = false;
            response.error = error;
            return response;
        }

        public static TariffExchangeResponse empty() {
            TariffExchangeResponse response = new TariffExchangeResponse();
            response.success = true;
            response.items = List.of();
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public List<ExchangeRateItem> getItems() { return items; }
    }

    public static class ExchangeRateItem {
        private String currencyCode;
        private BigDecimal exchangeRate;
        private String applyDate;

        public static ExchangeRateItem fromMap(Map<String, Object> map) {
            ExchangeRateItem item = new ExchangeRateItem();
            item.currencyCode = (String) map.get("currencyCode");
            item.exchangeRate = new BigDecimal((String) map.get("exchangeRate"));
            item.applyDate = (String) map.get("applyDate");
            return item;
        }

        // Getters and setters
        public String getCurrencyCode() { return currencyCode; }
        public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
        public BigDecimal getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
        public String getApplyDate() { return applyDate; }
        public void setApplyDate(String applyDate) { this.applyDate = applyDate; }
    }
    
    // Mock data methods for testing
    private HSCodeSearchResponse createMockHSCodeResponse(String productName) {
        HSCodeSearchResponse response = new HSCodeSearchResponse();
        response.success = true;
        response.items = List.of();
        
        String lowerProduct = productName.toLowerCase();
        
        // Mock data for common clothing terms
        if (productName.contains("의류") || lowerProduct.contains("clothes") || 
            lowerProduct.contains("apparel") || lowerProduct.contains("garment")) {
            HSCodeItem item1 = new HSCodeItem();
            item1.hsCode = "6101.20";
            item1.koreanName = "면제 남성용 편직제품으로 된 슈트";
            item1.englishName = "Men's or boys' suits, knitted or crocheted, of cotton";
            item1.unit = "벌";
            
            HSCodeItem item2 = new HSCodeItem();
            item2.hsCode = "6201.11";
            item2.koreanName = "남성용 모직제품으로 된 오버코트";
            item2.englishName = "Men's or boys' overcoats of wool or fine animal hair";
            item2.unit = "벌";
            
            response.items = List.of(item1, item2);
        }
        // Electronics
        else if (productName.contains("전자") || lowerProduct.contains("electronic") || 
                 lowerProduct.contains("laptop") || lowerProduct.contains("computer")) {
            HSCodeItem item1 = new HSCodeItem();
            item1.hsCode = "8471.30";
            item1.koreanName = "휴대용 자동자료처리기기";
            item1.englishName = "Portable automatic data processing machines";
            item1.unit = "대";
            
            HSCodeItem item2 = new HSCodeItem();
            item2.hsCode = "8528.71";
            item2.koreanName = "텔레비전 수신용 기기";
            item2.englishName = "Television receivers";
            item2.unit = "대";
            
            response.items = List.of(item1, item2);
        }
        // Cosmetics
        else if (productName.contains("화장품") || lowerProduct.contains("cosmetic") || 
                 lowerProduct.contains("makeup")) {
            HSCodeItem item1 = new HSCodeItem();
            item1.hsCode = "3304.10";
            item1.koreanName = "입술화장품";
            item1.englishName = "Lip make-up preparations";
            item1.unit = "kg";
            
            HSCodeItem item2 = new HSCodeItem();
            item2.hsCode = "3304.99";
            item2.koreanName = "기타 화장품";
            item2.englishName = "Other beauty or make-up preparations";
            item2.unit = "kg";
            
            response.items = List.of(item1, item2);
        }
        // Food
        else if (productName.contains("식품") || lowerProduct.contains("food") || 
                 lowerProduct.contains("snack")) {
            HSCodeItem item1 = new HSCodeItem();
            item1.hsCode = "1905.90";
            item1.koreanName = "빵류, 과자류";
            item1.englishName = "Bread, pastry, cakes, biscuits";
            item1.unit = "kg";
            
            response.items = List.of(item1);
        }
        // Toys
        else if (productName.contains("장난감") || lowerProduct.contains("toy")) {
            HSCodeItem item1 = new HSCodeItem();
            item1.hsCode = "9503.00";
            item1.koreanName = "장난감";
            item1.englishName = "Toys";
            item1.unit = "개";
            
            response.items = List.of(item1);
        }
        
        return response;
    }
    
    private HSCodeSearchResponse createMockHSCodeResponseByCode(String hsCode) {
        HSCodeSearchResponse response = new HSCodeSearchResponse();
        response.success = true;
        
        // Mock data for specific HS codes
        if (hsCode.equals("6101.20") || hsCode.equals("610120")) {
            HSCodeItem item = new HSCodeItem();
            item.hsCode = "6101.20";
            item.koreanName = "면제 남성용 편직제품으로 된 슈트";
            item.englishName = "Men's or boys' suits, knitted or crocheted, of cotton";
            item.unit = "벌";
            response.items = List.of(item);
        } else if (hsCode.startsWith("6101")) {
            HSCodeItem item = new HSCodeItem();
            item.hsCode = hsCode;
            item.koreanName = "남성용 편직제품";
            item.englishName = "Men's or boys' knitted garments";
            item.unit = "벌";
            response.items = List.of(item);
        } else if (hsCode.startsWith("61") || hsCode.startsWith("62")) {
            HSCodeItem item = new HSCodeItem();
            item.hsCode = hsCode;
            item.koreanName = "의류 및 그 부속품";
            item.englishName = "Articles of apparel and clothing accessories";
            item.unit = "개";
            response.items = List.of(item);
        } else {
            response.items = List.of();
        }
        
        return response;
    }
    
    /**
     * 캐시 통계 정보 조회
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("hitRate", cacheMisses > 0 ? (double) cacheHits / (cacheHits + cacheMisses) : 0.0);
        stats.put("hsCodeCacheSize", hsCodeCache.size());
        stats.put("tariffCacheSize", tariffCache.size());
        stats.put("exchangeCacheSize", exchangeCache.size());
        stats.put("cacheDurationMs", CACHE_DURATION_MS);
        return stats;
    }
    
    /**
     * 캐시 클리어 (만료된 항목 정리)
     */
    public void clearExpiredCache() {
        long cleared = 0;
        cleared += hsCodeCache.entrySet().removeIf(entry -> entry.getValue().isExpired()) ? 1 : 0;
        cleared += tariffCache.entrySet().removeIf(entry -> entry.getValue().isExpired()) ? 1 : 0;
        cleared += exchangeCache.entrySet().removeIf(entry -> entry.getValue().isExpired()) ? 1 : 0;
        log.info("Cleared {} expired cache entries", cleared);
    }
}