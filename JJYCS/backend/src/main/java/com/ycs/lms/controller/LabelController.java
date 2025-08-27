package com.ycs.lms.controller;

import com.ycs.lms.service.LabelService;
import com.ycs.lms.service.LabelService.*;
import com.ycs.lms.service.LabelGenerationService;
import com.ycs.lms.dto.label.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
@Slf4j
public class LabelController {
    
    private final LabelService labelService;
    private final LabelGenerationService labelGenerationService;
    
    @GetMapping("/{orderNumber}/qr")
    public ResponseEntity<Map<String, Object>> generateOrderQR(@PathVariable String orderNumber) {
        try {
            log.info("Generating QR code for order: {}", orderNumber);
            
            QRCodeData qrData = labelService.generateOrderQR(orderNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orderNumber", qrData.getOrderNumber(),
                "qrString", qrData.getQrString(),
                "qrCodeUrl", qrData.getQrCodeUrl(),
                "data", qrData.getData()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("QR code generation error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "QR 코드 생성 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/{orderNumber}/shipping")
    public ResponseEntity<Map<String, Object>> generateShippingLabel(@PathVariable String orderNumber) {
        try {
            log.info("Generating shipping label for order: {}", orderNumber);
            
            ShippingLabel label = labelService.generateShippingLabel(orderNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "label", label
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Shipping label generation error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "배송 라벨 생성 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/{orderNumber}/pickup")
    public ResponseEntity<Map<String, Object>> generatePickupLabel(@PathVariable String orderNumber) {
        try {
            log.info("Generating pickup label for order: {}", orderNumber);
            
            PickupLabel label = labelService.generatePickupLabel(orderNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "label", label
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Pickup label generation error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "픽업 라벨 생성 중 오류가 발생했습니다."
            ));
        }
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> generateBatchLabels(@RequestBody BatchLabelRequest request) {
        try {
            log.info("Generating batch labels for {} orders", request.getOrderNumbers().size());
            
            List<ShippingLabel> labels = labelService.generateBatchLabels(request.getOrderNumbers());
            
            // 성공/실패 카운트
            long successCount = labels.stream().filter(label -> label.getError() == null).count();
            long failureCount = labels.size() - successCount;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("일괄 라벨 생성 완료: 성공 %d개, 실패 %d개", successCount, failureCount),
                "labels", labels,
                "totalCount", labels.size(),
                "successCount", successCount,
                "failureCount", failureCount
            ));
            
        } catch (Exception e) {
            log.error("Batch label generation error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "일괄 라벨 생성 중 오류가 발생했습니다."
            ));
        }
    }
    
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> parseScanResult(@RequestBody ScanRequest request) {
        try {
            log.info("Parsing scan data: {}", request.getScanData().substring(0, Math.min(50, request.getScanData().length())));
            
            ScanResult result = labelService.parseScanResult(request.getScanData());
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result.getMessage(),
                    "orderNumber", result.getOrderNumber(),
                    "data", result.getData()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Scan parsing error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "스캔 데이터 해석 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/test-qr/{orderNumber}")
    public ResponseEntity<Map<String, Object>> testQRGeneration(@PathVariable String orderNumber) {
        try {
            // 테스트용 QR 코드 생성 및 파싱
            QRCodeData qrData = labelService.generateOrderQR(orderNumber);
            ScanResult scanResult = labelService.parseScanResult(qrData.getQrString());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "QR 코드 생성 및 파싱 테스트 완료",
                "original", qrData,
                "parsed", scanResult
            ));
            
        } catch (Exception e) {
            log.error("QR test error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "QR 코드 테스트 중 오류가 발생했습니다."
            ));
        }
    }
    
    // DTO 클래스들
    public static class BatchLabelRequest {
        private List<String> orderNumbers;
        
        public List<String> getOrderNumbers() {
            return orderNumbers;
        }
        
        public void setOrderNumbers(List<String> orderNumbers) {
            this.orderNumbers = orderNumbers;
        }
    }
    
    public static class ScanRequest {
        private String scanData;
        
        public String getScanData() {
            return scanData;
        }
        
        public void setScanData(String scanData) {
            this.scanData = scanData;
        }
    }
    
    // ===== 새로운 라벨 생성 시스템 API =====
    
    /**
     * 주문용 QR 코드 생성 (이미지 파일)
     */
    @PostMapping("/api/qr/order")
    public ResponseEntity<byte[]> generateOrderQRImage(@RequestBody OrderQRCodeRequest request) {
        try {
            log.info("주문 QR 코드 이미지 생성 요청: orderId={}, orderCode={}", request.getOrderId(), request.getOrderCode());
            
            QRCodeResult result = labelGenerationService.generateOrderQRCode(request.getOrderId(), request.getOrderCode());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("qr-order-%s.png", request.getOrderCode()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getQrCodeBytes());
                
        } catch (Exception e) {
            log.error("주문 QR 코드 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 재고용 QR 코드 생성 (이미지 파일)
     */
    @PostMapping("/api/qr/inventory")
    public ResponseEntity<byte[]> generateInventoryQRImage(@RequestBody InventoryQRCodeRequest request) {
        try {
            log.info("재고 QR 코드 생성 요청: inventoryCode={}", request.getInventoryCode());
            
            QRCodeResult result = labelGenerationService.generateInventoryQRCode(request.getInventoryCode());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("qr-inventory-%s.png", request.getInventoryCode()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getQrCodeBytes());
                
        } catch (Exception e) {
            log.error("재고 QR 코드 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 배송 라벨 생성 (이미지 파일)
     */
    @PostMapping("/api/shipping/label")
    public ResponseEntity<byte[]> generateShippingLabelImage(@RequestBody ShippingLabelRequest request) {
        try {
            log.info("배송 라벨 생성 요청: orderId={}, trackingNumber={}", request.getOrderId(), request.getTrackingNumber());
            
            ShippingLabelResult result = labelGenerationService.generateShippingLabel(request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("shipping-label-%s.png", request.getTrackingNumber()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getLabelImageBytes());
                
        } catch (Exception e) {
            log.error("배송 라벨 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 창고 입고 라벨 생성
     */
    @PostMapping("/api/warehouse/inbound")
    public ResponseEntity<byte[]> generateWarehouseInboundLabelImage(@RequestBody WarehouseInboundLabelRequest request) {
        try {
            log.info("창고 입고 라벨 생성: orderId={}, location={}", request.getOrderId(), request.getLocation());
            
            LabelTemplateRequest templateRequest = new LabelTemplateRequest();
            templateRequest.setOrderId(request.getOrderId());
            templateRequest.setOrderCode(request.getOrderCode());
            templateRequest.setWarehouseName(request.getWarehouseName());
            templateRequest.setLocation(request.getLocation());
            templateRequest.setTemplateType(LabelTemplateType.WAREHOUSE_INBOUND);
            
            LabelTemplateResult result = labelGenerationService.generateLabelTemplate(templateRequest);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("inbound-label-%s.png", request.getOrderCode()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getLabelBytes());
                
        } catch (Exception e) {
            log.error("창고 입고 라벨 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 창고 출고 라벨 생성
     */
    @PostMapping("/api/warehouse/outbound")
    public ResponseEntity<byte[]> generateWarehouseOutboundLabelImage(@RequestBody WarehouseOutboundLabelRequest request) {
        try {
            log.info("창고 출고 라벨 생성: orderId={}, trackingNumber={}", request.getOrderId(), request.getTrackingNumber());
            
            LabelTemplateRequest templateRequest = new LabelTemplateRequest();
            templateRequest.setOrderId(request.getOrderId());
            templateRequest.setOrderCode(request.getOrderCode());
            templateRequest.setTrackingNumber(request.getTrackingNumber());
            templateRequest.setTemplateType(LabelTemplateType.WAREHOUSE_OUTBOUND);
            
            LabelTemplateResult result = labelGenerationService.generateLabelTemplate(templateRequest);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("outbound-label-%s.png", request.getOrderCode()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getLabelBytes());
                
        } catch (Exception e) {
            log.error("창고 출고 라벨 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 재고 태그 라벨 생성
     */
    @PostMapping("/api/inventory/tag")
    public ResponseEntity<byte[]> generateInventoryTagLabelImage(@RequestBody InventoryTagLabelRequest request) {
        try {
            log.info("재고 태그 라벨 생성: inventoryCode={}", request.getInventoryCode());
            
            LabelTemplateRequest templateRequest = new LabelTemplateRequest();
            templateRequest.setInventoryCode(request.getInventoryCode());
            templateRequest.setTemplateType(LabelTemplateType.INVENTORY_TAG);
            
            LabelTemplateResult result = labelGenerationService.generateLabelTemplate(templateRequest);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("inventory-tag-%s.png", request.getInventoryCode()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getLabelBytes());
                
        } catch (Exception e) {
            log.error("재고 태그 라벨 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 바코드 생성
     */
    @PostMapping("/api/barcode")
    public ResponseEntity<byte[]> generateBarcodeImage(@RequestBody BarcodeRequest request) {
        try {
            log.info("바코드 생성 요청: data={}, type={}", request.getData(), request.getBarcodeType());
            
            BarcodeResult result = labelGenerationService.generateBarcode(request.getData(), request.getBarcodeType());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                String.format("barcode-%s.png", request.getData().replaceAll("[^a-zA-Z0-9]", "_")));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(result.getBarcodeBytes());
                
        } catch (Exception e) {
            log.error("바코드 생성 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 새로운 Request DTO 클래스들
    
    public static class OrderQRCodeRequest {
        private Long orderId;
        private String orderCode;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    }
    
    public static class InventoryQRCodeRequest {
        private String inventoryCode;
        
        // Getters and Setters
        public String getInventoryCode() { return inventoryCode; }
        public void setInventoryCode(String inventoryCode) { this.inventoryCode = inventoryCode; }
    }
    
    public static class BarcodeRequest {
        private String data;
        private String barcodeType = "CODE128";
        
        // Getters and Setters
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public String getBarcodeType() { return barcodeType; }
        public void setBarcodeType(String barcodeType) { this.barcodeType = barcodeType; }
    }
    
    public static class WarehouseInboundLabelRequest {
        private Long orderId;
        private String orderCode;
        private String warehouseName;
        private String location;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    
    public static class WarehouseOutboundLabelRequest {
        private Long orderId;
        private String orderCode;
        private String trackingNumber;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }
    
    public static class InventoryTagLabelRequest {
        private String inventoryCode;
        
        // Getters and Setters
        public String getInventoryCode() { return inventoryCode; }
        public void setInventoryCode(String inventoryCode) { this.inventoryCode = inventoryCode; }
    }
}