package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class HsCodeValidationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${hscode.api.base-url:https://www.data.go.kr/api/15076580/v1/uddi}")
    private String hsCodeApiBaseUrl;
    
    @Value("${hscode.api.key:}")
    private String hsCodeApiKey;
    
    @Value("${hscode.api.enabled:false}")
    private boolean hsCodeApiEnabled;
    
    // HS Code format validation (6, 8, or 10 digits)
    private static final Pattern HS_CODE_PATTERN = Pattern.compile("^\\d{6}(\\d{2})?(\\d{2})?$");
    
    /**
     * Validate HS Code format and existence
     */
    @Cacheable(value = "hsCodeValidation", key = "#hsCode")
    public HsCodeValidationResult validateHsCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return createErrorResult(hsCode, "HS Code cannot be empty");
        }
        
        // Format validation
        if (!HS_CODE_PATTERN.matcher(hsCode.trim()).matches()) {
            return createErrorResult(hsCode, "Invalid HS Code format (must be 6-10 digits)");
        }
        
        String cleanCode = hsCode.trim();
        
        if (!hsCodeApiEnabled || hsCodeApiKey.isEmpty()) {
            log.warn("HS Code API is disabled or API key is not configured");
            return createMockValidationResult(cleanCode);
        }
        
        try {
            log.info("Validating HS Code: {}", cleanCode);
            
            String url = buildHsCodeUrl(cleanCode);
            HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return parseHsCodeResponse(response.getBody(), cleanCode);
            } else {
                log.error("HS Code API returned error status: {}", response.getStatusCode());
                return createErrorResult(cleanCode, "API call failed");
            }
            
        } catch (Exception e) {
            log.error("Failed to validate HS Code: {}", cleanCode, e);
            return createErrorResult(cleanCode, "API call error: " + e.getMessage());
        }
    }
    
    /**
     * Validate multiple HS Codes
     */
    public Map<String, HsCodeValidationResult> validateMultipleHsCodes(List<String> hsCodes) {
        Map<String, HsCodeValidationResult> results = new HashMap<>();
        
        for (String hsCode : hsCodes) {
            try {
                HsCodeValidationResult result = validateHsCode(hsCode);
                results.put(hsCode, result);
                
                // Rate limiting delay
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("Failed to validate HS Code: {}", hsCode, e);
                results.put(hsCode, createErrorResult(hsCode, e.getMessage()));
            }
        }
        
        return results;
    }
    
    /**
     * Get detailed HS Code information
     */
    @Cacheable(value = "hsCodeInfo", key = "#hsCode")
    public HsCodeInfo getHsCodeInfo(String hsCode) {
        HsCodeValidationResult validationResult = validateHsCode(hsCode);
        
        if (validationResult.isValid()) {
            return validationResult.getHsCodeInfo();
        } else {
            return null;
        }
    }
    
    /**
     * Search HS Code by keyword
     */
    public List<HsCodeSearchResult> searchHsCode(String keyword, int maxResults) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        if (!hsCodeApiEnabled) {
            return createMockSearchResults(keyword, maxResults);
        }
        
        try {
            log.info("Searching HS Code with keyword: {}", keyword);
            
            String url = buildHsCodeSearchUrl(keyword, maxResults);
            HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return parseHsCodeSearchResponse(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Failed to search HS Code with keyword: {}", keyword, e);
        }
        
        return createMockSearchResults(keyword, maxResults);
    }
    
    /**
     * Build HS Code API URL
     */
    private String buildHsCodeUrl(String hsCode) {
        return UriComponentsBuilder.fromHttpUrl(hsCodeApiBaseUrl)
                .queryParam("serviceKey", hsCodeApiKey)
                .queryParam("hsCode", hsCode)
                .queryParam("type", "json")
                .build()
                .toUriString();
    }
    
    /**
     * Build HS Code search URL
     */
    private String buildHsCodeSearchUrl(String keyword, int maxResults) {
        return UriComponentsBuilder.fromHttpUrl(hsCodeApiBaseUrl + "/search")
                .queryParam("serviceKey", hsCodeApiKey)
                .queryParam("keyword", keyword)
                .queryParam("numOfRows", maxResults)
                .queryParam("type", "json")
                .build()
                .toUriString();
    }
    
    /**
     * Build HTTP headers
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "YCS-LMS/1.0");
        headers.set("Accept", "application/json");
        return headers;
    }
    
    /**
     * Parse HS Code API response
     */
    private HsCodeValidationResult parseHsCodeResponse(String responseBody, String hsCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode response = root.get("response");
            
            if (response != null) {
                JsonNode header = response.get("header");
                JsonNode body = response.get("body");
                
                if (header != null && "00".equals(header.get("resultCode").asText())) {
                    // Success response
                    HsCodeValidationResult result = new HsCodeValidationResult();
                    result.setHsCode(hsCode);
                    result.setValid(true);
                    result.setLastValidated(LocalDateTime.now());
                    result.setSource("Korean Government API");
                    
                    if (body != null && body.get("items") != null) {
                        JsonNode item = body.get("items").get("item");
                        if (item != null) {
                            HsCodeInfo info = new HsCodeInfo();
                            info.setHsCode(hsCode);
                            info.setDescription(item.get("itemName").asText());
                            info.setUnit(item.get("unit") != null ? item.get("unit").asText() : "");
                            info.setTariffRate(item.get("tariffRate") != null ? 
                                item.get("tariffRate").asText() : "");
                            
                            result.setHsCodeInfo(info);
                        }
                    }
                    
                    return result;
                } else {
                    return createErrorResult(hsCode, "Invalid HS Code or not found");
                }
            }
            
            return createErrorResult(hsCode, "Invalid API response format");
            
        } catch (Exception e) {
            log.error("Failed to parse HS Code response", e);
            return createErrorResult(hsCode, "Response parsing error");
        }
    }
    
    /**
     * Parse HS Code search response
     */
    private List<HsCodeSearchResult> parseHsCodeSearchResponse(String responseBody) {
        List<HsCodeSearchResult> results = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode response = root.get("response");
            
            if (response != null) {
                JsonNode body = response.get("body");
                if (body != null && body.get("items") != null) {
                    JsonNode items = body.get("items");
                    
                    if (items.isArray()) {
                        for (JsonNode item : items) {
                            HsCodeSearchResult result = new HsCodeSearchResult();
                            result.setHsCode(item.get("hsCode").asText());
                            result.setDescription(item.get("itemName").asText());
                            result.setUnit(item.get("unit") != null ? item.get("unit").asText() : "");
                            results.add(result);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse HS Code search response", e);
        }
        
        return results;
    }
    
    /**
     * Create mock validation result
     */
    private HsCodeValidationResult createMockValidationResult(String hsCode) {
        HsCodeValidationResult result = new HsCodeValidationResult();
        result.setHsCode(hsCode);
        result.setValid(true);
        result.setLastValidated(LocalDateTime.now());
        result.setSource("Mock Data");
        
        HsCodeInfo info = new HsCodeInfo();
        info.setHsCode(hsCode);
        info.setDescription(getMockDescription(hsCode));
        info.setUnit("KG");
        info.setTariffRate("8%");
        
        result.setHsCodeInfo(info);
        
        log.info("Created mock HS Code validation for: {}", hsCode);
        return result;
    }
    
    /**
     * Create mock search results
     */
    private List<HsCodeSearchResult> createMockSearchResults(String keyword, int maxResults) {
        List<HsCodeSearchResult> results = new ArrayList<>();
        
        // Sample mock data
        String[][] mockData = {
            {"850431", "Electronic transformers", "EA"},
            {"640399", "Leather footwear", "PAIR"},
            {"611020", "Cotton sweaters", "EA"},
            {"940360", "Wooden furniture", "EA"},
            {"392690", "Plastic articles", "KG"}
        };
        
        for (int i = 0; i < Math.min(mockData.length, maxResults); i++) {
            if (mockData[i][1].toLowerCase().contains(keyword.toLowerCase())) {
                HsCodeSearchResult result = new HsCodeSearchResult();
                result.setHsCode(mockData[i][0]);
                result.setDescription(mockData[i][1] + " (Mock Data)");
                result.setUnit(mockData[i][2]);
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Get mock description
     */
    private String getMockDescription(String hsCode) {
        Map<String, String> mockDescriptions = new HashMap<>();
        mockDescriptions.put("850431", "Electronic transformers (others)");
        mockDescriptions.put("640399", "Other leather footwear");
        mockDescriptions.put("611020", "Cotton pullovers, cardigans");
        mockDescriptions.put("940360", "Other wooden furniture");
        mockDescriptions.put("392690", "Other plastic articles");
        
        return mockDescriptions.getOrDefault(hsCode, "General product (Mock Data)");
    }
    
    /**
     * Create error result
     */
    private HsCodeValidationResult createErrorResult(String hsCode, String errorMessage) {
        HsCodeValidationResult result = new HsCodeValidationResult();
        result.setHsCode(hsCode);
        result.setValid(false);
        result.setErrorMessage(errorMessage);
        result.setLastValidated(LocalDateTime.now());
        return result;
    }
    
    /**
     * HS Code validation result DTO
     */
    public static class HsCodeValidationResult {
        private String hsCode;
        private boolean valid;
        private HsCodeInfo hsCodeInfo;
        private String errorMessage;
        private LocalDateTime lastValidated;
        private String source;
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public HsCodeInfo getHsCodeInfo() { return hsCodeInfo; }
        public void setHsCodeInfo(HsCodeInfo hsCodeInfo) { this.hsCodeInfo = hsCodeInfo; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getLastValidated() { return lastValidated; }
        public void setLastValidated(LocalDateTime lastValidated) { this.lastValidated = lastValidated; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
    
    /**
     * HS Code information DTO
     */
    public static class HsCodeInfo {
        private String hsCode;
        private String description;
        private String unit;
        private String tariffRate;
        private String category;
        private boolean restricted;
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getTariffRate() { return tariffRate; }
        public void setTariffRate(String tariffRate) { this.tariffRate = tariffRate; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public boolean isRestricted() { return restricted; }
        public void setRestricted(boolean restricted) { this.restricted = restricted; }
    }
    
    /**
     * HS Code search result DTO
     */
    public static class HsCodeSearchResult {
        private String hsCode;
        private String description;
        private String unit;
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}