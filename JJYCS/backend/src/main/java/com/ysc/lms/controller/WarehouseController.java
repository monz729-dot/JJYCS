package com.ysc.lms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    /**
     * 창고 스캔 - 입고
     */
    @PostMapping("/scan/inbound")
    public ResponseEntity<?> scanInbound(@RequestBody Map<String, Object> request) {
        try {
            String trackingNumber = (String) request.get("trackingNumber");
            String location = (String) request.get("location");
            
            log.info("Processing inbound scan for tracking: {}, location: {}", trackingNumber, location);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "입고 스캔이 완료되었습니다.");
            response.put("trackingNumber", trackingNumber);
            response.put("location", location);
            response.put("scanTime", LocalDateTime.now());
            response.put("scanType", "INBOUND");
            
            return ResponseEntity.ok(response);
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
    public ResponseEntity<?> scanOutbound(@RequestBody Map<String, Object> request) {
        try {
            String trackingNumber = (String) request.get("trackingNumber");
            String destination = (String) request.get("destination");
            
            log.info("Processing outbound scan for tracking: {}, destination: {}", trackingNumber, destination);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "출고 스캔이 완료되었습니다.");
            response.put("trackingNumber", trackingNumber);
            response.put("destination", destination);
            response.put("scanTime", LocalDateTime.now());
            response.put("scanType", "OUTBOUND");
            
            return ResponseEntity.ok(response);
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
    public ResponseEntity<?> scanItem(@RequestBody Map<String, Object> request) {
        try {
            String itemCode = (String) request.get("itemCode");
            String scanType = (String) request.get("scanType");
            
            log.info("Processing warehouse scan for item: {}, type: {}", itemCode, scanType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "스캔이 완료되었습니다.");
            response.put("itemCode", itemCode);
            response.put("scanType", scanType);
            response.put("scanTime", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
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
    public ResponseEntity<?> batchProcess(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> itemCodes = (List<String>) request.get("itemCodes");
            String processType = (String) request.get("processType");
            
            log.info("Processing batch operation for {} items, type: {}", itemCodes.size(), processType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", itemCodes.size() + "개 아이템의 일괄 처리가 완료되었습니다.");
            response.put("processedCount", itemCodes.size());
            response.put("processType", processType);
            response.put("processTime", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Batch processing error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "일괄 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 재고 조회
     */
    @GetMapping("/{warehouseId}/inventory")
    public ResponseEntity<?> getInventory(@PathVariable int warehouseId, 
                                        @RequestParam(required = false) String status) {
        try {
            log.info("Getting inventory for warehouse: {}, status: {}", warehouseId, status);
            
            // Mock inventory data
            List<Map<String, Object>> inventory = new ArrayList<>();
            
            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1);
            item1.put("orderNumber", "YCS-240115-001");
            item1.put("location", "A-01-03");
            item1.put("status", "ARRIVED");
            item1.put("arrivedAt", "2024-08-25T10:00:00");
            inventory.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", 2);
            item2.put("orderNumber", "YCS-240120-002");
            item2.put("location", "B-02-15");
            item2.put("status", "PROCESSING");
            item2.put("arrivedAt", "2024-08-26T14:30:00");
            inventory.add(item2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("inventory", inventory);
            response.put("warehouseId", warehouseId);
            response.put("totalCount", inventory.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get inventory error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "재고 조회 중 오류가 발생했습니다."));
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
}