package com.ycs.lms.controller;

import com.ycs.lms.util.ApiResponse;
import com.ycs.lms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 결제 관리 컨트롤러
 */
@Tag(name = "Payments", description = "결제 관리 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {

    @Operation(summary = "결제 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getPayments(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting payments for user: {}", userPrincipal.getId());
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("content", Arrays.asList(
            Map.of("id", 1, "paymentCode", "PAY202501001", "amount", 103000, "status", "completed"),
            Map.of("id", 2, "paymentCode", "PAY202501002", "amount", 35000, "status", "pending")
        ));
        mockData.put("totalElements", 2);
        mockData.put("totalPages", 1);
        
        return ResponseEntity.ok(ApiResponse.success(mockData));
    }

    @Operation(summary = "결제 상세 조회")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Object>> getPaymentDetail(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting payment detail: {} for user: {}", paymentId, userPrincipal.getId());
        
        Map<String, Object> mockData = Map.of(
            "id", paymentId,
            "paymentCode", "PAY202501001",
            "amount", 103000,
            "currency", "KRW",
            "status", "completed",
            "method", "credit_card",
            "createdAt", new Date().toString()
        );
        
        return ResponseEntity.ok(ApiResponse.success(mockData));
    }

    @Operation(summary = "결제 처리")
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Object>> processPayment(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Processing payment by user: {}", userPrincipal.getId());
        
        Map<String, Object> response = Map.of(
            "status", "success",
            "paymentId", 123L,
            "transactionId", "TXN" + System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "결제가 성공적으로 처리되었습니다."));
    }
}