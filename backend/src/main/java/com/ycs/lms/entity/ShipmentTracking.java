package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTracking {
    private Long id;
    private Long orderId;
    private Long orderBoxId;
    private String trackingNumber;
    private String carrier;
    private ShipmentType shipmentType;
    private TrackingStatus status;
    private String currentLocation;
    private String estimatedDeliveryDate;
    private String actualDeliveryDate;
    private LocalDateTime shippedDate;
    private LocalDateTime lastUpdated;
    private String trackingDetails; // JSON 형태의 상세 추적 정보
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ShipmentType {
        AIR,          // 항공
        SEA,          // 해상
        EXPRESS,      // 특급
        STANDARD,     // 일반
        ECONOMY       // 이코노미
    }

    public enum TrackingStatus {
        PENDING,      // 발송대기
        SHIPPED,      // 발송됨
        IN_TRANSIT,   // 운송중
        OUT_FOR_DELIVERY, // 배송중
        DELIVERED,    // 배송완료
        EXCEPTION,    // 예외상황
        DELAYED,      // 지연
        LOST,         // 분실
        RETURNED      // 반송
    }
}