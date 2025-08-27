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
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmsTrackingService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ems.api.base-url:https://service.epost.go.kr/trace.RetrieveReqService}")
    private String emsApiBaseUrl;
    
    @Value("${ems.api.key:}")
    private String emsApiKey;
    
    @Value("${ems.api.enabled:false}")
    private boolean emsApiEnabled;
    
    @Value("${ems.api.timeout:30000}")
    private int timeoutMs;
    
    /**
     * Get EMS tracking information
     */
    public EmsTrackingResult getTrackingInfo(String trackingNumber) {
        if (!emsApiEnabled || emsApiKey.isEmpty()) {
            log.warn("EMS API is disabled or API key is not configured");
            return createMockTrackingResult(trackingNumber);
        }
        
        try {
            log.info("Fetching EMS tracking info for: {}", trackingNumber);
            
            // Call EMS API
            String url = buildTrackingUrl(trackingNumber);
            HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return parseTrackingResponse(response.getBody(), trackingNumber);
            } else {
                log.error("EMS API returned error status: {}", response.getStatusCode());
                return createErrorResult(trackingNumber, "API call failed");
            }
            
        } catch (Exception e) {
            log.error("Failed to fetch EMS tracking info for: {}", trackingNumber, e);
            return createErrorResult(trackingNumber, "API call error: " + e.getMessage());
        }
    }
    
    /**
     * Get tracking information for multiple tracking numbers
     */
    public Map<String, EmsTrackingResult> getMultipleTrackingInfo(List<String> trackingNumbers) {
        Map<String, EmsTrackingResult> results = new HashMap<>();
        
        for (String trackingNumber : trackingNumbers) {
            try {
                EmsTrackingResult result = getTrackingInfo(trackingNumber);
                results.put(trackingNumber, result);
                
                // Rate limiting delay
                Thread.sleep(100);
                
            } catch (Exception e) {
                log.error("Failed to get tracking info for: {}", trackingNumber, e);
                results.put(trackingNumber, createErrorResult(trackingNumber, e.getMessage()));
            }
        }
        
        return results;
    }
    
    /**
     * Build tracking URL
     */
    private String buildTrackingUrl(String trackingNumber) {
        return UriComponentsBuilder.fromHttpUrl(emsApiBaseUrl)
                .queryParam("sid1", trackingNumber)
                .queryParam("displayHeader", "N")
                .build()
                .toUriString();
    }
    
    /**
     * Build HTTP headers
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "YCS-LMS/1.0");
        headers.set("Accept", "application/json, text/html");
        if (!emsApiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + emsApiKey);
        }
        return headers;
    }
    
    /**
     * Parse EMS API response
     */
    private EmsTrackingResult parseTrackingResponse(String responseBody, String trackingNumber) {
        try {
            // Note: Korea Post EMS API returns XML or HTML format
            // For now, create mock response structure
            
            EmsTrackingResult result = new EmsTrackingResult();
            result.setTrackingNumber(trackingNumber);
            result.setSuccess(true);
            result.setLastUpdated(LocalDateTime.now());
            
            // TODO: Parse actual API response format
            // Currently using mock data
            result.setCurrentStatus("In Transit");
            result.setCurrentLocation("Seoul");
            result.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
            
            List<EmsTrackingEvent> events = new ArrayList<>();
            events.add(new EmsTrackingEvent(
                LocalDateTime.now().minusDays(1),
                "Dispatched",
                "Origin Post Office",
                "Item dispatched"
            ));
            events.add(new EmsTrackingEvent(
                LocalDateTime.now(),
                "In Transit",
                "Seoul",
                "Item in transit"
            ));
            
            result.setTrackingEvents(events);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to parse EMS tracking response for: {}", trackingNumber, e);
            return createErrorResult(trackingNumber, "Response parsing error");
        }
    }
    
    /**
     * Create mock tracking result (when API is disabled)
     */
    private EmsTrackingResult createMockTrackingResult(String trackingNumber) {
        EmsTrackingResult result = new EmsTrackingResult();
        result.setTrackingNumber(trackingNumber);
        result.setSuccess(true);
        result.setCurrentStatus("In Transit (Mock Data)");
        result.setCurrentLocation("Seoul");
        result.setLastUpdated(LocalDateTime.now());
        result.setEstimatedDelivery(LocalDateTime.now().plusDays(2));
        
        List<EmsTrackingEvent> events = new ArrayList<>();
        events.add(new EmsTrackingEvent(
            LocalDateTime.now().minusDays(2),
            "Accepted",
            "Origin Post Office",
            "Item accepted (Mock Data)"
        ));
        events.add(new EmsTrackingEvent(
            LocalDateTime.now().minusDays(1),
            "Dispatched",
            "Seoul",
            "Item dispatched (Mock Data)"
        ));
        events.add(new EmsTrackingEvent(
            LocalDateTime.now(),
            "In Transit",
            "Destination City",
            "Item in transit (Mock Data)"
        ));
        
        result.setTrackingEvents(events);
        
        log.info("Created mock EMS tracking result for: {}", trackingNumber);
        return result;
    }
    
    /**
     * Create error result
     */
    private EmsTrackingResult createErrorResult(String trackingNumber, String errorMessage) {
        EmsTrackingResult result = new EmsTrackingResult();
        result.setTrackingNumber(trackingNumber);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setLastUpdated(LocalDateTime.now());
        return result;
    }
    
    /**
     * EMS tracking result DTO
     */
    public static class EmsTrackingResult {
        private String trackingNumber;
        private boolean success;
        private String currentStatus;
        private String currentLocation;
        private LocalDateTime estimatedDelivery;
        private LocalDateTime lastUpdated;
        private List<EmsTrackingEvent> trackingEvents;
        private String errorMessage;
        
        // Getters and Setters
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
        public String getCurrentLocation() { return currentLocation; }
        public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
        public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
        public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
        public List<EmsTrackingEvent> getTrackingEvents() { return trackingEvents; }
        public void setTrackingEvents(List<EmsTrackingEvent> trackingEvents) { this.trackingEvents = trackingEvents; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * EMS tracking event DTO
     */
    public static class EmsTrackingEvent {
        private LocalDateTime eventTime;
        private String status;
        private String location;
        private String description;
        
        public EmsTrackingEvent(LocalDateTime eventTime, String status, String location, String description) {
            this.eventTime = eventTime;
            this.status = status;
            this.location = location;
            this.description = description;
        }
        
        // Getters and Setters
        public LocalDateTime getEventTime() { return eventTime; }
        public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}