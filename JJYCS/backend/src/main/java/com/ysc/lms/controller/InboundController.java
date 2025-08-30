package com.ysc.lms.controller;

import com.ysc.lms.dto.ApiResponse;
import com.ysc.lms.dto.inbound.InboundConfirmationRequest;
import com.ysc.lms.dto.inbound.TrackingValidationRequest;
import com.ysc.lms.dto.inbound.TrackingValidationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 입고 관리 컨트롤러 - 목업 버전
 */
@RestController
@RequestMapping("/api/admin/inbound")
@RequiredArgsConstructor
@Slf4j
public class InboundController {
    
    /**
     * 송장번호 유효성 검사 및 기본 정보 조회 (목업)
     */
    @PostMapping("/validate-tracking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<ApiResponse<TrackingValidationResponse>> validateTracking(
            @Valid @RequestBody TrackingValidationRequest request) {
        
        log.info("Validating tracking number: {} for carrier: {}", request.getTrackingNumber(), request.getCarrier());
        
        try {
            // 목업 응답 생성
            TrackingValidationResponse.TrackingBasicInfo basicInfo = TrackingValidationResponse.TrackingBasicInfo.builder()
                    .trackingNumber(request.getTrackingNumber())
                    .carrier(request.getCarrier())
                    .carrierName("CJ대한통운")
                    .status("IN_TRANSIT")
                    .lastLocation("Seoul Distribution Center")
                    .recipientName("Test User")
                    .recipientPhone("010-1234-5678")
                    .recipientAddress("Seoul, Korea")
                    .weight(1.5)
                    .packageType("Box")
                    .serviceType("Standard")
                    .build();
            
            TrackingValidationResponse response = TrackingValidationResponse.builder()
                    .isValid(true)
                    .message("유효한 송장번호입니다.")
                    .basicInfo(basicInfo)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("송장번호가 유효합니다.", response));
            
        } catch (Exception e) {
            log.error("Error validating tracking number: {}", request.getTrackingNumber(), e);
            return ResponseEntity.ok(ApiResponse.error("송장번호 검증 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 송장 정보 상세 조회 (목업)
     */
    @GetMapping("/tracking-info/{carrier}/{trackingNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrackingInfo(
            @PathVariable String carrier,
            @PathVariable String trackingNumber) {
        
        log.info("Getting tracking info for: {} from carrier: {}", trackingNumber, carrier);
        
        try {
            // 목업 응답 생성
            Map<String, Object> trackingInfo = new HashMap<>();
            trackingInfo.put("trackingNumber", trackingNumber);
            trackingInfo.put("carrier", carrier);
            trackingInfo.put("status", "IN_TRANSIT");
            trackingInfo.put("lastLocation", "Seoul Distribution Center");
            trackingInfo.put("recipient", "Test User");
            trackingInfo.put("recipientPhone", "010-1234-5678");
            trackingInfo.put("recipientAddress", "Seoul, Korea");
            trackingInfo.put("packageType", "Box");
            trackingInfo.put("serviceType", "Standard");
            trackingInfo.put("estimatedDelivery", "2025-09-01");
            
            return ResponseEntity.ok(ApiResponse.success("송장 정보를 조회했습니다.", trackingInfo));
            
        } catch (Exception e) {
            log.error("Error getting tracking info for: {}", trackingNumber, e);
            return ResponseEntity.ok(ApiResponse.error("송장 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 입고 확인 처리 (목업)
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmInbound(
            @Valid @RequestBody InboundConfirmationRequest request) {
        
        log.info("Confirming inbound for tracking number: {}", request.getTrackingNumber());
        
        try {
            // 목업 응답 생성
            Map<String, Object> result = new HashMap<>();
            result.put("trackingNumber", request.getTrackingNumber());
            result.put("status", "CONFIRMED");
            result.put("inboundId", "INB" + System.currentTimeMillis());
            result.put("confirmedAt", java.time.LocalDateTime.now());
            result.put("confirmedBy", "admin@ysc.com");
            result.put("storageLocation", request.getStorageLocation());
            
            return ResponseEntity.ok(ApiResponse.success("입고가 확인되었습니다.", result));
            
        } catch (Exception e) {
            log.error("Error confirming inbound for tracking number: {}", request.getTrackingNumber(), e);
            return ResponseEntity.ok(ApiResponse.error("입고 확인 중 오류가 발생했습니다."));
        }
    }
}