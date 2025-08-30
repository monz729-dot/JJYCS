package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderHistory;
import com.ycs.lms.repository.OrderHistoryRepository;
import com.ycs.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WarehouseService {
    
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    
    /**
     * 바코드/QR 스캔을 통한 입고 처리
     */
    public ScanResult processInbound(String scanCode, String scanType, String location, String notes, String operatorId) {
        try {
            log.info("Processing inbound scan: code={}, type={}, location={}", scanCode, scanType, location);
            
            // 주문 번호로 주문 찾기
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(scanCode);
            if (orderOpt.isEmpty()) {
                return ScanResult.error("주문을 찾을 수 없습니다: " + scanCode);
            }
            
            Order order = orderOpt.get();
            
            // 입고 가능 상태 확인
            if (order.getStatus() != Order.OrderStatus.RECEIVED) {
                return ScanResult.error("입고할 수 없는 상태입니다. 현재 상태: " + order.getStatus());
            }
            
            // 창고 위치 자동 배정
            String assignedLocation = location != null ? location : generateWarehouseLocation();
            
            // 주문 상태 업데이트
            Order.OrderStatus previousStatus = order.getStatus();
            order.setStatus(Order.OrderStatus.ARRIVED);
            order.setStorageLocation(assignedLocation);
            order.setStorageArea(generateStorageArea(assignedLocation));
            order.setArrivedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = order.getWarehouseNotes();
                order.setWarehouseNotes(existingNotes != null ? existingNotes + " / " + notes : notes);
            }
            
            orderRepository.save(order);
            
            // 주문 이력 기록
            recordOrderHistory(order, previousStatus, Order.OrderStatus.ARRIVED, 
                             "창고 입고 처리", operatorId, assignedLocation);
            
            log.info("Inbound completed: order={}, location={}", scanCode, assignedLocation);
            
            return ScanResult.success("입고 처리 완료", order, assignedLocation);
            
        } catch (Exception e) {
            log.error("Inbound processing error: {}", e.getMessage(), e);
            return ScanResult.error("입고 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 출고 처리
     */
    public ScanResult processOutbound(String scanCode, String trackingNumber, BigDecimal actualWeight, 
                                     String notes, String operatorId) {
        try {
            log.info("Processing outbound scan: code={}, tracking={}", scanCode, trackingNumber);
            
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(scanCode);
            if (orderOpt.isEmpty()) {
                return ScanResult.error("주문을 찾을 수 없습니다: " + scanCode);
            }
            
            Order order = orderOpt.get();
            
            // 출고 가능 상태 확인
            if (order.getStatus() != Order.OrderStatus.ARRIVED && order.getStatus() != Order.OrderStatus.REPACKING) {
                return ScanResult.error("출고할 수 없는 상태입니다. 현재 상태: " + order.getStatus());
            }
            
            // 주문 상태 업데이트
            Order.OrderStatus previousStatus = order.getStatus();
            order.setStatus(Order.OrderStatus.SHIPPING);
            order.setTrackingNumber(trackingNumber);
            order.setShippedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            if (actualWeight != null) {
                order.setActualWeight(actualWeight);
            }
            
            if (notes != null && !notes.trim().isEmpty()) {
                String existingNotes = order.getWarehouseNotes();
                order.setWarehouseNotes(existingNotes != null ? existingNotes + " / " + notes : notes);
            }
            
            orderRepository.save(order);
            
            // 주문 이력 기록
            recordOrderHistory(order, previousStatus, Order.OrderStatus.SHIPPING, 
                             "창고 출고 처리", operatorId, trackingNumber);
            
            log.info("Outbound completed: order={}, tracking={}", scanCode, trackingNumber);
            
            return ScanResult.success("출고 처리 완료", order, trackingNumber);
            
        } catch (Exception e) {
            log.error("Outbound processing error: {}", e.getMessage(), e);
            return ScanResult.error("출고 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 보류 처리 (문제가 있는 패키지)
     */
    public ScanResult processHold(String scanCode, String reason, String operatorId) {
        try {
            log.info("Processing hold scan: code={}, reason={}", scanCode, reason);
            
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(scanCode);
            if (orderOpt.isEmpty()) {
                return ScanResult.error("주문을 찾을 수 없습니다: " + scanCode);
            }
            
            Order order = orderOpt.get();
            Order.OrderStatus previousStatus = order.getStatus();
            
            // 보류 처리를 위해 창고 메모에 이유 추가
            String holdMessage = "[보류] " + reason + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String existingNotes = order.getWarehouseNotes();
            order.setWarehouseNotes(existingNotes != null ? existingNotes + " / " + holdMessage : holdMessage);
            order.setUpdatedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            
            // 주문 이력 기록
            recordOrderHistory(order, previousStatus, order.getStatus(), 
                             "패키지 보류 처리: " + reason, operatorId, null);
            
            log.info("Hold completed: order={}, reason={}", scanCode, reason);
            
            return ScanResult.success("보류 처리 완료", order, reason);
            
        } catch (Exception e) {
            log.error("Hold processing error: {}", e.getMessage(), e);
            return ScanResult.error("보류 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 믹스박스 처리 (여러 주문을 하나의 박스로)
     */
    public ScanResult processMixBox(List<String> scanCodes, String mixBoxId, String operatorId) {
        try {
            log.info("Processing mix box: codes={}, mixBoxId={}", scanCodes, mixBoxId);
            
            if (scanCodes.isEmpty()) {
                return ScanResult.error("스캔할 주문이 없습니다.");
            }
            
            StringBuilder result = new StringBuilder("믹스박스 처리 완료:\n");
            int processedCount = 0;
            
            for (String scanCode : scanCodes) {
                Optional<Order> orderOpt = orderRepository.findByOrderNumber(scanCode);
                if (orderOpt.isEmpty()) {
                    result.append("- ").append(scanCode).append(": 주문을 찾을 수 없음\n");
                    continue;
                }
                
                Order order = orderOpt.get();
                Order.OrderStatus previousStatus = order.getStatus();
                
                // 믹스박스 정보 추가
                String mixBoxMessage = "[믹스박스] " + mixBoxId + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String existingNotes = order.getWarehouseNotes();
                order.setWarehouseNotes(existingNotes != null ? existingNotes + " / " + mixBoxMessage : mixBoxMessage);
                order.setUpdatedAt(LocalDateTime.now());
                
                orderRepository.save(order);
                
                // 주문 이력 기록
                recordOrderHistory(order, previousStatus, order.getStatus(), 
                                 "믹스박스 처리: " + mixBoxId, operatorId, mixBoxId);
                
                result.append("- ").append(scanCode).append(": 처리 완료\n");
                processedCount++;
            }
            
            log.info("Mix box completed: processed={}/{}, mixBoxId={}", processedCount, scanCodes.size(), mixBoxId);
            
            return ScanResult.success(result.toString(), null, mixBoxId + " (" + processedCount + "개 주문)");
            
        } catch (Exception e) {
            log.error("Mix box processing error: {}", e.getMessage(), e);
            return ScanResult.error("믹스박스 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 일괄 입고 처리
     */
    public BatchResult processBatchInbound(List<String> scanCodes, String operatorId) {
        BatchResult batchResult = new BatchResult();
        
        for (String scanCode : scanCodes) {
            ScanResult result = processInbound(scanCode, "INBOUND", null, "일괄 입고", operatorId);
            if (result.isSuccess()) {
                batchResult.addSuccess(scanCode, result.getMessage());
            } else {
                batchResult.addFailure(scanCode, result.getMessage());
            }
        }
        
        log.info("Batch inbound completed: success={}, failure={}", 
                batchResult.getSuccessCount(), batchResult.getFailureCount());
        
        return batchResult;
    }
    
    /**
     * 일괄 출고 처리
     */
    public BatchResult processBatchOutbound(List<String> scanCodes, String operatorId) {
        BatchResult batchResult = new BatchResult();
        
        for (String scanCode : scanCodes) {
            // 트래킹 번호 자동 생성
            String trackingNumber = generateTrackingNumber();
            ScanResult result = processOutbound(scanCode, trackingNumber, null, "일괄 출고", operatorId);
            if (result.isSuccess()) {
                batchResult.addSuccess(scanCode, result.getMessage());
            } else {
                batchResult.addFailure(scanCode, result.getMessage());
            }
        }
        
        log.info("Batch outbound completed: success={}, failure={}", 
                batchResult.getSuccessCount(), batchResult.getFailureCount());
        
        return batchResult;
    }
    
    /**
     * 창고 현황 조회
     */
    public WarehouseStatus getWarehouseStatus() {
        List<Order> arrivedOrders = orderRepository.findByStatus(Order.OrderStatus.ARRIVED);
        List<Order> repackingOrders = orderRepository.findByStatus(Order.OrderStatus.REPACKING);
        List<Order> shippingOrders = orderRepository.findByStatus(Order.OrderStatus.SHIPPING);
        
        return WarehouseStatus.builder()
            .arrivedCount(arrivedOrders.size())
            .repackingCount(repackingOrders.size())
            .shippingCount(shippingOrders.size())
            .totalInWarehouse(arrivedOrders.size() + repackingOrders.size())
            .orders(arrivedOrders)
            .build();
    }
    
    // 유틸리티 메서드들
    private String generateWarehouseLocation() {
        // A-01-03 형태로 창고 위치 생성
        long locationCount = orderRepository.count() % 1000;
        char line = (char) ('A' + (locationCount / 100));
        int row = (int) ((locationCount % 100) / 10) + 1;
        int position = (int) (locationCount % 10) + 1;
        return String.format("%c-%02d-%02d", line, row, position);
    }
    
    private String generateStorageArea(String location) {
        if (location == null) return null;
        // A-01-03 -> "A line 1 row 3"
        String[] parts = location.split("-");
        if (parts.length != 3) return location;
        return String.format("%s line %d row %d", parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
    
    private String generateTrackingNumber() {
        // EE + 9자리 숫자 + KR
        long number = System.currentTimeMillis() % 1000000000L;
        return String.format("EE%09dKR", number);
    }
    
    private void recordOrderHistory(Order order, Order.OrderStatus previousStatus, Order.OrderStatus newStatus,
                                   String reason, String operatorId, String trackingNumber) {
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangeReason(reason);
        history.setChangedBy(operatorId != null ? operatorId : "system");
        history.setStorageLocation(order.getStorageLocation());
        history.setStorageArea(order.getStorageArea());
        history.setTrackingNumber(trackingNumber);
        history.setCreatedAt(LocalDateTime.now());
        
        orderHistoryRepository.save(history);
    }
    
    // 결과 DTO 클래스들
    public static class ScanResult {
        private boolean success;
        private String message;
        private Order order;
        private String additionalInfo;
        
        private ScanResult(boolean success, String message, Order order, String additionalInfo) {
            this.success = success;
            this.message = message;
            this.order = order;
            this.additionalInfo = additionalInfo;
        }
        
        public static ScanResult success(String message, Order order, String additionalInfo) {
            return new ScanResult(true, message, order, additionalInfo);
        }
        
        public static ScanResult error(String message) {
            return new ScanResult(false, message, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Order getOrder() { return order; }
        public String getAdditionalInfo() { return additionalInfo; }
    }
    
    public static class BatchResult {
        private int successCount = 0;
        private int failureCount = 0;
        private StringBuilder successDetails = new StringBuilder();
        private StringBuilder failureDetails = new StringBuilder();
        
        public void addSuccess(String code, String message) {
            successCount++;
            successDetails.append(code).append(": ").append(message).append("\n");
        }
        
        public void addFailure(String code, String message) {
            failureCount++;
            failureDetails.append(code).append(": ").append(message).append("\n");
        }
        
        // Getters
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public String getSuccessDetails() { return successDetails.toString(); }
        public String getFailureDetails() { return failureDetails.toString(); }
        public int getTotalCount() { return successCount + failureCount; }
    }
    
    public static class WarehouseStatus {
        private int arrivedCount;
        private int repackingCount;
        private int shippingCount;
        private int totalInWarehouse;
        private List<Order> orders;
        
        private WarehouseStatus(Builder builder) {
            this.arrivedCount = builder.arrivedCount;
            this.repackingCount = builder.repackingCount;
            this.shippingCount = builder.shippingCount;
            this.totalInWarehouse = builder.totalInWarehouse;
            this.orders = builder.orders;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int arrivedCount;
            private int repackingCount;
            private int shippingCount;
            private int totalInWarehouse;
            private List<Order> orders;
            
            public Builder arrivedCount(int arrivedCount) {
                this.arrivedCount = arrivedCount;
                return this;
            }
            
            public Builder repackingCount(int repackingCount) {
                this.repackingCount = repackingCount;
                return this;
            }
            
            public Builder shippingCount(int shippingCount) {
                this.shippingCount = shippingCount;
                return this;
            }
            
            public Builder totalInWarehouse(int totalInWarehouse) {
                this.totalInWarehouse = totalInWarehouse;
                return this;
            }
            
            public Builder orders(List<Order> orders) {
                this.orders = orders;
                return this;
            }
            
            public WarehouseStatus build() {
                return new WarehouseStatus(this);
            }
        }
        
        // Getters
        public int getArrivedCount() { return arrivedCount; }
        public int getRepackingCount() { return repackingCount; }
        public int getShippingCount() { return shippingCount; }
        public int getTotalInWarehouse() { return totalInWarehouse; }
        public List<Order> getOrders() { return orders; }
    }
}