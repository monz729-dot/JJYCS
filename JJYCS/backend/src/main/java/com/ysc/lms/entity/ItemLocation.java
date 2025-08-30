package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * P0-6: 물품 위치 추적 엔티티
 * 주문 박스/아이템의 실제 적재 위치 및 이동 기록 관리
 */
@Entity
@Table(name = "item_locations")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ItemLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 추적 대상 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "itemLocations"})
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_box_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OrderBox orderBox; // 박스 단위 추적

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OrderItem orderItem; // 아이템 단위 추적 (필요시)

    // 위치 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StorageLocation storageLocation; // 현재 적재 위치

    @Column(nullable = false, length = 100)
    private String locationCode; // 위치 코드 스냅샷 (변경 추적용)

    @Column(length = 200)
    private String locationDescription; // 위치 설명

    // 적재 정보
    @Column(precision = 10, scale = 3)
    private BigDecimal weight; // 적재 중량

    @Column(precision = 10, scale = 6)
    private BigDecimal volume; // 적재 부피

    @Column(nullable = false)
    private Integer quantity = 1; // 수량

    @Column(length = 100)
    private String stackPosition; // 적재 위치 (상/중/하 등)

    // 상태 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemLocationStatus status = ItemLocationStatus.STORED; // 위치 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType = MovementType.INBOUND; // 이동 유형

    // 이동 정보
    @Column
    private LocalDateTime arrivedAt; // 도착 일시

    @Column
    private LocalDateTime departedAt; // 출발 일시

    @Column(length = 100)
    private String movedBy; // 이동 담당자

    @Column(length = 200)
    private String movementMethod; // 이동 방법 (지게차, 수작업 등)

    // 이전 위치 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_location_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StorageLocation previousLocation;

    @Column(length = 100)
    private String previousLocationCode;

    // 다음 예정 위치
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_planned_location_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StorageLocation nextPlannedLocation;

    @Column
    private LocalDateTime plannedMoveAt; // 계획된 이동 일시

    // 추적 정보
    @Column(length = 100)
    private String trackingCode; // 추적 코드 (바코드/QR코드)

    @Column(length = 100)
    private String rfidTag; // RFID 태그

    @Column
    private LocalDateTime lastScanAt; // 마지막 스캔 일시

    @Column(length = 100)
    private String lastScannedBy; // 마지막 스캔자

    // 알림 및 경고
    @Column(nullable = false)
    private Boolean isAlerted = false; // 알림 상태

    @Column(length = 500)
    private String alertReason; // 알림 사유

    @Column
    private LocalDateTime alertedAt; // 알림 일시

    // 픽업/배송 정보
    @Column
    private LocalDateTime scheduledPickupAt; // 픽업 예정 일시

    @Column
    private LocalDateTime actualPickupAt; // 실제 픽업 일시

    @Column(length = 100)
    private String pickedUpBy; // 픽업 담당자

    // 품질 및 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition itemCondition = ItemCondition.GOOD; // 물품 상태

    @Column(length = 1000)
    private String conditionNotes; // 상태 메모

    @Column
    private LocalDateTime lastInspectionAt; // 마지막 점검 일시

    @Column(length = 100)
    private String lastInspectedBy; // 마지막 점검자

    // 환경 정보
    @Column(precision = 5, scale = 2)
    private BigDecimal temperature; // 보관 온도

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity; // 보관 습도

    // 추가 정보
    @Column(length = 1000)
    private String notes; // 메모

    @Column(length = 500)
    private String specialInstructions; // 특별 지시사항

    @Column(length = 200)
    private String tags; // 태그 (콤마 구분)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ItemLocationStatus {
        IN_TRANSIT("이동중"),
        STORED("보관중"),
        RESERVED("예약됨"),
        PICKING("피킹중"),
        PICKED("피킹완료"),
        LOADING("적재중"),
        LOADED("적재완료"),
        DAMAGED("손상"),
        LOST("분실"),
        RETURNED("반송");

        private final String koreanName;

        ItemLocationStatus(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    public enum MovementType {
        INBOUND("입고"),
        INTERNAL("내부이동"),
        OUTBOUND("출고"),
        RETURN("반송"),
        TRANSFER("이송"),
        CONSOLIDATION("통합"),
        SEPARATION("분리");

        private final String koreanName;

        MovementType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    public enum ItemCondition {
        EXCELLENT("최상"),
        GOOD("양호"),
        FAIR("보통"),
        POOR("불량"),
        DAMAGED("손상"),
        UNKNOWN("미확인");

        private final String koreanName;

        ItemCondition(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    // 편의 메서드들

    /**
     * 위치 도착 처리
     */
    public void arrive(String movedBy, String method) {
        this.arrivedAt = LocalDateTime.now();
        this.movedBy = movedBy;
        this.movementMethod = method;
        this.status = ItemLocationStatus.STORED;
    }

    /**
     * 위치 출발 처리
     */
    public void depart(String movedBy, MovementType type) {
        this.departedAt = LocalDateTime.now();
        this.movedBy = movedBy;
        this.movementType = type;
        this.status = ItemLocationStatus.IN_TRANSIT;
        
        // 현재 위치를 이전 위치로 설정
        this.previousLocation = this.storageLocation;
        this.previousLocationCode = this.locationCode;
    }

    /**
     * 새 위치로 이동
     */
    public void moveTo(StorageLocation newLocation, String movedBy, String method) {
        // 출발 처리
        depart(movedBy, MovementType.INTERNAL);
        
        // 새 위치 설정
        this.storageLocation = newLocation;
        this.locationCode = newLocation.getLocationCode();
        this.locationDescription = newLocation.getLocationName();
        
        // 도착 처리
        arrive(movedBy, method);
    }

    /**
     * 픽업 예약
     */
    public void schedulePickup(LocalDateTime scheduledAt) {
        this.scheduledPickupAt = scheduledAt;
        this.status = ItemLocationStatus.RESERVED;
    }

    /**
     * 픽업 처리
     */
    public void pickup(String pickedBy) {
        this.actualPickupAt = LocalDateTime.now();
        this.pickedUpBy = pickedBy;
        this.status = ItemLocationStatus.PICKED;
    }

    /**
     * 스캔 기록
     */
    public void recordScan(String scannedBy) {
        this.lastScanAt = LocalDateTime.now();
        this.lastScannedBy = scannedBy;
    }

    /**
     * 알림 설정
     */
    public void setAlert(String reason) {
        this.isAlerted = true;
        this.alertReason = reason;
        this.alertedAt = LocalDateTime.now();
    }

    /**
     * 알림 해제
     */
    public void clearAlert() {
        this.isAlerted = false;
        this.alertReason = null;
        this.alertedAt = null;
    }

    /**
     * 상태 검사
     */
    public void inspect(ItemCondition condition, String notes, String inspector) {
        this.itemCondition = condition;
        this.conditionNotes = notes;
        this.lastInspectionAt = LocalDateTime.now();
        this.lastInspectedBy = inspector;
    }

    /**
     * 환경 정보 업데이트
     */
    public void updateEnvironment(BigDecimal temp, BigDecimal humid) {
        this.temperature = temp;
        this.humidity = humid;
    }

    /**
     * 지연 여부 확인
     */
    public boolean isOverdue() {
        if (plannedMoveAt == null) return false;
        return LocalDateTime.now().isAfter(plannedMoveAt) && 
               status == ItemLocationStatus.STORED;
    }

    /**
     * 보관 기간 계산 (시간)
     */
    public long getStorageDurationHours() {
        if (arrivedAt == null) return 0;
        LocalDateTime endTime = departedAt != null ? departedAt : LocalDateTime.now();
        return java.time.Duration.between(arrivedAt, endTime).toHours();
    }

    /**
     * 위치 이력 요약
     */
    public String getLocationHistory() {
        StringBuilder history = new StringBuilder();
        if (previousLocationCode != null) {
            history.append(previousLocationCode).append(" → ");
        }
        history.append(locationCode);
        if (nextPlannedLocation != null) {
            history.append(" → ").append(nextPlannedLocation.getLocationCode()).append(" (예정)");
        }
        return history.toString();
    }

    /**
     * 추적 가능 여부 확인
     */
    public boolean isTrackable() {
        return trackingCode != null || rfidTag != null;
    }

    /**
     * 긴급 처리 필요 여부 확인
     */
    public boolean needsUrgentAction() {
        return isAlerted || 
               itemCondition == ItemCondition.DAMAGED || 
               status == ItemLocationStatus.LOST ||
               isOverdue();
    }
}