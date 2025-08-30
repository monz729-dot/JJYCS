package com.ysc.lms.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String errorCode;
    private String traceId;
    
    // Additional error details
    private Map<String, String> validationErrors;
    private List<String> stackTrace;
    private ServiceError serviceError;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceError {
        private String serviceName;
        private String operation;
        private String errorMessage;
        private boolean retryable;
        private int retryAfterSeconds;
    }
}