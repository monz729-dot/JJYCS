package com.ycs.lms.controller;

import com.ycs.lms.entity.Order;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DashboardController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 대시보드 데이터 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDashboard(@PathVariable Long userId) {
        try {
            log.info("Getting dashboard data for userId: {}", userId);
            
            // 사용자 존재 여부 확인
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "사용자를 찾을 수 없습니다."
                ));
            }

            // 사용자의 주문 목록 조회
            List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            log.info("Found {} orders for user {}", userOrders.size(), userId);

            // 통계 계산
            long totalOrders = userOrders.size();
            long pendingOrders = userOrders.stream()
                .filter(order -> isPendingStatus(order.getStatus()))
                .count();
            long completedOrders = userOrders.stream()
                .filter(order -> isCompletedStatus(order.getStatus()))
                .count();

            // 대시보드 응답 구성
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("statusCounts", Map.of(
                "total", totalOrders,
                "pending", pendingOrders,
                "completed", completedOrders
            ));
            dashboardData.put("recentOrders", userOrders.subList(0, Math.min(10, userOrders.size())));
            dashboardData.put("paymentInfo", Map.of(
                "totalPaid", 0,
                "pendingAmount", 0
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dashboardData
            ));
            
        } catch (Exception e) {
            log.error("Failed to get dashboard data for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "대시보드 데이터 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자 통계 조회
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            log.info("Getting stats for userId: {}", userId);
            
            List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", userOrders.size());
            stats.put("pendingOrders", userOrders.stream().filter(order -> isPendingStatus(order.getStatus())).count());
            stats.put("completedOrders", userOrders.stream().filter(order -> isCompletedStatus(order.getStatus())).count());
            stats.put("success", true);

            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Failed to get stats for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "통계 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자 요약 정보 조회
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getUserSummary(@PathVariable Long userId) {
        try {
            log.info("Getting summary for userId: {}", userId);
            
            List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            // 최근 30일 주문
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentOrders = userOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalOrders", userOrders.size());
            summary.put("recentOrders", recentOrders);
            summary.put("warehouseItems", userOrders.stream().filter(order -> 
                Order.OrderStatus.ARRIVED.equals(order.getStatus()) || Order.OrderStatus.REPACKING.equals(order.getStatus())).count());
            summary.put("success", true);

            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Failed to get summary for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "요약 정보 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자 알림 조회
     */
    @GetMapping("/user/{userId}/alerts")
    public ResponseEntity<?> getUserAlerts(@PathVariable Long userId) {
        try {
            // TODO: 실제 알림 시스템 구현 시 교체
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", List.of()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get alerts for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "알림 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자 최근 활동 조회
     */
    @GetMapping("/user/{userId}/recent-activity")
    public ResponseEntity<?> getRecentActivity(@PathVariable Long userId) {
        try {
            List<Order> recentOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .subList(0, Math.min(5, orderRepository.findByUserIdOrderByCreatedAtDesc(userId).size()));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", recentOrders
            ));
            
        } catch (Exception e) {
            log.error("Failed to get recent activity for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "최근 활동 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    private boolean isPendingStatus(Order.OrderStatus status) {
        return List.of(Order.OrderStatus.RECEIVED, Order.OrderStatus.ARRIVED, Order.OrderStatus.REPACKING, 
                      Order.OrderStatus.SHIPPING, Order.OrderStatus.BILLING, Order.OrderStatus.PAYMENT_PENDING)
            .contains(status);
    }

    private boolean isCompletedStatus(Order.OrderStatus status) {
        return List.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.COMPLETED, Order.OrderStatus.PAYMENT_CONFIRMED)
            .contains(status);
    }
}