package com.ycs.lms.controller;

import com.ycs.lms.dto.*;
import com.ycs.lms.service.WarehouseService;
import com.ycs.lms.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse", description = "창고 관리 API")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/scan")
    @Operation(summary = "창고 스캔", description = "라벨/QR 코드를 스캔하여 입출고 처리")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ScanResponse>> scanBox(@Valid @RequestBody ScanRequest request) {
        ScanResponse response = warehouseService.scanBox(request);
        return ResponseEntity.ok(ApiResponse.success(response, "스캔이 완료되었습니다."));
    }

    @PostMapping("/batch-process")
    @Operation(summary = "일괄 처리", description = "여러 박스를 일괄 입출고 처리")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<BatchProcessResponse> batchProcess(@Valid @RequestBody BatchProcessRequest request) {
        BatchProcessResponse response = warehouseService.batchProcess(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{warehouseId}/inventory")
    @Operation(summary = "재고 조회", description = "창고별 재고 현황 조회")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<PageResponse<InventoryResponse>> getInventory(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getInventory(warehouseId, status, pageable));
    }

    @PostMapping("/hold/{boxId}")
    @Operation(summary = "박스 보류", description = "박스를 보류 상태로 변경")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<BoxResponse> holdBox(
            @PathVariable Long boxId,
            @RequestParam String reason) {
        return ResponseEntity.ok(warehouseService.holdBox(boxId, reason));
    }

    @PostMapping("/mixbox")
    @Operation(summary = "믹스박스 생성", description = "여러 주문의 박스를 하나로 합침")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<MixboxResponse> createMixbox(@Valid @RequestBody MixboxRequest request) {
        return ResponseEntity.ok(warehouseService.createMixbox(request));
    }

    @GetMapping("/scan-history")
    @Operation(summary = "스캔 이력 조회", description = "박스 스캔 이력 조회")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<PageResponse<ScanEventResponse>> getScanHistory(
            @RequestParam(required = false) String labelCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {
        return ResponseEntity.ok(warehouseService.getScanHistory(labelCode, startDate, endDate, pageable));
    }

    @PostMapping("/label/generate")
    @Operation(summary = "라벨 생성", description = "박스 라벨 및 QR 코드 생성")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<LabelResponse> generateLabel(@RequestParam Long boxId) {
        return ResponseEntity.ok(warehouseService.generateLabel(boxId));
    }

    @PostMapping("/label/print")
    @Operation(summary = "라벨 출력", description = "라벨 출력 요청")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<Void> printLabel(@RequestParam Long boxId) {
        warehouseService.printLabel(boxId);
        return ResponseEntity.noContent().build();
    }
}