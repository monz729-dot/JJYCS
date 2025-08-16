package com.ycs.lms.exception;

/**
 * 주문이 수정 불가능한 상태일 때 발생하는 예외
 */
public class OrderNotModifiableException extends RuntimeException {
    
    public OrderNotModifiableException(String message) {
        super(message);
    }
    
    public OrderNotModifiableException(String message, Throwable cause) {
        super(message, cause);
    }
} 