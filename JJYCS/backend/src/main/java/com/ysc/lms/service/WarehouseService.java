package com.ysc.lms.service;

import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.OrderBox;
import com.ysc.lms.entity.ScanEvent;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.repository.ScanEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WarehouseService {
    
    private final OrderRepository orderRepository;
    private final ScanEventRepository scanEventRepository;
    private final QrCodeService qrCodeService;
    
    /**
     * 입고 스캔 처리
     */
    public Map<String, Object> processInboundScan(String scanCode, String location, String scannedBy) {
        log.info("Processing inbound scan - code: {}, location: {}, scannedBy: {}", scanCode, location, scannedBy);
        
        try {
            // 스캔 코드로 주문 찾기
            Order order = findOrderByScanCode(scanCode);
            if (order == null) {
                return createErrorResponse("유효하지 않은 스캔 코드입니다: " + scanCode);
            }
            
            // 이미 입고 처리된 주문인지 확인
            boolean alreadyInbound = scanEventRepository.existsByOrderNumberAndScanType(
                order.getOrderNumber(), ScanEvent.ScanType.INBOUND);
            
            if (alreadyInbound) {
                return createErrorResponse("이미 입고 처리된 주문입니다: " + order.getOrderNumber());
            }
            
            // 주문 상태 업데이트
            order.setStatus(Order.OrderStatus.IN_WAREHOUSE);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            // 스캔 이벤트 기록
            ScanEvent scanEvent = new ScanEvent();
            scanEvent.setScanCode(scanCode);
            scanEvent.setScanType(ScanEvent.ScanType.INBOUND);
            scanEvent.setOrder(order);
            scanEvent.setLocation(location);
            scanEvent.setScannedBy(scannedBy);
            scanEvent.setNotes("입고 스캔 완료");
            scanEvent.setProcessed(true);
            scanEvent.setCreatedAt(LocalDateTime.now());
            
            scanEventRepository.save(scanEvent);
            
            log.info("Inbound scan completed for order: {}", order.getOrderNumber());
            
            return createSuccessResponse("입고 스캔이 완료되었습니다.", Map.of(
                "orderNumber", order.getOrderNumber(),
                "location", location,
                "scanTime", scanEvent.getCreatedAt(),
                "newStatus", order.getStatus().toString()
            ));
            
        } catch (Exception e) {
            log.error("Error processing inbound scan: ", e);
            return createErrorResponse("입고 스캔 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 출고 스캔 처리
     */
    public Map<String, Object> processOutboundScan(String scanCode, String destination, String scannedBy) {
        log.info("Processing outbound scan - code: {}, destination: {}, scannedBy: {}", scanCode, destination, scannedBy);
        
        try {
            // 스캔 코드로 주문 찾기
            Order order = findOrderByScanCode(scanCode);
            if (order == null) {
                return createErrorResponse("유효하지 않은 스캔 코드입니다: " + scanCode);
            }
            
            // 입고 처리가 되어 있는지 확인
            boolean hasInbound = scanEventRepository.existsByOrderNumberAndScanType(
                order.getOrderNumber(), ScanEvent.ScanType.INBOUND);
            
            if (!hasInbound) {
                return createErrorResponse("입고 처리가 되지 않은 주문입니다: " + order.getOrderNumber());
            }
            
            // 이미 출고 처리된 주문인지 확인
            boolean alreadyOutbound = scanEventRepository.existsByOrderNumberAndScanType(
                order.getOrderNumber(), ScanEvent.ScanType.OUTBOUND);
            
            if (alreadyOutbound) {
                return createErrorResponse("이미 출고 처리된 주문입니다: " + order.getOrderNumber());
            }
            
            // 주문 상태 업데이트
            order.setStatus(Order.OrderStatus.SHIPPED);
            order.setShippedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            // 스캔 이벤트 기록
            ScanEvent scanEvent = new ScanEvent();
            scanEvent.setScanCode(scanCode);
            scanEvent.setScanType(ScanEvent.ScanType.OUTBOUND);
            scanEvent.setOrder(order);
            scanEvent.setLocation(destination);
            scanEvent.setScannedBy(scannedBy);
            scanEvent.setNotes("출고 스캔 완료");
            scanEvent.setProcessed(true);
            scanEvent.setCreatedAt(LocalDateTime.now());
            
            scanEventRepository.save(scanEvent);
            
            log.info("Outbound scan completed for order: {}", order.getOrderNumber());
            
            return createSuccessResponse("출고 스캔이 완료되었습니다.", Map.of(
                "orderNumber", order.getOrderNumber(),
                "destination", destination,
                "scanTime", scanEvent.getCreatedAt(),
                "newStatus", order.getStatus().toString()
            ));
            
        } catch (Exception e) {
            log.error("Error processing outbound scan: ", e);
            return createErrorResponse("출고 스캔 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 일반 창고 스캔 처리
     */
    public Map<String, Object> processWarehouseScan(String scanCode, ScanEvent.ScanType scanType, String location, String scannedBy, String notes) {
        log.info("Processing warehouse scan - code: {}, type: {}, location: {}", scanCode, scanType, location);
        
        try {
            // 스캔 코드로 주문 찾기
            Order order = findOrderByScanCode(scanCode);
            if (order == null) {
                return createErrorResponse("유효하지 않은 스캔 코드입니다: " + scanCode);
            }
            
            // 스캔 이벤트 기록
            ScanEvent scanEvent = new ScanEvent();
            scanEvent.setScanCode(scanCode);
            scanEvent.setScanType(scanType);
            scanEvent.setOrder(order);
            scanEvent.setLocation(location);
            scanEvent.setScannedBy(scannedBy);
            scanEvent.setNotes(notes);
            scanEvent.setProcessed(true);
            scanEvent.setCreatedAt(LocalDateTime.now());
            
            scanEventRepository.save(scanEvent);
            
            // 스캔 타입에 따른 추가 처리
            if (scanType == ScanEvent.ScanType.HOLD) {
                order.setStatus(Order.OrderStatus.HOLD);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
            
            log.info("Warehouse scan completed for order: {} with type: {}", order.getOrderNumber(), scanType);
            
            return createSuccessResponse("스캔이 완료되었습니다.", Map.of(
                "orderNumber", order.getOrderNumber(),
                "scanType", scanType.toString(),
                "location", location,
                "scanTime", scanEvent.getCreatedAt()
            ));
            
        } catch (Exception e) {
            log.error("Error processing warehouse scan: ", e);
            return createErrorResponse("창고 스캔 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 스캔 코드로 주문 찾기
     */
    private Order findOrderByScanCode(String scanCode) {
        // 주문 번호로 직접 찾기
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(scanCode);
        if (orderOpt.isPresent()) {
            return orderOpt.get();
        }
        
        // QR 코드로 찾기 (박스 레이블 등)
        // TODO: OrderBox 기능은 나중에 구현
        List<OrderBox> boxes = new ArrayList<>();
        if (!boxes.isEmpty()) {
            return boxes.get(0).getOrder();
        }
        
        return null;
    }
    
    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.putAll(data);
        return response;
    }
    
    /**
     * 오류 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
    
    /**
     * 주문별 스캔 히스토리 조회
     */
    public List<ScanEvent> getScanHistory(String orderNumber) {
        return scanEventRepository.findByOrderIdOrderByCreatedAtDesc(
            orderRepository.findByOrderNumber(orderNumber).map(Order::getId).orElse(null)
        );
    }
    
    /**
     * 일괄 처리 작업
     */
    public Map<String, Object> processBatchOperation(List<String> itemCodes, String processType, 
                                                   String location, String scannedBy) {
        log.info("Processing batch operation - type: {}, items: {}, location: {}", processType, itemCodes.size(), location);
        
        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        ScanEvent.ScanType scanType;
        try {
            scanType = ScanEvent.ScanType.valueOf(processType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 처리 타입입니다: " + processType);
        }
        
        for (String itemCode : itemCodes) {
            try {
                Map<String, Object> result;
                
                switch (scanType) {
                    case INBOUND:
                        result = processInboundScan(itemCode, location, scannedBy);
                        break;
                    case OUTBOUND:
                        result = processOutboundScan(itemCode, location, scannedBy);
                        break;
                    default:
                        result = processWarehouseScan(itemCode, scanType, location, scannedBy, 
                            "일괄 처리: " + processType);
                        break;
                }
                
                results.add(Map.of(
                    "itemCode", itemCode,
                    "success", result.get("success"),
                    "message", result.getOrDefault("message", result.getOrDefault("error", ""))
                ));
                
                if ((Boolean) result.get("success")) {
                    successCount++;
                } else {
                    failCount++;
                }
                
            } catch (Exception e) {
                log.error("Batch processing failed for item: {}", itemCode, e);
                results.add(Map.of(
                    "itemCode", itemCode,
                    "success", false,
                    "message", "처리 실패: " + e.getMessage()
                ));
                failCount++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", String.format("일괄 처리 완료 - 성공: %d, 실패: %d", successCount, failCount));
        response.put("processType", processType);
        response.put("location", location);
        response.put("totalCount", itemCodes.size());
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        response.put("results", results);
        response.put("processedAt", LocalDateTime.now());
        response.put("processedBy", scannedBy);
        
        log.info("Batch operation completed - success: {}, failed: {}", successCount, failCount);
        
        return response;
    }
    
    /**
     * 재고 현황 조회 (실제 데이터)
     */
    public Map<String, Object> getInventoryStatus(String warehouseId, String status) {
        log.info("Getting inventory status for warehouse: {}, status: {}", warehouseId, status);
        
        try {
            // 창고별 주문 수 통계
            List<Order> orders;
            if (status != null && !status.trim().isEmpty()) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus);
            } else {
                orders = orderRepository.findAll();
            }
            
            // 상태별 그룹핑
            Map<String, Integer> statusCounts = new HashMap<>();
            List<Map<String, Object>> inventoryItems = new ArrayList<>();
            
            for (Order order : orders) {
                String orderStatus = order.getStatus().toString();
                statusCounts.put(orderStatus, statusCounts.getOrDefault(orderStatus, 0) + 1);
                
                Map<String, Object> item = new HashMap<>();
                item.put("orderNumber", order.getOrderNumber());
                item.put("status", orderStatus);
                item.put("createdAt", order.getCreatedAt());
                item.put("updatedAt", order.getUpdatedAt());
                item.put("recipientName", order.getRecipientName());
                item.put("orderType", order.getOrderType().toString());
                
                // 최근 스캔 이벤트 조회
                List<ScanEvent> recentScans = scanEventRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
                if (!recentScans.isEmpty()) {
                    ScanEvent lastScan = recentScans.get(0);
                    item.put("lastScanType", lastScan.getScanType().toString());
                    item.put("lastScanLocation", lastScan.getLocation());
                    item.put("lastScannedAt", lastScan.getCreatedAt());
                    item.put("lastScannedBy", lastScan.getScannedBy());
                }
                
                inventoryItems.add(item);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("warehouseId", warehouseId);
            response.put("statusFilter", status);
            response.put("inventory", inventoryItems);
            response.put("totalCount", inventoryItems.size());
            response.put("statusCounts", statusCounts);
            response.put("queriedAt", LocalDateTime.now());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting inventory status", e);
            throw new RuntimeException("재고 현황 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 창고 대시보드 통계
     */
    public Map<String, Object> getWarehouseStats() {
        log.info("Getting warehouse dashboard statistics");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            // 전체 주문 통계
            List<Order> allOrders = orderRepository.findAll();
            Map<String, Integer> statusCounts = new HashMap<>();
            
            for (Order order : allOrders) {
                String status = order.getStatus().toString();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
            
            // 스캔 이벤트 통계
            List<ScanEvent> todayScans = scanEventRepository.findByDateRange(startOfDay, now);
            List<ScanEvent> weekScans = scanEventRepository.findByDateRange(startOfWeek, now);
            List<ScanEvent> monthScans = scanEventRepository.findByDateRange(startOfMonth, now);
            
            Map<String, Integer> todayScanTypes = new HashMap<>();
            Map<String, Integer> weekScanTypes = new HashMap<>();
            
            for (ScanEvent scan : todayScans) {
                String type = scan.getScanType().toString();
                todayScanTypes.put(type, todayScanTypes.getOrDefault(type, 0) + 1);
            }
            
            for (ScanEvent scan : weekScans) {
                String type = scan.getScanType().toString();
                weekScanTypes.put(type, weekScanTypes.getOrDefault(type, 0) + 1);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalOrders", allOrders.size());
            response.put("orderStatusCounts", statusCounts);
            response.put("todayScans", todayScans.size());
            response.put("weekScans", weekScans.size());
            response.put("monthScans", monthScans.size());
            response.put("todayScanTypes", todayScanTypes);
            response.put("weekScanTypes", weekScanTypes);
            response.put("generatedAt", now);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting warehouse stats", e);
            throw new RuntimeException("창고 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 처리 대기 목록
     */
    public Map<String, Object> getPendingItems(String type) {
        log.info("Getting pending items, type: {}", type);
        
        try {
            List<Order> pendingOrders = new ArrayList<>();
            
            if (type == null || "inbound".equals(type)) {
                // 입고 대기: PENDING 또는 CONFIRMED 상태
                pendingOrders.addAll(orderRepository.findByStatus(Order.OrderStatus.PENDING));
                pendingOrders.addAll(orderRepository.findByStatus(Order.OrderStatus.CONFIRMED));
            } else if ("outbound".equals(type)) {
                // 출고 대기: IN_WAREHOUSE 상태
                pendingOrders.addAll(orderRepository.findByStatus(Order.OrderStatus.IN_WAREHOUSE));
            } else if ("hold".equals(type)) {
                // 보류: HOLD 상태
                pendingOrders.addAll(orderRepository.findByStatus(Order.OrderStatus.HOLD));
            }
            
            List<Map<String, Object>> pendingItems = new ArrayList<>();
            
            for (Order order : pendingOrders) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderNumber", order.getOrderNumber());
                item.put("status", order.getStatus().toString());
                item.put("orderType", order.getOrderType().toString());
                item.put("recipientName", order.getRecipientName());
                item.put("createdAt", order.getCreatedAt());
                item.put("updatedAt", order.getUpdatedAt());
                
                // 대기 시간 계산 (시간)
                long waitingHours = java.time.Duration.between(order.getCreatedAt(), LocalDateTime.now()).toHours();
                item.put("waitingHours", waitingHours);
                
                // 우선순위 (대기 시간이 길수록, 항공일수록 높음)
                int priority = (int) waitingHours + (order.getOrderType() == Order.ShippingType.AIR ? 50 : 0);
                item.put("priority", priority);
                
                pendingItems.add(item);
            }
            
            // 우선순위 순으로 정렬
            pendingItems.sort((a, b) -> Integer.compare((Integer) b.get("priority"), (Integer) a.get("priority")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", type);
            response.put("pendingItems", pendingItems);
            response.put("totalCount", pendingItems.size());
            response.put("queriedAt", LocalDateTime.now());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting pending items", e);
            throw new RuntimeException("대기 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}