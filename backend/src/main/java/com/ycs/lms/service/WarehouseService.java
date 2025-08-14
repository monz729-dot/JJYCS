package com.ycs.lms.service;

import com.ycs.lms.dto.*;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.OrderBoxRepository;
import com.ycs.lms.repository.UserRepository;
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

    private final OrderRepository orderRepository;
    private final OrderBoxRepository orderBoxRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;
    
    // Mock storage for warehouse/inventory data (will be replaced with database entities later)
    private final Map<Long, Map<String, Object>> warehouseData = new HashMap<>();
    private final Map<String, Map<String, Object>> inventoryData = new HashMap<>();
    private final Map<Long, Map<String, Object>> scanEventsData = new HashMap<>();
    private Long scanEventIdCounter = 1L;

    @Transactional
    public ScanResponse scanBox(ScanRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        
        // 1. 박스 정보 조회
        OrderBox box = orderBoxRepository.findByLabelCode(request.getLabelCode())
            .orElseThrow(() -> new NotFoundException("라벨 코드에 해당하는 박스를 찾을 수 없습니다: " + request.getLabelCode()));
        
        // 2. 창고 정보 조회 (Mock)
        Map<String, Object> warehouse = getWarehouseData(request.getWarehouseId());
        if (warehouse == null) {
            throw new NotFoundException("창고를 찾을 수 없습니다: " + request.getWarehouseId());
        }
        
        // 3. 상태 전환 검증
        String previousStatus = box.getStatus().name();
        String newStatus = determineNewStatus(request.getScanType(), previousStatus);
        
        if (!isValidStatusTransition(previousStatus, newStatus)) {
            throw new BadRequestException(
                String.format("잘못된 상태 전환입니다: %s -> %s", previousStatus, newStatus));
        }
        
        // 4. 박스 상태 업데이트
        try {
            box.setStatus(OrderBox.BoxStatus.valueOf(newStatus.toUpperCase()));
            box.setWarehouseId(request.getWarehouseId());
            box.setWarehouseLocation(request.getLocation());
            orderBoxRepository.save(box);
        } catch (Exception e) {
            throw new BadRequestException("박스 상태 업데이트에 실패했습니다: " + e.getMessage());
        }
        
        // 5. 재고 관리 업데이트
        updateInventory(request.getScanType(), box.getId(), request.getWarehouseId(), 
                       request.getLocation(), newStatus);
        
        // 6. 스캔 이벤트 기록 (Mock)
        recordScanEvent(
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
        String batchId = "BATCH-" + System.currentTimeMillis();
        
        // 1. 박스 정보 조회
        List<OrderBox> boxes = orderBoxRepository.findByLabelCodeIn(request.getLabelCodes());
        
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
                String previousStatus = box.getStatus().name();
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
                try {
                    box.setStatus(OrderBox.BoxStatus.valueOf(newStatus.toUpperCase()));
                    box.setWarehouseId(request.getWarehouseId());
                    box.setWarehouseLocation(request.getLocation());
                    orderBoxRepository.save(box);
                    // 재고 업데이트
                    updateInventory(request.getAction(), box.getId(), request.getWarehouseId(),
                                   request.getLocation(), newStatus);
                    
                    // 스캔 이벤트 기록 (Mock)
                    recordScanEvent(
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
                } catch (Exception updateException) {
                    results.add(BatchProcessResponse.ProcessResult.builder()
                        .labelCode(box.getLabelCode())
                        .status("failed")
                        .reason("Database update failed: " + updateException.getMessage())
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
        // Mock inventory data - in real implementation, query from inventory table
        List<InventoryResponse> mockData = generateMockInventory(warehouseId, status);
        
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), mockData.size());
        
        List<InventoryResponse> pageContent = mockData.subList(start, end);
        
        return PageResponse.<InventoryResponse>builder()
            .content(pageContent)
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements((long) mockData.size())
            .totalPages((int) Math.ceil((double) mockData.size() / pageable.getPageSize()))
            .first(pageable.getPageNumber() == 0)
            .last(end >= mockData.size())
            .build();
    }

    @Transactional
    public BoxResponse holdBox(Long boxId, String reason) {
        OrderBox box = orderBoxRepository.findById(boxId)
            .orElseThrow(() -> new NotFoundException("박스를 찾을 수 없습니다: " + boxId));
        
        UserPrincipal currentUser = getCurrentUser();
        String previousStatus = box.getStatus().name();
        String newStatus = "hold";
        
        // 상태 업데이트
        box.setStatus(OrderBox.BoxStatus.valueOf(newStatus.toUpperCase()));
        orderBoxRepository.save(box);
        
        // 스캔 이벤트 기록 (Mock)
        recordScanEvent(
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
        // Mock scan history data
        List<ScanEventResponse> mockEvents = generateMockScanHistory(labelCode, startDate, endDate);
        
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), mockEvents.size());
        
        List<ScanEventResponse> pageContent = mockEvents.subList(start, end);
        
        return PageResponse.<ScanEventResponse>builder()
            .content(pageContent)
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements((long) mockEvents.size())
            .totalPages((int) Math.ceil((double) mockEvents.size() / pageable.getPageSize()))
            .first(pageable.getPageNumber() == 0)
            .last(end >= mockEvents.size())
            .build();
    }

    public LabelResponse generateLabel(Long boxId) {
        // TODO: QR 코드 및 라벨 생성 로직
        String qrCodeUrl = qrCodeService.generateQrCode(boxId);
        
        // 박스 정보에 QR 코드 URL 업데이트
        OrderBox box = orderBoxRepository.findById(boxId).orElse(null);
        if (box != null) {
            box.setQrCodeUrl(qrCodeUrl);
            orderBoxRepository.save(box);
        }
        
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
        // Mock inventory update - in real implementation, update inventory table
        switch (scanType.toLowerCase()) {
            case "inbound" -> log.info("Mock: Update inventory for inbound - boxId: {}, warehouseId: {}, location: {}", boxId, warehouseId, location);
            case "outbound" -> log.info("Mock: Update inventory for outbound - boxId: {}, status: shipped", boxId);
            case "hold" -> log.info("Mock: Update inventory for hold - boxId: {}, status: hold", boxId);
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
    
    // Mock helper methods
    
    private Map<String, Object> getWarehouseData(Long warehouseId) {
        return warehouseData.computeIfAbsent(warehouseId, id -> {
            Map<String, Object> warehouse = new HashMap<>();
            warehouse.put("id", id);
            warehouse.put("name", "Warehouse " + id);
            warehouse.put("code", "WH" + String.format("%03d", id));
            return warehouse;
        });
    }
    
    private void recordScanEvent(String eventType, Long boxId, Long warehouseId, Long userId,
                                String previousStatus, String newStatus, String location,
                                String batchId, String deviceInfo, String notes, LocalDateTime timestamp) {
        Long eventId = scanEventIdCounter++;
        Map<String, Object> event = new HashMap<>();
        event.put("id", eventId);
        event.put("event_type", eventType);
        event.put("box_id", boxId);
        event.put("warehouse_id", warehouseId);
        event.put("user_id", userId);
        event.put("previous_status", previousStatus);
        event.put("new_status", newStatus);
        event.put("location_code", location);
        event.put("batch_id", batchId);
        event.put("device_info", deviceInfo);
        event.put("notes", notes);
        event.put("scan_timestamp", timestamp);
        
        scanEventsData.put(eventId, event);
        log.info("Scan event recorded: eventId={}, type={}, boxId={}", eventId, eventType, boxId);
    }
    
    private List<InventoryResponse> generateMockInventory(Long warehouseId, String status) {
        List<InventoryResponse> inventory = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            inventory.add(InventoryResponse.builder()
                .boxId((long) i)
                .labelCode("BOX-2024-00" + String.format("%03d", i) + "-01")
                .orderId((long) i)
                .orderCode("YCS-2024-00" + String.format("%03d", i))
                .status(status != null ? status : "inbound_completed")
                .location("A-" + (i % 3 + 1) + "-" + String.format("%02d", i % 10 + 1))
                .inboundDate(LocalDateTime.now().minusDays(i))
                .expectedOutboundDate(LocalDateTime.now().plusDays(7 - i))
                .daysInWarehouse(i)
                .cbm(BigDecimal.valueOf(0.5 + (i * 0.1)))
                .weight(BigDecimal.valueOf(2.0 + (i * 0.5)))
                .customer(InventoryResponse.CustomerInfo.builder()
                    .name("Customer " + i)
                    .memberCode("MEM" + String.format("%03d", i))
                    .role("INDIVIDUAL")
                    .build())
                .build());
        }
        
        return inventory;
    }
    
    private List<ScanEventResponse> generateMockScanHistory(String labelCode, String startDate, String endDate) {
        List<ScanEventResponse> events = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            events.add(ScanEventResponse.builder()
                .eventId((long) i)
                .eventType(i % 2 == 0 ? "INBOUND" : "OUTBOUND")
                .labelCode(labelCode)
                .orderId((long) (i % 3 + 1))
                .orderCode("YCS-2024-00" + String.format("%03d", i % 3 + 1))
                .previousStatus(i == 1 ? "created" : "inbound_completed")
                .newStatus(i % 2 == 0 ? "inbound_completed" : "outbound_completed")
                .location("A-" + (i % 3 + 1) + "-01")
                .batchId(i % 3 == 0 ? "BATCH-" + (System.currentTimeMillis() - i * 1000) : null)
                .deviceInfo("YCS-LMS-Scanner/1.0")
                .notes("Mock scan event " + i)
                .scanTimestamp(LocalDateTime.now().minusHours(i))
                .scannedBy(ScanEventResponse.UserInfo.builder()
                    .name("Warehouse Staff " + (i % 2 + 1))
                    .role("WAREHOUSE")
                    .build())
                .warehouse(ScanEventResponse.WarehouseInfo.builder()
                    .name("Main Warehouse")
                    .code("WH001")
                    .build())
                .build());
        }
        
        return events;
    }
}