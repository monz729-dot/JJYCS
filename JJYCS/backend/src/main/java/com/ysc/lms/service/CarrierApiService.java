package com.ysc.lms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysc.lms.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 실제 택배사 API 연동 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarrierApiService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${carrier.cj.api.url:https://www.doortodoor.co.kr/parcel/api/tracking}")
    private String cjApiUrl;
    
    @Value("${carrier.hanjin.api.url:https://www.hanjin.co.kr/kor/CMS/DeliveryMgr/WaybillResult.do}")
    private String hanjinApiUrl;
    
    @Value("${carrier.lotte.api.url:https://www.lotteglogis.com/open/tracking}")
    private String lotteApiUrl;
    
    @Value("${carrier.api.timeout:10000}")
    private int apiTimeout;
    
    /**
     * 택배사별 추적 정보 조회
     */
    @Retryable(value = {ResourceAccessException.class, HttpServerErrorException.class}, 
               maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public TrackingResult getTrackingInfo(String carrier, String trackingNumber) {
        try {
            log.info("Fetching tracking info from {} for number: {}", carrier, trackingNumber);
            
            return switch (carrier.toUpperCase()) {
                case "CJ" -> getCJTrackingInfo(trackingNumber);
                case "HANJIN" -> getHanjinTrackingInfo(trackingNumber);
                case "LOTTE" -> getLotteTrackingInfo(trackingNumber);
                case "POST" -> getPostTrackingInfo(trackingNumber);
                default -> {
                    log.warn("Unsupported carrier: {}", carrier);
                    yield TrackingResult.error("지원하지 않는 택배사입니다: " + carrier);
                }
            };
            
        } catch (HttpClientErrorException e) {
            log.warn("Client error from carrier API: {} - {}", e.getStatusCode(), e.getMessage());
            return handleClientError(e, carrier, trackingNumber);
            
        } catch (HttpServerErrorException e) {
            log.error("Server error from carrier API: {} - {}", e.getStatusCode(), e.getMessage());
            throw new ExternalApiException(carrier + " API", "택배사 서버 오류: " + e.getMessage());
            
        } catch (ResourceAccessException e) {
            log.error("Network error accessing carrier API: {}", e.getMessage());
            throw new ExternalApiException(carrier + " API", "택배사 서비스 연결 실패: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error getting tracking info", e);
            return TrackingResult.error("추적 정보 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * CJ대한통운 API 연동
     */
    private TrackingResult getCJTrackingInfo(String trackingNumber) {
        try {
            String url = cjApiUrl + "?t_key=" + trackingNumber + "&t_code=01";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "YCS-LMS/1.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseCJResponse(response.getBody(), trackingNumber);
            } else {
                return TrackingResult.error("CJ대한통운 서비스에서 응답을 받지 못했습니다.");
            }
            
        } catch (Exception e) {
            log.error("Error fetching CJ tracking info", e);
            return TrackingResult.error("CJ대한통운 서비스 오류: " + e.getMessage());
        }
    }
    
    /**
     * 한진택배 API 연동
     */
    private TrackingResult getHanjinTrackingInfo(String trackingNumber) {
        try {
            // 한진택배 API 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("wblnumText2", trackingNumber);
            params.put("submit", "조회");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "YCS-LMS/1.0");
            headers.set("Referer", "https://www.hanjin.co.kr");
            
            String paramString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");
            
            HttpEntity<String> entity = new HttpEntity<>(paramString, headers);
            ResponseEntity<String> response = restTemplate.exchange(hanjinApiUrl, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseHanjinResponse(response.getBody(), trackingNumber);
            } else {
                return TrackingResult.error("한진택배 서비스에서 응답을 받지 못했습니다.");
            }
            
        } catch (Exception e) {
            log.error("Error fetching Hanjin tracking info", e);
            return TrackingResult.error("한진택배 서비스 오류: " + e.getMessage());
        }
    }
    
    /**
     * 롯데택배 API 연동
     */
    private TrackingResult getLotteTrackingInfo(String trackingNumber) {
        try {
            String url = lotteApiUrl + "?InvNo=" + trackingNumber;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "YCS-LMS/1.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseLotteResponse(response.getBody(), trackingNumber);
            } else {
                return TrackingResult.error("롯데택배 서비스에서 응답을 받지 못했습니다.");
            }
            
        } catch (Exception e) {
            log.error("Error fetching Lotte tracking info", e);
            return TrackingResult.error("롯데택배 서비스 오류: " + e.getMessage());
        }
    }
    
    /**
     * 우체국택배 API 연동
     */
    private TrackingResult getPostTrackingInfo(String trackingNumber) {
        try {
            // 실제로는 우체국택배 Open API를 사용해야 함
            // 여기서는 기본 구조만 제공
            log.info("Post tracking not implemented yet for: {}", trackingNumber);
            return TrackingResult.error("우체국택배 연동이 준비 중입니다.");
            
        } catch (Exception e) {
            log.error("Error fetching Post tracking info", e);
            return TrackingResult.error("우체국택배 서비스 오류: " + e.getMessage());
        }
    }
    
    /**
     * CJ대한통운 응답 파싱
     */
    private TrackingResult parseCJResponse(String jsonResponse, String trackingNumber) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            // CJ API 응답 구조에 맞춘 파싱
            if (root.has("result") && "Y".equals(root.get("result").asText())) {
                JsonNode trackingDetails = root.get("trackingDetails");
                
                if (trackingDetails != null && trackingDetails.isArray() && trackingDetails.size() > 0) {
                    JsonNode latestStatus = trackingDetails.get(trackingDetails.size() - 1);
                    
                    String status = latestStatus.get("stat").asText();
                    String location = latestStatus.get("location").asText();
                    String time = latestStatus.get("timeString").asText();
                    
                    List<TrackingDetail> details = new ArrayList<>();
                    for (JsonNode detail : trackingDetails) {
                        details.add(TrackingDetail.builder()
                            .time(detail.get("timeString").asText())
                            .location(detail.get("location").asText())
                            .status(detail.get("stat").asText())
                            .description(detail.get("remark").asText())
                            .build());
                    }
                    
                    return TrackingResult.success(
                        trackingNumber,
                        status,
                        location,
                        time,
                        details
                    );
                }
            }
            
            return TrackingResult.notFound("해당 송장번호를 찾을 수 없습니다.");
            
        } catch (Exception e) {
            log.error("Error parsing CJ response", e);
            return TrackingResult.error("CJ대한통운 응답 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 한진택배 응답 파싱 (HTML 파싱)
     */
    private TrackingResult parseHanjinResponse(String htmlResponse, String trackingNumber) {
        try {
            // HTML 응답에서 배송 정보 추출
            // 실제로는 JSoup 등을 사용하여 HTML 파싱
            
            if (htmlResponse.contains("배송완료") || htmlResponse.contains("배달완료")) {
                return TrackingResult.success(
                    trackingNumber,
                    "배송완료",
                    "수취인",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    Collections.emptyList()
                );
            } else if (htmlResponse.contains("배송중") || htmlResponse.contains("배송출발")) {
                return TrackingResult.success(
                    trackingNumber,
                    "배송중",
                    "배송지역",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    Collections.emptyList()
                );
            } else if (htmlResponse.contains("접수") || htmlResponse.contains("집화")) {
                return TrackingResult.success(
                    trackingNumber,
                    "접수",
                    "발송지역",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    Collections.emptyList()
                );
            } else {
                return TrackingResult.notFound("해당 송장번호를 찾을 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("Error parsing Hanjin response", e);
            return TrackingResult.error("한진택배 응답 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 롯데택배 응답 파싱
     */
    private TrackingResult parseLotteResponse(String jsonResponse, String trackingNumber) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            if (root.has("result") && "success".equals(root.get("result").asText())) {
                JsonNode data = root.get("data");
                
                String status = data.get("lastStatus").asText();
                String location = data.get("lastLocation").asText();
                String time = data.get("lastTime").asText();
                
                return TrackingResult.success(
                    trackingNumber,
                    status,
                    location,
                    time,
                    Collections.emptyList()
                );
            }
            
            return TrackingResult.notFound("해당 송장번호를 찾을 수 없습니다.");
            
        } catch (Exception e) {
            log.error("Error parsing Lotte response", e);
            return TrackingResult.error("롯데택배 응답 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * HTTP 클라이언트 에러 처리
     */
    private TrackingResult handleClientError(HttpClientErrorException e, String carrier, String trackingNumber) {
        return switch (e.getStatusCode().value()) {
            case 400 -> TrackingResult.error("잘못된 송장번호 형식입니다.");
            case 404 -> TrackingResult.notFound("해당 송장번호를 찾을 수 없습니다.");
            case 429 -> TrackingResult.error("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
            default -> TrackingResult.error(carrier + " 서비스 오류 (" + e.getStatusCode() + ")");
        };
    }
    
    // 내부 클래스들
    
    public static class TrackingResult {
        private final boolean success;
        private final String trackingNumber;
        private final String status;
        private final String location;
        private final String time;
        private final List<TrackingDetail> details;
        private final String errorMessage;
        
        private TrackingResult(boolean success, String trackingNumber, String status, 
                              String location, String time, List<TrackingDetail> details, String errorMessage) {
            this.success = success;
            this.trackingNumber = trackingNumber;
            this.status = status;
            this.location = location;
            this.time = time;
            this.details = details != null ? details : Collections.emptyList();
            this.errorMessage = errorMessage;
        }
        
        public static TrackingResult success(String trackingNumber, String status, String location, 
                                           String time, List<TrackingDetail> details) {
            return new TrackingResult(true, trackingNumber, status, location, time, details, null);
        }
        
        public static TrackingResult notFound(String message) {
            return new TrackingResult(false, null, null, null, null, null, message);
        }
        
        public static TrackingResult error(String message) {
            return new TrackingResult(false, null, null, null, null, null, message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getTrackingNumber() { return trackingNumber; }
        public String getStatus() { return status; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public List<TrackingDetail> getDetails() { return details; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class TrackingDetail {
        private final String time;
        private final String location;
        private final String status;
        private final String description;
        
        private TrackingDetail(String time, String location, String status, String description) {
            this.time = time;
            this.location = location;
            this.status = status;
            this.description = description;
        }
        
        public static TrackingDetailBuilder builder() {
            return new TrackingDetailBuilder();
        }
        
        // Getters
        public String getTime() { return time; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        
        public static class TrackingDetailBuilder {
            private String time;
            private String location;
            private String status;
            private String description;
            
            public TrackingDetailBuilder time(String time) { this.time = time; return this; }
            public TrackingDetailBuilder location(String location) { this.location = location; return this; }
            public TrackingDetailBuilder status(String status) { this.status = status; return this; }
            public TrackingDetailBuilder description(String description) { this.description = description; return this; }
            
            public TrackingDetail build() {
                return new TrackingDetail(time, location, status, description);
            }
        }
    }
}