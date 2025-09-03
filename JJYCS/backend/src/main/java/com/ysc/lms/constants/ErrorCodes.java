package com.ysc.lms.constants;

/**
 * 표준 에러 코드 상수 정의
 * 프론트엔드에서 에러 처리를 위한 표준화된 코드
 */
public class ErrorCodes {
    
    // 4xx Client Errors
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String DUPLICATE_EMAIL = "DUPLICATE_EMAIL";
    public static final String DUPLICATE_USERNAME = "DUPLICATE_USERNAME";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String ACCOUNT_PENDING = "ACCOUNT_PENDING";
    public static final String ACCOUNT_REJECTED = "ACCOUNT_REJECTED";
    public static final String ACCOUNT_SUSPENDED = "ACCOUNT_SUSPENDED";
    public static final String INVALID_USER_TYPE = "INVALID_USER_TYPE";
    public static final String JSON_PARSING_ERROR = "JSON_PARSING_ERROR";
    public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    public static final String PARAMETER_TYPE_MISMATCH = "PARAMETER_TYPE_MISMATCH";
    public static final String CONSTRAINT_VIOLATION = "CONSTRAINT_VIOLATION";
    
    // Auth specific
    public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    
    // Business Logic Errors  
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
    
    // Order specific errors
    public static final String FORBIDDEN_NOT_APPROVED = "FORBIDDEN_NOT_APPROVED";
    public static final String INVALID_STATUS_TRANSITION = "INVALID_STATUS_TRANSITION";
    
    // 5xx Server Errors
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String SQL_ERROR = "SQL_ERROR";
    public static final String TRANSACTION_ERROR = "TRANSACTION_ERROR";
    public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    public static final String TIMEOUT_ERROR = "TIMEOUT_ERROR";
    public static final String CONNECTION_REFUSED = "CONNECTION_REFUSED";
    
    // Private constructor to prevent instantiation
    private ErrorCodes() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}