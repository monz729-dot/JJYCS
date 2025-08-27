package com.ycs.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * YCS LMS 시스템의 기본 예외 클래스
 */
public class YcsLmsException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object[] params;

    public YcsLmsException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public YcsLmsException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.params = null;
    }

    public YcsLmsException(String errorCode, String message, HttpStatus httpStatus, Object... params) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.params = params;
    }

    public YcsLmsException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public YcsLmsException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.params = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object[] getParams() {
        return params;
    }
}