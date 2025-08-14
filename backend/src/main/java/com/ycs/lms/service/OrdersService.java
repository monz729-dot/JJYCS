package com.ycs.lms.service;

import com.ycs.lms.dto.OrderCreateRequest;
import com.ycs.lms.dto.OrderResponse;
import com.ycs.lms.dto.PageResponse;
import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import com.ycs.lms.entity.User;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.OrderBoxRepository;
import com.ycs.lms.repository.OrderItemRepository;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrdersService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderBoxRepository orderBoxRepository;
    private final UserRepository userRepository;
    private final BusinessRuleService businessRuleService;

    /**
     * 주문 생성
     */
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // 사용자 존재 확인
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 주문 생성
        Order order = Order.builder()
            .orderCode(generateOrderCode())
            .userId(request.getUserId())
            .status(Order.OrderStatus.REQUESTED)
            .orderType(Order.OrderType.SEA) // 기본값, CBM 검사 후 변경될 수 있음
            .recipientName(request.getRecipientName())
            .recipientPhone(request.getRecipientPhone())
            .recipientAddress(request.getRecipientAddress())
            .recipientCountry(request.getRecipientCountry())
            .urgency(request.isUrgent() ? Order.OrderUrgency.URGENT : Order.OrderUrgency.NORMAL)
            .needsRepacking(request.isNeedsRepacking())
            .specialInstructions(request.getSpecialInstructions())
            .currency("THB")
            .paymentMethod(Order.PaymentMethod.PREPAID)
            .paymentStatus(Order.PaymentStatus.PENDING)
            .createdBy(request.getUserId())
            .build();

        // 주문 저장
        order = orderRepository.save(order);
        final Long savedOrderId = order.getId();

        // 주문 아이템 생성
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> OrderItem.builder()
                    .orderId(savedOrderId)
                    .itemOrder(itemRequest.getItemOrder())
                    .name(itemRequest.getName())
                    .description(itemRequest.getDescription())
                    .category(itemRequest.getCategory())
                    .quantity(itemRequest.getQuantity())
                    .unitWeight(itemRequest.getUnitWeight())
                    .unitPrice(itemRequest.getUnitPrice())
                    .totalAmount(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .currency("THB")
                    .hsCode(itemRequest.getHsCode())
                    .emsCode(itemRequest.getEmsCode())
                    .countryOfOrigin(itemRequest.getCountryOfOrigin())
                    .brand(itemRequest.getBrand())
                    .restricted(itemRequest.isRestricted())
                    .restrictionNote(itemRequest.getRestrictionNote())
                    .build())
                .collect(Collectors.toList());

            orderItemRepository.saveAll(items);
        }

        // 주문 박스 생성
        if (request.getBoxes() != null && !request.getBoxes().isEmpty()) {
            List<OrderBox> boxes = request.getBoxes().stream()
                .map(boxRequest -> OrderBox.builder()
                    .orderId(savedOrderId)
                    .boxNumber(boxRequest.getBoxNumber())
                    .labelCode(generateLabelCode(savedOrderId, boxRequest.getBoxNumber()))
                    .widthCm(boxRequest.getWidthCm())
                    .heightCm(boxRequest.getHeightCm())
                    .depthCm(boxRequest.getDepthCm())
                    .weightKg(boxRequest.getWeightKg())
                    .status(OrderBox.BoxStatus.CREATED)
                    .notes(boxRequest.getNotes())
                    .build())
                .collect(Collectors.toList());

            orderBoxRepository.saveAll(boxes);
        }

        // 비즈니스 룰 적용
        applyBusinessRules(savedOrderId);

        // 업데이트된 주문 조회 및 반환
        Order savedOrder = orderRepository.findById(savedOrderId).orElse(order);
        return convertToOrderResponse(savedOrder);
    }

    /**
     * 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(Long userId, String status, String orderType,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                Pageable pageable) {
        log.info("Getting orders for user: {}, status: {}, type: {}", userId, status, orderType);

        Page<Order> orders;

        if (userId != null) {
            if (status != null) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
            } else {
                orders = orderRepository.findByUserId(userId, pageable);
            }
        } else {
            if (status != null) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus, pageable);
            } else {
                orders = orderRepository.findAll(pageable);
            }
        }

        List<OrderResponse> orderResponses = orders.getContent().stream()
            .map(this::convertToOrderResponse)
            .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
            .content(orderResponses)
            .page(orders.getNumber())
            .size(orders.getSize())
            .totalElements(orders.getTotalElements())
            .totalPages(orders.getTotalPages())
            .first(orders.isFirst())
            .last(orders.isLast())
            .build();
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        log.info("Getting order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + orderId));

        return convertToOrderResponse(order);
    }

    /**
     * 주문 상태 업데이트
     */
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + orderId));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            order = orderRepository.save(order);

            return convertToOrderResponse(order);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("잘못된 주문 상태입니다: " + status);
        }
    }

    /**
     * 주문 취소
     */
    public OrderResponse cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getStatus() == Order.OrderStatus.DELIVERED ||
            order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new BadRequestException("배송된 주문은 취소할 수 없습니다");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() + "\n취소 사유: " + reason);
        order = orderRepository.save(order);

        return convertToOrderResponse(order);
    }

    /**
     * 비즈니스 룰 적용
     */
    private void applyBusinessRules(Long orderId) {
        log.info("Applying business rules for order: {}", orderId);

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;

        // CBM 계산 및 29 초과 시 항공 전환
        BigDecimal totalCbm = orderBoxRepository.sumCbmByOrderId(orderId);
        if (totalCbm != null) {
            order.setTotalCbmM3(totalCbm);

            if (totalCbm.compareTo(BigDecimal.valueOf(29.0)) > 0) {
                log.warn("Order {} CBM ({}) exceeds 29m³, converting to AIR", orderId, totalCbm);
                order.setOrderType(Order.OrderType.AIR);
            }
        }

        // THB 1,500 초과 시 추가 수취인 정보 필요
        BigDecimal totalAmount = orderItemRepository.sumTotalAmountByOrderId(orderId);
        if (totalAmount != null) {
            order.setTotalAmount(totalAmount);

            if (totalAmount.compareTo(BigDecimal.valueOf(1500.0)) > 0) {
                log.warn("Order {} amount ({}) exceeds 1500 THB, requires extra recipient info", orderId, totalAmount);
                order.setRequiresExtraRecipient(true);
            }
        }

        // 회원 코드 없는 사용자 지연 처리
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null && (user.getMemberCode() == null || user.getMemberCode().trim().isEmpty())) {
            log.warn("Order {} user has no member code, marking for delay", orderId);
            // 상태를 지연으로 설정하거나 특별 플래그 설정
            order.setNotes(order.getNotes() + "\n회원코드 미등록으로 지연 처리됨");
        }

        orderRepository.save(order);
    }

    /**
     * 주문 코드 생성
     */
    private String generateOrderCode() {
        long count = orderRepository.count();
        return String.format("YCS-%d-%05d", LocalDate.now().getYear(), count + 1);
    }

    /**
     * 라벨 코드 생성
     */
    private String generateLabelCode(Long orderId, int boxNumber) {
        return String.format("BOX-%d-%05d-%02d", LocalDate.now().getYear(), orderId, boxNumber);
    }

    /**
     * Order를 OrderResponse로 변환
     */
    private OrderResponse convertToOrderResponse(Order order) {
        // 관련 데이터 조회
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByItemOrder(order.getId());
        List<OrderBox> boxes = orderBoxRepository.findByOrderIdOrderByBoxNumber(order.getId());

        return OrderResponse.builder()
            .id(order.getId())
            .orderCode(order.getOrderCode())
            .userId(order.getUserId())
            .status(order.getStatus().name())
            .orderType(order.getOrderType().name())
            .recipientName(order.getRecipientName())
            .recipientPhone(order.getRecipientPhone())
            .recipientAddress(order.getRecipientAddress())
            .recipientCountry(order.getRecipientCountry())
            .urgency(order.getUrgency().name())
            .needsRepacking(order.isNeedsRepacking())
            .specialInstructions(order.getSpecialInstructions())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .totalCbmM3(order.getTotalCbmM3())
            .requiresExtraRecipient(order.isRequiresExtraRecipient())
            .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
            .actualDeliveryDate(order.getActualDeliveryDate())
            .paymentMethod(order.getPaymentMethod().name())
            .paymentStatus(order.getPaymentStatus().name())
            .estimatedCost(order.getEstimatedCost())
            .actualCost(order.getActualCost())
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .itemCount(items.size())
            .boxCount(boxes.size())
            .build();
    }

    /**
     * 사용자별 주문 통계
     */
    @Transactional(readOnly = true)
    public OrderStatsResponse getUserOrderStats(Long userId) {
        long totalOrders = orderRepository.countByUserId(userId);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, Order.OrderStatus.REQUESTED);
        long inTransitOrders = orderRepository.countByUserIdAndStatus(userId, Order.OrderStatus.IN_PROGRESS);
        BigDecimal totalSpent = orderRepository.sumTotalAmountByUserIdAndCompleted(userId);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        return OrderStatsResponse.builder()
            .totalOrders(totalOrders)
            .pendingOrders(pendingOrders)
            .inTransitOrders(inTransitOrders)
            .totalSpent(totalSpent)
            .build();
    }

    /**
     * 최근 주문 조회
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders(Long userId, int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return orders.stream()
            .map(this::convertToOrderResponse)
            .collect(Collectors.toList());
    }

    // Inner class for order statistics
    @lombok.Data
    @lombok.Builder
    public static class OrderStatsResponse {
        private long totalOrders;
        private long pendingOrders;
        private long inTransitOrders;
        private BigDecimal totalSpent;
    }
}