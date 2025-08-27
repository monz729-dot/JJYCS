package com.ycs.lms.controller;

import com.ycs.lms.service.DashboardService;
import com.ycs.lms.service.DashboardService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable Long userId) {
        try {
            log.info("Getting user dashboard for user: {}", userId);
            
            UserDashboard result = dashboardService.getUserDashboard(userId);
            log.info("UserDashboard result success: {}", result.isSuccess());
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", result.getUser(),
                    "recentOrders", result.getRecentOrders(),
                    "statusCounts", result.getStatusCounts(),
                    "paymentInfo", result.getPaymentInfo(),
                    "trackingOrders", result.getTrackingOrders(),
                    "monthlyStats", result.getMonthlyStats(),
                    "alerts", result.getAlerts()
                ));
            } else {
                log.error("UserDashboard failed: {}", result.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("User dashboard error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "대시보드 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/orders")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "created") String sortBy) {
        try {
            log.info("Getting user orders: userId={}, page={}, size={}, status={}, sortBy={}", 
                    userId, page, size, status, sortBy);
            
            OrderListResult result = dashboardService.getUserOrders(userId, page, size, status, sortBy);
            
            if (result.isSuccess()) {
                Page<com.ycs.lms.entity.Order> orders = result.getOrders();
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orders", orders.getContent(),
                    "totalElements", orders.getTotalElements(),
                    "totalPages", orders.getTotalPages(),
                    "currentPage", orders.getNumber(),
                    "pageSize", orders.getSize(),
                    "hasNext", orders.hasNext(),
                    "hasPrevious", orders.hasPrevious()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Get user orders error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "주문 목록 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/tracking/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderTracking(
            @PathVariable Long userId,
            @PathVariable String orderNumber) {
        try {
            log.info("Getting order tracking: userId={}, orderNumber={}", userId, orderNumber);
            
            TrackingResult result = dashboardService.getOrderTracking(orderNumber, userId);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "order", result.getOrder(),
                    "trackingSteps", result.getTrackingSteps(),
                    "estimatedDelivery", result.getEstimatedDelivery(),
                    "emsStatus", result.getEmsStatus()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Order tracking error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "배송 추적 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats(@PathVariable Long userId) {
        try {
            log.info("Getting order statistics for user: {}", userId);
            
            OrderStatsResult result = dashboardService.getOrderStats(userId);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalOrders", result.getTotalOrders(),
                    "completedOrders", result.getCompletedOrders(),
                    "pendingOrders", result.getPendingOrders(),
                    "totalCbm", result.getTotalCbm(),
                    "totalWeight", result.getTotalWeight(),
                    "shippingTypeStats", result.getShippingTypeStats(),
                    "monthlyTrend", result.getMonthlyTrend(),
                    "totalPaidAmount", result.getTotalPaidAmount()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Order statistics error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "주문 통계 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserSummary(@PathVariable Long userId) {
        try {
            log.info("Getting user summary for user: {}", userId);
            
            // 간단한 요약 정보만 제공
            OrderStatsResult statsResult = dashboardService.getOrderStats(userId);
            UserDashboard dashboardResult = dashboardService.getUserDashboard(userId);
            
            if (statsResult.isSuccess() && dashboardResult.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "summary", Map.of(
                        "totalOrders", statsResult.getTotalOrders(),
                        "pendingOrders", statsResult.getPendingOrders(),
                        "completedOrders", statsResult.getCompletedOrders(),
                        "pendingPayments", dashboardResult.getPaymentInfo().get("pendingPayments"),
                        "pendingAmount", dashboardResult.getPaymentInfo().get("pendingAmount"),
                        "alertCount", dashboardResult.getAlerts().size(),
                        "lastOrderDate", dashboardResult.getRecentOrders().isEmpty() ? null :
                                        dashboardResult.getRecentOrders().get(0).getCreatedAt()
                    )
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "사용자 요약 정보 조회 실패"
                ));
            }
            
        } catch (Exception e) {
            log.error("User summary error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "사용자 요약 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/alerts")
    public ResponseEntity<Map<String, Object>> getUserAlerts(@PathVariable Long userId) {
        try {
            log.info("Getting user alerts for user: {}", userId);
            
            UserDashboard result = dashboardService.getUserDashboard(userId);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "alerts", result.getAlerts(),
                    "alertCount", result.getAlerts().size()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("User alerts error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "알림 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    @GetMapping("/user/{userId}/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity(@PathVariable Long userId) {
        try {
            log.info("Getting recent activity for user: {}", userId);
            
            UserDashboard result = dashboardService.getUserDashboard(userId);
            
            if (result.isSuccess()) {
                // 최근 활동 내역 생성
                java.util.List<java.util.Map<String, Object>> activities = new java.util.ArrayList<>();
                
                // 최근 주문들을 활동 내역으로 변환
                result.getRecentOrders().stream()
                      .limit(5)
                      .forEach(order -> {
                          activities.add(Map.of(
                              "type", "ORDER_STATUS_CHANGE",
                              "title", "주문 " + order.getOrderNumber() + " 상태 변경",
                              "description", "상태: " + getStatusDisplayName(order.getStatus().name()),
                              "timestamp", order.getUpdatedAt(),
                              "orderNumber", order.getOrderNumber()
                          ));
                      });
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "activities", activities,
                    "totalCount", activities.size()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getMessage()
                ));
            }
            
        } catch (Exception e) {
            log.error("Recent activity error", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "최근 활동 조회 중 오류가 발생했습니다."
            ));
        }
    }
    
    // 유틸리티 메서드
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "RECEIVED": return "접수됨";
            case "ARRIVED": return "창고도착";
            case "REPACKING": return "리패킹중";
            case "SHIPPING": return "발송됨";
            case "DELIVERED": return "배송완료";
            case "BILLING": return "정산중";
            case "PAYMENT_PENDING": return "결제대기";
            default: return status;
        }
    }
}