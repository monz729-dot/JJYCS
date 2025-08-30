package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 주문용 QR 코드 데이터 생성
     */
    public QRCodeData generateOrderQR(String orderNumber) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderNumber);
        }
        
        Order order = orderOpt.get();
        
        // QR 코드에 포함할 데이터
        Map<String, Object> qrData = new LinkedHashMap<>();
        qrData.put("orderNumber", order.getOrderNumber());
        qrData.put("recipientName", order.getRecipientName());
        qrData.put("country", order.getCountry());
        qrData.put("shippingType", order.getShippingType().toString());
        qrData.put("totalWeight", order.getTotalWeight());
        qrData.put("totalCbm", order.getTotalCbm());
        qrData.put("status", order.getStatus().toString());
        qrData.put("created", order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        if (order.getStorageLocation() != null) {
            qrData.put("location", order.getStorageLocation());
        }
        
        if (order.getTrackingNumber() != null) {
            qrData.put("tracking", order.getTrackingNumber());
        }
        
        // QR 코드 문자열 생성 (JSON 형태)
        StringBuilder qrString = new StringBuilder();
        qrString.append("YCS-ORDER:");
        for (Map.Entry<String, Object> entry : qrData.entrySet()) {
            qrString.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        
        return new QRCodeData(
            order.getOrderNumber(),
            qrString.toString(),
            qrData,
            generateQRCodeUrl(qrString.toString())
        );
    }
    
    /**
     * 배송 라벨 정보 생성
     */
    public ShippingLabel generateShippingLabel(String orderNumber) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderNumber);
        }
        
        Order order = orderOpt.get();
        
        return ShippingLabel.builder()
            .orderNumber(order.getOrderNumber())
            .senderInfo(getSenderInfo())
            .recipientName(order.getRecipientName())
            .recipientPhone(order.getRecipientPhone())
            .recipientAddress(order.getRecipientAddress())
            .recipientPostalCode(order.getRecipientPostalCode())
            .country(order.getCountry())
            .shippingType(order.getShippingType().toString())
            .trackingNumber(order.getTrackingNumber())
            .weight(order.getTotalWeight())
            .cbm(order.getTotalCbm())
            .specialRequests(order.getSpecialRequests())
            .qrCode(generateOrderQR(orderNumber).getQrCodeUrl())
            .createdDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .build();
    }
    
    /**
     * 창고용 픽업 라벨 생성 (창고 직원용)
     */
    public PickupLabel generatePickupLabel(String orderNumber) {
        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderNumber);
        }
        
        Order order = orderOpt.get();
        
        return PickupLabel.builder()
            .orderNumber(order.getOrderNumber())
            .userName(order.getUser().getName())
            .userPhone(order.getUser().getPhone())
            .memberCode(order.getUser().getMemberCode())
            .storageLocation(order.getStorageLocation())
            .storageArea(order.getStorageArea())
            .weight(order.getTotalWeight())
            .cbm(order.getTotalCbm())
            .itemCount(order.getItems().size())
            .status(order.getStatus().toString())
            .arrivedDate(order.getArrivedAt() != null ? 
                        order.getArrivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .warehouseNotes(order.getWarehouseNotes())
            .qrCode(generateOrderQR(orderNumber).getQrCodeUrl())
            .build();
    }
    
    /**
     * 여러 주문의 라벨을 한번에 생성 (일괄 처리용)
     */
    public List<ShippingLabel> generateBatchLabels(List<String> orderNumbers) {
        List<ShippingLabel> labels = new ArrayList<>();
        
        for (String orderNumber : orderNumbers) {
            try {
                ShippingLabel label = generateShippingLabel(orderNumber);
                labels.add(label);
            } catch (Exception e) {
                log.error("Failed to generate label for order: {}", orderNumber, e);
                // 실패한 라벨은 에러 정보와 함께 추가
                labels.add(ShippingLabel.builder()
                    .orderNumber(orderNumber)
                    .error("라벨 생성 실패: " + e.getMessage())
                    .build());
            }
        }
        
        return labels;
    }
    
    /**
     * QR 코드 스캔 결과 파싱
     */
    public ScanResult parseScanResult(String scanData) {
        try {
            if (!scanData.startsWith("YCS-ORDER:")) {
                return ScanResult.error("유효하지 않은 QR 코드입니다.");
            }
            
            String data = scanData.substring("YCS-ORDER:".length());
            Map<String, String> parsedData = new HashMap<>();
            
            String[] pairs = data.split(";");
            for (String pair : pairs) {
                if (pair.contains("=")) {
                    String[] keyValue = pair.split("=", 2);
                    parsedData.put(keyValue[0], keyValue[1]);
                }
            }
            
            String orderNumber = parsedData.get("orderNumber");
            if (orderNumber == null) {
                return ScanResult.error("주문번호를 찾을 수 없습니다.");
            }
            
            // 주문 존재 확인
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                return ScanResult.error("주문을 찾을 수 없습니다: " + orderNumber);
            }
            
            return ScanResult.success(orderNumber, parsedData);
            
        } catch (Exception e) {
            log.error("Scan parsing error", e);
            return ScanResult.error("QR 코드 해석 중 오류가 발생했습니다.");
        }
    }
    
    // 유틸리티 메서드들
    private String generateQRCodeUrl(String data) {
        // 실제로는 QR 코드 생성 라이브러리를 사용하여 이미지 URL 생성
        // 여기서는 Mock URL 반환
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes());
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedData;
    }
    
    private SenderInfo getSenderInfo() {
        return SenderInfo.builder()
            .companyName("YCS Logistics Co., Ltd.")
            .address("123 Logistics Street, Seoul, Korea")
            .phone("+82-2-1234-5678")
            .email("info@ycs.com")
            .build();
    }
    
    // DTO 클래스들
    public static class QRCodeData {
        private String orderNumber;
        private String qrString;
        private Map<String, Object> data;
        private String qrCodeUrl;
        
        public QRCodeData(String orderNumber, String qrString, Map<String, Object> data, String qrCodeUrl) {
            this.orderNumber = orderNumber;
            this.qrString = qrString;
            this.data = data;
            this.qrCodeUrl = qrCodeUrl;
        }
        
        // Getters
        public String getOrderNumber() { return orderNumber; }
        public String getQrString() { return qrString; }
        public Map<String, Object> getData() { return data; }
        public String getQrCodeUrl() { return qrCodeUrl; }
    }
    
    public static class ShippingLabel {
        private String orderNumber;
        private SenderInfo senderInfo;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private String recipientPostalCode;
        private String country;
        private String shippingType;
        private String trackingNumber;
        private Object weight;
        private Object cbm;
        private String specialRequests;
        private String qrCode;
        private String createdDate;
        private String error;
        
        private ShippingLabel(Builder builder) {
            this.orderNumber = builder.orderNumber;
            this.senderInfo = builder.senderInfo;
            this.recipientName = builder.recipientName;
            this.recipientPhone = builder.recipientPhone;
            this.recipientAddress = builder.recipientAddress;
            this.recipientPostalCode = builder.recipientPostalCode;
            this.country = builder.country;
            this.shippingType = builder.shippingType;
            this.trackingNumber = builder.trackingNumber;
            this.weight = builder.weight;
            this.cbm = builder.cbm;
            this.specialRequests = builder.specialRequests;
            this.qrCode = builder.qrCode;
            this.createdDate = builder.createdDate;
            this.error = builder.error;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String orderNumber;
            private SenderInfo senderInfo;
            private String recipientName;
            private String recipientPhone;
            private String recipientAddress;
            private String recipientPostalCode;
            private String country;
            private String shippingType;
            private String trackingNumber;
            private Object weight;
            private Object cbm;
            private String specialRequests;
            private String qrCode;
            private String createdDate;
            private String error;
            
            public Builder orderNumber(String orderNumber) {
                this.orderNumber = orderNumber;
                return this;
            }
            
            public Builder senderInfo(SenderInfo senderInfo) {
                this.senderInfo = senderInfo;
                return this;
            }
            
            public Builder recipientName(String recipientName) {
                this.recipientName = recipientName;
                return this;
            }
            
            public Builder recipientPhone(String recipientPhone) {
                this.recipientPhone = recipientPhone;
                return this;
            }
            
            public Builder recipientAddress(String recipientAddress) {
                this.recipientAddress = recipientAddress;
                return this;
            }
            
            public Builder recipientPostalCode(String recipientPostalCode) {
                this.recipientPostalCode = recipientPostalCode;
                return this;
            }
            
            public Builder country(String country) {
                this.country = country;
                return this;
            }
            
            public Builder shippingType(String shippingType) {
                this.shippingType = shippingType;
                return this;
            }
            
            public Builder trackingNumber(String trackingNumber) {
                this.trackingNumber = trackingNumber;
                return this;
            }
            
            public Builder weight(Object weight) {
                this.weight = weight;
                return this;
            }
            
            public Builder cbm(Object cbm) {
                this.cbm = cbm;
                return this;
            }
            
            public Builder specialRequests(String specialRequests) {
                this.specialRequests = specialRequests;
                return this;
            }
            
            public Builder qrCode(String qrCode) {
                this.qrCode = qrCode;
                return this;
            }
            
            public Builder createdDate(String createdDate) {
                this.createdDate = createdDate;
                return this;
            }
            
            public Builder error(String error) {
                this.error = error;
                return this;
            }
            
            public ShippingLabel build() {
                return new ShippingLabel(this);
            }
        }
        
        // Getters
        public String getOrderNumber() { return orderNumber; }
        public SenderInfo getSenderInfo() { return senderInfo; }
        public String getRecipientName() { return recipientName; }
        public String getRecipientPhone() { return recipientPhone; }
        public String getRecipientAddress() { return recipientAddress; }
        public String getRecipientPostalCode() { return recipientPostalCode; }
        public String getCountry() { return country; }
        public String getShippingType() { return shippingType; }
        public String getTrackingNumber() { return trackingNumber; }
        public Object getWeight() { return weight; }
        public Object getCbm() { return cbm; }
        public String getSpecialRequests() { return specialRequests; }
        public String getQrCode() { return qrCode; }
        public String getCreatedDate() { return createdDate; }
        public String getError() { return error; }
    }
    
    public static class PickupLabel {
        private String orderNumber;
        private String userName;
        private String userPhone;
        private String memberCode;
        private String storageLocation;
        private String storageArea;
        private Object weight;
        private Object cbm;
        private int itemCount;
        private String status;
        private String arrivedDate;
        private String warehouseNotes;
        private String qrCode;
        
        private PickupLabel(Builder builder) {
            this.orderNumber = builder.orderNumber;
            this.userName = builder.userName;
            this.userPhone = builder.userPhone;
            this.memberCode = builder.memberCode;
            this.storageLocation = builder.storageLocation;
            this.storageArea = builder.storageArea;
            this.weight = builder.weight;
            this.cbm = builder.cbm;
            this.itemCount = builder.itemCount;
            this.status = builder.status;
            this.arrivedDate = builder.arrivedDate;
            this.warehouseNotes = builder.warehouseNotes;
            this.qrCode = builder.qrCode;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String orderNumber;
            private String userName;
            private String userPhone;
            private String memberCode;
            private String storageLocation;
            private String storageArea;
            private Object weight;
            private Object cbm;
            private int itemCount;
            private String status;
            private String arrivedDate;
            private String warehouseNotes;
            private String qrCode;
            
            public Builder orderNumber(String orderNumber) {
                this.orderNumber = orderNumber;
                return this;
            }
            
            public Builder userName(String userName) {
                this.userName = userName;
                return this;
            }
            
            public Builder userPhone(String userPhone) {
                this.userPhone = userPhone;
                return this;
            }
            
            public Builder memberCode(String memberCode) {
                this.memberCode = memberCode;
                return this;
            }
            
            public Builder storageLocation(String storageLocation) {
                this.storageLocation = storageLocation;
                return this;
            }
            
            public Builder storageArea(String storageArea) {
                this.storageArea = storageArea;
                return this;
            }
            
            public Builder weight(Object weight) {
                this.weight = weight;
                return this;
            }
            
            public Builder cbm(Object cbm) {
                this.cbm = cbm;
                return this;
            }
            
            public Builder itemCount(int itemCount) {
                this.itemCount = itemCount;
                return this;
            }
            
            public Builder status(String status) {
                this.status = status;
                return this;
            }
            
            public Builder arrivedDate(String arrivedDate) {
                this.arrivedDate = arrivedDate;
                return this;
            }
            
            public Builder warehouseNotes(String warehouseNotes) {
                this.warehouseNotes = warehouseNotes;
                return this;
            }
            
            public Builder qrCode(String qrCode) {
                this.qrCode = qrCode;
                return this;
            }
            
            public PickupLabel build() {
                return new PickupLabel(this);
            }
        }
        
        // Getters
        public String getOrderNumber() { return orderNumber; }
        public String getUserName() { return userName; }
        public String getUserPhone() { return userPhone; }
        public String getMemberCode() { return memberCode; }
        public String getStorageLocation() { return storageLocation; }
        public String getStorageArea() { return storageArea; }
        public Object getWeight() { return weight; }
        public Object getCbm() { return cbm; }
        public int getItemCount() { return itemCount; }
        public String getStatus() { return status; }
        public String getArrivedDate() { return arrivedDate; }
        public String getWarehouseNotes() { return warehouseNotes; }
        public String getQrCode() { return qrCode; }
    }
    
    public static class SenderInfo {
        private String companyName;
        private String address;
        private String phone;
        private String email;
        
        private SenderInfo(Builder builder) {
            this.companyName = builder.companyName;
            this.address = builder.address;
            this.phone = builder.phone;
            this.email = builder.email;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String companyName;
            private String address;
            private String phone;
            private String email;
            
            public Builder companyName(String companyName) {
                this.companyName = companyName;
                return this;
            }
            
            public Builder address(String address) {
                this.address = address;
                return this;
            }
            
            public Builder phone(String phone) {
                this.phone = phone;
                return this;
            }
            
            public Builder email(String email) {
                this.email = email;
                return this;
            }
            
            public SenderInfo build() {
                return new SenderInfo(this);
            }
        }
        
        // Getters
        public String getCompanyName() { return companyName; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }
    
    public static class ScanResult {
        private boolean success;
        private String message;
        private String orderNumber;
        private Map<String, String> data;
        
        private ScanResult(boolean success, String message, String orderNumber, Map<String, String> data) {
            this.success = success;
            this.message = message;
            this.orderNumber = orderNumber;
            this.data = data;
        }
        
        public static ScanResult success(String orderNumber, Map<String, String> data) {
            return new ScanResult(true, "스캔 성공", orderNumber, data);
        }
        
        public static ScanResult error(String message) {
            return new ScanResult(false, message, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getOrderNumber() { return orderNumber; }
        public Map<String, String> getData() { return data; }
    }
}