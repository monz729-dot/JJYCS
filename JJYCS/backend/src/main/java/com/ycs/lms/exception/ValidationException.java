package com.ycs.lms.exception;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * 검증 실패 시 발생하는 예외
 */
public class ValidationException extends YcsLmsException {

    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super("VALIDATION_FAILED", message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = null;
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super("VALIDATION_FAILED", message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    // 특화된 검증 예외들
    public static class InvalidEmailFormatException extends ValidationException {
        public InvalidEmailFormatException(String email) {
            super(String.format("이메일 형식이 올바르지 않습니다: %s", email));
        }
    }

    public static class InvalidPhoneFormatException extends ValidationException {
        public InvalidPhoneFormatException(String phone) {
            super(String.format("전화번호 형식이 올바르지 않습니다: %s", phone));
        }
    }

    public static class InvalidHsCodeException extends ValidationException {
        public InvalidHsCodeException(String hsCode) {
            super(String.format("유효하지 않은 HS 코드입니다: %s", hsCode));
        }
    }

    public static class InvalidFileTypeException extends ValidationException {
        public InvalidFileTypeException(String fileName, String fileType, List<String> allowedTypes) {
            super(String.format("파일 '%s'의 형식(%s)은 허용되지 않습니다. 허용되는 형식: %s", 
                  fileName, fileType, String.join(", ", allowedTypes)));
        }
    }

    public static class FileSizeExceededException extends ValidationException {
        public FileSizeExceededException(String fileName, long fileSize, long maxSize) {
            super(String.format("파일 '%s'의 크기(%dMB)가 최대 허용 크기(%dMB)를 초과했습니다.", 
                  fileName, fileSize / (1024 * 1024), maxSize / (1024 * 1024)));
        }
    }

    public static class DuplicateEntityException extends ValidationException {
        public DuplicateEntityException(String entityType, String field, Object value) {
            super(String.format("이미 존재하는 %s입니다. %s: %s", entityType, field, value));
        }
    }
}