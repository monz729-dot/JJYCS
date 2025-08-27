package com.ycs.lms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HSCodeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // HS부호검색 API
    @Value("${app.api.hscode.search.key:s240o275s078n237g000a070s0}")
    private String hsSearchApiKey;

    // 관세율기본조회 API
    @Value("${app.api.tariff.basic.key:y270u255i028u267i010o090s0}")
    private String tariffBasicApiKey;

    // 관세환율정보조회 API
    @Value("${app.api.tariff.exchange.key:p230m215f048a247h000v070a0}")
    private String tariffExchangeApiKey;

    private static final String HS_SEARCH_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/hsCodeSearchService/search";
    private static final String TARIFF_BASIC_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/tariffBasicService/search";
    private static final String TARIFF_EXCHANGE_BASE_URL = "https://unipass.customs.go.kr:38010/ext/rest/tariffExchangeService/search";

    /**
     * 품목명으로 HS Code 검색
     */
    public HSCodeSearchResponse searchHSCodeByProductName(String productName) {
        try {
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
                    .block();

            return parseHSCodeSearchResponse(response);
        } catch (Exception e) {
            log.error("Failed to search HS Code for product: {}", productName, e);
            return HSCodeSearchResponse.error("HS Code 검색에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * HS Code로 품목명 검색
     */
    public HSCodeSearchResponse searchProductNameByHSCode(String hsCode) {
        try {
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
                    .block();

            return parseHSCodeSearchResponse(response);
        } catch (Exception e) {
            log.error("Failed to search product name for HS Code: {}", hsCode, e);
            return HSCodeSearchResponse.error("품목명 검색에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * HS Code로 기본 관세율 조회
     */
    public TariffRateResponse getTariffRate(String hsCode) {
        try {
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
                    .block();

            return parseTariffRateResponse(response);
        } catch (Exception e) {
            log.error("Failed to get tariff rate for HS Code: {}", hsCode, e);
            return TariffRateResponse.error("관세율 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 관세 환율 정보 조회
     */
    public TariffExchangeResponse getTariffExchangeRate() {
        try {
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
                    .block();

            return parseTariffExchangeResponse(response);
        } catch (Exception e) {
            log.error("Failed to get tariff exchange rate", e);
            return TariffExchangeResponse.error("관세환율 조회에 실패했습니다: " + e.getMessage());
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
}