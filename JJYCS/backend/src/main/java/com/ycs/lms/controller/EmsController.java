package com.ycs.lms.controller;

import com.ycs.lms.service.EmsApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EMS API 컨트롤러 - 태국 전용 기능
 */
@RestController
@RequestMapping("/api/ems")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "EMS API", description = "EMS 관련 API (태국 전용)")
public class EmsController {
    
    private final EmsApiService emsApiService;

    /**
     * 태국 우편번호 검색
     * GET /api/ems/postal?country=TH&q=10110
     */
    @GetMapping("/postal")
    @Operation(
        summary = "태국 우편번호 검색", 
        description = "태국의 우편번호를 검색합니다. EMS API 실패 시 로컬 데이터 폴백"
    )
    public ResponseEntity<Map<String, Object>> searchPostalCode(
            @Parameter(description = "국가 코드 (고정값: TH)")
            @RequestParam(defaultValue = "TH") String country,
            @Parameter(description = "검색 쿼리 (우편번호 또는 지역명)")
            @RequestParam String q) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 태국만 지원
            if (!"TH".equalsIgnoreCase(country)) {
                response.put("success", false);
                response.put("message", "현재 태국(TH)만 지원됩니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Searching postal codes for country={}, query={}", country, q);
            
            List<EmsApiService.PostalCodeResult> results = emsApiService.searchPostalCode(q);
            
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            response.put("message", results.isEmpty() ? "검색 결과가 없습니다." : "검색 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Postal code search error: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "우편번호 검색 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * HS 코드 검색 (HD API 폴백용)
     * GET /api/ems/hscode?q=chocolate
     */
    @GetMapping("/hscode")
    @Operation(
        summary = "HS 코드 검색 (폴백)", 
        description = "HD API 실패 시 EMS API를 통한 HS 코드 검색"
    )
    public ResponseEntity<Map<String, Object>> searchHsCode(
            @Parameter(description = "검색 키워드")
            @RequestParam String q) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Searching HS codes with keyword: {}", q);
            
            List<EmsApiService.HsCodeResult> results = emsApiService.searchHsCode(q);
            
            response.put("success", true);
            response.put("data", results);
            response.put("count", results.size());
            response.put("message", results.isEmpty() ? "검색 결과가 없습니다." : "검색 성공");
            response.put("fallback", true); // 폴백 API임을 명시
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("HS code search error: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "HS 코드 검색 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * EMS 신청내역 조회
     * GET /api/ems/application/{emsNumber}?shipperName=홍길동
     */
    @GetMapping("/application/{emsNumber}")
    @Operation(
        summary = "EMS 신청내역 조회", 
        description = "주문 상세 페이지에서 EMS 신청내역을 조회합니다"
    )
    public ResponseEntity<Map<String, Object>> getEmsApplication(
            @Parameter(description = "EMS 번호")
            @PathVariable String emsNumber,
            @Parameter(description = "발송인명")
            @RequestParam String shipperName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Looking up EMS application: {} by {}", emsNumber, shipperName);
            
            EmsApiService.EmsApplicationResult result = emsApiService.getEmsApplication(emsNumber, shipperName);
            
            if (result != null) {
                response.put("success", true);
                response.put("data", result);
                response.put("message", "EMS 신청내역 조회 성공");
            } else {
                response.put("success", false);
                response.put("message", "EMS 신청내역을 찾을 수 없습니다.");
                response.put("code", "NOT_FOUND");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("EMS application lookup error: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "EMS 신청내역 조회 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * EMS 신청내역 새로고침
     * POST /api/ems/application/{emsNumber}/refresh
     */
    @PostMapping("/application/{emsNumber}/refresh")
    @Operation(
        summary = "EMS 신청내역 새로고침", 
        description = "캐시된 EMS 신청내역을 새로 조회합니다"
    )
    public ResponseEntity<Map<String, Object>> refreshEmsApplication(
            @Parameter(description = "EMS 번호")
            @PathVariable String emsNumber,
            @Parameter(description = "발송인명")
            @RequestParam String shipperName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Refreshing EMS application: {} by {}", emsNumber, shipperName);
            
            // 캐시 무시하고 새로 조회
            EmsApiService.EmsApplicationResult result = emsApiService.getEmsApplication(emsNumber, shipperName);
            
            if (result != null) {
                response.put("success", true);
                response.put("data", result);
                response.put("message", "EMS 신청내역 새로고침 완료");
                response.put("refreshed", true);
            } else {
                response.put("success", false);
                response.put("message", "EMS 신청내역을 찾을 수 없습니다.");
                response.put("code", "NOT_FOUND");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("EMS application refresh error: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("message", "EMS 신청내역 새로고침 중 오류가 발생했습니다.");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * EMS API 상태 확인
     * GET /api/ems/status
     */
    @GetMapping("/status")
    @Operation(
        summary = "EMS API 상태 확인", 
        description = "EMS API 연결 상태를 확인합니다"
    )
    public ResponseEntity<Map<String, Object>> getEmsApiStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 간단한 상태 체크 (태국 우편번호 검색 테스트)
            List<EmsApiService.PostalCodeResult> testResult = emsApiService.searchPostalCode("10110");
            
            response.put("success", true);
            response.put("status", "connected");
            response.put("message", "EMS API 연결 정상");
            response.put("testQuery", "10110");
            response.put("testResultCount", testResult.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("EMS API status check error: {}", e.getMessage(), e);
            
            response.put("success", false);
            response.put("status", "disconnected");
            response.put("message", "EMS API 연결 실패");
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response); // 200 OK로 상태 반환
        }
    }
}