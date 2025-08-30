package com.ysc.lms.controller;

import com.ysc.lms.dto.InboundLocationDto;
import com.ysc.lms.service.InboundLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * YCS 접수지 관리 컨트롤러 (어드민 전용)
 */
@RestController
@RequestMapping("/api/admin/inbound-locations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inbound Location Management", description = "YCS 접수지 관리 API (어드민 전용)")
public class InboundLocationController {
    
    private final InboundLocationService inboundLocationService;

    /**
     * 접수지 목록 조회 (페이징)
     * GET /api/admin/inbound-locations?page=0&size=10&isActive=true
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 목록 조회", 
        description = "YCS 접수지 목록을 페이징으로 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getInboundLocations(
            @Parameter(description = "활성 상태 필터")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Getting inbound locations: isActive={}, search={}, page={}", 
                isActive, search, pageable.getPageNumber());
            
            Page<InboundLocationDto> locations = inboundLocationService.getInboundLocations(
                isActive, search, pageable);
            
            response.put("success", true);
            response.put("data", locations.getContent());
            response.put("pagination", Map.of(
                "page", locations.getNumber(),
                "size", locations.getSize(),
                "totalElements", locations.getTotalElements(),
                "totalPages", locations.getTotalPages(),
                "hasNext", locations.hasNext(),
                "hasPrevious", locations.hasPrevious()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get inbound locations: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 목록 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 활성 접수지 목록 조회 (선택용)
     * GET /api/admin/inbound-locations/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GENERAL') or hasRole('CORPORATE')")
    @Operation(
        summary = "활성 접수지 목록", 
        description = "활성화된 접수지 목록을 표시 순서로 조회합니다. (주문 접수 시 선택용)"
    )
    public ResponseEntity<Map<String, Object>> getActiveInboundLocations() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<InboundLocationDto> locations = inboundLocationService.getActiveInboundLocations();
            
            response.put("success", true);
            response.put("data", locations);
            response.put("count", locations.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get active inbound locations: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "활성 접수지 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 접수지 상세 조회
     * GET /api/admin/inbound-locations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 상세 조회", 
        description = "특정 접수지의 상세 정보를 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getInboundLocation(
            @Parameter(description = "접수지 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Getting inbound location: {}", id);
            
            InboundLocationDto location = inboundLocationService.getInboundLocationById(id);
            
            if (location != null) {
                response.put("success", true);
                response.put("data", location);
            } else {
                response.put("success", false);
                response.put("message", "접수지를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get inbound location {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 새 접수지 등록
     * POST /api/admin/inbound-locations
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "새 접수지 등록", 
        description = "새로운 YCS 접수지를 등록합니다."
    )
    public ResponseEntity<Map<String, Object>> createInboundLocation(
            @Parameter(description = "접수지 등록 정보") 
            @Valid @RequestBody InboundLocationDto locationDto) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Creating inbound location: {}", locationDto.getName());
            
            InboundLocationDto created = inboundLocationService.createInboundLocation(locationDto);
            
            response.put("success", true);
            response.put("data", created);
            response.put("message", "접수지가 성공적으로 등록되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid inbound location data: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to create inbound location: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 등록 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 접수지 정보 수정
     * PUT /api/admin/inbound-locations/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 정보 수정", 
        description = "기존 접수지의 정보를 수정합니다."
    )
    public ResponseEntity<Map<String, Object>> updateInboundLocation(
            @Parameter(description = "접수지 ID") @PathVariable Long id,
            @Parameter(description = "수정할 접수지 정보") 
            @Valid @RequestBody InboundLocationDto locationDto) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Updating inbound location {}: {}", id, locationDto.getName());
            
            InboundLocationDto updated = inboundLocationService.updateInboundLocation(id, locationDto);
            
            if (updated != null) {
                response.put("success", true);
                response.put("data", updated);
                response.put("message", "접수지 정보가 성공적으로 수정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "접수지를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid inbound location update data: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to update inbound location {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 수정 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 접수지 활성/비활성 토글
     * PATCH /api/admin/inbound-locations/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 상태 토글", 
        description = "접수지의 활성/비활성 상태를 토글합니다."
    )
    public ResponseEntity<Map<String, Object>> toggleInboundLocationStatus(
            @Parameter(description = "접수지 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Toggling inbound location status: {}", id);
            
            InboundLocationDto updated = inboundLocationService.toggleInboundLocationStatus(id);
            
            if (updated != null) {
                response.put("success", true);
                response.put("data", updated);
                response.put("message", String.format("접수지가 %s되었습니다.", 
                    updated.getIsActive() ? "활성화" : "비활성화"));
            } else {
                response.put("success", false);
                response.put("message", "접수지를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to toggle inbound location status {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 상태 변경 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 접수지 표시 순서 변경
     * PATCH /api/admin/inbound-locations/{id}/display-order
     */
    @PatchMapping("/{id}/display-order")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 표시 순서 변경", 
        description = "접수지의 표시 순서를 변경합니다."
    )
    public ResponseEntity<Map<String, Object>> updateDisplayOrder(
            @Parameter(description = "접수지 ID") @PathVariable Long id,
            @Parameter(description = "새 표시 순서") @RequestParam Integer displayOrder) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Updating display order for inbound location {}: {}", id, displayOrder);
            
            InboundLocationDto updated = inboundLocationService.updateDisplayOrder(id, displayOrder);
            
            if (updated != null) {
                response.put("success", true);
                response.put("data", updated);
                response.put("message", "표시 순서가 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "접수지를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to update display order for inbound location {}: {}", 
                id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "표시 순서 변경 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 접수지 삭제 (실제로는 비활성화)
     * DELETE /api/admin/inbound-locations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "접수지 삭제", 
        description = "접수지를 삭제합니다. (실제로는 비활성화)"
    )
    public ResponseEntity<Map<String, Object>> deleteInboundLocation(
            @Parameter(description = "접수지 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Deleting inbound location: {}", id);
            
            boolean deleted = inboundLocationService.deleteInboundLocation(id);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "접수지가 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "접수지를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("Cannot delete inbound location {}: {}", id, e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to delete inbound location {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "접수지 삭제 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}