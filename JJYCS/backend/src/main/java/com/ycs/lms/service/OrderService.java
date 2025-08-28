package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import com.ycs.lms.entity.User;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BusinessLogicService businessLogicService;
    private final OrderBusinessRuleService orderBusinessRuleService;
    // private final NotificationService notificationService; // TODO: Re-enable after fixing NotificationService
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // CBM 임계값 (29.0 m³)
    private static final BigDecimal CBM_THRESHOLD = new BigDecimal("29.0");
    // THB 임계값 (1,500 THB)
    private static final BigDecimal THB_THRESHOLD = new BigDecimal("1500.0");
    
    public Order createOrder(CreateOrderRequest request) {
        // 먼저 데이터베이스에 모든 엔티티를 저장
        Order savedOrder = createOrderEntities(request);
        
        // 별도 트랜잭션에서 비즈니스 규칙 적용
        return applyBusinessRulesPostTransaction(savedOrder.getId());
    }
    
    /**
     * 주문과 관련 엔티티들을 데이터베이스에 저장
     */
    @Transactional
    public Order createOrderEntities(CreateOrderRequest request) {
        // 사용자 확인
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User account is not active");
        }
        
        // 주문 생성
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setStatus(Order.OrderStatus.RECEIVED);
        
        // shippingType 설정 (기본값: SEA)
        String shippingType = request.getShippingType();
        if (shippingType == null || shippingType.trim().isEmpty()) {
            shippingType = "SEA"; // 기본값
        }
        order.setShippingType(Order.ShippingType.valueOf(shippingType.toUpperCase()));
        
        order.setCountry(request.getCountry());
        order.setPostalCode(request.getPostalCode());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setRecipientAddress(request.getRecipientAddress());
        order.setRecipientPostalCode(request.getRecipientPostalCode());
        order.setSpecialRequests(request.getSpecialRequests());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // 회원코드 미기재 체크
        if (user.getMemberCode() == null || user.getMemberCode().trim().isEmpty()) {
            order.setNoMemberCode(true);
            log.warn("Order {} created without member code for user {}", order.getOrderNumber(), user.getEmail());
        } else {
            order.setNoMemberCode(false);
        }
        
        // 박스별 CBM 계산 및 총합 계산
        BigDecimal totalCbm = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        // 박스에서 CBM 계산
        if (request.getOrderBoxes() != null && !request.getOrderBoxes().isEmpty()) {
            for (CreateOrderRequest.OrderBoxRequest boxRequest : request.getOrderBoxes()) {
                // CBM 계산: (W × H × D) / 1,000,000
                if (boxRequest.getWidthCm() != null && boxRequest.getHeightCm() != null && boxRequest.getDepthCm() != null) {
                    BigDecimal boxCbm = calculateCbm(
                        boxRequest.getWidthCm(), 
                        boxRequest.getHeightCm(), 
                        boxRequest.getDepthCm(),
                        1 // 박스는 개수가 1개
                    );
                    totalCbm = totalCbm.add(boxCbm);
                }
            }
        }
        
        // 아이템별 무게 계산
        if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                if (itemRequest.getWeight() != null && itemRequest.getQuantity() != null) {
                    BigDecimal itemWeight = itemRequest.getWeight().multiply(new BigDecimal(itemRequest.getQuantity()));
                    totalWeight = totalWeight.add(itemWeight);
                }
            }
        }
        
        order.setTotalCbm(totalCbm);
        order.setTotalWeight(totalWeight);
        
        // 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        // 주문 항목 저장
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setHsCode(itemRequest.getHsCode());
            orderItem.setDescription(itemRequest.getDescription());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setWeight(itemRequest.getWeight());
            orderItem.setWidth(itemRequest.getWidth());
            orderItem.setHeight(itemRequest.getHeight());
            orderItem.setDepth(itemRequest.getDepth());
            orderItem.setUnitPrice(itemRequest.getUnitPrice());
            orderItem.setTotalPrice(itemRequest.getUnitPrice() != null ? 
                itemRequest.getUnitPrice().multiply(new BigDecimal(itemRequest.getQuantity())) : null);
            
            // 개별 항목 CBM은 박스에서 계산됨
            orderItem.setCbm(BigDecimal.ZERO); // 초기값
            orderItem.setCreatedAt(LocalDateTime.now());
            
            savedOrder.getItems().add(orderItem);
        }
        
        // 박스 정보 저장 (있는 경우)
        if (request.getOrderBoxes() != null && !request.getOrderBoxes().isEmpty()) {
            for (CreateOrderRequest.OrderBoxRequest boxRequest : request.getOrderBoxes()) {
                OrderBox orderBox = new OrderBox();
                orderBox.setOrder(savedOrder);
                orderBox.setBoxNumber("BOX-" + System.currentTimeMillis());
                orderBox.setWidth(boxRequest.getWidthCm());
                orderBox.setHeight(boxRequest.getHeightCm());
                orderBox.setDepth(boxRequest.getDepthCm());
                orderBox.setWeight(boxRequest.getWeightKg() != null ? boxRequest.getWeightKg() : BigDecimal.ZERO);
                
                savedOrder.getBoxes().add(orderBox);
            }
        }
        
        // 모든 엔티티 저장 완료
        entityManager.flush();
        
        log.info("Order entities created successfully for order: {}", savedOrder.getOrderNumber());
        return savedOrder;
    }
    
    /**
     * 별도 트랜잭션에서 비즈니스 규칙 적용
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order applyBusinessRulesPostTransaction(Long orderId) {
        // 완전히 새로운 트랜잭션에서 주문을 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        log.info("Applying business rules for order: {} with {} items", 
            order.getOrderNumber(), order.getOrderItems().size());
        
        // 비즈니스 규칙 적용
        OrderBusinessRuleService.OrderBusinessRuleResult ruleResult = 
            orderBusinessRuleService.applyBusinessRules(order);
        
        // 규칙 적용 결과를 주문에 반영
        order.setUpdatedAt(LocalDateTime.now());
        
        // 최종 저장
        Order finalOrder = orderRepository.save(order);
        
        log.info("Business rules applied successfully for order: {} (CBM: {}, THB: {}, Warnings: {})", 
            finalOrder.getOrderNumber(), ruleResult.getTotalCbm(), ruleResult.getTotalThbValue(), 
            ruleResult.hasWarnings() ? ruleResult.getWarnings().size() : 0);
        
        return finalOrder;
    }
    
    /**
     * 비즈니스 룰 검증 (주문 생성 전 미리보기)
     */
    public OrderBusinessRuleService.OrderBusinessRuleResult validateBusinessRules(CreateOrderRequest request) {
        // 임시 주문 객체 생성
        Order tempOrder = new Order();
        
        // 사용자 설정
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        tempOrder.setUser(user);
        
        // 주문 아이템들을 임시로 추가하여 검증
        tempOrder.setOrderItems(new ArrayList<>());
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem item = new OrderItem();
            item.setHsCode(itemRequest.getHsCode());
            item.setDescription(itemRequest.getDescription());
            item.setEnglishName(itemRequest.getEnglishName());
            item.setQuantity(itemRequest.getQuantity());
            item.setWeight(itemRequest.getWeight());
            item.setWidth(itemRequest.getWidth());
            item.setHeight(itemRequest.getHeight());
            item.setDepth(itemRequest.getDepth());
            item.setUnitPrice(itemRequest.getUnitPrice());
            
            // 관세 정보 설정
            if (itemRequest.getBasicTariffRate() != null) {
                item.setBasicTariffRate(itemRequest.getBasicTariffRate());
            }
            if (itemRequest.getWtoTariffRate() != null) {
                item.setWtoTariffRate(itemRequest.getWtoTariffRate());
            }
            if (itemRequest.getSpecialTariffRate() != null) {
                item.setSpecialTariffRate(itemRequest.getSpecialTariffRate());
            }
            if (itemRequest.getAppliedTariffRate() != null) {
                item.setAppliedTariffRate(itemRequest.getAppliedTariffRate());
            }
            if (itemRequest.getCustomsDutyAmount() != null) {
                item.setCustomsDutyAmount(itemRequest.getCustomsDutyAmount());
            }
            if (itemRequest.getTotalAmountWithDuty() != null) {
                item.setTotalAmountWithDuty(itemRequest.getTotalAmountWithDuty());
            }
            tempOrder.getOrderItems().add(item);
        }
        
        // 박스 정보가 있다면 설정
        if (request.getOrderBoxes() != null) {
            tempOrder.setOrderBoxes(new ArrayList<>());
            for (CreateOrderRequest.OrderBoxRequest boxRequest : request.getOrderBoxes()) {
                OrderBox box = new OrderBox();
                box.setWidth(boxRequest.getWidthCm());
                box.setHeight(boxRequest.getHeightCm());
                box.setDepth(boxRequest.getDepthCm());
                tempOrder.getOrderBoxes().add(box);
            }
        }
        
        return orderBusinessRuleService.applyBusinessRules(tempOrder);
    }
    
    private BigDecimal calculateCbm(BigDecimal width, BigDecimal height, BigDecimal depth, Integer quantity) {
        // null 체크
        if (width == null || height == null || depth == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        
        // CBM = (W × H × D × 수량) / 1,000,000
        BigDecimal volume = width.multiply(height).multiply(depth).multiply(new BigDecimal(quantity));
        return volume.divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
    }
    
    private String generateOrderNumber() {
        // YCS-YYMMDD-XXX 형식
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long orderCount = orderRepository.count() + 1;
        return String.format("YCS-%s-%03d", datePrefix, orderCount);
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 주문 정보 수정
     * @param orderId 주문 ID
     * @param updateRequest 수정 요청 데이터
     * @return 수정된 주문
     */
    public Order updateOrder(Long orderId, UpdateOrderRequest updateRequest) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // 수정 가능한 상태인지 확인
        if (!isOrderModifiable(order)) {
            throw new IllegalStateException("Order cannot be modified in current status: " + order.getStatus());
        }
        
        // 수취인 정보 업데이트
        if (updateRequest.getRecipientName() != null) {
            order.setRecipientName(updateRequest.getRecipientName());
        }
        if (updateRequest.getRecipientPhone() != null) {
            order.setRecipientPhone(updateRequest.getRecipientPhone());
        }
        if (updateRequest.getRecipientAddress() != null) {
            order.setRecipientAddress(updateRequest.getRecipientAddress());
        }
        if (updateRequest.getRecipientPostalCode() != null) {
            order.setRecipientPostalCode(updateRequest.getRecipientPostalCode());
        }
        
        // 배송 정보 업데이트
        if (updateRequest.getShippingType() != null) {
            order.setShippingType(Order.ShippingType.valueOf(updateRequest.getShippingType()));
        }
        if (updateRequest.getCountry() != null) {
            order.setCountry(updateRequest.getCountry());
        }
        
        // 특별 요청사항 업데이트
        if (updateRequest.getSpecialRequests() != null) {
            order.setSpecialRequests(updateRequest.getSpecialRequests());
        }
        
        // 업데이트 시간 기록
        order.setUpdatedAt(LocalDateTime.now());
        
        // 변경사항 저장
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order updated successfully: {} by user", orderId);
        
        return updatedOrder;
    }
    
    public Order updateOrderStatus(Long orderId, String newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            
        try {
            Order.OrderStatus status = Order.OrderStatus.valueOf(newStatus);
            Order.OrderStatus previousStatus = order.getStatus();
            
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            
            Order savedOrder = orderRepository.save(order);
            
            // 주문 완료 시 처리 (파트너 커미션 기능 제거됨)
            
            // 주문 상태 변경 알림 발송 - TODO: Re-enable after fixing NotificationService
            /*
            try {
                NotificationTemplate.TriggerEvent event = mapStatusToNotificationEvent(status);
                if (event != null) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("previousStatus", previousStatus.toString());
                    variables.put("newStatus", status.toString());
                    variables.put("changeReason", reason);
                    
                    notificationService.triggerOrderNotification(event, savedOrder, variables);
                }
            } catch (Exception e) {
                log.error("Failed to send status change notification for order: {}", 
                    savedOrder.getOrderNumber(), e);
            }
            */
            
            // TODO: OrderHistory 기록 추가 가능
            log.info("Order {} status updated from {} to {} by admin. Reason: {}", 
                order.getOrderNumber(), previousStatus, status, reason);
            
            return savedOrder;
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
    }
    
    /**
     * 주문 상태를 알림 이벤트로 매핑
     */
    private String mapStatusToNotificationEvent(Order.OrderStatus status) {
        switch (status) {
            case PROCESSING:
                return "ORDER_PREPARING";
            case SHIPPED:
                return "ORDER_SHIPPED";
            case IN_TRANSIT:
                return "ORDER_IN_TRANSIT";
            case DELIVERED:
                return "ORDER_DELIVERED";
            case CANCELLED:
                return "ORDER_CANCELLED";
            default:
                return null; // 알림이 필요하지 않은 상태들
        }
    }
    
    /**
     * 경고 메시지 생성 (기존 메서드 - 이미 존재함)
     */
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
    
    // DTO 클래스
    public static class CreateOrderRequest {
        private Long userId;
        private String shippingType; // "SEA" or "AIR"
        private String country;
        private String postalCode;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private String recipientPostalCode;
        private String specialRequests;
        private List<OrderItemRequest> orderItems;
        private List<OrderBoxRequest> orderBoxes;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getShippingType() { return shippingType; }
        public void setShippingType(String shippingType) { this.shippingType = shippingType; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        public String getRecipientAddress() { return recipientAddress; }
        public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
        public String getRecipientPostalCode() { return recipientPostalCode; }
        public void setRecipientPostalCode(String recipientPostalCode) { this.recipientPostalCode = recipientPostalCode; }
        public String getSpecialRequests() { return specialRequests; }
        public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
        public List<OrderItemRequest> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
        public List<OrderBoxRequest> getOrderBoxes() { return orderBoxes; }
        public void setOrderBoxes(List<OrderBoxRequest> orderBoxes) { this.orderBoxes = orderBoxes; }
        
        public static class OrderItemRequest {
            private String hsCode;
            private String description;
            private Integer quantity;
            private BigDecimal weight;
            private BigDecimal width;
            private BigDecimal height;
            private BigDecimal depth;
            private BigDecimal unitPrice; // THB
            
            // HS Code 관련 정보
            private String englishName;
            
            // 관세율 정보
            private BigDecimal basicTariffRate;
            private BigDecimal wtoTariffRate;
            private BigDecimal specialTariffRate;
            private BigDecimal appliedTariffRate;
            
            // 관세 계산 결과
            private BigDecimal customsDutyAmount;
            private BigDecimal totalAmountWithDuty;
            
            // Getters and Setters
            public String getHsCode() { return hsCode; }
            public void setHsCode(String hsCode) { this.hsCode = hsCode; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public Integer getQuantity() { return quantity; }
            public void setQuantity(Integer quantity) { this.quantity = quantity; }
            public BigDecimal getWeight() { return weight; }
            public void setWeight(BigDecimal weight) { this.weight = weight; }
            public BigDecimal getWidth() { return width; }
            public void setWidth(BigDecimal width) { this.width = width; }
            public BigDecimal getHeight() { return height; }
            public void setHeight(BigDecimal height) { this.height = height; }
            public BigDecimal getDepth() { return depth; }
            public void setDepth(BigDecimal depth) { this.depth = depth; }
            public BigDecimal getUnitPrice() { return unitPrice; }
            public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
            
            public String getEnglishName() { return englishName; }
            public void setEnglishName(String englishName) { this.englishName = englishName; }
            
            public BigDecimal getBasicTariffRate() { return basicTariffRate; }
            public void setBasicTariffRate(BigDecimal basicTariffRate) { this.basicTariffRate = basicTariffRate; }
            
            public BigDecimal getWtoTariffRate() { return wtoTariffRate; }
            public void setWtoTariffRate(BigDecimal wtoTariffRate) { this.wtoTariffRate = wtoTariffRate; }
            
            public BigDecimal getSpecialTariffRate() { return specialTariffRate; }
            public void setSpecialTariffRate(BigDecimal specialTariffRate) { this.specialTariffRate = specialTariffRate; }
            
            public BigDecimal getAppliedTariffRate() { return appliedTariffRate; }
            public void setAppliedTariffRate(BigDecimal appliedTariffRate) { this.appliedTariffRate = appliedTariffRate; }
            
            public BigDecimal getCustomsDutyAmount() { return customsDutyAmount; }
            public void setCustomsDutyAmount(BigDecimal customsDutyAmount) { this.customsDutyAmount = customsDutyAmount; }
            
            public BigDecimal getTotalAmountWithDuty() { return totalAmountWithDuty; }
            public void setTotalAmountWithDuty(BigDecimal totalAmountWithDuty) { this.totalAmountWithDuty = totalAmountWithDuty; }
        }
        
        public static class OrderBoxRequest {
            private BigDecimal widthCm;
            private BigDecimal heightCm;
            private BigDecimal depthCm;
            private BigDecimal weightKg;
            
            public BigDecimal getWidthCm() { return widthCm; }
            public void setWidthCm(BigDecimal widthCm) { this.widthCm = widthCm; }
            public BigDecimal getHeightCm() { return heightCm; }
            public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
            public BigDecimal getDepthCm() { return depthCm; }
            public void setDepthCm(BigDecimal depthCm) { this.depthCm = depthCm; }
            public BigDecimal getWeightKg() { return weightKg; }
            public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
        }
    }
    
    /**
     * 주문 수정 요청 DTO
     */
    public static class UpdateOrderRequest {
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private String recipientPostalCode;
        private String shippingType;
        private String country;
        private String specialRequests;
        
        // Getters and Setters
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        
        public String getRecipientAddress() { return recipientAddress; }
        public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
        
        public String getRecipientPostalCode() { return recipientPostalCode; }
        public void setRecipientPostalCode(String recipientPostalCode) { this.recipientPostalCode = recipientPostalCode; }
        
        public String getShippingType() { return shippingType; }
        public void setShippingType(String shippingType) { this.shippingType = shippingType; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getSpecialRequests() { return specialRequests; }
        public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    }
    
    /**
     * 주문 접근 권한 확인
     * @param order 주문
     * @param user 사용자
     * @return 접근 가능 여부
     */
    public boolean isOrderAccessible(Order order, User user) {
        if (order == null || user == null) {
            return false;
        }
        
        // 관리자는 모든 주문 접근 가능
        if (User.UserType.ADMIN.equals(user.getUserType())) {
            return true;
        }
        
        // 창고 담당자는 모든 주문 접근 가능
        if (User.UserType.WAREHOUSE.equals(user.getUserType())) {
            return true;
        }
        
        // 파트너는 자신과 관련된 주문만 접근 가능
        if (User.UserType.PARTNER.equals(user.getUserType())) {
            // TODO: 파트너와 주문 연결 로직 구현 필요
            return order.getUser().getId().equals(user.getId());
        }
        
        // 일반 사용자와 기업 사용자는 자신의 주문만 접근 가능
        return order.getUser().getId().equals(user.getId());
    }
    
    /**
     * 주문 소유자 확인
     * @param order 주문
     * @param userId 사용자 ID
     * @return 소유자 여부
     */
    public boolean isOrderOwner(Order order, Long userId) {
        if (order == null || userId == null) {
            return false;
        }
        return order.getUser().getId().equals(userId);
    }
    
    /**
     * 주문 취소 가능 여부 확인
     * @param order 주문
     * @return 취소 가능 여부
     */
    public boolean isOrderCancellable(Order order) {
        if (order == null) {
            return false;
        }
        
        // 취소 가능한 상태 목록
        List<Order.OrderStatus> cancellableStatuses = Arrays.asList(
            Order.OrderStatus.RECEIVED,
            Order.OrderStatus.PENDING,
            Order.OrderStatus.PROCESSING
        );
        
        return cancellableStatuses.contains(order.getStatus());
    }
    
    /**
     * 주문 수정 가능 여부 확인
     * @param order 주문
     * @return 수정 가능 여부
     */
    public boolean isOrderModifiable(Order order) {
        if (order == null) {
            return false;
        }
        
        // 수정 가능한 상태 목록
        List<Order.OrderStatus> modifiableStatuses = Arrays.asList(
            Order.OrderStatus.RECEIVED,
            Order.OrderStatus.PENDING
        );
        
        return modifiableStatuses.contains(order.getStatus());
    }
}