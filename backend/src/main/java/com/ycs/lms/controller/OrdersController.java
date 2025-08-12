package com.ycs.lms.controller;

import com.ycs.lms.dto.OrderCreateRequest;
import com.ycs.lms.dto.OrderResponse;
import com.ycs.lms.dto.PageResponse;
import com.ycs.lms.service.OrdersService;
import com.ycs.lms.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "주문 관리 API")
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. CBM 자동 계산 및 비즈니스 룰 적용")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = ordersService.createOrder(request);
        
        // CBM 29 초과 체크
        if (response.getTotalCBM() != null && response.getTotalCBM().compareTo(new java.math.BigDecimal("29.0")) > 0) {
            response.addWarning("CBM_EXCEEDED", 
                "총 CBM이 29를 초과하여 항공 배송으로 자동 전환되었습니다", 
                Map.of("cbm", response.getTotalCBM(), "threshold", 29.0));
        }
        
        // THB 1,500 초과 체크
        if ("THB".equals(response.getCurrency()) && response.getTotalAmount() != null && 
            response.getTotalAmount().compareTo(new java.math.BigDecimal("1500.0")) > 0) {
            response.addWarning("AMOUNT_EXCEEDED_THB_1500", 
                "금액이 THB 1,500를 초과합니다. 수취인 추가 정보를 입력해주세요",
                Map.of("amount", response.getTotalAmount(), "threshold", 1500.0));
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 조회", description = "주문 ID로 주문 정보를 조회합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(ordersService.getOrder(orderId)));
    }

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "주문 목록을 페이징하여 조회합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(ordersService.getOrders(status, startDate, endDate, pageable)));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "주문 수정", description = "주문 정보를 수정합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ordersService.updateOrder(orderId, request), "주문이 수정되었습니다."));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId) {
        ordersService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(null, "주문이 취소되었습니다."));
    }
}