package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.User;
import com.ycs.lms.repository.BillingRepository;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BillingRepository billingRepository;
    
    /**
     * 사용자 대시보드 메인 데이터
     */
    public UserDashboard getUserDashboard(Long userId) {
        try {
            log.info("Getting user dashboard for user: {}", userId);
            
            // 사용자 확인
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return UserDashboard.error("사용자를 찾을 수 없습니다.");
            }
            
            User user = userOpt.get();
            
            // 사용자의 주문 목록 (최근 순)
            List<Order> recentOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                                                     .stream()
                                                     .limit(10)
                                                     .collect(Collectors.toList());
            
            // 상태별 주문 카운트
            Map<String, Long> statusCounts = getOrderStatusCounts(userId);
            
            // 결제 관련 정보
            Map<String, Object> paymentInfo = getPaymentInfo(userId);
            
            // 최근 배송 추적 정보
            List<Order> trackingOrders = recentOrders.stream()
                                                   .filter(order -> order.getTrackingNumber() != null)
                                                   .limit(5)
                                                   .collect(Collectors.toList());
            
            // 월별 주문 통계 (최근 6개월)
            List<Map<String, Object>> monthlyStats = getMonthlyOrderStats(userId);
            
            // 알림/경고 메시지
            List<String> alerts = generateUserAlerts(recentOrders);
            
            return UserDashboard.success(
                user,
                recentOrders,
                statusCounts,
                paymentInfo,
                trackingOrders,
                monthlyStats,
                alerts
            );
            
        } catch (Exception e) {
            log.error("Error getting user dashboard: {}", e.getMessage(), e);
            return UserDashboard.error("대시보드 데이터 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 상세 배송 추적
     */
    public TrackingResult getOrderTracking(String orderNumber, Long userId) {
        try {
            log.info("Getting tracking info for order: {} by user: {}", orderNumber, userId);
            
            Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
            if (orderOpt.isEmpty()) {
                return TrackingResult.error("주문을 찾을 수 없습니다.");
            }
            
            Order order = orderOpt.get();
            
            // 사용자 권한 확인 (본인 주문만 조회 가능)
            if (!order.getUser().getId().equals(userId)) {
                return TrackingResult.error("해당 주문에 대한 권한이 없습니다.");
            }
            
            // 배송 추적 단계 생성
            List<TrackingStep> trackingSteps = generateTrackingSteps(order);
            
            // 예상 배송일 계산
            String estimatedDelivery = calculateEstimatedDelivery(order);
            
            // EMS 배송 상태 조회 (실제로는 외부 API 호출)
            String emsStatus = getEMSTrackingStatus(order.getTrackingNumber());
            
            return TrackingResult.success(
                order,
                trackingSteps,
                estimatedDelivery,
                emsStatus
            );
            
        } catch (Exception e) {
            log.error("Error getting order tracking: {}", e.getMessage(), e);
            return TrackingResult.error("배송 추적 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자 주문 목록 (페이징)
     */
    public OrderListResult getUserOrders(Long userId, int page, int size, String status, String sortBy) {
        try {
            log.info("Getting user orders: userId={}, page={}, size={}, status={}, sortBy={}", 
                    userId, page, size, status, sortBy);
            
            // 정렬 조건 설정
            Sort sort = Sort.by(Sort.Direction.DESC, getSortField(sortBy));
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Order> ordersPage;
            
            if (status != null && !status.equals("ALL")) {
                // 특정 상태 필터링
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                ordersPage = orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable);
            } else {
                // 전체 주문
                ordersPage = orderRepository.findByUserId(userId, pageable);
            }
            
            return OrderListResult.success(ordersPage);
            
        } catch (Exception e) {
            log.error("Error getting user orders: {}", e.getMessage(), e);
            return OrderListResult.error("주문 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주문 통계 정보
     */
    public OrderStatsResult getOrderStats(Long userId) {
        try {
            log.info("Getting order statistics for user: {}", userId);
            
            List<Order> allOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            // 기본 통계
            int totalOrders = allOrders.size();
            int completedOrders = (int) allOrders.stream()
                                                 .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                                                 .count();
            int pendingOrders = (int) allOrders.stream()
                                              .filter(order -> order.getStatus() == Order.OrderStatus.RECEIVED ||
                                                             order.getStatus() == Order.OrderStatus.ARRIVED ||
                                                             order.getStatus() == Order.OrderStatus.REPACKING)
                                              .count();
            
            // 총 CBM 및 중량
            BigDecimal totalCbm = allOrders.stream()
                                          .map(Order::getTotalCbm)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalWeight = allOrders.stream()
                                            .map(Order::getTotalWeight)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 배송 방법별 통계
            Map<String, Long> shippingTypeStats = allOrders.stream()
                                                          .collect(Collectors.groupingBy(
                                                              order -> order.getShippingType().name(),
                                                              Collectors.counting()
                                                          ));
            
            // 최근 6개월 주문 트렌드
            List<Map<String, Object>> monthlyTrend = getRecentMonthlyTrend(allOrders);
            
            // 결제 총액 (완료된 주문만)
            BigDecimal totalPaidAmount = billingRepository.findByUserId(userId).stream()
                                                        .filter(billing -> billing.getPaymentStatus() == 
                                                                         com.ycs.lms.entity.Billing.PaymentStatus.COMPLETED)
                                                        .map(com.ycs.lms.entity.Billing::getTotal)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return OrderStatsResult.success(
                totalOrders,
                completedOrders,
                pendingOrders,
                totalCbm,
                totalWeight,
                shippingTypeStats,
                monthlyTrend,
                totalPaidAmount
            );
            
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage(), e);
            return OrderStatsResult.error("주문 통계 조회 중 오류가 발생했습니다.");
        }
    }
    
    // 유틸리티 메서드들
    private Map<String, Long> getOrderStatusCounts(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        long total = orders.size();
        
        // pending: 진행중인 주문 (모든 미완료 상태)
        long pending = orders.stream()
                           .filter(order -> {
                               Order.OrderStatus status = order.getStatus();
                               return status == Order.OrderStatus.RECEIVED ||
                                      status == Order.OrderStatus.ARRIVED ||
                                      status == Order.OrderStatus.REPACKING ||
                                      status == Order.OrderStatus.SHIPPING ||
                                      status == Order.OrderStatus.BILLING ||
                                      status == Order.OrderStatus.PAYMENT_PENDING ||
                                      status == Order.OrderStatus.PAYMENT_CONFIRMED ||
                                      status == Order.OrderStatus.PENDING ||
                                      status == Order.OrderStatus.PROCESSING ||
                                      status == Order.OrderStatus.SHIPPED ||
                                      status == Order.OrderStatus.IN_TRANSIT;
                           })
                           .count();
        
        // completed: 완료된 주문 (DELIVERED, COMPLETED)
        long completed = orders.stream()
                              .filter(order -> {
                                  Order.OrderStatus status = order.getStatus();
                                  return status == Order.OrderStatus.DELIVERED ||
                                         status == Order.OrderStatus.COMPLETED;
                              })
                              .count();
        
        return Map.of(
            "total", total,
            "pending", pending,
            "completed", completed
        );
    }
    
    private Map<String, Object> getPaymentInfo(Long userId) {
        List<com.ycs.lms.entity.Billing> billings = billingRepository.findByUserId(userId);
        
        long pendingPayments = billings.stream()
                                     .filter(b -> b.getPaymentStatus() == 
                                                com.ycs.lms.entity.Billing.PaymentStatus.PENDING)
                                     .count();
        
        BigDecimal pendingAmount = billings.stream()
                                         .filter(b -> b.getPaymentStatus() == 
                                                    com.ycs.lms.entity.Billing.PaymentStatus.PENDING)
                                         .map(com.ycs.lms.entity.Billing::getTotal)
                                         .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPaid = billings.stream()
                                     .filter(b -> b.getPaymentStatus() == 
                                                com.ycs.lms.entity.Billing.PaymentStatus.COMPLETED)
                                     .map(com.ycs.lms.entity.Billing::getTotal)
                                     .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "pendingPayments", pendingPayments,
            "pendingAmount", pendingAmount,
            "totalPaid", totalPaid.intValue(), // 프론트엔드에서 정수로 기대
            "totalBillings", billings.size()
        );
    }
    
    private List<Map<String, Object>> getMonthlyOrderStats(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        Map<String, Long> monthlyMap = orders.stream()
                                           .filter(order -> order.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                                           .collect(Collectors.groupingBy(
                                               order -> order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                                               Collectors.counting()
                                           ));
        
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            result.add(Map.of(
                "month", month.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
                "count", monthlyMap.getOrDefault(monthKey, 0L)
            ));
        }
        
        return result;
    }
    
    private List<String> generateUserAlerts(List<Order> orders) {
        List<String> alerts = new ArrayList<>();
        
        // 회원코드 미기재 주문 체크
        long noMemberCodeCount = orders.stream()
                                     .filter(Order::getNoMemberCode)
                                     .count();
        if (noMemberCodeCount > 0) {
            alerts.add(String.format("회원코드 미기재로 지연된 주문이 %d건 있습니다.", noMemberCodeCount));
        }
        
        // THB 1,500 초과 주문 체크  
        long extraRecipientCount = orders.stream()
                                        .filter(Order::getRequiresExtraRecipient)
                                        .count();
        if (extraRecipientCount > 0) {
            alerts.add(String.format("추가 수취인 정보가 필요한 주문이 %d건 있습니다.", extraRecipientCount));
        }
        
        // 미결제 주문 체크
        long pendingPaymentCount = orders.stream()
                                        .filter(order -> order.getStatus() == Order.OrderStatus.PAYMENT_PENDING)
                                        .count();
        if (pendingPaymentCount > 0) {
            alerts.add(String.format("결제 대기 중인 주문이 %d건 있습니다.", pendingPaymentCount));
        }
        
        return alerts;
    }
    
    private List<TrackingStep> generateTrackingSteps(Order order) {
        List<TrackingStep> steps = new ArrayList<>();
        
        // 기본 단계들
        steps.add(new TrackingStep("주문 접수", "주문이 접수되었습니다.", 
                                  order.getCreatedAt(), true));
        
        if (order.getArrivedAt() != null) {
            steps.add(new TrackingStep("창고 입고", "상품이 창고에 도착했습니다.", 
                                      order.getArrivedAt(), true));
        }
        
        if (order.getRepackingCompleted()) {
            steps.add(new TrackingStep("리패킹 완료", "포장 작업이 완료되었습니다.", 
                                      null, true));
        }
        
        if (order.getShippedAt() != null) {
            steps.add(new TrackingStep("발송 완료", "상품이 발송되었습니다.", 
                                      order.getShippedAt(), true));
        }
        
        if (order.getDeliveredAt() != null) {
            steps.add(new TrackingStep("배송 완료", "상품이 배송되었습니다.", 
                                      order.getDeliveredAt(), true));
        } else if (order.getTrackingNumber() != null) {
            steps.add(new TrackingStep("배송 중", "상품이 배송 중입니다.", 
                                      null, false));
        }
        
        return steps;
    }
    
    private String calculateEstimatedDelivery(Order order) {
        if (order.getShippedAt() == null) {
            return "발송 후 예상 배송일을 안내해드립니다.";
        }
        
        LocalDateTime estimatedDate;
        if (order.getShippingType() == Order.ShippingType.AIR) {
            estimatedDate = order.getShippedAt().plusDays(7); // 항공: 7일
        } else {
            estimatedDate = order.getShippedAt().plusDays(21); // 해상: 21일
        }
        
        return estimatedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 예상"));
    }
    
    private String getEMSTrackingStatus(String trackingNumber) {
        if (trackingNumber == null) {
            return "운송장 번호가 없습니다.";
        }
        
        // 실제로는 EMS API 호출
        // 현재는 Mock 데이터 반환
        return "배송 중 - 태국 방콕 배송센터 통과";
    }
    
    private String getSortField(String sortBy) {
        if (sortBy == null) return "createdAt";
        switch (sortBy.toLowerCase()) {
            case "status": return "status";
            case "shipping": return "shippingType"; 
            case "updated": return "updatedAt";
            default: return "createdAt";
        }
    }
    
    private List<Map<String, Object>> getRecentMonthlyTrend(List<Order> orders) {
        return orders.stream()
                    .filter(order -> order.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                    .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                    ))
                    .entrySet().stream()
                    .map(entry -> Map.<String, Object>of(
                        "month", entry.getKey(),
                        "count", entry.getValue()
                    ))
                    .collect(Collectors.toList());
    }
    
    // 결과 DTO 클래스들
    public static class UserDashboard {
        private boolean success;
        private String message;
        private User user;
        private List<Order> recentOrders;
        private Map<String, Long> statusCounts;
        private Map<String, Object> paymentInfo;
        private List<Order> trackingOrders;
        private List<Map<String, Object>> monthlyStats;
        private List<String> alerts;
        
        public static UserDashboard success(User user, List<Order> recentOrders, 
                                          Map<String, Long> statusCounts, Map<String, Object> paymentInfo,
                                          List<Order> trackingOrders, List<Map<String, Object>> monthlyStats,
                                          List<String> alerts) {
            UserDashboard dashboard = new UserDashboard();
            dashboard.success = true;
            dashboard.user = user;
            dashboard.recentOrders = recentOrders;
            dashboard.statusCounts = statusCounts;
            dashboard.paymentInfo = paymentInfo;
            dashboard.trackingOrders = trackingOrders;
            dashboard.monthlyStats = monthlyStats;
            dashboard.alerts = alerts;
            return dashboard;
        }
        
        public static UserDashboard error(String message) {
            UserDashboard dashboard = new UserDashboard();
            dashboard.success = false;
            dashboard.message = message;
            return dashboard;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public List<Order> getRecentOrders() { return recentOrders; }
        public Map<String, Long> getStatusCounts() { return statusCounts; }
        public Map<String, Object> getPaymentInfo() { return paymentInfo; }
        public List<Order> getTrackingOrders() { return trackingOrders; }
        public List<Map<String, Object>> getMonthlyStats() { return monthlyStats; }
        public List<String> getAlerts() { return alerts; }
    }
    
    public static class TrackingResult {
        private boolean success;
        private String message;
        private Order order;
        private List<TrackingStep> trackingSteps;
        private String estimatedDelivery;
        private String emsStatus;
        
        public static TrackingResult success(Order order, List<TrackingStep> trackingSteps, 
                                           String estimatedDelivery, String emsStatus) {
            TrackingResult result = new TrackingResult();
            result.success = true;
            result.order = order;
            result.trackingSteps = trackingSteps;
            result.estimatedDelivery = estimatedDelivery;
            result.emsStatus = emsStatus;
            return result;
        }
        
        public static TrackingResult error(String message) {
            TrackingResult result = new TrackingResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Order getOrder() { return order; }
        public List<TrackingStep> getTrackingSteps() { return trackingSteps; }
        public String getEstimatedDelivery() { return estimatedDelivery; }
        public String getEmsStatus() { return emsStatus; }
    }
    
    public static class TrackingStep {
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private boolean completed;
        
        public TrackingStep(String title, String description, LocalDateTime timestamp, boolean completed) {
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.completed = completed;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isCompleted() { return completed; }
    }
    
    public static class OrderListResult {
        private boolean success;
        private String message;
        private Page<Order> orders;
        
        public static OrderListResult success(Page<Order> orders) {
            OrderListResult result = new OrderListResult();
            result.success = true;
            result.orders = orders;
            return result;
        }
        
        public static OrderListResult error(String message) {
            OrderListResult result = new OrderListResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Page<Order> getOrders() { return orders; }
    }
    
    public static class OrderStatsResult {
        private boolean success;
        private String message;
        private int totalOrders;
        private int completedOrders;
        private int pendingOrders;
        private BigDecimal totalCbm;
        private BigDecimal totalWeight;
        private Map<String, Long> shippingTypeStats;
        private List<Map<String, Object>> monthlyTrend;
        private BigDecimal totalPaidAmount;
        
        public static OrderStatsResult success(int totalOrders, int completedOrders, int pendingOrders,
                                             BigDecimal totalCbm, BigDecimal totalWeight, 
                                             Map<String, Long> shippingTypeStats,
                                             List<Map<String, Object>> monthlyTrend,
                                             BigDecimal totalPaidAmount) {
            OrderStatsResult result = new OrderStatsResult();
            result.success = true;
            result.totalOrders = totalOrders;
            result.completedOrders = completedOrders;
            result.pendingOrders = pendingOrders;
            result.totalCbm = totalCbm;
            result.totalWeight = totalWeight;
            result.shippingTypeStats = shippingTypeStats;
            result.monthlyTrend = monthlyTrend;
            result.totalPaidAmount = totalPaidAmount;
            return result;
        }
        
        public static OrderStatsResult error(String message) {
            OrderStatsResult result = new OrderStatsResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getTotalOrders() { return totalOrders; }
        public int getCompletedOrders() { return completedOrders; }
        public int getPendingOrders() { return pendingOrders; }
        public BigDecimal getTotalCbm() { return totalCbm; }
        public BigDecimal getTotalWeight() { return totalWeight; }
        public Map<String, Long> getShippingTypeStats() { return shippingTypeStats; }
        public List<Map<String, Object>> getMonthlyTrend() { return monthlyTrend; }
        public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    }
}