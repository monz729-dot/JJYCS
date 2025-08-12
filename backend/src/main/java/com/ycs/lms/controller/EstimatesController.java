package com.ycs.lms.controller;

import com.ycs.lms.dto.*;
import com.ycs.lms.service.EstimatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Estimates", description = "견적 관리 API")
public class EstimatesController {

    private final EstimatesService estimatesService;

    @PostMapping("/orders/{orderId}/estimates")
    @Operation(summary = "견적 생성", description = "주문에 대한 견적을 생성합니다 (1차/2차)")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<EstimateResponse> createEstimate(
            @PathVariable Long orderId,
            @Valid @RequestBody EstimateCreateRequest request) {
        EstimateResponse response = estimatesService.createEstimate(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}/estimates")
    @Operation(summary = "견적 조회", description = "주문의 모든 견적을 조회합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<List<EstimateResponse>> getEstimates(@PathVariable Long orderId) {
        return ResponseEntity.ok(estimatesService.getEstimates(orderId));
    }

    @GetMapping("/estimates/{estimateId}")
    @Operation(summary = "견적 상세 조회", description = "견적 ID로 상세 정보를 조회합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER', 'WAREHOUSE', 'ADMIN')")
    public ResponseEntity<EstimateResponse> getEstimate(@PathVariable Long estimateId) {
        return ResponseEntity.ok(estimatesService.getEstimate(estimateId));
    }

    @PostMapping("/estimates/{estimateId}/respond")
    @Operation(summary = "견적 응답", description = "견적을 승인하거나 거부합니다")
    @PreAuthorize("hasAnyRole('USER', 'ENTERPRISE', 'PARTNER')")
    public ResponseEntity<EstimateResponse> respondToEstimate(
            @PathVariable Long estimateId,
            @Valid @RequestBody EstimateResponseRequest request) {
        return ResponseEntity.ok(estimatesService.respondToEstimate(estimateId, request));
    }

    @PutMapping("/estimates/{estimateId}")
    @Operation(summary = "견적 수정", description = "견적 정보를 수정합니다")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<EstimateResponse> updateEstimate(
            @PathVariable Long estimateId,
            @Valid @RequestBody EstimateCreateRequest request) {
        return ResponseEntity.ok(estimatesService.updateEstimate(estimateId, request));
    }

    @PostMapping("/estimates/{estimateId}/second")
    @Operation(summary = "2차 견적 생성", description = "1차 견적 기반으로 2차 견적을 생성합니다")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<EstimateResponse> createSecondEstimate(
            @PathVariable Long estimateId,
            @Valid @RequestBody EstimateCreateRequest request) {
        EstimateResponse response = estimatesService.createSecondEstimate(estimateId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/estimates/calculate")
    @Operation(summary = "견적 자동 계산", description = "주문 정보를 기반으로 견적을 자동 계산합니다")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<EstimateCalculationResponse> calculateEstimate(
            @RequestParam Long orderId,
            @RequestParam String shippingMethod,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) String serviceLevel) {
        return ResponseEntity.ok(estimatesService.calculateEstimate(orderId, shippingMethod, carrier, serviceLevel));
    }

    @DeleteMapping("/estimates/{estimateId}")
    @Operation(summary = "견적 취소", description = "견적을 취소합니다")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> cancelEstimate(@PathVariable Long estimateId) {
        estimatesService.cancelEstimate(estimateId);
        return ResponseEntity.noContent().build();
    }
}