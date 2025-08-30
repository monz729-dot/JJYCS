package com.ysc.lms.service;

import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.OrderBox;
import com.ysc.lms.entity.PhotoRequirement;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.repository.PhotoRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * P0-5: 사진 촬영 의무 구간 서비스
 * - 입고/리패킹/출고 단계별 사진 촬영 요구사항 관리
 * - 자동 알림 및 에스컬레이션
 * - 품질 검증 및 승인 프로세스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoRequirementService {
    
    private final PhotoRequirementRepository photoRequirementRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService; // 알림 서비스 (추후 구현)
    
    @Value("${app.photo.storage.path:/photos}")
    private String photoStoragePath;
    
    @Value("${app.photo.max-file-size:5242880}") // 5MB
    private long maxFileSize;
    
    @Value("${app.photo.allowed-extensions:jpg,jpeg,png,bmp}")
    private String allowedExtensions;
    
    /**
     * 주문에 대한 기본 사진 요구사항 생성
     */
    @Transactional
    public List<PhotoRequirement> createDefaultRequirementsForOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            log.info("Creating default photo requirements for order: {}", orderId);
            
            List<PhotoRequirement> requirements = new ArrayList<>();
            
            // 1. 입고 단계 사진 요구사항
            PhotoRequirement inboundReq = createRequirement(
                order, null, PhotoRequirement.PhotoStage.INBOUND);
            inboundReq.setInstructions("화물 전체 모습과 포장 상태를 명확하게 촬영해주세요.");
            inboundReq.setMinPhotoCount(2);
            inboundReq.setRequireOverallView(true);
            inboundReq.setRequireDamageCheck(true);
            requirements.add(inboundReq);
            
            // 2. 리패킹 단계 사진 요구사항 (리패킹이 필요한 경우에만)
            if (order.getRepackingRequested() != null && order.getRepackingRequested()) {
                PhotoRequirement repackReq = createRequirement(
                    order, null, PhotoRequirement.PhotoStage.REPACK);
                repackReq.setInstructions("리패킹 전후 상태를 비교할 수 있도록 촬영해주세요.");
                repackReq.setMinPhotoCount(3);
                repackReq.setRequireOverallView(true);
                repackReq.setRequireDetailView(true);
                repackReq.setRequireContentView(true);
                requirements.add(repackReq);
            }
            
            // 3. 출고 단계 사진 요구사항
            PhotoRequirement outboundReq = createRequirement(
                order, null, PhotoRequirement.PhotoStage.OUTBOUND);
            outboundReq.setInstructions("최종 포장 상태와 라벨을 명확하게 촬영해주세요.");
            outboundReq.setMinPhotoCount(2);
            outboundReq.setRequireOverallView(true);
            outboundReq.setRequireLabelView(true);
            requirements.add(outboundReq);
            
            // 4. 고가품 또는 특별 취급 상품의 경우 추가 요구사항
            if (isHighValueOrder(order) || isSpecialHandlingRequired(order)) {
                PhotoRequirement specialReq = createRequirement(
                    order, null, PhotoRequirement.PhotoStage.SPECIAL);
                specialReq.setInstructions("고가품 또는 특별 취급 상품으로 추가 촬영이 필요합니다.");
                specialReq.setMinPhotoCount(1);
                specialReq.setMaxPhotoCount(5);
                specialReq.setRequireDetailView(true);
                requirements.add(specialReq);
            }
            
            List<PhotoRequirement> savedRequirements = photoRequirementRepository.saveAll(requirements);
            log.info("Created {} photo requirements for order: {}", savedRequirements.size(), orderId);
            
            return savedRequirements;
            
        } catch (Exception e) {
            log.error("Error creating photo requirements for order: {}", orderId, e);
            throw new RuntimeException("사진 요구사항 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 박스별 사진 요구사항 생성
     */
    @Transactional
    public PhotoRequirement createRequirementForBox(Long orderId, Long boxId, 
                                                   PhotoRequirement.PhotoStage stage) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            OrderBox box = order.getBoxes().stream()
                .filter(b -> b.getId().equals(boxId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("박스를 찾을 수 없습니다: " + boxId));
            
            log.info("Creating photo requirement for box: {} in order: {}", boxId, orderId);
            
            PhotoRequirement requirement = createRequirement(order, box, stage);
            requirement.setInstructions(String.format("%s 단계 박스별 촬영 - Box %s", 
                                                     stage.getKoreanName(), box.getBoxNumber()));
            
            return photoRequirementRepository.save(requirement);
            
        } catch (Exception e) {
            log.error("Error creating photo requirement for box: {} in order: {}", boxId, orderId, e);
            throw new RuntimeException("박스별 사진 요구사항 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사진 업로드 및 요구사항 완료 처리
     */
    @Transactional
    public PhotoRequirement uploadPhoto(Long requirementId, PhotoUploadRequest request) {
        try {
            PhotoRequirement requirement = photoRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new IllegalArgumentException("사진 요구사항을 찾을 수 없습니다: " + requirementId));
            
            if (requirement.getStatus() == PhotoRequirement.RequirementStatus.COMPLETED ||
                requirement.getStatus() == PhotoRequirement.RequirementStatus.WAIVED) {
                throw new IllegalStateException("이미 완료된 사진 요구사항입니다.");
            }
            
            // 파일 크기 및 확장자 검증
            validatePhotoFile(request);
            
            log.info("Uploading photo for requirement: {} by user: {}", requirementId, request.getUploadedBy());
            
            // 파일 저장 (실제로는 파일 시스템이나 S3에 저장)
            String filePath = savePhotoFile(requirement, request);
            
            // 사진 메타데이터와 함께 요구사항 업데이트
            requirement.addPhotoFile(filePath, request.getPhotoType(), request.getUploadedBy());
            
            // 상태 업데이트
            if (requirement.getStatus() == PhotoRequirement.RequirementStatus.PENDING) {
                requirement.setStatus(PhotoRequirement.RequirementStatus.IN_PROGRESS);
            }
            
            PhotoRequirement savedRequirement = photoRequirementRepository.save(requirement);
            
            log.info("Photo uploaded successfully for requirement: {}, total photos: {}", 
                    requirementId, savedRequirement.getCompletedPhotoCount());
            
            return savedRequirement;
            
        } catch (Exception e) {
            log.error("Error uploading photo for requirement: {}", requirementId, e);
            throw new RuntimeException("사진 업로드 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사진 품질 검증 및 승인/거부
     */
    @Transactional
    public PhotoRequirement validatePhotos(Long requirementId, PhotoValidationRequest request) {
        try {
            PhotoRequirement requirement = photoRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new IllegalArgumentException("사진 요구사항을 찾을 수 없습니다: " + requirementId));
            
            log.info("Validating photos for requirement: {} by validator: {}", 
                    requirementId, request.getValidatedBy());
            
            if (request.isApproved()) {
                // 승인 처리
                requirement.setStatus(PhotoRequirement.RequirementStatus.COMPLETED);
                requirement.setCompletedAt(LocalDateTime.now());
                requirement.setCompletedBy(request.getValidatedBy());
                requirement.setRemarks("품질 검증 완료: " + request.getComments());
            } else {
                // 거부 처리 - 재촬영 필요
                requirement.rejectPhotos(request.getComments());
                // 재촬영 알림 발송
                scheduleRetakeNotification(requirement);
            }
            
            PhotoRequirement savedRequirement = photoRequirementRepository.save(requirement);
            
            log.info("Photo validation completed for requirement: {}, approved: {}", 
                    requirementId, request.isApproved());
            
            return savedRequirement;
            
        } catch (Exception e) {
            log.error("Error validating photos for requirement: {}", requirementId, e);
            throw new RuntimeException("사진 품질 검증 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사진 촬영 면제 처리
     */
    @Transactional
    public PhotoRequirement waiveRequirement(Long requirementId, String reason, String processedBy) {
        try {
            PhotoRequirement requirement = photoRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new IllegalArgumentException("사진 요구사항을 찾을 수 없습니다: " + requirementId));
            
            requirement.waiveRequirement(reason);
            requirement.setCompletedBy(processedBy);
            
            PhotoRequirement savedRequirement = photoRequirementRepository.save(requirement);
            
            log.info("Photo requirement waived: {} by {} - reason: {}", requirementId, processedBy, reason);
            
            return savedRequirement;
            
        } catch (Exception e) {
            log.error("Error waiving photo requirement: {}", requirementId, e);
            throw new RuntimeException("사진 촬영 면제 처리 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 기한 초과 및 에스컬레이션 처리 (스케줄링)
     */
    @Transactional
    public void processOverdueRequirements() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 기한 초과 요구사항 조회
            List<PhotoRequirement> overdueRequirements = photoRequirementRepository.findOverdueRequirements(now);
            
            for (PhotoRequirement requirement : overdueRequirements) {
                if (!requirement.getIsEscalated()) {
                    // 에스컬레이션 처리
                    requirement.escalate();
                    photoRequirementRepository.save(requirement);
                    
                    // 에스컬레이션 알림 발송
                    sendEscalationNotification(requirement);
                    
                    log.warn("Photo requirement escalated: {} for order: {}", 
                            requirement.getId(), requirement.getOrder().getOrderNumber());
                }
            }
            
            log.info("Processed {} overdue photo requirements", overdueRequirements.size());
            
        } catch (Exception e) {
            log.error("Error processing overdue photo requirements", e);
        }
    }
    
    /**
     * 알림 발송 처리 (기한 임박)
     */
    @Transactional
    public void sendDueNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime alertTime = now.plusHours(2); // 2시간 후 기한
            LocalDateTime lastAlertTime = now.minusHours(1); // 1시간 전 마지막 알림
            
            List<PhotoRequirement> requirementsForNotification = 
                photoRequirementRepository.findRequirementsForNotification(now, alertTime, lastAlertTime);
            
            for (PhotoRequirement requirement : requirementsForNotification) {
                sendDueNotification(requirement);
                requirement.recordNotificationSent();
                photoRequirementRepository.save(requirement);
            }
            
            log.info("Sent due notifications for {} photo requirements", requirementsForNotification.size());
            
        } catch (Exception e) {
            log.error("Error sending due notifications", e);
        }
    }
    
    /**
     * 사용자별 미완료 사진 요구사항 조회
     */
    public List<PhotoRequirement> getIncompleteRequirementsByUser(Long userId) {
        try {
            return photoRequirementRepository.findIncompleteRequirementsByUserId(userId);
        } catch (Exception e) {
            log.error("Error getting incomplete requirements for user: {}", userId, e);
            throw new RuntimeException("사용자별 미완료 사진 요구사항 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 급한 사진 요구사항 조회 (기한 임박 + 필수)
     */
    public List<PhotoRequirement> getUrgentRequirements() {
        try {
            LocalDateTime urgentTime = LocalDateTime.now().plusHours(1); // 1시간 내
            return photoRequirementRepository.findUrgentRequirements(urgentTime);
        } catch (Exception e) {
            log.error("Error getting urgent photo requirements", e);
            throw new RuntimeException("급한 사진 요구사항 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 주문의 모든 필수 사진 요구사항 완료 여부 확인
     */
    public boolean areAllRequiredPhotosCompleted(Long orderId) {
        try {
            Long incompleteCount = photoRequirementRepository.countIncompleteRequiredByOrderId(orderId);
            return incompleteCount == 0;
        } catch (Exception e) {
            log.error("Error checking photo completion for order: {}", orderId, e);
            return false;
        }
    }
    
    private PhotoRequirement createRequirement(Order order, OrderBox box, PhotoRequirement.PhotoStage stage) {
        PhotoRequirement requirement = new PhotoRequirement();
        requirement.setOrder(order);
        requirement.setOrderBox(box);
        requirement.setStage(stage);
        requirement.setStatus(PhotoRequirement.RequirementStatus.PENDING);
        requirement.setDefaultDueDate();
        return requirement;
    }
    
    private boolean isHighValueOrder(Order order) {
        // 고가품 판단 로직 (예: 총 가격이 1000 THB 초과)
        return order.getItems().stream()
            .mapToDouble(item -> {
                if (item.getUnitPrice() != null && item.getQuantity() != null) {
                    return item.getUnitPrice().doubleValue() * item.getQuantity().doubleValue();
                }
                return 0;
            })
            .sum() > 1000;
    }
    
    private boolean isSpecialHandlingRequired(Order order) {
        // 특별 취급 필요 판단 로직 (예: 깨지기 쉬운 물건, 전자제품 등)
        return order.getSpecialRequests() != null && !order.getSpecialRequests().trim().isEmpty();
    }
    
    private void validatePhotoFile(PhotoUploadRequest request) {
        if (request.getFileSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String extension = getFileExtension(request.getFileName());
        if (!allowedExtensions.toLowerCase().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용 형식: " + allowedExtensions);
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
    
    private String savePhotoFile(PhotoRequirement requirement, PhotoUploadRequest request) {
        // 실제 구현에서는 파일 시스템이나 클라우드 스토리지에 저장
        String fileName = String.format("%s_%s_%s_%s", 
            requirement.getOrder().getOrderNumber(),
            requirement.getStage().name(),
            System.currentTimeMillis(),
            request.getFileName()
        );
        
        String filePath = String.format("%s/%s", photoStoragePath, fileName);
        
        // TODO: 실제 파일 저장 로직 구현
        log.info("Photo file saved: {}", filePath);
        
        return filePath;
    }
    
    private void scheduleRetakeNotification(PhotoRequirement requirement) {
        // TODO: 재촬영 알림 발송 로직 구현
        log.info("Scheduling retake notification for requirement: {}", requirement.getId());
    }
    
    private void sendEscalationNotification(PhotoRequirement requirement) {
        // TODO: 에스컬레이션 알림 발송 로직 구현
        log.warn("Sending escalation notification for requirement: {}", requirement.getId());
    }
    
    private void sendDueNotification(PhotoRequirement requirement) {
        // TODO: 기한 임박 알림 발송 로직 구현
        log.info("Sending due notification for requirement: {}", requirement.getId());
    }
    
    // DTO 클래스들
    
    public static class PhotoUploadRequest {
        private String fileName;
        private Long fileSize;
        private String photoType; // OVERALL, DETAIL, DAMAGE, LABEL, CONTENT
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
    
    public static class PhotoValidationRequest {
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
}