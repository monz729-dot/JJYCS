package com.ysc.lms.exception;

import com.ysc.lms.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, WebRequest request) {
        log.error("Business exception occurred: {}", e.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(e.getMessage(), e.getErrorCode());
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }
    
    /**
     * 유효성 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e, WebRequest request) {
        log.warn("Validation failed: {}", e.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        ApiResponse<Map<String, String>> response = ApiResponse.error("입력값이 올바르지 않습니다.", "VALIDATION_FAILED");
        response.setData(fieldErrors);
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 인증 실패 (JWT 토큰 없음, 만료, 변조 등)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "인증에 실패했습니다. 다시 로그인해주세요.");
        response.put("code", "AUTHENTICATION_FAILED");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * 권한 없음 (인증은 되었지만 접근 권한이 없는 경우)
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
     * 존재하지 않는 URL 경로 (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("No handler found for request: {} {}", e.getHttpMethod(), e.getRequestURL());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "요청하신 페이지를 찾을 수 없습니다.");
        response.put("code", "NOT_FOUND");
        response.put("path", e.getRequestURL());
        
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
     * 데이터 무결성 위반 예외 처리 (중복 키, NOT NULL 제약 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "DATA_INTEGRITY_VIOLATION");
        
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("duplicate") || message.contains("unique") || message.contains("already exists")) {
                response.put("error", "이미 사용 중인 데이터입니다. 다른 값을 입력해주세요.");
            } else if (message.contains("cannot be null") || message.contains("not-null")) {
                response.put("error", "필수 정보가 누락되었습니다.");
            } else {
                response.put("error", "데이터 제약조건을 위반했습니다.");
            }
        } else {
            response.put("error", "데이터 저장 중 오류가 발생했습니다.");
        }
        
        // 개발환경에서만 상세 정보 제공
        if (isDevEnvironment()) {
            response.put("debug", e.getMostSpecificCause().getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * 데이터베이스 접근 예외 처리
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException e) {
        log.error("Database access error: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "데이터베이스 접근 중 오류가 발생했습니다.");
        response.put("code", "DATABASE_ERROR");
        
        if (isDevEnvironment()) {
            response.put("debug", e.getMostSpecificCause().getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * SQL 예외 처리
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException e) {
        log.error("SQL error: {} - {}", e.getSQLState(), e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "데이터베이스 쿼리 실행 중 오류가 발생했습니다.");
        response.put("code", "SQL_ERROR");
        
        if (isDevEnvironment()) {
            response.put("debug", String.format("SQL State: %s, Error Code: %d, Message: %s", 
                e.getSQLState(), e.getErrorCode(), e.getMessage()));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 트랜잭션 예외 처리
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionSystemException(TransactionSystemException e) {
        log.error("Transaction error: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "트랜잭션 처리 중 오류가 발생했습니다.");
        response.put("code", "TRANSACTION_ERROR");
        
        if (isDevEnvironment()) {
            response.put("debug", e.getMostSpecificCause().getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * JSON 파싱 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("JSON parsing error: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "요청 데이터 형식이 올바르지 않습니다.");
        response.put("code", "JSON_PARSING_ERROR");
        
        if (isDevEnvironment()) {
            response.put("debug", e.getMostSpecificCause().getMessage());
        }
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Parameter type mismatch: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName()));
        response.put("code", "PARAMETER_TYPE_MISMATCH");
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("Missing required parameter: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName()));
        response.put("code", "MISSING_PARAMETER");
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Constraint 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "입력값 제약조건을 위반했습니다.");
        response.put("code", "CONSTRAINT_VIOLATION");
        
        // 상세한 제약조건 위반 정보
        Map<String, String> violations = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            violations.put(propertyPath, violation.getMessage());
        });
        response.put("violations", violations);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 기본 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");
        response.put("code", "INTERNAL_ERROR");
        
        // 개발 환경에서만 상세 오류 표시
        if (isDevEnvironment()) {
            response.put("debug", e.getMessage());
            response.put("exception", e.getClass().getSimpleName());
            // 스택 트레이스의 첫 3줄만 포함
            StackTraceElement[] stack = e.getStackTrace();
            if (stack.length > 0) {
                StringBuilder stackTrace = new StringBuilder();
                for (int i = 0; i < Math.min(3, stack.length); i++) {
                    stackTrace.append(stack[i].toString()).append("\n");
                }
                response.put("stackTrace", stackTrace.toString());
            }
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
    
    /**
     * HTTP 클라이언트 에러 (4xx) 처리
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpClientErrorException(HttpClientErrorException e, WebRequest request) {
        log.warn("HTTP client error: {} - {}", e.getStatusCode(), e.getMessage());
        
        String userMessage = switch (e.getStatusCode().value()) {
            case 400 -> "잘못된 요청입니다. 입력값을 확인해주세요.";
            case 401 -> "인증이 필요합니다. 로그인 후 다시 시도해주세요.";
            case 403 -> "해당 작업을 수행할 권한이 없습니다.";
            case 404 -> "요청한 리소스를 찾을 수 없습니다.";
            case 429 -> "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.";
            default -> "외부 서비스 요청 중 오류가 발생했습니다.";
        };
        
        ApiResponse<Object> response = ApiResponse.error(userMessage, "HTTP_CLIENT_ERROR");
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.status(e.getStatusCode()).body(response);
    }
    
    /**
     * HTTP 서버 에러 (5xx) 처리
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpServerErrorException(HttpServerErrorException e, WebRequest request) {
        log.error("HTTP server error: {} - {}", e.getStatusCode(), e.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "외부 서비스에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", 
            "EXTERNAL_SERVER_ERROR"
        );
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 택배 API 관련 타임아웃 처리
     */
    @ExceptionHandler(java.net.SocketTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleSocketTimeoutException(java.net.SocketTimeoutException e, WebRequest request) {
        log.error("Socket timeout: {}", e.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "택배사 서비스 응답시간이 초과되었습니다. 잠시 후 다시 시도해주세요.",
            "TIMEOUT_ERROR"
        );
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
    }
    
    /**
     * 연결 거부 처리 (택배사 API 서버 다운 등)
     */
    @ExceptionHandler(java.net.ConnectException.class)
    public ResponseEntity<ApiResponse<Object>> handleConnectException(java.net.ConnectException e, WebRequest request) {
        log.error("Connection refused: {}", e.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "택배사 서비스에 연결할 수 없습니다. 서비스 점검 중일 수 있습니다.",
            "CONNECTION_REFUSED"
        );
        response.setPath(getRequestPath(request));
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 요청 경로 추출 헬퍼 메서드
     */
    private String getRequestPath(WebRequest request) {
        try {
            return request.getDescription(false).replace("uri=", "");
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private boolean isDevEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("local") || profile.contains("supabase");
    }
}