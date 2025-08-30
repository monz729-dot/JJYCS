package com.ysc.lms.controller;

import com.ysc.lms.entity.PhotoRequirement;
import com.ysc.lms.service.PhotoRequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * P0-5: 사진 촬영 의무 구간 시스템 API
 * - 입고/리패킹/출고 단계별 사진 촬영 요구사항 관리
 * - 사진 업로드 및 품질 검증
 * - 알림 및 에스컬레이션 처리
 */
@RestController
@RequestMapping("/api/photo-requirements")
@RequiredArgsConstructor
@Slf4j
public class PhotoRequirementController {
    
    private final PhotoRequirementService photoRequirementService;
    
    /**
     * 주문에 대한 기본 사진 요구사항 생성
     */
    @PostMapping("/orders/{orderId}/create-default")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createDefaultRequirements(@PathVariable Long orderId) {
        try {
            List<PhotoRequirement> requirements = photoRequirementService.createDefaultRequirementsForOrder(orderId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirements,
                "count", requirements.size(),
                "message", "기본 사진 요구사항이 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error creating default photo requirements for order: {}", orderId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 박스별 사진 요구사항 생성
     */
    @PostMapping("/orders/{orderId}/boxes/{boxId}/requirements")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBoxRequirement(
            @PathVariable Long orderId,
            @PathVariable Long boxId,
            @RequestBody CreateBoxRequirementRequest request) {
        try {
            PhotoRequirement requirement = photoRequirementService.createRequirementForBox(
                orderId, boxId, request.getStage());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirement,
                "message", "박스별 사진 요구사항이 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error creating box photo requirement for order: {}, box: {}", orderId, boxId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사진 업로드
     */
    @PostMapping("/{requirementId}/upload")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @PathVariable Long requirementId,
            @RequestBody PhotoUploadRequestDto request) {
        try {
            PhotoRequirementService.PhotoUploadRequest serviceRequest = convertToUploadRequest(request);
            PhotoRequirement requirement = photoRequirementService.uploadPhoto(requirementId, serviceRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirement,
                "photoCount", requirement.getCompletedPhotoCount(),
                "message", "사진이 업로드되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error uploading photo for requirement: {}", requirementId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사진 품질 검증 및 승인/거부
     */
    @PostMapping("/{requirementId}/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> validatePhotos(
            @PathVariable Long requirementId,
            @RequestBody PhotoValidationRequestDto request) {
        try {
            PhotoRequirementService.PhotoValidationRequest serviceRequest = convertToValidationRequest(request);
            PhotoRequirement requirement = photoRequirementService.validatePhotos(requirementId, serviceRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirement,
                "validated", requirement.getStatus() == PhotoRequirement.RequirementStatus.COMPLETED,
                "message", request.isApproved() ? "사진이 승인되었습니다." : "사진이 거부되었습니다. 재촬영이 필요합니다."
            ));
            
        } catch (Exception e) {
            log.error("Error validating photos for requirement: {}", requirementId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사진 촬영 면제 처리
     */
    @PostMapping("/{requirementId}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> waiveRequirement(
            @PathVariable Long requirementId,
            @RequestBody WaiveRequirementRequest request) {
        try {
            PhotoRequirement requirement = photoRequirementService.waiveRequirement(
                requirementId, request.getReason(), request.getProcessedBy());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirement,
                "message", "사진 촬영이 면제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error waiving photo requirement: {}", requirementId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사용자별 미완료 사진 요구사항 조회
     */
    @GetMapping("/users/{userId}/incomplete")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getIncompleteRequirements(@PathVariable Long userId) {
        try {
            List<PhotoRequirement> requirements = photoRequirementService.getIncompleteRequirementsByUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirements,
                "count", requirements.size(),
                "message", "미완료 사진 요구사항이 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting incomplete requirements for user: {}", userId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 급한 사진 요구사항 조회 (관리자/창고용)
     */
    @GetMapping("/urgent")
    @PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUrgentRequirements() {
        try {
            List<PhotoRequirement> requirements = photoRequirementService.getUrgentRequirements();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requirements,
                "count", requirements.size(),
                "message", "급한 사진 요구사항이 조회되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting urgent photo requirements", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 주문별 사진 요구사항 조회
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRequirementsByOrder(@PathVariable Long orderId) {
        try {
            // PhotoRequirementService에 메서드 추가 필요 또는 Repository 직접 사용
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문별 사진 요구사항 조회 API는 구현 예정입니다.",
                "orderId", orderId
            ));
            
        } catch (Exception e) {
            log.error("Error getting photo requirements for order: {}", orderId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 주문의 모든 필수 사진 완료 여부 확인
     */
    @GetMapping("/orders/{orderId}/completion-status")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkCompletionStatus(@PathVariable Long orderId) {
        try {
            boolean allCompleted = photoRequirementService.areAllRequiredPhotosCompleted(orderId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "orderId", orderId,
                    "allRequiredPhotosCompleted", allCompleted,
                    "canProceedToNextStage", allCompleted
                ),
                "message", allCompleted ? "모든 필수 사진이 완료되었습니다." : "아직 완료되지 않은 필수 사진이 있습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error checking photo completion status for order: {}", orderId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사진 요구사항 상세 조회
     */
    @GetMapping("/{requirementId}")
    @PreAuthorize("hasRole('USER') or hasRole('CORPORATE') or hasRole('PARTNER') or hasRole('WAREHOUSE') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRequirement(@PathVariable Long requirementId) {
        try {
            // PhotoRequirementService에 조회 메서드 추가 필요
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사진 요구사항 상세 조회 API는 구현 예정입니다.",
                "requirementId", requirementId
            ));
            
        } catch (Exception e) {
            log.error("Error getting photo requirement: {}", requirementId, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 사진 요구사항 통계 조회 (관리자용)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            // 통계 조회 로직 구현 필요
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사진 요구사항 통계 API는 구현 예정입니다."
            ));
            
        } catch (Exception e) {
            log.error("Error getting photo requirements statistics", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    private PhotoRequirementService.PhotoUploadRequest convertToUploadRequest(PhotoUploadRequestDto dto) {
        PhotoRequirementService.PhotoUploadRequest request = new PhotoRequirementService.PhotoUploadRequest();
        request.setFileName(dto.getFileName());
        request.setFileSize(dto.getFileSize());
        request.setPhotoType(dto.getPhotoType());
        request.setUploadedBy(dto.getUploadedBy());
        request.setDeviceInfo(dto.getDeviceInfo());
        request.setGpsLocation(dto.getGpsLocation());
        return request;
    }
    
    private PhotoRequirementService.PhotoValidationRequest convertToValidationRequest(PhotoValidationRequestDto dto) {
        PhotoRequirementService.PhotoValidationRequest request = new PhotoRequirementService.PhotoValidationRequest();
        request.setApproved(dto.isApproved());
        request.setComments(dto.getComments());
        request.setValidatedBy(dto.getValidatedBy());
        return request;
    }
    
    // DTO 클래스들
    
    public static class CreateBoxRequirementRequest {
        private PhotoRequirement.PhotoStage stage;
        
        public PhotoRequirement.PhotoStage getStage() { return stage; }
        public void setStage(PhotoRequirement.PhotoStage stage) { this.stage = stage; }
    }
    
    public static class PhotoUploadRequestDto {
        private String fileName;
        private Long fileSize;
        private String photoType;
        private String uploadedBy;
        private String deviceInfo;
        private String gpsLocation;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getPhotoType() { return photoType; }
        public void setPhotoType(String photoType) { this.photoType = photoType; }
        
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
        
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
        
        public String getGpsLocation() { return gpsLocation; }
        public void setGpsLocation(String gpsLocation) { this.gpsLocation = gpsLocation; }
    }
    
    public static class PhotoValidationRequestDto {
        private boolean approved;
        private String comments;
        private String validatedBy;
        
        // Getters and Setters
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        
        public String getValidatedBy() { return validatedBy; }
        public void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }
    }
    
    public static class WaiveRequirementRequest {
        private String reason;
        private String processedBy;
        
        // Getters and Setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getProcessedBy() { return processedBy; }
        public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    }
}