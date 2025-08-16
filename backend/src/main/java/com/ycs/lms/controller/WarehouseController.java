package com.ycs.lms.controller;

import com.ycs.lms.dto.warehouse.*;
import com.ycs.lms.service.WarehouseService;
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

import java.util.List;

/**
 * 창고 관리 컨트롤러
 * - QR 스캔 처리 (입고/출고/보류/믹스박스)
 * - 일괄 처리 (일괄 입고/출고)
 * - 재고 관리 및 조회
 * - 창고 작업 로그
 */
@Tag(name = "Warehouse", description = "창고 관리 API")
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * QR/라벨 스캔 처리
     * 스캔 타입에 따른 처리:
     * - inbound: 입고 처리
     * - outbound: 출고 처리  
     * - hold: 보류 처리
     * - mixbox: 믹스박스 처리
     */
    @Operation(summary = "QR/라벨 스캔", description = "QR 코드 또는 라벨 코드를 스캔하여 창고 작업을 수행합니다.")
    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ScanResponse>> processScan(
            @Valid @RequestBody ScanRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Processing scan - labelCode: {}, scanType: {}, by user: {}", 
                request.getLabelCode(), request.getScanType(), userPrincipal.getId());
        
        try {
            ScanResponse response = warehouseService.processScan(request, userPrincipal.getId());
            
            return ResponseEntity.ok(ApiResponse.success(response, 
                String.format("%s 처리가 완료되었습니다.", request.getScanType().getDisplayName())));
                
        } catch (LabelNotFoundException e) {
            log.warn("Label not found: {}", request.getLabelCode());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("LABEL_NOT_FOUND", "라벨 코드를 찾을 수 없습니다: " + request.getLabelCode()));
                    
        } catch (InvalidScanOperationException e) {
            log.warn("Invalid scan operation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_SCAN_OPERATION", e.getMessage()));
                    
        } catch (WarehouseCapacityExceededException e) {
            log.warn("Warehouse capacity exceeded: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("WAREHOUSE_CAPACITY_EXCEEDED", e.getMessage()));
        }
    }

    /**
     * 일괄 처리 (다중 선택 후 일괄 입고/출고)
     */
    @Operation(summary = "일괄 처리", description = "여러 박스를 한 번에 입고/출고 처리합니다.")
    @PostMapping("/batch-process")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<BatchProcessResponse>> batchProcess(
            @Valid @RequestBody BatchProcessRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Processing batch operation - action: {}, labelCodes: {}, by user: {}", 
                request.getAction(), request.getLabelCodes().size(), userPrincipal.getId());
        
        try {
            BatchProcessResponse response = warehouseService.batchProcess(request, userPrincipal.getId());
            
            return ResponseEntity.ok(ApiResponse.success(response, 
                String.format("일괄 %s 처리가 완료되었습니다. 성공: %d개, 실패: %d개", 
                    request.getAction().getDisplayName(), response.getProcessed(), response.getFailed())));
                    
        } catch (BatchProcessException e) {
            log.error("Batch process error", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("BATCH_PROCESS_ERROR", e.getMessage()));
        }
    }

    /**
     * 재고 조회 (창고별)
     */
    @Operation(summary = "재고 조회", description = "창고별 재고 현황을 조회합니다.")
    @GetMapping("/{warehouseId}/inventory")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryItem>>> getInventory(
            @Parameter(description = "창고 ID") @PathVariable Long warehouseId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "inboundDate") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(description = "상태 필터") @RequestParam(required = false) String status,
            @Parameter(description = "위치 필터") @RequestParam(required = false) String location,
            @Parameter(description = "검색어") @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting inventory for warehouse: {}, page: {}, size: {}", warehouseId, page, size);
        
        try {
            // 창고 접근 권한 확인
            if (!warehouseService.hasWarehouseAccess(warehouseId, userPrincipal.getId(), userPrincipal.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("WAREHOUSE_ACCESS_DENIED", "해당 창고에 접근 권한이 없습니다."));
            }
            
            Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            InventorySearchFilter filter = InventorySearchFilter.builder()
                    .warehouseId(warehouseId)
                    .status(status)
                    .location(location)
                    .search(search)
                    .build();
            
            PagedResponse<InventoryItem> inventory = warehouseService.getInventory(filter, pageable);
            return ResponseEntity.ok(ApiResponse.success(inventory));
            
        } catch (WarehouseNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 재고 통계 조회
     */
    @Operation(summary = "재고 통계", description = "창고별 재고 통계를 조회합니다.")
    @GetMapping("/{warehouseId}/inventory/stats")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryStats>> getInventoryStats(
            @Parameter(description = "창고 ID") @PathVariable Long warehouseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting inventory stats for warehouse: {}", warehouseId);
        
        try {
            if (!warehouseService.hasWarehouseAccess(warehouseId, userPrincipal.getId(), userPrincipal.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("WAREHOUSE_ACCESS_DENIED", "해당 창고에 접근 권한이 없습니다."));
            }
            
            InventoryStats stats = warehouseService.getInventoryStats(warehouseId);
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (WarehouseNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 스캔 이벤트 로그 조회
     */
    @Operation(summary = "스캔 로그 조회", description = "스캔 이벤트 로그를 조회합니다.")
    @GetMapping("/scan-events")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ScanEvent>>> getScanEvents(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "창고 ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "박스 ID") @RequestParam(required = false) Long boxId,
            @Parameter(description = "이벤트 타입") @RequestParam(required = false) String eventType,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) String startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting scan events - warehouseId: {}, boxId: {}", warehouseId, boxId);
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "scanTimestamp"));
            
            ScanEventSearchFilter filter = ScanEventSearchFilter.builder()
                    .warehouseId(warehouseId)
                    .boxId(boxId)
                    .eventType(eventType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .userId(userPrincipal.getRole().equals("ADMIN") ? null : userPrincipal.getId())
                    .build();
            
            PagedResponse<ScanEvent> events = warehouseService.getScanEvents(filter, pageable);
            return ResponseEntity.ok(ApiResponse.success(events));
            
        } catch (Exception e) {
            log.error("Error getting scan events", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_ERROR", "스캔 이벤트 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 라벨/QR 코드 생성
     */
    @Operation(summary = "라벨/QR 생성", description = "박스용 라벨과 QR 코드를 생성합니다.")
    @PostMapping("/generate-label")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<LabelGenerationResponse>> generateLabel(
            @Valid @RequestBody LabelGenerationRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Generating label for boxId: {}", request.getBoxId());
        
        try {
            LabelGenerationResponse response = warehouseService.generateLabel(request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(response, "라벨이 성공적으로 생성되었습니다."));
            
        } catch (BoxNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("BOX_NOT_FOUND", "박스를 찾을 수 없습니다."));
                    
        } catch (LabelGenerationException e) {
            log.error("Label generation error", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("LABEL_GENERATION_ERROR", "라벨 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 라벨 출력 (PDF)
     */
    @Operation(summary = "라벨 출력", description = "선택된 박스들의 라벨을 PDF로 생성합니다.")
    @PostMapping("/print-labels")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<byte[]> printLabels(
            @Valid @RequestBody PrintLabelsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Printing labels for boxes: {}", request.getBoxIds());
        
        try {
            byte[] pdfData = warehouseService.generateLabelsPdf(request, userPrincipal.getId());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=labels.pdf")
                    .body(pdfData);
                    
        } catch (Exception e) {
            log.error("Error generating labels PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 창고 위치 조회 (피킹 최적화용)
     */
    @Operation(summary = "창고 위치 조회", description = "창고 내 위치별 박스 현황을 조회합니다.")
    @GetMapping("/{warehouseId}/locations")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<WarehouseLocation>>> getWarehouseLocations(
            @Parameter(description = "창고 ID") @PathVariable Long warehouseId,
            @Parameter(description = "구역 필터") @RequestParam(required = false) String zone,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting warehouse locations for warehouse: {}", warehouseId);
        
        try {
            if (!warehouseService.hasWarehouseAccess(warehouseId, userPrincipal.getId(), userPrincipal.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("WAREHOUSE_ACCESS_DENIED", "해당 창고에 접근 권한이 없습니다."));
            }
            
            List<WarehouseLocation> locations = warehouseService.getWarehouseLocations(warehouseId, zone);
            return ResponseEntity.ok(ApiResponse.success(locations));
            
        } catch (WarehouseNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 리패킹 사진 업로드
     */
    @Operation(summary = "리패킹 사진 업로드", description = "리패킹 전후 사진을 업로드합니다.")
    @PostMapping("/upload-repack-photo")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'ADMIN')")
    public ResponseEntity<ApiResponse<RepackPhotoResponse>> uploadRepackPhoto(
            @Valid @RequestBody RepackPhotoUploadRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Uploading repack photo for boxId: {}, type: {}", request.getBoxId(), request.getType());
        
        try {
            RepackPhotoResponse response = warehouseService.uploadRepackPhoto(request, userPrincipal.getId());
            return ResponseEntity.ok(ApiResponse.success(response, "사진이 성공적으로 업로드되었습니다."));
            
        } catch (BoxNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("BOX_NOT_FOUND", "박스를 찾을 수 없습니다."));
                    
        } catch (FileUploadException e) {
            log.error("File upload error", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("FILE_UPLOAD_ERROR", "파일 업로드 중 오류가 발생했습니다."));
        }
    }
}

// 예외 클래스들 (별도 파일로 생성 예정)
class LabelNotFoundException extends RuntimeException {
    public LabelNotFoundException(String message) { super(message); }
}

class InvalidScanOperationException extends RuntimeException {
    public InvalidScanOperationException(String message) { super(message); }
}

class WarehouseCapacityExceededException extends RuntimeException {
    public WarehouseCapacityExceededException(String message) { super(message); }
}

class BatchProcessException extends RuntimeException {
    public BatchProcessException(String message) { super(message); }
}

class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(String message) { super(message); }
}

class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(String message) { super(message); }
}

class LabelGenerationException extends RuntimeException {
    public LabelGenerationException(String message) { super(message); }
}

class FileUploadException extends RuntimeException {
    public FileUploadException(String message) { super(message); }
}