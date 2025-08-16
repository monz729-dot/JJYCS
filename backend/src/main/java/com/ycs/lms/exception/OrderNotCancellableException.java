package com.ycs.lms.exception;

/**
 * 주문이 취소 불가능한 상태일 때 발생하는 예외
 */
public class OrderNotCancellableException extends RuntimeException {
    
    public OrderNotCancellableException(String message) {
        super(message);
    }
    
    public OrderNotCancellableException(String message, Throwable cause) {
        super(message, cause);
    }
} 