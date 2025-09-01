package com.ysc.lms.controller;

import com.ysc.lms.entity.Order;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.service.LabelService;
import com.ysc.lms.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final LabelService labelService;
    private final QrCodeService qrCodeService;

    /**
     * 주문 라벨 생성
     */
    @PostMapping("/order/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> generateOrderLabel(@PathVariable String orderNumber) {
        try {
            log.info("Generating label for order: {}", orderNumber);
            
            // 주문 정보 조회
            Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));
            
            log.info("Found order: {} with recipient: {}", orderNumber, order.getRecipientName());
            
            // 실제 라벨 서비스를 사용하여 라벨 생성
            LabelService.LabelGenerationResult labelResult = labelService.generateOrderLabel(
                orderNumber,
                order.getRecipientName(),
                order.getRecipientAddress(),
                order.getOrderType().toString()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderNumber", orderNumber);
            response.put("labelImage", labelResult.getLabelImageBase64());
            response.put("qrCode", labelResult.getQrCode());
            response.put("generatedAt", labelResult.getGeneratedAt());
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE') or hasRole('PARTNER')")
    public ResponseEntity<Map<String, Object>> generateOrderQR(@PathVariable String orderNumber) {
        try {
            log.info("Generating QR code for order: {}", orderNumber);
            
            // 실제 QR 코드 서비스 사용
            Map<String, Object> qrResult = qrCodeService.generateOrderQrCode(orderNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "QR 코드가 생성되었습니다.");
            response.put("orderNumber", orderNumber);
            response.putAll(qrResult);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("QR code generation error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "QR 코드 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 박스 라벨 생성
     */
    @PostMapping("/box/{boxId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> generateBoxLabel(
            @PathVariable String boxId,
            @RequestBody Map<String, Object> request) {
        try {
            log.info("Generating box label for: {}", boxId);
            
            String orderNumber = (String) request.get("orderNumber");
            Integer boxNumber = (Integer) request.getOrDefault("boxNumber", 1);
            Integer totalBoxes = (Integer) request.getOrDefault("totalBoxes", 1);
            
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "주문번호가 필요합니다."));
            }
            
            LabelService.LabelGenerationResult labelResult = labelService.generateBoxLabel(
                orderNumber, boxId, boxNumber, totalBoxes
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("boxId", boxId);
            response.put("orderNumber", orderNumber);
            response.put("labelImage", labelResult.getLabelImageBase64());
            response.put("qrCode", labelResult.getQrCode());
            response.put("generatedAt", labelResult.getGeneratedAt());
            response.put("message", "박스 라벨이 생성되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate box label for: {}", boxId, e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "박스 라벨 생성에 실패했습니다: " + e.getMessage()));
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