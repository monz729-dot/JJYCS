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
import java.util.ArrayList;
import java.util.List;

/**
 * P0-6: 적재 위치 엔티티
 * 창고 내 구역/랙/셀 구조의 위치 정보 관리
 */
@Entity
@Table(name = "storage_locations")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String locationCode; // 위치 코드 (예: A01-R02-C03)

    @Column(nullable = false, length = 100)
    private String locationName; // 위치 이름 (예: A구역 1랙 2셀)

    // 계층 구조
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType locationType; // 위치 유형 (ZONE, RACK, CELL)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_location_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "childLocations"})
    private StorageLocation parentLocation; // 상위 위치

    @OneToMany(mappedBy = "parentLocation", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parentLocation"})
    private List<StorageLocation> childLocations = new ArrayList<>(); // 하위 위치들

    // 물리적 위치 정보
    @Column(length = 10)
    private String zoneCode; // 구역 코드 (A, B, C...)

    @Column(length = 10)
    private String rackCode; // 랙 코드 (01, 02, 03...)

    @Column(length = 10)
    private String cellCode; // 셀 코드 (A01, A02, B01...)

    @Column(nullable = false)
    private Integer floor = 1; // 층수

    @Column(nullable = false)
    private Integer level = 1; // 단수 (1단, 2단, 3단...)

    // 위치 상세 정보
    @Column(precision = 8, scale = 2)
    private BigDecimal xCoordinate; // X 좌표

    @Column(precision = 8, scale = 2)
    private BigDecimal yCoordinate; // Y 좌표

    @Column(precision = 8, scale = 2)
    private BigDecimal zCoordinate; // Z 좌표 (높이)

    // 용량 및 크기 정보
    @Column(precision = 10, scale = 3)
    private BigDecimal maxWeight; // 최대 적재 중량 (kg)

    @Column(precision = 10, scale = 6)
    private BigDecimal maxVolume; // 최대 적재 부피 (m³)

    @Column(precision = 8, scale = 2)
    private BigDecimal width; // 폭 (cm)

    @Column(precision = 8, scale = 2)
    private BigDecimal height; // 높이 (cm)

    @Column(precision = 8, scale = 2)
    private BigDecimal depth; // 깊이 (cm)

    // 현재 적재 정보
    @Column(precision = 10, scale = 3)
    private BigDecimal currentWeight = BigDecimal.ZERO; // 현재 적재 중량

    @Column(precision = 10, scale = 6)
    private BigDecimal currentVolume = BigDecimal.ZERO; // 현재 적재 부피

    @Column(nullable = false)
    private Integer currentItemCount = 0; // 현재 적재 물품 수

    // 상태 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationStatus status = LocationStatus.AVAILABLE; // 위치 상태

    @Column(nullable = false)
    private Boolean isActive = true; // 사용 가능 여부

    @Column(nullable = false)
    private Boolean isReserved = false; // 예약 여부

    @Column
    private LocalDateTime reservedUntil; // 예약 만료 시간

    @Column(length = 100)
    private String reservedBy; // 예약자

    // 접근성 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType = AccessType.MANUAL; // 접근 방식

    @Column(nullable = false)
    private Boolean requiresEquipment = false; // 장비 필요 여부

    @Column(length = 200)
    private String requiredEquipment; // 필요한 장비

    // 환경 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemperatureZone temperatureZone = TemperatureZone.NORMAL; // 온도 구역

    @Column(nullable = false)
    private Boolean isClimateControlled = false; // 온습도 관리 여부

    @Column(nullable = false)
    private Boolean isSecurityZone = false; // 보안 구역 여부

    // 관리 정보
    @Column
    private LocalDateTime lastInspectionAt; // 마지막 점검 일시

    @Column(length = 100)
    private String lastInspectedBy; // 마지막 점검자

    @Column
    private LocalDateTime lastMaintenanceAt; // 마지막 정비 일시

    @Column(length = 1000)
    private String maintenanceNotes; // 정비 메모

    // 추가 정보
    @Column(length = 1000)
    private String description; // 설명

    @Column(length = 1000)
    private String specialInstructions; // 특별 지시사항

    @Column(length = 500)
    private String tags; // 태그 (콤마 구분)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum LocationType {
        WAREHOUSE("창고"),
        ZONE("구역"),
        RACK("랙"),
        CELL("셀"),
        SPECIAL("특수위치");

        private final String koreanName;

        LocationType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    public enum LocationStatus {
        AVAILABLE("사용가능"),
        OCCUPIED("사용중"),
        RESERVED("예약됨"),
        MAINTENANCE("정비중"),
        DAMAGED("손상됨"),
        BLOCKED("차단됨"),
        RETIRED("폐기됨");

        private final String koreanName;

        LocationStatus(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    public enum AccessType {
        MANUAL("수동"),
        FORKLIFT("지게차"),
        CRANE("크레인"),
        ROBOT("로봇"),
        CONVEYOR("컨베이어");

        private final String koreanName;

        AccessType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    public enum TemperatureZone {
        FROZEN("냉동"),
        REFRIGERATED("냉장"),
        NORMAL("상온"),
        HEATED("가온");

        private final String koreanName;

        TemperatureZone(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() { return koreanName; }
    }

    // 편의 메서드들

    /**
     * 위치 예약
     */
    public void reserve(String reservedBy, LocalDateTime reservedUntil) {
        if (this.status != LocationStatus.AVAILABLE) {
            throw new IllegalStateException("사용 가능한 위치만 예약할 수 있습니다.");
        }
        this.isReserved = true;
        this.reservedBy = reservedBy;
        this.reservedUntil = reservedUntil;
        this.status = LocationStatus.RESERVED;
    }

    /**
     * 예약 취소
     */
    public void cancelReservation() {
        this.isReserved = false;
        this.reservedBy = null;
        this.reservedUntil = null;
        if (this.status == LocationStatus.RESERVED) {
            this.status = LocationStatus.AVAILABLE;
        }
    }

    /**
     * 위치 점유
     */
    public void occupy() {
        if (this.status != LocationStatus.AVAILABLE && this.status != LocationStatus.RESERVED) {
            throw new IllegalStateException("사용 가능하거나 예약된 위치만 점유할 수 있습니다.");
        }
        this.status = LocationStatus.OCCUPIED;
        this.isReserved = false;
        this.reservedBy = null;
        this.reservedUntil = null;
    }

    /**
     * 위치 비우기
     */
    public void vacate() {
        this.status = LocationStatus.AVAILABLE;
        this.currentWeight = BigDecimal.ZERO;
        this.currentVolume = BigDecimal.ZERO;
        this.currentItemCount = 0;
    }

    /**
     * 적재량 추가
     */
    public void addLoad(BigDecimal weight, BigDecimal volume, int itemCount) {
        if (weight != null) {
            this.currentWeight = this.currentWeight.add(weight);
        }
        if (volume != null) {
            this.currentVolume = this.currentVolume.add(volume);
        }
        this.currentItemCount += itemCount;

        // 용량 초과 체크
        if (isOverCapacity()) {
            throw new IllegalStateException("위치 용량을 초과했습니다.");
        }

        if (this.status == LocationStatus.AVAILABLE && 
            (this.currentWeight.compareTo(BigDecimal.ZERO) > 0 || this.currentItemCount > 0)) {
            this.status = LocationStatus.OCCUPIED;
        }
    }

    /**
     * 적재량 제거
     */
    public void removeLoad(BigDecimal weight, BigDecimal volume, int itemCount) {
        if (weight != null) {
            this.currentWeight = this.currentWeight.subtract(weight).max(BigDecimal.ZERO);
        }
        if (volume != null) {
            this.currentVolume = this.currentVolume.subtract(volume).max(BigDecimal.ZERO);
        }
        this.currentItemCount = Math.max(0, this.currentItemCount - itemCount);

        // 비어있으면 상태 변경
        if (this.currentWeight.compareTo(BigDecimal.ZERO) == 0 && this.currentItemCount == 0) {
            this.status = LocationStatus.AVAILABLE;
        }
    }

    /**
     * 용량 초과 여부 확인
     */
    public boolean isOverCapacity() {
        boolean weightExceeded = maxWeight != null && 
            currentWeight.compareTo(maxWeight) > 0;
        boolean volumeExceeded = maxVolume != null && 
            currentVolume.compareTo(maxVolume) > 0;
        
        return weightExceeded || volumeExceeded;
    }

    /**
     * 사용 가능 여부 확인
     */
    public boolean isAvailableForUse() {
        return isActive && 
               status == LocationStatus.AVAILABLE && 
               !isOverCapacity() &&
               (reservedUntil == null || LocalDateTime.now().isAfter(reservedUntil));
    }

    /**
     * 잔여 용량 계산
     */
    public BigDecimal getRemainingWeight() {
        if (maxWeight == null) return BigDecimal.valueOf(Double.MAX_VALUE);
        return maxWeight.subtract(currentWeight).max(BigDecimal.ZERO);
    }

    public BigDecimal getRemainingVolume() {
        if (maxVolume == null) return BigDecimal.valueOf(Double.MAX_VALUE);
        return maxVolume.subtract(currentVolume).max(BigDecimal.ZERO);
    }

    /**
     * 사용률 계산
     */
    public double getWeightUtilization() {
        if (maxWeight == null || maxWeight.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return currentWeight.divide(maxWeight, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
    }

    public double getVolumeUtilization() {
        if (maxVolume == null || maxVolume.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return currentVolume.divide(maxVolume, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
    }

    /**
     * 전체 위치 경로 생성
     */
    public String getFullLocationPath() {
        StringBuilder path = new StringBuilder();
        if (parentLocation != null) {
            path.append(parentLocation.getFullLocationPath()).append(" > ");
        }
        path.append(locationName);
        return path.toString();
    }

    /**
     * 하위 위치 추가
     */
    public void addChildLocation(StorageLocation childLocation) {
        this.childLocations.add(childLocation);
        childLocation.setParentLocation(this);
    }

    /**
     * 정비 기록
     */
    public void recordMaintenance(String notes, String maintainedBy) {
        this.lastMaintenanceAt = LocalDateTime.now();
        this.maintenanceNotes = notes;
        if (this.status == LocationStatus.MAINTENANCE) {
            this.status = LocationStatus.AVAILABLE;
        }
    }

    /**
     * 점검 기록
     */
    public void recordInspection(String inspector) {
        this.lastInspectionAt = LocalDateTime.now();
        this.lastInspectedBy = inspector;
    }
}