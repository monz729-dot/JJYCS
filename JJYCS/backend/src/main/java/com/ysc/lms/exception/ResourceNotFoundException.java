package com.ysc.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s를 찾을 수 없습니다. ID: %s", resourceType, resourceId));
    }
    
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}