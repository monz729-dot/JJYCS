package com.ycs.lms.service;

import com.ycs.lms.dto.orders.*;
import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import com.ycs.lms.entity.User;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.UserRepository;
import com.ycs.lms.util.CBMCalculator;
import com.ycs.lms.util.PagedResponse;
import com.ycs.lms.dto.orders.BusinessRuleValidationResponse.BusinessWarning;
import com.ycs.lms.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 주문 관리 서비스
 * 핵심 비즈니스 로직:
 * 1. CBM 계산 및 29 초과시 자동 air 전환
 * 2. THB 1,500 초과시 추가 정보 필요 경고
 * 3. member_code 미기재시 지연 처리
 * 4. EMS/HS 코드 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final NotificationService notificationService;
    private final CBMCalculator cbmCalculator;
    private final ConfigService configService;

    /**
     * 주문 생성 (모든 비즈니스 룰 적용)
     */
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        log.info("Creating order for user: {}", userId);
        
        // 1. 사용자 검증 및 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 2. 사용자 상태 검증 (승인된 사용자만 주문 가능)
        if (!"active".equals(user.getStatus())) {
            throw new BusinessRuleViolationException("승인되지 않은 사용자는 주문을 생성할 수 없습니다.");
        }
        
        // 3. 주문 기본 정보 생성
        Order order = createOrderEntity(request, user);
        
        // 4. 주문 상품 추가
        List<OrderItem> orderItems = createOrderItems(request.getItems(), order);
        order.setItems(orderItems);
        
        // 5. 주문 박스 추가 및 CBM 계산
        List<OrderBox> orderBoxes = createOrderBoxes(request.getBoxes(), order);
        order.setBoxes(orderBoxes);
        
        // 6. 비즈니스 룰 적용
        applyBusinessRules(order, user);
        
        // 7. EMS/HS 코드 검증 (비동기)
        validateExternalCodes(order);
        
        // 8. 주문 저장
        order = orderRepository.save(order);
        
        // 9. 알림 발송
        sendOrderNotifications(order);
        
        log.info("Order created successfully: {}", order.getOrderCode());
        
        return mapToOrderResponse(order);
    }

    /**
     * 주문 기본 엔티티 생성
     */
    private Order createOrderEntity(CreateOrderRequest request, User user) {
        Order order = new Order();
        
        // 주문 코드 생성 (ORD-2024-001 형식)
        order.setOrderCode(generateOrderCode());
        order.setUser(user);
        order.setStatus("requested");
        order.setOrderType(request.getShipping().getPreferredType()); // 초기값, CBM 계산 후 변경 가능
        
        // 수취인 정보
        order.setRecipientName(request.getRecipient().getName());
        order.setRecipientPhone(request.getRecipient().getPhone());
        order.setRecipientAddress(request.getRecipient().getAddress());
        order.setRecipientZipCode(request.getRecipient().getZipCode());
        order.setRecipientCountry(request.getRecipient().getCountry());
        
        // 배송 정보
        order.setUrgency(request.getShipping().getUrgency());
        order.setNeedsRepacking(request.getShipping().getNeedsRepacking());
        order.setSpecialInstructions(request.getShipping().getSpecialInstructions());
        
        // 결제 정보
        order.setPaymentMethod(request.getPayment().getMethod());
        order.setPaymentStatus("pending");
        
        return order;
    }

    /**
     * 주문 상품 엔티티 생성
     */
    private List<OrderItem> createOrderItems(List<CreateOrderRequest.Item> itemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (int i = 0; i < itemRequests.size(); i++) {
            CreateOrderRequest.Item itemRequest = itemRequests.get(i);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItemOrder(i + 1);
            orderItem.setName(itemRequest.getName());
            orderItem.setDescription(itemRequest.getDescription());
            orderItem.setCategory(itemRequest.getCategory());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitWeight(itemRequest.getWeight());
            orderItem.setTotalAmount(itemRequest.getAmount());
            orderItem.setCurrency(itemRequest.getCurrency());
            orderItem.setHsCode(itemRequest.getHsCode());
            orderItem.setCountryOfOrigin("KR"); // 기본값
            
            totalAmount = totalAmount.add(itemRequest.getAmount());
            orderItems.add(orderItem);
        }
        
        // 주문 총액 설정
        order.setTotalAmount(totalAmount);
        order.setCurrency(itemRequests.get(0).getCurrency()); // 첫 번째 아이템의 통화 사용
        
        return orderItems;
    }

    /**
     * 주문 박스 엔티티 생성 및 CBM 계산
     */
    private List<OrderBox> createOrderBoxes(List<CreateOrderRequest.Box> boxRequests, Order order) {
        List<OrderBox> orderBoxes = new ArrayList<>();
        
        for (int i = 0; i < boxRequests.size(); i++) {
            CreateOrderRequest.Box boxRequest = boxRequests.get(i);
            
            OrderBox orderBox = new OrderBox();
            orderBox.setOrder(order);
            orderBox.setBoxNumber(i + 1);
            orderBox.setLabelCode(generateLabelCode(order.getOrderCode(), i + 1));
            orderBox.setWidthCm(boxRequest.getWidth());
            orderBox.setHeightCm(boxRequest.getHeight());
            orderBox.setDepthCm(boxRequest.getDepth());
            orderBox.setWeightKg(boxRequest.getWeight());
            orderBox.setStatus("created");
            
            // CBM은 DB 가상컬럼에서 자동 계산되지만, 애플리케이션에서도 계산
            BigDecimal cbm = cbmCalculator.calculateCBM(
                boxRequest.getWidth(), 
                boxRequest.getHeight(), 
                boxRequest.getDepth()
            );
            
            orderBoxes.add(orderBox);
        }
        
        return orderBoxes;
    }

    /**
     * 비즈니스 룰 적용
     */
    private void applyBusinessRules(Order order, User user) {
        List<BusinessWarning> warnings = new ArrayList<>();
        
        // 1. member_code 검증
        if (user.getMemberCode() == null || user.getMemberCode().trim().isEmpty()) {
            order.setStatus("delayed");
            order.appendNote("[AUTO] 회원코드 미기재로 지연 처리");
            warnings.add(new BusinessWarning("MEMBER_CODE_REQUIRED", 
                "회원코드가 없어 주문이 지연 상태로 처리됩니다. 고객센터에 문의해주세요."));
            log.warn("Order delayed due to missing member_code for user: {}", user.getId());
        }
        
        // 2. CBM 계산 및 29 초과시 air 전환
        BigDecimal totalCBM = calculateTotalCBM(order.getBoxes());
        BigDecimal cbmThreshold = configService.getCBMThreshold(); // 29.0
        
        if (totalCBM.compareTo(cbmThreshold) > 0 && "sea".equals(order.getOrderType())) {
            order.setOrderType("air");
            order.appendNote(String.format("[AUTO] CBM %.3f 초과로 항공 배송 전환", totalCBM));
            warnings.add(new BusinessWarning("CBM_EXCEEDED", 
                String.format("총 CBM이 %.3fm³으로 %sm³을 초과하여 항공 배송으로 자동 전환되었습니다.", 
                    totalCBM, cbmThreshold), 
                Map.of("cbm", totalCBM, "threshold", cbmThreshold)));
            log.info("Order {} converted to air shipping due to CBM: {} > {}", 
                order.getOrderCode(), totalCBM, cbmThreshold);
        }
        
        // 3. THB 1,500 초과시 추가 정보 필요 경고
        if ("THB".equals(order.getCurrency()) && 
            order.getTotalAmount().compareTo(new BigDecimal("1500")) > 0) {
            order.setRequiresExtraRecipient(true);
            warnings.add(new BusinessWarning("AMOUNT_EXCEEDED_THB_1500",
                "금액이 THB 1,500을 초과합니다. 수취인 추가 정보를 입력해주세요.",
                Map.of("amount", order.getTotalAmount(), "threshold", 1500)));
            log.info("Order {} requires extra recipient info due to amount: {} THB > 1500", 
                order.getOrderCode(), order.getTotalAmount());
        }
        
        // 4. 경고 정보 저장 (JSON 형태로)
        if (!warnings.isEmpty()) {
            // TODO: 주문에 경고 정보 저장하는 필드 추가
            log.info("Order {} has {} business rule warnings", order.getOrderCode(), warnings.size());
        }
    }

    /**
     * 외부 API 코드 검증 (비동기)
     */
    private void validateExternalCodes(Order order) {
        // EMS/HS 코드 검증을 비동기로 실행
        // 실패시 폴백 처리
        order.getItems().forEach(item -> {
            if (item.getHsCode() != null) {
                validationService.validateHSCodeAsync(item.getHsCode())
                    .thenAccept(result -> {
                        if (!result.isValid() && !result.isFallbackUsed()) {
                            log.warn("Invalid HS code: {} for item: {}", 
                                item.getHsCode(), item.getName());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("HS code validation failed for: " + item.getHsCode(), ex);
                        return null;
                    });
            }
        });
    }

    /**
     * 알림 발송
     */
    private void sendOrderNotifications(Order order) {
        // 사용자에게 주문 접수 알림
        notificationService.sendOrderCreatedNotification(order);
        
        // 관리자에게 새 주문 알림
        if ("delayed".equals(order.getStatus())) {
            notificationService.sendDelayedOrderAlert(order);
        }
    }

    /**
     * 주문 조회
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        return mapToOrderResponse(order);
    }

    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummary> getOrders(OrderSearchFilter filter, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByFilter(filter, pageable);
        
        List<OrderSummary> orderSummaries = ordersPage.getContent().stream()
                .map(this::mapToOrderSummary)
                .toList();
        
        return PagedResponse.of(orderSummaries, ordersPage);
    }

    /**
     * 주문 수정
     */
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        // 권한 검증
        if (!isOrderOwner(orderId, userId)) {
            throw new AccessDeniedException("주문을 수정할 권한이 없습니다.");
        }
        
        // 수정 가능 상태 검증
        if (!isOrderModifiable(order.getStatus())) {
            throw new OrderNotModifiableException("현재 상태에서는 주문을 수정할 수 없습니다.");
        }
        
        // 주문 정보 업데이트
        updateOrderFromRequest(order, request);
        
        // 비즈니스 룰 재적용
        applyBusinessRules(order, order.getUser());
        
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }

    /**
     * 주문 상태 변경
     */
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        // 상태 전환 검증
        if (!isValidStatusTransition(order.getStatus(), request.getStatus())) {
            throw new InvalidStatusTransitionException("잘못된 상태 전환입니다: " + order.getStatus() + " -> " + request.getStatus());
        }
        
        // 상태 변경
        order.setStatus(request.getStatus());
        if (request.getReason() != null) {
            order.appendNote("[STATUS_CHANGE] " + request.getReason());
        }
        
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }

    /**
     * 주문 취소
     */
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
        
        // 권한 검증
        if (!isOrderOwner(orderId, userId)) {
            throw new AccessDeniedException("주문을 취소할 권한이 없습니다.");
        }
        
        // 취소 가능 상태 검증
        if (!isOrderCancellable(orderId, userId)) {
            throw new OrderNotCancellableException("현재 상태에서는 주문을 취소할 수 없습니다.");
        }
        
        // 주문 취소
        order.setStatus("cancelled");
        order.appendNote("[CANCELLED] " + request.getReason());
        
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }

    /**
     * CBM 계산
     */
    public CBMCalculationResponse calculateCBM(CBMCalculationRequest request) {
        BigDecimal totalCBM = BigDecimal.ZERO;
        List<CBMCalculationResponse.BoxCBM> boxCBMs = new ArrayList<>();
        
        for (CBMCalculationRequest.Box box : request.getBoxes()) {
            BigDecimal cbm = cbmCalculator.calculateCBM(box.getWidth(), box.getHeight(), box.getDepth());
            totalCBM = totalCBM.add(cbm);
            
            boxCBMs.add(CBMCalculationResponse.BoxCBM.builder()
                    .width(box.getWidth())
                    .height(box.getHeight())
                    .depth(box.getDepth())
                    .cbm(cbm)
                    .build());
        }
        
        // 배송 방법 추천
        BigDecimal cbmThreshold = configService.getCBMThreshold();
        String recommendedMethod = totalCBM.compareTo(cbmThreshold) > 0 ? "air" : "sea";
        
        return CBMCalculationResponse.builder()
                .totalCBM(totalCBM)
                .boxes(boxCBMs)
                .recommendedShippingMethod(recommendedMethod)
                .cbmThreshold(cbmThreshold)
                .exceedsThreshold(totalCBM.compareTo(cbmThreshold) > 0)
                .build();
    }

    /**
     * 비즈니스 룰 검증
     */
    public BusinessRuleValidationResponse validateBusinessRules(
            BusinessRuleValidationRequest request, Long userId) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        List<BusinessWarning> warnings = new ArrayList<>();
        
        // member_code 검증
        if (user.getMemberCode() == null || user.getMemberCode().trim().isEmpty()) {
            warnings.add(new BusinessWarning("MEMBER_CODE_REQUIRED", 
                "회원코드가 없어 주문이 지연될 수 있습니다."));
        }
        
        // CBM 검증
        BigDecimal totalCBM = request.getBoxes().stream()
                .map(box -> cbmCalculator.calculateCBM(box.getWidth(), box.getHeight(), box.getDepth()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalCBM.compareTo(configService.getCBMThreshold()) > 0) {
            warnings.add(new BusinessWarning("CBM_EXCEEDED", 
                "총 CBM이 임계값을 초과하여 항공 배송으로 자동 전환됩니다."));
        }
        
        // 금액 검증
        if ("THB".equals(request.getCurrency()) && 
            request.getTotalAmount().compareTo(new BigDecimal("1500")) > 0) {
            warnings.add(new BusinessWarning("AMOUNT_EXCEEDED_THB_1500",
                "금액이 THB 1,500을 초과하여 수취인 추가 정보가 필요합니다."));
        }
        
        return BusinessRuleValidationResponse.builder()
                .valid(warnings.isEmpty())
                .warnings(warnings)
                .totalCBM(totalCBM)
                .recommendedShippingMethod(totalCBM.compareTo(configService.getCBMThreshold()) > 0 ? "air" : "sea")
                .build();
    }

    // ================================
    // Private Helper Methods
    // ================================

    private String generateOrderCode() {
        return String.format("ORD-%d-%06d", 
                LocalDateTime.now().getYear(), 
                orderRepository.getNextOrderSequence());
    }

    private String generateLabelCode(String orderCode, int boxNumber) {
        return String.format("%s-%02d", orderCode.replace("ORD-", "BOX-"), boxNumber);
    }

    private BigDecimal calculateTotalCBM(List<OrderBox> boxes) {
        return boxes.stream()
                .map(box -> cbmCalculator.calculateCBM(
                    box.getWidthCm(), box.getHeightCm(), box.getDepthCm()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        // TODO: MapStruct 또는 ModelMapper 사용 권장
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .requiresExtraRecipient(order.getRequiresExtraRecipient())
                .createdAt(order.getCreatedAt())
                // ... 기타 필드 매핑
                .build();
    }

    private OrderSummary mapToOrderSummary(Order order) {
        return OrderSummary.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .recipientName(order.getRecipientName())
                .recipientCountry(order.getRecipientCountry())
                .createdAt(order.getCreatedAt())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .build();
    }

    // 권한 체크 메서드들
    public boolean isOrderAccessible(Long orderId, Long userId) {
        // TODO: 실제 구현
        return true;
    }

    public boolean isOrderOwner(Long orderId, Long userId) {
        // TODO: 실제 구현  
        return true;
    }

    public boolean isOrderCancellable(Long orderId, Long userId) {
        // TODO: 실제 구현
        return true;
    }

    private boolean isOrderModifiable(String status) {
        return List.of("requested", "confirmed", "payment_pending").contains(status);
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // 상태 전환 규칙 정의
        return switch (currentStatus) {
            case "requested" -> List.of("confirmed", "cancelled", "delayed").contains(newStatus);
            case "confirmed" -> List.of("processing", "cancelled").contains(newStatus);
            case "processing" -> List.of("ready_for_shipping", "cancelled").contains(newStatus);
            case "ready_for_shipping" -> List.of("shipped", "cancelled").contains(newStatus);
            case "shipped" -> List.of("delivered", "returned").contains(newStatus);
            case "delayed" -> List.of("confirmed", "cancelled").contains(newStatus);
            default -> false;
        };
    }

    private void updateOrderFromRequest(Order order, UpdateOrderRequest request) {
        // TODO: 실제 업데이트 로직 구현
        // 수취인 정보 업데이트
        if (request.getRecipient() != null) {
            order.setRecipientName(request.getRecipient().getName());
            order.setRecipientPhone(request.getRecipient().getPhone());
            order.setRecipientAddress(request.getRecipient().getAddress());
            order.setRecipientZipCode(request.getRecipient().getZipCode());
            order.setRecipientCountry(request.getRecipient().getCountry());
        }
        
        // 배송 정보 업데이트
        if (request.getShipping() != null) {
            order.setUrgency(request.getShipping().getUrgency());
            order.setNeedsRepacking(request.getShipping().getNeedsRepacking());
            order.setSpecialInstructions(request.getShipping().getSpecialInstructions());
        }
        
        // 결제 정보 업데이트
        if (request.getPayment() != null) {
            order.setPaymentMethod(request.getPayment().getMethod());
        }
    }
}

// 비즈니스 경고 클래스는 com.ycs.lms.dto.orders.BusinessRuleValidationResponse.BusinessWarning을 사용합니다.

// 추가 예외 클래스들
class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}

class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}

class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) { super(message); }
}

class OrderNotModifiableException extends RuntimeException {
    public OrderNotModifiableException(String message) { super(message); }
}

class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(String message) { super(message); }
}

class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) { super(message); }
} 