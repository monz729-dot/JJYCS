package com.ysc.lms.controller;

import com.ysc.lms.entity.Parcel;
import com.ysc.lms.service.ParcelTrackingService;
import com.ysc.lms.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 택배/송장 관리 API
 */
@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
@Slf4j
public class ParcelController {
    
    private final ParcelTrackingService parcelTrackingService;
    private final ParcelRepository parcelRepository;
    
    /**
     * 송장번호 유효성 확인 (주문폼에서 사용)
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTrackingNumber(
            @RequestBody ValidationRequest request) {
        try {
            ParcelTrackingService.ValidationResult result = 
                parcelTrackingService.validateTrackingNumber(request.carrier, request.trackingNumber);
            
            String badgeClass = switch (result.getStatus()) {
                case VALID -> "success";
                case INVALID -> "error";
                case ERROR -> "warning";
                default -> "info";
            };
            
            return ResponseEntity.ok(Map.of(
                "success", result.isValid(),
                "status", result.getStatus().toString(),
                "statusDescription", result.getStatus().getDescription(),
                "message", result.getMessage(),
                "trackingStatus", result.getTrackingStatus() != null ? result.getTrackingStatus() : "",
                "badgeClass", badgeClass,
                "isValid", result.isValid()
            ));
            
        } catch (Exception e) {
            log.error("Error validating tracking number: {} for carrier: {}", 
                     request.trackingNumber, request.carrier, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "status", "ERROR",
                    "message", "송장번호 검증 중 오류가 발생했습니다.",
                    "badgeClass", "error",
                    "isValid", false
                ));
        }
    }
    
    /**
     * 입고 스캔 매칭 (창고에서 사용)
     */
    @PostMapping("/scan/inbound")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> scanInbound(@RequestBody ScanRequest request) {
        try {
            ParcelTrackingService.MatchingResult result = 
                parcelTrackingService.matchInboundScan(request.trackingNumber);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.getMessage(),
                    "order", result.getOrder(),
                    "matchType", "AUTO"
                ));
            } else if (result.isNeedsManualSearch()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", result.getMessage(),
                    "needsManualSearch", true,
                    "matchType", "MANUAL"
                ));
            } else if (result.isAlreadyMatched()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", result.getMessage(),
                        "alreadyMatched", true
                    ));
            } else {
                return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "message", result.getMessage()
                    ));
            }
            
        } catch (Exception e) {
            log.error("Error scanning inbound parcel: {}", request.trackingNumber, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "입고 스캔 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 수동 매칭 검색
     */
    @PostMapping("/search/manual-match")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchManualMatch(@RequestBody SearchRequest request) {
        try {
            List<Parcel> results = parcelTrackingService.searchForManualMatching(request.keyword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "results", results,
                "count", results.size()
            ));
            
        } catch (Exception e) {
            log.error("Error searching for manual match with keyword: {}", request.keyword, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "수동 매칭 검색 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 수동 매칭 처리
     */
    @PostMapping("/match/manual")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> manualMatch(@RequestBody ManualMatchRequest request) {
        try {
            parcelTrackingService.manualMatch(request.parcelId, request.notes);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수동 매칭이 완료되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error processing manual match for parcel: {}", request.parcelId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "수동 매칭 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 미매칭 택배 목록 조회
     */
    @GetMapping("/unmatched")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUnmatchedParcels() {
        try {
            List<Parcel> unmatchedParcels = parcelRepository.findUnmatchedInboundParcels();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "parcels", unmatchedParcels,
                "count", unmatchedParcels.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting unmatched parcels", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "미매칭 택배 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 택배 통계 조회
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            List<Object[]> carrierStats = parcelRepository.findCarrierStatistics();
            List<Object[]> statusStats = parcelRepository.findValidationStatusStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "carrierStatistics", carrierStats,
                "validationStatusStatistics", statusStats
            ));
            
        } catch (Exception e) {
            log.error("Error getting parcel statistics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "택배 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    // DTO 클래스들
    
    public static class ValidationRequest {
        public String carrier;
        public String trackingNumber;
        
        public String getCarrier() { return carrier; }
        public void setCarrier(String carrier) { this.carrier = carrier; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }
    
    public static class ScanRequest {
        public String trackingNumber;
        
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }
    
    public static class SearchRequest {
        public String keyword;
        
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
    }
    
    public static class ManualMatchRequest {
        public Long parcelId;
        public String notes;
        
        public Long getParcelId() { return parcelId; }
        public void setParcelId(Long parcelId) { this.parcelId = parcelId; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}