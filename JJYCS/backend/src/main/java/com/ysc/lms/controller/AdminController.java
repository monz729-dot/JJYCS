package com.ysc.lms.controller;

import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.User;
import com.ysc.lms.entity.Notification;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.repository.NotificationRepository;
import com.ysc.lms.service.EmailService;
import com.ysc.lms.service.OrderService;
import com.ysc.lms.service.UserService;
import com.ysc.lms.service.NotificationService;
import com.ysc.lms.config.PerformanceMonitoringInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final PerformanceMonitoringInterceptor performanceMonitoringInterceptor;
    
    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pendingUsers = userRepository.findByStatusOrderByCreatedAtDesc(User.UserStatus.PENDING, pageable);
        
        return ResponseEntity.ok(Map.of(
            "users", pendingUsers.getContent(),
            "totalElements", pendingUsers.getTotalElements(),
            "totalPages", pendingUsers.getTotalPages(),
            "currentPage", page,
            "pageSize", size
        ));
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userType) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        
        try {
            if (status != null && !status.trim().isEmpty() && userType != null && !userType.trim().isEmpty()) {
                users = userRepository.findByStatusAndUserTypeOrderByCreatedAtDesc(
                    User.UserStatus.valueOf(status.toUpperCase()), 
                    User.UserType.valueOf(userType.toUpperCase()), 
                    pageable
                );
            } else if (status != null && !status.trim().isEmpty()) {
                users = userRepository.findByStatusOrderByCreatedAtDesc(
                    User.UserStatus.valueOf(status.toUpperCase()), 
                    pageable
                );
            } else if (userType != null && !userType.trim().isEmpty()) {
                users = userRepository.findByUserTypeOrderByCreatedAtDesc(
                    User.UserType.valueOf(userType.toUpperCase()), 
                    pageable
                );
            } else {
                users = userRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", users.getContent(),
                "totalElements", users.getTotalElements(),
                "totalPages", users.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch users: " + e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found"));
            }
            
            User user = userOpt.get();
            if (user.getStatus() != User.UserStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not in pending status"));
            }
            
            // 승인 처리
            user.setStatus(User.UserStatus.ACTIVE);
            user.setApprovedAt(LocalDateTime.now());
            user.setApprovedBy("admin"); // TODO: 실제 관리자 정보로 변경
            user.setApprovalNotes(request.get("notes"));
            
            // 멤버 코드 생성 (기업/파트너인 경우)
            if (user.getUserType() == User.UserType.CORPORATE && user.getMemberCode() == null) {
                user.setMemberCode(generateMemberCode("COR"));
            } else if (user.getUserType() == User.UserType.PARTNER && user.getMemberCode() == null) {
                user.setMemberCode(generateMemberCode("PAR"));
            } else if (user.getUserType() == User.UserType.GENERAL && user.getMemberCode() == null) {
                user.setMemberCode(generateMemberCode("GEN"));
            }
            
            userRepository.save(user);
            
            // 통합 알림 발송 (이메일 + DB 알림)
            try {
                notificationService.notifyUserApprovalStatusChange(user, true, null);
                log.info("Approval notification sent to user: {}", user.getEmail());
            } catch (Exception notificationEx) {
                log.error("Failed to send approval notification to: {}", user.getEmail(), notificationEx);
                // 알림 발송 실패해도 승인 처리는 성공으로 처리
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User approved successfully",
                "user", user
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to approve user: " + e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectUser(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User not found"));
            }
            
            User user = userOpt.get();
            if (user.getStatus() != User.UserStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not in pending status"));
            }
            
            // 거부 처리
            user.setStatus(User.UserStatus.REJECTED);
            user.setRejectedAt(LocalDateTime.now());
            user.setRejectionReason(request.get("reason"));
            
            userRepository.save(user);
            
            // 통합 알림 발송 (이메일 + DB 알림)
            try {
                notificationService.notifyUserApprovalStatusChange(user, false, request.get("reason"));
                log.info("Rejection notification sent to user: {}", user.getEmail());
            } catch (Exception notificationEx) {
                log.error("Failed to send rejection notification to: {}", user.getEmail(), notificationEx);
                // 알림 발송 실패해도 거절 처리는 성공으로 처리
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User rejected successfully",
                "user", user
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to reject user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        return ResponseEntity.ok(Map.of(
            "pendingUsers", userRepository.countByStatus(User.UserStatus.PENDING),
            "activeUsers", userRepository.countByStatus(User.UserStatus.ACTIVE),
            "totalUsers", userRepository.count(),
            "pendingCorporate", userRepository.countByStatusAndUserType(User.UserStatus.PENDING, User.UserType.CORPORATE),
            "pendingPartner", userRepository.countByStatusAndUserType(User.UserStatus.PENDING, User.UserType.PARTNER),
            "pendingGeneral", userRepository.countByStatusAndUserType(User.UserStatus.PENDING, User.UserType.GENERAL)
        ));
    }
    
    // Order Management Endpoints
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) String status) {
        
        try {
            log.info("Admin requesting orders - page: {}, size: {}, orderNumber: {}, recipientName: {}, status: {}", 
                    page, size, orderNumber, recipientName, status);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            
            // Apply filters based on parameters
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                orders = orderRepository.findByOrderNumberContainingIgnoreCaseOrderByCreatedAtDesc(orderNumber.trim(), pageable);
            } else if (recipientName != null && !recipientName.trim().isEmpty()) {
                orders = orderRepository.findByRecipientNameContainingIgnoreCaseOrderByCreatedAtDesc(recipientName.trim(), pageable);
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    orders = orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus, pageable);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Invalid status: " + status));
                }
            } else {
                orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", orders.getContent(),
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
            
        } catch (Exception e) {
            log.error("Error fetching orders for admin", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch orders: " + e.getMessage()));
        }
    }
    
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "order", orderOpt.get()
            ));
            
        } catch (Exception e) {
            log.error("Error fetching order by ID: {}", orderId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch order details"));
        }
    }
    
    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Status is required"));
            }
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            Order.OrderStatus currentStatus = order.getStatus();
            
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(newStatus.toUpperCase());
                order.setStatus(orderStatus);
                order.setUpdatedAt(LocalDateTime.now());
                
                Order savedOrder = orderRepository.save(order);
                
                log.info("Order status updated - ID: {}, from: {} to: {}", orderId, currentStatus, orderStatus);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order status updated successfully",
                    "order", savedOrder
                ));
                
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Invalid status: " + newStatus));
            }
            
        } catch (Exception e) {
            log.error("Error updating order status - orderId: {}", orderId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to update order status"));
        }
    }
    
    @GetMapping("/orders/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> searchOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        
        try {
            log.info("Admin searching orders - query: {}, status: {}, dateFrom: {}, dateTo: {}", 
                    query, status, dateFrom, dateTo);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            
            // For now, implement basic search by order number or recipient name
            if (query != null && !query.trim().isEmpty()) {
                orders = orderRepository.findByOrderNumberContainingIgnoreCaseOrRecipientNameContainingIgnoreCaseOrderByCreatedAtDesc(
                    query.trim(), query.trim(), pageable);
            } else {
                orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orders", orders.getContent(),
                "totalElements", orders.getTotalElements(),
                "totalPages", orders.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
            
        } catch (Exception e) {
            log.error("Error searching orders", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to search orders: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            // 기본 통계
            long totalUsers = userRepository.count();
            long totalOrders = orderRepository.count();
            long pendingApprovals = userRepository.countByStatus(User.UserStatus.PENDING);
            long activePartners = userRepository.countByStatusAndUserType(User.UserStatus.ACTIVE, User.UserType.PARTNER);
            
            // 주문 상태별 통계
            Map<String, Long> orderStatusStats = Map.of(
                "PENDING", orderRepository.countByStatus(Order.OrderStatus.PENDING),
                "CONFIRMED", orderRepository.countByStatus(Order.OrderStatus.CONFIRMED),
                "IN_WAREHOUSE", orderRepository.countByStatus(Order.OrderStatus.IN_WAREHOUSE),
                "PROCESSING", orderRepository.countByStatus(Order.OrderStatus.PROCESSING),
                "SHIPPED", orderRepository.countByStatus(Order.OrderStatus.SHIPPED),
                "DELIVERED", orderRepository.countByStatus(Order.OrderStatus.DELIVERED),
                "CANCELLED", orderRepository.countByStatus(Order.OrderStatus.CANCELLED),
                "HOLD", orderRepository.countByStatus(Order.OrderStatus.HOLD)
            );
            
            // 사용자 타입별 통계
            Map<String, Long> userTypeStats = Map.of(
                "GENERAL", userRepository.countByUserType(User.UserType.GENERAL),
                "CORPORATE", userRepository.countByUserType(User.UserType.CORPORATE),
                "PARTNER", userRepository.countByUserType(User.UserType.PARTNER),
                "WAREHOUSE", userRepository.countByUserType(User.UserType.WAREHOUSE)
            );
            
            // 배송 타입별 통계
            Map<String, Long> orderTypeStats = Map.of(
                "AIR", orderRepository.countByOrderType(Order.ShippingType.AIR),
                "SEA", orderRepository.countByOrderType(Order.ShippingType.SEA)
            );
            
            // 시간별 신규 주문 (오늘, 이번주, 이번달)
            Map<String, Long> newOrderStats = Map.of(
                "today", orderRepository.countByCreatedAtAfter(startOfDay),
                "thisWeek", orderRepository.countByCreatedAtAfter(startOfWeek),
                "thisMonth", orderRepository.countByCreatedAtAfter(startOfMonth)
            );
            
            // 신규 사용자 등록 (오늘, 이번주, 이번달)
            Map<String, Long> newUserStats = Map.of(
                "today", userRepository.countByCreatedAtAfter(startOfDay),
                "thisWeek", userRepository.countByCreatedAtAfter(startOfWeek),
                "thisMonth", userRepository.countByCreatedAtAfter(startOfMonth)
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "overview", Map.of(
                    "totalUsers", totalUsers,
                    "totalOrders", totalOrders,
                    "pendingApprovals", pendingApprovals,
                    "activePartners", activePartners
                ),
                "orderStatusStats", orderStatusStats,
                "userTypeStats", userTypeStats,
                "orderTypeStats", orderTypeStats,
                "newOrderStats", newOrderStats,
                "newUserStats", newUserStats,
                "lastUpdated", now
            ));
            
        } catch (Exception e) {
            log.error("Error fetching admin stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch statistics"));
        }
    }
    
    
    /**
     * 시스템 활동 로그 조회
     */
    @GetMapping("/activity-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);
            
            // 최근 활동 가져오기 (주문 생성, 사용자 가입, 상태 변경 등)
            java.util.List<Map<String, Object>> activities = new java.util.ArrayList<>();
            
            // 최근 주문들
            java.util.List<Order> recentOrders = orderRepository.findTop10ByCreatedAtAfterOrderByCreatedAtDesc(weekAgo);
            for (Order order : recentOrders) {
                activities.add(Map.of(
                    "type", "ORDER_CREATED",
                    "message", "새 주문이 생성되었습니다: " + order.getOrderNumber(),
                    "timestamp", order.getCreatedAt(),
                    "details", Map.of(
                        "orderNumber", order.getOrderNumber(),
                        "recipientName", order.getRecipientName(),
                        "orderType", order.getOrderType().toString()
                    )
                ));
            }
            
            // 최근 사용자 가입
            java.util.List<User> recentUsers = userRepository.findTop10ByCreatedAtAfterOrderByCreatedAtDesc(weekAgo);
            for (User user : recentUsers) {
                activities.add(Map.of(
                    "type", "USER_REGISTERED",
                    "message", "새 사용자가 등록되었습니다: " + user.getName(),
                    "timestamp", user.getCreatedAt(),
                    "details", Map.of(
                        "userName", user.getName(),
                        "userType", user.getUserType().toString(),
                        "status", user.getStatus().toString()
                    )
                ));
            }
            
            // 시간순 정렬
            activities.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
                LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
                return timeB.compareTo(timeA);
            });
            
            // 페이징 처리
            int start = Math.min(page * size, activities.size());
            int end = Math.min((page + 1) * size, activities.size());
            java.util.List<Map<String, Object>> pagedActivities = activities.subList(start, end);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "activities", pagedActivities,
                "totalElements", activities.size(),
                "totalPages", (int) Math.ceil((double) activities.size() / size),
                "currentPage", page,
                "pageSize", size
            ));
        } catch (Exception e) {
            log.error("Error fetching activity logs", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch activity logs: " + e.getMessage()));
        }
    }
    
    /**
     * 시스템 알림 브로드캐스트
     */
    @PostMapping("/broadcast-notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> broadcastNotification(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String message = (String) request.get("message");
            String targetType = (String) request.get("targetType"); // "ALL", "GENERAL", "CORPORATE", "PARTNER"
            
            if (title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "제목과 메시지가 필요합니다."));
            }
            
            java.util.List<User> targetUsers;
            
            if ("ALL".equals(targetType)) {
                targetUsers = userRepository.findByStatus(User.UserStatus.ACTIVE);
            } else {
                User.UserType userType = User.UserType.valueOf(targetType.toUpperCase());
                targetUsers = userRepository.findByStatusAndUserType(User.UserStatus.ACTIVE, userType);
            }
            
            // NotificationService를 통한 통합 알림 발송
            notificationService.broadcastSystemNotification(title, message, targetUsers);
            
            log.info("Broadcast notification initiated for {} users", targetUsers.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림이 성공적으로 전송되었습니다.",
                "recipientCount", targetUsers.size(),
                "targetType", targetType
            ));
        } catch (Exception e) {
            log.error("Error broadcasting notification", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "알림 전송에 실패했습니다: " + e.getMessage()));
        }
    }
    
    @PostMapping("/test/create-notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTestNotifications() {
        try {
            // Find the first active user to create notifications for
            Optional<User> userOpt = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .findFirst();
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "No active users found to create notifications for"));
            }
            
            User user = userOpt.get();
            
            // Create sample notifications
            Notification notification1 = new Notification(user, Notification.NotificationType.ORDER_STATUS_CHANGED, 
                "주문 상태 변경", "주문 #ORD001의 상태가 '배송 중'으로 변경되었습니다.");
            
            Notification notification2 = new Notification(user, Notification.NotificationType.ORDER_ARRIVED, 
                "창고 도착", "주문 #ORD002가 방콕 창고에 도착했습니다.");
            
            Notification notification3 = new Notification(user, Notification.NotificationType.PAYMENT_REQUIRED, 
                "결제 필요", "주문 #ORD001에 대한 배송비 결제가 필요합니다.");
            
            notificationRepository.save(notification1);
            notificationRepository.save(notification2);
            notificationRepository.save(notification3);
            
            log.info("Created 3 test notifications for user: {}", user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test notifications created successfully",
                "userId", user.getId(),
                "userEmail", user.getEmail(),
                "notificationCount", 3
            ));
            
        } catch (Exception e) {
            log.error("Error creating test notifications", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to create test notifications: " + e.getMessage()));
        }
    }

    private String generateMemberCode(String prefix) {
        // 해당 타입의 마지막 멤버 코드 조회하여 순차 생성
        String pattern = prefix + "%";
        // 간단한 구현 - 실제로는 더 정교한 코드 생성 로직이 필요
        long count = userRepository.count();
        return String.format("%s%03d", prefix, count + 1);
    }
    
    /**
     * API 성능 통계 조회
     */
    @GetMapping("/performance/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceStats() {
        try {
            var allStats = performanceMonitoringInterceptor.getAllStats();
            Map<String, Object> formattedStats = new HashMap<>();
            
            allStats.forEach((endpoint, stats) -> {
                Map<String, Object> endpointData = new HashMap<>();
                endpointData.put("totalRequests", stats.getTotalRequests());
                endpointData.put("averageResponseTime", Math.round(stats.getAverageResponseTime() * 100.0) / 100.0);
                endpointData.put("minResponseTime", stats.getMinDuration());
                endpointData.put("maxResponseTime", stats.getMaxDuration());
                endpointData.put("errorCount", stats.getErrorCount());
                endpointData.put("errorRate", Math.round(stats.getErrorRate() * 100.0) / 100.0);
                endpointData.put("lastUpdateTime", stats.getLastUpdateTime());
                
                formattedStats.put(endpoint, endpointData);
            });
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", formattedStats,
                "totalEndpoints", allStats.size(),
                "generatedAt", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error fetching performance stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch performance statistics: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 엔드포인트 성능 통계 조회
     */
    @GetMapping("/performance/stats/{endpoint}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEndpointStats(@PathVariable String endpoint) {
        try {
            // URL 디코딩 (예: GET%20/api/admin/stats -> GET /api/admin/stats)
            String decodedEndpoint = java.net.URLDecoder.decode(endpoint, "UTF-8");
            
            var stats = performanceMonitoringInterceptor.getEndpointStats(decodedEndpoint);
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "endpoint", decodedEndpoint,
                "totalRequests", stats.getTotalRequests(),
                "averageResponseTime", Math.round(stats.getAverageResponseTime() * 100.0) / 100.0,
                "minResponseTime", stats.getMinDuration(),
                "maxResponseTime", stats.getMaxDuration(),
                "errorCount", stats.getErrorCount(),
                "errorRate", Math.round(stats.getErrorRate() * 100.0) / 100.0,
                "lastUpdateTime", stats.getLastUpdateTime()
            ));
            
        } catch (Exception e) {
            log.error("Error fetching endpoint stats for: {}", endpoint, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch endpoint statistics: " + e.getMessage()));
        }
    }
    
    /**
     * 성능 통계 초기화
     */
    @PostMapping("/performance/stats/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearPerformanceStats() {
        try {
            performanceMonitoringInterceptor.clearStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "성능 통계가 초기화되었습니다.",
                "clearedAt", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error clearing performance stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to clear performance statistics: " + e.getMessage()));
        }
    }
}