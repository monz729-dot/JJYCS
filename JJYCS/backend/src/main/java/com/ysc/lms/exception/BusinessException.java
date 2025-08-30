package com.ysc.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 관련 예외
 */
public class BusinessException extends YscLmsException {

    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String errorCode, String message, Object... params) {
        super(errorCode, message, HttpStatus.BAD_REQUEST, params);
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, HttpStatus.BAD_REQUEST, cause);
    }

    // 비즈니스 규칙 관련 예외들
    public static class CbmThresholdExceededException extends BusinessException {
        public CbmThresholdExceededException(String orderNumber, double cbm) {
            super("CBM_THRESHOLD_EXCEEDED", 
                  String.format("주문 %s의 CBM(%.3fm³)이 임계값(29m³)을 초과했습니다.", orderNumber, cbm),
                  orderNumber, cbm);
        }
    }

    public static class ThbThresholdExceededException extends BusinessException {
        public ThbThresholdExceededException(String orderNumber, double amount) {
            super("THB_THRESHOLD_EXCEEDED", 
                  String.format("주문 %s의 총액(%.2fTHB)이 임계값(1500THB)을 초과했습니다.", orderNumber, amount),
                  orderNumber, amount);
        }
    }

    public static class MemberCodeRequiredException extends BusinessException {
        public MemberCodeRequiredException(String orderNumber) {
            super("MEMBER_CODE_REQUIRED", 
                  String.format("주문 %s의 처리를 위해 회원코드가 필요합니다.", orderNumber),
                  orderNumber);
        }
    }

    public static class InvalidOrderStatusException extends BusinessException {
        public InvalidOrderStatusException(String orderNumber, String currentStatus, String requestedStatus) {
            super("INVALID_ORDER_STATUS", 
                  String.format("주문 %s의 상태를 %s에서 %s로 변경할 수 없습니다.", orderNumber, currentStatus, requestedStatus),
                  orderNumber, currentStatus, requestedStatus);
        }
    }

    public static class FileUploadException extends BusinessException {
        public FileUploadException(String fileName, String reason) {
            super("FILE_UPLOAD_FAILED", 
                  String.format("파일 '%s' 업로드에 실패했습니다: %s", fileName, reason),
                  fileName, reason);
        }
    }

    public static class ExternalApiException extends BusinessException {
        public ExternalApiException(String apiName, String errorMessage) {
            super("EXTERNAL_API_ERROR", 
                  String.format("%s API 호출에 실패했습니다: %s", apiName, errorMessage),
                  apiName, errorMessage);
        }
    }
}