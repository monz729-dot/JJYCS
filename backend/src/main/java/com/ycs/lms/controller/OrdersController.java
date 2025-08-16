package com.ycs.lms.controller;

import com.ycs.lms.dto.orders.*;
import com.ycs.lms.service.OrderService;
import com.ycs.lms.util.ApiResponse;
import com.ycs.lms.util.PagedResponse;
import com.ycs.lms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 관리 컨트롤러
 * - 주문 생성/수정/조회
 * - 비즈니스 룰 검증 (CBM 29 초과, THB 1500 초과, member_code)
 * - EMS/HS 코드 검증
 */
@Tag(name = "Orders", description = "주문 관리 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrdersController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * 비즈니스 룰 적용:
     * 1. CBM 계산 및 29 초과시 air 전환
     * 2. THB 1,500 초과시 추가 정보 필요 경고
     * 3. member_code 검증
     * 4. EMS/HS 코드 검증
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. 모든 비즈니스 룰이 자동 적용됩니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Creating order for user: {}", userPrincipal.getId());
        
        try {
            OrderResponse order = orderService.createOrder(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(order, "주문이 성공적으로 생성되었습니다."));
                    
        } catch (BusinessRuleViolationException e) {
            log.warn("Business rule violation while creating order: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("BUSINESS_RULE_VIOLATION", e.getMessage()));
                    
        } catch (ValidationException e) {
            log.warn("Validation error while creating order: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        }
    }

    /**
     * 주문 상세 조회
     */
    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE', 'WAREHOUSE', 'ADMIN') and " +
                 "@orderService.isOrderAccessible(#orderId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting order {} for user: {}", orderId, userPrincipal.getId());
        
        try {
            OrderResponse order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(ApiResponse.success(order));
            
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 주문 수정
     */
    @Operation(summary = "주문 수정", description = "기존 주문 정보를 수정합니다. 비즈니스 룰이 재적용됩니다.")
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE') and " +
                 "@orderService.isOrderOwner(#orderId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Updating order {} for user: {}", orderId, userPrincipal.getId());
        
        try {
            OrderResponse order = orderService.updateOrder(orderId, request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(order, "주문이 성공적으로 수정되었습니다."));
            
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
            
        } catch (OrderNotModifiableException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_NOT_MODIFIABLE", e.getMessage()));
        }
    }

    /**
     * 주문 목록 조회 (페이징)
     */
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 목록을 페이징으로 조회합니다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummary>>> getOrders(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(description = "주문 상태 필터") @RequestParam(required = false) String status,
            @Parameter(description = "배송 방법 필터") @RequestParam(required = false) String orderType,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting orders list for user: {}, page: {}, size: {}", 
                userPrincipal.getId(), page, size);
        
        try {
            // 페이징 및 정렬 설정
            Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // 검색 필터 설정
            OrderSearchFilter filter = OrderSearchFilter.builder()
                    .userId(userPrincipal.getId())
                    .status(status)
                    .orderType(orderType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .userRole(userPrincipal.getRole())
                    .build();
            
            PagedResponse<OrderSummary> orders = orderService.getOrders(filter, pageable);
            return ResponseEntity.ok(ApiResponse.success(orders));
            
        } catch (Exception e) {
            log.error("Error getting orders list", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "주문 목록 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 주문 상태 변경
     */
    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다. (관리자/창고 담당자)")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "주문 ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Updating order {} status to {} by user: {}", 
                orderId, request.getStatus(), userPrincipal.getId());
        
        try {
            OrderResponse order = orderService.updateOrderStatus(orderId, request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(order, "주문 상태가 변경되었습니다."));
            
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
            
        } catch (InvalidStatusTransitionException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS_TRANSITION", e.getMessage()));
        }
    }

    /**
     * 주문 취소
     */
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE', 'ADMIN') and " +
                 "@orderService.isOrderCancellable(#orderId, authentication.principal.userId)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Cancelling order {} by user: {}", orderId, userPrincipal.getId());
        
        try {
            OrderResponse order = orderService.cancelOrder(orderId, request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(order, "주문이 취소되었습니다."));
            
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
            
        } catch (OrderNotCancellableException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_NOT_CANCELLABLE", e.getMessage()));
        }
    }

    /**
     * CBM 계산 (실시간)
     */
    @Operation(summary = "CBM 계산", description = "박스 치수를 기반으로 CBM을 계산하고 배송 방법을 추천합니다.")
    @PostMapping("/calculate-cbm")
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE')")
    public ResponseEntity<ApiResponse<CBMCalculationResponse>> calculateCBM(
            @Valid @RequestBody CBMCalculationRequest request) {
        
        log.info("Calculating CBM for boxes: {}", request.getBoxes().size());
        
        try {
            CBMCalculationResponse response = orderService.calculateCBM(request);
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error calculating CBM", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("CALCULATION_ERROR", "CBM 계산 중 오류가 발생했습니다."));
        }
    }

    /**
     * 비즈니스 룰 검증
     */
    @Operation(summary = "비즈니스 룰 검증", description = "주문 생성 전 비즈니스 룰을 미리 검증합니다.")
    @PostMapping("/validate-business-rules")
    @PreAuthorize("hasAnyRole('INDIVIDUAL', 'ENTERPRISE')")
    public ResponseEntity<ApiResponse<BusinessRuleValidationResponse>> validateBusinessRules(
            @Valid @RequestBody BusinessRuleValidationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Validating business rules for user: {}", userPrincipal.getId());
        
        try {
            BusinessRuleValidationResponse response = 
                orderService.validateBusinessRules(request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error validating business rules", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("VALIDATION_ERROR", "비즈니스 룰 검증 중 오류가 발생했습니다."));
        }
    }
}

// DTO 클래스들 (별도 파일로 생성 예정)
// TODO: 실제 구현시 각각 별도 파일로 분리

// 예외 클래스들 (별도 파일로 생성 예정)
class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}

class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

class OrderNotModifiableException extends RuntimeException {
    public OrderNotModifiableException(String message) {
        super(message);
    }
}

class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(String message) {
        super(message);
    }
}

class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

// UserPrincipal 클래스는 com.ycs.lms.security.UserPrincipal을 사용합니다.