package com.ysc.lms.controller;

import com.ysc.lms.constants.ErrorCodes;
import com.ysc.lms.dto.ApiResponse;
import com.ysc.lms.dto.OrderResponse;
import com.ysc.lms.dto.SimpleCreateOrderRequest;
import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.User;
import com.ysc.lms.exception.BusinessRuleViolationException;
import com.ysc.lms.exception.ForbiddenException;
import com.ysc.lms.exception.ResourceNotFoundException;
import com.ysc.lms.service.SimpleOrderService;
import com.ysc.lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 간단한 주문 관리 컨트롤러 - MVP용
 * 핵심 기능: 주문 생성, 조회, 제출
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class SimpleOrderController {
    
    private final SimpleOrderService simpleOrderService;
    private final UserService userService;
    
    /**
     * 주문 생성 - POST /api/orders
     */
    @PostMapping
    @PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody SimpleCreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
            // PENDING 상태 사용자는 주문 생성 불가
            if (user.getStatus() == User.UserStatus.PENDING) {
                throw new ForbiddenException(ErrorCodes.FORBIDDEN_NOT_APPROVED, 
                    "승인 대기 중인 계정은 주문을 생성할 수 없습니다. 승인 완료 후 이용해 주세요.");
            }
            
            // REJECTED, SUSPENDED 상태도 차단
            if (user.getStatus() == User.UserStatus.REJECTED) {
                throw new ForbiddenException(ErrorCodes.FORBIDDEN_NOT_APPROVED,
                    "승인 거절된 계정은 주문을 생성할 수 없습니다.");
            }
            
            if (user.getStatus() == User.UserStatus.SUSPENDED) {
                throw new ForbiddenException(ErrorCodes.FORBIDDEN_NOT_APPROVED,
                    "정지된 계정은 주문을 생성할 수 없습니다.");
            }
            
            Order order = simpleOrderService.createOrder(user.getId(), request);
            OrderResponse response = OrderResponse.from(order);
            
            log.info("주문 생성 완료: 사용자={}, 주문번호={}", user.getEmail(), order.getOrderNumber());
            
            return ResponseEntity.ok(ApiResponse.success(response, "주문이 성공적으로 생성되었습니다."));
            
        } catch (ForbiddenException e) {
            log.warn("주문 생성 권한 오류: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
                    
        } catch (BusinessRuleViolationException e) {
            log.warn("주문 생성 비즈니스 룰 위반: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCodes.BUSINESS_RULE_VIOLATION, e.getMessage()));
                    
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "주문 생성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 주문 제출 - PATCH /api/orders/{id}/submit
     */
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> submitOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
            Order order = simpleOrderService.submitOrder(id, user.getId());
            OrderResponse response = OrderResponse.from(order);
            
            log.info("주문 제출 완료: 사용자={}, 주문번호={}", user.getEmail(), order.getOrderNumber());
            
            return ResponseEntity.ok(ApiResponse.success(response, "주문이 성공적으로 제출되었습니다."));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCodes.RESOURCE_NOT_FOUND, e.getMessage()));
                    
        } catch (BusinessRuleViolationException e) {
            log.warn("주문 제출 상태 전환 오류: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCodes.INVALID_STATUS_TRANSITION, e.getMessage()));
                    
        } catch (Exception e) {
            log.error("주문 제출 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "주문 제출 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 내 주문 목록 조회 - GET /api/orders
     */
    @GetMapping
    @PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage = simpleOrderService.getUserOrders(user.getId(), pageable);
            Page<OrderResponse> responsePage = orderPage.map(OrderResponse::from);
            
            return ResponseEntity.ok(ApiResponse.success(responsePage));
            
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "주문 목록 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 특정 주문 조회 - GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
            Order order = simpleOrderService.getOrderByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
            
            OrderResponse response = OrderResponse.from(order);
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCodes.RESOURCE_NOT_FOUND, e.getMessage()));
                    
        } catch (Exception e) {
            log.error("주문 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "주문 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 내 주문 상태별 조회 - GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('PARTNER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = simpleOrderService.getUserOrdersByStatus(user.getId(), orderStatus);
            List<OrderResponse> responses = orders.stream()
                    .map(OrderResponse::from)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.success(responses));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCodes.VALIDATION_ERROR, "올바르지 않은 주문 상태입니다."));
                    
        } catch (Exception e) {
            log.error("상태별 주문 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(ErrorCodes.INTERNAL_ERROR, "주문 조회 중 오류가 발생했습니다."));
        }
    }
}