package com.ysc.lms.controller;

import com.ysc.lms.service.EnhancedQrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * QR 코드 및 바코드 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class QrCodeController {
    
    private final EnhancedQrCodeService qrCodeService;
    
    /**
     * QR 코드 생성
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<?> generateQrCode(@RequestBody QrCodeRequest request) {
        try {
            log.info("Generating QR code: type={}, data={}", request.getType(), request.getData());
            
            Map<String, Object> result;
            
            switch (request.getType().toUpperCase()) {
                case "ORDER":
                    result = qrCodeService.generateOrderQrCode(request.getData());
                    break;
                case "BOX":
                    result = qrCodeService.generateBoxQrCode(request.getData(), request.getOrderNumber());
                    break;
                case "LABEL":
                    result = qrCodeService.generateLabelQrCode(request.getData(), request.getLabelText());
                    break;
                case "BARCODE":
                    result = qrCodeService.generateBarcode(request.getData());
                    break;
                default:
                    result = qrCodeService.generateQrCode(request.getData());
                    break;
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * QR 코드 스캔 (디코딩)
     */
    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<?> scanQrCode(@RequestBody ScanRequest request) {
        try {
            log.info("Scanning QR code");
            
            Map<String, String> decodedData = qrCodeService.scanQrCode(request.getImageData());
            
            // 유효성 검증
            boolean isValid = true;
            if (request.getExpectedType() != null) {
                String qrData = String.join("|", 
                    decodedData.entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .toArray(String[]::new));
                isValid = qrCodeService.validateQrCode(qrData, request.getExpectedType());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", decodedData,
                "valid", isValid,
                "scannedAt", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Failed to scan QR code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 주문 QR 코드 생성
     */
    @GetMapping("/order/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'CORPORATE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<?> generateOrderQrCode(@PathVariable String orderNumber) {
        try {
            Map<String, Object> result = qrCodeService.generateOrderQrCode(orderNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to generate order QR code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 박스 QR 코드 생성
     */
    @GetMapping("/box/{boxId}/order/{orderNumber}")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<?> generateBoxQrCode(
            @PathVariable String boxId,
            @PathVariable String orderNumber) {
        try {
            Map<String, Object> result = qrCodeService.generateBoxQrCode(boxId, orderNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to generate box QR code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * QR 코드 생성 요청 DTO
     */
    public static class QrCodeRequest {
        private String type;  // ORDER, BOX, LABEL, BARCODE, CUSTOM
        private String data;
        private String orderNumber;  // BOX 타입일 때 사용
        private String labelText;    // LABEL 타입일 때 사용
        
        // Getters and Setters
        public String getType() {
            return type != null ? type : "CUSTOM";
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getData() {
            return data;
        }
        
        public void setData(String data) {
            this.data = data;
        }
        
        public String getOrderNumber() {
            return orderNumber;
        }
        
        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }
        
        public String getLabelText() {
            return labelText;
        }
        
        public void setLabelText(String labelText) {
            this.labelText = labelText;
        }
    }
    
    /**
     * QR 코드 스캔 요청 DTO
     */
    public static class ScanRequest {
        private String imageData;      // Base64 인코딩된 이미지 데이터
        private String expectedType;   // 예상되는 QR 코드 타입 (ORDER, BOX 등)
        
        // Getters and Setters
        public String getImageData() {
            return imageData;
        }
        
        public void setImageData(String imageData) {
            this.imageData = imageData;
        }
        
        public String getExpectedType() {
            return expectedType;
        }
        
        public void setExpectedType(String expectedType) {
            this.expectedType = expectedType;
        }
    }
}