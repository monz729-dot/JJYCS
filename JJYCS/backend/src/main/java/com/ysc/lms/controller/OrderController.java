package com.ysc.lms.controller;

import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.User;
import com.ysc.lms.exception.BusinessException;
import com.ysc.lms.exception.ResourceNotFoundException;
import com.ysc.lms.service.BusinessLogicService;
import com.ysc.lms.service.OrderBusinessRuleService;
import com.ysc.lms.service.OrderService;
import com.ysc.lms.service.OrderService.CreateOrderRequest;
import com.ysc.lms.service.UserService;
import com.ysc.lms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TEMPORARILY DISABLED - conflicts with SimpleOrderController
// @RestController
@RequestMapping("/api/orders")
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
            log.info("Received order creation request from user with auth header present: {}", authHeader != null);
            
            // JWT에서 사용자 정보 추출
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                throw new BusinessException("AUTH_REQUIRED", "인증이 필요합니다.");
            }
            
            // 요청 데이터 설정
            request.setUserId(userId);
            
            log.info("Creating order for user: {} with {} items", request.getUserId(),
                request.getOrderItems() != null ? request.getOrderItems().size() : 0);
            
            // 주문 생성
            Order order = orderService.createOrder(request);
            
            if (order == null) {
                log.error("Order creation returned null for user: {}", userId);
                return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "주문 생성에 실패했습니다."));
            }
            
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
            
            log.info("Order created successfully: {} for user: {}", order.getOrderNumber(), userId);
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            log.error("Business error during order creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during order creation for user {}: {}", 
                request != null ? request.getUserId() : "unknown", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "주문 생성 중 오류가 발생했습니다. 다시 시도해주세요."));
        }
    }
    
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            Order order = orderService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
            
            // 주문 접근 권한 확인
            if (!orderService.isOrderAccessible(order, currentUser)) {
                throw new BusinessException("ACCESS_DENIED", "해당 주문에 접근할 권한이 없습니다.");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting order {}", id, e);
            throw new BusinessException("INTERNAL_ERROR", "주문 조회 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(
            @PathVariable String orderNumber,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            Order order = orderService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("주문번호 '" + orderNumber + "'을 찾을 수 없습니다."));
            
            // 주문 접근 권한 확인
            if (!orderService.isOrderAccessible(order, currentUser)) {
                throw new BusinessException("ACCESS_DENIED", "해당 주문에 접근할 권한이 없습니다.");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting order by number {}", orderNumber, e);
            throw new BusinessException("INTERNAL_ERROR", "주문 조회 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'WAREHOUSE', 'PARTNER')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            List<Order> orders = getOrdersBasedOnUserRole(currentUser);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting all orders", e);
            throw new BusinessException("INTERNAL_ERROR", "주문 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'WAREHOUSE', 'PARTNER')")
    public ResponseEntity<Map<String, Object>> getMyOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            User currentUser = getCurrentUser(authHeader);
            List<Order> orders = orderService.findByUserId(currentUser.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting my orders", e);
            throw new BusinessException("INTERNAL_ERROR", "내 주문 조회 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/user/me/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'WAREHOUSE', 'PARTNER')")
    public ResponseEntity<Map<String, Object>> getMyOrderStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            List<Order> orders = orderService.findByUserId(currentUser.getId());
            
            Map<String, Object> stats = calculateOrderStats(orders);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting my order stats", e);
            throw new BusinessException("INTERNAL_ERROR", "내 주문 통계 조회 중 오류가 발생했습니다.");
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE') or #userId == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            
            // 다른 사용자 주문 조회 권한 확인
            if (!currentUser.getId().equals(userId) && 
                !User.UserType.ADMIN.equals(currentUser.getUserType()) &&
                !User.UserType.WAREHOUSE.equals(currentUser.getUserType())) {
                throw new BusinessException("ACCESS_DENIED", "다른 사용자의 주문을 조회할 권한이 없습니다.");
            }
            
            List<Order> orders = orderService.findByUserId(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting user orders for user {}", userId, e);
            throw new BusinessException("INTERNAL_ERROR", "사용자 주문 조회 중 오류가 발생했습니다.");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderService.UpdateOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "error", "인증이 필요합니다."));
            }
            
            User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            Optional<Order> orderOpt = orderService.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // 주문 수정 권한 확인 - 관리자, 창고, 또는 주문 소유자만 가능
            if (!orderService.isOrderAccessible(order, user)) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "error", "해당 주문을 수정할 권한이 없습니다."));
            }
            
            // 주문 소유자는 특정 상태에서만 수정 가능
            if (!User.UserType.ADMIN.equals(user.getUserType()) &&
                !User.UserType.WAREHOUSE.equals(user.getUserType())) {
                if (order.getStatus() != Order.OrderStatus.PENDING &&
                    order.getStatus() != Order.OrderStatus.PROCESSING) {
                    return ResponseEntity.status(403)
                        .body(Map.of("success", false, "error", "현재 상태에서는 주문을 수정할 수 없습니다."));
                }
            }
            
            Order updatedOrder = orderService.updateOrder(id, request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문이 성공적으로 수정되었습니다.",
                "order", updatedOrder
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Order update error for order {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "주문 수정 중 오류가 발생했습니다."));
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestBody StatusUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "error", "인증이 필요합니다."));
            }
            
            User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 주문 상태 변경은 관리자와 창고 사용자만 가능
            if (!User.UserType.ADMIN.equals(user.getUserType()) &&
                !User.UserType.WAREHOUSE.equals(user.getUserType())) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "error", "주문 상태를 변경할 권한이 없습니다."));
            }
            
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
    public ResponseEntity<Map<String, Object>> getOrderTracking(
            @PathVariable String orderNumber,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Getting order tracking for orderNumber: {}", orderNumber);
            
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "error", "인증이 필요합니다."));
            }
            
            User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            Optional<Order> orderOpt = orderService.findByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "주문번호를 찾을 수 없습니다."
                ));
            }
            
            Order order = orderOpt.get();
            
            // 주문 접근 권한 확인
            if (!orderService.isOrderAccessible(order, user)) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "error", "해당 주문의 추적 정보에 접근할 권한이 없습니다."));
            }
            
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
    private int getCurrentStage(com.ysc.lms.entity.Order.OrderStatus status) {
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
        
        if (order.getStatus() != com.ysc.lms.entity.Order.OrderStatus.PENDING) {
            timeline.add(Map.of(
                "eventName", "주문 처리",
                "eventTime", order.getCreatedAt(),
                "description", "주문 처리가 시작되었습니다."
            ));
        }
        
        if (order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.SHIPPED ||
            order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.IN_TRANSIT ||
            order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.DELIVERED) {
            timeline.add(Map.of(
                "eventName", "배송 시작",
                "eventTime", order.getCreatedAt(),
                "description", "배송이 시작되었습니다."
            ));
        }
        
        if (order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.IN_TRANSIT ||
            order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.DELIVERED) {
            timeline.add(Map.of(
                "eventName", "운송 중",
                "eventTime", order.getCreatedAt(),
                "description", "상품이 운송 중입니다."
            ));
        }
        
        if (order.getStatus() == com.ysc.lms.entity.Order.OrderStatus.DELIVERED) {
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
    
    // 현재 사용자 정보 추출 헬퍼 메소드
    private User getCurrentUser(String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            throw new BusinessException("AUTH_REQUIRED", "인증이 필요합니다.");
        }
        
        return userService.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
    
    // 사용자 역할에 따른 주문 목록 조회
    private List<Order> getOrdersBasedOnUserRole(User user) {
        return switch (user.getUserType()) {
            case ADMIN, WAREHOUSE -> orderService.findAll();
            case PARTNER -> orderService.findOrdersForPartner(user.getId());
            default -> orderService.findByUserId(user.getId());
        };
    }
    
    // 주문 통계 계산
    private Map<String, Object> calculateOrderStats(List<Order> orders) {
        int total = orders.size();
        
        List<Order.OrderStatus> inProgressStatuses = Arrays.asList(
            Order.OrderStatus.RECEIVED, Order.OrderStatus.ARRIVED, Order.OrderStatus.REPACKING,
            Order.OrderStatus.SHIPPING, Order.OrderStatus.BILLING, Order.OrderStatus.PAYMENT_PENDING,
            Order.OrderStatus.PROCESSING, Order.OrderStatus.IN_TRANSIT
        );
        
        List<Order.OrderStatus> deliveredStatuses = Arrays.asList(
            Order.OrderStatus.DELIVERED, Order.OrderStatus.COMPLETED, Order.OrderStatus.PAYMENT_CONFIRMED
        );
        
        int inProgress = (int) orders.stream()
            .filter(order -> inProgressStatuses.contains(order.getStatus()))
            .count();
            
        int delivered = (int) orders.stream()
            .filter(order -> deliveredStatuses.contains(order.getStatus()))
            .count();
            
        BigDecimal totalFreight = orders.stream()
            .map(order -> order.getTotalWeight() != null ? order.getTotalWeight() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "total", total,
            "inProgress", inProgress,
            "delivered", delivered,
            "totalFreight", totalFreight
        );
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