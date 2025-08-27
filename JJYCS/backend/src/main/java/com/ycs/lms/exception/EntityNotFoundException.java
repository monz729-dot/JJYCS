package com.ycs.lms.exception;

import org.springframework.http.HttpStatus;

/**
 * 엔티티를 찾을 수 없을 때 발생하는 예외
 */
public class EntityNotFoundException extends YcsLmsException {

    public EntityNotFoundException(String entityType, Object id) {
        super("ENTITY_NOT_FOUND", 
              String.format("%s(ID: %s)을 찾을 수 없습니다.", entityType, id), 
              HttpStatus.NOT_FOUND,
              entityType, id);
    }

    public EntityNotFoundException(String entityType, String field, Object value) {
        super("ENTITY_NOT_FOUND", 
              String.format("%s(%s: %s)을 찾을 수 없습니다.", entityType, field, value), 
              HttpStatus.NOT_FOUND,
              entityType, field, value);
    }

    // 특화된 예외들
    public static class UserNotFoundException extends EntityNotFoundException {
        public UserNotFoundException(Long userId) {
            super("사용자", userId);
        }
        
        public UserNotFoundException(String email) {
            super("사용자", "email", email);
        }
    }

    public static class OrderNotFoundException extends EntityNotFoundException {
        public OrderNotFoundException(Long orderId) {
            super("주문", orderId);
        }
        
        public OrderNotFoundException(String orderNumber) {
            super("주문", "orderNumber", orderNumber);
        }
    }

    public static class FileNotFoundException extends EntityNotFoundException {
        public FileNotFoundException(Long fileId) {
            super("파일", fileId);
        }
    }

    public static class NotificationNotFoundException extends EntityNotFoundException {
        public NotificationNotFoundException(Long notificationId) {
            super("알림", notificationId);
        }
    }
}