package com.ycs.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * 접근 권한이 없을 때 발생하는 예외
 */
public class AccessDeniedException extends YcsLmsException {

    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", message, HttpStatus.FORBIDDEN);
    }

    public AccessDeniedException(String resource, String action) {
        super("ACCESS_DENIED", 
              String.format("%s에 대한 %s 권한이 없습니다.", resource, action), 
              HttpStatus.FORBIDDEN,
              resource, action);
    }

    // 특화된 접근 거부 예외들
    public static class InsufficientRoleException extends AccessDeniedException {
        public InsufficientRoleException(String requiredRole, String currentRole) {
            super(String.format("필요한 권한: %s, 현재 권한: %s", requiredRole, currentRole));
        }
    }

    public static class OrderAccessDeniedException extends AccessDeniedException {
        public OrderAccessDeniedException(String orderNumber, String userId) {
            super(String.format("사용자 '%s'는 주문 '%s'에 접근할 권한이 없습니다.", userId, orderNumber));
        }
    }

    public static class FileAccessDeniedException extends AccessDeniedException {
        public FileAccessDeniedException(String fileName, String userId) {
            super(String.format("사용자 '%s'는 파일 '%s'에 접근할 권한이 없습니다.", userId, fileName));
        }
    }

    public static class AdminOnlyException extends AccessDeniedException {
        public AdminOnlyException(String action) {
            super(String.format("'%s' 작업은 관리자만 수행할 수 있습니다.", action));
        }
    }

    public static class AccountNotActivatedException extends AccessDeniedException {
        public AccountNotActivatedException() {
            super("계정이 활성화되지 않았습니다. 관리자의 승인을 기다려주세요.");
        }
    }
}