package com.ycs.lms.controller;

import com.ycs.lms.entity.Order;
import com.ycs.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class LabelController {

    private final OrderRepository orderRepository;

    /**
     * 주문 라벨 생성
     */
    @PostMapping("/order/{orderNumber}")
    public ResponseEntity<Map<String, Object>> generateOrderLabel(@PathVariable String orderNumber) {
        try {
            log.info("Generating label for order: {}", orderNumber);
            
            // 주문 정보 조회
            Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));
            
            log.info("Found order: {} with recipient: {}", orderNumber, order.getRecipientName());
            
            // 간단한 라벨 정보 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderNumber", orderNumber);
            response.put("labelUrl", "/api/labels/download/" + orderNumber + ".pdf");
            response.put("qrCode", "QR:" + orderNumber + ":" + System.currentTimeMillis());
            response.put("generatedAt", LocalDateTime.now());
            response.put("recipientName", order.getRecipientName());
            response.put("recipientAddress", order.getRecipientAddress());
            response.put("orderType", order.getOrderType().toString());
            response.put("message", "주문 라벨이 생성되었습니다.");
            
            log.info("Label generated successfully for order: {}", orderNumber);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate order label for: {}", orderNumber, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", "주문 라벨 생성에 실패했습니다: " + e.getMessage()
                ));
        }
    }

    /**
     * QR 코드 생성
     */
    @PostMapping("/generate/order/{orderNumber}")
    public ResponseEntity<Map<String, Object>> generateOrderQR(@PathVariable String orderNumber) {
        try {
            log.info("Generating QR code for order: {}", orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "QR 코드가 생성되었습니다.");
            response.put("orderNumber", orderNumber);
            response.put("qrCode", "QR:" + orderNumber + ":" + System.currentTimeMillis());
            response.put("qrUrl", "/api/labels/qr/" + orderNumber + ".png");
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("QR code generation error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "QR 코드 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 라벨 다운로드
     */
    @GetMapping("/download/{orderNumber}.pdf")
    public ResponseEntity<Map<String, Object>> downloadLabel(@PathVariable String orderNumber) {
        try {
            log.info("Label download requested for order: {}", orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "라벨 PDF가 준비되었습니다.");
            response.put("orderNumber", orderNumber);
            response.put("downloadUrl", "/api/labels/download/" + orderNumber + ".pdf");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to download label for order: {}", orderNumber, e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "라벨 다운로드에 실패했습니다."));
        }
    }

    /**
     * QR 코드 스캔 처리
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQRCode(@RequestBody Map<String, String> scanRequest) {
        try {
            String qrCode = scanRequest.get("qrCode");
            log.info("Processing QR code scan: {}", qrCode);
            
            if (qrCode == null || qrCode.isEmpty()) {
                throw new IllegalArgumentException("QR 코드가 제공되지 않았습니다.");
            }
            
            // QR 코드 파싱
            Map<String, Object> scanResult = parseQRCode(qrCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("qrCode", qrCode);
            response.put("scanResult", scanResult);
            response.put("scannedAt", System.currentTimeMillis());
            response.put("message", "QR 코드 스캔이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to scan QR code", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "error", "QR 코드 스캔에 실패했습니다: " + e.getMessage()
                ));
        }
    }

    /**
     * QR 코드 파싱 헬퍼 메서드
     */
    private Map<String, Object> parseQRCode(String qrCode) {
        Map<String, Object> result = new HashMap<>();
        
        if (qrCode.startsWith("QR:")) {
            String[] parts = qrCode.split(":");
            if (parts.length >= 3) {
                String identifier = parts[1];
                String timestamp = parts[2];
                
                result.put("type", identifier.contains("BOX") ? "box" : "order");
                result.put("identifier", identifier);
                result.put("timestamp", timestamp);
                
                if (identifier.startsWith("YCS-")) {
                    result.put("orderNumber", identifier);
                }
            }
        } else if (qrCode.startsWith("YCS-")) {
            result.put("type", "order");
            result.put("orderNumber", qrCode);
        } else {
            result.put("type", "unknown");
            result.put("rawData", qrCode);
        }
        
        return result;
    }
}