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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }
            
            // 요청 데이터 유효성 검증
            validateOrderRequest(request);
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
    
    /**
     * 주문 생성 요청 데이터 유효성 검증
     */
    private void validateOrderRequest(CreateOrderRequest request) {
        log.debug("Validating order request");
        
        if (request == null) {
            throw new BusinessException("INVALID_REQUEST", "주문 요청 데이터가 없습니다.");
        }
        
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new BusinessException("EMPTY_ORDER_ITEMS", "주문에는 최소 1개 이상의 상품이 필요합니다.");
        }
        
        if (request.getRecipientName() == null || request.getRecipientName().trim().isEmpty()) {
            throw new BusinessException("MISSING_RECIPIENT", "수취인 정보가 필요합니다.");
        }
        
        if (request.getCountry() == null || request.getCountry().trim().isEmpty()) {
            throw new BusinessException("MISSING_COUNTRY", "배송 국가 정보가 필요합니다.");
        }
        
        // 최대 아이템 개수 제한 (성능 보호)
        if (request.getOrderItems().size() > 100) {
            throw new BusinessException("TOO_MANY_ITEMS", "주문 아이템은 최대 100개까지 가능합니다.");
        }
        
        // 주문 아이템 개별 검증
        try {
            for (int i = 0; i < request.getOrderItems().size(); i++) {
                CreateOrderRequest.OrderItemRequest item = request.getOrderItems().get(i);
                validateOrderItem(item, i + 1);
            }
            log.debug("Order request validation passed for {} items", request.getOrderItems().size());
        } catch (Exception e) {
            log.error("Order request validation failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 주문 아이템 개별 유효성 검증
     */
    private void validateOrderItem(CreateOrderRequest.OrderItemRequest item, int itemIndex) {
        String prefix = "상품 " + itemIndex + "번: ";
        
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            throw new BusinessException("MISSING_ITEM_DESCRIPTION", 
                prefix + "상품 설명이 필요합니다.");
        }
        
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BusinessException("INVALID_QUANTITY", 
                prefix + "수량은 1개 이상이어야 합니다.");
        }
        
        if (item.getWeight() == null || item.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_WEIGHT", 
                prefix + "중량은 0보다 커야 합니다.");
        }
        
        if (item.getWidth() == null || item.getWidth().compareTo(BigDecimal.ZERO) <= 0 ||
            item.getHeight() == null || item.getHeight().compareTo(BigDecimal.ZERO) <= 0 ||
            item.getDepth() == null || item.getDepth().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_DIMENSIONS", 
                prefix + "상품 크기(가로/세로/높이)는 0보다 커야 합니다.");
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 사용자 인증 확인
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            throw new BusinessException("AUTH_REQUIRED", "로그인이 필요합니다.");
        }
        
        User user = userService.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        Order order = orderService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        
        // 주문 접근 권한 확인
        if (!orderService.isOrderAccessible(order, user)) {
            throw new BusinessException("ACCESS_DENIED", "해당 주문에 접근할 권한이 없습니다.");
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "order", order
        ));
    }
    
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(
            @PathVariable String orderNumber,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 사용자 인증 확인
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            throw new BusinessException("AUTH_REQUIRED", "로그인이 필요합니다.");
        }
        
        User user = userService.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        
        Order order = orderService.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("주문번호 '" + orderNumber + "'을 찾을 수 없습니다."));
        
        // 주문 접근 권한 확인
        if (!orderService.isOrderAccessible(order, user)) {
            throw new BusinessException("ACCESS_DENIED", "해당 주문에 접근할 권한이 없습니다.");
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "order", order
        ));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }
            
            User user = userService.findById(userId).orElse(null);
            if (user == null) {
                // 사용자를 찾을 수 없는 경우 빈 목록 반환
                log.warn("User not found for ID: {}, returning empty order list", userId);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", new java.util.ArrayList<>(),
                    "count", 0
                ));
            }
            
            List<Order> orders;
            
            // 역할에 따른 주문 목록 필터링
            if (User.UserType.ADMIN.equals(user.getUserType())) {
                // 관리자는 모든 주문 조회 가능
                orders = orderService.findAll();
            } else if (User.UserType.WAREHOUSE.equals(user.getUserType())) {
                // 창고 사용자는 모든 주문 조회 가능 (향후 창고별 필터링 가능)
                orders = orderService.findAll();
            } else if (User.UserType.PARTNER.equals(user.getUserType())) {
                // 파트너는 본인이 추천한 사용자들의 주문도 조회 가능
                orders = orderService.findOrdersForPartner(userId);
            } else {
                // 일반 사용자는 본인 주문만 조회 가능
                orders = orderService.findByUserId(userId);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting all orders", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "주문 목록 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getMyOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }
            
            List<Order> orders = orderService.findByUserId(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting my orders", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "내 주문 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/user/me/stats")
    public ResponseEntity<Map<String, Object>> getMyOrderStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Getting my order stats");
            // 사용자 인증 확인
            Long userId = getUserIdFromToken(authHeader);
            log.info("User ID from token: {}", userId);
            if (userId == null) {
                log.error("No user ID found in token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }
            
            log.info("Fetching orders for userId: {}", userId);
            List<Order> orders = orderService.findByUserId(userId);
            log.info("Found {} orders for user {}", orders.size(), userId);
            
            // 통계 계산
            int total = orders.size();
            int inProgress = (int) orders.stream()
                .filter(order -> Arrays.asList(
                    Order.OrderStatus.RECEIVED,
                    Order.OrderStatus.ARRIVED, 
                    Order.OrderStatus.REPACKING,
                    Order.OrderStatus.SHIPPING,
                    Order.OrderStatus.BILLING,
                    Order.OrderStatus.PAYMENT_PENDING,
                    Order.OrderStatus.PROCESSING,
                    Order.OrderStatus.IN_TRANSIT
                ).contains(order.getStatus()))
                .count();
                
            int delivered = (int) orders.stream()
                .filter(order -> Arrays.asList(
                    Order.OrderStatus.DELIVERED,
                    Order.OrderStatus.COMPLETED,
                    Order.OrderStatus.PAYMENT_CONFIRMED
                ).contains(order.getStatus()))
                .count();
                
            BigDecimal totalFreight = orders.stream()
                .map(order -> order.getTotalWeight() != null ? order.getTotalWeight() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            log.info("Stats calculated - total: {}, inProgress: {}, delivered: {}, totalFreight: {}", 
                total, inProgress, delivered, totalFreight);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", Map.of(
                    "total", total,
                    "inProgress", inProgress,
                    "delivered", delivered,
                    "totalFreight", totalFreight
                )
            );
            log.info("Returning response: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting my order stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "내 주문 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 사용자 인증 확인
            Long currentUserId = getUserIdFromToken(authHeader);
            if (currentUserId == null) {
                // 토큰이 없는 경우 요청된 userId를 그대로 사용 (테스트용)
                log.warn("No auth token found, allowing access to requested user orders for testing");
                currentUserId = userId;
            }
            
            User currentUser = userService.findById(currentUserId).orElse(null);
            if (currentUser == null) {
                // 사용자가 없는 경우 빈 목록 반환
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", new java.util.ArrayList<>(),
                    "count", 0
                ));
            }
            
            // 다른 사용자 주문 조회 권한 확인
            if (!currentUserId.equals(userId) && 
                !User.UserType.ADMIN.equals(currentUser.getUserType()) &&
                !User.UserType.WAREHOUSE.equals(currentUser.getUserType())) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "error", "다른 사용자의 주문을 조회할 권한이 없습니다."));
            }
            
            List<Order> orders = orderService.findByUserId(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders,
                "count", orders.size()
            ));
            
        } catch (Exception e) {
            log.error("Error getting user orders for user {}", userId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "사용자 주문 조회 중 오류가 발생했습니다."));
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