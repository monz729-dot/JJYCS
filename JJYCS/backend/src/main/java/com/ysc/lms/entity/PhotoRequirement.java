package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * P0-5: 사진 촬영 의무 구간 엔티티
 * 입고/리패킹/출고 단계별 사진 촬영 요구사항 및 완료 상태 관리
 */
@Entity
@Table(name = "photo_requirements")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PhotoRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "photoRequirements"})
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OrderBox orderBox; // 특정 박스에 대한 사진 요구사항 (선택사항)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoStage stage; // 촬영 단계

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementStatus status = RequirementStatus.PENDING; // 요구사항 상태

    @Column(nullable = false)
    private Boolean isRequired = true; // 필수 촬영 여부

    @Column(nullable = false)
    private Integer minPhotoCount = 1; // 최소 사진 수

    @Column(nullable = false)
    private Integer maxPhotoCount = 10; // 최대 사진 수

    // 사진 유형별 요구사항
    @Column(nullable = false)
    private Boolean requireOverallView = true; // 전체 모습 필수

    @Column(nullable = false)
    private Boolean requireDetailView = false; // 세부 모습 필요

    @Column(nullable = false)
    private Boolean requireDamageCheck = false; // 손상 확인 필요

    @Column(nullable = false)
    private Boolean requireLabelView = false; // 라벨 확인 필요

    @Column(nullable = false)
    private Boolean requireContentView = false; // 내용물 확인 필요

    // 완료 정보
    @Column(nullable = false)
    private Integer completedPhotoCount = 0; // 완료된 사진 수

    @Column
    private LocalDateTime completedAt; // 완료 일시

    @Column(length = 100)
    private String completedBy; // 완료자

    // 사진 파일 정보
    @ElementCollection
    @CollectionTable(name = "photo_requirement_files", joinColumns = @JoinColumn(name = "requirement_id"))
    @Column(name = "file_path")
    private List<String> photoFilePaths = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "photo_requirement_metadata", joinColumns = @JoinColumn(name = "requirement_id"))
    private List<PhotoMetadata> photoMetadata = new ArrayList<>();

    // 알림 및 escalation 정보
    @Column
    private LocalDateTime firstNotificationSentAt; // 첫 알림 발송 시간

    @Column
    private LocalDateTime lastNotificationSentAt; // 마지막 알림 발송 시간

    @Column(nullable = false)
    private Integer notificationCount = 0; // 알림 발송 횟수

    @Column
    private LocalDateTime escalationAt; // 에스컬레이션 시간

    @Column(nullable = false)
    private Boolean isEscalated = false; // 에스컬레이션 여부

    // 기한 관리
    @Column
    private LocalDateTime dueAt; // 완료 기한

    @Column(nullable = false)
    private Boolean isOverdue = false; // 기한 초과 여부

    // 추가 정보
    @Column(length = 1000)
    private String instructions; // 촬영 지시사항

    @Column(length = 1000)
    private String remarks; // 비고

    @Column(length = 500)
    private String rejectionReason; // 거부 사유 (품질 미달 등)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum PhotoStage {
        INBOUND("입고", "화물 입고 시 촬영"),
        REPACK("리패킹", "리패킹 작업 중 촬영"),
        OUTBOUND("출고", "화물 출고 시 촬영"),
        DAMAGE("손상확인", "손상 발견 시 촬영"),
        CUSTOMS("통관", "통관 관련 촬영"),
        SPECIAL("특별요청", "고객 특별 요청 촬영");

        private final String koreanName;
        private final String description;

        PhotoStage(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }

    public enum RequirementStatus {
        PENDING("대기중", "촬영 대기 중"),
        IN_PROGRESS("진행중", "촬영 진행 중"),
        COMPLETED("완료", "촬영 완료"),
        REJECTED("거부", "품질 미달로 재촬영 필요"),
        WAIVED("면제", "촬영 면제 처리"),
        OVERDUE("지연", "기한 초과");

        private final String koreanName;
        private final String description;

        RequirementStatus(String koreanName, String description) {
            this.koreanName = koreanName;
            this.description = description;
        }

        public String getKoreanName() { return koreanName; }
        public String getDescription() { return description; }
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PhotoMetadata {
        @Column(name = "file_name")
        private String fileName;

        @Column(name = "file_size")
        private Long fileSize;

        @Column(name = "photo_type")
        private String photoType; // OVERALL, DETAIL, DAMAGE, LABEL, CONTENT

        @Column(name = "taken_at")
        private LocalDateTime takenAt;

        @Column(name = "taken_by")
        private String takenBy;

        @Column(name = "device_info")
        private String deviceInfo; // 촬영 기기 정보

        @Column(name = "gps_location")
        private String gpsLocation; // GPS 위치 정보

        @Column(name = "is_validated")
        private Boolean isValidated = false;

        @Column(name = "validation_result")
        private String validationResult;

        public PhotoMetadata(String fileName, String photoType, String takenBy) {
            this.fileName = fileName;
            this.photoType = photoType;
            this.takenBy = takenBy;
            this.takenAt = LocalDateTime.now();
            this.isValidated = false;
        }
    }

    // 편의 메서드들

    /**
     * 사진 파일 추가
     */
    public void addPhotoFile(String filePath, String photoType, String takenBy) {
        this.photoFilePaths.add(filePath);
        this.photoMetadata.add(new PhotoMetadata(filePath, photoType, takenBy));
        this.completedPhotoCount = this.photoFilePaths.size();
        
        // 최소 요구사항 충족 시 완료 처리
        if (this.completedPhotoCount >= this.minPhotoCount && this.status == RequirementStatus.PENDING) {
            this.status = RequirementStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            this.completedBy = takenBy;
        }
    }

    /**
     * 사진 거부 처리
     */
    public void rejectPhotos(String reason) {
        this.status = RequirementStatus.REJECTED;
        this.rejectionReason = reason;
        this.completedAt = null;
        this.completedBy = null;
    }

    /**
     * 촬영 면제 처리
     */
    public void waiveRequirement(String reason) {
        this.status = RequirementStatus.WAIVED;
        this.remarks = "면제 처리: " + reason;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 알림 발송 기록
     */
    public void recordNotificationSent() {
        LocalDateTime now = LocalDateTime.now();
        if (this.firstNotificationSentAt == null) {
            this.firstNotificationSentAt = now;
        }
        this.lastNotificationSentAt = now;
        this.notificationCount++;
    }

    /**
     * 에스컬레이션 처리
     */
    public void escalate() {
        this.isEscalated = true;
        this.escalationAt = LocalDateTime.now();
        this.status = RequirementStatus.OVERDUE;
    }

    /**
     * 기한 초과 확인
     */
    public boolean isOverdueNow() {
        if (dueAt == null || status == RequirementStatus.COMPLETED || status == RequirementStatus.WAIVED) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueAt);
    }

    /**
     * 요구사항 충족 여부 확인
     */
    public boolean isRequirementMet() {
        if (status == RequirementStatus.COMPLETED || status == RequirementStatus.WAIVED) {
            return true;
        }
        return completedPhotoCount >= minPhotoCount;
    }

    /**
     * 필수 사진 유형별 완료 여부 확인
     */
    public boolean hasRequiredPhotoTypes() {
        if (!isRequired) return true;

        boolean hasOverall = !requireOverallView || hasPhotoType("OVERALL");
        boolean hasDetail = !requireDetailView || hasPhotoType("DETAIL");
        boolean hasDamage = !requireDamageCheck || hasPhotoType("DAMAGE");
        boolean hasLabel = !requireLabelView || hasPhotoType("LABEL");
        boolean hasContent = !requireContentView || hasPhotoType("CONTENT");

        return hasOverall && hasDetail && hasDamage && hasLabel && hasContent;
    }

    private boolean hasPhotoType(String photoType) {
        return photoMetadata.stream()
            .anyMatch(meta -> photoType.equals(meta.getPhotoType()));
    }

    /**
     * 기한 설정 (단계별 기본 기한)
     */
    public void setDefaultDueDate() {
        LocalDateTime now = LocalDateTime.now();
        switch (stage) {
            case INBOUND:
                this.dueAt = now.plusHours(4); // 입고 후 4시간 내
                break;
            case REPACK:
                this.dueAt = now.plusHours(8); // 리패킹 중 8시간 내
                break;
            case OUTBOUND:
                this.dueAt = now.plusHours(2); // 출고 전 2시간 내
                break;
            case DAMAGE:
                this.dueAt = now.plusMinutes(30); // 손상 발견 시 즉시
                break;
            default:
                this.dueAt = now.plusHours(24); // 기본 24시간
        }
    }
}