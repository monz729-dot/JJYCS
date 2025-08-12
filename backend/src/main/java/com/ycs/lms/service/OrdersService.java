package com.ycs.lms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycs.lms.dto.OrderCreateRequest;
import com.ycs.lms.dto.OrderResponse;
import com.ycs.lms.dto.PageResponse;
import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import com.ycs.lms.entity.User;
import com.ycs.lms.exception.BadRequestException;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.mapper.OrderMapper;
import com.ycs.lms.mapper.UserMapper;
import com.ycs.lms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final BusinessRuleService businessRuleService;
    private final ExternalApiService externalApiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        User user = userMapper.findById(currentUser.getId())
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 1. 주문 기본 정보 생성
        Order order = createOrderEntity(request, user);
        
        // 2. 상품 정보 생성 및 총액 계산
        List<OrderItem> items = createOrderItems(request.getItems(), order.getId());
        BigDecimal totalAmount = calculateTotalAmount(items);
        
        // 3. 박스 정보 생성 및 CBM 계산
        List<OrderBox> boxes = createOrderBoxes(request.getBoxes(), order.getId());
        BigDecimal totalCbm = calculateTotalCbm(boxes);
        
        // 4. 비즈니스 룰 적용
        BusinessRuleService.OrderValidationResult validation = businessRuleService.validateOrder(
            totalCbm, totalAmount, request.getItems().get(0).getCurrency(), 
            user.getMemberCode(), "active".equals(user.getStatus())
        );
        
        // 5. 주문 정보 업데이트
        updateOrderWithBusinessRules(order, totalAmount, totalCbm, validation);
        
        // 6. DB 저장
        String orderCode = orderMapper.generateOrderCode();
        order.setOrderCode(orderCode);
        orderMapper.insertOrder(order);
        
        // 상품 저장
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setOrderId(order.getId());
            items.get(i).setItemOrder(i + 1);
            orderMapper.insertOrderItem(items.get(i));
        }
        
        // 박스 저장
        for (int i = 0; i < boxes.size(); i++) {
            OrderBox box = boxes.get(i);
            box.setOrderId(order.getId());
            box.setBoxNumber(i + 1);
            box.setLabelCode(generateBoxLabelCode(orderCode, i + 1));
            box.setStatus("created");
            
            // JSON 변환
            try {
                String itemIdsJson = objectMapper.writeValueAsString(box.getItemIds());
                box.setItemIdsJson(itemIdsJson);
            } catch (Exception e) {
                log.error("Failed to serialize item IDs for box", e);
            }
            
            orderMapper.insertOrderBox(box);
        }
        
        // 7. 비동기 외부 API 검증 (백그라운드)
        validateExternalCodes(items);
        
        // 8. 응답 생성
        OrderResponse response = buildOrderResponse(order, items, boxes, user);
        
        // 경고 메시지 추가
        for (BusinessRuleService.Warning warning : validation.getWarnings()) {
            response.addWarning(warning.getType(), warning.getMessage(), null);
        }
        
        log.info("Order created successfully: orderId={}, orderCode={}, totalCbm={}, totalAmount={}", 
                order.getId(), order.getOrderCode(), totalCbm, totalAmount);
        
        return response;
    }

    public OrderResponse getOrder(Long orderId) {
        UserPrincipal currentUser = getCurrentUser();
        Order order = orderMapper.findOrderById(orderId);
        
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        
        // 권한 체크: 본인 주문이거나 관리자/창고 직원
        if (!canAccessOrder(currentUser, order)) {
            throw new BadRequestException("해당 주문에 접근할 권한이 없습니다.");
        }
        
        User user = userMapper.findById(order.getUserId()).orElse(null);
        return buildOrderResponse(order, order.getItems(), order.getBoxes(), user);
    }

    public PageResponse<OrderResponse> getOrders(String status, String startDate, String endDate, Pageable pageable) {
        UserPrincipal currentUser = getCurrentUser();
        Long userId = isAdminOrWarehouse(currentUser) ? null : currentUser.getId();
        
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        List<Order> orders = orderMapper.findOrders(userId, status, startDate, endDate, 
                                                   pageable.getPageSize(), offset);
        
        long total = orderMapper.countOrders(userId, status, startDate, endDate);
        
        List<OrderResponse> responses = orders.stream()
            .map(order -> {
                User user = userMapper.findById(order.getUserId()).orElse(null);
                return buildOrderResponseSummary(order, user);
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(responses, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderCreateRequest request) {
        UserPrincipal currentUser = getCurrentUser();
        Order existingOrder = orderMapper.findOrderById(orderId);
        
        if (existingOrder == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        
        if (!canModifyOrder(currentUser, existingOrder)) {
            throw new BadRequestException("해당 주문을 수정할 권한이 없습니다.");
        }
        
        // 수정 가능한 상태인지 체크
        if (!isModifiableStatus(existingOrder.getStatus())) {
            throw new BadRequestException("현재 상태에서는 주문을 수정할 수 없습니다.");
        }
        
        // TODO: 주문 수정 로직 구현
        // 현재는 새로운 주문 생성과 동일한 로직을 사용
        
        return getOrder(orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        UserPrincipal currentUser = getCurrentUser();
        Order order = orderMapper.findOrderById(orderId);
        
        if (order == null) {
            throw new NotFoundException("주문을 찾을 수 없습니다.");
        }
        
        if (!canModifyOrder(currentUser, order)) {
            throw new BadRequestException("해당 주문을 취소할 권한이 없습니다.");
        }
        
        if (!isCancellableStatus(order.getStatus())) {
            throw new BadRequestException("현재 상태에서는 주문을 취소할 수 없습니다.");
        }
        
        orderMapper.cancelOrder(orderId);
        log.info("Order cancelled: orderId={}, cancelledBy={}", orderId, currentUser.getId());
    }

    private Order createOrderEntity(OrderCreateRequest request, User user) {
        return Order.builder()
            .userId(user.getId())
            .status(businessRuleService.determineOrderStatus(user.getMemberCode(), "active".equals(user.getStatus())))
            .orderType(request.getShipping().getPreferredType())
            .recipientName(request.getRecipient().getName())
            .recipientPhone(request.getRecipient().getPhone())
            .recipientAddress(request.getRecipient().getAddress())
            .recipientZipCode(request.getRecipient().getZipCode())
            .recipientCountry(request.getRecipient().getCountry())
            .urgency(request.getShipping().getUrgency())
            .needsRepacking(request.getShipping().isNeedsRepacking())
            .specialInstructions(request.getShipping().getSpecialInstructions())
            .currency(request.getItems().get(0).getCurrency()) // 첫 번째 아이템 통화 사용
            .paymentMethod(request.getPayment().getMethod())
            .paymentStatus("pending")
            .createdBy(user.getId())
            .estimatedDeliveryDate(LocalDate.now().plusDays("air".equals(request.getShipping().getPreferredType()) ? 7 : 14))
            .build();
    }

    private List<OrderItem> createOrderItems(List<OrderCreateRequest.ItemInfo> itemInfos, Long orderId) {
        return itemInfos.stream()
            .map(itemInfo -> OrderItem.builder()
                .orderId(orderId)
                .name(itemInfo.getName())
                .description(itemInfo.getDescription())
                .category(itemInfo.getCategory())
                .quantity(itemInfo.getQuantity())
                .unitWeight(itemInfo.getWeight())
                .unitPrice(itemInfo.getAmount().divide(BigDecimal.valueOf(itemInfo.getQuantity()), 2, BigDecimal.ROUND_HALF_UP))
                .totalAmount(itemInfo.getAmount())
                .currency(itemInfo.getCurrency())
                .hsCode(itemInfo.getHsCode())
                .emsCode(itemInfo.getEmsCode())
                .brand(itemInfo.getBrand())
                .model(itemInfo.getModel())
                .restricted(false)
                .build())
            .collect(Collectors.toList());
    }

    private List<OrderBox> createOrderBoxes(List<OrderCreateRequest.BoxInfo> boxInfos, Long orderId) {
        return boxInfos.stream()
            .map(boxInfo -> {
                BigDecimal cbm = businessRuleService.calculateCBM(boxInfo.getWidth(), boxInfo.getHeight(), boxInfo.getDepth());
                
                // Convert item indexes to item IDs (for now, use indexes as IDs)
                List<Long> itemIds = boxInfo.getItemIndexes().stream()
                    .map(index -> (long) (index + 1))
                    .collect(Collectors.toList());
                
                return OrderBox.builder()
                    .orderId(orderId)
                    .widthCm(boxInfo.getWidth())
                    .heightCm(boxInfo.getHeight())
                    .depthCm(boxInfo.getDepth())
                    .cbmM3(cbm)
                    .weightKg(boxInfo.getWeight())
                    .itemIds(itemIds)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCbm(List<OrderBox> boxes) {
        return boxes.stream()
            .map(OrderBox::getCbmM3)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateOrderWithBusinessRules(Order order, BigDecimal totalAmount, BigDecimal totalCbm,
                                            BusinessRuleService.OrderValidationResult validation) {
        order.setTotalAmount(totalAmount);
        order.setTotalCbmM3(totalCbm);
        order.setRequiresExtraRecipient(validation.isRequiresExtraRecipient());
        
        if (validation.isConvertToAir()) {
            order.setOrderType("air");
            order.setEstimatedDeliveryDate(LocalDate.now().plusDays(7));
        }
        
        if (validation.isHasDelayedProcessing()) {
            order.setStatus("delayed");
        }
    }

    private String generateBoxLabelCode(String orderCode, int boxNumber) {
        return orderCode + "-" + String.format("%02d", boxNumber);
    }

    private void validateExternalCodes(List<OrderItem> items) {
        items.parallelStream().forEach(item -> {
            if (item.getEmsCode() != null) {
                CompletableFuture<ExternalApiService.EmsValidationResult> emsValidation = 
                    externalApiService.validateEmsCodeAsync(item.getEmsCode());
                
                emsValidation.thenAccept(result -> {
                    if (!result.isValid()) {
                        log.warn("Invalid EMS code detected: itemId={}, code={}, reason={}", 
                                item.getId(), item.getEmsCode(), result.getMessage());
                        // TODO: 알림 시스템으로 전송
                    }
                });
            }
            
            if (item.getHsCode() != null) {
                CompletableFuture<ExternalApiService.HsCodeValidationResult> hsValidation = 
                    externalApiService.validateHsCodeAsync(item.getHsCode());
                
                hsValidation.thenAccept(result -> {
                    if (!result.isValid()) {
                        log.warn("Invalid HS code detected: itemId={}, code={}, reason={}", 
                                item.getId(), item.getHsCode(), result.getMessage());
                        // TODO: 알림 시스템으로 전송
                    }
                });
            }
        });
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> items, List<OrderBox> boxes, User user) {
        return OrderResponse.builder()
            .orderId(order.getId())
            .orderCode(order.getOrderCode())
            .status(order.getStatus())
            .orderType(order.getOrderType())
            .totalCBM(order.getTotalCbmM3())
            .totalWeight(calculateTotalWeight(items))
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .requiresExtraRecipient(order.isRequiresExtraRecipient())
            .hasMemberCodeIssue(user != null && (user.getMemberCode() == null || user.getMemberCode().isEmpty()))
            .recipient(OrderResponse.RecipientInfo.builder()
                .name(order.getRecipientName())
                .phone(order.getRecipientPhone())
                .address(order.getRecipientAddress())
                .zipCode(order.getRecipientZipCode())
                .country(order.getRecipientCountry())
                .build())
            .items(mapOrderItems(items))
            .boxes(mapOrderBoxes(boxes))
            .shipping(OrderResponse.ShippingInfo.builder()
                .urgency(order.getUrgency())
                .needsRepacking(order.isNeedsRepacking())
                .specialInstructions(order.getSpecialInstructions())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .build())
            .estimatedDelivery(order.getEstimatedDeliveryDate())
            .actualDelivery(order.getActualDeliveryDate())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .user(user != null ? OrderResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .memberCode(user.getMemberCode())
                .role(user.getRole())
                .build() : null)
            .timeline(buildTimeline(order))
            .build();
    }

    private OrderResponse buildOrderResponseSummary(Order order, User user) {
        return OrderResponse.builder()
            .orderId(order.getId())
            .orderCode(order.getOrderCode())
            .status(order.getStatus())
            .orderType(order.getOrderType())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .recipient(OrderResponse.RecipientInfo.builder()
                .name(order.getRecipientName())
                .country(order.getRecipientCountry())
                .build())
            .createdAt(order.getCreatedAt())
            .estimatedDelivery(order.getEstimatedDeliveryDate())
            .user(user != null ? OrderResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .memberCode(user.getMemberCode())
                .role(user.getRole())
                .build() : null)
            .build();
    }

    // Helper methods
    private UserPrincipal getCurrentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean canAccessOrder(UserPrincipal user, Order order) {
        return order.getUserId().equals(user.getId()) || 
               isAdminOrWarehouse(user);
    }

    private boolean canModifyOrder(UserPrincipal user, Order order) {
        return order.getUserId().equals(user.getId()) || 
               user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isAdminOrWarehouse(UserPrincipal user) {
        return user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                             auth.getAuthority().equals("ROLE_WAREHOUSE"));
    }

    private boolean isModifiableStatus(String status) {
        return "requested".equals(status) || "confirmed".equals(status) || "delayed".equals(status);
    }

    private boolean isCancellableStatus(String status) {
        return !"shipped".equals(status) && !"delivered".equals(status) && !"cancelled".equals(status);
    }

    private BigDecimal calculateTotalWeight(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getUnitWeight().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderResponse.ItemInfo> mapOrderItems(List<OrderItem> items) {
        if (items == null) return new ArrayList<>();
        
        return items.stream()
            .map(item -> OrderResponse.ItemInfo.builder()
                .itemId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .quantity(item.getQuantity())
                .unitWeight(item.getUnitWeight())
                .unitPrice(item.getUnitPrice())
                .totalAmount(item.getTotalAmount())
                .currency(item.getCurrency())
                .hsCode(item.getHsCode())
                .emsCode(item.getEmsCode())
                .brand(item.getBrand())
                .model(item.getModel())
                .restricted(item.isRestricted())
                .restrictionNote(item.getRestrictionNote())
                .build())
            .collect(Collectors.toList());
    }

    private List<OrderResponse.BoxInfo> mapOrderBoxes(List<OrderBox> boxes) {
        if (boxes == null) return new ArrayList<>();
        
        return boxes.stream()
            .map(box -> OrderResponse.BoxInfo.builder()
                .boxId(box.getId())
                .boxNumber(box.getBoxNumber())
                .labelCode(box.getLabelCode())
                .qrCodeUrl(box.getQrCodeUrl())
                .width(box.getWidthCm())
                .height(box.getHeightCm())
                .depth(box.getDepthCm())
                .cbm(box.getCbmM3())
                .weight(box.getWeightKg())
                .status(box.getStatus())
                .trackingNumber(box.getTrackingNumber())
                .carrier(box.getCarrier())
                .shippedDate(box.getShippedDate())
                .itemIds(box.getItemIds())
                .build())
            .collect(Collectors.toList());
    }

    private List<OrderResponse.StatusTimeline> buildTimeline(Order order) {
        List<OrderResponse.StatusTimeline> timeline = new ArrayList<>();
        
        timeline.add(OrderResponse.StatusTimeline.builder()
            .status("requested")
            .timestamp(order.getCreatedAt())
            .note("주문 접수")
            .build());
        
        // TODO: 실제 상태 변경 로그에서 조회
        
        return timeline;
    }
}