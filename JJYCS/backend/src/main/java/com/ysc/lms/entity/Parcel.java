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
 * 송장/택배 엔티티
 * 고객이 주문서에 입력한 택배사/송장번호 정보 및 검증 상태 관리
 */
@Entity
@Table(name = "parcels")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parcels"})
    private Order order;

    @Column(nullable = false, length = 50)
    private String carrier; // 택배사 코드 (CJ, LOTTE, HANJIN 등)

    @Column(nullable = false, length = 100)
    private String trackingNumber; // 송장번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationStatus validationStatus = ValidationStatus.PENDING;

    @Column(length = 1000)
    private String validationMessage; // 검증 결과 메시지

    @Column(length = 500)
    private String lastKnownStatus; // 마지막 조회된 택배 상태

    @Column
    private LocalDateTime lastValidatedAt; // 마지막 검증 시간

    @Column
    private LocalDateTime inboundScanAt; // 입고 스캔 시간

    @Column(nullable = false)
    private Boolean isMatched = false; // 입고 시 매칭 여부

    @Column(length = 1000)
    private String matchingNotes; // 매칭 관련 메모

    // 사진 파일 경로들 (JSON 배열로 저장)
    @ElementCollection
    @CollectionTable(name = "parcel_photos", joinColumns = @JoinColumn(name = "parcel_id"))
    @Column(name = "photo_path")
    private List<String> photosPaths = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ValidationStatus {
        PENDING("검증 대기"),
        VALID("유효"),
        INVALID("무효"),
        IN_PROGRESS("진행 중"),
        DELIVERED("배송 완료"),
        ERROR("검증 오류");

        private final String description;

        ValidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 편의 메서드들
    public void addPhotoPath(String photoPath) {
        this.photosPaths.add(photoPath);
    }

    public void markAsMatched(String notes) {
        this.isMatched = true;
        this.matchingNotes = notes;
        this.inboundScanAt = LocalDateTime.now();
    }

    public void updateValidationStatus(ValidationStatus status, String message) {
        this.validationStatus = status;
        this.validationMessage = message;
        this.lastValidatedAt = LocalDateTime.now();
    }
}