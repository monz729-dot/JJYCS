package com.ysc.lms.controller;

import com.ysc.lms.dto.CourierCompanyDto;
import com.ysc.lms.service.CourierCompanyService;
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
 * 택배사 관리 컨트롤러 (어드민 전용)
 */
@RestController
@RequestMapping("/api/admin/courier-companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Courier Company Management", description = "택배사 관리 API (어드민 전용)")
public class CourierCompanyController {
    
    private final CourierCompanyService courierCompanyService;

    /**
     * 택배사 목록 조회 (페이징)
     * GET /api/admin/courier-companies?page=0&size=10&isActive=true
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "택배사 목록 조회", 
        description = "택배사 목록을 페이징으로 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getCourierCompanies(
            @Parameter(description = "활성 상태 필터")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "검색 키워드 (코드/이름)")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) 
            Pageable pageable) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Getting courier companies: isActive={}, search={}, page={}", 
                isActive, search, pageable.getPageNumber());
            
            Page<CourierCompanyDto> companies = courierCompanyService.getCourierCompanies(
                isActive, search, pageable);
            
            response.put("success", true);
            response.put("data", companies.getContent());
            response.put("pagination", Map.of(
                "page", companies.getNumber(),
                "size", companies.getSize(),
                "totalElements", companies.getTotalElements(),
                "totalPages", companies.getTotalPages(),
                "hasNext", companies.hasNext(),
                "hasPrevious", companies.hasPrevious()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get courier companies: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 목록 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 활성 택배사 목록 조회 (선택용)
     * GET /api/admin/courier-companies/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GENERAL') or hasRole('CORPORATE')")
    @Operation(
        summary = "활성 택배사 목록", 
        description = "활성화된 택배사 목록을 표시 순서로 조회합니다. (주문 접수 시 선택용)"
    )
    public ResponseEntity<Map<String, Object>> getActiveCourierCompanies() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CourierCompanyDto> companies = courierCompanyService.getActiveCourierCompanies();
            
            response.put("success", true);
            response.put("data", companies);
            response.put("count", companies.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get active courier companies: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "활성 택배사 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 추적 기능 지원 택배사 목록
     * GET /api/admin/courier-companies/with-tracking
     */
    @GetMapping("/with-tracking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GENERAL') or hasRole('CORPORATE')")
    @Operation(
        summary = "추적 지원 택배사 목록", 
        description = "송장 추적 기능을 지원하는 택배사 목록을 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getCourierCompaniesWithTracking() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CourierCompanyDto> companies = courierCompanyService.getCourierCompaniesWithTracking();
            
            response.put("success", true);
            response.put("data", companies);
            response.put("count", companies.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get courier companies with tracking: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "추적 지원 택배사 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 택배사 상세 조회
     * GET /api/admin/courier-companies/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "택배사 상세 조회", 
        description = "특정 택배사의 상세 정보를 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getCourierCompany(
            @Parameter(description = "택배사 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Getting courier company: {}", id);
            
            CourierCompanyDto company = courierCompanyService.getCourierCompanyById(id);
            
            if (company != null) {
                response.put("success", true);
                response.put("data", company);
            } else {
                response.put("success", false);
                response.put("message", "택배사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get courier company {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 새 택배사 등록
     * POST /api/admin/courier-companies
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "새 택배사 등록", 
        description = "새로운 택배사를 등록합니다."
    )
    public ResponseEntity<Map<String, Object>> createCourierCompany(
            @Parameter(description = "택배사 등록 정보") 
            @Valid @RequestBody CourierCompanyDto companyDto) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Creating courier company: {} [{}]", companyDto.getName(), companyDto.getCode());
            
            CourierCompanyDto created = courierCompanyService.createCourierCompany(companyDto);
            
            response.put("success", true);
            response.put("data", created);
            response.put("message", "택배사가 성공적으로 등록되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid courier company data: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to create courier company: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 등록 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 택배사 정보 수정
     * PUT /api/admin/courier-companies/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "택배사 정보 수정", 
        description = "기존 택배사의 정보를 수정합니다."
    )
    public ResponseEntity<Map<String, Object>> updateCourierCompany(
            @Parameter(description = "택배사 ID") @PathVariable Long id,
            @Parameter(description = "수정할 택배사 정보") 
            @Valid @RequestBody CourierCompanyDto companyDto) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Updating courier company {}: {} [{}]", id, companyDto.getName(), companyDto.getCode());
            
            CourierCompanyDto updated = courierCompanyService.updateCourierCompany(id, companyDto);
            
            if (updated != null) {
                response.put("success", true);
                response.put("data", updated);
                response.put("message", "택배사 정보가 성공적으로 수정되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "택배사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid courier company update data: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to update courier company {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 수정 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 택배사 활성/비활성 토글
     * PATCH /api/admin/courier-companies/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "택배사 상태 토글", 
        description = "택배사의 활성/비활성 상태를 토글합니다."
    )
    public ResponseEntity<Map<String, Object>> toggleCourierCompanyStatus(
            @Parameter(description = "택배사 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Toggling courier company status: {}", id);
            
            CourierCompanyDto updated = courierCompanyService.toggleCourierCompanyStatus(id);
            
            if (updated != null) {
                response.put("success", true);
                response.put("data", updated);
                response.put("message", String.format("택배사가 %s되었습니다.", 
                    updated.getIsActive() ? "활성화" : "비활성화"));
            } else {
                response.put("success", false);
                response.put("message", "택배사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to toggle courier company status {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 상태 변경 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 송장번호 추적 URL 생성
     * GET /api/admin/courier-companies/{code}/tracking-url?trackingNumber=123456789
     */
    @GetMapping("/{code}/tracking-url")
    @PreAuthorize("hasRole('ADMIN') or hasRole('GENERAL') or hasRole('CORPORATE')")
    @Operation(
        summary = "송장 추적 URL 생성", 
        description = "택배사 코드와 송장번호로 추적 URL을 생성합니다."
    )
    public ResponseEntity<Map<String, Object>> generateTrackingUrl(
            @Parameter(description = "택배사 코드") @PathVariable String code,
            @Parameter(description = "송장번호") @RequestParam String trackingNumber) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Generating tracking URL: {} - {}", code, trackingNumber);
            
            String trackingUrl = courierCompanyService.generateTrackingUrl(code, trackingNumber);
            
            if (trackingUrl != null) {
                response.put("success", true);
                response.put("trackingUrl", trackingUrl);
                response.put("courierCode", code);
                response.put("trackingNumber", trackingNumber);
            } else {
                response.put("success", false);
                response.put("message", "추적 URL을 생성할 수 없습니다. 택배사 정보나 추적 템플릿을 확인해주세요.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to generate tracking URL for {} - {}: {}", code, trackingNumber, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "추적 URL 생성 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 택배사 삭제 (실제로는 비활성화)
     * DELETE /api/admin/courier-companies/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "택배사 삭제", 
        description = "택배사를 삭제합니다. (실제로는 비활성화)"
    )
    public ResponseEntity<Map<String, Object>> deleteCourierCompany(
            @Parameter(description = "택배사 ID") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Deleting courier company: {}", id);
            
            boolean deleted = courierCompanyService.deleteCourierCompany(id);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "택배사가 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "택배사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("Cannot delete courier company {}: {}", id, e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("Failed to delete courier company {}: {}", id, e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "택배사 삭제 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}