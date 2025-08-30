package com.ysc.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 서비스 (이메일, SMS, 푸시 알림 등)
 * 현재는 스텁 구현, 실제로는 이메일/SMS 서비스 연동 필요
 */
@Service
@Slf4j
public class NotificationService {
    
    /**
     * 이메일 알림 발송
     */
    public void sendEmailNotification(String to, String subject, String content) {
        // TODO: 실제 이메일 발송 로직 구현
        log.info("Sending email to: {} with subject: {}", to, subject);
    }
    
    /**
     * SMS 알림 발송
     */
    public void sendSmsNotification(String phoneNumber, String message) {
        // TODO: 실제 SMS 발송 로직 구현
        log.info("Sending SMS to: {} with message: {}", phoneNumber, message);
    }
    
    /**
     * 푸시 알림 발송
     */
    public void sendPushNotification(String userId, String title, String body) {
        // TODO: 실제 푸시 알림 발송 로직 구현
        log.info("Sending push notification to user: {} with title: {}", userId, title);
    }
}