package com.ycs.lms.service;

import com.ycs.lms.dto.*;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.mapper.OrderMapper;
import com.ycs.lms.mapper.WarehouseMapper;
import com.ycs.lms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final OrderMapper orderMapper;
    private final QrCodeService qrCodeService;

    @Transactional
    public ScanResponse scanBox(ScanRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        
        // 1. 박스 정보 조회
        OrderBox box = orderMapper.findOrderBoxByLabelCode(request.getLabelCode());
        if (box == null) {
            throw new NotFoundException("라벨 코드에 해당하는 박스를 찾을 수 없습니다: " + request.getLabelCode());
        }
        
        // 2. 창고 정보 조회
        Map<String, Object> warehouse = warehouseMapper.findWarehouseById(request.getWarehouseId());
        if (warehouse == null) {
            throw new NotFoundException("창고를 찾을 수 없습니다: " + request.getWarehouseId());
        }
        
        // 3. 상태 전환 검증
        String previousStatus = box.getStatus();
        String newStatus = determineNewStatus(request.getScanType(), previousStatus);
        
        if (!isValidStatusTransition(previousStatus, newStatus)) {
            throw new BadRequestException(
                String.format("잘못된 상태 전환입니다: %s -> %s", previousStatus, newStatus));
        }
        
        // 4. 박스 상태 업데이트
        int updated = warehouseMapper.updateBoxStatus(request.getLabelCode(), newStatus, 
                                                     request.getWarehouseId(), request.getLocation());
        if (updated == 0) {
            throw new BadRequestException("박스 상태 업데이트에 실패했습니다.");
        }
        
        // 5. 재고 관리 업데이트
        updateInventory(request.getScanType(), box.getId(), request.getWarehouseId(), 
                       request.getLocation(), newStatus);
        
        // 6. 스캔 이벤트 기록
        warehouseMapper.insertScanEvent(
            request.getScanType().toUpperCase(),
            box.getId(),
            request.getWarehouseId(),
            currentUser.getId(),
            previousStatus,
            newStatus,
            request.getLocation(),
            null, // batch_id (단일 스캔)
            getUserAgent(), // device_info
            request.getNote(),
            LocalDateTime.now()
        );
        
        // 7. 리패킹 사진 처리 (필요한 경우)
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            // TODO: 사진 저장 로직 구현
            log.info("Repack photos uploaded for box: {}, count: {}", 
                    request.getLabelCode(), request.getPhotos().size());
        }
        
        // 8. 응답 생성
        ScanResponse response = buildScanResponse(box, warehouse, newStatus, previousStatus);
        
        log.info("Box scan completed: labelCode={}, scanType={}, status={}->{}",
                request.getLabelCode(), request.getScanType(), previousStatus, newStatus);
        
        return response;
    }

    @Transactional
    public BatchProcessResponse batchProcess(BatchProcessRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        String batchId = warehouseMapper.generateBatchId();
        
        // 1. 박스 정보 조회
        List<OrderBox> boxes = warehouseMapper.findBoxesByLabelCodes(request.getLabelCodes());
        
        if (boxes.size() != request.getLabelCodes().size()) {
            List<String> foundCodes = boxes.stream()
                .map(OrderBox::getLabelCode)
                .collect(Collectors.toList());
            List<String> notFound = request.getLabelCodes().stream()
                .filter(code -> !foundCodes.contains(code))
                .collect(Collectors.toList());
            
            throw new BadRequestException("다음 라벨 코드를 찾을 수 없습니다: " + String.join(", ", notFound));
        }
        
        // 2. 일괄 처리
        List<BatchProcessResponse.ProcessResult> results = new ArrayList<>();
        int processed = 0;
        int failed = 0;
        
        for (OrderBox box : boxes) {
            try {
                String previousStatus = box.getStatus();
                String newStatus = determineNewStatus(request.getAction(), previousStatus);
                
                if (!isValidStatusTransition(previousStatus, newStatus)) {
                    results.add(BatchProcessResponse.ProcessResult.builder()
                        .labelCode(box.getLabelCode())
                        .status("skipped")
                        .reason("Invalid status transition: " + previousStatus + " -> " + newStatus)
                        .previousStatus(previousStatus)
                        .build());
                    continue;
                }
                
                // 상태 업데이트
                int updated = warehouseMapper.updateBoxStatus(box.getLabelCode(), newStatus,
                                                             request.getWarehouseId(), request.getLocation());
                
                if (updated > 0) {
                    // 재고 업데이트
                    updateInventory(request.getAction(), box.getId(), request.getWarehouseId(),
                                   request.getLocation(), newStatus);
                    
                    // 스캔 이벤트 기록
                    warehouseMapper.insertScanEvent(
                        request.getAction().toUpperCase(),
                        box.getId(),
                        request.getWarehouseId(),
                        currentUser.getId(),
                        previousStatus,
                        newStatus,
                        request.getLocation(),
                        batchId,
                        getUserAgent(),
                        request.getNote(),
                        LocalDateTime.now()
                    );
                    
                    results.add(BatchProcessResponse.ProcessResult.builder()
                        .labelCode(box.getLabelCode())
                        .status("success")
                        .newStatus(newStatus)
                        .previousStatus(previousStatus)
                        .build());
                    
                    processed++;
                } else {
                    results.add(BatchProcessResponse.ProcessResult.builder()
                        .labelCode(box.getLabelCode())
                        .status("failed")
                        .reason("Database update failed")
                        .previousStatus(previousStatus)
                        .build());
                    failed++;
                }
                
            } catch (Exception e) {
                log.error("Batch process failed for box: {}", box.getLabelCode(), e);
                results.add(BatchProcessResponse.ProcessResult.builder()
                    .labelCode(box.getLabelCode())
                    .status("failed")
                    .reason(e.getMessage())
                    .build());
                failed++;
            }
        }
        
        // 3. 요약 정보 생성
        BatchProcessResponse.Summary summary = calculateBatchSummary(boxes);
        
        log.info("Batch process completed: batchId={}, processed={}, failed={}, action={}",
                batchId, processed, failed, request.getAction());
        
        return BatchProcessResponse.builder()
            .batchId(batchId)
            .processed(processed)
            .failed(failed)
            .results(results)
            .summary(summary)
            .build();
    }

    public PageResponse<InventoryResponse> getInventory(Long warehouseId, String status, Pageable pageable) {
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        List<Map<String, Object>> inventoryData = warehouseMapper.findInventory(
            warehouseId, status, pageable.getPageSize(), offset);
        
        long total = warehouseMapper.countInventory(warehouseId, status);
        
        List<InventoryResponse> responses = inventoryData.stream()
            .map(this::mapToInventoryResponse)
            .collect(Collectors.toList());
        
        return PageResponse.of(responses, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    @Transactional
    public BoxResponse holdBox(Long boxId, String reason) {
        OrderBox box = orderMapper.findOrderBoxByLabelCode(boxId.toString()); // TODO: findById 메서드 필요
        if (box == null) {
            throw new NotFoundException("박스를 찾을 수 없습니다: " + boxId);
        }
        
        UserPrincipal currentUser = getCurrentUser();
        String previousStatus = box.getStatus();
        String newStatus = "hold";
        
        // 상태 업데이트
        warehouseMapper.updateBoxStatusOnly(box.getLabelCode(), newStatus);
        
        // 스캔 이벤트 기록
        warehouseMapper.insertScanEvent(
            "HOLD",
            box.getId(),
            box.getWarehouseId(),
            currentUser.getId(),
            previousStatus,
            newStatus,
            box.getWarehouseLocation(),
            null,
            getUserAgent(),
            reason,
            LocalDateTime.now()
        );
        
        log.info("Box put on hold: boxId={}, reason={}", boxId, reason);
        
        return BoxResponse.builder()
            .boxId(box.getId())
            .labelCode(box.getLabelCode())
            .status(newStatus)
            .previousStatus(previousStatus)
            .note(reason)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Transactional
    public MixboxResponse createMixbox(MixboxRequest request) {
        // TODO: 믹스박스 로직 구현
        // 1. 원본 박스들 조회 및 검증
        // 2. 새 박스 생성
        // 3. 아이템들 새 박스로 이동
        // 4. 원본 박스들 상태 변경
        // 5. 공간 절약 계산
        
        throw new RuntimeException("믹스박스 기능은 추후 구현 예정입니다.");
    }

    public PageResponse<ScanEventResponse> getScanHistory(String labelCode, String startDate, 
                                                         String endDate, Pageable pageable) {
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        List<Map<String, Object>> events = warehouseMapper.findScanEvents(
            labelCode, startDate, endDate, pageable.getPageSize(), offset);
        
        long total = warehouseMapper.countScanEvents(labelCode, startDate, endDate);
        
        List<ScanEventResponse> responses = events.stream()
            .map(this::mapToScanEventResponse)
            .collect(Collectors.toList());
        
        return PageResponse.of(responses, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    public LabelResponse generateLabel(Long boxId) {
        // TODO: QR 코드 및 라벨 생성 로직
        String qrCodeUrl = qrCodeService.generateQrCode(boxId);
        
        // 박스 정보에 QR 코드 URL 업데이트
        warehouseMapper.updateBoxQrCode(boxId, qrCodeUrl);
        
        return LabelResponse.builder()
            .boxId(boxId)
            .qrCodeUrl(qrCodeUrl)
            .qrCodeData("{\"boxId\":" + boxId + ",\"type\":\"box_label\"}")
            .printable(true)
            .format("PNG")
            .build();
    }

    public void printLabel(Long boxId) {
        // TODO: 프린터 연동 로직
        log.info("Print label requested for box: {}", boxId);
        // 실제 구현에서는 프린터 API 호출
    }

    // Private helper methods
    private String determineNewStatus(String scanType, String currentStatus) {
        return switch (scanType.toLowerCase()) {
            case "inbound" -> "inbound_completed";
            case "outbound" -> "outbound_completed";
            case "hold" -> "hold";
            case "mixbox" -> "mixbox";
            case "inventory" -> currentStatus; // 재고 조사는 상태 변경 없음
            default -> throw new BadRequestException("지원되지 않는 스캔 타입입니다: " + scanType);
        };
    }

    private boolean isValidStatusTransition(String from, String to) {
        // 상태 전환 규칙 정의
        Map<String, List<String>> validTransitions = Map.of(
            "created", List.of("inbound_completed", "hold"),
            "inbound_pending", List.of("inbound_completed", "hold"),
            "inbound_completed", List.of("ready_for_outbound", "outbound_completed", "hold", "mixbox"),
            "ready_for_outbound", List.of("outbound_completed", "hold"),
            "hold", List.of("inbound_completed", "ready_for_outbound", "outbound_completed"),
            "mixbox", List.of("outbound_completed", "hold")
        );
        
        return validTransitions.getOrDefault(from, List.of()).contains(to);
    }

    private void updateInventory(String scanType, Long boxId, Long warehouseId, String location, String newStatus) {
        switch (scanType.toLowerCase()) {
            case "inbound" -> warehouseMapper.upsertInventory(warehouseId, boxId, location, "stored");
            case "outbound" -> warehouseMapper.updateInventoryStatus(warehouseId, boxId, "shipped");
            case "hold" -> warehouseMapper.updateInventoryStatus(warehouseId, boxId, "hold");
        }
    }

    private ScanResponse buildScanResponse(OrderBox box, Map<String, Object> warehouse, 
                                         String newStatus, String previousStatus) {
        return ScanResponse.builder()
            .box(ScanResponse.BoxInfo.builder()
                .boxId(box.getId())
                .labelCode(box.getLabelCode())
                .orderId(box.getOrderId())
                .status(newStatus)
                .previousStatus(previousStatus)
                .cbm(box.getCbmM3())
                .weight(box.getWeightKg())
                .build())
            .warehouse(ScanResponse.WarehouseInfo.builder()
                .id((Long) warehouse.get("id"))
                .name((String) warehouse.get("name"))
                .code((String) warehouse.get("code"))
                .build())
            .timestamp(LocalDateTime.now())
            .nextAction(determineNextAction(newStatus))
            .build();
    }

    private String determineNextAction(String currentStatus) {
        return switch (currentStatus) {
            case "inbound_completed" -> "ready_for_outbound";
            case "ready_for_outbound" -> "outbound_scan";
            case "outbound_completed" -> "shipped";
            case "hold" -> "resolve_hold";
            default -> "none";
        };
    }

    private BatchProcessResponse.Summary calculateBatchSummary(List<OrderBox> boxes) {
        Set<Long> orderIds = boxes.stream().map(OrderBox::getOrderId).collect(Collectors.toSet());
        BigDecimal totalCbm = boxes.stream()
            .map(OrderBox::getCbmM3)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWeight = boxes.stream()
            .map(OrderBox::getWeightKg)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return BatchProcessResponse.Summary.builder()
            .totalBoxes(boxes.size())
            .totalOrders(orderIds.size())
            .totalCbm(totalCbm)
            .totalWeight(totalWeight)
            .estimatedShippingCost(calculateEstimatedCost(totalCbm, totalWeight))
            .build();
    }

    private BigDecimal calculateEstimatedCost(BigDecimal cbm, BigDecimal weight) {
        // 간단한 비용 계산 로직 (실제로는 더 복잡한 요율 적용)
        BigDecimal cbmCost = cbm.multiply(BigDecimal.valueOf(120000)); // CBM당 12만원
        BigDecimal weightCost = weight.multiply(BigDecimal.valueOf(8000)); // kg당 8천원
        return cbmCost.max(weightCost); // 둘 중 큰 값
    }

    private InventoryResponse mapToInventoryResponse(Map<String, Object> data) {
        return InventoryResponse.builder()
            .boxId((Long) data.get("box_id"))
            .labelCode((String) data.get("label_code"))
            .orderId((Long) data.get("order_id"))
            .orderCode((String) data.get("order_code"))
            .status((String) data.get("status"))
            .location((String) data.get("location_code"))
            .inboundDate((LocalDateTime) data.get("inbound_date"))
            .expectedOutboundDate((LocalDateTime) data.get("expected_outbound_date"))
            .daysInWarehouse((Integer) data.get("days_in_warehouse"))
            .cbm((BigDecimal) data.get("cbm_m3"))
            .weight((BigDecimal) data.get("weight_kg"))
            .customer(InventoryResponse.CustomerInfo.builder()
                .name((String) data.get("customer_name"))
                .memberCode((String) data.get("member_code"))
                .role((String) data.get("customer_role"))
                .build())
            .build();
    }

    private ScanEventResponse mapToScanEventResponse(Map<String, Object> data) {
        return ScanEventResponse.builder()
            .eventId((Long) data.get("id"))
            .eventType((String) data.get("event_type"))
            .labelCode((String) data.get("label_code"))
            .orderId((Long) data.get("order_id"))
            .orderCode((String) data.get("order_code"))
            .previousStatus((String) data.get("previous_status"))
            .newStatus((String) data.get("new_status"))
            .location((String) data.get("location_code"))
            .batchId((String) data.get("batch_id"))
            .deviceInfo((String) data.get("device_info"))
            .notes((String) data.get("notes"))
            .scanTimestamp((LocalDateTime) data.get("scan_timestamp"))
            .scannedBy(ScanEventResponse.UserInfo.builder()
                .name((String) data.get("scanned_by_name"))
                .role((String) data.get("scanned_by_role"))
                .build())
            .warehouse(ScanEventResponse.WarehouseInfo.builder()
                .name((String) data.get("warehouse_name"))
                .code((String) data.get("warehouse_code"))
                .build())
            .build();
    }

    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserAgent() {
        // TODO: HTTP 요청에서 User-Agent 헤더 추출
        return "YCS-LMS-Scanner/1.0";
    }
}