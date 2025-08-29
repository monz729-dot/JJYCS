package com.ycs.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * 외부 API 연동 실패 시 발생하는 예외
 */
public class ExternalApiException extends RuntimeException {
    
    private final String apiName;
    private final HttpStatus statusCode;
    
    public ExternalApiException(String apiName, String message) {
        super(String.format("[%s] %s", apiName, message));
        this.apiName = apiName;
        this.statusCode = HttpStatus.SERVICE_UNAVAILABLE;
    }
    
    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(String.format("[%s] %s", apiName, message), cause);
        this.apiName = apiName;
        this.statusCode = HttpStatus.SERVICE_UNAVAILABLE;
    }
    
    public ExternalApiException(String apiName, String message, HttpStatus statusCode) {
        super(String.format("[%s] %s", apiName, message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }
    
    public String getApiName() {
        return apiName;
    }
    
    public HttpStatus getHttpStatus() {
        return statusCode;
    }
}