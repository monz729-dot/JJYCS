package com.ycs.lms.controller;

import com.ycs.lms.util.ApiResponse;
import com.ycs.lms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 배송 추적 컨트롤러
 */
@Tag(name = "Tracking", description = "배송 추적 API")
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    @Operation(summary = "배송 추적 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getTrackingList(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting tracking list for user: {}", userPrincipal.getId());
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("content", Arrays.asList(
            Map.of("trackingNumber", "KR123456789TH", "orderCode", "ORD202501001", "status", "in_transit", "carrier", "Korea Post"),
            Map.of("trackingNumber", "KR987654321TH", "orderCode", "ORD202501002", "status", "delivered", "carrier", "CJ Logistics")
        ));
        mockData.put("totalElements", 2);
        
        return ResponseEntity.ok(ApiResponse.success(mockData));
    }

    @Operation(summary = "배송 추적 상세")
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse<Object>> getTrackingDetail(
            @PathVariable String trackingNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting tracking detail for: {}", trackingNumber);
        
        Map<String, Object> mockData = Map.of(
            "trackingNumber", trackingNumber,
            "status", "in_transit",
            "currentLocation", "Bangkok International Airport",
            "estimatedDelivery", "2025-01-20",
            "events", Arrays.asList(
                Map.of("timestamp", "2025-01-15T10:00:00Z", "location", "Seoul", "description", "Departed from origin"),
                Map.of("timestamp", "2025-01-16T14:30:00Z", "location", "Bangkok", "description", "Arrived at destination")
            )
        );
        
        return ResponseEntity.ok(ApiResponse.success(mockData));
    }

    @Operation(summary = "배송 상태 새로고침")
    @PostMapping("/{trackingNumber}/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshTrackingStatus(
            @PathVariable String trackingNumber,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Refreshing tracking status for: {}", trackingNumber);
        
        Map<String, Object> response = Map.of(
            "trackingNumber", trackingNumber,
            "status", "updated",
            "lastUpdated", new Date().toString()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "배송 상태가 업데이트되었습니다."));
    }

    @Operation(summary = "운송장 번호 등록")
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> registerTracking(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Registering tracking by user: {}", userPrincipal.getId());
        
        Map<String, Object> response = Map.of(
            "id", 123L,
            "trackingNumber", request.get("trackingNumber"),
            "carrier", request.get("carrier"),
            "status", "registered"
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "운송장 번호가 등록되었습니다."));
    }
}