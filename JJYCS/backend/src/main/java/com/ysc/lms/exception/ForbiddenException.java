package com.ysc.lms.exception;

/**
 * 403 Forbidden 예외
 */
public class ForbiddenException extends RuntimeException {
    
    private final String errorCode;
    
    public ForbiddenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ForbiddenException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}