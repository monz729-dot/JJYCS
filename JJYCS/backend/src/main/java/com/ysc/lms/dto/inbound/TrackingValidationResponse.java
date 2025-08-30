package com.ysc.lms.dto.inbound;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 송장번호 유효성 검사 응답 DTO
 */
@Data
@Builder
public class TrackingValidationResponse {
    
    // 유효성 검사 결과
    private boolean isValid;
    
    // 검증 메시지
    private String message;
    
    // 오류 목록 (유효하지 않은 경우)
    private List<String> errors;
    
    // 기본 배송 정보 (유효한 경우)
    private TrackingBasicInfo basicInfo;
    
    // 매칭된 주문 정보 (있는 경우)
    private MatchedOrderInfo matchedOrder;
    
    @Data
    @Builder
    public static class TrackingBasicInfo {
        private String trackingNumber;
        private String carrier;
        private String carrierName;
        private String status;
        private String lastLocation;
        private LocalDateTime lastUpdated;
        private LocalDateTime estimatedDelivery;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private Double weight;
        private String packageType;
        private String serviceType;
    }
    
    @Data
    @Builder
    public static class MatchedOrderInfo {
        private Long orderId;
        private String orderNumber;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String orderStatus;
        private LocalDateTime orderDate;
        private Double totalAmount;
        private Integer itemCount;
        private String specialRequests;
        private boolean requiresInspection;
        private boolean requiresPhotos;
    }
}