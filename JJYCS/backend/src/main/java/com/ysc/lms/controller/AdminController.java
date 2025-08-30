package com.ysc.lms.controller;

import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.service.EmailService;
import com.ysc.lms.service.OrderService;
import com.ysc.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    
    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userType) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        
        if (status != null && userType != null) {
            users = userRepository.findByStatusAndUserTypeOrderByCreatedAtDesc(
                User.UserStatus.valueOf(status.toUpperCase()), 
                User.UserType.valueOf(userType.toUpperCase()), 
                pageable
            );
        } else if (status != null) {
            users = userRepository.findByStatusOrderByCreatedAtDesc(
                User.UserStatus.valueOf(status.toUpperCase()), 
                pageable
            );
        } else if (userType != null) {
            users = userRepository.findByUserTypeOrderByCreatedAtDesc(
                User.UserType.valueOf(userType.toUpperCase()), 
                pageable
            );
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return ResponseEntity.ok(Map.of(
            "users", users.getContent(),
            "totalElements", users.getTotalElements(),
            "totalPages", users.getTotalPages(),
            "currentPage", page,
            "pageSize", size
        ));
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
            
            // 승인 이메일 발송
            try {
                emailService.sendApprovalEmail(
                    user.getEmail(), 
                    user.getName(), 
                    user.getUserType().toString()
                );
                log.info("Approval email sent to user: {}", user.getEmail());
            } catch (Exception emailEx) {
                log.error("Failed to send approval email to: {}", user.getEmail(), emailEx);
                // 이메일 발송 실패해도 승인 처리는 성공으로 처리
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
            
            // 거절 이메일 발송
            try {
                emailService.sendRejectionEmail(
                    user.getEmail(), 
                    user.getName(), 
                    user.getUserType().toString(),
                    request.get("reason")
                );
                log.info("Rejection email sent to user: {}", user.getEmail());
            } catch (Exception emailEx) {
                log.error("Failed to send rejection email to: {}", user.getEmail(), emailEx);
                // 이메일 발송 실패해도 거절 처리는 성공으로 처리
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
            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalUsers", userRepository.count(),
                "pendingApprovals", userRepository.countByStatus(User.UserStatus.PENDING),
                "totalOrders", orderRepository.count(),
                "pendingOrders", orderRepository.countByStatus(Order.OrderStatus.PENDING),
                "processingOrders", orderRepository.countByStatus(Order.OrderStatus.PROCESSING),
                "shippedOrders", orderRepository.countByStatus(Order.OrderStatus.SHIPPED),
                "deliveredOrders", orderRepository.countByStatus(Order.OrderStatus.DELIVERED),
                "activePartners", userRepository.countByStatusAndUserType(User.UserStatus.ACTIVE, User.UserType.PARTNER)
            ));
            
        } catch (Exception e) {
            log.error("Error fetching admin stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "Failed to fetch statistics"));
        }
    }
    
    private String generateMemberCode(String prefix) {
        // 해당 타입의 마지막 멤버 코드 조회하여 순차 생성
        String pattern = prefix + "%";
        // 간단한 구현 - 실제로는 더 정교한 코드 생성 로직이 필요
        long count = userRepository.count();
        return String.format("%s%03d", prefix, count + 1);
    }
}