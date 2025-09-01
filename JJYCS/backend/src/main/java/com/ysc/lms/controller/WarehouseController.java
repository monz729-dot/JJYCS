package com.ysc.lms.controller;

import com.ysc.lms.entity.ScanEvent;
import com.ysc.lms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class WarehouseController {
    
    private final WarehouseService warehouseService;

    /**
     * 창고 스캔 - 입고
     */
    @PostMapping("/scan/inbound")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> scanInbound(@RequestBody Map<String, Object> request, 
                                       Authentication authentication) {
        try {
            String scanCode = (String) request.get("scanCode");
            String location = (String) request.get("location");
            String scannedBy = authentication.getName();
            
            if (scanCode == null || scanCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "스캔 코드가 필요합니다."));
            }
            
            Map<String, Object> result = warehouseService.processInboundScan(scanCode, location, scannedBy);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Inbound scan error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "입고 스캔 중 오류가 발생했습니다."));
        }
    }

    /**
     * 창고 스캔 - 출고
     */
    @PostMapping("/scan/outbound")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> scanOutbound(@RequestBody Map<String, Object> request,
                                        Authentication authentication) {
        try {
            String scanCode = (String) request.get("scanCode");
            String destination = (String) request.get("destination");
            String scannedBy = authentication.getName();
            
            if (scanCode == null || scanCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "스캔 코드가 필요합니다."));
            }
            
            Map<String, Object> result = warehouseService.processOutboundScan(scanCode, destination, scannedBy);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Outbound scan error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "출고 스캔 중 오류가 발생했습니다."));
        }
    }

    /**
     * 일반 창고 스캔
     */
    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> scanItem(@RequestBody Map<String, Object> request,
                                    Authentication authentication) {
        try {
            String scanCode = (String) request.get("scanCode");
            String scanTypeStr = (String) request.get("scanType");
            String location = (String) request.get("location");
            String notes = (String) request.get("notes");
            String scannedBy = authentication.getName();
            
            if (scanCode == null || scanCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "스캔 코드가 필요합니다."));
            }
            
            if (scanTypeStr == null || scanTypeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "스캔 타입이 필요합니다."));
            }
            
            ScanEvent.ScanType scanType;
            try {
                scanType = ScanEvent.ScanType.valueOf(scanTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "유효하지 않은 스캔 타입입니다: " + scanTypeStr));
            }
            
            Map<String, Object> result = warehouseService.processWarehouseScan(
                scanCode, scanType, location, scannedBy, notes);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("Warehouse scan error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "창고 스캔 중 오류가 발생했습니다."));
        }
    }

    /**
     * 일괄 처리
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> batchProcess(@RequestBody Map<String, Object> request,
                                        Authentication authentication) {
        try {
            @SuppressWarnings("unchecked")
            List<String> itemCodes = (List<String>) request.get("itemCodes");
            String processType = (String) request.get("processType");
            String location = (String) request.get("location");
            String scannedBy = authentication.getName();
            
            if (itemCodes == null || itemCodes.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "처리할 아이템 코드가 필요합니다."));
            }
            
            if (processType == null || processType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "처리 타입이 필요합니다."));
            }
            
            log.info("Processing batch operation for {} items, type: {}", itemCodes.size(), processType);
            
            Map<String, Object> batchResult = warehouseService.processBatchOperation(
                itemCodes, processType, location, scannedBy
            );
            
            return ResponseEntity.ok(batchResult);
            
        } catch (Exception e) {
            log.error("Batch processing error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "일괄 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 재고 조회
     */
    @GetMapping("/{warehouseId}/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> getInventory(@PathVariable String warehouseId, 
                                        @RequestParam(required = false) String status) {
        try {
            log.info("Getting inventory for warehouse: {}, status: {}", warehouseId, status);
            
            Map<String, Object> inventoryData = warehouseService.getInventoryStatus(warehouseId, status);
            
            return ResponseEntity.ok(inventoryData);
            
        } catch (Exception e) {
            log.error("Get inventory error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "재고 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 창고 위치 조회
     */
    @GetMapping("/locations")
    public ResponseEntity<?> getLocations() {
        try {
            List<Map<String, String>> locations = new ArrayList<>();
            
            // Mock location data
            for (char section = 'A'; section <= 'E'; section++) {
                for (int row = 1; row <= 20; row++) {
                    for (int col = 1; col <= 10; col++) {
                        Map<String, String> location = new HashMap<>();
                        location.put("code", section + "-" + String.format("%02d", row) + "-" + String.format("%02d", col));
                        location.put("description", section + "구역 " + row + "행 " + col + "번");
                        location.put("section", String.valueOf(section));
                        locations.add(location);
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("locations", locations);
            response.put("totalCount", locations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get locations error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "위치 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 위치 할당
     */
    @PostMapping("/assign-location")
    public ResponseEntity<?> assignLocation(@RequestBody Map<String, String> request) {
        try {
            String orderNumber = request.get("orderNumber");
            String location = request.get("location");
            
            log.info("Assigning location {} to order {}", location, orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "위치가 성공적으로 할당되었습니다.");
            response.put("orderNumber", orderNumber);
            response.put("assignedLocation", location);
            response.put("assignedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Location assignment error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "위치 할당 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 스캔 히스토리 조회
     */
    @GetMapping("/scan-history/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> getScanHistory(@PathVariable String orderNumber) {
        try {
            List<ScanEvent> history = warehouseService.getScanHistory(orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderNumber", orderNumber);
            response.put("history", history);
            response.put("totalEvents", history.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Scan history error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "스캔 히스토리 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 창고 대시보드 통계
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            log.info("Getting warehouse dashboard statistics");
            
            Map<String, Object> stats = warehouseService.getWarehouseStats();
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Dashboard stats error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "대시보드 통계 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 처리 대기 목록
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<?> getPendingItems(@RequestParam(required = false) String type) {
        try {
            log.info("Getting pending items, type: {}", type);
            
            Map<String, Object> pendingData = warehouseService.getPendingItems(type);
            
            return ResponseEntity.ok(pendingData);
        } catch (Exception e) {
            log.error("Get pending items error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "대기 목록 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}