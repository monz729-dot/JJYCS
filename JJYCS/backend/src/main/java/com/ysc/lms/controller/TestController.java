package com.ysc.lms.controller;

import com.ysc.lms.dto.ApiResponse;
import com.ysc.lms.service.EmailService;
import com.ysc.lms.service.CodeGenerationService;
import com.ysc.lms.service.ParcelTrackingService;
import com.ysc.lms.service.CarrierApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final EmailService emailService;
    private final CodeGenerationService codeGenerationService;
    private final ParcelTrackingService parcelTrackingService;
    private final CarrierApiService carrierApiService;
    
    @GetMapping("/password")
    public Map<String, String> generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        return Map.of(
            "raw", rawPassword,
            "encoded", encodedPassword
        );
    }
    
    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            emailService.sendTestEmail(email);
            
            response.put("success", true);
            response.put("message", "테스트 이메일이 성공적으로 발송되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/verification-email")
    public ResponseEntity<Map<String, Object>> testVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String name = request.get("name");
            String token = UUID.randomUUID().toString();
            
            emailService.sendVerificationEmail(email, name, token);
            
            response.put("success", true);
            response.put("message", "인증 이메일이 성공적으로 발송되었습니다.");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/password-reset-email")
    public ResponseEntity<Map<String, Object>> testPasswordResetEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String name = request.get("name");
            String token = UUID.randomUUID().toString();
            
            emailService.sendPasswordResetEmail(email, name, token);
            
            response.put("success", true);
            response.put("message", "비밀번호 재설정 이메일이 성공적으로 발송되었습니다.");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * P0-1: 코드 파싱 테스트
     */
    @GetMapping("/p0/parse/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> testParseCode(@PathVariable String code) {
        try {
            log.info("Testing code parsing for: {}", code);
            
            CodeGenerationService.CodeInfo codeInfo = codeGenerationService.parseCode(code);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", codeInfo,
                "message", "코드 파싱 성공"
            ));
            
        } catch (Exception e) {
            log.error("Error parsing code: {}", code, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * P0-1: HBL 번호 생성 테스트
     */
    @PostMapping("/p0/hbl/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testGenerateHblNumber() {
        try {
            log.info("Testing HBL number generation");
            
            String hblNumber = codeGenerationService.generateHblNumber();
            
            Map<String, Object> data = Map.of(
                "hblNumber", hblNumber,
                "generatedAt", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, "HBL 번호 생성 성공"));
            
        } catch (Exception e) {
            log.error("Error generating HBL number", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("HBL 번호 생성 실패: " + e.getMessage(), "HBL_GENERATION_ERROR"));
        }
    }
    
    /**
     * P0-2: 실제 택배사 API 연동 테스트
     */
    @PostMapping("/p0/carrier/real-api")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<ApiResponse<Object>> testRealCarrierApi(@RequestBody Map<String, String> request) {
        try {
            String carrier = request.get("carrier");
            String trackingNumber = request.get("trackingNumber");
            
            log.info("Testing real carrier API for: {} - {}", carrier, trackingNumber);
            
            CarrierApiService.TrackingResult result = carrierApiService.getTrackingInfo(carrier, trackingNumber);
            
            Map<String, Object> data = Map.of(
                "carrier", carrier,
                "trackingNumber", trackingNumber,
                "success", result.isSuccess(),
                "status", result.getStatus() != null ? result.getStatus() : "N/A",
                "location", result.getLocation() != null ? result.getLocation() : "N/A",
                "time", result.getTime() != null ? result.getTime() : "N/A",
                "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "N/A",
                "details", result.getDetails()
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(data, "실제 택배사 API 연동 성공"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(result.getErrorMessage(), "CARRIER_API_ERROR"));
            }
            
        } catch (Exception e) {
            log.error("Error testing real carrier API", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("택배사 API 테스트 실패: " + e.getMessage(), "CARRIER_API_TEST_ERROR"));
        }
    }
    
    /**
     * P0-2: 송장번호 검증 테스트
     */
    @PostMapping("/p0/tracking/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> testValidateTracking(@RequestBody Map<String, String> request) {
        try {
            String carrier = request.get("carrier");
            String trackingNumber = request.get("trackingNumber");
            
            log.info("Testing tracking validation for: {} - {}", carrier, trackingNumber);
            
            ParcelTrackingService.ValidationResult result = parcelTrackingService.validateTrackingNumber(carrier, trackingNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "status", result.getStatus(),
                    "message", result.getMessage(),
                    "trackingStatus", result.getTrackingStatus(),
                    "isValid", result.isValid()
                ),
                "message", "송장번호 검증 완료"
            ));
            
        } catch (Exception e) {
            log.error("Error validating tracking number", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * P0-2: 입고 스캔 매칭 테스트
     */
    @PostMapping("/p0/tracking/match")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> testInboundMatching(@RequestBody Map<String, String> request) {
        try {
            String trackingNumber = request.get("trackingNumber");
            
            log.info("Testing inbound matching for: {}", trackingNumber);
            
            ParcelTrackingService.MatchingResult result = parcelTrackingService.matchInboundScan(trackingNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "data", Map.of(
                    "message", result.getMessage(),
                    "needsManualSearch", result.isNeedsManualSearch(),
                    "alreadyMatched", result.isAlreadyMatched(),
                    "orderId", result.getOrder() != null ? result.getOrder().getId() : null
                ),
                "message", "매칭 테스트 완료"
            ));
            
        } catch (Exception e) {
            log.error("Error testing inbound matching", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * P0-2: 수동 매칭 검색 테스트
     */
    @GetMapping("/p0/tracking/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> testManualSearchMatching(@RequestParam String keyword) {
        try {
            log.info("Testing manual matching search for: {}", keyword);
            
            List<com.ysc.lms.entity.Parcel> results = parcelTrackingService.searchForManualMatching(keyword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "results", results.stream().map(parcel -> Map.of(
                        "id", parcel.getId(),
                        "trackingNumber", parcel.getTrackingNumber(),
                        "carrier", parcel.getCarrier(),
                        "orderId", parcel.getOrder() != null ? parcel.getOrder().getId() : null
                    )).toList(),
                    "count", results.size()
                ),
                "message", "수동 매칭 검색 완료"
            ));
            
        } catch (Exception e) {
            log.error("Error testing manual matching search", e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * 관리자 권한 테스트
     */
    @GetMapping("/p0/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testAdminAccess() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "관리자 권한 확인됨",
            "role", "ADMIN"
        ));
    }
}