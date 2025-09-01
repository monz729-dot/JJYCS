package com.ysc.lms.service;

import com.ysc.lms.entity.Notification;
import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.NotificationRepository;
import com.ysc.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 통합 알림 서비스 (이메일, DB 알림, SMS, 푸시 알림 등)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    /**
     * 주문 상태 변경 통합 알림
     */
    @Async
    public void notifyOrderStatusChange(Order order, String oldStatus, String newStatus) {
        try {
            log.info("Sending order status change notification for order: {} ({} -> {})", 
                order.getOrderNumber(), oldStatus, newStatus);
            
            // 사용자 조회
            User user = order.getCreatedBy() != null ? order.getCreatedBy() : order.getUser();
            if (user == null) {
                log.warn("User not found for order: {}", order.getOrderNumber());
                return;
            }
            
            String statusMessage = getOrderStatusMessage(newStatus);
            
            // 1. 데이터베이스 알림 생성
            Notification dbNotification = new Notification(
                user,
                Notification.NotificationType.ORDER_STATUS_CHANGED,
                "주문 상태 변경: " + order.getOrderNumber(),
                String.format("주문 %s의 상태가 '%s'로 변경되었습니다.", 
                    order.getOrderNumber(), statusMessage)
            );
            notificationRepository.save(dbNotification);
            
            // 2. 이메일 알림 발송 (중요한 상태 변경만)
            if (shouldSendEmailForStatus(newStatus)) {
                try {
                    sendOrderStatusEmail(user.getEmail(), user.getName(), order.getOrderNumber(), 
                                       oldStatus, newStatus);
                } catch (Exception e) {
                    log.error("Failed to send order status email for order: {}", order.getOrderNumber(), e);
                }
            }
            
            log.info("Order status change notification completed for order: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Error sending order status change notification for order: {}", 
                order.getOrderNumber(), e);
        }
    }
    
    /**
     * 사용자 승인 상태 변경 통합 알림
     */
    @Async
    public void notifyUserApprovalStatusChange(User user, boolean approved, String reason) {
        try {
            log.info("Sending user approval status notification for user: {} (approved: {})", 
                user.getEmail(), approved);
            
            // 1. 데이터베이스 알림 생성
            String title = approved ? "계정 승인 완료" : "계정 승인 보류";
            String message = approved ? 
                "축하합니다! 계정이 승인되었습니다. 이제 모든 서비스를 이용하실 수 있습니다." :
                "계정 승인이 보류되었습니다. 사유: " + (reason != null ? reason : "서류 검토 필요");
            
            Notification.NotificationType type = approved ? 
                Notification.NotificationType.USER_APPROVED : 
                Notification.NotificationType.USER_REJECTED;
                
            Notification dbNotification = new Notification(user, type, title, message);
            notificationRepository.save(dbNotification);
            
            // 2. 이메일 알림 발송
            try {
                if (approved) {
                    emailService.sendApprovalEmail(user.getEmail(), user.getName(), user.getUserType().toString());
                } else {
                    emailService.sendRejectionEmail(user.getEmail(), user.getName(), 
                        user.getUserType().toString(), reason);
                }
            } catch (Exception e) {
                log.error("Failed to send approval/rejection email for user: {}", user.getEmail(), e);
            }
            
            log.info("User approval status notification completed for user: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Error sending user approval status notification for user: {}", user.getEmail(), e);
        }
    }
    
    /**
     * 시스템 공지 브로드캐스트 알림
     */
    @Async
    public void broadcastSystemNotification(String title, String message, List<User> targetUsers) {
        try {
            log.info("Broadcasting system notification to {} users", targetUsers.size());
            
            int emailCount = 0;
            int dbNotificationCount = 0;
            
            for (User user : targetUsers) {
                try {
                    // 1. 데이터베이스 알림 생성
                    Notification dbNotification = new Notification(
                        user, 
                        Notification.NotificationType.SYSTEM_ANNOUNCEMENT, 
                        title, 
                        message
                    );
                    notificationRepository.save(dbNotification);
                    dbNotificationCount++;
                    
                    // 2. 중요한 공지만 이메일 발송 (제목에 '긴급' 또는 '중요' 포함)
                    if (title.contains("긴급") || title.contains("중요") || title.contains("URGENT")) {
                        try {
                            sendSystemNotificationEmail(user.getEmail(), user.getName(), title, message);
                            emailCount++;
                        } catch (Exception e) {
                            log.error("Failed to send system notification email to: {}", user.getEmail(), e);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to create notification for user: {}", user.getEmail(), e);
                }
            }
            
            log.info("System notification broadcast completed - DB notifications: {}, Emails: {}", 
                dbNotificationCount, emailCount);
            
        } catch (Exception e) {
            log.error("Error broadcasting system notification", e);
        }
    }
    
    /**
     * 이메일 알림만 발송 (기존 호환성)
     */
    public void sendEmailNotification(String to, String subject, String content) {
        try {
            emailService.sendSimpleEmail(to, subject, content);
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", to, e);
        }
    }
    
    /**
     * SMS 알림 발송 (스텁)
     */
    public void sendSmsNotification(String phoneNumber, String message) {
        // TODO: 실제 SMS 발송 로직 구현 (네이버, AWS SNS 등)
        log.info("SMS notification (stub): {} - {}", phoneNumber, message);
    }
    
    /**
     * 푸시 알림 발송 (스텁)
     */
    public void sendPushNotification(String userId, String title, String body) {
        // TODO: 실제 푸시 알림 발송 로직 구현 (FCM 등)
        log.info("Push notification (stub): {} - {} - {}", userId, title, body);
    }
    
    /**
     * 주문 상태별 이메일 발송 여부 결정
     */
    private boolean shouldSendEmailForStatus(String status) {
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> true;  // 접수 완료
            case "SHIPPED" -> true;    // 배송 시작
            case "DELIVERED" -> true;  // 배송 완료
            case "CANCELLED" -> true;  // 취소
            case "HOLD" -> true;       // 보류 (문제 발생)
            default -> false;
        };
    }
    
    /**
     * 주문 상태 메시지 반환
     */
    private String getOrderStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "접수 대기";
            case "CONFIRMED" -> "접수 완료";
            case "IN_WAREHOUSE" -> "창고 입고";
            case "PROCESSING" -> "처리 중";
            case "SHIPPED" -> "배송 중";
            case "DELIVERED" -> "배송 완료";
            case "CANCELLED" -> "취소됨";
            case "HOLD" -> "보류";
            default -> status;
        };
    }
    
    /**
     * 주문 상태 변경 이메일 발송
     */
    private void sendOrderStatusEmail(String to, String name, String orderNumber, 
                                    String oldStatus, String newStatus) {
        try {
            String subject = "[YCS LMS] 주문 상태 변경 알림 - " + orderNumber;
            String statusMessage = getOrderStatusMessage(newStatus);
            
            String content = String.format("""
                안녕하세요, %s님!
                
                주문 %s의 상태가 업데이트되었습니다.
                
                현재 상태: %s
                
                주문 상세 내용은 웹사이트에서 확인하실 수 있습니다.
                
                감사합니다.
                YCS 물류관리시스템
                """, name, orderNumber, statusMessage);
            
            emailService.sendSimpleEmail(to, subject, content);
            
        } catch (Exception e) {
            log.error("Failed to send order status change email", e);
            throw e;
        }
    }
    
    /**
     * 시스템 공지 이메일 발송
     */
    private void sendSystemNotificationEmail(String to, String name, String title, String message) {
        try {
            String subject = "[YCS LMS] 시스템 공지: " + title;
            
            String content = String.format("""
                안녕하세요, %s님!
                
                📢 %s
                
                %s
                
                더 자세한 내용은 웹사이트를 확인해주세요.
                
                감사합니다.
                YCS 물류관리시스템
                """, name, title, message);
            
            emailService.sendSimpleEmail(to, subject, content);
            
        } catch (Exception e) {
            log.error("Failed to send system notification email", e);
            throw e;
        }
    }
}