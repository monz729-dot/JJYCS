package com.ycs.lms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        log.error("Business exception occurred: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());
        response.put("code", e.getErrorCode());
        
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
    
    /**
     * 유효성 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "입력값이 올바르지 않습니다.");
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("details", fieldErrors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 권한 없음
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "해당 작업을 수행할 권한이 없습니다.");
        response.put("code", "ACCESS_DENIED");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * 리소스를 찾을 수 없음
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());
        response.put("code", "RESOURCE_NOT_FOUND");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 외부 API 연동 실패
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, Object>> handleExternalApiException(ExternalApiException e) {
        log.error("External API error: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "외부 서비스 연동 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        response.put("code", "EXTERNAL_API_ERROR");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 네트워크 연결 실패
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException e) {
        log.error("Network error: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.");
        response.put("code", "NETWORK_ERROR");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * IllegalArgumentException 처리 (잘못된 파라미터)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        // 사용자 친화적 메시지로 변환
        String userMessage = convertToUserFriendlyMessage(e.getMessage());
        response.put("error", userMessage);
        response.put("code", "INVALID_ARGUMENT");
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * IllegalStateException 처리 (잘못된 상태)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException e) {
        log.warn("Invalid state: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        // 사용자 친화적 메시지로 변환
        String userMessage = convertToUserFriendlyMessage(e.getMessage());
        response.put("error", userMessage);
        response.put("code", "INVALID_STATE");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * 기본 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        response.put("code", "INTERNAL_ERROR");
        
        // 개발 환경에서만 상세 오류 표시
        if (isDevEnvironment()) {
            response.put("debug", e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 기술적 메시지를 사용자 친화적 메시지로 변환
     */
    private String convertToUserFriendlyMessage(String technicalMessage) {
        if (technicalMessage == null) {
            return "요청 처리 중 오류가 발생했습니다.";
        }
        
        // 일반적인 오류 메시지 변환
        if (technicalMessage.contains("Order not found")) {
            return "주문을 찾을 수 없습니다.";
        }
        if (technicalMessage.contains("User not found")) {
            return "사용자를 찾을 수 없습니다.";
        }
        if (technicalMessage.contains("cannot be modified")) {
            return "현재 상태에서는 주문을 수정할 수 없습니다.";
        }
        if (technicalMessage.contains("cannot be cancelled")) {
            return "현재 상태에서는 주문을 취소할 수 없습니다.";
        }
        if (technicalMessage.contains("already exists")) {
            return "이미 존재하는 데이터입니다.";
        }
        if (technicalMessage.contains("Invalid status")) {
            return "올바르지 않은 상태값입니다.";
        }
        if (technicalMessage.contains("permission") || technicalMessage.contains("unauthorized")) {
            return "이 작업을 수행할 권한이 없습니다.";
        }
        
        // HS Code 관련
        if (technicalMessage.contains("HS") || technicalMessage.contains("hscode")) {
            return "HS Code 검증에 실패했습니다. 올바른 코드인지 확인해주세요.";
        }
        
        // 기본 메시지
        return "요청 처리 중 오류가 발생했습니다.";
    }
    
    private boolean isDevEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local");
    }
}