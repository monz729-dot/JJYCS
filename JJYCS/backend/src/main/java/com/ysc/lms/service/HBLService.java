package com.ysc.lms.service;

import com.ysc.lms.entity.HBL;
import com.ysc.lms.entity.Order;
import com.ysc.lms.repository.HBLRepository;
import com.ysc.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * P0-4: HBL 발행 및 라벨링 서비스
 * - HBL (House Bill of Lading) 생성/발행/인쇄
 * - 라벨 및 QR 코드 생성
 * - 운송서류 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HBLService {
    
    private final HBLRepository hblRepository;
    private final OrderRepository orderRepository;
    private final CodeGenerationService codeGenerationService;
    private final LabelGenerationService labelGenerationService; // 라벨 생성 서비스 (추후 구현)
    
    @Value("${app.hbl.default-shipper-name:YCS Thailand}")
    private String defaultShipperName;
    
    @Value("${app.hbl.default-shipper-address:Bangkok, Thailand}")
    private String defaultShipperAddress;
    
    @Value("${app.hbl.default-port-loading:Bangkok Port}")
    private String defaultPortLoading;
    
    @Value("${app.hbl.default-port-discharge:Busan Port}")
    private String defaultPortDischarge;
    
    /**
     * 주문에 대한 HBL 생성 (초안 상태)
     */
    @Transactional
    public HBL createHBL(Long orderId, HBLCreateRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            // 이미 HBL이 있는지 확인
            Optional<HBL> existingHbl = hblRepository.findByOrderId(orderId);
            if (existingHbl.isPresent()) {
                throw new IllegalStateException("이미 HBL이 생성된 주문입니다: " + orderId);
            }
            
            log.info("Creating HBL for order: {}", orderId);
            
            // HBL 번호 생성
            String hblNumber = codeGenerationService.generateHblNumber();
            
            // HBL 엔티티 생성
            HBL hbl = new HBL();
            hbl.setHblNumber(hblNumber);
            hbl.setOrder(order);
            
            // 발송인 정보 (기본값 또는 요청값)
            hbl.setShipperName(request.getShipperName() != null ? request.getShipperName() : defaultShipperName);
            hbl.setShipperAddress(request.getShipperAddress() != null ? request.getShipperAddress() : defaultShipperAddress);
            hbl.setShipperPhone(request.getShipperPhone());
            
            // 수취인 정보 (주문 정보에서 자동 설정)
            hbl.setConsigneeName(order.getRecipientName());
            hbl.setConsigneeAddress(order.getRecipientAddress());
            hbl.setConsigneePhone(order.getRecipientPhone());
            
            // 알림 수취인 (기본적으로 수취인과 동일)
            hbl.setNotifyPartyName(request.getNotifyPartyName() != null ? request.getNotifyPartyName() : order.getRecipientName());
            hbl.setNotifyPartyAddress(request.getNotifyPartyAddress() != null ? request.getNotifyPartyAddress() : order.getRecipientAddress());
            hbl.setNotifyPartyPhone(request.getNotifyPartyPhone() != null ? request.getNotifyPartyPhone() : order.getRecipientPhone());
            
            // 운송 정보 설정
            setupTransportInfo(hbl, order, request);
            
            // 화물 정보 설정
            setupCargoInfo(hbl, order, request);
            
            // 초안 상태로 저장
            hbl.setStatus(HBL.HblStatus.DRAFT);
            
            HBL savedHbl = hblRepository.save(hbl);
            log.info("HBL created successfully: {} for order: {}", hblNumber, orderId);
            
            return savedHbl;
            
        } catch (Exception e) {
            log.error("Error creating HBL for order: {}", orderId, e);
            throw new RuntimeException("HBL 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * HBL 승인 및 발행
     */
    @Transactional
    public HBL approveAndIssueHBL(Long hblId, String approver) {
        try {
            HBL hbl = hblRepository.findById(hblId)
                .orElseThrow(() -> new IllegalArgumentException("HBL을 찾을 수 없습니다: " + hblId));
            
            if (!hbl.isReadyForIssue()) {
                throw new IllegalStateException("HBL 정보가 완전하지 않아 발행할 수 없습니다.");
            }
            
            // 승인 및 발행 처리
            hbl.setStatus(HBL.HblStatus.APPROVED);
            hbl.issue(approver);
            
            // QR 코드 생성
            generateQrCodeForHBL(hbl);
            
            HBL savedHbl = hblRepository.save(hbl);
            log.info("HBL approved and issued: {} by {}", hbl.getHblNumber(), approver);
            
            return savedHbl;
            
        } catch (Exception e) {
            log.error("Error approving and issuing HBL: {}", hblId, e);
            throw new RuntimeException("HBL 승인/발행 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 라벨 생성 및 인쇄
     */
    @Transactional
    public HBL generateLabelsAndPrint(Long hblId, LabelGenerationRequest request) {
        try {
            HBL hbl = hblRepository.findById(hblId)
                .orElseThrow(() -> new IllegalArgumentException("HBL을 찾을 수 없습니다: " + hblId));
            
            if (hbl.getStatus() != HBL.HblStatus.ISSUED) {
                throw new IllegalStateException("발행된 HBL만 라벨을 생성할 수 있습니다.");
            }
            
            log.info("Generating labels for HBL: {}", hbl.getHblNumber());
            
            // 라벨 생성 (주문의 박스 수량만큼)
            List<String> labelPaths = generateLabelsForHBL(hbl, request);
            
            // 생성된 라벨 경로들을 HBL에 저장
            labelPaths.forEach(hbl::addLabelPath);
            
            // 인쇄 완료 처리
            hbl.markAsPrinted();
            
            HBL savedHbl = hblRepository.save(hbl);
            log.info("Labels generated and HBL marked as printed: {}", hbl.getHblNumber());
            
            return savedHbl;
            
        } catch (Exception e) {
            log.error("Error generating labels for HBL: {}", hblId, e);
            throw new RuntimeException("라벨 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * HBL 사본 발행
     */
    @Transactional
    public HBL issueCopy(Long hblId, String requester) {
        try {
            HBL hbl = hblRepository.findById(hblId)
                .orElseThrow(() -> new IllegalArgumentException("HBL을 찾을 수 없습니다: " + hblId));
            
            if (hbl.getStatus() != HBL.HblStatus.ISSUED && hbl.getStatus() != HBL.HblStatus.PRINTED) {
                throw new IllegalStateException("발행된 HBL만 사본을 발행할 수 있습니다.");
            }
            
            // 사본 발행 처리
            hbl.issueCopy();
            
            HBL savedHbl = hblRepository.save(hbl);
            log.info("Copy issued for HBL: {} by {}, total copies: {}", 
                    hbl.getHblNumber(), requester, hbl.getCopyCount());
            
            return savedHbl;
            
        } catch (Exception e) {
            log.error("Error issuing copy for HBL: {}", hblId, e);
            throw new RuntimeException("HBL 사본 발행 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사용자별 HBL 조회
     */
    public List<HBL> getHBLsByUserId(Long userId) {
        try {
            return hblRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("Error getting HBLs for user: {}", userId, e);
            throw new RuntimeException("사용자별 HBL 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 발행 대기 중인 HBL 조회 (관리자용)
     */
    public List<HBL> getPendingIssuanceHBLs() {
        try {
            return hblRepository.findPendingIssuance();
        } catch (Exception e) {
            log.error("Error getting pending issuance HBLs", e);
            throw new RuntimeException("발행 대기 HBL 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 인쇄 대기 중인 HBL 조회 (창고용)
     */
    public List<HBL> getPendingPrintHBLs() {
        try {
            return hblRepository.findPendingPrint();
        } catch (Exception e) {
            log.error("Error getting pending print HBLs", e);
            throw new RuntimeException("인쇄 대기 HBL 조회 실패: " + e.getMessage(), e);
        }
    }
    
    private void setupTransportInfo(HBL hbl, Order order, HBLCreateRequest request) {
        // 주문의 운송 타입에 따라 운송 모드 설정
        if (order.getShippingType() == Order.ShippingType.AIR) {
            hbl.setTransportMode(HBL.TransportMode.AIR);
        } else if (order.getShippingType() == Order.ShippingType.SEA) {
            hbl.setTransportMode(HBL.TransportMode.SEA);
        } else {
            hbl.setTransportMode(HBL.TransportMode.MULTIMODAL); // 기본값
        }
        
        // 항구/공항 정보
        hbl.setPortOfLoading(request.getPortOfLoading() != null ? 
                            request.getPortOfLoading() : defaultPortLoading);
        hbl.setPortOfDischarge(request.getPortOfDischarge() != null ? 
                              request.getPortOfDischarge() : defaultPortDischarge);
        hbl.setPlaceOfDelivery(request.getPlaceOfDelivery() != null ? 
                              request.getPlaceOfDelivery() : order.getRecipientAddress());
        
        // 선박/항공편 정보
        hbl.setVesselName(request.getVesselName());
        hbl.setVoyageNumber(request.getVoyageNumber());
    }
    
    private void setupCargoInfo(HBL hbl, Order order, HBLCreateRequest request) {
        // 주문의 박스 정보에서 화물 정보 추출
        hbl.setTotalPackages(order.getBoxes().size());
        hbl.setPackageType("CARTON"); // 기본값
        
        // 총 중량 및 CBM 계산
        BigDecimal totalWeight = order.getBoxes().stream()
            .map(box -> box.getWeight() != null ? box.getWeight() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCbm = order.getBoxes().stream()
            .map(box -> box.getCbm() != null ? box.getCbm() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        hbl.setGrossWeight(totalWeight);
        hbl.setTotalCbm(totalCbm);
        
        // 화물 기술서 (주문 아이템들의 설명을 조합)
        StringBuilder cargoDesc = new StringBuilder();
        order.getItems().forEach(item -> {
            if (cargoDesc.length() > 0) cargoDesc.append(", ");
            cargoDesc.append(item.getDescription());
            if (item.getQuantity() != null && item.getQuantity().intValue() > 1) {
                cargoDesc.append(" (").append(item.getQuantity()).append("EA)");
            }
        });
        hbl.setCargoDescription(cargoDesc.toString());
        
        // 대표 HS 코드 (첫 번째 아이템의 HS 코드 사용)
        if (!order.getItems().isEmpty() && order.getItems().get(0).getHsCode() != null) {
            hbl.setHsCode(order.getItems().get(0).getHsCode());
        }
        
        // 신고가격 (아이템들의 총 가격)
        BigDecimal declaredValue = order.getItems().stream()
            .map(item -> {
                BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                return price.multiply(BigDecimal.valueOf(item.getQuantity().longValue()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        hbl.setDeclaredValue(declaredValue);
        
        // 특별 지시사항
        hbl.setSpecialInstructions(request.getSpecialInstructions());
        hbl.setRemarks(request.getRemarks());
    }
    
    private void generateQrCodeForHBL(HBL hbl) {
        try {
            // QR 코드 데이터 생성 (HBL 번호, 주문 번호, 발행일 등 포함)
            String qrData = String.format("HBL:%s|ORDER:%s|ISSUED:%s|CBM:%s|WEIGHT:%s",
                hbl.getHblNumber(),
                hbl.getOrder().getOrderNumber(),
                hbl.getIssuedAt().toString(),
                hbl.getTotalCbm(),
                hbl.getGrossWeight()
            );
            
            // QR 코드 이미지 파일 경로 (실제 구현 시 파일 시스템 경로)
            String qrPath = String.format("/qrcodes/hbl/%s.png", hbl.getHblNumber());
            
            // QR 코드 설정 (실제 QR 이미지 생성은 별도 서비스에서 처리)
            hbl.setQrCode(qrData, qrPath);
            
            log.info("QR code generated for HBL: {}", hbl.getHblNumber());
            
        } catch (Exception e) {
            log.error("Error generating QR code for HBL: {}", hbl.getHblNumber(), e);
            // QR 코드 생성 실패는 HBL 발행을 중단시키지 않음
        }
    }
    
    private List<String> generateLabelsForHBL(HBL hbl, LabelGenerationRequest request) {
        try {
            // 라벨 생성 로직 (실제 구현 시 LabelGenerationService 사용)
            List<String> labelPaths = new java.util.ArrayList<>();
            
            int labelCount = hbl.getTotalPackages();
            for (int i = 1; i <= labelCount; i++) {
                String labelPath = String.format("/labels/hbl/%s-label-%03d.pdf", 
                                                hbl.getHblNumber(), i);
                labelPaths.add(labelPath);
            }
            
            log.info("Generated {} labels for HBL: {}", labelCount, hbl.getHblNumber());
            return labelPaths;
            
        } catch (Exception e) {
            log.error("Error generating labels for HBL: {}", hbl.getHblNumber(), e);
            throw new RuntimeException("라벨 생성 실패", e);
        }
    }
    
    // DTO 클래스들
    
    public static class HBLCreateRequest {
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
    
    public static class LabelGenerationRequest {
        private String labelFormat = "PDF"; // PDF, PNG, etc.
        private String labelSize = "A4"; // A4, Letter, etc.
        private boolean includeQrCode = true;
        private boolean includeBarcodes = true;
        
        // Getters and Setters
        public String getLabelFormat() { return labelFormat; }
        public void setLabelFormat(String labelFormat) { this.labelFormat = labelFormat; }
        
        public String getLabelSize() { return labelSize; }
        public void setLabelSize(String labelSize) { this.labelSize = labelSize; }
        
        public boolean isIncludeQrCode() { return includeQrCode; }
        public void setIncludeQrCode(boolean includeQrCode) { this.includeQrCode = includeQrCode; }
        
        public boolean isIncludeBarcodes() { return includeBarcodes; }
        public void setIncludeBarcodes(boolean includeBarcodes) { this.includeBarcodes = includeBarcodes; }
    }
}