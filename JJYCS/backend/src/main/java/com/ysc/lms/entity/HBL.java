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
 * P0-4: HBL (House Bill of Lading) 엔티티
 * 하우스 선하증권 발행 및 관리
 */
@Entity
@Table(name = "hbls")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HBL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String hblNumber; // HBL 번호 (자동 생성)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hbl"})
    private Order order;

    // 발송인 정보 (Shipper)
    @Column(nullable = false, length = 200)
    private String shipperName; // 발송인명

    @Column(nullable = false, length = 500)
    private String shipperAddress; // 발송인 주소

    @Column(length = 20)
    private String shipperPhone; // 발송인 전화번호

    // 수취인 정보 (Consignee)
    @Column(nullable = false, length = 200)
    private String consigneeName; // 수취인명

    @Column(nullable = false, length = 500)
    private String consigneeAddress; // 수취인 주소

    @Column(length = 20)
    private String consigneePhone; // 수취인 전화번호

    // 알림 수취인 정보 (Notify Party)
    @Column(length = 200)
    private String notifyPartyName;

    @Column(length = 500)
    private String notifyPartyAddress;

    @Column(length = 20)
    private String notifyPartyPhone;

    // 운송 정보
    @Column(nullable = false, length = 100)
    private String portOfLoading; // 선적항 (예: 방콕항)

    @Column(nullable = false, length = 100)
    private String portOfDischarge; // 양하항 (예: 부산항)

    @Column(nullable = false, length = 100)
    private String placeOfDelivery; // 인도지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportMode transportMode = TransportMode.SEA; // 운송 방식

    @Column(length = 100)
    private String vesselName; // 선박명

    @Column(length = 50)
    private String voyageNumber; // 항차번호

    // 화물 정보
    @Column(nullable = false)
    private Integer totalPackages = 0; // 총 포장수

    @Column(length = 50)
    private String packageType = "CARTON"; // 포장 형태

    @Column(precision = 10, scale = 3)
    private BigDecimal grossWeight; // 총 중량 (kg)

    @Column(precision = 10, scale = 6)
    private BigDecimal totalCbm; // 총 CBM

    @Column(length = 1000)
    private String cargoDescription; // 화물 기술서

    // HS 코드 및 관세 정보
    @Column(length = 20)
    private String hsCode; // 대표 HS 코드

    @Column(precision = 12, scale = 2)
    private BigDecimal declaredValue; // 신고가격 (THB)

    @Column(precision = 12, scale = 2)
    private BigDecimal declaredValueKrw; // 신고가격 (KRW)

    // HBL 발행 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HblStatus status = HblStatus.DRAFT; // HBL 상태

    @Column
    private LocalDateTime issuedAt; // 발행일시

    @Column(length = 100)
    private String issuedBy; // 발행자 (사용자명 또는 시스템)

    @Column
    private LocalDateTime printedAt; // 인쇄일시

    @Column(nullable = false)
    private Boolean isOriginal = true; // 원본 여부

    @Column(nullable = false)
    private Integer copyCount = 0; // 사본 발행 횟수

    // 라벨링 정보
    @ElementCollection
    @CollectionTable(name = "hbl_labels", joinColumns = @JoinColumn(name = "hbl_id"))
    @Column(name = "label_path")
    private List<String> labelPaths = new ArrayList<>(); // 라벨 파일 경로들

    @Column(length = 500)
    private String qrCodeData; // QR 코드 데이터

    @Column(length = 200)
    private String qrCodePath; // QR 코드 이미지 경로

    // 추가 정보
    @Column(length = 1000)
    private String specialInstructions; // 특별 지시사항

    @Column(length = 1000)
    private String remarks; // 비고

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum TransportMode {
        SEA("해상운송"),
        AIR("항공운송"),
        LAND("육상운송"),
        MULTIMODAL("복합운송");

        private final String description;

        TransportMode(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    public enum HblStatus {
        DRAFT("초안"),
        PENDING_APPROVAL("승인대기"),
        APPROVED("승인완료"),
        ISSUED("발행완료"),
        PRINTED("인쇄완료"),
        DELIVERED("전달완료"),
        CANCELLED("취소됨");

        private final String description;

        HblStatus(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    // 편의 메서드들

    /**
     * HBL 발행 처리
     */
    public void issue(String issuer) {
        this.status = HblStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
        this.issuedBy = issuer;
    }

    /**
     * HBL 인쇄 처리
     */
    public void markAsPrinted() {
        this.printedAt = LocalDateTime.now();
        if (this.status == HblStatus.ISSUED) {
            this.status = HblStatus.PRINTED;
        }
    }

    /**
     * 사본 발행
     */
    public void issueCopy() {
        this.copyCount++;
        this.isOriginal = false;
    }

    /**
     * 라벨 경로 추가
     */
    public void addLabelPath(String labelPath) {
        this.labelPaths.add(labelPath);
    }

    /**
     * QR 코드 설정
     */
    public void setQrCode(String qrData, String qrPath) {
        this.qrCodeData = qrData;
        this.qrCodePath = qrPath;
    }

    /**
     * 운송 모드에 따른 항구/공항 유효성 검증
     */
    public boolean isTransportInfoValid() {
        if (transportMode == TransportMode.SEA) {
            return portOfLoading != null && portOfDischarge != null;
        } else if (transportMode == TransportMode.AIR) {
            return portOfLoading != null && portOfDischarge != null; // 공항으로 사용
        }
        return true; // 육상/복합운송은 별도 검증 로직 필요
    }

    /**
     * HBL 완성도 검증
     */
    public boolean isReadyForIssue() {
        return hblNumber != null && !hblNumber.isEmpty() &&
               shipperName != null && !shipperName.isEmpty() &&
               shipperAddress != null && !shipperAddress.isEmpty() &&
               consigneeName != null && !consigneeName.isEmpty() &&
               consigneeAddress != null && !consigneeAddress.isEmpty() &&
               isTransportInfoValid() &&
               totalPackages > 0 &&
               grossWeight != null && grossWeight.compareTo(BigDecimal.ZERO) > 0;
    }
}