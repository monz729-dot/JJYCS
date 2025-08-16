package com.ycs.lms.exception;

/**
 * 비즈니스 룰 위반 예외
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    public BusinessRuleViolationException(String message) {
        super(message);
    }
    
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
} 