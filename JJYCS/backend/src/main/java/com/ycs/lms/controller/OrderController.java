package com.ycs.lms.controller;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.User;
import com.ycs.lms.service.BusinessLogicService;
import com.ycs.lms.service.OrderBusinessRuleService;
import com.ycs.lms.service.OrderService;
import com.ycs.lms.service.OrderService.CreateOrderRequest;
import com.ycs.lms.service.UserService;
import com.ycs.lms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    private final BusinessLogicService businessLogicService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // JWT에서 사용자 정보 추출
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                // 토큰이 없거나 유효하지 않은 경우 기본값 사용 (테스트용)
                log.warn("No valid auth token found, using default user ID for testing");
                userId = 1L;
            }
            request.setUserId(userId);
            
            log.info("Creating order for user: {}", request.getUserId());
            
            Order order = orderService.createOrder(request);
            
            // 비즈니스 규칙 경고 메시지 생성
            String warningMessage = buildWarningMessage(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "주문이 성공적으로 생성되었습니다.");
            response.put("order", order);
            response.put("orderNumber", order.getOrderNumber());
            response.put("status", order.getStatus().toString());
            response.put("shippingType", order.getShippingType().toString());
            response.put("totalCbm", order.getTotalCbm());
            response.put("warnings", warningMessage != null ? warningMessage : "");
            response.put("cbmExceeded", order.getTotalCbm().compareTo(new BigDecimal("29.0")) > 0);
            response.put("requiresExtraRecipient", order.getRequiresExtraRecipient());
            response.put("noMemberCode", order.getNoMemberCode());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Order creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Order creation error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "주문 생성 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "order", orderOpt.get()
        ));
    }
    
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<Order> orderOpt = orderService.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "order", orderOpt.get()
        ));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "orders", orders,
            "count", orders.size()
        ));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "orders", orders,
            "count", orders.size()
        ));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestBody StatusUpdateRequest request) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus(), request.getReason());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문 상태가 성공적으로 변경되었습니다.",
                "order", updatedOrder,
                "newStatus", updatedOrder.getStatus().toString()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Status update error for order {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "상태 변경 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateOrderData(@RequestBody ValidationRequest request) {
        try {
            // 기존 비즈니스 규칙 종합 검증 (호환성 유지)
            Map<String, Object> validation = businessLogicService.validateBusinessRules(
                request.getHsCode(),
                request.getEmsCode(),
                request.getWidth(),
                request.getHeight(), 
                request.getDepth(),
                request.getQuantity(),
                request.getTotalPrice(),
                request.getMemberCode()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "validation", validation
            ));
            
        } catch (Exception e) {
            log.error("Validation error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "검증 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/validate-business-rules")
    public ResponseEntity<Map<String, Object>> validateBusinessRules(@RequestBody CreateOrderRequest request) {
        try {
            // 새로운 비즈니스 룰 엔진 사용
            OrderBusinessRuleService.OrderBusinessRuleResult result = orderService.validateBusinessRules(request);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCbm", result.getTotalCbm());
            resultMap.put("cbmExceedsThreshold", result.isCbmExceedsThreshold());
            resultMap.put("totalThbValue", result.getTotalThbValue());
            resultMap.put("thbValueExceedsThreshold", result.isThbValueExceedsThreshold());
            resultMap.put("hasNoMemberCode", result.isHasNoMemberCode());
            resultMap.put("recommendedShippingMethod", result.getRecommendedShippingMethod());
            resultMap.put("requiresExtraRecipientInfo", result.isRequiresExtraRecipientInfo());
            resultMap.put("warnings", result.getWarnings());
            resultMap.put("errors", result.getErrors());
            resultMap.put("hasWarnings", result.hasWarnings());
            resultMap.put("hasErrors", result.hasErrors());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", resultMap
            ));
            
        } catch (Exception e) {
            log.error("Business rules validation error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "비즈니스 룰 검증 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/calculate-cbm")
    public ResponseEntity<Map<String, Object>> calculateCbm(@RequestBody CbmCalculationRequest request) {
        try {
            Map<String, Object> result = businessLogicService.calculateCbmAndCheck(
                request.getWidth(),
                request.getHeight(),
                request.getDepth(),
                request.getQuantity()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result
            ));
            
        } catch (Exception e) {
            log.error("CBM calculation error", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "CBM 계산 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/tracking/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderTracking(@PathVariable String orderNumber) {
        try {
            log.info("Getting order tracking for orderNumber: {}", orderNumber);
            
            Optional<Order> orderOpt = orderService.findByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "주문번호를 찾을 수 없습니다."
                ));
            }
            
            Order order = orderOpt.get();
            
            // 추적 정보 구성
            Map<String, Object> trackingData = Map.of(
                "orderNumber", order.getOrderNumber(),
                "recipientName", order.getRecipientName(),
                "recipientAddress", order.getRecipientAddress(),
                "createdAt", order.getCreatedAt(),
                "status", order.getStatus().toString(),
                "estimatedDelivery", order.getEstimatedDelivery(),
                "currentStage", getCurrentStage(order.getStatus()),
                "timeline", buildTimeline(order)
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trackingData
            ));
            
        } catch (Exception e) {
            log.error("Order tracking error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "추적 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/exchange-rates")
    public ResponseEntity<Map<String, Object>> getExchangeRates() {
        try {
            Map<String, BigDecimal> rates = businessLogicService.getExchangeRates();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "rates", rates,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Exchange rates error", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "환율 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
    private String buildWarningMessage(Order order) {
        StringBuilder warnings = new StringBuilder();
        
        if (order.getTotalCbm().compareTo(new BigDecimal("29.0")) > 0) {
            warnings.append("CBM ").append(order.getTotalCbm()).append(" m³가 임계값 29.0 m³를 초과하여 자동으로 항공 배송으로 전환되었습니다. ");
        }
        
        if (order.getRequiresExtraRecipient()) {
            warnings.append("총 금액이 1,500 THB를 초과하여 수취인 추가 정보가 필요합니다. ");
        }
        
        if (order.getNoMemberCode()) {
            warnings.append("회원코드가 미기재되어 발송이 지연될 수 있습니다. ");
        }
        
        return warnings.length() > 0 ? warnings.toString().trim() : null;
    }
    
    // 현재 단계 계산
    private int getCurrentStage(com.ycs.lms.entity.Order.OrderStatus status) {
        switch (status) {
            case PENDING: return 0;
            case PROCESSING: return 1; 
            case SHIPPED: return 2;
            case IN_TRANSIT: return 3;
            case DELIVERED: return 4;
            case CANCELLED: return -1;
            default: return 0;
        }
    }
    
    // 타임라인 구성
    private java.util.List<Map<String, Object>> buildTimeline(Order order) {
        java.util.List<Map<String, Object>> timeline = new java.util.ArrayList<>();
        
        // 기본 타임라인 구성 (실제로는 Order 엔티티의 이력을 기반으로 구성해야 함)
        timeline.add(Map.of(
            "eventName", "주문 접수",
            "eventTime", order.getCreatedAt(),
            "description", "주문이 정상적으로 접수되었습니다."
        ));
        
        if (order.getStatus() != com.ycs.lms.entity.Order.OrderStatus.PENDING) {
            timeline.add(Map.of(
                "eventName", "주문 처리",
                "eventTime", order.getCreatedAt(),
                "description", "주문 처리가 시작되었습니다."
            ));
        }
        
        if (order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.SHIPPED ||
            order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.IN_TRANSIT ||
            order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.DELIVERED) {
            timeline.add(Map.of(
                "eventName", "배송 시작",
                "eventTime", order.getCreatedAt(),
                "description", "배송이 시작되었습니다."
            ));
        }
        
        if (order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.IN_TRANSIT ||
            order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.DELIVERED) {
            timeline.add(Map.of(
                "eventName", "운송 중",
                "eventTime", order.getCreatedAt(),
                "description", "상품이 운송 중입니다."
            ));
        }
        
        if (order.getStatus() == com.ycs.lms.entity.Order.OrderStatus.DELIVERED) {
            timeline.add(Map.of(
                "eventName", "배송 완료",
                "eventTime", order.getCreatedAt(),
                "description", "배송이 완료되었습니다."
            ));
        }
        
        return timeline;
    }
    
    // JWT에서 사용자 ID 추출 헬퍼 메소드
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.getUsernameFromToken(token);
            
            if (!jwtUtil.validateToken(token, email)) {
                return null;
            }
            
            Optional<User> userOpt = userService.findByEmail(email);
            return userOpt.map(User::getId).orElse(null);
        } catch (Exception e) {
            log.error("Error extracting user from token", e);
            return null;
        }
    }
    
    // DTO 클래스들
    public static class ValidationRequest {
        private String hsCode;
        private String emsCode;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal depth;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String memberCode;
        
        // Getters and Setters
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public String getEmsCode() { return emsCode; }
        public void setEmsCode(String emsCode) { this.emsCode = emsCode; }
        public BigDecimal getWidth() { return width; }
        public void setWidth(BigDecimal width) { this.width = width; }
        public BigDecimal getHeight() { return height; }
        public void setHeight(BigDecimal height) { this.height = height; }
        public BigDecimal getDepth() { return depth; }
        public void setDepth(BigDecimal depth) { this.depth = depth; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    }
    
    public static class CbmCalculationRequest {
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal depth;
        private Integer quantity;
        
        // Getters and Setters
        public BigDecimal getWidth() { return width; }
        public void setWidth(BigDecimal width) { this.width = width; }
        public BigDecimal getHeight() { return height; }
        public void setHeight(BigDecimal height) { this.height = height; }
        public BigDecimal getDepth() { return depth; }
        public void setDepth(BigDecimal depth) { this.depth = depth; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
    
    public static class StatusUpdateRequest {
        private String status;
        private String reason;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}