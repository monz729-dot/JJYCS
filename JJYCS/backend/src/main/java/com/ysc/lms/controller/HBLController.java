package com.ysc.lms.controller;

import com.ysc.lms.entity.HBL;
import com.ysc.lms.service.HBLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * P0-4: HBL 발행 및 라벨링 시스템 API
 * - HBL (House Bill of Lading) 생성/발행/인쇄
 * - 라벨 및 QR 코드 생성
 * - 운송서류 관리
 */
@RestController
@RequestMapping("/api/hbl")
@RequiredArgsConstructor
@Slf4j
public class HBLController {
    
    private final HBLService hblService;
    
    /**
     * HBL 생성 (초안 상태)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createHBL(@RequestBody CreateHBLRequest request) {
        try {
            HBLService.HBLCreateRequest serviceRequest = convertToServiceRequest(request);
            HBL hbl = hblService.createHBL(request.getOrderId(), serviceRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbl,
                "message", "HBL이 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error creating HBL for order: {}", request.getOrderId(), e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * HBL 승인 및 발행
     */
    @PostMapping("/{hblId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveAndIssueHBL(
            @PathVariable Long hblId,
            @RequestBody ApproveHBLRequest request) {
        try {
            HBL hbl = hblService.approveAndIssueHBL(hblId, request.getApprover());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbl,
                "message", "HBL이 승인 및 발행되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error approving HBL: {}", hblId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 라벨 생성 및 인쇄
     */
    @PostMapping("/{hblId}/generate-labels")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateLabels(
            @PathVariable Long hblId,
            @RequestBody GenerateLabelsRequest request) {
        try {
            HBLService.LabelGenerationRequest serviceRequest = new HBLService.LabelGenerationRequest();
            serviceRequest.setLabelFormat(request.getLabelFormat());
            serviceRequest.setLabelSize(request.getLabelSize());
            serviceRequest.setIncludeQrCode(request.isIncludeQrCode());
            serviceRequest.setIncludeBarcodes(request.isIncludeBarcodes());
            
            HBL hbl = hblService.generateLabelsAndPrint(hblId, serviceRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbl,
                "labelCount", hbl.getLabelPaths().size(),
                "message", "라벨이 생성되고 인쇄 완료되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error generating labels for HBL: {}", hblId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * HBL 사본 발행
     */
    @PostMapping("/{hblId}/copy")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> issueCopy(
            @PathVariable Long hblId,
            @RequestBody IssueCopyRequest request) {
        try {
            HBL hbl = hblService.issueCopy(hblId, request.getRequester());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbl,
                "copyCount", hbl.getCopyCount(),
                "message", "HBL 사본이 발행되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error issuing copy for HBL: {}", hblId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사용자별 HBL 조회
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHBLsByUser(@PathVariable Long userId) {
        try {
            List<HBL> hbls = hblService.getHBLsByUserId(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbls,
                "count", hbls.size(),
                "message", "사용자별 HBL이 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting HBLs for user: {}", userId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 발행 대기 중인 HBL 조회 (관리자용)
     */
    @GetMapping("/pending-issuance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingIssuanceHBLs() {
        try {
            List<HBL> hbls = hblService.getPendingIssuanceHBLs();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbls,
                "count", hbls.size(),
                "message", "발행 대기 중인 HBL이 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting pending issuance HBLs", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 인쇄 대기 중인 HBL 조회 (창고용)
     */
    @GetMapping("/pending-print")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingPrintHBLs() {
        try {
            List<HBL> hbls = hblService.getPendingPrintHBLs();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hbls,
                "count", hbls.size(),
                "message", "인쇄 대기 중인 HBL이 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting pending print HBLs", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * HBL 상세 조회
     */
    @GetMapping("/{hblId}")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHBL(@PathVariable Long hblId) {
        try {
            // HBL 조회 로직 (HBLService에 메서드 추가 필요하거나 Repository 직접 사용)
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "HBL 상세 조회 API는 구현 예정입니다.",
                "hblId", hblId
            ));
            
        } catch (Exception e) {
            log.error("Error getting HBL: {}", hblId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * HBL 번호로 검색
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchHBL(@RequestParam String hblNumber) {
        try {
            // HBL 검색 로직 (HBLService에 메서드 추가 필요)
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "HBL 검색 API는 구현 예정입니다.",
                "searchQuery", hblNumber
            ));
            
        } catch (Exception e) {
            log.error("Error searching HBL: {}", hblNumber, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    private HBLService.HBLCreateRequest convertToServiceRequest(CreateHBLRequest request) {
        HBLService.HBLCreateRequest serviceRequest = new HBLService.HBLCreateRequest();
        serviceRequest.setShipperName(request.getShipperName());
        serviceRequest.setShipperAddress(request.getShipperAddress());
        serviceRequest.setShipperPhone(request.getShipperPhone());
        serviceRequest.setNotifyPartyName(request.getNotifyPartyName());
        serviceRequest.setNotifyPartyAddress(request.getNotifyPartyAddress());
        serviceRequest.setNotifyPartyPhone(request.getNotifyPartyPhone());
        serviceRequest.setPortOfLoading(request.getPortOfLoading());
        serviceRequest.setPortOfDischarge(request.getPortOfDischarge());
        serviceRequest.setPlaceOfDelivery(request.getPlaceOfDelivery());
        serviceRequest.setVesselName(request.getVesselName());
        serviceRequest.setVoyageNumber(request.getVoyageNumber());
        serviceRequest.setSpecialInstructions(request.getSpecialInstructions());
        serviceRequest.setRemarks(request.getRemarks());
        return serviceRequest;
    }
    
    // DTO 클래스들
    
    public static class CreateHBLRequest {
        private Long orderId;
        private String shipperName;
        private String shipperAddress;
        private String shipperPhone;
        private String notifyPartyName;
        private String notifyPartyAddress;
        private String notifyPartyPhone;
        private String portOfLoading;
        private String portOfDischarge;
        private String placeOfDelivery;
        private String vesselName;
        private String voyageNumber;
        private String specialInstructions;
        private String remarks;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public String getShipperName() { return shipperName; }
        public void setShipperName(String shipperName) { this.shipperName = shipperName; }
        
        public String getShipperAddress() { return shipperAddress; }
        public void setShipperAddress(String shipperAddress) { this.shipperAddress = shipperAddress; }
        
        public String getShipperPhone() { return shipperPhone; }
        public void setShipperPhone(String shipperPhone) { this.shipperPhone = shipperPhone; }
        
        public String getNotifyPartyName() { return notifyPartyName; }
        public void setNotifyPartyName(String notifyPartyName) { this.notifyPartyName = notifyPartyName; }
        
        public String getNotifyPartyAddress() { return notifyPartyAddress; }
        public void setNotifyPartyAddress(String notifyPartyAddress) { this.notifyPartyAddress = notifyPartyAddress; }
        
        public String getNotifyPartyPhone() { return notifyPartyPhone; }
        public void setNotifyPartyPhone(String notifyPartyPhone) { this.notifyPartyPhone = notifyPartyPhone; }
        
        public String getPortOfLoading() { return portOfLoading; }
        public void setPortOfLoading(String portOfLoading) { this.portOfLoading = portOfLoading; }
        
        public String getPortOfDischarge() { return portOfDischarge; }
        public void setPortOfDischarge(String portOfDischarge) { this.portOfDischarge = portOfDischarge; }
        
        public String getPlaceOfDelivery() { return placeOfDelivery; }
        public void setPlaceOfDelivery(String placeOfDelivery) { this.placeOfDelivery = placeOfDelivery; }
        
        public String getVesselName() { return vesselName; }
        public void setVesselName(String vesselName) { this.vesselName = vesselName; }
        
        public String getVoyageNumber() { return voyageNumber; }
        public void setVoyageNumber(String voyageNumber) { this.voyageNumber = voyageNumber; }
        
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
        
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
    
    public static class ApproveHBLRequest {
        private String approver;
        
        public String getApprover() { return approver; }
        public void setApprover(String approver) { this.approver = approver; }
    }
    
    public static class GenerateLabelsRequest {
        private String labelFormat = "PDF";
        private String labelSize = "A4";
        private boolean includeQrCode = true;
        private boolean includeBarcodes = true;
        
        public String getLabelFormat() { return labelFormat; }
        public void setLabelFormat(String labelFormat) { this.labelFormat = labelFormat; }
        
        public String getLabelSize() { return labelSize; }
        public void setLabelSize(String labelSize) { this.labelSize = labelSize; }
        
        public boolean isIncludeQrCode() { return includeQrCode; }
        public void setIncludeQrCode(boolean includeQrCode) { this.includeQrCode = includeQrCode; }
        
        public boolean isIncludeBarcodes() { return includeBarcodes; }
        public void setIncludeBarcodes(boolean includeBarcodes) { this.includeBarcodes = includeBarcodes; }
    }
    
    public static class IssueCopyRequest {
        private String requester;
        
        public String getRequester() { return requester; }
        public void setRequester(String requester) { this.requester = requester; }
    }
}