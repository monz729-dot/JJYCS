package com.ycs.lms.controller;

import com.ycs.lms.util.ApiResponse;
import com.ycs.lms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 견적 관리 컨트롤러
 */
@Tag(name = "Estimates", description = "견적 관리 API")
@RestController
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
@Slf4j
public class EstimatesController {

    @Operation(summary = "견적 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getEstimates(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "견적 상태") @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting estimates for user: {}", userPrincipal.getId());
        
        List<Map<String, Object>> estimates = new ArrayList<>();
        
        Map<String, Object> estimate1 = new HashMap<>();
        estimate1.put("id", 1);
        estimate1.put("estimateCode", "EST202501001");
        estimate1.put("orderCode", "ORD202501001");
        estimate1.put("status", "pending");
        estimate1.put("totalAmount", 35000);
        estimate1.put("currency", "KRW");
        estimate1.put("validUntil", "2025-01-25");
        estimate1.put("createdAt", "2025-01-18T10:00:00");
        
        Map<String, Object> estimate2 = new HashMap<>();
        estimate2.put("id", 2);
        estimate2.put("estimateCode", "EST202501002");
        estimate2.put("orderCode", "ORD202501002");
        estimate2.put("status", "approved");
        estimate2.put("totalAmount", 103000);
        estimate2.put("currency", "KRW");
        estimate2.put("validUntil", "2025-01-25");
        estimate2.put("createdAt", "2025-01-17T14:30:00");
        
        estimates.add(estimate1);
        estimates.add(estimate2);
        
        Map<String, Object> response = Map.of(
            "content", estimates,
            "totalElements", estimates.size(),
            "totalPages", 1,
            "size", 20,
            "number", 0
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "견적 상세 조회")
    @GetMapping("/{estimateId}")
    public ResponseEntity<ApiResponse<Object>> getEstimateDetail(
            @Parameter(description = "견적 ID") @PathVariable Long estimateId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting estimate detail: {} for user: {}", estimateId, userPrincipal.getId());
        
        Map<String, Object> estimate = new HashMap<>();
        estimate.put("id", estimateId);
        estimate.put("estimateCode", "EST202501001");
        estimate.put("orderCode", "ORD202501001");
        estimate.put("status", "pending");
        estimate.put("shippingCost", 25000);
        estimate.put("packagingCost", 5000);
        estimate.put("insuranceCost", 3000);
        estimate.put("additionalCost", 2000);
        estimate.put("totalAmount", 35000);
        estimate.put("currency", "KRW");
        estimate.put("validUntil", "2025-01-25");
        estimate.put("notes", "해상 운송 견적");
        estimate.put("createdAt", "2025-01-18T10:00:00");
        
        return ResponseEntity.ok(ApiResponse.success(estimate));
    }
    
    @Operation(summary = "견적 생성")
    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> createEstimate(
            @Valid @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Creating estimate for order: {} by user: {}", request.get("orderId"), userPrincipal.getId());
        
        Map<String, Object> estimate = new HashMap<>();
        estimate.put("id", 3);
        estimate.put("estimateCode", "EST202501003");
        estimate.put("orderId", request.get("orderId"));
        estimate.put("status", "draft");
        estimate.put("totalAmount", request.get("totalAmount"));
        estimate.put("currency", "KRW");
        estimate.put("createdAt", new Date().toString());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(estimate, "견적이 성공적으로 생성되었습니다."));
    }
    
    @Operation(summary = "견적 응답")
    @PostMapping("/{estimateId}/respond")
    public ResponseEntity<ApiResponse<Object>> respondToEstimate(
            @Parameter(description = "견적 ID") @PathVariable Long estimateId,
            @Valid @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String action = (String) request.get("action");
        log.info("Responding to estimate: {} with action: {} by user: {}", 
                estimateId, action, userPrincipal.getId());
        
        String message = "approved".equals(action) ? "견적이 승인되었습니다." : "견적이 거절되었습니다.";
        
        Map<String, Object> result = new HashMap<>();
        result.put("estimateId", estimateId);
        result.put("action", action);
        result.put("status", action);
        result.put("processedAt", new Date().toString());
        
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @Operation(summary = "견적 계산")
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> calculateEstimate(
            @Valid @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Calculating estimate for order: {}", request.get("orderId"));
        
        Map<String, Object> calculation = Map.of(
            "shippingCost", 25000,
            "packagingCost", 5000,
            "insuranceCost", 3000,
            "additionalCost", 2000,
            "totalAmount", 35000,
            "currency", "KRW"
        );
        
        return ResponseEntity.ok(ApiResponse.success(calculation));
    }

    @Operation(summary = "견적서 PDF 다운로드")
    @GetMapping("/{estimateId}/pdf")
    public ResponseEntity<byte[]> downloadEstimatePdf(
            @Parameter(description = "견적 ID") @PathVariable Long estimateId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Downloading estimate PDF: {} by user: {}", estimateId, userPrincipal.getId());
        
        // Mock PDF data
        String pdfContent = "Mock PDF content for estimate " + estimateId;
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=estimate_" + estimateId + ".pdf")
                .body(pdfContent.getBytes());
    }
}