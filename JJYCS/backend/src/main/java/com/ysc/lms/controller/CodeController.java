package com.ysc.lms.controller;

import com.ysc.lms.service.CodeGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 코드 생성 및 파싱 관리 API
 */
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
@Slf4j
public class CodeController {
    
    private final CodeGenerationService codeGenerationService;
    
    /**
     * 코드 파싱 정보 조회 (관리자용)
     */
    @GetMapping("/parse/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> parseCode(@PathVariable String code) {
        try {
            CodeGenerationService.CodeInfo codeInfo = codeGenerationService.parseCode(code);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", codeInfo
            ));
            
        } catch (Exception e) {
            log.error("Error parsing code: {}", code, e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "코드 파싱에 실패했습니다."));
        }
    }
    
    /**
     * HBL 번호 생성
     */
    @PostMapping("/hbl/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> generateHblNumber() {
        try {
            String hblNumber = codeGenerationService.generateHblNumber();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "hblNumber", hblNumber,
                "message", "HBL 번호가 생성되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Error generating HBL number", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", "HBL 번호 생성에 실패했습니다."));
        }
    }
    
    /**
     * 코드 구성요소 일괄 파싱 (여러 코드 한 번에 처리)
     */
    @PostMapping("/parse/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> parseCodes(@RequestBody BatchParseRequest request) {
        try {
            var results = request.codes.stream()
                .collect(java.util.stream.Collectors.toMap(
                    code -> code,
                    code -> codeGenerationService.parseCode(code)
                ));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "results", results
            ));
            
        } catch (Exception e) {
            log.error("Error parsing codes in batch", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "일괄 코드 파싱에 실패했습니다."));
        }
    }
    
    /**
     * 코드 유효성 검증
     */
    @PostMapping("/validate/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> validateCode(@PathVariable String code) {
        try {
            CodeGenerationService.CodeInfo codeInfo = codeGenerationService.parseCode(code);
            boolean isValid = codeInfo.getCodeType() != null;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isValid", isValid,
                "codeInfo", codeInfo
            ));
            
        } catch (Exception e) {
            log.error("Error validating code: {}", code, e);
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false, 
                    "isValid", false,
                    "error", "코드 유효성 검증에 실패했습니다."
                ));
        }
    }
    
    /**
     * 배치 파싱 요청 DTO
     */
    public static class BatchParseRequest {
        public java.util.List<String> codes;
        
        public java.util.List<String> getCodes() { return codes; }
        public void setCodes(java.util.List<String> codes) { this.codes = codes; }
    }
}